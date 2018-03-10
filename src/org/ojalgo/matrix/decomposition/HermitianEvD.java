/*
 * Copyright 1997-2018 Optimatika
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
import static org.ojalgo.function.PrimitiveFunction.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.ojalgo.ProgrammingError;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.RotateRight;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

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
public abstract class HermitianEvD<N extends Number> extends EigenvalueDecomposition<N> implements MatrixDecomposition.Solver<N> {

    static final class Big extends HermitianEvD<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY, new DeferredTridiagonal.Big());
        }

    }

    static final class Complex extends HermitianEvD<ComplexNumber> {

        Complex() {
            super(GenericDenseStore.COMPLEX, new DeferredTridiagonal.Complex());
        }

    }

    static final class DeferredPrimitive extends HermitianEvD<Double> {

        DeferredPrimitive() {
            super(PrimitiveDenseStore.FACTORY, new DeferredTridiagonal.Primitive());
        }

    }

    static final class Quat extends HermitianEvD<Quaternion> {

        Quat() {
            super(GenericDenseStore.QUATERNION, new DeferredTridiagonal.Quat());
        }

    }

    static final class Rational extends HermitianEvD<RationalNumber> {

        Rational() {
            super(GenericDenseStore.RATIONAL, new DeferredTridiagonal.Rational());
        }

    }

    static final class SimultaneousPrimitive extends HermitianEvD<Double> {

        SimultaneousPrimitive() {
            super(PrimitiveDenseStore.FACTORY, new SimultaneousTridiagonal());
        }

    }

    static void tql2(final double[] d, final double[] e, final RotateRight mtrxV) {

        final int size = d.length;

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
            magnitude = MAX.invoke(magnitude, ABS.invoke(d_l) + ABS.invoke(e_l));
            epsilon = MACHINE_EPSILON * magnitude;

            m = l;
            while ((m < size) && (ABS.invoke(e[m]) > epsilon)) {
                m++;
            }

            // If m == l, d[l] is an eigenvalue, otherwise, iterate.
            if (m > l) {
                do {

                    // Compute implicit shift

                    double p = (d[l + 1] - d_l) / (e_l + e_l);
                    double r = HYPOT.invoke(p, ONE);
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

                        r = HYPOT.invoke(p, e_i);

                        e[i + 1] = sin1 * r;

                        cos2 = cos1;

                        cos1 = p / r;
                        sin1 = e_i / r;

                        d[i + 1] = (cos2 * p) + (sin1 * ((cos1 * cos2 * e_i) + (sin1 * d_i)));

                        p = (cos1 * d_i) - (sin1 * cos2 * e_i);

                        // Accumulate transformation - rotate the eigenvector matrix
                        mtrxV.rotateRight(i, i + 1, cos1, sin1);
                    }

                    d_l = d[l] = cos1 * p;
                    e_l = e[l] = sin1 * p;

                } while (ABS.invoke(e[l]) > epsilon); // Check for convergence
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
    private HermitianEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory) {
        this(aFactory, null);
    }

    protected HermitianEvD(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory, final TridiagonalDecomposition<N> tridiagonal) {

        super(aFactory);

        myTridiagonal = tridiagonal;
    }

    public final N getDeterminant() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().product();

        this.getEigenvalues().visitAll(tmpVisitor);

        return this.scalar().cast(tmpVisitor.get());
    }

    public void getEigenvalues(final double[] realParts, final Optional<double[]> imaginaryParts) {

        final int length = realParts.length;

        System.arraycopy(d, 0, realParts, 0, length);

        if (imaginaryParts.isPresent()) {
            System.arraycopy(e, 0, imaginaryParts.get(), 0, length);
        }
    }

    public final MatrixStore<N> getInverse() {

        if (myInverse == null) {

            final MatrixStore<N> tmpV = this.getV();
            final MatrixStore<N> tmpD = this.getD();

            final int tmpDim = (int) tmpD.countRows();

            final PhysicalStore<N> tmpMtrx = tmpV.conjugate().copy();

            final N tmpZero = this.scalar().zero().get();
            final BinaryFunction<N> tmpDivide = this.function().divide();

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

    public final MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        if (myInverse == null) {

            final MatrixStore<N> tmpV = this.getV();
            final MatrixStore<N> tmpD = this.getD();

            final int tmpDim = (int) tmpD.countRows();

            final PhysicalStore<N> tmpMtrx = preallocated;
            //tmpMtrx.fillMatching(new TransposedStore<N>(tmpV));
            tmpMtrx.fillMatching(tmpV.transpose());

            final N tmpZero = this.scalar().zero().get();
            final BinaryFunction<N> tmpDivide = this.function().divide();

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

    public final MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs) {
        return this.getInverse().multiply(this.collect(rhs));
    }

    public final MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {
        preallocated.fillByMultiplying(this.getInverse(), this.collect(rhs));
        return preallocated;
    }

    public final ComplexNumber getTrace() {

        final AggregatorFunction<ComplexNumber> tmpVisitor = ComplexAggregator.getSet().sum();

        this.getEigenvalues().visitAll(tmpVisitor);

        return tmpVisitor.get();
    }

    public final MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {
        this.decompose(this.wrap(original));
        if (this.isSolvable()) {
            return this.getInverse();
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    public final MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {
        this.decompose(this.wrap(original));
        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    public final boolean isHermitian() {
        return true;
    }

    public boolean isOrdered() {
        return false;
    }

    public PhysicalStore<N> preallocate(final Structure2D template) {
        final long tmpCountRows = template.countRows();
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
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

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
    protected final boolean doGeneral(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean eigenvaluesOnly) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return false;
    }

    @Override
    protected final boolean doHermitian(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean valuesOnly) {

        final int size = (int) matrix.countRows();

        myTridiagonal.decompose(matrix);

        if ((d == null) || (d.length != size)) {
            d = new double[size];
            e = new double[size];
        }

        myTridiagonal.supplyDiagonalTo(d, e);

        final RotateRight tmpRotateRight = valuesOnly ? RotateRight.NULL : myTridiagonal.getDecompositionQ();
        HermitianEvD.tql2(d, e, tmpRotateRight);

        if (this.isOrdered()) {
            final ExchangeColumns tmpExchangeColumns = valuesOnly ? ExchangeColumns.NULL : myTridiagonal.getDecompositionQ();
            EigenvalueDecomposition.sort(d, tmpExchangeColumns);
        }

        if (!valuesOnly) {
            this.setV(myTridiagonal.getDecompositionQ());
        }

        return this.computed(true);
    }

    @Override
    protected double getDimensionalEpsilon() {
        return d.length * PrimitiveMath.MACHINE_EPSILON;
    }

    @Override
    protected MatrixStore<N> makeD() {
        final DiagonalBasicArray<Double> tmpDiagonal = new DiagonalBasicArray<>(Primitive64Array.wrap(d), null, null, ZERO);
        return this.wrap(tmpDiagonal).diagonal(false).get();
    }

    @Override
    protected Array1D<ComplexNumber> makeEigenvalues() {

        final int length = d.length;

        final Array1D<ComplexNumber> retVal = Array1D.COMPLEX.makeZero(length);

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
