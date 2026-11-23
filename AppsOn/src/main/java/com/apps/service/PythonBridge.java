package com.apps.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PythonBridge {

    private static final String PYTHON_EXE = "C:\\Users\\Lukas Rivaldo\\AppData\\Local\\Python\\pythoncore-3.14-64\\python.exe";
    private static final String SCRIPT_PATH = "E:\\Code\\PROJECT\\Mesin Manajemen & Kasir\\python_engine\\bridge_cli.py";
    
    // Token sesi aktif disimpan di memori RAM setelah login berhasil
    public static String SESSION_TOKEN = "";

    private static final Set<String> PUBLIC_ACTIONS = Set.of("CHECK_SETUP", "SETUP_OWNER", "LOGIN");
    private static final Set<String> ALLOWED_ACTIONS = Set.of(
        "CHECK_SETUP", "SETUP_OWNER", "LOGIN",
        "CHECK", "RESTOCK", "NEW_PRODUCT", "DELETE_STOCK", "DELETE_ITEM", "TRANSACTION"
    );

    public static String runCommand(String... args) {
        if (args == null || args.length == 0 || !ALLOWED_ACTIONS.contains(args[0])) {
            return "SECURITY_ERROR|Perintah tidak diizinkan!";
        }

        StringBuilder output = new StringBuilder();
        try {
            List<String> command = new ArrayList<>();
            command.add(PYTHON_EXE);
            command.add(SCRIPT_PATH);
            command.add(args[0]); // Nama aksi

            if (!PUBLIC_ACTIONS.contains(args[0])) {

                command.add(SESSION_TOKEN);
            }

            for (int i = 1; i < args.length; i++) {
                command.add(args[i]);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("E:\\Code\\PROJECT\\Mesin Manajemen & Kasir\\python_engine"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
        return output.toString().trim();
    }
}
