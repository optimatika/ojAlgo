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
package org.ojalgo.access;

import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public abstract class AccessUtils {

    public static int column(final int index, final int structure) {
        return index / structure;
    }

    public static int column(final int index, final int[] structure) {
        return AccessUtils.column(index, structure[0]);
    }

    public static int column(final long index, final int structure) {
        return (int) (index / structure);
    }

    public static long column(final long index, final long structure) {
        return index / structure;
    }

    public static long column(final long index, final long[] structure) {
        return AccessUtils.column(index, structure[0]);
    }

    /**
     * @param structure An access structure
     * @return The size of an access with that structure
     */
    public static int count(final int[] structure) {
        int retVal = 1;
        final int tmpLength = structure.length;
        for (int i = 0; i < tmpLength; i++) {
            retVal *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     */
    public static int count(final int[] structure, final int dimension) {
        return structure.length > dimension ? structure[dimension] : 1;
    }

    /**
     * @param structure An access structure
     * @return The size of an access with that structure
     */
    public static long count(final long[] structure) {
        long retVal = 1;
        final int tmpLength = structure.length;
        for (int i = 0; i < tmpLength; i++) {
            retVal *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     */
    public static long count(final long[] structure, final int dimension) {
        return structure.length > dimension ? structure[dimension] : 1;
    }

    @SuppressWarnings("unchecked")
    public static boolean equals(final Access1D<?> accessA, final Access1D<?> accessB, final NumberContext context) {

        final long tmpLength = accessA.count();

        boolean retVal = tmpLength == accessB.count();

        final double tmpError = context.error();

        if ((accessA.get(0) instanceof ComplexNumber) && (accessB.get(0) instanceof ComplexNumber)) {

            final Access1D<ComplexNumber> tmpAccessA = (Access1D<ComplexNumber>) accessA;
            final Access1D<ComplexNumber> tmpAccessB = (Access1D<ComplexNumber>) accessB;

            for (int i = 0; retVal && (i < tmpLength); i++) {
                retVal &= (tmpAccessA.get(i).subtract(tmpAccessB.get(i))).norm() <= tmpError;
            }

        } else {

            for (int i = 0; retVal && (i < tmpLength); i++) {
                retVal &= Math.abs(accessA.doubleValue(i) - accessB.doubleValue(i)) <= tmpError;
            }
        }

        return retVal;
    }

    public static boolean equals(final Access2D<?> accessA, final Access2D<?> accessB, final NumberContext context) {
        return (accessA.countRows() == accessB.countRows()) && (accessA.countColumns() == accessB.countColumns())
                && AccessUtils.equals((Access1D<?>) accessA, (Access1D<?>) accessB, context);
    }

    public static boolean equals(final AccessAnyD<?> accessA, final AccessAnyD<?> accessB, final NumberContext context) {

        boolean retVal = true;
        int d = 0;
        long tmpCount;

        do {
            tmpCount = accessA.count(d);
            retVal &= tmpCount == accessB.count(d);
            d++;
        } while (retVal && ((d <= 3) || (tmpCount > 1)));

        return retVal && AccessUtils.equals((Access1D<?>) accessA, (Access1D<?>) accessB, context);
    }

    public static int hashCode(final Access1D<?> access) {
        final int tmpSize = (int) access.count();
        int retVal = tmpSize + 31;
        for (int ij = 0; ij < tmpSize; ij++) {
            retVal *= access.doubleValue(ij);
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    public static int index(final int[] structure, final int[] reference) {
        int retVal = reference[0];
        int tmpFactor = structure[0];
        final int tmpLength = reference.length;
        for (int i = 1; i < tmpLength; i++) {
            retVal += tmpFactor * reference[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    public static int index(final int[] structure, final long[] reference) {
        int retVal = (int) reference[0];
        int tmpFactor = structure[0];
        final int tmpLength = reference.length;
        for (int i = 1; i < tmpLength; i++) {
            retVal += tmpFactor * reference[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    public static long index(final long[] structure, final long[] reference) {
        long retVal = reference[0];
        long tmpFactor = structure[0];
        final int tmpLength = reference.length;
        for (int i = 1; i < tmpLength; i++) {
            retVal += tmpFactor * reference[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    public static int[] makeDecreasingRange(final int first, final int count) {
        final int[] retVal = new int[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first - i;
        }
        return retVal;
    }

    public static long[] makeDecreasingRange(final long first, final int count) {
        final long[] retVal = new long[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first - i;
        }
        return retVal;
    }

    public static int[] makeIncreasingRange(final int first, final int count) {
        final int[] retVal = new int[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first + i;
        }
        return retVal;
    }

    public static long[] makeIncreasingRange(final long first, final int count) {
        final long[] retVal = new long[count];
        for (int i = 0; i < count; i++) {
            retVal[i] = first + i;
        }
        return retVal;
    }

    public static int row(final int index, final int structure) {
        return index % structure;
    }

    public static int row(final int index, final int[] structure) {
        return AccessUtils.row(index, structure[0]);
    }

    public static int row(final long index, final int structure) {
        return (int) (index % structure);
    }

    public static long row(final long index, final long structure) {
        return index % structure;
    }

    public static long row(final long index, final long[] structure) {
        return AccessUtils.row(index, structure[0]);
    }

    /**
     * @param structure An access structure
     * @return The size of an access with that structure
     * @deprecated v35 Use {@link #count(int[])} instead
     */
    @Deprecated
    public static int size(final int[] structure) {
        return AccessUtils.count(structure);
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     * @deprecated Use {@link #count(int[],int)} instead
     */
    @Deprecated
    public static int size(final int[] structure, final int dimension) {
        return AccessUtils.count(structure, dimension);
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index indication a direction
     * @return The step size (index change) in that direction
     */
    public static int step(final int[] structure, final int dimension) {
        int retVal = 1;
        for (int i = 0; i < dimension; i++) {
            retVal *= AccessUtils.count(structure, i);
        }
        return retVal;
    }

    /**
     * A more complex/general version of {@linkplain #step(int[], int)}.
     *
     * @param structure An access structure
     * @param increment A vector indication a direction (and size)
     * @return The step size (index change)
     */
    public static int step(final int[] structure, final int[] increment) {
        int retVal = 0;
        int tmpFactor = 1;
        final int tmpLimit = increment.length;
        for (int i = 1; i < tmpLimit; i++) {
            retVal += tmpFactor * increment[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index indication a direction
     * @return The step size (index change) in that direction
     */
    public static long step(final long[] structure, final int dimension) {
        long retVal = 1;
        for (int i = 0; i < dimension; i++) {
            retVal *= AccessUtils.count(structure, i);
        }
        return retVal;
    }

    /**
     * A more complex/general version of {@linkplain #step(int[], int)}.
     *
     * @param structure An access structure
     * @param increment A vector indication a direction (and size)
     * @return The step size (index change)
     */
    public static long step(final long[] structure, final long[] increment) {
        long retVal = 0;
        long tmpFactor = 1;
        final int tmpLimit = increment.length;
        for (int i = 1; i < tmpLimit; i++) {
            retVal += tmpFactor * increment[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    public static long[] structure(final StructureAnyD structure) {

        final long tmpSize = structure.count();

        long tmpTotal = structure.count(0);
        int tmpRank = 1;

        while (tmpTotal < tmpSize) {
            tmpTotal *= structure.count(tmpRank);
            tmpRank++;
        }

        final long[] retVal = new long[tmpRank];

        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = structure.count(i);
        }

        return retVal;
    }

    private AccessUtils() {
        super();
    }

}
