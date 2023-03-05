/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.optimisation.convex;

import org.ojalgo.array.SparseArray;
import org.ojalgo.function.multiary.MultiaryFunction.TwiceDifferentiable;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.optimisation.OptimisationData;
import org.ojalgo.structure.Access2D.RowView;

final class ConvexData<N extends Comparable<N>> implements OptimisationData<N> {

    @FunctionalInterface
    interface DataFactory<N extends Comparable<N>> {

        ConvexData<N> newInstance(int nbVars, int nbEqus, int nbIneq);

    }

    private final RowsSupplier<N> myAE;
    private final RowsSupplier<N> myAI;
    private final PhysicalStore<N> myBE;
    private final PhysicalStore<N> myBI;
    private final ConvexObjectiveFunction<N> myObjective;

    ConvexData(final PhysicalStore.Factory<N, ?> factory, final int nbVars, final int nbEqus, final int nbIneq) {

        super();

        myObjective = new ConvexObjectiveFunction<>(factory, nbVars);

        myAE = factory.makeRowsSupplier(nbVars);
        myAE.addRows(nbEqus);
        myBE = factory.make(nbEqus, 1);

        myAI = factory.makeRowsSupplier(nbVars);
        myAI.addRows(nbIneq);
        myBI = factory.make(nbIneq, 1);
    }

    public int countAdditionalConstraints() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int countConstraints() {
        return this.countEqualityConstraints() + this.countInequalityConstraints();
    }

    public int countEqualityConstraints() {
        return myAE != null ? myAE.getRowDim() : 0;
    }

    public int countInequalityConstraints() {
        return myAI != null ? myAI.getRowDim() : 0;
    }

    public int countVariables() {
        return myObjective.arity();
    }

    /**
     * Equality constraints body: [AE][X] == [BE]
     */
    public PhysicalStore<N> getAE() {
        return myAE.get();
    }

    public SparseArray<N> getAE(final int row) {
        return myAE.getRow(row);
    }

    public RowsSupplier<N> getAE(final int... rows) {
        return myAE.selectRows(rows);
    }

    /**
     * Inequality constraints body: [AI][X] <= [BI]
     */
    public PhysicalStore<N> getAI() {
        return myAI.get();
    }

    public SparseArray<N> getAI(final int row) {
        return myAI.getRow(row);
    }

    public RowsSupplier<N> getAI(final int... rows) {
        return myAI.selectRows(rows);
    }

    /**
     * Equality constraints RHS: [AE][X] == [BE]
     */
    public MatrixStore<N> getBE() {
        return myBE;
    }

    public double getBE(final int row) {
        return myBE.doubleValue(row);
    }

    /**
     * Inequality constraints RHS: [AI][X] <= [BI]
     */
    public MatrixStore<N> getBI() {
        return myBI;
    }

    public double getBI(final int row) {
        return myBI.doubleValue(row);
    }

    public ConvexObjectiveFunction<N> getObjective() {
        return myObjective;
    }

    public <T extends TwiceDifferentiable<N>> T getObjective(final Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    public RowView<N> getRowsAE() {
        return myAE.rows();
    }

    public RowView<N> getRowsAI() {
        return myAI.rows();
    }

    public void reset() {
        // TODO Auto-generated method stub
    }

    void addObjective(final int row, final Comparable<?> value) {
        myObjective.linear().add(row, 0, value);
    }

    void addObjective(final int row, final double value) {
        myObjective.linear().add(row, 0, value);
    }

    void addObjective(final int row, final int col, final Comparable<?> value) {
        myObjective.quadratic().add(row, col, value);
    }

    void addObjective(final int row, final int col, final double value) {
        myObjective.quadratic().add(row, col, value);
    }

    void setAE(final int row, final int col, final Comparable<?> value) {
        myAE.getRow(row).set(col, value);
    }

    void setAE(final int row, final int col, final double value) {
        myAE.getRow(row).set(col, value);
    }

    void setAI(final int row, final int col, final Comparable<?> value) {
        myAI.getRow(row).set(col, value);
    }

    void setAI(final int row, final int col, final double value) {
        myAI.getRow(row).set(col, value);
    }

    void setBE(final int row, final Comparable<?> value) {
        myBE.set(row, 0, value);
    }

    void setBE(final int row, final double value) {
        myBE.set(row, 0, value);
    }

    void setBI(final int row, final Comparable<?> value) {
        myBI.set(row, 0, value);
    }

    void setBI(final int row, final double value) {
        myBI.set(row, 0, value);
    }

    void setObjective(final int row, final Comparable<?> value) {
        myObjective.linear().set(row, 0, value);
    }

    void setObjective(final int row, final double value) {
        myObjective.linear().set(row, 0, value);
    }

    void setObjective(final int row, final int col, final Comparable<?> value) {
        myObjective.quadratic().set(row, col, value);
    }

    void setObjective(final int row, final int col, final double value) {
        myObjective.quadratic().set(row, col, value);
    }

}
