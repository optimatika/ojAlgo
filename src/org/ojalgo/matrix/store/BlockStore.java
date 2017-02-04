package org.ojalgo.matrix.store;

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;

/**
 * @deprecated Experimental code. Doesn't work and may never do so.
 * @author apete
 */
@Deprecated
final class BlockStore<N extends Number> extends FactoryStore<N> implements PhysicalStore<N> {

    private final PhysicalStore<N> myBlock00 = null;
    private final PhysicalStore<N> myBlock01 = null;
    private final PhysicalStore<N> myBlock10 = null;
    private final PhysicalStore<N> myBlock11 = null;

    BlockStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {
        super(factory, rowsCount, columnsCount);
    }

    public void add(final long row, final long col, final double addend) {
        // TODO Auto-generated method stub

    }

    public void add(final long row, final long col, final Number addend) {
        // TODO Auto-generated method stub

    }

    public List<N> asList() {
        // TODO Auto-generated method stub
        return null;
    }

    public void caxpy(final N scalarA, final int columnX, final int columnY, final int firstRow) {
        // TODO Auto-generated method stub

    }

    public double doubleValue(final long row, final long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void exchangeColumns(final long colA, final long colB) {
        // TODO Auto-generated method stub

    }

    public void exchangeRows(final long rowA, final long rowB) {
        // TODO Auto-generated method stub

    }

    public void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {
        // TODO Auto-generated method stub

    }

    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        // TODO Auto-generated method stub

    }

    public void fillOne(final long row, final long col, final N value) {
        // TODO Auto-generated method stub

    }

    public void fillOne(final long row, final long col, final NullaryFunction<N> supplier) {
        // TODO Auto-generated method stub

    }

    public N get(final long row, final long col) {
        // TODO Auto-generated method stub
        return null;
    }

    public long indexOfLargestInColumn(final long row, final long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInRange(final long first, final long limit) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInRow(final long row, final long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestOnDiagonal(final long first) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void modifyMatching(final Access1D<N> left, final BinaryFunction<N> function) {
        // TODO Auto-generated method stub

    }

    public void modifyMatching(final BinaryFunction<N> function, final Access1D<N> right) {
        // TODO Auto-generated method stub

    }

    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {
        // TODO Auto-generated method stub

    }

    public void raxpy(final N scalarA, final int rowX, final int rowY, final int firstColumn) {
        // TODO Auto-generated method stub

    }

    public ElementsConsumer<N> regionByColumns(final int... columns) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByLimits(final int rowLimit, final int columnLimit) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByOffsets(final int rowOffset, final int columnOffset) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByRows(final int... rows) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByTransposing() {
        // TODO Auto-generated method stub
        return null;
    }

    public void set(final long row, final long col, final double value) {
        // TODO Auto-generated method stub

    }

    public void set(final long row, final long col, final Number value) {
        // TODO Auto-generated method stub

    }

    public void transformLeft(final Householder<N> transformation, final int firstColumn) {
        // TODO Auto-generated method stub

    }

    public void transformLeft(final Rotation<N> transformation) {
        // TODO Auto-generated method stub

    }

    public void transformRight(final Householder<N> transformation, final int firstRow) {
        // TODO Auto-generated method stub

    }

    public void transformRight(final Rotation<N> transformation) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addNonZerosTo(final ElementsConsumer<N> consumer) {
        // TODO Auto-generated method stub

    }

}
