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
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.List;
import java.util.function.Supplier;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.structure.Access1D;

/**
 * @author apete
 */
abstract class TaskIterativeTests {

    static final boolean DEBUG = false;

    static final List<Supplier<Preconditioner>> PRECONDITIONERS = List.of(Preconditioner::newIdentity, Preconditioner::newJacobi,
            Preconditioner::newSymmetricGaussSeidel, Preconditioner.getSSOR(0.000001), Preconditioner.getSSOR(1.999999));

    /**
     * Utility to build the RHS vector b = A * x for tests.
     * <p>
     * Allocates a new dense column vector and performs a straightforward row-wise dot product to minimise
     * surprises and avoid any dependency on more elaborate multiplication paths (keeping test intent
     * explicit). Size mismatch results in an IllegalArgumentException.
     */
    static R064Store rhs(final MatrixStore<Double> A, final Access1D<?> x) {

        int rows = A.getRowDim();
        int cols = A.getColDim();
        if (x.size() != cols) {
            throw new IllegalArgumentException("Incompatible dimensions: A is " + rows + "x" + cols + ", x has length " + x.size());
        }

        R064Store b = R064Store.FACTORY.make(rows, 1);

        for (int i = 0; i < rows; i++) {
            double sum = ZERO;
            for (int j = 0; j < cols; j++) {
                double aij = A.doubleValue(i, j);
                if (aij != ZERO) { // skip obvious zeroes (many test matrices are sparse/structured)
                    sum += aij * x.doubleValue(j);
                }
            }
            b.set(i, sum);
        }

        return b;
    }

}