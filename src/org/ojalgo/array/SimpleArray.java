/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.array;

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Iterator1D;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.TypeUtils;

/**
 * The purpose of (use case for) this class is to provide a minimal class that implements the {@link Access1D} interface
 * and is mutable. Further the user can assume that an ArrayAccess<BigDecimal> instance really is an ArrayAccess.Big
 * instance (and correspondingly for Double, ComplexNumber and RationalNumber). Therefore it is safe to type cast and
 * reference the data attribute. Further this class allows you to, with generics, mix primitive value arrays and object
 * reference arrays.
 * 
 * @author apete
 * @deprecated v36 Use {@linkplain BasicArray} instead.
 */
@Deprecated
public abstract class SimpleArray<N extends Number> implements Access1D<N>, Access1D.Fillable<N> {

    /**
     * Big
     * 
     * @author apete
     * @deprecated v36 Use {@linkplain BigArray} instead
     */
    @Deprecated
    public static final class Big extends SimpleArray<BigDecimal> {

        public final BigDecimal[] data;

        Big(final BigDecimal[] theData) {

            super(theData.length);

            data = theData;
        }

        Big(final int aLength) {

            super(aLength);

            data = new BigDecimal[aLength];
            Arrays.fill(data, ZERO);
        }

        public double doubleValue(final long index) {
            return data[(int) index].doubleValue();
        }

        public void fillAll(final BigDecimal value) {
            Arrays.fill(data, value);
        }

        public void fillRange(final long first, final long limit, final BigDecimal value) {
            for (int i = (int) first; i < limit; i++) {
                data[i] = value;
            }
        }

        public BigDecimal get(final long index) {
            return data[(int) index];
        }

        public BigDecimal set(final int index, final Number value) {
            final BigDecimal retVal = data[index];
            data[index] = TypeUtils.toBigDecimal(value);
            return retVal;
        }

        public void set(final long index, final double value) {
            data[(int) index] = TypeUtils.toBigDecimal(value);
        }

        public void set(final long index, final Number value) {
            data[(int) index] = TypeUtils.toBigDecimal(value);
        }

    }

    /**
     * Complex
     * 
     * @author apete
     * @deprecated v36 Use {@linkplain ComplexArray} instead
     */
    @Deprecated
    public static final class Complex extends SimpleArray<ComplexNumber> {

        public final ComplexNumber[] data;

        Complex(final ComplexNumber[] theData) {

            super(theData.length);

            data = theData;
        }

        Complex(final int aLength) {

            super(aLength);

            data = new ComplexNumber[aLength];
            Arrays.fill(data, ComplexNumber.ZERO);
        }

        public double doubleValue(final long index) {
            return data[(int) index].doubleValue();
        }

        public void fillAll(final ComplexNumber value) {
            Arrays.fill(data, value);
        }

        public void fillRange(final long first, final long limit, final ComplexNumber value) {
            for (int i = (int) first; i < limit; i++) {
                data[i] = value;
            }
        }

        public ComplexNumber get(final long index) {
            return data[(int) index];
        }

        public ComplexNumber set(final int index, final Number value) {
            final ComplexNumber retVal = data[index];
            data[index] = TypeUtils.toComplexNumber(value);
            return retVal;
        }

        public void set(final long index, final double value) {
            data[(int) index] = TypeUtils.toComplexNumber(value);
        }

        public void set(final long index, final Number value) {
            data[(int) index] = TypeUtils.toComplexNumber(value);
        }

    }

    /**
     * Primitive
     * 
     * @author apete
     * @deprecated v36 Use {@linkplain PrimitiveArray} instead
     */
    @Deprecated
    public static final class Primitive extends SimpleArray<Double> {

        public final double[] data;

        Primitive(final double[] theData) {

            super(theData.length);

            data = theData;
        }

        Primitive(final int aLength) {

            super(aLength);

            data = new double[aLength];
        }

        public double doubleValue(final long index) {
            return data[(int) index];
        }

        public void fillAll(final Double value) {
            Arrays.fill(data, value);
        }

        public void fillRange(final long first, final long limit, final Double value) {
            for (int i = (int) first; i < limit; i++) {
                data[i] = value;
            }
        }

        public Double get(final long index) {
            return data[(int) index];
        }

        public Double set(final int index, final Number value) {
            final double retVal = data[index];
            data[index] = value.doubleValue();
            return retVal;
        }

        public void set(final long index, final double value) {
            data[(int) index] = value;
        }

        public void set(final long index, final Number value) {
            data[(int) index] = value.doubleValue();
        }

    }

    /**
     * Rational
     * 
     * @author apete
     * @deprecated v36 Use {@linkplain RationalArray} instead
     */
    @Deprecated
    public static final class Rational extends SimpleArray<RationalNumber> {

        public final RationalNumber[] data;

        Rational(final int aLength) {

            super(aLength);

            data = new RationalNumber[aLength];
            Arrays.fill(data, RationalNumber.ZERO);
        }

        Rational(final RationalNumber[] theData) {

            super(theData.length);

            data = theData;
        }

        public double doubleValue(final long index) {
            return data[(int) index].doubleValue();
        }

        public void fillAll(final RationalNumber value) {
            Arrays.fill(data, value);
        }

        public void fillRange(final long first, final long limit, final RationalNumber value) {
            for (int i = (int) first; i < limit; i++) {
                data[i] = value;
            }
        }

        public RationalNumber get(final long index) {
            return data[(int) index];
        }

        public RationalNumber set(final int index, final Number value) {
            final RationalNumber retVal = data[index];
            data[index] = TypeUtils.toRationalNumber(value);
            return retVal;
        }

        public void set(final long index, final double value) {
            data[(int) index] = TypeUtils.toRationalNumber(value);
        }

        public void set(final long index, final Number value) {
            data[(int) index] = TypeUtils.toRationalNumber(value);
        }

    }

    public static final SimpleArray.Big makeBig(final int aLength) {
        return new SimpleArray.Big(aLength);
    }

    public static final SimpleArray.Complex makeComplex(final int aLength) {
        return new SimpleArray.Complex(aLength);
    }

    public static final SimpleArray.Primitive makePrimitive(final int aLength) {
        return new SimpleArray.Primitive(aLength);
    }

    public static final SimpleArray.Rational makeRational(final int aLength) {
        return new SimpleArray.Rational(aLength);
    }

    public static final SimpleArray.Big wrapBig(final BigDecimal[] theData) {
        return new SimpleArray.Big(theData);
    }

    public static final SimpleArray.Complex wrapComplex(final ComplexNumber[] theData) {
        return new SimpleArray.Complex(theData);
    }

    public static final SimpleArray.Primitive wrapPrimitive(final double[] theData) {
        return new SimpleArray.Primitive(theData);
    }

    public static final SimpleArray.Rational wrapRational(final RationalNumber[] theData) {
        return new SimpleArray.Rational(theData);
    }

    public final int length;

    @SuppressWarnings("unused")
    private SimpleArray() {
        this(0);
    }

    SimpleArray(final int aLength) {

        super();

        length = aLength;
    }

    public long count() {
        return length;
    }

    public final Iterator<N> iterator() {
        return new Iterator1D<N>(this);
    }

    public int size() {
        return length;
    }

}
