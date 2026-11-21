import os
from pymongo import MongoClient
from dotenv import load_dotenv

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
load_dotenv(os.path.join(BASE_DIR, ".env"))

class DatabaseManager:
    def __init__(self):
        self._url = os.getenv("MONGO_URL", "mongodb://localhost:27017/")
        self._db_name = os.getenv("DB_NAME", "toko_stock_db")
        self.client = MongoClient(self._url)
        self.db = self.client[self._db_name]
        self.product = self.db["product"]
        self.transaction = self.db["transaction"]
        self.product.create_index("barcode", unique=True)

    def product_search(self, barcode):
        return self.product.find_one({"barcode": barcode}, {"_id": 0})
