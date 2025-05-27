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

import org.ojalgo.structure.Structure2D;

/**
 * Interface for sparse matrix implementations that store only non-zero elements.
 * <p>
 * This interface defines the contract for sparse matrix structures, which are optimized for storing matrices
 * where most elements are zero. Implementations typically use specialized data structures like Compressed
 * Sparse Row (CSR), Compressed Sparse Column (CSC), or Coordinate Format (COO) to efficiently store and
 * access non-zero elements.
 * </p>
 * <p>
 * The interface provides methods to:
 * <ul>
 * <li>Convert the sparse matrix to a list of {@link Triplet}s representing non-zero elements</li>
 * <li>Calculate the density of the matrix (ratio of non-zero elements to total elements)</li>
 * </ul>
 * </p>
 *
 * @author apete
 */
public interface SparseStructure2D extends Structure2D {

    int countNonzeros();

    /**
     * Returns the density of the matrix, defined as the ratio of non-zero elements to the total number of
     * elements in the matrix.
     *
     * @return The density of the matrix, between 0.0 and 1.0
     */
    default double density() {

        double totalCount = this.count();

        if (totalCount == ZERO) {
            return ZERO;
        } else {
            return this.countNonzeros() / totalCount;
        }
    }

    R064CSC toCSC();

    R064CSR toCSR();

}
