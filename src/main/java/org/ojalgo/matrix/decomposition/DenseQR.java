/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.MACHINE_SMALLEST;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.matrix.operation.GEQRF;
import org.ojalgo.matrix.operation.ORGQR;
import org.ojalgo.matrix.operation.ORMQR;
import org.ojalgo.matrix.operation.TRTRS;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.NumberDefinition;

abstract class DenseQR<N extends Comparable<N>, M extends PhysicalStore<N>> extends InPlaceDecomposition<N, M> implements QR<N> {

    static final class C128 extends Generic<ComplexNumber> {

        C128() {
            this(false);
        }

        C128(final boolean fullSize) {
            super(GenericStore.C128, fullSize);
        }

    }

    /**
     * The factorisation is done by {@link GEQRF}. Q is formed by {@link ORGQR} and applied by {@link ORMQR},
     * and systems with R are solved by {@link TRTRS}.
     */
    abstract static class Generic<N extends Scalar<N>> extends DenseQR<N, GenericStore<N>> {

        /**
         * [A]=[Q][R] — Q from the reflectors stored below the diagonal and their tau, as returned by
         * {@link GEQRF}.
         * <p>
         * ftran applies Q<sup>H</sup>. btran applies Q.
         */
        static final class FactorQ<N extends Scalar<N>> implements MatrixDecomposition.Factor<N> {

            private final GenericStore<N> myBody;
            private final boolean myFullSize;
            private final int myMinDim;
            private final N[] myTau;

            FactorQ(final GenericStore<N> body, final N[] tau, final int minDim, final boolean fullSize) {
                super();
                myBody = body;
                myTau = tau;
                myMinDim = minDim;
                myFullSize = fullSize;
            }

            @Override
            public void btran(final double[] arg) {
                PhysicalStore<N> x = myBody.physical().column(arg);
                this.btran(x);
                x.supplyTo(arg);
            }

            @Override
            public void btran(final PhysicalStore<N> arg) {
                this.multiply(false, arg);
            }

            @Override
            public void ftran(final double[] arg) {
                PhysicalStore<N> x = myBody.physical().column(arg);
                this.ftran(x);
                x.supplyTo(arg);
            }

            @Override
            public void ftran(final PhysicalStore<N> arg) {
                this.multiply(true, arg);
            }

            @Override
            public MatrixStore<N> get() {

                int m = myBody.getRowDim();

                GenericStore<N> retVal = myBody.physical().make(m, myFullSize ? m : myMinDim);

                System.arraycopy(myBody.data, 0, retVal.data, 0, m * myMinDim);

                ORGQR.invoke(m, retVal.getColDim(), myMinDim, retVal.data, 0, Math.max(1, m), myTau, 0, myBody.physical().scalar());

                return retVal;
            }

            @Override
            public int getColDim() {
                return myBody.getRowDim();
            }

            @Override
            public int getRowDim() {
                return myBody.getRowDim();
            }

            /**
             * The first m rows of [X] are multiplied; other stores are multiplied via a {@link GenericStore}
             * copy.
             */
            private void multiply(final boolean transpose, final PhysicalStore<N> arg) {

                int m = myBody.getRowDim();

                if (arg instanceof GenericStore) {
                    GenericStore<N> store = (GenericStore<N>) arg;
                    ORMQR.invoke(transpose, m, store.getColDim(), myMinDim, myBody.data, 0, Math.max(1, m), myTau, 0, store.data, 0,
                            Math.max(1, store.getRowDim()));
                } else {
                    GenericStore<N> copy = myBody.physical().copy(arg);
                    this.multiply(transpose, copy);
                    copy.supplyTo(arg);
                }
            }

        }

        /**
         * tau of the reflectors, as returned by {@link GEQRF}
         */
        private N[] myTau = null;

        Generic(final GenericStore.Factory<N> factory, final boolean fullSize) {
            super(factory, fullSize);
        }

        @Override
        public int countSignificant(final double threshold) {

            GenericStore<N> internal = this.getInPlace();

            int significant = 0;
            for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
                if (internal.get(ij, ij).norm() > threshold) {
                    significant++;
                }
            }

            return significant;
        }

