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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.math.MathContext;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
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
abstract class IterativeASS extends ActiveSetSolver {

    final class MyIterativeSolver extends MutableSolver<ConjugateGradientSolver> implements Access2D<Double> {

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
        }

        @Override
        public long countColumns() {
            return IterativeASS.this.countEqualityConstraints() + myActivator.countIncluded();
        }

        @Override
        public long countRows() {
            return IterativeASS.this.countEqualityConstraints() + myActivator.countIncluded();
        }

        public double doubleValue(final long row, final long col) {

            int tmpColumn = (int) col;

            if (tmpColumn >= myCountE) {
                tmpColumn = myCountE + myActivator.getIncluded()[tmpColumn - myCountE];
            }

            return this.doubleValue((int) row, tmpColumn);
        }

        public Double get(final long row, final long col) {
            return this.doubleValue(row, col);
        }

        void add(final int j, final Access1D<Double> column, final double rhs, final int numberOfNonzeros) {

            final int[] myIncluded = myActivator.getIncluded();

            final Equation tmpNewRow = new Equation(j, myFullDim, rhs, numberOfNonzeros);
            myIterationRows[j] = tmpNewRow;
            this.add(tmpNewRow);

            if (IterativeASS.this.getAE() != null) {

                final PhysicalStore<Double> tmpProdE = IterativeASS.this.getAE().factory().makeZero(IterativeASS.this.getAE().countRows(), 1L);
                IterativeASS.this.getAE().multiply(column, tmpProdE);

                for (int i = 0; i < myCountE; i++) {
                    final double tmpVal = tmpProdE.doubleValue(i);
                    if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                        final Equation tmpRowE = myIterationRows[i];
                        if (tmpRowE != null) {
                            tmpRowE.set(j, tmpVal);
                        }
                        tmpNewRow.set(i, tmpVal);
                    }
                }
            }

            if ((IterativeASS.this.getAI() != null) && (myIncluded.length > 0)) {

                final PhysicalStore<Double> tmpProdI = IterativeASS.this.getAI().factory().makeZero(myIncluded.length, 1L);
                IterativeASS.this.getAI().logical().row(myIncluded).get().multiply(column, tmpProdI);

                for (int _i = 0; _i < myIncluded.length; _i++) {
                    final double tmpVal = tmpProdI.doubleValue(_i);
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

            tmpNewRow.initialise(myIterationL);

        }

        void remove(final int i) {

            final Equation tmpO = myIterationRows[i];
            if (tmpO != null) {
                this.remove(tmpO);
            }
            myIterationRows[i] = null;

            myIterationL.set(i, ZERO);
        }

    }

    private final MyIterativeSolver myS;

    IterativeASS(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        myS = new MyIterativeSolver();
    }

