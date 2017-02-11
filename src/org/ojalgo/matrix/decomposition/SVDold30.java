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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.concurrent.DaemonPoolExecutor;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * Samma som orginalet, but without QR. Instead Householder directly. Wasn't faster. Try going directly to
 * bidiagonal instead. Based SVDold2, but with GenericRotaion replaced with Rotation.
 *
 * @author apete
 */
abstract class SVDold30<N extends Number & Comparable<N>> extends SingularValueDecomposition<N> {

    static final class Big extends SVDold30<BigDecimal> {

        Big() {
            super(BigDenseStore.FACTORY, new BidiagonalDecomposition.Big());
        }

        @Override
        protected Rotation<BigDecimal>[] rotations(final PhysicalStore<BigDecimal> aStore, final int aLowInd, final int aHighInd,
                final Rotation<BigDecimal>[] retVal) {

            final BigDecimal a00 = aStore.get(aLowInd, aLowInd);
            final BigDecimal a01 = aStore.get(aLowInd, aHighInd);
            final BigDecimal a10 = aStore.get(aHighInd, aLowInd);
            final BigDecimal a11 = aStore.get(aHighInd, aHighInd);

            final BigDecimal x = a00.add(a11);
            final BigDecimal y = a10.subtract(a01);

            BigDecimal t; // tan, cot or something temporary

            // Symmetrise - Givens
            final BigDecimal cg; // cos Givens
            final BigDecimal sg; // sin Givens

            if (y.signum() == 0) {
                cg = BigFunction.SIGNUM.invoke(x);
                sg = BigMath.ZERO;
            } else if (x.signum() == 0) {
                sg = BigFunction.SIGNUM.invoke(y);
                cg = BigMath.ZERO;
            } else if (y.abs().compareTo(x.abs()) == 1) {
                t = BigFunction.DIVIDE.invoke(x, y); // cot
                sg = BigFunction.DIVIDE.invoke(BigFunction.SIGNUM.invoke(y), BigFunction.SQRT1PX2.invoke(t));
                cg = sg.multiply(t);
            } else {
                t = BigFunction.DIVIDE.invoke(y, x); // tan
                cg = BigFunction.DIVIDE.invoke(BigFunction.SIGNUM.invoke(x), BigFunction.SQRT1PX2.invoke(t));
                sg = cg.multiply(t);
            }

            final BigDecimal b00 = cg.multiply(a00).add(sg.multiply(a10));
            final BigDecimal b11 = cg.multiply(a11).subtract(sg.multiply(a01));
            final BigDecimal b2 = cg.multiply(a01.add(a10)).add(sg.multiply(a11.subtract(a00))); // b01 + b10

            t = BigFunction.DIVIDE.invoke(b11.subtract(b00), b2);
            t = BigFunction.DIVIDE.invoke(BigFunction.SIGNUM.invoke(t), BigFunction.SQRT1PX2.invoke(t).add(t.abs()));

            // Annihilate - Jacobi
            final BigDecimal cj = BigFunction.DIVIDE.invoke(BigMath.ONE, BigFunction.SQRT1PX2.invoke(t)); // Cos Jacobi
            final BigDecimal sj = cj.multiply(t); // Sin Jacobi

            retVal[1] = new Rotation.Big(aLowInd, aHighInd, cj, sj); // Jacobi
            retVal[0] = new Rotation.Big(aLowInd, aHighInd, cj.multiply(cg).add(sj.multiply(sg)), cj.multiply(sg).subtract(sj.multiply(cg))); // Givens - Jacobi

            return retVal;
        }

    }

    static final class Complex extends SVDold30<ComplexNumber> {

        Complex() {
            super(ComplexDenseStore.FACTORY, new BidiagonalDecomposition.Complex());
        }

