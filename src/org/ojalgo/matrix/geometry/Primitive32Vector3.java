package org.ojalgo.matrix.geometry;

public class Primitive32Vector3 implements GeometryVector {

    /**
     * The vector elements
     */
    public float v0, v1, v2;

    public Primitive32Vector3() {
        super();
    }

    public Primitive32Vector3(final float v0, final float v1, final float v2) {
        super();
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    public long count() {
        return 3L;
    }

    @Override
    public boolean equals(final Object obj) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(v0);
        result = (prime * result) + Float.floatToIntBits(v1);
        result = (prime * result) + Float.floatToIntBits(v2);
        return result;
    }
}
