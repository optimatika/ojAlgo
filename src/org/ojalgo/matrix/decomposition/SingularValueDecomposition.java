/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.NegateColumn;
import org.ojalgo.matrix.decomposition.function.RotateRight;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

abstract class SingularValueDecomposition<N extends Number & Comparable<N>> extends GenericDecomposition<N> implements SingularValue<N> {

    static final class Big extends SingularValueDecomposition<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY, new BidiagonalDecomposition.Big());
        }

    }
    static final class Complex extends SingularValueDecomposition<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY, new BidiagonalDecomposition.Complex());
        }

    }
    static final class Primitive extends SingularValueDecomposition<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY, new BidiagonalDecomposition.Primitive());
        }

    }

    private final BidiagonalDecomposition<N> myBidiagonal;
    private transient MatrixStore<N> myD;
    private boolean myFullSize = false;
    private transient MatrixStore<N> myInverse;
    private transient MatrixStore<N> myQ1;
    private transient MatrixStore<N> myQ2;
    private transient Array1D<Double> mySingularValues;
    private boolean myTransposed = false;
    private boolean myValuesOnly = false;

    @SuppressWarnings("unused")
    private SingularValueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        this(factory, null);
    }

    protected SingularValueDecomposition(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory,
            final BidiagonalDecomposition<N> bidiagonal) {

        super(factory);

        myBidiagonal = bidiagonal;
    }

    public boolean computeValuesOnly(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.compute(matrix, true, false);
    }

    public boolean decompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix) {
        return this.compute(matrix, false, this.isFullSize());
    }

    public double getCondition() {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();

        return tmpSingularValues.doubleValue(0) / tmpSingularValues.doubleValue(tmpSingularValues.length - 1);
    }

    public MatrixStore<N> getD() {

        if (this.isComputed() && (myD == null)) {
            myD = this.makeD();
        }

        return myD;
    }

    public double getFrobeniusNorm() {

        double retVal = PrimitiveMath.ZERO;

        final Array1D<Double> tmpSingularValues = this.getSingularValues();
        double tmpVal;

        for (int i = tmpSingularValues.size() - 1; i >= 0; i--) {
            tmpVal = tmpSingularValues.doubleValue(i);
            retVal += tmpVal * tmpVal;
        }

        return PrimitiveFunction.SQRT.invoke(retVal);
    }

    public MatrixStore<N> getInverse() {

        return this.getInverse(this.preallocate(new Structure2D() {

            public long countColumns() {
                return myTransposed ? myBidiagonal.getRowDim() : myBidiagonal.getColDim();
            }

            public long countRows() {
                return myTransposed ? myBidiagonal.getColDim() : myBidiagonal.getRowDim();
            }
        }));
    }

    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        if (myInverse == null) {

            final MatrixStore<N> tmpQ1 = this.getQ1();
            final Array1D<Double> tmpSingulars = this.getSingularValues();
            final MatrixStore<N> tmpQ2 = this.getQ2();

            final int rank = this.getRank();

            final PhysicalStore<N> tmpMtrx = tmpQ2.logical().limits(-1, rank).copy();

            final Scalar.Factory<N> tmpScalar = this.scalar();
            final BinaryFunction<N> tmpDivide = this.function().divide();

            for (int j = 0; j < rank; j++) {
                tmpMtrx.modifyColumn(0L, j, tmpDivide.second(tmpScalar.cast(tmpSingulars.doubleValue(j))));
            }

            preallocated.fillByMultiplying(tmpMtrx, tmpQ1.logical().limits(-1, rank).conjugate().get());
            myInverse = preallocated;
        }

        return myInverse;
    }

    public double getKyFanNorm(final int k) {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();

        double retVal = PrimitiveMath.ZERO;

        for (int i = Math.min(tmpSingularValues.size(), k) - 1; i >= 0; i--) {
            retVal += tmpSingularValues.doubleValue(i);
        }

        return retVal;
    }

    public double getOperatorNorm() {
        return this.getSingularValues().doubleValue(0);
    }

    public MatrixStore<N> getQ1() {

        if (!myValuesOnly && this.isComputed() && (myQ1 == null)) {

            if (myTransposed) {
                myQ1 = this.makeQ2();
            } else {
                myQ1 = this.makeQ1();
            }
        }

        return myQ1;
    }

    public MatrixStore<N> getQ2() {

        if (!myValuesOnly && this.isComputed() && (myQ2 == null)) {
            if (myTransposed) {
                myQ2 = this.makeQ1();
            } else {
                myQ2 = this.makeQ2();
            }
        }

        return myQ2;
    }

    public int getRank() {

        final Array1D<Double> tmpSingularValues = this.getSingularValues();
        int retVal = tmpSingularValues.size();

        // Tolerance based on min-dim but should be max-dim
        final double tmpTolerance = retVal * (tmpSingularValues.doubleValue(0) * PrimitiveMath.MACHINE_EPSILON);

        for (int i = retVal - 1; i >= 0; i--) {
            if (tmpSingularValues.doubleValue(i) <= tmpTolerance) {
                retVal--;
            } else {
                return retVal;
            }
        }

        return retVal;
    }

    public Array1D<Double> getSingularValues() {

        if ((mySingularValues == null) && this.isComputed()) {
            mySingularValues = this.makeSingularValues();
        }

        return mySingularValues;
    }

    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getInverse().multiply(this.collect(rhs));
    }

    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {
        preallocated.fillByMultiplying(this.getInverse(), this.collect(rhs));
        return preallocated;
    }

    public double getTraceNorm() {
        return this.getKyFanNorm(this.getSingularValues().size());
    }

    public MatrixStore<N> invert(final Access2D<?> original) throws TaskException {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse();
        } else {
            throw TaskException.newNotInvertible();
        }
    }

    public MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws TaskException {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw TaskException.newNotInvertible();
        }
    }

    public boolean isFullSize() {
        return myFullSize;
    }

    public boolean isOrdered() {
        return true;
    }

    public boolean isSolvable() {
        return this.isComputed();
    }

    public PhysicalStore<N> preallocate(final Structure2D template) {
        return this.allocate(template.countColumns(), template.countRows());
    }

    public PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        myBidiagonal.reset();

        myD = null;
        myQ1 = null;
        myQ2 = null;

        myInverse = null;

        myValuesOnly = false;
        myTransposed = false;
    }

    public void setFullSize(final boolean fullSize) {
        myFullSize = fullSize;
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) throws TaskException {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs));
        } else {
            throw TaskException.newNotSolvable();
        }
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<N> preallocated) throws TaskException {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw TaskException.newNotSolvable();
        }
    }

    private MatrixStore<N> getInverseOldVersion(final DecompositionStore<N> preallocated) {

        if (myInverse == null) {

            final MatrixStore<N> tmpQ1 = this.getQ1();
            final Array1D<Double> tmpSingulars = this.getSingularValues();
            final MatrixStore<N> tmpQ2 = this.getQ2();

            final int tmpRowDim = (int) tmpSingulars.count();
            final int tmpColDim = (int) tmpQ1.countRows();
            final PhysicalStore<N> tmpMtrx = this.makeZero(tmpRowDim, tmpColDim);

            double tmpValue;
            final int rank = this.getRank();
            for (int i = 0; i < rank; i++) {
                tmpValue = tmpSingulars.doubleValue(i);
                for (int j = 0; j < tmpColDim; j++) {
                    tmpMtrx.set(i, j, tmpQ1.toScalar(j, i).conjugate().divide(tmpValue).getNumber());
                }
            }

            preallocated.fillByMultiplying(tmpQ2, tmpMtrx);
            myInverse = preallocated;
        }

        return myInverse;
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
        myBidiagonal.setFullSize(fullSize);
        return myBidiagonal.decompose(matrix);
    }

    protected boolean doCompute(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly, final boolean fullSize) {

        this.computeBidiagonal(matrix, fullSize);

        final DiagonalArray1D<N> tmpBidiagonal = this.getBidiagonalAccessD();

        final DecompositionStore<N> tmpQ1 = valuesOnly ? null : this.getBidiagonalQ1();
        final DecompositionStore<N> tmpQ2 = valuesOnly ? null : this.getBidiagonalQ2();
        final int tmpDiagDim = (int) ((DiagonalAccess<?, ?>) tmpBidiagonal).mainDiagonal.count();

        final double[] s = (tmpBidiagonal).mainDiagonal.toRawCopy1D(); // s
        final double[] e = new double[tmpDiagDim]; // e
        final int tmpOffLength = (tmpBidiagonal).superdiagonal.size();
        for (int i = 0; i < tmpOffLength; i++) {
            e[i] = (tmpBidiagonal).superdiagonal.doubleValue(i);
        }

        final RotateRight q1RotR = tmpQ1 != null ? tmpQ1 : RotateRight.NULL;
        final RotateRight q2RotR = tmpQ2 != null ? tmpQ2 : RotateRight.NULL;
        final ExchangeColumns q1XchgCols = tmpQ1 != null ? tmpQ1 : ExchangeColumns.NULL;
        final ExchangeColumns q2XchgCols = tmpQ2 != null ? tmpQ2 : ExchangeColumns.NULL;
        final NegateColumn q2NegCol = tmpQ1 != null ? tmpQ2 : NegateColumn.NULL;

        SVD1D.toDiagonal(s, e, q1RotR, q2RotR, q1XchgCols, q2XchgCols, q2NegCol);

        final Array1D<Double> tmpDiagonal = Array1D.PRIMITIVE64.wrap(Primitive64Array.wrap(s));

        this.setSingularValues(tmpDiagonal);

        return this.computed(true);
    }

    protected DiagonalArray1D<N> getBidiagonalAccessD() {
        return myBidiagonal.getDiagonalAccessD();
    }

    protected int getBidiagonalDim() {
        return myBidiagonal.getMinDim();
    }

    protected DecompositionStore<N> getBidiagonalQ1() {
        return (DecompositionStore<N>) myBidiagonal.getQ1();
    }

    protected DecompositionStore<N> getBidiagonalQ2() {
        return (DecompositionStore<N>) myBidiagonal.getQ2();
    }

    protected boolean isTransposed() {
        return myTransposed;
    }

    protected MatrixStore<N> makeD() {
        return this.wrap(new DiagonalArray1D<>(this.getSingularValues(), null, null, ZERO)).get();
    }

    protected MatrixStore<N> makeQ1() {
        return this.getBidiagonalQ1();
    }

    protected MatrixStore<N> makeQ2() {
        return this.getBidiagonalQ2();
    }

    protected Array1D<Double> makeSingularValues() {
        throw new IllegalStateException("Should never have to be called!");
    }

    void setD(final MatrixStore<N> someD) {
        myD = someD;
    }

    void setSingularValues(final Array1D<Double> singularValues) {
        mySingularValues = singularValues;
    }

}
