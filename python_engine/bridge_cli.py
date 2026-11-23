import sys
import json
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
        if action == "CHECK_SETUP":
            print("READY" if sec.setup_complete() else "NEED_SETUP")
            return
        elif action == "SETUP_OWNER":
            sec.create_initial_owner(sys.argv[2], sys.argv[3], sys.argv[4])
            print("OK")
            return
        elif action == "LOGIN":
            status = sec.authentication_user(sys.argv[2], sys.argv[3])
            if status == "OK":
                token = sec.create_session_token(sys.argv[2])
                audit = sec.audit_server_security()
                print(f"OK|{token}|{audit}")
            else:
                print(status)
            return
        if len(sys.argv) < 3:
            print("UNAUTHORIZED_SESSION")
            return

        token = sys.argv[2]
        if not sec.verify_session_token(token):
            sec.log_event("TOKEN_BYPASS_ATTEMPT", f"Illegal access to action '{action}' without a valid session token!")
            print("UNAUTHORIZED_SESSION")
            return

        warehouse = WarehouseManager()
        casier = CasierManager()
        if action == "CHECK":
            produk = casier.product_search(sys.argv[3])
            if produk:
                if not sec.verify_product_integrity(produk):
                    print("TAMPERED| This product data has been detected as having been illegally modified in the database!")
                else:
                    print(f"OK|{produk['product_name']}|{produk['price']}|{produk['stock']}")
            else:
                print("NOT_FOUND")
        elif action == "RESTOCK":
            warehouse.restock(sys.argv[3], int(sys.argv[4]))
            print("OK")
        elif action == "NEW_PRODUCT":
            warehouse.product_save(sys.argv[3], sys.argv[4], int(sys.argv[5]), int(sys.argv[6]))
            print("OK")
        elif action == "DELETE_STOCK":
            casier.remove_stock(sys.argv[3], int(sys.argv[4]))
            print("OK")
        elif action == "DELETE_ITEM":
            if casier.verification_delete(sys.argv[3], sys.argv[4], int(sys.argv[5])):
                print("OK")
            else:
                print("UNAUTHORIZED")
        elif action == "TRANSACTION":
            casier.transaction(json.loads(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5]), int(sys.argv[6]))
            print("OK")
        else:
            print("ERROR - Command not found!")
    except Exception as e:
        print(f"ERROR - {str(e)}")

if __name__ == "__main__":
    main()
