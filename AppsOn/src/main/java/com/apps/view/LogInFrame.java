package com.apps.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import com.apps.service.PythonBridge;

/**
 *
 * @author Lukas Rivaldo
 */
public class LogInFrame extends javax.swing.JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnSignUp;
    private JButton btnOwnerPortal;

    public LogInFrame() {
        setTitle("POS & Warehouse System - Authentication");
        setSize(420, 300);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitle = new JLabel("SYSTEM AUTHENTICATION");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBounds(105, 20, 230, 25);
        add(lblTitle);

        JLabel lblUser = new JLabel("Username");
        lblUser.setBounds(60, 65, 80, 25);
        add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(145, 65, 200, 30);
        txtUsername.putClientProperty("JTextField.placeholderText", "Enter username...");
        add(txtUsername);

        JLabel lblPass = new JLabel("Password");
        lblPass.setBounds(60, 108, 80, 25);
        add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(145, 108, 200, 30);
        txtPassword.putClientProperty("JTextField.placeholderText", "Enter password...");
        txtPassword.putClientProperty("FlatLaf.style", "showRevealButton: true");
        add(txtPassword);

        // Button Log In
        btnLogin = new JButton("Log In");
        btnLogin.setBounds(145, 155, 95, 34);
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(btnLogin);

        // Button Sign Up (Employee)
        btnSignUp = new JButton("Sign Up");
        btnSignUp.setBounds(250, 155, 95, 34);
        btnSignUp.setBackground(new Color(22, 163, 74));
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(btnSignUp);

        // Button Owner Setup Portal
        btnOwnerPortal = new JButton("Owner Account Management");
        btnOwnerPortal.setBounds(145, 202, 200, 28);
        btnOwnerPortal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnOwnerPortal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(btnOwnerPortal);

        // Action Listeners
        btnLogin.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());
        btnSignUp.addActionListener(e -> handleEmployeeSignUp());
        btnOwnerPortal.addActionListener(e -> new OwnerSetupFrame().setVisible(true));
    }

    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!");
            return;
        }

        String response = PythonBridge.runCommand("LOGIN", user, pass);

        if (response.startsWith("OK|")) {
            String[] parts = response.split("\\|", 4);
            String role = parts[1];
            PythonBridge.SESSION_TOKEN = parts[2];

            if (parts.length > 3 && parts[3].startsWith("WARNING|")) {
                String warningMsg = parts[4].substring(8).replace(" ; ", "\n- ");
                JOptionPane.showMessageDialog(
                    this,
                    "SERVER SECURITY AWARENESS ALERT:\n- " + warningMsg + "\n\nPlease secure your server configuration.",
                    "Security Warning",
                    JOptionPane.WARNING_MESSAGE
                );
            }

            JOptionPane.showMessageDialog(this, "Welcome, " + user + " (" + role + ")!");
            DashboardFrame dashboard = new DashboardFrame();
            dashboard.setLocationRelativeTo(null);
            dashboard.setVisible(true);
            this.dispose();

        } else if (response.equals("NOT_REGISTERED")) {
            JOptionPane.showMessageDialog(
                this,
                "Account '" + user + "' is not registered in the database!\nPlease click 'Sign Up' first to create an Employee account.",
                "Account Not Found",
                JOptionPane.WARNING_MESSAGE
            );
        } else if (response.startsWith("IP_NOT_APPROVED|")) {
            String ip = response.split("\\|")[1];
            JOptionPane.showMessageDialog(
                this,
                "SERVER ACCESS DENIED!\nYour device IP (" + ip + ") is not authorized on the server.\nPlease contact the IT / Server Administrator to approve your IP address.",
                "IT Approval Required",
                JOptionPane.ERROR_MESSAGE
            );
        } else if (response.equals("LOCKED")) {
            JOptionPane.showMessageDialog(
                this,
                "Account locked due to 5 consecutive failed login attempts!",
                "Security Lockout",
                JOptionPane.ERROR_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(this, "Login failed! Invalid username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEmployeeSignUp() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField txtNewUser = new JTextField();
        JPasswordField txtNewPass = new JPasswordField();

        panel.add(new JLabel("New Employee Username:"));
        panel.add(txtNewUser);
        panel.add(new JLabel("New Password (min 6 chars):"));
        panel.add(txtNewPass);

        int option = JOptionPane.showConfirmDialog(
            this, panel, "Employee Sign Up", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String newUser = txtNewUser.getText().trim();
            String newPass = new String(txtNewPass.getPassword());

            if (newUser.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty!");
                return;
            }

            String res = PythonBridge.runCommand("SIGNUP_EMPLOYEE", newUser, newPass);
            if (res.equals("OK_AUTHORIZED")) {
                JOptionPane.showMessageDialog(this, "Employee account '" + newUser + "' created! Your device IP is already authorized. You may Log In now.");
            } else if (res.startsWith("PENDING_IT|")) {
                String ip = res.split("\\|")[1];
                JOptionPane.showMessageDialog(
                    this,
                    "Account '" + newUser + "' registered!\nHowever, your device IP (" + ip + ") is new and requires IT / Server Administrator approval before you can Log In.",
                    "Pending IT Approval",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(this, "Sign Up failed:\n" + res, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 470, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static void main(String args[]) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            javax.swing.UIManager.put("Button.arc", 10);
            javax.swing.UIManager.put("Component.arc", 10);
            javax.swing.UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(() -> {
            new LogInFrame().setVisible(true);
        });
    }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

