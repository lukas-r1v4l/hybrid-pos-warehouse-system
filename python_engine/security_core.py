import os
import hmac
import uuid
import socket
import hashlib
import secrets
import platform
from datetime import datetime
from db_config import DatabaseManager


class SecurityManager(DatabaseManager):
    def __init__(self):
        super().__init__()
        self.__server_secret = os.getenv("SERVER_SECRET_KEY", "").encode("utf-8")

    @staticmethod
    def get_current_ip():
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
                s.connect(("8.8.8.8", 80))
                return s.getsockname()[0]
        except Exception:
            return "127.0.0.1"

    @staticmethod
    def get_current_device_id():
        try:
            import winreg
            with winreg.OpenKey(
                winreg.HKEY_LOCAL_MACHINE,
                r"SOFTWARE\Microsoft\Cryptography"
            ) as key:
                guid, _ = winreg.QueryValueEx(key, "MachineGuid")
                return str(guid)
        except Exception:
            return f"{platform.node()}-{hex(uuid.getnode())}"

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

    def owner_signup(self, username, password, manager_card):
        clean_user = self.sanitize_input(username, r"^[A-Za-z0-9_]{3,20}$")
        if len(password) < 8 or len(manager_card) < 4:
            raise ValueError("Password must be at least 8 characters and Manager Card at least 4 characters!")

        if self.users.find_one({"username": clean_user}):
            raise ValueError(f"Username '{clean_user}' is already taken!")
        ip_addr = self.get_current_ip()
        device_id = self.get_current_device_id()
        self.users.insert_one({
            "username": clean_user,
            "password_hash": self.hash_credential(password),
            "manager_card_hash": self.hash_credential(manager_card),
            "role": "OWNER",
            "registered_ip": ip_addr,
            "device_id": device_id,
            "created_at": datetime.now()
        })
        self.authorized_devices.update_one(
            {"device_id": device_id},
            {"$set": {
                "ip_address": ip_addr,
                "device_name": os.environ.get("COMPUTERNAME", "OWNER-PC"),
                "status": "AUTHORIZED",
                "approved_by": "OWNER_SELF_REGISTER",
                "updated_at": datetime.now()
            }},
            upsert=True
        )
        self.log_event("OWNER_REGISTERED", f"New Owner '{clean_user}' registered from IP {ip_addr}.")
        return True

    def owner_delete(self, username, manager_card, password):
        clean_user = self.sanitize_input(username, r"^[A-Za-z0-9_]{3,20}$")
        owner = self.users.find_one({"username": clean_user, "role": "OWNER"})
        if not owner:
            raise ValueError("Owner account not found!")
        valid_card = self.verify_credential(manager_card, owner["manager_card_hash"])
        valid_pass = self.verify_credential(password, owner["password_hash"])
        if not (valid_card and valid_pass):
            self.log_event(
                "UNAUTHORIZED_OWNER_DELETE",
                f"Failed deletion verification for Owner '{clean_user}' from IP {self.get_current_ip()}!"
            )
            return False
        self.users.delete_one({"_id": owner["_id"]})
        self.log_event("OWNER_DELETED", f"Owner account '{clean_user}' has been deleted after valid verification.")
        return True

    def employee_signup(self, username, password):
        clean_user = self.sanitize_input(username, r"^[A-Za-z0-9_]{3,20}$")
        if len(password) < 6:
            raise ValueError("Employee password must be at least 6 characters long!")
        if self.users.find_one({"username": clean_user}):
            raise ValueError(f"Username '{clean_user}' is already registered! Please Log In.")
        ip_addr = self.get_current_ip()
        device_id = self.get_current_device_id()
        self.users.insert_one({
            "username": clean_user,
            "password_hash": self.hash_credential(password),
            "role": "EMPLOYEE",
            "registered_ip": ip_addr,
            "device_id": device_id,
            "created_at": datetime.now()
        })
        existing_dev = self.authorized_devices.find_one({"device_id": device_id, "ip_address": ip_addr})
        if existing_dev and existing_dev.get("status") == "AUTHORIZED":
            self.log_event("EMPLOYEE_REGISTERED", f"Employee '{clean_user}' registered on authorized IP ({ip_addr}).")
            return "OK_AUTHORIZED"
        else:
            self.authorized_devices.update_one(
                {"device_id": device_id},
                {"$set": {
                    "ip_address": ip_addr,
                    "device_name": os.environ.get("COMPUTERNAME", "EMPLOYEE-PC"),
                    "requested_by": clean_user,
                    "status": "PENDING_IT_APPROVAL",
                    "requested_at": datetime.now()
                }},
                upsert=True
            )
            self.log_event(
                "PENDING_IP_APPROVAL",
                f"Employee '{clean_user}' signed up from new IP ({ip_addr}). Awaiting IT approval."
            )
            return f"PENDING_IT|{ip_addr}"

    def authentication_user(self, username, password):
        clean_user = self.sanitize_input(username, r"^[A-Za-z0-9_]{3,20}$")
        ip_addr = self.get_current_ip()
        device_id = self.get_current_device_id()
        user_doc = self.users.find_one({"username": clean_user})
        if not user_doc:
            self.log_event(
                "UNREGISTERED_LOGIN",
                f"Unregistered username '{clean_user}' attempted login from IP {ip_addr}."
            )
            return "NOT_REGISTERED"
        device = self.authorized_devices.find_one({"device_id": device_id, "ip_address": ip_addr})
        if not device or device.get("status") != "AUTHORIZED":
            self.log_event(
                "IP_NOT_APPROVED",
                f"Access denied for '{clean_user}': IP {ip_addr} is not approved by IT!"
            )
            return f"IP_NOT_APPROVED|{ip_addr}"
        if user_doc.get("failed_attempts", 0) >= 5:
            return "LOCKED"
        if self.verify_credential(password, user_doc["password_hash"]):
            self.users.update_one({"username": clean_user}, {"$set": {"failed_attempts": 0}})
            return f"OK|{user_doc.get('role', 'EMPLOYEE')}"
        else:
            attempts = user_doc.get("failed_attempts", 0) + 1
            self.users.update_one({"username": clean_user}, {"$set": {"failed_attempts": attempts}})
            self.log_event(
                "FAILED_LOGIN",
                f"Wrong password attempt #{attempts} for '{clean_user}' from IP {ip_addr}."
            )
            return "LOCKED" if attempts >= 5 else "WRONG_PASSWORD"

    def it_approve_ip(self, target_ip):
        result = self.authorized_devices.update_many(
            {"ip_address": target_ip},
            {"$set": {
                "status": "AUTHORIZED",
                "approved_by": "IT_ADMIN",
                "approved_at": datetime.now()
            }}
        )
        if result.matched_count == 0:
            raise ValueError(f"IP Address '{target_ip}' not found in the device queue!")
        self.log_event("IT_IP_APPROVED", f"IT Administrator approved server access for IP {target_ip}.")
        return True

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

    def generate_signature(self, payload_string):
        if not self.__server_secret:
            return "UNSEALED"
        return hmac.new(self.__server_secret, payload_string.encode("utf-8"), hashlib.sha256).hexdigest()

    def verify_product_integrity(self, product_doc):
        if not self.__server_secret:
            return True
        if "_sig" not in product_doc:
            self.log_event(
                "DATA_TAMPERING_DETECTED",
                f"CRITICAL: Signature missing for product {product_doc.get('barcode', 'UNKNOWN')}!"
            )
            return False

        payload = f"{product_doc['barcode']}|{product_doc['product_name']}|{product_doc['price']}|{product_doc['stock']}"
        is_valid = hmac.compare_digest(self.generate_signature(payload), product_doc["_sig"])
        if not is_valid:
            self.log_event(
                "DATA_TAMPERING_DETECTED",
                f"CRITICAL: Product {product_doc['barcode']} was illegally modified outside the application!"
            )
        return is_valid

    def audit_server_security(self):
        warnings = []
        mongo_url = os.getenv("MONGO_URL", "")
        if "@" not in mongo_url:
            warnings.append("Database server is not using Username & Password authentication.")
        if not self.__server_secret:
            warnings.append("SERVER_SECRET_KEY is not configured in .env.")
        return "SAFE" if not warnings else "WARNING|" + " ; ".join(warnings)

    def log_event(self, event_type, detail):
        self.security_logs.insert_one({
            "timestamp": datetime.now(),
            "event": event_type,
            "ip_address": self.get_current_ip(),
            "device_id": self.get_current_device_id(),
            "detail": detail
        })

    @staticmethod
    def verify_it_credentials(it_user, it_pass):
        from pymongo import MongoClient
        from urllib.parse import urlparse, quote_plus
        try:
            raw_url = os.getenv("MONGO_URL", "mongodb://127.0.0.1:27017/?authSource=admin")
            parsed = urlparse(raw_url)
            host = parsed.hostname or "127.0.0.1"
            port = parsed.port or 27017
            query = f"?{parsed.query}" if parsed.query else "?authSource=admin"

            test_uri = f"mongodb://{quote_plus(it_user)}:{quote_plus(it_pass)}@{host}:{port}/{query}"
            test_client = MongoClient(test_uri, serverSelectionTimeoutMS=3000)
            test_client.admin.command("ping")
            test_client.close()
            return "OK"
        except Exception as e:
            return f"FAIL|{str(e)}"
    def get_all_devices(self):
        devices = list(self.authorized_devices.find({}, {"_id": 0}))
        lines = []
        for d in devices:
            lines.append(
                f"{d.get('ip_address', '-')}|{d.get('device_id', '-')}|{d.get('device_name', '-')}|{d.get('requested_by', '-')}|{d.get('status', '-')}"
            )
        return "\n".join(lines) if lines else "EMPTY"
    def revoke_device_ip(self, target_ip):
        self.authorized_devices.update_many(
            {"ip_address": target_ip},
            {"$set": {"status": "REVOKED_BY_IT", "revoked_at": datetime.now()}}
        )
        self.log_event("IT_IP_REVOKED", f"IT Administrator revoked server access for IP {target_ip}.")
        return True
    def get_security_logs(self):
        logs = list(self.security_logs.find({}, {"_id": 0}).sort("timestamp", -1).limit(50))
        lines = []
        for l in logs:
            ts = str(l.get("timestamp", ""))[:19]
            lines.append(f"{ts}|{l.get('event', '-')}|{l.get('ip_address', '-')}|{l.get('detail', '-')}")
        return "\n".join(lines) if lines else "EMPTY"