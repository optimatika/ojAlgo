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
package org.ojalgo.optimisation.linear;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerProblems;
import org.ojalgo.type.context.NumberContext;

public class TableauCutGeneratorTest extends OptimisationLinearTests {

    private static Equation generateGomory(final double rhs, final double... factors) {

        Primitive1D body = new Primitive1D() {

            @Override
            public int size() {
                return factors.length;
            }

            @Override
            double doubleValue(final int index) {
                return factors[index];
            }

            @Override
            void set(final int index, final double value) {
                factors[index] = value;
            }

        };

        boolean[] integer = new boolean[factors.length];
        Arrays.fill(integer, true);

        return TableauCutGenerator.doGomory(body, 0, rhs, PrimitiveMath.ELEVENTH);
    }

    private static Equation generateGomoryMixed(final double rhs, final int basic, final double... factors) {

        Primitive1D body = new Primitive1D() {

            @Override
            public int size() {
                return factors.length;
            }

            @Override
            double doubleValue(final int index) {
                return factors[index];
            }

            @Override
            void set(final int index, final double value) {
                factors[index] = value;
            }

        };

        boolean[] integer = new boolean[factors.length];
        Arrays.fill(integer, true);

        return TableauCutGenerator.doGomoryMixedInteger(body, basic, rhs, integer, PrimitiveMath.ELEVENTH);
    }

    /**
     * <p>
     * Example from The Gomory Mixed Integer Cut, by John Mitchell comparing a Gomory mixed integer cut with a
     * standard Gomory cut (all variables integer).
     */
    @Test
    public void testCompareMixedIntegerWithStandard() {

        double[] body = { 1.0, 0.3, 0.8 };
        double rhs = 3.4;

        Equation gomory = TableauCutGeneratorTest.generateGomory(rhs, body);
        TestUtils.assertEquals(0.0 / 0.4, gomory.doubleValue(0));
        TestUtils.assertEquals(0.3 / 0.4, gomory.doubleValue(1));
        TestUtils.assertEquals(0.8 / 0.4, gomory.doubleValue(2));
        TestUtils.assertEquals(0.4 / 0.4, gomory.getRHS());

        Equation gomoryMixed = TableauCutGeneratorTest.generateGomoryMixed(rhs, 0, body);
        TestUtils.assertEquals(0.0 / 0.4, gomoryMixed.doubleValue(0));
        TestUtils.assertEquals(0.3 / 0.4, gomoryMixed.doubleValue(1));
        TestUtils.assertEquals(2.0 / 15.0 / 0.4, gomoryMixed.doubleValue(2));
        TestUtils.assertEquals(0.4 / 0.4, gomoryMixed.getRHS());

        TestUtils.assertEquals(gomory.getRHS(), gomoryMixed.getRHS());
        TestUtils.assertTrue(gomoryMixed.doubleValue(0) <= gomory.doubleValue(0));
        TestUtils.assertTrue(gomoryMixed.doubleValue(1) <= gomory.doubleValue(1));
        TestUtils.assertTrue(gomoryMixed.doubleValue(2) <= gomory.doubleValue(2));
    }

    /**
     * Test to explore some characteristics of {@link IntegerProblems#testP20130409a()}. It appears to add the
     * same cut over and over again. Believe it's just numerically "unfortunate". These cuts should be
     * filtered out somehow.
     *
     * @see IntegerProblems#testP20130409a()
     */
    @Test
    public void testExploreP20130409a() {

        NumberContext accuracy = NumberContext.of(7);

        Variable[] variables = { new Variable("x1").lower(BigMath.ZERO).weight(BigMath.ONE), new Variable("x2013").lower(BigMath.ZERO).integer(),
                new Variable("x2014").lower(BigMath.ZERO).integer() };

        ExpressionsBasedModel originalModel = new ExpressionsBasedModel(variables);

        Expression expr1 = originalModel.newExpression("Expr1");
        expr1.set(0, -1);
        expr1.set(1, 5100);
        expr1.set(2, -5000);
        expr1.upper(BigMath.ZERO);

        Expression expr2 = originalModel.newExpression("Expr2");
        expr2.set(0, 1);
        expr2.set(1, 5100);
        expr2.set(2, -5000);
        expr2.lower(BigMath.ZERO);

        Expression expr3 = originalModel.newExpression("Expr3");
        expr3.set(1, 5000);
        expr3.set(2, 5000);
        expr3.level(new BigDecimal(19105000));

        Optimisation.Result solutionMIP = Optimisation.Result.of(4200, Optimisation.State.OPTIMAL, 4200, 1892, 1929);
        Result actualMIP = originalModel.minimise();
        TestUtils.assertStateAndSolution(solutionMIP, actualMIP, accuracy);

        ExpressionsBasedModel relaxedModel = originalModel.copy();
        relaxedModel.relax();

        Result solutionLP = relaxedModel.minimise();

        TestUtils.assertEquals(1891.5841584158416, solutionLP.doubleValue(1));
        TestUtils.assertEquals(1929.4158415841584, solutionLP.doubleValue(2));

        TestUtils.assertTrue(relaxedModel.validate(solutionLP));

        TestUtils.assertTrue(relaxedModel.validate(solutionLP));

        ExpressionsBasedModel slackedModel = originalModel.copy();
        slackedModel.relax();
        slackedModel.getVariable(1).upper(3821);
        slackedModel.getVariable(2).upper(3821);
        Variable expr2_L = slackedModel.newVariable("Expr2_L_slack").lower(0);
        Variable expr1_U = slackedModel.newVariable("Expr1_U_slack").lower(0);
        Variable x2013_U = slackedModel.newVariable("x2013_U_slack").lower(0);
        Variable x2014_U = slackedModel.newVariable("x2014_U_slack").lower(0);

    }

