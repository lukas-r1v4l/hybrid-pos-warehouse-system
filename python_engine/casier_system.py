from datetime import datetime
from security_core import SecurityManager

class CasierManager(SecurityManager):
    def remove_stock(self, barcode, qty):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        product = self.product.find_one({"barcode": clean_barcode})
        if not product or not self.verify_product_integrity(product):
            raise ValueError("SECURITY ALERT: Product integrity verification failed!")

        qty_int = int(qty)
        if qty_int <= 0 or int(product["stock"]) < qty_int:
            raise ValueError("Insufficient stock or invalid quantity!")

        new_stock = int(product["stock"]) - qty_int
        payload = f"{clean_barcode}|{product['product_name']}|{product['price']}|{new_stock}"
        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": new_stock, "_sig": self.generate_signature(payload)}}
        )

    def verification_delete(self, card, barcode, qty):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        owners = list(self.users.find({"role": "OWNER"}))
        is_authorized = any(self.verify_credential(card, o.get("manager_card_hash", "")) for o in owners)

        if not is_authorized:
            self.log_event(
                "UNAUTHORIZED_DELETE_ATTEMPT",
                f"Unauthorized void attempt on barcode '{clean_barcode}' from IP {self.get_current_ip()}!"
            )
            return False

        product = self.product.find_one({"barcode": clean_barcode})
        new_stock = int(product["stock"]) + int(qty)
        payload = f"{clean_barcode}|{product['product_name']}|{product['price']}|{new_stock}"
        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": new_stock, "_sig": self.generate_signature(payload)}}
        )
        self.log_event("ITEM_DELETED", f"Item '{clean_barcode}' (qty {qty}) voided with valid Manager Card.")
        return True

    def transaction(self, items, total, buy, change):
        timestamp = datetime.now().isoformat()
        sig = self.generate_signature(f"{timestamp}|{total}|{buy}|{change}|{len(items)}")
        self.transaction.insert_one({
            "timestamp": timestamp,
            "items": items,
            "total_amount": int(total),
            "cash_paid": int(buy),
            "change": int(change),
            "_sig": sig
        })

    def verify_manager_only(self, card, barcode, qty):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        owners = list(self.users.find({"role": "OWNER"}))
        is_authorized = any(self.verify_credential(card, o.get("manager_card_hash", "")) for o in owners)
        if not is_authorized:
            self.log_event(
                "UNAUTHORIZED_VOID_ATTEMPT",
                f"Unauthorized cart removal attempt for barcode '{clean_barcode}' from IP {self.get_current_ip()}!"
            )
            return False
        self.log_event("CART_ITEM_REMOVED", f"Item '{clean_barcode}' (qty {qty}) removed from customer cart with Manager verification.")
        return True
