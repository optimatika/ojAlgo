package org.ojalgo.matrix.geometry;

import org.ojalgo.matrix.MatrixUtils;

public class Primitive32Matrix3 implements GeometryMatrix<Primitive32Matrix3> {

    /**
     * The matrix elements
     */
    public float m00, m10, m20, m01, m11, m21, m02, m12, m22;

    public Primitive32Matrix3() {
        super();
    }

    public Primitive32Matrix3(final float m00, final float m10, final float m20, final float m01, final float m11, final float m21, final float m02,
            final float m12, final float m22) {
        super();
        this.m00 = m00;
        this.m10 = m10;
        this.m20 = m20;
        this.m01 = m01;
        this.m11 = m11;
        this.m21 = m21;
        this.m02 = m02;
        this.m12 = m12;
        this.m22 = m22;
    }

    public final long countColumns() {
        return 3L;
    }

    public final long countRows() {
        return 3L;
    }

    public final double doubleValue(final int row, final int col) {
        switch (col) {
        case 0:
            switch (row) {
            case 0:
                return (m00);
            case 1:
                return (m10);
            case 2:
                return (m20);
            default:
                break;
            }
        case 1:
            switch (row) {
            case 0:
                return (m01);
            case 1:
                return (m11);
            case 2:
                return (m21);
            default:
                break;
            }
        case 2:
            switch (row) {
            case 0:
                return (m02);
            case 1:
                return (m12);
            case 2:
                return (m22);
            default:
                break;
            }
        default:
            break;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Primitive32Matrix3)) {
            return false;
        }
        final Primitive32Matrix3 other = (Primitive32Matrix3) obj;
        if (Float.floatToIntBits(m00) != Float.floatToIntBits(other.m00)) {
            return false;
        }
        if (Float.floatToIntBits(m01) != Float.floatToIntBits(other.m01)) {
            return false;
        }
        if (Float.floatToIntBits(m02) != Float.floatToIntBits(other.m02)) {
            return false;
        }
        if (Float.floatToIntBits(m10) != Float.floatToIntBits(other.m10)) {
            return false;
        }
        if (Float.floatToIntBits(m11) != Float.floatToIntBits(other.m11)) {
            return false;
        }
        if (Float.floatToIntBits(m12) != Float.floatToIntBits(other.m12)) {
            return false;
        }
        if (Float.floatToIntBits(m20) != Float.floatToIntBits(other.m20)) {
            return false;
        }
        if (Float.floatToIntBits(m21) != Float.floatToIntBits(other.m21)) {
            return false;
        }
        if (Float.floatToIntBits(m22) != Float.floatToIntBits(other.m22)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(m00);
        result = (prime * result) + Float.floatToIntBits(m01);
        result = (prime * result) + Float.floatToIntBits(m02);
        result = (prime * result) + Float.floatToIntBits(m10);
        result = (prime * result) + Float.floatToIntBits(m11);
        result = (prime * result) + Float.floatToIntBits(m12);
        result = (prime * result) + Float.floatToIntBits(m20);
        result = (prime * result) + Float.floatToIntBits(m21);
        result = (prime * result) + Float.floatToIntBits(m22);
        return result;
    }

    public final void negate(final Primitive32Matrix3 matrix) {

        m00 = -matrix.m00;
        m01 = -matrix.m01;
        m02 = -matrix.m02;

        m10 = -matrix.m10;
        m11 = -matrix.m11;
        m12 = -matrix.m12;

        m20 = -matrix.m20;
        m21 = -matrix.m21;
        m22 = -matrix.m22;
    }

    public final void transpose(final Primitive32Matrix3 matrix) {

        m00 = matrix.m00;
        m01 = matrix.m10;
        m02 = matrix.m20;

        m10 = matrix.m01;
        m11 = matrix.m11;
        m12 = matrix.m21;

        m20 = matrix.m02;
        m21 = matrix.m12;
        m22 = matrix.m22;
    }

    @Override
    public final String toString() {
        return MatrixUtils.toString(this);
    }

}
