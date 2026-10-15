package mediroute.test;

import mediroute.algorithms.dp.BitmaskPackageCover;
import mediroute.algorithms.dp.EditDistance;
import mediroute.algorithms.dp.MatrixChainMultiplication;
import mediroute.model.SupplyPackage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DynamicProgrammingTest {

    public static void run() {
        System.out.println("-> Running Dynamic Programming (CO3) Tests...");

        // 1. Edit Distance Tests
        String s1 = "paracetamol";
        String s2 = "paractamol"; // 1 deletion
        TestRunner.assertTrue(EditDistance.levenshtein(s1, s2) == 1, "Levenshtein distance should be 1 for single deletion");

        // Transposition test: Damerau vs Levenshtein
        // "insulni" swaps adjacent 'i' and 'n' at end of "insulin"
        int damerauDist = EditDistance.damerauLevenshtein("insulin", "insulni");
        int levDist = EditDistance.levenshtein("insulin", "insulni");

        TestRunner.assertTrue(damerauDist == 1, "Damerau-Levenshtein should treat adjacent transposition as cost 1");
        TestRunner.assertTrue(levDist == 2, "Standard Levenshtein should treat adjacent transposition as 2 operations");

        // Weighted Edit Distance
        EditDistance.EditWeights weights = new EditDistance.EditWeights(2.0, 2.0, 1.0, 0.5);
        double weightedDist = EditDistance.weightedEditDistance("insulin", "insulni", weights);
        TestRunner.assertTrue(Math.abs(weightedDist - 0.5) < 1e-6, "Weighted edit distance should apply custom transpose cost");

        // 2. Bitmask DP Package Cover Test
        // 3 categories: 0: A, 1: B, 2: C
        List<String> catNames = Arrays.asList("A", "B", "C");
        SupplyPackage p1 = new SupplyPackage("P1", "Kit 1", 100.0, (1 << 0) | (1 << 1), Arrays.asList("A", "B")); // covers A, B
        SupplyPackage p2 = new SupplyPackage("P2", "Kit 2", 60.0, (1 << 1), Collections.singletonList("B"));         // covers B
        SupplyPackage p3 = new SupplyPackage("P3", "Kit 3", 80.0, (1 << 2), Collections.singletonList("C"));         // covers C
        SupplyPackage p4 = new SupplyPackage("P4", "Kit 4", 170.0, (1 << 0) | (1 << 1) | (1 << 2), Arrays.asList("A", "B", "C")); // covers all for 170

        // Target: Cover A, B, C
        // Option 1: P1 + P3 = 100 + 80 = 180
        // Option 2: P4 = 170 -> OPTIMAL
        BitmaskPackageCover.CoverResult res = BitmaskPackageCover.solve(Arrays.asList(p1, p2, p3, p4), catNames, Arrays.asList(0, 1, 2));
        TestRunner.assertTrue(res.isFeasible(), "Bitmask package cover should be feasible");
        TestRunner.assertTrue(Math.abs(res.getTotalCost() - 170.0) < 1e-6,
                "Bitmask DP should identify optimal bundle cost $170.0");
        TestRunner.assertTrue(res.getSelectedPackages().size() == 1 && res.getSelectedPackages().get(0).getPackageId().equals("P4"),
                "Bitmask DP should select P4");

        // 3. Matrix-Chain Multiplication Test
        // Dimensions [10, 20, 30, 40, 30]
        // Matrices: A1(10x20), A2(20x30), A3(30x40), A4(40x30)
        // Minimum scalar multiplications is 30,000 with parenthesization (((A1 x A2) x A3) x A4)
        int[] dims = {10, 20, 30, 40, 30};
        String[] names = {"A1", "A2", "A3", "A4"};
        MatrixChainMultiplication.MCMResult mcm = MatrixChainMultiplication.solve(dims, names);
        TestRunner.assertTrue(mcm.getMinMultiplications() == 30000L,
                "Matrix-Chain Multiplication should yield 30,000 minimum scalar ops for [10,20,30,40,30]");
        TestRunner.assertTrue(mcm.getOptimalParenthesization().contains("A1 x A2"),
                "Optimal parenthesization must parenthesize (A1 x A2)");

        System.out.println("  [PASS] Dynamic Programming Tests Completed.");
    }
}
