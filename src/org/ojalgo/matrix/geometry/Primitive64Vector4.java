package org.ojalgo.matrix.geometry;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.operation.MultiplyBoth;

public class Primitive64Vector4 extends GeometryVector {

    /**
     * The vector elements
     */
    public double v0, v1, v2, v3;

    public Primitive64Vector4() {
        super(MultiplyBoth.getPrimitive(4L, 1L), 4L, 1L);
    }

    public Primitive64Vector4(final double v0, final double v1, final double v2, final double v3) {
        this();
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
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
        case 3:
            v3 += addend;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public final long count() {
        return 4L;
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
        case 3:
            return v3;
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
        if (!(obj instanceof Primitive64Vector4)) {
            return false;
        }
        final Primitive64Vector4 other = (Primitive64Vector4) obj;
        if (Double.doubleToLongBits(v0) != Double.doubleToLongBits(other.v0)) {
            return false;
        }
        if (Double.doubleToLongBits(v1) != Double.doubleToLongBits(other.v1)) {
            return false;
        }
        if (Double.doubleToLongBits(v2) != Double.doubleToLongBits(other.v2)) {
            return false;
        }
        if (Double.doubleToLongBits(v3) != Double.doubleToLongBits(other.v3)) {
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
        temp = Double.doubleToLongBits(v3);
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
        case 3:
            v3 = modifier.invoke(v3);
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
        case 3:
            v3 = value;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

}
