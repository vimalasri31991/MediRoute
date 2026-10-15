package mediroute.test;

import mediroute.algorithms.suffix.*;

import java.util.Arrays;
import java.util.List;

public class SuffixStructuresTest {

    public static void run() {
        System.out.println("-> Running Suffix Structures (CO2) Tests...");

        // 1. Suffix Array & SA-IS on "banana"
        String testStr = "banana";
        SuffixArray saObj = new SuffixArray(testStr);
        int[] saExpected = {5, 3, 1, 0, 4, 2}; // "a", "ana", "anana", "banana", "na", "nana"
        TestRunner.assertTrue(Arrays.equals(saObj.getArray(), saExpected),
                "SuffixArray should produce correct lexicographical suffix indices for 'banana'");

        // SA-IS Linear Time Suffix Array
        int[] saisArr = SAIS.buildSuffixArray(testStr);
        TestRunner.assertTrue(Arrays.equals(saisArr, saExpected),
                "SA-IS linear time algorithm must produce exact same suffix array as standard SA for 'banana'");

        // Test SA substring search
        List<Integer> occurrences = saObj.search("ana");
        TestRunner.assertTrue(occurrences.size() == 2 && occurrences.contains(1) && occurrences.contains(3),
                "Suffix Array search should locate all occurrences of 'ana'");

        // 2. Kasai LCP Array on "banana"
        int[] lcp = KasaiLCP.buildLcpArray(testStr, saExpected);
        // Suffixes:
        // sa[0] = 5: "a"
        // sa[1] = 3: "ana" -> LCP("a", "ana") = 1
        // sa[2] = 1: "anana" -> LCP("ana", "anana") = 3
        // sa[3] = 0: "banana" -> LCP("anana", "banana") = 0
        // sa[4] = 4: "na" -> LCP("banana", "na") = 0
        // sa[5] = 2: "nana" -> LCP("na", "nana") = 2
        int[] lcpExpected = {0, 1, 3, 0, 0, 2};
        TestRunner.assertTrue(Arrays.equals(lcp, lcpExpected),
                "Kasai LCP array should match theoretical LCP values for 'banana'");

        // 3. Suffix Tree Multi-Document Test
        List<String> docs = Arrays.asList(
                "Paracetamol tablets for fever and pain relief",
                "Oxygen cylinder for respiratory intensive care"
        );
        SuffixTree tree = new SuffixTree(docs);

        List<SuffixTree.MatchLocation> reliefMatches = tree.search("relief");
        TestRunner.assertTrue(reliefMatches.size() == 1 && reliefMatches.get(0).getDocId() == 0,
                "Suffix Tree should locate 'relief' in Doc#0");

        List<SuffixTree.MatchLocation> oxyMatches = tree.search("respiratory");
        TestRunner.assertTrue(oxyMatches.size() == 1 && oxyMatches.get(0).getDocId() == 1,
                "Suffix Tree should locate 'respiratory' in Doc#1");

        // Cross-boundary isolation test: pattern spanning between docs should not match
        String crossPattern = "reliefOxygen";
        TestRunner.assertTrue(tree.search(crossPattern).isEmpty(),
                "Suffix Tree must never match across document boundary delimiters");

        // 4. Suffix Automaton Test
        SuffixAutomaton sam = new SuffixAutomaton("broad spectrum cephalosporin antibiotic injection");
        TestRunner.assertTrue(sam.contains("cephalosporin"), "Suffix Automaton should contain 'cephalosporin'");
        TestRunner.assertTrue(sam.contains("antibiotic"), "Suffix Automaton should contain 'antibiotic'");
        TestRunner.assertTrue(!sam.contains("nonexistent"), "Suffix Automaton should return false for nonexistent substring");
        TestRunner.assertTrue(sam.indexOf("antibiotic") >= 0, "Suffix Automaton indexOf should return valid offset");

        System.out.println("  [PASS] Suffix Structures Tests Completed.");
    }
}
