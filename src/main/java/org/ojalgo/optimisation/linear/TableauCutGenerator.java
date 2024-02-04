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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.equation.Equation;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

abstract class TableauCutGenerator {

    private static final boolean DEBUG = false;

    private static final NumberContext ACCURACY = NumberContext.of(4, 16);

    /**
     * All variables must be integer.
     */
    static Equation doGomory(final Primitive1D body, final int variableIndex, final double rhs, final double fractionality) {

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

        return Equation.of(ONE, variableIndex, cut);
    }

    static Equation doGomoryMixedInteger(final Primitive1D body, final int variableIndex, final double rhs, final boolean[] integer, final double fractionality,
            final boolean[] negated, final int[] excluded) {

        int nbVariables = integer.length;
        if (body.size() < nbVariables || negated.length != nbVariables) {
            throw new IllegalArgumentException();
        }

        if (DEBUG) {
            BasicLogger.debug(1, "{} {} = {}", variableIndex, rhs, body);
            BasicLogger.debug(1, "Integers: {}", Arrays.toString(integer));
            BasicLogger.debug(1, "Negated:  {}", Arrays.toString(negated));
        }

        // boolean negateRHS = rhs < ZERO;
        // boolean negateRHS = negated[variableIndex];
        boolean negateRHS = false;

        double f0 = TableauCutGenerator.fraction(negateRHS ? -rhs : rhs);
        if (!TableauCutGenerator.isFractionalEnough(rhs, f0, fractionality)) {
            return null;
        }
        double cf0 = ONE - f0;

        double[] cut = new double[nbVariables];

        for (int je = 0; je < excluded.length; je++) {
            int j = excluded[je];

            if (j < nbVariables) {

                double aj = negateRHS ^ negated[j] ? -body.doubleValue(j) : body.doubleValue(j);

                if (j != variableIndex && !ACCURACY.isZero(aj)) {

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
                    } else {
                        cut[j] = -aj / cf0;
                    }
                }
            }
        }

        return Equation.of(ONE, variableIndex, cut);
    }

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

}
