# POS & Warehouse Management System (Hybrid Desktop App)

Sistem manajemen kasir (_Point of Sale_) dan stok gudang berbasis desktop yang menggabungkan antarmuka **Java Swing (FlatLaf)** dengan backend **Python OOP** dan database **MongoDB**.

## Fitur Utama

- **Autentikasi Login:** Gerbang masuk pegawai menggunakan Java Swing dengan tema modern FlatLaf.
- **Warehouse Management (`WarehouseFrame`):**
  - **Restock Batch:** Memindai/menginput banyak produk ke dalam tabel antrean sebelum menyimpannya sekaligus ke MongoDB.
  - **New Product:** Mendaftarkan produk baru beserta barcode unik, nama, harga, dan stok awal.
- **Cashier System (`CasierFrame`):**
  - Pemindaian barcode / input kode manual dengan pengurangan stok otomatis di database.
  - **Manager Verification:** Penghapusan item dari keranjang kasir memerlukan verifikasi Kartu Manager (`MGR-01`) dan otomatis mengembalikan stok ke database.
  - **Cash Payment Validation:** Validasi otomatis jika nominal pembayaran kurang serta perhitungan kembalian dan pencatatan riwayat transaksi.

## Arsitektur Sistem

```text
[Java Swing + FlatLaf]  <--->  [Python OOP Middleware]  <--->  [MongoDB]
   (Desktop UI Layer)          (bridge_cli.py & PyMongo)       (Database)
```

## Cara Menjalankan Proyek

1. **Database:** Pastikan layanan **MongoDB** lokal berjalan pada `mongodb://localhost:27017/`.
2. **Backend Python:**
   ```bash
   cd python_engine
   pip install -r ../requirements.txt
   cp .env.example .env
   ```
3. **Frontend Java:**
   - Buka folder `AppOn` di **Apache NetBeans**.
   - Sesuaikan `PYTHON_EXE` dan `SCRIPT_PATH` pada `PythonBridge.java` dengan lokasi Python di komputer Anda.
   - Jalankan `LogInFrame.java` (`Shift + F6`).
