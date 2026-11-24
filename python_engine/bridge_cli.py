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
        elif action == "LOGIN":
            auth_res = sec.authentication_user(sys.argv[2], sys.argv[3])
            if auth_res.startswith("OK|"):
                role = auth_res.split("|")[1]
                token = sec.create_session_token(sys.argv[2])
                audit = sec.audit_server_security()
                print(f"OK|{role}|{token}|{audit}")
            else:
                print(auth_res)
            return
        if len(sys.argv) < 3 or not sec.verify_session_token(sys.argv[2]):
            sec.log_event("TOKEN_BYPASS_ATTEMPT", f"Unauthorized CLI access to '{action}' without a valid session token!")
            print("UNAUTHORIZED_SESSION")
            return

        warehouse = WarehouseManager()
        casier = CasierManager()

        if action == "CHECK":
            product = casier.product_search(sys.argv[3])
            if product:
                if not sec.verify_product_integrity(product):
                    print("TAMPERED|Product integrity verification failed! Possible unauthorized database modification.")
                else:
                    print(f"OK|{product['product_name']}|{product['price']}|{product['stock']}")
            else:
                print("NOT_FOUND")
        elif action == "VERIFY_MANAGER":
            if casier.verify_manager_only(sys.argv[3], sys.argv[4], int(sys.argv[5])):
                print("OK")
            else:
                print("UNAUTHORIZED")
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