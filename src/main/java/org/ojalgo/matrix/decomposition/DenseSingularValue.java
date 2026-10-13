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

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.BasicArray;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.operation.BDSQR;
import org.ojalgo.matrix.operation.ExchangeColumns;
import org.ojalgo.matrix.operation.GESDD;
import org.ojalgo.matrix.operation.NegateColumn;
import org.ojalgo.matrix.operation.RotateColumns;
import org.ojalgo.matrix.operation.RotateRight;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.SortOrder;

abstract class DenseSingularValue<N extends Comparable<N>, M extends PhysicalStore<N>> extends AbstractDecomposition<N, M> implements SingularValue<N> {

    static final class C128 extends Generic<ComplexNumber> {

        C128() {
            this(false);
        }

        C128(final boolean fullSize) {
            super(GenericStore.C128, new DenseBidiagonal.C128(fullSize), fullSize);
        }

    }

    /**
     * Scale by [S]<sup>-1</sup> (reciprocal singular values).
     */
    static final class FactorSinv<N extends Comparable<N>> implements InvertibleFactor<N> {

        private final int myDim;
        private final int myRank;
        private final double[] mySingularValues;

        FactorSinv(final double[] singularValues, final int rank, final int dim) {
            super();
            mySingularValues = singularValues;
            myRank = rank;
            myDim = dim;
        }

        @Override
        public void btran(final double[] arg) {
            this.ftran(arg);
        }

        @Override
        public void btran(final PhysicalStore<N> arg) {
            double[] tmp = arg.toRawCopy1D();
            this.ftran(tmp);
            arg.fillMatching(tmp);
        }

        @Override
        public void ftran(final double[] arg) {
            for (int i = 0; i < myRank; i++) {
                arg[i] /= mySingularValues[i];
            }
        }

        @Override
        public void ftran(final PhysicalStore<N> arg) {
            double[] tmp = arg.toRawCopy1D();
            this.ftran(tmp);
            arg.fillMatching(tmp);
        }

        @Override
        public int getColDim() {
            return myDim;
        }

        @Override
        public int getRowDim() {
            return myDim;
        }

    }

    /**
     * Multiply by [U]<sup>T</sup>.
     */
    static final class FactorUT<N extends Comparable<N>> implements InvertibleFactor<N> {

        private final int myDim;
        private final MatrixStore<N> myMatrix;
        private final int myRank;

        FactorUT(final MatrixStore<N> u, final int rank, final int dim) {
            super();
            myMatrix = u;
            myRank = rank;
            myDim = dim;
        }

        @Override
        public void btran(final double[] arg) {
            double[] work = new double[myDim];
            for (int i = 0; i < myRank; i++) {
                double ai = arg[i];
                for (int j = 0; j < myDim; j++) {
                    work[j] += myMatrix.doubleValue(j, i) * ai;
                }
            }
            System.arraycopy(work, 0, arg, 0, myDim);
        }

        @Override
        public void btran(final PhysicalStore<N> arg) {
            double[] tmp = arg.toRawCopy1D();
            this.btran(tmp);
            arg.fillMatching(tmp);
        }

        @Override
        public void ftran(final double[] arg) {
            double[] work = new double[myRank];
            for (int i = 0; i < myRank; i++) {
                double sum = ZERO;
                for (int j = 0; j < myDim; j++) {
                    sum += myMatrix.doubleValue(j, i) * arg[j];
                }
                work[i] = sum;
            }
            System.arraycopy(work, 0, arg, 0, myRank);
            for (int i = myRank; i < myDim; i++) {
                arg[i] = ZERO;
            }
        }

        @Override
        public void ftran(final PhysicalStore<N> arg) {
            double[] tmp = arg.toRawCopy1D();
            this.ftran(tmp);
            arg.fillMatching(tmp);
        }

        @Override
        public int getColDim() {
            return myDim;
        }

        @Override
        public int getRowDim() {
            return myDim;
        }

    }

    /**
     * Multiply by [V].
     */
    static final class FactorV<N extends Comparable<N>> implements InvertibleFactor<N> {

        private final int myDim;
        private final MatrixStore<N> myMatrix;
        private final int myRank;

        FactorV(final MatrixStore<N> v, final int rank, final int dim) {
            super();
            myMatrix = v;
            myRank = rank;
            myDim = dim;
        }

        @Override
        public void btran(final double[] arg) {
            double[] work = new double[myRank];
            for (int i = 0; i < myRank; i++) {
                double sum = ZERO;
                for (int j = 0; j < myDim; j++) {
                    sum += myMatrix.doubleValue(j, i) * arg[j];
                }
                work[i] = sum;
            }
            System.arraycopy(work, 0, arg, 0, myRank);
            for (int i = myRank; i < myDim; i++) {
                arg[i] = ZERO;
            }
        }

