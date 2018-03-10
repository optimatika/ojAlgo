/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.math.MathContext;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.matrix.task.iterative.Equation;
import org.ojalgo.matrix.task.iterative.MutableSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.context.NumberContext;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] &lt;= [BI]
 * </p>
 * Where [AE] and [BE] are optinal.
 *
 * @author apete
 */
final class IterativeASS extends ActiveSetSolver {

    final class MyIterativeSolver extends MutableSolver<ConjugateGradientSolver> implements Access2D<Double> {

        private final PhysicalStore<Double> myColumnE;
        private final int myCountE = IterativeASS.this.countEqualityConstraints();
        private final long myFullDim = myCountE + IterativeASS.this.countInequalityConstraints();
        private final Equation[] myIterationRows;

        MyIterativeSolver() {

            super(new ConjugateGradientSolver(), IterativeASS.this.countEqualityConstraints() + IterativeASS.this.countInequalityConstraints());

            // GaussSeidel
            //this.setTerminationContext(NumberContext.getMath(MathContext.DECIMAL64).newPrecision(13));
            //this.getDelegate().setRelaxationFactor(1.5);

            // ConjugateGradient
            this.setAccuracyContext(NumberContext.getMath(MathContext.DECIMAL64).newPrecision(9));

            myIterationRows = new Equation[(int) myFullDim];

            myColumnE = PrimitiveDenseStore.FACTORY.makeZero(myCountE, 1);
        }

        @Override
        public long countColumns() {
            return IterativeASS.this.countEqualityConstraints() + IterativeASS.this.countIncluded();
        }

        @Override
        public long countRows() {
            return IterativeASS.this.countEqualityConstraints() + IterativeASS.this.countIncluded();
        }

        public double doubleValue(final long row, final long col) {

            int tmpColumn = (int) col;

            if (tmpColumn >= myCountE) {
                tmpColumn = myCountE + IterativeASS.this.getIncluded()[tmpColumn - myCountE];
            }

            return this.doubleValue((int) row, tmpColumn);
        }

        public Double get(final long row, final long col) {
            return this.doubleValue(row, col);
        }

        void add(final int j, final Access1D<Double> column, final double rhs, final int numberOfNonzeros) {

            final int[] myIncluded = IterativeASS.this.getIncluded();

            final Equation tmpNewRow = new Equation(j, myFullDim, rhs, numberOfNonzeros);
            myIterationRows[j] = tmpNewRow;
            this.add(tmpNewRow);

            if (myCountE > 0) {

                IterativeASS.this.getMatrixAE().multiply(column, myColumnE);

                for (int i = 0; i < myCountE; i++) {
                    final double tmpVal = myColumnE.doubleValue(i);
                    if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                        final Equation tmpRowE = myIterationRows[i];
                        if (tmpRowE != null) {
                            tmpRowE.set(j, tmpVal);
                        }
                        tmpNewRow.set(i, tmpVal);
                    }
                }
            }

            if (IterativeASS.this.countIncluded() > 0) {

                //                final PhysicalStore<Double> tmpProdI = PrimitiveDenseStore.FACTORY.makeZero(myIncluded.length, 1L);
                //                IterativeASS.this.getMatrixAI(myIncluded).get().multiply(column, tmpProdI);

                for (int _i = 0; _i < myIncluded.length; _i++) {
                    // final double tmpVal = tmpProdI.doubleValue(_i);
                    final double tmpVal = IterativeASS.this.getMatrixAI(myIncluded[_i]).dot(column);
                    if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                        final int i = myCountE + myIncluded[_i];
                        final Equation tmpRowI = myIterationRows[i];
                        if (tmpRowI != null) {
                            tmpRowI.set(j, tmpVal);
                        }
                        tmpNewRow.set(i, tmpVal);
                    }
                }

            }

