package mediroute.algorithms.complexity;

import java.util.*;

/**
 * 2-Approximation Algorithm for the Minimum Vertex Cover Problem on Allocation Conflict Graphs.
 * Uses the Maximal Matching technique (Gavril & Yannakakis) guaranteeing |C| <= 2 * OPT.
 */
public class VertexCoverApproximation {

    public static class ApproxResult {
        private final Set<Integer> vertexCover;
        private final List<String> coveredVertexLabels;
        private final boolean allEdgesCovered;
        private final int edgeCount;

        public ApproxResult(Set<Integer> vertexCover, List<String> coveredVertexLabels,
                            boolean allEdgesCovered, int edgeCount) {
            this.vertexCover = vertexCover;
            this.coveredVertexLabels = coveredVertexLabels;
            this.allEdgesCovered = allEdgesCovered;
            this.edgeCount = edgeCount;
        }

        public Set<Integer> getVertexCover() { return vertexCover; }
        public List<String> getCoveredVertexLabels() { return coveredVertexLabels; }
        public boolean isAllEdgesCovered() { return allEdgesCovered; }
        public int getEdgeCount() { return edgeCount; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Vertex Cover 2-Approximation Analysis:\n");
            sb.append(String.format("  - Selected Vertices (|C| = %d): %s\n", vertexCover.size(), coveredVertexLabels));
            sb.append(String.format("  - All %d Conflict Edges Covered: %s\n", edgeCount, allEdgesCovered ? "YES" : "NO"));
            sb.append("  - Approximation Guarantee: |C| <= 2 * OPT (at most twice the size of optimal minimum vertex cover).");
            return sb.toString();
        }
    }

    /**
     * Finds a 2-approximate vertex cover for graph g.
     */
    public static ApproxResult solve(Reductions.Graph g) {
        Set<Integer> cover = new TreeSet<>();
        int n = g.getNumVertices();

        // Edge list
        List<int[]> edges = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (int v : g.getNeighbors(u)) {
                if (u < v) {
                    edges.add(new int[]{u, v});
                }
            }
        }

        // Maximal matching selection
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            if (!cover.contains(u) && !cover.contains(v)) {
                cover.add(u);
                cover.add(v);
            }
        }

        // Verification step: verify every edge has at least one endpoint in cover
        boolean allCovered = true;
        for (int[] edge : edges) {
            if (!cover.contains(edge[0]) && !cover.contains(edge[1])) {
                allCovered = false;
                break;
            }
        }

        List<String> labels = new ArrayList<>();
        for (int v : cover) {
            labels.add(g.getVertexLabel(v));
        }

        return new ApproxResult(cover, labels, allCovered, edges.size());
    }
}
