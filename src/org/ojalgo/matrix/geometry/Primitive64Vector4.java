package org.ojalgo.matrix.geometry;

public class Primitive64Vector4 implements GeometryVector {

    /**
     * The vector elements
     */
    public double v0, v1, v2, v3;

    public Primitive64Vector4() {
        super();
    }

    public Primitive64Vector4(final double v0, final double v1, final double v2, final double v3) {
        super();
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public long count() {
        return 4L;
    }

    @Override
    public boolean equals(final Object obj) {
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
    public int hashCode() {
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

}
