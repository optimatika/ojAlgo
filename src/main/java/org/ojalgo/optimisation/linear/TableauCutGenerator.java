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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.Arrays;

import org.ojalgo.equation.Equation;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.type.context.NumberContext;

abstract class TableauCutGenerator {

    private static final NumberContext ACCURACY = NumberContext.of(4, 16);
    private static final boolean DEBUG = false;

    private static double fraction(final double value) {
        return value - Math.floor(value);
    }

    private static boolean isFractionalEnough(final double value, final double fraction, final double away) {
        if (ACCURACY.isSmall(value, Math.min(fraction, ONE - fraction))) {
            return false;
        } else {
            return away < fraction && fraction < ONE - away;
        }
    }

    /**
     * Calculates a Gomory cut – all variables must be integer.
     * 
     * @param body Simplex tableau row (excluding the RHS column)
     * @param index The pivot index of the returned equation. Otherwise not used in the generation.
     * @param rhs The right hand side value of the row – the value that should be an integer.
     * @param fractionality The fractionality threshold. If the fractional part of the right hand side is less
     *        than this value, no cut is generated.
     * @return A Gomory cut equation, or null if no cut was generated.
     */
    static Equation doGomory(final Primitive1D body, final int index, final double rhs, final double fractionality) {

        int nbVariables = body.size();

        double f0 = TableauCutGenerator.fraction(rhs);
        if (!TableauCutGenerator.isFractionalEnough(rhs, f0, fractionality)) {
            return null;
        }

        double[] cut = new double[nbVariables];

        for (int j = 0; j < nbVariables; j++) {

            double aj = body.doubleValue(j);

            if (!ACCURACY.isZero(aj)) {

                double fj = TableauCutGenerator.fraction(aj);
                if (!ACCURACY.isZero(fj)) {
                    cut[j] = fj / f0;
                }
            }
        }

        return Equation.of(ONE, index, cut);
    }

    /**
     * Assumes all variables positive [0,∞). Further, this method assumes:
     * <ul>
     * <li>body.length == integer.length
     * <li>body[index] == 1.0
     * <li>integer[index] == true
     * <li>rhs > 0.0
     * </ul>
     * 
     * @param body The equation body.
     * @param index The index of the variable that should integer valued, but is not.
     * @param rhs The equation right hand side – the value of the variable that should be integer, but is not.
     * @param integer Which of the variables are integer?
     * @return A GMI cut
     */
    static Equation doGomoryMixedInteger(final Primitive1D body, final int index, final double rhs, final boolean[] integer) {

        int nbVariables = integer.length;
        if (body.size() != nbVariables || ACCURACY.isDifferent(ONE, body.doubleValue(index)) || !integer[index] || rhs <= ZERO) {
            throw new IllegalArgumentException();
        }

        if (DEBUG) {
            BasicLogger.debug(1, "{} {} = {}", index, rhs, body);
            BasicLogger.debug(1, "Integers: {}", Arrays.toString(integer));
        }

        double f0 = TableauCutGenerator.fraction(rhs);
        double cf0 = ONE - f0;

        double[] cut = new double[nbVariables];

        for (int j = 0; j < nbVariables; j++) {

            double aj = body.doubleValue(j);

            if (integer[j]) {

                double fj = TableauCutGenerator.fraction(aj);
                if (fj <= f0) {
                    cut[j] = fj / f0;
                } else {
                    double cfj = ONE - fj;
                    cut[j] = cfj / cf0;
                }

            } else if (aj > ZERO) {
                cut[j] = aj / f0;
            } else if (aj < ZERO) {
                cut[j] = -aj / cf0;
            }
        }

        return Equation.of(ONE, index, cut);
    }

    static Equation doGomoryMixedInteger(final Primitive1D body, final int index, final double rhs, final double fractionality, final int[] excluded,
            final boolean[] integer) {

        int nbVariables = integer.length; // Excluding artificial variables
        if (body.size() < nbVariables) {
            throw new IllegalArgumentException();
        }

        if (DEBUG) {
            BasicLogger.debug(1, "{} {} = {}", index, rhs, body);
            BasicLogger.debug(1, "Integer:{}", Arrays.toString(integer));
        }

        double f0 = TableauCutGenerator.fraction(rhs);
        if (!TableauCutGenerator.isFractionalEnough(rhs, f0, fractionality)) {
            return null;
        }
        double cf0 = ONE - f0;

        double limit = ONE;
        double[] cut = new double[nbVariables];

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];

