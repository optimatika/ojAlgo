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
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Transformation2D;

/**
 * Intermediate step in a matrix pipeline – a chain of operations to be executed when the elements are
 * extracted. Intermediate steps cannot alter the size/shape of the (future) matrix, only the elements
 * themselves. One notable exception is the {@linkplain #transpose()} operation, which can change the shape of
 * the matrix.
 */
abstract class MatrixPipeline<N extends Comparable<N>> implements ElementsSupplier<N> {

    static final class BinaryOperatorLeft<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final Access2D<N> myLeft;
        private final BinaryFunction<N> myOperator;

        BinaryOperatorLeft(final Access2D<N> left, final BinaryFunction<N> operator, final ElementsSupplier<N> right) {
            super(right);
            myLeft = left;
            myOperator = operator;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            this.getContext().supplyTo(receiver);
            receiver.modifyMatching(myLeft, myOperator);
        }
    }

    static final class BinaryOperatorRight<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final BinaryFunction<N> myOperator;
        private final Access2D<N> myRight;

        BinaryOperatorRight(final ElementsSupplier<N> left, final BinaryFunction<N> operator, final Access2D<N> right) {
            super(left);
            myRight = right;
            myOperator = operator;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            this.getContext().supplyTo(receiver);
            receiver.modifyMatching(myOperator, myRight);
        }
    }

    static final class ColumnsModifier<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final BinaryFunction<N> myFunction;
        private final Access1D<N> myLeftArgumnts;
        private final Access1D<N> myRightArgumnts;

        ColumnsModifier(final Access1D<N> left, final BinaryFunction<N> modifier, final ElementsSupplier<N> base) {
            super(base);
            myLeftArgumnts = left;
            myFunction = modifier;
            myRightArgumnts = null;
        }

        ColumnsModifier(final ElementsSupplier<N> base, final BinaryFunction<N> modifier, final Access1D<N> right) {
            super(base);
            myLeftArgumnts = null;
            myFunction = modifier;
            myRightArgumnts = right;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {

            this.getContext().supplyTo(receiver);

            if (myLeftArgumnts != null) {
                for (int j = 0, limit = Math.min(receiver.getColDim(), myLeftArgumnts.size()); j < limit; j++) {
                    receiver.modifyColumn(j, myFunction.first(myLeftArgumnts.get(j)));
                }
            } else if (myRightArgumnts != null) {
                for (int j = 0, limit = Math.min(receiver.getColDim(), myRightArgumnts.size()); j < limit; j++) {
                    receiver.modifyColumn(j, myFunction.second(myRightArgumnts.get(j)));
                }
            }
        }

    }

    static final class ColumnsReducer<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final Aggregator myAggregator;
        private final MatrixStore<N> myBase;

        ColumnsReducer(final MatrixStore<N> base, final Aggregator aggregator) {
            super(base, 1, base.getColDim());
            myBase = base;
            myAggregator = aggregator;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            myBase.reduceColumns(myAggregator, receiver);
        }

    }

    static final class Multiplication<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final Access1D<N> myLeft;
        private final MatrixStore<N> myRight;

        Multiplication(final Access1D<N> left, final MatrixStore<N> right) {

            super(right, Math.toIntExact(left.count() / right.countRows()), right.getColDim());

            myLeft = left;
            myRight = right;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            receiver.fillByMultiplying(myLeft, myRight);
        }

    }

    static final class RowsModifier<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final BinaryFunction<N> myFunction;
        private final Access1D<N> myLeftArgumnts;
        private final Access1D<N> myRightArgumnts;

        RowsModifier(final Access1D<N> left, final BinaryFunction<N> modifier, final ElementsSupplier<N> base) {
            super(base);
            myFunction = modifier;
            myLeftArgumnts = left;
            myRightArgumnts = null;
        }

        RowsModifier(final ElementsSupplier<N> base, final BinaryFunction<N> modifier, final Access1D<N> right) {
            super(base);
            myFunction = modifier;
            myLeftArgumnts = null;
            myRightArgumnts = right;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {

            this.getContext().supplyTo(receiver);

            if (myLeftArgumnts != null) {
                for (int i = 0, limit = Math.min(receiver.getRowDim(), myLeftArgumnts.size()); i < limit; i++) {
                    receiver.modifyRow(i, myFunction.first(myLeftArgumnts.get(i)));
                }
            } else if (myRightArgumnts != null) {
                for (int i = 0, limit = Math.min(receiver.getRowDim(), myRightArgumnts.size()); i < limit; i++) {
                    receiver.modifyRow(i, myFunction.second(myRightArgumnts.get(i)));
                }
            }
        }

    }

    static final class RowsReducer<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final Aggregator myAggregator;
        private final MatrixStore<N> myBase;

        RowsReducer(final MatrixStore<N> base, final Aggregator aggregator) {
            super(base, base.getRowDim(), 1);
            myBase = base;
            myAggregator = aggregator;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            myBase.reduceRows(myAggregator, receiver);
        }

    }

    static final class Transformer<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final Transformation2D<N> myTransformer;

        Transformer(final ElementsSupplier<N> context, final Transformation2D<N> operator) {
            super(context);
            myTransformer = operator;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            this.getContext().supplyTo(receiver);
            myTransformer.transform(receiver);
        }
    }

    static final class Transpose<N extends Comparable<N>> extends MatrixPipeline<N> {

        Transpose(final ElementsSupplier<N> context) {
            super(context, context.getColDim(), context.getRowDim());
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            this.getContext().supplyTo(receiver.regionByTransposing());
        }

        @Override
        public ElementsSupplier<N> transpose() {
            return this.getContext();
        }
    }

    static final class UnaryOperator<N extends Comparable<N>> extends MatrixPipeline<N> {

        private final UnaryFunction<N> myOperator;

        UnaryOperator(final ElementsSupplier<N> context, final UnaryFunction<N> operator) {
            super(context);
            myOperator = operator;
        }

        @Override
        public void supplyTo(final TransformableRegion<N> receiver) {
            this.getContext().supplyTo(receiver);
            receiver.modifyAll(myOperator);
        }
    }

    private final int myColumnsCount;
    private final ElementsSupplier<N> myContext;
    private final int myRowsCount;

    MatrixPipeline(final ElementsSupplier<N> context) {
        this(context, context.getRowDim(), context.getColDim());
    }

    MatrixPipeline(final ElementsSupplier<N> context, final int rowsCount, final int columnsCount) {
        super();
        myContext = context;
        myRowsCount = rowsCount;
        myColumnsCount = columnsCount;
    }

    @Override
    public final long countColumns() {
        return myColumnsCount;
    }

    @Override
    public final long countRows() {
        return myRowsCount;
    }

    @Override
    public final int getColDim() {
        return myColumnsCount;
    }

    @Override
    public final int getRowDim() {
        return myRowsCount;
    }

    @Override
    public final String toString() {
        return myRowsCount + "x" + myColumnsCount + " " + this.getClass();
    }

    final ElementsSupplier<N> getContext() {
        return myContext;
    }

}
