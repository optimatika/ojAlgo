/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.optimisation;

import java.util.HashMap;
import java.util.Map;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.multiary.MultiaryFunction.TwiceDifferentiable;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.RowView;

/**
 * Should be able hold data for any problem solvable by ojAlgo's built-in solvers.
 *
 * @author apete
 */
public final class OptimisationData {

    private static final Factory<Double, Primitive64Store> FACTORY = Primitive64Store.FACTORY;

    /**
     * Assumed constrained to be <= 0.0
     */
    private Map<String, MultiaryFunction.TwiceDifferentiable<Double>> myAdditionalConstraints;
    private MatrixStore<Double> myAE = null;
    private RowsSupplier<Double> myAI = null;
    private MatrixStore<Double> myBE = null;
    private MatrixStore<Double> myBI = null;
    private MultiaryFunction.TwiceDifferentiable<Double> myObjective;

    OptimisationData() {
        super();
    }

    public int countAdditionalConstraints() {
        return myAdditionalConstraints != null ? myAdditionalConstraints.size() : 0;
    }

    public int countEqualityConstraints() {
        return myAE != null ? myAE.getRowDim() : 0;
    }

    public int countInequalityConstraints() {
        return myAI != null ? myAI.getRowDim() : 0;
    }

    public int countVariables() {

        if (myAE != null) {
            return myAE.getColDim();
        }

        if (myAI != null) {
            return myAI.getColDim();
        }

        if (myObjective != null) {
            return myObjective.arity();
        }

        throw new ProgrammingError("Cannot deduce the number of variables!");
    }

    /**
     * Equality constraints body: [AE][X] == [BE]
     */
    public MatrixStore<Double> getAE() {
        return myAE;
    }

    /**
     * Inequality constraints body: [AI][X] &lt;= [BI]
     */
    public MatrixStore<Double> getAI() {
        return myAI.get();
    }

    public SparseArray<Double> getAI(final int row) {
        return myAI.getRow(row);
    }

    public RowsSupplier<Double> getAI(final int... rows) {
        return myAI.selectRows(rows);
    }

    /**
     * Equality constraints RHS: [AE][X] == [BE]
     */
    public MatrixStore<Double> getBE() {
        return myBE;
    }

    /**
     * Inequality constraints RHS: [AI][X] &lt;= [BI]
     */
    public MatrixStore<Double> getBI() {
        return myBI;
    }

    public double getBI(final int row) {
        return myBI.doubleValue(row);
    }

    public <T extends MultiaryFunction.TwiceDifferentiable<Double>> T getObjective() {
        return (T) myObjective;
    }

    public RowView<Double> getRowsAI() {
        return myAI.rows();
    }

    public boolean hasAdditionalConstraints() {
        return this.countAdditionalConstraints() > 0;
    }

    public boolean hasEqualityConstraints() {
        return this.countEqualityConstraints() > 0;
    }

    public boolean hasInequalityConstraints() {
        return this.countInequalityConstraints() > 0;
    }

    public boolean isObjectiveSet() {
        return myObjective != null;
    }

    public void reset() {
        myAE = null;
        myAI = null;
        myBE = null;
        myBI = null;
        myObjective = null;
    }

    public void setEqualities(final Access2D<Double> mtrxAE, final Access1D<Double> mtrxBE) {

        ProgrammingError.throwIfNull(mtrxAE, mtrxBE);
        ProgrammingError.throwIfNotEqualRowDimensions(mtrxAE, mtrxBE);

        if (mtrxAE instanceof MatrixStore) {
            myAE = (MatrixStore<Double>) mtrxAE;
        } else {
            myAE = Primitive64Store.FACTORY.makeWrapper(mtrxAE);
        }

        if (mtrxBE instanceof MatrixStore) {
            myBE = (MatrixStore<Double>) mtrxBE;
        } else {
            myBE = FACTORY.columns(mtrxBE);
        }
    }

    public void clearEqualities() {
        myAE = null;
        myBE = null;
    }