    /**
     * <p>
     * A few examples from: Generating Gomory's Cuts for linear integer programming problems: the HOW and WHY
     * <p>
     * http://www.ms.unimelb.edu.au/~moshe/620-362/gomory/index.html
     */
    @Test
    public void testTheHowAndWhyGomory() {

        Equation cut1 = TableauCutGeneratorTest.generateGomory(3.75, 0.0, 1.0, -1.25, 0.25);
        TestUtils.assertEquals(1.0, cut1.getRHS());
        TestUtils.assertEquals(0.0 / 0.75, cut1.doubleValue(0));
        TestUtils.assertEquals(0.0 / 0.75, cut1.doubleValue(1));
        TestUtils.assertEquals(0.75 / 0.75, cut1.doubleValue(2));
        TestUtils.assertEquals(0.25 / 0.75, cut1.doubleValue(3));

        Equation cut2 = TableauCutGeneratorTest.generateGomory(8.0 / 3.0, 3.0, 1.25, 1.0 / 3.0);
        TestUtils.assertEquals(1.0, cut2.getRHS());
        TestUtils.assertEquals(0.0 / (2.0 / 3.0), cut2.doubleValue(0));
        TestUtils.assertEquals(0.25 / (2.0 / 3.0), cut2.doubleValue(1));
        TestUtils.assertEquals(1.0 / 3.0 / (2.0 / 3.0), cut2.doubleValue(2));

        Equation cut3 = TableauCutGeneratorTest.generateGomory(8.0 / 3.0, 3.0, -1.25, 1.0 / 3.0);
        TestUtils.assertEquals(1.0, cut3.getRHS());
        TestUtils.assertEquals(0.0 / (2.0 / 3.0), cut3.doubleValue(0));
        TestUtils.assertEquals(0.75 / (2.0 / 3.0), cut3.doubleValue(1));
        TestUtils.assertEquals(1.0 / 3.0 / (2.0 / 3.0), cut3.doubleValue(2));

        Equation cut4 = TableauCutGeneratorTest.generateGomory(73.0 / 7.0, 0 - 0, -0.75, 1.0 / 3.0, -13.0 / 4.0);
        TestUtils.assertEquals(0.0 / (3.0 / 7.0), cut4.doubleValue(0));
        TestUtils.assertEquals(0.25 / (3.0 / 7.0), cut4.doubleValue(1));
        TestUtils.assertEquals(1.0 / 3.0 / (3.0 / 7.0), cut4.doubleValue(2));
        TestUtils.assertEquals(0.75 / (3.0 / 7.0), cut4.doubleValue(3));
        TestUtils.assertEquals(1.0, cut4.getRHS());

        Equation cut5 = TableauCutGeneratorTest.generateGomory(35.0 / 4.0, 3.0, 17.0 / 5.0, -2.0 / 5.0);
        TestUtils.assertEquals(0.0 / 0.75, cut5.doubleValue(0));
        TestUtils.assertEquals(0.4 / 0.75, cut5.doubleValue(1));
        TestUtils.assertEquals(0.6 / 0.75, cut5.doubleValue(2));
        TestUtils.assertEquals(0.75 / 0.75, cut5.getRHS());

        Equation cut6 = TableauCutGeneratorTest.generateGomory(47.0 / 6.0, -3.25, 0.4, -0.4);
        TestUtils.assertEquals(0.75 / (5.0 / 6.0), cut6.doubleValue(0));
        TestUtils.assertEquals(0.4 / (5.0 / 6.0), cut6.doubleValue(1));
        TestUtils.assertEquals(0.6 / (5.0 / 6.0), cut6.doubleValue(2));
        TestUtils.assertEquals(5.0 / 6.0 / (5.0 / 6.0), cut6.getRHS());
    }

}
