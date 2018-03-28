package org.ojalgo.matrix.geometry;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.transformation.TransformationMatrix;

public class Primitive32Matrix2 implements GeometryMatrix<Primitive32Matrix2>, TransformationMatrix<Double, Primitive32Vector2> {

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
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(m00);
        result = (prime * result) + Float.floatToIntBits(m01);
        result = (prime * result) + Float.floatToIntBits(m10);
        result = (prime * result) + Float.floatToIntBits(m11);
        return result;
    }

    public final void multiply(final Primitive32Vector2 right, final Primitive32Vector2 product) {

        final float right0 = right.v0, right1 = right.v1;

        product.v0 = (m00 * right0) + (m01 * right1);
        product.v1 = (m10 * right0) + (m11 * right1);
    }

    public final void negate(final Primitive32Matrix2 matrix) {

        m00 = -matrix.m00;
        m01 = -matrix.m01;

        m10 = -matrix.m10;
        m11 = -matrix.m11;
    }

    @Override
    public final String toString() {
        return Access2D.toString(this);
    }

    public final void transform(final Primitive32Vector2 transformable) {
        this.multiply(transformable, transformable);
    }

    public final void transpose(final Primitive32Matrix2 matrix) {

        m00 = matrix.m00;
        m01 = matrix.m10;

        m10 = matrix.m01;
        m11 = matrix.m11;
    }

}
