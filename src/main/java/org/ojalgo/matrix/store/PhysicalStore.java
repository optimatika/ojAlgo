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
package org.ojalgo.matrix.store;

import java.util.List;

import org.ojalgo.array.DenseArray;
import org.ojalgo.array.operation.AMAX;
import org.ojalgo.array.operation.SubstituteBackwards;
import org.ojalgo.array.operation.SubstituteForwards;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Transformation2D;
import org.ojalgo.tensor.TensorFactory1D;
import org.ojalgo.tensor.TensorFactory2D;

/**
 * <p>
 * PhysicalStore:s, as opposed to MatrixStore:s, are mutable. The vast majority of the methods defined here
 * return void and none return {@linkplain PhysicalStore} or {@linkplain MatrixStore}.
 * </p>
 * <p>
 * This interface and its implementations are central to ojAlgo.
 * </p>
 *
 * @author apete
 */
public interface PhysicalStore<N extends Comparable<N>> extends MatrixStore<N>, TransformableRegion<N>, Access1D.Collectable<N, Mutate1D> {

    public interface Factory<N extends Comparable<N>, I extends PhysicalStore<N> & Factory2D.Builder<I>> extends Factory2D.TwoStep<I, I> {

        AggregatorSet<N> aggregator();

        DenseArray.Factory<N> array();

        I conjugate(Access2D<?> source);

        @Override
        FunctionSet<N> function();

        default ColumnsSupplier<N> makeColumnsSupplier(final int numberOfRows) {
            return new ColumnsSupplier<>(this, numberOfRows);
        }

        default <D extends Access1D<?>> DiagonalStore.Builder<N, D> makeDiagonal(final D mainDiagonal) {
            return DiagonalStore.builder(this, mainDiagonal);
        }

        default I makeEye(final long rows, final long columns) {

            I retVal = this.make(rows, columns);

            N tmpVal = this.scalar().one().get();

            retVal.fillDiagonal(tmpVal);

            return retVal;
        }

        default I makeEye(final Structure2D shape) {
            return this.makeEye(shape.countRows(), shape.countColumns());
        }

        @Override
        default I makeFilled(final long rows, final long columns, final NullaryFunction<?> supplier) {

            I retVal = this.newBuilder(rows, columns);

            retVal.fillAll(supplier);

            return retVal;
        }

        Householder<N> makeHouseholder(int length);

        default MatrixStore<N> makeIdentity(final long dimension) {
            return new IdentityStore<>(this, dimension);
        }

        Rotation<N> makeRotation(int low, int high, double cos, double sin);

        Rotation<N> makeRotation(int low, int high, N cos, N sin);

        default RowsSupplier<N> makeRowsSupplier(final int numberOfColumns) {
            return new RowsSupplier<>(this, numberOfColumns);
        }

        default MatrixStore<N> makeSingle(final double element) {
            return this.makeSingle(this.scalar().cast(element));
        }

        default MatrixStore<N> makeSingle(final N element) {
            return new SingleStore<>(this, element);
        }

        default SparseStore<N> makeSparse(final long nbRows, final long nbCols) {
            return SparseStore.factory(this).make(nbRows, nbCols);
        }

        default SparseStore<N> makeSparse(final Structure2D shape) {
            return this.makeSparse(shape.countRows(), shape.countColumns());
        }

        /**
         * Make a random Symmetric Positive Definite matrix
         */
        default I makeSPD(final int dim) {

            double[] random = new double[dim];
            I retVal = this.make(dim, dim);

            for (int i = 0; i < dim; i++) {
                random[i] = Math.random();
                for (int j = 0; j < i; j++) {
                    retVal.set(i, j, random[i] * random[j]);
                    retVal.set(j, i, random[j] * random[i]);
                }
                retVal.set(i, i, random[i] + 1.0);
            }

            return retVal;
        }

        default MatrixStore<N> makeWrapper(final Access2D<?> access) {
            return new WrapperStore<>(this, access);
        }

        default MatrixStore<N> makeWrapperColumn(final Access1D<?> access) {
            return new WrapperStore<>(access, this);
        }

        default MatrixStore<N> makeZero(final long rowsCount, final long columnsCount) {
            return new ZeroStore<>(this, rowsCount, columnsCount);
        }

        default MatrixStore<N> makeZero(final Structure2D shape) {
            return this.makeZero(shape.countRows(), shape.countColumns());
        }

        @Override
        default I newBuilder(final long nbRows, final long nbCols) {
            return this.make(nbRows, nbCols);
        }

        @Override
        Scalar.Factory<N> scalar();

        default TensorFactory1D<N, I> tensor1D() {
            return TensorFactory1D.of(this.asFactory1D());
        }

        default TensorFactory2D<N, I> tensor2D() {
            return TensorFactory2D.of(this);
        }

        I transpose(Access2D<?> source);

    }

    /**
     * @return The elements of the physical store as a fixed size (1 dimensional) list. The elements may be
     *         accessed either row or colomn major.
     */
    @Override
    List<N> asList();

    default int indexOfLargestInColumn(final int row, final int col) {
        long structure = this.countRows();
        long first = Structure2D.index(structure, row, col);
        long limit = Structure2D.index(structure, 0L, col + 1L);
        long step = 1L;
        long largest = AMAX.invoke(this, first, limit, step);
        return Math.toIntExact(largest % structure);
    }

    default int indexOfLargestInRow(final int row, final int col) {
        long structure = this.countRows();
        long first = Structure2D.index(structure, row, col);
        long limit = Structure2D.index(structure, 0L, this.countColumns());
        long step = structure;
        long largest = AMAX.invoke(this, first, limit, step);
        return Math.toIntExact(largest / structure);
    }

    default int indexOfLargestOnDiagonal(final int row, final int col) {
        long structure = this.countRows();
        long first = Structure2D.index(structure, row, col);
        long limit = Structure2D.index(structure, 0L, this.countColumns());
        long step = structure + 1L;
        long largest = AMAX.invoke(this, first, limit, step);
        return Math.toIntExact(largest / structure);
    }

    @Override
    default void modifyAny(final Transformation2D<N> modifier) {
        modifier.transform(this);
    }

    /**
     * Will solve the equation system [A][X]=[B] where:
     * <ul>
     * <li>[body][this]=[this] is [A][X]=[B] ("this" is the right hand side, and it will be overwritten with
     * the solution).</li>
     * <li>[A] is upper/right triangular</li>
     * </ul>
     *
     * @see SubstituteBackwards#invoke(double[], int, int, int, Access2D, boolean, boolean, boolean)
     */
    void substituteBackwards(Access2D<N> body, boolean unitDiagonal, boolean conjugated, boolean hermitian);

    /**
     * Will solve the equation system [A][X]=[B] where:
     * <ul>
     * <li>[body][this]=[this] is [A][X]=[B] ("this" is the right hand side, and it will be overwritten with
     * the solution).</li>
     * <li>[A] is lower/left triangular</li>
     * </ul>
     *
     * @see SubstituteForwards#invoke(double[], int, int, int, Access2D, boolean, boolean, boolean)
     */
    void substituteForwards(Access2D<N> body, boolean unitDiagonal, boolean conjugated, boolean identity);

    @Override
    default void supplyTo(final TransformableRegion<N> receiver) {
        if (this != receiver) {
            receiver.fillMatching(this);
        }
    }

    void transformLeft(Householder<N> transformation, int firstColumn);

    /**
     * <p>
     * As in {@link MatrixStore#premultiply(Access1D)} where the left/parameter matrix is a plane rotation.
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
     * As in {@link MatrixStore#multiply(MatrixStore)} where the right/parameter matrix is a plane rotation.
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
