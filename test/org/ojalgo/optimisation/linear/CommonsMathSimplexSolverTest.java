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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;

/**
 * Contains the same test cases found in: org.apache.commons.math3.optimization.linear.SimplexSolverTest
 *
 * @author apete
 */
public class CommonsMathSimplexSolverTest extends OptimisationLinearTests {

    /**
     * Copied/stolen from Commons Math to simplify "copying" the test cases
     */
    enum GoalType implements Serializable {

        /**
         * Maximization goal.
         */
        MAXIMIZE,

        /**
         * Minimization goal.
         */
        MINIMIZE

    }

    static final class LinearConstraint {

        private final double[] myFactors;

        private final double myRhs;

        private final Relationship myType;

        public LinearConstraint(final double[] factors, final Relationship type, final double rhs) {
            myFactors = factors;
            myType = type;
            myRhs = rhs;
        }

        public double[] getFactors() {
            return myFactors;
        }

        public double getRhs() {
            return myRhs;
        }

        public Relationship getType() {
            return myType;
        }

    }

    static final class LinearObjectiveFunction {

        private final double myConstant;
        private final ExpressionsBasedModel myModel;

        public LinearObjectiveFunction(final double[] weights, final double constant) {

            super();

            final Variable[] tmpVariables = new Variable[weights.length];
            for (int v = 0; v < tmpVariables.length; v++) {
                tmpVariables[v] = new Variable("VAR" + v);
                tmpVariables[v].weight(new BigDecimal(weights[v]));
            }

            myModel = new ExpressionsBasedModel(tmpVariables);
            myConstant = constant;
        }

        public Expression addExpression(final String aName) {
            return myModel.addExpression(aName);
        }

        public double getConstant() {
            return myConstant;
        }

        public MultiaryFunction<Double> getObjectiveFunction() {
            return myModel.objective().toFunction();
        }

        public List<Variable> getVariables() {
            return myModel.getVariables();
        }

        public Result maximise() {
            return myModel.maximise();
        }

        public Result minimise() {
            return myModel.minimise();
        }

    }

    static final class PointValuePair {

        private final LinearObjectiveFunction myObjFunc;
        private final Optimisation.Result myResult;

        PointValuePair(final LinearObjectiveFunction objFunc, final Result result) {
            super();
            myObjFunc = objFunc;
            myResult = result;
        }

        public State getState() {
            return myResult.getState();
        }

        public double getValue() {
            final Access1D<?> tmpAccess = Access1D.wrap(this.getPoint());
            return myObjFunc.getObjectiveFunction().invoke(Access1D.asPrimitive1D(tmpAccess)) + myObjFunc.getConstant();
        }

        double[] getPoint() {

            final double[] retVal = new double[myResult.size()];
            for (int i = 0; i < retVal.length; i++) {
                retVal[i] = myResult.doubleValue(i);
            }

            return retVal;
        }
    }

    static final class Precision {

        /**
         * Smallest positive number such that {@code 1 - EPSILON} is not numerically equal to 1: {@value} .
         */
        public static final double EPSILON = 0x1.0p-53;
        /**
         * Safe minimum, such that {@code 1 / SAFE_MIN} does not overflow. In IEEE 754 arithmetic, this is
         * also the smallest normalized number 2<sup>-1022</sup>: {@value} .
         */
        public static final double SAFE_MIN = 0x1.0p-1022;
        /**
         * Offset to order signed double numbers lexicographically.
         */
        private static final long SGN_MASK = 0x8000000000000000L;
        /**
         * Offset to order signed double numbers lexicographically.
         */
        private static final int SGN_MASK_FLOAT = 0x80000000;

        /**
         * Compares two numbers given some amount of allowed error.
         *
         * @param x the first number
         * @param y the second number
         * @param eps the amount of error to allow when checking for equality
         * @return
         *         <ul>
         *         <li>0 if {@link #equals(double, double, double) equals(x, y, eps)}</li>
         *         <li>&lt; 0 if !{@link #equals(double, double, double) equals(x, y, eps)} &amp;&amp; x &lt;
         *         y</li>
         *         <li>>0 if !{@link #equals(double, double, double) equals(x, y, eps)} &amp;&amp; x > y</li>
         *         </ul>
         */
        public static int compareTo(final double x, final double y, final double eps) {
            if (Precision.equals(x, y, eps)) {
                return 0;
            } else if (x < y) {
                return -1;
            }
            return 1;
        }

        /**
         * Compares two numbers given some amount of allowed error. Two float numbers are considered equal if
         * there are {@code (maxUlps - 1)} (or fewer) floating point numbers between them, i.e. two adjacent
         * floating point numbers are considered equal. Adapted from
         * <a href="http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm"> Bruce
         * Dawson</a>
         *
         * @param x first value
         * @param y second value
         * @param maxUlps {@code (maxUlps - 1)} is the number of floating point values between {@code x} and
         *        {@code y}.
         * @return
         *         <ul>
         *         <li>0 if {@link #equals(double, double, int) equals(x, y, maxUlps)}</li>
         *         <li>&lt; 0 if !{@link #equals(double, double, int) equals(x, y, maxUlps)} &amp;&amp; x &lt;
         *         y</li>
         *         <li>>0 if !{@link #equals(double, double, int) equals(x, y, maxUlps)} &amp;&amp; x > y</li>
         *         </ul>
         */
        public static int compareTo(final double x, final double y, final int maxUlps) {
            if (Precision.equals(x, y, maxUlps)) {
                return 0;
            } else if (x < y) {
                return -1;
            }
            return 1;
        }

