package mediroute.service;

import mediroute.algorithms.flow.*;
import mediroute.data.DataRepository;
import mediroute.model.Hospital;
import mediroute.model.Route;
import mediroute.model.Warehouse;

import java.util.*;

/**
 * Service for analyzing the MediRoute logistics network and identifying flow bottlenecks.
 * Integrates Ford-Fulkerson, Edmonds-Karp, Dinic's Algorithm, Bipartite Matching, and Min-Cut Analysis.
 */
public class NetworkAnalysisService {

    private final DataRepository repository;

    public NetworkAnalysisService(DataRepository repository) {
        this.repository = repository;
    }

    public static class FlowComparisonReport {
        public final int fordFulkersonFlow;
        public final int edmondsKarpFlow;
        public final int dinicFlow;
        public final boolean flowsMatch;
        public final MinCutAnalyzer.MinCutResult minCut;
        public final List<String> augmentingPathsSample;

        public FlowComparisonReport(int fordFulkersonFlow, int edmondsKarpFlow, int dinicFlow,
                                   boolean flowsMatch, MinCutAnalyzer.MinCutResult minCut,
                                   List<String> augmentingPathsSample) {
            this.fordFulkersonFlow = fordFulkersonFlow;
            this.edmondsKarpFlow = edmondsKarpFlow;
            this.dinicFlow = dinicFlow;
            this.flowsMatch = flowsMatch;
            this.minCut = minCut;
            this.augmentingPathsSample = augmentingPathsSample;
        }
    }

    /**
     * Builds the complete distribution network model and compares maximum flow algorithms.
     */
    public FlowComparisonReport analyzeNetworkFlow() {
        Set<String> nodes = new LinkedHashSet<>();
        nodes.add("SOURCE");
        nodes.add("SINK");
        nodes.addAll(repository.getWarehouses().keySet());
        nodes.addAll(repository.getHospitals().keySet());

        for (Route r : repository.getRoutes()) {
            nodes.add(r.getSourceId());
            nodes.add(r.getDestinationId());
        }

        List<String> nodeList = new ArrayList<>(nodes);
        Map<String, Integer> idMap = new HashMap<>();
        for (int i = 0; i < nodeList.size(); i++) {
            idMap.put(nodeList.get(i), i);
        }

        int sourceIdx = idMap.get("SOURCE");
        int sinkIdx = idMap.get("SINK");

        // Helper to construct a fresh network instance
        FlowNetwork netFF = createNetworkInstance(nodeList, idMap, sourceIdx, sinkIdx);
        FlowNetwork netEK = createNetworkInstance(nodeList, idMap, sourceIdx, sinkIdx);
        FlowNetwork netDinic = createNetworkInstance(nodeList, idMap, sourceIdx, sinkIdx);

        // 1. Ford-Fulkerson
        FordFulkerson.FlowResult ffRes = FordFulkerson.computeMaxFlow(netFF, sourceIdx, sinkIdx);

        // 2. Edmonds-Karp
        EdmondsKarp.FlowResult ekRes = EdmondsKarp.computeMaxFlow(netEK, sourceIdx, sinkIdx);

        // 3. Dinic
        int dinicFlow = DinicAlgorithm.computeMaxFlow(netDinic, sourceIdx, sinkIdx);

        // 4. Min-Cut on Dinic's residual network
        MinCutAnalyzer.MinCutResult minCut = MinCutAnalyzer.analyzeMinCut(netDinic, sourceIdx, sinkIdx);

        boolean match = (ffRes.getMaxFlow() == ekRes.getMaxFlow()) && (ekRes.getMaxFlow() == dinicFlow);

        return new FlowComparisonReport(ffRes.getMaxFlow(), ekRes.getMaxFlow(), dinicFlow, match, minCut, ekRes.getAugmentingPaths());
    }

    private FlowNetwork createNetworkInstance(List<String> nodeList, Map<String, Integer> idMap, int sourceIdx, int sinkIdx) {
        FlowNetwork net = new FlowNetwork(nodeList.size());
        for (int i = 0; i < nodeList.size(); i++) {
            net.setLabel(i, nodeList.get(i));
        }

        // SOURCE -> Warehouses (bulk outgoing trunk limit)
        for (Warehouse w : repository.getWarehouses().values()) {
            net.addEdge(sourceIdx, idMap.get(w.getWarehouseId()), 800);
        }

        // Internal transit routes
        for (Route r : repository.getRoutes()) {
            Integer u = idMap.get(r.getSourceId());
            Integer v = idMap.get(r.getDestinationId());
            if (u != null && v != null) {
                net.addEdge(u, v, r.getCapacity());
            }
        }

        // Hospitals -> SINK (hospital intake capacity)
        for (Hospital h : repository.getHospitals().values()) {
            net.addEdge(idMap.get(h.getHospitalId()), sinkIdx, 500);
        }

        return net;
    }

    /**
     * Solves unit Bipartite Matching to assign hospitals to regional supply warehouses.
     */
    public BipartiteMatching.MatchingResult computeHospitalWarehouseMatching() {
        List<String> hospitals = new ArrayList<>(repository.getHospitals().keySet());
        List<String> warehouses = new ArrayList<>(repository.getWarehouses().keySet());

        Map<String, List<String>> compatibility = new HashMap<>();
        // Two facilities are compatible if a direct or transit route exists between them
        for (String hId : hospitals) {
            List<String> eligibleWh = new ArrayList<>();
            for (String wId : warehouses) {
                if (isRouteReachable(wId, hId)) {
                    eligibleWh.add(wId);
                }
            }
            compatibility.put(hId, eligibleWh);
        }

        return BipartiteMatching.computeMaxMatching(hospitals, warehouses, compatibility);
    }

    private boolean isRouteReachable(String start, String target) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            if (curr.equals(target)) return true;

            for (Route r : repository.getRoutes()) {
                if (r.getSourceId().equals(curr) && r.getCapacity() > 0 && !visited.contains(r.getDestinationId())) {
                    visited.add(r.getDestinationId());
                    queue.add(r.getDestinationId());
                }
            }
        }
        return false;
    }
}
