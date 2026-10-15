package mediroute.algorithms.strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Knuth-Morris-Pratt (KMP) string matching algorithm.
 * Time Complexity: O(N + M) where N is text length, M is pattern length.
 * Space Complexity: O(M) for the Longest Proper Prefix which is also Suffix (LPS) array.
 */
public class KMPMatcher {

    /**
     * Computes the LPS (Longest Prefix Suffix) array for the given pattern.
     * lps[i] stores the length of the longest proper prefix of pattern[0..i]
     * that is also a suffix of pattern[0..i].
     */
    public static int[] computeLPS(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new int[0];
        }
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1]; // Fallback to previous longest prefix
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    /**
     * Searches for all occurrences of pattern in text using KMP.
     *
     * @param text The text to search within
     * @param pattern The pattern to search for
     * @return List of 0-based starting indices where pattern matches
     */
    public static List<Integer> search(String text, String pattern) {
        return search(text, pattern, false);
    }

    /**
     * Searches for occurrences of pattern in text, with optional case insensitivity.
     */
    public static List<Integer> search(String text, String pattern, boolean ignoreCase) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return matches;
        }

        String searchSource = ignoreCase ? text.toLowerCase() : text;
        String searchPattern = ignoreCase ? pattern.toLowerCase() : pattern;

        int[] lps = computeLPS(searchPattern);
        int n = searchSource.length();
        int m = searchPattern.length();

        int i = 0; // index in text
        int j = 0; // index in pattern

        while (i < n) {
            if (searchPattern.charAt(j) == searchSource.charAt(i)) {
                i++;
                j++;
            }

            if (j == m) {
                matches.add(i - j); // Match found at (i - j)
                j = lps[j - 1];
            } else if (i < n && searchPattern.charAt(j) != searchSource.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return matches;
    }
}
