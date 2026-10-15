package mediroute.algorithms.suffix;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Kasai's Algorithm for Longest Common Prefix (LCP) Array Construction.
 * Time Complexity: O(N)
 * Space Complexity: O(N)
 */
public class KasaiLCP {

    public static class RepeatedFragment {
        private final String fragment;
        private final int length;
        private final int firstOccurrence;
        private final int secondOccurrence;

        public RepeatedFragment(String fragment, int length, int firstOccurrence, int secondOccurrence) {
            this.fragment = fragment;
            this.length = length;
            this.firstOccurrence = firstOccurrence;
            this.secondOccurrence = secondOccurrence;
        }

        public String getFragment() { return fragment; }
        public int getLength() { return length; }
        public int getFirstOccurrence() { return firstOccurrence; }
        public int getSecondOccurrence() { return secondOccurrence; }

        @Override
        public String toString() {
            return String.format("\"%s\" (Length %d, Indices [%d, %d])",
                    fragment, length, firstOccurrence, secondOccurrence);
        }
    }

    /**
     * Constructs the LCP array using Kasai's algorithm.
     * lcp[i] = length of the longest common prefix between suffix sa[i] and suffix sa[i-1].
     * By definition, lcp[0] = 0.
     *
     * @param text The input string
     * @param sa The suffix array for the string
     * @return The LCP array of length N
     */
    public static int[] buildLcpArray(String text, int[] sa) {
        if (text == null || sa == null || sa.length == 0) {
            return new int[0];
        }

        int n = text.length();
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int[] lcp = new int[n];
        int h = 0;

        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int prevSuffix = sa[rank[i] - 1];
                while (i + h < n && prevSuffix + h < n && text.charAt(i + h) == text.charAt(prevSuffix + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            } else {
                lcp[0] = 0;
            }
        }

        return lcp;
    }

    /**
     * Finds repeated description fragments in medical texts using LCP values.
     *
     * @param text The text corpus
     * @param sa The suffix array
     * @param lcp The LCP array
     * @param minLength Minimum fragment length to consider
     * @return List of discovered repeated fragments
     */
    public static List<RepeatedFragment> findRepeatedFragments(String text, int[] sa, int[] lcp, int minLength) {
        List<RepeatedFragment> fragments = new ArrayList<>();
        if (text == null || sa == null || lcp == null) return fragments;

        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] >= minLength) {
                int len = lcp[i];
                int idx1 = sa[i - 1];
                int idx2 = sa[i];
                String frag = text.substring(idx1, idx1 + len);
                fragments.add(new RepeatedFragment(frag, len, Math.min(idx1, idx2), Math.max(idx1, idx2)));
            }
        }

        return fragments;
    }
}
