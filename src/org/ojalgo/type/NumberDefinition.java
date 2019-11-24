/*
 * Copyright 1997-2019 Optimatika
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

/**
 * An interface that defines what is already in {@link java.lang.Number}. Hopefully Java itself will one day
 * get an interface similar to this. When/if that happens this interface, and possibly some additional usage
 * of <code>extends java.lang.Number</code>, will be replaced by that new interface.
 */
public interface NumberDefinition {

    default boolean booleanValue() {
        return this.intValue() != 0;
    }

    default byte byteValue() {
        return (byte) this.intValue();
    }

    double doubleValue();

    default float floatValue() {
        return (float) this.doubleValue();
    }

    default int intValue() {
        return Math.round(this.floatValue());
    }

    default long longValue() {
        return Math.round(this.doubleValue());
    }

    default short shortValue() {
        return (short) this.intValue();
    }

}
