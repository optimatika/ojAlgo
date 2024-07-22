/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.optimisation.integer;

import static org.ojalgo.function.constant.BigMath.ONE;
import static org.ojalgo.function.constant.BigMath.ZERO;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.OptimisationCase;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * Here is a junit test. Currently, tests 1 and 4 fail and the others pass. I don't understand what is causing
 * the problem, but in the test cases that fail, the binary variables are being set to values other than 0 or
 * 1.
 *
 * @author Luke
 */
public class KnapsackTest extends OptimisationIntegerTests {

    static class KnapsackProblemBuilder {

        final ArrayList<KnapsackItem> items = new ArrayList<>();

        final BigDecimal maxWeight;

        KnapsackProblemBuilder(final double maxWeight) {
            this.maxWeight = new BigDecimal(maxWeight);
        }

        KnapsackProblemBuilder addItem(final int weight, final int value) {
            items.add(new KnapsackItem(weight, value));
            return this;
        }

        ExpressionsBasedModel build() {

            ExpressionsBasedModel retVal = new ExpressionsBasedModel();

            Variable[] variables = new Variable[items.size()];
            for (int i = 0; i < variables.length; i++) {
                variables[i] = retVal.newVariable("Var" + String.valueOf(i));
                variables[i].lower(ZERO).upper(ONE).weight(items.get(i).value).integer(true);
            }

            Expression totalWeightExpr = retVal.newExpression("Total Weight");
            for (int i = 0; i < items.size(); i++) {
                totalWeightExpr.set(i, items.get(i).weight);
            }
            totalWeightExpr.lower(ZERO).upper(maxWeight);

            if (DEBUG) {
                retVal.options.debug(IntegerSolver.class);
            }

            return retVal;
        }

    }

    private static void assertOne(final Variable v) {
        TestUtils.assertEquals(BigMath.ONE, v.getValue(), NumberContext.of(7, 6));
    }

    private static void assertZero(final Variable v) {
        TestUtils.assertEquals(BigMath.ZERO, v.getValue(), NumberContext.of(7, 6));
    }

    static OptimisationCase makeCase0() {

        ExpressionsBasedModel model = new KnapsackProblemBuilder(3d).addItem(20, 2).addItem(30, 4).build();

        Optimisation.Result expected = Optimisation.Result.parse("OPTIMAL 20.0 @ { 1, 0 }");

        return OptimisationCase.of(model, Sense.MAX, expected);
    }

    static OptimisationCase makeCase1() {

        ExpressionsBasedModel model = new KnapsackProblemBuilder(1.1d).addItem(20, 2).addItem(30, 4).build();

        Optimisation.Result expected = Optimisation.Result.parse("DISTINCT 0.0 @ { 0, 0 }");

        return OptimisationCase.of(model, Sense.MAX, expected);
    }

    static OptimisationCase makeCase2() {

        ExpressionsBasedModel model = new KnapsackProblemBuilder(0d).addItem(20, 2).addItem(30, 4).build();

        Optimisation.Result expected = Optimisation.Result.parse("DISTINCT 0.0 @ { 0, 0 }");

        return OptimisationCase.of(model, Sense.MAX, expected);
    }

    static OptimisationCase makeCase3() {

        ExpressionsBasedModel model = new KnapsackProblemBuilder(10d).addItem(20, 2).addItem(30, 4).build();

        Optimisation.Result expected = Optimisation.Result.parse("OPTIMAL 50.0 @ { 1, 1 }");

        return OptimisationCase.of(model, Sense.MAX, expected);
    }

    static OptimisationCase makeCase4() {

        ExpressionsBasedModel model = new KnapsackProblemBuilder(5d).addItem(20, 2).addItem(30, 4).build();

        Optimisation.Result expected = Optimisation.Result.parse("OPTIMAL 30.0 @ { 0, 1 }");

        return OptimisationCase.of(model, Sense.MAX, expected);
    }

    @Test
    public void testVaryingMaxWeight0() {

        OptimisationCase testCase = KnapsackTest.makeCase0();

        testCase.assertResult();

        //Expected: just first item
        KnapsackTest.assertOne(testCase.model.getVariable(0));
        KnapsackTest.assertZero(testCase.model.getVariable(1));
    }

    @Test
    public void testVaryingMaxWeight1() {

        OptimisationCase testCase = KnapsackTest.makeCase1();

        testCase.assertResult();

        //Expected: nothing
        KnapsackTest.assertZero(testCase.model.getVariable(0));
        KnapsackTest.assertZero(testCase.model.getVariable(1));
    }

    @Test
    public void testVaryingMaxWeight2() {

        OptimisationCase testCase = KnapsackTest.makeCase2();

        testCase.assertResult();

        //Expected: nothing
        KnapsackTest.assertZero(testCase.model.getVariable(0));
        KnapsackTest.assertZero(testCase.model.getVariable(1));
    }

    @Test
    public void testVaryingMaxWeight3() {

        OptimisationCase testCase = KnapsackTest.makeCase3();

        testCase.assertResult();

        //Expected: both
        KnapsackTest.assertOne(testCase.model.getVariable(0));
        KnapsackTest.assertOne(testCase.model.getVariable(1));
    }

    @Test
    public void testVaryingMaxWeight4() {

        OptimisationCase testCase = KnapsackTest.makeCase4();

        testCase.assertResult();

        //Expected: just second item
        KnapsackTest.assertZero(testCase.model.getVariable(0));
        KnapsackTest.assertOne(testCase.model.getVariable(1));
    }

}
