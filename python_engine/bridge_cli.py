import sys
import json
import base64
from security_core import SecurityManager
from input_stock import WarehouseManager
from casier_system import CasierManager

def main():
    if len(sys.argv) < 2:
        print("ERROR - Incomplete command!")
        return

    action = sys.argv[1]
    sec = SecurityManager()

    try:
        # --- 1. PUBLIC ACTIONS (PRE-LOGIN / IT CONSOLE) ---
        if action == "SIGNUP_EMPLOYEE":
            res = sec.employee_signup(sys.argv[2], sys.argv[3])
            print(res)
            return
        elif action == "SIGNUP_OWNER":
            sec.owner_signup(sys.argv[2], sys.argv[3], sys.argv[4])
            print("OK")
            return
        elif action == "DELETE_OWNER":
            if sec.owner_delete(sys.argv[2], sys.argv[3], sys.argv[4]):
                print("OK")
            else:
                print("INVALID_VERIFICATION")
            return
        elif action == "IT_APPROVE_IP":
            sec.it_approve_ip(sys.argv[2])
            print("OK")
            return
        elif action == "IT_LOGIN":
            res = sec.verify_it_credentials(sys.argv[2], sys.argv[3])
            if res == "OK":
                sec.log_event("IT_CONSOLE_LOGIN", f"IT Administrator '{sys.argv[2]}' logged into IT Security Console.")
                print("OK")
            else:
                sec.log_event("IT_LOGIN_FAILED", f"Failed IT Console login for '{sys.argv[2]}': {res}")
                print(res)
            return
        elif action == "IT_GET_DEVICES":
            print(sec.get_all_devices())
            return
        elif action == "IT_REVOKE_IP":
            sec.revoke_device_ip(sys.argv[2])
            print("OK")
            return
        elif action == "IT_GET_LOGS":
            print(sec.get_security_logs())
            return
        elif action == "LOGIN":
            auth_res = sec.authentication_user(sys.argv[2], sys.argv[3])
            if auth_res.startswith("OK|"):
                parts = auth_res.split("|")
                role = parts[1]
                position = parts[2]
                token = sec.create_session_token(sys.argv[2])
                audit = sec.audit_server_security()
                print(f"OK|{role}|{position}|{token}|{audit}")
            else:
                print(auth_res)
            return

        # --- 2. PROTECTED ACTIONS (TOKEN + ROLE ENFORCEMENT) ---
        if len(sys.argv) < 3:
            print("UNAUTHORIZED_SESSION")
            return

        token = sys.argv[2]
        warehouse = WarehouseManager()
        casier = CasierManager()

        if action == "CHECK":
            session_user = sec.verify_session_and_role(token, ["CASIER", "WAREHOUSE"])
            if session_user is None:
                sec.log_event("TOKEN_BYPASS_ATTEMPT", "Invalid/expired session token on CHECK!")
                print("UNAUTHORIZED_SESSION")
                return
            if session_user == "FORBIDDEN_ROLE":
                print("FORBIDDEN_ROLE")
                return

            product = casier.product_search(sys.argv[3])
            if product:
                if not sec.verify_product_integrity(product):
                    print("TAMPERED|Product integrity verification failed! Possible unauthorized database modification.")
                else:
                    print(f"OK|{product['product_name']}|{product['price']}|{product['stock']}")
            else:
                print("NOT_FOUND")

        elif action in ["VERIFY_MANAGER", "DELETE_STOCK", "DELETE_ITEM", "TRANSACTION"]:
            session_user = sec.verify_session_and_role(token, ["CASIER"])
            if session_user is None:
                sec.log_event("TOKEN_BYPASS_ATTEMPT", f"Invalid/expired session token on '{action}'!")
                print("UNAUTHORIZED_SESSION")
                return
            if session_user == "FORBIDDEN_ROLE":
                print("FORBIDDEN_ROLE|Access restricted to Cashier or Owner!")
                return

            actor = session_user["username"]

            if action == "VERIFY_MANAGER":
                if casier.verify_manager_only(sys.argv[3], sys.argv[4], int(sys.argv[5]), actor):
                    print("OK")
                else:
                    print("UNAUTHORIZED")
            elif action == "DELETE_STOCK":
                casier.remove_stock(sys.argv[3], int(sys.argv[4]), actor)
                print("OK")
            elif action == "DELETE_ITEM":
                if casier.verification_delete(sys.argv[3], sys.argv[4], int(sys.argv[5])):
                    print("OK")
                else:
                    print("UNAUTHORIZED")
            elif action == "TRANSACTION":
                raw_json = base64.b64decode(sys.argv[3]).decode("utf-8")
                receipt_info = casier.process_transaction(
                    json.loads(raw_json),
                    int(sys.argv[4]),
                    int(sys.argv[5]),
                    int(sys.argv[6]),
                    actor
                )
                print(f"OK|{receipt_info}")


        elif action in ["RESTOCK", "NEW_PRODUCT"]:
            session_user = sec.verify_session_and_role(token, ["WAREHOUSE"])
            if session_user is None:
                sec.log_event("TOKEN_BYPASS_ATTEMPT", f"Invalid/expired session token on '{action}'!")
                print("UNAUTHORIZED_SESSION")
                return
            if session_user == "FORBIDDEN_ROLE":
                print("FORBIDDEN_ROLE|Access restricted to Warehouse or Owner!")
                return

            actor = session_user["username"]

            if action == "RESTOCK":
                warehouse.restock(sys.argv[3], int(sys.argv[4]), actor)
                print("OK")
            elif action == "NEW_PRODUCT":
                warehouse.product_save(sys.argv[3], sys.argv[4], int(sys.argv[5]), int(sys.argv[6]), actor)
                print("OK")
        elif action in ["DAMAGED_STOCK", "EXPORT_WAREHOUSE"]:
            session_user = sec.verify_session_and_role(token, ["WAREHOUSE"])
            if session_user is None:
                sec.log_event("TOKEN_BYPASS_ATTEMPT", f"Invalid/expired session token on '{action}'!")
                print("UNAUTHORIZED_SESSION")
                return
            if session_user == "FORBIDDEN_ROLE":
                print("FORBIDDEN_ROLE|Access restricted to Warehouse or Owner!")
                return

            actor = session_user["username"]

            if action == "DAMAGED_STOCK":
                note = sys.argv[5] if len(sys.argv) > 5 else "Damaged Goods"
                warehouse.record_damaged(sys.argv[3], int(sys.argv[4]), actor, note)
                print("OK")
            elif action == "EXPORT_WAREHOUSE":
                from report_manager import ReportManager
                date_filter = sys.argv[3] if len(sys.argv) > 3 else ""
                path = ReportManager().export_warehouse_history_csv(date_filter)
                print(f"OK|{path}")

        elif action == "EXPORT_ACCOUNTING":
            session_user = sec.verify_session_and_role(token, ["OWNER"])
            if session_user is None:
                sec.log_event("TOKEN_BYPASS_ATTEMPT", "Invalid/expired session token on 'EXPORT_ACCOUNTING'!")
                print("UNAUTHORIZED_SESSION")
                return
            if session_user == "FORBIDDEN_ROLE":
                print("FORBIDDEN_ROLE|Only the Company Owner can export accounting reports!")
                return

            from report_manager import ReportManager
            path = ReportManager().export_accounting_report(sys.argv[3], sys.argv[4])
            print(f"OK|{path}")
        else:
            print("ERROR - Command not found!")
    except Exception as e:
        print(f"ERROR - {str(e)}")

if __name__ == "__main__":
    main()
