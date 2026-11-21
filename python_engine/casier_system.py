import os
from datetime import datetime
from db_config import DatabaseManager

class CasierManager(DatabaseManager):
    def __init__(self):
        super().__init__()
        self.__manager_card=os.getenv("MANAGER_CARD", "MGR-01")
    def remove_stock(self, barcode, qty):
        self.product.update_one({"barcode": barcode}, {"$inc": {"stock": -int(qty)}})
    def verification_delete(self, card, barcode, qty):
        if card == self.__manager_card:
            self.product.update_one({"barcode": barcode}, {"$inc": {"stock": int(qty)}})
            return True
        return False
    def transaction(self, items, total, buy, change):
        self.transaction.insert_one({
            "time": datetime.now(),
            "items": items,
            "total": int(total),
            "buy": int(buy),
            "change": int(change)
        })