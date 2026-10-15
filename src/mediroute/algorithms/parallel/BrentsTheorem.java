package mediroute.algorithms.parallel;

/**
 * Theoretical Performance Bound Analyzer based on Brent's Scheduling Theorem.
 * Formulates the theoretical parallel execution time:
 * T_p <= (W - D) / P + D
 * where:
 * W = Total Work (operations on 1 processor)
 * D = Critical-Path Span / Depth (operations with infinite processors)
 * P = Number of available processing units
 */
public class BrentsTheorem {

    public static class TheoreticalBoundReport {
        private final String algorithmName;
        private final long work;
        private final long span;
        private final int processors;
        private final double theoreticalTime;
        private final double theoreticalSpeedup;
        private final double theoreticalEfficiency;

        public TheoreticalBoundReport(String algorithmName, long work, long span, int processors,
                                      double theoreticalTime, double theoreticalSpeedup, double theoreticalEfficiency) {
            this.algorithmName = algorithmName;
            this.work = work;
            this.span = span;
            this.processors = processors;
            this.theoreticalTime = theoreticalTime;
            this.theoreticalSpeedup = theoreticalSpeedup;
            this.theoreticalEfficiency = theoreticalEfficiency;
        }

        public String getAlgorithmName() { return algorithmName; }
        public long getWork() { return work; }
        public long getSpan() { return span; }
        public int getProcessors() { return processors; }
        public double getTheoreticalTime() { return theoreticalTime; }
        public double getTheoreticalSpeedup() { return theoreticalSpeedup; }
        public double getTheoreticalEfficiency() { return theoreticalEfficiency; }

        @Override
        public String toString() {
            return String.format(
                    "Brent's Theorem Bound Report [%s]:\n" +
                    "  - Total Work W: %d ops\n" +
                    "  - Critical Span D: %d steps\n" +
                    "  - Processors P: %d cores\n" +
                    "  - Theoretical Time Upper Bound T_P: %.2f steps\n" +
                    "  - Theoretical Speedup S_P (W / T_P): %.2fx\n" +
                    "  - Parallel Efficiency E_P: %.1f%%\n" +
                    "  * Distinction: Reflects asymptotic algorithmic schedule bounds, independent of OS thread scheduling jitter.",
                    algorithmName, work, span, processors, theoreticalTime, theoreticalSpeedup, theoreticalEfficiency * 100);
        }
    }

    /**
     * Calculates the Brent's Theorem bound for an algorithm.
     */
    public static TheoreticalBoundReport calculate(String algorithmName, long work, long span, int processors) {
        if (processors <= 0) {
            processors = Runtime.getRuntime().availableProcessors();
        }
        if (work < span) {
            work = span; // Work cannot be less than span
        }

        // T_p <= (W - D) / P + D
        double timeBound = ((double)(work - span) / processors) + span;
        double speedup = (double) work / timeBound;
        double efficiency = speedup / processors;

        return new TheoreticalBoundReport(algorithmName, work, span, processors, timeBound, speedup, efficiency);
    }
}
