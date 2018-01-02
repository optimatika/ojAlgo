package org.ojalgo.matrix.geometry;

import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.transformation.TransformationMatrix;

public class Primitive64Matrix2 implements GeometryMatrix<Primitive64Matrix2>, TransformationMatrix<Double, Primitive64Vector2> {

    /**
     * The matrix elements
     */
    public double m00, m10, m01, m11;

    public Primitive64Matrix2() {
        super();
    }

    public final long countColumns() {
        return 2L;
    }

    public final long countRows() {
        return 2L;
    }

    public final double doubleValue(final int row, final int col) {
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
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Primitive64Matrix2)) {
            return false;
        }
        final Primitive64Matrix2 other = (Primitive64Matrix2) obj;
        if (Double.doubleToLongBits(m00) != Double.doubleToLongBits(other.m00)) {
            return false;
        }
        if (Double.doubleToLongBits(m01) != Double.doubleToLongBits(other.m01)) {
            return false;
        }
        if (Double.doubleToLongBits(m10) != Double.doubleToLongBits(other.m10)) {
            return false;
        }
        if (Double.doubleToLongBits(m11) != Double.doubleToLongBits(other.m11)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(m00);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m01);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m10);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m11);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public final void negate(final Primitive64Matrix2 matrix) {

        m00 = -matrix.m00;
        m01 = -matrix.m01;

        m10 = -matrix.m10;
        m11 = -matrix.m11;
    }

    @Override
    public final String toString() {
        return MatrixUtils.toString(this);
    }

    public void transform(final Primitive64Vector2 transformable) {

        final double tmp0 = transformable.v0, tmp1 = transformable.v1;

        transformable.v0 = (m00 * tmp0) + (m01 * tmp1);
        transformable.v1 = (m10 * tmp0) + (m11 * tmp1);
    }

    public final void transpose(final Primitive64Matrix2 matrix) {

        m00 = matrix.m00;
        m01 = matrix.m10;

        m10 = matrix.m01;
        m11 = matrix.m11;
    }

}
