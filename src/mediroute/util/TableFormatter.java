package mediroute.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for formatting tabular data with clean ASCII borders.
 */
public class TableFormatter {

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return "";

        int cols = headers.length;
        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) {
            widths[i] = headers[i].length();
        }

        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < Math.min(cols, row.length); i++) {
                    if (row[i] != null && row[i].length() > widths[i]) {
                        widths[i] = row[i].length();
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        String separator = buildSeparator(widths);

        sb.append(separator).append("\n");
        sb.append(buildRow(headers, widths)).append("\n");
        sb.append(separator).append("\n");

        if (rows != null && !rows.isEmpty()) {
            for (String[] row : rows) {
                sb.append(buildRow(row, widths)).append("\n");
            }
        } else {
            sb.append("| ").append(padRight("(no records)", sum(widths) + 3 * (cols - 1))).append(" |\n");
        }
        sb.append(separator);

        return sb.toString();
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private static String buildRow(String[] items, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String val = (i < items.length && items[i] != null) ? items[i] : "";
            sb.append(" ").append(padRight(val, widths[i])).append(" |");
        }
        return sb.toString();
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private static int sum(int[] arr) {
        int s = 0;
        for (int a : arr) s += a;
        return s;
    }
}
