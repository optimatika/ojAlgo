/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.optimisation;

import org.ojalgo.array.SparseArray;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.structure.Access2D.RowView;

/**
 * Should be able to provide data for any problem solvable by ojAlgo solvers.
 *
 * @author apete
 * @deprecated Use {@link Optimisation.SolverData} instead.
 */
@Deprecated
public interface OptimisationData<N extends Comparable<N>> extends Optimisation.SolverData<N> {

    /**
     * Equality constraints body: [AE][X] == [BE]
     */
    MatrixStore<N> getAE();

    SparseArray<N> getAE(int row);

    RowsSupplier<N> getAE(int... rows);

    /**
     * Inequality constraints body: [AI][X] <= [BI]
     */
    MatrixStore<N> getAI();

    SparseArray<N> getAI(int row);

    RowsSupplier<N> getAI(int... rows);

    /**
     * Equality constraints RHS: [AE][X] == [BE]
     */
    MatrixStore<N> getBE();

    double getBE(int row);

    /**
     * Inequality constraints RHS: [AI][X] <= [BI]
     */
    MatrixStore<N> getBI();

    double getBI(int row);

    default MultiaryFunction.TwiceDifferentiable<N> getObjective() {
        return this.getObjective(MultiaryFunction.TwiceDifferentiable.class);
    }

    <T extends MultiaryFunction.TwiceDifferentiable<N>> T getObjective(Class<T> type);

    RowView<N> getRowsAE();

    RowView<N> getRowsAI();

    void reset();

}
