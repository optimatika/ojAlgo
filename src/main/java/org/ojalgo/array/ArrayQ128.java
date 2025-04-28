/*
 * Copyright 1997-2025 Optimatika
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

import java.util.Arrays;

import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.math.MathType;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain org.ojalgo.scalar.RationalNumber}.
 *
 * @author apete
 */
public class ArrayQ128 extends ScalarArray<RationalNumber> {

    public static final ScalarArray.Factory<RationalNumber> FACTORY = new ScalarArray.Factory<>(MathType.Q128) {

        public ArrayQ128 make(final int size) {
            return ArrayQ128.make(size);
        }
    };

    public static ArrayQ128 make(final int size) {
        RationalNumber[] data = new RationalNumber[size];
        Arrays.fill(data, RationalNumber.ZERO);
        return new ArrayQ128(data);
    }

    public static ArrayQ128 wrap(final RationalNumber... data) {
        return new ArrayQ128(data);
    }

    protected ArrayQ128(final RationalNumber[] data) {
        super(FACTORY, data);
    }

}
