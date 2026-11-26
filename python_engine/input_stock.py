from security_core import SecurityManager
from report_manager import ReportManager

class WarehouseManager(SecurityManager):
    def restock(self, barcode, qty, actor="SYSTEM", note="Supplier Restock"):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        product = self.product.find_one({"barcode": clean_barcode})
        if not product:
            raise ValueError("Product not found!")
        if not self.verify_product_integrity(product):
            raise ValueError("SECURITY ALERT: Product data was illegally tampered with outside the system!")

        stock_before = int(product["stock"])
        qty_int = int(qty)
        new_stock = stock_before + qty_int
        payload = f"{clean_barcode}|{product['product_name']}|{product['price']}|{new_stock}"

        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": new_stock, "_sig": self.generate_signature(payload)}}
        )
        ReportManager().log_warehouse_activity(
            actor, "RESTOCK", clean_barcode, product["product_name"], qty_int, stock_before, new_stock, note
        )
        self.log_event("RESTOCK_COMPLETED", f"User '{actor}' added {qty_int} units to '{clean_barcode}'.")

    def product_save(self, barcode, name, price, stock, actor="SYSTEM", note="Initial Stock"):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        clean_name = self.sanitize_input(name)
        price_int = int(price)
        stock_int = int(stock)

        payload = f"{clean_barcode}|{clean_name}|{price_int}|{stock_int}"
        self.product.insert_one({
            "barcode": clean_barcode,
            "product_name": clean_name,
            "price": price_int,
            "stock": stock_int,
            "_sig": self.generate_signature(payload)
        })
        ReportManager().log_warehouse_activity(
            actor, "NEW_PRODUCT", clean_barcode, clean_name, stock_int, 0, stock_int, note
        )
        self.log_event("NEW_PRODUCT_ADDED", f"User '{actor}' registered new product '{clean_name}' ({clean_barcode}).")

    def record_damaged(self, barcode, qty, actor="SYSTEM", note="Damaged Goods"):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        product = self.product.find_one({"barcode": clean_barcode})
        if not product or not self.verify_product_integrity(product):
            raise ValueError("Product not found or integrity check failed!")

        stock_before = int(product["stock"])
        qty_int = int(qty)
        if qty_int <= 0 or stock_before < qty_int:
            raise ValueError("Invalid damaged quantity!")

        new_stock = stock_before - qty_int
        payload = f"{clean_barcode}|{product['product_name']}|{product['price']}|{new_stock}"
        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": new_stock, "_sig": self.generate_signature(payload)}}
        )
        ReportManager().log_warehouse_activity(
            actor, "DAMAGED", clean_barcode, product["product_name"], -qty_int, stock_before, new_stock, note
        )
        self.log_event("DAMAGED_STOCK_RECORDED", f"User '{actor}' recorded {qty_int} damaged units for '{clean_barcode}'.")
