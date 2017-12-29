package org.ojalgo.matrix.geometry;

public class Primitive32Vector4 implements GeometryVector {

    /**
     * The vector elements
     */
    public float v0, v1, v2, v3;

    public Primitive32Vector4() {
        super();
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(v0);
        result = (prime * result) + Float.floatToIntBits(v1);
        result = (prime * result) + Float.floatToIntBits(v2);
        result = (prime * result) + Float.floatToIntBits(v3);
        return result;
    }

}
