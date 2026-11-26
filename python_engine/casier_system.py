import re
from datetime import datetime
from security_core import SecurityManager
from report_manager import ReportManager


class CasierManager(SecurityManager):
    def remove_stock(self, barcode, qty, actor="SYSTEM"):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        product = self.product.find_one({"barcode": clean_barcode})
        if not product or not self.verify_product_integrity(product):
            raise ValueError("SECURITY ALERT: Product not found or integrity verification failed!")

        qty_int = int(qty)
        if qty_int <= 0 or int(product["stock"]) < qty_int:
            raise ValueError("Invalid quantity or insufficient stock!")

        new_stock = int(product["stock"]) - qty_int
        payload = f"{clean_barcode}|{product['product_name']}|{product['price']}|{new_stock}"
        self.product.update_one(
            {"barcode": clean_barcode},
            {"$set": {"stock": new_stock, "_sig": self.generate_signature(payload)}}
        )
        self.log_event("STOCK_DEDUCTED", f"User '{actor}' deducted {qty_int} units from '{clean_barcode}' (New stock: {new_stock}).")

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

    def process_transaction(self, items, total, buy, change, actor="UNKNOWN"):
        if not isinstance(items, list) or len(items) == 0:
            raise ValueError("Transaction items cannot be empty!")

        total_int = int(total)
        buy_int = int(buy)
        change_int = int(change)

        if total_int < 0 or buy_int < total_int or change_int != (buy_int - total_int):
            raise ValueError("SECURITY ALERT: Invalid payment or change calculation detected!")

        sanitized_items = []
        calculated_total = 0
        products_to_update = []

        # 1. Validate ALL items & stock availability BEFORE touching database
        for item in items:
            b_code = self.sanitize_input(item.get("barcode", ""), r"^[A-Za-z0-9\-]{1,30}$")
            p_name = self.sanitize_input(item.get("product_name", ""), r"^[A-Za-z0-9\s\-\_\.\(\)]{1,60}$")
            price = int(item.get("price", 0))
            qty = int(item.get("qty", 0))
            subtotal = int(item.get("subtotal", 0))

            if price < 0 or qty <= 0 or subtotal != (price * qty):
                raise ValueError(f"SECURITY ALERT: Tampered price/subtotal on item '{b_code}'!")

            product = self.product.find_one({"barcode": b_code})
            if not product or not self.verify_product_integrity(product):
                raise ValueError(f"SECURITY ALERT: Product '{b_code}' not found or integrity check failed!")

            current_stock = int(product["stock"])
            if current_stock < qty:
                raise ValueError(f"Insufficient stock for '{p_name}' (Available: {current_stock}, Requested: {qty})!")

            calculated_total += subtotal
            sanitized_items.append({
                "barcode": b_code, "product_name": p_name,
                "price": price, "qty": qty, "subtotal": subtotal
            })
            products_to_update.append((b_code, product["product_name"], int(product["price"]), current_stock - qty))

        if calculated_total != total_int:
            raise ValueError("SECURITY ALERT: Total amount mismatch!")

        # 2. Deduct stock in MongoDB ONLY AFTER all validations pass
        for b_code, prod_name, prod_price, new_stock in products_to_update:
            payload = f"{b_code}|{prod_name}|{prod_price}|{new_stock}"
            self.product.update_one(
                {"barcode": b_code},
                {"$set": {"stock": new_stock, "_sig": self.generate_signature(payload)}}
            )

        # 3. Save sealed receipt & export CSV
        now = datetime.now()
        trx_id = f"TRX-{now.strftime('%Y%m%d-%H%M%S')}"
        sig = self.generate_signature(f"{trx_id}|{now.isoformat()}|{actor}|{total_int}|{buy_int}|{change_int}")

        self.transaction.insert_one({
            "trx_id": trx_id,
            "timestamp": now.isoformat(),
            "day": now.strftime("%A"),
            "date": now.strftime("%Y-%m-%d"),
            "cashier": actor,
            "items": sanitized_items,
            "total_amount": total_int,
            "cash_paid": buy_int,
            "change": change_int,
            "_sig": sig
        })

        reporter = ReportManager()
        csv_path = reporter.generate_transaction_receipt_csv(
            trx_id, actor, sanitized_items, total_int, buy_int, change_int, now
        )
        self.log_event("TRANSACTION_COMPLETED", f"User '{actor}' completed {trx_id} (Rp {total_int}).")
        return f"{trx_id}|{now.strftime('%A, %Y-%m-%d %H:%M:%S')}|{actor}|{csv_path}"

    def verify_manager_only(self, card, barcode, qty, actor="UNKNOWN"):
        clean_barcode = self.sanitize_input(barcode, r"^[A-Za-z0-9\-]{1,30}$")
        if not card or len(str(card)) > 64:
            raise ValueError("Invalid Manager ID Card format!")

        owners = list(self.users.find({"role": "OWNER"}))
        is_authorized = any(self.verify_credential(str(card), o.get("manager_card_hash", "")) for o in owners)

        if not is_authorized:
            self.log_event(
                "UNAUTHORIZED_VOID_ATTEMPT",
                f"User '{actor}' failed Manager Card verification while trying to void '{clean_barcode}'!"
            )
            return False

        self.log_event(
            "CART_ITEM_REMOVED",
            f"User '{actor}' removed item '{clean_barcode}' (qty {qty}) from customer cart with valid Manager Card."
        )
        return True
