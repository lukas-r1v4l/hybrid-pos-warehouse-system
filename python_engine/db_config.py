from pymongo.errors import ServerSelectionTimeoutError
import os
import re
from pymongo import MongoClient
from dotenv import load_dotenv

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
load_dotenv(os.path.join(BASE_DIR, ".env"))

class DatabaseManager:
    def __init__(self):
        self.__url = os.getenv("MONGO_URL", "mongodb://localhost:27017/")
        self.__db_name = os.getenv("DB_NAME", "toko_stock_db")
        self.__client = MongoClient(self.__url, ServerSelectionTimeoutMS=3000)
        self.__db = self.__client[self.__db_name]
        self.product = self.__db["product"]
        self.transaction = self.__db["transaction"]
        self.users = self.__db["users"]
        self.security_logs = self.__db["security_logs"]
        self.authorized_devices = self.__db["authorized_devices"]

        self._secure_container()

    def _secure_container(self):
        self.product.create_index("barcode", unique=True)
        self.users.create_index("username", unique=True)
        self.authorized_devices.create_index("device_id", unique=True)
        try:
            self.__db.command("collMod", "product", validator={
                "$jsonSchema": {
                    "bsonType": "object",
                    "required": ["barcode", "product_name", "price", "stock"],
                    "properties": {
                        "barcode": {"bsonType": "string"},
                        "product_name": {"bsonType": "string"},
                        "price": {"bsonType": "int", "minimum": 0},
                        "stock": {"bsonType": "int", "minimum": 0}
                    }
                }
            })
        except Exception:
            pass

    @staticmethod
    def sanitize_input(text, pattern=r"^[A-Za-z0-9_\-\s\.]{1,50}$"):
        if not isinstance(text, str) or not re.match(pattern, text):
            raise ValueError("The input contains forbidden characters!")
        return text.strip()

    def product_search(self, barcode):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        return self.product.find_one({"barcode": clean_barcode}, {"_id": 0})
