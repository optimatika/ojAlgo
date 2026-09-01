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
package org.ojalgo.optimisation.linear;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerProblems;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.type.context.NumberContext;

public class TableauCutGeneratorTest extends OptimisationLinearTests {

    /**
     * Assumes nothing special about the factors. Just the normal Gomory cut requirement that the RHS must be
     * fractional, all variables integer and at least one of the factors non-zero. The returned cut equation
     * always has pivot index 0.
     */
    private static Equation generateGomory(final double rhs, final double... factors) {

        Primitive1D body = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return factors[index];
            }

            @Override
            public void set(final int index, final double value) {
                factors[index] = value;
            }

            @Override
            public int size() {
                return factors.length;
            }

        };

        return TableauCutGenerator.doGomory(body, 0, rhs, PrimitiveMath.ELEVENTH);
    }

    /**
     * Input must meet the requirements of
     * {@link TableauCutGenerator#doGomoryMixedInteger(Primitive1D, int, double, boolean[])}, but then
     * compares the generated cut with those of the more general implementations.
     */
    static Equation doTestAllImplementationsEqual(final double rhs, final int basis, final double... factors) {

        Primitive1D body = new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return factors[index];
            }

            @Override
            public void set(final int index, final double value) {
                factors[index] = value;
            }

            @Override
            public int size() {
                return factors.length;
            }

        };

        int[] excluded = new int[factors.length - 1];
        for (int j = 0, je = 0; j < factors.length; j++) {
            if (j != basis) {
                excluded[je++] = j;
            }
        }

        boolean[] integer = new boolean[factors.length];
        Arrays.fill(integer, true);

        Equation base = TableauCutGenerator.doGomoryMixedInteger(body, basis, rhs, integer);
        Equation tableau = TableauCutGenerator.doGomoryMixedInteger(body, basis, rhs, PrimitiveMath.ELEVENTH, excluded, integer, false);

        double[] lower = new double[factors.length];
        Arrays.fill(lower, PrimitiveMath.ZERO);

        double[] upper = new double[factors.length];
        Arrays.fill(upper, PrimitiveMath.POSITIVE_INFINITY);

        boolean[] atUpper = new boolean[factors.length];

        Equation unified = TableauCutGenerator.doGomoryMixedInteger(body, basis, rhs, PrimitiveMath.ELEVENTH, excluded, integer, lower, upper, atUpper, false);

        TestUtils.assertEquals(base.index, tableau.index);
        TestUtils.assertEquals(base.index, unified.index);

        TestUtils.assertEquals(base.getPivot(), tableau.getPivot());
        TestUtils.assertEquals(base.getPivot(), unified.getPivot());

        TestUtils.assertEquals(base.getRHS(), tableau.getRHS());
        TestUtils.assertEquals(base.getRHS(), unified.getRHS());

        TestUtils.assertEquals(base.getBody(), tableau.getBody());
        TestUtils.assertEquals(base.getBody(), unified.getBody());

        return unified;
    }

    /**
     * Test case based on output from a ChatGPT session. Couldn't really make it do what I wanted, but I
     * generated this test case.
     */
    @Test
    public void testChatGPT4() {

        Primitive1D body = Primitive1D.of(0.5, 1.5, -0.5, 1.0, 0.0, 0.0); // A simplex tableau row
        int index = 3;
        double rhs = 2.3;
        double fractionality = PrimitiveMath.ELEVENTH;
        int[] excluded = { 0, 1, 2 }; // Non-basic variables
        boolean[] integer = { true, true, false, true, true, false };

        double[] lower = new double[integer.length];
        Arrays.fill(lower, PrimitiveMath.ZERO);

        double[] upper = new double[integer.length];
        Arrays.fill(upper, PrimitiveMath.POSITIVE_INFINITY);

        boolean[] atUpper = new boolean[integer.length];

        Equation gomoryMixedCut = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upper, atUpper, false);

        double f0 = rhs - Math.floor(rhs);
        double cf0 = 1 - f0;
        double[] cut = gomoryMixedCut.getCoefficients();

        for (int i = 0; i < cut.length; i++) {

            double actual = cut[i];

            if (i == index || IndexOf.indexOf(excluded, i) < 0) {
                // Basic variable, should be 0.0

                TestUtils.assertEquals(PrimitiveMath.ZERO, actual);

            } else {
                // Non-basic variable

                double ai = body.doubleValue(i);
                double fi = ai - Math.floor(ai);
                double cfi = 1 - fi;

                if (integer[i]) {
                    Assertions.assertEquals(fi <= f0 ? fi / f0 : cfi / cf0, actual);
                } else {
                    Assertions.assertEquals(ai > 0 ? ai / f0 : -ai / cf0, actual);
                }
            }
        }
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

        Equation gomoryMixed = TableauCutGeneratorTest.doTestAllImplementationsEqual(rhs, 0, body);
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
     * Test generated by copilot (almost, had to tweak the code to make it work).
     */
    @Test
    public void testDoGomory() {

        Primitive1D body = Primitive1D.of(2.0, 1.5, 0.5);
        double rhs = 5.3;

        Equation gomoryCut = TableauCutGeneratorTest.generateGomory(rhs, body.toRawCopy1D());

        double f0 = rhs - Math.floor(rhs);
        double[] cut = gomoryCut.getBody().toRawCopy1D();

        for (int i = 0; i < cut.length; i++) {
            double ai = body.doubleValue(i);
            double fi = ai - Math.floor(ai);

            TestUtils.assertTrue(cut[i] == fi / f0);
        }
    }

    /**
     * Just a made up test (by copilot).
     */
    @Test
    public void testDoGomoryMixedInteger() {

        Primitive1D body = Primitive1D.of(1.0, 2.0, 1.5, 0.5);
        double rhs = 5.7;

        Equation gomoryMixedCut = TableauCutGeneratorTest.doTestAllImplementationsEqual(rhs, 0, body.toRawCopy1D());

        double f0 = rhs - Math.floor(rhs);
        double cf0 = 1 - f0;
        double[] cut = gomoryMixedCut.getBody().toRawCopy1D();

        for (int i = 0; i < cut.length; i++) {
            double ai = body.doubleValue(i);
            double fi = ai - Math.floor(ai);
            double cfi = 1 - fi;

            TestUtils.assertEquals(cut[i], fi <= f0 ? fi / f0 : cfi / cf0);
        }
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

        ExpressionsBasedModel originalModel = new ExpressionsBasedModel();

        NumberContext accuracy = NumberContext.of(7);

        Variable[] variables = { originalModel.newVariable("x1").lower(BigMath.ZERO).weight(BigMath.ONE),
                originalModel.newVariable("x2013").lower(BigMath.ZERO).integer(), originalModel.newVariable("x2014").lower(BigMath.ZERO).integer() };

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
     * Verify that the GMI formula produces the same cut regardless of finite vs infinite upper bounds, as
     * long as the variable is not at its upper bound.
     */
    @Test
    public void testFiniteVsInfiniteUpperBound() {

        Primitive1D body = Primitive1D.of(1.0, 1.5, -0.5);
        int index = 0;
        double rhs = 2.3;
        double fractionality = PrimitiveMath.ELEVENTH;
        int[] excluded = { 1, 2 };
        boolean[] integer = { true, true, false };

        double[] lower = { 0, 0, 0 };
        boolean[] atUpper = { false, false, false };

        double[] upperInf = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
        Equation infRange = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upperInf, atUpper, false);

        double[] upperFin = { Double.POSITIVE_INFINITY, 10, Double.POSITIVE_INFINITY };
        Equation finRange = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upperFin, atUpper, false);

        TestUtils.assertEquals(infRange.getRHS(), finRange.getRHS());

        for (int j = 0; j < integer.length; j++) {
            TestUtils.assertEquals(infRange.doubleValue(j), finRange.doubleValue(j));
        }
    }

    /**
     * Verify that non-zero shifts correctly adjust the cut RHS (lower-bound case). A shifted variable with
     * finite range triggers the {@code limit += tmpVal * shift} path in
     * {@link TableauCutGenerator#applyShiftBackConversion}.
     */
    @Test
    public void testNonZeroShiftLowerBound() {

        // 3 variables: x0 (integer, basic at index 0), x1 (integer, non-basic), x2 (continuous, non-basic)
        Primitive1D body = Primitive1D.of(1.0, 1.5, -0.5);
        int index = 0;
        double rhs = 2.3;
        double fractionality = PrimitiveMath.ELEVENTH;
        int[] excluded = { 1, 2 };
        boolean[] integer = { true, true, false };

        double[] lower0 = { 0, 0, 0 };
        double[] upper0 = { Double.POSITIVE_INFINITY, 10, Double.POSITIVE_INFINITY };
        boolean[] atUpper = { false, false, false };

        Equation baseline = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower0, upper0, atUpper, false);

        // Apply shift back-conversion with shift of 3.0 on x1
        Equation shifted = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower0, upper0, atUpper, false);
        double[] shift1 = { 0, 3.0, 0 };
        TableauCutGenerator.applyShiftBackConversion(shifted, excluded, lower0, upper0, shift1, atUpper);

        // x2 has infinite range, so shift has no effect on it
        TestUtils.assertEquals(baseline.doubleValue(2), shifted.doubleValue(2));

        // The RHS should differ: shifted limit = baseline limit + cut[1] * shift
        double expectedRHS = baseline.getRHS() + baseline.doubleValue(1) * 3.0;
        TestUtils.assertEquals(expectedRHS, shifted.getRHS());
    }

    /**
     * Verify that non-zero shifts correctly adjust the cut when the variable is at its upper bound. This
     * triggers the {@code limit -= tmpVal * shift; eq.set(j, -tmpVal)} path in
     * {@link TableauCutGenerator#applyShiftBackConversion}.
     */
    @Test
    public void testNonZeroShiftNegatedVariable() {

        // 3 variables: x0 (integer, basic), x1 (integer, non-basic, at upper bound), x2 (continuous)
        Primitive1D body = Primitive1D.of(1.0, 1.5, -0.5);
        int index = 0;
        double rhs = 2.3;
        double fractionality = PrimitiveMath.ELEVENTH;
        int[] excluded = { 1, 2 };
        boolean[] integer = { true, true, false };

        double[] lower = { 0, -10, 0 };
        double[] upper = { Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY };
        boolean[] atUpper = { false, true, false };

        Equation baseline = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upper, atUpper, false);

        // Apply shift back-conversion with shift of -2.0 on x1
        Equation shifted = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upper, atUpper, false);
        double[] shift1 = { 0, -2.0, 0 };
        TableauCutGenerator.applyShiftBackConversion(shifted, excluded, lower, upper, shift1, atUpper);

        // For an at-upper variable: limit -= tmpVal * shift, and coeff = -tmpVal
        double expectedRHS = baseline.getRHS() - baseline.doubleValue(1) * (-2.0);
        TestUtils.assertEquals(expectedRHS, shifted.getRHS());

        // The coefficient should be negated
        TestUtils.assertEquals(-baseline.doubleValue(1), shifted.doubleValue(1));
    }

    /**
     * Verify that the no-shift variant correctly handles a variable at its upper bound when UB > 0 (e.g. a
     * binary variable at value 1). Before the fix, this code path used {@code uppers[j] <= 0} for negVar
     * determination, which is wrong for unshifted variables — a binary at UB=1 has uppers[j]=1.0 so negVar
     * would be false when it should be true. The fix uses {@code atUpper[j]} from the simplex basis state.
     */
    @Test
    public void testNoShiftUpperBoundPositive() {

        // x0: integer, basic, fractional (source row for GMI cut)
        // x1: integer, non-basic, AT UPPER BOUND (UB=1, binary)
        // x2: continuous, non-basic, at lower bound
        Primitive1D body = Primitive1D.of(1.0, 0.7, -0.3);
        int index = 0;
        double rhs = 2.3;
        double fractionality = PrimitiveMath.ELEVENTH;
        int[] excluded = { 1, 2 };
        boolean[] integer = { true, true, false };

        double[] lower = { 0, 0, 0 };
        double[] upper = { Double.POSITIVE_INFINITY, 1, Double.POSITIVE_INFINITY };

        // x1 IS at its upper bound — atUpper[1] = true
        boolean[] atUpper = { false, true, false };

        Equation correct = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upper, atUpper, false);

        // x1 NOT at its upper bound — atUpper[1] = false
        boolean[] atLower = { false, false, false };

        Equation wrongNegVar = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upper, atLower, false);

        // The two must differ: negVar changes the sign of aj before computing the GMI coefficient
        Assertions.assertNotEquals(correct.doubleValue(1), wrongNegVar.doubleValue(1), "Cut coefficient must differ when negVar is correct vs incorrect");

        // Verify the correct cut manually:
        // negRHS = false (rhs > 0), negVar = true for x1
        // aj = body[1] = 0.7, negRHS ^ negVar = true, so aj = -0.7
        // For integer variable: fraction(-0.7) = -0.7 - floor(-0.7) = -0.7 - (-1) = 0.3
        // f0 = fraction(2.3) = 0.3, cf0 = 0.7
        // fj=0.3 <= f0=0.3, so cut[1] = 0.3/0.3 = 1.0

        double f0 = 0.3;
        TestUtils.assertEquals(1.0, correct.doubleValue(1));

        // With wrong negVar (false): aj = 0.7, no negation
        // fraction(0.7) = 0.7, fj=0.7 > f0=0.3, so cut[1] = (1-0.7)/(1-0.3) = 0.3/0.7
        TestUtils.assertEquals(0.3 / 0.7, wrongNegVar.doubleValue(1));
    }

    /**
     * <p>
     * A few examples from: Generating Gomory's Cuts for linear integer programming problems: the HOW and WHY
     * <p>
     * http://www.ms.unimelb.edu.au/~moshe/620-362/gomory/index.html
     * https://www.ou.edu/class/che-design/che5480-11/Gomory%20Cuts-The%20How%20and%20the%20Why.pdf
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
        TestUtils.assertEquals(1.0, cut4.getRHS());
        TestUtils.assertEquals(0.0 / (3.0 / 7.0), cut4.doubleValue(0));
        TestUtils.assertEquals(0.25 / (3.0 / 7.0), cut4.doubleValue(1));
        TestUtils.assertEquals(1.0 / 3.0 / (3.0 / 7.0), cut4.doubleValue(2));
        TestUtils.assertEquals(0.75 / (3.0 / 7.0), cut4.doubleValue(3));

        Equation cut5 = TableauCutGeneratorTest.generateGomory(35.0 / 4.0, 3.0, 17.0 / 5.0, -2.0 / 5.0);
        TestUtils.assertEquals(0.75 / 0.75, cut5.getRHS());
        TestUtils.assertEquals(0.0 / 0.75, cut5.doubleValue(0));
        TestUtils.assertEquals(0.4 / 0.75, cut5.doubleValue(1));
        TestUtils.assertEquals(0.6 / 0.75, cut5.doubleValue(2));

        Equation cut6 = TableauCutGeneratorTest.generateGomory(47.0 / 6.0, -3.25, 0.4, -0.4);
        TestUtils.assertEquals(5.0 / 6.0 / (5.0 / 6.0), cut6.getRHS());
        TestUtils.assertEquals(0.75 / (5.0 / 6.0), cut6.doubleValue(0));
        TestUtils.assertEquals(0.4 / (5.0 / 6.0), cut6.doubleValue(1));
        TestUtils.assertEquals(0.6 / (5.0 / 6.0), cut6.doubleValue(2));
    }

    /**
     * Verify that applying shift back-conversion with zero shifts produces the same result as no
     * back-conversion. Uses mixed integer/continuous variables and includes a variable at its upper bound.
     */
    @Test
    public void testZeroShiftBackConversionIsNoOp() {

        Primitive1D body = Primitive1D.of(1.0, 1.5, -0.5, 2.3);
        int index = 0;
        double rhs = 2.3;
        double fractionality = PrimitiveMath.ELEVENTH;
        int[] excluded = { 1, 2, 3 };
        boolean[] integer = { true, true, false, true };

        double[] lower = { 0, 0, 0, -5 };
        double[] upper = { Double.POSITIVE_INFINITY, 10, Double.POSITIVE_INFINITY, 0 };
        boolean[] atUpper = { false, false, false, true };

        Equation plain = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upper, atUpper, false);

        Equation withZeroShift = TableauCutGenerator.doGomoryMixedInteger(body, index, rhs, fractionality, excluded, integer, lower, upper, atUpper, false);
        double[] shift = { 0, 0, 0, 0 };
        TableauCutGenerator.applyShiftBackConversion(withZeroShift, excluded, lower, upper, shift, atUpper);

        TestUtils.assertEquals(plain.index, withZeroShift.index);
        TestUtils.assertEquals(plain.getRHS(), withZeroShift.getRHS());
        TestUtils.assertEquals(plain.getBody(), withZeroShift.getBody());
    }
}
