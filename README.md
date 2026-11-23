# Hybrid POS & Warehouse Management System

A desktop Point of Sale (POS) and warehouse inventory management application built with **Java Swing (FlatLaf)**, **Python OOP**, and **MongoDB**. Designed with a **Zero-Trust Security Architecture** to protect store operations, credentials, and financial records against external attacks and unauthorized server-side tampering.

---

## Key Features

### 1. Zero-Trust Cybersecurity Layer (Authentication & Cashier)

- **Self-Service First-Time Setup:** No hardcoded credentials. Upon initial launch, the store owner registers their own username, password, and Manager Card directly through the UI, hashed using **PBKDF2-HMAC-SHA256** with random cryptographic salts.
- **Hardware Device Whitelisting:** Binds authorized cashier terminals to the server using OS hardware identifiers (`MachineGuid`), blocking unrecognized computers from accessing the system.
- **Session Token Enforcement:** Generates temporary cryptographic session tokens upon login to prevent unauthorized CLI/terminal bypasses against the Python backend.
- **Anti-Tampering HMAC Signatures:** Seals product and transaction records with **HMAC-SHA256** signatures (`_sig`) to detect unauthorized direct database modifications on third-party or untrusted servers.
- **Brute-Force Protection & Audit Logging:** Automatically locks accounts after 5 failed login attempts and records all suspicious events (`TOKEN_BYPASS_ATTEMPT`, `DATA_TAMPERING_DETECTED`, `UNAUTHORIZED_DEVICE`) in `security_logs`.
- **Server Security Awareness Alerts:** Audits the database connection on login and warns the user if authentication, TLS/SSL encryption, or server secret keys are missing.

### 2. Cashier System (`CasierFrame`)

- **Barcode Scanning & Manual Entry:** Real-time product lookup with integrity verification and automatic stock deduction.
- **Manager-Authorized Void (`Delete Item`):** Removing scanned items requires cryptographic verification of the Owner's Manager Card before restoring stock.
- **Cash Payment Validation:** Validates customer payment amounts, calculates change, and records sealed receipts in MongoDB.

### 3. Warehouse Management (`WarehouseFrame`)

- **Batch Restock Queue:** Scan and queue multiple incoming items into a staging table before committing updates to MongoDB.
- **New Product Registration:** Register new SKUs with unique barcode indexing and JSON Schema validation.

---

## System Architecture

```text
[Java Swing + FlatLaf]  <--->  [Python OOP Security & Middleware]  <--->  [MongoDB]
   (Desktop UI Layer)          (security_core.py & bridge_cli.py)         (Database)
```

---

## Project Structure

```text
hybrid-pos-warehouse-system/
├── AppsOn/                         # Java Swing Maven Project (NetBeans)
│   └── src/main/java/com/apps/
│       ├── service/
│       │   └── PythonBridge.java   # Whitelisted CLI bridge with session token injection
│       └── view/
│           ├── LogInFrame.java     # First-time setup, login & security awareness UI
│           ├── DashboardFrame.java # Main transition screen
│           ├── CasierFrame.java    # POS cashier interface
│           └── WarehouseFrame.java # Inventory restock & new product interface
├── python_engine/                  # Python OOP Backend & Security Engine
│   ├── db_config.py                # MongoDB connection, regex sanitization & schema validation
│   ├── security_core.py            # PBKDF2 hashing, HMAC sealing, device binding & server audit
│   ├── casier_system.py            # Secured cashier & transaction operations
│   ├── input_stock.py              # Warehouse inventory operations
│   ├── bridge_cli.py               # Token-protected CLI entry point
│   └── .env.example                # Environment configuration template
├── requirements.txt                # Python dependencies
└── README.md
```

---

## Getting Started

### Prerequisites

- **Java JDK 17+** & **Apache NetBeans** (or Maven)
- **Python 3.10+**
- **MongoDB Community Server** (running locally or on a secured remote server)

### Installation & Setup

1. **Clone the repository:**

   ```bash
   git clone https://github.com
   cd hybrid-pos-warehouse-system
   ```

2. **Configure the Python backend:**

   ```bash
   cd python_engine
   pip install -r ../requirements.txt
   cp .env.example .env
   ```

   Edit `.env` and set your `MONGO_URL`, `DB_NAME`, and a strong random `SERVER_SECRET_KEY`.

3. **Run the Java Desktop Application:**
   - Open the `AppsOn` folder in **Apache NetBeans**.
   - Verify the Python executable and script paths inside `PythonBridge.java`.
   - Run `LogInFrame.java` (`Shift + F6`) to complete the **First-Time Security Setup**.
