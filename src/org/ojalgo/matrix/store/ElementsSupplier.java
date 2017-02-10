/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.util.function.Supplier;

import org.ojalgo.access.Stream2D;

/**
 * An elements supplier is not (yet) a matrix. There are 4 things you can do with them:
 * <ol>
 * <li>You can query the size/shape of the (future) matrix</li>
 * <li>You can get that matrix</li>
 * <li>You can supply the elements to an already existing matrix (or more precisely to an
 * {@linkplain ElementsConsumer})</li>
 * <li>You can also define additional, chained, operations to be executed when the elements are extracted.
 * </li>
 * </ol>
 *
 * @author apete
 */
public interface ElementsSupplier<N extends Number> extends Stream2D<N, MatrixStore<N>, ElementsConsumer<N>, ElementsSupplier<N>>, Supplier<MatrixStore<N>> {

}
