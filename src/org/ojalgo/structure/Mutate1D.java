/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.structure;

import java.util.function.Consumer;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.scalar.ComplexNumber;

/**
 * 1-dimensional mutator methods
 *
 * @author apete
 */
public interface Mutate1D extends Structure1D {

    /**
     * Fills the target
     *
     * @author apete
     */
    interface Fillable<N extends Comparable<N>> extends Structure1D {

        default void fillAll(final N value) {
            this.fillRange(0L, this.count(), value);
        }

        default void fillAll(final NullaryFunction<?> supplier) {
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

        void fillOne(long index, Access1D<?> values, long valueIndex);

        void fillOne(long index, N value);

        void fillOne(long index, NullaryFunction<?> supplier);

        default void fillRange(final long first, final long limit, final N value) {
            Structure1D.loopRange(first, limit, i -> this.fillOne(i, value));
        }

        default void fillRange(final long first, final long limit, final NullaryFunction<?> supplier) {
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
    interface Mixable<N extends Comparable<N>> extends Structure1D {

        /**
         * @return The new/mixed value
         */
        double mix(long index, BinaryFunction<N> mixer, double addend);

        N mix(long index, BinaryFunction<N> mixer, N addend);

    }

    interface Modifiable<N extends Comparable<N>> extends Structure1D {

        default void modifyAll(final UnaryFunction<N> modifier) {
            this.modifyRange(0L, this.count(), modifier);
        }

        default void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
            Structure1D.loopMatching(left, this, i -> this.modifyOne(i, function.first(left.get(i))));
        }

        default void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
            Structure1D.loopMatching(this, right, i -> this.modifyOne(i, function.second(right.get(i))));
        }

        void modifyOne(long index, UnaryFunction<N> modifier);

        default void modifyRange(final long first, final long limit, final UnaryFunction<N> modifier) {
            Structure1D.loopRange(first, limit, i -> this.modifyOne(i, modifier));
        }

    }

    /**
     * A utility interface to simplify declaring to implement "everything mutable".
     *
     * @author apete
     */
    interface ModifiableReceiver<N extends Comparable<N>> extends Modifiable<N>, Receiver<N> {

        void modifyAny(Transformation1D<N> modifier);

    }

    /**
     * Anything/everything that does not require interaction with already existing elements.
     *
     * @author apete
     */
    interface Receiver<N extends Comparable<N>> extends Mutate1D, Mutate1D.Fillable<N>, Consumer<Access1D<?>> {

        @Override
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

    /**
     * Copies the argument of the ComplexNumber elements to the destination.
     */
    static void copyComplexArgument(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        source.loopAll(i -> destination.set(i, source.get(i).getArgument()));
    }

    /**
     * Copies the imaginary part of the ComplexNumber elements to the destination.
     */
    static void copyComplexImaginary(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        source.loopAll(i -> destination.set(i, source.get(i).getImaginary()));
    }

    /**
     * Copies the modulus of the ComplexNumber elements to the destination.
     */
    static void copyComplexModulus(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        source.loopAll(i -> destination.set(i, source.get(i).getModulus()));
    }

    /**
     * Simultaneously copies the modulus and argument of the ComplexNumber elements to the destinations.
     */
    static void copyComplexModulusAndArgument(final Access1D<ComplexNumber> source, final Mutate1D modDest, final Mutate1D argDest) {
        source.loopAll(i -> {
            ComplexNumber cmplx = source.get(i);
            modDest.set(i, cmplx.getModulus());
            argDest.set(i, cmplx.getArgument());
        });
    }

    /**
     * Copies the real part of the ComplexNumber elements to the destination.
     */
    static void copyComplexReal(final Access1D<ComplexNumber> source, final Mutate1D destination) {
        source.loopAll(i -> destination.set(i, source.get(i).getReal()));
    }

    /**
     * Simultaneously copies the real and imaginary parts of the ComplexNumber elements to the destinations.
     */
    static void copyComplexRealAndImaginary(final Access1D<ComplexNumber> source, final Mutate1D realDest, final Mutate1D imagDest) {
        source.loopAll(i -> {
            ComplexNumber cmplx = source.get(i);
            realDest.set(i, cmplx.getReal());
            imagDest.set(i, cmplx.getImaginary());
        });
    }

    void add(long index, Comparable<?> addend);

    void add(long index, double addend);

    default void add(final long index, final float addend) {
        this.add(index, (double) addend);
    }

    /**
     * Reset this mutable structure to some standard (all zeros) initial state. It must still be usuable after
     * this call, and the structure/size/shape must not change.
     */
    default void reset() {
        this.loopAll(i -> this.set(i, PrimitiveMath.ZERO));
    }

    void set(long index, Comparable<?> value);

    void set(long index, double value);

    default void set(final long index, final float value) {
        this.set(index, (double) value);
    }

}
