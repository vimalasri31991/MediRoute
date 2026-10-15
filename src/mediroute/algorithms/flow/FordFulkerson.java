package mediroute.algorithms.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * Ford-Fulkerson Maximum Flow Algorithm using DFS Augmenting Paths.
 * Time Complexity: O(E * max_flow)
 */
public class FordFulkerson {

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

        boolean[] visited = new boolean[network.getVertexCount()];

        while (true) {
            java.util.Arrays.fill(visited, false);
            List<FlowNetwork.FlowEdge> currentPath = new ArrayList<>();
            int bottleneck = dfs(network, source, sink, Integer.MAX_VALUE, visited, currentPath);

            if (bottleneck == 0) {
                break; // No more augmenting paths
            }

            maxFlow += bottleneck;

            // Reconstruct path log
            StringBuilder sb = new StringBuilder();
            sb.append(network.getLabel(source));
            for (FlowNetwork.FlowEdge edge : currentPath) {
                sb.append(" -> ").append(network.getLabel(edge.to));
            }
            sb.append(" (flow +").append(bottleneck).append(")");
            pathLogs.add(sb.toString());
        }

        return new FlowResult(maxFlow, pathLogs);
    }

    private static int dfs(FlowNetwork network, int u, int sink, int bottleneck,
                           boolean[] visited, List<FlowNetwork.FlowEdge> currentPath) {
        if (u == sink) {
            return bottleneck;
        }

        visited[u] = true;

        for (FlowNetwork.FlowEdge edge : network.getEdgesFrom(u)) {
            if (!visited[edge.to] && edge.remainingCapacity() > 0) {
                currentPath.add(edge);
                int pushed = dfs(network, edge.to, sink, Math.min(bottleneck, edge.remainingCapacity()), visited, currentPath);
                if (pushed > 0) {
                    edge.flow += pushed;
                    network.getReverseEdge(edge).flow -= pushed;
                    return pushed;
                }
                currentPath.remove(currentPath.size() - 1);
            }
        }

        return 0;
    }
}