        @Override
        public void btran(final PhysicalStore<N> arg) {
            double[] tmp = arg.toRawCopy1D();
            this.btran(tmp);
            arg.fillMatching(tmp);
        }

        @Override
        public void ftran(final double[] arg) {
            double[] work = new double[myDim];
            for (int i = 0; i < myRank; i++) {
                double ai = arg[i];
                for (int j = 0; j < myDim; j++) {
                    work[j] += myMatrix.doubleValue(j, i) * ai;
                }
            }
            System.arraycopy(work, 0, arg, 0, myDim);
        }

        @Override
        public void ftran(final PhysicalStore<N> arg) {
            double[] tmp = arg.toRawCopy1D();
            this.ftran(tmp);
            arg.fillMatching(tmp);
        }

        @Override
        public int getColDim() {
            return myDim;
        }

        @Override
        public int getRowDim() {
            return myDim;
        }

    }

    /**
     * The number types other than double: the matrix (or its conjugate transpose, if it has fewer rows than
     * columns) is reduced to bidiagonal form by {@link DenseBidiagonal}, and the bidiagonal matrix
     * diagonalised by {@link BDSQR}.
     */
    abstract static class Generic<N extends Scalar<N>> extends DenseSingularValue<N, GenericStore<N>> {

        private double[] e = null;
        private final DenseBidiagonal<N, GenericStore<N>> myBidiagonal;

        Generic(final PhysicalStore.Factory<N, GenericStore<N>> factory, final DenseBidiagonal<N, GenericStore<N>> bidiagonal, final boolean fullSize) {
            super(factory, fullSize);
            myBidiagonal = bidiagonal;
        }

        @Override
        int compute(final Collectable<N, ? super TransformableRegion<N>> matrix, final double[] values, final GenericStore<N> u, final GenericStore<N> v,
                final boolean fullSize) {

            boolean transposed = matrix.countRows() < matrix.countColumns();

            myBidiagonal.decompose(transposed ? this.collect(matrix).conjugate() : matrix);

            DiagonalStore<N, BasicArray<N>> tmpBidiagonal = myBidiagonal.doGetDiagonal();

            int size = tmpBidiagonal.getDimension();

            if (e == null || e.length != size) {
                e = new double[size];
            }

            tmpBidiagonal.supplyMainDiagonalTo(values);
            tmpBidiagonal.supplySuperdiagonalTo(e);

            boolean vectors = u != null;

            GenericStore<N> tmpQ1 = vectors ? myBidiagonal.doGetLQ() : null;
            GenericStore<N> tmpQ2 = vectors ? myBidiagonal.doGetRQ() : null;

            UnaryFunction<N> negate = this.function().negate();

            RotateColumns q1RotR = tmpQ1 != null ? (low, high, cos, sin) -> RotateRight.invokeGeneric(tmpQ1, low, high, cos, sin) : RotateColumns.NULL;
            RotateColumns q2RotR = tmpQ2 != null ? (low, high, cos, sin) -> RotateRight.invokeGeneric(tmpQ2, low, high, cos, sin) : RotateColumns.NULL;
            ExchangeColumns q1XchgCols = tmpQ1 != null ? tmpQ1::exchangeColumns : ExchangeColumns.NULL;
            ExchangeColumns q2XchgCols = tmpQ2 != null ? tmpQ2::exchangeColumns : ExchangeColumns.NULL;
            NegateColumn q2NegCol = tmpQ2 != null ? col -> tmpQ2.modifyColumn(0L, col, negate) : NegateColumn.NULL;

            BDSQR.invoke(values, e, q1RotR, q2RotR, q1XchgCols, q2XchgCols, q2NegCol);

            if (vectors) {
                u.fillMatching(transposed ? tmpQ2 : tmpQ1);
                v.fillMatching(transposed ? tmpQ1 : tmpQ2);
            }

            return 0;
        }

    }

    static final class H256 extends Generic<Quaternion> {

        H256() {
            this(false);
        }

        H256(final boolean fullSize) {
            super(GenericStore.H256, new DenseBidiagonal.H256(fullSize), fullSize);
        }

    }

    static final class Q128 extends Generic<RationalNumber> {

        Q128() {
            this(false);
        }

        Q128(final boolean fullSize) {
            super(GenericStore.Q128, new DenseBidiagonal.Q128(fullSize), fullSize);
        }

    }