        /**
         * Returns true iff they are equal as defined by {@link #equals(double, double, int) equals(x, y, 1)}.
         *
         * @param x first value
         * @param y second value
         * @return {@code true} if the values are equal.
         */
        public static boolean equals(final double x, final double y) {
            return Precision.equals(x, y, 1);
        }

        /**
         * Returns {@code true} if there is no double value strictly between the arguments or the difference
         * between them is within the range of allowed error (inclusive).
         *
         * @param x First value.
         * @param y Second value.
         * @param eps Amount of allowed absolute error.
         * @return {@code true} if the values are two adjacent floating point numbers or they are within range
         *         of each other.
         */
        public static boolean equals(final double x, final double y, final double eps) {
            return Precision.equals(x, y, 1) || (PrimitiveFunction.ABS.invoke(y - x) <= eps);
        }

        /**
         * Returns true if both arguments are equal or within the range of allowed error (inclusive). Two
         * float numbers are considered equal if there are {@code (maxUlps - 1)} (or fewer) floating point
         * numbers between them, i.e. two adjacent floating point numbers are considered equal. Adapted from
         * <a href="http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm"> Bruce
         * Dawson</a>
         *
         * @param x first value
         * @param y second value
         * @param maxUlps {@code (maxUlps - 1)} is the number of floating point values between {@code x} and
         *        {@code y}.
         * @return {@code true} if there are fewer than {@code maxUlps} floating point values between
         *         {@code x} and {@code y}.
         */
        public static boolean equals(final double x, final double y, final int maxUlps) {
            long xInt = Double.doubleToLongBits(x);
            long yInt = Double.doubleToLongBits(y);

            // Make lexicographically ordered as a two's-complement integer.
            if (xInt < 0) {
                xInt = SGN_MASK - xInt;
            }
            if (yInt < 0) {
                yInt = SGN_MASK - yInt;
            }

            final boolean isEqual = PrimitiveFunction.ABS.invoke(xInt - yInt) <= maxUlps;

            return isEqual && !Double.isNaN(x) && !Double.isNaN(y);
        }

        /**
         * Returns true iff they are equal as defined by {@link #equals(float, float, int) equals(x, y, 1)}.
         *
         * @param x first value
         * @param y second value
         * @return {@code true} if the values are equal.
         */
        public static boolean equals(final float x, final float y) {
            return Precision.equals(x, y, 1);
        }

        /**
         * Returns true if both arguments are equal or within the range of allowed error (inclusive).
         *
         * @param x first value
         * @param y second value
         * @param eps the amount of absolute error to allow.
         * @return {@code true} if the values are equal or within range of each other.
         * @since 2.2
         */
        public static boolean equals(final float x, final float y, final float eps) {
            return Precision.equals(x, y, 1) || (PrimitiveFunction.ABS.invoke(y - x) <= eps);
        }

        /**
         * Returns true if both arguments are equal or within the range of allowed error (inclusive). Two
         * float numbers are considered equal if there are {@code (maxUlps - 1)} (or fewer) floating point
         * numbers between them, i.e. two adjacent floating point numbers are considered equal. Adapted from
         * <a href="http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm"> Bruce
         * Dawson</a>
         *
         * @param x first value
         * @param y second value
         * @param maxUlps {@code (maxUlps - 1)} is the number of floating point values between {@code x} and
         *        {@code y}.
         * @return {@code true} if there are fewer than {@code maxUlps} floating point values between
         *         {@code x} and {@code y}.
         * @since 2.2
         */
        public static boolean equals(final float x, final float y, final int maxUlps) {
            int xInt = Float.floatToIntBits(x);
            int yInt = Float.floatToIntBits(y);

            // Make lexicographically ordered as a two's-complement integer.
            if (xInt < 0) {
                xInt = SGN_MASK_FLOAT - xInt;
            }
            if (yInt < 0) {
                yInt = SGN_MASK_FLOAT - yInt;
            }

            final boolean isEqual = PrimitiveFunction.ABS.invoke(xInt - yInt) <= maxUlps;

            return isEqual && !Float.isNaN(x) && !Float.isNaN(y);
        }

        /**
         * Returns true if both arguments are NaN or neither is NaN and they are equal as defined by
         * {@link #equals(double, double) equals(x, y, 1)}.
         *
         * @param x first value
         * @param y second value
         * @return {@code true} if the values are equal or both are NaN.
         * @since 2.2
         */
        public static boolean equalsIncludingNaN(final double x, final double y) {
            return (Double.isNaN(x) && Double.isNaN(y)) || Precision.equals(x, y, 1);
        }

        /**
         * Returns true if both arguments are NaN or are equal or within the range of allowed error
         * (inclusive).
         *
         * @param x first value
         * @param y second value
         * @param eps the amount of absolute error to allow.
         * @return {@code true} if the values are equal or within range of each other, or both are NaN.
         * @since 2.2
         */
        public static boolean equalsIncludingNaN(final double x, final double y, final double eps) {
            return Precision.equalsIncludingNaN(x, y) || (PrimitiveFunction.ABS.invoke(y - x) <= eps);
        }

