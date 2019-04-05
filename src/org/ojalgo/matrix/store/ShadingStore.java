/*
 * Copyright 1997-2019 Optimatika
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

/**
 * Does not change the matrix size/shape, but applies some structure to the elements. Shaded elements are
 * assumed to be zero.
 *
 * @author apete
 */
abstract class ShadingStore<N extends Number> extends LogicalStore<N> {

    protected ShadingStore(final MatrixStore<N> base, final int rowsCount, final int columnsCount) {
        super(base, rowsCount, columnsCount);
    }

    public void supplyTo(final ElementsConsumer<N> consumer) {

        consumer.reset();

        final int tmpColDim = this.getColDim();

        if (this.isPrimitive()) {

            for (int j = 0; j < tmpColDim; j++) {
                final int tmpFirst = this.firstInColumn(j);
                final int tmpLimit = this.limitOfColumn(j);
                for (int i = tmpFirst; i < tmpLimit; i++) {
                    consumer.set(i, j, this.doubleValue(i, j));
                }
            }

        } else {

            for (int j = 0; j < tmpColDim; j++) {
                final int tmpFirst = this.firstInColumn(j);
                final int tmpLimit = this.limitOfColumn(j);
                for (int i = tmpFirst; i < tmpLimit; i++) {
                    consumer.fillOne(i, j, this.get(i, j));
                }
            }
        }

    }

}
