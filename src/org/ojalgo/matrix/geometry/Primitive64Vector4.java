package org.ojalgo.matrix.geometry;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.NullaryFunction;
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
