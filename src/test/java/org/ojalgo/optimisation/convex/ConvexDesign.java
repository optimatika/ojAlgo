/*
 * Copyright 1997-2022 Optimatika
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
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.convex.ConvexSolver.Builder;
import org.ojalgo.scalar.RationalNumber;

public class ConvexDesign extends OptimisationConvexTests {

    private static final Factory<RationalNumber, GenericStore<RationalNumber>> Q128 = GenericStore.Q128;

    /**
     * Attempt to re-implement the numerical example in "Solving quadratic programs to high precision using
     * scaled iterative refinement".
     * <p>
     * In the first iteration ojAlgo returns a feasible solution, exactly, not just roughly as in the example.
     * It's just not the optimal solution. Wrong sign on the dual variable.
     * <P>
     * In the second iteration ojAlgo finds the exact optimal solution, but the dual still has the wrong
     * sign...
     */
    @Test
    public void testIterativeRefinement() {

        GenericStore<RationalNumber> mtrxQ = Q128.make(2, 2);
        mtrxQ.set(0, 0, RationalNumber.ONE);
        mtrxQ.set(1, 1, RationalNumber.ONE);

        GenericStore<RationalNumber> mtrxC = Q128.make(2, 1);
        mtrxC.set(0, RationalNumber.ONE.negate());
        mtrxC.set(1, RationalNumber.parse("1.000001").negate());

        GenericStore<RationalNumber> mtrxAE = Q128.make(1, 2);
        mtrxAE.set(0, 0, RationalNumber.ONE);
        mtrxAE.set(0, 1, RationalNumber.ONE);
        GenericStore<RationalNumber> mtrxBE = Q128.make(1, 1);
        mtrxBE.set(0, RationalNumber.parse("0.000001"));

        GenericStore<RationalNumber> mtrxAI = Q128.make(2, 2);
        mtrxAI.set(0, 0, RationalNumber.NEG);
        mtrxAI.set(1, 1, RationalNumber.NEG);
        GenericStore<RationalNumber> mtrxBI = Q128.make(2, 1);
        mtrxBI.set(0, RationalNumber.ZERO);
        mtrxBI.set(1, RationalNumber.ZERO);

        Builder builder = ConvexSolver.newBuilder(mtrxQ).linear(mtrxC).equalities(mtrxAE, mtrxBE).inequalities(mtrxAI, mtrxBI);

        Result resultStep1 = builder.solve();

        GenericStore<RationalNumber> step1X = Q128.columns(resultStep1);
        GenericStore<RationalNumber> step1L = Q128.columns(resultStep1.getMultipliers().get());

        if (DEBUG) {
            BasicLogger.debugMatrix("Step 1 Solution", step1X);
            BasicLogger.debugMatrix("Step 1 Dual", step1L);
        }

        RationalNumber scale = RationalNumber.valueOf(1_000_000);
        RationalNumber inverse = scale.invert();

        mtrxBE = mtrxBE.subtract(mtrxAE.multiply(step1X)).multiply(scale).collect(Q128);
        mtrxBI = mtrxBI.subtract(mtrxAI.multiply(step1X)).multiply(scale).collect(Q128);

        mtrxC = mtrxC.subtract(mtrxQ.multiply(step1X)).subtract(mtrxAE.below(mtrxAI).transpose().multiply(step1L)).multiply(scale).collect(Q128);

        builder = ConvexSolver.newBuilder(mtrxQ).linear(mtrxC).equalities(mtrxAE, mtrxBE).inequalities(mtrxAI, mtrxBI);
        Result resultStep2 = builder.solve();

        GenericStore<RationalNumber> step2X = Q128.columns(resultStep2);
        GenericStore<RationalNumber> step2L = Q128.columns(resultStep2.getMultipliers().get());

        if (DEBUG) {
            BasicLogger.debugMatrix("Step 2 Solution", step2X);
            BasicLogger.debugMatrix("Step 2 Dual", step2L);
        }

        MatrixStore<RationalNumber> mtrxX = step1X.add(step2X.multiply(inverse));
        MatrixStore<RationalNumber> mtrxL = step1L.add(step2L.multiply(inverse));

        if (DEBUG) {
            BasicLogger.debugMatrix("Final/total Solution", mtrxX);
            BasicLogger.debugMatrix("Final/total Dual", mtrxL);
        }

        TestUtils.assertEquals(1E-6, mtrxX.doubleValue(0));
        TestUtils.assertEquals(0.0, mtrxX.doubleValue(1));
    }

}
