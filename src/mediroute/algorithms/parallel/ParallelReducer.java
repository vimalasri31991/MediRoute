package mediroute.algorithms.parallel;

import mediroute.model.DemandRecord;
import mediroute.model.InventoryItem;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Parallel Aggregation Reducer using Java's ForkJoinPool framework.
 * Efficiently computes aggregate statistics, total stock, and product demands across datasets.
 */
public class ParallelReducer {

    private static final int SEQUENTIAL_THRESHOLD = 50;
    private static final ForkJoinPool POOL = ForkJoinPool.commonPool();

    private static class SumTask extends RecursiveTask<Long> {
        private final long[] array;
        private final int start;
        private final int end;

        SumTask(long[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= SEQUENTIAL_THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }

            int mid = start + (end - start) / 2;
            SumTask left = new SumTask(array, start, mid);
            SumTask right = new SumTask(array, mid, end);

            left.fork();
            long rightRes = right.compute();
            long leftRes = left.join();

            return leftRes + rightRes;
        }
    }

    /**
     * Parallel summation of a long array using ForkJoin.
     */
    public static long parallelSum(long[] array) {
        if (array == null || array.length == 0) return 0;
        return POOL.invoke(new SumTask(array, 0, array.length));
    }

    /**
     * Sequential reference sum for verification.
     */
    public static long sequentialSum(long[] array) {
        if (array == null) return 0;
        long sum = 0;
        for (long val : array) {
            sum += val;
        }
        return sum;
    }

    /**
     * Computes total demand quantity in parallel from DemandRecord list.
     */
    public static long parallelTotalDemand(List<DemandRecord> records) {
        if (records == null || records.isEmpty()) return 0;
        long[] quantities = new long[records.size()];
        for (int i = 0; i < records.size(); i++) {
            quantities[i] = records.get(i).getQuantityRequested();
        }
        return parallelSum(quantities);
    }

    /**
     * Computes total warehouse stock in parallel from InventoryItem collection.
     */
    public static long parallelTotalInventoryStock(Collection<InventoryItem> items) {
        if (items == null || items.isEmpty()) return 0;
        long[] stocks = new long[items.size()];
        int idx = 0;
        for (InventoryItem item : items) {
            stocks[idx++] = item.getAvailableQuantity();
        }
        return parallelSum(stocks);
    }

    /**
     * Aggregates demand totals grouped by medicine ID in parallel.
     */
    public static Map<String, Long> parallelDemandPerProduct(List<DemandRecord> records) {
        if (records == null || records.isEmpty()) return Collections.emptyMap();

        return records.parallelStream()
                .collect(java.util.stream.Collectors.groupingBy(
                        DemandRecord::getMedicineId,
                        java.util.stream.Collectors.summingLong(DemandRecord::getQuantityRequested)
                ));
    }
}
