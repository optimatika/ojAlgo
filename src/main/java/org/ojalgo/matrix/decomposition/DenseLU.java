/*
 * Copyright 1997-2026 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.MACHINE_SMALLEST;

import java.util.List;
import java.util.Optional;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.operation.GETRF;
import org.ojalgo.matrix.operation.GETRS;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.type.NumberDefinition;

abstract class DenseLU<N extends Comparable<N>, M extends PhysicalStore<N>> extends InPlaceDecomposition<N, M> implements LU<N> {

    static final class C128 extends Generic<ComplexNumber> {

        C128() {
            super(GenericStore.C128);
        }

    }

    /**
     * [A]=[P][L][U][Q]
     */
    static final class FactorL<N extends Comparable<N>> implements MatrixDecomposition.Factor<N> {

        private final PhysicalStore<N> myBody;

        FactorL(final PhysicalStore<N> body) {
            super();
            myBody = body;
        }

        @Override
        public void btran(final double[] arg) {
            myBody.substituteBackwards(true, true, arg);
        }

        @Override
        public void btran(final PhysicalStore<N> arg) {
            myBody.substituteBackwards(true, true, arg);
        }

        @Override
        public void ftran(final double[] arg) {
            myBody.substituteForwards(false, true, arg);
        }

        @Override
        public void ftran(final PhysicalStore<N> arg) {
            myBody.substituteForwards(false, true, arg);
        }

        @Override
        public MatrixStore<N> get() {
            return myBody.triangular(false, true).limits(this.getRowDim(), this.getColDim());
        }

        @Override
        public int getColDim() {
            return myBody.getMinDim();
        }

        @Override
        public int getRowDim() {
            return myBody.getRowDim();
        }

    }

    /**
     * [A]=[P][L][U][Q]
     */
    static final class FactorU<N extends Comparable<N>> implements MatrixDecomposition.Factor<N> {

        private final PhysicalStore<N> myBody;
        private final Pivot myColPivot;

        FactorU(final PhysicalStore<N> body, final Pivot colPivot) {
            super();
            myBody = body;
            myColPivot = colPivot;
        }

        @Override
        public void btran(final double[] arg) {
            myBody.substituteForwards(true, false, arg);
        }

        @Override
        public void btran(final PhysicalStore<N> arg) {
            myBody.substituteForwards(true, false, arg);
        }

        @Override
        public void ftran(final double[] arg) {
            myBody.substituteBackwards(false, false, arg);
        }

        @Override
        public void ftran(final PhysicalStore<N> arg) {
            myBody.substituteBackwards(false, false, arg);
        }

        @Override
        public MatrixStore<N> get() {
            MatrixStore<N> retVal = myBody.triangular(true, false).limits(this.getRowDim(), this.getColDim());
            if (myColPivot != null && myColPivot.isModified()) {
                retVal = retVal.columns(myColPivot.reverseOrder());
            }
            return retVal;
        }

        @Override
        public int getColDim() {
            return myBody.getColDim();
        }

        @Override
        public int getRowDim() {
            return myBody.getMinDim();
        }

    }

    /**
     * The factorisation is done by {@link GETRF}. Square systems are solved, and inverted, by {@link GETRS}.
     */
    abstract static class Generic<N extends Scalar<N>> extends DenseLU<N, GenericStore<N>> {

        Generic(final GenericStore.Factory<N> factory) {
            super(factory);
        }

        @Override
        public int countSignificant(final double threshold) {

            GenericStore<N> internal = this.getInPlace();

            int significant = 0;
            for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
                if (internal.get(ij, ij).norm() > threshold) {
                    significant++;
                }
            }

            return significant;
        }

        @Override
        void factorise(final GenericStore<N> inPlace, final int[] ipiv) {
            int nbRows = this.getRowDim();
            GETRF.invoke(nbRows, this.getColDim(), inPlace.data, 0, Math.max(1, nbRows), ipiv, 0, this.scalar());
        }

        @Override
        void factoriseWithoutPivoting(final GenericStore<N> inPlace) {
            int nbRows = this.getRowDim();
            GETRF.invokeWithoutPivoting(nbRows, this.getColDim(), inPlace.data, 0, Math.max(1, nbRows), this.scalar());
        }

        @Override
        void substitute(final boolean transpose, final PhysicalStore<N> arg) {

            int dim = this.getRowDim();

            if (this.isSquare() && arg instanceof GenericStore && arg.getRowDim() == dim) {
                GenericStore<N> rhs = (GenericStore<N>) arg;
                GETRS.invoke(transpose, dim, rhs.getColDim(), this.getInPlace().data, 0, Math.max(1, dim), this.getInterchanges(), 0, rhs.data, 0,
                        Math.max(1, dim), this.scalar());
            } else {
                super.substitute(transpose, arg);
            }
        }

        @Override
        void substituteColumns(final PhysicalStore<N> rhs) {

            int dim = this.getRowDim();

            if (this.isSquare() && rhs instanceof GenericStore && rhs.getRowDim() == dim) {
                GenericStore<N> columns = (GenericStore<N>) rhs;
                GETRS.invoke(false, dim, columns.getColDim(), this.getInPlace().data, 0, Math.max(1, dim), this.getInterchanges(), 0, columns.data, 0,
                        Math.max(1, dim), this.scalar());
            } else {
                super.substituteColumns(rhs);
            }
        }

        @Override
        void substituteIdentity(final PhysicalStore<N> preallocated) {

            int dim = this.getRowDim();

            if (this.isSquare() && preallocated instanceof GenericStore && preallocated.getRowDim() == dim && preallocated.getColDim() == dim) {
                GenericStore<N> inverse = (GenericStore<N>) preallocated;
                GETRS.invokeIdentity(dim, this.getInPlace().data, 0, Math.max(1, dim), this.getInterchanges(), 0, inverse.data, 0, Math.max(1, dim),
                        this.scalar());
            } else {
                super.substituteIdentity(preallocated);
            }
        }

        @Override
        boolean update(final Pivot rowOrder, final Pivot colOrder, final int columnIndex,
                final Access1D.Collectable<N, ? super TransformableRegion<N>> column) {
            return FletcherMatthews.update(rowOrder, this.getInPlace(), colOrder, columnIndex, column, this.getWorkerColumn(this.getRowDim()), this.scalar());
        }

    }

    static final class H256 extends Generic<Quaternion> {

        H256() {
            super(GenericStore.H256);
        }

    }

    static final class Q128 extends Generic<RationalNumber> {

        Q128() {
            super(GenericStore.Q128);
        }

    }

    /**
     * The factorisation is done by {@link GETRF}. Square systems are solved, and inverted, by {@link GETRS}.
     */
    static final class R064 extends DenseLU<Double, R064Store> {

        R064() {
            super(R064Store.FACTORY);
        }

        @Override
        public int countSignificant(final double threshold) {

            R064Store internal = this.getInPlace();

            int significant = 0;
            for (int ij = 0, limit = this.getMinDim(); ij < limit; ij++) {
                if (Math.abs(internal.doubleValue(ij, ij)) > threshold) {
                    significant++;
                }
            }

            return significant;
        }

        @Override
        void factorise(final R064Store inPlace, final int[] ipiv) {
            int nbRows = this.getRowDim();
            GETRF.invoke(nbRows, this.getColDim(), inPlace.data, 0, Math.max(1, nbRows), ipiv, 0);
        }

        @Override
        void factoriseWithoutPivoting(final R064Store inPlace) {
            int nbRows = this.getRowDim();
            GETRF.invokeWithoutPivoting(nbRows, this.getColDim(), inPlace.data, 0, Math.max(1, nbRows));
        }

        @Override
        void substitute(final boolean transpose, final double[] arg) {

            int dim = this.getRowDim();

            if (this.isSquare() && arg.length == dim) {
                GETRS.invoke(transpose, dim, 1, this.getInPlace().data, 0, Math.max(1, dim), this.getInterchanges(), 0, arg, 0, Math.max(1, dim));
            } else {
                super.substitute(transpose, arg);
            }
        }

        @Override
        void substitute(final boolean transpose, final PhysicalStore<Double> arg) {

            int dim = this.getRowDim();

            if (this.isSquare() && arg instanceof R064Store && arg.getRowDim() == dim) {
                R064Store rhs = (R064Store) arg;
                GETRS.invoke(transpose, dim, rhs.getColDim(), this.getInPlace().data, 0, Math.max(1, dim), this.getInterchanges(), 0, rhs.data, 0,
                        Math.max(1, dim));
            } else {
                super.substitute(transpose, arg);
            }
        }

        @Override
        void substituteColumns(final PhysicalStore<Double> rhs) {

            int dim = this.getRowDim();

            if (this.isSquare() && rhs instanceof R064Store && rhs.getRowDim() == dim) {
                R064Store columns = (R064Store) rhs;
                GETRS.invoke(false, dim, columns.getColDim(), this.getInPlace().data, 0, Math.max(1, dim), this.getInterchanges(), 0, columns.data, 0,
                        Math.max(1, dim));
            } else {
                super.substituteColumns(rhs);
            }
        }

        @Override
        void substituteIdentity(final PhysicalStore<Double> preallocated) {

            int dim = this.getRowDim();

            if (this.isSquare() && preallocated instanceof R064Store && preallocated.getRowDim() == dim && preallocated.getColDim() == dim) {
                R064Store inverse = (R064Store) preallocated;
                GETRS.invokeIdentity(dim, this.getInPlace().data, 0, Math.max(1, dim), this.getInterchanges(), 0, inverse.data, 0, Math.max(1, dim));
            } else {
                super.substituteIdentity(preallocated);
            }
        }

        @Override
        boolean update(final Pivot rowOrder, final Pivot colOrder, final int columnIndex,
                final Access1D.Collectable<Double, ? super TransformableRegion<Double>> column) {
            return FletcherMatthews.update(rowOrder, this.getInPlace(), colOrder, columnIndex, column, this.getWorkerColumn(this.getRowDim()));
        }

    }

    static final class R128 extends Generic<Quadruple> {

        R128() {
            super(GenericStore.R128);
        }

    }

    private Pivot myColPivot = null;
    /**
     * The row interchanges, as {@link GETRS} takes them: as returned by {@link GETRF}, or derived from the
     * pivot order.
     */
    private transient int[] myInterchanges = null;
    private final Pivot myPivot = new Pivot();
    private transient PhysicalStore<N> myWorkerColumn = null;
    private transient PhysicalStore<N> myWorkerRow = null;

    protected DenseLU(final PhysicalStore.Factory<N, ? extends M> factory) {
        super(factory);
    }

    @Override
    public void btran(final double[] arg) {

        if (myColPivot != null) {
            myColPivot.applyPivotOrder(arg);
        }

        this.substitute(true, arg);
    }

    @Override
    public void btran(final PhysicalStore<N> arg) {

        if (myColPivot != null) {
            myColPivot.applyPivotOrder(arg);
        }

        this.substitute(true, arg);
    }

    @Override
    public N calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    @Override
    public boolean decompose(final Access2D.Collectable<N, ? super TransformableRegion<N>> matrix) {
        return this.doDecompose(matrix, true);
    }

    @Override
    public boolean decomposeWithoutPivoting(final Collectable<N, ? super TransformableRegion<N>> matrix) {
        return this.doDecompose(matrix, false);
    }

    @Override
    public void ftran(final double[] arg) {

        this.substitute(false, arg);

        if (myColPivot != null) {
            myColPivot.applyReverseOrder(arg);
        }
    }

    @Override
    public void ftran(final PhysicalStore<N> arg) {

        this.substitute(false, arg);

        if (myColPivot != null) {
            myColPivot.applyReverseOrder(arg);
        }
    }

    @Override
    public N getDeterminant() {

        AggregatorFunction<N> aggrFunc = this.aggregator().product();

        this.getInPlace().visitDiagonal(aggrFunc);

        if (myPivot.signum() < 0) {
            return aggrFunc.toScalar().negate().get();
        } else {
            return aggrFunc.get();
        }
    }

    /**
     * [A]=[P][L][U][Q]
     */
    @Override
    public List<InvertibleFactor<N>> getFactors() {

        if (myColPivot != null) {
            return List.of(this.getFactorP(), this.getFactorL(), this.getFactorU(), this.getFactorQ().get());
        } else {
            return List.of(this.getFactorP(), this.getFactorL(), this.getFactorU());
        }
    }

    @Override
    public MatrixStore<N> getInverse(final PhysicalStore<N> preallocated) {

        this.substituteIdentity(preallocated);

        if (myColPivot != null) {
            this.applyReverseOrder(myColPivot, preallocated);
        }

        return preallocated;
    }

    @Override
    public MatrixStore<N> getL() {
        return this.getFactorL().get();
    }

    @Override
    public int[] getPivotOrder() {
        return myPivot.getOrder();
    }

    @Override
    public double getRankThreshold() {

        N largest = this.getInPlace().aggregateDiagonal(Aggregator.LARGEST);
        double epsilon = this.getDimensionalEpsilon();

        return epsilon * Math.max(MACHINE_SMALLEST, NumberDefinition.doubleValue(largest));
    }

    @Override
    public int[] getReversePivotOrder() {
        return myPivot.reverseOrder();
    }

    /**
     * Solves [this][X] = [rhs] by first solving
     *
     * <pre>{@code
     * [L][Y] = [rhs]
     * }</pre>
     *
     * and then
     *
     * <pre>{@code
     * [U][X] = [Y]
     * }</pre>
     *
     * @param rhs The right hand side
     * @return [X] The solution will be written to "preallocated" and then returned.
     */
    @Override
    public MatrixStore<N> getSolution(final Collectable<N, ? super PhysicalStore<N>> rhs, final PhysicalStore<N> preallocated) {

        rhs.supplyTo(preallocated);

        this.substituteColumns(preallocated);

        if (myColPivot != null) {
            this.applyReverseOrder(myColPivot, preallocated);
        }

        return preallocated;
    }

    @Override
    public MatrixStore<N> getU() {
        return this.getFactorU().get();
    }

    @Override
    public MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse();
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    @Override
    public MatrixStore<N> invert(final Access2D<?> original, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    @Override
    public boolean isPivoted() {
        return myPivot.isModified();
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public PhysicalStore<N> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public MatrixStore<N> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<N> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    @Override
    public boolean updateColumn(final int columnIndex, final Access1D.Collectable<N, ? super TransformableRegion<N>> newColumn) {

        myInterchanges = null;

        if (myColPivot == null) {
            myColPivot = new Pivot();
            myColPivot.reset(this.getColDim());
        }

        return this.update(myPivot, myColPivot, columnIndex, newColumn);
    }

    private boolean doDecompose(final Access2D.Collectable<N, ? super PhysicalStore<N>> matrix, final boolean pivoting) {

        this.reset();

        M tmpInPlace = this.setInPlace(matrix);

        myPivot.reset(this.getRowDim());

        if (pivoting) {

            int[] ipiv = new int[this.getMinDim()];

            this.factorise(tmpInPlace, ipiv);

            for (int i = 0; i < ipiv.length; i++) {
                myPivot.change(ipiv[i] - 1, i);
            }

            myInterchanges = ipiv;

        } else {

            this.factoriseWithoutPivoting(tmpInPlace);

            myInterchanges = null;
        }

        return this.computed(true);
    }

    @Override
    protected boolean checkSolvability() {
        return this.isSquare() && this.isFullRank();
    }

    /**
     * Factorises the in-place matrix, [A]=[P][L][U], returning [P] as row interchanges, as {@link GETRF}
     * does.
     */
    abstract void factorise(M inPlace, int[] ipiv);

    /**
     * Factorises the in-place matrix, [A]=[L][U], without pivoting.
     */
    abstract void factoriseWithoutPivoting(M inPlace);

    /**
     * [A]=[P][L][U][Q]
     */
    MatrixDecomposition.Factor<N> getFactorL() {
        return new FactorL<>(this.getInPlace());
    }

    /**
     * [A]=[P][L][U][Q]
     */
    MatrixDecomposition.Factor<N> getFactorP() {
        return new FactorPivot<>(this.makeIdentity(this.getRowDim()), myPivot, true);
    }

    /**
     * [A]=[P][L][U][Q]
     */
    Optional<MatrixDecomposition.Factor<N>> getFactorQ() {

        if (myColPivot != null) {
            return Optional.of(new FactorPivot<>(this.makeIdentity(this.getColDim()), myColPivot, false));
        } else {
            return Optional.empty();
        }
    }

    /**
     * [A]=[P][L][U][Q]
     */
    MatrixDecomposition.Factor<N> getFactorU() {
        return new FactorU<>(this.getInPlace(), myColPivot);
    }

    /**
     * The pivot order as a sequence of row interchanges: interchanging row i with row ipiv[i] - 1, for i = 0,
     * 1, ..., puts the rows in the pivot order.
     */
    int[] getInterchanges() {

        if (myInterchanges == null) {

            int[] order = this.getPivotOrder();
            int dim = order.length;

            // The row at each position, and the position of each row, as the interchanges are made
            int[] rows = new int[dim];
            int[] positions = new int[dim];
            for (int i = 0; i < dim; i++) {
                rows[i] = i;
                positions[i] = i;
            }

            int[] interchanges = new int[dim];
            for (int i = 0; i < dim; i++) {
                int other = positions[order[i]];
                interchanges[i] = other + 1;
                int displaced = rows[i];
                rows[other] = displaced;
                positions[displaced] = other;
                rows[i] = order[i];
                positions[order[i]] = i;
            }

            myInterchanges = interchanges;
        }

        return myInterchanges;
    }

    int[] getReducedPivots() {

        int[] retVal = new int[this.getRank()];
        int[] tmpFullPivots = this.getPivotOrder();

        PhysicalStore<N> tmpInPlace = this.getInPlace();

        int tmpRedInd = 0;
        for (int ij = 0; ij < tmpFullPivots.length; ij++) {
            if (!tmpInPlace.isSmall(ij, ij, PrimitiveMath.ONE)) {
                retVal[tmpRedInd++] = tmpFullPivots[ij];
            }
        }

        return retVal;
    }

    PhysicalStore<N> getWorkerColumn(final int nbRows) {
        if (myWorkerColumn == null || myWorkerColumn.getRowDim() != nbRows) {
            myWorkerColumn = this.preallocate(nbRows, 1, 1);
        }
        return myWorkerColumn;
    }

    PhysicalStore<N> getWorkerRow(final int nbCols) {
        if (myWorkerRow == null || myWorkerRow.getColDim() != nbCols) {
            myWorkerRow = this.preallocate(1, nbCols, 1);
        }
        return myWorkerRow;
    }

    /**
     * Solves [P][L][U][x]=[b], or ([P][L][U])<sup>T</sup>[x]=[b] when transposed, in place – ftran and btran
     * without the column pivot.
     */
    void substitute(final boolean transpose, final double[] arg) {

        M body = this.getInPlace();

        if (transpose) {
            body.substituteForwards(true, false, arg);
            body.substituteBackwards(true, true, arg);
            myPivot.applyReverseOrder(arg);
        } else {
            myPivot.applyPivotOrder(arg);
            body.substituteForwards(false, true, arg);
            body.substituteBackwards(false, false, arg);
        }
    }

    /**
     * @see #substitute(boolean, double[])
     */
    void substitute(final boolean transpose, final PhysicalStore<N> arg) {

        M body = this.getInPlace();

        if (transpose) {
            body.substituteForwards(true, false, arg);
            body.substituteBackwards(true, true, arg);
            myPivot.applyReverseOrder(arg);
        } else {
            myPivot.applyPivotOrder(arg);
            body.substituteForwards(false, true, arg);
            body.substituteBackwards(false, false, arg);
        }
    }

    /**
     * Solves [P][L][U][X]=[B] in place, for all columns of [B].
     */
    void substituteColumns(final PhysicalStore<N> rhs) {

        this.applyPivotOrder(myPivot, rhs);

        M body = this.getInPlace();

        rhs.substituteForwards(body, true, false, false);

        rhs.substituteBackwards(body, false, false, false);
    }

    /**
     * Solves [P][L][U][X]=[I], writing the inverse of [P][L][U] to preallocated.
     */
    void substituteIdentity(final PhysicalStore<N> preallocated) {

        if (myPivot.isModified()) {
            preallocated.fillAll(this.scalar().zero().get());
            int[] pivotOrder = myPivot.getOrder();
            int numbRows = this.getRowDim();
            for (int i = 0; i < numbRows; i++) {
                preallocated.set(i, pivotOrder[i], PrimitiveMath.ONE);
            }
        }

        M body = this.getInPlace();

        preallocated.substituteForwards(body, true, false, !myPivot.isModified());

        preallocated.substituteBackwards(body, false, false, false);
    }

    /**
     * Updates [P][L][U][Q], in place, when the column at columnIndex of the decomposed matrix is replaced –
     * by {@link FletcherMatthews}.
     *
     * @return false if the updated matrix is singular, or the update numerically unstable
     */
    abstract boolean update(Pivot rowOrder, Pivot colOrder, int columnIndex, Access1D.Collectable<N, ? super TransformableRegion<N>> column);

}
