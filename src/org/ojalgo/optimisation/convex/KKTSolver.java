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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.CholeskyDecomposition;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.EigenvalueDecomposition;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.QRDecomposition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.ZeroStore;

/**
 * When the KKT matrix is nonsingular, there is a unique optimal primal-dual pair (x,l). If the KKT matrix is singular,
 * but the KKT system is still solvable, any solution yields an optimal pair (x,l). If the KKT system is not solvable,
 * the quadratic optimization problem is unbounded below or infeasible.
 *
 * @author apete
 */
public final class KKTSolver extends Object {

    public static final class Input {

        private final MatrixStore<Double> myA;
        private final MatrixStore<Double> myB;
        private final MatrixStore<Double> myC;
        private final MatrixStore<Double> myQ;

        /**
         * | Q | = | C |
         *
         * @param Q
         * @param C
         */
        public Input(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            this(Q, C, null, null);
        }

        /**
         * | Q | A<sup>T</sup> | = | C | <br>
         * | A | 0 | = | B |
         *
         * @param Q
         * @param C
         * @param A
         * @param B
         */
        public Input(final MatrixStore<Double> Q, final MatrixStore<Double> C, final MatrixStore<Double> A, final MatrixStore<Double> B) {

            super();

            myQ = Q;
            myC = C;
            myA = A;
            myB = B;
        }

        final MatrixStore<Double> getA() {
            return myA;
        }

        final MatrixStore<Double> getB() {
            return myB;
        }

        final MatrixStore<Double> getC() {
            return myC;
        }

        final MatrixStore<Double> getKKT() {
            if (myA != null) {
                return myQ.builder().right(myA.transpose()).below(myA).build();
            } else {
                return myQ;
            }
        }

        final MatrixStore<Double> getQ() {
            return myQ;
        }

        final MatrixStore<Double> getRHS() {
            if (myB != null) {
                return myC.builder().below(myB).build();
            } else {
                return myC;
            }
        }

        final boolean isConstrained() {
            return (myA != null) && (myA.count() > 0L);
        }

    }

    public static final class Output {

        private final MatrixStore<Double> myL;
        private final boolean mySolvable;
        private final MatrixStore<Double> myX;

        Output(final MatrixStore<Double> X, final MatrixStore<Double> L, final boolean solvable) {

            super();

            myX = X;
            myL = L;
            mySolvable = solvable;
        }

        public final MatrixStore<Double> getL() {
            return myL;
        }

        public final MatrixStore<Double> getX() {
            return myX;
        }

        public final boolean isSolvable() {
            return mySolvable;
        }

    }

    private static final double SCALE = 1.0E2;

    private static final double SMALL = PrimitiveMath.MACHINE_EPSILON * 10; // ≈1.0E-15

    private transient PrimitiveDenseStore myCalculationC = null;
    private transient PrimitiveDenseStore myCalculationQ = null;

    private final Cholesky<Double> myCholesky;
    private final QR<Double> myQR;

    public KKTSolver() {

        super();

        myCholesky = CholeskyDecomposition.makePrimitive();
        myQR = QRDecomposition.makePrimitive();
    }

    public KKTSolver(final KKTSolver.Input template) {

        super();

        myCholesky = CholeskyDecomposition.make(template.getQ());
        myQR = QRDecomposition.makePrimitive();
    }

    public Output solve(final Input input) {
        return this.solve(input, false);
    }

