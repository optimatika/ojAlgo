package org.ojalgo.matrix.geometry;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.operation.MultiplyBoth;

public class Primitive64Vector3 extends GeometryVector {

    /**
     * The vector elements
     */
    public double v0, v1, v2;

    public Primitive64Vector3() {
        super(MultiplyBoth.getPrimitive(3L, 1L), 3L, 1L);
    }

    public Primitive64Vector3(final double v0, final double v1, final double v2) {
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
        if (!(obj instanceof Primitive64Vector3)) {
            return false;
        }
        final Primitive64Vector3 other = (Primitive64Vector3) obj;
        if (Double.doubleToLongBits(v0) != Double.doubleToLongBits(other.v0)) {
            return false;
        }
        if (Double.doubleToLongBits(v1) != Double.doubleToLongBits(other.v1)) {
            return false;
        }
        if (Double.doubleToLongBits(v2) != Double.doubleToLongBits(other.v2)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(v0);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(v1);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(v2);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public final void modifyOne(final int row, final UnaryFunction<Double> modifier) {
        switch (row) {
        case 0:
            v0 = modifier.invoke(v0);
            break;
        case 1:
            v1 = modifier.invoke(v1);
            break;
        case 2:
            v2 = modifier.invoke(v2);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final void set(final int row, final double value) {
        switch (row) {
        case 0:
            v0 = value;
            break;
        case 1:
            v1 = value;
            break;
        case 2:
            v2 = value;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

}
