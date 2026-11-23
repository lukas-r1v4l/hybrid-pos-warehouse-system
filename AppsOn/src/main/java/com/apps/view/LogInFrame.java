package com.apps.view;

/**
 *
 * @author Lukas Rivaldo
 */
public class LogInFrame extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LogInFrame.class.getName());

    public LogInFrame() {
        initComponents();
        cekWadahSetupAwal();
        txtUsername.putClientProperty("JTextField.placeholderText", "username...");
        txtPassword.putClientProperty("JTextField.placeholderText", "password...");
        txtPassword.putClientProperty("FlatLaf.style", "showRevealButton: true");
        btnLogin.setBackground(new java.awt.Color(37, 99, 235));
        btnLogin.setForeground(java.awt.Color.WHITE);
        btnLogin.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void cekWadahSetupAwal() {
        String status = com.apps.service.PythonBridge.runCommand("CHECK_SETUP");
        if (status.equals("NEED_SETUP")) {
            javax.swing.JTextField txtNewUser = new javax.swing.JTextField();
            javax.swing.JPasswordField txtNewPass = new javax.swing.JPasswordField();
            javax.swing.JPasswordField txtNewCard = new javax.swing.JPasswordField();

            Object[] form = {
                "Welcome! Create Your Owner Account & Manager Card:",
                "New Username (letters/numbers, min. 3 characters):", txtNewUser,
                "Password Baru (min 8 karakter):", txtNewPass,
                "New Manager Card Code (min. 4 characters):", txtNewCard
            };

            int option = javax.swing.JOptionPane.showConfirmDialog(
                this, form, "First-Time Security Setup", javax.swing.JOptionPane.OK_CANCEL_OPTION
            );

            if (option == javax.swing.JOptionPane.OK_OPTION) {
                String res = com.apps.service.PythonBridge.runCommand(
                    "SETUP_OWNER",
                    txtNewUser.getText().trim(),
                    new String(txtNewPass.getPassword()),
                    new String(txtNewCard.getPassword())
                );
                if (res.equals("OK")) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Owner Account & Manager Card have been successfully created and encrypted!");
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Setup Fail: " + res);
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        } else if (!status.equals("READY")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Status Setup: [" + status + "]");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Username");
        jLabel1.setAutoscrolls(true);

        jLabel2.setText("Password");
        jLabel2.setAutoscrolls(true);

        txtUsername.addActionListener(this::txtUsernameActionPerformed);

        btnLogin.setText("Log In");
        btnLogin.setAutoscrolls(true);
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(81, 81, 81)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnLogin)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPassword)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(101, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLogin)
                .addContainerGap(116, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        txtPassword.requestFocus();
    }//GEN-LAST:event_txtUsernameActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        String respon = com.apps.service.PythonBridge.runCommand("LOGIN", user, pass);

        if (respon.startsWith("OK|")) {
            String[] parts = respon.split("\\|", 3);
            com.apps.service.PythonBridge.SESSION_TOKEN = parts[1];

            if (parts.length > 2 && parts[2].startsWith("WARNING|")) {
                String peringatan = parts[2].substring(8).replace(" ; ", "\n- ");
                javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "WARNING:\n- " + peringatan + "\n\nPlease improve your server security to prevent hacking.!",
                    "Security Awareness Alert",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );
            }

            DashboardFrame dashboard = new DashboardFrame();
            dashboard.setLocationRelativeTo(null);
            dashboard.setVisible(true);
            this.dispose();
        } else if (respon.equals("LOCKED")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Account locked due to 5 failed attempts!", "Security Lock", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else if (respon.equals("DEVICE_BLOCKED")) {
            javax.swing.JOptionPane.showMessageDialog(this, "This computer is not registered on the server!", "Unauthorized Device", javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Login failed! Response: " + respon);
        }
    }//GEN-LAST:event_btnLoginActionPerformed
    public static void main(String args[]) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            javax.swing.UIManager.put("Button.arc", 10);
            javax.swing.UIManager.put("Component.arc", 10);
            javax.swing.UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            LogInFrame login = new LogInFrame();
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
