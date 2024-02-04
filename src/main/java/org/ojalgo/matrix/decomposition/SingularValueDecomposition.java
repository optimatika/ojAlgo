/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

abstract class SingularValueDecomposition<N extends Comparable<N>> extends GenericDecomposition<N> implements SingularValue<N> {

    static final class C128 extends SingularValueDecomposition<ComplexNumber> {

        C128() {
            this(false);
        }

        C128(final boolean fullSize) {
            super(GenericStore.C128, new BidiagonalDecomposition.C128(fullSize), fullSize);
        }

    }

    static final class H256 extends SingularValueDecomposition<Quaternion> {

        H256() {
            this(false);
        }

        H256(final boolean fullSize) {
            super(GenericStore.H256, new BidiagonalDecomposition.H256(fullSize), fullSize);
        }

    }

    static final class Q128 extends SingularValueDecomposition<RationalNumber> {

        Q128() {
            this(false);
        }

        Q128(final boolean fullSize) {
            super(GenericStore.Q128, new BidiagonalDecomposition.Q128(fullSize), fullSize);
        }

    }

    static final class R064 extends SingularValueDecomposition<Double> {

        R064() {
            this(false);
        }

        R064(final boolean fullSize) {
            super(Primitive64Store.FACTORY, new BidiagonalDecomposition.R064(fullSize), fullSize);
        }

    }

    static final class R128 extends SingularValueDecomposition<Quadruple> {

        R128() {
            this(false);
        }

        R128(final boolean fullSize) {
            super(GenericStore.R128, new BidiagonalDecomposition.R128(fullSize), fullSize);
        }

    }

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
            // kase = 1     if s[p] and e[k-1] are negligible and k<p                           => deflate negligible s[p]
            // kase = 2     if s[k] is negligible and k<p                                       => split at negligible s[k]
            // kase = 3     if e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible    => perform QR-step
            // kase = 4     if e[p-1] is negligible                                             => convergence

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

                SingularValueDecomposition.doCase1(s, e, p, k, q2RotR);
                break;

            // s[k] is negligible and k<p
            case 2: // Split at negligible s[k]

                SingularValueDecomposition.doCase2(s, e, p, k, q1RotR);
                break;

            // e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible
            case 3: // Perform QR-step.

                SingularValueDecomposition.doCase3(s, e, p, k, q1RotR, q2RotR);
                break;

            // e[p-1] is negligible
            case 4: // Convergence

                SingularValueDecomposition.doCase4(s, k, q2NegCol, q1XchgCols, q2XchgCols);
                p--;
                break;

            // Should never happen
            default:

