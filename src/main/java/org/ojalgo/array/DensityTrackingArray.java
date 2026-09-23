package org.ojalgo.array;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Arrays;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.NumberDefinition;

/**
 * A primitive 1D array that tracks its nonzero pattern alongside the stored values.
 * <p>
 * Backed by a plain {@code double[]} with an explicit index list for the currently nonzero positions. This
 * makes it possible to traverse only the nonzero entries when the array is sufficiently sparse, while still
 * supporting direct indexed access to all elements.
 * <p>
 * The index of the nonzero positions is maintained incrementally (for a wrapped array it is built with a scan
 * when first needed). It lists every nonzero value exactly once. It may also list positions whose values have
 * become zero (through cancellation or being set to zero), until the next {@link #reset()},
 * {@link #reindex()} or {@link #tighten(double)}. Accumulating with {@link #add(int, double)} never requires
 * a rescan of the values, not even when a sum cancels to exactly zero.
 * <p>
 * Performance critical code (sparse triangular solves, sparse matrix-vector products) may write to
 * {@link #values} and to the array returned by {@link #indices()} directly, and then declare the result with
 * {@link #setNonzeroCount(int)}. Code that writes only to {@link #values} calls {@link #reindex()}, or
 * {@link #invalidateIndex()} to have the index rebuilt when (if) it is needed.
 */
public class DensityTrackingArray extends Primitive1D implements Mutate1D.Modifiable<Double>, Structure1D.Sparse {

    private static final double SPARSE_THRESHOLD = TENTH;

    /**
     * Creates an array of the specified size with a single unit entry.
     */
    public static DensityTrackingArray unit(final int dim, final int index) {
        DensityTrackingArray retVal = new DensityTrackingArray(dim);
        retVal.set(index, ONE);
        return retVal;
    }

    /**
     * Wraps an existing array as the backing store.
     * <p>
     * Mutations to either this object or the supplied array are reflected in the other. After writing to the
     * array directly, call {@link #reindex()} or {@link #invalidateIndex()}.
     */
    public static DensityTrackingArray wrap(final double[] data) {
        return new DensityTrackingArray(data);
    }

    /**
     * The backing value array.
     */
    public final double[] values;
    private boolean myIndexValid;
    private final int[] myIndices;
    /**
     * Whether a position is in the index, used to avoid listing a position twice. Built lazily, only when
     * needed by {@link #add(int, double)} or {@link #set(int, double)}: when {@link #myListedValid} is false
     * no position is flagged.
     */
    private final boolean[] myListed;
    private boolean myListedValid;
    private int myNonzeroCount;

    public DensityTrackingArray(final int dim) {

        super();

        values = new double[dim];
        myIndices = new int[dim];
        myListed = new boolean[dim];
        myListedValid = true;
        myNonzeroCount = 0;
        myIndexValid = true;
    }

    DensityTrackingArray(final double[] data) {

        super();

        values = data;
        myIndices = new int[data.length];
        myListed = new boolean[data.length];
        myListedValid = false;
        myNonzeroCount = 0;
        myIndexValid = false;
    }

    @Override
    public void add(final int i, final double value) {
        if (value != ZERO) {
            values[i] += value;
            this.list(i);
        }
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        this.add(Math.toIntExact(index), NumberDefinition.doubleValue(addend));
    }

    /**
     * y = y + a [this], visiting only the listed positions.
     *
     * @param a The scale
     * @param y The dense vector to update
     */
    public void axpy(final double a, final double[] y) {

        for (int k = 0, limit = this.countNonzeros(); k < limit; k++) {
            int i = myIndices[k];
            y[i] += a * values[i];
        }
    }

    /**
     * The number of listed positions: every nonzero, and possibly positions whose values have become zero.
     */
    @Override
    public int countNonzeros() {

        if (!myIndexValid) {
            this.reindex();
        }
        return myNonzeroCount;

    }

    /**
     * The ratio of {@link #countNonzeros()} to the total size.
     */
    @Override
    public double density() {
        if (!myIndexValid) {
            this.reindex();
        }
        return ((double) myNonzeroCount) / values.length;
    }

    /**
     * The dot product with a dense vector, visiting only the listed positions.
     */
    public double dot(final double[] vector) {

        double retVal = ZERO;
        for (int k = 0, limit = this.countNonzeros(); k < limit; k++) {
            int i = myIndices[k];
            retVal += values[i] * vector[i];
        }
        return retVal;
    }

    @Override
    public double doubleValue(final int index) {
        return values[index];
    }

