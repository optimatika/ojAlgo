/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.access;

import java.math.BigDecimal;

import org.ojalgo.array.BasicArray;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

@Deprecated
public abstract class AccessUtils {

    /**
     * @deprecated v44 Use {@link Access1D#asBig1D(Access1D<?>)} instead
     */
    @Deprecated
    public static Access1D<BigDecimal> asBig1D(final Access1D<?> access) {
        return Access1D.asBig1D(access);
    }

    /**
     * @deprecated v44 Use {@link Access2D#asBig2D(Access2D<?>)} instead
     */
    @Deprecated
    public static Access2D<BigDecimal> asBig2D(final Access2D<?> access) {
        return Access2D.asBig2D(access);
    }

    /**
     * @deprecated v44 Use {@link AccessAnyD#asBigAnyD(AccessAnyD<?>)} instead
     */
    @Deprecated
    public static AccessAnyD<BigDecimal> asBigAnyD(final AccessAnyD<?> access) {
        return AccessAnyD.asBigAnyD(access);
    }

    /**
     * @deprecated v44 Use {@link Access1D#asComplex1D(Access1D<?>)} instead
     */
    @Deprecated
    public static Access1D<ComplexNumber> asComplex1D(final Access1D<?> access) {
        return Access1D.asComplex1D(access);
    }

    /**
     * @deprecated v44 Use {@link Access2D#asComplex2D(Access2D<?>)} instead
     */
    @Deprecated
    public static Access2D<ComplexNumber> asComplex2D(final Access2D<?> access) {
        return Access2D.asComplex2D(access);
    }

    /**
     * @deprecated v44 Use {@link AccessAnyD#asComplexAnyD(AccessAnyD<?>)} instead
     */
    @Deprecated
    public static AccessAnyD<ComplexNumber> asComplexAnyD(final AccessAnyD<?> access) {
        return AccessAnyD.asComplexAnyD(access);
    }

    /**
     * @deprecated v44 Use {@link Access1D#asPrimitive1D(Access1D<?>)} instead
     */
    @Deprecated
    public static Access1D<Double> asPrimitive1D(final Access1D<?> access) {
        return Access1D.asPrimitive1D(access);
    }

    /**
     * @deprecated v44 Use {@link Access2D#asPrimitive2D(Access2D<?>)} instead
     */
    @Deprecated
    public static Access2D<Double> asPrimitive2D(final Access2D<?> access) {
        return Access2D.asPrimitive2D(access);
    }

    /**
     * @deprecated v44 Use {@link AccessAnyD#asPrimitiveAnyD(AccessAnyD<?>)} instead
     */
    @Deprecated
    public static AccessAnyD<Double> asPrimitiveAnyD(final AccessAnyD<?> access) {
        return AccessAnyD.asPrimitiveAnyD(access);
    }

    /**
     * @deprecated v44 Use {@link Access1D#asQuaternion1D(Access1D<?>)} instead
     */
    @Deprecated
    public static Access1D<Quaternion> asQuaternion1D(final Access1D<?> access) {
        return Access1D.asQuaternion1D(access);
    }

    /**
     * @deprecated v44 Use {@link Access2D#asQuaternion2D(Access2D<?>)} instead
     */
    @Deprecated
    public static Access2D<Quaternion> asQuaternion2D(final Access2D<?> access) {
        return Access2D.asQuaternion2D(access);
    }

    /**
     * @deprecated v44 Use {@link AccessAnyD#asQuaternionAnyD(AccessAnyD<?>)} instead
     */
    @Deprecated
    public static AccessAnyD<Quaternion> asQuaternionAnyD(final AccessAnyD<?> access) {
        return AccessAnyD.asQuaternionAnyD(access);
    }

    /**
     * @deprecated v44 Use {@link Access1D#asRational1D(Access1D<?>)} instead
     */
    @Deprecated
    public static Access1D<RationalNumber> asRational1D(final Access1D<?> access) {
        return Access1D.asRational1D(access);
    }

