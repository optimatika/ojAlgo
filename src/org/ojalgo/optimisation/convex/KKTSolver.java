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

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.CholeskyDecomposition;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.LUDecomposition;
import org.ojalgo.matrix.store.IdentityStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.Builder;

public final class KKTSolver extends Object {

    public static final class Input {

        private final MatrixStore<Double> myA;
        private final MatrixStore<Double> myB;
        private final MatrixStore<Double> myC;
        private final MatrixStore<Double> myQ;

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

    static final int ALTERNATIVE = 2;
    static final int MODIFIED = 3;
    static final int PLAIN = 1;

    private final Cholesky<Double> myInverterQ;
    private final LU<Double> myInverterS;

    public KKTSolver(final KKTSolver.Input template) {

        super();

        myInverterQ = CholeskyDecomposition.makePrimitive();
        myInverterS = LUDecomposition.makePrimitive();
    }

    @SuppressWarnings("unused")
    private KKTSolver() {

        super();

        myInverterQ = null;
        myInverterS = null;
    }

    public Output solve(final Input input) {
        return this.solve(input, PLAIN);
    }

    Output solve(final Input input, final int model) {

        boolean tmpSolvable = true;

        MatrixStore<Double> tmpQ = input.getQ();
        MatrixStore<Double> tmpC = input.getC();
        final MatrixStore<Double> tmpA = input.getA();
        final MatrixStore<Double> tmpB = input.getB();

        if (model < MODIFIED) {

            if (model > PLAIN) {
                tmpQ = tmpQ.add(tmpA.multiplyLeft(tmpA.transpose()));
                tmpC = tmpC.add(tmpB.multiplyLeft(tmpA.transpose()));
            }

            myInverterQ.compute(tmpQ);
            if (tmpSolvable = myInverterQ.isSolvable()) {

                final MatrixStore<Double> tmpInvQAT = myInverterQ.solve(tmpA.transpose());
                final MatrixStore<Double> tmpInvQC = myInverterQ.solve(tmpC);

                // Negated Schur complement
                final MatrixStore<Double> tmpS = tmpInvQAT.multiplyLeft(tmpA);

                myInverterS.compute(tmpS);
                if (tmpSolvable = myInverterS.isSolvable()) {

                    final MatrixStore<Double> tmpL = myInverterS.solve(tmpInvQC.multiplyLeft(tmpA).add(tmpB.negate()));
                    final MatrixStore<Double> tmpX = myInverterQ.solve(tmpC.add(tmpL.multiplyLeft(tmpA.transpose()).negate()));

                    return new Output(tmpX, tmpL, tmpSolvable);

                } else {

                    return this.solve(input, MODIFIED);
                }

            } else {

                return this.solve(input, model + 1);
            }

        } else {

            final double tmpLargest = tmpQ.aggregateAll(Aggregator.LARGEST);
            final double tmpRelativelySmall = MACHINE_DOUBLE_ERROR * tmpLargest;
            final double tmpPracticalLimit = MACHINE_DOUBLE_ERROR + IS_ZERO;
            final double tmpSmallToAdd = Math.max(tmpRelativelySmall, tmpPracticalLimit);

            final MatrixStore<Double> tmpModQ = IdentityStore.makePrimitive((int) tmpQ.countRows()).scale(tmpSmallToAdd);

            final MatrixStore<Double> tmpKKT = input.getKKT().builder().superimpose(tmpModQ).build();
            final MatrixStore<Double> tmpRHS = input.getRHS();

            myInverterS.compute(tmpKKT);
            if (tmpSolvable = myInverterS.isSolvable()) {

                final MatrixStore<Double> tmpXL = myInverterS.solve(tmpRHS);

                final int tmpSplit = (int) tmpQ.countColumns();

                final Builder<Double> tmpX = tmpXL.builder().rows(0, tmpSplit);
                final Builder<Double> tmpL = tmpXL.builder().rows(tmpSplit, (int) tmpKKT.countColumns());

                return new Output(tmpX.build(), tmpL.build(), tmpSolvable);

            } else {

                return new Output(null, null, tmpSolvable);
            }

        }

    }

}
