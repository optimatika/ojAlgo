package org.ojalgo.matrix.geometry;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.operation.MultiplyBoth;

public class Primitive32Vector3 extends GeometryVector {

    /**
     * The vector elements
     */
    public float v0, v1, v2;

    public Primitive32Vector3() {
        super(MultiplyBoth.getPrimitive(3L, 1L), 3L, 1L);
    }

    public Primitive32Vector3(final float v0, final float v1, final float v2) {
        this();
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public final void add(final int row, final double addend) {
        switch (row) {
        case 0:
            v0 += addend;
            break;
        case 1:
            v1 += addend;
            break;
        case 2:
            v2 += addend;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public final long count() {
        return 3L;
    }

    @Override
    public final double doubleValue(final int index) {
        switch (index) {
        case 0:
            return v0;
        case 1:
            return v1;
        case 2:
            return v2;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Primitive32Vector3)) {
            return false;
        }
        final Primitive32Vector3 other = (Primitive32Vector3) obj;
        if (Float.floatToIntBits(v0) != Float.floatToIntBits(other.v0)) {
            return false;
        }
        if (Float.floatToIntBits(v1) != Float.floatToIntBits(other.v1)) {
            return false;
        }
        if (Float.floatToIntBits(v2) != Float.floatToIntBits(other.v2)) {
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
        return result;
    }

    @Override
    public final void modifyOne(final int row, final UnaryFunction<Double> modifier) {
        switch (row) {
        case 0:
            v0 = (float) modifier.invoke(v0);
            break;
        case 1:
            v1 = (float) modifier.invoke(v1);
            break;
        case 2:
            v2 = (float) modifier.invoke(v2);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final void set(final int row, final double value) {
        switch (row) {
        case 0:
            v0 = (float) value;
            break;
        case 1:
            v1 = (float) value;
            break;
        case 2:
            v2 = (float) value;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
}
