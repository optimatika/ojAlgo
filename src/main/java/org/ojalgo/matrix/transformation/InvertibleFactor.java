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
package org.ojalgo.matrix.transformation;

import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

/**
 * A chainable and reversible in-place (equation system) solver.
 * <p>
 * Matrix decompositions produce factorisations like [A] = [L][U], [A] = [Q][R] or [A] =
 * [U][D][V]<sup>T</sup>. When solving equation systems the factors are used in sequence:
 * <p>
 * [A][x] = [b] and [A] = [L][U] gives [L][U][x] = [b], which is solved in these steps:
 * <ol>
 * <li>[L][y] = [b] or [y] = [L]<sup>-1</sup>[b]
 * <li>[U][x] = [y] or [x] = [U]<sup>-1</sup>[y]
 * </ol>
 * That's forward transformation (ftran) using the [L] and [U] (invertible) factors of [A].
 * <p>
 * If we instead want to solve [x]<sup>T</sup>[A] = [b]<sup>T</sup> or [A]<sup>T</sup>[x] = [b] the steps are:
 * <ol>
 * <li>[U]<sup>T</sup>[y] = [b] or [y] = [U]<sup>-T</sup>[b]
 * <li>[L]<sup>T</sup>[x] = [y] or [x] = [L]<sup>-T</sup>[y]
 * </ol>
 * That's backwards transformation (btran).
 * <p>
 * Implementing this interface can be useful whenever a matrix (or its inverse) can be constructed using a
 * sequence of factors.
 * <p>
 * An invertible factor needs to be square.
 *
 * @author apete
 */
public interface InvertibleFactor<N extends Comparable<N>> extends Structure2D {

    static final class IdentityFactor<N extends Comparable<N>> implements InvertibleFactor<N> {

        private final int myDim;

        IdentityFactor(final int dim) {
            super();
            myDim = dim;
        }

        public void btran(final PhysicalStore<N> arg) {
            // No-op
        }

        public long countColumns() {
            return myDim;
        }

        public long countRows() {
            return myDim;
        }

        public void ftran(final PhysicalStore<N> arg) {
            // No-op
        }

    }

    static <N extends Comparable<N>> InvertibleFactor<N> identity(final int dim) {
        return new IdentityFactor<>(dim);
    }

    default void btran(final Collectable<N, ? super PhysicalStore<N>> lhs, final PhysicalStore<N> solution) {
        lhs.supplyTo(solution);
        this.btran(solution);
    }

    /**
     * Backwards-transformation
     * <p>
     * Solve [x]<sup>T</sup>[A] = [b]<sup>T</sup> (equivalent to [A]<sup>T</sup>[x] = [b]) by transforming [b]
     * into [x] in-place.
     *
     * @param arg [b] transformed into [x]
     */
    void btran(PhysicalStore<N> arg);

    default void ftran(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> solution) {
        rhs.supplyTo(solution);
        this.ftran(solution);
    }

    /**
     * Forward-transformation
     * <p>
     * Solve [A][x] = [b] by transforming [b] into [x] in-place.
     *
     * @param arg [b] transformed into [x]
     */
    void ftran(PhysicalStore<N> arg);

}
