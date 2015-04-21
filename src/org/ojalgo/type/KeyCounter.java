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
package org.ojalgo.type;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class KeyCounter<K> {

    protected static final int INT_ZERO = 0;

    private final HashMap<K, AtomicInteger> myDelegate = new HashMap<K, AtomicInteger>();

    public KeyCounter() {
        super();
    }

    public int decrement(final K aKey) {
        return this.getValue(aKey).decrementAndGet();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof KeyCounter)) {
            return false;
        }
        final KeyCounter<?> other = (KeyCounter<?>) obj;
        if (myDelegate == null) {
            if (other.myDelegate != null) {
                return false;
            }
        } else if (!myDelegate.equals(other.myDelegate)) {
            return false;
        }
        return true;
    }

    public int get(final K aKey) {
        return this.getValue(aKey).get();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((myDelegate == null) ? 0 : myDelegate.hashCode());
        return result;
    }

    public int increment(final K aKey) {
        return this.getValue(aKey).incrementAndGet();
    }

    public int reset(final K aKey) {
        this.getValue(aKey).set(INT_ZERO);
        return INT_ZERO;
    }

    public int set(final K aKey, final int aValue) {
        this.getValue(aKey).set(aValue);
        return aValue;
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    private synchronized AtomicInteger getValue(final K aKey) {
        AtomicInteger retVal = myDelegate.get(aKey);
        if (retVal == null) {
            retVal = new AtomicInteger();
            myDelegate.put(aKey, retVal);
        }
        return retVal;
    }

}
