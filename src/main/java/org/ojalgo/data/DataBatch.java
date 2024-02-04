/*
 * Copyright 1997-2024 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.data;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.Collection;

import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Mutate2D.Receiver;

/**
 * A reusable data batch. Build 2D data structures by adding a set of 1D structures treating then as rows. Use
 * it, {@link #reset()} and do it again.
 *
 * @author apete
 */
public final class DataBatch implements Access2D<Double>, Access2D.Collectable<Double, Mutate2D.Receiver<Double>> {

    /**
     * With the batch size/capacity specified. The returned instance will throw an exception if too many rows
     * are added to it.
     */
    public static DataBatch from(final Factory2D<? extends Mutate2D.ModifiableReceiver<Double>> factory, final int batchSize, final int dataNodes) {
        return new DataBatch(factory.make(batchSize, dataNodes));
    }

    private int myCursor = 0;
    private final Mutate2D.ModifiableReceiver<Double> myData;

    DataBatch(final Mutate2D.ModifiableReceiver<Double> data) {

        super();

        myData = data;
    }

    public void addRow(final Access1D<Double> row) {
        myData.fillRow(myCursor, row);
        myCursor++;
    }

    public void addRows(final Collection<? extends Access1D<Double>> rows) {
        rows.forEach(this::addRow);
    }

    public void addRowWithSingleUnit(final int unitIndex) {
        myData.fillRow(myCursor, ZERO);
        myData.set(myCursor, unitIndex, ONE);
        myCursor++;
    }

    /**
     * Assumes all rows are of equal length and will only check the first. Returns 0 if there are no rows.
     *
     * @see org.ojalgo.structure.Structure2D#countColumns()
     */
    @Override
    public long countColumns() {
        return myData.countColumns();
    }

    @Override
    public long countRows() {
        return myData.countRows();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return myData.doubleValue(row, col);
    }

    @Override
    public Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    public boolean isFull() {
        return this.remaining() == 0;
    }

    /**
     * Remaining capacity / batch entries / number of rows.
     */
    public int remaining() {
        return this.getRowDim() - myCursor;
    }

    public void reset() {
        myCursor = 0;
    }

    @Override
    public void supplyTo(final Receiver<Double> receiver) {
        receiver.fillMatching(myData);
    }

}
