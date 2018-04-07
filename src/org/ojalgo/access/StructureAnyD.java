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

import java.util.Arrays;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.aggregator.Aggregator;

/**
 * A (fixed size) any-dimensional data structure.
 *
 * @author apete
 */
public interface StructureAnyD extends Structure1D {

    public final class IntReference implements Comparable<IntReference> {

        public final int[] reference;

        public IntReference(final int... aReference) {

            super();

            reference = aReference;
        }

        @SuppressWarnings("unused")
        private IntReference() {
            this(-1);
        }

        public int compareTo(final IntReference ref) {

            int retVal = reference.length - ref.reference.length;

            int i = reference.length - 1;
            while ((retVal == 0) && (i >= 0)) {
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
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof IntReference)) {
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
            result = (prime * result) + Arrays.hashCode(reference);
            return result;
        }

        @Override
        public String toString() {
            return Arrays.toString(reference);
        }

    }

    public final class LongReference implements Comparable<LongReference> {

        public final long[] reference;

        public LongReference(final long... aReference) {

            super();

            reference = aReference;
        }

        @SuppressWarnings("unused")
        private LongReference() {
            this(-1L);
        }

        public int compareTo(final LongReference ref) {

            int retVal = Integer.compare(reference.length, ref.reference.length);

            int i = reference.length - 1;
            while ((retVal == 0) && (i >= 0)) {
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
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof LongReference)) {
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
            result = (prime * result) + Arrays.hashCode(reference);
            return result;
        }

        @Override
        public String toString() {
            return Arrays.toString(reference);
        }

    }

    public interface Reducible<R extends Structure1D> extends StructureAnyD {

        R reduce(int dimension, Aggregator aggregator);

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

    public static StructureAnyD.ReferenceMapper mapperOf(final StructureAnyD structure, final Structure1D.IndexMapper<Object>[] mappers) {
        return new StructureAnyD.ReferenceMapper(structure, mappers);
    }

    /**
     * @param structure An access structure
     * @return The size of an access with that structure
     */
    static int count(final int[] structure) {
        int retVal = 1;
        final int tmpLength = structure.length;
        for (int i = 0; i < tmpLength; i++) {
            retVal *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     */
    static int count(final int[] structure, final int dimension) {
        return structure.length > dimension ? structure[dimension] : 1;
    }

    /**
     * @param structure An access structure
     * @return The size of an access with that structure
     */
    static long count(final long[] structure) {
        long retVal = 1;
        final int tmpLength = structure.length;
        for (int i = 0; i < tmpLength; i++) {
            retVal *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index
     * @return The size of that dimension
     */
    static long count(final long[] structure, final int dimension) {
        return structure.length > dimension ? structure[dimension] : 1;
    }

    /**
     * @param structure An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    static int index(final int[] structure, final int[] reference) {
        int retVal = reference[0];
        int tmpFactor = structure[0];
        final int tmpLength = reference.length;
        for (int i = 1; i < tmpLength; i++) {
            retVal += tmpFactor * reference[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    static int index(final int[] structure, final long[] reference) {
        int retVal = (int) reference[0];
        int tmpFactor = structure[0];
        final int tmpLength = reference.length;
        for (int i = 1; i < tmpLength; i++) {
            retVal += tmpFactor * reference[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param reference An access element reference
     * @return The index of that element
     */
    static long index(final long[] structure, final long[] reference) {
        long retVal = reference[0];
        long tmpFactor = structure[0];
        final int tmpLength = Math.min(structure.length, reference.length);
        for (int i = 1; i < tmpLength; i++) {
            retVal += tmpFactor * reference[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    static void loopMatching(final StructureAnyD structureA, final StructureAnyD structureB, final ReferenceCallback callback) {
        final long[] tmpShape = structureA.shape();
        if (!Arrays.equals(tmpShape, structureB.shape())) {
            throw new ProgrammingError("The 2 structures must have the same shape!");
        }
        for (long i = 0L; i < structureA.count(); i++) {
            callback.call(StructureAnyD.reference(i, tmpShape));
        }
    }

    static long[] reference(final long index, final long[] structure) {

        final long[] retVal = new long[structure.length];

        long tmpPrev = 1L;
        long tmpNext = 1L;

        for (int s = 0; s < structure.length; s++) {
            tmpNext *= structure[s];
            retVal[s] = (index % tmpNext) / tmpPrev;
            tmpPrev = tmpNext;
        }

        return retVal;
    }

    static long[] shape(final StructureAnyD structure) {

        final long tmpSize = structure.count();

        long tmpTotal = structure.count(0);
        int tmpRank = 1;

        while (tmpTotal < tmpSize) {
            tmpTotal *= structure.count(tmpRank);
            tmpRank++;
        }

        final long[] retVal = new long[tmpRank];

        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = structure.count(i);
        }

        return retVal;
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index indication a direction
     * @return The step size (index change) in that direction
     */
    static int step(final int[] structure, final int dimension) {
        int retVal = 1;
        for (int i = 0; i < dimension; i++) {
            retVal *= StructureAnyD.count(structure, i);
        }
        return retVal;
    }

    /**
     * A more complex/general version of {@linkplain #step(int[], int)}.
     *
     * @param structure An access structure
     * @param increment A vector indication a direction (and size)
     * @return The step size (index change)
     */
    static int step(final int[] structure, final int[] increment) {
        int retVal = 0;
        int tmpFactor = 1;
        final int tmpLimit = increment.length;
        for (int i = 1; i < tmpLimit; i++) {
            retVal += tmpFactor * increment[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    /**
     * @param structure An access structure
     * @param dimension A dimension index indication a direction
     * @return The step size (index change) in that direction
     */
    static long step(final long[] structure, final int dimension) {
        long retVal = 1;
        for (int i = 0; i < dimension; i++) {
            retVal *= StructureAnyD.count(structure, i);
        }
        return retVal;
    }

    /**
     * A more complex/general version of {@linkplain #step(int[], int)}.
     *
     * @param structure An access structure
     * @param increment A vector indication a direction (and size)
     * @return The step size (index change)
     */
    static long step(final long[] structure, final long[] increment) {
        long retVal = 0;
        long tmpFactor = 1;
        final int tmpLimit = increment.length;
        for (int i = 1; i < tmpLimit; i++) {
            retVal += tmpFactor * increment[i];
            tmpFactor *= structure[i];
        }
        return retVal;
    }

    /**
     * count() == count(0) * count(1) * count(2) * count(3) * ...
     */
    default long count() {
        return StructureAnyD.count(this.shape());
    }

    long count(int dimension);

    default void loopAll(final ReferenceCallback callback) {
        final long[] tmpShape = this.shape();
        for (long i = 0L; i < this.count(); i++) {
            callback.call(StructureAnyD.reference(i, tmpShape));
        }
    }

    long[] shape();

}
