/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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

import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.Scalar;

public interface AccessAnyD<N extends Number> extends StructureAnyD, Access1D<N> {

    /**
     * This interface mimics {@linkplain Fillable}, but methods return the builder instance instead, and then adds the
     * {@link #build()} method.
     * 
     * @author apete
     */
    public interface Builder<I extends AccessAnyD<?>> extends StructureAnyD, Access1D.Builder<I> {

        I build();

        Builder<I> set(long[] reference, double value);

        Builder<I> set(long[] reference, Number value);

    }

    public interface Elements extends StructureAnyD, Access1D.Elements {

        /**
         * @see Scalar#isAbsolute()
         */
        boolean isAbsolute(long[] reference);

        /**
         * @see Scalar#isInfinite()
         * @deprecated v36 Only plan to keep {@link #isAbsolute(long[])} and {@link #isZero(long[])}.
         */
        @Deprecated
        boolean isInfinite(long[] reference);

        /**
         * @see Scalar#isNaN()
         * @deprecated v36 Only plan to keep {@link #isAbsolute(long[])} and {@link #isZero(long[])}.
         */
        @Deprecated
        boolean isNaN(long[] reference);

        /**
         * @see Scalar#isPositive()
         * @deprecated v36 Only plan to keep {@link #isAbsolute(long[])} and {@link #isZero(long[])}.
         */
        @Deprecated
        boolean isPositive(long[] reference);

        /**
         * @see Scalar#isReal()
         * @deprecated v36 Only plan to keep {@link #isAbsolute(long[])} and {@link #isZero(long[])}.
         */
        @Deprecated
        boolean isReal(long[] reference);

        /**
         * @see Scalar#isZero()
         */
        boolean isZero(long[] reference);

    }

    public interface Factory<I extends AccessAnyD<?>> {

        I copy(AccessAnyD<?> source);

        I makeRandom(long[] structure, RandomNumber distribution);

        I makeZero(long... structure);

    }

    public interface Fillable<N extends Number> extends StructureAnyD, Access1D.Fillable<N> {

        void set(long[] reference, double value);

        void set(long[] reference, Number value);

    }

    public interface Modifiable<N extends Number> extends StructureAnyD, Access1D.Modifiable<N> {

    }

    public interface Visitable<N extends Number> extends StructureAnyD, Access1D.Visitable<N> {

    }

    double doubleValue(long[] reference);

    N get(long[] reference);

}
