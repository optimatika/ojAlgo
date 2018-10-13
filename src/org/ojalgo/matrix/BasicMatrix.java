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
package org.ojalgo.matrix;

import org.ojalgo.algebra.NormedVectorSpace;
import org.ojalgo.algebra.Operation;
import org.ojalgo.algebra.ScalarOperation;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * This interface declares a limited set of high level methods for linear algebra. If this is not enough for
 * your use case, then look at the various interfaces/classes in the {@linkplain org.ojalgo.matrix.store}
 * and/or {@linkplain org.ojalgo.matrix.decomposition} packages.
 * </p>
 *
 * @author apete
 * @deprecated v46.3 Use the specific implementations instead {@link PrimitiveMatrix}, {@link ComplexMatrix}
 *             or {@link RationalMatrix}.
 */
@Deprecated
public interface BasicMatrix<N extends Number, M extends BasicMatrix<N, M>> extends NormedVectorSpace<M, N>, Operation.Subtraction<M>,
        Operation.Multiplication<M>, ScalarOperation.Addition<M, N>, ScalarOperation.Division<M, N>, ScalarOperation.Subtraction<M, N>, Access2D<N>,
        Access2D.Elements, Access2D.Aggregatable<N>, Structure2D.ReducibleTo1D<M>, NumberContext.Enforceable<M> {

}
