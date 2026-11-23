from datetime import datetime
from security_core import SecurityManager

class CasierManager(SecurityManager):
    def remove_stock(self, barcode, qty):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        produk = self.product.find_one({"barcode": clean_barcode})
        if not produk or not self.verify_product_integrity(produk):
            raise ValueError("SECURITY ALERT: Product data integrity failed verification!")

        stok_baru = int(produk["stock"]) - int(qty)
        payload = f"{clean_barcode}|{produk['product_name']}|{produk['price']}|{stok_baru}"
        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": stok_baru, "_sig": self.generate_signature(payload)}}
        )

    def verification_delete(self, card, barcode, qty):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        owner = self.users.find_one({"role": "OWNER"})
        if not owner or not self.verify_credential(card, owner["manager_card_hash"]):
            self.log_event("UNAUTHORIZED_DELETE_ATTEMPT", f"Attempt to delete {clean_barcode} using Card Manager is invalid!")
            return False

        produk = self.product.find_one({"barcode": clean_barcode})
        stok_baru = int(produk["stock"]) + int(qty)
        payload = f"{clean_barcode}|{produk['product_name']}|{produk['price']}|{stok_baru}"
        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": stok_baru, "_sig": self.generate_signature(payload)}}
        )
        self.log_event("ITEM_DELETED", f"Item {clean_barcode} (qty {qty}) was removed with Manager authorization.")
        return True

    def transaction(self, items, total, buy, change):
        time = datetime.now().isoformat()
        sig = self.generate_signature(f"{time}|{total}|{buy}|{change}|{len(items)}")
        self.transaction.insert_one({
            "timestamp": time,
            "items": items,
            "total_amount": int(total),
            "cash_paid": int(buy),
            "change": int(change),
            "_sig": sig
        })
