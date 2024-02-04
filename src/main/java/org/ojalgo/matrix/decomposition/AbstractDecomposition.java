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
package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.MACHINE_EPSILON;

import org.ojalgo.function.FunctionSet;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.Scalar;

/**
 * @author apete
 */
abstract class AbstractDecomposition<N extends Comparable<N>> implements MatrixDecomposition<N> {

    private boolean myComputed = false;
    private Boolean mySolvable = null;

    AbstractDecomposition() {
        super();
    }

    public final long countColumns() {
        return this.getColDim();
    }

    public final long countRows() {
        return this.getRowDim();
    }

    public final boolean isComputed() {
        return myComputed;
    }

    public void reset() {
        myComputed = false;
        mySolvable = null;
    }

    protected abstract PhysicalStore<N> allocate(long numberOfRows, long numberOfColumns);

    protected boolean checkSolvability() {
        return false;
    }

    protected final boolean computed(final boolean computed) {
        return myComputed = computed;
    }

    protected abstract FunctionSet<N> function();

    protected final double getDimensionalEpsilon() {
        return this.getMaxDim() * MACHINE_EPSILON;
    }

    protected final boolean isAspectRatioNormal() {
        return this.getRowDim() >= this.getColDim();
    }

    protected abstract Scalar.Factory<N> scalar();

    boolean isSolvable() {
        if (myComputed && mySolvable == null) {
            if (this instanceof MatrixDecomposition.Solver) {
                mySolvable = Boolean.valueOf(this.checkSolvability());
            } else {
                mySolvable = Boolean.FALSE;
            }
        }
        return myComputed && mySolvable != null && mySolvable.booleanValue();
    }

}