            if (j < nbVariables) {

                double aj = body.doubleValue(j);

                if (!ACCURACY.isZero(aj)) {

                    if (integer[j]) {

                        double fj = TableauCutGenerator.fraction(aj);

                        if (fj <= f0) {
                            if (!ACCURACY.isZero(fj)) {
                                cut[j] = fj / f0;
                            }
                        } else {
                            double cfj = ONE - fj;
                            if (!ACCURACY.isZero(cfj)) {
                                cut[j] = cfj / cf0;
                            }
                        }

                    } else if (aj > ZERO) {
                        cut[j] = aj / f0;
                    } else if (aj < ZERO) {
                        cut[j] = -aj / cf0;
                    }
                }

            } else {

                if (DEBUG) {
                    BasicLogger.debug("Artificial variable: {} {}", j, body.doubleValue(j));
                }
            }
        }

        return Equation.of(limit, index, cut);
    }

    /**
     * Calculates a Gomory Mixed Integer (GMI) cut.
     * 
     * @param body The equation body (simplex tableau row). The tableau is assumed to be in an optimal (phase
     *        2) state. Any reference to artificial variables will be ignored.
     * @param index Index (tableau column) of the variable to be cut. A basic variable that should be integer,
     *        but is not. The body value at this index must be integer (not 0) and if this actually is from a
     *        tableau row it should be (will be) 1.
     * @param rhs The equation right hand side value – the value that should be an integer.
     * @param fractionality The fractionality threshold.
     * @param excluded Indices of the non-basic variables (excluded from the basis).
     * @param integers Which variables are integer? There must be one element for each variable - the length
     *        of this array defines the number of variables.
     * @param lowers Variables' lower bounds.
     * @param uppers Variables' upper bounds.
     * @param shifts The variable bounds may be shifted from their original values.
     * @return A GMI cut equation, or null if no cut was generated.
     */
    static Equation doGomoryMixedInteger(final Primitive1D body, final int index, final double rhs, final double fractionality, final int[] excluded,
            final boolean[] integers, final double[] lowers, final double[] uppers, final double[] shifts) {

        int nbVariables = integers.length; // Excluding artificial variables
        if (body.size() < nbVariables || lowers.length < nbVariables || uppers.length < nbVariables || shifts.length < nbVariables) {
            throw new IllegalArgumentException();
        }

        if (DEBUG) {
            BasicLogger.debug(1, "{} {} = {}", index, rhs, body);
            BasicLogger.debug(1, "Integer:{}", Arrays.toString(integers));
            BasicLogger.debug(1, "Lower:  {}", Arrays.toString(lowers));
            BasicLogger.debug(1, "Upper:  {}", Arrays.toString(uppers));
            BasicLogger.debug(1, "Shift:  {}", Arrays.toString(shifts));
        }

        boolean negRHS = rhs < ZERO;

        double f0 = TableauCutGenerator.fraction(negRHS ? -rhs : rhs);
        if (!TableauCutGenerator.isFractionalEnough(rhs, f0, fractionality)) {
            return null;
        }
        double cf0 = ONE - f0;

        double limit = ONE;
        double[] cut = new double[nbVariables];

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];

            if (j < nbVariables) {

                boolean negVar = uppers[j] <= ZERO;

                double aj = body.doubleValue(j);
                if (negRHS ^ negVar) {
                    aj = -aj;
                }

                if (!ACCURACY.isZero(aj)) {

                    if (integers[j]) {

                        double fj = TableauCutGenerator.fraction(aj);

                        if (fj <= f0) {
                            if (!ACCURACY.isZero(fj)) {
                                cut[j] = fj / f0;
                            }
                        } else {
                            double cfj = ONE - fj;
                            if (!ACCURACY.isZero(cfj)) {
                                cut[j] = cfj / cf0;
                            }
                        }

                    } else if (aj > ZERO) {
                        cut[j] = aj / f0;
                    } else if (aj < ZERO) {
                        cut[j] = -aj / cf0;
                    }
                }

                double range = uppers[j] - lowers[j];
                double shift = shifts[j];
                /*
                 * Attempt to handle implied bound slack variables. The idea is that a finite variable range
                 * implies a constraint, as well as a slack variable, expressing the upper bound. We would
                 * have a constraint saying that the sum of the actual variable and the slack variable is
                 * equal to the (upper) bound. So, here we assume the actual variable to be basic and at a
                 * value corresponding to the shift. Then the slack variable is non-basic and at a value
                 * corresponding to the range.
                 */
                if (Double.isFinite(range) && !ACCURACY.isZero(shift)) {

                    double tmpVal = cut[j];

                    if (negVar) {
                        // Uppger bound

                        limit -= tmpVal * shift;

                        cut[j] = -tmpVal;

                    } else {
                        // Lower bound

                        limit += tmpVal * shift;
                    }
                }

            } else {

                if (DEBUG) {
                    BasicLogger.debug("Artificial variable: {} {}", j, body.doubleValue(j));
                }
            }
        }

        return Equation.of(limit, index, cut);
    }

}
