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

import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.RotateRight;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

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
abstract class HermitianEvD<N extends Comparable<N>> extends EigenvalueDecomposition<N> implements MatrixDecomposition.Solver<N> {

    static final class C128 extends HermitianEvD<ComplexNumber> {

        C128() {
            super(GenericStore.C128, new DeferredTridiagonal.C128());
        }

        public Eigenpair getEigenpair(final int index) {

            ComplexNumber value = this.getD().get(index, index);
            Access1D<ComplexNumber> vector = this.getV().sliceColumn(index);

            return new Eigenpair(value, vector);
        }

    }

    static final class H256 extends HermitianEvD<Quaternion> {

        H256() {
            super(GenericStore.H256, new DeferredTridiagonal.H256());
        }

    }

    static final class Q128 extends HermitianEvD<RationalNumber> {

        Q128() {
            super(GenericStore.Q128, new DeferredTridiagonal.Q128());
        }

    }

    static final class R064 extends HermitianEvD<Double> {

        R064() {
            super(Primitive64Store.FACTORY, new SimultaneousTridiagonal());
        }

    }

    static final class R128 extends HermitianEvD<Quadruple> {

        R128() {
            super(GenericStore.R128, new DeferredTridiagonal.R128());
        }

    }

    static void tql2(final double[] d, final double[] e, final RotateRight mtrxV) {

        int size = d.length;
        int limit = size - 1;

        double shift = ZERO;
        double increment;

        double magnitude = ZERO;
        double epsilon;

        double d_l, e_l;

        int m;
        // Main loop
        for (int l = 0; l < size; l++) {

            d_l = d[l];
            e_l = e[l];

            // Find small subdiagonal element
            magnitude = PrimitiveMath.MAX.invoke(magnitude, PrimitiveMath.ABS.invoke(d_l) + PrimitiveMath.ABS.invoke(e_l));
            epsilon = MACHINE_EPSILON * magnitude;

            m = l;
            while (m < limit && PrimitiveMath.ABS.invoke(e[m]) > epsilon) {
                m++;
            }

            // If m == l, d[l] is an eigenvalue, otherwise, iterate.
            if (l < m) {
                do {

                    // Compute implicit shift

                    double p = (d[l + 1] - d_l) / (e_l + e_l);
                    double r = PrimitiveMath.HYPOT.invoke(p, ONE);
                    if (p < ZERO) {
                        r = -r;
                    }

                    d[l + 1] = e_l * (p + r);
                    increment = d_l - (d[l] = e_l / (p + r));
                    for (int i = l + 2; i < size; i++) {
                        d[i] -= increment;
                    }
                    shift += increment;

                    // Implicit QL transformation

                    double cos1 = ONE, sin1 = ZERO, cos2 = cos1;
                    double d_i, e_i;

                    p = d[m];
                    for (int i = m - 1; i >= l; i--) {
                        d_i = d[i];
                        e_i = e[i];

                        r = PrimitiveMath.HYPOT.invoke(p, e_i);

                        e[i + 1] = sin1 * r;

                        cos2 = cos1;

                        cos1 = p / r;
                        sin1 = e_i / r;

                        d[i + 1] = cos2 * p + sin1 * (cos1 * cos2 * e_i + sin1 * d_i);

                        p = cos1 * d_i - sin1 * cos2 * e_i;

                        // Accumulate transformation - rotate the eigenvector matrix
                        mtrxV.rotateRight(i, i + 1, cos1, sin1);
                    }

                    d_l = d[l] = cos1 * p;
                    e_l = e[l] = sin1 * p;

                } while (PrimitiveMath.ABS.invoke(e[l]) > epsilon); // Check for convergence
            } // End if (m > l)

            d[l] += shift;
            e[l] = ZERO;

        } // End main loop - l
    }

    private double[] d;
    private double[] e;
    private transient MatrixStore<N> myInverse;
    private final TridiagonalDecomposition<N> myTridiagonal;

