package org.ojalgo.optimisation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.ojalgo.ProgrammingError;
import org.ojalgo.TestUtils;

public class PureQuadTest extends OptimisationTests {

    /**
     * Due to the pre-solver only considering the linear factors quadratic constraints would incorrectly be
     * marked and redundant. With that this model would result with some sort of (incorrect) solution. The
     * correct behaviour is to keep the quadratic constraints and then find that there is no solver to handle
     * such a model.
     * <p>
     * Currently none of the built-in solvers can handle quadratic constraints. When/if, in the future, this
     * is supported; this test needs to reconstructed.
     */
    @Test
    public void testPresolveWithPureQuadraticConstraint() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        List<Entry<String, Double>> vars = List.of(Map.entry("A", 0.05), Map.entry("B", 0.05), Map.entry("C", 0.25), Map.entry("D", 0.1), Map.entry("E", 0.05));
        Map<String, Variable> variables = new HashMap<>();
        for (Entry<String, Double> v : vars) {
            Variable idAndWeight = model.addVariable(v.getKey());
            variables.put(v.getKey(), idAndWeight);
            idAndWeight.lower(v.getValue());
        }

        // Sum to 1 constraint
        Expression sumExpr = model.addExpression("sum").level(1.0);
        for (Variable v : variables.values()) {
            sumExpr.set(v, 1.0);
        }

        // Objective function: maximize the sum of all variables
        Expression objExpr = model.addExpression("obj").weight(1.0);
        for (Variable v : variables.values()) {
            objExpr.set(v, 1.0);
        }
        double[][] matrix = { { 0.00250000, 0.00022000, 0.00010000, 0.00044000, 0.00200000 }, { 0.00022000, 0.00048400, 0.00004400, 0.00096800, 0.00088000 },
                { 0.00010000, 0.00004400, 0.00010000, 0.00008800, 0.00200000 }, { 0.00044000, 0.00096800, 0.00008800, 0.00193600, 0.00176000 },
                { 0.00200000, 0.00088000, 0.00200000, 0.00176000, 0.04000000 } };

        // create a quadratic only constraint
        var constraintQ = model.addExpression("quadonly").weight(1.0);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                Variable v1 = variables.get(vars.get(i).getKey());
                Variable v2 = variables.get(vars.get(j).getKey());
                double coeff = matrix[i][j];

                constraintQ.set(v1, v2, coeff);
            }
        }

        constraintQ.upper(0.0004);

        // Expect a ProgrammingError when attempting to solve with a pure quadratic constraint
        TestUtils.assertThrows(ProgrammingError.class, () -> model.maximise());

    }
}