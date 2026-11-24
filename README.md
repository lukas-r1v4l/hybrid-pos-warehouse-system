# Hybrid POS & Warehouse Management System

A desktop Point of Sale (POS) and warehouse inventory management application built with **Java Swing (FlatLaf)**, **Python OOP**, and **MongoDB**. Designed with a **Zero-Trust Security Architecture** and **3-Tier Role Access Control** to protect store operations, credentials, and financial records across synchronized company terminals.

---

## Key Features

### 1. Three-Tier Access Control & Zero-Trust Cybersecurity

- **Tier 1 — IT / Server Administrator (MongoDB & Network Layer):** Controls database authentication (`authorization: enabled`), network isolation, and approves new terminal IP addresses (`IT_APPROVE_IP`) before staff can connect to the central server.
- **Tier 2 — Company Owner / Admin (`OwnerSetupFrame`):** Dedicated management portal allowing the store owner to register or replace their account (`Sign Up Owner`) with a **Manager ID Card**, or securely remove an existing owner account (`Delete Account`) via dual verification (Manager Card + Owner Password).
- **Tier 3 — Employees / Cashiers (`LogInFrame`):** Staff can self-register (`Sign Up`) from any terminal. Their hardware `MachineGuid` and local IP address are automatically captured and queued (`PENDING_IT_APPROVAL`) until approved by the IT Administrator.
- **Cryptographic Hashing & Session Tokens:** All passwords and Manager Cards are hashed with **PBKDF2-HMAC-SHA256** + random salts. Temporary session tokens prevent unauthorized CLI bypasses against `bridge_cli.py`.
- **Anti-Tampering HMAC Signatures:** Seals product and transaction records with **HMAC-SHA256** (`_sig`) to detect unauthorized direct edits on untrusted or third-party servers.
- **Server Security Awareness Alerts:** Audits database authentication and secret key configurations upon login, warning users if the server environment is insecure.

### 2. Cashier System (`CasierFrame`)

- **Barcode Scanning & Manual Entry:** Real-time product lookup with integrity verification and automatic stock deduction.
- **Manager-Authorized Void (`Delete Item`):** Removing scanned items requires cryptographic verification of an Owner's Manager ID Card before restoring inventory stock.
- **Cash Payment Validation:** Validates customer cash payments, calculates change, and records sealed receipts in MongoDB.

### 3. Warehouse Management (`WarehouseFrame`)

- **Batch Restock Queue:** Scan and queue multiple incoming items into a staging table before committing updates to MongoDB.
- **New Product Registration:** Register new SKUs with unique barcode indexing and MongoDB JSON Schema validation.

---

## System Architecture

```text
[Cashier / Warehouse PCs (Java Swing)]  --->  [Central Server (Python Security + MongoDB)]
  (Auto-resolved relative paths & IP)          (PBKDF2, HMAC-SHA256, IP Whitelisting)
```

---

## Project Structure

```text
hybrid-pos-warehouse-system/
├── AppsOn/                         # Java Swing Maven Project (NetBeans)
│   └── src/main/java/com/apps/
│       ├── service/
│       │   └── PythonBridge.java   # Whitelisted CLI bridge with dynamic path & session token
│       └── view/
│           ├── LogInFrame.java     # Employee Log In, Sign Up & Owner Portal launcher
│           ├── OwnerSetupFrame.java# Dedicated Owner Sign Up & Dual-Verified Account Deletion
│           ├── DashboardFrame.java # Main module navigation screen
│           ├── CasierFrame.java    # POS cashier interface
│           └── WarehouseFrame.java # Inventory restock & new product interface
├── python_engine/                  # Python OOP Backend & Security Engine
│   ├── db_config.py                # MongoDB connection, regex sanitization & schema validation
│   ├── security_core.py            # 3-tier auth, IP tracking, PBKDF2 hashing & HMAC sealing
│   ├── casier_system.py            # Secured cashier & transaction operations
│   ├── input_stock.py              # Warehouse inventory operations
│   ├── bridge_cli.py               # Token-protected CLI entry point
│   └── .env.example                # Environment configuration template
├── requirements.txt                # Python dependencies
└── README.md
```

---

## Deployment & Device Setup Guide (For New Computers / Company Servers)

Because this project uses **relative directory resolution** (`../python_engine/bridge_cli.py`) and system PATH execution (`python`), you do **not** need to replicate the original developer's folder paths. Follow these steps to deploy on any new device or company network:

### Prerequisites

- **Java JDK 17+** & **Apache NetBeans** (or Maven)
- **Python 3.10+** (Ensure **"Add Python to PATH"** is checked during installation)
- **MongoDB Community Server** (Installed on the central server PC or cloud host)

### Step 1: Clone & Install Dependencies

1. Clone the repository to any directory on the target computer:
   ```bash
   git clone https://github.com
   cd hybrid-pos-warehouse-system
   ```
2. Install the required Python libraries:
   ```bash
   cd python_engine
   pip install -r ../requirements.txt
   ```

### Step 2: Configure Environment Variables (`.env`)

1. Copy the template file to create your local `.env`:
   ```bash
   cp .env.example .env
   ```
2. Open `python_engine/.env` and configure your server connection:

   ```env
   # For single-PC / local testing:
   MONGO_URL=mongodb://adminUser:YourPassword@127.0.0.1:27017/?authSource=admin

   # For multi-PC company network (replace with your Central Server's LAN IP):
   # MONGO_URL=mongodb://adminUser:YourPassword@192.168.1.10:27017/?authSource=admin

   DB_NAME=toko_stock_db
   SERVER_SECRET_KEY=replace_with_a_random_secret_string_at_least_32_chars
   ```

   _(Note: Ensure all company terminals share the exact same `SERVER_SECRET_KEY` so HMAC data seals remain valid across devices)._

### Step 3: Verify Python Path (Optional)

- By default, `PythonBridge.java` uses `PYTHON_EXE = "python"`.
- If your IDE does not recognize the global `python` command, run `where python` (Windows) or `which python3` (Linux/macOS) in your terminal and paste that path into `PYTHON_EXE` inside `AppsOn/src/main/java/com/apps/service/PythonBridge.java`.

### Step 4: Initialize Accounts & Approve Terminal IPs

1. **Run the Application:** Open `AppsOn` in Apache NetBeans and run `LogInFrame.java` (`Shift + F6`).
2. **Register Company Owner (Tier 2):** Click **Owner Account Management** -> **Sign Up Owner** to create the Owner credentials and Manager ID Card (automatically authorizes the Owner's PC).
3. **Register Employees (Tier 3):** Staff click **Sign Up** on `LogInFrame` from their respective cashier/warehouse computers. New terminal IPs are placed in `PENDING_IT_APPROVAL`.
4. **IT Administrator IP Approval (Tier 1):** On the central server terminal, run the following command to authorize a new employee terminal IP:
   ```bash
   cd python_engine
   python bridge_cli.py IT_APPROVE_IP <EMPLOYEE_IP_ADDRESS>
   ```
