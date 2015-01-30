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
package org.ojalgo.matrix.task;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

public interface SolverTask<N extends Number> extends MatrixTask<N> {

    public static abstract class Factory<N extends Number> {

        public final SolverTask<N> make(final MatrixStore<N> templateBody, final MatrixStore<N> templateRhs) {
            return this.make(templateBody, templateRhs, MatrixUtils.isHermitian(templateBody));
        }

        public abstract SolverTask<N> make(MatrixStore<N> templateBody, MatrixStore<N> templateRhs, boolean symmetric);

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        @Override
        public SolverTask<BigDecimal> make(final MatrixStore<BigDecimal> templateBody, final MatrixStore<BigDecimal> templateRhs, final boolean symmetric) {
            if (symmetric) {
                return Cholesky.make(templateBody);
            } else if (templateBody.countRows() == templateBody.countColumns()) {
                return LU.make(templateBody);
            } else if (templateBody.countRows() >= templateBody.countColumns()) {
                return QR.make(templateBody);
            } else {
                return SingularValue.make(templateBody);
            }
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        @Override
        public SolverTask<ComplexNumber> make(final MatrixStore<ComplexNumber> templateBody, final MatrixStore<ComplexNumber> templateRhs,
                final boolean symmetric) {
            if (symmetric) {
                return Cholesky.make(templateBody);
            } else if (templateBody.countRows() == templateBody.countColumns()) {
                return LU.make(templateBody);
            } else if (templateBody.countRows() >= templateBody.countColumns()) {
                return QR.make(templateBody);
            } else {
                return SingularValue.make(templateBody);
            }
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        @Override
        public SolverTask<Double> make(final MatrixStore<Double> templateBody, final MatrixStore<Double> templateRhs, final boolean symmetric) {
            if (symmetric) {
                final long tmpDim = templateBody.countColumns();
                if (tmpDim == 1l) {
                    return AbstractSolver.FULL_1X1;
                } else if (tmpDim == 2l) {
                    return AbstractSolver.SYMMETRIC_2X2;
                } else if (tmpDim == 3l) {
                    return AbstractSolver.SYMMETRIC_3X3;
                } else if (tmpDim == 4l) {
                    return AbstractSolver.SYMMETRIC_4X4;
                } else if (tmpDim == 5l) {
                    return AbstractSolver.SYMMETRIC_5X5;
                } else {
                    return Cholesky.make(templateBody);
                }
            } else {
                final long tmpDim = templateBody.countColumns();
                if (templateBody.countRows() == tmpDim) {
                    if (tmpDim == 1l) {
                        return AbstractSolver.FULL_1X1;
                    } else if (tmpDim == 2l) {
                        return AbstractSolver.FULL_2X2;
                    } else if (tmpDim == 3l) {
                        return AbstractSolver.FULL_3X3;
                    } else if (tmpDim == 4l) {
                        return AbstractSolver.FULL_4X4;
                    } else if (tmpDim == 5l) {
                        return AbstractSolver.FULL_5X5;
                    } else {
                        return LU.make(templateBody);
                    }
                } else if (templateBody.countRows() >= tmpDim) {
                    if (tmpDim <= 5) {
                        return AbstractSolver.LEAST_SQUARES;
                    } else {
                        return QR.make(templateBody);
                    }
                } else {
                    return SingularValue.make(templateBody);
                }
            }
        }

    };

    /**
     * <p>
     * Implementiong this method is optional.
     * </p>
     * Will create a {@linkplain DecompositionStore} instance suitable for use with
     * {@link #solve(Access2D, DecompositionStore)}. When solving an equation system [A][X]=[B] ([mxn][nxb]=[mxb]) the
     * preallocated memory/matrix will typically be either mxb or nxb (if A is square then there is no doubt).
     * 
     * @param templateBody
     * @param templateRHS
     * @return
     * @throws UnsupportedOperationException When/if this feature is not implemented
     */
    DecompositionStore<N> preallocate(Access2D<N> templateBody, Access2D<N> templateRHS);

    /**
     * [A][X]=[B] or [this][return]=[aRHS]
     */
    MatrixStore<N> solve(Access2D<N> body, Access2D<N> rhs) throws TaskException;

    /**
     * <p>
     * Implementiong this method is optional.
     * </p>
     * <p>
     * Exactly how a specific implementation makes use of <code>preallocated</code> is not specified by this interface.
     * It must be documented for each implementation.
     * </p>
     * <p>
     * Should produce the same results as calling {@link #solve(Access2D)}.
     * </p>
     * 
     * @param rhs The Right Hand Side, wont be modfied
     * @param preallocated Preallocated memory for the results, possibly some intermediate results. You must assume this
     *        is modified, but you cannot assume it will contain the full/final/correct solution.
     * @return The solution
     * @throws UnsupportedOperationException When/if this feature is not implemented
     */
    MatrixStore<N> solve(Access2D<N> body, Access2D<N> rhs, DecompositionStore<N> preallocated) throws TaskException;
}
