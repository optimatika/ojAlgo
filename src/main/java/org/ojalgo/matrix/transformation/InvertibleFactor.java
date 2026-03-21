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
package org.ojalgo.matrix.transformation;

import java.util.List;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.operation.COPY;
import org.ojalgo.matrix.store.PhysicalStore;
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

        @Override
        public void btran(final double[] arg) {
            // No-op
        }

        @Override
        public void btran(final PhysicalStore<N> arg) {
            // No-op
        }

        @Override
        public void ftran(final double[] arg) {
            // No-op
        }

        @Override
        public void ftran(final PhysicalStore<N> arg) {
            // No-op
        }

        @Override
        public int getColDim() {
            return myDim;
        }

        @Override
        public int getRowDim() {
            return myDim;
        }

    }

    static <N extends Comparable<N>> void btran(final List<InvertibleFactor<N>> factors, final double[] arg) {
        for (int i = factors.size() - 1; i >= 0; i--) {
            factors.get(i).btran(arg);
        }
    }

    static <N extends Comparable<N>> void btran(final List<InvertibleFactor<N>> factors, final PhysicalStore<N> arg) {
        for (int i = factors.size() - 1; i >= 0; i--) {
            factors.get(i).btran(arg);
        }
    }

    /**
     * Do primitive {@link #ftran(double[])}
     */
    static void doPrimitive(final InvertibleFactor<Double> factor, final PhysicalStore<Double> arg) {
        if (arg instanceof ArrayR064) {
            factor.ftran(((ArrayR064) arg).data);
        } else {
            double[] x = arg.toRawCopy1D();
            factor.ftran(x);
            COPY.invoke(x, arg);
        }
    }

    /**
     * Do primitive {@link #btran(double[])}
     */
    static void doPrimitive(final PhysicalStore<Double> arg, final InvertibleFactor<Double> factor) {
        if (arg instanceof ArrayR064) {
            factor.btran(((ArrayR064) arg).data);
        } else {
            double[] x = arg.toRawCopy1D();
            factor.btran(x);
            COPY.invoke(x, arg);
        }
    }

    static <N extends Comparable<N>> void ftran(final List<InvertibleFactor<N>> factors, final double[] arg) {
        for (int i = 0, limit = factors.size(); i < limit; i++) {
            factors.get(i).ftran(arg);
        }
    }

    static <N extends Comparable<N>> void ftran(final List<InvertibleFactor<N>> factors, final PhysicalStore<N> arg) {
        for (int i = 0, limit = factors.size(); i < limit; i++) {
            factors.get(i).ftran(arg);
        }
    }

    static <N extends Comparable<N>> InvertibleFactor<N> identity(final int dim) {
        return new IdentityFactor<>(dim);
    }

    /**
     * @see IdentityFactor#btran(PhysicalStore)
     */
    void btran(double[] arg);

    /**
     * Backwards-transformation
     * <p>
     * Solve [x]<sup>T</sup>[A] = [b]<sup>T</sup> (equivalent to [A]<sup>T</sup>[x] = [b]) by transforming [b]
     * into [x] in-place.
     *
     * @param arg [b] transformed into [x]
     */
    void btran(PhysicalStore<N> arg);

    /**
     * @see IdentityFactor#ftran(PhysicalStore)
     */
    void ftran(double[] arg);

    /**
     * Forward-transformation
     * <p>
     * Solve [A][x] = [b] by transforming [b] into [x] in-place.
     *
     * @param arg [b] transformed into [x]
     */
    void ftran(PhysicalStore<N> arg);

}
