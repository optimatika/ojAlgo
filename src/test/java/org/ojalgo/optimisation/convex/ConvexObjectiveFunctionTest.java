/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.optimisation.convex;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.type.context.NumberContext;

/**
 * {@link ConvexObjectiveFunction} is 1/2 [x]<sup>T</sup>[Q][x] - [l]<sup>T</sup>[x] + c. It used to return (c
 * + [x]<sup>T</sup>[Q][x]) / 2 - [l]<sup>T</sup>[x] – the constant halved – and a gradient and Hessian twice
 * as large: those of [x]<sup>T</sup>[Q][x] - [l]<sup>T</sup>[x].
 * <p>
 * With [Q] = [[2, 0], [0, 4]], [l] = (1, 1), c = 3 and [x] = (1, 2) the value is 9 (it was 7.5), the gradient
 * (1, 7) (it was (3, 15)) and the Hessian [Q] (it was 2 [Q]).
 */
public class ConvexObjectiveFunctionTest extends OptimisationConvexTests {

    private static final NumberContext ACCURACY = NumberContext.of(14);

    private static void doTest(final double[][] quadratic) {

        R064Store mtrxQ = R064Store.FACTORY.rows(quadratic);
        R064Store mtrxL = R064Store.FACTORY.column(1.0, 1.0);
        R064Store point = R064Store.FACTORY.column(1.0, 2.0);

        ConvexObjectiveFunction<Double> objective = ConvexSolver.newBuilder(2).objective(mtrxQ, mtrxL).getObjective();
        objective.setConstant(3.0);

        TestUtils.assertEquals(9.0, objective.invoke(point).doubleValue(), ACCURACY);

        MatrixStore<Double> gradient = objective.getGradient(point);
        TestUtils.assertEquals(1.0, gradient.doubleValue(0), ACCURACY);
        TestUtils.assertEquals(7.0, gradient.doubleValue(1), ACCURACY);

        TestUtils.assertEquals(R064Store.FACTORY.rows(new double[][] { { 2.0, 0.0 }, { 0.0, 4.0 } }), objective.getHessian(point), ACCURACY);
    }

    /**
     * Only the symmetric part of [Q] matters – the same value, gradient and Hessian.
     */
    @Test
    public void testNonSymmetric() {
        ConvexObjectiveFunctionTest.doTest(new double[][] { { 2.0, 1.0 }, { -1.0, 4.0 } });
    }

    @Test
    public void testSymmetric() {
        ConvexObjectiveFunctionTest.doTest(new double[][] { { 2.0, 0.0 }, { 0.0, 4.0 } });
    }

}
