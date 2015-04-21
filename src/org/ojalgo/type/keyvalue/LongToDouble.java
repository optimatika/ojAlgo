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
package org.ojalgo.type.keyvalue;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.netio.ASCII;

public final class LongToDouble implements KeyValue<Long, Double> {

    public final long key;
    public final double value;

    public LongToDouble(final long aKey) {

        super();

        key = aKey;
        value = PrimitiveMath.NaN;
    }

    public LongToDouble(final long aKey, final double aValue) {

        super();

        key = aKey;
        value = aValue;
    }

    public LongToDouble(final long aKey, final Double aValue) {

        super();

        key = aKey;
        value = aValue != null ? aValue : ZERO;
    }

    public LongToDouble(final Long aKey, final double aValue) {

        super();

        key = aKey != null ? aKey : 0l;
        value = aValue;
    }

    public LongToDouble(final Long aKey, final Double aValue) {

        super();

        key = aKey != null ? aKey : 0l;
        value = aValue != null ? aValue : ZERO;
    }

    LongToDouble() {
        this(0l, ZERO);
    }

    public int compareTo(final KeyValue<Long, ?> aReference) {
        return (key < aReference.getKey() ? -1 : (key == aReference.getKey() ? 0 : 1));
    }

    public int compareTo(final LongToDouble aReference) {
        return (key < aReference.key ? -1 : (key == aReference.key ? 0 : 1));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LongToDouble)) {
            return false;
        }
        final LongToDouble other = (LongToDouble) obj;
        if (key != other.key) {
            return false;
        }
        return true;
    }

    public Long getKey() {
        return key;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (key ^ (key >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(key) + String.valueOf(ASCII.EQUALS) + String.valueOf(value);
    }

}
