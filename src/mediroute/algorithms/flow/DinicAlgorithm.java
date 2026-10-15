package mediroute.algorithms.flow;

import java.util.*;

/**
 * Dinic's Algorithm for Maximum Network Flow.
 * Combines Level Graphs (via BFS) with Blocking Flows (via DFS and dead-end pruning).
 *
 * Time Complexity: O(V^2 * E) on general networks, O(E * sqrt(V)) on unit networks.
 * Space Complexity: O(V + E)
 */
public class DinicAlgorithm {

    private final FlowNetwork network;
    private final int source;
    private final int sink;
    private final int n;
    private final int[] level;
    private final int[] ptr;

    public DinicAlgorithm(FlowNetwork network, int source, int sink) {
        this.network = network;
        this.source = source;
        this.sink = sink;
        this.n = network.getVertexCount();
        this.level = new int[n];
        this.ptr = new int[n];
    }

    public static int computeMaxFlow(FlowNetwork network, int source, int sink) {
        DinicAlgorithm dinic = new DinicAlgorithm(network, source, sink);
        return dinic.solve();
    }

    public int solve() {
        network.resetFlows();
        int maxFlow = 0;

        while (bfsLevelGraph()) {
            Arrays.fill(ptr, 0);
            while (true) {
                int pushed = dfsBlockingFlow(source, Integer.MAX_VALUE);
                if (pushed == 0) break;
                maxFlow += pushed;
            }
        }

        return maxFlow;
    }

    private boolean bfsLevelGraph() {
        Arrays.fill(level, -1);
        level[source] = 0;
        Queue<Integer> q = new ArrayDeque<>();
        q.add(source);

        while (!q.isEmpty()) {
            int u = q.poll();
            for (FlowNetwork.FlowEdge edge : network.getEdgesFrom(u)) {
                if (edge.remainingCapacity() > 0 && level[edge.to] == -1) {
                    level[edge.to] = level[u] + 1;
                    q.add(edge.to);
                }
            }
        }

        return level[sink] != -1;
    }

    private int dfsBlockingFlow(int u, int pushed) {
        if (pushed == 0 || u == sink) {
            return pushed;
        }

        List<FlowNetwork.FlowEdge> edges = network.getEdgesFrom(u);
        for (; ptr[u] < edges.size(); ptr[u]++) {
            FlowNetwork.FlowEdge edge = edges.get(ptr[u]);
            int v = edge.to;

            if (level[v] == level[u] + 1 && edge.remainingCapacity() > 0) {
                int tr = dfsBlockingFlow(v, Math.min(pushed, edge.remainingCapacity()));
                if (tr > 0) {
                    edge.flow += tr;
                    network.getReverseEdge(edge).flow -= tr;
                    return tr;
                }
            }
        }

        return 0;
    }
}
