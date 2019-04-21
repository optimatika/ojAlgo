/*
 * Copyright 1997-2019 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.blas.DOT;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.MatrixStore.LogicalBuilder;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;

final class RawLDL extends RawDecomposition implements LDL<Double> {

    private Pivot myPivot;
    private boolean mySPD = false;

    RawLDL() {
        super();
    }

    public Double calculateDeterminant(final Access2D<?> matrix) {

        final double[][] data = this.reset(matrix, false);

        this.doDecompose(data, matrix, false);

        return this.getDeterminant();
    }

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        final double[][] data = this.reset(matrix, false);

        final RawStore store = this.getRawInPlaceStore();
        matrix.supplyTo(store);

        return this.doDecompose(data, store, false);
    }

    public MatrixStore<Double> getD() {
        return this.getRawInPlaceStore().logical().diagonal(false).get();
    }

    public Double getDeterminant() {

        final double[][] tmpData = this.getRawInPlaceData();

        double retVal = ONE;
        for (int ij = 0; ij < tmpData.length; ij++) {
            retVal *= tmpData[ij][ij];
        }
        return retVal;
    }

    public MatrixStore<Double> getInverse() {
        final int tmpRowDim = this.getRowDim();
        return this.doGetInverse(this.allocate(tmpRowDim, tmpRowDim));
    }

    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.doGetInverse(preallocated);
    }

    public MatrixStore<Double> getL() {
        final RawStore tmpRawInPlaceStore = this.getRawInPlaceStore();
        final LogicalBuilder<Double> tmpBuilder = tmpRawInPlaceStore.logical();
        final LogicalBuilder<Double> tmpTriangular = tmpBuilder.triangular(false, true);
        return tmpTriangular.get();
    }

    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    public int getRank() {
        // TODO Auto-generated method stub
        return 0;
    }

    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs) {
        final DecompositionStore<Double> tmpPreallocated = this.allocate(rhs.countRows(), rhs.countColumns());
        return this.getSolution(rhs, tmpPreallocated);
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {
        return this.doSolve(rhs, preallocated);
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        final double[][] data = this.reset(original, false);

        this.doDecompose(data, original, false);

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    public boolean isFullRank() {
        return this.getRank() == this.getMinDim();
    }

    public boolean isPivoted() {
        return myPivot.isModified();
    }

    public boolean isSPD() {
        return mySPD;
    }

    public PhysicalStore<Double> preallocate(final Structure2D template) {
        return this.allocate(template.countRows(), template.countRows());
    }

    public PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        return this.allocate(templateBody.countRows(), templateRHS.countColumns());
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs) throws RecoverableCondition {
        return this.solve(body, rhs, this.preallocate(body, rhs));
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        final double[][] data = this.reset(body, false);

        this.doDecompose(data, body, false);

        if (this.isSolvable()) {
            return this.doSolve(MatrixStore.PRIMITIVE.makeWrapper(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    /**
     * Copy while decomposing, but the source and destination could already be the same storage.
     */
    private boolean doDecompose(final double[][] destination, final Access2D<?> source, boolean pivoting) {

        int dim = this.getRowDim();
        mySPD = (this.getColDim() == dim);

        myPivot = new Pivot(dim);

        final double[] rowIJ = new double[dim];
        double[] rowI;

        // Main loop.
        for (int ij = 0; ij < dim; ij++) { // For each row/column, along the diagonal
            rowI = destination[ij];

            for (int j = 0; j < ij; j++) {
                rowIJ[j] = rowI[j] * destination[j][j];
            }

            if (pivoting) {
                int pivotRow = ij;
                // Find next pivot row
                for (int p = ij; p < dim; p++) {
                    if (!PrimitiveScalar.isSmall(ONE, source.doubleValue(p, p))) {
                        pivotRow = p;
                        break;
                    }
                }

                // Pivot?
                if (pivotRow != ij) {
                    this.exchangeHermitian(this.getRawInPlaceStore(), pivotRow, ij);
                    myPivot.change(pivotRow, ij);
                }
            }

            final double tmpD = rowI[ij] = source.doubleValue(ij, ij) - DOT.invoke(rowI, 0, rowIJ, 0, 0, ij);
            mySPD &= (tmpD > ZERO);

            for (int i = ij + 1; i < dim; i++) { // Update column below current row
                rowI = destination[i];

                rowI[ij] = (source.doubleValue(i, ij) - DOT.invoke(rowI, 0, rowIJ, 0, 0, ij)) / tmpD;
            }
        }

        return this.computed(true);
    }

    private MatrixStore<Double> doGetInverse(final PhysicalStore<Double> preallocated) {

        preallocated.fillAll(ZERO);
        preallocated.fillDiagonal(0L, 0L, ONE);

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, true, false, true);

        for (int i = 0; i < preallocated.countRows(); i++) {
            preallocated.modifyRow(i, 0, DIVIDE.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated;
    }

    private MatrixStore<Double> doSolve(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        final RawStore tmpBody = this.getRawInPlaceStore();

        preallocated.substituteForwards(tmpBody, true, false, false);

        for (int i = 0; i < preallocated.countRows(); i++) {
            preallocated.modifyRow(i, 0, DIVIDE.second(tmpBody.doubleValue(i, i)));
        }

        preallocated.substituteBackwards(tmpBody, true, true, false);

        return preallocated;
    }

    @Override
    protected boolean checkSolvability() {

        boolean retVal = this.getRowDim() == this.getColDim();

        double largest = this.getRawInPlaceStore().aggregateDiagonal(Aggregator.LARGEST);
        double smallest = this.getRawInPlaceStore().aggregateDiagonal(Aggregator.SMALLEST);

        return retVal && !PrimitiveScalar.isSmall(largest, smallest);
    }

    public boolean decomposeWithoutPivoting(Collectable<Double, ? super PhysicalStore<Double>> matrix) {
        // TODO Auto-generated method stub
        return false;
    }

    public void exchangeHermitian(RawStore matrix, final int indexA, final int indexB) {

        final int indexMin = Math.min(indexA, indexB);
        final int indexMax = Math.max(indexA, indexB);

        double tmpVal;

        for (int j = 0; j < indexMin; j++) {
            tmpVal = matrix.doubleValue(indexMin, j);
            matrix.set(indexMin, j, matrix.doubleValue(indexMax, j));
            matrix.set(indexMax, j, tmpVal);
        }

        tmpVal = matrix.doubleValue(indexMin, indexMin);
        matrix.set(indexMin, indexMin, matrix.doubleValue(indexMax, indexMax));
        matrix.set(indexMax, indexMax, tmpVal);

        for (int ij = indexMin + 1; ij < indexMax; ij++) {
            tmpVal = matrix.doubleValue(ij, indexMin);
            matrix.set(ij, indexMin, matrix.doubleValue(indexMax, ij));
            matrix.set(indexMax, ij, tmpVal);
        }

        for (int i = indexMax + 1; i < matrix.countRows(); i++) {
            tmpVal = matrix.doubleValue(i, indexMin);
            matrix.set(i, indexMin, matrix.doubleValue(i, indexMax));
            matrix.set(i, indexMax, tmpVal);
        }

    }

}
