package mediroute.service;

import mediroute.algorithms.parallel.*;
import mediroute.data.DataRepository;
import mediroute.model.DemandRecord;
import mediroute.model.Medicine;

import java.util.*;

/**
 * Service for analyzing medical demand history and parallel performance.
 * Integrates Randomized QuickSort, Reservoir Sampling, Blelloch Scan, Parallel Reduce, and Brent's Theorem.
 */
public class DemandAnalyticsService {

    private final DataRepository repository;

    public DemandAnalyticsService(DataRepository repository) {
        this.repository = repository;
    }

    public static class ProductDemandRank {
        private final String medicineId;
        private final String medicineName;
        private final long totalDemand;

        public ProductDemandRank(String medicineId, String medicineName, long totalDemand) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.totalDemand = totalDemand;
        }

        public String getMedicineId() { return medicineId; }
        public String getMedicineName() { return medicineName; }
        public long getTotalDemand() { return totalDemand; }
    }

    /**
     * Ranks medicines by total historical demand using Randomized QuickSort.
     */
    public List<ProductDemandRank> getRankedDemand() {
        Map<String, Long> demandMap = ParallelReducer.parallelDemandPerProduct(repository.getDemandHistory());
        List<ProductDemandRank> list = new ArrayList<>();

        for (Map.Entry<String, Long> entry : demandMap.entrySet()) {
            Medicine med = repository.getMedicine(entry.getKey());
            String name = (med != null) ? med.getName() : "Unknown";
            list.add(new ProductDemandRank(entry.getKey(), name, entry.getValue()));
        }

        // Sort descending by total demand using Randomized QuickSort
        RandomizedQuickSort.sort(list, (a, b) -> Long.compare(b.getTotalDemand(), a.getTotalDemand()));
        return list;
    }

    /**
     * Samples historical demand records using Reservoir Sampling.
     */
    public List<DemandRecord> sampleDemandHistory(int sampleSize) {
        ReservoirSampler sampler = new ReservoirSampler();
        return sampler.sample(repository.getDemandHistory(), sampleSize);
    }

    /**
     * Computes cumulative demand using Blelloch Parallel Scan.
     */
    public int[] computeCumulativeDemand() {
        List<DemandRecord> records = repository.getDemandHistory();
        int[] demands = new int[records.size()];
        for (int i = 0; i < records.size(); i++) {
            demands[i] = records.get(i).getQuantityRequested();
        }
        return BlellochScan.inclusiveScan(demands);
    }

    /**
     * Computes aggregate stats using Parallel Reduce.
     */
    public Map<String, Long> computeAggregateDemandStats() {
        long totalHistoricalDemand = ParallelReducer.parallelTotalDemand(repository.getDemandHistory());
        long totalCurrentInventory = ParallelReducer.parallelTotalInventoryStock(repository.getInventory().values());

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Total Historical Demand (Units)", totalHistoricalDemand);
        stats.put("Total Warehouse Available Stock (Units)", totalCurrentInventory);
        stats.put("Total Historical Demand Records Count", (long) repository.getDemandHistory().size());
        return stats;
    }

    /**
     * Generates a Brent's Theorem Theoretical Speedup Analysis Report.
     */
    public BrentsTheorem.TheoreticalBoundReport generateBrentsTheoremReport(int processors) {
        int n = repository.getDemandHistory().size();
        // Blelloch Parallel Scan: Work = 2 * N ops, Span = 2 * log2(N) steps
        long work = 2L * n;
        long span = Math.max(1, (long) (2.0 * (Math.log(n) / Math.log(2))));

        return BrentsTheorem.calculate("Blelloch Cumulative Demand Scan", work, span, processors);
    }
}
