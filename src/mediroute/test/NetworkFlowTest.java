package mediroute.test;

import mediroute.algorithms.flow.*;

import java.util.*;

public class NetworkFlowTest {

    public static void run() {
        System.out.println("-> Running Network Flow (CO4) Tests...");

        // Construct standard diamond test network:
        // Source: 0, Sink: 5
        // Edges:
        // 0 -> 1 (10)
        // 0 -> 2 (10)
        // 1 -> 2 (2)
        // 1 -> 3 (4)
        // 1 -> 4 (8)
        // 2 -> 4 (9)
        // 3 -> 5 (10)
        // 4 -> 5 (10)
        int numNodes = 6;
        int source = 0;
        int sink = 5;

        // 1. Ford-Fulkerson
        FlowNetwork netFF = buildTestNetwork(numNodes);
        FordFulkerson.FlowResult ff = FordFulkerson.computeMaxFlow(netFF, source, sink);

        // 2. Edmonds-Karp
        FlowNetwork netEK = buildTestNetwork(numNodes);
        EdmondsKarp.FlowResult ek = EdmondsKarp.computeMaxFlow(netEK, source, sink);

        // 3. Dinic
        FlowNetwork netDinic = buildTestNetwork(numNodes);
        int dinicFlow = DinicAlgorithm.computeMaxFlow(netDinic, source, sink);

        // Verification of flow algorithm equivalence
        TestRunner.assertTrue(ff.getMaxFlow() == ek.getMaxFlow() && ek.getMaxFlow() == dinicFlow,
                "Ford-Fulkerson, Edmonds-Karp, and Dinic must produce identical maximum flow");
        TestRunner.assertTrue(dinicFlow == 14, "Maximum flow for test network must be 14");

        // 4. Min-Cut Analysis
        MinCutAnalyzer.MinCutResult minCut = MinCutAnalyzer.analyzeMinCut(netDinic, source, sink);
        TestRunner.assertTrue(minCut.getCutCapacity() == dinicFlow,
                "Max-Flow Min-Cut Theorem: Minimum cut capacity (" + minCut.getCutCapacity() +
                ") must strictly equal maximum flow (" + dinicFlow + ")");
        TestRunner.assertTrue(!minCut.getBottleneckCutEdges().isEmpty(),
                "Min-cut analyzer must detect saturated bottleneck edges");

        // 5. Bipartite Matching Test
        // 3 Hospitals: H1, H2, H3
        // 3 Warehouses: W1, W2, W3
        // H1 -> [W1, W2]
        // H2 -> [W1]
        // H3 -> [W2, W3]
        List<String> left = Arrays.asList("H1", "H2", "H3");
        List<String> right = Arrays.asList("W1", "W2", "W3");
        Map<String, List<String>> compat = new HashMap<>();
        compat.put("H1", Arrays.asList("W1", "W2"));
        compat.put("H2", Collections.singletonList("W1"));
        compat.put("H3", Arrays.asList("W2", "W3"));

        BipartiteMatching.MatchingResult match = BipartiteMatching.computeMaxMatching(left, right, compat);
        TestRunner.assertTrue(match.getMatchingSize() == 3,
                "Bipartite matching should find complete matching of size 3");
        TestRunner.assertTrue(match.getAssignments().size() == 3,
                "All 3 hospitals must be assigned distinct warehouses");

        System.out.println("  [PASS] Network Flow Tests Completed.");
    }

    private static FlowNetwork buildTestNetwork(int n) {
        FlowNetwork net = new FlowNetwork(n);
        net.setLabel(0, "S");
        net.setLabel(1, "A");
        net.setLabel(2, "B");
        net.setLabel(3, "C");
        net.setLabel(4, "D");
        net.setLabel(5, "T");

        net.addEdge(0, 1, 10);
        net.addEdge(0, 2, 10);
        net.addEdge(1, 2, 2);
        net.addEdge(1, 3, 4);
        net.addEdge(1, 4, 8);
        net.addEdge(2, 4, 9);
        net.addEdge(3, 5, 10);
        net.addEdge(4, 5, 10);

        return net;
    }
}
