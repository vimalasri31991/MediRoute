package mediroute.algorithms.complexity;

import java.util.*;

/**
 * Exact Boolean Satisfiability (SAT / 3-SAT) Solver using DPLL (Davis-Putnam-Logemann-Loveland).
 * Uses unit propagation and backtracking to verify whether an allocation constraint configuration is satisfiable.
 */
public class SATSolver {

    public static class Solution {
        private final boolean satisfiable;
        private final Map<Integer, Boolean> model; // Variable index -> True/False

        public Solution(boolean satisfiable, Map<Integer, Boolean> model) {
            this.satisfiable = satisfiable;
            this.model = model == null ? Collections.emptyMap() : model;
        }

        public boolean isSatisfiable() { return satisfiable; }
        public Map<Integer, Boolean> getModel() { return model; }

        @Override
        public String toString() {
            if (!satisfiable) {
                return "UNSATISFIABLE: No valid assignment satisfies all allocation clauses.";
            }
            return "SATISFIABLE with assignment: " + model;
        }
    }

    public static Solution solve(SATInstance instance) {
        Map<Integer, Boolean> assignment = new HashMap<>();
        boolean sat = dpll(instance.getClauses(), instance.getNumVariables(), assignment);
        return new Solution(sat, sat ? assignment : null);
    }

    private static boolean dpll(List<List<Integer>> clauses, int numVars, Map<Integer, Boolean> assignment) {
        // Unit propagation
        boolean changed = true;
        while (changed) {
            changed = false;
            for (List<Integer> clause : clauses) {
                int unassignedCount = 0;
                int lastLit = 0;
                boolean clauseSatisfied = false;

                for (int lit : clause) {
                    int var = Math.abs(lit);
                    Boolean val = assignment.get(var);
                    if (val != null) {
                        boolean litVal = lit > 0 ? val : !val;
                        if (litVal) {
                            clauseSatisfied = true;
                            break;
                        }
                    } else {
                        unassignedCount++;
                        lastLit = lit;
                    }
                }

                if (clauseSatisfied) continue;

                if (unassignedCount == 0) {
                    return false; // Conflict: clause has all literals false
                }

                if (unassignedCount == 1) {
                    // Unit clause!
                    int var = Math.abs(lastLit);
                    assignment.put(var, lastLit > 0);
                    changed = true;
                    break;
                }
            }
        }

        // Check if all clauses are satisfied
        boolean allSatisfied = true;
        for (List<Integer> clause : clauses) {
            boolean satisfied = false;
            for (int lit : clause) {
                int var = Math.abs(lit);
                Boolean val = assignment.get(var);
                if (val != null && (lit > 0 ? val : !val)) {
                    satisfied = true;
                    break;
                }
            }
            if (!satisfied) {
                allSatisfied = false;
                break;
            }
        }
        if (allSatisfied) return true;

        // Choose first unassigned variable
        int branchVar = -1;
        for (int v = 1; v <= numVars; v++) {
            if (!assignment.containsKey(v)) {
                branchVar = v;
                break;
            }
        }
        if (branchVar == -1) return false;

        // Try branchVar = TRUE
        Map<Integer, Boolean> tryTrue = new HashMap<>(assignment);
        tryTrue.put(branchVar, true);
        if (dpll(clauses, numVars, tryTrue)) {
            assignment.putAll(tryTrue);
            return true;
        }

        // Try branchVar = FALSE
        Map<Integer, Boolean> tryFalse = new HashMap<>(assignment);
        tryFalse.put(branchVar, false);
        if (dpll(clauses, numVars, tryFalse)) {
            assignment.putAll(tryFalse);
            return true;
        }

        return false;
    }
}
