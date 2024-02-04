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
package org.ojalgo.type;

/**
 * Intended as a "boxed" version of any/all primitive numeric types.
 */
public interface PrimitiveNumber extends ComparableNumber<PrimitiveNumber> {

    class BoxedByte implements PrimitiveNumber {

        public final byte value;

        BoxedByte(final byte value) {
            super();
            this.value = value;
        }

        public byte byteValue() {
            return value;
        }

        public int compareTo(final PrimitiveNumber other) {
            return Byte.compare(value, other.byteValue());
        }

        public double doubleValue() {
            return value;
        }

        public float floatValue() {
            return value;
        }

        public int intValue() {
            return value;
        }

        public long longValue() {
            return value;
        }

        public short shortValue() {
            return value;
        }

    }

    class BoxedDouble implements PrimitiveNumber {

        public final double value;

        BoxedDouble(final double value) {
            super();
            this.value = value;
        }

        public int compareTo(final PrimitiveNumber other) {
            return Double.compare(value, other.doubleValue());
        }

        public double doubleValue() {
            return value;
        }

    }

    class BoxedFloat implements PrimitiveNumber {

        public final float value;

        BoxedFloat(final float value) {
            super();
            this.value = value;
        }

        public int compareTo(final PrimitiveNumber other) {
            return Float.compare(value, other.floatValue());
        }

        public double doubleValue() {
            return value;
        }

        public float floatValue() {
            return value;
        }

    }

    class BoxedInt implements PrimitiveNumber {

        public final int value;

        BoxedInt(final int value) {
            super();
            this.value = value;
        }

        public int compareTo(final PrimitiveNumber other) {
            return Integer.compare(value, other.intValue());
        }

        public double doubleValue() {
            return value;
        }

        public float floatValue() {
            return value;
        }

        public int intValue() {
            return value;
        }

        public long longValue() {
            return value;
        }

    }

    class BoxedLong implements PrimitiveNumber {

        public final long value;

        BoxedLong(final long value) {
            super();
            this.value = value;
        }

        public int compareTo(final PrimitiveNumber other) {
            return Long.compare(value, other.longValue());
        }

        public double doubleValue() {
            return value;
        }

        public float floatValue() {
            return value;
        }

        public long longValue() {
            return value;
        }

    }

    class BoxedShort implements PrimitiveNumber {

        public final short value;

        BoxedShort(final short value) {
            super();
            this.value = value;
        }

        public int compareTo(final PrimitiveNumber other) {
            return Short.compare(value, other.shortValue());
        }

        public double doubleValue() {
            return value;
        }

        public float floatValue() {
            return value;
        }

        public int intValue() {
            return value;
        }

        public long longValue() {
            return value;
        }

        public short shortValue() {
            return value;
        }

    }

    static PrimitiveNumber of(final byte value) {
        return new BoxedByte(value);
    }

    static PrimitiveNumber of(final double value) {
        return new BoxedDouble(value);
    }

    static PrimitiveNumber of(final float value) {
        return new BoxedFloat(value);
    }

    static PrimitiveNumber of(final int value) {
        return new BoxedInt(value);
    }

    static PrimitiveNumber of(final long value) {
        return new BoxedLong(value);
    }

    static PrimitiveNumber of(final short value) {
        return new BoxedShort(value);
    }

    static PrimitiveNumber valueOf(final byte value) {
        return PrimitiveNumber.of(value);
    }

    static PrimitiveNumber valueOf(final double value) {
        return PrimitiveNumber.of(value);
    }

    static PrimitiveNumber valueOf(final float value) {
        return PrimitiveNumber.of(value);
    }

    static PrimitiveNumber valueOf(final int value) {
        return PrimitiveNumber.of(value);
    }

    static PrimitiveNumber valueOf(final long value) {
        return PrimitiveNumber.of(value);
    }

    static PrimitiveNumber valueOf(final short value) {
        return PrimitiveNumber.of(value);
    }

}
