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
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * @author pauslaender
 */
public class ComPictetPamBamTest extends OptimisationConvexTests {

    private ExpressionsBasedModel model;
    private BigDecimal[] point;
    private Variable[] vars;

    @Test
    public void test1() {
        this.setupModel(6);
        this.solve();
    }

    @Test
    public void test2() {
        this.setupModel(6);
        vars[3].level(new BigDecimal(40.0));
        vars[4].level(new BigDecimal(18.0));
        this.solve();
    }

    @Test
    public void test3() {
        this.setupModel(6);
        vars[3].level(new BigDecimal(48.0));
        vars[4].level(new BigDecimal(18.0));
        vars[5].level(new BigDecimal(5.0));
        this.solve();
    }

    @Test
    public void test4() {
        this.setupModel(42);
        this.solve();
    }

    @Test
    public void test5() {
        this.setupModel(42);
        vars[3].level(new BigDecimal(40.0));
        vars[4].level(new BigDecimal(18.0));
        this.solve();
    }

    @Test
    public void test6() {
        this.setupModel(42);
        vars[3].level(new BigDecimal(40.0));
        vars[4].level(new BigDecimal(18.0));
        vars[5].level(new BigDecimal(5.0));
        this.solve();
    }

    /**
     * @param numberOfVars greater than 6
     */
    void setupModel(final int numberOfVars) {
        if (numberOfVars < 6) {
            throw new IllegalArgumentException("numberOfVars must be >= 6 !!!");
        }
        //
        // variables
        //
        vars = new Variable[numberOfVars];
        vars[0] = new Variable("x0").lower(new BigDecimal(00.0)).upper(new BigDecimal(15.0));
        vars[1] = new Variable("x1").lower(new BigDecimal(17.0)).upper(new BigDecimal(27.0));
        vars[2] = new Variable("x2").lower(new BigDecimal(19.0)).upper(new BigDecimal(34.0));
        vars[3] = new Variable("x3").lower(new BigDecimal(25.0)).upper(new BigDecimal(48.0));
        vars[4] = new Variable("x4").lower(new BigDecimal(05.0)).upper(new BigDecimal(18.0));
        vars[5] = new Variable("x5").lower(new BigDecimal(02.0)).upper(new BigDecimal(09.0));
        for (int i = 6; i < numberOfVars; ++i) {
            vars[i] = new Variable("x" + i).level(BigMath.ZERO);
        }
        //
        // minimise distance to this point
        //
        point = new BigDecimal[numberOfVars];
        point[0] = new BigDecimal(1.0);
        point[1] = new BigDecimal(25.0);
        point[2] = new BigDecimal(33.0);
        point[3] = new BigDecimal(29.0);
        point[4] = new BigDecimal(9.0);
        point[5] = new BigDecimal(2.0);
        for (int i = 6; i < numberOfVars; ++i) {
            point[i] = new BigDecimal(0.0);
        }
        //
        // model
        //
        model = new ExpressionsBasedModel(vars);
        //
        // objective function
        //
        {
            final int tmpLength = model.countVariables();

            final Expression retVal = model.addExpression("objective");

            for (int ij = 0; ij < tmpLength; ij++) {
                retVal.set(ij, ij, ONE);
            }

            final BigDecimal tmpLinearWeight = TWO.negate();
            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, Arrays.asList(point).get(i).multiply(tmpLinearWeight));
            }
            final Expression e = retVal;
            e.weight(BigMath.HALF);
        }
        //
        // sum(xi) = 100.0
        //
        {
            final int tmpLength = model.countVariables();

            final Expression retVal = model.addExpression("sum(xi) = 100.0");

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, ONE);
            }
            final Expression e = retVal;
            e.level(BigMath.HUNDRED);
        }
        //
        // x1 + x2 <= 45
        //
        {
            final Expression e = model.addExpression("x1 + x2 <= 45.0");
            e.set(1, BigMath.ONE);
            e.set(2, BigMath.ONE);
            e.lower(BigMath.ZERO).upper(new BigDecimal(45.0));
        }
        //
        // x4 - 2*x5 = 0
        //
        {
            final Expression e = model.addExpression("x4 - 2*x5 = 0");
            e.set(4, BigMath.ONE);
            e.set(5, BigMath.TWO.negate());
            e.level(BigMath.ZERO);
        }

        // model.options.debug(ConvexSolver.class);
    }

    void solve() {

        //  final ConvexSolver solver = new ConvexSolver.Builder(model).build();

        final Optimisation.Result result = model.minimise();

        if (RationalMatrix.FACTORY.columns(result) != null) {
            final boolean validated = model.validate(result, model.options.feasibility);
            if (result.getState().isFeasible()) {
                final String message = "State: " + result.getState() + ", validated: " + validated;
                TestUtils.assertTrue(message, validated);
            } else {
                final String message = "State: " + result.getState() + ", validated: " + validated;
                TestUtils.assertFalse(message, validated);
            }
        } else {
            TestUtils.assertFalse("No solution but state FEASIBLE", result.getState().isFeasible());
        }

        OptimisationConvexTests.assertDirectAndIterativeEquals(model);
    }
}
