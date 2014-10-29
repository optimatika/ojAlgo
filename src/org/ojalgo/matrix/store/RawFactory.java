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
package org.ojalgo.matrix.store;

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.AggregatorCollection;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar.Factory;

/**
 * Implements both {@linkplain BasicMatrix.Factory} and {@linkplain PhysicalStore.Factory}, and creates
 * {@linkplain RawStore} instances.
 *
 * @author apete
 */
final class RawFactory extends Object implements PhysicalStore.Factory<Double, RawStore> {

    RawFactory() {
        super();
    }

    public AggregatorCollection<Double> aggregator() {
        return PrimitiveAggregator.getCollection();
    }

    public RawStore columns(final Access1D<?>... source) {

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

        return new RawStore(retVal);
    }

    public RawStore columns(final double[]... source) {

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

        return new RawStore(retVal);
    }

    public RawStore columns(final List<? extends Number>... source) {

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

        return new RawStore(retVal);
    }

    public RawStore columns(final Number[]... source) {

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

        return new RawStore(retVal);
    }

    public RawStore conjugate(final Access2D<?> source) {
        return this.transpose(source);
    }

    public RawStore copy(final Access2D<?> source) {

        final int tmpRowDim = (int) source.countRows();
        final int tmpColDim = (int) source.countColumns();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                retVal[i][j] = source.doubleValue(i, j);
            }
        }

        return new RawStore(retVal, tmpRowDim, tmpColDim);
    }

    public FunctionSet<Double> function() {
        return PrimitiveFunction.getSet();
    }

    public BasicArray<Double> makeArray(final int length) {
        return PrimitiveArray.make(length);
    }

    public RawStore makeEye(final long rows, final long columns) {

        final RawStore retVal = this.makeZero(rows, columns);

        retVal.fillDiagonal(0, 0, this.scalar().one().getNumber());

        return retVal;
    }

    public Householder<Double> makeHouseholder(final int length) {
        return new Householder.Primitive(length);
    }

    public RawStore makeRandom(final long rows, final long columns, final RandomNumber distribution) {

        final double[][] retVal = new double[(int) rows][(int) columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retVal[i][j] = distribution.doubleValue();
            }
        }

        return new RawStore(retVal);
    }

    public Rotation<Double> makeRotation(final int low, final int high, final double cos, final double sin) {
        return new Rotation.Primitive(low, high, cos, sin);
    }

    public Rotation<Double> makeRotation(final int low, final int high, final Double cos, final Double sin) {
        return new Rotation.Primitive(low, high, cos, sin);
    }

    public RawStore makeZero(final long rows, final long columns) {
        return new RawStore(new double[(int) rows][(int) columns]);
    }

    public RawStore rows(final Access1D<?>... source) {

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

        return new RawStore(retVal);
    }

    public RawStore rows(final double[]... source) {

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

        return new RawStore(retVal);
    }

    public RawStore rows(final List<? extends Number>... source) {

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

        return new RawStore(retVal);
    }

    public RawStore rows(final Number[]... source) {

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

        return new RawStore(retVal);
    }

    public Factory<Double> scalar() {
        return PrimitiveScalar.FACTORY;
    }

    public RawStore transpose(final Access2D<?> source) {

        final int tmpRowDim = (int) source.countColumns();
        final int tmpColDim = (int) source.countRows();

        final double[][] retVal = new double[tmpRowDim][tmpColDim];

        for (int i = 0; i < tmpRowDim; i++) {
            for (int j = 0; j < tmpColDim; j++) {
                retVal[i][j] = source.doubleValue(j, i);
            }
        }

        return new RawStore(retVal);
    }

}
