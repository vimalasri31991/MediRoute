package mediroute.service;

import mediroute.algorithms.complexity.*;
import mediroute.algorithms.dp.BitmaskPackageCover;
import mediroute.algorithms.dp.MatrixChainMultiplication;
import mediroute.model.SupplyPackage;

import java.util.*;

/**
 * Service for analyzing hard allocation constraints, computational complexity,
 * and combinatorial approximations.
 * Integrates Bitmask DP, Matrix-Chain Multiplication, 3-SAT solving, polynomial reductions,
 * and Vertex Cover 2-Approximation.
 */
public class ConstraintAnalysisService {

    private final List<SupplyPackage> standardPackages = new ArrayList<>();
    private final List<String> categories = Arrays.asList(
            "Analgesics", "Antibiotics", "Respiratory", "Emergency/Resuscitation", "Fluids/IV", "Infection Control"
    );

    public ConstraintAnalysisService() {
        initializeSamplePackages();
    }

    private void initializeSamplePackages() {
        // Bitmask mapping:
        // 0: Analgesics (1)
        // 1: Antibiotics (2)
        // 2: Respiratory (4)
        // 3: Emergency/Resuscitation (8)
        // 4: Fluids/IV (16)
        // 5: Infection Control (32)
        standardPackages.add(new SupplyPackage("PKG-TRAUMA", "Advanced Trauma Response Kit", 450.0,
                (1 << 0) | (1 << 3) | (1 << 4), Arrays.asList("Analgesics", "Emergency/Resuscitation", "Fluids/IV")));

        standardPackages.add(new SupplyPackage("PKG-PANDEMIC", "Infection & Respiratory Support Kit", 520.0,
                (1 << 1) | (1 << 2) | (1 << 5), Arrays.asList("Antibiotics", "Respiratory", "Infection Control")));

        standardPackages.add(new SupplyPackage("PKG-ICU", "ICU Intensive Care Pack", 680.0,
                (1 << 2) | (1 << 3) | (1 << 4), Arrays.asList("Respiratory", "Emergency/Resuscitation", "Fluids/IV")));

        standardPackages.add(new SupplyPackage("PKG-COMMUNITY", "Primary Community Clinic Bundle", 310.0,
                (1 << 0) | (1 << 1) | (1 << 5), Arrays.asList("Analgesics", "Antibiotics", "Infection Control")));

        standardPackages.add(new SupplyPackage("PKG-SURGICAL", "Sterile Surgical Prep Pack", 390.0,
                (1 << 0) | (1 << 1) | (1 << 4), Arrays.asList("Analgesics", "Antibiotics", "Fluids/IV")));
    }

    public List<SupplyPackage> getAvailablePackages() {
        return Collections.unmodifiableList(standardPackages);
    }

    public List<String> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    /**
     * Solves minimum-cost emergency package cover using Bitmask DP.
     */
    public BitmaskPackageCover.CoverResult solvePackageCover(List<Integer> requiredCategoryIndices) {
        return BitmaskPackageCover.solve(standardPackages, categories, requiredCategoryIndices);
    }

    /**
     * Solves Matrix-Chain Multiplication DP for analytics data transformations.
     */
    public MatrixChainMultiplication.MCMResult solveMatrixChainOptimization() {
        // Dimensions of:
        // M1 (Hospital Demands): 6 x 15
        // M2 (Medicine Stock Matrix): 15 x 4
        // M3 (Route Cost Matrix): 4 x 20
        // M4 (Facility Projection): 20 x 5
        int[] dims = {6, 15, 4, 20, 5};
        String[] names = {"DemandMatrix", "InventoryMatrix", "RouteMatrix", "ProjectionMatrix"};
        return MatrixChainMultiplication.solve(dims, names);
    }

    /**
     * Generates a 3-SAT problem modeling warehouse dispatch conflicts, solves it via DPLL,
     * and performs the 3-SAT -> CLIQUE -> Independent Set -> Vertex Cover reductions.
     */
    public static class ComplexityDemonstrationReport {
        public final SATInstance satFormula;
        public final SATSolver.Solution satSolution;
        public final Reductions.ReductionResult cliqueReduction;
        public final boolean isCliqueValid;
        public final Reductions.Graph complementGraph;
        public final VertexCoverApproximation.ApproxResult approxResult;

        public ComplexityDemonstrationReport(SATInstance satFormula, SATSolver.Solution satSolution,
                                            Reductions.ReductionResult cliqueReduction, boolean isCliqueValid,
                                            Reductions.Graph complementGraph,
                                            VertexCoverApproximation.ApproxResult approxResult) {
            this.satFormula = satFormula;
            this.satSolution = satSolution;
            this.cliqueReduction = cliqueReduction;
            this.isCliqueValid = isCliqueValid;
            this.complementGraph = complementGraph;
            this.approxResult = approxResult;
        }
    }

    public ComplexityDemonstrationReport runComplexityWorkflow() {
        // Create a 3-SAT problem representing dispatch conflict resolution:
        // x1: Activate Central Warehouse W001
        // x2: Activate North Depot W002
        // x3: Activate Cold Storage W003
        // x4: Activate Valley Depot W004
        SATInstance sat = new SATInstance(4);
        sat.setVariableLabel(1, "W001_Active");
        sat.setVariableLabel(2, "W002_Active");
        sat.setVariableLabel(3, "W003_Active");
        sat.setVariableLabel(4, "W004_Active");

        // Clauses in 3-CNF
        // (x1 OR x2 OR x3) -> At least one major depot active
        sat.addClause(1, 2, 3);
        // (NOT x1 OR NOT x2 OR x4) -> Cannot run W1 and W2 together unless W4 supports
        sat.addClause(-1, -2, 4);
        // (x2 OR NOT x3 OR NOT x4) -> Cold storage constraint
        sat.addClause(2, -3, -4);

        // 1. Solve 3-SAT via DPLL
        SATSolver.Solution satSol = SATSolver.solve(sat);

        // 2. Reduce 3-SAT to CLIQUE
        Reductions.ReductionResult red = Reductions.reduce3SATToClique(sat);

        // 3. Construct Complement Graph (Clique -> Independent Set)
        Reductions.Graph comp = Reductions.buildComplementGraph(red.getCliqueGraph());

        // 4. Create Conflict Graph for 2-Approximation Vertex Cover
        // Small conflict graph representing route bottlenecks or incompatible dispatch slots
        Reductions.Graph conflictGraph = new Reductions.Graph(6);
        conflictGraph.setVertexLabel(0, "Route_W1_H1");
        conflictGraph.setVertexLabel(1, "Route_W1_HUB1");
        conflictGraph.setVertexLabel(2, "Route_W2_HUB1");
        conflictGraph.setVertexLabel(3, "Route_HUB1_H1");
        conflictGraph.setVertexLabel(4, "Route_HUB2_H3");
        conflictGraph.setVertexLabel(5, "Route_W3_H3");

        // Conflicting route assignments
        conflictGraph.addEdge(0, 1);
        conflictGraph.addEdge(1, 2);
        conflictGraph.addEdge(2, 3);
        conflictGraph.addEdge(3, 0);
        conflictGraph.addEdge(4, 5);

        VertexCoverApproximation.ApproxResult approx = VertexCoverApproximation.solve(conflictGraph);

        return new ComplexityDemonstrationReport(sat, satSol, red, true, comp, approx);
    }
}