    /**
     * {@link GESDD} on a copy of the matrix.
     */
    static final class R064 extends DenseSingularValue<Double, R064Store> {

        private transient R064Store myWork = null;

        R064() {
            this(false);
        }

        R064(final boolean fullSize) {
            super(R064Store.FACTORY, fullSize);
        }

        @Override
        int compute(final Collectable<Double, ? super TransformableRegion<Double>> matrix, final double[] values, final R064Store u, final R064Store v,
                final boolean fullSize) {

            int m = (int) matrix.countRows();
            int n = (int) matrix.countColumns();

            if (myWork == null || myWork.getRowDim() != m || myWork.getColDim() != n) {
                myWork = this.makeZero(m, n);
            }
            matrix.supplyTo(myWork);

            boolean vectors = u != null;

            return GESDD.invoke(vectors, fullSize, m, n, myWork.data, 0, Math.max(1, m), values, 0, vectors ? u.data : null, 0, Math.max(1, m),
                    vectors ? v.data : null, 0, Math.max(1, n));
        }

    }

    static final class R128 extends Generic<Quadruple> {

        R128() {
            this(false);
        }

        R128(final boolean fullSize) {
            super(GenericStore.R128, new DenseBidiagonal.R128(fullSize), fullSize);
        }

    }

    private int myColDim = 0;
    private final boolean myFullSize;
    private transient MatrixStore<N> myInverse = null;
    private int myRowDim = 0;
    private transient MatrixStore<N> myS = null;
    private transient Array1D<Double> mySingularValues = null;
    private M myU = null;
    private M myV = null;
    private boolean myValuesOnly = false;
    private double[] s = null;

    DenseSingularValue(final PhysicalStore.Factory<N, ? extends M> factory, final boolean fullSize) {

        super(factory);

        myFullSize = fullSize;
    }

    @Override
    public void btran(final double[] arg) {

        MatrixStore<N> mU = this.getU();
        MatrixStore<N> mV = this.getV();

        int rank = this.getRank();
        int dim = arg.length;

        double[] work = new double[rank];
        for (int i = 0; i < rank; i++) {
            double sum = ZERO;
            for (int j = 0; j < dim; j++) {
                sum += mV.doubleValue(j, i) * arg[j];
            }
            work[i] = sum / s[i];
        }

        java.util.Arrays.fill(arg, ZERO);
        for (int i = 0; i < rank; i++) {
            double wi = work[i];
            for (int j = 0; j < dim; j++) {
                arg[j] += mU.doubleValue(j, i) * wi;
            }
        }
    }

    @Override
    public void btran(final PhysicalStore<N> arg) {
        arg.fillByMultiplying(this.getInverse().transpose(), arg.copy());
    }

