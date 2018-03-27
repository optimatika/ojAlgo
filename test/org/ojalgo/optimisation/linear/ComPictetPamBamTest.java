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
package org.ojalgo.optimisation.linear;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * @author pauslaender
 */
public class ComPictetPamBamTest extends OptimisationLinearTests {

    final static int numberOfVars = 2;

    ExpressionsBasedModel linearModel;
    BigDecimal[] point;
    Variable[] vars;

    @Test
    public void test1() {
        this.setupModel();
        vars[0].level(new BigDecimal(10.0));
        vars[1].lower(BigMath.ZERO).upper(BigMath.HUNDRED);
        final MatrixStore<Double> solution = this.solveLinear();
        TestUtils.assertTrue(solution != null);
    }

    @Test
    public void test2() {
        this.setupModel();
        vars[0].lower(BigMath.ZERO).upper(BigMath.HUNDRED);
        vars[1].level(new BigDecimal(5.0));
        final MatrixStore<Double> solution = this.solveLinear();
        TestUtils.assertTrue(solution != null);
    }

    void setupModel() {
        //
        // variables
        //
        vars = new Variable[numberOfVars];
        vars[0] = new Variable("x0").lower(BigMath.ZERO).upper(BigMath.HUNDRED);
        vars[1] = new Variable("x1").lower(BigMath.ZERO).upper(BigMath.HUNDRED);
        //
        // model
        //
        linearModel = new ExpressionsBasedModel(vars);
        {
            //
            // x0 = 2*x1, i.e. x0 - 2*x1 = 0
            //
            final Expression e = linearModel.addExpression("x0 = 2*x1");
            e.set(0, BigMath.ONE);
            e.set(1, BigMath.TWO.negate());
            e.level(BigMath.ZERO);
        }
    }

    MatrixStore<Double> solveLinear() {

        final Optimisation.Result tmpResult = linearModel.minimise();

        final boolean validated = linearModel.validate(tmpResult, linearModel.options.feasibility);
        if (tmpResult.getState().isFeasible()) {
            final String message = "State: " + tmpResult.getState() + ", validated: " + validated;
            TestUtils.assertTrue(message, validated);
            if (validated) {
                return PrimitiveDenseStore.FACTORY.copy(RationalMatrix.FACTORY.columns(tmpResult));
            }
        } else {
            final String message = "State: " + tmpResult.getState() + ", validated: " + validated;
            TestUtils.assertFalse(message, validated);
        }

        return null;
    }

}
