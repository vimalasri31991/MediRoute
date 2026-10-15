package mediroute.algorithms.dp;

import mediroute.model.SupplyPackage;

import java.util.*;

/**
 * Bitmask Dynamic Programming for the Medical Supply Package Selection Problem.
 * Finds the minimum-cost combination of pre-bundled emergency supply packages
 * to fully cover a required set of medical categories.
 *
 * State Space: 2^K where K is the number of distinct medical categories.
 * Time Complexity: O(M * 2^K) where M is the number of available packages.
 */
public class BitmaskPackageCover {

    public static class CoverResult {
        private final boolean feasible;
        private final double totalCost;
        private final List<SupplyPackage> selectedPackages;
        private final int coveredMask;
        private final List<String> coveredCategories;

        public CoverResult(boolean feasible, double totalCost, List<SupplyPackage> selectedPackages,
                           int coveredMask, List<String> coveredCategories) {
            this.feasible = feasible;
            this.totalCost = totalCost;
            this.selectedPackages = selectedPackages;
            this.coveredMask = coveredMask;
            this.coveredCategories = coveredCategories;
        }

        public boolean isFeasible() { return feasible; }
        public double getTotalCost() { return totalCost; }
        public List<SupplyPackage> getSelectedPackages() { return selectedPackages; }
        public int getCoveredMask() { return coveredMask; }
        public List<String> getCoveredCategories() { return coveredCategories; }

        @Override
        public String toString() {
            if (!feasible) {
                return "UNFEASIBLE: Available packages cannot satisfy all requested category requirements.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Optimal Package Cover (Total Cost: $%.2f):\n", totalCost));
            for (SupplyPackage p : selectedPackages) {
                sb.append("  - ").append(p.toString()).append("\n");
            }
            sb.append("Covered Categories: ").append(coveredCategories);
            return sb.toString();
        }
    }

    /**
     * Solves the minimum-cost category cover problem using Bitmask DP.
     *
     * @param packages Available packages
     * @param categoryNames Names of all categories in bit order (0..K-1)
     * @param targetCategoryIndices Indices of categories required to be covered
     * @return CoverResult detailing optimal selection
     */
    public static CoverResult solve(List<SupplyPackage> packages, List<String> categoryNames, List<Integer> targetCategoryIndices) {
        int k = categoryNames.size();
        if (k > 20) {
            throw new IllegalArgumentException("Number of categories (" + k + ") is too large for Bitmask DP.");
        }

        int targetMask = 0;
        for (int idx : targetCategoryIndices) {
            if (idx >= 0 && idx < k) {
                targetMask |= (1 << idx);
            }
        }

        int states = 1 << k;
        double[] dp = new double[states];
        Arrays.fill(dp, Double.POSITIVE_INFINITY);
        int[] parentMask = new int[states];
        int[] parentPackageIdx = new int[states];
        Arrays.fill(parentMask, -1);
        Arrays.fill(parentPackageIdx, -1);

        dp[0] = 0.0;

        for (int mask = 0; mask < states; mask++) {
            if (Double.isInfinite(dp[mask])) continue;

            for (int pIdx = 0; pIdx < packages.size(); pIdx++) {
                SupplyPackage pkg = packages.get(pIdx);
                int nextMask = mask | pkg.getCategoryMask();
                double nextCost = dp[mask] + pkg.getCost();

                if (nextCost < dp[nextMask]) {
                    dp[nextMask] = nextCost;
                    parentMask[nextMask] = mask;
                    parentPackageIdx[nextMask] = pIdx;
                }
            }
        }

        // Find best mask that covers at least all bits in targetMask
        double bestCost = Double.POSITIVE_INFINITY;
        int bestMask = -1;

        for (int mask = 0; mask < states; mask++) {
            if ((mask & targetMask) == targetMask) {
                if (dp[mask] < bestCost) {
                    bestCost = dp[mask];
                    bestMask = mask;
                }
            }
        }

        if (Double.isInfinite(bestCost) || bestMask == -1) {
            return new CoverResult(false, 0.0, Collections.emptyList(), 0, Collections.emptyList());
        }

        // Reconstruct selected packages
        List<SupplyPackage> selected = new ArrayList<>();
        int curr = bestMask;
        while (curr > 0 && parentMask[curr] != -1) {
            int pIdx = parentPackageIdx[curr];
            selected.add(packages.get(pIdx));
            curr = parentMask[curr];
        }
        Collections.reverse(selected);

        List<String> coveredNames = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            if ((bestMask & (1 << i)) != 0) {
                coveredNames.add(categoryNames.get(i));
            }
        }

        return new CoverResult(true, bestCost, selected, bestMask, coveredNames);
    }
}
