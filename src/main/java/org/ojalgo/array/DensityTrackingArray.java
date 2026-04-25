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
 * Backed by a plain {@code double[]} with an explicit index list for the currently nonzero positions.
 * This makes it possible to traverse only the nonzero entries when the array is sufficiently sparse,
 * while still supporting direct indexed access to all elements.
 * <p>
 * The nonzero index list is maintained incrementally when possible and rebuilt lazily when entries are
 * cleared.
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
     * Mutations to either this object or the supplied array are reflected in the other.
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
    private int myNonzeroCount;

    public DensityTrackingArray(final int dim) {

        super();

        values = new double[dim];
        myIndices = new int[dim];
        myNonzeroCount = 0;
        myIndexValid = true;
    }

    DensityTrackingArray(final double[] data) {

        super();

        values = data;
        myIndices = new int[data.length];
        myNonzeroCount = 0;
        myIndexValid = false;
    }

    @Override
    public void add(final int i, final double value) {
        if (value == ZERO) {
            return;
        }
        if (values[i] == ZERO) {
            this.putNonzero(i);
        }
        values[i] += value;
    }

    @Override
    public void add(final long index, final Comparable<?> addend) {
        this.add(Math.toIntExact(index), NumberDefinition.doubleValue(addend));
    }

    @Override
    public int countNonzeros() {

        if (!myIndexValid) {
            this.rebuildIndex();
        }
        return myNonzeroCount;

    }

    /**
     * Returns the current ratio of nonzero entries to total size.
     */
    @Override
    public double density() {
        if (!myIndexValid) {
            this.rebuildIndex();
        }
        return ((double) myNonzeroCount) / values.length;
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
            this.rebuildIndex();
        }
        return myIndices;
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

    @Override
    public void reset() {
        if (myIndexValid) {
            for (int k = 0; k < myNonzeroCount; k++) {
                values[myIndices[k]] = ZERO;
            }
        } else {
            Arrays.fill(values, ZERO);
        }
        myNonzeroCount = 0;
        myIndexValid = true;
    }

    @Override
    public void set(final int i, final double value) {
        if (value != ZERO && values[i] == ZERO) {
            this.putNonzero(i);
        } else if (value == ZERO && values[i] != ZERO) {
            myIndexValid = false;
        }
        values[i] = value;
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public double[] toRawCopy1D() {
        return Arrays.copyOf(values, values.length);
    }

    private void putNonzero(final int i) {
        if (myIndexValid) {
            myIndices[myNonzeroCount++] = i;
        }
    }

    private void rebuildIndex() {
        myNonzeroCount = 0;
        for (int i = 0, limit = values.length; i < limit; i++) {
            if (values[i] != ZERO) {
                myIndices[myNonzeroCount++] = i;
            }
        }
        myIndexValid = true;
    }
}
