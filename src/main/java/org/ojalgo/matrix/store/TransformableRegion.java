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
package org.ojalgo.matrix.store;

import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Mutate2D.ModifiableReceiver;
import org.ojalgo.structure.Transformation2D;

/**
 * A transformable 2D (sub)region.
 *
 * @author apete
 */
public interface TransformableRegion<N extends Comparable<N>> extends ModifiableReceiver<N> {

    @FunctionalInterface
    interface FillByMultiplying<N extends Comparable<N>> {

        void invoke(TransformableRegion<N> product, Access1D<N> left, int complexity, Access1D<N> right);

        default void invoke(final TransformableRegion<N> product, final Access1D<N> left, final long complexity, final Access1D<N> right) {
            this.invoke(product, left, Math.toIntExact(complexity), right);
        }

    }

    static <N extends Comparable<N>> TransformableRegion<N> cast(final Mutate2D.ModifiableReceiver<N> target) {
        if (target instanceof TransformableRegion) {
            return (TransformableRegion<N>) target;
        } else {
            return new Subregion2D.WrapperRegion<>(target);
        }
    }

    @Override
    default void exchangeColumns(final long colA, final long colB) {
        N valA, valB;
        for (long i = 0L, limit = this.countRows(); i < limit; i++) {
            valA = this.get(i, colA);
            valB = this.get(i, colB);
            this.set(i, colB, valA);
            this.set(i, colA, valB);
        }
    }

    @Override
    default void exchangeRows(final long rowA, final long rowB) {
        N valA, valB;
        for (long j = 0L, limit = this.countColumns(); j < limit; j++) {
            valA = this.get(rowA, j);
            valB = this.get(rowB, j);
            this.set(rowB, j, valA);
            this.set(rowA, j, valB);
        }
    }

    void fillByMultiplying(final Access1D<N> left, final Access1D<N> right);

    @Override
    default void modifyAny(final Transformation2D<N> modifier) {
        modifier.transform(this);
    }

    /**
     * @return A consumer (sub)region
     */
    TransformableRegion<N> regionByColumns(int... columns);

    /**
     * @return A consumer (sub)region
     */
    TransformableRegion<N> regionByLimits(int rowLimit, int columnLimit);

    /**
     * @return A consumer (sub)region
     */
    TransformableRegion<N> regionByOffsets(int rowOffset, int columnOffset);

    /**
     * @return A consumer (sub)region
     */
    TransformableRegion<N> regionByRows(int... rows);

    /**
     * @return A transposed consumer region
     */
    TransformableRegion<N> regionByTransposing();

}
