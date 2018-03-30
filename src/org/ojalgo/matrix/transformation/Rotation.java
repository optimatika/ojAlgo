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
package org.ojalgo.matrix.transformation;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;

public abstract class Rotation<N extends Number> implements TransformationMatrix<N, PhysicalStore<N>> {

    public static final class Big extends Rotation<BigDecimal> {

        public final BigDecimal cos;
        public final BigDecimal sin;

        public Big(final int index) {
            this(index, index, null, null);
        }

        public Big(final int aLowerIndex, final int aHigherIndex) {
            this(aLowerIndex, aHigherIndex, null, null);
        }

        public Big(final int aLowerIndex, final int aHigherIndex, final BigDecimal aCosine, final BigDecimal aSine) {

            super(aLowerIndex, aHigherIndex);

            cos = aCosine;
            sin = aSine;
        }

        public Big(final Rotation<BigDecimal> aRotation) {

            super(aRotation.low, aRotation.high);

            cos = aRotation.getCosine();
            sin = aRotation.getSine();
        }

        @Override
        public double doubleCosineValue() {
            return cos.doubleValue();
        }

        @Override
        public double doubleSineValue() {
            return sin.doubleValue();
        }

        @Override
        public BigDecimal getCosine() {
            return cos;
        }

        @Override
        public BigDecimal getSine() {
            return sin;
        }

        @Override
        public Big invert() {
            return new Big(high, low, cos, sin);
        }

    }

    public static final class Complex extends Rotation<ComplexNumber> {

        public final ComplexNumber cos;
        public final ComplexNumber sin;

        public Complex(final int index) {
            this(index, index, null, null);
        }

        public Complex(final int aLowerIndex, final int aHigherIndex) {
            this(aLowerIndex, aHigherIndex, null, null);
        }

        public Complex(final int aLowerIndex, final int aHigherIndex, final ComplexNumber aCosine, final ComplexNumber aSine) {

            super(aLowerIndex, aHigherIndex);

            cos = aCosine;
            sin = aSine;
        }

        public Complex(final Rotation<ComplexNumber> aRotation) {

            super(aRotation.low, aRotation.high);

            cos = aRotation.getCosine();
            sin = aRotation.getSine();
        }

        @Override
        public double doubleCosineValue() {
            return cos.doubleValue();
        }

        @Override
        public double doubleSineValue() {
            return sin.doubleValue();
        }

        @Override
        public ComplexNumber getCosine() {
            return cos;
        }

        @Override
        public ComplexNumber getSine() {
            return sin;
        }

        @Override
        public Complex invert() {
            return new Complex(high, low, cos, sin);
        }

    }

    public static final class Generic<N extends Number & Scalar<N>> extends Rotation<N> {

        public final N cos;
        public final N sin;

        public Generic(final int index) {
            this(index, index, null, null);
        }

        public Generic(final int aLowerIndex, final int aHigherIndex) {
            this(aLowerIndex, aHigherIndex, null, null);
        }

        public Generic(final int aLowerIndex, final int aHigherIndex, final N aCosine, final N aSine) {

            super(aLowerIndex, aHigherIndex);

            cos = aCosine;
            sin = aSine;
        }

        public Generic(final Rotation<N> aRotation) {

            super(aRotation.low, aRotation.high);

            cos = aRotation.getCosine();
            sin = aRotation.getSine();
        }

        @Override
        public double doubleCosineValue() {
            return cos.doubleValue();
        }

        @Override
        public double doubleSineValue() {
            return sin.doubleValue();
        }

        @Override
        public N getCosine() {
            return cos;
        }

        @Override
        public N getSine() {
            return sin;
        }

        @Override
        public Generic<N> invert() {
            return new Generic<>(high, low, cos, sin);
        }

    }

    public static final class Primitive extends Rotation<Double> {

        public final double cos;
        public final double sin;

        public Primitive(final int index) {
            this(index, index, PrimitiveMath.NaN, PrimitiveMath.NaN);
        }

        public Primitive(final int aLowerIndex, final int aHigherIndex) {
            this(aLowerIndex, aHigherIndex, PrimitiveMath.NaN, PrimitiveMath.NaN);
        }

        public Primitive(final int aLowerIndex, final int aHigherIndex, final double aCosine, final double aSine) {

            super(aLowerIndex, aHigherIndex);

            cos = aCosine;
            sin = aSine;
        }

