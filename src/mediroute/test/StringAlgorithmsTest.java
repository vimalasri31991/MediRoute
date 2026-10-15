package mediroute.test;

import mediroute.algorithms.strings.*;

import java.util.Arrays;
import java.util.List;

public class StringAlgorithmsTest {

    public static void run() {
        System.out.println("-> Running String Algorithms (CO1) Tests...");

        // 1. KMP Test
        String text1 = "ABABDABACDABABCABAB";
        String pat1 = "ABABCABAB";
        List<Integer> kmpMatches = KMPMatcher.search(text1, pat1);
        TestRunner.assertTrue(kmpMatches.size() == 1 && kmpMatches.get(0) == 10,
                "KMP should find pattern at index 10");

        int[] lps = KMPMatcher.computeLPS("AABAACAABAA");
        TestRunner.assertTrue(lps[lps.length - 1] == 5,
                "KMP LPS computation should match expected prefix length");

        // 2. Z-Algorithm Test
        String zText = "aabxaabxcaabxaabxay";
        int[] z = ZAlgorithm.calculateZ(zText);
        TestRunner.assertTrue(z[4] == 4, "Z-algorithm Z[4] should be 4 ('aabx')");

        List<Integer> zMatches = ZAlgorithm.search("medical oxygen cylinder oxygen supply", "oxygen");
        TestRunner.assertTrue(zMatches.size() == 2 && zMatches.get(0) == 8 && zMatches.get(1) == 24,
                "Z-algorithm search should find all occurrences of 'oxygen'");

        // 3. Rabin-Karp Test
        String rkText = "PRC-500-TAB, AMX-250-CAP, OXY-040-CYL";
        List<Integer> rkMatches = RabinKarpMatcher.search(rkText, "AMX-250-CAP");
        TestRunner.assertTrue(rkMatches.size() == 1 && rkMatches.get(0) == 13,
                "Rabin-Karp should find exact product code offset");

        // Rabin-Karp collision immunity / candidate verification
        List<Integer> rkNone = RabinKarpMatcher.search("NONEXISTENT", "FOUND");
        TestRunner.assertTrue(rkNone.isEmpty(), "Rabin-Karp should return empty for non-existent pattern");

        // 4. Aho-Corasick Test
        AhoCorasickMatcher ac = new AhoCorasickMatcher(Arrays.asList("oxygen", "ventilator", "emergency", "urgent"));
        String clinicalNote = "Patient in ICU requires urgent mechanical ventilator and high-flow oxygen.";
        List<AhoCorasickMatcher.Match> acMatches = ac.search(clinicalNote);

        TestRunner.assertTrue(acMatches.size() >= 3, "Aho-Corasick should detect multiple emergency terms");
        boolean foundUrgent = acMatches.stream().anyMatch(m -> m.getKeyword().equals("urgent"));
        boolean foundVentilator = acMatches.stream().anyMatch(m -> m.getKeyword().equals("ventilator"));
        boolean foundOxygen = acMatches.stream().anyMatch(m -> m.getKeyword().equals("oxygen"));

        TestRunner.assertTrue(foundUrgent && foundVentilator && foundOxygen,
                "Aho-Corasick must find 'urgent', 'ventilator', and 'oxygen'");

        System.out.println("  [PASS] String Algorithms Tests Completed.");
    }
}
