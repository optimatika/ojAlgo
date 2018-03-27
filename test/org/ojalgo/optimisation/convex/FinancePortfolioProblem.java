/*
 * Copyright 1997-2018 Optimatika
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

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.ProgrammingError;
import org.ojalgo.TestUtils;
import org.ojalgo.function.BigFunction;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;

public class FinancePortfolioProblem extends OptimisationConvexTests {

    public static final class P20170508 {

        static final PrimitiveMatrix COVARIANCES;
        static final PrimitiveMatrix RETURNS;

        static {

            Builder<PrimitiveMatrix> mtrxBuilder;

            mtrxBuilder = PrimitiveMatrix.FACTORY.getBuilder(2, 2);
            mtrxBuilder.add(0, 0, 0.040000);
            mtrxBuilder.add(0, 1, 0.1000);
            mtrxBuilder.add(1, 0, 0.1000);
            mtrxBuilder.add(1, 1, 0.250000);
            COVARIANCES = mtrxBuilder.build();

            mtrxBuilder = PrimitiveMatrix.FACTORY.getBuilder(2);
            mtrxBuilder.add(0, 0.20000);
            mtrxBuilder.add(1, 0.40000);
            RETURNS = mtrxBuilder.build();

        }

    }

    private static ExpressionsBasedModel buildModel(final BasicMatrix covariances, final BasicMatrix returns, final BigDecimal riskAversion) {

        ProgrammingError.throwIfNotSquare(covariances);
        ProgrammingError.throwIfNotEqualRowDimensions(covariances, returns);

        final int numberOfVariables = (int) returns.countRows();

        final Variable[] tmpVariables = new Variable[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            tmpVariables[i] = Variable.make("Asset_" + Integer.toString(i)).lower(ZERO).upper(ONE).weight(-returns.doubleValue(i));
        }

        final ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);

        final Expression tmp100P = retVal.addExpression("Balance");
        for (int i = 0; i < numberOfVariables; i++) {
            tmp100P.set(i, ONE);
        }
        tmp100P.level(ONE);

        final Expression tmpVar = retVal.addExpression("Variance");
        for (int i = 0; i < numberOfVariables; i++) {
            for (int j = 0; j < numberOfVariables; j++) {
                tmpVar.set(i, j, covariances.doubleValue(i, j));
            }
        }
        tmpVar.weight(BigFunction.DIVIDE.invoke(riskAversion, TWO));

        return retVal;
    }

    /**
     * There were several problems (symptoms) related to this case. This test primarily tests that the
     * returned solution is actually valid. There was a problem (among others) that a subproblem from the
     * Markowitz model class (set up this way) did not produce a valid/feasible solution.
     */
    @Test
    public void testP20170508() {

        final BigDecimal raf = BigDecimal.valueOf(177.82794100389228);

        final ExpressionsBasedModel model = FinancePortfolioProblem.buildModel(P20170508.COVARIANCES, P20170508.RETURNS, raf);

        //        model.options.debug(Optimisation.Solver.class);
        //        model.options.validate = false;

        final BigDecimal w0 = BigDecimal.valueOf(0.9639383);
        final BigDecimal w1 = BigDecimal.valueOf(0.036061702);

        model.getVariable(0).setValue(w0);
        model.getVariable(1).setValue(w1);

        final Result result = model.minimise();

        if (DEBUG) {
            BasicLogger.debug(result);
        }

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertTrue(model.validate());
        TestUtils.assertTrue(model.validate(result));

        OptimisationConvexTests.assertDirectAndIterativeEquals(model);
    }

}
