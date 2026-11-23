import os
import hmac
import uuid
import hashlib
import secrets
import winreg
import platform
from datetime import datetime
from db_config import DatabaseManager

class SecurityManager(DatabaseManager):
    def __init__(self):
        super().__init__()
        self.__server_secret = os.getenv("SERVER_SECRET_KEY", "").encode("utf-8")
    
    @staticmethod
    def hash_credential(plain_text, salt=None):
        if salt is None:
            salt = secrets.token_hex(16)
        dk = hashlib.pbkdf2_hmac(
            "sha256",
            plain_text.encode("utf-8"),
            salt.encode("utf-8"),
            100_000
        )
        return f"{salt}${dk.hex()}"

    @staticmethod
    def verify_credential(plain_text, stored_hash):
        if not stored_hash or "$" not in stored_hash:
            return False
        salt, _ = stored_hash.split("$", 1)
        return hmac.compare_digest(
            SecurityManager.hash_credential(plain_text, salt),
            stored_hash
        )

    def setup_complete(self):
        return self.users.count_documents({}) > 0

    def create_initial_owner(self, username, password, manager_card):
        if self.setup_complete():
            raise ValueError("The system already has an Owner! Initial registration is locked.")
        clean_user = self.sanitize_input(username, r"^[A-Za-z0-9_]{3,20}$")
        if len(password) < 8 or len(manager_card) < 4:
            raise ValueError("Password must be at least 8 characters long, and Card Manager must be at least 4 characters long!")
        
        device_id = self.get_current_device_id()
        self.users.insert_one({
            "username": clean_user,
            "password_hash": self.hash_credential(password),
            "manager_card_hash": self.hash_credential(manager_card),
            "role": "OWNER",
            "created_at": datetime.now()
        })
        self.authorized_devices.insert_one({
            "device_id": device_id,
            "device_name": os.environ.get("COMPUTERNAME", "PRIMARY-TERMINAL"),
            "status": "AUTHORIZED",
            "registered_at": datetime.now()
        })
        self.log_event("SYSTEM_INITIALIZED", f"Owner '{clean_user}' registered the system from device '{device_id}'.")
        return True

    def authentication_user(self, username, password):
        clean_user = self.sanitize_input(username, r"^[A-Za-z0-9_]{3,20}$")
        device_id = self.get_current_device_id()
        device = self.authorized_devices.find_one({"device_id": device_id, "status": "AUTHORIZED"})
        if not device:
            self.log_event("UNAUTHORIZED_DEVICE", f"Foreign device ({device_id}) tried to log in as '{clean_user}'!")
            return "DEVICE_BLOCKED"

        user_doc = self.users.find_one({"username": clean_user})
        if not user_doc:
            self.log_event("FAILED_LOGIN", f"Login attempt with an unknown username: '{clean_user}' from {device_id}")
            return "INVALID"
        if user_doc.get("failed_attempts", 0) >= 5:
            return "LOCKED"
        if self.verify_credential(password, user_doc["password_hash"]):
            self.users.update_one({"username": clean_user}, {"$set": {"failed_attempts": 0}})
            return "OK"
        else:
            attempts = user_doc.get("failed_attempts", 0) + 1
            self.users.update_one({"username": clean_user}, {"$set": {"failed_attempts": attempts}})
            self.log_event("FAILED_LOGIN", f"Incorrect password (attempt {attempts}) for '{clean_user}' from {device_id}")
            return "LOCKED" if attempts >= 5 else "INVALID"

    def generate_signature(self, payload_string):
        if not self.__server_secret:
            return "UNSEALED"
        return hmac.new(
            self.__server_secret,
            payload_string.encode("utf-8"),
            hashlib.sha256
        ).hexdigest()

    def verify_product_integrity(self, product_doc):
        if not self.__server_secret or "_sig" not in product_doc:
            return True
        payload = f"{product_doc['barcode']}|{product_doc['product_name']}|{product_doc['price']}|{product_doc['stock']}"
        expected_sig = self.generate_signature(payload)
        is_valid = hmac.compare_digest(expected_sig, product_doc["_sig"])
        if not is_valid:
            self.log_event("DATA_TAMPERING_DETECTED", f"CRITICAL WARNING: Product data for {product_doc['barcode']} has been illegally modified outside of the application!")
        return is_valid

    def audit_server_security(self):
        warnings = []
        mongo_url = os.getenv("MONGO_URL", "")
        if "@" not in mongo_url:
            warnings.append("The database server does not use username and password authentication.")
        if "localhost" not in mongo_url and "127.0.0.1" not in mongo_url and "tls=true" not in mongo_url.lower() and "mongodb+srv" not in mongo_url.lower():
            warnings.append("The connection to the remote server is unencrypted (TLS/SSL is disabled).")
        if not self.__server_secret:
            warnings.append("SERVER_SECRET_KEY has not been set in .env (data anti-tampering seal is disabled).")
        recent_alerts = self.security_logs.count_documents({
            "event": {"$in": ["DATA_TAMPERING_DETECTED", "UNAUTHORIZED_DEVICE", "UNAUTHORIZED_DELETE_ATTEMPT"]}
        })
        if recent_alerts > 0:
            warnings.append(f"DETECTED! {recent_alerts} suspicious activities in the server security logs!")
        return "SAFE" if not warnings else "WARNING|" + " ; ".join(warnings)

    @staticmethod
    def get_current_device_id():
        try:
            key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\Microsoft\Cryptography")
            guid, _ = winreg.QueryValueEx(key, "MachineGuid")
            winreg.CloseKey(key)
            return str(guid)
        except Exception:
            return f"{platform.node()}-{hex(uuid.getnode())}"

    def log_event(self, event_type, detail):
        self.security_logs.insert_one({
            "timestamp": datetime.now(),
            "event": event_type,
            "device_id": self.get_current_device_id(),
            "detail": detail
        })

    def create_session_token(self, username):
        token = secrets.token_hex(24)
        self.users.update_one(
            {"username": username},
            {"$set": {"active_token": token, "token_device": self.get_current_device_id()}}
        )
        return token

    def verify_session_token(self, token):
        if not token or len(token) != 48:
            return False
        user = self.users.find_one({
            "active_token": token,
            "token_device": self.get_current_device_id()
        })
        return user is not None
