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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.MatrixStore;

/**
 * Tridiagonal: [A] = [Q][D][Q]<sup>H</sup>
 * Any square symmetric (hermitian) matrix [A] can be factorized by
 * similarity transformations into the form,
 * [A]=[Q][D][Q]<sup>-1</sup>
 * where [Q] is an orthogonal (unitary) matrix and [D] is a real
 * symmetric tridiagonal matrix. Note that [D] can/should be made real
 * even when [A] has complex elements. Since [Q] is orthogonal (unitary)
 * [Q]<sup>-1</sup> = [Q]<sup>H</sup> and when it is real [Q]<sup>H</sup> = [Q]<sup>T</sup>.
 *
 * @author apete
 */
public interface Tridiagonal<N extends Number> extends MatrixDecomposition<N> {

    MatrixStore<N> getD();

    MatrixStore<N> getQ();

}
