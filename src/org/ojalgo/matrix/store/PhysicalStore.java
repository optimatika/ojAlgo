/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.access.Factory2D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.matrix.transformation.TransformationMatrix;
import org.ojalgo.scalar.Scalar;

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
public interface PhysicalStore<N extends Number>
        extends MatrixStore<N>, Access2D.IndexOf, ElementsConsumer<N>, Mutate2D.Exchangeable, TransformationMatrix.Transformable<N> {

    public static interface Factory<N extends Number, I extends PhysicalStore<N>> extends Factory2D<I>, Serializable {

        AggregatorSet<N> aggregator();

        DenseArray.Factory<N> array();

        MatrixStore.Factory<N> builder();

        I conjugate(Access2D<?> source);

        FunctionSet<N> function();

        default ColumnsSupplier<N> makeColumnsSupplier(final int numberOfRows) {
            return new ColumnsSupplier<>(this, numberOfRows);
        }

        Householder<N> makeHouseholder(int length);

        Rotation<N> makeRotation(int low, int high, double cos, double sin);

        Rotation<N> makeRotation(int low, int high, N cos, N sin);

        default RowsSupplier<N> makeRowsSupplier(final int numberOfColumns) {
            return new RowsSupplier<>(this, numberOfColumns);
        }

        Scalar.Factory<N> scalar();

        I transpose(Access2D<?> source);

    }

    /**
     * @return The elements of the physical store as a fixed size (1 dimensional) list. The elements may be
     *         accessed either row or colomn major.
     */
    List<N> asList();

    /**
     * Will solve the equation system [A][X]=[B] where:
     * <ul>
     * <li>[body][this]=[this] is [A][X]=[B] ("this" is the right hand side, and it will be overwritten with
     * the solution).</li>
     * <li>[A] is upper/right triangular</li>
     * </ul>
     *
     * @param body The equation system body parameters [A]
     * @param unitDiagonal TODO
     * @param conjugated true if the upper/right part of body is actually stored in the lower/left part of the
     *        matrix, and the elements conjugated.
     * @param hermitian TODO
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
     * @param body The equation system body parameters [A]
     * @param unitDiagonal true if body has ones on the diagonal
     * @param conjugated TODO
     * @param identity
     */
    void substituteForwards(Access2D<N> body, boolean unitDiagonal, boolean conjugated, boolean identity);

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
