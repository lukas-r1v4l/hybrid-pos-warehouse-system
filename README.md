# Hybrid POS & Warehouse Management System

A desktop Point of Sale (POS) and warehouse inventory management system built with **Java Swing (FlatLaf)**, **Python OOP**, and **MongoDB**. Engineered with a **Zero-Trust Cybersecurity Architecture** and **Three-Tier Role Access Control**, this system protects store operations, employee credentials, terminal access, and financial records across synchronized multi-computer environments.

---

## Program Introduction

Small and medium retail businesses often face two major operational vulnerabilities when running centralized POS and inventory systems: **unauthorized terminal access** and **database-level tampering** (especially when databases are hosted on shared or third-party servers).

This application solves both challenges by decoupling the user interface (**Java Swing**) from a hardened security and business-logic middleware (**Python OOP**). Every database read, write, login, and cart modification is cryptographically verified, sanitized against injection attacks, and bound to IT-approved hardware terminals.

---

## Core Functions & Features

### 1. Three-Tier Role-Based Access Control (RBAC)

- **Tier 1 — IT / Server Administrator (`ITAdminFrame`):**
  - Authenticates directly against MongoDB server admin credentials.
  - Manages network security via the **IT Security & Server Administration Console**, allowing IT staff to approve (`AUTHORIZED`) or block (`REVOKED_BY_IT`) employee terminal IP addresses and inspect real-time `security_logs`.
- **Tier 2 — Company Owner / Admin (`OwnerSetupFrame`):**
  - Dedicated management portal allowing business owners to self-register (`Sign Up Owner`) with a **Manager ID Card**.
  - Supports secure account handover/deletion (`Delete Account`) requiring dual cryptographic verification (**Manager ID Card + Owner Password**).
- **Tier 3 — Employees / Cashiers (`LogInFrame`):**
  - Self-service employee registration (`Sign Up`) that automatically captures the workstation's IP address and OS hardware identifier (`MachineGuid`).
  - New terminals are placed in a `PENDING_IT_APPROVAL` queue until authorized by the IT Administrator.

### 2. Zero-Trust Cybersecurity Engine (`security_core.py`)

- **PBKDF2-HMAC-SHA256 Credential Hashing:** All user passwords and Manager ID Cards are salted and hashed with 100,000 iterations (zero plain-text storage).
- **Anti-Tampering HMAC Signatures (`_sig`):** Product records and sales transactions are sealed with **HMAC-SHA256** signatures. Any unauthorized manual modification made directly in MongoDB is immediately detected and blocked (`TAMPERED`).
- **Session Token Enforcement:** Generates temporary 48-character cryptographic session tokens upon login to prevent unauthorized command-line bypasses against `bridge_cli.py`.
- **Brute-Force Lockout & Input Sanitization:** Automatically locks accounts after 5 consecutive failed login attempts and blocks NoSQL/regex injection payloads.
- **Server Security Awareness Alerts:** Audits database authentication and secret key configurations upon login and warns users if the server environment is insecure.

### 3. Point of Sale / Cashier Module (`CasierFrame`)

- **Real-Time Barcode Scanning & Lookup:** Verifies product integrity and available stock before adding items to the customer's cart.
- **Manager-Authorized Cart Removal (`Delete Item`):** Removing scanned items from a customer's order requires cryptographic verification of an Owner's Manager ID Card—removing the item from the shopping list without altering database stock.
- **Atomic Checkout & Stock Deduction:** Validates cash payment amounts, calculates change, deducts inventory stock, and records HMAC-sealed receipts upon payment completion.

### 4. Warehouse Inventory Module (`WarehouseFrame`)

- **Batch Restock Staging:** Scan and queue multiple incoming items in a staging table before committing batch stock updates to MongoDB.
- **New SKU Registration:** Register new products with unique barcode indexing and MongoDB JSON Schema validation.

---

## System Architecture

```text
[Java Swing + FlatLaf UI]  <--->  [Python OOP Security Middleware]  <--->  [Central MongoDB Server]
 (LogIn, Owner, IT, POS)           (security_core.py & bridge_cli)          (Hashed & HMAC-Sealed)
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
│           ├── DashboardFrame.java # Module selection screen
│           ├── CasierFrame.java    # POS cashier interface
│           └── WarehouseFrame.java # Inventory restock & new product interface
├── python_engine/                  # Python OOP Backend & Security Engine
│   ├── db_config.py                # MongoDB connection, input sanitization & JSON schema rules
│   ├── security_core.py            # 3-tier auth, IP tracking, PBKDF2 hashing & HMAC sealing
│   ├── casier_system.py            # Secured cashier & manager verification operations
│   ├── input_stock.py              # Warehouse inventory operations
│   ├── bridge_cli.py               # Token-protected CLI router
│   └── .env.example                # Environment configuration template
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
3. **Register Employee Terminals (Tier 3):** On each cashier/warehouse PC, click **Sign Up** on `LogInFrame` to register an employee account and submit the PC's IP address for approval.
4. **Approve Terminal IPs (Tier 1):** Click **IT Server & Security Console**, log in using your MongoDB Admin credentials, select the pending terminal row, and click **Approve Selected IP**.

---

## Important Security Warnings for Users

> **CRITICAL — READ BEFORE DEPLOYING IN PRODUCTION:**
>
> 1. **Protect Your `.env` File & `SERVER_SECRET_KEY`:** Never commit your `.env` file to public repositories. All company terminals connected to the same server **must share the exact same `SERVER_SECRET_KEY`**. Changing or losing this key will cause existing HMAC product seals (`_sig`) to fail integrity verification.
> 2. **Enable MongoDB Authentication (`authorization: enabled`):** Never run the central MongoDB server without username/password authentication enabled in `mongod.cfg`. If hosted over the internet or an untrusted network, enforce **TLS/SSL encryption**.
> 3. **Keep Manager ID Cards Confidential:** The Owner's Manager ID Card authorizes item removals and account deletions. Do not share this code with unauthorized staff.
> 4. **Monitor `security_logs` Regularly:** IT Administrators should periodically review the **Security Audit Logs** tab in `ITAdminFrame` for `DATA_TAMPERING_DETECTED`, `TOKEN_BYPASS_ATTEMPT`, or repeated `FAILED_LOGIN` events.
> 5. **Do Not Edit Database Values Manually:** Editing product prices or stock quantities directly inside MongoDB Compass will invalidate the cryptographic `_sig` seal and automatically lock the item from being sold at the cashier.
