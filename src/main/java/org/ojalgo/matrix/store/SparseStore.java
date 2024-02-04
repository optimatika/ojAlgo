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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.math.MathType;

public final class SparseStore<N extends Comparable<N>> extends FactoryStore<N> implements TransformableRegion<N> {

    public static final class Factory<N extends Comparable<N>> implements Factory2D<SparseStore<N>> {

        private final PhysicalStore.Factory<N, ?> myPhysicalFactory;

        Factory(final org.ojalgo.matrix.store.PhysicalStore.Factory<N, ?> physicalFactory) {
            super();
            myPhysicalFactory = physicalFactory;
        }

        @Override
        public FunctionSet<?> function() {
            return myPhysicalFactory.function();
        }

        @Override
        public SparseStore<N> make(final long rows, final long columns) {
            return SparseStore.makeSparse(myPhysicalFactory, rows, columns);
        }

        @Override
        public Scalar.Factory<?> scalar() {
            return myPhysicalFactory.scalar();
        }

        @Override
        public MathType getMathType() {
            return myPhysicalFactory.getMathType();
        }

    }

    public static final SparseStore.Factory<ComplexNumber> C128 = SparseStore.factory(GenericStore.C128);
    public static final SparseStore.Factory<Quaternion> H256 = SparseStore.factory(GenericStore.H256);
    public static final SparseStore.Factory<Double> R032 = SparseStore.factory(Primitive32Store.FACTORY);
    public static final SparseStore.Factory<Double> R064 = SparseStore.factory(Primitive64Store.FACTORY);
    public static final SparseStore.Factory<Quadruple> R128 = SparseStore.factory(GenericStore.R128);
    public static final SparseStore.Factory<RationalNumber> Q128 = SparseStore.factory(GenericStore.Q128);

    /**
     * @deprecated v53 Use {@link #C128} instead
     */
    @Deprecated
    public static final SparseStore.Factory<ComplexNumber> COMPLEX = C128;
    /**
     * @deprecated v53 Use {@link #R032} instead
     */
    @Deprecated
    public static final SparseStore.Factory<Double> PRIMITIVE32 = R032;
    /**
     * @deprecated v53 Use {@link #R064} instead
     */
    @Deprecated
    public static final SparseStore.Factory<Double> PRIMITIVE64 = R064;
    /**
     * @deprecated v53 Use {@link #H256} instead
     */
    @Deprecated
    public static final SparseStore.Factory<Quaternion> QUATERNION = H256;
    /**
     * @deprecated v53 Use {@link #Q128} instead
     */
    @Deprecated
    public static final SparseStore.Factory<RationalNumber> RATIONAL = Q128;

    public static <N extends Comparable<N>> SparseStore.Factory<N> factory(final PhysicalStore.Factory<N, ?> physicalFactory) {
        return new SparseStore.Factory<>(physicalFactory);
    }

    /**
     * @deprecated v53 Use {@link #C128} instead
     */
    @Deprecated
    public static SparseStore<ComplexNumber> makeComplex(final long rowsCount, final long columnsCount) {
        return SparseStore.makeSparse(GenericStore.C128, rowsCount, columnsCount);
    }

    /**
     * @deprecated v53 Use {@link #R064} instead
     */
    @Deprecated
    public static SparseStore<Double> makePrimitive(final long rowsCount, final long columnsCount) {
        return SparseStore.makeSparse(Primitive64Store.FACTORY, rowsCount, columnsCount);
    }

    /**
     * @deprecated v53 Use {@link #R032} instead
     */
    @Deprecated
    public static SparseStore<Double> makePrimitive32(final long rowsCount, final long columnsCount) {
        return SparseStore.makeSparse(Primitive32Store.FACTORY, rowsCount, columnsCount);
    }

    /**
     * @deprecated v53 Use {@link #H256} instead
     */
    @Deprecated
    public static SparseStore<Quaternion> makeQuaternion(final long rowsCount, final long columnsCount) {
        return SparseStore.makeSparse(GenericStore.H256, rowsCount, columnsCount);
    }

