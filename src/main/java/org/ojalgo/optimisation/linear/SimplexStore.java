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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.EnumPartition;

abstract class SimplexStore implements Optimisation.SolverData<Double> {

    enum ColumnState {

        /**
         * Basic variables
         */
        BASIS('B'),
        /**
         * Variables at their lower bound - may increase
         */
        LOWER('L'),
        /**
         * Used for unbounded variables that are not (yet) in the basis. Assigned value 0.0 and may either
         * increase or decrease.
         */
        UNBOUNDED('–'),
        /**
         * Variables at their upper bound – may decrease
         */
        UPPER('U');

        private final String myKey;

        ColumnState(final char key) {
            myKey = String.valueOf(key);
        }

        String key() {
            return myKey;
        }
    }

    static final class SimplexStructure {

        final int nbArtiVars;
        final int nbEqConstr;
        final int nbLoConstr;
        final int nbProbVars;
        final int nbSlckVars;
        final int nbUpConstr;

        SimplexStructure(final int constraints, final int variables) {
            this(0, 0, constraints, variables, 0, 0);
        }

        SimplexStructure(final int constrUp, final int constrLo, final int constrEq, final int varsProb, final int varsSlck, final int varsArti) {

            super();

            nbUpConstr = constrUp;
            nbLoConstr = constrLo;
            nbEqConstr = constrEq;

            nbProbVars = varsProb;
            nbSlckVars = varsSlck;
            nbArtiVars = varsArti;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SimplexStructure)) {
                return false;
            }
            SimplexStructure other = (SimplexStructure) obj;
            if ((nbArtiVars != other.nbArtiVars) || (nbEqConstr != other.nbEqConstr) || (nbLoConstr != other.nbLoConstr) || (nbProbVars != other.nbProbVars)) {
                return false;
            }
            if ((nbSlckVars != other.nbSlckVars) || (nbUpConstr != other.nbUpConstr)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + nbArtiVars;
            result = prime * result + nbEqConstr;
            result = prime * result + nbLoConstr;
            result = prime * result + nbProbVars;
            result = prime * result + nbSlckVars;
            return prime * result + nbUpConstr;
        }

        @Override
        public String toString() {
            return "SimplexStructure [nbUpConstr=" + nbUpConstr + ", nbLoConstr=" + nbLoConstr + ", nbEqConstr=" + nbEqConstr + ", nbProbVars=" + nbProbVars
                    + ", nbSlckVars=" + nbSlckVars + ", nbArtiVars=" + nbArtiVars + "]";
        }

        int countConstraints() {
            return nbUpConstr + nbLoConstr + nbEqConstr;
        }

        int countVariables() {
            return nbProbVars + nbSlckVars + nbArtiVars;
        }

