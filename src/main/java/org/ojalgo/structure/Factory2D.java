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
package org.ojalgo.structure;

import java.util.List;

import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
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

        /**
         * @deprecated v54 Use {@link #newDenseBuilder(long,long)} instead
         */
        @Deprecated
        default DENSE makeDense(final long nbRows, final long nbCols) {
            return this.newDenseBuilder(nbRows, nbCols);
        }

        /**
         * @deprecated v54 Use {@link #newDenseBuilder(Structure2D)} instead
         */
        @Deprecated
        default DENSE makeDense(final Structure2D shape) {
            return this.newDenseBuilder(shape.countRows(), shape.countColumns());
        }

        /**
         * @deprecated v54 Use {@link #newSparseBuilder(long,long)} instead
         */
        @Deprecated
        default SPARSE makeSparse(final long nbRows, final long nbCols) {
            return this.newSparseBuilder(nbRows, nbCols);
        }

        /**
         * @deprecated v54 Use {@link #newSparseBuilder(Structure2D)} instead
         */
        @Deprecated
        default SPARSE makeSparse(final Structure2D shape) {
            return this.newSparseBuilder(shape.countRows(), shape.countColumns());
        }

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

        /**
         * @deprecated v54 Use {@link #row(Access1D)}, {@link #column(Access1D)} or
         *             {@link #newBuilder(long,long)} instead.
         */
        @Deprecated
        default I columns(final Access1D<?>... source) {

            int nbCols = source.length;
            long nbRows = source[0].count();

            B builder = this.newBuilder(nbRows, nbCols);

            if (this.getMathType().isPrimitive()) {
                long index = 0L;
                for (int j = 0; j < nbCols; j++) {
                    Access1D<?> column = source[j];
                    for (long i = 0L; i < nbRows; i++) {
                        builder.set(index++, column.doubleValue(i));
                    }
                }
            } else {
                long index = 0L;
                for (int j = 0; j < nbCols; j++) {
                    Access1D<?> column = source[j];
                    for (long i = 0L; i < nbRows; i++) {
                        builder.set(index++, column.get(i));
                    }
                }
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #row(Comparable<?>[])}, {@link #column(Comparable<?>[])} or
         *             {@link #newBuilder(long,long)} instead.
         */
        @Deprecated
        default I columns(final Comparable<?>[]... source) {

            int nbCols = source.length;
            int nbRows = source[0].length;

            B builder = this.newBuilder(nbRows, nbCols);

            long index = 0L;
            for (int j = 0; j < nbCols; j++) {
                Comparable<?>[] column = source[j];
                for (int i = 0; i < nbRows; i++) {
                    builder.set(index++, column[i]);
                }
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #row(double[])}, {@link #column(double[])} or
         *             {@link #newBuilder(long,long)} instead. In some cases {@link RawStore#wrap(double[][])}
         *             could also be a good alternative (avoids copying).
         */
        @Deprecated
        default I columns(final double[]... source) {

            int nbCols = source.length;
            int nbRows = source[0].length;

            B builder = this.newBuilder(nbRows, nbCols);

            long index = 0L;
            for (int j = 0; j < nbCols; j++) {
                double[] column = source[j];
                for (int i = 0; i < nbRows; i++) {
                    builder.set(index++, column[i]);
                }
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #newBuilder(long,long)} instead.
         */
        @Deprecated
        default I columns(final List<? extends Comparable<?>>... source) {

            int nbCols = source.length;
            int nbRows = source[0].size();

            B builder = this.newBuilder(nbRows, nbCols);

            long index = 0L;
            for (int j = 0; j < nbCols; j++) {
                List<? extends Comparable<?>> column = source[j];
                for (int i = 0; i < nbRows; i++) {
                    builder.set(index++, column.get(i));
                }
            }

            return builder.build();
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

        /**
         * @deprecated v54 Use {@link #makeFilled(long,long,NullaryFunction)} instead
         */
        @Deprecated
        default I makeFilled(final Structure2D shape, final NullaryFunction<?> supplier) {
            return this.makeFilled(shape.countRows(), shape.countColumns(), supplier);
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

        /**
         * @deprecated v54 Use {@link #row(Access1D)}, {@link #column(Access1D)} or
         *             {@link #newBuilder(long,long)} instead.
         */
        @Deprecated
        default I rows(final Access1D<?>... source) {

            int nbRows = source.length;
            long nbCols = source[0].count();

            B builder = this.newBuilder(nbRows, nbCols);

            if (this.getMathType().isPrimitive()) {
                for (int i = 0; i < nbRows; i++) {
                    Access1D<?> row = source[i];
                    for (long j = 0L; j < nbCols; j++) {
                        builder.set(Structure2D.index(nbRows, i, j), row.doubleValue(j));
                    }
                }
            } else {
                for (int i = 0; i < nbRows; i++) {
                    Access1D<?> row = source[i];
                    for (long j = 0L; j < nbCols; j++) {
                        builder.set(Structure2D.index(nbRows, i, j), row.get(j));
                    }
                }
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #row(Comparable<?>[])}, {@link #column(Comparable<?>[])} or
         *             {@link #newBuilder(long,long)} instead.
         */
        @Deprecated
        default I rows(final Comparable<?>[]... source) {

            int nbRows = source.length;
            int nbCols = source[0].length;

            B builder = this.newBuilder(nbRows, nbCols);

            for (int i = 0; i < nbRows; i++) {
                Comparable<?>[] row = source[i];
                for (int j = 0; j < nbCols; j++) {
                    builder.set(Structure2D.index(nbRows, i, j), row[j]);
                }
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #row(double[])}, {@link #column(double[])} or
         *             {@link #newBuilder(long,long)} instead. In some cases {@link RawStore#wrap(double[][])}
         *             could also be a good alternative (avoids copying).
         */
        @Deprecated
        default I rows(final double[]... source) {

            int nbRows = source.length;
            int nbCols = source[0].length;

            B builder = this.newBuilder(nbRows, nbCols);

            for (int i = 0; i < nbRows; i++) {
                double[] row = source[i];
                for (int j = 0; j < nbCols; j++) {
                    builder.set(Structure2D.index(nbRows, i, j), row[j]);
                }
            }

            return builder.build();
        }

        /**
         * @deprecated v54 Use {@link #newBuilder(long,long)} instead.
         */
        @Deprecated
        default I rows(final List<? extends Comparable<?>>... source) {

            int nbRows = source.length;
            int nbCols = source[0].size();

            B builder = this.newBuilder(nbRows, nbCols);

            for (int i = 0; i < nbRows; i++) {
                List<? extends Comparable<?>> row = source[i];
                for (int j = 0; j < nbCols; j++) {
                    builder.set(Structure2D.index(nbRows, i, j), row.get(j));
                }
            }

            return builder.build();
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
