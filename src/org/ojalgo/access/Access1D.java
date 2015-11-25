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
package org.ojalgo.access;

import java.util.Iterator;
import java.util.List;
import java.util.stream.BaseStream;
import java.util.stream.StreamSupport;

import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionUtils;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
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
         * @param index
         * @return
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

    public interface IndexOf extends Structure1D {

        default long indexOfLargest() {
            return this.indexOfLargestInRange(0L, this.count());
        }

        long indexOfLargestInRange(final long first, final long limit);

    }

    public interface Factory<I extends Access1D<?>> {

        I copy(Access1D<?> source);

        I copy(double... source);

        I copy(List<? extends Number> source);

        I copy(Number... source);

        I makeFilled(long count, NullaryFunction<?> supplier);

        I makeZero(long count);

    }

    public interface Fillable<N extends Number> extends Settable<N> {

        void fillAll(N value);

        void fillAll(NullaryFunction<N> supplier);

        /**
         * <p>
         * Will fill the elements of [this] with the corresponding input values, and in the process (if
         * necessary) convert the elements to the correct type:
         * </p>
         * <code>this(i) = values(i)</code>
         */
        default void fillMatching(final Access1D<?> values) {
            final long tmpLimit = FunctionUtils.min(this.count(), values.count());
            for (long i = 0; i < tmpLimit; i++) {
                this.fillOneMatching(i, values, i);
            }
        }

        /**
         * <p>
         * Will fill the elements of [this] with the results of element wise invocation of the input binary
         * funtion:
         * </p>
         * <code>this(i) = function.invoke(left(i),right(i))</code>
         */
        default void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
            final long tmpLimit = FunctionUtils.min(left.count(), right.count(), this.count());
            for (long i = 0; i < tmpLimit; i++) {
                this.fillOne(i, function.invoke(left.get(i), right.get(i)));
            }
        }

        /**
         * <p>
         * Will fill the elements of [this] with the results of element wise invocation of the input unary
         * funtion:
         * </p>
         * <code>this(i) = function.invoke(arguments(i))</code>
         */
        default void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {
            final long tmpLimit = FunctionUtils.min(this.count(), arguments.count());
            for (long i = 0L; i < tmpLimit; i++) {
                this.fillOne(i, function.invoke(arguments.get(i)));
            }
        }

        void fillOne(long index, N value);

        void fillOne(long index, NullaryFunction<N> supplier);

        void fillOneMatching(long index, final Access1D<?> values, long valueIndex);

        void fillRange(long first, long limit, N value);

        void fillRange(long first, long limit, NullaryFunction<N> supplier);

    }

    public interface Modifiable<N extends Number> extends Settable<N> {

        void modifyAll(UnaryFunction<N> function);

        void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function);

        void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right);

        // void modifyOneMatching(long index, final Access1D<N> left, final BinaryFunction<N> function);

        // void modifyOneMatching(long index, final BinaryFunction<N> function, final Access1D<N> right);

        void modifyOne(long index, UnaryFunction<N> function);

        void modifyRange(long first, long limit, UnaryFunction<N> function);

    }

    public interface Settable<N extends Number> extends Structure1D {

        void add(long index, double addend);

        void add(long index, Number addend);

        void set(long index, double value);

        void set(long index, Number value);

    }

    public interface Sliceable<N extends Number> extends Structure1D {

        Access1D<N> sliceRange(long first, long limit);

    }

    public interface Visitable<N extends Number> extends Structure1D {

        void visitAll(VoidFunction<N> visitor);

        void visitOne(long index, VoidFunction<N> visitor);

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

    default double[] toRawCopy1D() {
        return ArrayUtils.toRawCopyOf(this);
    }

}
