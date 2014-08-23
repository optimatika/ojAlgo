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

import java.io.Serializable;
import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorCollection;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.Scalar;

/**
 * <p>
 * PhysicalStore:s, as opposed to MatrixStore:s, are mutable. The vast majorty of the methods defined here return void
 * and none return {@linkplain PhysicalStore} or {@linkplain MatrixStore}.
 * </p>
 * <p>
 * This interface and its implementations are central to ojAlgo.
 * </p>
 * 
 * @author apete
 */
public interface PhysicalStore<N extends Number> extends MatrixStore<N>, Access2D.Fillable<N>, Access2D.Modifiable<N> {

    public static interface Factory<N extends Number, I extends PhysicalStore<N>> extends Access2D.Factory<I>, Serializable {

        AggregatorCollection<N> aggregator();

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
     * @return The elements of the physical store as a fixed size (1 dimensional) list. The elements may be accessed
     *         either row or colomn major.
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
    void caxpy(final N aSclrA, final int aColX, final int aColY, final int aFirstRow);

    void exchangeColumns(int aColA, int aColB);

    void exchangeRows(int aRowA, int aRowB);

    void fillByMultiplying(final Access1D<N> leftMtrx, final Access1D<N> rightMtrx);

    void fillConjugated(Access2D<? extends Number> source);

    void fillMatching(Access1D<? extends Number> source);

    /**
     * <p>
     * Will replace the elements of [this] with the results of element wise invocation of the input binary funtion:
     * </p>
     * <code>this(i,j) = aFunc.invoke(aLeftArg(i,j),aRightArg(i,j))</code>
     */
    void fillMatching(Access1D<N> leftArg, BinaryFunction<N> func, Access1D<N> rightArg);

    /**
     * <p>
     * Will replace the elements of [this] with the results of element wise invocation of the input binary funtion:
     * </p>
     * <code>this(i,j) = aFunc.invoke(aLeftArg(i,j),aRightArg))</code>
     */
    void fillMatching(Access1D<N> leftArg, BinaryFunction<N> func, N rightArg);

    /**
     * <p>
     * Will replace the elements of [this] with the results of element wise invocation of the input binary funtion:
     * </p>
     * <code>this(i,j) = aFunc.invoke(aLeftArg,aRightArg(i,j))</code>
     */
    void fillMatching(N leftArg, BinaryFunction<N> func, Access1D<N> rightArg);

    void fillTransposed(Access2D<? extends Number> source);

    /**
     * <p>
     * <b>m</b>atrix <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this] = aSclrA [aMtrxX] + [this]
     * 
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void maxpy(final N aSclrA, final MatrixStore<N> aMtrxX);

    void modifyOne(long row, long column, UnaryFunction<N> func);

    /**
     * <p>
     * <b>r</b>ow <b>a</b> * <b>x</b> <b>p</b>lus <b>y</b>
     * </p>
     * [this(aRowY,*)] = aSclrA [this(aRowX,*)] + [this(aRowY,*)]
     * 
     * @deprecated v32 Let me know if you need this
     */
    @Deprecated
    void raxpy(final N aSclrA, final int aRowX, final int aRowY, final int aFirstCol);

    void transformLeft(Householder<N> transformation, int firstColumn);

    /**
     * <p>
     * As in {@link MatrixStore#multiplyLeft(MatrixStore)} where the left/parameter matrix is a plane rotation.
     * </p>
     * <p>
     * Multiplying by a plane rotation from the left means that [this] gets two of its rows updated to new combinations
     * of those two (current) rows.
     * </p>
     * <p>
     * There are two ways to transpose/invert a rotation. Either you negate the angle or you interchange the two indeces
     * that define the rotation plane.
     * </p>
     * 
     * @see #transformRight(Rotation)
     */
    void transformLeft(Rotation<N> transformation);

    void transformRight(Householder<N> transformation, int firstRow);

    /**
     * <p>
     * As in {@link MatrixStore#multiplyRight(MatrixStore)} where the right/parameter matrix is a plane rotation.
     * </p>
     * <p>
     * Multiplying by a plane rotation from the right means that [this] gets two of its columns updated to new
     * combinations of those two (current) columns.
     * </p>
     * <p>
     * There result is undefined if the two input indeces are the same (in which case the rotation plane is undefined).
     * </p>
     * 
     * @see #transformLeft(Rotation)
     */
    void transformRight(Rotation<N> transformation);

}
