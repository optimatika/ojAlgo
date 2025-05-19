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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

import org.ojalgo.array.operation.AMAX;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.math.MathType;

/**
 * A one- and/or arbitrary-dimensional array of {@linkplain java.math.BigDecimal}.
 *
 * @author apete
 */
public class ArrayR256 extends ReferenceTypeArray<BigDecimal> {

    public static final ReferenceTypeArray.Factory<BigDecimal> FACTORY = new ReferenceTypeArray.Factory<>(MathType.R256) {

        public ArrayR256 make(final int size) {
            return ArrayR256.make(size);
        }

    };

    public static ArrayR256 make(final int size) {
        BigDecimal[] data = new BigDecimal[size];
        Arrays.fill(data, BigDecimal.ZERO);
        return new ArrayR256(data);
    }

    public static ArrayR256 wrap(final BigDecimal... data) {
        return new ArrayR256(data);
    }

    protected ArrayR256(final BigDecimal[] data) {
        super(FACTORY, data);
    }

    @Override
    public void axpy(final double a, final Mutate1D.Modifiable<?> y) {
        AXPY.invoke(y, a, data);
    }

    @Override
    public byte byteValue(final int index) {
        return this.get(index).byteValue();
    }

    @Override
    public double doubleValue(final int index) {
        return data[index].doubleValue();
    }

    @Override
    public float floatValue(final int index) {
        return data[index].floatValue();
    }

    @Override
    public int intValue(final int index) {
        return this.get(index).intValue();
    }

    @Override
    public long longValue(final int index) {
        return this.get(index).longValue();
    }

    @Override
    public void set(final int index, final long value) {
        data[index] = new BigDecimal(value);
    }

    @Override
    public short shortValue(final int index) {
        return this.get(index).shortValue();
    }

    @Override
    public void sortAscending() {
        Arrays.sort(data);
    }

    @Override
    public void sortDescending() {
        Arrays.sort(data, Comparator.reverseOrder());
    }

    @Override
    protected void add(final int index, final Comparable<?> addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)));
    }

    @Override
    protected void add(final int index, final double addend) {
        this.fillOne(index, this.get(index).add(this.valueOf(addend)));
    }

    @Override
    protected void fillOne(final int index, final Access1D<?> values, final long valueIndex) {
        data[index] = this.valueOf(values.get(valueIndex));
    }

    @Override
    protected int indexOfLargest(final int first, final int limit, final int step) {
        return AMAX.invoke(data, first, limit, step);
    }

    @Override
    protected boolean isAbsolute(final int index) {
        return BigScalar.isAbsolute(data[index]);
    }

    @Override
    protected boolean isSmall(final int index, final double comparedTo) {
        return BigScalar.isSmall(comparedTo, data[index]);
    }

}
