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
package org.ojalgo.matrix.transformation;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;

public abstract class Rotation<N extends Comparable<N>> {

    public static final class Generic<N extends Scalar<N>> extends Rotation<N> {

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

    public static <N extends Scalar<N>> Generic<N> makeGeneric(final FunctionSet<N> functions, final int aLowerIndex, final int aHigherIndex, final N anAngle) {
        return new Generic<>(aLowerIndex, aHigherIndex, functions.cos().invoke(anAngle), functions.sin().invoke(anAngle));
    }

    public static Primitive makePrimitive(final int aLowerIndex, final int aHigherIndex, final double anAngle) {
        return new Primitive(aLowerIndex, aHigherIndex, PrimitiveMath.COS.invoke(anAngle), PrimitiveMath.SIN.invoke(anAngle));
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
            cg = PrimitiveMath.SIGNUM.invoke(x);
            sg = PrimitiveMath.ZERO;
        } else if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, x)) {
            sg = PrimitiveMath.SIGNUM.invoke(y);
            cg = PrimitiveMath.ZERO;
        } else if (PrimitiveMath.ABS.invoke(y) > PrimitiveMath.ABS.invoke(x)) {
            t = x / y; // cot
            sg = PrimitiveMath.SIGNUM.invoke(y) / PrimitiveMath.SQRT1PX2.invoke(t);
            cg = sg * t;
        } else {
            t = y / x; // tan
            cg = PrimitiveMath.SIGNUM.invoke(x) / PrimitiveMath.SQRT1PX2.invoke(t);
            sg = cg * t;
        }

        final double b00 = (cg * a00) + (sg * a10);
        final double b11 = (cg * a11) - (sg * a01);
        final double b2 = (cg * (a01 + a10)) + (sg * (a11 - a00)); // b01 + b10

        t = (b11 - b00) / b2;
        t = PrimitiveMath.SIGNUM.invoke(t) / (PrimitiveMath.SQRT1PX2.invoke(t) + PrimitiveMath.ABS.invoke(t)); // tan Jacobi

        // Annihilate - Jacobi
        final double cj = PrimitiveMath.ONE / PrimitiveMath.SQRT1PX2.invoke(t); // cos Jacobi
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