        boolean isFullSetOfArtificials() {
            return nbArtiVars == this.countConstraints();
        }

    }

    static SimplexStore build(final ExpressionsBasedModel model) {
        return SimplexStore.build(model, SimplexStore::newInstance);
    }

    static <S extends SimplexStore> S build(final ExpressionsBasedModel model, final Function<SimplexStructure, S> storeFactory) {

        Set<IntIndex> fixedVariables = model.getFixedVariables();
        List<Variable> freeVariables = model.getFreeVariables();

        List<Expression> equalityConstraints = new ArrayList<>();
        List<Expression> lowerConstraints = new ArrayList<>();
        List<Expression> upperConstraints = new ArrayList<>();

        model.constraints().map(c -> c.compensate(fixedVariables)).forEach(constraint -> {
            if (constraint.isEqualityConstraint()) {
                equalityConstraints.add(constraint);
            } else {
                if (constraint.isLowerConstraint()) {
                    lowerConstraints.add(constraint);
                }
                if (constraint.isUpperConstraint()) {
                    upperConstraints.add(constraint);
                }
            }
        });

        Expression objective = model.objective().compensate(fixedVariables);

        int nbUpConstr = upperConstraints.size();
        int nbLoConstr = lowerConstraints.size();
        int nbEqConstr = equalityConstraints.size();

        int nbProbVars = freeVariables.size();
        int nbSlckVars = nbUpConstr + nbLoConstr;
        int nbArtiVars = nbEqConstr;

        SimplexStructure structure = new SimplexStructure(nbUpConstr, nbLoConstr, nbEqConstr, nbProbVars, nbSlckVars, nbArtiVars);

        S simplex = storeFactory.apply(structure);

        double[] lowerBounds = simplex.getLowerBounds();
        double[] upperBounds = simplex.getUpperBounds();

        Mutate2D mtrxA = simplex.constraintsBody();
        Mutate1D mtrxB = simplex.constraintsRHS();
        Mutate1D mtrxC = simplex.objective();

        for (int i = 0; i < nbUpConstr; i++) {
            Expression expression = upperConstraints.get(i);
            for (IntIndex key : expression.getLinearKeySet()) {
                int column = model.indexOfFreeVariable(key);
                double factor = expression.doubleValue(key, true);
                mtrxA.set(i, column, factor);
            }
            mtrxA.set(i, nbProbVars + i, ONE);
            mtrxB.set(i, expression.getUpperLimit(true, POSITIVE_INFINITY));
            lowerBounds[nbProbVars + i] = ZERO;
            upperBounds[nbProbVars + i] = POSITIVE_INFINITY;
        }

        for (int i = 0; i < nbLoConstr; i++) {
            Expression expression = lowerConstraints.get(i);
            for (IntIndex key : expression.getLinearKeySet()) {
                int column = model.indexOfFreeVariable(key);
                double factor = expression.doubleValue(key, true);
                mtrxA.set(nbUpConstr + i, column, factor);
            }
            mtrxA.set(nbUpConstr + i, nbProbVars + nbUpConstr + i, ONE);
            mtrxB.set(nbUpConstr + i, expression.getLowerLimit(true, NEGATIVE_INFINITY));
            lowerBounds[nbProbVars + nbUpConstr + i] = NEGATIVE_INFINITY;
            upperBounds[nbProbVars + nbUpConstr + i] = ZERO;
        }

        for (int i = 0; i < nbEqConstr; i++) {
            Expression expression = equalityConstraints.get(i);
            for (IntIndex key : expression.getLinearKeySet()) {
                int column = model.indexOfFreeVariable(key);
                double factor = expression.doubleValue(key, true);
                mtrxA.set(nbUpConstr + nbLoConstr + i, column, factor);
            }
            mtrxA.set(nbUpConstr + nbLoConstr + i, nbProbVars + nbSlckVars + i, ONE);
            mtrxB.set(nbUpConstr + nbLoConstr + i, expression.getUpperLimit(true, ZERO));
            lowerBounds[nbProbVars + nbSlckVars + i] = ZERO;
            upperBounds[nbProbVars + nbSlckVars + i] = ZERO;
        }

        for (int i = 0; i < nbProbVars; i++) {
            Variable variable = freeVariables.get(i);
            lowerBounds[i] = variable.getLowerLimit(false, NEGATIVE_INFINITY);
            upperBounds[i] = variable.getUpperLimit(false, POSITIVE_INFINITY);
        }

        boolean negate = model.getOptimisationSense() == Optimisation.Sense.MAX;
        for (IntIndex key : objective.getLinearKeySet()) {
            double weight = objective.doubleValue(key, true);
            mtrxC.set(model.indexOfFreeVariable(key), negate ? -weight : weight);
        }

        return simplex;
    }

    static SimplexStore build(final LinearSolver.GeneralBuilder builder) {
        return SimplexStore.build(builder, SimplexStore::newInstance);
    }

    static <S extends SimplexStore> S build(final LinearSolver.GeneralBuilder builder, final Function<SimplexStructure, S> storeFactory, final int... basis) {
        return builder.newSimplexStore(storeFactory, basis);
    }

    static SimplexStore newInstance(final SimplexStructure structure) {
        if (Math.max(structure.nbProbVars, structure.countConstraints()) > 1) {
            return new RevisedStore(structure);
        } else {
            return new TableauStore(structure);
        }
    }

    private transient int[] myExcludedLower = null;
    private transient int[] myExcludedUnbounded = null;
    private transient int[] myExcludedUpper = null;
    private final double[] myLowerBounds;
    private final EnumPartition<SimplexStore.ColumnState> myPartition;
    private final SimplexStructure myStructure;
    private final List<String> myToStringList = new ArrayList<>();
    private final double[] myUpperBounds;
    /**
     * excluded == not in the basis
     */
    final int[] excluded;
    /**
     * included == in the basis
     */
    final int[] included;
    /**
     * The number of constraints (upper, lower and equality)
     */
    final int m;
    /**
     * The number of variables (all kinds)
     */
    final int n;

    SimplexStore(final SimplexStructure structure) {

        super();

        m = structure.countConstraints();
        n = structure.countVariables();

        myLowerBounds = new double[n];
        myUpperBounds = new double[n];

        excluded = Structure1D.newIncreasingRange(0, n - m);
        included = Structure1D.newIncreasingRange(n - m, m);

        myPartition = new EnumPartition<>(n, ColumnState.LOWER);
        for (int j = 0; j < m; j++) {
            myPartition.update(included[j], ColumnState.BASIS);
        }

        myStructure = structure;
    }

    public int countAdditionalConstraints() {
        return 0;
    }

    public int countConstraints() {
        return m;
    }

    public int countEqualityConstraints() {
        return m;
    }

    public int countInequalityConstraints() {
        return 0;
    }

    public int countVariables() {
        return n;
    }

    @Override
    public String toString() {

        myToStringList.clear();

        for (int i = 0; i < myPartition.size(); i++) {
            myToStringList.add(myPartition.get(i).key());
        }

        return myToStringList.toString();
    }

    private SimplexStore basis(final int index) {
        myPartition.update(index, ColumnState.BASIS);
        return this;
    }

    protected void pivot(final SimplexSolver.IterDescr iteration) {

        ExitInfo exit = iteration.exit;
        EnterInfo enter = iteration.enter;

        this.updateBasis(exit.index, exit.to, enter.index);
    }

    protected void shiftColumn(final int col, final double shift) {
        myLowerBounds[col] -= shift;
        myUpperBounds[col] -= shift;
    }

    abstract void calculateDualDirection(ExitInfo exit);

    abstract void calculateIteration();

    abstract void calculateIteration(IterDescr iteration);

    abstract void calculatePrimalDirection(EnterInfo enter);

    /**
     * The simplex' constraints body (including the parts corresponding to slack and artificial variables).
     */
    abstract Mutate2D constraintsBody();

    /**
     * The simplex' constraints RHS.
     */
    abstract Mutate1D constraintsRHS();

    abstract void copyBasicSolution(double[] solution);

    abstract void copyObjective();

    double[] extractSolution() {

        double[] retVal = new double[n];

        int[] lower = this.getExcludedLower();
        for (int i = 0; i < lower.length; i++) {
            int j = lower[i];
            double value = this.getLowerBound(j);
            if (Double.isFinite(value)) {
                retVal[j] = value;
            } else {
                retVal[j] = ZERO;
            }
        }

        int[] upper = this.getExcludedUpper();
        for (int i = 0; i < upper.length; i++) {
            int j = upper[i];
            double value = this.getUpperBound(j);
            if (Double.isFinite(value)) {
                retVal[j] = value;
            } else {
                retVal[j] = ZERO;
            }
        }

        this.copyBasicSolution(retVal);

        return retVal;
    }

    abstract double extractValue();

    ColumnState getColumnState(final int index) {
        return myPartition.get(index);
    }

    abstract double getCost(int i);

    /**
     * Indices of columns/variables at their lower bounds.
     */
    int[] getExcludedLower() {
        int count = myPartition.count(ColumnState.LOWER);
        if (myExcludedLower == null || myExcludedLower.length != count) {
            myExcludedLower = new int[count];
        }
        myPartition.extract(ColumnState.LOWER, false, myExcludedLower);
        return myExcludedLower;
    }

    /**
     * Indices of unbounded columns/variables.
     */
    int[] getExcludedUnbounded() {
        int count = myPartition.count(ColumnState.UNBOUNDED);
        if (myExcludedUnbounded == null || myExcludedUnbounded.length != count) {
            myExcludedUnbounded = new int[count];
        }
        myPartition.extract(ColumnState.UNBOUNDED, false, myExcludedUnbounded);
        return myExcludedUnbounded;
    }

    /**
     * Indices of columns/variables at their upper bounds.
     */
    int[] getExcludedUpper() {
        int count = myPartition.count(ColumnState.UPPER);
        if (myExcludedUpper == null || myExcludedUpper.length != count) {
            myExcludedUpper = new int[count];
        }
        myPartition.extract(ColumnState.UPPER, false, myExcludedUpper);
        return myExcludedUpper;
    }

    abstract double getInfeasibility(int i);

    double getLowerBound(final int index) {
        return myLowerBounds[index];
    }

    double[] getLowerBounds() {
        return myLowerBounds;
    }

    /**
     * The "distance" from the current basic value to the lower bound.
     */
    final double getLowerGap(final int i) {

        double xi = this.getTableauRHS(i);
        double lb = this.getLowerBound(included[i]);

        if (xi > lb) {
            return xi - lb;
        } else {
            return ZERO;
        }
    }

    /**
     * {@link #getUpperBound(int)} minus {@link #getLowerBound(int)}
     */
    double getRange(final int index) {
        return myUpperBounds[index] - myLowerBounds[index];
    }

    abstract double getReducedCost(int je);

    abstract double getTableauElement(ExitInfo exit, int je);

    abstract double getTableauElement(int i, EnterInfo enter);

    abstract double getTableauRHS(int i);

    double getUpperBound(final int index) {
        return myUpperBounds[index];
    }

    double[] getUpperBounds() {
        return myUpperBounds;
    }

    /**
     * The "distance" from the current basic value to the upper bound.
     */
    final double getUpperGap(final int i) {

        double xi = this.getTableauRHS(i);
        double ub = this.getUpperBound(included[i]);

        if (ub > xi) {
            return ub - xi;
        } else {
            return ZERO;
        }
    }

    /**
     * The problem is small enough to be explicitly printed/logged – log the entire tableau at each iteration
     * when debugging.
     */
    boolean isPrintable() {
        return myStructure.countVariables() <= 32;
    }

    SimplexStore lower(final int index) {
        myPartition.update(index, ColumnState.LOWER);
        return this;
    }

    final DualSimplexSolver newDualSimplexSolver(final Options optimisationOptions, final int... basis) {
        DualSimplexSolver solver = new DualSimplexSolver(optimisationOptions, this);
        if (basis.length > 0) {
            solver.basis(basis);
        }
        return solver;
    }

    final PhasedSimplexSolver newPhasedSimplexSolver(final Options optimisationOptions, final int... basis) {
        PhasedSimplexSolver solver = new PhasedSimplexSolver(optimisationOptions, this);
        if (basis.length > 0) {
            solver.basis(basis);
        }
        return solver;
    }

    final PrimalSimplexSolver newPrimalSimplexSolver(final Options optimisationOptions, final int... basis) {
        PrimalSimplexSolver solver = new PrimalSimplexSolver(optimisationOptions, this);
        if (basis.length > 0) {
            solver.basis(basis);
        }
        return solver;
    }

    /**
     * The simplex' objective function.
     */
    abstract Mutate1D objective();

    /**
     * Everything that is not in the basis is set to be in at lower bound.
     */
    void resetBasis(final int[] newBasis) {

        if (newBasis.length != m) {
            throw new IllegalStateException();
        }

        myPartition.fill(ColumnState.LOWER);

        for (int i = 0; i < newBasis.length; i++) {
            myPartition.update(newBasis[i], ColumnState.BASIS);
            included[i] = newBasis[i];
        }

        myPartition.extract(ColumnState.BASIS, true, excluded);
    }

    abstract void restoreObjective();

    final SimplexStructure structure() {
        return myStructure;
    }

    SimplexStore unbounded(final int index) {
        myPartition.update(index, ColumnState.UNBOUNDED);
        return this;
    }

    void updateBasis(final int exit, final ColumnState exitToBound, final int enter) {

        int inclExit = included[exit];
        int exclEnter = excluded[enter];

        if (exitToBound == ColumnState.LOWER) {
            this.lower(inclExit);
        } else if (exitToBound == ColumnState.UPPER) {
            this.upper(inclExit);
        } else {
            throw new IllegalArgumentException();
        }

        this.basis(exclEnter);

        included[exit] = exclEnter;
        excluded[enter] = inclExit;
    }

    SimplexStore upper(final int index) {
        myPartition.update(index, ColumnState.UPPER);
        return this;
    }

}