        @Override
        int factorise(final GenericStore<N> inPlace) {

            int m = this.getRowDim();
            int r = this.getMinDim();

            if (myTau == null || myTau.length != r) {
                myTau = this.scalar().newArrayInstance(r);
            }

            GEQRF.invoke(m, this.getColDim(), inPlace.data, 0, Math.max(1, m), myTau, 0, this.scalar());

            int retVal = 0;
            for (int k = 0; k < r; k++) {
                if (!myTau[k].isZero()) {
                    retVal++;
                }
            }
            return retVal;
        }

        @Override
        MatrixDecomposition.Factor<N> getFactorQ() {
            return new FactorQ<>(this.getInPlace(), myTau, this.getMinDim(), this.isFullSize());
        }

        @Override
        void substitute(final boolean transpose, final PhysicalStore<N> arg) {

            int m = this.getRowDim();

            if (arg instanceof GenericStore && arg.getRowDim() == m) {
                GenericStore<N> store = (GenericStore<N>) arg;
                int info = TRTRS.invoke(true, transpose, false, this.getMinDim(), store.getColDim(), this.getInPlace().data, 0, Math.max(1, m), store.data, 0,
                        Math.max(1, m), this.scalar());
                if (info == 0) {
                    return;
                }
            }

            // Also when R is exactly singular, and nothing was solved: the generic code then divides by zero
            super.substitute(transpose, arg);
        }

    }

    static final class H256 extends Generic<Quaternion> {

        H256() {
            this(false);
        }

        H256(final boolean fullSize) {
            super(GenericStore.H256, fullSize);
        }

    }

    static final class Q128 extends Generic<RationalNumber> {

        Q128() {
            this(false);
        }

        Q128(final boolean fullSize) {
            super(GenericStore.Q128, fullSize);
        }

    }

    static final class R064 extends DenseQR<Double, R064Store> {

        /**
         * [A]=[Q][R] — Q from the reflectors stored below the diagonal and their tau, as returned by
         * {@link GEQRF}.
         * <p>
         * ftran applies Q^T. btran applies Q.
         */
        static final class FactorQ extends AbstractDecomposition.PrimitiveFactor {

            private final double[] myData;
            private final boolean myFullSize;
            private final int myM;
            private final int myMinDim;
            private final double[] myTau;

            FactorQ(final double[] data, final double[] tau, final int m, final int minDim, final boolean fullSize) {
                super();
                myData = data;
                myTau = tau;
                myM = m;
                myMinDim = minDim;
                myFullSize = fullSize;
            }

            @Override
            public void btran(final double[] arg) {
                ORMQR.invoke(false, myM, 1, myMinDim, myData, 0, Math.max(1, myM), myTau, 0, arg, 0, Math.max(1, myM));
            }

            @Override
            public void ftran(final double[] arg) {
                ORMQR.invoke(true, myM, 1, myMinDim, myData, 0, Math.max(1, myM), myTau, 0, arg, 0, Math.max(1, myM));
            }

            @Override
            public MatrixStore<Double> get() {

                R064Store retVal = R064Store.FACTORY.make(myM, myFullSize ? myM : myMinDim);

                System.arraycopy(myData, 0, retVal.data, 0, myM * myMinDim);

                ORGQR.invoke(myM, retVal.getColDim(), myMinDim, retVal.data, 0, Math.max(1, myM), myTau, 0);

                return retVal;
            }

            @Override
            public int getColDim() {
                return myM;
            }

            @Override
            public int getRowDim() {
                return myM;
            }

        }

        /**
         * tau of the reflectors, as returned by {@link GEQRF}
         */
        private double[] myTau = null;

        R064() {
            this(false);
        }

        R064(final boolean fullSize) {
            super(R064Store.FACTORY, fullSize);
        }

        @Override
        public int countSignificant(final double threshold) {

            R064Store internal = this.getInPlace();

            int significant = 0;
            for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
                if (Math.abs(internal.doubleValue(ij, ij)) > threshold) {
                    significant++;
                }
            }

            return significant;
        }

        @Override
        int factorise(final R064Store inPlace) {

            int m = this.getRowDim();
            int r = this.getMinDim();

            if (myTau == null || myTau.length != r) {
                myTau = new double[r];
            }

            GEQRF.invoke(m, this.getColDim(), inPlace.data, 0, Math.max(1, m), myTau, 0);

            int retVal = 0;
            for (int k = 0; k < r; k++) {
                if (myTau[k] != ZERO) {
                    retVal++;
                }
            }
            return retVal;
        }

        @Override
        MatrixDecomposition.Factor<Double> getFactorQ() {
            return new FactorQ(this.getInPlace().data, myTau, this.getRowDim(), this.getMinDim(), this.isFullSize());
        }

        /**
         * The first m rows of [X] are multiplied; other stores are multiplied via an {@link R064Store} copy.
         */
        @Override
        void reflect(final boolean transpose, final PhysicalStore<Double> arg) {

            int m = this.getRowDim();

            if (arg instanceof R064Store) {
                R064Store store = (R064Store) arg;
                ORMQR.invoke(transpose, m, store.getColDim(), this.getMinDim(), this.getInPlace().data, 0, Math.max(1, m), myTau, 0, store.data, 0,
                        Math.max(1, store.getRowDim()));
            } else {
                R064Store copy = R064Store.FACTORY.copy(arg);
                this.reflect(transpose, copy);
                copy.supplyTo(arg);
            }
        }

        @Override
        void substitute(final boolean transpose, final PhysicalStore<Double> arg) {

            int m = this.getRowDim();

            if (arg instanceof R064Store && arg.getRowDim() == m) {
                R064Store store = (R064Store) arg;
                int info = TRTRS.invoke(true, transpose, false, this.getMinDim(), store.getColDim(), this.getInPlace().data, 0, Math.max(1, m), store.data, 0,
                        Math.max(1, m));
                if (info == 0) {
                    return;
                }
            }

            // Also when R is exactly singular, and nothing was solved: the generic code then divides by zero
            super.substitute(transpose, arg);
        }

    }

    static final class R128 extends Generic<Quadruple> {

        R128() {
            this(false);
        }

        R128(final boolean fullSize) {
            super(GenericStore.R128, fullSize);
        }

    }

    /**
     * [A]=[Q][R] — Upper triangular factor R stored in the upper part of the in-place body.
     * <p>
     * ftran solves [R][x]=[b] via back-substitution. btran solves [R]^T[x]=[b] via forward-substitution.
     */
    static final class FactorR<N extends Comparable<N>> implements MatrixDecomposition.Factor<N> {

        private final PhysicalStore<N> myBody;
        private final boolean myFullSize;

        FactorR(final PhysicalStore<N> body, final boolean fullSize) {
            super();
            myBody = body;
            myFullSize = fullSize;
        }

        @Override
        public void btran(final double[] arg) {
            myBody.substituteForwards(true, false, arg);
        }

        @Override
        public void btran(final PhysicalStore<N> arg) {
            myBody.substituteForwards(true, false, arg);
        }

        @Override
        public void ftran(final double[] arg) {
            myBody.substituteBackwards(false, false, arg);
        }

        @Override
        public void ftran(final PhysicalStore<N> arg) {
            myBody.substituteBackwards(false, false, arg);
        }

        @Override
        public MatrixStore<N> get() {

            MatrixStore<N> logical = myBody.triangular(true, false);

            int nbRows = myBody.getRowDim();
            int nbCols = myBody.getColDim();

            if (!myFullSize && nbRows > nbCols) {
                return logical.limits(nbCols, -1);
            }

            return logical;
        }

        @Override
        public int getColDim() {
            return myBody.getColDim();
        }

        @Override
        public int getRowDim() {
            return myBody.getMinDim();
        }

    }

    private final boolean myFullSize;
    private int myNumberOfHouseholderTransformations = 0;
    private transient MatrixStore<N> myQ = null;

    protected DenseQR(final PhysicalStore.Factory<N, ? extends M> factory, final boolean fullSize) {
        super(factory);
        myFullSize = fullSize;
    }

    @Override
    public void btran(final double[] arg) {
        PhysicalStore<N> x = this.copyColumn(arg);
        this.btran(x);
        x.supplyTo(arg);
    }

    @Override
    public void btran(final PhysicalStore<N> arg) {
        this.substitute(true, arg);
        this.reflect(false, arg);
    }

    @Override
    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    @Override
    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {

        this.reset();

        M tmpStore = this.setInPlace(matrix);

        myNumberOfHouseholderTransformations = this.factorise(tmpStore);

        return this.computed(true);
    }

    @Override
    public void ftran(final double[] arg) {
        PhysicalStore<N> x = this.copyColumn(arg);
        this.ftran(x);
        x.supplyTo(arg);
    }

    @Override
    public void ftran(final PhysicalStore<N> arg) {
        this.reflect(true, arg);
        this.substitute(false, arg);
    }

    @Override
    public N getDeterminant() {

        AggregatorFunction<N> aggregator = this.aggregator().product();

        this.getInPlace().visitDiagonal(aggregator);

        if (myNumberOfHouseholderTransformations % 2 != 0) {
            return this.scalar().one().negate().multiply(aggregator.get()).get();
        }

        return aggregator.get();
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {
        return this.getSolution(this.makeIdentity(this.getRowDim()), preallocated);
    }

    /**
     * [A]=[Q][R]
     */
    @Override
    public List<InvertibleFactor<N>> getFactors() {
        return List.of(this.getFactorQ(), this.getFactorR());
    }

    @Override
    public MatrixStore<N> getQ() {
        if (myQ == null) {
            myQ = this.getFactorQ().get();
        }
        return myQ;
    }

    @Override
    public MatrixStore<N> getR() {
        return this.getFactorR().get();
    }

    @Override
    public double getRankThreshold() {

        N largest = this.getInPlace().aggregateDiagonal(Aggregator.LARGEST);
        double epsilon = this.getDimensionalEpsilon();

        return epsilon * Math.max(MACHINE_SMALLEST, NumberDefinition.doubleValue(largest));
    }

    /**
     * Solve [A]*[X]=[B] by first solving [Q]*[Y]=[B] and then [R]*[X]=[Y]. [X] minimises the 2-norm of
     * [Q]*[R]*[X]-[B].
     *
     * @param rhs The right hand side [B]
     * @return [X] "preallocated" is used to form the results, but the solution is in the returned
     *         MatrixStore.
     */
    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        rhs.supplyTo(preallocated);

        this.reflect(true, preallocated);
        this.substitute(false, preallocated);

        int m = this.getRowDim();
        int n = this.getColDim();

        if (n < m) {
            return preallocated.limits(n, preallocated.getColDim());
        } else if (n > m) {
            return preallocated.below(n - m);
        } else {
            return preallocated;
        }
    }

    @Override
    public MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse();
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    @Override
    public MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    @Override
    public boolean isFullSize() {
        return myFullSize;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public PhysicalStore<N> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public void reset() {

        super.reset();

        myNumberOfHouseholderTransformations = 0;
        myQ = null;
    }

    @Override
    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    @Override
    protected boolean checkSolvability() {
        return this.isAspectRatioNormal() && this.isFullRank();
    }

    /**
     * Factorises the in-place matrix, [A]=[Q][R], and returns the number of Householder reflections (those
     * that are not the identity).
     */
    abstract int factorise(M inPlace);

    /**
     * [A]=[Q][R]
     */
    abstract MatrixDecomposition.Factor<N> getFactorQ();

    /**
     * [A]=[Q][R]
     */
    MatrixDecomposition.Factor<N> getFactorR() {
        return new FactorR<>(this.getInPlace(), myFullSize);
    }

    /**
     * Multiplies [X] by [Q]<sup>T</sup>, or by [Q] when not transposed, in place, for all columns of [X].
     */
    void reflect(final boolean transpose, final PhysicalStore<N> arg) {
        if (transpose) {
            this.getFactorQ().ftran(arg);
        } else {
            this.getFactorQ().btran(arg);
        }
    }

    /**
     * Solves [R][X]=[B], or [R]<sup>T</sup>[X]=[B] when transposed, in place, for all columns of [B]. Only
     * the first min(m,n) rows of [B] are solved for; the others are left as they are.
     */
    void substitute(final boolean transpose, final PhysicalStore<N> arg) {

        M body = this.getInPlace();

        if (transpose) {
            arg.substituteForwards(body, false, true, false);
        } else {
            arg.substituteBackwards(body, false, false, false);
        }
    }

    /**
     * @return L as in R<sup>T</sup>.
     */
    protected PhysicalStore<N> getL() {

        int tmpRowDim = this.getColDim();
        int tmpColDim = this.getMinDim();

        PhysicalStore<N> retVal = this.makeZero(tmpRowDim, tmpColDim);

        PhysicalStore<N> tmpStore = this.getInPlace();
        for (int j = 0; j < tmpColDim; j++) {
            for (int i = j; i < tmpRowDim; i++) {
                retVal.set(i, j, tmpStore.get(j, i));
            }
        }

        return retVal;
    }

}
