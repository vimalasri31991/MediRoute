package mediroute.test;

/**
 * Self-contained Automated Test Runner for the MediRoute DSA3 Project.
 * Runs unit and integration tests across all 6 Algorithm Modules without requiring external frameworks like JUnit.
 */
public class TestRunner {

    private static int totalChecks = 0;
    private static int passedChecks = 0;
    private static int failedChecks = 0;

    public static void assertTrue(boolean condition, String message) {
        totalChecks++;
        if (condition) {
            passedChecks++;
        } else {
            failedChecks++;
            System.err.println("  [FAIL] Assertion failed: " + message);
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                 MEDIROUTE AUTOMATED DSA3 TEST SUITE RUNNER                     ");
        System.out.println("================================================================================");

        long startTime = System.currentTimeMillis();

        try {
            StringAlgorithmsTest.run();
            SuffixStructuresTest.run();
            DynamicProgrammingTest.run();
            NetworkFlowTest.run();
            ComplexityAlgorithmsTest.run();
            ParallelRandomizedTest.run();
            IntegrationWorkflowTest.run();
        } catch (Exception e) {
            failedChecks++;
            System.err.println("[FATAL EXCEPTION DURING TEST EXECUTION]:");
            e.printStackTrace();
        }

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST EXECUTION SUMMARY:");
        System.out.println("  - Total Checks Executed:  " + totalChecks);
        System.out.println("  - Checks Passed:          " + passedChecks);
        System.out.println("  - Checks Failed:          " + failedChecks);
        System.out.println("  - Execution Time:         " + elapsed + " ms");

        if (failedChecks == 0) {
            System.out.println("\n>>> ALL TESTS PASSED SUCCESSFULLY! (100% CORRECTNESS VERIFIED) <<<");
            System.out.println("================================================================================");
            System.exit(0);
        } else {
            System.err.println("\n>>> TEST SUITE FAILED WITH " + failedChecks + " ERRORS! <<<");
            System.out.println("================================================================================");
            System.exit(1);
        }
    }
}
