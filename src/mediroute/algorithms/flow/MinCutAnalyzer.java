package mediroute.algorithms.flow;

import java.util.*;

/**
 * Minimum Cut Analyzer based on the Max-Flow Min-Cut Theorem.
 * Identifies the saturated bottleneck routes separating the supply sources from destination facilities.
 */
public class MinCutAnalyzer {

    public static class CutEdge {
        private final String fromLabel;
        private final String toLabel;
        private final int capacity;
        private final int flow;

        public CutEdge(String fromLabel, String toLabel, int capacity, int flow) {
            this.fromLabel = fromLabel;
            this.toLabel = toLabel;
            this.capacity = capacity;
            this.flow = flow;
        }

        public String getFromLabel() { return fromLabel; }
        public String getToLabel() { return toLabel; }
        public int getCapacity() { return capacity; }
        public int getFlow() { return flow; }

        @Override
        public String toString() {
            return String.format("%s -> %s [Capacity: %d, Flow: %d (SATURATED BOTTLENECK)]",
                    fromLabel, toLabel, capacity, flow);
        }
    }

    public static class MinCutResult {
        private final int cutCapacity;
        private final Set<String> reachableNodes; // S
        private final Set<String> unreachableNodes; // T
        private final List<CutEdge> bottleneckCutEdges;

        public MinCutResult(int cutCapacity, Set<String> reachableNodes, Set<String> unreachableNodes,
                            List<CutEdge> bottleneckCutEdges) {
            this.cutCapacity = cutCapacity;
            this.reachableNodes = reachableNodes;
            this.unreachableNodes = unreachableNodes;
            this.bottleneckCutEdges = bottleneckCutEdges;
        }

        public int getCutCapacity() { return cutCapacity; }
        public Set<String> getReachableNodes() { return reachableNodes; }
        public Set<String> getUnreachableNodes() { return unreachableNodes; }
        public List<CutEdge> getBottleneckCutEdges() { return bottleneckCutEdges; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Minimum Cut Analysis (Total Cut Capacity: %d):\n", cutCapacity));
            sb.append("Source Partition (S): ").append(reachableNodes).append("\n");
            sb.append("Sink Partition (T): ").append(unreachableNodes).append("\n");
            sb.append("Saturated Bottleneck Routes (Min-Cut Edges):\n");
            for (CutEdge edge : bottleneckCutEdges) {
                sb.append("  * ").append(edge).append("\n");
            }
            sb.append("Explanation: The maximum supply distribution flow is strictly bounded by the total capacity of these saturated route segments.");
            return sb.toString();
        }
    }

    /**
     * Identifies the minimum-cut edges from a residual network after maximum flow execution.
     */
    public static MinCutResult analyzeMinCut(FlowNetwork network, int source, int sink) {
        int n = network.getVertexCount();
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();

        visited[source] = true;
        queue.add(source);

        // BFS to find all vertices reachable in the residual graph
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowNetwork.FlowEdge edge : network.getEdgesFrom(u)) {
                if (edge.remainingCapacity() > 0 && !visited[edge.to]) {
                    visited[edge.to] = true;
                    queue.add(edge.to);
                }
            }
        }

        Set<String> sNodes = new LinkedHashSet<>();
        Set<String> tNodes = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) {
            if (visited[i]) {
                sNodes.add(network.getLabel(i));
            } else {
                tNodes.add(network.getLabel(i));
            }
        }

        List<CutEdge> cutEdges = new ArrayList<>();
        int totalCutCapacity = 0;

        for (int u = 0; u < n; u++) {
            if (visited[u]) {
                for (FlowNetwork.FlowEdge edge : network.getEdgesFrom(u)) {
                    // Check if original forward edge goes from S to T
                    if (edge.capacity > 0 && !visited[edge.to]) {
                        cutEdges.add(new CutEdge(
                                network.getLabel(u),
                                network.getLabel(edge.to),
                                edge.capacity,
                                edge.flow
                        ));
                        totalCutCapacity += edge.capacity;
                    }
                }
            }
        }

        return new MinCutResult(totalCutCapacity, sNodes, tNodes, cutEdges);
    }
}
