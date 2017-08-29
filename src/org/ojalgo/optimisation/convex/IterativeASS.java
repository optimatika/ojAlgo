/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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

            if (IterativeASS.this.getMatrixAE() != null) {

                final PhysicalStore<Double> tmpProdE = IterativeASS.this.getMatrixAE().physical().makeZero(IterativeASS.this.getMatrixAE().countRows(), 1L);
                IterativeASS.this.getMatrixAE().multiply(column, tmpProdE);

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

            if (IterativeASS.this.countIncluded() > 0) {

                final PhysicalStore<Double> tmpProdI = PrimitiveDenseStore.FACTORY.makeZero(myIncluded.length, 1L);
                IterativeASS.this.getMatrixAI(myIncluded).get().multiply(column, tmpProdI);

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

            tmpNewRow.initialise(IterativeASS.this.getL());

        }

        void remove(final int i) {

            final Equation tmpO = myIterationRows[i];
            if (tmpO != null) {
                this.remove(tmpO);
            }
            myIterationRows[i] = null;

            IterativeASS.this.getL().set(i, ZERO);
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
            this.debug(this.toActivatorString());
        }

        final int tmpToInclude = this.getConstraintToInclude();
        this.setConstraintToInclude(-1);
        final int[] tmpIncluded = this.getIncluded();

        boolean tmpSolvable = false;

        if (tmpToInclude >= 0) {

            // final LogicalBuilder<Double> rowAlt1 = this.getMatrixAI().logical().row(tmpToInclude);
            final Access1D<Double> rowAlt2 = this.getMatrixAI(tmpToInclude);

            // final LogicalBuilder<Double> rowToIncludeTransposed = rowAlt1.transpose();

            final MatrixStore<Double> body = this.getSolutionQ(Access2D.newPrimitiveColumnCollectable(rowAlt2));
            final double rhs = this.getInvQC().premultiply(rowAlt2).get().doubleValue(0L) - this.getMatrixBI().doubleValue(tmpToInclude);

            myS.add(this.countEqualityConstraints() + tmpToInclude, body, rhs, 3);
        }

        if ((this.countIterationConstraints() < this.countVariables()) && (tmpSolvable = this.isSolvableQ())) {
            // Q is SPD

            if (this.countIterationConstraints() == 0L) {
                // Unconstrained - can happen when PureASS and all inequalities are inactive

                this.getSolutionQ(this.getIterationC(), this.getIterationX());

            } else {
                // Actual/normal optimisation problem

                final double tmpRelativeError = myS.resolve(this.getL());

                if (this.isDebug()) {
                    this.debug("Relative error {} in solution for L={}", tmpRelativeError, this.getIterationL(tmpIncluded));
                }

                final ElementsSupplier<Double> tmpRHS = this.getIterationL(tmpIncluded).premultiply(this.getIterationA(tmpIncluded).transpose())
                        .operateOnMatching(this.getIterationC(), SUBTRACT);
                this.getSolutionQ(tmpRHS, this.getIterationX());
            }
        }

        if (!tmpSolvable && (tmpSolvable = this.computeGeneral(this.getIterationKKT()))) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = this.getSolutionGeneral(this.getIterationRHS());
            final int tmpCountVariables = this.countVariables();
            this.getIterationX().fillMatching(tmpXL.logical().limits(tmpCountVariables, (int) tmpXL.countColumns()).get());

            for (int i = 0; i < this.countEqualityConstraints(); i++) {
                this.getL().set(i, tmpXL.doubleValue(tmpCountVariables + i));
            }
            final int tmpLengthIncluded = tmpIncluded.length;
            for (int i = 0; i < tmpLengthIncluded; i++) {
                this.getL().set(this.countEqualityConstraints() + tmpIncluded[i], tmpXL.doubleValue(tmpCountVariables + this.countEqualityConstraints() + i));
            }

        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            if ((this.countVariables() + (long) this.countVariables()) < 20) {
                options.debug_appender.printmtrx("KKT", this.getIterationKKT());
                options.debug_appender.printmtrx("RHS", this.getIterationRHS());
            }
        }

        this.handleSubsolution(tmpSolvable, this.getIterationX(), tmpIncluded);

        // BasicLogger.debug("Iteration L: {}", myIterationL.asList().copy());
    }

    @Override
    void excludeAndRemove(final int toExclude) {
        this.exclude(toExclude);
        myS.remove(this.countEqualityConstraints() + toExclude);
    }

    @Override
    void initSolution(final MatrixStore<Double> tmpBI, final int tmpNumVars, final int tmpNumEqus) {

        if (this.hasInequalityConstraints()) {

            final int[] tmpExcluded = this.getExcluded();
            final MatrixStore<Double> tmpSI = this.getSI();

            for (int i = 0; i < tmpExcluded.length; i++) {
                final double tmpSlack = tmpSI.doubleValue(tmpExcluded[i]);
                if (options.slack.isZero(tmpSlack) && (this.getL().doubleValue(tmpNumEqus + tmpExcluded[i]) != ZERO)) {
                    this.include(tmpExcluded[i]);
                }
            }
        }

        while (((tmpNumEqus + this.countIncluded()) >= tmpNumVars) && (this.countIncluded() > 0)) {
            this.shrink();
        }

        if (this.isDebug() && ((tmpNumEqus + this.countIncluded()) > tmpNumVars)) {
            this.debug("Redundant contraints!");
        }

        this.setInvQC(this.getSolutionQ(this.getIterationC()));

        final int[] tmpIncluded = this.getIncluded();

        myS.clear();

        if ((tmpNumEqus + tmpIncluded.length) > 0) {

            final MatrixStore<Double> tmpIterA = this.getIterationA(tmpIncluded);
            final MatrixStore<Double> tmpIterB = this.getIterationB(tmpIncluded);

            final MatrixStore<Double> tmpCols = this.getSolutionQ(tmpIterA.transpose());
            final MatrixStore<Double> tmpRHS = this.getInvQC().premultiply(tmpIterA).operateOnMatching(SUBTRACT, tmpIterB).get();

            for (int j = 0; j < tmpNumEqus; j++) {
                myS.add(j, tmpCols.sliceColumn(0, j), tmpRHS.doubleValue(j), tmpNumVars);
            }
            for (int j = 0; j < tmpIncluded.length; j++) {
                myS.add(tmpNumEqus + tmpIncluded[j], tmpCols.sliceColumn(0, tmpNumEqus + j), tmpRHS.doubleValue(tmpNumEqus + j), 3);
            }
        }
    }

}
