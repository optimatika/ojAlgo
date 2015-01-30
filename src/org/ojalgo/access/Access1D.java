/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import java.util.Iterator;
import java.util.List;
import java.util.stream.BaseStream;
import java.util.stream.StreamSupport;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.Scalar;

public interface Access1D<N extends Number> extends Structure1D, Iterable<N> {

    public interface Builder<I extends Access1D<?>> extends Structure1D {

        I build();

        Builder<I> fillAll(final Number value);

        Builder<I> set(long index, double value);

        Builder<I> set(long index, Number value);

    }

    public interface Elements extends Structure1D {

        /**
         * @see Scalar#isAbsolute()
         */
        boolean isAbsolute(long index);

        /**
         * @see Scalar#isSmall(double)
         */
        boolean isSmall(long index, double comparedTo);

        /**
         * @see Scalar#isZero()
         * @deprecated v37
         */
        @Deprecated
        default boolean isZero(final long index) {
            return this.isSmall(index, PrimitiveMath.ONE);
        }

    }

    public interface Factory<I extends Access1D<?>> {

        I copy(Access1D<?> source);

        I copy(double... source);

        I copy(List<? extends Number> source);

        I copy(Number... source);

        I makeRandom(long count, RandomNumber distribution);

        I makeZero(long count);

    }

    public interface Fillable<N extends Number> extends Structure1D {

        void fillAll(N value);

        void fillAll(NullaryFunction<N> supplier);

        void fillRange(long first, long limit, N value);

        void fillRange(long first, long limit, NullaryFunction<N> supplier);

        void set(long index, double value);

        void set(long index, Number value);

    }

    public interface Modifiable<N extends Number> extends Structure1D {

        void modifyAll(UnaryFunction<N> function);

        void modifyOne(long index, UnaryFunction<N> function);

        void modifyRange(long first, long limit, UnaryFunction<N> function);

    }

    public interface Visitable<N extends Number> extends Structure1D {

        void visitAll(VoidFunction<N> visitor);

        void visitRange(long first, long limit, VoidFunction<N> visitor);

    }

    double doubleValue(long index);

    N get(long index);

    default Iterator<N> iterator() {
        return new Iterator1D<>(this);
    }

    default BaseStream<N, ? extends BaseStream<N, ?>> stream(final boolean parallel) {
        return StreamSupport.stream(this.spliterator(), parallel);
    }

}
