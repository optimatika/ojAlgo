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
package org.ojalgo.business;

import org.ojalgo.constant.PrimitiveMath;

public enum AccessLevel {

    ADMIN(9), MINIMAL(1), NONE(0), NORMAL(3), RESTRICTED(2), SUPER(6);

    public static AccessLevel valueOf(final Number value) {

        final int tmpIntValue = (int) Math.log10(value.doubleValue());

        if (tmpIntValue >= 9) {
            return ADMIN;
        } else if (tmpIntValue >= 6) {
            return SUPER;
        } else if (tmpIntValue >= 3) {
            return NORMAL;
        } else if (tmpIntValue >= 2) {
            return RESTRICTED;
        } else if (tmpIntValue >= 1) {
            return MINIMAL;
        } else {
            return NONE;
        }
    }

    private final int myIntValue;

    private AccessLevel(final int levelExp) {
        myIntValue = (int) Math.pow(PrimitiveMath.TEN, levelExp);
    }

    public int intValue() {
        return myIntValue;
    }
}
