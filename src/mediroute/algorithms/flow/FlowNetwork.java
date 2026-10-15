package mediroute.algorithms.flow;

import java.util.*;

/**
 * Representation of a Directed Capacitated Residual Flow Network.
 * Maintains forward and backward residual edges for maximum flow and min-cut algorithms.
 */
public class FlowNetwork {

    public static class FlowEdge {
        public final int from;
        public final int to;
        public final int capacity;
        public int flow;
        public int rev; // Index of the reverse residual edge in graph.get(to)

        public FlowEdge(int from, int to, int capacity, int flow, int rev) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = flow;
            this.rev = rev;
        }

        public int remainingCapacity() {
            return capacity - flow;
        }

        @Override
        public String toString() {
            return String.format("%d -> %d [%d/%d]", from, to, flow, capacity);
        }
    }

    private final int vertexCount;
    private final List<List<FlowEdge>> adjacency;
    private final Map<String, Integer> labelToId = new HashMap<>();
    private final Map<Integer, String> idToLabel = new HashMap<>();

    public FlowNetwork(int vertexCount) {
        this.vertexCount = vertexCount;
        this.adjacency = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void setLabel(int id, String label) {
        labelToId.put(label, id);
        idToLabel.put(id, label);
    }

    public int getId(String label) {
        return labelToId.getOrDefault(label, -1);
    }

    public String getLabel(int id) {
        return idToLabel.getOrDefault(id, String.valueOf(id));
    }

    public void addEdge(int from, int to, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Edge capacity must be non-negative: " + capacity);
        }
        FlowEdge forward = new FlowEdge(from, to, capacity, 0, adjacency.get(to).size());
        FlowEdge backward = new FlowEdge(to, from, 0, 0, adjacency.get(from).size());
        adjacency.get(from).add(forward);
        adjacency.get(to).add(backward);
    }

    public void resetFlows() {
        for (List<FlowEdge> edges : adjacency) {
            for (FlowEdge e : edges) {
                e.flow = 0;
            }
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public List<FlowEdge> getEdgesFrom(int u) {
        return adjacency.get(u);
    }

    public FlowEdge getReverseEdge(FlowEdge edge) {
        return adjacency.get(edge.to).get(edge.rev);
    }

    public List<FlowEdge> getAllEdges() {
        List<FlowEdge> all = new ArrayList<>();
        for (List<FlowEdge> edges : adjacency) {
            for (FlowEdge e : edges) {
                if (e.capacity > 0) { // Only original forward edges
                    all.add(e);
                }
            }
        }
        return all;
    }
}
