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
package org.ojalgo.access;

import java.math.BigDecimal;

import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public abstract class AccessUtils {

    public static Access1D<BigDecimal> asBig1D(final Access1D<?> access) {
        return new Access1D<BigDecimal>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public BigDecimal get(final long index) {
                return TypeUtils.toBigDecimal(access.get(index));
            }

        };
    }

    public static Access2D<BigDecimal> asBig2D(final Access2D<?> access) {
        return new Access2D<BigDecimal>() {

            public long count() {
                return access.count();
            }

            public long countColumns() {
                return access.countColumns();
            }

            public long countRows() {
                return access.countRows();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long row, final long column) {
                return access.doubleValue(row, column);
            }

            public BigDecimal get(final long index) {
                return TypeUtils.toBigDecimal(access.get(index));
            }

            public BigDecimal get(final long row, final long column) {
                return TypeUtils.toBigDecimal(access.get(row, column));
            }

        };
    }

    public static AccessAnyD<BigDecimal> asBigAnyD(final AccessAnyD<?> access) {
        return new AccessAnyD<BigDecimal>() {

            public long count() {
                return access.count();
            }

            public long count(final int dimension) {
                return access.count(dimension);
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long[] reference) {
                return access.doubleValue(reference);
            }

            public BigDecimal get(final long index) {
                return TypeUtils.toBigDecimal(access.get(index));
            }

            public BigDecimal get(final long[] reference) {
                return TypeUtils.toBigDecimal(access.get(reference));
            }

            public long[] shape() {
                return access.shape();
            }

        };
    }

    public static Access1D<ComplexNumber> asComplex1D(final Access1D<?> access) {
        return new Access1D<ComplexNumber>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public ComplexNumber get(final long index) {
                return ComplexNumber.valueOf(access.get(index));
            }

        };
    }

    public static Access2D<ComplexNumber> asComplex2D(final Access2D<?> access) {
        return new Access2D<ComplexNumber>() {

            public long count() {
                return access.count();
            }

            public long countColumns() {
                return access.countColumns();
            }

            public long countRows() {
                return access.countRows();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long row, final long column) {
                return access.doubleValue(row, column);
            }

            public ComplexNumber get(final long index) {
                return ComplexNumber.valueOf(access.get(index));
            }

            public ComplexNumber get(final long row, final long column) {
                return ComplexNumber.valueOf(access.get(row, column));
            }

        };
    }

    public static AccessAnyD<ComplexNumber> asComplexAnyD(final AccessAnyD<?> access) {
        return new AccessAnyD<ComplexNumber>() {

            public long count() {
                return access.count();
            }

            public long count(final int dimension) {
                return access.count(dimension);
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long[] reference) {
                return access.doubleValue(reference);
            }

            public ComplexNumber get(final long index) {
                return ComplexNumber.valueOf(access.get(index));
            }

            public ComplexNumber get(final long[] reference) {
                return ComplexNumber.valueOf(access.get(reference));
            }

            public long[] shape() {
                return access.shape();
            }

        };
    }

    public static Access1D<Double> asPrimitive1D(final Access1D<?> access) {
        return new Access1D<Double>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public Double get(final long index) {
                return access.doubleValue(index);
            }

        };
    }

    public static Access2D<Double> asPrimitive2D(final Access2D<?> access) {
        return new Access2D<Double>() {

            public long count() {
                return access.count();
            }

            public long countColumns() {
                return access.countColumns();
            }

            public long countRows() {
                return access.countRows();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long row, final long column) {
                return access.doubleValue(row, column);
            }

            public Double get(final long index) {
                return access.doubleValue(index);
            }

            public Double get(final long row, final long column) {
                return access.doubleValue(row, column);
            }

        };
    }

    public static AccessAnyD<Double> asPrimitiveAnyD(final AccessAnyD<?> access) {
        return new AccessAnyD<Double>() {

            public long count() {
                return access.count();
            }

            public long count(final int dimension) {
                return access.count(dimension);
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long[] reference) {
                return access.doubleValue(reference);
            }

            public Double get(final long index) {
                return access.doubleValue(index);
            }

            public Double get(final long[] reference) {
                return access.doubleValue(reference);
            }

            public long[] shape() {
                return access.shape();
            }

        };
    }

    public static Access1D<Quaternion> asQuaternion1D(final Access1D<?> access) {
        return new Access1D<Quaternion>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public Quaternion get(final long index) {
                return Quaternion.valueOf(access.get(index));
            }

        };
    }

    public static Access2D<Quaternion> asQuaternion2D(final Access2D<?> access) {
        return new Access2D<Quaternion>() {

            public long count() {
                return access.count();
            }

            public long countColumns() {
                return access.countColumns();
            }

            public long countRows() {
                return access.countRows();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long row, final long column) {
                return access.doubleValue(row, column);
            }

            public Quaternion get(final long index) {
                return Quaternion.valueOf(access.get(index));
            }

            public Quaternion get(final long row, final long column) {
                return Quaternion.valueOf(access.get(row, column));
            }

        };
    }

    public static AccessAnyD<Quaternion> asQuaternionAnyD(final AccessAnyD<?> access) {
        return new AccessAnyD<Quaternion>() {

            public long count() {
                return access.count();
            }

            public long count(final int dimension) {
                return access.count(dimension);
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long[] reference) {
                return access.doubleValue(reference);
            }

            public Quaternion get(final long index) {
                return Quaternion.valueOf(access.get(index));
            }

            public Quaternion get(final long[] reference) {
                return Quaternion.valueOf(access.get(reference));
            }

            public long[] shape() {
                return access.shape();
            }

        };
    }

    public static Access1D<RationalNumber> asRational1D(final Access1D<?> access) {
        return new Access1D<RationalNumber>() {

            public long count() {
                return access.count();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public RationalNumber get(final long index) {
                return RationalNumber.valueOf(access.get(index));
            }

        };
    }

    public static Access2D<RationalNumber> asRational2D(final Access2D<?> access) {
        return new Access2D<RationalNumber>() {

            public long count() {
                return access.count();
            }

            public long countColumns() {
                return access.countColumns();
            }

            public long countRows() {
                return access.countRows();
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long row, final long column) {
                return access.doubleValue(row, column);
            }

            public RationalNumber get(final long index) {
                return RationalNumber.valueOf(access.get(index));
            }

            public RationalNumber get(final long row, final long column) {
                return RationalNumber.valueOf(access.get(row, column));
            }

        };
    }

    public static AccessAnyD<RationalNumber> asRationalAnyD(final AccessAnyD<?> access) {
        return new AccessAnyD<RationalNumber>() {

            public long count() {
                return access.count();
            }

            public long count(final int dimension) {
                return access.count(dimension);
            }

            public double doubleValue(final long index) {
                return access.doubleValue(index);
            }

            public double doubleValue(final long[] reference) {
                return access.doubleValue(reference);
            }

            public RationalNumber get(final long index) {
                return RationalNumber.valueOf(access.get(index));
            }

            public RationalNumber get(final long[] reference) {
                return RationalNumber.valueOf(access.get(reference));
            }

            public long[] shape() {
                return access.shape();
            }

        };
    }

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

        if ((accessA.get(0) instanceof ComplexNumber) && (accessB.get(0) instanceof ComplexNumber)) {

            final Access1D<ComplexNumber> tmpAccessA = (Access1D<ComplexNumber>) accessA;
            final Access1D<ComplexNumber> tmpAccessB = (Access1D<ComplexNumber>) accessB;

            for (int i = 0; retVal && (i < tmpLength); i++) {
                retVal &= !context.isDifferent(tmpAccessA.get(i).getReal(), tmpAccessB.get(i).getReal());
                retVal &= !context.isDifferent(tmpAccessA.get(i).i, tmpAccessB.get(i).i);
            }

        } else {

            for (int i = 0; retVal && (i < tmpLength); i++) {
                retVal &= !context.isDifferent(accessA.doubleValue(i), accessB.doubleValue(i));
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

    public static int index(final int structure, final int row, final int column) {
        return row + (column * structure);
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

    public static long index(final long structure, final long row, final long column) {
        return row + (column * structure);
    }

    /**
     * @param structure An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    public static long index(final long[] structure, final long[] reference) {
        long retVal = reference[0];
        long tmpFactor = structure[0];
        final int tmpLength = Math.min(structure.length, reference.length);
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

    public static long[] reference(final long index, final long[] structure) {

        final long[] retVal = new long[structure.length];

        long tmpPrev = 1L;
        long tmpNext = 1L;

        for (int s = 0; s < structure.length; s++) {
            tmpNext *= structure[s];
            retVal[s] = (index % tmpNext) / tmpPrev;
            tmpPrev = tmpNext;
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

    public static long[] shape(final StructureAnyD structure) {

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

    /**
     * @deprecated v39 Use {@link #shape(StructureAnyD)} instead
     */
    @Deprecated
    public static long[] structure(final StructureAnyD structure) {
        return AccessUtils.shape(structure);
    }

    private AccessUtils() {
        super();
    }

}
