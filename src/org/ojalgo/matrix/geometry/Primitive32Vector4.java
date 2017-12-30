package org.ojalgo.matrix.geometry;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.operation.MultiplyBoth;

public class Primitive32Vector4 extends GeometryVector {

    /**
     * The vector elements
     */
    public float v0, v1, v2, v3;

    public Primitive32Vector4() {
        super(MultiplyBoth.getPrimitive(4L, 1L), 4L, 1L);
    }

    public final long count() {
        return 4L;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Primitive32Vector4)) {
            return false;
        }
        final Primitive32Vector4 other = (Primitive32Vector4) obj;
        if (Float.floatToIntBits(v0) != Float.floatToIntBits(other.v0)) {
            return false;
        }
        if (Float.floatToIntBits(v1) != Float.floatToIntBits(other.v1)) {
            return false;
        }
        if (Float.floatToIntBits(v2) != Float.floatToIntBits(other.v2)) {
            return false;
        }
        if (Float.floatToIntBits(v3) != Float.floatToIntBits(other.v3)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(v0);
        result = (prime * result) + Float.floatToIntBits(v1);
        result = (prime * result) + Float.floatToIntBits(v2);
        result = (prime * result) + Float.floatToIntBits(v3);
        return result;
    }

    public void add(final long row, final long col, final double addend) {
        // TODO Auto-generated method stub

    }

    public void add(final long row, final long col, final Number addend) {
        // TODO Auto-generated method stub

    }

    public void set(final long row, final long col, final double value) {
        // TODO Auto-generated method stub

    }

    public void set(final long row, final long col, final Number value) {
        // TODO Auto-generated method stub

    }

    public long countColumns() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long countRows() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        // TODO Auto-generated method stub

    }

    public void fillOne(final long row, final long col, final Double value) {
        // TODO Auto-generated method stub

    }

    public void fillOne(final long row, final long col, final NullaryFunction<Double> supplier) {
        // TODO Auto-generated method stub

    }

    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
        // TODO Auto-generated method stub

    }

}
