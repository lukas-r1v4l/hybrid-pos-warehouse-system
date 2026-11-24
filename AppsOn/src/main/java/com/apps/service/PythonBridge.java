package com.apps.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PythonBridge {

    private static final String PYTHON_EXE = "python";
    private static final File SCRIPT_FILE = new File("../python_engine/bridge_cli.py").getAbsoluteFile();

    public static String SESSION_TOKEN = "";

    private static final Set<String> PUBLIC_ACTIONS = Set.of(
        "SIGNUP_EMPLOYEE", "SIGNUP_OWNER", "DELETE_OWNER", "IT_APPROVE_IP", "LOGIN"
    );
    private static final Set<String> ALLOWED_ACTIONS = Set.of(
        "SIGNUP_EMPLOYEE", "SIGNUP_OWNER", "DELETE_OWNER", "IT_APPROVE_IP", "LOGIN",
        "CHECK", "RESTOCK", "NEW_PRODUCT", "DELETE_STOCK", "DELETE_ITEM", "VERIFY_MANAGER", "TRANSACTION"
    );

    public static String runCommand(String... args) {
        if (args == null || args.length == 0 || !ALLOWED_ACTIONS.contains(args[0])) {
            return "SECURITY_ERROR|Action is not permitted!";
        }

        StringBuilder output = new StringBuilder();
        try {
            List<String> command = new ArrayList<>();
            command.add(PYTHON_EXE);
            command.add(SCRIPT_FILE.getAbsolutePath());
            command.add(args[0]);

            if (!PUBLIC_ACTIONS.contains(args[0])) {
                command.add(SESSION_TOKEN);
            }

            for (int i = 1; i < args.length; i++) {
                command.add(args[i]);
            }

ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(SCRIPT_FILE.getParentFile());
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
