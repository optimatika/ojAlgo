/*
 * Copyright 1997-2023 Optimatika
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

import java.util.Arrays;
import java.util.Iterator;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.type.context.NumberContext;

/**
 * N-dimensional accessor methods
 *
 * @see Access1D
 * @author apete
 */
public interface AccessAnyD<N extends Comparable<N>> extends StructureAnyD, Access1D<N> {

    public interface Aggregatable<N extends Comparable<N>> extends StructureAnyD, Access1D.Aggregatable<N> {

        N aggregateSet(int dimension, long dimensionalIndex, Aggregator aggregator);

        N aggregateSet(long[] initial, int dimension, Aggregator aggregator);

        default void reduce(final int dimension, final Aggregator aggregator, final Mutate1D receiver) {
            long count1 = this.count(dimension);
            long count2 = receiver.count();
            for (long i = 0L, limit = Math.min(count1, count2); i < limit; i++) {
                receiver.set(i, this.aggregateSet(dimension, i, aggregator));
            }
        }

    }

    public interface Collectable<N extends Comparable<N>, R extends MutateAnyD> extends StructureAnyD {

        default <I extends R> I collect(final FactoryAnyD<I> factory) {

            I retVal = factory.make(this.shape());

            this.supplyTo(retVal);

            return retVal;
        }

        void supplyTo(R receiver);

    }

    public static final class ElementView<N extends Comparable<N>> implements ElementViewAnyD<N, ElementView<N>> {

        private final ElementView1D<N, ?> myDelegate1D;
        private final long[] myStructure;

        public ElementView(final ElementView1D<N, ?> delegate, final long[] structure) {

            super();

            myDelegate1D = delegate;
            myStructure = structure;
        }

        public double doubleValue() {
            return myDelegate1D.doubleValue();
        }

        public long estimateSize() {
            return myDelegate1D.estimateSize();
        }

        public N get() {
            return myDelegate1D.get();
        }

        public boolean hasNext() {
            return myDelegate1D.hasNext();
        }

        public boolean hasPrevious() {
            return myDelegate1D.hasPrevious();
        }

        public long index() {
            return myDelegate1D.index();
        }

        public ElementView<N> iterator() {
            return new ElementView<>(myDelegate1D.iterator(), myStructure);
        }

        public ElementView<N> next() {
            myDelegate1D.next();
            return this;
        }

        public long nextIndex() {
            return myDelegate1D.nextIndex();
        }

        public ElementView<N> previous() {
            myDelegate1D.previous();
            return this;
        }

        public long previousIndex() {
            return myDelegate1D.previousIndex();
        }

        public long[] reference() {
            return StructureAnyD.reference(myDelegate1D.index(), myStructure);
        }

        @Override
        public String toString() {
            return myDelegate1D.toString();
        }

        public ElementView<N> trySplit() {

            ElementView1D<N, ?> delegateSpliterator = myDelegate1D.trySplit();

            if (delegateSpliterator != null) {
                return new ElementView<>(delegateSpliterator, myStructure);
            }
            return null;
        }

    }

