package mediroute.algorithms.complexity;

import java.util.*;

/**
 * Standard Polynomial-Time Reductions demonstrating NP-Completeness equivalence:
 * 1. 3-SAT <=p CLIQUE
 * 2. CLIQUE <=p INDEPENDENT SET (via Graph Complement)
 * 3. INDEPENDENT SET <=p VERTEX COVER (via Set Complement)
 */
public class Reductions {

    public static class Graph {
        private final int numVertices;
        private final Map<Integer, Set<Integer>> adj = new HashMap<>();
        private final Map<Integer, String> vertexLabels = new HashMap<>();

        public Graph(int numVertices) {
            this.numVertices = numVertices;
            for (int i = 0; i < numVertices; i++) {
                adj.put(i, new TreeSet<>());
            }
        }

        public void addEdge(int u, int v) {
            if (u != v && u >= 0 && u < numVertices && v >= 0 && v < numVertices) {
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
        }

        public boolean hasEdge(int u, int v) {
            return adj.get(u).contains(v);
        }

        public int getNumVertices() { return numVertices; }
        public Set<Integer> getNeighbors(int u) { return adj.get(u); }

        public void setVertexLabel(int u, String label) {
            vertexLabels.put(u, label);
        }

        public String getVertexLabel(int u) {
            return vertexLabels.getOrDefault(u, "v" + u);
        }

        public int getEdgeCount() {
            int count = 0;
            for (Set<Integer> set : adj.values()) {
                count += set.size();
            }
            return count / 2;
        }
    }

    public static class ReductionResult {
        private final Graph cliqueGraph;
        private final int targetCliqueSize; // Equals number of 3-SAT clauses
        private final List<String> vertexLiteralMap;

        public ReductionResult(Graph cliqueGraph, int targetCliqueSize, List<String> vertexLiteralMap) {
            this.cliqueGraph = cliqueGraph;
            this.targetCliqueSize = targetCliqueSize;
            this.vertexLiteralMap = vertexLiteralMap;
        }

        public Graph getCliqueGraph() { return cliqueGraph; }
        public int getTargetCliqueSize() { return targetCliqueSize; }
        public List<String> getVertexLiteralMap() { return vertexLiteralMap; }
    }

    /**
     * Reduces a 3-SAT instance with m clauses to a CLIQUE graph instance with 3m vertices.
     */
    public static ReductionResult reduce3SATToClique(SATInstance sat) {
        if (!sat.is3SAT()) {
            throw new IllegalArgumentException("Input formula must be in 3-CNF format.");
        }

        List<List<Integer>> clauses = sat.getClauses();
        int m = clauses.size();
        int totalVertices = 3 * m;
        Graph g = new Graph(totalVertices);
        List<String> labels = new ArrayList<>();

        // Create 3 vertices per clause
        for (int i = 0; i < m; i++) {
            List<Integer> clause = clauses.get(i);
            for (int j = 0; j < 3; j++) {
                int vId = i * 3 + j;
                int lit = clause.get(j);
                String label = String.format("C%d_L%d(%s%s)", i + 1, j + 1,
                        lit < 0 ? "NOT " : "", sat.getVariableLabel(Math.abs(lit)));
                g.setVertexLabel(vId, label);
                labels.add(label);
            }
        }

        // Add edge between two vertices if they come from different clauses and do not contradict
        for (int i1 = 0; i1 < m; i1++) {
            for (int j1 = 0; j1 < 3; j1++) {
                int u = i1 * 3 + j1;
                int lit1 = clauses.get(i1).get(j1);

                for (int i2 = i1 + 1; i2 < m; i2++) {
                    for (int j2 = 0; j2 < 3; j2++) {
                        int v = i2 * 3 + j2;
                        int lit2 = clauses.get(i2).get(j2);

                        // If not from the same clause and literals are not contradictory (e.g. x1 and -x1)
                        if (lit1 != -lit2) {
                            g.addEdge(u, v);
                        }
                    }
                }
            }
        }

        return new ReductionResult(g, m, labels);
    }

    /**
     * Constructs the complement graph G' = (V, E') where (u, v) in E' iff (u, v) not in E.
     * Theorem: S is a Clique in G iff S is an Independent Set in G'.
     */
    public static Graph buildComplementGraph(Graph g) {
        int n = g.getNumVertices();
        Graph comp = new Graph(n);
        for (int i = 0; i < n; i++) {
            comp.setVertexLabel(i, g.getVertexLabel(i));
        }

        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                if (!g.hasEdge(u, v)) {
                    comp.addEdge(u, v);
                }
            }
        }
        return comp;
    }

    /**
     * Converts an Independent Set S in graph G to a Vertex Cover C = V \ S.
     * Theorem: S is an Independent Set in G iff V \ S is a Vertex Cover in G.
     */
    public static Set<Integer> independentSetToVertexCover(Graph g, Set<Integer> independentSet) {
        Set<Integer> vertexCover = new TreeSet<>();
        int n = g.getNumVertices();
        for (int i = 0; i < n; i++) {
            if (!independentSet.contains(i)) {
                vertexCover.add(i);
            }
        }
        return vertexCover;
    }

    /**
     * Verifies whether a given subset of vertices forms a valid clique in graph g.
     */
    public static boolean isClique(Graph g, Collection<Integer> vertices) {
        List<Integer> list = new ArrayList<>(vertices);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (!g.hasEdge(list.get(i), list.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
