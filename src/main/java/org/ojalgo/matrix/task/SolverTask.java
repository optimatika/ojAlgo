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
package org.ojalgo.matrix.task;

import java.util.Optional;
import java.util.function.Supplier;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.Provider2D;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

public interface SolverTask<N extends Comparable<N>> extends MatrixTask<N> {

    public static abstract class Factory<N extends Comparable<N>> {

        public SolverTask<N> make(final int numberOfEquations, final int numberOfVariables, final int numberOfSolutions, final boolean symmetric,
                final boolean positiveDefinite) {

            Structure2D templateBody = new Structure2D() {

                @Override
                public int getColDim() {
                    return numberOfVariables;
                }

                @Override
                public int getRowDim() {
                    return numberOfEquations;
                }
            };

            Structure2D templateRHS = new Structure2D() {

                @Override
                public int getColDim() {
                    return numberOfSolutions;
                }

                @Override
                public int getRowDim() {
                    return numberOfEquations;
                }
            };

            return this.make(templateBody, templateRHS, symmetric, positiveDefinite);
        }

        public SolverTask<N> make(final MatrixStore<N> templateBody, final MatrixStore<N> templateRHS) {
            return this.make(templateBody, templateRHS, templateBody.isHermitian(), false);
        }

        public abstract SolverTask<N> make(Structure2D templateBody, Structure2D templateRHS, boolean symmetric, boolean positiveDefinite);

        /**
         * [A][X]=[B] or [body][return]=[rhs]
         */
        public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {
            return this.make(body, rhs, false, false).solve(body, rhs);
        }

    }

    Factory<ComplexNumber> C128 = new Factory<>() {

        @Override
        public SolverTask<ComplexNumber> make(final Structure2D templateBody, final Structure2D templateRHS, final boolean symmetric,
                final boolean positiveDefinite) {
            if (templateBody.isSquare()) {
                if (symmetric && positiveDefinite) {
                    return Cholesky.C128.make(templateBody);
                } else {
                    return LU.C128.make(templateBody);
                }
            } else if (templateBody.isTall()) {
                return QR.C128.make(templateBody);
            } else {
                return SingularValue.C128.make(templateBody);
            }
        }

    };

    Factory<Quaternion> H256 = new Factory<>() {

        @Override
        public SolverTask<Quaternion> make(final Structure2D templateBody, final Structure2D templateRHS, final boolean symmetric,
                final boolean positiveDefinite) {
            if (templateBody.isSquare()) {
                if (symmetric && positiveDefinite) {
                    return Cholesky.H256.make(templateBody);
                } else {
                    return LU.H256.make(templateBody);
                }
            } else if (templateBody.isTall()) {
                return QR.H256.make(templateBody);
            } else {
                return SingularValue.H256.make(templateBody);
            }
        }

    };

    Factory<RationalNumber> Q128 = new Factory<>() {

        @Override
        public SolverTask<RationalNumber> make(final Structure2D templateBody, final Structure2D templateRHS, final boolean symmetric,
                final boolean positiveDefinite) {
            if (templateBody.isSquare()) {
                if (symmetric && positiveDefinite) {
                    return Cholesky.Q128.make(templateBody);
                } else {
                    return LU.Q128.make(templateBody);
                }
            } else if (templateBody.isTall()) {
                return QR.Q128.make(templateBody);
            } else {
                return SingularValue.Q128.make(templateBody);
            }
        }

    };

