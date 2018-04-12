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

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.PhysicalStore.Factory;

abstract class MatrixPipeline<N extends Number> implements ElementsSupplier<N> {

    static final class BinaryOperatorLeft<N extends Number> extends MatrixPipeline<N> {

        private final MatrixStore<N> myLeft;
        private final BinaryFunction<N> myOperator;

        BinaryOperatorLeft(final MatrixStore<N> left, final BinaryFunction<N> operator, final ElementsSupplier<N> right) {
            super(right);
            myLeft = left;
            myOperator = operator;
        }

        @Override
        public void supplyTo(final ElementsConsumer<N> receiver) {
            this.getContext().supplyTo(receiver);
            receiver.modifyMatching(myLeft, myOperator);
        }
    }

    static final class BinaryOperatorRight<N extends Number> extends MatrixPipeline<N> {

        private final BinaryFunction<N> myOperator;
        private final MatrixStore<N> myRight;

        BinaryOperatorRight(final ElementsSupplier<N> left, final BinaryFunction<N> operator, final MatrixStore<N> right) {
            super(left);
            myRight = right;
            myOperator = operator;
        }

        @Override
        public void supplyTo(final ElementsConsumer<N> receiver) {
            this.getContext().supplyTo(receiver);
            receiver.modifyMatching(myOperator, myRight);
        }
    }

    static final class ColumnsReducer<N extends Number> extends MatrixPipeline<N> {

        private final Aggregator myAggregator;
        private final MatrixStore<N> myBase;

        ColumnsReducer(MatrixStore<N> base, Aggregator aggregator) {
            super(base);
            myBase = base;
            myAggregator = aggregator;
        }

        @Override
        public long countRows() {
            return 1L;
        }

        @Override
        public void supplyTo(ElementsConsumer<N> receiver) {
            myBase.reduceColumns(myAggregator, receiver);
        }

    }

    static final class Multiplication<N extends Number> extends MatrixPipeline<N> {

        private final Access1D<N> myLeft;
        private final MatrixStore<N> myRight;

        Multiplication(final Access1D<N> left, final MatrixStore<N> right) {

            super(right);

            myLeft = left;
            myRight = right;
        }

        @Override
        public long countColumns() {
            return myRight.countColumns();
        }

        @Override
        public long countRows() {
            return myLeft.count() / myRight.countRows();
        }

        @Override
        public void supplyTo(final ElementsConsumer<N> receiver) {
            receiver.fillByMultiplying(myLeft, myRight);
        }

    }

    static final class RowsReducer<N extends Number> extends MatrixPipeline<N> {

        private final Aggregator myAggregator;
        private final MatrixStore<N> myBase;

        RowsReducer(MatrixStore<N> base, Aggregator aggregator) {
            super(base);
            myBase = base;
            myAggregator = aggregator;
        }

        @Override
        public long countColumns() {
            return 1L;
        }

        @Override
        public void supplyTo(ElementsConsumer<N> receiver) {
            myBase.reduceRows(myAggregator, receiver);
        }

    }

    static final class Transpose<N extends Number> extends MatrixPipeline<N> {

        Transpose(final ElementsSupplier<N> context) {
            super(context);
        }

        @Override
        public long countColumns() {
            return this.getContext().countRows();
        }

        @Override
        public long countRows() {
            return this.getContext().countColumns();
        }

        public MatrixStore<N> get() {

            final PhysicalStore<N> retVal = this.physical().makeZero(this.getContext().countRows(), this.getContext().countColumns());

            this.supplyTo(retVal);

            return retVal;
        }

        public ElementsSupplier<N> operateOnAll(final UnaryFunction<N> operator) {
            return this.getContext().operateOnAll(operator);
        }

        public ElementsSupplier<N> operateOnMatching(final BinaryFunction<N> operator, final MatrixStore<N> right) {
            return this.getContext().operateOnMatching(operator, right.transpose());
        }

        public ElementsSupplier<N> operateOnMatching(final MatrixStore<N> left, final BinaryFunction<N> operator) {
            return this.getContext().operateOnMatching(left.transpose(), operator);
        }

        @Override
        public void supplyTo(final ElementsConsumer<N> receiver) {
            this.getContext().supplyTo(receiver.regionByTransposing());
        }

        public ElementsSupplier<N> transpose() {
            return this.getContext();
        }
    }

    static final class UnaryOperator<N extends Number> extends MatrixPipeline<N> {

        private final UnaryFunction<N> myOperator;

        UnaryOperator(final ElementsSupplier<N> context, final UnaryFunction<N> operator) {
            super(context);
            myOperator = operator;
        }

        @Override
        public void supplyTo(final ElementsConsumer<N> receiver) {
            this.getContext().supplyTo(receiver);
            receiver.modifyAll(myOperator);
        }
    }

    private final ElementsSupplier<N> myContext;

    protected MatrixPipeline(final ElementsSupplier<N> context) {
        super();
        myContext = context;
    }

    public long countColumns() {
        return myContext.countColumns();
    }

    public long countRows() {
        return myContext.countRows();
    }

    public final Factory<N, ?> physical() {
        return myContext.physical();
    }

    @Override
    public String toString() {
        return myContext.toString();
    }

    ElementsSupplier<N> getContext() {
        return myContext;
    }

}