        @Override
        protected Rotation<ComplexNumber>[] rotations(final PhysicalStore<ComplexNumber> aStore, final int aLowInd, final int aHighInd,
                final Rotation<ComplexNumber>[] retVal) {

            final ComplexNumber a00 = aStore.get(aLowInd, aLowInd);
            final ComplexNumber a01 = aStore.get(aLowInd, aHighInd);
            final ComplexNumber a10 = aStore.get(aHighInd, aLowInd);
            final ComplexNumber a11 = aStore.get(aHighInd, aHighInd);

            final ComplexNumber x = a00.add(a11);
            final ComplexNumber y = a10.subtract(a01);

            ComplexNumber t; // tan, cot or something temporary

            // Symmetrise - Givens
            final ComplexNumber cg; // cos Givens
            final ComplexNumber sg; // sin Givens

            if (ComplexNumber.isSmall(PrimitiveMath.ONE, y)) {
                cg = x.signum();
                sg = ComplexNumber.ZERO;
            } else if (ComplexNumber.isSmall(PrimitiveMath.ONE, x)) {
                sg = y.signum();
                cg = ComplexNumber.ZERO;
            } else if (y.compareTo(x) == 1) {
                t = x.divide(y); // cot
                sg = y.signum().divide(ComplexFunction.SQRT1PX2.invoke(t));
                cg = sg.multiply(t);
            } else {
                t = y.divide(x); // tan
                cg = x.signum().divide(ComplexFunction.SQRT1PX2.invoke(t));
                sg = cg.multiply(t);
            }

            final ComplexNumber b00 = cg.multiply(a00).add(sg.multiply(a10));
            final ComplexNumber b11 = cg.multiply(a11).subtract(sg.multiply(a01));
            final ComplexNumber b2 = cg.multiply(a01.add(a10)).add(sg.multiply(a11.subtract(a00))); // b01 + b10

            t = b11.subtract(b00).divide(b2);
            t = t.signum().divide(ComplexFunction.SQRT1PX2.invoke(t).add(t.norm()));

            // Annihilate - Jacobi
            final ComplexNumber cj = ComplexFunction.SQRT1PX2.invoke(t).invert(); // Cos Jacobi
            final ComplexNumber sj = cj.multiply(t); // Sin Jacobi

            retVal[1] = new Rotation.Complex(aLowInd, aHighInd, cj, sj); // Jacobi
            retVal[0] = new Rotation.Complex(aLowInd, aHighInd, cj.multiply(cg).add(sj.multiply(sg)), cj.multiply(sg).subtract(sj.multiply(cg))); // Givens - Jacobi

            return retVal;
        }

    }

    static final class Primitive extends SVDold30<Double> {

        Primitive() {
            super(PrimitiveDenseStore.FACTORY, new BidiagonalDecomposition.Primitive());
        }