    public static final class MatrixView<N extends Comparable<N>>
            implements Access2D<N>, Iterable<MatrixView<N>>, Iterator<MatrixView<N>>, Comparable<MatrixView<N>>, Access2D.Collectable<N, Mutate2D> {

        private final long myColumnsCount;
        private final long myCount;
        private final AccessAnyD<N> myDelegateAnyD;
        private final long myLastOffset;
        private long myOffset;
        private final long myRowsCount;

        protected MatrixView(final AccessAnyD<N> access) {
            this(access, -1L);
        }

        MatrixView(final AccessAnyD<N> access, final long index) {

            super();

            myDelegateAnyD = access;

            myRowsCount = access.count(0);
            myColumnsCount = access.count(1);
            myCount = myRowsCount * myColumnsCount;

            myOffset = index * myCount;
            myLastOffset = myDelegateAnyD.count() - myCount;
        }

        public int compareTo(final MatrixView<N> other) {
            return Long.compare(myOffset, other.getOffset());
        }

        public long count() {
            return myCount;
        }

        public long countColumns() {
            return myColumnsCount;
        }

        public long countRows() {
            return myRowsCount;
        }

        public double doubleValue(final long row, final long col) {
            return myDelegateAnyD.doubleValue(myOffset + Structure2D.index(myRowsCount, row, col));
        }

        public long estimateSize() {
            return (myLastOffset - myOffset) / myCount;
        }

        public N get(final long row, final long col) {
            return myDelegateAnyD.get(myOffset + Structure2D.index(myRowsCount, row, col));
        }

        /**
         * Move the view to a specific matrix. The index specified here should correspond to what is returned
         * by the {@link #index()} method.
         *
         * @see #index()
         */
        public void goToMatrix(final long index) {
            myOffset = index * myCount;
        }

        public boolean hasNext() {
            return myOffset < myLastOffset;
        }

        public boolean hasPrevious() {
            return myOffset > 0L;
        }

        /**
         * If the underlying {@link AccessAnyD} data structure was created as 4x5x7x8, it can be viewed as 7x8
         * 4x5 matrices. The dimensions of the MatrixView would be 4x5, and the index returned by this method
         * indicates which of 7x8 matrices the view currently points to. The range of these indices would be
         * 0-55.
         *
         * @return The index of the matrix (which matrix are we currently viewing).
         */
        public long index() {
            return myOffset / myCount;
        }

        public MatrixView<N> iterator() {
            return new MatrixView<>(myDelegateAnyD);
        }

        public MatrixView<N> next() {
            myOffset += myCount;
            return this;
        }

        public MatrixView<N> previous() {
            myOffset -= myCount;
            return this;
        }

        public void remove() {
            ProgrammingError.throwForUnsupportedOptionalOperation();
        }

        public void supplyTo(final Mutate2D receiver) {
            for (long j = 0L, nbColumns = this.countColumns(); j < nbColumns; j++) {
                for (long i = 0L, nbRows = this.countRows(); i < nbRows; i++) {
                    receiver.set(i, j, this.get(i, j));
                }
            }
        }

        @Override
        public String toString() {
            return Access2D.toString(this);
        }

        long getOffset() {
            return myOffset;
        }

    }

    public static final class SelectionView<N extends Comparable<N>> implements AccessAnyD<N>, Collectable<N, MutateAnyD> {

        private final AccessAnyD<N> myFullData;
        private final long[][] mySelections;
        private final long[] myShape;

        SelectionView(final AccessAnyD<N> fullData, final long[][] selections) {

            super();

            myFullData = fullData;

            myShape = new long[fullData.rank()];
            mySelections = new long[fullData.rank()][];
            for (int d = 0, limit = Math.min(mySelections.length, selections.length); d < limit; d++) {
                mySelections[d] = Structure1D.replaceNullOrEmptyWithFull(selections[d], fullData.size(d));
                myShape[d] = mySelections[d].length;
            }
            for (int d = selections.length, limit = mySelections.length; d < limit; d++) {
                mySelections[d] = Structure1D.replaceNullOrEmptyWithFull(null, fullData.size(d));
                myShape[d] = mySelections[d].length;
            }
        }

        public long count(final int dimension) {
            return myShape[dimension];
        }

        public double doubleValue(final long... ref) {
            return myFullData.doubleValue(this.translate(ref));
        }

        public N get(final long... ref) {
            return myFullData.get(this.translate(ref));
        }

        public long[] shape() {
            return myShape;
        }

        public void supplyTo(final MutateAnyD receiver) {

            long[] filteredRef = new long[myShape.length];
            long[] fullRef = new long[myShape.length];

            for (long i = 0L, limit = this.count(); i < limit; i++) {

                StructureAnyD.reference(i, myShape, filteredRef);
                this.translate(filteredRef, fullRef);

                receiver.set(i, myFullData.get(fullRef));
            }
        }

        @Override
        public String toString() {
            return AccessAnyD.toString(this);
        }

        private long[] translate(final long[] filteredRef) {

            long[] fullRef = new long[myShape.length];

            this.translate(filteredRef, fullRef);

            return fullRef;
        }

        private void translate(final long[] filteredRef, final long[] fullRef) {
            for (int d = 0, limit = Math.min(fullRef.length, filteredRef.length); d < limit; d++) {
                fullRef[d] = mySelections[d][Math.toIntExact(filteredRef[d])];
            }
        }

    }

