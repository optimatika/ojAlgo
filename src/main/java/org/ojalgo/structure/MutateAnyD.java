/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.function.special.MissingMath;

/**
 * N-dimensional mutator methods
 *
 * @author apete
 */
public interface MutateAnyD extends StructureAnyD, Mutate1D {

    interface Fillable<N extends Comparable<N>> extends MutateAnyD, Mutate1D.Fillable<N> {

        /**
         * @deprecated v52 Use {@link #set(long, Comparable)} instead.
         */
        @Override
        @Deprecated
        default void fillOne(final long index, final N value) {
            this.fillOne(StructureAnyD.reference(index, this.shape()), value);
        }

        /**
         * @deprecated v52 Use {@link #set(long, Comparable)} instead.
         */
        @Override
        @Deprecated
        default void fillOne(final long index, final NullaryFunction<?> supplier) {
            this.fillOne(StructureAnyD.reference(index, this.shape()), supplier);
        }

        /**
         * @deprecated v52 Use {@link #set(long, Comparable)} instead.
         */
        @Deprecated
        default void fillOne(final long[] reference, final Access1D<?> values, final long valueIndex) {
            this.fillOne(reference, (N) values.get(valueIndex));
        }

        /**
         * @deprecated v52 Use {@link #set(long[], Comparable)} instead.
         */
        @Deprecated
        default void fillOne(final long[] reference, final N value) {
            this.set(reference, value);
        }

        /**
         * @deprecated v52 Use {@link #set(long[], Comparable)} instead.
         */
        @Deprecated
        default void fillOne(final long[] reference, final NullaryFunction<?> supplier) {
            this.set(reference, supplier.get());
        }

        void fillSet(int dimension, long dimensionalIndex, N value);

        void fillSet(int dimension, long dimensionalIndex, NullaryFunction<?> supplier);

        void fillSet(long[] initial, int dimension, N value);

        void fillSet(long[] initial, int dimension, NullaryFunction<?> supplier);

    }

    interface Mixable<N extends Comparable<N>> extends StructureAnyD, Mutate1D.Mixable<N> {

        @Override
        default double mix(final long index, final BinaryFunction<N> mixer, final double addend) {
            return this.mix(StructureAnyD.reference(index, this.shape()), mixer, addend);
        }

        @Override
        default N mix(final long index, final BinaryFunction<N> mixer, final N addend) {
            return this.mix(StructureAnyD.reference(index, this.shape()), mixer, addend);
        }

        double mix(long[] reference, BinaryFunction<N> mixer, double addend);

        N mix(long[] reference, BinaryFunction<N> mixer, N addend);

    }

    interface Modifiable<N extends Comparable<N>> extends StructureAnyD, Mutate1D.Modifiable<N> {

        @Override
        default void add(final long index, final byte addend) {
            this.add(StructureAnyD.reference(index, this.shape()), addend);
        }

        @Override
        default void add(final long index, final Comparable<?> addend) {
            this.add(StructureAnyD.reference(index, this.shape()), addend);
        }

        @Override
        default void add(final long index, final double addend) {
            this.add(StructureAnyD.reference(index, this.shape()), addend);
        }

        @Override
        default void add(final long index, final float addend) {
            this.add(StructureAnyD.reference(index, this.shape()), addend);
        }

        @Override
        default void add(final long index, final int addend) {
            this.add(StructureAnyD.reference(index, this.shape()), addend);
        }

        @Override
        default void add(final long index, final long addend) {
            this.add(StructureAnyD.reference(index, this.shape()), addend);
        }

        @Override
        default void add(final long index, final short addend) {
            this.add(StructureAnyD.reference(index, this.shape()), addend);
        }

        default void add(final long[] reference, final byte addend) {
            this.add(reference, (short) addend);
        }

        void add(long[] reference, Comparable<?> addend);

        void add(long[] reference, double addend);

        default void add(final long[] reference, final float addend) {
            this.add(reference, (double) addend);
        }

        default void add(final long[] reference, final int addend) {
            this.add(reference, (long) addend);
        }

        default void add(final long[] reference, final long addend) {
            this.add(reference, (double) addend);
        }

        default void add(final long[] reference, final short addend) {
            this.add(reference, (int) addend);
        }

        void modifyOne(long[] reference, UnaryFunction<N> modifier);

        void modifySet(int dimension, long dimensionalIndex, UnaryFunction<N> modifier);

        void modifySet(long[] initial, int dimension, UnaryFunction<N> modifier);

    }

    /**
     * @see Mutate2D.ModifiableReceiver
     * @author apete
     */
    interface ModifiableReceiver<N extends Comparable<N>> extends Modifiable<N>, Receiver<N>, AccessAnyD<N> {

        void modifyAny(TransformationAnyD<N> modifier);

    }

    interface Receiver<N extends Comparable<N>> extends MutateAnyD, Fillable<N>, Consumer<AccessAnyD<?>> {

        @Override
        default void accept(final AccessAnyD<?> supplied) {
            if (this.isAcceptable(supplied)) {
                supplied.loopAllReferences(ref -> this.set(ref, supplied.get(ref)));
            } else {
                throw new ProgrammingError("Not acceptable!");
            }
        }

        default boolean isAcceptable(final StructureAnyD supplier) {

            boolean retVal = true;

            int tmpRank = MissingMath.max(this.shape().length, this.shape().length);

            for (int i = 0; i < tmpRank; i++) {
                retVal &= this.count(i) >= supplier.count(i);
            }

            return retVal;
        }

    }

    @Override
    default void set(final int index, final byte value) {
        this.set((long) index, value);
    }

    @Override
    default void set(final int index, final double value) {
        this.set((long) index, value);
    }

    @Override
    default void set(final int index, final float value) {
        this.set((long) index, value);
    }

    @Override
    default void set(final int index, final int value) {
        this.set((long) index, value);
    }

    @Override
    default void set(final int index, final long value) {
        this.set((long) index, value);
    }

    @Override
    default void set(final int index, final short value) {
        this.set((long) index, value);
    }

    @Override
    default void set(final long index, final byte value) {
        this.set(StructureAnyD.reference(index, this.shape()), value);
    }

    @Override
    default void set(final long index, final Comparable<?> value) {
        this.set(StructureAnyD.reference(index, this.shape()), value);
    }

    @Override
    default void set(final long index, final double value) {
        this.set(StructureAnyD.reference(index, this.shape()), value);
    }

    @Override
    default void set(final long index, final float value) {
        this.set(StructureAnyD.reference(index, this.shape()), value);
    }

    @Override
    default void set(final long index, final int value) {
        this.set(StructureAnyD.reference(index, this.shape()), value);
    }

    @Override
    default void set(final long index, final long value) {
        this.set(StructureAnyD.reference(index, this.shape()), value);
    }

    @Override
    default void set(final long index, final short value) {
        this.set(StructureAnyD.reference(index, this.shape()), value);
    }

    default void set(final long[] reference, final byte value) {
        this.set(reference, (short) value);
    }

    void set(long[] reference, Comparable<?> value);

    void set(long[] reference, double value);

    default void set(final long[] reference, final float value) {
        this.set(reference, (double) value);
    }

    default void set(final long[] reference, final int value) {
        this.set(reference, (long) value);
    }

    default void set(final long[] reference, final long value) {
        this.set(reference, (double) value);
    }

    default void set(final long[] reference, final short value) {
        this.set(reference, (int) value);
    }

}
