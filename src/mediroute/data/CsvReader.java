package mediroute.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable RFC-4180-aware CSV Reader using Java standard libraries.
 * Handles quoted fields, escaped quotes, leading/trailing whitespace, and malformed lines.
 */
public class CsvReader {

    /**
     * Reads a CSV file and returns all records as a list of string arrays.
     * The first element is typically the header row.
     *
     * @param path Path to the CSV file
     * @return List of parsed records
     * @throws IOException If file reading fails
     */
    public static List<String[]> readCsv(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("CSV file not found: " + path.toAbsolutePath());
        }

        List<String[]> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                String[] row = parseLine(line);
                if (row.length > 0) {
                    records.add(row);
                }
            }
        }
        return records;
    }

    /**
     * Parses a single CSV line into an array of string tokens.
     */
    public static String[] parseLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Check if next char is also a quote (escaped quote "")
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++; // Skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    tokens.add(sb.toString().trim());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        tokens.add(sb.toString().trim());

        return tokens.toArray(new String[0]);
    }
}
