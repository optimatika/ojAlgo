package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * Basic correctness test comparing the null-space wrapper solver against the existing solver on a tiny
 * problem with both equalities and inequalities.
 */
public class NullSpaceASSBasicTest extends OptimisationConvexTests {

    private static final NumberContext ACCURACY = NumberContext.of(12);

    @Test
    public void testSimple() {

        int n = 3;
        int mE = 2;
        int mI = 6;

        ConvexData<Double> data = new ConvexData<>(false, R064Store.FACTORY, n, mE, mI);
        for (int i = 0; i < n; i++) {
            data.setObjective(i, i, ONE); // Q diag = 1
        }
        data.setObjective(0, ONE);
        data.setObjective(1, TWO);
        data.setObjective(2, THREE);
        // AE
        data.setAE(0, 0, ONE);
        data.setAE(0, 1, ONE);
        data.setBE(0, THREE);
        data.setAE(1, 1, ONE);
        data.setAE(1, 2, ONE);
        data.setBE(1, FIVE);
        // Inequalities -x <= 0 and x <= 10
        for (int i = 0; i < n; i++) {
            data.setAI(i, i, -ONE);
            data.setBI(i, ZERO);
        }
        for (int i = 0; i < n; i++) {
            data.setAI(n + i, i, ONE);
            data.setBI(n + i, TEN);
        }

        Optimisation.Options options = new Optimisation.Options();
        Result reference = BasePrimitiveSolver.newSolver(data, options).solve();
        Result test = new EqualityEliminatingASS(options, data).solve();

        TestUtils.assertEquals(reference.getState(), test.getState());
        // TestUtils.assertEquals(reference.getValue(), test.getValue(), ACCURACY);
        for (int i = 0; i < n; i++) {
            TestUtils.assertEquals(reference.doubleValue(i), test.doubleValue(i), ACCURACY);
        }
        // Check equality residual of reconstructed solution
        double e0 = test.doubleValue(0) + test.doubleValue(1) - THREE;
        double e1 = test.doubleValue(1) + test.doubleValue(2) - FIVE;
        TestUtils.assertEquals(ZERO, e0, ACCURACY);
        TestUtils.assertEquals(ZERO, e1, ACCURACY);
    }

}