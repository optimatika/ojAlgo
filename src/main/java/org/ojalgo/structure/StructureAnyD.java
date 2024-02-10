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

import java.util.Arrays;
import java.util.function.Predicate;

import org.ojalgo.function.aggregator.Aggregator;

/**
 * A (fixed size) any-dimensional data structure.
 *
 * @author apete
 */
public interface StructureAnyD extends Structure1D {

    public final class IntReference implements Comparable<IntReference> {

        public static IntReference of(final int... aReference) {
            return new IntReference(aReference);
        }

        public final int[] reference;

        public IntReference(final int... aReference) {

            super();

            reference = aReference;
        }

        @SuppressWarnings("unused")
        private IntReference() {
            this(-1);
        }

        @Override
        public int compareTo(final IntReference ref) {

            int retVal = reference.length - ref.reference.length;

            int i = reference.length - 1;
            while (retVal == 0 && i >= 0) {
                retVal = reference[i] - ref.reference[i];
                i--;
            }

            return retVal;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof IntReference)) {
                return false;
            }
            final IntReference other = (IntReference) obj;
            if (!Arrays.equals(reference, other.reference)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            return prime * result + Arrays.hashCode(reference);
        }

        @Override
        public String toString() {
            return Arrays.toString(reference);
        }

    }

    interface Logical<S extends StructureAnyD, B extends Logical<S, B>> extends StructureAnyD {

    }

    public final class LongReference implements Comparable<LongReference> {

        public static LongReference of(final long... aReference) {
            return new LongReference(aReference);
        }

        public final long[] reference;

        public LongReference(final long... aReference) {

            super();

            reference = aReference;
        }

        @SuppressWarnings("unused")
        private LongReference() {
            this(-1L);
        }

        @Override
        public int compareTo(final LongReference ref) {

            int retVal = Integer.compare(reference.length, ref.reference.length);

            int i = reference.length - 1;
            while (retVal == 0 && i >= 0) {
                retVal = Long.compare(reference[i], ref.reference[i]);
                i--;
            }

            return retVal;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof LongReference)) {
                return false;
            }
            final LongReference other = (LongReference) obj;
            if (!Arrays.equals(reference, other.reference)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            return prime * result + Arrays.hashCode(reference);
        }

        @Override
        public String toString() {
            return Arrays.toString(reference);
        }

    }

    public interface ReducibleTo1D<R extends Structure1D> extends StructureAnyD {

        /**
         * @param dimension Which of the AnyD-dimensions should be mapped to the resulting 1D structure.
         * @param aggregator How to aggregate the values of the reduction
         * @return A 1D data structure with aggregated values
         */
        R reduce(int dimension, Aggregator aggregator);

    }

    public interface ReducibleTo2D<R extends Structure2D> extends StructureAnyD {

        /**
         * @param rowDimension Which of the AnyD-dimensions should be mapped to the rows of the resulting 2D
         *        structure.
         * @param columnDimension Which of the AnyD-dimensions should be mapped to the columns of the
         *        resulting 2D structure.
         * @param aggregator How to aggregate the values of the reduction
         * @return A 2D data structure with aggregated values
         */
        R reduce(int rowDimension, int columnDimension, Aggregator aggregator);

    }

    @FunctionalInterface
    public interface ReferenceCallback {

        /**
         * @param ref Element reference (indices)
         */
        void call(long[] ref);

    }

    class ReferenceMapper implements IndexMapper<Object[]> {

        private final IndexMapper<Object>[] myMappers;
        private final long[] myStructure;

        protected ReferenceMapper(final StructureAnyD structure, final IndexMapper<Object>[] mappers) {
            super();
            myMappers = mappers;
            myStructure = structure.shape();
        }

        public <T> long toIndex(final int dim, final T key) {
            return myMappers[dim].toIndex(key);
        }

        @Override
        public long toIndex(final Object[] keys) {

            final long[] ref = new long[keys.length];

            for (int i = 0; i < ref.length; i++) {
                ref[i] = myMappers[i].toIndex(keys[i]);
            }

            return StructureAnyD.index(myStructure, ref);
        }

        @SuppressWarnings("unchecked")
        public <T> T toKey(final int dim, final long index) {
            return (T) myMappers[dim].toKey(index);
        }

        @Override
        public Object[] toKey(final long index) {

            final long[] ref = StructureAnyD.reference(index, myStructure);

            final Object[] retVal = new Object[ref.length];

            for (int i = 0; i < ref.length; i++) {
                retVal[i] = myMappers[i].toKey(ref[i]);

            }
            return retVal;
        }

        @SuppressWarnings("unchecked")
        public <T extends Comparable<? super T>> T toKey(final long index, final int dim) {
            final long[] ref = StructureAnyD.reference(index, myStructure);
            return (T) myMappers[dim].toKey(ref[dim]);
        }

    }

    public interface Reshapable extends StructureAnyD {

        /**
         * If necessary increase the rank to the specified number (without changing the total number of
         * components)
         */
        StructureAnyD expand(int rank);

        /**
         * Flattens this to a 1D structure. This operation is largely redundant in ojAlgo as anything AnyD is
         * also/simultaneously 1D.
         */
        Structure1D flatten();

        /**
         * The same array viewed/accessed with a different shape
         */
        StructureAnyD reshape(long... shape);

        /**
         * Squeezing removes the dimensions or axes that have a length of one. (This does not change the total
         * number of components.)
         */
        StructureAnyD squeeze();

    }

    static long count(final int... shape) {
        long retVal = 1L;
        for (int i = 0, limit = shape.length; i < limit; i++) {
            retVal *= shape[i];
        }
        return retVal;
    }

    /**
     * @param shape An access structure
     * @return The size of an access with that structure
     */
    static long count(final long... shape) {
        long retVal = 1L;
        for (int i = 0, limit = shape.length; i < limit; i++) {
            retVal *= shape[i];
        }
        return retVal;
    }

    /**
     * @param shape An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     */
    static long count(final long[] shape, final int dimension) {
        return shape.length > dimension ? shape[dimension] : 1L;
    }

    /**
     * @see #index(long[], long[])
     */
    static long index(final int[] shape, final int[] reference) {
        long retVal = reference[0];
        long factor = shape[0];
        for (int i = 1, limit = Math.min(shape.length, reference.length); i < limit; i++) {
            retVal += factor * reference[i];
            factor *= shape[i];
        }
        return retVal;
    }

    /**
     * @see #index(long[], long[])
     */
    static long index(final int[] shape, final long[] reference) {
        long retVal = reference[0];
        long factor = shape[0];
        for (int i = 1, limit = Math.min(shape.length, reference.length); i < limit; i++) {
            retVal += factor * reference[i];
            factor *= shape[i];
        }
        return retVal;
    }

    /**
     * @see #index(long[], long[])
     */
    static long index(final long[] shape, final int[] reference) {
        long retVal = reference[0];
        long factor = shape[0];
        for (int i = 1, limit = Math.min(shape.length, reference.length); i < limit; i++) {
            retVal += factor * reference[i];
            factor *= shape[i];
        }
        return retVal;
    }

    /**
     * @param shape An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    static long index(final long[] shape, final long[] reference) {
        long retVal = reference[0];
        long factor = shape[0];
        for (int i = 1, limit = Math.min(shape.length, reference.length); i < limit; i++) {
            retVal += factor * reference[i];
            factor *= shape[i];
        }
        return retVal;
    }

    /**
     * @see #index(long[], long[])
     */
    static long index(final StructureAnyD structure, final int[] reference) {
        long retVal = reference[0];
        long factor = structure.count(0);
        for (int i = 1, limit = Math.min(structure.rank(), reference.length); i < limit; i++) {
            retVal += factor * reference[i];
            factor *= structure.count(i);
        }
        return retVal;
    }

    /**
     * @see #index(long[], long[])
     */
    static long index(final StructureAnyD structure, final long[] reference) {
        long retVal = reference[0];
        long factor = structure.count(0);
        for (int i = 1, limit = Math.min(structure.rank(), reference.length); i < limit; i++) {
            retVal += factor * reference[i];
            factor *= structure.count(i);
        }
        return retVal;
    }

    static StructureAnyD.ReferenceMapper mapperOf(final StructureAnyD structure, final Structure1D.IndexMapper<Object>[] mappers) {
        return new StructureAnyD.ReferenceMapper(structure, mappers);
    }

    static long[] reference(final long index, final long[] shape) {

        long[] retVal = new long[shape.length];

        StructureAnyD.reference(index, shape, retVal);

        return retVal;
    }

    /**
     * Based on the input index and structure/shape the reference array will derived.
     *
     * @param index Input index
     * @param shape Relevant structure/shape
     * @param reference Will be updated to the correct reference array given the index and structure
     */
    static void reference(final long index, final long[] shape, final long[] reference) {

        long tmpPrev = 1L;
        long tmpNext = 1L;

        for (int s = 0; s < shape.length; s++) {
            tmpNext *= shape[s];
            reference[s] = index % tmpNext / tmpPrev;
            tmpPrev = tmpNext;
        }
    }

    /**
     * @param shape An access structure
     * @return The size of an access with that structure
     */
    static int size(final int... shape) {
        int retVal = 1;
        for (int i = 0, limit = shape.length; i < limit; i++) {
            retVal *= shape[i];
        }
        return retVal;
    }

    /**
     * @param shape An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     */
    static int size(final int[] shape, final int dimension) {
        return shape.length > dimension ? shape[dimension] : 1;
    }

    /**
     * @param shape An access structure
     * @param dimension A dimension index indication a direction
     * @return The step size (index change) in that direction
     */
    static int step(final int[] shape, final int dimension) {
        int retVal = 1;
        for (int i = 0; i < dimension; i++) {
            retVal *= StructureAnyD.size(shape, i);
        }
        return retVal;
    }

    /**
     * A more complex/general version of {@linkplain #step(int[], int)}.
     *
     * @param shape An access structure
     * @param increment A vector indication a direction (and size)
     * @return The step size (index change)
     */
    static int step(final int[] shape, final int[] increment) {
        int retVal = 0;
        int tmpFactor = 1;
        final int tmpLimit = increment.length;
        for (int i = 1; i < tmpLimit; i++) {
            retVal += tmpFactor * increment[i];
            tmpFactor *= shape[i];
        }
        return retVal;
    }

    /**
     * How does the index change when stepping to the next dimensional unit (next row, next column. next
     * matrix/area, next cube...)
     *
     * @param shape An access structure
     * @param dimension Which reference index to increment
     * @return The step size (index change)
     */
    static long step(final long[] shape, final int dimension) {
        long retVal = 1;
        for (int i = 0; i < dimension; i++) {
            retVal *= StructureAnyD.count(shape, i);
        }
        return retVal;
    }

    /**
     * A more complex/general version of {@linkplain #step(int[], int)}.
     *
     * @param shape An access structure
     * @param increment A vector indication a direction (and size)
     * @return The step size (index change)
     */
    static long step(final long[] shape, final long[] increment) {

        long retVal = 0L;
        long factor = 1L;

        for (int i = 1, limit = increment.length; i < limit; i++) {
            retVal += factor * increment[i];
            factor *= shape[i];
        }

        return retVal;
    }

    private void loop(final int dim, final long[] reference, final Predicate<long[]> filter, final ReferenceCallback callback) {
        for (long i = 0L, limit = this.count(dim); i < limit; i++) {
            reference[dim] = i;
            if (dim == 0) {
                if (filter.test(reference)) {
                    callback.call(reference);
                }
            } else {
                this.loop(dim - 1, reference, filter, callback);
            }
        }
    }

    private void loop(final int dim, final long[] reference, final ReferenceCallback callback) {
        for (long i = 0L, limit = this.count(dim); i < limit; i++) {
            reference[dim] = i;
            if (dim == 0) {
                callback.call(reference);
            } else {
                this.loop(dim - 1, reference, callback);
            }
        }
    }

    /**
     * count() == count(0) * count(1) * count(2) * count(3) * ...
     */
    default long count(final int dimension) {
        return this.size(dimension);
    }

    /**
     * Will loop through this multidimensional data structure so that one index value of one dimension is
     * fixed. (Ex: Loop through all items with row index == 5.)
     *
     * @param dimension The dimension with a fixed/supplied index. (0==row, 1==column, 2=matrix/area...)
     * @param dimensionalIndex The index value that dimension is fixed to. (Which row, column or matrix/area)
     * @param callback A callback with parameters that define a sub-loop
     */
    default void loop(final int dimension, final long dimensionalIndex, final LoopCallback callback) {

        final long[] structure = this.shape();

        long innerCount = 1L;
        long dimenCount = 1L;
        long outerCount = 1L;
        for (int i = 0; i < structure.length; i++) {
            if (i < dimension) {
                innerCount *= structure[i];
            } else if (i > dimension) {
                outerCount *= structure[i];
            } else {
                dimenCount = structure[i];
            }
        }
        final long totalCount = innerCount * dimenCount * outerCount;

        if (innerCount == 1L) {
            callback.call(dimensionalIndex * innerCount, totalCount, dimenCount);
        } else {
            final long step = innerCount * dimenCount;
            for (long i = dimensionalIndex * innerCount; i < totalCount; i += step) {
                callback.call(i, innerCount + i, 1L);
            }
        }

    }

    default void loop(final long[] initial, final int dimension, final LoopCallback callback) {

        long[] structure = this.shape();

        final long remaining = StructureAnyD.count(structure, dimension) - initial[dimension];

        final long first = StructureAnyD.index(structure, initial);
        final long step = StructureAnyD.step(structure, dimension);
        final long limit = first + step * remaining;

        callback.call(first, limit, step);
    }

    default void loopAllReferences(final ReferenceCallback callback) {

        int rank = this.rank();

        long[] reference = new long[rank];

        this.loop(rank - 1, reference, callback);
    }

    default void loopReferences(final Predicate<long[]> filter, final ReferenceCallback callback) {

        int rank = this.rank();

        long[] reference = new long[rank];

        this.loop(rank - 1, reference, filter, callback);
    }

    /**
     * @return The number of dimensions (the number of indices used to reference one element)
     */
    default int rank() {

        long count = this.count();

        long total = this.count(0);
        int retVal = 1;

        while (total < count) {
            total *= this.count(retVal);
            retVal++;
        }

        return retVal;
    }

    default long[] shape() {

        int rank = this.rank();

        long[] retVal = new long[rank];

        for (int i = 0; i < rank; i++) {
            retVal[i] = this.count(i);
        }

        return retVal;
    }

    int size(final int dimension);

}
