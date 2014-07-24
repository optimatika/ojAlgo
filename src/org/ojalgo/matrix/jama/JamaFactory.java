/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.jama;

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.Access2D.Builder;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.AggregatorCollection;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.scalar.Scalar.Factory;

/**
 * Implements both {@linkplain BasicMatrix.Factory} and {@linkplain PhysicalStore.Factory}, and creates
 * {@linkplain JamaMatrix} instances.
 *
 * @author apete
 */
public final class JamaFactory extends Object implements BasicMatrix.Factory<JamaMatrix>, PhysicalStore.Factory<Double, JamaMatrix> {

    JamaFactory() {
        super();
    }

    public AggregatorCollection<Double> aggregator() {
        return PrimitiveAggregator.getCollection();
    }

    public JamaMatrix columns(final Access1D<?>... source) {

        final int tmpRowDim = (int) source[0].count();
        final int tmpColDim = source.length;

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        Access1D<?> tmpColumn;
        for (int j = 0; j < tmpColDim; j++) {
            tmpColumn = source[j];
            for (int i = 0; i < tmpRowDim; i++) {
                retVal[i][j] = tmpColumn.doubleValue(i);
            }
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix columns(final double[]... source) {

        final int tmpRowDim = source[0].length;
        final int tmpColDim = source.length;

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        double[] tmpColumn;
        for (int j = 0; j < tmpColDim; j++) {
            tmpColumn = source[j];
            for (int i = 0; i < tmpRowDim; i++) {
                retVal[i][j] = tmpColumn[i];
            }
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix columns(final List<? extends Number>... source) {

        final int tmpRowDim = source[0].size();
        final int tmpColDim = source.length;

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        List<? extends Number> tmpColumn;
        for (int j = 0; j < tmpColDim; j++) {
            tmpColumn = source[j];
            for (int i = 0; i < tmpRowDim; i++) {
                retVal[i][j] = tmpColumn.get(i).doubleValue();
            }
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix columns(final Number[]... source) {

        final int tmpRowDim = source[0].length;
        final int tmpColDim = source.length;

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        Number[] tmpColumn;
        for (int j = 0; j < tmpColDim; j++) {
            tmpColumn = source[j];
            for (int i = 0; i < tmpRowDim; i++) {
                retVal[i][j] = tmpColumn[i].doubleValue();
            }
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix conjugate(final Access2D<?> source) {
        return this.transpose(source);
    }

    public JamaMatrix copy(final Access2D<?> source) {

        final int tmpRowDim = (int) source.countRows();
        final int tmpColDim = (int) source.countColumns();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                retVal[i][j] = source.doubleValue(i, j);
            }
        }

        return new JamaMatrix(retVal, tmpRowDim, tmpColDim);
    }

    public FunctionSet<Double> function() {
        return PrimitiveFunction.getSet();
    }

    /**
     * @deprecated Use {@link #aggregator()} instead
     */
    @Deprecated
    public AggregatorCollection<Double> getAggregatorCollection() {
        return this.aggregator();
    }

    public Access2D.Builder<JamaMatrix> getBuilder(final int count) {
        return this.getBuilder(count, 1);
    }

    public Access2D.Builder<JamaMatrix> getBuilder(final int rows, final int columns) {

        final JamaMatrix tmpDelegate = this.makeZero(rows, columns);

        return new Builder<JamaMatrix>() {

            public JamaMatrix build() {
                return tmpDelegate;
            }

            public long count() {
                return this.size();
            }

            public long countColumns() {
                return tmpDelegate.getColDim();
            }

            public long countRows() {
                return tmpDelegate.getRowDim();
            }

            public Access1D.Builder<JamaMatrix> fillAll(final Number aNmbr) {
                tmpDelegate.fillAll(aNmbr.doubleValue());
                return this;
            }

            public Builder<JamaMatrix> fillColumn(final long aRow, final long aCol, final Number aNmbr) {
                tmpDelegate.fillColumn(rows, columns, aNmbr.doubleValue());
                return this;
            }

            public Builder<JamaMatrix> fillDiagonal(final long aRow, final long aCol, final Number aNmbr) {
                tmpDelegate.fillDiagonal(rows, columns, aNmbr.doubleValue());
                return this;
            }

            public Builder<JamaMatrix> fillRow(final long aRow, final long aCol, final Number aNmbr) {
                tmpDelegate.fillRow(rows, columns, aNmbr.doubleValue());
                return this;
            }

            public int getColDim() {
                return tmpDelegate.getColDim();
            }

            public int getRowDim() {
                return tmpDelegate.getRowDim();
            }

            public Builder<JamaMatrix> set(final long index, final double aNmbr) {
                tmpDelegate.set(index, aNmbr);
                return this;
            }

            public Builder<JamaMatrix> set(final long aRow, final long aCol, final double aNmbr) {
                tmpDelegate.set((int) aRow, (int) aCol, aNmbr);
                return this;
            }

            public Builder<JamaMatrix> set(final long aRow, final long aCol, final Number aNmbr) {
                tmpDelegate.set((int) aRow, (int) aCol, aNmbr);
                return this;
            }

            public Builder<JamaMatrix> set(final long index, final Number aNmbr) {
                tmpDelegate.set(index, aNmbr);
                return this;
            }

            public int size() {
                return tmpDelegate.getRowDim() * tmpDelegate.getColDim();
            }

        };
    }

    /**
     * @deprecated Use {@link #function()} instead
     */
    @Deprecated
    public FunctionSet<Double> getFunctionSet() {
        return this.function();
    }

    /**
     * @deprecated v35 Use {@link #scalar()} instead.
     */
    @Deprecated
    public Double getNumber(final double value) {
        return this.scalar().cast(value);
    }

    /**
     * @deprecated v35 Use {@link #scalar()} instead.
     */
    @Deprecated
    public Double getNumber(final Number value) {
        return this.scalar().cast(value);
    }

    /**
     * @deprecated v35 Use {@link #scalar()} instead.
     */
    @Deprecated
    public Scalar<Double> getStaticOne() {
        return this.scalar().one();
    }

    /**
     * @deprecated v35 Use {@link #scalar()} instead.
     */
    @Deprecated
    public Scalar<Double> getStaticZero() {
        return this.scalar().zero();
    }

    public BasicArray<Double> makeArray(final int length) {
        return PrimitiveArray.make(length);
    }

    public JamaMatrix makeEye(final long rows, final long columns) {

        final JamaMatrix retVal = this.makeZero(rows, columns);

        retVal.fillDiagonal(0, 0, this.getStaticOne().getNumber());

        return retVal;
    }

    public Householder<Double> makeHouseholder(final int length) {
        return new Householder.Primitive(length);
    }

    public JamaMatrix makeRandom(final long rows, final long columns, final RandomNumber distribution) {

        final double[][] retVal = new double[(int) rows][(int) columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retVal[i][j] = distribution.doubleValue();
            }
        }

        return new JamaMatrix(retVal);
    }

    public Rotation<Double> makeRotation(final int low, final int high, final double cos, final double sin) {
        return new Rotation.Primitive(low, high, cos, sin);
    }

    public Rotation<Double> makeRotation(final int low, final int high, final Double cos, final Double sin) {
        return new Rotation.Primitive(low, high, cos, sin);
    }

    public JamaMatrix makeZero(final long rows, final long columns) {
        return new JamaMatrix(new double[(int) rows][(int) columns]);
    }

    public JamaMatrix rows(final Access1D<?>... source) {

        final int tmpRowDim = source.length;
        final int tmpColDim = (int) source[0].count();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        Access1D<?> tmpSource;
        double[] tmpDestination;
        for (int i = 0; i < tmpRowDim; i++) {
            tmpSource = source[i];
            tmpDestination = retVal[i];
            for (int j = 0; j < tmpColDim; j++) {
                tmpDestination[j] = tmpSource.doubleValue(j);
            }
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix rows(final double[]... source) {

        final int tmpRowDim = source.length;
        final int tmpColDim = source[0].length;

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        double[] tmpSource;
        double[] tmpDestination;
        for (int i = 0; i < tmpRowDim; i++) {
            tmpSource = source[i];
            tmpDestination = retVal[i];
            for (int j = 0; j < tmpColDim; j++) {
                tmpDestination[j] = tmpSource[j];
            }
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix rows(final List<? extends Number>... source) {

        final int tmpRowDim = source.length;
        final int tmpColDim = source[0].size();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        List<? extends Number> tmpSource;
        double[] tmpDestination;
        for (int i = 0; i < tmpRowDim; i++) {
            tmpSource = source[i];
            tmpDestination = retVal[i];
            for (int j = 0; j < tmpColDim; j++) {
                tmpDestination[j] = tmpSource.get(j).doubleValue();
            }
        }

        return new JamaMatrix(retVal);
    }

    public JamaMatrix rows(final Number[]... source) {

        final int tmpRowDim = source.length;
        final int tmpColDim = source[0].length;

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        Number[] tmpSource;
        double[] tmpDestination;
        for (int i = 0; i < tmpRowDim; i++) {
            tmpSource = source[i];
            tmpDestination = retVal[i];
            for (int j = 0; j < tmpColDim; j++) {
                tmpDestination[j] = tmpSource[j].doubleValue();
            }
        }

        return new JamaMatrix(retVal);
    }

    public Factory<Double> scalar() {
        return PrimitiveScalar.FACTORY;
    }

    /**
     * @deprecated v35 Use {@link #scalar()} instead.
     */
    @Deprecated
    public Scalar<Double> toScalar(final double value) {
        return this.scalar().convert(value);
    }

    /**
     * @deprecated v35 Use {@link #scalar()} instead.
     */
    @Deprecated
    public Scalar<Double> toScalar(final Number value) {
        return this.scalar().convert(value);
    }

    public JamaMatrix transpose(final Access2D<?> source) {

        final int tmpRowDim = (int) source.countColumns();
        final int tmpColDim = (int) source.countRows();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                retVal[i][j] = source.doubleValue(j, i);
            }
        }

        return new JamaMatrix(retVal);
    }

}
