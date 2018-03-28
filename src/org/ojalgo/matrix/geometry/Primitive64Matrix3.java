package org.ojalgo.matrix.geometry;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.transformation.TransformationMatrix;

public class Primitive64Matrix3 implements GeometryMatrix<Primitive64Matrix3>, TransformationMatrix<Double, Primitive64Vector3> {

    /**
     * The matrix elements
     */
    public double m00, m10, m20, m01, m11, m21, m02, m12, m22;

    public Primitive64Matrix3() {
        super();
    }

    public Primitive64Matrix3(final double m00, final double m10, final double m20, final double m01, final double m11, final double m21, final double m02,
            final double m12, final double m22) {
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
        if (!(obj instanceof Primitive64Matrix3)) {
            return false;
        }
        final Primitive64Matrix3 other = (Primitive64Matrix3) obj;
        if (Double.doubleToLongBits(m00) != Double.doubleToLongBits(other.m00)) {
            return false;
        }
        if (Double.doubleToLongBits(m01) != Double.doubleToLongBits(other.m01)) {
            return false;
        }
        if (Double.doubleToLongBits(m02) != Double.doubleToLongBits(other.m02)) {
            return false;
        }
        if (Double.doubleToLongBits(m10) != Double.doubleToLongBits(other.m10)) {
            return false;
        }
        if (Double.doubleToLongBits(m11) != Double.doubleToLongBits(other.m11)) {
            return false;
        }
        if (Double.doubleToLongBits(m12) != Double.doubleToLongBits(other.m12)) {
            return false;
        }
        if (Double.doubleToLongBits(m20) != Double.doubleToLongBits(other.m20)) {
            return false;
        }
        if (Double.doubleToLongBits(m21) != Double.doubleToLongBits(other.m21)) {
            return false;
        }
        if (Double.doubleToLongBits(m22) != Double.doubleToLongBits(other.m22)) {
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
        temp = Double.doubleToLongBits(m02);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m10);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m11);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m12);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m20);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m21);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(m22);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public final void multiply(final Primitive64Vector2 right, final double right2, final Primitive64Vector2 product) {

        final double right0 = right.v0, right1 = right.v1;

        product.v0 = (m00 * right0) + (m01 * right1) + (m02 * right2);
        product.v1 = (m10 * right0) + (m11 * right1) + (m12 * right2);
    }

    public final void multiply(final Primitive64Vector3 right, final Primitive64Vector3 product) {

        final double right0 = right.v0, right1 = right.v1, right2 = right.v2;

        product.v0 = (m00 * right0) + (m01 * right1) + (m02 * right2);
        product.v1 = (m10 * right0) + (m11 * right1) + (m12 * right2);
        product.v2 = (m20 * right0) + (m21 * right1) + (m22 * right2);
    }

    public final void negate(final Primitive64Matrix3 matrix) {

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

    @Override
    public final String toString() {
        return Access2D.toString(this);
    }

    public final void transform(final Primitive64Vector2 transformable, final double v2) {
        this.multiply(transformable, v2, transformable);
    }

    public final void transform(final Primitive64Vector3 transformable) {
        this.multiply(transformable, transformable);
    }

    public final void transpose(final Primitive64Matrix3 matrix) {

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

}
