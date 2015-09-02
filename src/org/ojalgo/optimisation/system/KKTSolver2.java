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
package org.ojalgo.optimisation.system;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Optimisation;

/**
 * When the KKT matrix is nonsingular, there is a unique optimal primal-dual pair (x,l). If the KKT matrix is
 * singular, but the KKT system is still solvable, any solution yields an optimal pair (x,l). If the KKT
 * system is not solvable, the quadratic optimization problem is unbounded below or infeasible.
 *
 * @author apete
 */
public class KKTSolver2 extends KKTSystem {

    private final Cholesky<Double> myCholesky;
    private final LU<Double> myLU;

    private transient PrimitiveDenseStore myX = null;

    public KKTSolver2(final KKTSystem.Input template) {

        super();

        final MatrixStore<Double> tmpQ = template.getQ();

        myCholesky = Cholesky.make(tmpQ);
        myLU = LU.make(tmpQ);

        myCholesky.compute(tmpQ);
    }

    KKTSolver2() {
        this(null);
    }

    @Override
    public KKTSystem.Output solve(final KKTSystem.Input input, final Optimisation.Options options) {

        final MatrixStore<Double> tmpQ = input.getQ();
        final MatrixStore<Double> tmpC = input.getC();
        final MatrixStore<Double> tmpA = input.getA();
        final MatrixStore<Double> tmpB = input.getB();

        boolean tmpSolvable = true;
        if (options.validate) {
            this.doValidate(input);
        }

        final PrimitiveDenseStore tmpX = this.getX(input);
        MatrixStore<Double> tmpL = null;

        if (input.isConstrained() && (tmpA.countRows() == tmpA.countColumns()) && (tmpSolvable = myLU.compute(tmpA))) {
            // Only 1 possible solution

            myLU.solve(tmpB, tmpX);

            myLU.decompose(tmpA.transpose()); //TODO Shouldn't have to do this. Can solve directly with the already calculated  myLU.compute(tmpA).
            tmpL = myLU.solve(tmpC.subtract(tmpQ.multiply(tmpX)));

        } else if (tmpSolvable = myCholesky.isSolvable()) { // TODO Doesn't change inbetween active set iterations
            // Q is SPD

            if (!input.isConstrained()) {
                // Unconstrained

                myCholesky.solve(tmpC, tmpX);
                tmpL = MatrixStore.PRIMITIVE.makeZero(0, 1).get();

            } else {
                // Actual/normal optimisation problem

                final MatrixStore<Double> tmpInvQAT = myCholesky.solve(tmpA.transpose()); //TODO Only some columns change inbetween active set iterations, precalculate all columns and only select which at active set iteration

                // Negated Schur complement
                final MatrixStore<Double> tmpS = tmpInvQAT.multiplyLeft(tmpA); //TODO Selection of rows/columns based on active set
                if (tmpSolvable = myLU.compute(tmpS)) {

                    final MatrixStore<Double> tmpInvQC = myCholesky.solve(tmpC); //TODO Constant if C doesn't change

                    tmpL = myLU.solve(tmpInvQC.multiplyLeft(tmpA).subtract(tmpB));
                    myCholesky.solve(tmpC.subtract(tmpL.multiplyLeft(tmpA.transpose())), tmpX);
                }
            }
        }

        if (!tmpSolvable && (tmpSolvable = myLU.compute(input.getKKT()))) {
            // The above failed
            // Try solving the full KKT system instaed

            final MatrixStore<Double> tmpXL = myLU.solve(input.getRHS());
            tmpX.fillMatching(tmpXL.builder().rows(0, (int) tmpQ.countColumns()).build());
            tmpL = tmpXL.builder().rows((int) tmpQ.countColumns(), (int) tmpXL.count()).build();
        }

        //        if (!tmpSolvable) {
        //            this.doValidate(input);
        //        }
        if (!tmpSolvable && (options.debug_appender != null)) {

            options.debug_appender.println("KKT system unsolvable");
            options.debug_appender.printmtrx("KKT", input.getKKT());
            options.debug_appender.printmtrx("RHS", input.getRHS());
            if (input.isConstrained()) {
                options.debug_appender.printmtrx("Q", input.getQ());
                options.debug_appender.printmtrx("C", input.getC());
                options.debug_appender.printmtrx("A", input.getA());
                options.debug_appender.printmtrx("B", input.getB());
            }
        }

        return new KKTSystem.Output(tmpX, tmpL, tmpSolvable);
    }

    public boolean validate(final KKTSystem.Input input) {

        try {

            this.doValidate(input);

            return true;

        } catch (final IllegalArgumentException exception) {

            return false;
        }
    }

    private void doValidate(final KKTSystem.Input input) {

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
            myLU.decompose(tmpA.countRows() < tmpA.countColumns() ? tmpA.transpose() : tmpA);
            if (myLU.getRank() != tmpA.countRows()) {
                throw new IllegalArgumentException("A must have full (row) rank!");
            }
        }
    }

    PrimitiveDenseStore getX(final KKTSystem.Input input) {
        if (myX == null) {
            myX = PrimitiveDenseStore.FACTORY.makeZero(input.getQ().countRows(), 1L);
        }
        return myX;
    }

}
