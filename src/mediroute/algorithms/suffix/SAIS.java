package mediroute.algorithms.suffix;

import java.util.Arrays;

/**
 * Genuine Linear-Time Suffix Array Construction by Induced Sorting (SA-IS).
 * Reference: Ge Nong, Sen Zhang, Wai Hong Chan (2009) "Linear Suffix Array Construction by Almost Pure Induced Sorting".
 *
 * Time Complexity: O(N)
 * Space Complexity: O(N + AlphabetSize)
 */
public class SAIS {

    private static final boolean S_TYPE = false;
    private static final boolean L_TYPE = true;

    /**
     * Builds Suffix Array for a String in O(N) time using SA-IS.
     * Appends a unique terminal sentinel (0) internally.
     */
    public static int[] buildSuffixArray(String text) {
        if (text == null || text.isEmpty()) {
            return new int[0];
        }

        int n = text.length();
        int[] s = new int[n + 1];
        int maxChar = 0;
        for (int i = 0; i < n; i++) {
            s[i] = text.charAt(i) + 1; // Shift by 1 so 0 is strictly smaller sentinel
            if (s[i] > maxChar) maxChar = s[i];
        }
        s[n] = 0; // Terminal sentinel

        int[] saWithSentinel = sais(s, maxChar + 1);

        // The first element of saWithSentinel is the sentinel at index n. Strip it.
        int[] sa = new int[n];
        for (int i = 1; i <= n; i++) {
            sa[i - 1] = saWithSentinel[i];
        }
        return sa;
    }

    /**
     * Core recursive SA-IS algorithm on integer array s of length n with alphabet [0..k-1].
     */
    public static int[] sais(int[] s, int k) {
        int n = s.length;
        if (n == 0) return new int[0];
        if (n == 1) return new int[]{0};

        // Step 1: Classify characters into S-type or L-type
        boolean[] type = new boolean[n];
        type[n - 1] = S_TYPE;
        for (int i = n - 2; i >= 0; i--) {
            if (s[i] < s[i + 1]) {
                type[i] = S_TYPE;
            } else if (s[i] > s[i + 1]) {
                type[i] = L_TYPE;
            } else {
                type[i] = type[i + 1];
            }
        }

        // Identify LMS (Leftmost S-type) characters
        int[] lms = new int[n];
        int lmsCount = 0;
        for (int i = 1; i < n; i++) {
            if (type[i] == S_TYPE && type[i - 1] == L_TYPE) {
                lms[lmsCount++] = i;
            }
        }

        // Step 2: Bucket sort LMS suffixes via induced sorting
        int[] sa = new int[n];
        Arrays.fill(sa, -1);

        int[] bucketHead = new int[k];
        int[] bucketTail = new int[k];
        getBucketLimits(s, k, bucketHead, bucketTail);

        // Place LMS characters at the ends of their respective buckets
        for (int i = 0; i < lmsCount; i++) {
            int p = lms[i];
            sa[bucketTail[s[p]]--] = p;
        }

        // Induce sort L-type suffixes left to right
        induceSortL(s, sa, type, k);
        // Induce sort S-type suffixes right to left
        induceSortS(s, sa, type, k);

        // Step 3: Name LMS substrings
        // Gather sorted LMS suffixes from SA
        int[] sortedLms = new int[lmsCount];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            int p = sa[i];
            if (p > 0 && type[p] == S_TYPE && type[p - 1] == L_TYPE) {
                sortedLms[idx++] = p;
            }
        }

        int[] name = new int[n];
        Arrays.fill(name, -1);
        int currentName = 0;
        name[sortedLms[0]] = currentName;

        for (int i = 1; i < lmsCount; i++) {
            int prev = sortedLms[i - 1];
            int curr = sortedLms[i];
            if (!lmsSubstringsEqual(s, type, prev, curr)) {
                currentName++;
            }
            name[curr] = currentName;
        }

        // Form summary string s1
        int[] s1 = new int[lmsCount];
        int s1Idx = 0;
        for (int i = 0; i < n; i++) {
            if (name[i] >= 0) {
                s1[s1Idx++] = name[i];
            }
        }

        // Step 4: Recursively compute SA1
        int[] sa1;
        int uniqueNames = currentName + 1;
        if (uniqueNames == lmsCount) {
            // All names are unique: directly invert
            sa1 = new int[lmsCount];
            for (int i = 0; i < lmsCount; i++) {
                sa1[s1[i]] = i;
            }
        } else {
            sa1 = sais(s1, uniqueNames);
        }

        // Step 5: Induce full SA from SA1
        Arrays.fill(sa, -1);
        getBucketLimits(s, k, bucketHead, bucketTail);

        for (int i = lmsCount - 1; i >= 0; i--) {
            int origLmsIdx = lms[sa1[i]];
            sa[bucketTail[s[origLmsIdx]]--] = origLmsIdx;
        }

        induceSortL(s, sa, type, k);
        induceSortS(s, sa, type, k);

        return sa;
    }

    private static boolean lmsSubstringsEqual(int[] s, boolean[] type, int p1, int p2) {
        int i = 0;
        while (true) {
            int c1 = s[p1 + i];
            int c2 = s[p2 + i];
            if (c1 != c2) return false;

            boolean t1 = type[p1 + i];
            boolean t2 = type[p2 + i];
            if (t1 != t2) return false;

            if (i > 0) {
                boolean isLms1 = (type[p1 + i] == S_TYPE && type[p1 + i - 1] == L_TYPE);
                boolean isLms2 = (type[p2 + i] == S_TYPE && type[p2 + i - 1] == L_TYPE);
                if (isLms1 && isLms2) return true;
                if (isLms1 || isLms2) return false;
            }
            i++;
        }
    }

    private static void getBucketLimits(int[] s, int k, int[] bucketHead, int[] bucketTail) {
        int[] count = new int[k];
        for (int c : s) {
            count[c]++;
        }
        int sum = 0;
        for (int i = 0; i < k; i++) {
            bucketHead[i] = sum;
            sum += count[i];
            bucketTail[i] = sum - 1;
        }
    }

    private static void induceSortL(int[] s, int[] sa, boolean[] type, int k) {
        int[] bucketHead = new int[k];
        int[] bucketTail = new int[k];
        getBucketLimits(s, k, bucketHead, bucketTail);

        for (int i = 0; i < sa.length; i++) {
            int p = sa[i];
            if (p > 0 && type[p - 1] == L_TYPE) {
                sa[bucketHead[s[p - 1]]++] = p - 1;
            }
        }
    }

    private static void induceSortS(int[] s, int[] sa, boolean[] type, int k) {
        int[] bucketHead = new int[k];
        int[] bucketTail = new int[k];
        getBucketLimits(s, k, bucketHead, bucketTail);

        for (int i = sa.length - 1; i >= 0; i--) {
            int p = sa[i];
            if (p > 0 && type[p - 1] == S_TYPE) {
                sa[bucketTail[s[p - 1]]--] = p - 1;
            }
        }
    }
}
