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
package org.ojalgo.array;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;
import org.ojalgo.tensor.TensorFactory2D;
import org.ojalgo.type.math.MathType;

/**
 * Array2D
 *
 * @author apete
 */
public final class Array2D<N extends Comparable<N>>
        implements Access2D.Visitable<N>, Access2D.Aggregatable<N>, Access2D.Sliceable<N>, Structure2D.ReducibleTo1D<Array1D<N>>,
        Access2D.Collectable<N, Mutate2D>, Mutate2D.ModifiableReceiver<N>, Mutate2D.Mixable<N>, Structure2D.Reshapable, Factory2D.Builder<Array2D<N>> {

    public static final class Factory<N extends Comparable<N>> implements Factory2D.MayBeSparse<Array2D<N>, Array2D<N>, Array2D<N>> {

        private final BasicArray.Factory<N> myDelegate;

        Factory(final DenseArray.Factory<N> denseArray) {
            super();
            myDelegate = new BasicArray.Factory<>(denseArray);
        }

        @Override
        public Array2D<N> copy(final Access2D<?> source) {
            BasicArray<N> basic = myDelegate.make(source.count());
            basic.fillMatching(source);
            return basic.wrapInArray2D(source.countRows());
        }

        @Override
        public FunctionSet<N> function() {
            return myDelegate.function();
        }

        @Override
        public MathType getMathType() {
            return myDelegate.getMathType();
        }

        @Override
        public Array2D<N> make(final int nbRows, final int nbCols) {
            return this.newDenseBuilder(nbRows, nbCols);
        }

        @Override
        public Array2D<N> make(final long nbRows, final long nbCols) {
            return this.newDenseBuilder(nbRows, nbCols);
        }

        @Override
        public Array2D<N> makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {
            BasicArray<N> basic = myDelegate.makeToBeFilled(rows, columns);
            basic.fillAll(supplier);
            return basic.wrapInArray2D(rows);
        }

        @Override
        public Array2D<N> newDenseBuilder(final long nbRows, final long nbCols) {
            return myDelegate.makeToBeFilled(nbRows, nbCols).wrapInArray2D(nbRows);
        }

        @Override
        public Array2D<N> newSparseBuilder(final long nbRows, final long nbCols) {
            return myDelegate.makeStructuredZero(nbRows, nbCols).wrapInArray2D(nbRows);
        }

        @Override
        public Scalar.Factory<N> scalar() {
            return myDelegate.scalar();
        }

        public TensorFactory2D<N, Array2D<N>> tensor() {
            return TensorFactory2D.of(this);
        }

    }

    public static final Factory<Double> R032 = Array2D.factory(ArrayR032.FACTORY);
    public static final Factory<Double> R064 = Array2D.factory(ArrayR064.FACTORY);
    public static final Factory<Quadruple> R128 = Array2D.factory(ArrayR128.FACTORY);
    public static final Factory<BigDecimal> R256 = Array2D.factory(ArrayR256.FACTORY);
    public static final Factory<ComplexNumber> C128 = Array2D.factory(ArrayC128.FACTORY);
    public static final Factory<Quaternion> H256 = Array2D.factory(ArrayH256.FACTORY);
    public static final Factory<RationalNumber> Q128 = Array2D.factory(ArrayQ128.FACTORY);
    public static final Factory<Double> Z008 = Array2D.factory(ArrayZ008.FACTORY);
    public static final Factory<Double> Z016 = Array2D.factory(ArrayZ016.FACTORY);
    public static final Factory<Double> Z032 = Array2D.factory(ArrayZ032.FACTORY);
    public static final Factory<Double> Z064 = Array2D.factory(ArrayZ064.FACTORY);

    public static <N extends Comparable<N>> Array2D.Factory<N> factory(final DenseArray.Factory<N> denseArray) {
        return new Array2D.Factory<>(denseArray);
    }

    private final long myColumnsCount;
    private final BasicArray<N> myDelegate;
    private final long myRowsCount;

    Array2D(final BasicArray<N> delegate, final long structure) {

        super();

        myDelegate = delegate;

        myRowsCount = structure;
        myColumnsCount = structure == 0L ? 0L : delegate.count() / structure;
    }

    @Override
    public void add(final long index, final byte addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final double addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final float addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final int addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final long addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long row, final long col, final byte addend) {
        myDelegate.add(Structure2D.index(myRowsCount, row, col), addend);
    }

    @Override
    public void add(final long row, final long col, final Comparable<?> addend) {
        myDelegate.add(Structure2D.index(myRowsCount, row, col), addend);
    }

    @Override
    public void add(final long row, final long col, final double addend) {
        myDelegate.add(Structure2D.index(myRowsCount, row, col), addend);
    }

    @Override
    public void add(final long row, final long col, final float addend) {
        myDelegate.add(Structure2D.index(myRowsCount, row, col), addend);
    }

    @Override
    public void add(final long row, final long col, final int addend) {
        myDelegate.add(Structure2D.index(myRowsCount, row, col), addend);
    }

    @Override
    public void add(final long row, final long col, final long addend) {
        myDelegate.add(Structure2D.index(myRowsCount, row, col), addend);
    }

    @Override
    public void add(final long row, final long col, final short addend) {
        myDelegate.add(Structure2D.index(myRowsCount, row, col), addend);
    }

    @Override
    public void add(final long index, final short addend) {
        myDelegate.add(index, addend);
    }

    @Override
    public N aggregateColumn(final long row, final long col, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitColumn(row, col, visitor);
        return visitor.get();
    }

    @Override
    public N aggregateDiagonal(final long row, final long col, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitDiagonal(row, col, visitor);
        return visitor.get();
    }

    @Override
    public N aggregateRange(final long first, final long limit, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitRange(first, limit, visitor);
        return visitor.get();
    }

    @Override
    public N aggregateRow(final long row, final long col, final Aggregator aggregator) {
        AggregatorFunction<N> visitor = aggregator.getFunction(myDelegate.factory().aggregator());
        this.visitRow(row, col, visitor);
        return visitor.get();
    }

    @Override
    public Array2D<N> build() {
        return this;
    }

    @Override
    public byte byteValue(final int index) {
        return myDelegate.byteValue(index);
    }

    @Override
    public byte byteValue(final int row, final int col) {
        return myDelegate.byteValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public byte byteValue(final long index) {
        return myDelegate.byteValue(index);
    }

    @Override
    public byte byteValue(final long row, final long col) {
        return myDelegate.byteValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public long count() {
        return myDelegate.count();
    }

    @Override
    public long countColumns() {
        return myColumnsCount;
    }

    @Override
    public long countRows() {
        return myRowsCount;
    }

    @Override
    public double doubleValue(final int index) {
        return myDelegate.doubleValue(index);
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myDelegate.doubleValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public double doubleValue(final long index) {
        return myDelegate.doubleValue(index);
    }

    @Override
    public double doubleValue(final long row, final long col) {
        return myDelegate.doubleValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Array2D)) {
            return false;
        }
        Array2D<?> other = (Array2D<?>) obj;
        if (myRowsCount != other.myRowsCount || myColumnsCount != other.myColumnsCount) {
            return false;
        }
        if (myDelegate == null) {
            if (other.myDelegate != null) {
                return false;
            }
        } else if (!myDelegate.equals(other.myDelegate)) {
            return false;
        }
        return true;
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {
        myDelegate.exchange(colA * myRowsCount, colB * myRowsCount, 1L, myRowsCount);
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        myDelegate.exchange(rowA, rowB, myRowsCount, myColumnsCount);
    }

    @Override
    public void fillAll(final N value) {
        myDelegate.fill(0L, this.count(), 1L, value);
    }

    @Override
    public void fillAll(final NullaryFunction<?> supplier) {
        myDelegate.fill(0L, this.count(), 1L, supplier);
    }

    @Override
    public void fillColumn(final long row, final long col, final Access1D<N> values) {

        long offset = Structure2D.index(myRowsCount, row, col);
        long limit = Math.min(this.countRows() - row, values.count());

        if (myDelegate.isPrimitive()) {
            for (long i = 0L; i < limit; i++) {
                this.set(offset + i, values.doubleValue(i));
            }
        } else {
            for (long i = 0L; i < limit; i++) {
                this.set(offset + i, values.get(i));
            }
        }
    }

    @Override
    public void fillColumn(final long row, final long col, final N value) {
        myDelegate.fill(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, myRowsCount, col), 1L, value);
    }

    @Override
    public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
        myDelegate.fill(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, myRowsCount, col), 1L, supplier);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final N value) {
        long tmpCount = Math.min(myRowsCount - row, myColumnsCount - col);
        myDelegate.fill(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row + tmpCount, col + tmpCount), 1L + myRowsCount, value);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
        long tmpCount = Math.min(myRowsCount - row, myColumnsCount - col);
        myDelegate.fill(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row + tmpCount, col + tmpCount), 1L + myRowsCount, supplier);
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {
        myDelegate.fill(first, limit, 1L, value);
    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
        myDelegate.fill(first, limit, 1L, supplier);
    }

    @Override
    public void fillRow(final long row, final long col, final Access1D<N> values) {

        long offset = Structure2D.index(myRowsCount, row, col);
        long limit = Math.min(this.countColumns() - col, values.count());

        if (myDelegate.isPrimitive()) {
            for (long i = 0L; i < limit; i++) {
                this.set(offset + i * myRowsCount, values.doubleValue(i));
            }
        } else {
            for (long i = 0L; i < limit; i++) {
                this.set(offset + i * myRowsCount, values.get(i));
            }
        }
    }

    @Override
    public void fillRow(final long row, final long col, final N value) {
        myDelegate.fill(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row, myColumnsCount), myRowsCount, value);
    }

    @Override
    public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
        myDelegate.fill(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row, myColumnsCount), myRowsCount, supplier);
    }

    /**
     * Flattens this two dimensional array to a one dimensional array. The (internal/actual) array is not
     * copied, it is just accessed through a different adaptor.
     *
     * @see org.ojalgo.structure.Structure2D.Reshapable#flatten()
     */
    @Override
    public Array1D<N> flatten() {
        return myDelegate.wrapInArray1D();
    }

    @Override
    public float floatValue(final int index) {
        return myDelegate.floatValue(index);
    }

    @Override
    public float floatValue(final int row, final int col) {
        return myDelegate.floatValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public float floatValue(final long index) {
        return myDelegate.floatValue(index);
    }

    @Override
    public float floatValue(final long row, final long col) {
        return myDelegate.floatValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public N get(final long index) {
        return myDelegate.get(index);
    }

    @Override
    public N get(final long row, final long col) {
        return myDelegate.get(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public int getColDim() {
        return Math.toIntExact(myColumnsCount);
    }

    @Override
    public int getRowDim() {
        return Math.toIntExact(myRowsCount);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (int) (myColumnsCount ^ myColumnsCount >>> 32);
        result = prime * result + (myDelegate == null ? 0 : myDelegate.hashCode());
        return prime * result + (int) (myRowsCount ^ myRowsCount >>> 32);
    }

    @Override
    public long indexOfLargest() {
        return myDelegate.indexOfLargest();
    }

    @Override
    public int intValue(final int index) {
        return myDelegate.intValue(index);
    }

    @Override
    public int intValue(final int row, final int col) {
        return myDelegate.intValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public int intValue(final long index) {
        return myDelegate.intValue(index);
    }

    @Override
    public int intValue(final long row, final long col) {
        return myDelegate.intValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public long longValue(final int index) {
        return myDelegate.longValue(index);
    }

    @Override
    public long longValue(final int row, final int col) {
        return myDelegate.longValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public long longValue(final long index) {
        return myDelegate.longValue(index);
    }

    @Override
    public long longValue(final long row, final long col) {
        return myDelegate.longValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public double mix(final long row, final long col, final BinaryFunction<N> mixer, final double addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            double oldValue = this.doubleValue(row, col);
            double newValue = mixer.invoke(oldValue, addend);
            this.set(row, col, newValue);
            return newValue;
        }
    }

    @Override
    public N mix(final long row, final long col, final BinaryFunction<N> mixer, final N addend) {
        ProgrammingError.throwIfNull(mixer);
        synchronized (myDelegate) {
            N oldValue = this.get(row, col);
            N newValue = mixer.invoke(oldValue, addend);
            this.set(row, col, newValue);
            return newValue;
        }
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {
        myDelegate.modify(0L, this.count(), 1L, modifier);
    }

    @Override
    public void modifyAny(final Transformation2D<N> modifier) {
        modifier.transform(this);
    }

    @Override
    public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
        myDelegate.modify(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, myRowsCount, col), 1L, modifier);
    }

    @Override
    public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
        long tmpCount = Math.min(myRowsCount - row, myColumnsCount - col);
        myDelegate.modify(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row + tmpCount, col + tmpCount), 1L + myRowsCount, modifier);
    }

    @Override
    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        myDelegate.modify(0L, this.count(), 1L, left, function);
    }

    @Override
    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        myDelegate.modify(0L, this.count(), 1L, function, right);
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(Structure2D.index(myRowsCount, row, col), modifier);
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        myDelegate.modifyOne(index, modifier);
    }

    @Override
    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        myDelegate.modify(first, limit, 1L, modifier);
    }

    @Override
    public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
        myDelegate.modify(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row, myColumnsCount), myRowsCount, modifier);
    }

    @Override
    public Array1D<N> reduceColumns(final Aggregator aggregator) {
        Array1D<N> retVal = myDelegate.factory().make(myColumnsCount).wrapInArray1D();
        this.reduceColumns(aggregator, retVal);
        return retVal;
    }

    @Override
    public Array1D<N> reduceRows(final Aggregator aggregator) {
        Array1D<N> retVal = myDelegate.factory().make(myRowsCount).wrapInArray1D();
        this.reduceRows(aggregator, retVal);
        return retVal;
    }

    @Override
    public void reset() {
        myDelegate.reset();
    }

    @Override
    public Array2D<N> reshape(final long rows, final long columns) {
        if (Structure2D.count(rows, columns) != this.count()) {
            throw new IllegalArgumentException();
        }
        return myDelegate.wrapInArray2D(rows);
    }

    @Override
    public void set(final int index, final byte value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final double value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final float value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final int value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int row, final int col, final byte value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final int row, final int col, final double value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final int row, final int col, final float value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final int row, final int col, final int value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final int row, final int col, final long value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final int row, final int col, final short value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final int index, final long value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final int index, final short value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final byte value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final double value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final float value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final int value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long index, final long value) {
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long row, final long col, final byte value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final long row, final long col, final double value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final long row, final long col, final float value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final long row, final long col, final int value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final long row, final long col, final long value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final long row, final long col, final short value) {
        myDelegate.set(Structure2D.index(myRowsCount, row, col), value);
    }

    @Override
    public void set(final long index, final short value) {
        myDelegate.set(index, value);
    }

    @Override
    public short shortValue(final int index) {
        return myDelegate.shortValue(index);
    }

    @Override
    public short shortValue(final int row, final int col) {
        return myDelegate.shortValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public short shortValue(final long index) {
        return myDelegate.shortValue(index);
    }

    @Override
    public short shortValue(final long row, final long col) {
        return myDelegate.shortValue(Structure2D.index(myRowsCount, row, col));
    }

    @Override
    public Array1D<N> sliceColumn(final long col) {
        return this.sliceColumn(0L, col);
    }

    @Override
    public Array1D<N> sliceColumn(final long row, final long col) {
        return new Array1D<>(myDelegate, Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, myRowsCount, col), 1L);
    }

    @Override
    public Array1D<N> sliceDiagonal(final long row, final long col) {
        long tmpCount = Math.min(myRowsCount - row, myColumnsCount - col);
        return new Array1D<>(myDelegate, Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row + tmpCount, col + tmpCount),
                1L + myRowsCount);
    }

    @Override
    public Array1D<N> sliceRange(final long first, final long limit) {
        return myDelegate.wrapInArray1D().sliceRange(first, limit);
    }

    @Override
    public Array1D<N> sliceRow(final long row) {
        return this.sliceRow(row, 0L);
    }

    @Override
    public Array1D<N> sliceRow(final long row, final long col) {
        return new Array1D<>(myDelegate, Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row, myColumnsCount), myRowsCount);
    }

    @Override
    public void supplyTo(final Mutate2D receiver) {
        myDelegate.supplyTo(receiver);
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    @Override
    public void visitAll(final VoidFunction<N> visitor) {
        myDelegate.visit(0L, this.count(), 1L, visitor);
    }

    @Override
    public void visitColumn(final long row, final long col, final VoidFunction<N> visitor) {
        myDelegate.visit(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, myRowsCount, col), 1L, visitor);
    }

    @Override
    public void visitDiagonal(final long row, final long col, final VoidFunction<N> visitor) {
        long tmpCount = Math.min(myRowsCount - row, myColumnsCount - col);
        myDelegate.visit(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row + tmpCount, col + tmpCount), 1L + myRowsCount, visitor);
    }

    @Override
    public void visitOne(final long row, final long col, final VoidFunction<N> visitor) {
        myDelegate.visitOne(Structure2D.index(myRowsCount, row, col), visitor);
    }

    @Override
    public void visitOne(final long index, final VoidFunction<N> visitor) {
        myDelegate.visitOne(index, visitor);
    }

    @Override
    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        myDelegate.visit(first, limit, 1L, visitor);
    }

    @Override
    public void visitRow(final long row, final long col, final VoidFunction<N> visitor) {
        myDelegate.visit(Structure2D.index(myRowsCount, row, col), Structure2D.index(myRowsCount, row, myColumnsCount), myRowsCount, visitor);
    }

    BasicArray<N> getDelegate() {
        return myDelegate;
    }

}
