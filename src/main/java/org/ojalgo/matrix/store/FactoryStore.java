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
package org.ojalgo.matrix.store;

import org.ojalgo.scalar.Scalar;

abstract class FactoryStore<N extends Comparable<N>> extends AbstractStore<N> {

    private final PhysicalStore.Factory<N, ?> myFactory;
    private final Scalar<N> myOne;
    private final Scalar<N> myZero;

    protected FactoryStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {

        super(rowsCount, columnsCount);

        myFactory = factory;

        myZero = factory.scalar().zero();
        myOne = factory.scalar().one();
    }

    protected FactoryStore(final PhysicalStore.Factory<N, ?> factory, final long rowsCount, final long columnsCount) {
        this(factory, Math.toIntExact(rowsCount), Math.toIntExact(columnsCount));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof FactoryStore)) {
            return false;
        }
        FactoryStore other = (FactoryStore) obj;
        if (myFactory == null) {
            if (other.myFactory != null) {
                return false;
            }
        } else if (!myFactory.equals(other.myFactory)) {
            return false;
        }
        if (myOne == null) {
            if (other.myOne != null) {
                return false;
            }
        } else if (!myOne.equals(other.myOne)) {
            return false;
        }
        if (myZero == null) {
            if (other.myZero != null) {
                return false;
            }
        } else if (!myZero.equals(other.myZero)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((myFactory == null) ? 0 : myFactory.hashCode());
        result = (prime * result) + ((myOne == null) ? 0 : myOne.hashCode());
        result = (prime * result) + ((myZero == null) ? 0 : myZero.hashCode());
        return result;
    }

    public final PhysicalStore.Factory<N, ?> physical() {
        return myFactory;
    }

    final Scalar<N> one() {
        return myOne;
    }

    final Scalar<N> zero() {
        return myZero;
    }

}