    Factory<Double> R064 = new Factory<>() {

        @Override
        public SolverTask<Double> make(final Structure2D templateBody, final Structure2D templateRHS, final boolean symmetric, final boolean positiveDefinite) {

            boolean vectorRHS = templateRHS.countColumns() == 1L;

            long nbCols = templateBody.countColumns();

            if (templateBody.isSquare()) {
                if (symmetric) {
                    if (!vectorRHS) {
                        return positiveDefinite ? Cholesky.R064.make(templateBody) : LU.R064.make(templateBody);
                    } else if (nbCols == 1L) {
                        return AbstractSolver.FULL_1X1;
                    } else if (nbCols == 2L) {
                        return AbstractSolver.SYMMETRIC_2X2;
                    } else if (nbCols == 3L) {
                        return AbstractSolver.SYMMETRIC_3X3;
                    } else if (nbCols == 4L) {
                        return AbstractSolver.SYMMETRIC_4X4;
                    } else if (nbCols == 5L) {
                        return AbstractSolver.SYMMETRIC_5X5;
                    } else {
                        return positiveDefinite ? Cholesky.R064.make(templateBody) : LU.R064.make(templateBody);
                    }
                } else if (!vectorRHS) {
                    return LU.R064.make(templateBody);
                } else if (nbCols == 1L) {
                    return AbstractSolver.FULL_1X1;
                } else if (nbCols == 2L) {
                    return AbstractSolver.FULL_2X2;
                } else if (nbCols == 3L) {
                    return AbstractSolver.FULL_3X3;
                } else if (nbCols == 4L) {
                    return AbstractSolver.FULL_4X4;
                } else if (nbCols == 5L) {
                    return AbstractSolver.FULL_5X5;
                } else {
                    return LU.R064.make(templateBody);
                }
            } else if (!templateBody.isTall()) {
                return SingularValue.R064.make(templateBody);
            } else if (vectorRHS && nbCols <= 5) {
                return AbstractSolver.LEAST_SQUARES;
            } else {
                return QR.R064.make(templateBody);
            }
        }

    };

    Factory<Quadruple> R128 = new Factory<>() {

        @Override
        public SolverTask<Quadruple> make(final Structure2D templateBody, final Structure2D templateRHS, final boolean symmetric,
                final boolean positiveDefinite) {
            if (templateBody.isSquare()) {
                if (symmetric && positiveDefinite) {
                    return Cholesky.R128.make(templateBody);
                } else {
                    return LU.R128.make(templateBody);
                }
            } else if (templateBody.isTall()) {
                return QR.R128.make(templateBody);
            } else {
                return SingularValue.R128.make(templateBody);
            }
        }

    };

    PhysicalStore<N> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions);

    /**
     * <p>
     * Will create a {@linkplain PhysicalStore} instance suitable for use with
     * {@link #solve(Access2D, Access2D, PhysicalStore)}. The dimensions of the returned instance is not
     * specified by this interface - it is specified by the behaviour/requirements of each implementation.
     * </p>
     * <p>
     * When solving an equation system [A][X]=[B] ([mxn][nxb]=[mxb]) the preallocated memory/matrix will
     * typically be either mxb or nxb.
     * </p>
     */
    default PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {

        int nbEquations = templateBody.getRowDim();
        int nbVariables = templateBody.getColDim();
        int nbSolutions = templateRHS.getColDim();

        if (templateRHS.getRowDim() != nbEquations) {
            throw new IllegalArgumentException();
        }

        return this.preallocate(nbEquations, nbVariables, nbSolutions);
    }

    /**
     * [A][X]=[B] or [body][return]=[rhs]
     */
    default MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {
        return this.solve(body, rhs, this.preallocate(body, rhs));
    }

    /**
     * <p>
     * Exactly how (if at all) a specific implementation makes use of <code>preallocated</code> is not
     * specified by this interface. It must be documented for each implementation.
     * </p>
     * <p>
     * Should produce the same results as calling {@link #solve(Access2D, Access2D)}.
     * </p>
     * <p>
     * Use {@link #preallocate(Structure2D, Structure2D)} to obtain a suitbale <code>preallocated</code>.
     * </p>
     *
     * @param rhs          The Right Hand Side, wont be modfied
     * @param preallocated Preallocated memory for the results, possibly some intermediate results. You must
     *                     assume this is modified, but you cannot assume it will contain the full/ /correct
     *                     solution.
     * @return The solution
     */
    MatrixStore<N> solve(Access2D<?> body, Access2D<?> rhs, PhysicalStore<N> preallocated) throws RecoverableCondition;

    default Provider2D.Solution<Optional<MatrixStore<N>>> toSolutionProvider(final ElementsSupplier<N> body,
            final Supplier<MatrixStore<N>> alternativeBodySupplier, final Access2D<?> rhs) {
        try {
            MatrixStore<N> solution = this.solve(alternativeBodySupplier.get(), rhs);
            return r -> Optional.of(solution);
        } catch (RecoverableCondition cause) {
            return r -> Optional.empty();
        }
    }

}
