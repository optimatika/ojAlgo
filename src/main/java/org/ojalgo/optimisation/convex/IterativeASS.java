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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.iterative.ConjugateGradientSolver;
import org.ojalgo.matrix.task.iterative.MutableSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.ObjectPool;

/**
 * Solves optimisation problems of the form:
 * <p>
 * min 1/2 [X]<sup>T</sup>[Q][X] - [C]<sup>T</sup>[X]<br>
 * when [AE][X] == [BE]<br>
 * and [AI][X] <= [BI]
 * </p>
 * Where [AE] and [BE] are optinal.
 *
 * @author apete
 */
final class IterativeASS extends ActiveSetSolver {

    /**
     * The equation system body is the (negated) Schur complement (of the Q-matrix in the full KKT-system).
     *
     * @author apete
     */
    final class SchurComplementSolver extends MutableSolver<ConjugateGradientSolver> implements MatrixStore<Double> {

        private final int myCountE = IterativeASS.this.countEqualityConstraints();
        private final SparseArrayPool myEquationBodyPool;
        private final int myFullDim = myCountE + IterativeASS.this.countInequalityConstraints();
        private final Equation[] myIterationRows;

        SchurComplementSolver() {

            super(new ConjugateGradientSolver(), IterativeASS.this.countEqualityConstraints() + IterativeASS.this.countInequalityConstraints());

            // GaussSeidel
            //this.setTerminationContext(NumberContext.getMath(MathContext.DECIMAL64).newPrecision(13));
            //this.getDelegate().setRelaxationFactor(1.5);

            // ConjugateGradient
            this.setAccuracyContext(options.convex().iterative());
            this.setIterationsLimit(myFullDim + myFullDim);

            // this.setDebugPrinter(BasicLogger.DEBUG);

            myEquationBodyPool = new SparseArrayPool(myFullDim);
            myIterationRows = new Equation[myFullDim];
        }

        @Override
        public long countColumns() {
            return IterativeASS.this.countEqualityConstraints() + IterativeASS.this.countIncluded();
        }

        @Override
        public long countRows() {
            return IterativeASS.this.countEqualityConstraints() + IterativeASS.this.countIncluded();
        }

        @Override
        public double doubleValue(final int row, final int col) {

            int intRow = row;
            int intCol = col;

            if (intCol >= myCountE) {
                intCol = myCountE + IterativeASS.this.getIncluded(intCol - myCountE);
            }

            return super.doubleValue(intRow, intCol);
        }

        @Override
        public Double get(final int row, final int col) {
            return Double.valueOf(this.doubleValue(row, col));
        }

        @Override
        public PhysicalStore.Factory<Double, ?> physical() {
            return MATRIX_FACTORY;
        }

        void add(final int j, final Access1D<Double> column, final double rhs) {

            int[] incl = IterativeASS.this.getIncluded();

            Equation tmpNewRow = Equation.wrap(myEquationBodyPool.borrow(), j, rhs);
            myIterationRows[j] = tmpNewRow;
            this.add(tmpNewRow);

            if (myCountE > 0) {
                for (int i = 0; i < myCountE; i++) {
                    double tmpVal = IterativeASS.this.getMatrixAE(i).dot(column);
                    if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                        Equation tmpRowE = myIterationRows[i];
                        if (tmpRowE != null) {
                            tmpRowE.set(j, tmpVal);
                        }
                        tmpNewRow.set(i, tmpVal);
                    }
                }
            }

            if (incl.length > 0) {
                for (int _i = 0; _i < incl.length; _i++) {
                    double tmpVal = IterativeASS.this.getMatrixAI(incl[_i]).dot(column);
                    if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                        int i = myCountE + incl[_i];
                        Equation tmpRowI = myIterationRows[i];
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

            Equation rowI = myIterationRows[i];
            if (rowI != null) {
                this.remove(rowI);
                myEquationBodyPool.giveBack((BasicArray<Double>) rowI.getBody());
            }
            myIterationRows[i] = null;

            IterativeASS.this.getSolutionL().set(i, ZERO);
        }

    }

    static final class SparseArrayPool extends ObjectPool<BasicArray<Double>> {

        private static final SparseFactory<Double> ARRAY_FACTORY = SparseArray.factory(ArrayR064.FACTORY).initial(3);

        private final int myCount;

        SparseArrayPool(final int count) {
            super();
            myCount = count;
        }

        @Override
        protected BasicArray<Double> newObject() {
            return ARRAY_FACTORY.make(myCount);
        }

        @Override
        protected void reset(final BasicArray<Double> object) {
            object.reset();
        }

    }

    private final PhysicalStore<Double> myColumnInvQAt;
    /**
     * Equation system solver corresponding to the (negated) Schur complement. Used to solve for the Lagrange
     * multipliers.
     */
    private final SchurComplementSolver myS;

