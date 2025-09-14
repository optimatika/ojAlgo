/*
 * Copyright 1997-2025 Optimatika
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

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.NegateColumn;
import org.ojalgo.matrix.decomposition.function.RotateRight;
import org.ojalgo.matrix.store.DiagonalStore;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

abstract class DenseSingularValue<N extends Comparable<N>> extends AbstractDecomposition<N, DecompositionStore<N>> implements SingularValue<N> {

    static final class C128 extends DenseSingularValue<ComplexNumber> {

        C128() {
            this(false);
        }

        C128(final boolean fullSize) {
            super(GenericStore.C128, new DenseBidiagonal.C128(fullSize), fullSize);
        }

    }

    static final class H256 extends DenseSingularValue<Quaternion> {

        H256() {
            this(false);
        }

        H256(final boolean fullSize) {
            super(GenericStore.H256, new DenseBidiagonal.H256(fullSize), fullSize);
        }

    }

    static final class Q128 extends DenseSingularValue<RationalNumber> {

        Q128() {
            this(false);
        }

        Q128(final boolean fullSize) {
            super(GenericStore.Q128, new DenseBidiagonal.Q128(fullSize), fullSize);
        }

    }

    static final class R064 extends DenseSingularValue<Double> {

        R064() {
            this(false);
        }

        R064(final boolean fullSize) {
            super(R064Store.FACTORY, new DenseBidiagonal.R064(fullSize), fullSize);
        }

    }

    static final class R128 extends DenseSingularValue<Quadruple> {

        R128() {
            this(false);
        }

        R128(final boolean fullSize) {
            super(GenericStore.R128, new DenseBidiagonal.R128(fullSize), fullSize);
        }

    }

    /**
     * ≈ 1.6E-291
     */
    private static final double TINY = Math.pow(2.0, -966.0);

    private static void doCase1(final double[] s, final double[] e, final int p, final int k, final RotateRight q2RotR) {

        double f = e[p - 2];
        e[p - 2] = ZERO;

        double tmp, cos, sin;

        for (int j = p - 2; j > k; j--) {

            tmp = HYPOT.invoke(s[j], f);
            cos = s[j] / tmp;
            sin = f / tmp;
            s[j] = tmp;

            q2RotR.rotateRight(p - 1, j, cos, sin);

            tmp = e[j - 1];
            f = -sin * tmp;
            e[j - 1] = cos * tmp;
        }

        tmp = HYPOT.invoke(s[k], f);
        cos = s[k] / tmp;
        sin = f / tmp;
        s[k] = tmp;

        q2RotR.rotateRight(p - 1, k, cos, sin);
    }

    private static void doCase2(final double[] s, final double[] e, final int p, final int k, final RotateRight mtrxQ1) {

        double f = e[k - 1];
        e[k - 1] = ZERO;

        double tmp, cos, sin;

        for (int j = k; j < p; j++) {

            tmp = HYPOT.invoke(s[j], f);
            cos = s[j] / tmp;
            sin = f / tmp;
            s[j] = tmp;

            mtrxQ1.rotateRight(k - 1, j, cos, sin);

            tmp = e[j];
            f = -sin * tmp;
            e[j] = cos * tmp;
        }
    }

    private static void doCase3(final double[] s, final double[] e, final int p, final int k, final RotateRight q1RotR, final RotateRight q2RotR) {

        // Calculate the shift.
        final double scale = MAX.invoke(MAX.invoke(MAX.invoke(MAX.invoke(ABS.invoke(s[p - 1]), ABS.invoke(s[p - 2])), ABS.invoke(e[p - 2])), ABS.invoke(s[k])),
                ABS.invoke(e[k]));

        final double s_p1 = s[p - 1] / scale;
        final double s_p2 = s[p - 2] / scale;
        final double e_p2 = e[p - 2] / scale;

        final double s_k = s[k] / scale;
        final double e_k = e[k] / scale;

        final double b = ((s_p2 + s_p1) * (s_p2 - s_p1) + e_p2 * e_p2) / TWO;
        final double c = s_p1 * e_p2 * (s_p1 * e_p2);

        double shift = ZERO;
        if (NumberContext.compare(b, ZERO) != 0 || NumberContext.compare(c, ZERO) != 0) {
            shift = SQRT.invoke(b * b + c);
            if (b < ZERO) {
                shift = -shift;
            }
            shift = c / (b + shift);
        }

        double f = (s_k + s_p1) * (s_k - s_p1) + shift;
        double g = s_k * e_k;

        double tmp, cos, sin;

        // Chase zeros.
        for (int j = k; j < p - 1; j++) {

            tmp = HYPOT.invoke(f, g);
            cos = f / tmp;
            sin = g / tmp;

            if (j != k) {
                e[j - 1] = tmp;
            }

            f = cos * s[j] + sin * e[j];
            e[j] = cos * e[j] - sin * s[j];
            g = sin * s[j + 1];
            s[j + 1] = cos * s[j + 1];

            q2RotR.rotateRight(j + 1, j, cos, sin);

            tmp = HYPOT.invoke(f, g);
            cos = f / tmp;
            sin = g / tmp;
            s[j] = tmp;

            f = cos * e[j] + sin * s[j + 1];
            s[j + 1] = -sin * e[j] + cos * s[j + 1];
            g = sin * e[j + 1];
            e[j + 1] = cos * e[j + 1];

            q1RotR.rotateRight(j + 1, j, cos, sin);
        }

        e[p - 2] = f;
    }

    private static void doCase4(final double[] s, final int k, final NegateColumn q2NegCol, final ExchangeColumns q1XchgCols,
            final ExchangeColumns q2XchgCols) {

        // Make the singular values positive.
        if (s[k] <= ZERO) {
            s[k] = s[k] < ZERO ? -s[k] : ZERO;
            q2NegCol.negateColumn(k);
        }

        final int size = s.length;
        double tmp;

        // Order the singular values.
        for (int iter = k, next = iter + 1; next < size && s[iter] < s[next]; iter++, next++) {

            tmp = s[iter];
            s[iter] = s[next];
            s[next] = tmp;

            q1XchgCols.exchangeColumns(iter, next);
            q2XchgCols.exchangeColumns(iter, next);
        }
    }

    static void toDiagonal(final double[] s, final double[] e, final RotateRight q1RotR, final RotateRight q2RotR, final ExchangeColumns q1XchgCols,
            final ExchangeColumns q2XchgCols, final NegateColumn q2NegCol) {

        int p = s.length;
        while (p > 0) {
            int k, kase;

            // This section of the program inspects for negligible elements in the s and e arrays.
            // On completion the variables kase and k are set as follows:
            //
            // kase = 1 if s[p] and e[k-1] are negligible and k<p => deflate negligible s[p]
            // kase = 2 if s[k] is negligible and k<p => split at negligible s[k]
            // kase = 3 if e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible => perform QR-step
            // kase = 4 if e[p-1] is negligible => convergence

            for (k = p - 2; k >= 0; k--) {
                if (ABS.invoke(e[k]) <= TINY + MACHINE_EPSILON * (ABS.invoke(s[k]) + ABS.invoke(s[k + 1]))) {
                    e[k] = ZERO;
                    break;
                }
            }
            if (k == p - 2) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks > k; ks--) {
                    final double t = ABS.invoke(e[ks]) + (ks != k + 1 ? ABS.invoke(e[ks - 1]) : ZERO);
                    if (ABS.invoke(s[ks]) <= TINY + MACHINE_EPSILON * t) {
                        s[ks] = ZERO;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == p - 1) {
                    kase = 1;
                } else {
                    kase = 2;
                    k = ks;
                }
            }
            k++;

            switch (kase) { // Perform the task indicated by kase.

            // s[p] and e[k-1] are negligible and k<p
            case 1: // Deflate negligible s[p]

                DenseSingularValue.doCase1(s, e, p, k, q2RotR);
                break;

            // s[k] is negligible and k<p
            case 2: // Split at negligible s[k]

                DenseSingularValue.doCase2(s, e, p, k, q1RotR);
                break;

            // e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible
            case 3: // Perform QR-step.

                DenseSingularValue.doCase3(s, e, p, k, q1RotR, q2RotR);
                break;

            // e[p-1] is negligible
            case 4: // Convergence

                DenseSingularValue.doCase4(s, k, q2NegCol, q1XchgCols, q2XchgCols);
                p--;
                break;

            // Should never happen
            default:

                throw new IllegalStateException();

            } // switch
        } // while
    }

    private double[] e = null;
    private final DenseBidiagonal<N> myBidiagonal;
    private final boolean myFullSize;
    private final Structure2D myInputStructure = new Structure2D() {

        @Override
        public int getColDim() {
            return myTransposed ? myBidiagonal.getRowDim() : myBidiagonal.getColDim();
        }

        @Override
        public int getRowDim() {
            return myTransposed ? myBidiagonal.getColDim() : myBidiagonal.getRowDim();
        }
    };
    private transient MatrixStore<N> myInverse = null;
    private transient MatrixStore<N> myS = null;
    private transient Array1D<Double> mySingularValues = null;
    private boolean myTransposed = false;
    private transient MatrixStore<N> myU = null;
    private transient MatrixStore<N> myV = null;
    private boolean myValuesOnly = false;
    private double[] s = null;

    DenseSingularValue(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory, final DenseBidiagonal<N> bidiagonal,
            final boolean fullSize) {

        super(factory);

        myBidiagonal = bidiagonal;
        myFullSize = fullSize;
    }

    @Override
    public final void btran(final PhysicalStore<N> arg) {
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
    public int getColDim() {
        return myTransposed ? myBidiagonal.getRowDim() : myBidiagonal.getColDim();
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

        return tmp.multiply(tmp.transpose());
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

        if (myInverse == null) {
            myInverse = SingularValue.invert(this, preallocated);
        }

        return myInverse;
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
    public double getRankThreshold() {
        return Math.max(MACHINE_SMALLEST, s[0]) * this.getDimensionalEpsilon();
    }

    @Override
    public int getRowDim() {
        return myTransposed ? myBidiagonal.getColDim() : myBidiagonal.getRowDim();
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

        if (!myValuesOnly && this.isComputed() && myU == null) {

            if (myTransposed) {
                myU = myBidiagonal.doGetRQ();
            } else {
                myU = myBidiagonal.doGetLQ();
            }
        }

        return myU;
    }

    @Override
    public MatrixStore<N> getV() {

        if (this.isComputed() && !myValuesOnly && myV == null) {
            if (myTransposed) {
                myV = myBidiagonal.doGetLQ();
            } else {
                myV = myBidiagonal.doGetRQ();
            }
        }

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
    public boolean isOrdered() {
        return true;
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

        myBidiagonal.reset();

        myS = null;
        myU = null;
        myV = null;
        mySingularValues = null;

        myInverse = null;

        myValuesOnly = false;
        myTransposed = false;
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

    private MatrixStore<N> getInverseOldVersion(final DecompositionStore<N> preallocated) {

        if (myInverse == null) {

            final MatrixStore<N> tmpQ1 = this.getU();
            final Array1D<Double> tmpSingulars = this.getSingularValues();
            final MatrixStore<N> tmpQ2 = this.getV();

            final int tmpRowDim = (int) tmpSingulars.count();
            final int tmpColDim = (int) tmpQ1.countRows();
            final PhysicalStore<N> tmpMtrx = this.makeZero(tmpRowDim, tmpColDim);

            double tmpValue;
            final int rank = this.getRank();
            for (int i = 0; i < rank; i++) {
                tmpValue = tmpSingulars.doubleValue(i);
                for (int j = 0; j < tmpColDim; j++) {
                    tmpMtrx.set(i, j, tmpQ1.toScalar(j, i).conjugate().divide(tmpValue).get());
                }
            }

            preallocated.fillByMultiplying(tmpQ2, tmpMtrx);
            myInverse = preallocated;
        }

        return myInverse;
    }

    @Override
    protected boolean checkSolvability() {
        return true;
    }

    protected boolean compute(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix, final boolean valuesOnly, final boolean fullSize) {

        this.reset();

        if (matrix.countRows() >= matrix.countColumns()) {
            myTransposed = false;
        } else {
            myTransposed = true;
        }

        myValuesOnly = valuesOnly;

        boolean computeOK = false;

        try {

            computeOK = this.doCompute(myTransposed ? this.collect(matrix).conjugate() : matrix, valuesOnly, fullSize);

        } catch (final Exception xcptn) {

            BasicLogger.error(xcptn.toString());

            this.reset();

            computeOK = false;
        }

        return this.computed(computeOK);
    }

    protected boolean computeBidiagonal(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix, final boolean fullSize) {
        return myBidiagonal.decompose(matrix);
    }

    protected boolean doCompute(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix, final boolean valuesOnly, final boolean fullSize) {

        this.computeBidiagonal(matrix, fullSize);

        final DiagonalStore<N, Array1D<N>> tmpBidiagonal = myBidiagonal.doGetDiagonal();

        final DecompositionStore<N> tmpQ1 = valuesOnly ? null : myBidiagonal.doGetLQ();
        final DecompositionStore<N> tmpQ2 = valuesOnly ? null : myBidiagonal.doGetRQ();

        final int size = tmpBidiagonal.getDimension();

        if (s == null || s.length != size) {
            s = new double[size];
            e = new double[size];
        }

        tmpBidiagonal.supplyMainDiagonalTo(s);
        tmpBidiagonal.supplySuperdiagonalTo(e);

        final RotateRight q1RotR = tmpQ1 != null ? tmpQ1 : RotateRight.NULL;
        final RotateRight q2RotR = tmpQ2 != null ? tmpQ2 : RotateRight.NULL;
        final ExchangeColumns q1XchgCols = tmpQ1 != null ? tmpQ1 : ExchangeColumns.NULL;
        final ExchangeColumns q2XchgCols = tmpQ2 != null ? tmpQ2 : ExchangeColumns.NULL;
        final NegateColumn q2NegCol = tmpQ1 != null ? tmpQ2 : NegateColumn.NULL;

        DenseSingularValue.toDiagonal(s, e, q1RotR, q2RotR, q1XchgCols, q2XchgCols, q2NegCol);

        return this.computed(true);
    }

    protected boolean isTransposed() {
        return myTransposed;
    }

    protected MatrixStore<N> makeD() {
        MatrixStore<N> retVal = this.makeDiagonal(this.getSingularValues()).get();
        if (myFullSize) {
            if (myInputStructure.countRows() > retVal.countRows()) {
                retVal = retVal.below((int) (myInputStructure.countRows() - retVal.countRows()));
            } else if (myInputStructure.countColumns() > retVal.countColumns()) {
                retVal = retVal.right((int) (myInputStructure.countColumns() - retVal.countColumns()));
            }
        }
        return retVal;
    }

    protected Array1D<Double> makeSingularValues() {
        return Array1D.R064.wrap(ArrayR064.wrap(s));
    }

}