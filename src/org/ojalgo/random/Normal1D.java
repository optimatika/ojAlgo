/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.random;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;

public final class Normal1D extends RandomNumber1D {

    static Access2D<?> correlations(final Access2D<?> covariances) {

        final int tmpDim = (int) covariances.countRows();

        final Array2D<Double> retVal = Array2D.PRIMITIVE.makeZero(tmpDim, tmpDim);
        final Array1D<Double> tmpStdDev = Array1D.PRIMITIVE.makeZero(tmpDim);

        for (int ij = 0; ij < tmpDim; ij++) {
            tmpStdDev.set(ij, Math.sqrt(covariances.doubleValue(ij, ij)));
        }

        double tmpCorrelation;
        for (int j = 0; j < tmpDim; j++) {

            retVal.set(j, j, ONE);

            for (int i = j + 1; i < tmpDim; i++) {

                tmpCorrelation = covariances.doubleValue(i, j) / (tmpStdDev.doubleValue(i) * tmpStdDev.doubleValue(j));

                retVal.set(i, j, tmpCorrelation);
                retVal.set(j, i, tmpCorrelation);
            }
        }

        return retVal;
    }

    private final Array1D<Double> myLocations;
    private final Array1D<Double> myScales;

    public Normal1D(final Access1D<?> locations, final Access2D<?> covariances) {

        super(Normal1D.correlations(covariances));

        final int tmpDim = (int) covariances.countRows();

        myLocations = Array1D.PRIMITIVE.copy(locations);
        myScales = Array1D.PRIMITIVE.makeZero(tmpDim);
        for (int ij = 0; ij < tmpDim; ij++) {
            myScales.set(ij, Math.sqrt(covariances.doubleValue(ij, ij)));
        }
    }

    private Normal1D(final Access2D<?> correlations) {
        super(correlations);
        myLocations = null;
        myScales = null;
    }

    public Array1D<Double> doubleValue() {

        final Array1D<Double> retVal = this.random().nextGaussian();

        for (int i = 0; i < retVal.length; i++) {
            retVal.set(i, myLocations.doubleValue(i) + (myScales.doubleValue(i) * retVal.doubleValue(i)));
        }

        return retVal;
    }

    @Override
    public Array1D<Double> getExpected() {
        return myLocations;
    }

    @Override
    public Array1D<Double> getStandardDeviation() {
        return myScales;
    }

}
