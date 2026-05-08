package org.ojalgo.optimisation.linear;

import java.math.BigDecimal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.CuteMarosMeszarosCase;
import org.ojalgo.type.context.NumberContext;

/**
 * A subset of {@link CuteMarosMeszarosCase} but with equilibration explicitly turned on
 */
public class LinearScalingTest extends OptimisationLinearTests {

    static final NumberContext ACCURACY = NumberContext.of(6);

    private static void doTest(final String name, final String expMinValStr) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("netlib", name, false);

        // This is what turns equilibration ON
        model.options.linear().equilibration(10);

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);

        BigDecimal expected = new BigDecimal(expMinValStr);
        TestUtils.assertEquals(expected, result.getValue(), ACCURACY);

        TestUtils.assertTrue(name + " solution not valid!", model.validate(result, ACCURACY));
    }

    @Test
    public void testAFIRO() {
        LinearScalingTest.doTest("AFIRO.SIF", "-464.7531428571429");
    }

    @Test
    public void testBLEND() {
        LinearScalingTest.doTest("BLEND.SIF", "-30.812149845828174");
    }

    @Test
    @Disabled("equilibration causes this model to fail")
    public void testGROW7() {
        LinearScalingTest.doTest("GROW7.SIF", "-4.7787811814711526E7");
    }

    @Test
    public void testSCSD8() {
        LinearScalingTest.doTest("SCSD8.SIF", "905");
    }

    @Test
    public void testSHARE2B() {
        LinearScalingTest.doTest("SHARE2B.SIF", "-415.7322407391888");
    }
}
