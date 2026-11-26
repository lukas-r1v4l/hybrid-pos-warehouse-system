# Hybrid POS & Warehouse Management System

A desktop Point of Sale (POS) and warehouse inventory management system built with **Java Swing (FlatLaf)**, **Python OOP**, and **MongoDB**. Engineered with a **Zero-Trust Cybersecurity Architecture**, **Three-Tier Role-Based Access Control (RBAC)**, and **Automated CSV Reporting & Receipt Printing**, this system protects store operations, employee credentials, terminal access, and financial records across synchronized multi-computer environments.

---

## Program Introduction

Small and medium retail businesses often face two major operational vulnerabilities when running centralized POS and inventory systems: **unauthorized terminal/role access** and **database-level tampering** (especially when databases are hosted on shared or third-party servers).

This application solves both challenges by decoupling the user interface (**Java Swing**) from a hardened security and business-logic middleware (**Python OOP**). Every database read, write, login, cart modification, and transaction is cryptographically verified, sanitized against injection attacks, bound to IT-approved hardware terminals, and logged for audit and accounting review.

---

## Core Functions & Features

### 1. Three-Tier Role & Position-Based Access Control (RBAC)

- **Tier 1 — IT / Server Administrator (`ITAdminFrame`):**
  - Authenticates directly against MongoDB server admin credentials.
  - Manages network security via the **IT Security & Server Administration Console**, allowing IT staff to approve (`AUTHORIZED`) or block (`REVOKED_BY_IT`) employee terminal IP addresses and inspect real-time `security_logs`.
- **Tier 2 — Company Owner / Admin (`OwnerSetupFrame`):**
  - Dedicated management portal allowing business owners to self-register (`Sign Up Owner`) with a **Manager ID Card**.
  - Supports secure account handover/deletion (`Delete Account`) requiring dual cryptographic verification (**Manager ID Card + Owner Password**).
  - Grants full access to both Cashier and Warehouse modules as well as **Monthly & Yearly Accounting CSV Exports**.
- **Tier 3 — Employees (`LogInFrame` with Enforced Username Prefixes):**
  - Staff self-register (`Sign Up`) using strict role-prefixed usernames:
    - **`Casier_<number>`** (e.g., `Casier_01`): Assigned `CASIER` position—restricted exclusively to the **Point of Sale / Cashier** module.
    - **`Warehouse_<number>`** (e.g., `Warehouse_10089`): Assigned `WAREHOUSE` position—restricted exclusively to the **Warehouse Inventory** module.
  - Automatically captures the workstation's IP address and OS hardware identifier (`MachineGuid`), placing new terminals in `PENDING_IT_APPROVAL` until approved by the IT Administrator.

### 2. Zero-Trust Cybersecurity Engine (`security_core.py`)

- **PBKDF2-HMAC-SHA256 Credential Hashing:** All user passwords and Manager ID Cards are salted and hashed with 100,000 iterations (zero plain-text storage).
- **Anti-Tampering HMAC Signatures (`_sig`):** Product records, warehouse logs, and sales transactions are sealed with **HMAC-SHA256** signatures. Any unauthorized manual modification made directly in MongoDB is immediately detected and blocked (`TAMPERED`).
- **Session Token & Position Enforcement:** Generates temporary 8-hour cryptographic session tokens bound to the user's position and hardware ID, blocking cross-role CLI bypasses (`FORBIDDEN_ROLE`).
- **Brute-Force Lockout & Input Sanitization:** Automatically locks accounts after 5 consecutive failed login attempts, validates all form inputs (including Base64-encoded transaction payloads), and logs all critical actions with the responsible actor's username.

### 3. Point of Sale / Cashier Module (`CasierFrame`)

- **Real-Time Barcode Scanning & Lookup:** Verifies product integrity and available stock before adding items to the customer's cart.
- **Manager-Authorized Cart Removal (`Delete Item`):** Removing scanned items from a customer's order requires cryptographic verification of an Owner's Manager ID Card—removing the item from the shopping list without altering database stock.
- **Atomic Checkout, Receipt Printout & CSV Export:** Validates cash payment amounts, deducts inventory stock atomically in MongoDB, displays a printable **Customer Receipt** (recommended paper size: **58mm / 80mm thermal** or **A6**), and saves a detailed per-transaction `.csv` file in `exports/receipts/`.

### 4. Warehouse Inventory & Accounting Reports (`WarehouseFrame` & `DashboardFrame`)

- **Batch Restock & New SKU Registration:** Stage multiple supplier restocks or register new products with unique barcode indexing and HMAC sealing.
- **Damaged Goods Tracking (`Damaged Goods`):** Record damaged inventory items with reason notes, automatically deducting stock and logging the event in `warehouse_logs`.
- **Warehouse History CSV Export:** Export daily or complete warehouse activity logs (`NEW_PRODUCT`, `RESTOCK`, `DAMAGED`) to `exports/warehouse/` in Excel-ready `.csv` format.
- **Monthly & Yearly Accounting Sales Reports:** Company Owners can export consolidated monthly (`YYYY-MM`) and yearly (`YYYY`) sales reports to `exports/accounting/` for accounting team review and business planning.

