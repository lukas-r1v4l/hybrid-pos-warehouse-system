from db_config import DatabaseManager

class WarehouseManager(DatabaseManager):
    def restock(self, barcode, qty):
        self.product.update_one({"barcode": barcode}, {"$inc": {"stock": int(qty)}})
    def product_save(self, barcode, name, price, stock):
        self.product.insert_one({
            "barcode": barcode,
            "product_name": name,
            "price": int(price),
            "stock": int(stock)
        })