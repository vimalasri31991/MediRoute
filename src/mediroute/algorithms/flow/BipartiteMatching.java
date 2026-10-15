package mediroute.algorithms.flow;

import java.util.*;

/**
 * Maximum Cardinality Bipartite Matching for Hospital-Warehouse Unit Assignment.
 * Uses Augmenting Path / Flow reduction to find the maximum number of simultaneous
 * eligible facility pairings under unit dispatch constraints.
 */
public class BipartiteMatching {

    public static class MatchingResult {
        private final int matchingSize;
        private final Map<String, String> assignments; // LeftNode -> RightNode

        public MatchingResult(int matchingSize, Map<String, String> assignments) {
            this.matchingSize = matchingSize;
            this.assignments = assignments;
        }

        public int getMatchingSize() { return matchingSize; }
        public Map<String, String> getAssignments() { return assignments; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Bipartite Matching (Max Size: %d pairs):\n", matchingSize));
            for (Map.Entry<String, String> entry : assignments.entrySet()) {
                sb.append(String.format("  - %s <===> %s\n", entry.getKey(), entry.getValue()));
            }
            return sb.toString();
        }
    }

    /**
     * Solves maximum bipartite matching between left and right candidate nodes.
     *
     * @param leftNodes List of left side nodes (e.g., Hospitals)
     * @param rightNodes List of right side nodes (e.g., Warehouses)
     * @param compatibility Map from each left node to list of compatible right nodes
     * @return MatchingResult containing matched pairs
     */
    public static MatchingResult computeMaxMatching(List<String> leftNodes, List<String> rightNodes,
                                                      Map<String, List<String>> compatibility) {
        int nLeft = leftNodes.size();
        int nRight = rightNodes.size();
        int totalNodes = nLeft + nRight + 2;
        int source = 0;
        int sink = totalNodes - 1;

        FlowNetwork net = new FlowNetwork(totalNodes);
        net.setLabel(source, "SOURCE");
        net.setLabel(sink, "SINK");

        Map<String, Integer> leftIdMap = new HashMap<>();
        for (int i = 0; i < nLeft; i++) {
            int u = 1 + i;
            String label = leftNodes.get(i);
            leftIdMap.put(label, u);
            net.setLabel(u, label);
            net.addEdge(source, u, 1); // Source -> Left unit capacity
        }

        Map<String, Integer> rightIdMap = new HashMap<>();
        for (int j = 0; j < nRight; j++) {
            int v = 1 + nLeft + j;
            String label = rightNodes.get(j);
            rightIdMap.put(label, v);
            net.setLabel(v, label);
            net.addEdge(v, sink, 1); // Right -> Sink unit capacity
        }

        // Compatibility edges (unit capacity)
        for (Map.Entry<String, List<String>> entry : compatibility.entrySet()) {
            String left = entry.getKey();
            Integer u = leftIdMap.get(left);
            if (u == null) continue;

            for (String right : entry.getValue()) {
                Integer v = rightIdMap.get(right);
                if (v != null) {
                    net.addEdge(u, v, 1);
                }
            }
        }

        int maxMatch = DinicAlgorithm.computeMaxFlow(net, source, sink);

        // Reconstruct assignments
        Map<String, String> matchedPairs = new LinkedHashMap<>();
        for (String left : leftNodes) {
            int u = leftIdMap.get(left);
            for (FlowNetwork.FlowEdge edge : net.getEdgesFrom(u)) {
                if (edge.capacity == 1 && edge.flow == 1 && edge.to != source) {
                    matchedPairs.put(left, net.getLabel(edge.to));
                    break;
                }
            }
        }

        return new MatchingResult(maxMatch, matchedPairs);
    }
}
