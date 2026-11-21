package com.apps.view;

/**
 *
 * @author Lukas Rivaldo
 */
public class CasierFrame extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CasierFrame.class.getName());
        public CasierFrame() {
        initComponents();
        txtBarcode.setText("");
        txtBarcode.putClientProperty("JTextField.placeholderText", "Scan Barcode / Input Manual Code...");
        tblBelanja.setRowHeight(30);
        ((javax.swing.table.DefaultTableModel) tblBelanja.getModel()).setRowCount(0);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtBarcode = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBelanja = new javax.swing.JTable();
        btnDelete = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        btnBuy = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtBarcode.addActionListener(this::txtBarcodeActionPerformed);

        tblBelanja.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Code", "Product Name", "Price", "QTY", "Sub-Total"
            }
        ));
        jScrollPane1.setViewportView(tblBelanja);

        btnDelete.setText("Delete Item");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        lblTotal.setText("Total: Rp 0");

        btnBuy.setText("Buy");
        btnBuy.addActionListener(this::btnBuyActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBuy, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotal))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBuy)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private int total = 0;
    private void scanorSearchProduct() {
        String kode = txtBarcode.getText().trim();
        if (kode.isEmpty()) return;
        String respon = com.apps.service.PythonBridge.runCommand("CHECK", kode);
        if (respon.startsWith("OK|")) {
            String[] data = respon.split("\\|");
            String nama = data[1];
            int harga = Integer.parseInt(data[2]);
            int stok = Integer.parseInt(data[3]);
            String inputQty = javax.swing.JOptionPane.showInputDialog(
                this,
                "Product: " + nama + "\nPrice: Rp " + harga + "\nStock: " + stok + "\n\nWhat is the quantity purchased??",
                "Quantity Input",
                javax.swing.JOptionPane.QUESTION_MESSAGE
            );
            if (inputQty != null && !inputQty.trim().isEmpty()) {
                int qty = Integer.parseInt(inputQty.trim());
                if (qty > 0 && qty <= stok) {
                    com.apps.service.PythonBridge.runCommand("DELETE_STOCK", kode, String.valueOf(qty));
                    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblBelanja.getModel();
                    boolean ditemukan = false;
                    for (int i = 0; i < model.getRowCount(); i++) {
                        if (model.getValueAt(i, 0).toString().equals(kode)) {
                            int qtyLama = Integer.parseInt(model.getValueAt(i, 3).toString());
                            int qtyBaru = qtyLama + qty;
                            model.setValueAt(qtyBaru, i, 3);
                            model.setValueAt(qtyBaru * harga, i, 4);
                            ditemukan = true;
                            break;
                        }
                    }
                    if (!ditemukan) {
                        model.addRow(new Object[]{kode, nama, harga, qty, harga * qty});
                    }
                    subTotal();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Quantity is invalid or exceeds stock!");
                }
            }
        } else if (respon.equals("NOT_FOUND")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Product code not found in the database!");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, respon);
        }
        txtBarcode.setText("");
        txtBarcode.requestFocus();
    }

    private void subTotal() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblBelanja.getModel();
        total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += Integer.parseInt(model.getValueAt(i, 4).toString());
        }
        lblTotal.setText("Total: Rp " + total);
    }

    private void openWindowDeleteItem() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblBelanja.getModel();
        if (model.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "The shopping list is still empty!");
            return;
        }
        javax.swing.JDialog dialogDelete = new javax.swing.JDialog(this, "Delete Item - Verification Manager", true);
        dialogDelete.setSize(380, 230);
        dialogDelete.setLocationRelativeTo(this);
        dialogDelete.setLayout(new java.awt.GridLayout(5, 1, 8, 8));
        ((javax.swing.JPanel) dialogDelete.getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        javax.swing.JPasswordField txtCard = new javax.swing.JPasswordField();
        txtCard.putClientProperty("JTextField.placeholderText", "Scan Card Manager...");
        javax.swing.JTextField txtDelBarcode = new javax.swing.JTextField();
        txtDelBarcode.putClientProperty("JTextField.placeholderText", "Scan Barcode Product...");
        javax.swing.JButton btnConfirmDel = new javax.swing.JButton("Delete Item");
        btnConfirmDel.setBackground(new java.awt.Color(220, 38, 38));
        btnConfirmDel.setForeground(java.awt.Color.WHITE);
        dialogDelete.add(new javax.swing.JLabel("Card Manager:"));
        dialogDelete.add(txtCard);
        dialogDelete.add(new javax.swing.JLabel("Deleted Product Barcode:"));
        dialogDelete.add(txtDelBarcode);
        dialogDelete.add(btnConfirmDel);
        btnConfirmDel.addActionListener(e -> {
            String card = new String(txtCard.getPassword()).trim();
            String delBarcode = txtDelBarcode.getText().trim();
            int targetRow = -1;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).toString().equals(delBarcode)) {
                    targetRow = i;
                    break;
                }
            }
            if (targetRow == -1) {
                javax.swing.JOptionPane.showMessageDialog(dialogDelete, "That barcode is not on the shopping list.!");
                return;
            }
            String qty = model.getValueAt(targetRow, 3).toString();
            String hasil = com.apps.service.PythonBridge.runCommand("DELETE_ITEM", card, delBarcode, qty);
            if (hasil.equals("OK")) {
                model.removeRow(targetRow);
                subTotal();
                javax.swing.JOptionPane.showMessageDialog(dialogDelete, "Item successfully removed & stock returned!");
                dialogDelete.dispose();
                txtBarcode.requestFocus();
            } else {
                javax.swing.JOptionPane.showMessageDialog(dialogDelete, "Card Manager error! Access denied.", "Unauthorized", javax.swing.JOptionPane.ERROR_MESSAGE);
                txtCard.setText("");
                txtCard.requestFocus();
            }
        });
        dialogDelete.setVisible(true);
    }

    private void openWindowBuy() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblBelanja.getModel();
        if (model.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "No items have been purchased yet!");
            return;
        }
        javax.swing.JDialog dialogBuy = new javax.swing.JDialog(this, "Transaction Payment", true);
        dialogBuy.setSize(380, 240);
        dialogBuy.setLocationRelativeTo(this);
        dialogBuy.setLayout(new java.awt.GridLayout(4, 1, 10, 10));
        ((javax.swing.JPanel) dialogBuy.getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        javax.swing.JLabel lblDialogTotal = new javax.swing.JLabel("Total: Rp " + total);
        lblDialogTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        javax.swing.JTextField txtUangCustomer = new javax.swing.JTextField();
        txtUangCustomer.putClientProperty("JTextField.placeholderText", "Enter the customer's payment amount...");
        javax.swing.JButton btnDoneBuy = new javax.swing.JButton("Done");
        btnDoneBuy.setBackground(new java.awt.Color(22, 163, 74));
        btnDoneBuy.setForeground(java.awt.Color.WHITE);
        btnDoneBuy.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        dialogBuy.add(lblDialogTotal);
        dialogBuy.add(new javax.swing.JLabel("Amount Paid by the Customer:"));
        dialogBuy.add(txtUangCustomer);
        dialogBuy.add(btnDoneBuy);

        btnDoneBuy.addActionListener(e -> {
            try {
                int uangBayar = Integer.parseInt(txtUangCustomer.getText().trim());
                if (uangBayar < total) {
                    int kurang = total - uangBayar;
                    javax.swing.JOptionPane.showMessageDialog(
                        dialogBuy,
                        "Transaction Declined! Insufficient funds Rp " + kurang + ".\nPlease insert the exact amount or more..",
                        "Insufficient Funds",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                    txtUangCustomer.requestFocus();
                    return;
                }
                int kembalian = uangBayar - total;
                StringBuilder itemsJson = new StringBuilder("[");
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (i > 0) itemsJson.append(",");
                    itemsJson.append(String.format(
                        "{\"barcode\":\"%s\",\"product_name\":\"%s\",\"price\":%s,\"qty\":%s,\"subtotal\":%s}",
                        model.getValueAt(i, 0), model.getValueAt(i, 1),
                        model.getValueAt(i, 2), model.getValueAt(i, 3), model.getValueAt(i, 4)
                    ));
                }
                itemsJson.append("]");
                com.apps.service.PythonBridge.runCommand(
                    "TRANSACTION", itemsJson.toString(),
                    String.valueOf(total), String.valueOf(uangBayar), String.valueOf(kembalian)
                );
                dialogBuy.dispose();
                openWindowKembalian(kembalian);
            } catch (NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(dialogBuy, "Enter a valid monetary amount!");
            }
        });
        dialogBuy.setVisible(true);
    }

    private void openWindowKembalian(int change) {
        javax.swing.JDialog dialogChange = new javax.swing.JDialog(this, "Transaction Done", true);
        dialogChange.setSize(360, 200);
        dialogChange.setLocationRelativeTo(this);
        dialogChange.setLayout(new java.awt.GridLayout(3, 1, 10, 10));
        ((javax.swing.JPanel) dialogChange.getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        javax.swing.JLabel lblTitle = new javax.swing.JLabel("Total Change for Customer:", javax.swing.SwingConstants.CENTER);
        javax.swing.JLabel lblNilaiKembalian = new javax.swing.JLabel("Rp " + change, javax.swing.SwingConstants.CENTER);
        lblNilaiKembalian.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        lblNilaiKembalian.setForeground(new java.awt.Color(22, 163, 74));

        javax.swing.JButton btnSelesaiSemua = new javax.swing.JButton("Done");
        btnSelesaiSemua.setBackground(new java.awt.Color(37, 99, 235));
        btnSelesaiSemua.setForeground(java.awt.Color.WHITE);
        btnSelesaiSemua.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

        dialogChange.add(lblTitle);
        dialogChange.add(lblNilaiKembalian);
        dialogChange.add(btnSelesaiSemua);

        btnSelesaiSemua.addActionListener(e -> {
            ((javax.swing.table.DefaultTableModel) tblBelanja.getModel()).setRowCount(0);
            total = 0;
            lblTotal.setText("Total: Rp 0");
            dialogChange.dispose();
            txtBarcode.requestFocus();
        });

        dialogChange.setVisible(true);
    }

    private void btnBuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuyActionPerformed
        openWindowBuy();
    }//GEN-LAST:event_btnBuyActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        openWindowDeleteItem();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void txtBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBarcodeActionPerformed
        scanorSearchProduct();
    }//GEN-LAST:event_txtBarcodeActionPerformed

    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        //</editor-fold>
        java.awt.EventQueue.invokeLater(() -> new CasierFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuy;
    private javax.swing.JButton btnDelete;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTable tblBelanja;
    private javax.swing.JTextField txtBarcode;
    // End of variables declaration//GEN-END:variables
}
