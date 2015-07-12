/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.io.Serializable;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

abstract class AbstractStore<N extends Number> implements MatrixStore<N>, Serializable {

    private final int myColDim;
    private transient Class<?> myComponentType = null;
    private final int myRowDim;

    @SuppressWarnings("unused")
    private AbstractStore() {

        this(0, 0);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected AbstractStore(final int rowsCount, final int columnsCount) {

        super();

        myRowDim = rowsCount;
        myColDim = columnsCount;
    }

    public MatrixStore<N> add(final MatrixStore<N> addend) {
        return new SuperimposedStore<>(this, addend);
    }

    @SuppressWarnings("unchecked")
    public N aggregateAll(final Aggregator aggregator) {

        final AggregatorFunction<N> tmpFunction = (AggregatorFunction<N>) aggregator.getFunction(this.getComponentType());

        this.visitAll(tmpFunction);

        return tmpFunction.getNumber();
    }

    public final MatrixStore.Builder<N> builder() {
        return new MatrixStore.Builder<N>(this);
    }

    public MatrixStore<N> conjugate() {
        return new ConjugatedStore<>(this);
    }

    public PhysicalStore<N> copy() {
        return this.factory().copy(this);
    }

    public long count() {
        return myRowDim * myColDim;
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    public double doubleValue(final long index) {
        return this.doubleValue(AccessUtils.row((int) index, myRowDim), AccessUtils.column((int) index, myRowDim));
    }

    public final boolean equals(final MatrixStore<N> other, final NumberContext context) {
        return AccessUtils.equals(this, other, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(final Object someObj) {
        if (someObj instanceof MatrixStore) {
            return this.equals((MatrixStore<N>) someObj, NumberContext.getGeneral(6));
        } else {
            return super.equals(someObj);
        }
    }

    public N get(final long index) {
        return this.get(AccessUtils.row(index, myRowDim), AccessUtils.column(index, myRowDim));
    }

    @Override
    public final int hashCode() {
        return MatrixUtils.hashCode(this);
    }

    public boolean isAbsolute(final long index) {
        return this.toScalar(index).isAbsolute();
    }

    public boolean isAbsolute(final long row, final long column) {
        return this.toScalar(row, column).isAbsolute();
    }

    /**
     * @see org.ojalgo.access.Access1D.Elements#isSmall(long, double)
     */
    public boolean isSmall(final long index, final double comparedTo) {
        return this.toScalar(index).isSmall(comparedTo);
    }

    /**
     * @see org.ojalgo.access.Access2D.Elements#isSmall(long, long, double)
     */
    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return this.toScalar(row, column).isSmall(comparedTo);
    }

    public MatrixStore<N> multiply(final double scalar) {
        if (this.isPrimitive()) {
            return new ModificationStore<>(this, this.factory().function().multiply().first(scalar));
        } else {
            return new ModificationStore<>(this, this.factory().function().multiply().first(this.factory().scalar().cast(scalar)));
        }
    }

    public MatrixStore<N> multiply(final N scalar) {
        return new ModificationStore<>(this, this.factory().function().multiply().first(scalar));
    }

    public MatrixStore<N> multiplyLeft(final Access1D<N> leftMtrx) {

        final int tmpRowDim = (int) (leftMtrx.count() / this.countRows());
        final int tmpColDim = this.getColDim();

        final PhysicalStore<N> retVal = this.factory().makeZero(tmpRowDim, tmpColDim);

        retVal.fillByMultiplying(leftMtrx, this);

        return retVal;
    }

    public final MatrixStore<N> negate() {
        return new ModificationStore<>(this, this.factory().function().negate());
    }

    public MatrixStore<N> subtract(final MatrixStore<N> subtrahend) {
        return this.add(subtrahend.negate());
    }

    @Override
    public final String toString() {
        return MatrixUtils.toString(this);
    }

    public MatrixStore<N> transpose() {
        return new TransposedStore<>(this);
    }

    public void visitAll(final VoidFunction<N> visitor) {
        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        for (int j = 0; j < tmpColDim; j++) {
            for (int i = 0; i < tmpRowDim; i++) {
                visitor.invoke(this.get(i, j));
            }
        }
    }

    public void visitColumn(final long row, final long column, final VoidFunction<N> visitor) {
        final long tmpRowDim = this.countRows();
        for (long i = row; i < tmpRowDim; i++) {
            visitor.invoke(this.get(i, column));
        }
    }

    public void visitDiagonal(final long row, final long column, final VoidFunction<N> visitor) {
        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        for (int ij = 0; ((row + ij) < tmpRowDim) && ((column + ij) < tmpColDim); ij++) {
            visitor.invoke(this.get(row + ij, column + ij));
        }
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        for (long i = first; i < limit; i++) {
            visitor.invoke(this.get(i));
        }
    }

    public void visitRow(final long row, final long column, final VoidFunction<N> visitor) {
        final long tmpColDim = this.countColumns();
        for (long j = column; j < tmpColDim; j++) {
            visitor.invoke(this.get(row, j));
        }
    }

    private Scalar<N> toScalar(final long index) {
        return this.toScalar(AccessUtils.row(index, myRowDim), AccessUtils.column(index, myRowDim));
    }

    protected final int getColDim() {
        return myColDim;
    }

    protected final Class<?> getComponentType() {
        if (myComponentType == null) {
            myComponentType = this.get(0, 0).getClass();
        }
        return myComponentType;
    }

    protected final int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    protected final int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    protected final int getRowDim() {
        return myRowDim;
    }

    boolean isPrimitive() {
        return this.getComponentType().equals(Double.class);
    }

    public double norm() {
        // TODO Auto-generated method stub
        return 0;
    }

}
