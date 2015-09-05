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
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;

/**
 * <p>
 * PhysicalStore:s, as opposed to MatrixStore:s, are mutable. The vast majorty of the methods defined here
 * return void and none return {@linkplain PhysicalStore} or {@linkplain MatrixStore}.
 * </p>
 * <p>
 * This interface and its implementations are central to ojAlgo.
 * </p>
 *
 * @author apete
 */
public interface PhysicalStore<N extends Number> extends MatrixStore<N>, MatrixStore.ElementsConsumer<N> {

    public static final class ConsumerRegion<N extends Number> implements MatrixStore.ElementsConsumer<N> {

        private final MatrixStore.ElementsConsumer<N> myDelegate;
        private final int myRowOffset, myColumnOffset; // origin/offset

        ConsumerRegion(final MatrixStore.ElementsConsumer<N> delegate, final int row, final int column) {
            super();
            myDelegate = delegate;
            myRowOffset = row;
            myColumnOffset = column;
        }

        public void accept(final Access2D<N> supplied) {
            final long tmpCountRows = supplied.countRows();
            final long tmpCountColumns = supplied.countColumns();
            for (long j = 0; j < tmpCountColumns; j++) {
                for (long i = 0; i < tmpCountRows; i++) {
                    myDelegate.set(myRowOffset + i, myColumnOffset + j, supplied.get(i, j));
                }
            }
        }

        public void add(final long row, final long column, final double addend) {
            myDelegate.add(myRowOffset + row, myColumnOffset + column, addend);
        }

        public void add(final long row, final long column, final Number addend) {
            myDelegate.add(myRowOffset + row, myColumnOffset + column, addend);
        }

        public long countColumns() {
            return myDelegate.countColumns() - myColumnOffset;
        }

        public long countRows() {
            return myDelegate.countRows() - myRowOffset;
        }

        public void fillAll(final N value) {
            final long tmpCountColumns = myDelegate.countColumns();
            for (long j = myColumnOffset; j < tmpCountColumns; j++) {
                myDelegate.fillColumn(myRowOffset, j, value);
            }
        }

        public void fillAll(final NullaryFunction<N> supplier) {
            final long tmpCountColumns = myDelegate.countColumns();
            for (long j = myColumnOffset; j < tmpCountColumns; j++) {
                myDelegate.fillColumn(myRowOffset, j, supplier);
            }
        }

