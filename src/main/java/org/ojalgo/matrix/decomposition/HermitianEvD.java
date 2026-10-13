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

import java.util.Arrays;
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayC128;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
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

        int[] significant = this.getSignificant();
        int dim = arg.length;

        double[] work = new double[significant.length];
        for (int k = 0; k < significant.length; k++) {
            int i = significant[k];
            double sum = ZERO;
            for (int j = 0; j < dim; j++) {
                sum += mV.doubleValue(j, i) * arg[j];
            }
            work[k] = sum / d[i];
        }

        Arrays.fill(arg, ZERO);
        for (int k = 0; k < significant.length; k++) {
            int i = significant[k];
            double wk = work[k];
            for (int j = 0; j < dim; j++) {
                arg[j] += mV.doubleValue(j, i) * wk;
            }
        }
    }

    @Override
    public void ftran(final PhysicalStore<N> arg) {
        arg.fillByMultiplying(this.getInverse(), arg.copy());
    }

    @Override
    public double getCondition() {
        Array1D<Double> values = this.getSingularValues();
        return values.doubleValue(0) / values.doubleValue(values.size() - 1);
    }

    @Override
    public MatrixStore<N> getCovariance() {

        int[] significant = this.getSignificant();

        MatrixStore<N> v = this.getV().columns(significant);
        Access1D<N> values = this.getD().rows(significant).columns(significant).sliceDiagonal();

        BinaryFunction<N> divide = this.function().divide();

        MatrixStore<N> tmp = v.onColumns(divide, values).collect(v.physical());

        return tmp.multiply(tmp.conjugate());
    }

    @Override
    public N getDeterminant() {

        AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return this.scalar().cast(tmpVisitor.get());
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
        int[] significant = this.getSignificant();
        int rank = significant.length;
        int dim = d.length;
        double[] values = d;
        if (rank < dim) {
            mV = mV.columns(significant);
            values = new double[rank];
            for (int k = 0; k < rank; k++) {
                values[k] = d[significant[k]];
            }
        }
        return List.of(new DenseSingularValue.FactorUT<>(mV, rank, dim), new DenseSingularValue.FactorSinv<>(values, rank, dim),
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

        Array1D<Double> values = this.getSingularValues();

        double retVal = ZERO;

        for (int i = Math.min(d.length, k) - 1; i >= 0; i--) {
            retVal += values.doubleValue(i);
        }

        return retVal;
    }

    @Override
    public double getOperatorNorm() {
        // d is in ascending order
        return Math.max(Math.abs(d[0]), Math.abs(d[d.length - 1]));
    }

    @Override
    public SortOrder getOrder() {
        return SortOrder.UNSORTED;
    }

    @Override
    public double getRankThreshold() {
        return Math.max(MACHINE_SMALLEST, this.getOperatorNorm()) * this.getDimensionalEpsilon();
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
            // d is in ascending order, so the largest remaining |d| is always at one of the ends
            double[] abs = new double[d.length];
            for (int i = 0, first = 0, last = d.length - 1; first <= last; i++) {
                if (Math.abs(d[first]) > Math.abs(d[last])) {
                    abs[i] = Math.abs(d[first++]);
                } else {
                    abs[i] = Math.abs(d[last--]);
                }
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

    /**
     * The columns of [D] and [V] with the significant eigenvalues, those counted by {@link #getRank()}.
     */
    private int[] getSignificant() {

        double threshold = this.getRankThreshold();

        int[] retVal = new int[this.countSignificant(threshold)];
        for (int i = 0, k = 0; i < d.length; i++) {
            if (Math.abs(d[i]) > threshold) {
                retVal[k++] = i;
            }
        }
        return retVal;
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
        }

        if (this.compute(matrix, myInPlace, d, valuesOnly) != 0) {
            return this.computed(false);
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

        ArrayC128 retVal = ArrayC128.make(length);

        for (int ij = 0; ij < length; ij++) {
            retVal.data[ij] = ComplexNumber.valueOf(d[ij]);
        }
        Arrays.sort(retVal.data, Eigenvalue.DESCENDING_NORM);

        return Array1D.C128.wrap(retVal);
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
