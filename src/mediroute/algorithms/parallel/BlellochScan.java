package mediroute.algorithms.parallel;

import java.util.Arrays;

/**
 * Implementation of Guy Blelloch's Work-Efficient Parallel Prefix Sum Scan Algorithm.
 * Correctly executes Upsweep (Parallel Reduce) and Downsweep phases.
 * Handles empty arrays, single elements, and arbitrary non-power-of-two input lengths via power-of-two padding.
 *
 * Work: O(N) operations (Work-Efficient)
 * Depth / Span: O(log N)
 */
public class BlellochScan {

    /**
     * Computes the exclusive prefix sum scan: result[i] = sum(arr[0..i-1]), result[0] = 0.
     */
    public static int[] exclusiveScan(int[] arr) {
        if (arr == null || arr.length == 0) {
            return new int[0];
        }
        int n = arr.length;
        if (n == 1) {
            return new int[]{0};
        }

        // Find smallest power of 2 >= n
        int m = 1;
        while (m < n) {
            m <<= 1;
        }

        int[] tree = new int[m];
        System.arraycopy(arr, 0, tree, 0, n);

        // Phase 1: Upsweep (Reduce phase)
        for (int step = 1; step < m; step <<= 1) {
            int stride = step << 1;
            for (int k = 0; k < m; k += stride) {
                tree[k + stride - 1] += tree[k + step - 1];
            }
        }

        // Clear root of tree to zero for exclusive scan
        tree[m - 1] = 0;

        // Phase 2: Downsweep phase
        for (int step = m >> 1; step >= 1; step >>= 1) {
            int stride = step << 1;
            for (int k = 0; k < m; k += stride) {
                int leftVal = tree[k + step - 1];
                tree[k + step - 1] = tree[k + stride - 1];
                tree[k + stride - 1] += leftVal;
            }
        }

        // Return sliced output of length n
        return Arrays.copyOf(tree, n);
    }

    /**
     * Converts exclusive scan to inclusive prefix sums: result[i] = sum(arr[0..i]).
     */
    public static int[] inclusiveScan(int[] arr) {
        if (arr == null || arr.length == 0) {
            return new int[0];
        }
        int[] excl = exclusiveScan(arr);
        int[] incl = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            incl[i] = excl[i] + arr[i];
        }
        return incl;
    }

    /**
     * Sequential reference implementation for verification and validation tests.
     */
    public static int[] sequentialInclusive(int[] arr) {
        if (arr == null || arr.length == 0) return new int[0];
        int[] res = new int[arr.length];
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
            res[i] = sum;
        }
        return res;
    }

    public static int[] sequentialExclusive(int[] arr) {
        if (arr == null || arr.length == 0) return new int[0];
        int[] res = new int[arr.length];
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            res[i] = sum;
            sum += arr[i];
        }
        return res;
    }
}
