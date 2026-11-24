package com.apps.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import com.apps.service.PythonBridge;
/**
 *
 * @author Lukas Rivaldo
 */
public class ITAdminFrame extends JFrame {

    private DefaultTableModel modelDevices;
    private DefaultTableModel modelLogs;

    public ITAdminFrame() {
        setTitle("IT Security & Server Administration Console");
        setSize(760, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- TAB 1: TERMINAL IP & DEVICE APPROVALS ---
        JPanel panelDevices = new JPanel(new BorderLayout(8, 8));
        panelDevices.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelDevices = new DefaultTableModel(
            new Object[]{"IP Address", "Device ID (MachineGuid)", "PC Name", "Requested By", "Status"}, 0
        );
        JTable tblDevices = new JTable(modelDevices);
        tblDevices.setRowHeight(28);

        JPanel panelDevButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefreshDev = new JButton("Refresh");
        JButton btnApproveIP = new JButton("Approve Selected IP");
        btnApproveIP.setBackground(new Color(22, 163, 74));
        btnApproveIP.setForeground(Color.WHITE);
        btnApproveIP.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnRevokeIP = new JButton("Revoke / Block IP");
        btnRevokeIP.setBackground(new Color(220, 38, 38));
        btnRevokeIP.setForeground(Color.WHITE);
        btnRevokeIP.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelDevButtons.add(btnRefreshDev);
        panelDevButtons.add(btnApproveIP);
        panelDevButtons.add(btnRevokeIP);

        panelDevices.add(new JScrollPane(tblDevices), BorderLayout.CENTER);
        panelDevices.add(panelDevButtons, BorderLayout.SOUTH);

        // --- TAB 2: SECURITY AUDIT LOGS ---
        JPanel panelLogs = new JPanel(new BorderLayout(8, 8));
        panelLogs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelLogs = new DefaultTableModel(
            new Object[]{"Timestamp", "Event Type", "Source IP", "Security Detail"}, 0
        );
        JTable tblLogs = new JTable(modelLogs);
        tblLogs.setRowHeight(26);

        JPanel panelLogButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefreshLogs = new JButton("Refresh Logs");
        panelLogButtons.add(btnRefreshLogs);

        panelLogs.add(new JScrollPane(tblLogs), BorderLayout.CENTER);
        panelLogs.add(panelLogButtons, BorderLayout.SOUTH);

        tabs.addTab("Device & IP Access Control", panelDevices);
        tabs.addTab("Security Audit Logs (MongoDB)", panelLogs);
        add(tabs, BorderLayout.CENTER);

        // Actions
        btnRefreshDev.addActionListener(e -> loadDevices());
        btnRefreshLogs.addActionListener(e -> loadLogs());

        btnApproveIP.addActionListener(e -> {
            int row = tblDevices.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a device row first!");
                return;
            }
            String ip = modelDevices.getValueAt(row, 0).toString();
            String res = PythonBridge.runCommand("IT_APPROVE_IP", ip);
            if (res.equals("OK")) {
                JOptionPane.showMessageDialog(this, "IP Address " + ip + " is now AUTHORIZED!");
                loadDevices();
                loadLogs();
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + res);
            }
        });

        btnRevokeIP.addActionListener(e -> {
            int row = tblDevices.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a device row first!");
                return;
            }
            String ip = modelDevices.getValueAt(row, 0).toString();
            String res = PythonBridge.runCommand("IT_REVOKE_IP", ip);
            if (res.equals("OK")) {
                JOptionPane.showMessageDialog(this, "Access for IP " + ip + " has been REVOKED!");
                loadDevices();
                loadLogs();
            }
        });

        loadDevices();
        loadLogs();
    }

    private void loadDevices() {
        modelDevices.setRowCount(0);
        String res = PythonBridge.runCommand("IT_GET_DEVICES");
        if (!res.equals("EMPTY") && !res.startsWith("ERROR")) {
            for (String line : res.split("\n")) {
                String[] cols = line.split("\\|", 5);
                if (cols.length == 5) modelDevices.addRow(cols);
            }
        }
    }

    private void loadLogs() {
        modelLogs.setRowCount(0);
        String res = PythonBridge.runCommand("IT_GET_LOGS");
        if (!res.equals("EMPTY") && !res.startsWith("ERROR")) {
            for (String line : res.split("\n")) {
                String[] cols = line.split("\\|", 4);
                if (cols.length == 4) modelLogs.addRow(cols);
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
    public static void openWithITLogin(java.awt.Component parent) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField txtITUser = new JTextField();
        JPasswordField txtITPass = new JPasswordField();
        panel.add(new JLabel("MongoDB IT Admin Username:"));
        panel.add(txtITUser);
        panel.add(new JLabel("MongoDB IT Admin Password:"));
        panel.add(txtITPass);

        int opt = JOptionPane.showConfirmDialog(
            parent, panel, "IT Administrator Authentication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (opt == JOptionPane.OK_OPTION) {
            String res = PythonBridge.runCommand("IT_LOGIN", txtITUser.getText().trim(), new String(txtITPass.getPassword()));
            if (res.equals("OK")) {
                new ITAdminFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(parent, "Access Denied!\nDetail: " + res, "IT Security Alert", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
