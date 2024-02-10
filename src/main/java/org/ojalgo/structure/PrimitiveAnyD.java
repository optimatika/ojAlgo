/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.structure;

import org.ojalgo.type.NumberDefinition;

public abstract class PrimitiveAnyD implements AccessAnyD<Double>, MutateAnyD {

    static final class Simple extends PrimitiveAnyD {

        private final int[] myStructure;
        private final double[] myValues;

        Simple(final int[] structure) {
            myValues = new double[StructureAnyD.size(structure)];
            myStructure = structure;
        }

        @Override
        public double doubleValue(final int index) {
            return myValues[index];
        }

        @Override
        public int rank() {
            return myStructure.length;
        }

        @Override
        public void set(final int index, final double value) {
            myValues[index] = value;
        }

        @Override
        public int size() {
            return myValues.length;
        }

        @Override
        public int size(final int dimension) {
            return myStructure[dimension];
        }
    }

    static final class Wrapper extends PrimitiveAnyD {

        private final StructureAnyD myDelegate;

        Wrapper(final StructureAnyD delegate) {
            myDelegate = delegate;
        }

        @Override
        public long count() {
            return myDelegate.count();
        }

        @Override
        public long count(final int dimension) {
            return myDelegate.count(dimension);
        }

        @Override
        public double doubleValue(final int index) {
            if (myDelegate instanceof AccessAnyD<?>) {
                return ((AccessAnyD<?>) myDelegate).doubleValue(index);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public double doubleValue(final long index) {
            if (myDelegate instanceof AccessAnyD<?>) {
                return ((AccessAnyD<?>) myDelegate).doubleValue(index);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public int rank() {
            return myDelegate.rank();
        }

        @Override
        public void set(final int index, final double value) {
            if (myDelegate instanceof MutateAnyD) {
                ((MutateAnyD) myDelegate).set(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void set(final long index, final double value) {
            if (myDelegate instanceof MutateAnyD) {
                ((MutateAnyD) myDelegate).set(index, value);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public long[] shape() {
            return myDelegate.shape();
        }

        @Override
        public int size() {
            return myDelegate.size();
        }

        @Override
        public int size(final int dimension) {
            return myDelegate.size(dimension);
        }
    }

    public static final PrimitiveAnyD EMPTY = new Simple(new int[0]);

    public static PrimitiveAnyD newInstance(final int... structure) {
        return new Simple(structure);
    }

    public static PrimitiveAnyD wrap(final StructureAnyD delegate) {
        return new Wrapper(delegate);
    }

    @Override
    public final Double get(final long... ref) {
        return Double.valueOf(this.doubleValue(ref));
    }

    @Override
    public final Double get(final long index) {
        return Double.valueOf(this.doubleValue(index));
    }

    @Override
    public final void set(final long index, final Comparable<?> value) {
        this.set(index, NumberDefinition.doubleValue(value));

    }

    @Override
    public final void set(final long[] reference, final Comparable<?> value) {
        this.set(reference, NumberDefinition.doubleValue(value));
    }

    @Override
    public final String toString() {
        return AccessAnyD.toString(this);
    }
}