    /**
     * @deprecated v53 Use {@link #Q128} instead
     */
    @Deprecated
    public static SparseStore<RationalNumber> makeRational(final long rowsCount, final long columnsCount) {
        return SparseStore.makeSparse(GenericStore.Q128, rowsCount, columnsCount);
    }

    private static <N extends Scalar<N>> void doGenericColumnAXPY(final SparseArray<N> elements, final long colX, final long colY, final N a,
            final TransformableRegion<N> y) {

        long structure = y.countRows();

        long first = structure * colX;
        long limit = first + structure;

        elements.visitReferenceTypeNonzerosInRange(first, limit, (index, value) -> y.add(Structure2D.row(index, structure), colY, value.multiply(a)));
    }

    private static void doPrimitiveColumnAXPY(final SparseArray<Double> elements, final long colX, final long colY, final double a,
            final TransformableRegion<Double> y) {

        long structure = y.countRows();

        long first = structure * colX;
        long limit = first + structure;

        elements.visitPrimitiveNonzerosInRange(first, limit, (index, value) -> y.add(Structure2D.row(index, structure), colY, a * value));
    }

    static <N extends Comparable<N>> SparseStore<N> makeSparse(final PhysicalStore.Factory<N, ?> physical, final long numberOfRows,
            final long numberOfColumns) {
        return new SparseStore<>(physical, Math.toIntExact(numberOfRows), Math.toIntExact(numberOfColumns));
    }

    static <N extends Comparable<N>> SparseStore<N> makeSparse(final PhysicalStore.Factory<N, ?> physical, final Structure2D shape) {
        return SparseStore.makeSparse(physical, shape.countRows(), shape.countColumns());
    }

    static <N extends Comparable<N>> void multiply(final SparseStore<N> left, final SparseStore<N> right, final TransformableRegion<N> target) {

        target.reset();

        if (left.isPrimitive()) {

            SparseArray<Double> tmpLeft = (SparseArray<Double>) left.getElements();
            TransformableRegion<Double> tmpTarget = (TransformableRegion<Double>) target;

            right.nonzeros().stream().forEach(element -> {
                SparseStore.doPrimitiveColumnAXPY(tmpLeft, element.row(), element.column(), element.doubleValue(), tmpTarget);
            });

        } else if (left.getComponentType().isAssignableFrom(ComplexNumber.class)) {

            SparseArray<ComplexNumber> tmpLeft = (SparseArray<ComplexNumber>) left.getElements();
            SparseStore<ComplexNumber> tmpRight = (SparseStore<ComplexNumber>) right;
            TransformableRegion<ComplexNumber> tmpTarget = (TransformableRegion<ComplexNumber>) target;

            tmpRight.nonzeros().stream().forEach(element -> {
                SparseStore.doGenericColumnAXPY(tmpLeft, element.row(), element.column(), element.get(), tmpTarget);
            });

        } else if (left.getComponentType().isAssignableFrom(RationalNumber.class)) {

            SparseArray<RationalNumber> tmpLeft = (SparseArray<RationalNumber>) left.getElements();
            SparseStore<RationalNumber> tmpRight = (SparseStore<RationalNumber>) right;
            TransformableRegion<RationalNumber> tmpTarget = (TransformableRegion<RationalNumber>) target;

            tmpRight.nonzeros().stream().forEach(element -> {
                SparseStore.doGenericColumnAXPY(tmpLeft, element.row(), element.column(), element.get(), tmpTarget);
            });

        } else if (left.getComponentType().isAssignableFrom(Quaternion.class)) {

            SparseArray<Quaternion> tmpLeft = (SparseArray<Quaternion>) left.getElements();
            SparseStore<Quaternion> tmpRight = (SparseStore<Quaternion>) right;
            TransformableRegion<Quaternion> tmpTarget = (TransformableRegion<Quaternion>) target;

            tmpRight.nonzeros().stream().forEach(element -> {
                SparseStore.doGenericColumnAXPY(tmpLeft, element.row(), element.column(), element.get(), tmpTarget);
            });

        } else {

            throw new IllegalStateException("Unsupported element type!");
        }
    }

