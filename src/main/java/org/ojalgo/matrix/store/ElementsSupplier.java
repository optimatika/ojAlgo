/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Operate2D;
import org.ojalgo.structure.Transformation2D;

/**
 * An {@link ElementsSupplier} is not necessarily (or not yet) a matrix, but something from which the elements
 * of a matrix can be derived. There are several matrix related things you can do with them:
 * <ol>
 * <li>You can query the size/shape of the (future) matrix.</li>
 * <li>You can supply the elements to an already existing matrix (or more precisely to an
 * {@linkplain TransformableRegion}) or collect them into a new matrix using a {@linkplain Factory2D}.</li>
 * <li>You can define a stream of additional operations to be executed when the elements are extracted.</li>
 * </ol>
 *
 * @author apete
 */
public interface ElementsSupplier<N extends Comparable<N>> extends Operate2D<N, ElementsSupplier<N>>, Access2D.Collectable<N, TransformableRegion<N>> {

    default ElementsSupplier<N> onAll(final UnaryFunction<N> operator) {
        return new MatrixPipeline.UnaryOperator<>(this, operator);
    }

    default ElementsSupplier<N> onAny(final Transformation2D<N> operator) {
        return new MatrixPipeline.Transformer<>(this, operator);
    }

    default ElementsSupplier<N> onColumns(final BinaryFunction<N> operator, final Access1D<N> right) {
        return new MatrixPipeline.ColumnsModifier<>(this, operator, right);
    }

    default ElementsSupplier<N> onMatching(final Access2D<N> left, final BinaryFunction<N> operator) {
        return new MatrixPipeline.BinaryOperatorLeft<>(left, operator, this);
    }

    default ElementsSupplier<N> onMatching(final BinaryFunction<N> operator, final Access2D<N> right) {
        return new MatrixPipeline.BinaryOperatorRight<>(this, operator, right);
    }

    default ElementsSupplier<N> onRows(final BinaryFunction<N> operator, final Access1D<N> right) {
        return new MatrixPipeline.RowsModifier<>(this, operator, right);
    }

    default ElementsSupplier<N> transpose() {
        return new MatrixPipeline.Transpose<>(this);
    }

}
