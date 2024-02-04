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
package org.ojalgo.random;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access2D;

public final class Random1D {

    public static void setSeed(final long seed) {
        Random1D.random().setSeed(seed);
    }

    private static Random random() {
        return ThreadLocalRandom.current();
    }

    public final int length;

    private final MatrixStore<Double> myCholeskiedCorrelations;

    public Random1D(final Access2D<?> correlations) {

        super();

        Cholesky<Double> cholesky = Cholesky.PRIMITIVE.make();
        cholesky.decompose(Primitive64Store.FACTORY.makeWrapper(correlations));
        myCholeskiedCorrelations = cholesky.getL();

        cholesky.reset();

        length = (int) myCholeskiedCorrelations.countRows();
    }

    /**
     * If the variables are uncorrelated.
     */
    public Random1D(final int size) {

        super();

        myCholeskiedCorrelations = null;

        length = size;
    }

    @SuppressWarnings("unused")
    private Random1D() {
        this(null);
    }

    /**
     * An array of correlated random numbers, provided that you gave a correlations matrix to the constructor.
     */
    public Array1D<Double> nextDouble() {

        Primitive64Store uncorrelated = Primitive64Store.FACTORY.make(length, 1);

        for (int i = 0; i < length; i++) {
            uncorrelated.set(i, 0, Random1D.random().nextDouble());
        }

        if (myCholeskiedCorrelations != null) {
            return ((Primitive64Store) myCholeskiedCorrelations.multiply(uncorrelated)).asList();
        } else {
            return uncorrelated.asList();
        }
    }

    /**
     * An array of correlated random numbers, provided that you gave a correlations matrix to the constructor.
     */
    public Array1D<Double> nextGaussian() {

        Primitive64Store uncorrelated = Primitive64Store.FACTORY.make(length, 1);

        for (int i = 0; i < length; i++) {
            uncorrelated.set(i, 0, Random1D.random().nextGaussian());
        }

        if (myCholeskiedCorrelations != null) {
            return ((Primitive64Store) myCholeskiedCorrelations.multiply(uncorrelated)).asList();
        } else {
            return uncorrelated.asList();
        }
    }

    public int size() {
        return length;
    }

}