    public interface Sliceable<N extends Comparable<N>> extends StructureAnyD, Access1D.Sliceable<N> {

        /**
         * If the intial reference is {0, 2, 3} and the slice dimension is 1 then the sliced 1D view will map
         * to the following elements in the AnyD data structure:
         *
         * <pre>
         * 0 => {0, 2, 3}
         * 1 => {0, 3, 3}
         * 2 => {0, 4, 3}
         * 3 => {0, 5, 3}
         * 4 => {0, 6, 3}
         * 5 => {0, 7, 3}
         * 6 => ...
         * </pre>
         *
         * Meaning the row index is always '0', and the plane/matrix/area index is always '3', but the column
         * index starts at '2' and then increments.
         * <p>
         * If you have a data structure defined as 3 x 3 x n (that is n 3x3 matrices) and you want to access
         * the second element of the first column of each of the matrices, then the intial refrence is {1, 0,
         * 0} and the slice dimension is 2.
         *
         * @param initial Indices pointing to what will be the first element of the sliced {@link Access1D}
         * @param dimension Which indices that make out an element reference should be incremented
         * @return A sliced 1D view of the underlying AnyD data structure
         */
        Access1D<N> sliceSet(long[] initial, int dimension);

    }

    public static final class VectorView<N extends Comparable<N>>
            implements Access1D<N>, Iterable<VectorView<N>>, Iterator<VectorView<N>>, Comparable<VectorView<N>>, Access1D.Collectable<N, Mutate1D> {

        private final long myCount;
        private final AccessAnyD<N> myDelegateAnyD;
        private final long myLastOffset;
        private long myOffset;

        protected VectorView(final AccessAnyD<N> access) {
            this(access, -1L);
        }

        VectorView(final AccessAnyD<N> access, final long index) {

            super();

            myDelegateAnyD = access;

            myCount = access.count(0);

            myOffset = index * myCount;
            myLastOffset = myDelegateAnyD.count() - myCount;
        }

        public int compareTo(final VectorView<N> other) {
            return Long.compare(myOffset, other.getOffset());
        }

        public long count() {
            return myCount;
        }

        public double doubleValue(final long index) {
            return myDelegateAnyD.doubleValue(myOffset + Structure2D.index(myCount, index, 0));
        }

        public long estimateSize() {
            return (myLastOffset - myOffset) / myCount;
        }

        public N get(final long index) {
            return myDelegateAnyD.get(myOffset + Structure2D.index(myCount, index, 0));
        }

        /**
         * Move the view to a specific vector. The index specified here should correspond to what is returned
         * by the {@link #index()} method.
         *
         * @see #index()
         */
        public void goToVector(final long index) {
            myOffset = index * myCount;
        }

        public boolean hasNext() {
            return myOffset < myLastOffset;
        }

        public boolean hasPrevious() {
            return myOffset > 0L;
        }

        /**
         * If the underlying {@link AccessAnyD} data structure was created as 4x5x7, it can be viewed as 5x7
         * 4-dimensional vectors. The size of the VectorView would be 4, and the index returned by this method
         * indicates which of 5x7 vectors the view currently points to. The range of these indices would be
         * 0-34.
         *
         * @return The index of the vector (which vector are we currently viewing).
         */
        public long index() {
            return myOffset / myCount;
        }

        public VectorView<N> iterator() {
            return new VectorView<>(myDelegateAnyD);
        }

        public VectorView<N> next() {
            myOffset += myCount;
            return this;
        }

        public VectorView<N> previous() {
            myOffset -= myCount;
            return this;
        }

        public void remove() {
            ProgrammingError.throwForUnsupportedOptionalOperation();
        }

        public void supplyTo(final Mutate1D receiver) {
            for (long i = 0L, limit = Math.min(this.count(), receiver.count()); i < limit; i++) {
                receiver.set(i, this.get(i));
            }
        }

        @Override
        public String toString() {
            return this.index() + " = " + Access1D.toString(this);
        }

        long getOffset() {
            return myOffset;
        }

    }

    public interface Visitable<N extends Comparable<N>> extends StructureAnyD, Access1D.Visitable<N> {

