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
package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.structure.Access2D;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro: 2015-03-29
 *
 * <pre>
 * # Run complete. Total time: 01:21:55
 *
 * Benchmark                            (dim)   Mode  Cnt        Score       Error  Units
 * TuneRawCholesky.testCopiedVersion        5  thrpt  200  7478341,834 ± 42564,388  ops/s
 * TuneRawCholesky.testModifiedVersion      5  thrpt  200  5911517,520 ± 11091,618  ops/s
 * TuneRawCholesky.testCopiedVersion       10  thrpt  200  1715248,993 ± 45872,591  ops/s
 * TuneRawCholesky.testModifiedVersion     10  thrpt  200  1593993,807 ±  6181,005  ops/s
 * TuneRawCholesky.testCopiedVersion       20  thrpt  200   434878,301 ±  1855,893  ops/s
 * TuneRawCholesky.testModifiedVersion     20  thrpt  200   384146,586 ±  2525,388  ops/s
 * TuneRawCholesky.testCopiedVersion       50  thrpt  200    38934,484 ±   227,318  ops/s
 * TuneRawCholesky.testModifiedVersion     50  thrpt  200    38389,101 ±    44,107  ops/s
 * TuneRawCholesky.testCopiedVersion      100  thrpt  200     5407,133 ±    24,550  ops/s
 * TuneRawCholesky.testModifiedVersion    100  thrpt  200     5259,123 ±     9,409  ops/s
 * TuneRawCholesky.testCopiedVersion     1000  thrpt  200        4,884 ±     0,003  ops/s
 * TuneRawCholesky.testModifiedVersion   1000  thrpt  200        4,772 ±     0,008  ops/s
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class TuneRawCholesky {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(TuneRawCholesky.class);
    }

    @Param({ "5", "10", "20", "50", "100", "1000" })
    public int dim;

    private boolean mySPD;

    R064Store matrix;
    double[][] rawInPlace;

    @Setup
    public void setup() {

        final int dim1 = dim;
        matrix = R064Store.FACTORY.makeSPD(dim1);

        rawInPlace = new double[dim][dim];
    }

    /**
     * Copy-and-paste from the actual compute() implementation - change as little as possible
     */
    @Benchmark
    public boolean testCopiedVersion() {

        this.reset();

        final double[][] tmpData = this.setRawInPlace(matrix, false);

        final int tmpDiagDim = this.getRowDim();
        mySPD = (this.getColDim() == tmpDiagDim);

        double[] tmpRowIJ;
        double[] tmpRowI;

        // Main loop.
        for (int ij = 0; ij < tmpDiagDim; ij++) { // For each row/column, along the diagonal
            tmpRowIJ = tmpData[ij];
            final int count = ij;

            final double tmpD = tmpRowIJ[ij] = Math.sqrt(Math.max(matrix.doubleValue(ij, ij) - DOT.invoke(tmpRowIJ, 0, tmpRowIJ, 0, 0, count), ZERO));
            mySPD &= (tmpD > ZERO);

            for (int i = ij + 1; i < tmpDiagDim; i++) { // Update column below current row
                tmpRowI = tmpData[i];
                final double[] array11 = tmpRowI;
                final double[] array21 = tmpRowIJ;
                final int count1 = ij;

                tmpRowI[ij] = (matrix.doubleValue(i, ij) - DOT.invoke(array11, 0, array21, 0, 0, count1)) / tmpD;
            }
        }

        return this.computed(true);
    }

    /**
     * 2 things are modified: 1) Call copy(...) after setRawInPlace 2) Use that copied/inPlace data rather
     * than the original/input during calculations.
     */
    @Benchmark
    public boolean testModifiedVersion() {

        this.reset();

        final double[][] tmpData = this.setRawInPlace(matrix, false);

        final int tmpDiagDim = this.getRowDim();
        mySPD = (this.getColDim() == tmpDiagDim);

        double[] tmpRowIJ;
        double[] tmpRowI;

        // Main loop.
        for (int ij = 0; ij < tmpDiagDim; ij++) { // For each row/column, along the diagonal
            tmpRowIJ = tmpData[ij];
            final int count = ij;

            final double tmpD = tmpRowIJ[ij] = Math.sqrt(Math.max(matrix.doubleValue(ij, ij) - DOT.invoke(tmpRowIJ, 0, tmpRowIJ, 0, 0, count), ZERO));
            mySPD &= (tmpD > ZERO);

            for (int i = ij + 1; i < tmpDiagDim; i++) { // Update column below current row
                tmpRowI = tmpData[i];
                final double[] array11 = tmpRowI;
                final double[] array21 = tmpRowIJ;
                final int count1 = ij;

                tmpRowI[ij] = (matrix.doubleValue(i, ij) - DOT.invoke(array11, 0, array21, 0, 0, count1)) / tmpD;
            }
        }

        return this.computed(true);
    }

    private boolean computed(final boolean b) {
        return b && mySPD;
    }

    private void copy(final Access2D<?> source, final int rows, final int columns, final double[][] destination) {
        for (int i = 0; i < rows; i++) {
            final double[] tmpRow = destination[i];
            for (int j = 0; j <= i; j++) {
                tmpRow[j] = source.doubleValue(i, j);
            }
        }
    }

    private int getColDim() {
        return dim;
    }

    private int getRowDim() {
        return dim;
    }

    private void reset() {

    }

    private double[][] setRawInPlace(final R064Store matrix2, final boolean transpose) {
        return rawInPlace;
    }

}
