package com.apps.view;

/**
 *
 * @author Lukas Rivaldo
 */
public class WarehouseFrame extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WarehouseFrame.class.getName());
    public WarehouseFrame() {
        setTitle("Warehouse Management System");
        initComponents();
        setupCustomLayout();
        setLocationRelativeTo(null);
    }
    private void setupCustomLayout() {
        getContentPane().removeAll();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.JButton btnDamaged = new javax.swing.JButton("DAMAGED GOODS");
        btnDamaged.setBackground(new java.awt.Color(220, 38, 38));
        btnDamaged.setForeground(java.awt.Color.WHITE);
        btnDamaged.addActionListener(e -> openWindowDamaged());

        javax.swing.JButton btnExportWh = new javax.swing.JButton("EXPORT HISTORY (.CSV)");
        btnExportWh.setBackground(new java.awt.Color(37, 99, 235));
        btnExportWh.setForeground(java.awt.Color.WHITE);
        btnExportWh.addActionListener(e -> exportWarehouseHistory());

        btnNewProduct.setText("NEW PRODUCT");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(105, 105, 105)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnRestock, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(btnNewProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(btnDamaged, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(btnExportWh, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap(105, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(btnRestock, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnNewProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnDamaged, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnExportWh, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(65, Short.MAX_VALUE))
        );

        pack();
        revalidate();
        repaint();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnRestock = new javax.swing.JButton();
        btnNewProduct = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnRestock.setText("RESTOCK");
        btnRestock.addActionListener(this::btnRestockActionPerformed);

        btnNewProduct.setText("NEW");
        btnNewProduct.addActionListener(this::btnNewProductActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(137, 137, 137)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNewProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRestock, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(156, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(116, 116, 116)
                .addComponent(btnRestock)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNewProduct)
                .addContainerGap(132, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void openWindowRestock() {
        javax.swing.JDialog dialogRestock = new javax.swing.JDialog(this, "Product Restock", true);
        dialogRestock.setSize(540, 440);
        dialogRestock.setLocationRelativeTo(this);
        dialogRestock.setLayout(new java.awt.BorderLayout(10, 10));
        ((javax.swing.JPanel) dialogRestock.getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        javax.swing.JPanel panelForm = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 8, 8));
        javax.swing.JTextField txtBarcode = new javax.swing.JTextField();
        txtBarcode.putClientProperty("JTextField.placeholderText", "Type or scan the barcode (e.g., 1001)");
        javax.swing.JTextField txtQty = new javax.swing.JTextField();
        txtQty.putClientProperty("JTextField.placeholderText", "Enter the quantity (e.g., 10)");
        javax.swing.JButton btnAddList = new javax.swing.JButton("Add to list");
        btnAddList.setBackground(new java.awt.Color(37, 99, 235));
        btnAddList.setForeground(java.awt.Color.WHITE);
        btnAddList.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        panelForm.add(new javax.swing.JLabel("1. Product Barcode:"));
        panelForm.add(txtBarcode);
        panelForm.add(new javax.swing.JLabel("2. Quantity Restock:"));
        panelForm.add(txtQty);
        panelForm.add(new javax.swing.JLabel(""));
        panelForm.add(btnAddList);
        javax.swing.table.DefaultTableModel modelRestock = new javax.swing.table.DefaultTableModel(
            new Object[]{"Barcode", "Product Name", "Qty Restock"}, 0
        );
        javax.swing.JTable tblRestock = new javax.swing.JTable(modelRestock);
        tblRestock.setRowHeight(28);
        javax.swing.JButton btnDone = new javax.swing.JButton("Done");
        btnDone.setBackground(new java.awt.Color(22, 163, 74));
        btnDone.setForeground(java.awt.Color.WHITE);
        btnDone.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        dialogRestock.add(panelForm, java.awt.BorderLayout.NORTH);
        dialogRestock.add(new javax.swing.JScrollPane(tblRestock), java.awt.BorderLayout.CENTER);
        dialogRestock.add(btnDone, java.awt.BorderLayout.SOUTH);
        java.awt.event.ActionListener aksiTambahList = e -> {
            String kode = txtBarcode.getText().trim();
            String qtyStr = txtQty.getText().trim();
            if (kode.isEmpty() || qtyStr.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(dialogRestock, "Fill in the Barcode and Quantity first!");
                return;
            }
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    javax.swing.JOptionPane.showMessageDialog(dialogRestock, "Quantity must be greater than 0!");
                    return;
                }
                String respon = com.apps.service.PythonBridge.runCommand("CHECK", kode);
                if (respon.startsWith("OK|")) {
                    String[] bagian = respon.split("\\|");
                    String namaProduk = bagian[1].trim();                  
                    boolean sudahAda = false;
                    for (int i = 0; i < modelRestock.getRowCount(); i++) {
                        if (modelRestock.getValueAt(i, 0).toString().equals(kode)) {
                            int qtyLama = Integer.parseInt(modelRestock.getValueAt(i, 2).toString());
                            modelRestock.setValueAt(qtyLama + qty, i, 2);
                            sudahAda = true;
                            break;
                        }
                    }
                    if (!sudahAda) {
                        modelRestock.addRow(new Object[]{kode, namaProduk, qty});
                    }
                    
                    modelRestock.fireTableDataChanged();
                    txtBarcode.setText("");
                    txtQty.setText("");
                    txtBarcode.requestFocus();
                } else if (respon.equals("NOT_FOUND")) {
                    javax.swing.JOptionPane.showMessageDialog(dialogRestock, "Barcode '" + kode + "' Not found in the database! Add a new product.");
                } else {
                    javax.swing.JOptionPane.showMessageDialog(dialogRestock, "The response from Python is incorrect:\n[" + respon + "]");
                }
            } catch (NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(dialogRestock, "Quantity must be a whole number (e.g., 10)!");
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(dialogRestock, "Error: " + ex.getMessage());
            }
        };
        btnAddList.addActionListener(aksiTambahList);
        txtQty.addActionListener(aksiTambahList); // Bisa langsung tekan Enter di kolom Qty
        btnDone.addActionListener(e -> {
            if (tblRestock.isEditing()) {
                tblRestock.getCellEditor().stopCellEditing();
            }
            for (int i = 0; i < modelRestock.getRowCount(); i++) {
                String kode = modelRestock.getValueAt(i, 0).toString();
                String qty = modelRestock.getValueAt(i, 2).toString();
                String hasil = com.apps.service.PythonBridge.runCommand("RESTOCK", kode, qty);
                if (!hasil.equals("OK")) {
                    javax.swing.JOptionPane.showMessageDialog(dialogRestock, "Failed to Restock " + kode + ":\n[" + hasil + "]");
                    return;
                }
            }
            javax.swing.JOptionPane.showMessageDialog(dialogRestock, "All restock data has been successfully saved!");
            dialogRestock.dispose();
        });
        dialogRestock.setVisible(true);
    }

    private void openWindowNewProduct() {
        javax.swing.JDialog dialogNew = new javax.swing.JDialog(this, "Add New Product", true);
        dialogNew.setSize(400, 340);
        dialogNew.setLocationRelativeTo(this);
        dialogNew.setLayout(new java.awt.GridLayout(9, 1, 6, 6));
        ((javax.swing.JPanel) dialogNew.getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        javax.swing.JTextField txtNewBarcode = new javax.swing.JTextField();
        txtNewBarcode.putClientProperty("JTextField.placeholderText", "Scan / Input New Barcode...");
        javax.swing.JTextField txtNewName = new javax.swing.JTextField();
        txtNewName.putClientProperty("JTextField.placeholderText", "Product Name...");
        javax.swing.JTextField txtNewPrice = new javax.swing.JTextField();
        txtNewPrice.putClientProperty("JTextField.placeholderText", "Price (Rp)...");
        javax.swing.JTextField txtNewQty = new javax.swing.JTextField();
        txtNewQty.putClientProperty("JTextField.placeholderText", "Quantity Total...");
        javax.swing.JButton btnDoneNew = new javax.swing.JButton("Done");
        btnDoneNew.setBackground(new java.awt.Color(22, 163, 74));
        btnDoneNew.setForeground(java.awt.Color.WHITE);
        btnDoneNew.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        dialogNew.add(new javax.swing.JLabel("Barcode Produk Baru:"));
        dialogNew.add(txtNewBarcode);
        dialogNew.add(new javax.swing.JLabel("Product Name:"));
        dialogNew.add(txtNewName);
        dialogNew.add(new javax.swing.JLabel("Price (Rp):"));
        dialogNew.add(txtNewPrice);
        dialogNew.add(new javax.swing.JLabel("Quantity Total:"));
        dialogNew.add(txtNewQty);
        dialogNew.add(btnDoneNew);
        btnDoneNew.addActionListener(e -> {
            String kode = txtNewBarcode.getText().trim();
            String nama = txtNewName.getText().trim();
            String hargaStr = txtNewPrice.getText().trim();
            String qtyStr = txtNewQty.getText().trim();
            if (kode.isEmpty() || nama.isEmpty() || hargaStr.isEmpty() || qtyStr.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(dialogNew, "Please complete all fields!");
                return;
            }
            try {
                int harga = Integer.parseInt(hargaStr);
                int qty = Integer.parseInt(qtyStr);
                String hasil = com.apps.service.PythonBridge.runCommand(
                    "NEW_PRODUCT", kode, nama, String.valueOf(harga), String.valueOf(qty)
                );
                if (hasil.equals("OK")) {
                    javax.swing.JOptionPane.showMessageDialog(dialogNew, "The new product has been successfully saved to MongoDB.!");
                    dialogNew.dispose();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(dialogNew, "Failed to save (Barcode may already be registered): " + hasil);
                }
            } catch (NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(dialogNew, "Price and Quantity must be numbers!");
            }
        });
        dialogNew.setVisible(true);
    }
    
    private void openWindowDamaged() {
        String barcode = javax.swing.JOptionPane.showInputDialog(this, "Enter Damaged Product Barcode:");
        if (barcode == null || barcode.trim().isEmpty()) return;

        String qty = javax.swing.JOptionPane.showInputDialog(this, "Enter Damaged Quantity:");
        if (qty == null || qty.trim().isEmpty()) return;

        String note = javax.swing.JOptionPane.showInputDialog(this, "Damage Reason / Supplier Note:", "Damaged in Warehouse");
        String res = com.apps.service.PythonBridge.runCommand(
            "DAMAGED_STOCK", barcode.trim(), qty.trim(), note != null ? note.trim() : "Damaged"
        );

        if (res.equals("OK")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Damaged stock recorded and deducted from inventory!");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Failed:\n" + res, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportWarehouseHistory() {
        String dateFilter = javax.swing.JOptionPane.showInputDialog(
            this, "Enter Date Filter (YYYY-MM-DD) or leave blank for ALL history:", "2026-11-26"
        );
        if (dateFilter != null) {
            String res = com.apps.service.PythonBridge.runCommand("EXPORT_WAREHOUSE", dateFilter.trim());
            if (res.startsWith("OK|")) {
                javax.swing.JOptionPane.showMessageDialog(this, "Warehouse History CSV saved at:\n" + res.split("\\|", 2));
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Export failed:\n" + res, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void btnNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProductActionPerformed
        openWindowNewProduct();
    }//GEN-LAST:event_btnNewProductActionPerformed

    private void btnRestockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestockActionPerformed
        openWindowRestock();
    }//GEN-LAST:event_btnRestockActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new WarehouseFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewProduct;
    private javax.swing.JButton btnRestock;
    // End of variables declaration//GEN-END:variables
}
