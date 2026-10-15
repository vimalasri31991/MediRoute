package mediroute.algorithms.flow;

import java.util.*;

/**
 * Edmonds-Karp Maximum Flow Algorithm using BFS Shortest Augmenting Paths.
 * Time Complexity: O(V * E^2)
 * Space Complexity: O(V + E)
 */
public class EdmondsKarp {

    public static class FlowResult {
        private final int maxFlow;
        private final List<String> augmentingPaths;

        public FlowResult(int maxFlow, List<String> augmentingPaths) {
            this.maxFlow = maxFlow;
            this.augmentingPaths = augmentingPaths;
        }

        public int getMaxFlow() { return maxFlow; }
        public List<String> getAugmentingPaths() { return augmentingPaths; }
    }

    public static FlowResult computeMaxFlow(FlowNetwork network, int source, int sink) {
        network.resetFlows();
        int maxFlow = 0;
        List<String> pathLogs = new ArrayList<>();
        int n = network.getVertexCount();

        FlowNetwork.FlowEdge[] parentEdge = new FlowNetwork.FlowEdge[n];

        while (true) {
            Arrays.fill(parentEdge, null);
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(source);

            while (!queue.isEmpty() && parentEdge[sink] == null) {
                int u = queue.poll();

                for (FlowNetwork.FlowEdge edge : network.getEdgesFrom(u)) {
                    if (edge.to != source && parentEdge[edge.to] == null && edge.remainingCapacity() > 0) {
                        parentEdge[edge.to] = edge;
                        queue.add(edge.to);
                    }
                }
            }

            if (parentEdge[sink] == null) {
                break; // No augmenting path found by BFS
            }

            // Find bottleneck along BFS shortest path
            int bottleneck = Integer.MAX_VALUE;
            for (FlowNetwork.FlowEdge e = parentEdge[sink]; e != null; e = parentEdge[e.from]) {
                bottleneck = Math.min(bottleneck, e.remainingCapacity());
            }

            // Push flow along the augmenting path
            List<String> pathNodes = new ArrayList<>();
            for (FlowNetwork.FlowEdge e = parentEdge[sink]; e != null; e = parentEdge[e.from]) {
                e.flow += bottleneck;
                network.getReverseEdge(e).flow -= bottleneck;
                pathNodes.add(network.getLabel(e.to));
            }
            pathNodes.add(network.getLabel(source));
            Collections.reverse(pathNodes);

            maxFlow += bottleneck;
            pathLogs.add(String.join(" -> ", pathNodes) + " (flow +" + bottleneck + ")");
        }

        return new FlowResult(maxFlow, pathLogs);
    }
}
