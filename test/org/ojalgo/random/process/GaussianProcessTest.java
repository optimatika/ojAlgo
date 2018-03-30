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
package org.ojalgo.random.process;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Normal;
import org.ojalgo.random.process.GaussianField.Mean;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.ComparableToDouble;

/**
 * @author apete
 */
public class GaussianProcessTest {

    @Test
    public void testTutorial() {

        final GaussianField.Covariance<Double> tmpCovar = new GaussianField.Covariance<Double>() {

            public void calibrate(final Collection<ComparableToDouble<Double>> observations, final Mean<Double> mean) {
            }

            public double invoke(final Double anArg1, final Double anArg2) {
                return this.invoke(anArg1.doubleValue(), anArg2.doubleValue());
            }

            double invoke(final double anArg1, final double anArg2) {

                final double tmpSF = 1.27;
                final double tmpSN = 0.3;

                final double tmpL = 1.0;

                double retVal = tmpSF * tmpSF * PrimitiveFunction.EXP.invoke(-PrimitiveFunction.POW.invoke(anArg1 - anArg2, TWO) / (TWO * tmpL * tmpL));

                if (anArg1 == anArg2) {
                    retVal += tmpSN * tmpSN;
                }

                return retVal;
            }

        };

        final GaussianProcess tmpProc = new GaussianProcess(tmpCovar);
        tmpProc.addObservation(-1.5, -1.6);
        tmpProc.addObservation(-1.0, -1.1);
        tmpProc.addObservation(-0.75, -0.4);
        tmpProc.addObservation(-0.4, 0.1);
        tmpProc.addObservation(-0.25, 0.5);
        tmpProc.addObservation(0.0, 0.8);

        final PrimitiveDenseStore tmpExpected = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.7029, 1.423379254178694, 1.2174807940480699, 0.8807634427271873, 0.7384394292014367, 0.5236319646022823 },
                        { 1.423379254178694, 1.7029, 1.5632762838868954, 1.3472073239852407, 1.2174807940480699, 0.9782733010505065 },
                        { 1.2174807940480699, 1.5632762838868954, 1.7029, 1.5170744874003474, 1.423379254178694, 1.2174807940480699 },
                        { 0.8807634427271873, 1.3472073239852407, 1.5170744874003474, 1.7029, 1.5948565596534579, 1.4888943550870049 },
                        { 0.7384394292014367, 1.2174807940480699, 1.423379254178694, 1.5948565596534579, 1.7029, 1.5632762838868954 },
                        { 0.5236319646022823, 0.9782733010505065, 1.2174807940480699, 1.4888943550870049, 1.5632762838868954, 1.7029 } });
        TestUtils.assertEquals(tmpExpected, tmpProc.getCovariances(), new NumberContext(8, 2));

        final Normal tmpDistr = tmpProc.getDistribution(0.2);
        TestUtils.assertEquals("Mean", 0.911277527445648, tmpDistr.getExpected(), 0.005);
        TestUtils.assertEquals("Variance", 0.20604504349662636, tmpDistr.getVariance(), 0.005);
    }
}
