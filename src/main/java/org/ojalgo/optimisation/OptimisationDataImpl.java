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
package org.ojalgo.optimisation;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.HashMap;
import java.util.Map;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.multiary.MultiaryFunction.TwiceDifferentiable;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.RowView;

/**
 * Should be able hold data for any problem solvable by ojAlgo's built-in solvers. This class is typically
 * wrapped by different solver specific builders. Those builders apply various additional context on how to
 * interpret this data. Are variables assumed positive or not? In the objective function linear or convex? Are
 * the RHS:s required to be positive or not? Is this a max or min problem?...
 *
 * @author apete
 * @deprecated Use {@link OptimisationData} or some other implementation of that interfce instead.
 */
@Deprecated
public final class OptimisationDataImpl implements OptimisationData<Double> {

    private static final Factory<Double, Primitive64Store> FACTORY = Primitive64Store.FACTORY;

    private static MatrixStore<Double> add(final RowsSupplier<Double> baseA, final MatrixStore<Double> baseB, final Access2D<?> addA, final Access1D<?> addB) {

        ProgrammingError.throwIfNull(addA, addB);
        ProgrammingError.throwIfNotEqualRowDimensions(addA, addB);

        int baseRowDim = baseA.getRowDim();
        int addRowDim = addA.getRowDim();
        int addColDim = addA.getColDim();

        baseA.addRows(addRowDim);

        if (addA instanceof SparseStore) {

            ((SparseStore<?>) addA).nonzeros().forEach(nz -> baseA.getRow(baseRowDim + Math.toIntExact(nz.row())).set(nz.column(), nz.doubleValue()));

        } else {

            double value;
            for (int i = 0; i < addRowDim; i++) {
                SparseArray<Double> tmpRow = baseA.getRow(baseRowDim + i);
                for (int j = 0; j < addColDim; j++) {
                    value = addA.doubleValue(i, j);
                    if (value != ZERO) {
                        tmpRow.set(j, value);
                    }
                }
            }
        }

        Primitive64Store retB = FACTORY.make(baseRowDim + addRowDim, 1);
        retB.fillMatching(baseB);
        retB.regionByOffsets(baseRowDim, 0).fillMatching(addB);

        return retB;
    }

    /**
     * Assumed constrained to be <= 0.0
     */
    private Map<String, MultiaryFunction.TwiceDifferentiable<Double>> myAdditionalConstraints = null;
    private RowsSupplier<Double> myAE = null;
    private RowsSupplier<Double> myAI = null;
    private MatrixStore<Double> myBE = null;
    private MatrixStore<Double> myBI = null;
    private Primitive64Store myLowerBounds = null;
    private MultiaryFunction.TwiceDifferentiable<Double> myObjective = null;
    private Primitive64Store myUpperBounds = null;

    OptimisationDataImpl() {
        super();
    }

    @Override
    public int countAdditionalConstraints() {
        return myAdditionalConstraints != null ? myAdditionalConstraints.size() : 0;
    }

    @Override
    public int countConstraints() {
        return this.countEqualityConstraints() + this.countInequalityConstraints() + this.countAdditionalConstraints();
    }

    @Override
    public int countEqualityConstraints() {
        return myAE != null ? myAE.getRowDim() : 0;
    }

    @Override
    public int countInequalityConstraints() {
        return myAI != null ? myAI.getRowDim() : 0;
    }

    @Override
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
    @Override
    public MatrixStore<Double> getAE() {
        if (myAE != null) {
            return myAE.get();
        } else {
            return FACTORY.makeZero(0, this.countVariables());
        }
    }

    @Override
    public SparseArray<Double> getAE(final int row) {
        return myAE.getRow(row);
    }

    @Override
    public RowsSupplier<Double> getAE(final int... rows) {
        return myAE.selectRows(rows);
    }

    /**
     * Inequality constraints body: [AI][X] <= [BI]
     */
    @Override
    public MatrixStore<Double> getAI() {
        if (myAI != null) {
            return myAI.get();
        } else {
            return FACTORY.makeZero(0, this.countVariables());
        }
    }

    @Override
    public SparseArray<Double> getAI(final int row) {
        return myAI.getRow(row);
    }

    @Override
    public RowsSupplier<Double> getAI(final int... rows) {
        return myAI.selectRows(rows);
    }

    /**
     * Equality constraints RHS: [AE][X] == [BE]
     */
    @Override
    public MatrixStore<Double> getBE() {
        if (myBE != null) {
            return myBE;
        } else {
            return FACTORY.makeZero(0, 1);
        }
    }

    @Override
    public double getBE(final int row) {
        return myBE.doubleValue(row);
    }

    /**
     * Inequality constraints RHS: [AI][X] <= [BI]
     */
    @Override
    public MatrixStore<Double> getBI() {
        if (myBI != null) {
            return myBI;
        } else {
            return FACTORY.makeZero(0, 1);
        }
    }

    @Override
    public double getBI(final int row) {
        return myBI.doubleValue(row);
    }

