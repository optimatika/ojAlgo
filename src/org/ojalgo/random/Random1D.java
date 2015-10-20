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

import java.util.Random;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.Alternator;

public class Random1D {

    public final int length;

    private final Alternator<Random> myAlternator = RandomNumber.makeRandomAlternator();
    private final MatrixStore<Double> myCholeskiedCorrelations;

    public Random1D(final Access2D<?> aCorrelationsMatrix) {

        super();

        final Cholesky<Double> tmpCholesky = Cholesky.makePrimitive();
        tmpCholesky.decompose(MatrixStore.PRIMITIVE.makeWrapper(aCorrelationsMatrix));
        myCholeskiedCorrelations = tmpCholesky.getL();

        tmpCholesky.reset();

        length = (int) myCholeskiedCorrelations.countRows();
    }

    /**
     * If the variables are uncorrelated.
     */
    public Random1D(final int aLength) {

        super();

        myCholeskiedCorrelations = null;

        length = aLength;
    }

    @SuppressWarnings("unused")
    private Random1D() {
        this(null);
    }

    /**
     * An array of correlated random numbers, provided that you gave a correlations matrix to the constructor.
     */
    public Array1D<Double> nextDouble() {

        final PrimitiveDenseStore tmpUncorrelated = PrimitiveDenseStore.FACTORY.makeZero(length, 1);

        for (int i = 0; i < length; i++) {
            tmpUncorrelated.set(i, 0, this.random().nextDouble());
        }

        if (myCholeskiedCorrelations != null) {
            return ((PrimitiveDenseStore) myCholeskiedCorrelations.multiply(tmpUncorrelated)).asList();
        } else {
            return tmpUncorrelated.asList();
        }
    }

    /**
     * An array of correlated random numbers, provided that you gave a correlations matrix to the constructor.
     */
    public Array1D<Double> nextGaussian() {

        final PrimitiveDenseStore tmpUncorrelated = PrimitiveDenseStore.FACTORY.makeZero(length, 1);

        for (int i = 0; i < length; i++) {
            tmpUncorrelated.set(i, 0, this.random().nextGaussian());
        }

        if (myCholeskiedCorrelations != null) {
            return ((PrimitiveDenseStore) myCholeskiedCorrelations.multiply(tmpUncorrelated)).asList();
        } else {
            return tmpUncorrelated.asList();
        }
    }

    public int size() {
        return length;
    }

    protected Random random() {
        return myAlternator.get();
    }

}
