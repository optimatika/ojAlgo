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
package org.ojalgo.matrix;

import java.util.function.Supplier;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Transformation2D;

abstract class Mutator2D<N extends Comparable<N>, M extends BasicMatrix<N, M>, MR extends MatrixStore<N> & Mutate2D.ModifiableReceiver<N>>
        implements Mutate2D.ModifiableReceiver<N>, Supplier<M>, Access2D.Collectable<N, TransformableRegion<N>> {

    private final MR myDelegate;
    private boolean mySafe = true;

    Mutator2D(final MR delegate) {

        super();

        myDelegate = delegate;
    }

    @Override
    public void accept(final Access2D<?> supplied) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.accept(supplied);
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long index, final double addend) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.add(index, addend);
    }

    @Override
    public void add(final long row, final long col, final Comparable<?> value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.add(row, col, value);
    }

    @Override
    public void add(final long row, final long col, final double value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.add(row, col, value);
    }

    @Override
    public long count() {
        return myDelegate.count();
    }

    @Override
    public long countColumns() {
        return myDelegate.countColumns();
    }

    @Override
    public long countRows() {
        return myDelegate.countRows();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        if (mySafe) {
            return myDelegate.doubleValue(row, col);
        }
        throw new IllegalStateException();
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.exchangeColumns(colA, colB);
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.exchangeRows(rowA, rowB);
    }

    @Override
    public void fillAll(final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillAll(myDelegate.physical().scalar().cast(value));
    }

    @Override
    public void fillAll(final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillAll(supplier);
    }

    @Override
    public void fillColumn(final long col, final Access1D<N> values) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillColumn(col, values);
    }

    @Override
    public void fillColumn(final long row, final long col, final Access1D<N> values) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillColumn(row, col, values);
    }

    @Override
    public void fillColumn(final long row, final long column, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillColumn((int) row, (int) column, myDelegate.physical().scalar().cast(value));
    }

    @Override
    public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillColumn(row, col, supplier);
    }

    @Override
    public void fillColumn(final long col, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillColumn(col, value);
    }

    @Override
    public void fillColumn(final long col, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillColumn(col, supplier);
    }

    @Override
    public void fillDiagonal(final Access1D<N> values) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillDiagonal(values);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final Access1D<N> values) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillDiagonal(row, col, values);
    }

    @Override
    public void fillDiagonal(final long row, final long column, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillDiagonal(row, column, myDelegate.physical().scalar().cast(value));
    }

    @Override
    public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillDiagonal(row, col, supplier);
    }

    @Override
    public void fillDiagonal(final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillDiagonal(value);
    }

    @Override
    public void fillDiagonal(final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillDiagonal(supplier);
    }

    @Override
    public void fillMatching(final Access1D<?> values) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillMatching(values);
    }

    @Override
    public void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillMatching(left, function, right);
    }

    @Override
    public void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillMatching(function, arguments);
    }

    @Override
    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillOne(index, values, valueIndex);
    }

    @Override
    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillOne(row, col, values, valueIndex);
    }

    @Override
    public void fillOne(final long row, final long col, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillOne(row, col, value);
    }

    @Override
    public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillOne(row, col, supplier);
    }

    @Override
    public void fillOne(final long index, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillOne(index, value);
    }

    @Override
    public void fillOne(final long index, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillOne(index, supplier);
    }

    @Override
    public void fillRange(final long first, final long limit, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRange(first, limit, value);
    }

    @Override
    public void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRange(first, limit, supplier);
    }

    @Override
    public void fillRow(final long row, final Access1D<N> values) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRow(row, values);
    }

    @Override
    public void fillRow(final long row, final long col, final Access1D<N> values) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRow(row, col, values);
    }

    @Override
    public void fillRow(final long row, final long column, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRow((int) row, (int) column, myDelegate.physical().scalar().cast(value));
    }

    @Override
    public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRow(row, col, supplier);
    }

    @Override
    public void fillRow(final long row, final N value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRow(row, value);
    }

    @Override
    public void fillRow(final long row, final NullaryFunction<?> supplier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.fillRow(row, supplier);
    }

    @Override
    public M get() {
        mySafe = false;
        return this.instantiate(myDelegate);
    }

    @Override
    public N get(final long row, final long col) {
        if (mySafe) {
            return myDelegate.get(row, col);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyAll(modifier);
    }

    @Override
    public void modifyAny(final Transformation2D<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        modifier.transform(myDelegate);
    }

    @Override
    public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyColumn(row, col, modifier);
    }

    @Override
    public void modifyColumn(final long col, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyColumn(col, modifier);
    }

    @Override
    public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyDiagonal(row, col, modifier);
    }

    @Override
    public void modifyDiagonal(final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyDiagonal(modifier);
    }

    @Override
    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyMatching(left, function);
    }

    @Override
    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyMatching(function, right);
    }

    @Override
    public void modifyMatchingInColumns(final Access1D<N> left, final BinaryFunction<N> function) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyMatchingInColumns(left, function);
    }

    @Override
    public void modifyMatchingInColumns(final BinaryFunction<N> function, final Access1D<N> right) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyMatchingInColumns(function, right);
    }

    @Override
    public void modifyMatchingInRows(final Access1D<N> left, final BinaryFunction<N> function) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyMatchingInRows(left, function);
    }

    @Override
    public void modifyMatchingInRows(final BinaryFunction<N> function, final Access1D<N> right) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyMatchingInRows(function, right);
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyOne(row, col, modifier);
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyOne(index, modifier);
    }

    @Override
    public void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyRange(first, limit, modifier);
    }

    @Override
    public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyRow(row, col, modifier);
    }

    @Override
    public void modifyRow(final long row, final UnaryFunction<N> modifier) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.modifyRow(row, modifier);
    }

    @Override
    public void reset() {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.reset();
    }

    @Override
    public void set(final long index, final Comparable<?> value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.set(index, myDelegate.physical().scalar().cast(value));
    }

    @Override
    public void set(final long index, final double value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.set(index, value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.set(row, col, value);
    }

    @Override
    public void set(final int row, final int col, final double value) {
        if (!mySafe) {
            throw new IllegalStateException();
        }
        myDelegate.set(row, col, value);
    }

    @Override
    public void supplyTo(final TransformableRegion<N> receiver) {
        myDelegate.supplyTo(receiver);
    }

    abstract M instantiate(MatrixStore<N> store);

}