    public void setInequalities(final Access2D<Double> mtrxAI, final Access1D<Double> mtrxBI) {

        ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
        ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

        if (mtrxAI instanceof RowsSupplier) {

            myAI = (RowsSupplier<Double>) mtrxAI;

        } else {

            myAI = FACTORY.makeRowsSupplier(mtrxAI.getColDim());
            myAI.addRows(mtrxAI.getRowDim());

            if (mtrxAI instanceof SparseStore) {

                ((SparseStore<Double>) mtrxAI).nonzeros().forEach(nz -> myAI.getRow(Math.toIntExact(nz.row())).set(nz.column(), nz.doubleValue()));

            } else {

                double value;
                for (int i = 0; i < mtrxAI.countRows(); i++) {
                    SparseArray<Double> tmpRow = myAI.getRow(i);
                    for (int j = 0; j < mtrxAI.countColumns(); j++) {
                        value = mtrxAI.doubleValue(i, j);
                        if (value != PrimitiveMath.ZERO) {
                            tmpRow.set(j, value);
                        }
                    }
                }
            }
        }

        if (mtrxBI instanceof MatrixStore) {
            myBI = (MatrixStore<Double>) mtrxBI;
        } else {
            myBI = FACTORY.columns(mtrxBI);
        }
    }

    public void addInequalities(final MatrixStore<Double> mtrxAI, final MatrixStore<Double> mtrxBI) {

        ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
        ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

        if (myAI == null || myBI == null) {
            this.setInequalities(mtrxAI, mtrxBI);
        }

        int offset = myAI.getRowDim();

        myAI.addRows(mtrxAI.getRowDim());

        if (mtrxAI instanceof SparseStore) {

            ((SparseStore<Double>) mtrxAI).nonzeros().forEach(nz -> myAI.getRow(offset + Math.toIntExact(nz.row())).set(nz.column(), nz.doubleValue()));

        } else {

            double value;
            for (int i = 0; i < mtrxAI.getRowDim(); i++) {
                SparseArray<Double> tmpRow = myAI.getRow(offset + i);
                for (int j = 0; j < mtrxAI.getColDim(); j++) {
                    value = mtrxAI.doubleValue(i, j);
                    if (value != PrimitiveMath.ZERO) {
                        tmpRow.set(j, value);
                    }
                }
            }
        }

        myBI = myBI.logical().below(mtrxBI).get();
    }

    public void setObjective(final MultiaryFunction.TwiceDifferentiable<Double> objective) {

        ProgrammingError.throwIfNull(objective);

        myObjective = objective;
    }

    @Override
    public String toString() {

        StringBuilder retVal = new StringBuilder();

        retVal.append("[ variables=");
        retVal.append(this.countVariables());

        retVal.append(", equalities=");
        retVal.append(this.countEqualityConstraints());

        retVal.append(", inequalities=");
        retVal.append(this.countInequalityConstraints());

        retVal.append(" ]");

        return retVal.toString();
    }

    public void validate() {

        // Reset to trigger input validation

        if (!(myAE == null && myBE == null)) {
            this.setEqualities(myAE, myBE);
        }

        if (!(myAI == null && myBI == null)) {
            this.setInequalities(myAI, myBI);
        }

        this.setObjective(myObjective);

        // Check number of variables

        int nbVariables = this.countVariables();

        if (myAE != null && myAE.getColDim() != nbVariables) {
            throw new ProgrammingError("AE has the wrong number of columns!");
        }

        if (myBE != null && myBE.getColDim() != 1) {
            throw new ProgrammingError("BE must have precisely one column!");
        }

        if (myAI != null && myAI.getColDim() != nbVariables) {
            throw new ProgrammingError("AI has the wrong number of columns!");
        }

        if (myBI != null && myBI.getColDim() != 1) {
            throw new ProgrammingError("BI must have precisely one column!");
        }

        if (myObjective != null && myObjective.arity() != nbVariables) {
            throw new ProgrammingError("The objective function has the wrong arity!");
        }
    }

    void addConstraint(final String key, final TwiceDifferentiable<Double> value) {
        if (myAdditionalConstraints == null) {
            myAdditionalConstraints = new HashMap<>();
        }
        myAdditionalConstraints.put(key, value);
    }

    void newEqualities(final int nbEqualities, final int nbVariables) {

        MatrixStore<Double> mtrxAE = FACTORY.make(nbEqualities, nbVariables);
        MatrixStore<Double> mtrxBE = FACTORY.make(nbEqualities, 1);

        this.setEqualities(mtrxAE, mtrxBE);
    }

    void newInequalities(final int nbInequalities, final int nbVariables) {

        RowsSupplier<Double> mtrxAI = FACTORY.makeRowsSupplier(nbVariables);
        mtrxAI.addRows(nbInequalities);
        MatrixStore<Double> mtrxBI = FACTORY.make(nbInequalities, 1);

        this.setInequalities(mtrxAI, mtrxBI);
    }

}