        void visitOne(long[] reference, VoidFunction<N> visitor);

        void visitSet(int dimension, long dimensionalIndex, VoidFunction<N> visitor);

        void visitSet(long[] initial, int dimension, VoidFunction<N> visitor);

    }

    static AccessAnyD<Double> asPrimitiveAnyD(final AccessAnyD<?> access) {
        return new AccessAnyD<>() {

            public long count() {
                return access.count();
            }

            public long count(final int dimension) {
                return access.count(dimension);
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long... ref) {
                return access.doubleValue(ref);
            }

            public Double get(final long index) {
                return access.doubleValue(index);
            }

            public Double get(final long... ref) {
                return access.doubleValue(ref);
            }

            public long[] shape() {
                return access.shape();
            }

        };
    }

    static boolean equals(final AccessAnyD<?> accessA, final AccessAnyD<?> accessB, final NumberContext accuracy) {

        boolean retVal = true;
        int d = 0;
        long tmpCount;

        do {
            tmpCount = accessA.count(d);
            retVal &= tmpCount == accessB.count(d);
            d++;
        } while (retVal && (d <= 3 || tmpCount > 1));

        return retVal && Access1D.equals(accessA, accessB, accuracy);
    }

    static String toString(final AccessAnyD<?> array) {
        return Arrays.toString(array.shape()) + " " + Access1D.toString(array);
    }

    default <NN extends Comparable<NN>, R extends MutateAnyD.Receiver<NN>> Collectable<NN, R> asCollectableAnyD() {
        return new Collectable<>() {

            public long count(final int dimension) {
                return AccessAnyD.this.count(dimension);
            }

            public long[] shape() {
                return AccessAnyD.this.shape();
            }

            public void supplyTo(final R receiver) {
                receiver.accept(AccessAnyD.this);
            }

        };
    }

    default byte byteValue(final long index) {
        return this.byteValue(StructureAnyD.reference(index, this.shape()));
    }

    default byte byteValue(final long... ref) {
        return (byte) this.shortValue(ref);
    }

    /**
     * Will pass through each matching element position calling the {@code through} function. What happens is
     * entirely dictated by how you implement the callback.
     */
    default double doubleValue(final long index) {
        return this.doubleValue(StructureAnyD.reference(index, this.shape()));
    }

    double doubleValue(long... ref);

    default ElementViewAnyD<N, ?> elements() {
        return new AccessAnyD.ElementView<>(Access1D.super.elements(), this.shape());
    }

    default float floatValue(final long index) {
        return this.floatValue(StructureAnyD.reference(index, this.shape()));
    }

    default float floatValue(final long... ref) {
        return (float) this.doubleValue(ref);
    }

    default N get(final long index) {
        return this.get(StructureAnyD.reference(index, this.shape()));
    }

    N get(long... ref);

    default int intValue(final long index) {
        return this.intValue(StructureAnyD.reference(index, this.shape()));
    }

    default int intValue(final long... ref) {
        return (int) this.longValue(ref);
    }

    default long longValue(final long index) {
        return this.longValue(StructureAnyD.reference(index, this.shape()));
    }

    default long longValue(final long... ref) {
        return Math.round(this.doubleValue(ref));
    }

    default MatrixView<N> matrices() {
        return new MatrixView<>(this);
    }

    /**
     * Creates a view of the underlying data structure of only the selected elements. There should be one
     * long[] of indices per dimension of the {@link AccessAnyD}, but any such array that is null, empty or
     * missing will be replaced by a "full selection" in that dimension. For instance if you have
     * 3-dimensional array and want to select only the second and third columns of any/all matrices:
     * <code>select(null, {1,2})</code> You have to input null for the row indices (otherwise there is no way
     * of knowing that {1,2} refers to column indices) but may leave out specification of matrix indices.
     */
    default AccessAnyD<N> select(final long[]... selections) {
        return new AccessAnyD.SelectionView<>(this, selections);
    }

    default short shortValue(final long index) {
        return this.shortValue(StructureAnyD.reference(index, this.shape()));
    }

    default short shortValue(final long... ref) {
        return (short) this.intValue(ref);
    }

    default VectorView<N> vectors() {
        return new VectorView<>(this);
    }
}
