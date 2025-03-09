/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public abstract class OptimisationConvexTests {

    static boolean DEBUG = false;

    private static void assertDirectAndIterativeEquals(final ConvexData<Double> convexData, final NumberContext accuracy, Optimisation.Options options) {

        if (options == null) {
            options = new Optimisation.Options();
        }

        if (accuracy != null) {
            options.solution = accuracy;
        }

        if (convexData.countInequalityConstraints() > 0) {
            // ActiveSetSolver (ASS)

            DirectASS directASS = new DirectASS(convexData, options);
            Optimisation.Result direct = directASS.solve();

            IterativeASS iterativeASS = new IterativeASS(convexData, options);
            Optimisation.Result iterative = iterativeASS.solve();

            if (!direct.getState().isFeasible()) {
                TestUtils.assertFalse(iterative.getState().isFeasible());
            } else if (accuracy != null) {
                TestUtils.assertStateAndSolution(direct, iterative, accuracy);
            } else {
                TestUtils.assertStateAndSolution(direct, iterative);
            }
        }
    }

    protected static void assertDirectAndIterativeEquals(final ConvexSolver.Builder builder, final NumberContext accuracy, final Optimisation.Options options) {

        ConvexData<Double> convexData = builder.getConvexData(R064Store.FACTORY);

        OptimisationConvexTests.assertDirectAndIterativeEquals(convexData, accuracy, options);
    }

    protected static void assertDirectAndIterativeEquals(final ExpressionsBasedModel model, final NumberContext accuracy) {

        ConvexData<Double> convexData = ConvexSolver.copy(model, R064Store.FACTORY);

        OptimisationConvexTests.assertDirectAndIterativeEquals(convexData, accuracy, model.options);
    }

    /**
     * Build model, and initialise variable values to the expected solution (if not null)
     */
    static ExpressionsBasedModel buildModel(final Access2D<?>[] matrices, final Access1D<?> expectedSolution) {

        Access2D<?> mtrxAE = matrices[0];
        Access2D<?> mtrxBE = matrices[1];

        Access2D<?> mtrxQ = matrices[2];
        Access2D<?> mtrxC = matrices[3];

        Access2D<?> mtrxAI = matrices[4];
        Access2D<?> mtrxBI = matrices[5];

        ExpressionsBasedModel retVal = new ExpressionsBasedModel();

        int nbVariables = mtrxC.size();

        for (int v = 0; v < nbVariables; v++) {
            Variable tmpVariable = retVal.newVariable("X" + v);
            if (expectedSolution != null) {
                tmpVariable.setValue(BigDecimal.valueOf(expectedSolution.doubleValue(v)));
            }
        }

        if (mtrxAE != null && mtrxBE != null) {
            for (int e = 0; e < mtrxAE.countRows(); e++) {
                Expression tmpExpression = retVal.newExpression("E" + e);
                for (int v = 0; v < nbVariables; v++) {
                    tmpExpression.set(v, mtrxAE.get(e, v));
                }
                tmpExpression.level(mtrxBE.doubleValue(e));
            }
        }

        Expression tmpObjQ = retVal.newExpression("Q");
        for (int r = 0; r < nbVariables; r++) {
            for (int v = 0; v < nbVariables; v++) {
                tmpObjQ.set(r, v, mtrxQ.doubleValue(r, v));
            }
        }
        tmpObjQ.weight(HALF);
        Expression tmpObjC = retVal.newExpression("C");
        for (int v = 0; v < nbVariables; v++) {
            tmpObjC.set(v, mtrxC.doubleValue(v));
        }
        tmpObjC.weight(NEG);

        if (mtrxAI != null && mtrxBI != null) {
            for (int i = 0; i < mtrxAI.countRows(); i++) {
                Expression tmpExpression = retVal.newExpression("I" + i);
                for (int v = 0; v < nbVariables; v++) {
                    tmpExpression.set(v, mtrxAI.get(i, v));
                }
                tmpExpression.upper(mtrxBI.doubleValue(i));
            }
        }

        return retVal;
    }

    static ExpressionsBasedModel toModel(final ConvexSolver.Builder builder) {

        ConvexData<Double> data = builder.getConvexData(R064Store.FACTORY);
        ConvexObjectiveFunction<Double> objective = data.getObjective();

        Access2D<?>[] matrices = new Access2D<?>[] { data.getAE(), data.getBE(), objective.quadratic(), objective.linear(), data.getAI(), data.getBI() };

        return OptimisationConvexTests.buildModel(matrices, null);
    }

}