    @Override
    protected void performIteration() {

        if (this.isDebug()) {
            this.debug("\nPerformIteration {}", 1 + this.countIterations());
            this.debug(myActivator.toString());
        }

        final int tmpToInclude = myConstraintToInclude;
        myConstraintToInclude = -1;
        final int[] tmpIncluded = myActivator.getIncluded();

        final MatrixStore<Double> tmpIterC = this.getIterationC();
        final MatrixStore<Double> tmpIterA = this.getIterationA(tmpIncluded);

        final long tmpCountRowsIterA = tmpIterA.countRows();
        final long tmpCountColsIterA = tmpIterA.countColumns();

        boolean tmpSolvable = false;

        final int tmpCountE = this.countEqualityConstraints();

        if (tmpToInclude >= 0) {
            final MatrixStore<Double> tmpElements = myCholesky.solve(this.getAI().logical().row(tmpToInclude).transpose());
            final double tmpRHS = myInvQC.premultiply(this.getAI().sliceRow(tmpToInclude, 0L)).get().doubleValue(0L) - this.getBI().doubleValue(tmpToInclude);
            myS.add(tmpCountE + tmpToInclude, tmpElements, tmpRHS, 3);
        }

        if ((tmpCountRowsIterA < tmpCountColsIterA) && (tmpSolvable = myCholesky.isSolvable())) {
            // Q is SPD

            if (tmpCountRowsIterA == 0L) {
                // Unconstrained - can happen when PureASS and all inequalities are inactive

                myCholesky.solve(tmpIterC, myIterationX);

            } else {
                // Actual/normal optimisation problem

                final double tmpRelativeError = myS.resolve(myIterationL);

                if (this.isDebug()) {
                    this.debug("Relative error in solution for L={}", tmpRelativeError);
                    // this.debug("Iteration L", this.getIterationL(tmpIncluded));
                }

                myCholesky.solve(this.getIterationL(tmpIncluded).premultiply(tmpIterA.transpose()).operateOnMatching(tmpIterC, SUBTRACT), myIterationX);
            }
        }

        if (!tmpSolvable && (tmpSolvable = myLU.compute(this.getIterationKKT(tmpIncluded)))) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = myLU.solve(this.getIterationRHS(tmpIncluded));
            final int tmpCountVariables = this.countVariables();
            myIterationX.fillMatching(tmpXL.logical().limits(tmpCountVariables, (int) tmpXL.countColumns()).get());

            for (int i = 0; i < tmpCountE; i++) {
                myIterationL.set(i, tmpXL.doubleValue(tmpCountVariables + i));
            }
            final int tmpLengthIncluded = tmpIncluded.length;
            for (int i = 0; i < tmpLengthIncluded; i++) {
                myIterationL.set(tmpCountE + tmpIncluded[i], tmpXL.doubleValue(tmpCountVariables + tmpCountE + i));
            }

        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            if ((this.countVariables() + tmpCountColsIterA) < 20) {
                options.debug_appender.printmtrx("KKT", this.getIterationKKT());
                options.debug_appender.printmtrx("RHS", this.getIterationRHS());
            }
        }

        this.handleSubsolution(tmpSolvable, myIterationX, tmpIncluded);

        // BasicLogger.debug("Iteration L: {}", myIterationL.asList().copy());
    }

    @Override
    void excludeAndRemove(final int toExclude) {
        myActivator.exclude(toExclude);
        myS.remove(this.countEqualityConstraints() + toExclude);
    }

    @Override
    void initSolution(final MatrixStore<Double> tmpBI, final int tmpNumVars, final int tmpNumEqus) {

        if (this.hasInequalityConstraints()) {

            final int[] tmpExcluded = myActivator.getExcluded();

            final MatrixStore<Double> tmpAIX = this.getAIX(tmpExcluded);
            for (int i = 0; i < tmpExcluded.length; i++) {
                final double tmpBody = tmpAIX.doubleValue(i);
                final double tmpRHS = tmpBI.doubleValue(tmpExcluded[i]);
                if (!options.slack.isDifferent(tmpRHS, tmpBody) && (myIterationL.doubleValue(tmpNumEqus + tmpExcluded[i]) != ZERO)) {
                    myActivator.include(tmpExcluded[i]);
                }
            }
        }

        while (((tmpNumEqus + myActivator.countIncluded()) >= tmpNumVars) && (myActivator.countIncluded() > 0)) {
            this.shrink();
        }

        if (this.isDebug() && ((tmpNumEqus + myActivator.countIncluded()) > tmpNumVars)) {
            this.debug("Redundant contraints!");
        }

        myInvQC = myCholesky.solve(this.getIterationC());

        final int[] tmpIncluded = myActivator.getIncluded();

        myS.clear();

        if ((tmpNumEqus + tmpIncluded.length) > 0) {

            final MatrixStore<Double> tmpIterA = this.getIterationA(tmpIncluded);
            final MatrixStore<Double> tmpIterB = this.getIterationB(tmpIncluded);

            final MatrixStore<Double> tmpCols = myCholesky.solve(tmpIterA.transpose());
            final MatrixStore<Double> tmpRHS = myInvQC.premultiply(tmpIterA).operateOnMatching(SUBTRACT, tmpIterB).get();

            for (int j = 0; j < tmpNumEqus; j++) {
                myS.add(j, tmpCols.sliceColumn(0, j), tmpRHS.doubleValue(j), tmpNumVars);
            }
            for (int j = 0; j < tmpIncluded.length; j++) {
                myS.add(tmpNumEqus + tmpIncluded[j], tmpCols.sliceColumn(0, tmpNumEqus + j), tmpRHS.doubleValue(tmpNumEqus + j), 3);
            }
        }
    }

}
