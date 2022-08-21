/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.type.math;

import java.math.BigDecimal;
import java.math.MathContext;

import org.ojalgo.algebra.NumberSet;
import org.ojalgo.machine.JavaType;

/**
 * The number sets supported by ojAlgo, paired with a declaration of how they are implemented/approximated.
 *
 * @author apete
 */
public enum MathType {

    /**
     * Complex Number: 2 * double
     */
    C128(NumberSet.C, JavaType.DOUBLE, 2),
    /**
     * Quaternion: 4 * double
     */
    H256(NumberSet.H, JavaType.DOUBLE, 4),
    /**
     * Rational Number: 2 * long
     */
    Q128(NumberSet.Q, JavaType.LONG, 2),
    /**
     * Real Number: float
     */
    R032(NumberSet.R, JavaType.FLOAT, 1),
    /**
     * Real Number: double
     */
    R064(NumberSet.R, JavaType.DOUBLE, 1),
    /**
     * Real Number: {@link BigDecimal}
     * <p>
     * Refer to it as "128" since the {@link MathContext#DECIMAL128} is used by default (when necessary).
     */
    R128(NumberSet.R, JavaType.REFERENCE, 1),
    /**
     * Integer: byte
     */
    Z008(NumberSet.Z, JavaType.BYTE, 1),
    /**
     * Integer: short
     */
    Z016(NumberSet.Z, JavaType.SHORT, 1),
    /**
     * Integer: int
     */
    Z032(NumberSet.Z, JavaType.INT, 1),
    /**
     * Integer: long
     */
    Z064(NumberSet.Z, JavaType.LONG, 1);

    private int myComponents;
    private JavaType myJavaType;
    private NumberSet myNumberSet;

    MathType(final NumberSet numberSet, final JavaType javaType, final int components) {
        myNumberSet = numberSet;
        myJavaType = javaType;
        myComponents = components;
    }

}