        /**
         * Returns true if both arguments are NaN or if they are equal as defined by
         * {@link #equals(double, double, int) equals(x, y, maxUlps)}.
         *
         * @param x first value
         * @param y second value
         * @param maxUlps {@code (maxUlps - 1)} is the number of floating point values between {@code x} and
         *        {@code y}.
         * @return {@code true} if both arguments are NaN or if there are less than {@code maxUlps} floating
         *         point values between {@code x} and {@code y}.
         * @since 2.2
         */
        public static boolean equalsIncludingNaN(final double x, final double y, final int maxUlps) {
            return (Double.isNaN(x) && Double.isNaN(y)) || Precision.equals(x, y, maxUlps);
        }

        /**
         * Returns true if both arguments are NaN or neither is NaN and they are equal as defined by
         * {@link #equals(float, float) equals(x, y, 1)}.
         *
         * @param x first value
         * @param y second value
         * @return {@code true} if the values are equal or both are NaN.
         * @since 2.2
         */
        public static boolean equalsIncludingNaN(final float x, final float y) {
            return (Float.isNaN(x) && Float.isNaN(y)) || Precision.equals(x, y, 1);
        }

        /**
         * Returns true if both arguments are NaN or are equal or within the range of allowed error
         * (inclusive).
         *
         * @param x first value
         * @param y second value
         * @param eps the amount of absolute error to allow.
         * @return {@code true} if the values are equal or within range of each other, or both are NaN.
         * @since 2.2
         */
        public static boolean equalsIncludingNaN(final float x, final float y, final float eps) {
            return Precision.equalsIncludingNaN(x, y) || (PrimitiveFunction.ABS.invoke(y - x) <= eps);
        }

        /**
         * Returns true if both arguments are NaN or if they are equal as defined by
         * {@link #equals(float, float, int) equals(x, y, maxUlps)}.
         *
         * @param x first value
         * @param y second value
         * @param maxUlps {@code (maxUlps - 1)} is the number of floating point values between {@code x} and
         *        {@code y}.
         * @return {@code true} if both arguments are NaN or if there are less than {@code maxUlps} floating
         *         point values between {@code x} and {@code y}.
         * @since 2.2
         */
        public static boolean equalsIncludingNaN(final float x, final float y, final int maxUlps) {
            return (Float.isNaN(x) && Float.isNaN(y)) || Precision.equals(x, y, maxUlps);
        }

        /**
         * Computes a number {@code delta} close to {@code originalDelta} with the property that
         * <p>
         *
         * <pre>
         * <code>
         *   x + delta - x
         * </code>
         * </pre>
         * <p>
         * is exactly machine-representable. This is useful when computing numerical derivatives, in order to
         * reduce roundoff errors.
         *
         * @param x Value.
         * @param originalDelta Offset value.
         * @return a number {@code delta} so that {@code x + delta} and {@code x} differ by a representable
         *         floating number.
         */
        public static double representableDelta(final double x, final double originalDelta) {
            return (x + originalDelta) - x;
        }

        /**
         * Rounds the given value to the specified number of decimal places. The value is rounded using the
         * {@link BigDecimal#ROUND_HALF_UP} method.
         *
         * @param x Value to round.
         * @param scale Number of digits to the right of the decimal point.
         * @return the rounded value.
         * @since 1.1 (previously in {@code MathUtils}, moved as of version 3.0)
         */
        public static double round(final double x, final int scale) {
            return Precision.round(x, scale, BigDecimal.ROUND_HALF_UP);
        }

        /**
         * Rounds the given value to the specified number of decimal places. The value is rounded using the
         * given method which is any method defined in {@link BigDecimal}. If {@code x} is infinite or
         * {@code NaN}, then the value of {@code x} is returned unchanged, regardless of the other parameters.
         *
         * @param x Value to round.
         * @param scale Number of digits to the right of the decimal point.
         * @param roundingMethod Rounding method as defined in {@link BigDecimal}.
         * @return the rounded value.
         * @throws ArithmeticException if {@code roundingMethod == ROUND_UNNECESSARY} and the specified
         *         scaling operation would require rounding.
         * @throws IllegalArgumentException if {@code roundingMethod} does not represent a valid rounding
         *         mode.
         * @since 1.1 (previously in {@code MathUtils}, moved as of version 3.0)
         */
        public static double round(final double x, final int scale, final int roundingMethod) {
            try {
                return (new BigDecimal(Double.toString(x)).setScale(scale, roundingMethod)).doubleValue();
            } catch (final NumberFormatException ex) {
                if (Double.isInfinite(x)) {
                    return x;
                } else {
                    return Double.NaN;
                }
            }
        }

        /**
         * Rounds the given value to the specified number of decimal places. The value is rounded using the
         * {@link BigDecimal#ROUND_HALF_UP} method.
         *
         * @param x Value to round.
         * @param scale Number of digits to the right of the decimal point.
         * @return the rounded value.
         * @since 1.1 (previously in {@code MathUtils}, moved as of version 3.0)
         */
        public static float round(final float x, final int scale) {
            return Precision.round(x, scale, BigDecimal.ROUND_HALF_UP);
        }

        /**
         * Rounds the given value to the specified number of decimal places. The value is rounded using the
         * given method which is any method defined in {@link BigDecimal}.
         *
         * @param x Value to round.
         * @param scale Number of digits to the right of the decimal point.
         * @param roundingMethod Rounding method as defined in {@link BigDecimal}.
         * @return the rounded value.
         * @since 1.1 (previously in {@code MathUtils}, moved as of version 3.0)
         */
        public static float round(final float x, final int scale, final int roundingMethod) {
            final float sign = Math.copySign(1f, x);
            final float factor = (float) PrimitiveFunction.POW.invoke(10.0f, scale) * sign;
            return (float) Precision.roundUnscaled(x * factor, sign, roundingMethod) / factor;
        }