    public Output solve(final Input input, final boolean validate) {

        final MatrixStore<Double> tmpQ = input.getQ();
        final MatrixStore<Double> tmpC = input.getC();
        final MatrixStore<Double> tmpA = input.getA();
        final MatrixStore<Double> tmpB = input.getB();

        boolean tmpSolvable = true;

        if (validate) {
            this.doValidate(input);
        }

        MatrixStore<Double> tmpX = null;
        MatrixStore<Double> tmpL = null;

        if (!input.isConstrained() && myQR.compute(tmpQ) && (tmpSolvable = myQR.isSolvable())) {
            // Unconstrained

            tmpX = myQR.solve(tmpC);
            tmpL = ZeroStore.makePrimitive(0, 1);

        } else if (input.isConstrained() && (tmpA.countRows() >= tmpA.countColumns()) && myQR.compute(tmpA) && (tmpSolvable = myQR.isSolvable())) {
            // Only 1 possible solution

            tmpX = myQR.solve(tmpB);

            myQR.compute(tmpA.transpose()); //TODO Shouldn't have to do this. Can solve directly with the already calculated  myQR.compute(tmpA).

            tmpL = myQR.solve(tmpC).subtract(tmpQ.multiplyRight(tmpX));

        } else {
            // Actual optimisation problem

            final int tmpSize = (int) tmpQ.countRows();
            final double tmpLargestQ = tmpQ.aggregateAll(Aggregator.LARGEST);

            final PrimitiveDenseStore tmpCalcQ = this.getCalculationQ(tmpQ);
            final PrimitiveDenseStore tmpCalcC = this.getCalculationC(tmpC);

            tmpCalcQ.maxpy(SCALE, tmpA.multiplyLeft(tmpA.transpose()));
            tmpCalcC.maxpy(SCALE, tmpB.multiplyLeft(tmpA.transpose()));
            while (!myCholesky.compute(tmpCalcQ)) {
                tmpCalcQ.modifyDiagonal(0, 0, PrimitiveFunction.ADD.second(SMALL * tmpCalcQ.aggregateAll(Aggregator.LARGEST)));
            }

            if (tmpSolvable = myCholesky.isSolvable()) {

                final MatrixStore<Double> tmpInvQAT = myCholesky.solve(tmpA.transpose());
                final MatrixStore<Double> tmpInvQC = myCholesky.solve(tmpCalcC);

                // Negated Schur complement
                final MatrixStore<Double> tmpS = tmpInvQAT.multiplyLeft(tmpA);

                myQR.compute(tmpS);
                if (tmpSolvable = myQR.isSolvable()) {

                    tmpL = myQR.solve(tmpInvQC.multiplyLeft(tmpA).add(tmpB.negate()));
                    tmpX = myCholesky.solve(tmpCalcC.add(tmpL.multiplyLeft(tmpA.transpose()).negate()));
                } else {
                    //                    BasicLogger.debug("Negated Schur complement QR");
                    //                    BasicLogger.debug("Q", myQR.getQ());
                    //                    BasicLogger.debug("R", myQR.getR());
                }
            } else {
                //                BasicLogger.debug("Q Cholesky");
                //                BasicLogger.debug("L", myCholesky.getL());
            }

        }

        return new Output(tmpX, tmpL, tmpSolvable);
    }

    public boolean validate(final Input input) {

        try {

            this.doValidate(input);

            return true;

        } catch (final IllegalArgumentException exception) {

            return false;
        }
    }

    private void doValidate(final Input input) {

        final MatrixStore<Double> tmpQ = input.getQ();
        final MatrixStore<Double> tmpC = input.getC();
        final MatrixStore<Double> tmpA = input.getA();
        final MatrixStore<Double> tmpB = input.getB();

        if ((tmpQ == null) || (tmpC == null)) {
            throw new IllegalArgumentException("Neither Q nor C may be null!");
        }

        if (((tmpA != null) && (tmpB == null)) || ((tmpA == null) && (tmpB != null))) {
            throw new IllegalArgumentException("One of A and B is null, and the other one is not!");
        }

        myCholesky.compute(tmpQ, true);
        if (!myCholesky.isSPD()) {
            // Not positive definite. Check if at least positive semidefinite.

            final Eigenvalue<Double> tmpEvD = EigenvalueDecomposition.makePrimitive(true);

            tmpEvD.compute(tmpQ, true);

            final MatrixStore<Double> tmpD = tmpEvD.getD();

            tmpEvD.reset();

            final int tmpLength = (int) tmpD.countRows();
            for (int ij = 0; ij < tmpLength; ij++) {
                if (tmpD.doubleValue(ij, ij) < ZERO) {
                    throw new IllegalArgumentException("Q must be positive semidefinite!");
                }
            }
        }

        if (tmpA != null) {
            myQR.compute(tmpA.transpose());
            if (myQR.getRank() != tmpA.countRows()) {
                throw new IllegalArgumentException("A must have full (row) rank!");
            }
        }
    }

    final PrimitiveDenseStore getCalculationC(final MatrixStore<Double> inputC) {
        if ((myCalculationC == null) || (myCalculationC.count() != inputC.count())) {
            myCalculationC = PrimitiveDenseStore.FACTORY.copy(inputC);
        } else {
            myCalculationC.fillMatching(inputC);
        }
        return myCalculationC;
    }

    final PrimitiveDenseStore getCalculationQ(final MatrixStore<Double> inputQ) {
        if ((myCalculationQ == null) || (myCalculationQ.count() != inputQ.count())) {
            myCalculationQ = PrimitiveDenseStore.FACTORY.copy(inputQ);
        } else {
            myCalculationQ.fillMatching(inputQ);
        }
        return myCalculationQ;
    }
}