    IterativeASS(final ConvexData<Double> convexSolverBuilder, final Optimisation.Options optimisationOptions) {

        super(convexSolverBuilder, optimisationOptions);

        myS = new SchurComplementSolver();
        myColumnInvQAt = MATRIX_FACTORY.make(this.countVariables(), 1);
    }

    @Override
    protected void exclude(final int toExclude) {
        super.exclude(toExclude);
        myS.remove(this.countEqualityConstraints() + toExclude);
    }

    @Override
    protected void performIteration() {

        if (this.isLogProgress()) {
            this.log();
            this.log("PerformIteration {}", 1 + this.countIterations());
            this.log(this.toActivatorString());
        }

        int toInclude = this.getConstraintToInclude();
        this.setConstraintToInclude(-1);
        int[] incl = this.getIncluded();
        int[] excl = this.getExcluded();

        boolean solved = false;

        if (toInclude >= 0) {

            int constrIndex = this.countEqualityConstraints() + toInclude;
            SparseArray<Double> constrBody = this.getMatrixAI(toInclude);
            double constrRHS = this.getMatrixBI(toInclude);

            this.addConstraint(constrIndex, constrBody, constrRHS);
        }

        Primitive64Store iterX = this.getIterationX();

        if (this.countIterationConstraints() <= this.countVariables() && (solved = this.isSolvableQ())) {
            // Q is SPD

            MatrixStore<Double> invQC = this.getInvQC();

            if (this.countIterationConstraints() == 0L) {
                // Unconstrained - can happen when there are no equality constraints and all inequalities are inactive

                iterX.fillMatching(invQC);

            } else {
                // Actual/normal optimisation problem

                double relativeError = myS.resolve(this.getSolutionL());

                if (this.isLogDebug()) {
                    this.log("RHS={}", myS.getRHS());
                    this.log("Relative error {} in solution for L={}", relativeError, Arrays.toString(this.getIterationL(incl).toRawCopy1D()));
                }

                if (solved = options.convex().iterative().isZero(relativeError)) {

                    ElementsSupplier<Double> rhsX = this.getIterationL(incl).premultiply(this.getIterationA().transpose()).onMatching(this.getIterationC(),
                            SUBTRACT);
                    this.getSolutionQ(rhsX, iterX);
                }
            }
        }

        if (!solved) {
            // The above failed, try solving the full KKT system instaed

            Primitive64Store tmpXL = MATRIX_FACTORY.make(this.countVariables() + this.countIterationConstraints(), 1L);

            if (solved = this.solveFullKKT(tmpXL)) {

                iterX.fillMatching(tmpXL.limits(this.countVariables(), 1));

                for (int i = 0; i < this.countEqualityConstraints(); i++) {
                    this.getSolutionL().set(i, tmpXL.doubleValue(this.countVariables() + i));
                }
                int tmpLengthIncluded = incl.length;
                for (int i = 0; i < tmpLengthIncluded; i++) {
                    this.getSolutionL().set(this.countEqualityConstraints() + incl[i],
                            tmpXL.doubleValue(this.countVariables() + this.countEqualityConstraints() + i));
                }
            }
        }

        this.handleIterationResults(solved, iterX, incl, excl);
    }

    void addConstraint(final int constrIndex, final SparseArray<Double> constrBody, final double constrRHS) {

        MatrixStore<Double> body = this.getSolutionQ(Access2D.newPrimitiveColumnCollectable(constrBody), myColumnInvQAt);

        double rhs = constrBody.dot(this.getInvQC()) - constrRHS;

        myS.add(constrIndex, body, rhs);
    }

    @Override
    void resetActivator() {

        super.resetActivator();

        int nbEqus = this.countEqualityConstraints();
        int nbVars = this.countVariables();

        myS.clear();
        int[] incl = this.getIncluded();

        if (nbEqus + incl.length > 0) {

            MatrixStore<Double> iterA = this.getIterationA();
            MatrixStore<Double> iterB = this.getIterationB();

            MatrixStore<Double> tmpCols = this.getSolutionQ(iterA.transpose());
            MatrixStore<Double> tmpRHS = this.getInvQC().premultiply(iterA).onMatching(SUBTRACT, iterB).collect(MATRIX_FACTORY);

            for (int j = 0; j < nbEqus; j++) {
                myS.add(j, tmpCols.sliceColumn(j), tmpRHS.doubleValue(j));
            }
            for (int j = 0; j < incl.length; j++) {
                myS.add(nbEqus + incl[j], tmpCols.sliceColumn(nbEqus + j), tmpRHS.doubleValue(nbEqus + j));
            }
        }

    }

}