    @Override
    public boolean computeValuesOnly(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {
        return this.compute(matrix, true, false);
    }

    @Override
    public int countSignificant(final double threshold) {
        int significant = 0;
        for (int i = 0; i < s.length; i++) {
            if (s[i] > threshold) {
                significant++;
            }
        }
        return significant;
    }

    @Override
    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {
        return this.compute(matrix, false, this.isFullSize());
    }

    @Override
    public void ftran(final double[] arg) {

        MatrixStore<N> mU = this.getU();
        MatrixStore<N> mV = this.getV();

        int rank = this.getRank();
        int dim = arg.length;

        double[] work = new double[rank];
        for (int i = 0; i < rank; i++) {
            double sum = ZERO;
            for (int j = 0; j < dim; j++) {
                sum += mU.doubleValue(j, i) * arg[j];
            }
            work[i] = sum / s[i];
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
    public int getColDim() {
        return myColDim;
    }

    @Override
    public double getCondition() {
        return s[0] / s[s.length - 1];
    }

    @Override
    public MatrixStore<N> getCovariance() {

        MatrixStore<N> v = this.getV();
        MatrixStore<N> d = this.getS();
        Access1D<N> values = d.sliceDiagonal();

        int rank = this.getRank();

        BinaryFunction<N> divide = this.function().divide();

        MatrixStore<N> tmp = v.limits(-1, rank).onColumns(divide, values).collect(v.physical());

        return tmp.multiply(tmp.conjugate());
    }

    /**
     * @deprecated Use {@link #getS()} instead
     */
    @Deprecated
    @Override
    public MatrixStore<N> getD() {
        return this.getS();
    }

    @Override
    public List<InvertibleFactor<N>> getFactors() {
        if (myValuesOnly) {
            return List.of(this);
        }
        MatrixStore<N> mU = this.getU();
        MatrixStore<N> mV = this.getV();
        if (mU == null || mV == null) {
            return List.of(this);
        }
        int rank = this.getRank();
        int dim = s.length;
        return List.of(new FactorUT<>(mU, rank, dim), new FactorSinv<>(s, rank, dim), new FactorV<>(mV, rank, dim));
    }

    @Override
    public double getFrobeniusNorm() {

        double retVal = ZERO;

        double tmpVal;
        for (int i = s.length - 1; i >= 0; i--) {
            tmpVal = s[i];
            retVal += tmpVal * tmpVal;
        }

        return SQRT.invoke(retVal);
    }

    @Override
    public MatrixStore<N> getInverse() {

        if (myInverse == null) {
            int nbRows = this.getRowDim();
            int nbCols = this.getColDim();
            PhysicalStore<N> preallocated = this.preallocate(nbRows, nbCols, nbRows);
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

        for (int i = Math.min(s.length, k) - 1; i >= 0; i--) {
            retVal += s[i];
        }

        return retVal;
    }

    @Override
    public double getOperatorNorm() {
        return s[0];
    }

    @Override
    public SortOrder getOrder() {
        return SortOrder.DESCENDING;
    }

    @Override
    public double getRankThreshold() {
        return Math.max(MACHINE_SMALLEST, s[0]) * this.getDimensionalEpsilon();
    }

    @Override
    public int getRowDim() {
        return myRowDim;
    }

    @Override
    public MatrixStore<N> getS() {

        if (this.isComputed() && myS == null) {
            myS = this.makeD();
        }

        return myS;
    }

    @Override
    public Array1D<Double> getSingularValues() {

        if (mySingularValues == null && this.isComputed()) {
            mySingularValues = this.makeSingularValues();
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

        if (this.isComputed() && !myValuesOnly) {

            return SingularValue.solve(this, mRHS, preallocated);

        } else {

            throw new IllegalStateException();
        }
    }

    @Override
    public double getTraceNorm() {
        return this.getKyFanNorm(s.length);
    }

    @Override
    public MatrixStore<N> getU() {
        return myU;
    }

    @Override
    public MatrixStore<N> getV() {
        return myV;
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
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    @Override
    public boolean isFullRank() {
        return s[s.length - 1] > this.getRankThreshold();
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
        return this.makeZero(nbVariables, nbSolutions);
    }

    @Override
    public void reset() {

        super.reset();

        myS = null;
        myU = null;
        myV = null;
        mySingularValues = null;

        myInverse = null;

        myValuesOnly = false;
    }

    @Override
    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs));
        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    @Override
    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    @Override
    protected boolean checkSolvability() {
        return true;
    }

    protected boolean compute(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix, final boolean valuesOnly, final boolean fullSize) {

        this.reset();

        myRowDim = (int) matrix.countRows();
        myColDim = (int) matrix.countColumns();
        myValuesOnly = valuesOnly;

        int minDim = Math.min(myRowDim, myColDim);

        if (s == null || s.length != minDim) {
            s = new double[minDim];
        }

        M u = valuesOnly ? null : this.makeZero(myRowDim, fullSize ? myRowDim : minDim);
        M v = valuesOnly ? null : this.makeZero(myColDim, fullSize ? myColDim : minDim);

        boolean computeOK = false;

        try {

            computeOK = this.compute(matrix, s, u, v, fullSize) == 0;

        } catch (final Exception xcptn) {

            BasicLogger.error(xcptn.toString());

            computeOK = false;
        }

        if (computeOK) {
            myU = u;
            myV = v;
        } else {
            this.reset();
        }

        return this.computed(computeOK);
    }

    protected MatrixStore<N> makeD() {
        MatrixStore<N> retVal = this.makeDiagonal(this.getSingularValues()).get();
        if (myFullSize) {
            if (myRowDim > retVal.countRows()) {
                retVal = retVal.below((int) (myRowDim - retVal.countRows()));
            } else if (myColDim > retVal.countColumns()) {
                retVal = retVal.right((int) (myColDim - retVal.countColumns()));
            }
        }
        return retVal;
    }

    protected Array1D<Double> makeSingularValues() {
        return Array1D.R064.wrap(ArrayR064.wrap(s));
    }

    /**
     * The singular values, in descending order, to {@code values}, and, if u and v are not null, the left and
     * right singular vectors to the columns of u and v: m-by-min(m,n) and n-by-min(m,n), or m-by-m and n-by-n
     * with fullSize.
     *
     * @return 0 if successful
     */
    abstract int compute(Collectable<N, ? super TransformableRegion<N>> matrix, double[] values, M u, M v, boolean fullSize);

}
