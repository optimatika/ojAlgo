package org.ojalgo.matrix.geometry;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.operation.MultiplyBoth;

public class Primitive32Vector2 extends GeometryVector {

    /**
     * The vector elements
     */
    public float v0, v1;

    public Primitive32Vector2() {
        super(MultiplyBoth.getPrimitive(2L, 1L), 2L, 1L);
    }

    public Primitive32Vector2(final float v0, final float v1) {
        this();
        this.v0 = v0;
        this.v1 = v1;
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
        default:
            throw new IllegalArgumentException();
        }
    }

    public final long count() {
        return 2L;
    }

    @Override
    public final double doubleValue(final int index) {
        switch (index) {
        case 0:
            return v0;
        case 1:
            return v1;
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
        if (!(obj instanceof Primitive32Vector2)) {
            return false;
        }
        final Primitive32Vector2 other = (Primitive32Vector2) obj;
        if (Float.floatToIntBits(v0) != Float.floatToIntBits(other.v0)) {
            return false;
        }
        if (Float.floatToIntBits(v1) != Float.floatToIntBits(other.v1)) {
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
        default:
            throw new IllegalArgumentException();
        }
    }

}
