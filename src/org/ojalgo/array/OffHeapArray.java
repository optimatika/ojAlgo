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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.lang.reflect.Field;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.machine.JavaType;
import org.ojalgo.scalar.PrimitiveScalar;

import sun.misc.Unsafe;

/**
 * Off heap memory array.
 *
 * @author apete
 */
public final class OffHeapArray extends BasicArray<Double> {

    public static final ArrayFactory<Double> FACTORY = new ArrayFactory<Double>() {

        @Override
        long getElementSize() {
            return JavaType.DOUBLE.memory();
        }

        @Override
        BasicArray<Double> makeStructuredZero(final long... structure) {
            return new OffHeapArray(AccessUtils.count(structure));
        }

        @Override
        BasicArray<Double> makeToBeFilled(final long... structure) {
            return new OffHeapArray(AccessUtils.count(structure));
        }

    };

    static Unsafe UNSAFE;

    static {

        Unsafe tmpUnsafe = null;

        try {
            final Field tmpField = Unsafe.class.getDeclaredField("theUnsafe");
            tmpField.setAccessible(true);
            tmpUnsafe = (Unsafe) tmpField.get(null);
        } catch (final Exception e) {
            tmpUnsafe = null;
        } finally {
            UNSAFE = tmpUnsafe;
        }
    }

    public static OffHeapArray make(final long count) {
        return new OffHeapArray(count);
    }

    public static final SegmentedArray<Double> makeSegmented(final long count) {
        return SegmentedArray.make(FACTORY, count);
    }

    private final long data;

    private final long myCount;

    OffHeapArray(final long count) {

        super();

        myCount = count;

        data = UNSAFE.allocateMemory(Unsafe.ARRAY_DOUBLE_INDEX_SCALE * count);

        this.fillAll(PrimitiveMath.ZERO);
    }

    public void add(final long index, final double addend) {
        final long tmpAddress = this.address(index);
        final double tmpCurrentValue = UNSAFE.getDouble(tmpAddress);
        UNSAFE.putDouble(tmpAddress, tmpCurrentValue + addend);
    }

    public void add(final long index, final Number addend) {
        this.add(index, addend.doubleValue());
    }

    public long count() {
        return myCount;
    }

    public double doubleValue(final long index) {
        return UNSAFE.getDouble(this.address(index));
    }

    public void fillOne(final long index, final Access1D<?> values, final long valueIndex) {
        this.set(index, values.doubleValue(valueIndex));
    }

    public void fillOne(final long index, final Double value) {
        this.set(index, value.doubleValue());
    }

    public void fillOne(final long index, final NullaryFunction<Double> supplier) {
        this.set(index, supplier.doubleValue());
    }

    public Double get(final long index) {
        return this.doubleValue(index);
    }

    public boolean isAbsolute(final long index) {
        return PrimitiveScalar.isAbsolute(this.doubleValue(index));
    }

    public boolean isSmall(final long index, final double comparedTo) {
        return PrimitiveScalar.isSmall(this.doubleValue(index), comparedTo);
    }

    public void modifyOne(final long index, final UnaryFunction<Double> modifier) {
        final long tmpAddress = this.address(index);
        final double tmpCurrentValue = UNSAFE.getDouble(tmpAddress);
        UNSAFE.putDouble(tmpAddress, modifier.invoke(tmpCurrentValue));
    }

    public void set(final long index, final double value) {
        UNSAFE.putDouble(this.address(index), value);
    }

    public void set(final long index, final Number value) {
        this.set(index, value.doubleValue());
    }

    public void visitOne(final long index, final VoidFunction<Double> visitor) {
        visitor.accept(this.doubleValue(index));
    }

    private final long address(final long index) {
        return data + (index * Unsafe.ARRAY_DOUBLE_INDEX_SCALE);
    }

    private final long increment(final long step) {
        return step * Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
    }

    @Override
    protected void exchange(final long firstA, final long firstB, final long step, final long count) {

        long tmpIndexA = firstA;
        long tmpIndexB = firstB;

        double tmpVal;

        for (long i = 0; i < count; i++) {

            tmpVal = this.doubleValue(tmpIndexA);
            this.set(tmpIndexA, this.doubleValue(tmpIndexB));
            this.set(tmpIndexB, tmpVal);

            tmpIndexA += step;
            tmpIndexB += step;
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final Double value) {
        final long tmpFirst = this.address(first);
        final long tmpLimit = this.address(limit);
        final long tmpStep = this.increment(step);
        final double tmpValue = value.doubleValue();
        for (long a = tmpFirst; a < tmpLimit; a += tmpStep) {
            UNSAFE.putDouble(a, tmpValue);
        }
    }

    @Override
    protected void fill(final long first, final long limit, final long step, final NullaryFunction<Double> supplier) {
        final long tmpFirst = this.address(first);
        final long tmpLimit = this.address(limit);
        final long tmpStep = this.increment(step);
        for (long a = tmpFirst; a < tmpLimit; a += tmpStep) {
            UNSAFE.putDouble(a, supplier.doubleValue());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        UNSAFE.freeMemory(data);
    }

    @Override
    protected long indexOfLargest(final long first, final long limit, final long step) {

        long retVal = first;
        double tmpLargest = ZERO;
        double tmpValue;

        for (long i = first; i < limit; i += step) {
            tmpValue = Math.abs(this.doubleValue(i));
            if (tmpValue > tmpLargest) {
                tmpLargest = tmpValue;
                retVal = i;
            }
        }

        return retVal;
    }

    @Override
    protected boolean isSmall(final long first, final long limit, final long step, final double comparedTo) {

        boolean retVal = true;

        for (long i = first; retVal && (i < limit); i += step) {
            retVal &= PrimitiveScalar.isSmall(comparedTo, this.doubleValue(i));
        }

        return retVal;
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final Access1D<Double> left, final BinaryFunction<Double> function) {
        long tmpAddress;
        for (long i = first; i < limit; i += step) {
            tmpAddress = this.address(i);
            UNSAFE.putDouble(tmpAddress, function.invoke(left.doubleValue(i), UNSAFE.getDouble(tmpAddress)));
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final BinaryFunction<Double> function, final Access1D<Double> right) {
        long tmpAddress;
        for (long i = first; i < limit; i += step) {
            tmpAddress = this.address(i);
            UNSAFE.putDouble(tmpAddress, function.invoke(UNSAFE.getDouble(tmpAddress), right.doubleValue(i)));
        }
    }

    @Override
    protected void modify(final long first, final long limit, final long step, final UnaryFunction<Double> function) {
        final long tmpFirst = this.address(first);
        final long tmpLimit = this.address(limit);
        final long tmpStep = this.increment(step);
        for (long a = tmpFirst; a < tmpLimit; a += tmpStep) {
            UNSAFE.putDouble(a, function.invoke(UNSAFE.getDouble(a)));
        }
    }

    @Override
    protected void visit(final long first, final long limit, final long step, final VoidFunction<Double> visitor) {
        final long tmpFirst = this.address(first);
        final long tmpLimit = this.address(limit);
        final long tmpStep = this.increment(step);
        for (long a = tmpFirst; a < tmpLimit; a += tmpStep) {
            visitor.invoke(UNSAFE.getDouble(a));
        }
    }

    @Override
    void clear() {
        ;
    }

    @Override
    boolean isPrimitive() {
        return true;
    }

}