        public void fillColumn(final long row, final long column, final N value) {
            myDelegate.fillColumn(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillColumn(final long row, final long column, final NullaryFunction<N> supplier) {
            myDelegate.fillColumn(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void fillDiagonal(final long row, final long column, final N value) {
            myDelegate.fillDiagonal(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillDiagonal(final long row, final long column, final NullaryFunction<N> supplier) {
            myDelegate.fillDiagonal(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void fillOne(final long row, final long column, final N value) {
            myDelegate.fillOne(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillOne(final long row, final long column, final NullaryFunction<N> supplier) {
            myDelegate.fillOne(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void fillRow(final long row, final long column, final N value) {
            myDelegate.fillRow(myRowOffset + row, myColumnOffset + column, value);
        }

        public void fillRow(final long row, final long column, final NullaryFunction<N> supplier) {
            myDelegate.fillRow(myRowOffset + row, myColumnOffset + column, supplier);
        }

        public void modifyAll(final UnaryFunction<N> function) {
            for (long j = myColumnOffset; j < myDelegate.countColumns(); j++) {
                myDelegate.modifyColumn(myRowOffset, j, function);
            }
        }

        public void modifyColumn(final long row, final long column, final UnaryFunction<N> function) {
            myDelegate.modifyColumn(myRowOffset + row, myColumnOffset + column, function);
        }

        public void modifyDiagonal(final long row, final long column, final UnaryFunction<N> function) {
            myDelegate.modifyDiagonal(myRowOffset + row, myColumnOffset + column, function);
        }

        public void modifyOne(final long row, final long column, final UnaryFunction<N> function) {
            myDelegate.modifyOne(myRowOffset + row, myColumnOffset + column, function);
        }

        public void modifyRow(final long row, final long column, final UnaryFunction<N> function) {
            myDelegate.modifyRow(myRowOffset + row, myColumnOffset + column, function);
        }

        public MatrixStore.ElementsConsumer<N> region(final int offsetRow, final int offsetColumn) {
            return new ConsumerRegion<N>(this, offsetRow, offsetColumn);
        }

        public void set(final long row, final long column, final double value) {
            myDelegate.set(myRowOffset + row, myColumnOffset + column, value);
        }

        public void set(final long row, final long column, final Number value) {
            myDelegate.set(myRowOffset + row, myColumnOffset + column, value);
        }

    }

    public static interface Factory<N extends Number, I extends PhysicalStore<N>> extends Access2D.Factory<I>, Serializable {

        AggregatorSet<N> aggregator();

        MatrixStore.Factory<N> builder();

        I conjugate(Access2D<?> source);

        FunctionSet<N> function();

        BasicArray<N> makeArray(int length);

        Householder<N> makeHouseholder(int length);

        Rotation<N> makeRotation(int low, int high, double cos, double sin);

        Rotation<N> makeRotation(int low, int high, N cos, N sin);

        Scalar.Factory<N> scalar();

        I transpose(Access2D<?> source);

    }

    /**
     * @return The elements of the physical store as a fixed size (1 dimensional) list. The elements may be
     *         accessed either row or colomn major.
     */
    List<N> asList();

    /**
     * <p>
     * <b>c</b>olumn <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this(*,aColY)] = aSclrA [this(*,aColX)] + [this(*,aColY)]
     *
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void caxpy(final N scalarA, final int columnX, final int columnY, final int firstRow);

    void exchangeColumns(int colA, int colB);

    void exchangeRows(int rowA, int rowB);

    void fillByMultiplying(final Access1D<N> left, final Access1D<N> right);

    void fillMatching(Access1D<? extends Number> source);

    /**
     * <p>
     * Will replace the elements of [this] with the results of element wise invocation of the input binary
     * funtion:
     * </p>
     * <code>this(i,j) = aFunc.invoke(aLeftArg(i,j),aRightArg(i,j))</code>
     */
    void fillMatching(Access1D<N> leftArg, BinaryFunction<N> func, Access1D<N> rightArg);

    /**
     * <p>
     * Will replace the elements of [this] with the results of element wise invocation of the input binary
     * funtion:
     * </p>
     * <code>this(i,j) = aFunc.invoke(aLeftArg(i,j),aRightArg))</code>
     */
    void fillMatching(Access1D<N> leftArg, BinaryFunction<N> func, N rightArg);

    /**
     * <p>
     * Will replace the elements of [this] with the results of element wise invocation of the input binary
     * funtion:
     * </p>
     * <code>this(i,j) = aFunc.invoke(aLeftArg,aRightArg(i,j))</code>
     */
    void fillMatching(N leftArg, BinaryFunction<N> func, Access1D<N> rightArg);

    /**
     * <p>
     * <b>m</b>atrix <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this] = aSclrA [aMtrxX] + [this]
     *
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void maxpy(final N scalarA, final MatrixStore<N> matrixX);

    /**
     * <p>
     * <b>r</b>ow <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this(aRowY,*)] = aSclrA [this(aRowX,*)] + [this(aRowY,*)]
     *
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void raxpy(final N scalarA, final int rowX, final int rowY, final int firstColumn);

    void transformLeft(Householder<N> transformation, int firstColumn);

    /**
     * <p>
     * As in {@link MatrixStore#multiplyLeft(Access1D)} where the left/parameter matrix is a plane rotation.
     * </p>
     * <p>
     * Multiplying by a plane rotation from the left means that [this] gets two of its rows updated to new
     * combinations of those two (current) rows.
     * </p>
     * <p>
     * There are two ways to transpose/invert a rotation. Either you negate the angle or you interchange the
     * two indeces that define the rotation plane.
     * </p>
     *
     * @see #transformRight(Rotation)
     */
    void transformLeft(Rotation<N> transformation);

    void transformRight(Householder<N> transformation, int firstRow);

    /**
     * <p>
     * As in {@link MatrixStore#multiply(Access1D)} where the right/parameter matrix is a plane rotation.
     * </p>
     * <p>
     * Multiplying by a plane rotation from the right means that [this] gets two of its columns updated to new
     * combinations of those two (current) columns.
     * </p>
     * <p>
     * There result is undefined if the two input indeces are the same (in which case the rotation plane is
     * undefined).
     * </p>
     *
     * @see #transformLeft(Rotation)
     */
    void transformRight(Rotation<N> transformation);

}
