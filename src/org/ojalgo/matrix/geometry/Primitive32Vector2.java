package org.ojalgo.matrix.geometry;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.ElementsConsumer;
import org.ojalgo.matrix.store.operation.MultiplyBoth;

public class Primitive32Vector2 extends GeometryVector {

    /**
     * The vector elements
     */
    public float v0, v1;

    public Primitive32Vector2() {
        super(MultiplyBoth.getPrimitive(2L, 1L), 2L, 1L);
    }

    public Primitive32Vector2(final float v0, final float v1) {
        this();
        this.v0 = v0;
        this.v1 = v1;
    }

    public void add(final long row, final long col, final double addend) {
        // TODO Auto-generated method stub

    }

    public void add(final long row, final long col, final Number addend) {
        // TODO Auto-generated method stub

    }

    public final long count() {
        return 2L;
    }

    public long countColumns() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long countRows() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final boolean equals(final Object obj) {
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

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        // TODO Auto-generated method stub

    }

    public void fillOne(final long row, final long col, final Double value) {
        // TODO Auto-generated method stub

    }

    public void fillOne(final long row, final long col, final NullaryFunction<Double> supplier) {
        // TODO Auto-generated method stub

    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(v0);
        result = (prime * result) + Float.floatToIntBits(v1);
        return result;
    }

    @Override
    public void modifyMatching(final Access1D<Double> left, final BinaryFunction<Double> function) {
        // TODO Auto-generated method stub

    }

    @Override
    public void modifyMatching(final BinaryFunction<Double> function, final Access1D<Double> right) {
        // TODO Auto-generated method stub

    }

    public void modifyOne(final long row, final long col, final UnaryFunction<Double> modifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public ElementsConsumer<Double> regionByTransposing() {
        // TODO Auto-generated method stub
        return null;
    }

    public void set(final long row, final long col, final double value) {
        // TODO Auto-generated method stub

    }

    public void set(final long row, final long col, final Number value) {
        // TODO Auto-generated method stub

    }

}