    @SuppressWarnings("unused")
    private HermitianEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory) {
        this(factory, null);
    }

    protected HermitianEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> factory, final TridiagonalDecomposition<N> tridiagonal) {

        super(factory);

        myTridiagonal = tridiagonal;
    }

    public final void btran(final PhysicalStore<N> arg) {
        arg.fillByMultiplying(this.getInverse(), arg.copy());
    }

    public boolean checkAndDecompose(final MatrixStore<N> matrix) {
        if (matrix.isHermitian()) {
            return this.decompose(matrix);
        }
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return false;
    }

    public N getDeterminant() {

        AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return this.scalar().cast(tmpVisitor.get());
    }

    public void getEigenvalues(final double[] realParts, final Optional<double[]> imaginaryParts) {

        int length = realParts.length;

        System.arraycopy(d, 0, realParts, 0, length);

        if (imaginaryParts.isPresent()) {
            System.arraycopy(e, 0, imaginaryParts.get(), 0, length);
        }
    }

    public MatrixStore<N> getInverse() {

        if (myInverse == null) {

            MatrixStore<N> tmpV = this.getV();
            MatrixStore<N> tmpD = this.getD();

            int tmpDim = (int) tmpD.countRows();

            PhysicalStore<N> tmpMtrx = tmpV.conjugate().copy();

            N tmpZero = this.scalar().zero().get();
            BinaryFunction<N> tmpDivide = this.function().divide();

            for (int i = 0; i < tmpDim; i++) {
                if (tmpD.isSmall(i, i, ONE)) {
                    tmpMtrx.fillRow(i, 0, tmpZero);
                } else {
                    tmpMtrx.modifyRow(i, 0, tmpDivide.second(tmpD.get(i, i)));
                }
            }

            myInverse = tmpV.multiply(tmpMtrx);
        }

        return myInverse;
    }

    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        if (myInverse == null) {

            MatrixStore<N> tmpV = this.getV();
            MatrixStore<N> tmpD = this.getD();

            int tmpDim = (int) tmpD.countRows();

            PhysicalStore<N> tmpMtrx = preallocated;
            //tmpMtrx.fillMatching(new TransposedStore<N>(tmpV));
            tmpMtrx.fillMatching(tmpV.transpose());

            N tmpZero = this.scalar().zero().get();
            BinaryFunction<N> tmpDivide = this.function().divide();

            for (int i = 0; i < tmpDim; i++) {
                if (tmpD.isSmall(i, i, ONE)) {
                    tmpMtrx.fillRow(i, 0, tmpZero);
                } else {
                    tmpMtrx.modifyRow(i, 0, tmpDivide.second(tmpD.get(i, i)));
                }
            }

            myInverse = tmpV.multiply(tmpMtrx);
        }

        return myInverse;
    }

    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getInverse().multiply(this.collect(rhs));
    }

    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {
        rhs.supplyTo(preallocated);
        preallocated.fillByMultiplying(this.getInverse(), preallocated.copy());
        return preallocated;
    }

    public ComplexNumber getTrace() {

        AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    public MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {
        this.decompose(this.wrap(original));
        if (this.isSolvable()) {
            return this.getInverse();
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    public MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {
        this.decompose(this.wrap(original));
        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        }
        throw RecoverableCondition.newMatrixNotInvertible();
    }

    public boolean isHermitian() {
        return true;
    }

    public boolean isOrdered() {
        return false;
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    public PhysicalStore<N> preallocate(final Structure2D template) {
        long tmpCountRows = template.countRows();
        return this.allocate(tmpCountRows, tmpCountRows);
    }

    public PhysicalStore<N> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateRHS.countRows(), templateRHS.countColumns());
    }

    @Override
    public void reset() {

        super.reset();

        myTridiagonal.reset();

        myInverse = null;
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs));
        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        }
        throw RecoverableCondition.newEquationSystemNotSolvable();
    }

    @Override
    protected boolean checkSolvability() {
        return this.isComputed() && this.isHermitian();
    }

    @Override
    protected boolean doDecompose(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly) {

        int size = (int) matrix.countRows();

        myTridiagonal.decompose(matrix);

        if (d == null || d.length != size) {
            d = new double[size];
            e = new double[size];
        }

        myTridiagonal.supplyDiagonalTo(d, e);

        RotateRight tmpRotateRight = valuesOnly ? RotateRight.NULL : myTridiagonal.getDecompositionQ();
        HermitianEvD.tql2(d, e, tmpRotateRight);

        if (this.isOrdered()) {
            ExchangeColumns tmpExchangeColumns = valuesOnly ? ExchangeColumns.NULL : myTridiagonal.getDecompositionQ();
            EigenvalueDecomposition.sort(d, tmpExchangeColumns);
        }

        if (!valuesOnly) {
            this.setV(myTridiagonal.getDecompositionQ());
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
        return myTridiagonal.getQ();
    }
}
