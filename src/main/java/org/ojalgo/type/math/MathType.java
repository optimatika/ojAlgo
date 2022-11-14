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

import org.ojalgo.algebra.NumberSet;
import org.ojalgo.machine.JavaType;
import org.ojalgo.scalar.ComplexNumber;

/**
 * The number sets supported by ojAlgo, paired with a declaration of how they are implemented/approximated.
 * The naming scheme is the {@link NumberSet} symbol paired with the total number of bits used to represent
 * the components.
 * <p>
 * For instance: C128 referns to C = {@link ComplexNumber} which is implemeted as two double:s each of 64
 * bits. If there would be a float based complex number implementation it would be named C064.
 * <p>
 * R032 is a Real number implemented/approximated using a single 32 bit float.
 *
 * @author apete
 */
public enum MathType {

    /**
     * Complex Number: 2 * double
     */
    C128(NumberSet.C, 2, JavaType.DOUBLE),
    /**
     * Quaternion: 4 * double
     */
    H256(NumberSet.H, 4, JavaType.DOUBLE),
    /**
     * Rational Number: 2 * long
     */
    Q128(NumberSet.Q, 2, JavaType.LONG),
    /**
     * Real Number: float
     */
    R032(NumberSet.R, 1, JavaType.FLOAT),
    /**
     * Real Number: double
     */
    R064(NumberSet.R, 1, JavaType.DOUBLE),
    /**
     * Real Number: 2 * double
     * <p>
     * Quadruple precision emulated using a pair of double precision numbers
     */
    R128(NumberSet.R, 2, JavaType.DOUBLE),
    /**
     * Real Number: {@link BigDecimal}
     * <p>
     * Refer to it as "256" since (the way it's used in ojAlgo) it roughly corresponds to binary 256 octuple
     * precision.
     */
    R256(NumberSet.R, 1, JavaType.REFERENCE),
    /**
     * Integer: byte
     */
    Z008(NumberSet.Z, 1, JavaType.BYTE),
    /**
     * Integer: short
     */
    Z016(NumberSet.Z, 1, JavaType.SHORT),
    /**
     * Integer: int
     */
    Z032(NumberSet.Z, 1, JavaType.INT),
    /**
     * Integer: long
     */
    Z064(NumberSet.Z, 1, JavaType.LONG);

    private final int myComponents;
    private final JavaType myJavaType;
    private final NumberSet myNumberSet;

    MathType(final NumberSet numberSet, final int components, final JavaType javaType) {
        myNumberSet = numberSet;
        myJavaType = javaType;
        myComponents = components;
    }

    public int getComponents() {
        return myComponents;
    }

    public Class<?> getJavaClass() {
        return myJavaType.getJavaClass();
    }

    public JavaType getJavaType() {
        return myJavaType;
    }

    public NumberSet getNumberSet() {
        return myNumberSet;
    }

    public long getTotalMemory() {
        return myComponents * myJavaType.memory();
    }

    public boolean isPrimitive() {
        return myComponents == 1 && myJavaType != JavaType.REFERENCE;
    }

}