        public Primitive(final Rotation<Double> aRotation) {

            super(aRotation.low, aRotation.high);

            if ((aRotation.getCosine() != null) && !Double.isNaN(aRotation.doubleCosineValue())) {
                cos = aRotation.doubleCosineValue();
            } else {
                cos = Double.NaN;
            }

            if ((aRotation.getSine() != null) && !Double.isNaN(aRotation.doubleSineValue())) {
                sin = aRotation.doubleSineValue();
            } else {
                sin = Double.NaN;
            }
        }

        @Override
        public double doubleCosineValue() {
            return cos;
        }

        @Override
        public double doubleSineValue() {
            return sin;
        }

        @Override
        public Double getCosine() {
            return cos;
        }

        @Override
        public Double getSine() {
            return sin;
        }

        @Override
        public Primitive invert() {
            return new Primitive(high, low, cos, sin);
        }

    }

    public static Big makeBig(final int aLowerIndex, final int aHigherIndex, final BigDecimal anAngle) {
        return new Big(aLowerIndex, aHigherIndex, BigFunction.COS.invoke(anAngle), BigFunction.SIN.invoke(anAngle));
    }

    public static Complex makeComplex(final int aLowerIndex, final int aHigherIndex, final ComplexNumber anAngle) {
        return new Complex(aLowerIndex, aHigherIndex, ComplexFunction.COS.invoke(anAngle), ComplexFunction.SIN.invoke(anAngle));
    }

    public static <N extends Number & Scalar<N>> Generic<N> makeGeneric(final FunctionSet<N> functions, final int aLowerIndex, final int aHigherIndex,
            final N anAngle) {
        return new Generic<>(aLowerIndex, aHigherIndex, functions.cos().invoke(anAngle), functions.sin().invoke(anAngle));
    }

    public static Primitive makePrimitive(final int aLowerIndex, final int aHigherIndex, final double anAngle) {
        return new Primitive(aLowerIndex, aHigherIndex, PrimitiveFunction.COS.invoke(anAngle), PrimitiveFunction.SIN.invoke(anAngle));
    }

    static Rotation<BigDecimal>[] rotationsB(final PhysicalStore<BigDecimal> matrix, final int low, final int high, final Rotation<BigDecimal>[] results) {

        final BigDecimal a00 = matrix.get(low, low);
        final BigDecimal a01 = matrix.get(low, high);
        final BigDecimal a10 = matrix.get(high, low);
        final BigDecimal a11 = matrix.get(high, high);

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

        results[1] = new Rotation.Big(low, high, cj, sj); // Jacobi
        results[0] = new Rotation.Big(low, high, cj.multiply(cg).add(sj.multiply(sg)), cj.multiply(sg).subtract(sj.multiply(cg))); // Givens - Jacobi

        return results;
    }

    static Rotation<ComplexNumber>[] rotationsC(final PhysicalStore<ComplexNumber> matrix, final int low, final int high,
            final Rotation<ComplexNumber>[] results) {

        final ComplexNumber a00 = matrix.get(low, low);
        final ComplexNumber a01 = matrix.get(low, high);
        final ComplexNumber a10 = matrix.get(high, low);
        final ComplexNumber a11 = matrix.get(high, high);

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

        results[1] = new Rotation.Complex(low, high, cj, sj); // Jacobi
        results[0] = new Rotation.Complex(low, high, cj.multiply(cg).add(sj.multiply(sg)), cj.multiply(sg).subtract(sj.multiply(cg))); // Givens - Jacobi

        return results;
    }

    static Rotation<Double>[] rotationsP(final PhysicalStore<Double> matrix, final int low, final int high, final Rotation<Double>[] results) {

        final double a00 = matrix.doubleValue(low, low);
        final double a01 = matrix.doubleValue(low, high);
        final double a10 = matrix.doubleValue(high, low);
        final double a11 = matrix.doubleValue(high, high);

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

        results[1] = new Rotation.Primitive(low, high, cj, sj); // Jacobi
        results[0] = new Rotation.Primitive(low, high, ((cj * cg) + (sj * sg)), ((cj * sg) - (sj * cg))); // Givens - Jacobi

        return results;
    }

    public final int high;

    public final int low;

    @SuppressWarnings("unused")
    private Rotation() {

        this(0, 0);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected Rotation(final int aLowerIndex, final int aHigherIndex) {

        super();

        low = aLowerIndex;
        high = aHigherIndex;
    }

    public abstract double doubleCosineValue();

    public abstract double doubleSineValue();

    public abstract N getCosine();

    public abstract N getSine();

    public abstract Rotation<N> invert();

    @Override
    public String toString() {
        return "low=" + low + ", high=" + high + ", cos=" + this.getCosine() + ", sin=" + this.getSine();
    }

    public void transform(final PhysicalStore<N> matrix) {
        matrix.transformLeft(this);
    }

}
