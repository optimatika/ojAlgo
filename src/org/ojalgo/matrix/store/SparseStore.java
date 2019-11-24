/*
 * Copyright 1997-2019 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.operation.MultiplyBoth;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

public final class SparseStore<N extends Comparable<N>> extends FactoryStore<N> implements TransformableRegion<N> {

    public interface Factory<N extends Comparable<N>> {

        SparseStore<N> make(long rowsCount, long columnsCount);

        default SparseStore<N> make(final Structure2D shape) {
            return this.make(shape.countRows(), shape.countColumns());
        }

    }

    public static final SparseStore.Factory<ComplexNumber> COMPLEX = (rowsCount, columnsCount) -> SparseStore.makeComplex((int) rowsCount, (int) columnsCount);

    public static final SparseStore.Factory<Double> PRIMITIVE64 = (rowsCount, columnsCount) -> SparseStore.makePrimitive((int) rowsCount, (int) columnsCount);

    public static final SparseStore.Factory<Double> PRIMITIVE32 = (rowsCount, columnsCount) -> SparseStore.makePrimitive32((int) rowsCount, (int) columnsCount);

    public static final SparseStore.Factory<Quaternion> QUATERNION = (rowsCount, columnsCount) -> SparseStore.makeQuaternion((int) rowsCount,
            (int) columnsCount);

    public static final SparseStore.Factory<RationalNumber> RATIONAL = (rowsCount, columnsCount) -> SparseStore.makeRational((int) rowsCount,
            (int) columnsCount);

    public static SparseStore<ComplexNumber> makeComplex(final int rowsCount, final int columnsCount) {
        return SparseStore.makeSparse(GenericStore.COMPLEX, rowsCount, columnsCount);
    }

    public static SparseStore<Double> makePrimitive(final int rowsCount, final int columnsCount) {
        return SparseStore.makeSparse(Primitive64Store.FACTORY, rowsCount, columnsCount);
    }

    public static SparseStore<Double> makePrimitive32(final int rowsCount, final int columnsCount) {
        return SparseStore.makeSparse(Primitive32Store.FACTORY, rowsCount, columnsCount);
    }

    public static SparseStore<Quaternion> makeQuaternion(final int rowsCount, final int columnsCount) {
        return SparseStore.makeSparse(GenericStore.QUATERNION, rowsCount, columnsCount);
    }

    public static SparseStore<RationalNumber> makeRational(final int rowsCount, final int columnsCount) {
        return SparseStore.makeSparse(GenericStore.RATIONAL, rowsCount, columnsCount);
    }

    static <N extends Comparable<N>> SparseStore<N> makeSparse(final PhysicalStore.Factory<N, ?> physical, final long numberOfRows,
            final long numberOfColumns) {
        return new SparseStore<>(physical, Math.toIntExact(numberOfRows), Math.toIntExact(numberOfColumns));
    }

    static <N extends Comparable<N>> SparseStore<N> makeSparse(final PhysicalStore.Factory<N, ?> physical, final Structure2D shape) {
        return SparseStore.makeSparse(physical, shape.countRows(), shape.countColumns());
    }

    static <N extends Comparable<N>> void multiply(final SparseStore<N> left, final SparseStore<N> right, final TransformableRegion<N> target) {

        if (left.isPrimitive()) {

            target.reset();

            right.nonzeros().stream().forEach(element -> {
                left.doColumnAXPY(element.row(), element.column(), element.doubleValue(), target);
            });

        } else {

            left.multiply(right, target);
        }
    }

    private final SparseArray<N> myElements;
    private final int[] myFirsts;
    private final int[] myLimits;
    private final TransformableRegion.FillByMultiplying<N> myMultiplyer;

    SparseStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {

        super(factory, rowsCount, columnsCount);

        myElements = SparseArray.factory(factory.array(), this.count()).initial(Math.max(rowsCount, columnsCount)).make();
        myFirsts = new int[rowsCount];
        myLimits = new int[rowsCount];
        Arrays.fill(myFirsts, columnsCount);
        // Arrays.fill(myLimits, 0); // Behövs inte, redan 0

        final Class<? extends Comparable> tmpType = factory.scalar().zero().get().getClass();
        if (tmpType.equals(Double.class)) {
            myMultiplyer = (TransformableRegion.FillByMultiplying<N>) MultiplyBoth.newPrimitive64(rowsCount, columnsCount);
        } else if (tmpType.equals(ComplexNumber.class)) {
            myMultiplyer = (TransformableRegion.FillByMultiplying<N>) MultiplyBoth.newGeneric(rowsCount, columnsCount);
        } else {
            myMultiplyer = null;
        }
    }

    public void add(final long row, final long col, final Comparable<?> addend) {
        myElements.add(Structure2D.index(myFirsts.length, row, col), addend);
        this.updateNonZeros(row, col);
    }

    public void add(final long row, final long col, final double addend) {
        myElements.add(Structure2D.index(myFirsts.length, row, col), addend);
        this.updateNonZeros(row, col);
    }

    public double doubleValue(final long row, final long col) {
        return myElements.doubleValue(Structure2D.index(myFirsts.length, row, col));
    }

    public void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {

        final int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        myMultiplyer.invoke(this, left, (int) (left.count() / this.countRows()), right);
    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.get(valueIndex));
    }

    public void fillOne(final long row, final long col, final N value) {
        myElements.fillOne(Structure2D.index(myFirsts.length, row, col), value);
        this.updateNonZeros(row, col);
    }

    public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        myElements.fillOne(Structure2D.index(myFirsts.length, row, col), supplier);
        this.updateNonZeros(row, col);
    }

    public int firstInColumn(final int col) {

        final long structure = myFirsts.length;

        final long rangeFirst = structure * col;
        final long rangeLimit = structure * (col + 1);

        final long firstInRange = myElements.firstInRange(rangeFirst, rangeLimit);

        if (rangeFirst == firstInRange) {
            return 0;
        } else {
            return (int) (firstInRange % structure);
        }
    }

    public int firstInRow(final int row) {
        return myFirsts[row];
    }

    public N get(final long row, final long col) {
        return myElements.get(Structure2D.index(myFirsts.length, row, col));
    }

    @Override
    public int limitOfColumn(final int col) {

        final long structure = myFirsts.length;

        final long rangeFirst = structure * col;
        final long rangeLimit = rangeFirst + structure;

        final long limitOfRange = myElements.limitOfRange(rangeFirst, rangeLimit);

        if (rangeLimit == limitOfRange) {
            return (int) structure;
        } else {
            return (int) (limitOfRange % structure);
        }
    }

    @Override
    public int limitOfRow(final int row) {
        return myLimits[row];
    }

    public void modifyAll(final UnaryFunction<N> modifier) {
        final long tmpLimit = this.count();
        if (this.isPrimitive()) {
            for (long i = 0L; i < tmpLimit; i++) {
                this.set(i, modifier.invoke(this.doubleValue(i)));
            }
        } else {
            for (long i = 0L; i < tmpLimit; i++) {
                this.set(i, modifier.invoke(this.get(i)));
            }
        }
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {

        final long limit = Math.min(left.count(), this.count());
        boolean notModifiesZero = function.invoke(E, ZERO) == ZERO;

        if (this.isPrimitive()) {
            if (notModifiesZero) {
                for (NonzeroView<N> element : myElements.nonzeros()) {
                    element.modify(left.doubleValue(element.index()), function);
                }
            } else {
                for (long i = 0L; i < limit; i++) {
                    this.set(i, function.invoke(left.doubleValue(i), this.doubleValue(i)));
                }
            }
        } else {
            if (notModifiesZero) {
                for (NonzeroView<N> element : myElements.nonzeros()) {
                    element.modify(left.get(element.index()), function);
                }
            } else {
                for (long i = 0L; i < limit; i++) {
                    this.set(i, function.invoke(left.get(i), this.get(i)));
                }
            }
        }
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {

        final long limit = Math.min(this.count(), right.count());
        boolean notModifiesZero = function.invoke(ZERO, E) == ZERO;

        if (this.isPrimitive()) {
            if (notModifiesZero) {
                for (NonzeroView<N> element : myElements.nonzeros()) {
                    element.modify(function, right.doubleValue(element.index()));
                }
            } else {
                for (long i = 0L; i < limit; i++) {
                    this.set(i, function.invoke(this.doubleValue(i), right.doubleValue(i)));
                }
            }
        } else {
            if (notModifiesZero) {
                for (NonzeroView<N> element : myElements.nonzeros()) {
                    element.modify(function, right.get(element.index()));
                }
            } else {
                for (long i = 0L; i < limit; i++) {
                    this.set(i, function.invoke(this.get(i), right.get(i)));
                }
            }
        }
    }

    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        if (this.isPrimitive()) {
            this.set(row, col, modifier.invoke(this.doubleValue(row, col)));
        } else {
            this.set(row, col, modifier.invoke(this.get(row, col)));
        }
    }

    public void multiply(final Access1D<N> right, final TransformableRegion<N> target) {

        if (right instanceof SparseStore<?>) {

            SparseStore.multiply(this, (SparseStore<N>) right, target);

        } else if (this.isPrimitive()) {

            final long complexity = this.countColumns();
            final long numberOfColumns = target.countColumns();

            target.reset();

            this.nonzeros().stream().forEach(element -> {

                final long row = element.row();
                final long col = element.column();
                final double value = element.doubleValue();

                final long first = MatrixStore.firstInRow(right, col, 0L);
                final long limit = MatrixStore.limitOfRow(right, col, numberOfColumns);
                for (long j = first; j < limit; j++) {
                    final long index = Structure2D.index(complexity, col, j);
                    final double addition = value * right.doubleValue(index);
                    if (NumberContext.compare(addition, ZERO) != 0) {
                        target.add(row, j, addition);
                    }
                }
            });

        } else {

            super.multiply(right, target);
        }
    }

    public MatrixStore<N> multiply(final double scalar) {

        final SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), this);

        if (this.isPrimitive()) {

            for (final ElementView2D<N, ?> nonzero : this.nonzeros()) {
                retVal.set(nonzero.index(), nonzero.doubleValue() * scalar);
            }

        } else {

            final Scalar<N> sclr = this.physical().scalar().convert(scalar);

            for (final ElementView2D<N, ?> nonzero : this.nonzeros()) {
                retVal.set(nonzero.index(), sclr.multiply(nonzero.get()).get());
            }
        }

        return retVal;
    }

    public MatrixStore<N> multiply(final MatrixStore<N> right) {

        long numberOfRows = this.countRows();
        long numberOfColumns = right.countColumns();

        if (right instanceof SparseStore) {

            final SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), numberOfRows, numberOfColumns);

            SparseStore.multiply(this, (SparseStore<N>) right, retVal);

            return retVal;

        } else {

            final PhysicalStore<N> retVal = this.physical().makeZero(numberOfRows, numberOfColumns);

            this.multiply(right, retVal);

            return retVal;
        }
    }

    public MatrixStore<N> multiply(final N scalar) {

        final SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), this);

        if (this.isPrimitive()) {

            final double sclr = Scalar.doubleValue(scalar);

            for (final ElementView2D<N, ?> nonzero : this.nonzeros()) {
                retVal.set(nonzero.index(), nonzero.doubleValue() * sclr);
            }

        } else {

            final Scalar<N> sclr = this.physical().scalar().convert(scalar);

            for (final ElementView2D<N, ?> nonzero : this.nonzeros()) {
                retVal.set(nonzero.index(), sclr.multiply(nonzero.get()).get());
            }
        }

        return retVal;
    }

    @Override
    public N multiplyBoth(final Access1D<N> leftAndRight) {
        // TODO Auto-generated method stub
        return super.multiplyBoth(leftAndRight);
    }

    public ElementView2D<N, ?> nonzeros() {
        return new Access2D.ElementView<>(myElements.nonzeros(), this.countRows());
    }

    public ElementsSupplier<N> premultiply(final Access1D<N> left) {

        long complexity = this.countRows();
        long numberOfColumns = this.countColumns();
        long numberOfRows = left.count() / complexity;

        if (left instanceof SparseStore<?>) {

            final SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), numberOfRows, numberOfColumns);

            SparseStore.multiply((SparseStore<N>) left, this, retVal);

            return retVal;

        } else if (this.isPrimitive()) {

            final SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), numberOfRows, numberOfColumns);

            this.nonzeros().stream().forEach(element -> {

                final long row = element.row();
                final long col = element.column();
                final double value = element.doubleValue();

                final long first = MatrixStore.firstInColumn(left, row, 0L);
                final long limit = MatrixStore.limitOfColumn(left, row, numberOfRows);
                for (long i = first; i < limit; i++) {
                    final long index = Structure2D.index(numberOfRows, i, row);
                    final double addition = value * left.doubleValue(index);
                    if (NumberContext.compare(addition, ZERO) != 0) {
                        retVal.add(i, col, addition);
                    }
                }
            });

            return retVal;

        } else {

            return super.premultiply(left);
        }
    }

    public void reduceColumns(final Aggregator aggregator, final Mutate1D receiver) {
        if (aggregator == Aggregator.SUM) {
            if (this.isPrimitive()) {
                this.nonzeros().forEach(element -> receiver.add(element.column(), element.doubleValue()));
            } else {
                this.nonzeros().forEach(element -> receiver.add(element.column(), element.get()));
            }
        } else {
            super.reduceColumns(aggregator, receiver);
        }
    }

    public void reduceRows(final Aggregator aggregator, final Mutate1D receiver) {
        if (aggregator == Aggregator.SUM) {
            if (this.isPrimitive()) {
                this.nonzeros().forEach(element -> receiver.add(element.row(), element.doubleValue()));
            } else {
                this.nonzeros().forEach(element -> receiver.add(element.row(), element.get()));
            }
        } else {
            super.reduceColumns(aggregator, receiver);
        }
    }

    public TransformableRegion<N> regionByColumns(final int... columns) {
        return new TransformableRegion.ColumnsRegion<>(this, myMultiplyer, columns);
    }

    public TransformableRegion<N> regionByLimits(final int rowLimit, final int columnLimit) {
        return new TransformableRegion.LimitRegion<>(this, myMultiplyer, rowLimit, columnLimit);
    }

    public TransformableRegion<N> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new TransformableRegion.OffsetRegion<>(this, myMultiplyer, rowOffset, columnOffset);
    }

    public TransformableRegion<N> regionByRows(final int... rows) {
        return new TransformableRegion.RowsRegion<>(this, myMultiplyer, rows);
    }

    public TransformableRegion<N> regionByTransposing() {
        return new TransformableRegion.TransposedRegion<>(this, myMultiplyer);
    }

    public void reset() {
        myElements.reset();
        Arrays.fill(myFirsts, this.getColDim());
        Arrays.fill(myLimits, 0);
    }

    public void set(final long row, final long col, final Comparable<?> value) {
        myElements.set(Structure2D.index(myFirsts.length, row, col), value);
        this.updateNonZeros(row, col);
    }

    public void set(final long row, final long col, final double value) {
        myElements.set(Structure2D.index(myFirsts.length, row, col), value);
        this.updateNonZeros(row, col);
    }

    public void supplyTo(final TransformableRegion<N> receiver) {

        receiver.reset();

        myElements.supplyNonZerosTo(receiver);
    }

    public void visitColumn(final long row, final long col, final VoidFunction<N> visitor) {

        long structure = this.countRows();
        long first = Structure2D.index(structure, row, col);
        long limit = Structure2D.index(structure, 0, col + 1L);

        myElements.visitRange(first, limit, visitor);
    }

    public void visitRow(final long row, final long col, final VoidFunction<N> visitor) {
        int counter = 0;
        if (this.isPrimitive()) {
            for (ElementView2D<N, ?> nzv : this.nonzeros()) {
                if (nzv.row() == row) {
                    visitor.accept(nzv.doubleValue());
                    counter++;
                }
            }
        } else {
            for (ElementView2D<N, ?> nzv : this.nonzeros()) {
                if (nzv.row() == row) {
                    visitor.accept(nzv.get());
                    counter++;
                }
            }
        }
        if ((col + counter) < this.countColumns()) {
            visitor.accept(0.0);
        }
    }

    private void updateNonZeros(final long row, final long col) {
        this.updateNonZeros((int) row, (int) col);
    }

    void doColumnAXPY(final long colX, final long colY, final double a, final TransformableRegion<N> y) {

        long structure = y.countRows();

        final long first = structure * colX;
        final long limit = first + structure;

        myElements.visitPrimitiveNonzerosInRange(first, limit, (index, value) -> y.add(Structure2D.row(index, structure), colY, a * value));
    }

    void updateNonZeros(final int row, final int col) {
        myFirsts[row] = Math.min(col, myFirsts[row]);
        myLimits[row] = Math.max(col + 1, myLimits[row]);
    }

}
