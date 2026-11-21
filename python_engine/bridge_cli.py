import sys
import json
from input_stock import WarehouseManager
from casier_system import CasierManager

def main():
    if len(sys.argv) < 2:
        print("ERROR - Incomplete command!")
        return
    
    action = sys.argv[1]
    warehouse = WarehouseManager()
    casier = CasierManager()

    try:
        if action == "CHECK":
            barcode = sys.argv[2]
            produk = casier.product_search(barcode)
            if produk:
                print(f"OK|{produk['product_name']}|{produk['price']}|{produk['stock']}")
            else:
                print("NOT_FOUND")

        elif action == "RESTOCK":
            barcode = sys.argv[2]
            qty = int(sys.argv[3])
            warehouse.restock(barcode, qty)
            print("OK")
        elif action == "NEW_PRODUCT":
            barcode = sys.argv[2]
            name = sys.argv[3]
            price = int(sys.argv[4])
            stock = int(sys.argv[5])
            warehouse.product_save(barcode, name, price, stock)
            print("OK")
        elif action == "DELETE_STOCK":
            barcode = sys.argv[2]
            qty = int(sys.argv[3])
            casier.remove_stock(barcode, qty)
            print("OK")
        elif action == "DELETE_ITEM":
            card = sys.argv[2]
            barcode = sys.argv[3]
            qty = int(sys.argv[4]) 
            if casier.verification_delete(card, barcode, qty):
                print("OK")
            else:
                print("UNAUTHORIZED")
        elif action == "TRANSACTION":
            items = json.loads(sys.argv[2])
            total = int(sys.argv[3])
            buy = int(sys.argv[4])
            change = int(sys.argv[5])
            casier.transaction(items, total, buy, change)
            print("OK")
        else:
            print("ERROR - Command not found!")
    except Exception as e:
        print(f"ERROR - {str(e)}")

if __name__ == "__main__":
    main()