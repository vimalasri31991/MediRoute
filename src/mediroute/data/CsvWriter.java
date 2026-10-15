package mediroute.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reusable CSV Writer using Java standard libraries.
 * Handles quoting of fields containing commas, quotes, or newlines.
 */
public class CsvWriter {

    /**
     * Writes records to a CSV file.
     *
     * @param path Target CSV file path
     * @param header Header row tokens
     * @param rows List of row tokens
     * @throws IOException If writing fails
     */
    public static void writeCsv(Path path, String[] header, List<String[]> rows) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            if (header != null && header.length > 0) {
                writer.write(formatRow(header));
                writer.newLine();
            }
            for (String[] row : rows) {
                writer.write(formatRow(row));
                writer.newLine();
            }
        }
    }

    private static String formatRow(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            String field = fields[i] == null ? "" : fields[i];
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                sb.append('"').append(field.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(field);
            }
        }
        return sb.toString();
    }
}
