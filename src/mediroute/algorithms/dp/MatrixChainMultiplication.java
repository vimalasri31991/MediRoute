package mediroute.algorithms.dp;

import java.util.Arrays;

/**
 * Matrix-Chain Multiplication Dynamic Programming Algorithm.
 * Optimizes the evaluation order for cascading matrix-based analytics operations
 * (e.g., Demand Matrix x Route Capacity Matrix x Warehouse Inventory Tensor).
 *
 * Time Complexity: O(N^3) where N is the number of matrices.
 * Space Complexity: O(N^2) for memoization and split-tracking tables.
 */
public class MatrixChainMultiplication {

    public static class MCMResult {
        private final int[] dimensions;
        private final String[] matrixNames;
        private final long minMultiplications;
        private final String optimalParenthesization;
        private final long[][] costTable;
        private final int[][] splitTable;

        public MCMResult(int[] dimensions, String[] matrixNames, long minMultiplications,
                         String optimalParenthesization, long[][] costTable, int[][] splitTable) {
            this.dimensions = dimensions;
            this.matrixNames = matrixNames;
            this.minMultiplications = minMultiplications;
            this.optimalParenthesization = optimalParenthesization;
            this.costTable = costTable;
            this.splitTable = splitTable;
        }

        public int[] getDimensions() { return dimensions; }
        public String[] getMatrixNames() { return matrixNames; }
        public long getMinMultiplications() { return minMultiplications; }
        public String getOptimalParenthesization() { return optimalParenthesization; }
        public long[][] getCostTable() { return costTable; }
        public int[][] getSplitTable() { return splitTable; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Matrix Chain Multiplication Optimization Report:\n");
            sb.append("  - Dimension Sequence: ").append(Arrays.toString(dimensions)).append("\n");
            sb.append("  - Minimum Scalar Multiplications: ").append(minMultiplications).append("\n");
            sb.append("  - Optimal Parenthesization: ").append(optimalParenthesization).append("\n");
            sb.append("  * Note: This optimizes algorithmic throughput in analytical batch matrix transformations.");
            return sb.toString();
        }
    }

    /**
     * Solves the matrix-chain multiplication problem for dimensions p.
     * Matrix i has dimension p[i-1] x p[i] for 1 <= i <= n.
     */
    public static MCMResult solve(int[] p, String[] names) {
        if (p == null || p.length < 2) {
            throw new IllegalArgumentException("Dimension array must contain at least 2 elements for 1 matrix.");
        }

        int n = p.length - 1; // Number of matrices
        String[] matrixNames = new String[n + 1];
        for (int i = 1; i <= n; i++) {
            if (names != null && i - 1 < names.length) {
                matrixNames[i] = names[i - 1];
            } else {
                matrixNames[i] = "M" + i;
            }
        }

        long[][] m = new long[n + 1][n + 1];
        int[][] s = new int[n + 1][n + 1];

        // Length of chain
        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                m[i][j] = Long.MAX_VALUE;

                for (int k = i; k < j; k++) {
                    long cost = m[i][k] + m[k + 1][j] + (long) p[i - 1] * p[k] * p[j];
                    if (cost < m[i][j]) {
                        m[i][j] = cost;
                        s[i][j] = k;
                    }
                }
            }
        }

        String paren = buildParenthesization(s, 1, n, matrixNames);
        return new MCMResult(p, matrixNames, m[1][n], paren, m, s);
    }

    private static String buildParenthesization(int[][] s, int i, int j, String[] names) {
        if (i == j) {
            return names[i];
        }
        int k = s[i][j];
        return "(" + buildParenthesization(s, i, k, names) + " x " + buildParenthesization(s, k + 1, j, names) + ")";
    }
}
