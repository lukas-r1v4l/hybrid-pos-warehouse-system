from security_core import SecurityManager

class WarehouseManager(SecurityManager):
    def restock(self, barcode, qty):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        product = self.product.find_one({"barcode": clean_barcode})
        if not product:
            raise ValueError("Product not found!")
        if not self.verify_product_integrity(product):
            raise ValueError("SECURITY ALERT: Product data was illegally tampered with outside the system!")

        new_stock = int(product["stock"]) + int(qty)
        payload = f"{clean_barcode}|{product['product_name']}|{product['price']}|{new_stock}"
        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": new_stock, "_sig": self.generate_signature(payload)}}
        )

    def product_save(self, barcode, name, price, stock):
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
