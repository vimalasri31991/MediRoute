package mediroute.algorithms.strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Rabin-Karp String Matching Algorithm using a rolling polynomial hash.
 * Candidate matches are rigorously verified using character-by-character comparison to prevent
 * hash-collision false positives.
 *
 * Time Complexity: Average O(N + M), Worst-case O(N * M)
 * Space Complexity: O(1) auxiliary space.
 */
public class RabinKarpMatcher {

    private static final long BASE = 257L;
    private static final long MOD = 1_000_000_007L;

    /**
     * Searches for occurrences of pattern in text using rolling hash with candidate verification.
     *
     * @param text Text to search within (e.g. batch codes, catalog descriptions)
     * @param pattern Pattern to match (e.g. product code, shipment prefix)
     * @return List of 0-based match start indices
     */
    public static List<Integer> search(String text, String pattern) {
        return search(text, pattern, false);
    }

    public static List<Integer> search(String text, String pattern, boolean ignoreCase) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return matches;
        }

        String t = ignoreCase ? text.toLowerCase() : text;
        String p = ignoreCase ? pattern.toLowerCase() : pattern;

        int n = t.length();
        int m = p.length();

        // Compute BASE^(m-1) % MOD
        long highPower = 1;
        for (int i = 0; i < m - 1; i++) {
            highPower = (highPower * BASE) % MOD;
        }

        // Compute initial hash of pattern and first window of text
        long patternHash = 0;
        long windowHash = 0;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + p.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + t.charAt(i)) % MOD;
        }

        for (int i = 0; i <= n - m; i++) {
            // Check if hash matches
            if (windowHash == patternHash) {
                // Verification step to guard against hash collisions
                boolean matched = true;
                for (int j = 0; j < m; j++) {
                    if (t.charAt(i + j) != p.charAt(j)) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    matches.add(i);
                }
            }

            // Roll hash for next window
            if (i < n - m) {
                // Remove leftmost character, multiply by BASE, add new character
                windowHash = (windowHash - t.charAt(i) * highPower) % MOD;
                if (windowHash < 0) {
                    windowHash += MOD;
                }
                windowHash = (windowHash * BASE + t.charAt(i + m)) % MOD;
            }
        }

        return matches;
    }
}
