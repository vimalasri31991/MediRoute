package mediroute.test;

import mediroute.algorithms.complexity.*;

import java.util.*;

public class ComplexityAlgorithmsTest {

    public static void run() {
        System.out.println("-> Running Complexity and NP-Reduction (CO5) Tests...");

        // 1. SAT / 3-SAT DPLL Solver
        SATInstance sat = new SATInstance(3);
        // (x1 OR x2 OR x3) AND (NOT x1 OR x2 OR x3) AND (NOT x2 OR NOT x3 OR x1)
        sat.addClause(1, 2, 3);
        sat.addClause(-1, 2, 3);
        sat.addClause(-2, -3, 1);

        SATSolver.Solution sol = SATSolver.solve(sat);
        TestRunner.assertTrue(sol.isSatisfiable(), "Test 3-SAT formula must be satisfiable");

        // Unsatisfiable formula: (x1) AND (NOT x1)
        SATInstance unsat = new SATInstance(1);
        unsat.addClause(1);
        unsat.addClause(-1);
        SATSolver.Solution unsatSol = SATSolver.solve(unsat);
        TestRunner.assertTrue(!unsatSol.isSatisfiable(), "Formula with x1 and NOT x1 must be unsatisfiable");

        // 2. 3-SAT to CLIQUE Reduction
        Reductions.ReductionResult red = Reductions.reduce3SATToClique(sat);
        TestRunner.assertTrue(red.getCliqueGraph().getNumVertices() == 3 * sat.getNumClauses(),
                "3-SAT to CLIQUE graph must contain exactly 3 * m vertices");
        TestRunner.assertTrue(red.getTargetCliqueSize() == sat.getNumClauses(),
                "Target clique size must equal clause count m");

        // 3. CLIQUE to INDEPENDENT SET (via Complement Graph)
        Reductions.Graph comp = Reductions.buildComplementGraph(red.getCliqueGraph());
        TestRunner.assertTrue(comp.getNumVertices() == red.getCliqueGraph().getNumVertices(),
                "Complement graph must have identical vertex count");
        TestRunner.assertTrue(comp.getEdgeCount() + red.getCliqueGraph().getEdgeCount()
                == (comp.getNumVertices() * (comp.getNumVertices() - 1)) / 2,
                "Sum of edges in graph and complement must equal complete graph edge count");

        // 4. Vertex Cover 2-Approximation Test
        Reductions.Graph conflictGraph = new Reductions.Graph(5);
        // Cycle graph C5
        conflictGraph.addEdge(0, 1);
        conflictGraph.addEdge(1, 2);
        conflictGraph.addEdge(2, 3);
        conflictGraph.addEdge(3, 4);
        conflictGraph.addEdge(4, 0);

        VertexCoverApproximation.ApproxResult approx = VertexCoverApproximation.solve(conflictGraph);
        TestRunner.assertTrue(approx.isAllEdgesCovered(),
                "Vertex cover 2-approximation must cover 100% of graph conflict edges");
        TestRunner.assertTrue(approx.getVertexCover().size() <= 2 * 3,
                "Vertex cover size must be within factor 2 of optimal (OPT = 3 for C5)");

        System.out.println("  [PASS] Complexity Algorithms Tests Completed.");
    }
}
