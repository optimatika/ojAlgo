/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;

/**
 * <p>
 * Only classes that will act as a delegate to a {@linkplain MatrixDecomposition} implementation from this
 * package should implement this interface. The interface specifications are entirely dictated by the classes
 * in this package.
 * </p>
 * <p>
 * Do not use it for anything else!
 * </p>
 *
 * @author apete
 */
public interface DecompositionStore<N extends Number> extends PhysicalStore<N> {

    /**
     * Cholesky transformations
     */
    void applyCholesky(final int iterationPoint, final BasicArray<N> multipliers);

    /**
     * LDL transformations
     */
    void applyLDL(final int iterationPoint, final BasicArray<N> multipliers);

    /**
     * LU transformations
     */
    void applyLU(final int iterationPoint, final BasicArray<N> multipliers);

    Array1D<ComplexNumber> computeInPlaceSchur(PhysicalStore<N> transformationCollector, boolean eigenvalue);

    void divideAndCopyColumn(int row, int column, BasicArray<N> destination);

    void exchangeHermitian(int indexA, int indexB);

    boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<N> destination);

    boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<N> destination);

    void negateColumn(int column);

    void rotateRight(int aLow, int aHigh, double aCos, double aSin);

    void setToIdentity(int aCol);

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
     * @param unitDiagonal true if body as ones on the diagonal
     * @param conjugated TODO
     * @param identity
     */
    void substituteForwards(Access2D<N> body, boolean unitDiagonal, boolean conjugated, boolean identity);

    void transformSymmetric(Householder<N> transformation);

    void tred2(BasicArray<N> mainDiagonal, BasicArray<N> offDiagonal, boolean yesvecs);

}
