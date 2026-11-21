package com.apps.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PythonBridge {

    // Path Python 3.14 tempat pymongo & python-dotenv terinstal
    private static final String PYTHON_EXE = "C:\\Users\\Lukas Rivaldo\\AppData\\Local\\Python\\pythoncore-3.14-64\\python.exe";
    private static final String SCRIPT_PATH = "E:\\Code\\PROJECT\\Mesin Manajemen & Kasir\\python_engine\\bridge_cli.py";

    public static String runCommand(String... args) {
        StringBuilder output = new StringBuilder();
        try {
            List<String> command = new ArrayList<>();
            command.add(PYTHON_EXE);
            command.add(SCRIPT_PATH);
            command.addAll(Arrays.asList(args));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File("E:\\Code\\PROJECT\\Mesin Manajemen & Kasir\\python_engine"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
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



