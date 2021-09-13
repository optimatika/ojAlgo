/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.matrix;

import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.Operation;
import org.ojalgo.algebra.ScalarOperation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.structure.Access2D;

/**
 * Definition of what's common to {@link BasicMatrix} and {@link MatrixStore}. At this point, at least, it is
 * not recommended to write any code in terms of this interface. It's new, the definition may change and it
 * may even be removed again.
 *
 * @author apete
 */
public interface Matrix2D<N extends Comparable<N>, M extends Matrix2D<N, M>>
        extends Access2D<N>, Access2D.Aggregatable<N>, NormedVectorSpace<M, N>, Operation.Subtraction<M>, Operation.Multiplication<M>,
        ScalarOperation.Addition<M, N>, ScalarOperation.Subtraction<M, N>, ScalarOperation.Division<M, N> {

    M transpose();

}
