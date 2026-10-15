package mediroute.algorithms.suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Standard Suffix Array implementation with Prefix-Doubling Construction and Substring Search.
 * Time Complexity: O(N log^2 N) construction, O(M log N) substring queries.
 * Space Complexity: O(N).
 */
public class SuffixArray {

    private final String text;
    private final int n;
    private final int[] sa; // sa[i] = starting index of the i-th lexicographically smallest suffix

    public SuffixArray(String text) {
        this.text = text == null ? "" : text;
        this.n = this.text.length();
        this.sa = buildSuffixArray(this.text);
    }

    private static class SuffixRank implements Comparable<SuffixRank> {
        int index;
        int rank0;
        int rank1;

        @Override
        public int compareTo(SuffixRank o) {
            if (this.rank0 != o.rank0) {
                return Integer.compare(this.rank0, o.rank0);
            }
            return Integer.compare(this.rank1, o.rank1);
        }
    }

    private static int[] buildSuffixArray(String s) {
        int len = s.length();
        if (len == 0) return new int[0];
        if (len == 1) return new int[]{0};

        SuffixRank[] suffixes = new SuffixRank[len];
        for (int i = 0; i < len; i++) {
            suffixes[i] = new SuffixRank();
            suffixes[i].index = i;
            suffixes[i].rank0 = s.charAt(i);
            suffixes[i].rank1 = (i + 1 < len) ? s.charAt(i + 1) : -1;
        }

        Arrays.sort(suffixes);
        int[] ind = new int[len];

        for (int k = 4; k < 2 * len; k *= 2) {
            int rank = 0;
            int prevRank0 = suffixes[0].rank0;
            int prevRank1 = suffixes[0].rank1;
            suffixes[0].rank0 = rank;
            ind[suffixes[0].index] = 0;

            for (int i = 1; i < len; i++) {
                if (suffixes[i].rank0 == prevRank0 && suffixes[i].rank1 == prevRank1) {
                    suffixes[i].rank0 = rank;
                } else {
                    prevRank0 = suffixes[i].rank0;
                    prevRank1 = suffixes[i].rank1;
                    rank++;
                    suffixes[i].rank0 = rank;
                }
                ind[suffixes[i].index] = i;
            }

            for (int i = 0; i < len; i++) {
                int nextIndex = suffixes[i].index + k / 2;
                suffixes[i].rank1 = (nextIndex < len) ? suffixes[ind[nextIndex]].rank0 : -1;
            }

            Arrays.sort(suffixes);
        }

        int[] result = new int[len];
        for (int i = 0; i < len; i++) {
            result[i] = suffixes[i].index;
        }
        return result;
    }

    public int[] getArray() {
        return Arrays.copyOf(sa, sa.length);
    }

    public String getText() {
        return text;
    }

    /**
     * Binary search to find all occurrences of pattern in text using the suffix array.
     */
    public List<Integer> search(String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        if (pattern == null || pattern.isEmpty() || n == 0) {
            return occurrences;
        }

        int m = pattern.length();
        int low = 0, high = n - 1;
        int first = -1;

        // Find first occurrence index in SA
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int suffixStart = sa[mid];
            int cmp = comparePrefix(pattern, text, suffixStart, m);

            if (cmp <= 0) {
                if (cmp == 0) first = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        if (first == -1) return occurrences;

        // Find last occurrence index in SA
        low = 0;
        high = n - 1;
        int last = -1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int suffixStart = sa[mid];
            int cmp = comparePrefix(pattern, text, suffixStart, m);

            if (cmp >= 0) {
                if (cmp == 0) last = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (last != -1) {
            for (int i = first; i <= last; i++) {
                occurrences.add(sa[i]);
            }
        }

        return occurrences;
    }

    private static int comparePrefix(String pattern, String text, int suffixStart, int patternLen) {
        int textRemaining = text.length() - suffixStart;
        int compareLen = Math.min(patternLen, textRemaining);

        for (int i = 0; i < compareLen; i++) {
            char pChar = pattern.charAt(i);
            char tChar = text.charAt(suffixStart + i);
            if (pChar != tChar) {
                return Character.compare(pChar, tChar);
            }
        }

        if (patternLen > textRemaining) {
            return 1; // Pattern is longer than available suffix
        }
        return 0; // Match
    }
}