                throw new IllegalStateException();

            } // switch
        } // while
    }

    private double[] e = null;
    private final BidiagonalDecomposition<N> myBidiagonal;
    private transient MatrixStore<N> myD = null;
    private final boolean myFullSize;
    private final Structure2D myInputStructure = new Structure2D() {

        @Override
        public long countColumns() {
            return myTransposed ? myBidiagonal.getRowDim() : myBidiagonal.getColDim();
        }

        @Override
        public long countRows() {
            return myTransposed ? myBidiagonal.getColDim() : myBidiagonal.getRowDim();
        }
    };
    private transient MatrixStore<N> myInverse = null;
    private transient Array1D<Double> mySingularValues = null;
    private boolean myTransposed = false;
    private transient MatrixStore<N> myU = null;
    private transient MatrixStore<N> myV = null;
    private boolean myValuesOnly = false;
    private double[] s = null;

    @SuppressWarnings("unused")
    private SingularValueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        this(factory, null, false);
    }

    protected SingularValueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory,
            final BidiagonalDecomposition<N> bidiagonal, final boolean fullSize) {

        super(factory);

        myBidiagonal = bidiagonal;
        myFullSize = fullSize;
    }

    @Override
    public final void btran(final PhysicalStore<N> arg) {
        arg.fillByMultiplying(this.getInverse().transpose(), arg.copy());
    }

    @Override
    public boolean computeValuesOnly(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
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
    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.compute(matrix, false, this.isFullSize());
    }

    @Override
    public int getColDim() {
        return myTransposed ? myBidiagonal.getRowDim() : myBidiagonal.getColDim();
    }

    @Override
    public double getCondition() {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();

        return tmpSingularValues.doubleValue(0) / tmpSingularValues.doubleValue(tmpSingularValues.length - 1);
    }

    @Override
    public MatrixStore<N> getCovariance() {

        MatrixStore<N> v = this.getV();
        MatrixStore<N> d = this.getD();
        Access1D<N> values = d.sliceDiagonal();

        int rank = this.getRank();

        BinaryFunction<N> divide = this.function().divide();

        MatrixStore<N> tmp = v.limits(-1, rank).onColumns(divide, values).collect(v.physical());

        return tmp.multiply(tmp.transpose());
    }

    @Override
    public MatrixStore<N> getD() {

        if (this.isComputed() && myD == null) {
            myD = this.makeD();
        }

        return myD;
    }

    @Override
    public double getFrobeniusNorm() {

        double retVal = ZERO;

        final Array1D<Double> tmpSingularValues = this.getSingularValues();
        double tmpVal;

        for (int i = tmpSingularValues.size() - 1; i >= 0; i--) {
            tmpVal = tmpSingularValues.doubleValue(i);
            retVal += tmpVal * tmpVal;
        }

        return SQRT.invoke(retVal);
    }

    @Override
    public MatrixStore<N> getInverse() {
        return this.getInverse(this.preallocate(myInputStructure));
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        if (myInverse == null) {

            int rank = this.getRank();

            MatrixStore<N> tmpV = this.getV();
            PhysicalStore<N> tmpMtrx = tmpV.limits(-1, rank).collect(tmpV.physical());

            Scalar.Factory<N> scalar = this.scalar();
            BinaryFunction<N> divide = this.function().divide();

            Array1D<Double> singularValues = this.getSingularValues();
            for (int j = 0; j < rank; j++) {
                tmpMtrx.modifyColumn(0L, j, divide.by(scalar.cast(singularValues.doubleValue(j))));
            }

            preallocated.fillByMultiplying(tmpMtrx, this.getU().limits(-1, rank).conjugate());

            myInverse = preallocated;
        }

        return myInverse;
    }

    @Override
    public double getKyFanNorm(final int k) {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();

        double retVal = ZERO;

        for (int i = Math.min(tmpSingularValues.size(), k) - 1; i >= 0; i--) {
            retVal += tmpSingularValues.doubleValue(i);
        }

        return retVal;
    }

    @Override
    public double getOperatorNorm() {
        return this.getSingularValues().doubleValue(0);
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
    public Array1D<Double> getSingularValues() {

        if (mySingularValues == null && this.isComputed()) {
            mySingularValues = this.makeSingularValues();
        }

        return mySingularValues;
    }

    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getInverse().multiply(this.collect(rhs));
    }

    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {
        preallocated.fillByMultiplying(this.getInverse(), this.collect(rhs));
        return preallocated;
    }

    @Override
    public double getTraceNorm() {
        return this.getKyFanNorm(this.getSingularValues().size());
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

        if (!myValuesOnly && this.isComputed() && myV == null) {
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
    public PhysicalStore<N> preallocate(final Structure2D template) {
        return this.allocate(template.countColumns(), template.countRows());
    }

    @Override
    public PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countColumns(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        myBidiagonal.reset();

        myD = null;
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

    protected boolean compute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly, final boolean fullSize) {

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

    protected boolean computeBidiagonal(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean fullSize) {
        return myBidiagonal.decompose(matrix);
    }

    protected boolean doCompute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly, final boolean fullSize) {

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

        SingularValueDecomposition.toDiagonal(s, e, q1RotR, q2RotR, q1XchgCols, q2XchgCols, q2NegCol);

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

    void setD(final MatrixStore<N> someD) {
        myD = someD;
    }

    void setSingularValues(final Array1D<Double> singularValues) {
        mySingularValues = singularValues;
    }

}
