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
import org.ojalgo.function.special.MissingMath;

/**
 * N-dimensional mutator methods
 *
 * @author apete
 */
public interface MutateAnyD extends StructureAnyD, Mutate1D {

    interface Fillable<N extends Comparable<N>> extends StructureAnyD, Mutate1D.Fillable<N> {

        void fillOne(long[] reference, N value);

        void fillOne(long[] reference, NullaryFunction<?> supplier);

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

        void add(long[] reference, Comparable<?> addend);

        void add(long[] reference, double addend);

        default void add(final long[] reference, final float addend) {
            this.add(reference, (double) addend);
        }

        void modifyOne(long[] reference, UnaryFunction<N> modifier);

        void modifySet(int dimension, long dimensionalIndex, UnaryFunction<N> modifier);

        void modifySet(long[] initial, int dimension, UnaryFunction<N> modifier);

    }

    /**
     * A utility interface to simplify declaring to implement "everything mutable".
     *
     * @author apete
     */
    interface ModifiableReceiver<N extends Comparable<N>> extends Modifiable<N>, Receiver<N> {

        void modifyAny(TransformationAnyD<N> modifier);

    }

    interface Receiver<N extends Comparable<N>> extends MutateAnyD, Fillable<N>, Consumer<AccessAnyD<?>> {

        @Override
        default void accept(final AccessAnyD<?> supplied) {
            if (this.isAcceptable(supplied)) {
                supplied.loopAll((final long[] ref) -> this.set(ref, supplied.get(ref)));
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

    void set(long[] reference, Comparable<?> value);

    void set(long[] reference, double value);

    default void set(final long[] reference, final float value) {
        this.set(reference, (double) value);
    }

}