        /**
         * Rounds the given non-negative value to the "nearest" integer. Nearest is determined by the rounding
         * method specified. Rounding methods are defined in {@link BigDecimal}.
         *
         * @param unscaled Value to round.
         * @param sign Sign of the original, scaled value.
         * @param roundingMethod Rounding method, as defined in {@link BigDecimal}.
         * @return the rounded value.
         * @throws RuntimeException if {@code roundingMethod} is not a valid rounding method.
         * @since 1.1 (previously in {@code MathUtils}, moved as of version 3.0)
         */
        private static double roundUnscaled(double unscaled, final double sign, final int roundingMethod) {
            switch (roundingMethod) {
            case BigDecimal.ROUND_CEILING:
                if (sign == -1) {
                    unscaled = PrimitiveFunction.FLOOR.invoke(Math.nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                } else {
                    unscaled = PrimitiveFunction.CEIL.invoke(Math.nextAfter(unscaled, Double.POSITIVE_INFINITY));
                }
                break;
            case BigDecimal.ROUND_DOWN:
                unscaled = PrimitiveFunction.FLOOR.invoke(Math.nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                break;
            case BigDecimal.ROUND_FLOOR:
                if (sign == -1) {
                    unscaled = PrimitiveFunction.CEIL.invoke(Math.nextAfter(unscaled, Double.POSITIVE_INFINITY));
                } else {
                    unscaled = PrimitiveFunction.FLOOR.invoke(Math.nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                }
                break;
            case BigDecimal.ROUND_HALF_DOWN: {
                unscaled = Math.nextAfter(unscaled, Double.NEGATIVE_INFINITY);
                final double fraction = unscaled - PrimitiveFunction.FLOOR.invoke(unscaled);
                if (fraction > 0.5) {
                    unscaled = PrimitiveFunction.CEIL.invoke(unscaled);
                } else {
                    unscaled = PrimitiveFunction.FLOOR.invoke(unscaled);
                }
                break;
            }
            case BigDecimal.ROUND_HALF_EVEN: {
                final double fraction = unscaled - PrimitiveFunction.FLOOR.invoke(unscaled);
                if (fraction > 0.5) {
                    unscaled = PrimitiveFunction.CEIL.invoke(unscaled);
                } else if (fraction < 0.5) {
                    unscaled = PrimitiveFunction.FLOOR.invoke(unscaled);
                } else {
                    // The following equality test is intentional and needed for rounding purposes
                    if ((PrimitiveFunction.FLOOR.invoke(unscaled) / 2.0) == PrimitiveFunction.FLOOR.invoke(PrimitiveFunction.FLOOR.invoke(unscaled) / 2.0)) { // even
                        unscaled = PrimitiveFunction.FLOOR.invoke(unscaled);
                    } else { // odd
                        unscaled = PrimitiveFunction.CEIL.invoke(unscaled);
                    }
                }
                break;
            }
            case BigDecimal.ROUND_HALF_UP: {
                unscaled = Math.nextAfter(unscaled, Double.POSITIVE_INFINITY);
                final double fraction = unscaled - PrimitiveFunction.FLOOR.invoke(unscaled);
                if (fraction >= 0.5) {
                    unscaled = PrimitiveFunction.CEIL.invoke(unscaled);
                } else {
                    unscaled = PrimitiveFunction.FLOOR.invoke(unscaled);
                }
                break;
            }
            case BigDecimal.ROUND_UNNECESSARY:
                if (unscaled != PrimitiveFunction.FLOOR.invoke(unscaled)) {
                    throw new RuntimeException();
                }
                break;
            case BigDecimal.ROUND_UP:
                unscaled = PrimitiveFunction.CEIL.invoke(Math.nextAfter(unscaled, Double.POSITIVE_INFINITY));
                break;
            default:
                throw new RuntimeException();
            }
            return unscaled;
        }

        /**
         * Private constructor.
         */
        private Precision() {
        }
    }

    /**
     * Copied/stolen from Commons Math to simplify "copying" the test cases
     */
    static enum Relationship {

        /**
         * Equality relationship.
         */
        EQ("="),

        /**
         * Greater than or equal relationship.
         */
        GEQ(">="),

        /**
         * Lesser than or equal relationship.
         */
        LEQ("<=");

        /**
         * Display string for the relationship.
         */
        private final String stringValue;

        /**
         * Simple constructor.
         *
         * @param stringValue display string for the relationship
         */
        private Relationship(final String stringValue) {
            this.stringValue = stringValue;
        }

        /**
         * Get the relationship obtained when multiplying all coefficients by -1.
         *
         * @return relationship obtained when multiplying all coefficients by -1
         */
        public Relationship oppositeRelationship() {
            switch (this) {
            case LEQ:
                return GEQ;
            case GEQ:
                return LEQ;
            default:
                return EQ;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return stringValue;
        }

    }

    static final class SimplexSolver {

        PointValuePair optimize(final LinearObjectiveFunction model, final Collection<LinearConstraint> constraints, final GoalType minOrMax,
                final boolean positiveOnlyVariables) {

            if (positiveOnlyVariables) {
                for (final Variable tmpVariable : model.getVariables()) {
                    tmpVariable.lower(BigMath.ZERO);
                }
            }

            for (final LinearConstraint tmpLinearConstraint : constraints) {
                final Expression tmpExpression = model.addExpression(tmpLinearConstraint.toString());
                final double[] tmpFactors = tmpLinearConstraint.getFactors();
                for (int i = 0; i < tmpFactors.length; i++) {
                    tmpExpression.set(i, tmpFactors[i]);
                }
                switch (tmpLinearConstraint.getType()) {
                case GEQ:
                    tmpExpression.lower(new BigDecimal(tmpLinearConstraint.getRhs()));
                    break;
                case LEQ:
                    tmpExpression.upper(new BigDecimal(tmpLinearConstraint.getRhs()));
                    break;
                default:
                    tmpExpression.level(new BigDecimal(tmpLinearConstraint.getRhs()));
                    break;
                }
            }

            //model.myModel.options.debug(LinearSolver.class);

            Optimisation.Result tmpResult = null;
            if (minOrMax == GoalType.MINIMIZE) {
                tmpResult = model.minimise();
            } else {
                tmpResult = model.maximise();
            }

            return new PointValuePair(model, tmpResult);
        }

    }

    @Test
    public void testDegeneracy() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.7 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 18.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 8.0));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        TestUtils.assertEquals(13.6, solution.getValue(), .0000001);
    }

