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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

abstract class HermitianEvD<N extends Number> extends EigenvalueDecomposition<N> implements MatrixDecomposition.Solver<N> {

    static final class Big extends HermitianEvD<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY, new TridiagonalDecomposition.Big());
        }

    }

    static final class Complex extends HermitianEvD<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY, new TridiagonalDecomposition.Complex());
        }

    }

    /**
     * Eigenvalues and eigenvectors of a real matrix.
     * <P>
     * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is diagonal and the eigenvector matrix
     * V is orthogonal. I.e. A = V.times(D.times(V.transpose())) and V.times(V.transpose()) equals the
     * identity matrix.
     * <P>
     * If A is not symmetric, then the eigenvalue matrix D is block diagonal with the real eigenvalues in
     * 1-by-1 blocks and any complex eigenvalues, lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda].
     * The columns of V represent the eigenvectors in the sense that A*V = V*D, i.e. A.times(V) equals
     * V.times(D). The matrix V may be badly conditioned, or even singular, so the validity of the equation A
     * = V*D*inverse(V) depends upon V.cond().
     **/
    static final class Primitive extends HermitianEvD<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY, new TridiagonalDecomposition.Primitive());
        }

    }

    private static final double EPSILON = Math.pow(2.0, -52.0);

    static Array1D<Double> toDiagonal(final DiagonalAccess<?> aTridiagonal, final DecompositionStore<?> transformationAccumulator) {

        //   BasicLogger.logDebug("Tridiagonal={}", aTridiagonal.toString());

        final Array1D<?> tmpMainDiagonal = aTridiagonal.mainDiagonal;
        final Array1D<?> tmpSubdiagonal = aTridiagonal.subdiagonal;

        final int tmpDim = tmpMainDiagonal.size();

        final double[] tmpMainDiagData = tmpMainDiagonal.toRawCopy(); // Actually unnecessary to copy
        final double[] tmpOffDiagData = new double[tmpDim]; // The algorith needs the array to be the same length as the main diagonal
        final int tmpLength = tmpSubdiagonal.size();
        for (int i = 0; i < tmpLength; i++) {
            tmpOffDiagData[i] = tmpSubdiagonal.doubleValue(i);
        }

        //        BasicLogger.logDebug("BEGIN diagonalize");
        //        BasicLogger.logDebug("Main D: {}", Arrays.toString(tmpMainDiagonal));
        //        BasicLogger.logDebug("Seco D: {}", Arrays.toString(tmpOffDiagonal));
        //        BasicLogger.logDebug("V", aV);
        //        BasicLogger.logDebug();

        double tmpShift = PrimitiveMath.ZERO;
        double tmpShiftIncr;

        double tmpMagnitude = PrimitiveMath.ZERO;
        double tmpLocalEpsilon;

        int m;
        // Main loop
        for (int l = 0; l < tmpDim; l++) {

            //BasicLogger.logDebug("Loop l=" + l, tmpMainDiagonal, tmpOffDiagonal);

            // Find small subdiagonal element
            tmpMagnitude = Math.max(tmpMagnitude, Math.abs(tmpMainDiagData[l]) + Math.abs(tmpOffDiagData[l]));
            tmpLocalEpsilon = EPSILON * tmpMagnitude;

            m = l;
            while (m < tmpDim) {
                if (Math.abs(tmpOffDiagData[m]) <= tmpLocalEpsilon) {
                    break;
                }
                m++;
            }

            // If m == l, aMainDiagonal[l] is an eigenvalue, otherwise, iterate.
            if (m > l) {

                do {

                    final double tmp1Ml0 = tmpMainDiagData[l]; // (l,l)
                    final double tmp1Ml1 = tmpMainDiagData[l + 1]; // (l+1,l+1)
                    final double tmp1Sl0 = tmpOffDiagData[l]; // (l+1,l) and (l,l+1)

                    // Compute implicit shift

                    double p = (tmp1Ml1 - tmp1Ml0) / (tmp1Sl0 + tmp1Sl0);
                    double r = Math.hypot(p, PrimitiveMath.ONE);
                    if (p < 0) {
                        r = -r;
                    }

                    final double tmp2Ml0 = tmpMainDiagData[l] = tmp1Sl0 / (p + r); // (l,l)
                    final double tmp2Ml1 = tmpMainDiagData[l + 1] = tmp1Sl0 * (p + r); // (l+1,l+1)
                    final double tmp2Sl1 = tmpOffDiagData[l + 1]; // (l+1,l) and (l,l+1)

                    tmpShiftIncr = tmp1Ml0 - tmp2Ml0;
                    for (int i = l + 2; i < tmpDim; i++) {
                        tmpMainDiagData[i] -= tmpShiftIncr;
                    }
                    tmpShift += tmpShiftIncr;

                    //BasicLogger.logDebug("New shift =" + tmpShift, tmpMainDiagonal, tmpOffDiagonal);

                    // Implicit QL transformation

                    double tmpRotCos = PrimitiveMath.ONE;
                    double tmpRotSin = PrimitiveMath.ZERO;

                    double tmpRotCos2 = tmpRotCos;
                    double tmpRotSin2 = PrimitiveMath.ZERO;

                    double tmpRotCos3 = tmpRotCos;

                    p = tmpMainDiagData[m]; // Initiate p
                    //      BasicLogger.logDebug("m={} l={}", m, l);
                    for (int i = m - 1; i >= l; i--) {

                        final double tmp1Mi0 = tmpMainDiagData[i];
                        final double tmp1Si0 = tmpOffDiagData[i];

                        r = Math.hypot(p, tmp1Si0);

                        tmpRotCos3 = tmpRotCos2;

                        tmpRotCos2 = tmpRotCos;
                        tmpRotSin2 = tmpRotSin;

                        tmpRotCos = p / r;
                        tmpRotSin = tmp1Si0 / r;

                        tmpMainDiagData[i + 1] = (tmpRotCos2 * p) + (tmpRotSin * ((tmpRotCos * tmpRotCos2 * tmp1Si0) + (tmpRotSin * tmp1Mi0)));
                        tmpOffDiagData[i + 1] = tmpRotSin2 * r;

                        p = (tmpRotCos * tmp1Mi0) - (tmpRotSin * tmpRotCos2 * tmp1Si0); // Next p

                        // Accumulate transformation - rotate the eigenvector matrix
                        //aV.transformRight(new Rotation.Primitive(i, i + 1, tmpRotCos, tmpRotSin));

                        //BasicLogger.logDebug("low={} high={} cos={} sin={}", i, i + 1, tmpRotCos, tmpRotSin);
                        if (transformationAccumulator != null) {
                            transformationAccumulator.rotateRight(i, i + 1, tmpRotCos, tmpRotSin);
                        }

                        //          EigenvalueDecomposition.log("QL step done i=" + i, tmpMainDiagonal, tmpOffDiagonal);

                    }

                    p = (-tmpRotSin * tmpRotSin2 * tmpRotCos3 * tmp2Sl1 * tmpOffDiagData[l]) / tmp2Ml1; // Final p

                    tmpMainDiagData[l] = tmpRotCos * p;
                    tmpOffDiagData[l] = tmpRotSin * p;

                } while (Math.abs(tmpOffDiagData[l]) > tmpLocalEpsilon); // Check for convergence
            } // End if (m > l)

            tmpMainDiagData[l] = tmpMainDiagData[l] + tmpShift;
            tmpOffDiagData[l] = PrimitiveMath.ZERO;
        } // End main loop - l

        //        BasicLogger.logDebug("END diagonalize");
        //        BasicLogger.logDebug("Main D: {}", Arrays.toString(tmpMainDiagonal));
        //        BasicLogger.logDebug("Seco D: {}", Arrays.toString(tmpOffDiagonal));
        //        BasicLogger.logDebug("V", aV);
        //        BasicLogger.logDebug();

        //        for (int i = 0; i < tmpMainDiagData.length; i++) {
        //            tmpMainDiagonal.set(i, tmpMainDiagData[i]);
        //        }

        //return new PrimitiveArray(tmpMainDiagonal).asArray1D();
        return Array1D.PRIMITIVE.wrap(PrimitiveArray.wrap(tmpMainDiagData));
    }

    private Array1D<Double> myDiagonalValues;
    private transient MatrixStore<N> myInverse;

    private final TridiagonalDecomposition<N> myTridiagonal;

    @SuppressWarnings("unused")
    private HermitianEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        this(aFactory, null);
    }

    protected HermitianEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory, final TridiagonalDecomposition<N> aTridiagonal) {

        super(aFactory);

        myTridiagonal = aTridiagonal;
    }

    public final boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return MatrixUtils.equals(aStore, this, context);
    }

    public final N getDeterminant() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return this.scalar().cast(tmpVisitor.getNumber());
    }

    public final MatrixStore<N> getInverse() {

        if (myInverse == null) {

            final MatrixStore<N> tmpV = this.getV();
            final MatrixStore<N> tmpD = this.getD();

            final int tmpDim = (int) tmpD.countRows();

            final PhysicalStore<N> tmpMtrx = tmpV.conjugate().copy();

            final N tmpZero = this.scalar().zero().getNumber();
            final BinaryFunction<N> tmpDivide = this.function().divide();

            for (int i = 0; i < tmpDim; i++) {
                if (tmpD.isZero(i, i)) {
                    tmpMtrx.fillRow(i, 0, tmpZero);
                } else {
                    tmpMtrx.modifyRow(i, 0, tmpDivide.second(tmpD.get(i, i)));
                }
            }

            myInverse = tmpV.multiply(tmpMtrx);
        }

        return myInverse;
    }

    public final MatrixStore<N> getInverse(final DecompositionStore<N> preallocated) {

        if (myInverse == null) {

            final MatrixStore<N> tmpV = this.getV();
            final MatrixStore<N> tmpD = this.getD();

            final int tmpDim = (int) tmpD.countRows();

            final PhysicalStore<N> tmpMtrx = preallocated;
            //tmpMtrx.fillMatching(new TransposedStore<N>(tmpV));
            tmpMtrx.fillMatching(tmpV.transpose());

            final N tmpZero = this.scalar().zero().getNumber();
            final BinaryFunction<N> tmpDivide = this.function().divide();

            for (int i = 0; i < tmpDim; i++) {
                if (tmpD.isZero(i, i)) {
                    tmpMtrx.fillRow(i, 0, tmpZero);
                } else {
                    tmpMtrx.modifyRow(i, 0, tmpDivide.second(tmpD.get(i, i)));
                }
            }

            myInverse = tmpV.multiply(tmpMtrx);
        }

        return myInverse;
    }

    public final ComplexNumber getTrace() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.getNumber();
    }

    public final boolean isHermitian() {
        return true;
    }

    public final boolean isOrdered() {
        return true;
    }

    public final boolean isSolvable() {
        return this.isComputed() && this.isHermitian();
    }

    @Override
    public void reset() {

        super.reset();

        myTridiagonal.reset();

        myInverse = null;
    }

    @Override
    protected final boolean doNonsymmetric(final ElementsSupplier<N> aMtrx, final boolean eigenvaluesOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final boolean doSymmetric(final ElementsSupplier<N> aMtrx, final boolean eigenvaluesOnly) {

        final int tmpDim = (int) aMtrx.countRows();

        myTridiagonal.decompose(aMtrx);

        final DiagonalAccess<N> tmpTridiagonal = myTridiagonal.getDiagonalAccessD();

        //        BasicLogger.logDebug("Tridiagonal1={}", tmpTridiagonal);

        final DecompositionStore<N> tmpV = eigenvaluesOnly ? null : myTridiagonal.doQ();

        //        BasicLogger.logDebug("Tridiagonal2={}", tmpTridiagonal);

        final Array1D<Double> tmpDiagonal = myDiagonalValues = HermitianEvD.toDiagonal(tmpTridiagonal, tmpV);

        for (int ij1 = 0; ij1 < (tmpDim - 1); ij1++) {
            final double tmpValue1 = tmpDiagonal.doubleValue(ij1);

            int ij2 = ij1;
            double tmpValue2 = tmpValue1;

            for (int ij2exp = ij1 + 1; ij2exp < tmpDim; ij2exp++) {
                final double tmpValue2exp = tmpDiagonal.doubleValue(ij2exp);

                if ((Math.abs(tmpValue2exp) > Math.abs(tmpValue1)) || ((Math.abs(tmpValue2exp) == Math.abs(tmpValue1)) && (tmpValue2exp > tmpValue1))) {
                    ij2 = ij2exp;
                    tmpValue2 = tmpValue2exp;
                }
            }

            if (ij2 != ij1) {
                tmpDiagonal.set(ij1, tmpValue2);
                tmpDiagonal.set(ij2, tmpValue1);
                if (tmpV != null) {
                    tmpV.exchangeColumns(ij1, ij2);
                }
            }
        }

        if (!eigenvaluesOnly) {
            this.setV(tmpV);
        }

        return this.computed(true);
    }

    @Override
    protected MatrixStore<N> makeD() {
        final DiagonalAccess<Double> tmpDiagonal = new DiagonalAccess<Double>(myDiagonalValues, null, null, PrimitiveMath.ZERO);
        return this.wrap(tmpDiagonal).diagonal(false).get();
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {

        final int tmpDim = myDiagonalValues.size();

        final Array1D<ComplexNumber> retVal = Array1D.COMPLEX.makeZero(tmpDim);

        for (int ij = 0; ij < tmpDim; ij++) {
            retVal.set(ij, ComplexNumber.valueOf(myDiagonalValues.doubleValue(ij)));
        }

        return retVal;
    }

    @Override
    protected MatrixStore<N> makeV() {
        return myTridiagonal.getQ();
    }

    public MatrixStore<N> invert(final Access2D<?> original) {
        this.decompose(this.wrap(original));
        return this.getInverse();
    }

    public MatrixStore<N> invert(final Access2D<?> original, final DecompositionStore<N> preallocated) {
        this.decompose(this.wrap(original));
        return this.getInverse(preallocated);
    }

    public DecompositionStore<N> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
        return this.preallocate(tmpCountRows, tmpCountRows);
    }

    public DecompositionStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.preallocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) {
        this.decompose(this.wrap(body));
        return this.solve(this.wrap(rhs));
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<N> preallocated) {
        this.decompose(this.wrap(body));
        return this.solve(rhs, preallocated);
    }

    public final MatrixStore<N> solve(final ElementsSupplier<N> rhs) {
        return this.getInverse().multiply(rhs.get());
    }

    public final MatrixStore<N> solve(final ElementsSupplier<N> rhs, final DecompositionStore<N> preallocated) {
        preallocated.fillByMultiplying(this.getInverse(), rhs.get());
        return preallocated;
    }

}
