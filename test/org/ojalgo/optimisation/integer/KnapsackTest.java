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
package org.ojalgo.optimisation.integer;

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
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

            final Variable[] tmpVariables = new Variable[items.size()];
            for (int i = 0; i < tmpVariables.length; i++) {
                tmpVariables[i] = new Variable("Var" + String.valueOf(i));
                tmpVariables[i].lower(ZERO).upper(ONE).weight(items.get(i).value).integer(true);
            }

            final ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);
            final Expression tmpTotalWeightExpr = retVal.addExpression("Total Weight");
            for (int i = 0; i < items.size(); i++) {
                tmpTotalWeightExpr.set(i, items.get(i).weight);
            }
            tmpTotalWeightExpr.lower(ZERO).upper(maxWeight);

            retVal.setMaximisation();

            return retVal;
        }

    }

    @Test
    public void testVaryingMaxWeight0() {
        ExpressionsBasedModel model = new KnapsackProblemBuilder(3d).addItem(20, 2).addItem(30, 4).build();
        //        model.options.debug(IntegerSolver.class);
        model.maximise();
        //Expected: just first item
        this.assertOne(model.getVariables().get(0));
        this.assertZero(model.getVariables().get(1));

    }

    @Test
    public void testVaryingMaxWeight1() {
        ExpressionsBasedModel model = new KnapsackProblemBuilder(1.1d).addItem(20, 2).addItem(30, 4).build();
        model.maximise();
        //Expected: nothing
        this.assertZero(model.getVariables().get(0));
        this.assertZero(model.getVariables().get(1));
    }

    @Test
    public void testVaryingMaxWeight2() {
        ExpressionsBasedModel model = new KnapsackProblemBuilder(0d).addItem(20, 2).addItem(30, 4).build();
        model.maximise();
        //Expected: nothing
        this.assertZero(model.getVariables().get(0));
        this.assertZero(model.getVariables().get(1));
    }

    @Test
    public void testVaryingMaxWeight3() {
        ExpressionsBasedModel model = new KnapsackProblemBuilder(10d).addItem(20, 2).addItem(30, 4).build();
        model.maximise();
        //Expected: both
        this.assertOne(model.getVariables().get(0));
        this.assertOne(model.getVariables().get(1));
    }

    @Test
    public void testVaryingMaxWeight4() {

        ExpressionsBasedModel model = new KnapsackProblemBuilder(5d).addItem(20, 2).addItem(30, 4).build();

        // model.options.debug(IntegerSolver.class);

        model.maximise();
        //Expected: just second item
        this.assertOne(model.getVariables().get(1));
        this.assertZero(model.getVariables().get(0));
    }

    private void assertOne(final Variable v) {
        TestUtils.assertEquals(BigMath.ONE, v.getValue(), new NumberContext(7, 6));
    }

    private void assertZero(final Variable v) {
        TestUtils.assertEquals(BigMath.ZERO, v.getValue(), new NumberContext(7, 6));
    }

}
