package com.apps.view;

/**
 *
 * @author Lukas Rivaldo
 */
public class DashboardFrame extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardFrame.class.getName());
public DashboardFrame() {
    setTitle("Dashboard Frame");
    initComponents();
}

    private void jalankanPythonOtomatis() {
        new Thread(() -> {
        String hasil = com.apps.service.PythonBridge.runCommand("admin");
        java.awt.EventQueue.invokeLater(() -> {
            jLabel1.setText(hasil);
        });
    }).start();
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnCasier = new javax.swing.JButton();
        btnWarehoue = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Welcome to Dashboard!");

        btnCasier.setText("Casier");
        btnCasier.addActionListener(this::btnCasierActionPerformed);

        btnWarehoue.setText("Warehouse");
        btnWarehoue.addActionListener(this::btnWarehoueActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(152, 152, 152)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnWarehoue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCasier, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addComponent(jLabel1)))
                .addContainerGap(138, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(122, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(btnCasier)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnWarehoue)
                .addGap(92, 92, 92))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCasierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCasierActionPerformed
        CasierFrame casier = new CasierFrame();
        casier.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnCasierActionPerformed

    private void btnWarehoueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWarehoueActionPerformed
        WarehouseFrame warehouse = new WarehouseFrame();
        warehouse.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnWarehoueActionPerformed

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

        java.awt.EventQueue.invokeLater(() -> new DashboardFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCasier;
    private javax.swing.JButton btnWarehoue;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
