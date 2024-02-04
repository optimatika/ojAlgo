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

/**
 * Does not change the matrix size/shape, but applies some structure to the elements. Shaded elements are
 * assumed to be zero.
 *
 * @author apete
 */
abstract class ShadingStore<N extends Comparable<N>> extends LogicalStore<N> {

    protected ShadingStore(final MatrixStore<N> base) {
        super(base, base.getRowDim(), base.getColDim());
    }

    @Override
    public void supplyTo(final TransformableRegion<N> consumer) {

        consumer.reset();

        int numberOfColumns = this.getColDim();

        if (this.isPrimitive()) {

            for (int j = 0; j < numberOfColumns; j++) {
                int first = this.firstInColumn(j);
                int limit = this.limitOfColumn(j);
                for (int i = first; i < limit; i++) {
                    consumer.set(i, j, this.doubleValue(i, j));
                }
            }

        } else {

            for (int j = 0; j < numberOfColumns; j++) {
                int first = this.firstInColumn(j);
                int limit = this.limitOfColumn(j);
                for (int i = first; i < limit; i++) {
                    consumer.fillOne(i, j, this.get(i, j));
                }
            }
        }
    }

}
