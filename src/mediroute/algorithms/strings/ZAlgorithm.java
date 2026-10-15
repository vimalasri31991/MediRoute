package mediroute.algorithms.strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Z-Algorithm (Linear-Time Pattern Matching and Prefix Analysis).
 * Time Complexity: O(N) where N is string length.
 * Space Complexity: O(N) for the Z-array.
 */
public class ZAlgorithm {

    /**
     * Computes the Z-array for string s.
     * Z[i] is the length of the longest substring starting from s[i]
     * that is also a prefix of s.
     */
    public static int[] calculateZ(String s) {
        if (s == null || s.isEmpty()) {
            return new int[0];
        }

        int n = s.length();
        int[] z = new int[n];
        z[0] = n; // By convention, prefix matches entire string at index 0

        int l = 0, r = 0; // Current [l, r] match interval (Z-box)

        for (int i = 1; i < n; i++) {
            if (i > r) {
                // Outside current Z-box: compare naively
                l = r = i;
                while (r < n && s.charAt(r - l) == s.charAt(r)) {
                    r++;
                }
                z[i] = r - l;
                r--;
            } else {
                // Inside current Z-box: reuse previously computed Z-values
                int k = i - l;
                if (z[k] < r - i + 1) {
                    z[i] = z[k];
                } else {
                    l = i;
                    while (r < n && s.charAt(r - l) == s.charAt(r)) {
                        r++;
                    }
                    z[i] = r - l;
                    r--;
                }
            }
        }
        return z;
    }

    /**
     * Searches for pattern in text using the Z-algorithm.
     * Concatenates pattern + '\0' + text and checks Z-values >= pattern.length().
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return matches;
        }

        String combined = pattern + "\u0000" + text;
        int[] z = calculateZ(combined);
        int m = pattern.length();

        for (int i = m + 1; i < combined.length(); i++) {
            if (z[i] >= m) {
                matches.add(i - (m + 1));
            }
        }
        return matches;
    }

    /**
     * Analyzes repeated prefixes or periods in medical descriptions.
     * Returns the length and prefix string of the longest repeating prefix occurrence.
     */
    public static PrefixAnalysisResult analyzeRepeatedPrefix(String text) {
        if (text == null || text.length() < 2) {
            return new PrefixAnalysisResult("", 0, -1);
        }

        int[] z = calculateZ(text);
        int maxLen = 0;
        int bestIndex = -1;

        for (int i = 1; i < text.length(); i++) {
            if (z[i] > maxLen) {
                maxLen = z[i];
                bestIndex = i;
            }
        }

        if (maxLen > 0 && bestIndex != -1) {
            String prefix = text.substring(0, maxLen);
            return new PrefixAnalysisResult(prefix, maxLen, bestIndex);
        }
        return new PrefixAnalysisResult("", 0, -1);
    }

    public static class PrefixAnalysisResult {
        private final String matchingPrefix;
        private final int length;
        private final int repeatedAtIndex;

        public PrefixAnalysisResult(String matchingPrefix, int length, int repeatedAtIndex) {
            this.matchingPrefix = matchingPrefix;
            this.length = length;
            this.repeatedAtIndex = repeatedAtIndex;
        }

        public String getMatchingPrefix() { return matchingPrefix; }
        public int getLength() { return length; }
        public int getRepeatedAtIndex() { return repeatedAtIndex; }

        @Override
        public String toString() {
            if (length == 0) return "No repeating prefix found.";
            return String.format("Longest repeating prefix: \"%s\" (length %d, occurs at index %d)",
                    matchingPrefix, length, repeatedAtIndex);
        }
    }
}