    @Test
    public void testEpsilon() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 10, 5, 1 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 9, 8, 0 }, Relationship.EQ, 17));
        constraints.add(new LinearConstraint(new double[] { 0, 7, 8 }, Relationship.LEQ, 7));
        constraints.add(new LinearConstraint(new double[] { 10, 0, 2 }, Relationship.LEQ, 10));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        final double tmpError = 1E-14 / PrimitiveMath.THREE;
        TestUtils.assertEquals(1.0, solution.getPoint()[0], tmpError);
        TestUtils.assertEquals(1.0, solution.getPoint()[1], tmpError);
        TestUtils.assertEquals(0.0, solution.getPoint()[2], tmpError);
        TestUtils.assertEquals(15.0, solution.getValue(), tmpError);
    }

    //@Test(expected = NoFeasibleSolutionException.class)
    @Test
    public void testInfeasibleSolution() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.GEQ, 3));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair tmpResult = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

    @Test
    public void testLargeModel() {
        final double[] objective = new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

        final LinearObjectiveFunction f = new LinearObjectiveFunction(objective, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(this.equationFromString(objective.length, "x0 + x1 + x2 + x3 - x12 = 0"));
        constraints.add(this.equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 - x13 = 0"));
        constraints.add(this.equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 >= 49"));
        constraints.add(this.equationFromString(objective.length, "x0 + x1 + x2 + x3 >= 42"));
        constraints.add(this.equationFromString(objective.length, "x14 + x15 + x16 + x17 - x26 = 0"));
        constraints.add(this.equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x27 = 0"));
        constraints.add(this.equationFromString(objective.length, "x14 + x15 + x16 + x17 - x12 = 0"));
        constraints.add(this.equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x13 = 0"));
        constraints.add(this.equationFromString(objective.length, "x28 + x29 + x30 + x31 - x40 = 0"));
        constraints.add(this.equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 - x41 = 0"));
        constraints.add(this.equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 >= 49"));
        constraints.add(this.equationFromString(objective.length, "x28 + x29 + x30 + x31 >= 42"));
        constraints.add(this.equationFromString(objective.length, "x42 + x43 + x44 + x45 - x54 = 0"));
        constraints.add(this.equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x55 = 0"));
        constraints.add(this.equationFromString(objective.length, "x42 + x43 + x44 + x45 - x40 = 0"));
        constraints.add(this.equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x41 = 0"));
        constraints.add(this.equationFromString(objective.length, "x56 + x57 + x58 + x59 - x68 = 0"));
        constraints.add(this.equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 - x69 = 0"));
        constraints.add(this.equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 >= 51"));
        constraints.add(this.equationFromString(objective.length, "x56 + x57 + x58 + x59 >= 44"));
        constraints.add(this.equationFromString(objective.length, "x70 + x71 + x72 + x73 - x82 = 0"));
        constraints.add(this.equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x83 = 0"));
        constraints.add(this.equationFromString(objective.length, "x70 + x71 + x72 + x73 - x68 = 0"));
        constraints.add(this.equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x69 = 0"));
        constraints.add(this.equationFromString(objective.length, "x84 + x85 + x86 + x87 - x96 = 0"));
        constraints.add(this.equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 - x97 = 0"));
        constraints.add(this.equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 >= 51"));
        constraints.add(this.equationFromString(objective.length, "x84 + x85 + x86 + x87 >= 44"));
        constraints.add(this.equationFromString(objective.length, "x98 + x99 + x100 + x101 - x110 = 0"));
        constraints.add(this.equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x111 = 0"));
        constraints.add(this.equationFromString(objective.length, "x98 + x99 + x100 + x101 - x96 = 0"));
        constraints.add(this.equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x97 = 0"));
        constraints.add(this.equationFromString(objective.length, "x112 + x113 + x114 + x115 - x124 = 0"));
        constraints.add(this.equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 - x125 = 0"));
        constraints.add(this.equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 >= 49"));
        constraints.add(this.equationFromString(objective.length, "x112 + x113 + x114 + x115 >= 42"));
        constraints.add(this.equationFromString(objective.length, "x126 + x127 + x128 + x129 - x138 = 0"));
        constraints.add(this.equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x139 = 0"));
        constraints.add(this.equationFromString(objective.length, "x126 + x127 + x128 + x129 - x124 = 0"));
        constraints.add(this.equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x125 = 0"));
        constraints.add(this.equationFromString(objective.length, "x140 + x141 + x142 + x143 - x152 = 0"));
        constraints.add(this.equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 - x153 = 0"));
        constraints.add(this.equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 >= 59"));
        constraints.add(this.equationFromString(objective.length, "x140 + x141 + x142 + x143 >= 42"));
        constraints.add(this.equationFromString(objective.length, "x154 + x155 + x156 + x157 - x166 = 0"));
        constraints.add(this.equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x167 = 0"));
        constraints.add(this.equationFromString(objective.length, "x154 + x155 + x156 + x157 - x152 = 0"));
        constraints.add(this.equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x153 = 0"));
        constraints.add(this.equationFromString(objective.length, "x83 + x82 - x168 = 0"));
        constraints.add(this.equationFromString(objective.length, "x111 + x110 - x169 = 0"));
        constraints.add(this.equationFromString(objective.length, "x170 - x182 = 0"));
        constraints.add(this.equationFromString(objective.length, "x171 - x183 = 0"));
        constraints.add(this.equationFromString(objective.length, "x172 - x184 = 0"));
        constraints.add(this.equationFromString(objective.length, "x173 - x185 = 0"));
        constraints.add(this.equationFromString(objective.length, "x174 - x186 = 0"));
        constraints.add(this.equationFromString(objective.length, "x175 + x176 - x187 = 0"));
        constraints.add(this.equationFromString(objective.length, "x177 - x188 = 0"));
        constraints.add(this.equationFromString(objective.length, "x178 - x189 = 0"));
        constraints.add(this.equationFromString(objective.length, "x179 - x190 = 0"));
        constraints.add(this.equationFromString(objective.length, "x180 - x191 = 0"));
        constraints.add(this.equationFromString(objective.length, "x181 - x192 = 0"));
        constraints.add(this.equationFromString(objective.length, "x170 - x26 = 0"));
        constraints.add(this.equationFromString(objective.length, "x171 - x27 = 0"));
        constraints.add(this.equationFromString(objective.length, "x172 - x54 = 0"));
        constraints.add(this.equationFromString(objective.length, "x173 - x55 = 0"));
        constraints.add(this.equationFromString(objective.length, "x174 - x168 = 0"));
        constraints.add(this.equationFromString(objective.length, "x177 - x169 = 0"));
        constraints.add(this.equationFromString(objective.length, "x178 - x138 = 0"));
        constraints.add(this.equationFromString(objective.length, "x179 - x139 = 0"));
        constraints.add(this.equationFromString(objective.length, "x180 - x166 = 0"));
        constraints.add(this.equationFromString(objective.length, "x181 - x167 = 0"));
        constraints.add(this.equationFromString(objective.length, "x193 - x205 = 0"));
        constraints.add(this.equationFromString(objective.length, "x194 - x206 = 0"));
        constraints.add(this.equationFromString(objective.length, "x195 - x207 = 0"));
        constraints.add(this.equationFromString(objective.length, "x196 - x208 = 0"));
        constraints.add(this.equationFromString(objective.length, "x197 - x209 = 0"));
        constraints.add(this.equationFromString(objective.length, "x198 + x199 - x210 = 0"));
        constraints.add(this.equationFromString(objective.length, "x200 - x211 = 0"));
        constraints.add(this.equationFromString(objective.length, "x201 - x212 = 0"));
        constraints.add(this.equationFromString(objective.length, "x202 - x213 = 0"));
        constraints.add(this.equationFromString(objective.length, "x203 - x214 = 0"));
        constraints.add(this.equationFromString(objective.length, "x204 - x215 = 0"));
        constraints.add(this.equationFromString(objective.length, "x193 - x182 = 0"));
        constraints.add(this.equationFromString(objective.length, "x194 - x183 = 0"));
        constraints.add(this.equationFromString(objective.length, "x195 - x184 = 0"));
        constraints.add(this.equationFromString(objective.length, "x196 - x185 = 0"));
        constraints.add(this.equationFromString(objective.length, "x197 - x186 = 0"));
        constraints.add(this.equationFromString(objective.length, "x198 + x199 - x187 = 0"));
        constraints.add(this.equationFromString(objective.length, "x200 - x188 = 0"));
        constraints.add(this.equationFromString(objective.length, "x201 - x189 = 0"));
        constraints.add(this.equationFromString(objective.length, "x202 - x190 = 0"));
        constraints.add(this.equationFromString(objective.length, "x203 - x191 = 0"));
        constraints.add(this.equationFromString(objective.length, "x204 - x192 = 0"));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        TestUtils.assertEquals(7518.0, solution.getValue(), .0000001);
    }

    @Test
    public void testMath272() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 2, 1 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.GEQ, 1));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        TestUtils.assertEquals(0.0, solution.getPoint()[0], .0000001);
        TestUtils.assertEquals(1.0, solution.getPoint()[1], .0000001);
        TestUtils.assertEquals(1.0, solution.getPoint()[2], .0000001);
        TestUtils.assertEquals(3.0, solution.getValue(), .0000001);
    }

    @Test
    public void testMath286() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.6, 0.4 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0 }, Relationship.GEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0 }, Relationship.GEQ, 5.0));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        TestUtils.assertEquals(25.8, solution.getValue(), .0000001);
        TestUtils.assertEquals(23.0, solution.getPoint()[0] + solution.getPoint()[2] + solution.getPoint()[4], 0.0000001);
        TestUtils.assertEquals(23.0, solution.getPoint()[1] + solution.getPoint()[3] + solution.getPoint()[5], 0.0000001);
        TestUtils.assertTrue(solution.getPoint()[0] >= (10.0 - 0.0000001));
        TestUtils.assertTrue(solution.getPoint()[2] >= (8.0 - 0.0000001));
        TestUtils.assertTrue(solution.getPoint()[4] >= (5.0 - 0.0000001));
    }

    @Test
    public void testMath288() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 7, 3, 0, 0 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 3, 0, -5, 0 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 2, 0, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 0, 3, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0 }, Relationship.LEQ, 1.0));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        TestUtils.assertEquals(10.0, solution.getValue(), .0000001);
    }

    @Test
    public void testMath290GEQ() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.GEQ, -1.0));
        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        TestUtils.assertEquals(0, solution.getValue(), .0000001);
        TestUtils.assertEquals(0, solution.getPoint()[0], .0000001);
        TestUtils.assertEquals(0, solution.getPoint()[1], .0000001);
    }

    //@Test(expected = NoFeasibleSolutionException.class)
    @Test
    public void testMath290LEQ() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.LEQ, -1.0));
        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair tmpResult = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

    /**
     * https://issues.apache.org/jira/browse/MATH-293
     */
    @Test
    public void testMath293() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
        constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, 10.0));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution1 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        TestUtils.assertEquals(15.7143, solution1.getPoint()[0], .0001);
        TestUtils.assertEquals(0.0, solution1.getPoint()[1], .0001);
        TestUtils.assertEquals(14.2857, solution1.getPoint()[2], .0001);
        TestUtils.assertEquals(0.0, solution1.getPoint()[3], .0001);
        TestUtils.assertEquals(0.0, solution1.getPoint()[4], .0001);
        TestUtils.assertEquals(30.0, solution1.getPoint()[5], .0001);
        TestUtils.assertEquals(40.57143, solution1.getValue(), .0001);

        final double valA = (0.8 * solution1.getPoint()[0]) + (0.2 * solution1.getPoint()[1]);
        final double valB = (0.7 * solution1.getPoint()[2]) + (0.3 * solution1.getPoint()[3]);
        final double valC = (0.4 * solution1.getPoint()[4]) + (0.6 * solution1.getPoint()[5]);

        f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6 }, 0);
        constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
        constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, valA));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, valB));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, valC));

        final PointValuePair solution2 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        TestUtils.assertEquals(40.57143, solution2.getValue(), .0001);
    }

    @Test
    public void testMath434NegativeVariable() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.0, 0.0, 1.0 }, 0.0d);
        final ArrayList<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.EQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1 }, Relationship.GEQ, -10));

        final double epsilon = 1e-6;
        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        TestUtils.assertEquals(5.0, solution.getPoint()[0] + solution.getPoint()[1], epsilon);
        TestUtils.assertEquals(-10.0, solution.getPoint()[2], epsilon);
        TestUtils.assertEquals(-10.0, solution.getValue(), epsilon);

    }

    @Test
    public void testMath434PivotRowSelection() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);

        final double epsilon = 1e-6;
        final ArrayList<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 200 }, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 100 }, Relationship.GEQ, 0.499900001));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        TestUtils.assertTrue(Precision.compareTo(solution.getPoint()[0] * 200.d, 1.d, epsilon) >= 0);
        TestUtils.assertEquals(0.0050, solution.getValue(), epsilon);
    }

    @Test
    public void testMath434PivotRowSelection2() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.0d, 1.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d }, 0.0d);

        final ArrayList<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1.0d, -0.1d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d }, Relationship.EQ, -0.1d));
        constraints.add(new LinearConstraint(new double[] { 1.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d }, Relationship.GEQ, -1e-18d));
        constraints.add(new LinearConstraint(new double[] { 0.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d }, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] { 0.0d, 0.0d, 0.0d, 1.0d, 0.0d, -0.0128588d, 1e-5d }, Relationship.EQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] { 0.0d, 0.0d, 0.0d, 0.0d, 1.0d, 1e-5d, -0.0128586d }, Relationship.EQ, 1e-10d));
        constraints.add(new LinearConstraint(new double[] { 0.0d, 0.0d, 1.0d, -1.0d, 0.0d, 0.0d, 0.0d }, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] { 0.0d, 0.0d, 1.0d, 1.0d, 0.0d, 0.0d, 0.0d }, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] { 0.0d, 0.0d, 1.0d, 0.0d, -1.0d, 0.0d, 0.0d }, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] { 0.0d, 0.0d, 1.0d, 0.0d, 1.0d, 0.0d, 0.0d }, Relationship.GEQ, 0.0d));

        final double epsilon = 1e-7;
        final SimplexSolver simplex = new SimplexSolver();
        final PointValuePair solution = simplex.optimize(f, constraints, GoalType.MINIMIZE, false);

        TestUtils.assertTrue(Precision.compareTo(solution.getPoint()[0], -1e-18d, epsilon) >= 0);
        TestUtils.assertEquals(1.0d, solution.getPoint()[1], epsilon);
        TestUtils.assertEquals(0.0d, solution.getPoint()[2], epsilon);
        TestUtils.assertEquals(1.0d, solution.getValue(), epsilon);
    }

    //@Test(expected = NoFeasibleSolutionException.class)
    @Test
    public void testMath434UnfeasibleSolution() {
        final double epsilon = 1e-6;

        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 0.0 }, 0.0);
        final ArrayList<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { epsilon / 2, 0.5 }, Relationship.EQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1e-3, 0.1 }, Relationship.EQ, 10));

        final SimplexSolver solver = new SimplexSolver();
        // allowing only non-negative values, no feasible solution shall be found
        final PointValuePair tmpResult = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        TestUtils.assertEquals(State.INFEASIBLE, tmpResult.getState());
    }

    @Test
    public void testMath713NegativeVariable() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0d);
        final ArrayList<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 1));

        final double epsilon = 1e-6;
        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        TestUtils.assertTrue(Precision.compareTo(solution.getPoint()[0], 0.0d, epsilon) >= 0);
        TestUtils.assertTrue(Precision.compareTo(solution.getPoint()[1], 0.0d, epsilon) >= 0);
    }

    @Test
    public void testMinimization() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, -5);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        TestUtils.assertEquals(4.0, solution.getPoint()[0]);
        TestUtils.assertEquals(0.0, solution.getPoint()[1]);
        TestUtils.assertEquals(-13.0, solution.getValue());
    }

    /**
     * With no artificial variables needed (no equals and no greater than constraints) we can go straight to
     * Phase 2.
     */
    @Test
    public void testModelWithNoArtificialVars() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        TestUtils.assertEquals(2.0, solution.getPoint()[0]);
        TestUtils.assertEquals(2.0, solution.getPoint()[1]);
        TestUtils.assertEquals(50.0, solution.getValue());
    }

    @Test
    public void testRestrictVariablesToNonNegative() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 409, 523, 70, 204, 339 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 43, 56, 345, 56, 5 }, Relationship.LEQ, 4567456));
        constraints.add(new LinearConstraint(new double[] { 12, 45, 7, 56, 23 }, Relationship.LEQ, 56454));
        constraints.add(new LinearConstraint(new double[] { 8, 768, 0, 34, 7456 }, Relationship.LEQ, 1923421));
        constraints.add(new LinearConstraint(new double[] { 12342, 2342, 34, 678, 2342 }, Relationship.GEQ, 4356));
        constraints.add(new LinearConstraint(new double[] { 45, 678, 76, 52, 23 }, Relationship.EQ, 456356));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        TestUtils.assertEquals(2902.92783505155, solution.getPoint()[0], .0000001);
        TestUtils.assertEquals(480.419243986254, solution.getPoint()[1], .0000001);
        TestUtils.assertEquals(0.0, solution.getPoint()[2], .0000001);
        TestUtils.assertEquals(0.0, solution.getPoint()[3], .0000001);
        TestUtils.assertEquals(0.0, solution.getPoint()[4], .0000001);
        TestUtils.assertEquals(1438556.7491409, solution.getValue(), .000001); // TODO 2013-12-01 Minskade delta från .0000001 till .000001
    }

    @Test
    public void testSimplexSolver() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 7);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 4));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        TestUtils.assertEquals(2.0, solution.getPoint()[0]);
        TestUtils.assertEquals(2.0, solution.getPoint()[1]);
        TestUtils.assertEquals(57.0, solution.getValue());
    }

    @Test
    public void testSingleVariableAndConstraint() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        TestUtils.assertEquals(10.0, solution.getPoint()[0]);
        TestUtils.assertEquals(30.0, solution.getValue());
    }

    @Test
    public void testSolutionWithNegativeDecisionVariable() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 14));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        final double tmpError = 10.0 * (1E-14 / PrimitiveMath.THREE);
        TestUtils.assertEquals(-2.0, solution.getPoint()[0], tmpError);
        TestUtils.assertEquals(8.0, solution.getPoint()[1], tmpError);
        TestUtils.assertEquals(12.0, solution.getValue(), tmpError);
    }

    @Test
    public void testTrivialModel() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 0));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        TestUtils.assertEquals(0, solution.getValue(), .0000001);
    }

    //@Test(expected = UnboundedSolutionException.class)
    @Test
    public void testUnboundedSolution() {
        final LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        final Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 2));

        final SimplexSolver solver = new SimplexSolver();
        final PointValuePair tmpResult = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        TestUtils.assertEquals(State.UNBOUNDED, tmpResult.getState());
    }

    /**
     * Converts a test string to a {@link LinearConstraint}. Ex: x0 + x1 + x2 + x3 - x12 = 0
     */
    private LinearConstraint equationFromString(final int numCoefficients, final String s) {
        Relationship relationship;
        if (s.contains(">=")) {
            relationship = Relationship.GEQ;
        } else if (s.contains("<=")) {
            relationship = Relationship.LEQ;
        } else if (s.contains("=")) {
            relationship = Relationship.EQ;
        } else {
            throw new IllegalArgumentException();
        }

        final String[] equationParts = s.split("[>|<]?=");
        final double rhs = Double.parseDouble(equationParts[1].trim());

        final double[] lhs = new double[numCoefficients];
        final String left = equationParts[0].replaceAll(" ?x", "");
        final String[] coefficients = left.split(" ");
        for (final String coefficient : coefficients) {
            final double value = coefficient.charAt(0) == '-' ? -1 : 1;
            final int index = Integer.parseInt(coefficient.replaceFirst("[+|-]", "").trim());
            lhs[index] = value;
        }
        return new LinearConstraint(lhs, relationship, rhs);
    }

}
