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

import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Options;

public abstract class KKTSystem {

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

        final long countConstraints() {
            return myB != null ? myB.count() : 0L;
        }

        final long countVariables() {
            return myC.count();
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

        Output() {

            super();

            myX = null;
            myL = null;
            mySolvable = false;
        }

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

    public static final Output FAILURE = new Output();

    private static final Options DEFAULT_OPTIONS = new Optimisation.Options();

    protected KKTSystem() {
        super();
    }

    public final KKTSystem.Output solve(final KKTSystem.Input input) {
        return this.solve(input, DEFAULT_OPTIONS);
    }

    public abstract KKTSystem.Output solve(final KKTSystem.Input input, final Optimisation.Options options);

}
