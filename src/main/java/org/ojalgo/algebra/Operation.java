/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.algebra;

/**
 * @author apete
 * @see "https://en.wikipedia.org/wiki/Operation_(mathematics)"
 */
public interface Operation {

    /**
     * @see "https://en.wikipedia.org/wiki/Addition"
     */
    public interface Addition<T> extends Operation {

        /**
         * @param addend What to add
         * @return {@code this + addend}
         */
        T add(T addend);

    }

    /**
     * @see "https://en.wikipedia.org/wiki/Division_(mathematics)"
     */
    public interface Division<T> extends Operation {

        /**
         * @param divisor The divisor
         * @return {@code this / divisor}.
         */
        T divide(T divisor);

    }

    /**
     * @see "https://en.wikipedia.org/wiki/Multiplication"
     */
    public interface Multiplication<T> extends Operation {

        /**
         * @param multiplicand The multiplicand
         * @return {@code this * multiplicand}.
         */
        T multiply(T multiplicand);

        /**
         * Multiply by itself {@code power} times.
         */
        T power(int power);

    }

    /**
     * @see "https://en.wikipedia.org/wiki/Subtraction"
     */
    public interface Subtraction<T> extends Operation {

        /**
         * @param subtrahend The subtrahend
         * @return {@code this - subtrahend}.
         */
        T subtract(T subtrahend);
    }

}
