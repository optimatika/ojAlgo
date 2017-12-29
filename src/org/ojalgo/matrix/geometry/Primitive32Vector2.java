package org.ojalgo.matrix.geometry;

public class Primitive32Vector2 implements GeometryVector {

    /**
     * The vector elements
     */
    public float v0, v1;

    public Primitive32Vector2() {
        super();
    }

    public Primitive32Vector2(final float v0, final float v1) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(v0);
        result = (prime * result) + Float.floatToIntBits(v1);
        return result;
    }

}