    /**
     * Returns the backing array of nonzero indices.
     * <p>
     * Only the first {@link #countNonzeros()} entries are defined.
     */
    public int[] indices() {
        if (!myIndexValid) {
            this.reindex();
        }
        return myIndices;
    }

    /**
     * Declare that {@link #values} have been written to directly. The index is rebuilt, with a scan of the
     * values, when it is next needed.
     */
    public void invalidateIndex() {
        this.unlistAll();
        myNonzeroCount = 0;
        myIndexValid = false;
    }

    /**
     * Returns {@code true} when the current density is below the internal sparse-threshold heuristic.
     */
    public boolean isSparse() {
        return this.density() < SPARSE_THRESHOLD;
    }

    @Override
    public void modifyOne(final long index, final UnaryFunction<Double> modifier) {
        this.set(index, modifier.applyAsDouble(this.doubleValue(index)));
    }

    /**
     * Rebuild the index by scanning all values. Needed after writing to {@link #values} directly, unless the
     * index is declared using {@link #setNonzeroCount(int)}.
     */
    public void reindex() {
        this.unlistAll();
        myNonzeroCount = 0;
        for (int i = 0, limit = values.length; i < limit; i++) {
            if (values[i] != ZERO) {
                myIndices[myNonzeroCount++] = i;
            }
        }
        myIndexValid = true;
    }

    @Override
    public void reset() {
        this.unlistAll();
        if (myIndexValid && myNonzeroCount < values.length / 2) {
            for (int k = 0; k < myNonzeroCount; k++) {
                values[myIndices[k]] = ZERO;
            }
        } else {
            Arrays.fill(values, ZERO);
        }
        myNonzeroCount = 0;
        myIndexValid = true;
        myListedValid = true;
    }

    @Override
    public void set(final int i, final double value) {
        values[i] = value;
        if (value != ZERO) {
            this.list(i);
        }
    }

    /**
     * Declare that the first {@code count} entries of the array returned by {@link #indices()} have been set
     * (by the caller) to the distinct positions of all the nonzero values. Intended for code that writes to
     * {@link #values} and the index array directly, starting from a {@link #reset()} array or extending the
     * current index (all previously listed positions remain listed).
     */
    public void setNonzeroCount(final int count) {
        if (myIndexValid && myListedValid && myNonzeroCount > 0) {
            for (int k = 0; k < count; k++) {
                myListed[myIndices[k]] = true;
            }
        } else {
            myListedValid = count == 0;
        }
        myNonzeroCount = count;
        myIndexValid = true;
    }

    @Override
    public int size() {
        return values.length;
    }

    /**
     * Copy the listed nonzeros to another array, resetting it first, and set its index. Listed positions
     * whose values have become zero are not copied.
     */
    public void supplyTo(final DensityTrackingArray receiver) {

        receiver.reset();

        int count = 0;

        for (int k = 0, limit = this.countNonzeros(); k < limit; k++) {
            int i = myIndices[k];
            double value = values[i];
            if (value != ZERO) {
                receiver.values[i] = value;
                receiver.myIndices[count++] = i;
            }
        }

        receiver.setNonzeroCount(count);
    }

    /**
     * Set the values with a magnitude not larger than the tolerance to zero, and remove them (and any zeros)
     * from the index. NaN values are kept.
     */
    public void tighten(final double tolerance) {
        if (!myIndexValid) {
            this.reindex();
        }
        int count = 0;
        for (int k = 0; k < myNonzeroCount; k++) {
            int i = myIndices[k];
            if (Math.abs(values[i]) <= tolerance) {
                values[i] = ZERO;
                if (myListedValid) {
                    myListed[i] = false;
                }
            } else {
                myIndices[count++] = i;
            }
        }
        myNonzeroCount = count;
    }

    @Override
    public double[] toRawCopy1D() {
        return Arrays.copyOf(values, values.length);
    }

    private void list(final int i) {
        if (myIndexValid) {
            if (!myListedValid) {
                for (int k = 0; k < myNonzeroCount; k++) {
                    myListed[myIndices[k]] = true;
                }
                myListedValid = true;
            }
            if (!myListed[i]) {
                myListed[i] = true;
                myIndices[myNonzeroCount++] = i;
            }
        }
    }

    /**
     * Clear all flags.
     */
    private void unlistAll() {
        if (myIndexValid && myListedValid) {
            if (myNonzeroCount < values.length / 2) {
                for (int k = 0; k < myNonzeroCount; k++) {
                    myListed[myIndices[k]] = false;
                }
            } else {
                Arrays.fill(myListed, false);
            }
        }
        myListedValid = false;
    }

}
