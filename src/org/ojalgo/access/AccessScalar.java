/*
 * Copyright 1997-2018 Optimatika
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

public interface AccessScalar<N extends Number> {

    /**
     * @see Number#byteValue()
     */
    default byte byteValue() {
        return this.get().byteValue();
    }

    /**
     * @see Number#doubleValue()
     */
    default double doubleValue() {
        return this.get().doubleValue();
    }

    /**
     * @see Number#floatValue()
     */
    default float floatValue() {
        return this.get().floatValue();
    }

    N get();

    /**
     * @deprecated v45 Use {@link #get()} instead
     */
    @Deprecated
    default N getNumber() {
        return this.get();
    }

    /**
     * @see Number#intValue()
     */
    default int intValue() {
        return this.get().intValue();
    }

    /**
     * @see Number#longValue()
     */
    default long longValue() {
        return this.get().longValue();
    }

    /**
     * @see Number#shortValue()
     */
    default short shortValue() {
        return this.get().shortValue();
    }

}
