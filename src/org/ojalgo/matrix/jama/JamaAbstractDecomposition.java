/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.jama;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.type.context.NumberContext;

/**
 * JamaAbstractDecomposition
 * 
 * @author apete
 */
abstract class JamaAbstractDecomposition implements MatrixDecomposition<Double> {

    static Matrix cast(final Access2D<?> aStore) {
        if (aStore instanceof JamaMatrix) {
            return ((JamaMatrix) aStore).getDelegate();
        } else {
            return new Matrix(ArrayUtils.toRawCopyOf(aStore));
        }
    }

    protected JamaAbstractDecomposition() {
        super();
    }

    public final boolean compute(final Access2D<?> aStore) {

        this.reset();

        return this.compute(JamaAbstractDecomposition.cast(aStore));
    }

    public final boolean equals(final MatrixDecomposition<Double> other, final NumberContext context) {
        return AccessUtils.equals(this.reconstruct(), other.reconstruct(), context);
    }

    public abstract JamaMatrix getInverse();

    /**
     * Makes no use of <code>preallocated</code> at all. Simply delegates to {@link #getInverse()}.
     * 
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition#getInverse(org.ojalgo.matrix.decomposition.DecompositionStore)
     */
    public final MatrixStore<Double> getInverse(final DecompositionStore<Double> preallocated) {
        return this.getInverse();
    }

    public final MatrixStore<Double> invert(final MatrixStore<Double> original) {
        this.compute(original);
        return this.getInverse();
    }

    public final MatrixStore<Double> invert(final MatrixStore<Double> original, final DecompositionStore<Double> preallocated) {
        this.compute(original);
        return this.getInverse(preallocated);
    }

    public final DecompositionStore<Double> preallocate(final Access2D<Double> template) {
        return this.preallocate(template, template);
    }

    public final DecompositionStore<Double> preallocate(final Access2D<Double> templateBody, final Access2D<Double> templateRHS) {
        return null;
    }

    public JamaMatrix solve(final Access2D<Double> rhs) {
        return new JamaMatrix(this.solve(JamaAbstractDecomposition.cast(rhs)));
    }

    public final MatrixStore<Double> solve(final Access2D<Double> body, final Access2D<Double> rhs) {
        this.compute(body);
        return this.solve(rhs);
    }

    public final MatrixStore<Double> solve(final Access2D<Double> body, final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {
        this.compute(body);
        return this.solve(rhs, preallocated);
    }

    /**
     * Makes no use of <code>preallocated</code> at all. Simply delegates to {@link #solve(Access2D)}.
     * 
     * @see org.ojalgo.matrix.decomposition.MatrixDecomposition#solve(Access2D,
     *      org.ojalgo.matrix.decomposition.DecompositionStore)
     */
    public final JamaMatrix solve(final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {
        return this.solve(rhs);
    }

    protected JamaMatrix makeEyeStore(final int aRowDim, final int aColDim) {
        return new JamaMatrix(Matrix.identity(aRowDim, aColDim));
    }

    abstract boolean compute(Matrix aDelegate);

    abstract Matrix solve(Matrix aRHS);
}