    @Override
    public MultiaryFunction.TwiceDifferentiable<Double> getObjective() {
        return myObjective;
    }

    @Override
    public <T extends MultiaryFunction.TwiceDifferentiable<Double>> T getObjective(final Class<T> type) {
        return (T) myObjective;
    }

    @Override
    public RowView<Double> getRowsAE() {
        return myAE.rows();
    }

    @Override
    public RowView<Double> getRowsAI() {
        return myAI.rows();
    }

    public void reset() {

        if (myAdditionalConstraints != null) {
            myAdditionalConstraints.clear();
        }

        myAdditionalConstraints = null;
        myAE = null;
        myAI = null;
        myBE = null;
        myBI = null;
        myLowerBounds = null;
        myObjective = null;
        myUpperBounds = null;
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

    void addConstraint(final String key, final TwiceDifferentiable<Double> value) {
        if (myAdditionalConstraints == null) {
            myAdditionalConstraints = new HashMap<>();
        }
        myAdditionalConstraints.put(key, value);
    }

    void addEqualities(final MatrixStore<?> mtrxAE, final MatrixStore<?> mtrxBE) {

        ProgrammingError.throwIfNull(mtrxAE, mtrxBE);
        ProgrammingError.throwIfNotEqualRowDimensions(mtrxAE, mtrxBE);

        if (myAE == null || myBE == null) {
            myAE = FACTORY.makeRowsSupplier(mtrxAE.getColDim());
            myBE = FACTORY.makeZero(0, 1);
        }

        myBE = OptimisationDataImpl.add(myAE, myBE, mtrxAE, mtrxBE);
    }

    void addInequalities(final MatrixStore<?> mtrxAI, final MatrixStore<?> mtrxBI) {

        ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
        ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

        if (myAI == null || myBI == null) {
            myAI = FACTORY.makeRowsSupplier(mtrxAI.getColDim());
            myBI = FACTORY.makeZero(0, 0);
        }

        myBI = OptimisationDataImpl.add(myAI, myBI, mtrxAI, mtrxBI);
    }

    Primitive64Store getLowerBounds() {
        return myLowerBounds;
    }

    Primitive64Store getLowerBounds(final double defaultValue) {
        if (myLowerBounds == null) {
            myLowerBounds = FACTORY.make(this.countVariables(), 1);
            myLowerBounds.fillAll(defaultValue);
        }
        return myLowerBounds;
    }

    Primitive64Store getUpperBounds() {
        return myUpperBounds;
    }

    Primitive64Store getUpperBounds(final double defaultValue) {
        if (myUpperBounds == null) {
            myUpperBounds = FACTORY.make(this.countVariables(), 1);
            myUpperBounds.fillAll(defaultValue);
        }
        return myUpperBounds;
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

    void setBounds(final Access1D<Double> lower, final Access1D<Double> upper) {

        ProgrammingError.throwIfNull(lower, upper);

        if (lower instanceof Primitive64Store) {
            myLowerBounds = (Primitive64Store) lower;
        } else {
            myLowerBounds = FACTORY.columns(lower);
        }

        if (upper instanceof Primitive64Store) {
            myUpperBounds = (Primitive64Store) upper;
        } else {
            myUpperBounds = FACTORY.columns(upper);
        }
    }

    void setEqualities(final Access2D<?> mtrxAE, final Access1D<?> mtrxBE) {

        ProgrammingError.throwIfNull(mtrxAE, mtrxBE);
        ProgrammingError.throwIfNotEqualRowDimensions(mtrxAE, mtrxBE);

        myAE = FACTORY.makeRowsSupplier(mtrxAE.getColDim());
        myBE = FACTORY.makeZero(0, 0);

        myBE = OptimisationDataImpl.add(myAE, myBE, mtrxAE, mtrxBE);
    }

    void setInequalities(final Access2D<?> mtrxAI, final Access1D<?> mtrxBI) {

        ProgrammingError.throwIfNull(mtrxAI, mtrxBI);
        ProgrammingError.throwIfNotEqualRowDimensions(mtrxAI, mtrxBI);

        myAI = FACTORY.makeRowsSupplier(mtrxAI.getColDim());
        myBI = FACTORY.makeZero(0, 0);

        myBI = OptimisationDataImpl.add(myAI, myBI, mtrxAI, mtrxBI);
    }

    void setObjective(final MultiaryFunction.TwiceDifferentiable<Double> objective) {

        ProgrammingError.throwIfNull(objective);

        myObjective = objective;
    }

    void validate() {

        ProgrammingError.throwIfNull(myObjective);

        if (((myAE != null) || (myBE != null))) {
            ProgrammingError.throwIfNull(myAE, myBE);
            ProgrammingError.throwIfNotEqualRowDimensions(myAE, myBE);
        }

        if (((myAI != null) || (myBI != null))) {
            ProgrammingError.throwIfNull(myAI, myBI);
            ProgrammingError.throwIfNotEqualRowDimensions(myAI, myBI);
        }

        // Check number of variables/columns

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

}
