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

import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure2D;

/**
 * Additional methods for sparse matrix implementations that store only non-zero elements.
 * <p>
 * This interface defines common functionality for sparse matrix structures, which are optimized for storing
 * matrices where most elements are zero. Implementations typically use specialized data structures like
 * Compressed Sparse Row (CSR), Compressed Sparse Column (CSC), or Coordinate Format (COO) to efficiently
 * store and access non-zero elements.
 * </p>
 * <p>
 * The interface provides methods to:
 * <ul>
 * <li>Count the number of non-zero elements in the matrix</li>
 * <li>Calculate the density of the matrix (ratio of non-zero elements to total elements)</li>
 * <li>Convert the sparse matrix to specific sparse formats (CSR, CSC)</li>
 * </ul>
 * </p>
 * <p>
 * Note: This interface extends {@link Structure2D} and provides additional sparse-specific functionality. The
 * main matrix functionality is defined in other interfaces that implement this one.
 * </p>
 *
 * @author apete
 */
public interface SparseStructure2D extends Structure2D, Structure1D.Sparse {

    R064CSC toCSC();

    R064CSR toCSR();

}
