/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.ElementsConsumer;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.matrix.task.iterative.Equation;
import org.ojalgo.matrix.task.iterative.MutableSolver;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.LinearSolver;
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
            this.setTerminationContext(NumberContext.getMath(MathContext.DECIMAL64).newPrecision(9));

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

        public double doubleValue(final long row, final long column) {

            int tmpColumn = (int) column;

            if (tmpColumn >= myCountE) {
                tmpColumn = myCountE + myActivator.getIncluded()[tmpColumn - myCountE];
            }

            return this.doubleValue((int) row, tmpColumn);
        }

        public Double get(final long row, final long column) {
            return this.doubleValue(row, column);
        }

        void add(final int j, final Access1D<Double> column, final double rhs) {

            final int[] myIncluded = myActivator.getIncluded();

            final Equation tmpNewRow = new Equation(j, myFullDim, rhs);
            myIterationRows[j] = tmpNewRow;
            this.add(tmpNewRow);

            if (IterativeASS.this.getAE() != null) {

                final MatrixStore<Double> tmpProdE = IterativeASS.this.getAE().multiply(column);
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

                final MatrixStore<Double> tmpProdI = IterativeASS.this.getAI().builder().row(myIncluded).get().multiply(column);
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

            myIterationL.set(i, 0.0);
        }

    }

    private final MyIterativeSolver myS;

    IterativeASS(final ConvexSolver.Builder matrices, final Optimisation.Options solverOptions) {

        super(matrices, solverOptions);

        myS = new MyIterativeSolver();
    }

    @Override
    protected boolean initialise(final Result kickStarter) {

        super.initialise(kickStarter);

        final MatrixStore<Double> tmpQ = this.getQ();
        final MatrixStore<Double> tmpC = this.getC();
        final MatrixStore<Double> tmpAE = this.getAE();
        final MatrixStore<Double> tmpBE = this.getBE();
        final MatrixStore<Double> tmpAI = this.getAI();
        final MatrixStore<Double> tmpBI = this.getBI();

        final int tmpNumVars = (int) tmpC.countRows();
        final int tmpNumEqus = tmpAE != null ? (int) tmpAE.countRows() : 0;
        final int tmpNumInes = tmpAI != null ? (int) tmpAI.countRows() : 0;

        final DecompositionStore<Double> tmpX = this.getX();

        myActivator.excludeAll();

        boolean tmpFeasible = false;
        final boolean tmpUsableKickStarter = (kickStarter != null) && kickStarter.getState().isApproximate();

        if (tmpUsableKickStarter) {
            this.fillX(kickStarter);
            tmpFeasible = this.checkFeasibility(false);
        }

        if (!tmpFeasible) {
            // Form LP to check feasibility

            final MatrixStore<Double> tmpGradient = tmpUsableKickStarter ? tmpQ.multiply(tmpX).subtract(tmpC) : tmpC.negate();
            final MatrixStore<Double> tmpLinearC = tmpGradient.builder().below(tmpGradient.negate()).below(tmpNumInes).build();
            // final MatrixStore<Double> tmpLinearC = MatrixStore.PRIMITIVE.makeZero(tmpNumVars + tmpNumVars + tmpNumInes, 1).get();

            final LinearSolver.Builder tmpLinearBuilder = LinearSolver.getBuilder(tmpLinearC);

            MatrixStore<Double> tmpAEpart = null;
            MatrixStore<Double> tmpBEpart = null;

            if (tmpNumEqus > 0) {
                tmpAEpart = tmpAE.builder().right(tmpAE.negate()).right(tmpNumInes).build();
                tmpBEpart = tmpBE;
            }

            if (tmpNumInes > 0) {
                final MatrixStore<Double> tmpAIpart = tmpAI.builder().right(tmpAI.negate()).right(MatrixStore.PRIMITIVE.makeIdentity(tmpNumInes).get()).build();
                final MatrixStore<Double> tmpBIpart = tmpBI;
                if (tmpAEpart != null) {
                    tmpAEpart = tmpAEpart.builder().below(tmpAIpart).build();
                    tmpBEpart = tmpBEpart.builder().below(tmpBIpart).build();
                } else {
                    tmpAEpart = tmpAIpart;
                    tmpBEpart = tmpBIpart;
                }
            }

            if (tmpAEpart != null) {

                final PhysicalStore<Double> tmpLinearAE = tmpAEpart.copy();
                final PhysicalStore<Double> tmpLinearBE = tmpBEpart.copy();

                for (int i = 0; i < tmpLinearBE.countRows(); i++) {
                    if (tmpLinearBE.doubleValue(i) < 0.0) {
                        tmpLinearAE.modifyRow(i, 0, NEGATE);
                        tmpLinearBE.modifyRow(i, 0, NEGATE);
                    }
                }

                tmpLinearBuilder.equalities(tmpLinearAE, tmpLinearBE);
            }

            final LinearSolver tmpLinearSolver = tmpLinearBuilder.build();

            final Result tmpLinearResult = tmpLinearSolver.solve();

            if (tmpFeasible = tmpLinearResult.getState().isFeasible()) {

                final ElementsConsumer<Double> tmpLI = myIterationL.regionByOffsets(tmpNumEqus, 0);

                for (int i = 0; i < tmpNumVars; i++) {
                    this.setX(i, tmpLinearResult.doubleValue(i) - tmpLinearResult.doubleValue(tmpNumVars + i));
                }
                @SuppressWarnings("deprecation")
                final double[] tmpResidual = tmpLinearSolver.getResidualCosts();
                for (int i = tmpNumVars * 2; i < tmpResidual.length; i++) {
                    final int tmpIndexToInclude = i - (2 * tmpNumVars);
                    // this.setLI(tmpIndexToInclude, tmpResidual[i]);
                    tmpLI.set(tmpIndexToInclude, tmpResidual[i]);
                }
            }
        }

        if (tmpFeasible) {

            this.setState(State.FEASIBLE);

            if (this.hasInequalityConstraints()) {

                final int[] tmpExcluded = myActivator.getExcluded();

                final MatrixStore<Double> tmpAIX = this.getAIX(tmpExcluded);
                for (int i = 0; i < tmpExcluded.length; i++) {
                    final double tmpBody = tmpAIX.doubleValue(i);
                    final double tmpRHS = tmpBI.doubleValue(tmpExcluded[i]);
                    if (!options.slack.isDifferent(tmpRHS, tmpBody)) {
                        myActivator.include(tmpExcluded[i]);
                    }
                }
            }

            while ((tmpNumEqus + myActivator.countIncluded()) > tmpNumVars) {
                this.shrink();
            }

            if (this.isDebug() && ((tmpNumEqus + myActivator.countIncluded()) > tmpNumVars)) {
                this.debug("Redundant contraints!");
            }

            myInvQC = myCholesky.solve(this.getIterationC());

            final int[] tmpIncluded = myActivator.getIncluded();

            if ((tmpNumEqus + tmpIncluded.length) > 0) {

                final MatrixStore<Double> tmpCols = myCholesky.solve(this.getIterationA(tmpIncluded).transpose());
                final MatrixStore<Double> tmpRHS = myInvQC.multiplyLeft(this.getIterationA(tmpIncluded))
                        .operateOnMatching(SUBTRACT, this.getIterationB(tmpIncluded)).get();
                for (int j = 0; j < tmpNumEqus; j++) {
                    myS.add(j, tmpCols.sliceColumn(0, j), tmpRHS.doubleValue(j));
                }
                for (int j = 0; j < tmpIncluded.length; j++) {
                    myS.add(tmpNumEqus + tmpIncluded[j], tmpCols.sliceColumn(0, tmpNumEqus + j), tmpRHS.doubleValue(tmpNumEqus + j));
                }
            }

        } else {

            this.setState(State.INFEASIBLE);

            this.resetX();
        }

        if (this.isDebug()) {

            this.debug("Initial solution: {}", tmpX.copy().asList());
            if (tmpAE != null) {
                this.debug("Initial E-slack: {}", this.getSE().copy().asList());
            }
            if (tmpAI != null) {
                this.debug("Initial I-included-slack: {}", this.getSI(myActivator.getIncluded()).copy().asList());
                this.debug("Initial I-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy().asList());
            }
        }

        return this.getState().isFeasible();
    }

    @Override
    protected boolean needsAnotherIteration() {

        if (this.isDebug()) {
            this.debug("\nNeedsAnotherIteration?");
            this.debug(myActivator.toString());
        }

        int tmpToInclude = -1;
        int tmpToExclude = -1;

        if (this.hasInequalityConstraints()) {
            tmpToInclude = this.suggestConstraintToInclude();
            if (tmpToInclude == -1) {
                tmpToExclude = this.suggestConstraintToExclude();
            }
        }

        if (this.isDebug()) {
            this.debug("Suggested to include: {}", tmpToInclude);
            this.debug("Suggested to exclude: {}", tmpToExclude);
        }

        if (tmpToExclude == -1) {
            if (tmpToInclude == -1) {
                // Suggested to do nothing
                this.setState(State.OPTIMAL);
                return false;
            } else {
                // Only suggested to include
                myActivator.include(tmpToInclude);
                this.setState(State.APPROXIMATE);
                return true;
            }
        } else {
            if (tmpToInclude == -1) {
                // Only suggested to exclude
                myActivator.exclude(tmpToExclude);
                myS.remove(this.countEqualityConstraints() + tmpToExclude);
                this.setState(State.APPROXIMATE);
                return true;
            } else {
                // Suggested both to exclude and include
                myActivator.exclude(tmpToExclude);
                myS.remove(this.countEqualityConstraints() + tmpToExclude);
                myActivator.include(tmpToInclude);
                this.setState(State.APPROXIMATE);
                return true;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void performIteration() {

        if (this.isDebug()) {
            this.debug("\nPerformIteration {}", 1 + this.countIterations());
            this.debug(myActivator.toString());
        }

        final int tmpToInclude = myConstraintToInclude;
        final int tmpToExclude = tmpToInclude >= 0 ? -1 : myActivator.getLastExcluded();

        myConstraintToInclude = -1;
        final int[] tmpIncluded = myActivator.getIncluded();

        final MatrixStore<Double> tmpIterQ = this.getIterationQ();
        final MatrixStore<Double> tmpIterC = this.getIterationC();
        final MatrixStore<Double> tmpIterA = this.getIterationA(tmpIncluded);
        final MatrixStore<Double> tmpIterB = this.getIterationB(tmpIncluded);

        boolean tmpSolvable = false;

        final PrimitiveDenseStore tmpIterX = myIterationX;
        final PrimitiveDenseStore tmpIterL = PrimitiveDenseStore.FACTORY.makeZero(tmpIterA.countRows(), 1L);

        final int tmpCountEqualityConstraints = this.countEqualityConstraints();

        if (tmpToInclude >= 0) {
            final int tmpIndex = tmpCountEqualityConstraints + tmpToInclude;
            final MatrixStore<Double> tmpElements = myCholesky.solve(this.getAI().builder().row(tmpToInclude).transpose().get());
            final double tmpRHS = myInvQC.multiplyLeft(this.getAI().sliceRow(tmpToInclude, 0L)).get().doubleValue(0L) - this.getBI().doubleValue(tmpToInclude);
            myS.add(tmpIndex, tmpElements, tmpRHS);
        }

        if ((tmpIterA.countRows() < tmpIterA.countColumns()) && (tmpSolvable = myCholesky.isSolvable())) {
            // Q is SPD

            if (tmpIterA.countRows() == 0L) {
                // Unconstrained - can happen when PureASS and all inequalities are inactive

                myCholesky.solve(tmpIterC, tmpIterX);

            } else {
                // Actual/normal optimisation problem

                if (this.isDebug()) {

                    BasicLogger.debug(Arrays.toString(tmpIncluded), myS);

                    BasicLogger.debug("Is hermitian? {}", MatrixUtils.isHermitian(myS));

                    if (myS.count() > 1) {

                        final Eigenvalue<Double> tmpEvD = Eigenvalue.makePrimitive(true);
                        tmpEvD.decompose(MatrixStore.PRIMITIVE.makeWrapper(myS));
                        BasicLogger.debug("Eigenvalues: {}", tmpEvD.getEigenvalues());

                    }

                    BasicLogger.debug();
                }

                final MatrixStore<Double> tmpL = myS.resolve(myIterationL);
                for (int i = 0; i < tmpCountEqualityConstraints; i++) {
                    tmpIterL.set(i, tmpL.doubleValue(i));
                }
                for (int i = 0; i < tmpIncluded.length; i++) {
                    tmpIterL.set(tmpCountEqualityConstraints + i, tmpL.doubleValue(tmpCountEqualityConstraints + tmpIncluded[i]));
                }

                myCholesky.solve(tmpIterL.multiplyLeft(tmpIterA.transpose()).operateOnMatching(tmpIterC, SUBTRACT), tmpIterX);
            }
        }

        if (!tmpSolvable && (tmpSolvable = myLU.compute(this.getIterationKKT(tmpIncluded)))) {
            // The above failed, but the KKT system is solvable
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = myLU.solve(this.getIterationRHS(tmpIncluded));
            tmpIterX.fillMatching(tmpXL.builder().rows(0, this.countVariables()).build());
            tmpIterL.fillMatching(tmpXL.builder().rows(this.countVariables(), (int) tmpXL.count()).build());
        }

        if (!tmpSolvable && this.isDebug()) {
            options.debug_appender.println("KKT system unsolvable!");
            options.debug_appender.printmtrx("KKT", this.getIterationKKT());
            options.debug_appender.printmtrx("RHS", this.getIterationRHS());
        }

        if (tmpSolvable) {
            // Subproblem solved successfully

            tmpIterX.fillMatching(tmpIterX, SUBTRACT, this.getX());

            if (this.isDebug()) {
                this.debug("Current: {}", this.getX().asList());
                this.debug("Step: {}", tmpIterX.copy().asList());
                this.debug("L: {}", tmpIterL.copy().asList());
            }

            final double tmpNormCurrentX = this.getX().aggregateAll(Aggregator.NORM2);
            final double tmpNormStepX = tmpIterX.aggregateAll(Aggregator.NORM2);
            if (!options.solution.isSmall(tmpNormCurrentX, tmpNormStepX)) {
                // Non-zero solution

                double tmpStepLength = ONE;

                final int[] tmpExcluded = myActivator.getExcluded();
                if (tmpExcluded.length > 0) {

                    final MatrixStore<Double> tmpNumer = this.getSI(tmpExcluded);
                    final MatrixStore<Double> tmpDenom = this.getAI().builder().row(tmpExcluded).build().multiply(tmpIterX);

                    if (this.isDebug()) {
                        final PhysicalStore<Double> tmpStepLengths = tmpNumer.copy();
                        tmpStepLengths.fillMatching(tmpStepLengths, DIVIDE, tmpDenom);
                        this.debug("Looking for the largest possible step length (smallest positive scalar) among these: {}).", tmpStepLengths.asList());
                    }

                    for (int i = 0; i < tmpExcluded.length; i++) {

                        final double tmpN = tmpNumer.doubleValue(i); // Current slack
                        final double tmpD = tmpDenom.doubleValue(i); // Proposed slack change
                        final double tmpVal = options.slack.isSmall(tmpD, tmpN) ? ZERO : tmpN / tmpD;

                        if ((tmpD > ZERO) && (tmpVal >= ZERO) && (tmpVal < tmpStepLength) && !options.solution.isSmall(tmpNormStepX, tmpD)) {
                            tmpStepLength = tmpVal;
                            myConstraintToInclude = tmpExcluded[i];
                            if (this.isDebug()) {
                                this.debug("Best so far: {} @ {} ({}).", tmpStepLength, i, myConstraintToInclude);
                            }
                        }
                    }

                }

                if (tmpStepLength > ZERO) { // It is possible that it becomes == 0.0
                    this.getX().maxpy(tmpStepLength, tmpIterX);
                }

                this.setState(State.APPROXIMATE);

            } else if (this.isDebug()) {
                // Zero solution

                if (this.isDebug()) {
                    this.debug("Step too small!");
                }

                this.setState(State.FEASIBLE);
            }

            final ElementsConsumer<Double> tmpLE = myIterationL.regionByLimits(this.countEqualityConstraints(), 1);
            final ElementsConsumer<Double> tmpLI = myIterationL.regionByOffsets(this.countEqualityConstraints(), 0);

            for (int i = 0; i < this.countEqualityConstraints(); i++) {
                // this.setLE(i, tmpIterL.doubleValue(i));
                tmpLE.set(i, tmpIterL.doubleValue(i));
            }

            for (int i = 0; i < tmpIncluded.length; i++) {
                // this.setLI(tmpIncluded[i], tmpIterL.doubleValue(this.countEqualityConstraints() + i));
                tmpLI.set(tmpIncluded[i], tmpIterL.doubleValue(this.countEqualityConstraints() + i));
            }

        } else if (tmpIncluded.length >= 1) {
            // Subproblem NOT solved successfully
            // At least 1 active inequality

            this.shrink();

            this.performIteration();

        } else if (this.checkFeasibility(false)) {
            // Subproblem NOT solved successfully
            // No active inequality
            // Feasible current solution

            this.setState(State.FEASIBLE);

        } else {
            // Subproblem NOT solved successfully
            // No active inequality
            // Not feasible current solution

            this.setState(State.INFEASIBLE);
        }

        if (this.isDebug()) {
            this.debug("Post iteration");
            this.debug("\tSolution: {}", this.getX().copy().asList());
            if ((this.getAE() != null) && (this.getAE().count() > 0)) {
                this.debug("\tE-slack: {}", this.getSE().copy().asList());
            }
            if ((this.getAI() != null) && (this.getAI().count() > 0)) {
                if (tmpIncluded.length != 0) {
                    this.debug("\tI-included-slack: {}", this.getSI(tmpIncluded).copy().asList());
                }
                if (myActivator.getExcluded().length != 0) {
                    this.debug("\tI-excluded-slack: {}", this.getSI(myActivator.getExcluded()).copy().asList());
                }
            }
        }
    }

    void shrink() {

        final int[] tmpIncluded = myActivator.getIncluded();

        int tmpToExclude = tmpIncluded[0];
        double tmpMaxLagrange = ZERO;

        // final MatrixStore<Double> tmpLI = this.getLI(tmpIncluded);
        final MatrixStore<Double> tmpLI = myIterationL.builder().offsets(this.countEqualityConstraints(), 0).row(tmpIncluded).get();
        for (int i = 0; i < tmpIncluded.length; i++) {
            final double tmpVal = Math.abs(tmpLI.doubleValue(i));
            if (tmpVal >= tmpMaxLagrange) {
                tmpMaxLagrange = tmpVal;
                tmpToExclude = tmpIncluded[i];
            }
        }
        myActivator.exclude(tmpToExclude);
        myS.remove(this.countEqualityConstraints() + tmpToExclude);
    }

}