        @Override
        protected Rotation<Double>[] rotations(final PhysicalStore<Double> aStore, final int aLowInd, final int aHighInd, final Rotation<Double>[] retVal) {

            final double a00 = aStore.doubleValue(aLowInd, aLowInd);
            final double a01 = aStore.doubleValue(aLowInd, aHighInd);
            final double a10 = aStore.doubleValue(aHighInd, aLowInd);
            final double a11 = aStore.doubleValue(aHighInd, aHighInd);

            final double x = a00 + a11;
            final double y = a10 - a01;

            double t; // tan, cot or something temporary

            // Symmetrise - Givens
            final double cg; // cos Givens
            final double sg; // sin Givens

            if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, y)) {
                cg = PrimitiveFunction.SIGNUM.invoke(x);
                sg = PrimitiveMath.ZERO;
            } else if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, x)) {
                sg = PrimitiveFunction.SIGNUM.invoke(y);
                cg = PrimitiveMath.ZERO;
            } else if (PrimitiveFunction.ABS.invoke(y) > PrimitiveFunction.ABS.invoke(x)) {
                t = x / y; // cot
                sg = PrimitiveFunction.SIGNUM.invoke(y) / PrimitiveFunction.SQRT1PX2.invoke(t);
                cg = sg * t;
            } else {
                t = y / x; // tan
                cg = PrimitiveFunction.SIGNUM.invoke(x) / PrimitiveFunction.SQRT1PX2.invoke(t);
                sg = cg * t;
            }

            final double b00 = (cg * a00) + (sg * a10);
            final double b11 = (cg * a11) - (sg * a01);
            final double b2 = (cg * (a01 + a10)) + (sg * (a11 - a00)); // b01 + b10

            t = (b11 - b00) / b2;
            t = PrimitiveFunction.SIGNUM.invoke(t) / (PrimitiveFunction.SQRT1PX2.invoke(t) + PrimitiveFunction.ABS.invoke(t)); // tan Jacobi

            // Annihilate - Jacobi
            final double cj = PrimitiveMath.ONE / PrimitiveFunction.SQRT1PX2.invoke(t); // cos Jacobi
            final double sj = cj * t; // sin Jacobi

            retVal[1] = new Rotation.Primitive(aLowInd, aHighInd, cj, sj); // Jacobi
            retVal[0] = new Rotation.Primitive(aLowInd, aHighInd, ((cj * cg) + (sj * sg)), ((cj * sg) - (sj * cg))); // Givens - Jacobi

            return retVal;
        }

    }

    private Future<PhysicalStore<N>> myFutureQ1;
    private Future<PhysicalStore<N>> myFutureQ2;
    private final List<Rotation<N>> myQ1Rotations = new ArrayList<>();
    private final List<Rotation<N>> myQ2Rotations = new ArrayList<>();

    protected SVDold30(final DecompositionStore.Factory<N, ? extends DecompositionStore<N>> aFactory, final BidiagonalDecomposition<N> aBidiagonal) {
        super(aFactory, aBidiagonal);
    }

    public boolean equals(final MatrixStore<N> aStore, final NumberContext context) {
        return SingularValue.equals(aStore, this, context);
    }

    public boolean isOrdered() {
        return false;
    }

    public boolean isSolvable() {
        return this.isComputed();
    }

    @Override
    public void reset() {

        super.reset();

        myQ1Rotations.clear();
        myQ2Rotations.clear();

        myFutureQ1 = null;
        myFutureQ2 = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean doCompute(final Access2D.Collectable<N, ? super PhysicalStore<N>> aStore, final boolean singularValuesOnly, final boolean fullSize) {

        final int tmpMinDim = (int) Math.min(aStore.countRows(), aStore.countColumns());

        this.computeBidiagonal(aStore, fullSize);

        final DecompositionStore<N> tmpSimilar = this.copy(this.getBidiagonalAccessD());
        this.setD(tmpSimilar);

        this.setSingularValues(Array1D.PRIMITIVE.makeZero(tmpMinDim));

        Rotation<N>[] tmpRotations = new Rotation[2]; // [Givens - Jacobi, Jacobi]

        //        int iter = 0;
        //        BasicLogger.logDebug(this.getClass().toString());
        //        BasicLogger.logDebug("Init D", myD);

        final N tmpZero = this.scalar().zero().getNumber();
        boolean tmpNotAllZeros = true;
        for (int l = 0; tmpNotAllZeros && (l < tmpMinDim); l++) {

            tmpNotAllZeros = false;

            int i;
            //for (int i0 = tmpMinDim - 1; i0 > 0; i0--) { // Performs much slower
            for (int i0 = 1; i0 < tmpMinDim; i0++) {
                for (int j = 0; j < (tmpMinDim - i0); j++) {
                    i = i0 + j;

                    if (!tmpSimilar.isSmall(i, j, PrimitiveMath.ONE) || !tmpSimilar.isSmall(j, i, PrimitiveMath.ONE)) {

                        tmpNotAllZeros = true;

                        tmpRotations = this.rotations(tmpSimilar, j, i, tmpRotations);

                        tmpSimilar.transformLeft(tmpRotations[0]);
                        tmpSimilar.transformRight(tmpRotations[1]);

                        myQ1Rotations.add(tmpRotations[0].invert());
                        myQ2Rotations.add(tmpRotations[1]);

                        //                        BasicLogger.logDebug("Iter-" + ++iter + " D", myD);

                    }

                    tmpSimilar.set(i, j, tmpZero);
                    tmpSimilar.set(j, i, tmpZero);
                }
            }
        }

        double tmpSingularValue;
        for (int ij = 0; ij < tmpMinDim; ij++) {

            if (tmpSimilar.isSmall(ij, ij, PrimitiveMath.ONE)) {

                tmpSingularValue = PrimitiveMath.ZERO;

            } else if (tmpSimilar.isAbsolute(ij, ij)) {

                tmpSingularValue = tmpSimilar.doubleValue(ij, ij);

            } else {

                final Scalar<N> tmpDiagSclr = tmpSimilar.toScalar(ij, ij);
                final N tmpSignum = tmpDiagSclr.signum().getNumber();
                tmpSingularValue = tmpDiagSclr.divide(tmpSignum).norm();

                tmpSimilar.set(ij, ij, tmpSingularValue);
                myQ2Rotations.add(this.makeRotation(ij, ij, tmpSignum, tmpSignum));
            }

            this.getSingularValues().set(ij, tmpSingularValue);
        }

        this.getSingularValues().sortDescending();

        myFutureQ1 = DaemonPoolExecutor.invoke(() -> {

            final PhysicalStore<N> retVal = SVDold30.this.getBidiagonalQ1();

            final List<Rotation<N>> tmpRotations1 = myQ1Rotations;

            final int tmpLimit = tmpRotations1.size();
            for (int r = 0; r < tmpLimit; r++) {
                retVal.transformRight(tmpRotations1.get(r));
            }

            return retVal;
        });

        myFutureQ2 = DaemonPoolExecutor.invoke(() -> {

            final PhysicalStore<N> retVal = SVDold30.this.getBidiagonalQ2();

            final List<Rotation<N>> tmpRotations1 = myQ2Rotations;

            final int tmpLimit = tmpRotations1.size();
            for (int r = 0; r < tmpLimit; r++) {
                retVal.transformRight(tmpRotations1.get(r));
            }

            return retVal;
        });

        return this.computed(true);
    }

    protected DiagonalAccess<N> extractSimilar(final PhysicalStore<N> aStore, final boolean aNormalAspectRatio) {

        final DecompositionStore<N> tmpArray2D = ((DecompositionStore<N>) aStore);

        final Array1D<N> tmpMain = (Array1D<N>) tmpArray2D.sliceDiagonal(0, 0);

        if (aNormalAspectRatio) {

            final Array1D<N> tmpSuper = (Array1D<N>) tmpArray2D.sliceDiagonal(0, 1);

            return new DiagonalAccess<>(tmpMain, tmpSuper, null, this.scalar().zero().getNumber());

        } else {

            final Array1D<N> tmpSub = (Array1D<N>) tmpArray2D.sliceDiagonal(1, 0);

            return new DiagonalAccess<>(tmpMain, null, tmpSub, this.scalar().zero().getNumber());
        }
    }

    @Override
    protected MatrixStore<N> makeD() {

        //        final int tmpMinDim = this..getMinDim();
        //
        //        final PhysicalStore<N> retVal = this.makeZero(tmpMinDim, tmpMinDim);
        //
        //        for (int ij = 0; ij < tmpMinDim; ij++) {
        //            retVal.set(ij, ij, myD.get(ij, ij));
        //        }

        return null;
    }

    @Override
    protected MatrixStore<N> makeQ1() {
        try {
            return myFutureQ1.get();
        } catch (final InterruptedException anException) {
            throw new ProgrammingError(anException.getMessage());
        } catch (final ExecutionException anException) {
            throw new ProgrammingError(anException.getMessage());
        }
    }

    @Override
    protected MatrixStore<N> makeQ2() {
        try {
            return myFutureQ2.get();
        } catch (final InterruptedException anException) {
            throw new ProgrammingError(anException.getMessage());
        } catch (final ExecutionException anException) {
            throw new ProgrammingError(anException.getMessage());
        }
    }

    @Override
    protected Array1D<Double> makeSingularValues() {
        // TODO Auto-generated method stub
        return null;
    }

    protected abstract Rotation<N>[] rotations(PhysicalStore<N> aStore, int aLowInd, int aHighInd, Rotation<N>[] retVal);

}