    private final SparseArray<N> myElements;
    private final int[] myFirsts;
    private final int[] myLimits;
    private final TransformableRegion.FillByMultiplying<N> myMultiplier;

    SparseStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {

        super(factory, rowsCount, columnsCount);

        myElements = SparseArray.factory(factory.array()).limit(this.count()).initial(Math.max(rowsCount, columnsCount)).make();
        myFirsts = new int[rowsCount];
        myLimits = new int[rowsCount];
        Arrays.fill(myFirsts, columnsCount);
        // Arrays.fill(myLimits, 0); // Behövs inte, redan 0

        Class<? extends Comparable> tmpType = factory.scalar().zero().get().getClass();
        myMultiplier = Subregion2D.findMultiplier(tmpType, rowsCount, columnsCount);
    }

    @Override
    public void add(final long row, final long col, final Comparable<?> addend) {
        synchronized (myElements) {
            myElements.add(Structure2D.index(myFirsts.length, row, col), addend);
        }
        this.updateNonZeros(row, col);
    }

    @Override
    public void add(final long row, final long col, final double addend) {
        synchronized (myElements) {
            myElements.add(Structure2D.index(myFirsts.length, row, col), addend);
        }
        this.updateNonZeros(row, col);
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myElements.doubleValue(Structure2D.index(myFirsts.length, row, col));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof SparseStore)) {
            return false;
        }
        SparseStore<?> other = (SparseStore<?>) obj;
        if (myElements == null) {
            if (other.myElements != null) {
                return false;
            }
        } else if (!myElements.equals(other.myElements)) {
            return false;
        }
        if (!Arrays.equals(myFirsts, other.myFirsts) || !Arrays.equals(myLimits, other.myLimits)) {
            return false;
        }
        return true;
    }

    @Override
    public void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {

        int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        myMultiplier.invoke(this, left, complexity, right);
    }

    @Override
    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.get(valueIndex));
    }

    @Override
    public void fillOne(final long row, final long col, final N value) {
        synchronized (myElements) {
            myElements.fillOne(Structure2D.index(myFirsts.length, row, col), value);
        }
        this.updateNonZeros(row, col);
    }

    @Override
    public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        synchronized (myElements) {
            myElements.fillOne(Structure2D.index(myFirsts.length, row, col), supplier);
        }
        this.updateNonZeros(row, col);
    }

    @Override
    public int firstInColumn(final int col) {

        long structure = myFirsts.length;

        long rangeFirst = structure * col;
        long rangeLimit = structure * (col + 1);

        long firstInRange = myElements.firstInRange(rangeFirst, rangeLimit);

        if (rangeFirst == firstInRange) {
            return 0;
        }
        return (int) (firstInRange % structure);
    }

    @Override
    public int firstInRow(final int row) {
        return myFirsts[row];
    }

    @Override
    public N get(final int row, final int col) {
        return myElements.get(Structure2D.index(myFirsts.length, row, col));
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myElements == null ? 0 : myElements.hashCode());
        result = prime * result + Arrays.hashCode(myFirsts);
        return prime * result + Arrays.hashCode(myLimits);
    }

    @Override
    public long indexOfLargest() {
        return myElements.indexOfLargest();
    }

    @Override
    public int limitOfColumn(final int col) {

        long structure = myFirsts.length;

        long rangeFirst = structure * col;
        long rangeLimit = rangeFirst + structure;

        long limitOfRange = myElements.limitOfRange(rangeFirst, rangeLimit);

        if (rangeLimit == limitOfRange) {
            return (int) structure;
        }
        return (int) (limitOfRange % structure);
    }

    @Override
    public int limitOfRow(final int row) {
        return myLimits[row];
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {
        long tmpLimit = this.count();
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

    @Override
    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {

        long limit = Math.min(left.count(), this.count());
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
        } else if (notModifiesZero) {
            for (NonzeroView<N> element : myElements.nonzeros()) {
                element.modify(left.get(element.index()), function);
            }
        } else {
            for (long i = 0L; i < limit; i++) {
                this.set(i, function.invoke(left.get(i), this.get(i)));
            }
        }
    }

    @Override
    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {

        long limit = Math.min(this.count(), right.count());
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
        } else if (notModifiesZero) {
            for (NonzeroView<N> element : myElements.nonzeros()) {
                element.modify(function, right.get(element.index()));
            }
        } else {
            for (long i = 0L; i < limit; i++) {
                this.set(i, function.invoke(this.get(i), right.get(i)));
            }
        }
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        if (this.isPrimitive()) {
            this.set(row, col, modifier.invoke(this.doubleValue(row, col)));
        } else {
            this.set(row, col, modifier.invoke(this.get(row, col)));
        }
    }

    @Override
    public void multiply(final Access1D<N> right, final TransformableRegion<N> target) {

        if (right instanceof SparseStore<?>) {

            SparseStore.multiply(this, (SparseStore<N>) right, target);

        } else if (this.isPrimitive()) {

            long complexity = this.countColumns();
            long numberOfColumns = target.countColumns();

            target.reset();

            this.nonzeros().stream().forEach(element -> {

                long row = element.row();
                long col = element.column();
                double value = element.doubleValue();

                long first = Structure2D.firstInRow(right, col, 0L);
                long limit = Structure2D.limitOfRow(right, col, numberOfColumns);
                for (long j = first; j < limit; j++) {
                    long index = Structure2D.index(complexity, col, j);
                    double addition = value * right.doubleValue(index);
                    if (NumberContext.compare(addition, ZERO) != 0) {
                        target.add(row, j, addition);
                    }
                }
            });

        } else {

            super.multiply(right, target);
        }
    }

    @Override
    public MatrixStore<N> multiply(final double scalar) {

        SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), this);

        if (this.isPrimitive()) {

            for (ElementView2D<N, ?> nonzero : this.nonzeros()) {
                retVal.set(nonzero.index(), nonzero.doubleValue() * scalar);
            }

        } else {

            Scalar<N> sclr = this.physical().scalar().convert(scalar);

            for (ElementView2D<N, ?> nonzero : this.nonzeros()) {
                retVal.set(nonzero.index(), sclr.multiply(nonzero.get()).get());
            }
        }

        return retVal;
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {

        long numberOfRows = this.countRows();
        long numberOfColumns = right.countColumns();

        if (right instanceof SparseStore) {

            SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), numberOfRows, numberOfColumns);

            SparseStore.multiply(this, (SparseStore<N>) right, retVal);

            return retVal;
        }

        PhysicalStore<N> retVal = this.physical().make(numberOfRows, numberOfColumns);

        this.multiply(right, retVal);

        return retVal;
    }

    @Override
    public MatrixStore<N> multiply(final N scalar) {

        SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), this);

        if (this.isPrimitive()) {

            double sclr = NumberDefinition.doubleValue(scalar);

            for (ElementView2D<N, ?> nonzero : this.nonzeros()) {
                retVal.set(nonzero.index(), nonzero.doubleValue() * sclr);
            }

        } else {

            Scalar<N> sclr = this.physical().scalar().convert(scalar);

            for (ElementView2D<N, ?> nonzero : this.nonzeros()) {
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

    @Override
    public ElementView2D<N, ?> nonzeros() {
        return new Access2D.ElementView<>(myElements.nonzeros(), this.countRows());
    }

    @Override
    public ElementsSupplier<N> premultiply(final Access1D<N> left) {

        long complexity = this.countRows();
        long numberOfColumns = this.countColumns();
        long numberOfRows = left.count() / complexity;

        if (left instanceof SparseStore<?>) {

            SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), numberOfRows, numberOfColumns);

            SparseStore.multiply((SparseStore<N>) left, this, retVal);

            return retVal;

        }
        if (!this.isPrimitive()) {

            return super.premultiply(left);
        }
        SparseStore<N> retVal = SparseStore.makeSparse(this.physical(), numberOfRows, numberOfColumns);

        this.nonzeros().stream().forEach(element -> {

            long row = element.row();
            long col = element.column();
            double value = element.doubleValue();

            long first = Structure2D.firstInColumn(left, row, 0L);
            long limit = Structure2D.limitOfColumn(left, row, numberOfRows);
            for (long i = first; i < limit; i++) {
                long index = Structure2D.index(numberOfRows, i, row);
                double addition = value * left.doubleValue(index);
                if (NumberContext.compare(addition, ZERO) != 0) {
                    retVal.add(i, col, addition);
                }
            }
        });

        return retVal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reduceColumns(final Aggregator aggregator, final Mutate1D receiver) {
        if (aggregator == Aggregator.SUM && receiver instanceof Mutate1D.Modifiable) {
            if (this.isPrimitive()) {
                this.nonzeros().forEach(element -> ((Mutate2D.Modifiable<?>) receiver).add(element.column(), element.doubleValue()));
            } else {
                this.nonzeros().forEach(element -> ((Mutate2D.Modifiable<?>) receiver).add(element.column(), element.get()));
            }
        } else {
            super.reduceColumns(aggregator, receiver);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reduceRows(final Aggregator aggregator, final Mutate1D receiver) {
        if (aggregator == Aggregator.SUM && receiver instanceof Mutate1D.Modifiable) {
            if (this.isPrimitive()) {
                this.nonzeros().forEach(element -> ((Mutate2D.Modifiable<?>) receiver).add(element.row(), element.doubleValue()));
            } else {
                this.nonzeros().forEach(element -> ((Mutate2D.Modifiable<?>) receiver).add(element.row(), element.get()));
            }
        } else {
            super.reduceColumns(aggregator, receiver);
        }
    }

    @Override
    public TransformableRegion<N> regionByColumns(final int... columns) {
        return new Subregion2D.ColumnsRegion<>(this, myMultiplier, columns);
    }

    @Override
    public TransformableRegion<N> regionByLimits(final int rowLimit, final int columnLimit) {
        return new Subregion2D.LimitRegion<>(this, myMultiplier, rowLimit, columnLimit);
    }

    @Override
    public TransformableRegion<N> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new Subregion2D.OffsetRegion<>(this, myMultiplier, rowOffset, columnOffset);
    }

    @Override
    public TransformableRegion<N> regionByRows(final int... rows) {
        return new Subregion2D.RowsRegion<>(this, myMultiplier, rows);
    }

    @Override
    public TransformableRegion<N> regionByTransposing() {
        return new Subregion2D.TransposedRegion<>(this, myMultiplier);
    }

    @Override
    public void reset() {
        myElements.reset();
        Arrays.fill(myFirsts, this.getColDim());
        Arrays.fill(myLimits, 0);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        synchronized (myElements) {
            myElements.set(Structure2D.index(myFirsts.length, row, col), value);
        }
        this.updateNonZeros(row, col);
    }

    @Override
    public void set(final int row, final int col, final double value) {
        synchronized (myElements) {
            myElements.set(Structure2D.index(myFirsts.length, row, col), value);
        }
        this.updateNonZeros(row, col);
    }

    @Override
    public void supplyTo(final TransformableRegion<N> receiver) {

        receiver.reset();

        myElements.supplyNonZerosTo(receiver);
    }

    @Override
    public void visitColumn(final long row, final long col, final VoidFunction<N> visitor) {

        long structure = this.countRows();
        long first = Structure2D.index(structure, row, col);
        long limit = Structure2D.index(structure, 0, col + 1L);

        myElements.visitRange(first, limit, visitor);
    }

    @Override
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
        if (col + counter < this.countColumns()) {
            visitor.accept(0.0);
        }
    }

    private void updateNonZeros(final long row, final long col) {
        this.updateNonZeros((int) row, (int) col);
    }

    SparseArray<N> getElements() {
        return myElements;
    }

    void updateNonZeros(final int row, final int col) {
        myFirsts[row] = Math.min(col, myFirsts[row]);
        myLimits[row] = Math.max(col + 1, myLimits[row]);
    }

}
