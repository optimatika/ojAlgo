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
 */
public interface ScalarOperation {

    public interface Addition<T, N extends Number> extends ScalarOperation {

        /**
         * @return <code>this + scalarAddend</code>.
         */
        T add(double scalarAddend);

        /**
         * @return <code>this + scalarAddend</code>.
         */
        T add(N scalarAddend);

    }

    public interface Division<T, N extends Number> extends ScalarOperation {

        /**
         * @return <code>this / scalarDivisor</code>.
         */
        T divide(double scalarDivisor);

        /**
         * @return <code>this / scalarDivisor</code>.
         */
        T divide(N scalarDivisor);

    }

    public interface Multiplication<T, N extends Number> extends ScalarOperation {

        /**
         * @return <code>this * scalarMultiplicand</code>.
         */
        T multiply(double scalarMultiplicand);

        /**
         * @return <code>this * multiplicand</code>.
         */
        T multiply(N scalarMultiplicand);

    }

    public interface Subtraction<T, N extends Number> extends ScalarOperation {

        /**
         * @return <code>this - scalarSubtrahend</code>.
         */
        T subtract(double scalarSubtrahend);

        /**
         * @return <code>this - scalarSubtrahend</code>.
         */
        T subtract(N scalarSubtrahend);

    }

}
