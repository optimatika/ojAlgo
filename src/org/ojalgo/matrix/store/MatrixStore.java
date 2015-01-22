/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Consumer2D;
import org.ojalgo.access.Supplier2D;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * A {@linkplain MatrixStore} is a two dimensional store of numbers/scalars.
 * </p>
 * <p>
 * A {@linkplain MatrixStore} extends {@linkplain Access2D} (as well as {@linkplain Access2D.Visitable} and
 * {@linkplain Access2D.Elements}) and defines some futher funtionality - mainly matrix multiplication.
 * </p>
 * <p>
 * This interface does not define any methods that require implementations to alter the matrix. Either the methods
 * return matrix elements, some meta data or produce new instances.
 * </p>
 * <p>
 * The methods {@linkplain #conjugate()}, {@linkplain #copy()} and {@linkplain #transpose()} return
 * {@linkplain PhysicalStore} instances. {@linkplain PhysicalStore} extends {@linkplain MatrixStore}. It defines
 * additional methods, and is mutable.
 * </p>
 *
 * @author apete
 */
public interface MatrixStore<N extends Number> extends Access2D<N>, Access2D.Visitable<N>, Access2D.Elements {

    public static final class Builder<N extends Number> {

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final int aMinRowDim, final MatrixStore<N>... aColStore) {
            MatrixStore<N> retVal = aColStore[0];
            for (int i = 1; i < aColStore.length; i++) {
                retVal = new AboveBelowStore<N>(retVal, aColStore[i]);
            }
            final int tmpRowDim = (int) retVal.countRows();
            if (tmpRowDim < aMinRowDim) {
                retVal = new AboveBelowStore<N>(retVal, new ZeroStore<N>(retVal.factory(), aMinRowDim - tmpRowDim, (int) retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildColumn(final PhysicalStore.Factory<N, ?> aFactory, final int aMinRowDim, final N... aColStore) {
            MatrixStore<N> retVal = aFactory.columns(aColStore);
            final int tmpRowDim = (int) retVal.countRows();
            if (tmpRowDim < aMinRowDim) {
                retVal = new AboveBelowStore<N>(retVal, new ZeroStore<N>(aFactory, aMinRowDim - tmpRowDim, (int) retVal.countColumns()));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final int aMinColDim, final MatrixStore<N>... aRowStore) {
            MatrixStore<N> retVal = aRowStore[0];
            for (int j = 1; j < aRowStore.length; j++) {
                retVal = new LeftRightStore<N>(retVal, aRowStore[j]);
            }
            final int tmpColDim = (int) retVal.countColumns();
            if (tmpColDim < aMinColDim) {
                retVal = new LeftRightStore<N>(retVal, new ZeroStore<N>(retVal.factory(), (int) retVal.countRows(), aMinColDim - tmpColDim));
            }
            return retVal;
        }

        @SafeVarargs
        static <N extends Number> MatrixStore<N> buildRow(final PhysicalStore.Factory<N, ?> aFactory, final int aMinColDim, final N... aRowStore) {
            MatrixStore<N> retVal = new TransposedStore<N>(aFactory.columns(aRowStore));
            final int tmpColDim = (int) retVal.countColumns();
            if (tmpColDim < aMinColDim) {
                retVal = new LeftRightStore<N>(retVal, new ZeroStore<N>(aFactory, (int) retVal.countRows(), aMinColDim - tmpColDim));
            }
            return retVal;
        }

        private MatrixStore<N> myStore;

        public Builder(final MatrixStore<N> matrixStore) {

            super();

            myStore = matrixStore;
        }

        @SuppressWarnings("unused")
        private Builder() {

            this(null);

            ProgrammingError.throwForIllegalInvocation();
        }

        public final Builder<N> above(final int aRowDim) {
            final ZeroStore<N> tmpUpperStore = new ZeroStore<N>(myStore.factory(), aRowDim, (int) myStore.countColumns());
            myStore = new AboveBelowStore<N>(tmpUpperStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> above(final MatrixStore<N>... upperStore) {
            final MatrixStore<N> tmpUpperStore = Builder.buildRow((int) myStore.countColumns(), upperStore);
            myStore = new AboveBelowStore<N>(tmpUpperStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> above(final N... anUpperStore) {
            final MatrixStore<N> tmpUpperStore = Builder.buildRow(myStore.factory(), (int) myStore.countColumns(), anUpperStore);
            myStore = new AboveBelowStore<N>(tmpUpperStore, myStore);
            return this;
        }

        public final Builder<N> below(final int aRowDim) {
            final ZeroStore<N> tmpLowerStore = new ZeroStore<N>(myStore.factory(), aRowDim, (int) myStore.countColumns());
            myStore = new AboveBelowStore<N>(myStore, tmpLowerStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> below(final MatrixStore<N>... aLowerStore) {
            final MatrixStore<N> tmpLowerStore = Builder.buildRow((int) myStore.countColumns(), aLowerStore);
            myStore = new AboveBelowStore<N>(myStore, tmpLowerStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> below(final N... aLowerStore) {
            final MatrixStore<N> tmpLowerStore = Builder.buildRow(myStore.factory(), (int) myStore.countColumns(), aLowerStore);
            myStore = new AboveBelowStore<N>(myStore, tmpLowerStore);
            return this;
        }

        public final Builder<N> bidiagonal(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<N>(new LowerHessenbergStore<N>(myStore), assumeOne);
            } else {
                myStore = new LowerTriangularStore<N>(new UpperHessenbergStore<N>(myStore), assumeOne);
            }
            return this;
        }

        public final MatrixStore<N> build() {
            return myStore;
        }

        public final Builder<N> column(final int... aCol) {
            myStore = new ColumnsStore<N>(myStore, aCol);
            return this;
        }

        public final Builder<N> columns(final int aFirst, final int aLimit) {
            myStore = new ColumnsStore<N>(aFirst, aLimit, myStore);
            return this;
        }

        public final Builder<N> conjugate() {
            if (myStore instanceof ConjugatedStore) {
                myStore = ((ConjugatedStore<N>) myStore).getOriginal();
            } else {
                myStore = new ConjugatedStore<N>(myStore);
            }
            return this;
        }

        public final PhysicalStore<N> copy() {
            return myStore.copy();
        }

        public final Builder<N> diagonal(final boolean assumeOne) {
            myStore = new UpperTriangularStore<N>(new LowerTriangularStore<N>(myStore, assumeOne), assumeOne);
            return this;
        }

        @SafeVarargs
        public final Builder<N> diagonally(final MatrixStore<N>... aDiagonalStore) {

            final PhysicalStore.Factory<N, ?> tmpFactory = myStore.factory();

            MatrixStore<N> tmpDiagonalStore;
            for (int ij = 0; ij < aDiagonalStore.length; ij++) {

                tmpDiagonalStore = aDiagonalStore[ij];

                final int tmpBaseRowDim = (int) myStore.countRows();
                final int tmpBaseColDim = (int) myStore.countColumns();

                final int tmpDiagRowDim = (int) tmpDiagonalStore.countRows();
                final int tmpDiagColDim = (int) tmpDiagonalStore.countColumns();

                final MatrixStore<N> tmpRightStore = new ZeroStore<N>(tmpFactory, tmpBaseRowDim, tmpDiagColDim);
                final MatrixStore<N> tmpAboveStore = new LeftRightStore<N>(myStore, tmpRightStore);

                final MatrixStore<N> tmpLeftStore = new ZeroStore<N>(tmpFactory, tmpDiagRowDim, tmpBaseColDim);
                final MatrixStore<N> tmpBelowStore = new LeftRightStore<N>(tmpLeftStore, tmpDiagonalStore);

                myStore = new AboveBelowStore<N>(tmpAboveStore, tmpBelowStore);
            }

            return this;
        }

        public final Builder<N> hessenberg(final boolean upper) {
            if (upper) {
                myStore = new UpperHessenbergStore<N>(myStore);
            } else {
                myStore = new LowerHessenbergStore<N>(myStore);
            }
            return this;
        }

        public final Builder<N> left(final int aColDim) {
            final MatrixStore<N> tmpLeftStore = new ZeroStore<N>(myStore.factory(), (int) myStore.countRows(), aColDim);
            myStore = new LeftRightStore<N>(tmpLeftStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> left(final MatrixStore<N>... aLeftStore) {
            final MatrixStore<N> tmpLeftStore = Builder.buildColumn((int) myStore.countRows(), aLeftStore);
            myStore = new LeftRightStore<N>(tmpLeftStore, myStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> left(final N... aLeftStore) {
            final MatrixStore<N> tmpLeftStore = Builder.buildColumn(myStore.factory(), (int) myStore.countRows(), aLeftStore);
            myStore = new LeftRightStore<N>(tmpLeftStore, myStore);
            return this;
        }

        public final Builder<N> modify(final UnaryFunction<N> aFunc) {
            myStore = new ModificationStore<N>(myStore, aFunc);
            return this;
        }

        public final Builder<N> multiplyLeft(final Access1D<N> leftMtrx) {
            myStore = myStore.multiplyLeft(leftMtrx);
            return this;
        }

        public final Builder<N> multiplyRight(final Access1D<N> rightMtrx) {
            myStore = myStore.multiply(rightMtrx);
            return this;
        }

        public final Builder<N> negate() {
            myStore = myStore.negate();
            return this;
        }

        public final Builder<N> right(final int aColDim) {
            final MatrixStore<N> tmpRightStore = new ZeroStore<N>(myStore.factory(), (int) myStore.countRows(), aColDim);
            myStore = new LeftRightStore<N>(myStore, tmpRightStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> right(final MatrixStore<N>... aRightStore) {
            final MatrixStore<N> tmpRightStore = Builder.buildColumn((int) myStore.countRows(), aRightStore);
            myStore = new LeftRightStore<N>(myStore, tmpRightStore);
            return this;
        }

        @SafeVarargs
        public final Builder<N> right(final N... aRightStore) {
            final MatrixStore<N> tmpRightStore = Builder.buildColumn(myStore.factory(), (int) myStore.countRows(), aRightStore);
            myStore = new LeftRightStore<N>(myStore, tmpRightStore);
            return this;
        }

        public final Builder<N> row(final int... aRow) {
            myStore = new RowsStore<N>(myStore, aRow);
            return this;
        }

        public final Builder<N> rows(final int aFirst, final int aLimit) {
            myStore = new RowsStore<N>(aFirst, aLimit, myStore);
            return this;
        }

        public final Builder<N> scale(final N scalar) {
            myStore = myStore.scale(scalar);
            return this;
        }

        public final Builder<N> superimpose(final int aRow, final int aCol, final MatrixStore<N> aStore) {
            myStore = new SuperimposedStore<N>(myStore, aRow, aCol, aStore);
            return this;
        }

        public final Builder<N> superimpose(final int aRow, final int aCol, final N aStore) {
            myStore = new SuperimposedStore<N>(myStore, aRow, aCol, new SingleStore<N>(myStore.factory(), aStore));
            return this;
        }

        public final Builder<N> superimpose(final MatrixStore<N> aStore) {
            myStore = new SuperimposedStore<N>(myStore, 0, 0, aStore);
            return this;
        }

        @Override
        public String toString() {
            return myStore.toString();
        }

        public final Builder<N> transpose() {
            if (myStore instanceof TransposedStore) {
                myStore = ((TransposedStore<N>) myStore).getOriginal();
            } else {
                myStore = new TransposedStore<N>(myStore);
            }
            return this;
        }

        public final Builder<N> triangular(final boolean upper, final boolean assumeOne) {
            if (upper) {
                myStore = new UpperTriangularStore<N>(myStore, assumeOne);
            } else {
                myStore = new LowerTriangularStore<N>(myStore, assumeOne);
            }
            return this;
        }

        public final Builder<N> tridiagonal() {
            myStore = new UpperHessenbergStore<N>(new LowerHessenbergStore<N>(myStore));
            return this;
        }

    }

    public static interface ElementsConsumer<N extends Number> extends Consumer2D<Access2D<N>>, Access2D.Fillable<N>, Access2D.Modifiable<N> {

        default void accept(final MatrixStore.ElementsSupplier<N> supplier) {
            supplier.supplyTo(this);
        }

        default boolean isAcceptable(final MatrixStore.ElementsSupplier<N> supplier) {
            return (this.countRows() >= supplier.countRows()) && (this.countColumns() >= supplier.countColumns());
        }

        /**
         * @return A consumer (sub)region
         */
        ElementsConsumer<N> region(int row, int column);

    }

    public static interface ElementsSupplier<N extends Number> extends Supplier2D<MatrixStore<N>> {

        void supplyTo(ElementsConsumer<N> target);

    }

    MatrixStore<N> add(MatrixStore<N> addend);

    N aggregateAll(Aggregator aggregator);

    MatrixStore.Builder<N> builder();

    /**
     * Each call must produce a new instance.
     *
     * @return A new conjugated instance.
     */
    MatrixStore<N> conjugate();

    /**
     * Each call must produce a new instance.
     *
     * @return A new {@linkplain PhysicalStore} copy.
     */
    PhysicalStore<N> copy();

    boolean equals(MatrixStore<N> other, NumberContext context);

    PhysicalStore.Factory<N, ?> factory();

    /**
     * The entries below (left of) the first subdiagonal are zero - effectively an upper Hessenberg matrix.
     *
     * @see #isUpperRightShaded()
     */
    boolean isLowerLeftShaded();

    /**
     * The entries above (right of) the first superdiagonal are zero - effectively a lower Hessenberg matrix.
     *
     * @see #isLowerLeftShaded()
     */
    boolean isUpperRightShaded();

    MatrixStore<N> multiply(Access1D<N> right);

    MatrixStore<N> multiplyLeft(Access1D<N> leftMtrx);

    /**
     * @deprecated v38
     */
    @Deprecated
    default MatrixStore<N> multiplyRight(final Access1D<N> right) {
        return this.multiply(right);
    }

    MatrixStore<N> negate();

    MatrixStore<N> scale(N scalar);

    MatrixStore<N> subtract(MatrixStore<N> subtrahend);

    Scalar<N> toScalar(long row, long column);

    /**
     * Each call must produce a new instance.
     *
     * @return A new transposed instance.
     */
    MatrixStore<N> transpose();

}
