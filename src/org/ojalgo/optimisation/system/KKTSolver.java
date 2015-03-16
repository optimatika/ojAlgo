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
package org.ojalgo.optimisation.system;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.ZeroStore;
import org.ojalgo.netio.BasicLogger.Appender;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Options;

/**
 * When the KKT matrix is nonsingular, there is a unique optimal primal-dual pair (x,l). If the KKT matrix is
 * singular, but the KKT system is still solvable, any solution yields an optimal pair (x,l). If the KKT
 * system is not solvable, the quadratic optimization problem is unbounded below or infeasible.
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

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            final StringBuilder tmpSB = new StringBuilder();

            tmpSB.append(mySolvable);

            if (mySolvable) {

                tmpSB.append(" X=");

                tmpSB.append(Array1D.PRIMITIVE.copy(this.getX()));

                tmpSB.append(" L=");

                tmpSB.append(Array1D.PRIMITIVE.copy(this.getL()));
            }

            return tmpSB.toString();
        }

    }

    private static final Options DEFAULT_OPTIONS = new Optimisation.Options();

    private final Cholesky<Double> myCholesky;
    private final LU<Double> myLU;

    public KKTSolver() {

        super();

        myCholesky = Cholesky.makePrimitive();
        myLU = LU.makePrimitive();
    }

    public KKTSolver(final KKTSolver.Input template) {

        super();

        final MatrixStore<Double> tmpQ = template.getQ();

        myCholesky = Cholesky.make(tmpQ);
        myLU = LU.make(tmpQ);
    }

    public Output solve(final Input input) {
        return this.solve(input, DEFAULT_OPTIONS);
    }

    private transient PrimitiveDenseStore myX = null;

    PrimitiveDenseStore getX(final Input input) {
        if (myX == null) {
            myX = PrimitiveDenseStore.FACTORY.makeZero(input.getQ().countRows(), 1L);
        }
        return myX;
    }

    public Output solve(final Input input, final Optimisation.Options options) {

        final MatrixStore<Double> tmpQ = input.getQ();
        final MatrixStore<Double> tmpC = input.getC();
        final MatrixStore<Double> tmpA = input.getA();
        final MatrixStore<Double> tmpB = input.getB();

        boolean tmpSolvable = true;
        final Appender tmpAppender = options.debug_appender;

        if (options.validate) {
            this.doValidate(input);
        }

        final PrimitiveDenseStore tmpX = this.getX(input);
        MatrixStore<Double> tmpL = null;

        if (!input.isConstrained() && myCholesky.compute(tmpQ) && (tmpSolvable = myCholesky.isSolvable())) {
            // Unconstrained

            myCholesky.solve(tmpC, tmpX);
            tmpL = ZeroStore.makePrimitive(0, 1);

        } else if (input.isConstrained() && (tmpA.countRows() >= tmpA.countColumns()) && myLU.compute(tmpA) && (tmpSolvable = myLU.isSolvable())) {
            // Only 1 possible solution

            myLU.solve(tmpB, tmpX);

            final MatrixStore<Double> tmpSolve;
            if (tmpA.countRows() == tmpA.countColumns()) {
                myLU.compute(tmpA.transpose()); //TODO Shouldn't have to do this. Can solve directly with the already calculated  myQR.compute(tmpA).
                tmpSolve = myLU.solve(tmpC); // Problem here! when A is overdetermined/redundant.
            } else {
                // This should rarely be necessary...
                final SingularValue<Double> tmpSVD = SingularValue.makePrimitive();
                tmpSVD.compute(tmpA.transpose());
                tmpSolve = tmpSVD.solve(tmpC);
            }

            tmpL = tmpSolve.subtract(tmpQ.multiply(tmpX));

        } else {
            // Actual optimisation problem

            // myCholesky.compute(tmpQ);
            if (!myCholesky.isComputed()) {
                myCholesky.compute(tmpQ);
            }

            if (tmpSolvable = myCholesky.isSolvable()) {

                final MatrixStore<Double> tmpInvQAT = myCholesky.solve(tmpA.transpose());
                final MatrixStore<Double> tmpInvQC = myCholesky.solve(tmpC);

                // Negated Schur complement
                final MatrixStore<Double> tmpS = tmpInvQAT.multiplyLeft(tmpA);

                myLU.compute(tmpS);

                if (tmpSolvable = myLU.isSolvable()) {

                    tmpL = myLU.solve(tmpInvQC.multiplyLeft(tmpA).add(tmpB.negate()));
                    myCholesky.solve(tmpC.add(tmpL.multiplyLeft(tmpA.transpose()).negate()), tmpX);

                } else if (tmpAppender != null) {

                    tmpAppender.println("Negated Schur complement LU");
                    tmpAppender.printmtrx("S", tmpS);
                    tmpAppender.printmtrx("L", myLU.getL());
                    tmpAppender.printmtrx("U", myLU.getU());
                }

            } else if (tmpAppender != null) {

                tmpAppender.println("Q Cholesky");
                tmpAppender.printmtrx("Q", tmpQ);
                tmpAppender.printmtrx("L", myCholesky.getL());
            }

            if (!tmpSolvable) {
                // Try solving the full system instaed
                myLU.compute(input.getKKT());
                if (tmpSolvable = myLU.isSolvable()) {

                    final MatrixStore<Double> tmpXL = myLU.solve(input.getRHS());
                    // tmpX = tmpXL.builder().rows(0, (int) tmpQ.countColumns()).build();
                    tmpX.fillMatching(tmpXL.builder().rows(0, (int) tmpQ.countColumns()).build());
                    tmpL = tmpXL.builder().rows((int) tmpQ.countColumns(), (int) tmpXL.count()).build();

                    final MatrixStore<Double> tmpAX = tmpA.multiply(tmpX);
                    for (int i = 0; i < tmpA.countRows(); i++) {
                        tmpSolvable &= !options.slack.isDifferent(tmpB.doubleValue(i), tmpAX.doubleValue(i));
                    }

                    if (!tmpSolvable && (tmpAppender != null)) {
                        tmpAppender.printmtrx("B", tmpB, options.slack);
                        tmpAppender.printmtrx("AX", tmpAX, options.slack);
                    }

                } else if (tmpAppender != null) {

                    tmpAppender.println("Full KKT System LU");
                    tmpAppender.printmtrx("KKT", input.getKKT());
                    tmpAppender.printmtrx("L", myLU.getL());
                    tmpAppender.printmtrx("U", myLU.getU());
                }
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
            throw new IllegalArgumentException("Either A or B is null, and the other one is not!");
        }

        myCholesky.compute(tmpQ, true);
        if (!myCholesky.isSPD()) {
            // Not positive definite. Check if at least positive semidefinite.

            final Eigenvalue<Double> tmpEvD = Eigenvalue.makePrimitive(true);

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
            myLU.compute(tmpA.transpose());
            if (myLU.getRank() != tmpA.countRows()) {
                throw new IllegalArgumentException("A must have full (row) rank!");
            }
        }
    }

}
