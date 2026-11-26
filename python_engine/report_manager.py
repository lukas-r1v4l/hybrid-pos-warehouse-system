import os
import csv
from datetime import datetime
from security_core import SecurityManager

class ReportManager(SecurityManager):
    def __init__(self):
        super().__init__()
        self.warehouse_logs = self._DatabaseManager__db["warehouse_logs"]
        self.base_export_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "exports"))
        for sub in ["receipts", "accounting", "warehouse"]:
            os.makedirs(os.path.join(self.base_export_dir, sub), exist_ok=True)

    # --- 1. CUSTOMER RECEIPT CSV (PER TRANSACTION) ---
    def generate_transaction_receipt_csv(self, trx_id, cashier_id, items, total, paid, change, dt_obj=None):
        if dt_obj is None:
            dt_obj = datetime.now()

        day_name = dt_obj.strftime("%A")
        date_str = dt_obj.strftime("%Y-%m-%d")
        time_str = dt_obj.strftime("%H:%M:%S")

        filename = f"Receipt_{trx_id}_{date_str}.csv"
        filepath = os.path.join(self.base_export_dir, "receipts", filename)

        with open(filepath, mode="w", newline="", encoding="utf-8-sig") as f:
            writer = csv.writer(f)
            writer.writerow([
                "Transaction_ID", "Day", "Date", "Time", "Cashier_Username",
                "Barcode", "Product_Name", "Unit_Price", "Qty", "Subtotal",
                "Total_Amount", "Cash_Paid", "Change_Returned"
            ])
            for idx, item in enumerate(items):
                writer.writerow([
                    trx_id, day_name, date_str, time_str, cashier_id,
                    item["barcode"], item["product_name"], item["price"], item["qty"], item["subtotal"],
                    total if idx == 0 else "",
                    paid if idx == 0 else "",
                    change if idx == 0 else ""
                ])
        return filepath

    # --- 2. MONTHLY & YEARLY SALES CSV FOR ACCOUNTANTS ---
    def export_accounting_report(self, mode, period_value):
        """
        mode: 'MONTHLY' (e.g. '2026-11') or 'YEARLY' (e.g. '2026')
        """
        clean_mode = self.sanitize_input(mode, r"^(MONTHLY|YEARLY)$")
        clean_period = self.sanitize_input(period_value, r"^[0-9]{4}(-[0-9]{2})?$")

        all_trx = list(self.transaction.find({}))
        filtered = [t for t in all_trx if str(t.get("timestamp", "")).startswith(clean_period)]

        if not filtered:
            raise ValueError(f"No sales transactions found for period '{clean_period}'!")

        total_trx = len(filtered)
        total_items = sum(
            sum(int(i.get("qty", 0)) for i in t.get("items", []))
            for t in filtered
        )
        gross_revenue = sum(int(t.get("total_amount", 0)) for t in filtered)
        total_cash = sum(int(t.get("cash_paid", 0)) for t in filtered)
        total_change = sum(int(t.get("change", 0)) for t in filtered)

        filename = f"Accounting_{clean_mode}_{clean_period}.csv"
        filepath = os.path.join(self.base_export_dir, "accounting", filename)

        with open(filepath, mode="w", newline="", encoding="utf-8-sig") as f:
            writer = csv.writer(f)
            writer.writerow(["COMPANY SALES REPORT FOR ACCOUNTING REVIEW", clean_mode, f"Period: {clean_period}"])
            writer.writerow(["Total_Transactions", "Total_Items_Sold", "Gross_Revenue_Rp", "Total_Cash_Paid_Rp", "Total_Change_Given_Rp"])
            writer.writerow([total_trx, total_items, gross_revenue, total_cash, total_change])
            writer.writerow([])
            writer.writerow(["Transaction_ID", "Day", "Date", "Time", "Cashier_Username", "Items_Purchased", "Total_Amount", "Cash_Paid", "Change"])

            for t in filtered:
                ts = str(t.get("timestamp", ""))
                try:
                    dt = datetime.fromisoformat(ts)
                    d_name = dt.strftime("%A")
                    d_date = dt.strftime("%Y-%m-%d")
                    d_time = dt.strftime("%H:%M:%S")
                except Exception:
                    d_name, d_date, d_time = "-", ts[:10], ts[11:19]

                items_summary = "; ".join([f"{i['product_name']} (x{i['qty']})" for i in t.get("items", [])])
                writer.writerow([
                    t.get("trx_id", "-"),
                    d_name,
                    d_date,
                    d_time,
                    t.get("cashier", "UNKNOWN"),
                    items_summary,
                    t.get("total_amount", 0),
                    t.get("cash_paid", 0),
                    t.get("change", 0)
                ])
        return filepath

    # --- 3. WAREHOUSE HISTORY LOGGING & CSV EXPORT ---
    def log_warehouse_activity(self, staff_id, activity_type, barcode, product_name, qty_change, stock_before, stock_after, note="-"):
        now = datetime.now()
        payload = f"{now.isoformat()}|{staff_id}|{activity_type}|{barcode}|{qty_change}|{stock_after}"
        self.warehouse_logs.insert_one({
            "timestamp": now.isoformat(),
            "day": now.strftime("%A"),
            "date": now.strftime("%Y-%m-%d"),
            "time": now.strftime("%H:%M:%S"),
            "staff_id": staff_id,
            "activity_type": activity_type,  # NEW_PRODUCT, RESTOCK, DAMAGED
            "barcode": barcode,
            "product_name": product_name,
            "qty_change": int(qty_change),
            "stock_before": int(stock_before),
            "stock_after": int(stock_after),
            "note": note,
            "_sig": self.generate_signature(payload)
        })

    def export_warehouse_history_csv(self, date_prefix=""):
        query = {"date": {"$regex": f"^{date_prefix}"}} if date_prefix else {}
        logs = list(self.warehouse_logs.find(query).sort("timestamp", -1))
        if not logs:
            raise ValueError("No warehouse history records found!")

        label = date_prefix if date_prefix else datetime.now().strftime("%Y-%m-%d")
        filepath = os.path.join(self.base_export_dir, "warehouse", f"Warehouse_History_{label}.csv")

        with open(filepath, mode="w", newline="", encoding="utf-8-sig") as f:
            writer = csv.writer(f)
            writer.writerow([
                "Day", "Date", "Time", "Warehouse_Staff_ID", "Activity_Type",
                "Barcode", "Product_Name", "Qty_Change", "Stock_Before", "Stock_After", "Supplier_Or_Note"
            ])
            for row in logs:
                writer.writerow([
                    row.get("day", "-"), row.get("date", "-"), row.get("time", "-"),
                    row.get("staff_id", "-"), row.get("activity_type", "-"),
                    row.get("barcode", "-"), row.get("product_name", "-"),
                    row.get("qty_change", 0), row.get("stock_before", 0), row.get("stock_after", 0),
                    row.get("note", "-")
                ])
        return filepath
