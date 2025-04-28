package org.ojalgo.array;

import java.util.Arrays;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.structure.Access1D;

/**
 * A compressed fixed size copy obtained using {@link SparseArray#toCompressedR064()}.
 */
public final class CompressedArrayR064 implements Access1D<Double> {

    public final int[] indices;
    public final int length;
    public final double[] values;

    CompressedArrayR064(final int[] indices, final double[] values) {
        super();
        this.indices = indices;
        this.values = values;
        length = Math.min(indices.length, values.length);
    }

    @Override
    public double doubleValue(final int index) {

        int internalIndex = Arrays.binarySearch(indices, 0, length, index);

        if (internalIndex >= 0) {
            return values[internalIndex];
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    @Override
    public Double get(final long index) {
        return Double.valueOf(this.doubleValue(index));
    }

    @Override
    public int size() {
        return length;
    }

}
