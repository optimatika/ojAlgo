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
package org.ojalgo.matrix.decomposition;

/**
 * <p>
 * LDU: [A] = [L][D][U] ( [P1][L][D][U][P2] )
 * </p>
 * <ul>
 * <li>[A] can be any matrix.</li>
 * <li>[L] is a unit lower (left) triangular matrix. It has the same number of rows as [A], and ones on the
 * diagonal.</li>
 * <li>[D] is a square diagonal matrix.</li>
 * <li>[U] is a unit upper (right) triangular matrix. It has the same number of columns as [A], and ones on
 * the diagonal.</li>
 * <li>[P1] is a permutation matrix (row pivot order).</li>
 * <li>[P2] is a permutation matrix (column pivot order).</li>
 * </ul>
 * <p>
 * Row and/or column permutations may not be necessary and are therefore optional. Numerical stability usually
 * does require ordering of either the rows or columns (most algorithms reorder rows).
 * </p>
 * <p>
 * Solving the equation system [A][X]=[B] turns into this [L][D][U][X] = [B] and is solved in these steps:
 * </p>
 * <ol>
 * <li>[L][Z]=[B] ( [Z] = [D][U][X] )</li>
 * <li>[D][Y]=[Z] ( [Y] = [U][X] )</li>
 * <li>[U][X]=[Y]</li>
 * </ol>
 * <p>
 * [A]<sup>H</sup> = [U]<sup>H</sup>[D]<sup>H</sup>[L]<sup>H</sup>
 * </p>
 * <p>
 * ojAlgo does not have a full/general LDU decompositions but contains 3 variations of it:
 * </p>
 * <ul>
 * <li>LU: [A] = [L][U] where [U<sub>LU</sub>] = [D<sub>LDU</sub>][U<sub>LDU</sub>]</li>
 * <li>Cholesky: [A] = [L][L]<sup>H</sup> where [A] is hermitian positive definite and [L<sub>Cholesky</sub>]
 * = [L<sub>LDU</sub>][D<sub>LDU</sub>]<sup>½</sup></li>
 * <li>LDL: [A] = [L][D][L]<sup>H</sup> where [A] is hermitian and [L<sub>LDL</sub>]<sup>H</sup> =
 * [U<sub>LDU</sub>]</li>
 * </ul>
 *
 * @author apete
 */
public interface LDU<N extends Number> extends MatrixDecomposition<N>, MatrixDecomposition.Solver<N>, MatrixDecomposition.Determinant<N> {

}
