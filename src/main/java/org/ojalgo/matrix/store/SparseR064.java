/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.operation.MultiplyBoth;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.type.context.NumberContext;

public abstract class SparseR064 extends FactoryStore<Double> implements TransformableRegion<Double> {

    public static final class ElementNode implements ElementView1D<Double, ElementNode> {

        /**
         * row or col index
         */
        public int index;

        /**
         * next in linked list
         */
        public ElementNode next;

        /**
         * The value
         */
        public double value;

        /**
         * previous in linked list
         */
        public ElementNode previous;

        public ElementNode(final int index, final double value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public long estimateSize() {
            return 1L;
        }

        @Override
        public Double get() {
            return Double.valueOf(value);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public boolean hasPrevious() {
            return previous != null;
        }

        @Override
        public long index() {
            return index;
        }

        @Override
        public ElementNode iterator() {
            return this;
        }

        @Override
        public ElementNode next() {
            return next;
        }

        @Override
        public long nextIndex() {
            return next != null ? next.index : Long.MAX_VALUE;
        }

        @Override
        public ElementNode previous() {
            return previous;
        }

        @Override
        public long previousIndex() {
            return previous != null ? previous.index : Long.MIN_VALUE;
        }

        @Override
        public String toString() {
            return index + "=" + value;
        }

        @Override
        public ElementNode trySplit() {
            return null;
        }

    }

    static final NumberContext PRECISION = NumberContext.of(16);

    private final TransformableRegion.FillByMultiplying<Double> myMultiplier;

    protected SparseR064(final int nbRows, final int nbCols) {
        super(R064Store.FACTORY, nbRows, nbCols);
        myMultiplier = MultiplyBoth.newPrimitive64(nbRows, nbCols);
    }

    @Override
    public final void add(final long row, final long col, final Comparable<?> addend) {
        this.add(row, col, Scalar.doubleValue(addend));

    }

    @Override
    public final void add(final long row, final long col, final double addend) {
        if (addend != ZERO) {
            ElementNode node = this.getNode((int) row, (int) col);
            node.value += addend;
            this.removeIfZero((int) row, (int) col, node);
        }
    }

    @Override
    public final void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        int complexity = Math.toIntExact(left.count() / this.countRows());

        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        myMultiplier.invoke(this.synchronised(), left, complexity, right);
    }

    @Override
    public final Double get(final int row, final int col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    /**
     * Always returns a node, creating one if necessary.
     */
    public abstract ElementNode getNode(final int row, final int col);

    public abstract ElementNode getNodeIfExists(final int row, final int col);

    @Override
    public final void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
        ElementNode node = this.getNode((int) row, (int) col);
        node.value = modifier.invoke(node.value);
        this.removeIfZero((int) row, (int) col, node);
    }

    @Override
    public final TransformableRegion<Double> regionByColumns(final int... columns) {
        return new Subregion2D.ColumnsRegion<>(this.synchronised(), myMultiplier, columns);
    }

    @Override
    public final TransformableRegion<Double> regionByLimits(final int rowLimit, final int columnLimit) {
        return new Subregion2D.LimitRegion<>(this.synchronised(), myMultiplier, rowLimit, columnLimit);
    }

    @Override
    public final TransformableRegion<Double> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new Subregion2D.OffsetRegion<>(this.synchronised(), myMultiplier, rowOffset, columnOffset);
    }

    @Override
    public final TransformableRegion<Double> regionByRows(final int... rows) {
        return new Subregion2D.RowsRegion<>(this.synchronised(), myMultiplier, rows);
    }

    @Override
    public final TransformableRegion<Double> regionByTransposing() {
        return new Subregion2D.TransposedRegion<>(this.synchronised(), myMultiplier);
    }

    @Override
    public final void set(final int row, final int col, final double value) {
        ElementNode node = this.getNode(row, col);
        node.value = value;
        this.removeIfZero(row, col, node);
    }

    @Override
    public final void set(final long row, final long col, final Comparable<?> value) {
        this.set(Math.toIntExact(row), Math.toIntExact(col), Scalar.doubleValue(value));
    }

    abstract void removeIfZero(final int row, final int col, final ElementNode node);

}
