package org.ojalgo.matrix.geometry;

public class Primitive64Vector2 implements GeometryVector {

    /**
     * The vector elements
     */
    public double v0, v1;

    public Primitive64Vector2() {
        super();
    }

    public Primitive64Vector2(final double v0, final double v1) {
        super();
        this.v0 = v0;
        this.v1 = v1;
    }

    public long count() {
        return 2L;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Primitive64Vector2)) {
            return false;
        }
        final Primitive64Vector2 other = (Primitive64Vector2) obj;
        if (Double.doubleToLongBits(v0) != Double.doubleToLongBits(other.v0)) {
            return false;
        }
        if (Double.doubleToLongBits(v1) != Double.doubleToLongBits(other.v1)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(v0);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(v1);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
