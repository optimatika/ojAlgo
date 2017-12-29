package org.ojalgo.matrix.geometry;

public class Primitive32Matrix2 implements GeometryMatrix<Primitive32Matrix2> {

    /**
     * The matrix elements
     */
    public float m00, m10, m01, m11;

    public Primitive32Matrix2() {
        super();
    }

    public Primitive32Matrix2(final float m00, final float m10, final float m01, final float m11) {
        super();
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
    }

    public long countColumns() {
        return 2L;
    }

    public long countRows() {
        return 2L;
    }

    public double doubleValue(final int row, final int col) {
        switch (col) {
        case 0:
            switch (row) {
            case 0:
                return (m00);
            case 1:
                return (m10);
            default:
                break;
            }
        case 1:
            switch (row) {
            case 0:
                return (m01);
            case 1:
                return (m11);
            default:
                break;
            }
        default:
            break;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Primitive32Matrix2)) {
            return false;
        }
        final Primitive32Matrix2 other = (Primitive32Matrix2) obj;
        if (Float.floatToIntBits(m00) != Float.floatToIntBits(other.m00)) {
            return false;
        }
        if (Float.floatToIntBits(m01) != Float.floatToIntBits(other.m01)) {
            return false;
        }
        if (Float.floatToIntBits(m10) != Float.floatToIntBits(other.m10)) {
            return false;
        }
        if (Float.floatToIntBits(m11) != Float.floatToIntBits(other.m11)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(m00);
        result = (prime * result) + Float.floatToIntBits(m01);
        result = (prime * result) + Float.floatToIntBits(m10);
        result = (prime * result) + Float.floatToIntBits(m11);
        return result;
    }

    public void negate(final Primitive32Matrix2 matrix) {

        m00 = -matrix.m00;
        m01 = -matrix.m01;

        m10 = -matrix.m10;
        m11 = -matrix.m11;
    }

    public void transpose(final Primitive32Matrix2 matrix) {

        m00 = matrix.m00;
        m01 = matrix.m10;

        m10 = matrix.m01;
        m11 = matrix.m11;
    }

}
