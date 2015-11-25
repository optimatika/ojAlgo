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
package org.ojalgo.array;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.DenseArray.DenseFactory;
import org.ojalgo.array.SegmentedArray.SegmentedFactory;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.netio.ASCII;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

/**
 * <p>
 * A BasicArray is one-dimensional, but designed to easily be extended or encapsulated, and then treated as
 * arbitrary-dimensional. It stores/handles (any subclass of) {@linkplain java.lang.Number} elements depending
 * on the subclass/implementation.
 * </p>
 * <p>
 * This abstract class defines a set of methods to access and modify array elements. It does not "know"
 * anything about linear algebra or similar.
 * </p>
 *
 * @author apete
 */
public abstract class BasicArray<N extends Number>
        implements Access1D<N>, Access1D.Elements, Access1D.IndexOf, Access1D.Fillable<N>, Access1D.Modifiable<N>, Access1D.Visitable<N>, Serializable {

    static abstract class BasicFactory<N extends Number> extends ArrayFactory<N> {

        abstract DenseArray.DenseFactory<N> getDenseFactory();

        abstract SegmentedArray.SegmentedFactory<N> getSegmentedFactory();

        abstract SparseArray.SparseFactory<N> getSparseFactory();

        @Override
        BasicArray<N> makeStructuredZero(final long... structure) {

            final long tmpTotal = AccessUtils.count(structure);

            if (tmpTotal > MAX_ARRAY_SIZE) {
                return this.getSegmentedFactory().makeStructuredZero(structure);
            } else if (tmpTotal > OjAlgoUtils.ENVIRONMENT.getCacheDim1D(this.getDenseFactory().getElementSize())) {
                return new SparseArray<N>(tmpTotal, this.getDenseFactory());
            } else {
                return this.getDenseFactory().makeStructuredZero(structure);
            }
        }

        @Override
        BasicArray<N> makeToBeFilled(final long... structure) {

            final long tmpTotal = AccessUtils.count(structure);

            if (tmpTotal > MAX_ARRAY_SIZE) {
                return this.getSegmentedFactory().makeToBeFilled(structure);
            } else {
                return this.getDenseFactory().makeToBeFilled(structure);
            }

        }

    }

    /**
     * Exists as a private constant in {@link ArrayList}. The Oracle JVM seems to actually be limited at
     * Integer.MAX_VALUE - 2 but other JVM:s may have different limits.
     */
    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    static final BasicFactory<BigDecimal> BIG = new BasicFactory<BigDecimal>() {

        @Override
        DenseFactory<BigDecimal> getDenseFactory() {
            return BigArray.FACTORY;
        }

        @Override
        long getElementSize() {
            return BigArray.ELEMENT_SIZE;
        }

        @Override
        SegmentedFactory<BigDecimal> getSegmentedFactory() {
            return SegmentedArray.BIG;
        }

        @Override
        SparseFactory<BigDecimal> getSparseFactory() {
            return SparseArray.BIG;
        }

    };

    static final BasicFactory<ComplexNumber> COMPLEX = new BasicFactory<ComplexNumber>() {

        @Override
        DenseFactory<ComplexNumber> getDenseFactory() {
            return ComplexArray.FACTORY;
        }

        @Override
        long getElementSize() {
            return ComplexArray.ELEMENT_SIZE;
        }

        @Override
        SegmentedFactory<ComplexNumber> getSegmentedFactory() {
            return SegmentedArray.COMPLEX;
        }

        @Override
        SparseFactory<ComplexNumber> getSparseFactory() {
            return SparseArray.COMPLEX;
        }

    };

    static final BasicFactory<Double> PRIMITIVE = new BasicFactory<Double>() {

        @Override
        DenseFactory<Double> getDenseFactory() {
            return PrimitiveArray.FACTORY;
        }

        @Override
        long getElementSize() {
            return PrimitiveArray.ELEMENT_SIZE;
        }

        @Override
        SegmentedFactory<Double> getSegmentedFactory() {
            return SegmentedArray.PRIMITIVE;
        }

        @Override
        SparseFactory<Double> getSparseFactory() {
            return SparseArray.PRIMITIVE;
        }

    };

    static final BasicFactory<Quaternion> QUATERNION = new BasicFactory<Quaternion>() {

        @Override
        DenseFactory<Quaternion> getDenseFactory() {
            return QuaternionArray.FACTORY;
        }

        @Override
        long getElementSize() {
            return QuaternionArray.ELEMENT_SIZE;
        }

        @Override
        SegmentedFactory<Quaternion> getSegmentedFactory() {
            return SegmentedArray.QUATERNION;
        }

        @Override
        SparseFactory<Quaternion> getSparseFactory() {
            return SparseArray.QUATERNION;
        }

    };

    static final BasicFactory<RationalNumber> RATIONAL = new BasicFactory<RationalNumber>() {

        @Override
        DenseFactory<RationalNumber> getDenseFactory() {
            return RationalArray.FACTORY;
        }

        @Override
        long getElementSize() {
            return RationalArray.ELEMENT_SIZE;
        }

        @Override
        SegmentedFactory<RationalNumber> getSegmentedFactory() {
            return SegmentedArray.RATIONAL;
        }

        @Override
        SparseFactory<RationalNumber> getSparseFactory() {
            return SparseArray.RATIONAL;
        }

    };

    protected BasicArray() {
        super();
    }

    public long indexOfLargest() {
        return this.indexOfLargest(0L, this.count(), 1L);
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        return this.indexOfLargest(first, limit, 1L);
    }

    public void modifyAll(final UnaryFunction<N> function) {
        this.modify(0L, this.count(), 1L, function);
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        this.fillMatching(left, function, this);
    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        this.fillMatching(this, function, right);
    }

    public void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
        this.modify(first, limit, 1L, function);
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder();

        retVal.append(ASCII.LCB);
        retVal.append(ASCII.SP);
        final int tmpLength = (int) this.count();
        if (tmpLength >= 1) {
            retVal.append(this.get(0).toString());
            for (int i = 1; i < tmpLength; i++) {
                retVal.append(ASCII.COMMA);
                retVal.append(ASCII.SP);
                retVal.append(this.get(i).toString());
            }
            retVal.append(ASCII.SP);
        }
        retVal.append(ASCII.RCB);

        return retVal.toString();
    }

    public void visitAll(final VoidFunction<N> visitor) {
        this.visit(0L, this.count(), 1L, visitor);
    }

    public void visitRange(final long first, final long limit, final VoidFunction<N> visitor) {
        this.visit(first, limit, 1L, visitor);
    }

    /**
     * <p>
     * A utility facade that conveniently/consistently presents the {@linkplain org.ojalgo.array.BasicArray}
     * as a one-dimensional array. Note that you will modify the actual array by accessing it through this
     * facade.
     * </p>
     * <p>
     * Disregards the array structure, and simply treats it as one-domensional.
     * </p>
     */
    protected final Array1D<N> asArray1D() {
        return new Array1D<N>(this);
    }

    /**
     * <p>
     * A utility facade that conveniently/consistently presents the {@linkplain org.ojalgo.array.BasicArray}
     * as a two-dimensional array. Note that you will modify the actual array by accessing it through this
     * facade.
     * </p>
     * <p>
     * If "this" has more than two dimensions then only the first plane of the first cube of the first... is
     * used/accessed. If this only has one dimension then everything is assumed to be in the first column of
     * the first plane of the first cube...
     * </p>
     */
    protected final Array2D<N> asArray2D(final long structure) {
        return new Array2D<N>(this, structure);
    }

    /**
     * <p>
     * A utility facade that conveniently/consistently presents the {@linkplain org.ojalgo.array.BasicArray}
     * as a multi-dimensional array. Note that you will modify the actual array by accessing it through this
     * facade.
     * </p>
     */
    protected final ArrayAnyD<N> asArrayAnyD(final long[] structure) {
        return new ArrayAnyD<N>(this, structure);
    }

    protected abstract void exchange(long firstA, long firstB, long step, long count);

    protected abstract void fill(long first, long limit, long step, N value);

    protected abstract void fill(long first, long limit, long step, NullaryFunction<N> supplier);

    protected abstract long indexOfLargest(long first, long limit, long step);

    protected abstract boolean isSmall(long first, long limit, long step, double comparedTo);

    protected abstract void modify(long first, long limit, long step, Access1D<N> left, BinaryFunction<N> function);

    protected abstract void modify(long first, long limit, long step, BinaryFunction<N> function, Access1D<N> right);

    protected abstract void modify(long first, long limit, long step, UnaryFunction<N> function);

    protected abstract void visit(long first, long limit, long step, VoidFunction<N> visitor);

    /**
     * Safe to cast as DenseArray.
     */
    final boolean isDense() {
        return this instanceof DenseArray;
    }

    /**
     * Primitive (double) elements
     */
    abstract boolean isPrimitive();

    /**
     * Safe to cast as SegmentedArray.
     */
    final boolean isSegmented() {
        return this instanceof SegmentedArray;
    }

    /**
     * Safe to cast as SparseArray.
     */
    final boolean isSparse() {
        return this instanceof SparseArray;
    }

}
