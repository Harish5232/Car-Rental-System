package essentials;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class envLoader {
    private static final Map<String, String> envMap = new HashMap<>();

    // Load only once
    static {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // ignore comments and empty lines
                if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    // Strip surrounding single/double quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envMap.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Could not load .env file: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return envMap.get(key);
    }
}
