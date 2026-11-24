package com.apps.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import com.apps.service.PythonBridge;

/**
 *
 * @author Lukas Rivaldo
 */
public class OwnerSetupFrame extends JFrame {

    public OwnerSetupFrame() {
        setTitle("Owner Account Management (Company Admin)");
        setSize(440, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- TAB 1: SIGN UP OWNER ---
        JPanel panelSignUp = new JPanel(new GridLayout(4, 2, 10, 12));
        panelSignUp.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtNewOwnerUser = new JTextField();
        txtNewOwnerUser.putClientProperty("JTextField.placeholderText", "Min 3 chars (letters/numbers)");

        JPasswordField txtNewOwnerPass = new JPasswordField();
        txtNewOwnerPass.putClientProperty("JTextField.placeholderText", "Min 8 characters");
        txtNewOwnerPass.putClientProperty("FlatLaf.style", "showRevealButton: true");

        JPasswordField txtNewManagerCard = new JPasswordField();
        txtNewManagerCard.putClientProperty("JTextField.placeholderText", "Number ID Card (Min 4 chars)");
        txtNewManagerCard.putClientProperty("FlatLaf.style", "showRevealButton: true");

        JButton btnRegisterOwner = new JButton("Sign Up Owner");
        btnRegisterOwner.setBackground(new Color(37, 99, 235));
        btnRegisterOwner.setForeground(Color.WHITE);
        btnRegisterOwner.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panelSignUp.add(new JLabel("Owner Username:"));
        panelSignUp.add(txtNewOwnerUser);
        panelSignUp.add(new JLabel("Owner Password:"));
        panelSignUp.add(txtNewOwnerPass);
        panelSignUp.add(new JLabel("Number ID Card (Manager):"));
        panelSignUp.add(txtNewManagerCard);
        panelSignUp.add(new JLabel(""));
        panelSignUp.add(btnRegisterOwner);

        // --- TAB 2: DELETE OWNER ACCOUNT ---
        JPanel panelDelete = new JPanel(new GridLayout(4, 2, 10, 12));
        panelDelete.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtDelOwnerUser = new JTextField();
        txtDelOwnerUser.putClientProperty("JTextField.placeholderText", "Target Owner Username");

        JPasswordField txtDelManagerCard = new JPasswordField();
        txtDelManagerCard.putClientProperty("JTextField.placeholderText", "Verify Manager ID Card");
        txtDelManagerCard.putClientProperty("FlatLaf.style", "showRevealButton: true");

        JPasswordField txtDelOwnerPass = new JPasswordField();
        txtDelOwnerPass.putClientProperty("JTextField.placeholderText", "Verify Owner Password");
        txtDelOwnerPass.putClientProperty("FlatLaf.style", "showRevealButton: true");

        JButton btnDeleteOwner = new JButton("Delete Account");
        btnDeleteOwner.setBackground(new Color(220, 38, 38));
        btnDeleteOwner.setForeground(Color.WHITE);
        btnDeleteOwner.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panelDelete.add(new JLabel("Owner Username:"));
        panelDelete.add(txtDelOwnerUser);
        panelDelete.add(new JLabel("Number ID Card (Manager):"));
        panelDelete.add(txtDelManagerCard);
        panelDelete.add(new JLabel("Owner Password:"));
        panelDelete.add(txtDelOwnerPass);
        panelDelete.add(new JLabel(""));
        panelDelete.add(btnDeleteOwner);

        tabbedPane.addTab("Sign Up Owner", panelSignUp);
        tabbedPane.addTab("Delete Account", panelDelete);
        add(tabbedPane, BorderLayout.CENTER);

        // Action: Sign Up Owner
        btnRegisterOwner.addActionListener(e -> {
            String user = txtNewOwnerUser.getText().trim();
            String pass = new String(txtNewOwnerPass.getPassword());
            String card = new String(txtNewManagerCard.getPassword());

            if (user.isEmpty() || pass.isEmpty() || card.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String res = PythonBridge.runCommand("SIGNUP_OWNER", user, pass, card);
            if (res.equals("OK")) {
                JOptionPane.showMessageDialog(this, "Owner account '" + user + "' successfully registered and authorized!");
                txtNewOwnerUser.setText("");
                txtNewOwnerPass.setText("");
                txtNewManagerCard.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed:\n" + res, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action: Delete Owner Account
        btnDeleteOwner.addActionListener(e -> {
            String user = txtDelOwnerUser.getText().trim();
            String card = new String(txtDelManagerCard.getPassword());
            String pass = new String(txtDelOwnerPass.getPassword());

            if (user.isEmpty() || card.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, Number ID Card, and Password are required for verification!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete Owner account '" + user + "'?",
                "Confirm Owner Deletion",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                String res = PythonBridge.runCommand("DELETE_OWNER", user, card, pass);
                if (res.equals("OK")) {
                    JOptionPane.showMessageDialog(this, "Owner account '" + user + "' has been verified and deleted.");
                    txtDelOwnerUser.setText("");
                    txtDelManagerCard.setText("");
                    txtDelOwnerPass.setText("");
                } else if (res.equals("INVALID_VERIFICATION")) {
                    JOptionPane.showMessageDialog(this, "Verification Failed! Invalid Manager ID Card or Owner Password.", "Security Alert", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Deletion failed:\n" + res, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(() -> new OwnerSetupFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
