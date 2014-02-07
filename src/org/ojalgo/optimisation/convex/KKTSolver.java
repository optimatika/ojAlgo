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

import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.CholeskyDecomposition;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.decomposition.SingularValueDecomposition;
import org.ojalgo.matrix.store.MatrixStore;

public abstract class KKTSolver {

    public static final class Data {

        public final MatrixStore<Double> myQ;
        public final MatrixStore<Double> myA;
        public final MatrixStore<Double> myB;
        public final MatrixStore<Double> myC;

        /**
         * | Q | A<sup>T</sup> | = | C | <br>
         * | A | 0 | = | B |
         * 
         * @param Q
         * @param C
         * @param A
         * @param B
         */
        protected Data(final MatrixStore<Double> Q, final MatrixStore<Double> C, final MatrixStore<Double> A, final MatrixStore<Double> B) {

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

        @SuppressWarnings("unchecked")
        final MatrixStore<Double> getKKT() {
            return myQ.builder().right(myA.transpose()).below(myA).build();
        }

        final MatrixStore<Double> getQ() {
            return myQ;
        }

        final MatrixStore<Double> getRHS() {
            return myC.builder().below(myB).build();
        }

    }

    static class EliminationSolver extends KKTSolver {

        EliminationSolver(final MatrixStore<Double> Q, final MatrixStore<Double> C, final MatrixStore<Double> A, final MatrixStore<Double> B) {

            super(Q, C, A, B);

        }

        @Override
        protected MatrixStore<Double> solve() {

            final Cholesky<Double> tmpCholesky = CholeskyDecomposition.make(this.getQ());
            tmpCholesky.compute(this.getQ());

            final MatrixStore<Double> tmpAT = tmpCholesky.solve(this.getA().transpose());
            final MatrixStore<Double> tmpC = tmpCholesky.solve(this.getC().builder().modify(PrimitiveFunction.NEGATE).build());

            final MatrixStore<Double> tmpSchurComplement = tmpAT.multiplyLeft(this.getA()).builder().modify(PrimitiveFunction.NEGATE).build();

            return null;
        }

    }

    static class SafeSolver extends KKTSolver {

        private final SingularValue<Double> myDelegate;

        SafeSolver(final MatrixStore<Double> Q, final MatrixStore<Double> C, final MatrixStore<Double> A, final MatrixStore<Double> B) {

            super(Q, C, A, B);

            myDelegate = SingularValueDecomposition.make(this.getKKT());
        }

        @Override
        protected MatrixStore<Double> solve() {
            return myDelegate.solve(this.getRHS());
        }

    }

    private final MatrixStore<Double> myQ;
    private final MatrixStore<Double> myA;
    private final MatrixStore<Double> myB;
    private final MatrixStore<Double> myC;
    private final MatrixStore<Double> myKKT;
    private final MatrixStore<Double> myRHS;

    private KKTSolver() {
        this(null, null, null, null);
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
    protected KKTSolver(final MatrixStore<Double> Q, final MatrixStore<Double> C, final MatrixStore<Double> A, final MatrixStore<Double> B) {

        super();

        myQ = Q;
        myC = C;
        myA = A;
        myB = B;

        myKKT = myQ.builder().right(myA.builder().transpose().build()).below(myA).build();
        myRHS = myC.builder().below(myB).build();
    }

    protected abstract MatrixStore<Double> solve();

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
        return myKKT;
    }

    final MatrixStore<Double> getQ() {
        return myQ;
    }

    final MatrixStore<Double> getRHS() {
        return myRHS;
    }

}