            tmpNewRow.initialise(IterativeASS.this.getSolutionL());
        }

        void remove(final int i) {

            final Equation tmpO = myIterationRows[i];
            if (tmpO != null) {
                this.remove(tmpO);
            }
            myIterationRows[i] = null;

            IterativeASS.this.getSolutionL().set(i, ZERO);
        }

    }

    private final PhysicalStore<Double> myColumnS;
    private final MyIterativeSolver myS;

    IterativeASS(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        myS = new MyIterativeSolver();
        myColumnS = PrimitiveDenseStore.FACTORY.makeZero(this.countVariables(), 1);
    }

    private void addConstraint(final int constrIndex, final Access1D<?> constrBody, final double constrRHS) {

        final MatrixStore<Double> body = this.getSolutionQ(Access2D.newPrimitiveColumnCollectable(constrBody), myColumnS);

        final double rhs = constrBody.dot(this.getInvQC()) - constrRHS;

        myS.add(constrIndex, body, rhs, 3);
    }

    @Override
    protected void exclude(final int toExclude) {
        super.exclude(toExclude);
        myS.remove(this.countEqualityConstraints() + toExclude);
    }

    @Override
    protected void performIteration() {

        if (this.isDebug()) {
            this.log("\nPerformIteration {}", 1 + this.countIterations());
            this.log(this.toActivatorString());
        }

        final int toInclude = this.getConstraintToInclude();
        this.setConstraintToInclude(-1);
        final int[] incl = this.getIncluded();
        final int[] excl = this.getExcluded();

        boolean solved = false;

        if (toInclude >= 0) {

            final int constrIndex = this.countEqualityConstraints() + toInclude;
            final SparseArray<Double> constrBody = this.getMatrixAI(toInclude);
            final double constrRHS = this.getMatrixBI(toInclude);

            this.addConstraint(constrIndex, constrBody, constrRHS);
        }

        final PrimitiveDenseStore iterX = this.getIterationX();

        if ((this.countIterationConstraints() < this.countVariables()) && (solved = this.isSolvableQ())) {
            // Q is SPD

            if (this.countIterationConstraints() == 0L) {
                // Unconstrained - can happen when there are no equality constraints and all inequalities are inactive

                iterX.fillMatching(this.getInvQC());

            } else {
                // Actual/normal optimisation problem

                final double tmpRelativeError = myS.resolve(this.getSolutionL());

                if (this.isDebug()) {
                    this.log("Relative error {} in solution for L={}", tmpRelativeError, this.getIterationL(incl));
                }

                final ElementsSupplier<Double> tmpRHS = this.getIterationL(incl).premultiply(this.getIterationA().transpose())
                        .operateOnMatching(this.getIterationC(), SUBTRACT);
                this.getSolutionQ(tmpRHS, iterX);
            }
        }

        if (!solved) {
            // The above failed, try solving the full KKT system instaed

            final PrimitiveDenseStore tmpXL = PrimitiveDenseStore.FACTORY.makeZero(this.countVariables() + this.countIterationConstraints(), 1L);

            if (solved = this.solveFullKKT(tmpXL)) {

                iterX.fillMatching(tmpXL.logical().limits(this.countVariables(), 1).get());

                for (int i = 0; i < this.countEqualityConstraints(); i++) {
                    this.getSolutionL().set(i, tmpXL.doubleValue(this.countVariables() + i));
                }
                final int tmpLengthIncluded = incl.length;
                for (int i = 0; i < tmpLengthIncluded; i++) {
                    this.getSolutionL().set(this.countEqualityConstraints() + incl[i],
                            tmpXL.doubleValue(this.countVariables() + this.countEqualityConstraints() + i));
                }
            }
        }

        this.handleIterationResults(solved, iterX, incl, excl);
    }

    @Override
    void resetActivator() {

        super.resetActivator();

        final int numbEqus = this.countEqualityConstraints();
        final int numbVars = this.countVariables();

        myS.clear();
        final int[] incl = this.getIncluded();

        if ((numbEqus + incl.length) > 0) {

            final MatrixStore<Double> iterA = this.getIterationA();
            final MatrixStore<Double> iterB = this.getIterationB();

            final MatrixStore<Double> tmpCols = this.getSolutionQ(iterA.transpose());
            final MatrixStore<Double> tmpRHS = this.getInvQC().premultiply(iterA).operateOnMatching(SUBTRACT, iterB).get();

            for (int j = 0; j < numbEqus; j++) {
                myS.add(j, tmpCols.sliceColumn(j), tmpRHS.doubleValue(j), numbVars);
            }
            for (int j = 0; j < incl.length; j++) {
                myS.add(numbEqus + incl[j], tmpCols.sliceColumn(numbEqus + j), tmpRHS.doubleValue(numbEqus + j), 3);
            }
        }

    }

}
