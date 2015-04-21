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
package org.ojalgo.matrix.transformation;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.scalar.ComplexNumber;

public abstract class Rotation<N extends Number> extends Object {

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

    public static Primitive makePrimitive(final int aLowerIndex, final int aHigherIndex, final double anAngle) {
        return new Primitive(aLowerIndex, aHigherIndex, PrimitiveFunction.COS.invoke(anAngle), PrimitiveFunction.SIN.invoke(anAngle));
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

}
