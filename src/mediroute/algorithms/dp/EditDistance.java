package mediroute.algorithms.dp;

import java.util.*;

/**
 * Advanced Edit Distance Dynamic Programming algorithms:
 * 1. Classical Levenshtein Distance (Insert, Delete, Replace)
 * 2. Damerau-Levenshtein Distance (Adjacent Transpositions)
 * 3. Weighted Edit Distance with configurable operation penalties
 *
 * Used for resilient spelling correction and typo tolerance across medical supplies and hospitals.
 */
public class EditDistance {

    public static class EditWeights {
        public final double insertCost;
        public final double deleteCost;
        public final double replaceCost;
        public final double transposeCost;

        public EditWeights(double insertCost, double deleteCost, double replaceCost, double transposeCost) {
            this.insertCost = insertCost;
            this.deleteCost = deleteCost;
            this.replaceCost = replaceCost;
            this.transposeCost = transposeCost;
        }

        public static EditWeights standard() {
            return new EditWeights(1.0, 1.0, 1.0, 1.0);
        }

        public static EditWeights keyboardTypoBiased() {
            // Replacements and transpositions are more common than insertions/deletions in typing
            return new EditWeights(1.5, 1.5, 1.0, 0.8);
        }
    }

    public static class Suggestion implements Comparable<Suggestion> {
        private final String candidate;
        private final double distance;
        private final String method;

        public Suggestion(String candidate, double distance, String method) {
            this.candidate = candidate;
            this.distance = distance;
            this.method = method;
        }

        public String getCandidate() { return candidate; }
        public double getDistance() { return distance; }
        public String getMethod() { return method; }

        @Override
        public int compareTo(Suggestion o) {
            int cmp = Double.compare(this.distance, o.distance);
            if (cmp != 0) return cmp;
            return this.candidate.compareTo(o.candidate);
        }

        @Override
        public String toString() {
            return String.format("%s (Distance: %.1f via %s)", candidate, distance, method);
        }
    }

    /**
     * Computes the Levenshtein Distance between strings s1 and s2.
     * Operations: Insertion (+1), Deletion (+1), Substitution (+1).
     */
    public static int levenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        dp[i - 1][j] + 1, // deletion
                        Math.min(
                                dp[i][j - 1] + 1, // insertion
                                dp[i - 1][j - 1] + cost // substitution
                        )
                );
            }
        }

        return dp[m][n];
    }

    /**
     * Computes the Damerau-Levenshtein Distance between strings s1 and s2.
     * Operations: Insertion (+1), Deletion (+1), Substitution (+1), Adjacent Transposition (+1).
     */
    public static int damerauLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        dp[i - 1][j] + 1, // deletion
                        Math.min(
                                dp[i][j - 1] + 1, // insertion
                                dp[i - 1][j - 1] + cost // substitution
                        )
                );

                // Adjacent transposition check
                if (i > 1 && j > 1
                        && s1.charAt(i - 1) == s2.charAt(j - 2)
                        && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Computes the Weighted Edit Distance with custom costs.
     */
    public static double weightedEditDistance(String s1, String s2, EditWeights weights) {
        if (s1 == null || s2 == null) return Double.MAX_VALUE;
        int m = s1.length();
        int n = s2.length();

        double[][] dp = new double[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i * weights.deleteCost;
        for (int j = 0; j <= n; j++) dp[0][j] = j * weights.insertCost;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                double subCost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : weights.replaceCost;

                dp[i][j] = Math.min(
                        dp[i - 1][j] + weights.deleteCost,
                        Math.min(
                                dp[i][j - 1] + weights.insertCost,
                                dp[i - 1][j - 1] + subCost
                        )
                );

                if (i > 1 && j > 1
                        && s1.charAt(i - 1) == s2.charAt(j - 2)
                        && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + weights.transposeCost);
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Finds and ranks candidate spelling corrections from a list of known candidates.
     */
    public static List<Suggestion> findSuggestions(String query, Collection<String> candidates, int topK, EditWeights weights) {
        List<Suggestion> list = new ArrayList<>();
        if (query == null || candidates == null) return list;

        String qLower = query.trim().toLowerCase();

        for (String c : candidates) {
            String cLower = c.trim().toLowerCase();
            double dist;
            String method;

            if (weights != null) {
                dist = weightedEditDistance(qLower, cLower, weights);
                method = "Weighted-Damerau";
            } else {
                dist = damerauLevenshtein(qLower, cLower);
                method = "Damerau-Levenshtein";
            }

            list.add(new Suggestion(c, dist, method));
        }

        Collections.sort(list);
        if (list.size() > topK) {
            return list.subList(0, topK);
        }
        return list;
    }
}
