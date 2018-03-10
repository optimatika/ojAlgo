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

import java.util.function.Consumer;

import org.ojalgo.ProgrammingError;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * 1-dimensional mutator methods
 *
 * @author apete
 */
public interface Mutate1D extends Structure1D {

    interface BiModifiable<N extends Number> extends Structure1D {

        void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function);

        void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right);

    }

    /**
     * Fills the target
     *
     * @author apete
     */
    interface Fillable<N extends Number> extends Structure1D {

        default void fillAll(final N value) {
            this.fillRange(0L, this.count(), value);
        }

        default void fillAll(final NullaryFunction<N> supplier) {
            this.fillRange(0L, this.count(), supplier);
        }

        /**
         * <p>
         * Will fill the elements of [this] with the corresponding input values, and in the process (if
         * necessary) convert the elements to the correct type:
         * </p>
         * <code>this(i) = values(i)</code>
         */
        default void fillMatching(final Access1D<?> values) {
            Structure1D.loopMatching(this, values, i -> this.fillOne(i, values, i));
        }

        default void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {
            Structure1D.loopMatching(left, right, i -> this.fillOne(i, function.invoke(left.get(i), right.get(i))));
        }

        default void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {
            Structure1D.loopMatching(this, arguments, i -> this.fillOne(i, function.invoke(arguments.get(i))));
        }

        void fillOne(long index, final Access1D<?> values, long valueIndex);

        void fillOne(long index, N value);

        void fillOne(long index, NullaryFunction<N> supplier);

        default void fillRange(final long first, final long limit, final N value) {
            Structure1D.loopRange(first, limit, i -> this.fillOne(i, value));
        }

        default void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
            Structure1D.loopRange(first, limit, i -> this.fillOne(i, supplier));
        }
    }

    /**
     * Mix/combine the previously existing value, at index, with the supplied addend. The intention is that
     * implementations should make this method thread safe even if the class as a whole is not (which is
     * usually the case).
     *
     * @author apete
     */
    interface Mixable<N extends Number> extends Structure1D {

        /**
         * @return The new/mixed value
         */
        double mix(long index, BinaryFunction<N> mixer, double addend);

        N mix(long index, BinaryFunction<N> mixer, N addend);

    }

    interface Modifiable<N extends Number> extends Structure1D {

        default void modifyAll(final UnaryFunction<N> modifier) {
            this.modifyRange(0L, this.count(), modifier);
        }

        void modifyOne(long index, UnaryFunction<N> modifier);

        default void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
            Structure1D.loopRange(first, limit, i -> this.modifyOne(i, modifier));
        }

    }

    /**
     * Anything/everything that does not require interaction with already existing elements.
     *
     * @author apete
     */
    interface Receiver<N extends Number> extends Mutate1D, Mutate1D.Fillable<N>, Consumer<Access1D<?>> {

        default void accept(final Access1D<?> supplied) {
            if (this.isAcceptable(supplied)) {
                supplied.loopAll(i -> this.set(i, supplied.get(i)));
            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

        default boolean isAcceptable(final Structure1D supplier) {
            return this.count() >= supplier.count();
        }

    }

    interface Sortable extends Structure1D {

        void sortAscending();

        void sortDescending();

    }

    void add(long index, double addend);

    void add(long index, Number addend);

    /**
     * Reset this mutable structure to some standard (all zeros) initial state. It must still be usuable after
     * this call, and the structure/size/shape must not change.
     */
    default void reset() {
        this.loopAll(i -> this.set(i, PrimitiveMath.ZERO));
    }

    void set(long index, double value);

    void set(long index, Number value);

}
