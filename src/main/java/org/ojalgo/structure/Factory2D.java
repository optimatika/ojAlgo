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
package org.ojalgo.structure;

import java.util.List;

import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.Scalar.Factory;
import org.ojalgo.type.math.MathType;

public interface Factory2D<I extends Structure2D> extends FactorySupplement {

    interface Builder<I extends Structure2D> extends Mutate2D {

        I build();

    }

    /**
     * For when the structures can be either dense or sparse.
     *
     * @author apete
     */
    interface MayBeSparse<I extends Structure2D, DENSE extends Builder<I>, SPARSE extends Builder<I>> extends TwoStep<I, DENSE> {

        @Override
        default DENSE newBuilder(final long nbRows, final long nbCols) {
            return this.newDenseBuilder(nbRows, nbCols);
        }

        DENSE newDenseBuilder(long nbRows, long nbCols);

        SPARSE newSparseBuilder(long nbRows, long nbCols);

    }

    public interface TwoStep<I extends Structure2D, B extends Builder<I>> extends Factory2D<I> {

        default I column(final Access1D<?> column) {

            long nbRows = column.count();

            B builder = this.newBuilder(nbRows, 1L);

            if (this.getMathType().isPrimitive()) {
                for (long i = 0L; i < nbRows; i++) {
                    builder.set(i, column.doubleValue(i));
                }
            } else {
                for (long i = 0L; i < nbRows; i++) {
                    builder.set(i, column.get(i));
                }
            }

            return builder.build();
        }

        default I column(final Comparable<?>... column) {

            int nbCols = 1;
            int nbRows = column.length;

            B builder = this.newBuilder(nbRows, nbCols);

            for (int i = 0; i < nbRows; i++) {
                builder.set(i, column[i]);
            }

            return builder.build();
        }

        default I column(final double[] column) {

            int nbCols = 1;
            int nbRows = column.length;

            B builder = this.newBuilder(nbRows, nbCols);

            for (int i = 0; i < nbRows; i++) {
                builder.set(i, column[i]);
            }

            return builder.build();
        }

        default I column(final List<? extends Comparable<?>> column) {

            int nbRows = column.size();

            B builder = this.newBuilder(nbRows, 1L);

            for (int i = 0; i < nbRows; i++) {
                builder.set(i, column.get(i));
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link RawStore#wrap(double[][])} and {@link MatrixStore#transpose()} instead.
         */
        @Deprecated
        default I columns(final double[][] columns) {
            return this.copy(RawStore.wrap(columns).transpose());
        }

        default I copy(final Access2D<?> source) {

            long nbRows = source.countRows();
            long nbCols = source.countColumns();
            B builder = this.newBuilder(nbRows, nbCols);

            if (this.getMathType().isPrimitive()) {
                for (long j = 0L; j < nbCols; j++) {
                    for (long i = 0L; i < nbRows; i++) {
                        builder.set(i, j, source.doubleValue(i, j));
                    }
                }
            } else {
                for (long j = 0L; j < nbCols; j++) {
                    for (long i = 0L; i < nbRows; i++) {
                        builder.set(i, j, source.get(i, j));
                    }
                }
            }

            return builder.build();
        }

        @Override
        default I make(final int nbRows, final int nbCols) {
            B builder = this.newBuilder(nbRows, nbCols);
            return builder.build();
        }

        @Override
        default I make(final long nbRows, final long nbCols) {
            B builder = this.newBuilder(nbRows, nbCols);
            return builder.build();
        }

        @Override
        default I make(final Structure2D shape) {
            B builder = this.newBuilder(shape.countRows(), shape.countColumns());
            return builder.build();
        }

        default I makeFilled(final long nbRows, final long nbCols, final NullaryFunction<?> supplier) {
            B builder = this.newBuilder(nbRows, nbCols);
            for (long j = 0L; j < nbCols; j++) {
                for (long i = 0L; i < nbRows; i++) {
                    builder.set(i, j, supplier.get());
                }
            }
            return builder.build();
        }

        B newBuilder(long nbRows, long nbCols);

        default I row(final Access1D<?> row) {

            long nbCols = row.count();

            B builder = this.newBuilder(1L, nbCols);

            if (this.getMathType().isPrimitive()) {
                for (long j = 0L; j < nbCols; j++) {
                    builder.set(j, row.doubleValue(j));
                }
            } else {
                for (long j = 0L; j < nbCols; j++) {
                    builder.set(j, row.get(j));
                }
            }

            return builder.build();
        }

        default I row(final Comparable<?>... row) {

            int nbRows = 1;
            int nbCols = row.length;

            B builder = this.newBuilder(nbRows, nbCols);

            for (int j = 0; j < nbCols; j++) {
                builder.set(Structure2D.index(nbRows, 0, j), row[j]);
            }

            return builder.build();
        }

        default I row(final double[] row) {

            int nbRows = 1;
            int nbCols = row.length;

            B builder = this.newBuilder(nbRows, nbCols);

            for (int j = 0; j < nbCols; j++) {
                builder.set(Structure2D.index(nbRows, 0, j), row[j]);
            }

            return builder.build();
        }

        default I row(final List<? extends Comparable<?>> row) {

            int nbCols = row.size();

            B builder = this.newBuilder(1L, nbCols);

            for (int j = 0; j < nbCols; j++) {
                builder.set(j, row.get(j));
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link RawStore#wrap(double[][])} instead.
         */
        @Deprecated
        default I rows(final double[][] rows) {
            return this.copy(RawStore.wrap(rows));
        }

    }

    default Factory1D<I> asFactory1D() {
        return new Factory1D<>() {

            public FunctionSet<?> function() {
                return Factory2D.this.function();
            }

            public MathType getMathType() {
                return Factory2D.this.getMathType();
            }

            public I make(final int size) {
                return Factory2D.this.make(size, 1);
            }

            public I make(final long count) {
                return Factory2D.this.make(count, 1L);
            }

            public I make(final Structure1D shape) {
                return Factory2D.this.make(shape.count(), 1L);
            }

            public Factory<?> scalar() {
                return Factory2D.this.scalar();
            }

        };
    }

    I make(int nbRows, int nbCols);

    default I make(final long nbRows, final long nbCols) {
        return this.make(Math.toIntExact(nbRows), Math.toIntExact(nbCols));
    }

    /**
     * Make new instance of compatible dimensions.
     */
    default I make(final Structure1D struct1, final Structure1D struct2) {
        long[] compatible = StructureAnyD.compatible(struct1, struct2);
        if (compatible.length == 2) {
            return this.make(compatible[0], compatible[1]);
        } else if (compatible.length == 1) {
            return this.make(compatible[0], 1L);
        } else {
            throw new IllegalArgumentException();
        }
    }

    default I make(final Structure2D shape) {
        return this.make(shape.countRows(), shape.countColumns());
    }

}
