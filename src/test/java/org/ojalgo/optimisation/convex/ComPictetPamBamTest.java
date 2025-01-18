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

import static org.ojalgo.function.constant.BigMath.ONE;
import static org.ojalgo.function.constant.BigMath.TWO;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * @author pauslaender
 */
public class ComPictetPamBamTest extends OptimisationConvexTests {

    static void assertStateValidationConsistency(final ExpressionsBasedModel model) {

        Optimisation.Result result = model.minimise();

        boolean resultFeasible = result.getState().isFeasible();
        boolean validated = model.validate(result);
        String message = "State: " + result.getState() + ", validated: " + validated;

        if (resultFeasible) {
            TestUtils.assertTrue(message, validated);
        } else {
            TestUtils.assertFalse(message, validated);
        }

        //        model.options.debug(ConvexSolver.class);
        //        model.options.validate = false;

        OptimisationConvexTests.assertDirectAndIterativeEquals(model, null);
    }

    private ExpressionsBasedModel model;
    private BigDecimal[] point;
    private Variable[] vars;

    @Test
    public void test1() {
        this.build(6);
        ComPictetPamBamTest.assertStateValidationConsistency(model);
    }

    @Test
    public void test2() {
        this.build(6);
        vars[3].level(new BigDecimal(40.0));
        vars[4].level(new BigDecimal(18.0));
        ComPictetPamBamTest.assertStateValidationConsistency(model);
    }

    @Test
    public void test3() {
        this.build(6);
        vars[3].level(new BigDecimal(48.0));
        vars[4].level(new BigDecimal(18.0));
        vars[5].level(new BigDecimal(5.0));
        ComPictetPamBamTest.assertStateValidationConsistency(model);
    }

    @Test
    public void test4() {
        this.build(42);
        ComPictetPamBamTest.assertStateValidationConsistency(model);
    }

    @Test
    public void test5() {
        this.build(42);
        vars[3].level(new BigDecimal(40.0));
        vars[4].level(new BigDecimal(18.0));
        ComPictetPamBamTest.assertStateValidationConsistency(model);
    }

    @Test
    public void test6() {
        this.build(42);
        vars[3].level(new BigDecimal(40.0));
        vars[4].level(new BigDecimal(18.0));
        vars[5].level(new BigDecimal(5.0));
        ComPictetPamBamTest.assertStateValidationConsistency(model);
    }

    /**
     * @param numberOfVars greater than 6
     */
    void build(final int numberOfVars) {

        if (numberOfVars < 6) {
            throw new IllegalArgumentException("numberOfVars must be >= 6 !!!");
        }

        model = new ExpressionsBasedModel();

        //
        // variables
        //
        vars = new Variable[numberOfVars];
        vars[0] = model.newVariable("x0").lower(new BigDecimal(00.0)).upper(new BigDecimal(15.0));
        vars[1] = model.newVariable("x1").lower(new BigDecimal(17.0)).upper(new BigDecimal(27.0));
        vars[2] = model.newVariable("x2").lower(new BigDecimal(19.0)).upper(new BigDecimal(34.0));
        vars[3] = model.newVariable("x3").lower(new BigDecimal(25.0)).upper(new BigDecimal(48.0));
        vars[4] = model.newVariable("x4").lower(new BigDecimal(05.0)).upper(new BigDecimal(18.0));
        vars[5] = model.newVariable("x5").lower(new BigDecimal(02.0)).upper(new BigDecimal(09.0));
        for (int i = 6; i < numberOfVars; ++i) {
            vars[i] = model.newVariable("x" + i).level(BigMath.ZERO);
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

        //
        // objective function
        //
        {
            int tmpLength = model.countVariables();

            Expression retVal = model.newExpression("objective");

            for (int ij = 0; ij < tmpLength; ij++) {
                retVal.set(ij, ij, ONE);
            }

            BigDecimal tmpLinearWeight = TWO.negate();
            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, Arrays.asList(point).get(i).multiply(tmpLinearWeight));
            }
            Expression e = retVal;
            e.weight(BigMath.HALF);
        }
        //
        // sum(xi) = 100.0
        //
        {
            int tmpLength = model.countVariables();

            Expression retVal = model.newExpression("sum(xi) = 100.0");

            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, ONE);
            }
            Expression e = retVal;
            e.level(BigMath.HUNDRED);
        }
        //
        // x1 + x2 <= 45
        //
        {
            Expression e = model.newExpression("x1 + x2 <= 45.0");
            e.set(1, BigMath.ONE);
            e.set(2, BigMath.ONE);
            e.lower(BigMath.ZERO).upper(new BigDecimal(45.0));
        }
        //
        // x4 - 2*x5 = 0
        //
        {
            Expression e = model.newExpression("x4 - 2*x5 = 0");
            e.set(4, BigMath.ONE);
            e.set(5, BigMath.TWO.negate());
            e.level(BigMath.ZERO);
        }

        // model.options.debug(ConvexSolver.class);
    }
}