---

## System Architecture

```text
[Java Swing + FlatLaf UI]  <--->  [Python OOP Security & Reporting]  <--->  [Central MongoDB Server]
 (LogIn, Owner, IT, POS)          (security_core & report_manager)         (Hashed & HMAC-Sealed)
```

---

## Project Structure

```text
hybrid-pos-warehouse-system/
├── AppsOn/                         # Java Swing Maven Project (NetBeans)
│   └── src/main/java/com/apps/
│       ├── service/
│       │   └── PythonBridge.java   # Whitelisted CLI bridge with relative path & session tokens
│       └── view/
│           ├── LogInFrame.java     # Main authentication portal (Employee, Owner, IT launcher)
│           ├── OwnerSetupFrame.java# Owner Sign Up & dual-verified account deletion portal
│           ├── ITAdminFrame.java   # IT Security Console (IP approvals & audit log viewer)
│           ├── DashboardFrame.java # Role-restricted navigation & Accounting CSV export
│           ├── CasierFrame.java    # POS cashier interface & Customer Receipt printout
│           └── WarehouseFrame.java # Inventory restock, damaged goods & history CSV export
├── python_engine/                  # Python OOP Backend, Security & Reporting Engine
│   ├── db_config.py                # MongoDB connection, input sanitization & JSON schema rules
│   ├── security_core.py            # 3-tier auth, position RBAC, PBKDF2 hashing & HMAC sealing
│   ├── casier_system.py            # Atomic cashier transactions & manager verification
│   ├── input_stock.py              # Warehouse restock, new SKU & damaged goods operations
│   ├── report_manager.py           # Receipt, accounting (monthly/yearly) & warehouse CSV generator
│   ├── bridge_cli.py               # Token & role-protected CLI router
│   └── .env.example                # Environment configuration template
├── exports/                        # Auto-generated Excel-ready CSV files (git-ignored)
├── requirements.txt                # Python dependencies
└── README.md
```

---

## Installation & Deployment Guide

Because `PythonBridge.java` uses **relative path resolution** (`../python_engine/bridge_cli.py`) and system PATH execution (`python`), you can deploy this application on any Windows/Linux machine without modifying hardcoded file paths.

### Prerequisites

- **Java JDK 17+** and **Apache NetBeans** (or Maven)
- **Python 3.10+** (Ensure **"Add Python to PATH"** is enabled during installation)
- **MongoDB Community Server** (Installed on the central server PC or cloud host)

### Step 1: Clone & Install Dependencies

```bash
git clone https://github.com
cd hybrid-pos-warehouse-system/python_engine
pip install -r ../requirements.txt
```

### Step 2: Configure Environment Variables (`.env`)

1. Copy `.env.example` to `.env` inside `python_engine/`:
   ```bash
   cp .env.example .env
   ```
2. Edit `python_engine/.env` with your MongoDB server credentials and secret key:

   ```env
   # Single-PC local deployment:
   MONGO_URL=mongodb://adminUser:YourStrongPassword@127.0.0.1:27017/?authSource=admin

   # Multi-PC company network deployment (use Central Server LAN IP):
   # MONGO_URL=mongodb://adminUser:YourStrongPassword@192.168.1.10:27017/?authSource=admin

   DB_NAME=toko_stock_db
   SERVER_SECRET_KEY=replace_with_a_random_secret_key_minimum_32_characters
   ```

### Step 3: Initialize Accounts & Approve Terminals

1. Open the `AppsOn` project in **Apache NetBeans** and run `LogInFrame.java` (`Shift + F6`).
2. **Create Company Owner (Tier 2):** Click **Owner Account Management** -> **Sign Up Owner** to register the Owner account and Manager ID Card.
3. **Register Employee Terminals (Tier 3):** On each cashier/warehouse PC, click **Sign Up** on `LogInFrame` using the required prefix (`Casier_01` or `Warehouse_01`) to submit the PC's IP address for approval.
4. **Approve Terminal IPs (Tier 1):** Click **IT Server & Security Console**, log in using your MongoDB Admin credentials, select the pending terminal row, and click **Approve Selected IP**.

---

## Important Security & Operational Warnings for Users

> **CRITICAL — READ BEFORE DEPLOYING IN PRODUCTION:**
>
> 1. **Protect Your `.env` File & `SERVER_SECRET_KEY`:** Never commit your `.env` file or `exports/` folder to public repositories. All company terminals connected to the same server **must share the exact same `SERVER_SECRET_KEY`**. Changing or losing this key will cause existing HMAC product seals (`_sig`) to fail integrity verification.
