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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.operation.ExchangeColumns;
import org.ojalgo.matrix.operation.SYEVD;
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
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.SortOrder;

/**
 * Eigenvalues and eigenvectors of a real matrix.
 * <P>
 * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is diagonal and the eigenvector matrix V
 * is orthogonal. I.e. A = V.times(D.times(V.transpose())) and V.times(V.transpose()) equals the identity
 * matrix.
 * <P>
 * If A is not symmetric, then the eigenvalue matrix D is block diagonal with the real eigenvalues in 1-by-1
 * blocks and any complex eigenvalues, lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda]. The columns
 * of V represent the eigenvectors in the sense that A*V = V*D, i.e. A.times(V) equals V.times(D). The matrix
 * V may be badly conditioned, or even singular, so the validity of the equation A = V*D*inverse(V) depends
 * upon V.cond().
 **/
abstract class HermitianEvD<N extends Comparable<N>, M extends PhysicalStore<N>> extends DenseEigenvalue<N, M> implements Eigenvalue.Spectral<N> {

    static final class C128 extends Generic<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

        @Override
        public Eigenpair getEigenpair(final int index) {

            ComplexNumber value = this.getD().get(index, index);
            Access1D<ComplexNumber> vector = this.getV().sliceColumn(index);

            return new Eigenpair(value, vector);
        }

    }

    /**
     * The eigenvalues and eigenvectors are computed by {@link SYEVD}.
     */
    abstract static class Generic<N extends Scalar<N>> extends HermitianEvD<N, GenericStore<N>> {

        Generic(final GenericStore.Factory<N> factory) {
            super(factory);
        }

        @Override
        int compute(final Collectable<N, ? super TransformableRegion<N>> matrix, final GenericStore<N> inPlace, final double[] values,
                final boolean valuesOnly) {
            int n = inPlace.getRowDim();
            matrix.supplyTo(inPlace);
            return SYEVD.invoke(!valuesOnly, n, inPlace.data, 0, Math.max(1, n), values, 0, this.scalar());
        }

    }

    static final class H256 extends Generic<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

    }

    static final class Q128 extends Generic<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

    }

    /**
     * The eigenvalues and eigenvectors are computed by {@link SYEVD}.
     */
    static final class R064 extends HermitianEvD<Double, R064Store> {

        R064() {
            super(R064Store.FACTORY);
        }

        /**
         * An {@link R064Store} matrix is read where it is, not copied to the in-place store first.
         */
        @Override
        int compute(final Collectable<Double, ? super TransformableRegion<Double>> matrix, final R064Store inPlace, final double[] values,
                final boolean valuesOnly) {
            int n = inPlace.getRowDim();
            if (matrix instanceof R064Store source) {
                return SYEVD.invoke(!valuesOnly, n, source.data, 0, Math.max(1, n), values, 0, inPlace.data, 0, Math.max(1, n));
            }
            matrix.supplyTo(inPlace);
            return SYEVD.invoke(!valuesOnly, n, inPlace.data, 0, Math.max(1, n), values, 0);
        }

    }

    static final class R128 extends Generic<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

    }

    private double[] d;
    /**
     * The imaginary parts of the eigenvalues, all zero
     */
    private double[] e;
    /**
     * The matrix, overwritten with the eigenvectors
     */
    private transient M myInPlace = null;
    private transient MatrixStore<N> myInverse;
    private transient MatrixStore<N> myS = null;
    private transient Array1D<Double> mySingularValues = null;
    private transient MatrixStore<N> myU = null;

    protected HermitianEvD(final PhysicalStore.Factory<N, ? extends M> factory) {
        super(factory);
    }

    @Override
    public void btran(final double[] arg) {
        this.ftran(arg);
    }

    @Override
    public void btran(final PhysicalStore<N> arg) {
        this.ftran(arg);
    }

    @Override
    public boolean checkAndDecompose(final MatrixStore<N> matrix) {
        if (matrix.isHermitian()) {
            return this.decompose(matrix);
        }
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return false;
    }

    @Override
    public int countSignificant(final double threshold) {

        int significant = 0;
        for (int i = 0; i < d.length; i++) {
            if (Math.abs(d[i]) > threshold) {
                significant++;
            }
        }
        return significant;
    }

    @Override
    public void ftran(final double[] arg) {

        MatrixStore<N> mV = this.getV();

        int rank = this.getRank();
        int dim = arg.length;

        double[] work = new double[rank];
        for (int i = 0; i < rank; i++) {
            double sum = ZERO;
            for (int j = 0; j < dim; j++) {
                sum += mV.doubleValue(j, i) * arg[j];
            }
            work[i] = sum / d[i];
        }

        java.util.Arrays.fill(arg, ZERO);
        for (int i = 0; i < rank; i++) {
            double wi = work[i];
            for (int j = 0; j < dim; j++) {
                arg[j] += mV.doubleValue(j, i) * wi;
            }
        }
    }

    @Override
    public void ftran(final PhysicalStore<N> arg) {
        arg.fillByMultiplying(this.getInverse(), arg.copy());
    }

    @Override
    public double getCondition() {
        double largest = Math.abs(d[0]);
        double smallest = Math.abs(d[d.length - 1]);
        return largest / smallest;
    }

    @Override
    public MatrixStore<N> getCovariance() {

        MatrixStore<N> v = this.getV();
        Access1D<N> values = this.getD().sliceDiagonal();

        int rank = this.getRank();

        BinaryFunction<N> divide = this.function().divide();

        MatrixStore<N> tmp = v.limits(-1, rank).onColumns(divide, values).collect(v.physical());

        return tmp.multiply(tmp.conjugate());
    }

    @Override
    public N getDeterminant() {

        AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return this.scalar().cast(tmpVisitor.get());
    }

    @Override
    public void getEigenvalues(final double[] realParts, final Optional<double[]> imaginaryParts) {

        int length = realParts.length;

        System.arraycopy(d, 0, realParts, 0, length);

        if (imaginaryParts.isPresent()) {
            System.arraycopy(e, 0, imaginaryParts.get(), 0, length);
        }
    }

    @Override
    public List<InvertibleFactor<N>> getFactors() {
        if (this.isValuesOnly()) {
            return List.of(this);
        }
        MatrixStore<N> mV = this.getV();
        if (mV == null) {
            return List.of(this);
        }
        int rank = this.getRank();
        int dim = d.length;
        return List.of(new DenseSingularValue.FactorUT<>(mV, rank, dim), new DenseSingularValue.FactorSinv<>(d, rank, dim),
                new DenseSingularValue.FactorV<>(mV, rank, dim));
    }

    @Override
    public double getFrobeniusNorm() {

        double retVal = ZERO;

        double tmpVal;
        for (int i = d.length - 1; i >= 0; i--) {
            tmpVal = d[i];
            retVal += tmpVal * tmpVal;
        }

        return SQRT.invoke(retVal);
    }

    @Override
    public MatrixStore<N> getInverse() {

        if (myInverse == null) {
            int dim = this.getRowDim();
            PhysicalStore<N> preallocated = this.preallocate(dim, dim, dim);
            myInverse = SingularValue.invert(this, preallocated);
        }

        return myInverse;
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        if (myInverse != null) {
            return myInverse;
        }

        // Not cached – preallocated belongs to the caller, and may be reused for something else
        return SingularValue.invert(this, preallocated);
    }

    @Override
    public double getKyFanNorm(final int k) {

        double retVal = ZERO;

        for (int i = Math.min(d.length, k) - 1; i >= 0; i--) {
            retVal += Math.abs(d[i]);
        }

        return retVal;
    }

    @Override
    public double getOperatorNorm() {
        return Math.abs(d[0]);
    }

    @Override
    public SortOrder getOrder() {
        return SortOrder.DESCENDING;
    }

    @Override
    public double getRankThreshold() {
        return Math.max(MACHINE_SMALLEST, Math.abs(d[0])) * this.getDimensionalEpsilon();
    }

    /**
     * If there are no negative eigenvalues this method will simply reuse the eigenvalue diagonal (D)
     */
    @Override
    public MatrixStore<N> getS() {

        if (this.isComputed() && myS == null) {

            boolean negative = false;
            for (int i = 0; i < d.length && !negative; i++) {
                negative = d[i] < ZERO;
            }

            if (negative) {
                double[] abs = new double[d.length];
                for (int i = 0; i < d.length; i++) {
                    abs[i] = Math.abs(d[i]);
                }
                myS = this.makeDiagonal(ArrayR064.wrap(abs)).get();
            } else {
                myS = this.getD();
            }
        }

        return myS;
    }

    @Override
    public Array1D<Double> getSingularValues() {

        if (this.isComputed() && mySingularValues == null) {
            double[] abs = new double[d.length];
            for (int i = 0; i < d.length; i++) {
                abs[i] = Math.abs(d[i]);
            }
            mySingularValues = Array1D.R064.wrap(ArrayR064.wrap(abs));
        }

        return mySingularValues;
    }

    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        MatrixStore<N> mRHS = this.collect(rhs);

        if (myInverse != null) {
            preallocated.fillByMultiplying(myInverse, mRHS);
            return preallocated;
        }

        if (this.isComputed() && !this.isValuesOnly()) {

            return SingularValue.solve(this, mRHS, preallocated);

        } else {

            throw new IllegalStateException();
        }
    }

    @Override
    public ComplexNumber getTrace() {

        double sum = ZERO;
        if (d != null) {
            for (double v : d) {
                sum += v;
            }
        }

        return ComplexNumber.valueOf(sum);
    }

    @Override
    public double getTraceNorm() {
        return this.getKyFanNorm(d.length);
    }

    /**
     * For SPD matrices U == V; for indefinite we must absorb eigenvalue sign into U
     */
    @Override
    public MatrixStore<N> getU() {

        if (this.isComputed() && myU == null) {

            boolean negative = false;
            for (int i = 0; i < d.length && !negative; i++) {
                negative = d[i] < ZERO;
            }

            MatrixStore<N> v = this.getV();

            if (negative) {
                PhysicalStore<N> u = v.copy();
                UnaryFunction<N> negate = this.function().negate();
                for (int j = 0; j < d.length; j++) {
                    if (d[j] < ZERO) {
                        u.modifyColumn(0L, j, negate);
                    }
                }
                myU = u;
            } else {
                myU = v;
            }
        }

        return myU;
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
        return true;
    }

    @Override
    public boolean isHermitian() {
        return true;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public boolean isSPD() {

        if (this.isComputed()) {
            for (double value : d) {
                if (value <= 0) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public PhysicalStore<N> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public MatrixStore<N> reconstruct() {
        return super.reconstruct();
    }

    @Override
    public void reset() {

        super.reset();

        myInverse = null;
        myS = null; // clear adjusted caches
        myU = null;
        mySingularValues = null;
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
        return this.isComputed() && this.isHermitian();
    }

    @Override
    protected boolean doDecompose(final Collectable<N, ? super TransformableRegion<N>> matrix, final boolean valuesOnly) {

        int size = (int) matrix.countRows();

        if (myInPlace == null || myInPlace.getRowDim() != size || myInPlace.getColDim() != size) {
            myInPlace = this.makeZero(size, size);
        }

        if (d == null || d.length != size) {
            d = new double[size];
            e = new double[size];
        }

        if (this.compute(matrix, myInPlace, d, valuesOnly) != 0) {
            return this.computed(false);
        }

        if (this.isOrdered()) {
            ExchangeColumns tmpExchangeColumns = valuesOnly ? ExchangeColumns.NULL : myInPlace::exchangeColumns;
            Eigenvalue.sort(d, tmpExchangeColumns);
        }

        if (!valuesOnly) {
            this.setV(myInPlace);
        }

        return this.computed(true);
    }

    @Override
    protected MatrixStore<N> makeD() {
        return this.makeDiagonal(ArrayR064.wrap(d)).get();
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {

        int length = d.length;

        Array1D<ComplexNumber> retVal = Array1D.C128.make(length);

        for (int ij = 0; ij < length; ij++) {
            retVal.set(ij, ComplexNumber.valueOf(d[ij]));
        }

        return retVal;
    }

    @Override
    protected MatrixStore<N> makeV() {
        return null;
    }

    /**
     * Writes the eigenvalues of the matrix, in ascending order, to values, and its eigenvectors (unless
     * values only) to the in-place store. The matrix is not changed.
     *
     * @return 0, or not if the computation failed
     */
    abstract int compute(Collectable<N, ? super TransformableRegion<N>> matrix, M inPlace, double[] values, boolean valuesOnly);

}
