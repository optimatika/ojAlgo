package org.ojalgo.matrix.store;

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;

public class BlockStore<N extends Number> extends FactoryStore<N> implements PhysicalStore<N> {

    private PhysicalStore<N> myBlock00 = null;
    private PhysicalStore<N> myBlock01 = null;
    private PhysicalStore<N> myBlock10 = null;
    private PhysicalStore<N> myBlock11 = null;

    BlockStore(PhysicalStore.Factory<N, ?> factory, int rowsCount, int columnsCount) {
        super(factory, rowsCount, columnsCount);
    }

    public double doubleValue(long row, long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public N get(long row, long col) {
        // TODO Auto-generated method stub
        return null;
    }

    public void fillByMultiplying(Access1D<N> left, Access1D<N> right) {
        // TODO Auto-generated method stub

    }

    public ElementsConsumer<N> regionByColumns(int... columns) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByLimits(int rowLimit, int columnLimit) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByOffsets(int rowOffset, int columnOffset) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByRows(int... rows) {
        // TODO Auto-generated method stub
        return null;
    }

    public ElementsConsumer<N> regionByTransposing() {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(long row, long col, double addend) {
        // TODO Auto-generated method stub

    }

    public void add(long row, long col, Number addend) {
        // TODO Auto-generated method stub

    }

    public void set(long row, long col, double value) {
        // TODO Auto-generated method stub

    }

    public void set(long row, long col, Number value) {
        // TODO Auto-generated method stub

    }

    public void fillOne(long row, long col, Access1D<?> values, long valueIndex) {
        // TODO Auto-generated method stub

    }

    public void fillOne(long row, long col, N value) {
        // TODO Auto-generated method stub

    }

    public void fillOne(long row, long col, NullaryFunction<N> supplier) {
        // TODO Auto-generated method stub

    }

    public void modifyOne(long row, long col, UnaryFunction<N> modifier) {
        // TODO Auto-generated method stub

    }

    public void modifyMatching(Access1D<N> left, BinaryFunction<N> function) {
        // TODO Auto-generated method stub

    }

    public void modifyMatching(BinaryFunction<N> function, Access1D<N> right) {
        // TODO Auto-generated method stub

    }

    public long indexOfLargestInColumn(long row, long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInDiagonal(long row, long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInRow(long row, long col) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long indexOfLargestInRange(long first, long limit) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void exchangeColumns(long colA, long colB) {
        // TODO Auto-generated method stub

    }

    public void exchangeRows(long rowA, long rowB) {
        // TODO Auto-generated method stub

    }

    public List<N> asList() {
        // TODO Auto-generated method stub
        return null;
    }

    public void caxpy(N scalarA, int columnX, int columnY, int firstRow) {
        // TODO Auto-generated method stub

    }

    public void maxpy(N scalarA, MatrixStore<N> matrixX) {
        // TODO Auto-generated method stub

    }

    public void raxpy(N scalarA, int rowX, int rowY, int firstColumn) {
        // TODO Auto-generated method stub

    }

    public void transformLeft(Householder<N> transformation, int firstColumn) {
        // TODO Auto-generated method stub

    }

    public void transformLeft(Rotation<N> transformation) {
        // TODO Auto-generated method stub

    }

    public void transformRight(Householder<N> transformation, int firstRow) {
        // TODO Auto-generated method stub

    }

    public void transformRight(Rotation<N> transformation) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addNonZerosTo(ElementsConsumer<N> consumer) {
        // TODO Auto-generated method stub

    }

}