    /**
     * @deprecated v44 Use {@link Access2D#asRational2D(Access2D<?>)} instead
     */
    @Deprecated
    public static Access2D<RationalNumber> asRational2D(final Access2D<?> access) {
        return Access2D.asRational2D(access);
    }

    /**
     * @deprecated v44 Use {@link AccessAnyD#asRationalAnyD(AccessAnyD<?>)} instead
     */
    @Deprecated
    public static AccessAnyD<RationalNumber> asRationalAnyD(final AccessAnyD<?> access) {
        return AccessAnyD.asRationalAnyD(access);
    }

    /**
     * @param structure An access structure
     * @return The size of an access with that structure
     * @deprecated v44 Use {@link StructureAnyD#count(int[])} instead
     */
    @Deprecated
    public static int count(final int[] structure) {
        return StructureAnyD.count(structure);
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     * @deprecated v44 Use {@link StructureAnyD#count(int[],int)} instead
     */
    @Deprecated
    public static int count(final int[] structure, final int dimension) {
        return StructureAnyD.count(structure, dimension);
    }

    /**
     * @param structure An access structure
     * @return The size of an access with that structure
     * @deprecated v44 Use {@link StructureAnyD#count(long[])} instead
     */
    @Deprecated
    public static long count(final long[] structure) {
        return StructureAnyD.count(structure);
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     * @deprecated v44 Use {@link StructureAnyD#count(long[],int)} instead
     */
    @Deprecated
    public static long count(final long[] structure, final int dimension) {
        return StructureAnyD.count(structure, dimension);
    }

    /**
     * @deprecated v44 Use {@link Access1D#equals(Access1D<?>,Access1D<?>,NumberContext)} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static boolean equals(final Access1D<?> accessA, final Access1D<?> accessB, final NumberContext context) {
        return Access1D.equals(accessA, accessB, context);
    }

    /**
     * @deprecated v44 Use {@link Access2D#equals(Access2D<?>,Access2D<?>,NumberContext)} instead
     */
    @Deprecated
    public static boolean equals(final Access2D<?> accessA, final Access2D<?> accessB, final NumberContext context) {
        return Access2D.equals(accessA, accessB, context);
    }

    /**
     * @deprecated v44 Use {@link AccessAnyD#equals(AccessAnyD<?>,AccessAnyD<?>,NumberContext)} instead
     */
    @Deprecated
    public static boolean equals(final AccessAnyD<?> accessA, final AccessAnyD<?> accessB, final NumberContext context) {
        return AccessAnyD.equals(accessA, accessB, context);
    }

    /**
     * @deprecated v44 Use {@link Access1D#hashCode(Access1D<?>)} instead
     */
    @Deprecated
    public static int hashCode(final Access1D<?> access) {
        return Access1D.hashCode(access);
    }

    /**
     * @deprecated v44 Use {@link BasicArray#makeDecreasingRange(int,int)} instead
     */
    @Deprecated
    public static int[] makeDecreasingRange(final int first, final int count) {
        return BasicArray.makeDecreasingRange(first, count);
    }

    /**
     * @deprecated v44 Use {@link BasicArray#makeDecreasingRange(long,int)} instead
     */
    @Deprecated
    public static long[] makeDecreasingRange(final long first, final int count) {
        return BasicArray.makeDecreasingRange(first, count);
    }

    /**
     * @deprecated v44 Use {@link BasicArray#makeIncreasingRange(int,int)} instead
     */
    @Deprecated
    public static int[] makeIncreasingRange(final int first, final int count) {
        return BasicArray.makeIncreasingRange(first, count);
    }

    /**
     * @deprecated v44 Use {@link BasicArray#makeIncreasingRange(long,int)} instead
     */
    @Deprecated
    public static long[] makeIncreasingRange(final long first, final int count) {
        return BasicArray.makeIncreasingRange(first, count);
    }

    private AccessUtils() {
        super();
    }

}
