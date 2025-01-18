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
package org.ojalgo.series.primitive;

import org.ojalgo.data.DataProcessors;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;

public class SeriesSet {

    private final PrimitiveSeries[] mySet;

    SeriesSet(final PrimitiveSeries[] set) {
        super();
        mySet = set;
    }

    public SeriesSet differences() {

        PrimitiveSeries[] retSet = new PrimitiveSeries[mySet.length];

        for (int i = 0; i < retSet.length; i++) {
            retSet[i] = mySet[i].differences();
        }

        return new SeriesSet(retSet);
    }

    public <M extends Mutate2D> M getCorrelations(final Factory2D<M> factory) {
        return DataProcessors.correlations(factory, mySet);
    }

    public <M extends Mutate2D> M getCovariances(final Factory2D<M> factory) {
        return DataProcessors.covariances(factory, mySet);
    }

    /**
     * Return a {@link Structure2D} with variables in columns and matching samples in rows.
     */
    public <M extends Access2D<?>> M getData(final Factory2D.TwoStep<M, ?> factory) {

        int nbCols = mySet.length;
        int nbRows = mySet[0].size();
        Factory2D.Builder<M> builder = factory.newBuilder(nbRows, nbCols);
        for (int j = 0; j < nbCols; j++) {
            PrimitiveSeries tmpSet = mySet[j];
            for (int i = 0; i < nbRows; i++) {
                builder.set(i, j, tmpSet.doubleValue(i));
            }
        }
        return builder.build();
    }

    public SeriesSet log() {

        PrimitiveSeries[] retSet = new PrimitiveSeries[mySet.length];

        for (int i = 0; i < retSet.length; i++) {
            retSet[i] = mySet[i].log();
        }

        return new SeriesSet(retSet);
    }

    public SeriesSet quotients() {

        PrimitiveSeries[] retSet = new PrimitiveSeries[mySet.length];

        for (int i = 0; i < retSet.length; i++) {
            retSet[i] = mySet[i].quotients();
        }

        return new SeriesSet(retSet);
    }

}
