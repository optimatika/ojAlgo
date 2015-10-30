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
package org.ojalgo.algebra;

/**
 * @author apete
 * @see <a href="https://en.wikipedia.org/wiki/Operation_(mathematics)">Operation (mathematics)</a>
 */
public interface Operation {

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Addition">Addition</a>
     */
    public interface Addition<T> extends Operation {

        /**
         * @param addend What to add
         * @return <code>this + addend</code>
         */
        T add(T addend);

    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Division_(mathematics)">Division (mathematics)</a>
     */
    public interface Division<T> extends Operation {

        /**
         * @return <code>this / divisor</code>.
         */
        T divide(T divisor);

    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Multiplication">Multiplication</a>
     */
    public interface Multiplication<T> extends Operation {

        /**
         * @return <code>this * multiplicand</code>.
         */
        T multiply(T multiplicand);

    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Subtraction">Subtraction</a>
     */
    public interface Subtraction<T> extends Operation {

        /**
         * @return <code>this - subtrahend</code>.
         */
        T subtract(T subtrahend);
    }

}
