/*
 * Copyright 1997-2017 Optimatika
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
package org.ojalgo.matrix.transformation;

import org.ojalgo.matrix.store.PhysicalStore;

/**
 * Represents an in-place matrix transformation – the matrix/vector operated on is mutated. A
 * MatrixTransformation instance represents an implied transformation matrix (to be used as the input argument
 * to the {@link PhysicalStore#multiply(org.ojalgo.matrix.store.MatrixStore)} or
 * {@link PhysicalStore#premultiply(org.ojalgo.access.Access1D)} methods). But, this interface does not
 * require you to disclose the size and shape of that matrix, nor to be able to explicitly access any of the
 * individual elements.
 *
 * @author apete
 */
@FunctionalInterface
public interface MatrixTransformation<N extends Number> {

    void transform(PhysicalStore<N> matrix);

}
