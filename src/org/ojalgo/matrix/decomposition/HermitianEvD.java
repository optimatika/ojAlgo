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

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Collectable;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.Array1D;
import static org.ojalgo.constant.PrimitiveMath.*;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.ComplexAggregator;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;

public abstract class HermitianEvD<N extends Number> extends EigenvalueDecomposition<N> implements MatrixDecomposition.Solver<N> {

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

            final N tmpZero = this.scalar().zero().getNumber();
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

        return tmpVisitor.getNumber();
    }

    public final MatrixStore<N> invert(final Access2D<?> original) throws TaskException {
        this.decompose(this.wrap(original));
        if (this.isSolvable()) {
            return this.getInverse();
        } else {
            throw TaskException.newNotInvertible();
        }
    }

    public final MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws TaskException {
        this.decompose(this.wrap(original));
        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw TaskException.newNotInvertible();
        }
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

    @Override
    protected final boolean doGeneral(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean eigenvaluesOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final boolean doHermitian(final Collectable<N, ? super PhysicalStore<N>> matrix, final boolean eigenvaluesOnly) {

        final int tmpDim = (int) matrix.countRows();

        myTridiagonal.decompose(matrix);

        final DiagonalAccess<N> tmpTridiagonal = myTridiagonal.getDiagonalAccessD();

        //        BasicLogger.logDebug("Tridiagonal1={}", tmpTridiagonal);

        final DecompositionStore<N> tmpV = eigenvaluesOnly ? null : myTridiagonal.doQ();
        BasicLogger.debug("Tridiagonal={}", tmpTridiagonal.toString());
        
        final Array1D<?> tmpMainDiagonal = tmpTridiagonal.mainDiagonal;
        final Array1D<?> tmpSubdiagonal = tmpTridiagonal.subdiagonal;
        
        final int size = tmpMainDiagonal.size();
        
        final double[] d = tmpMainDiagonal.toRawCopy1D(); // Actually unnecessary to copy
        final double[] e = new double[size]; // The algorith needs the array to be the same length as the main diagonal
        final int tmpLength = tmpSubdiagonal.size();
        for (int i = 0; i < tmpLength; i++) {
            e[i] = tmpSubdiagonal.doubleValue(i);
        }

        //        BasicLogger.logDebug("Tridiagonal2={}", tmpTridiagonal);

        final Array1D<Double> tmpDiagonal = myDiagonalValues = EvD1D.tql2a(d, e, tmpV);

        for (int ij1 = 0; ij1 < (tmpDim - 1); ij1++) {
            final double tmpValue1 = tmpDiagonal.doubleValue(ij1);

            int ij2 = ij1;
            double tmpValue2 = tmpValue1;

            for (int ij2exp = ij1 + 1; ij2exp < tmpDim; ij2exp++) {
                final double tmpValue2exp = tmpDiagonal.doubleValue(ij2exp);

                if ((PrimitiveFunction.ABS.invoke(tmpValue2exp) > PrimitiveFunction.ABS.invoke(tmpValue1))
                        || ((PrimitiveFunction.ABS.invoke(tmpValue2exp) == PrimitiveFunction.ABS.invoke(tmpValue1)) && (tmpValue2exp > tmpValue1))) {
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
        final DiagonalAccess<Double> tmpDiagonal = new DiagonalAccess<>(myDiagonalValues, null, null, ZERO);
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

}
