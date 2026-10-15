package mediroute.test;

import mediroute.algorithms.parallel.*;

import java.util.*;

public class ParallelRandomizedTest {

    public static void run() {
        System.out.println("-> Running Parallel and Randomized (CO6) Tests...");

        // 1. Randomized QuickSort Test
        List<Integer> list = new ArrayList<>(Arrays.asList(45, 12, 89, 23, 7, 90, 34, 1, 55, 62));
        RandomizedQuickSort.sort(list, Integer::compare);
        for (int i = 0; i < list.size() - 1; i++) {
            TestRunner.assertTrue(list.get(i) <= list.get(i + 1),
                    "Randomized QuickSort must sort elements in ascending order");
        }

        // 2. Reservoir Sampling Test
        List<Integer> stream = new ArrayList<>();
        for (int i = 0; i < 100; i++) stream.add(i);

        ReservoirSampler sampler = new ReservoirSampler(42L);
        List<Integer> sample = sampler.sample(stream, 7);
        TestRunner.assertTrue(sample.size() == 7, "Reservoir sample size must strictly equal requested k=7");
        for (int val : sample) {
            TestRunner.assertTrue(stream.contains(val), "Sampled element must originate from data stream");
        }

        // 3. Miller-Rabin Primality Test
        long[] primes = {2, 3, 17, 101, 10007, 104729, 999999937L};
        for (long p : primes) {
            TestRunner.assertTrue(MillerRabinPrimality.isPrime(p, 10),
                    "Miller-Rabin must recognize " + p + " as prime");
        }

        long[] composites = {4, 15, 27, 561 /* Carmichael number */, 10000, 104730};
        for (long c : composites) {
            TestRunner.assertTrue(!MillerRabinPrimality.isPrime(c, 10),
                    "Miller-Rabin must reject composite number " + c);
        }

        // 4. Blelloch Parallel Scan Test
        // Test cases: Empty, Single, Power of 2, Non-power of 2
        TestRunner.assertTrue(BlellochScan.exclusiveScan(new int[0]).length == 0, "Blelloch scan on empty array must be empty");

        int[] single = {42};
        TestRunner.assertTrue(BlellochScan.exclusiveScan(single)[0] == 0, "Blelloch exclusive scan on [42] must be [0]");
        TestRunner.assertTrue(BlellochScan.inclusiveScan(single)[0] == 42, "Blelloch inclusive scan on [42] must be [42]");

        int[] nonPowerOfTwo = {3, 1, 7, 0, 4, 1, 6}; // Length 7
        int[] blellochExcl = BlellochScan.exclusiveScan(nonPowerOfTwo);
        int[] seqExcl = BlellochScan.sequentialExclusive(nonPowerOfTwo);
        TestRunner.assertTrue(Arrays.equals(blellochExcl, seqExcl),
                "Blelloch exclusive scan must match sequential prefix sum on non-power-of-two length");

        int[] blellochIncl = BlellochScan.inclusiveScan(nonPowerOfTwo);
        int[] seqIncl = BlellochScan.sequentialInclusive(nonPowerOfTwo);
        TestRunner.assertTrue(Arrays.equals(blellochIncl, seqIncl),
                "Blelloch inclusive scan must match sequential prefix sum on non-power-of-two length");

        // 5. Parallel Reduce Test
        long[] bigArr = new long[500];
        for (int i = 0; i < bigArr.length; i++) bigArr[i] = i + 1;
        long pSum = ParallelReducer.parallelSum(bigArr);
        long sSum = ParallelReducer.sequentialSum(bigArr);
        TestRunner.assertTrue(pSum == sSum, "ForkJoin parallel sum must strictly match sequential sum (" + sSum + ")");

        // 6. Brent's Theorem Bound Calculation Test
        long work = 1000;
        long span = 10;
        int procs = 4;
        BrentsTheorem.TheoreticalBoundReport report = BrentsTheorem.calculate("TestParallelTask", work, span, procs);
        // T_p <= (1000 - 10) / 4 + 10 = 990 / 4 + 10 = 247.5 + 10 = 257.5
        TestRunner.assertTrue(Math.abs(report.getTheoreticalTime() - 257.5) < 1e-6,
                "Brent's theorem formula T_p <= (W - D)/P + D must hold exactly");
        TestRunner.assertTrue(report.getTheoreticalSpeedup() > 1.0, "Theoretical speedup must be greater than 1.0");

        System.out.println("  [PASS] Parallel and Randomized Tests Completed.");
    }
}
