package org.ojalgo.optimisation;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation.Result;

public class PresolveTest extends OptimisationTests {

    /**
     * https://github.com/optimatika/ojAlgo/issues/663
     * <p>
     * Was a problem with the pre-solve logic. A purely quadratic expression was passed to a pre-solver that
     * only works for linear expressions. This caused an ArithmeticException and a complete failure.
     * <p>
     * Further this model is infeasible and should be recognised as such (by the pre-solver).
     */
    @Test
    void testGitHubIssue663() {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", "GitHub663.ebm", false);

        Result result = model.minimise();

        TestUtils.assertStateInfeasible(result);
    }

}
