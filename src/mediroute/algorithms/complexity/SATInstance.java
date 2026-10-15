package mediroute.algorithms.complexity;

import java.util.*;

/**
 * Representation of a Boolean formula in Conjunctive Normal Form (CNF) / 3-CNF.
 * Variables are 1-indexed (x1, x2, ..., xN).
 * Positive literal: +i represents xi
 * Negative literal: -i represents NOT xi
 */
public class SATInstance {

    private final int numVariables;
    private final List<List<Integer>> clauses = new ArrayList<>();
    private final Map<Integer, String> variableLabels = new HashMap<>();

    public SATInstance(int numVariables) {
        this.numVariables = numVariables;
    }

    public void setVariableLabel(int varIndex, String label) {
        variableLabels.put(varIndex, label);
    }

    public String getVariableLabel(int varIndex) {
        return variableLabels.getOrDefault(varIndex, "x" + varIndex);
    }

    public void addClause(int... literals) {
        List<Integer> clause = new ArrayList<>();
        for (int lit : literals) {
            if (lit == 0 || Math.abs(lit) > numVariables) {
                throw new IllegalArgumentException("Invalid literal: " + lit);
            }
            clause.add(lit);
        }
        clauses.add(clause);
    }

    public int getNumVariables() {
        return numVariables;
    }

    public int getNumClauses() {
        return clauses.size();
    }

    public List<List<Integer>> getClauses() {
        return clauses;
    }

    public boolean is3SAT() {
        for (List<Integer> c : clauses) {
            if (c.size() != 3) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clauses.size(); i++) {
            if (i > 0) sb.append(" AND ");
            sb.append("(");
            List<Integer> c = clauses.get(i);
            for (int j = 0; j < c.size(); j++) {
                if (j > 0) sb.append(" OR ");
                int lit = c.get(j);
                if (lit < 0) {
                    sb.append("NOT ").append(getVariableLabel(-lit));
                } else {
                    sb.append(getVariableLabel(lit));
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
