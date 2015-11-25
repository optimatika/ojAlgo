/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.BigMath.*;
import static org.ojalgo.function.BigFunction.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.IntIndex;
import org.ojalgo.access.IntRowColumn;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Lets you construct optimisation problems by combining (mathematical) expressions in terms of variables.
 * Each expression or variable can be a constraint and/or contribute to the objective function. An expression
 * or variable is turned into a constraint by setting a lower and/or upper limit. Use
 * {@linkplain ModelEntity#lower(Number)}, {@linkplain ModelEntity#upper(Number)} or
 * {@linkplain ModelEntity#level(Number)}. An expression or variable is made part of (contributing to) the
 * objective function by setting a contribution weight. Use {@linkplain ModelEntity#weight(Number)}.
 * </p>
 * <p>
 * You may think of variables as simple (the simplest possible) expressions, and of expressions as weighted
 * combinations of variables. They are both model entities and it is as such they can be turned into
 * constraints and set to contribute to the objective function. Alternatively you may choose to disregard the
 * fact that variables are model entities and simply treat them as index values. In this case everything
 * (constraints and objective) needs to be defined using expressions.
 * </p>
 * <p>
 * Basic instructions:
 * </p>
 * <ol>
 * <li>Define (create) a set of variables. Set contribution weights and lower/upper limits as needed.</li>
 * <li>Create a model using that set of variables.</li>
 * <li>Add expressions to the model. The model is the expression factory. Set contribution weights and
 * lower/upper limits as needed.</li>
 * <li>Solve your problem using either minimise() or maximise()</li>
 * </ol>
 * <p>
 * When using this class you do not need to worry about which solver will actually be used. The docs of the
 * various solvers describe requirements on input formats and similar. This is handled for you and should
 * absolutely NOT be considered here! Compared to using the various solvers directly this class actually does
 * something for you:
 * </p>
 * <ol>
 * <li>You can model your problems without worrying about specific solver requirements.</li>
 * <li>It knows which solver to use.</li>
 * <li>It knows how to use that solver.</li>
 * <li>It has a presolver that tries to simplify the problem before invoking a solver (sometimes it turns out
 * there is no need to invoke a solver at all).</li>
 * <li>When/if needed it scales problem parameters, before creating solver specific data structures, to
 * minimize numerical problems in the solvers.</li>
 * <li>It's the only way to access the integer solver.</li>
 * </ol>
 * <p>
 * There are some restrictions on the models you can create:
 * </p>
 * <ul>
 * <li>No quadratic constraints</li>
 * </ul>
 * <p>
 * The plan is that future versions should not have any restrictions like these.
 * </p>
 *
 * @author apete
 */
public final class ExpressionsBasedModel extends AbstractModel<GenericSolver> {

    public static abstract class Integration<S extends Optimisation.Solver> implements Optimisation.Integration<ExpressionsBasedModel, S> {

        /**
         * @see org.ojalgo.optimisation.Optimisation.Integration#extractSolverState(org.ojalgo.optimisation.Optimisation.Model)
         */
        public final Result extractSolverState(final ExpressionsBasedModel model) {
            return this.toSolverState(model.getVariableValues(), model);
        }

        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            final PrimitiveArray tmpModelSolution = PrimitiveArray.make(model.countVariables());

            for (final IntIndex tmpFixed : model.getFixedVariables()) {
                tmpModelSolution.set(tmpFixed.index, model.getVariable(tmpFixed.index).getValue().doubleValue());
            }

            final List<Variable> tmpFreeVariables = model.getFreeVariables();
            for (int f = 0; f < tmpFreeVariables.size(); f++) {
                final Variable tmpVariable = tmpFreeVariables.get(f);
                final int tmpIndex = model.indexOf(tmpVariable);
                tmpModelSolution.set(tmpIndex, solverState.doubleValue(f));
            }

            return new Result(solverState.getState(), solverState.getValue(), tmpModelSolution);
        }

        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            final List<Variable> tmpFreeVariables = model.getFreeVariables();

            final PrimitiveArray tmpSolverSolution = PrimitiveArray.make(tmpFreeVariables.size());
            final double[] tmpData = tmpSolverSolution.data;

            for (int i = 0; i < tmpData.length; i++) {
                final Variable tmpVariable = tmpFreeVariables.get(i);
                final int tmpIndex = model.indexOf(tmpVariable);
                tmpData[i] = modelState.doubleValue(tmpIndex);
            }

            return new Result(modelState.getState(), modelState.getValue(), tmpSolverSolution);
        }

    }

    public static abstract class Presolver implements Comparable<Presolver> {

        private final int myExecutionOrder;
        private final UUID myUUID = UUID.randomUUID();

        protected Presolver(final int executionOrder) {
            super();
            myExecutionOrder = executionOrder;
        }

        public int compareTo(final Presolver reference) {
            return Integer.compare(myExecutionOrder, reference.getExecutionOrder());
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Presolver)) {
                return false;
            }
            final Presolver other = (Presolver) obj;
            if (myUUID == null) {
                if (other.myUUID != null) {
                    return false;
                }
            } else if (!myUUID.equals(other.myUUID)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((myUUID == null) ? 0 : myUUID.hashCode());
            return result;
        }

        /**
         * @param expression
         * @param fixedVariables
         * @return True if any model entity was modified so that a re-run of the presolvers is necessary -
         *         typically when/if a variable was fixed.
         */
        public abstract boolean simplify(Expression expression, Set<IntIndex> fixedVariables);

        final int getExecutionOrder() {
            return myExecutionOrder;
        }

    }

    private static final String NEW_LINE = "\n";

    private static final String OBJ_FUNC_AS_CONSTR_KEY = UUID.randomUUID().toString();

    private static final String OBJECTIVE = "Generated/Aggregated Objective";

    private static final String START_END = "############################################\n";
    static final Comparator<Expression> CE = new Comparator<Expression>() {

        public int compare(final Expression o1, final Expression o2) {
            return Integer.compare(o2.countLinearFactors(), o1.countLinearFactors());
        }

    };

    static final List<ExpressionsBasedModel.Integration<?>> INTEGRATIONS = new ArrayList<>();
    static final TreeSet<Presolver> PRESOLVERS = new TreeSet<>();

    static {
        ExpressionsBasedModel.addPresolver(Presolvers.ZERO_ONE_TWO);
        ExpressionsBasedModel.addPresolver(Presolvers.OPPOSITE_SIGN);
    }

    public static boolean addIntegration(final Integration<?> integration) {
        return INTEGRATIONS.add(integration);
    }

    public static boolean addPresolver(final Presolver presolver) {
        return PRESOLVERS.add(presolver);
    }

    public static void clearIntegrations() {
        INTEGRATIONS.clear();
    }

    public static void clearPresolvers() {
        PRESOLVERS.clear();
    }

    public static boolean removeIntegration(final Integration<?> integration) {
        return INTEGRATIONS.remove(integration);
    }

    public static boolean removePresolver(final Presolver presolver) {
        return PRESOLVERS.remove(presolver);
    }

    private final HashMap<String, Expression> myExpressions = new HashMap<String, Expression>();
    private final HashSet<IntIndex> myFixedVariables = new HashSet<IntIndex>();

    private transient int[] myFreeIndices = null;
    private final List<Variable> myFreeVariables = new ArrayList<>();
    private transient int[] myIntegerIndices = null;
    private final List<Variable> myIntegerVariables = new ArrayList<>();
    private transient int[] myNegativeIndices = null;
    private final List<Variable> myNegativeVariables = new ArrayList<>();
    private transient int[] myPositiveIndices = null;
    private final List<Variable> myPositiveVariables = new ArrayList<>();

    private final ArrayList<Variable> myVariables = new ArrayList<Variable>();
    private final boolean myWorkCopy;

    public ExpressionsBasedModel() {

        super();

        myWorkCopy = false;
    }

    public ExpressionsBasedModel(final Collection<? extends Variable> variables) {

        super();

        for (final Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }

        myWorkCopy = false;
    }

    public ExpressionsBasedModel(final Variable... variables) {

        super();

        for (final Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }

        myWorkCopy = false;
    }

    ExpressionsBasedModel(final ExpressionsBasedModel modelToCopy, final boolean workCopy) {

        super(modelToCopy.options);

        this.setMinimisation(modelToCopy.isMinimisation());

        for (final Variable tmpVariable : modelToCopy.getVariables()) {
            this.addVariable(tmpVariable.copy());
        }

        for (final Expression tmpExpression : modelToCopy.getExpressions()) {
            myExpressions.put(tmpExpression.getName(), tmpExpression.copy(this, !workCopy));
        }

        if (myWorkCopy = workCopy) {
            myFixedVariables.addAll(modelToCopy.getFixedVariables());
        }
    }

    ExpressionsBasedModel(final Options someOptions) {

        super(someOptions);

        myWorkCopy = false;
    }

    public Expression addExpression(final String name) {

        final Expression retVal = new Expression(name, this);

        myExpressions.put(name, retVal);

        return retVal;
    }

    public void addVariable(final Variable variable) {
        if (myWorkCopy) {
            throw new IllegalStateException("This model is a copy - its set of variables cannot be modified!");
        } else {
            myVariables.add(variable);
            variable.setIndex(new IntIndex(myVariables.size() - 1));
        }
    }

    public void addVariables(final Collection<? extends Variable> variables) {
        for (final Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }
    }

    /**
     * @return A stream of variables that are constraints and not fixed
     */
    public Stream<Variable> bounds() {
        return this.variables().filter((final Variable v) -> v.isConstraint());
    }

    /**
     * @return A prefiltered stream of expressions that are constraints and have not been markes as redundant
     */
    public Stream<Expression> constraints() {
        return myExpressions.values().stream().filter((final Expression c) -> c.isConstraint() && !c.isRedundant());
    }

    public ExpressionsBasedModel copy() {
        return new ExpressionsBasedModel(this, false);
    }

    public int countExpressions() {
        return myExpressions.size();
    }

    public int countVariables() {
        return myVariables.size();
    }

    @Override
    public void dispose() {

        this.flushCaches();

        for (final Expression tmpExprerssion : myExpressions.values()) {
            tmpExprerssion.destroy();
        }
        myExpressions.clear();

        for (final Variable tmpVariable : myVariables) {
            tmpVariable.destroy();
        }
        myVariables.clear();

        myFixedVariables.clear();
    }

    public Expression getExpression(final String name) {
        return myExpressions.get(name);
    }

    public Collection<Expression> getExpressions() {
        return Collections.unmodifiableCollection(myExpressions.values());
    }

    public Set<IntIndex> getFixedVariables() {
        return Collections.unmodifiableSet(myFixedVariables);
    }

    /**
     * @return A list of the variables that are not fixed at a specific value
     */
    public List<Variable> getFreeVariables() {

        if (myFreeIndices == null) {
            this.categoriseVariables();
        }

        return Collections.unmodifiableList(myFreeVariables);
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and are marked as integer
     *         variables
     */
    public List<Variable> getIntegerVariables() {

        if (myIntegerIndices == null) {
            this.categoriseVariables();
        }

        return Collections.unmodifiableList(myIntegerVariables);
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include negative
     *         values
     */
    public List<Variable> getNegativeVariables() {

        if (myNegativeIndices == null) {
            this.categoriseVariables();
        }

        return Collections.unmodifiableList(myNegativeVariables);
    }

    /**
     * @return The full objective function
     */
    public Expression getObjectiveExpression() {

        final Expression myObjectiveExpression = new Expression(OBJECTIVE, this);

        Variable tmpVariable;
        for (int i = 0; i < myVariables.size(); i++) {
            tmpVariable = myVariables.get(i);

            if (tmpVariable.isObjective()) {
                myObjectiveExpression.set(i, tmpVariable.getContributionWeight());
            }
        }

        BigDecimal tmpOldVal = null;
        BigDecimal tmpDiff = null;
        BigDecimal tmpNewVal = null;

        for (final Expression tmpExpression : myExpressions.values()) {

            if (tmpExpression.isObjective()) {

                final BigDecimal tmpContributionWeight = tmpExpression.getContributionWeight();
                final boolean tmpNotOne = tmpContributionWeight.compareTo(ONE) != 0; // To avoid multiplication by 1.0

                if (tmpExpression.isAnyLinearFactorNonZero()) {
                    for (final IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                        tmpOldVal = myObjectiveExpression.get(tmpKey);
                        tmpDiff = tmpExpression.get(tmpKey);
                        tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                        final Number value = tmpNewVal;
                        myObjectiveExpression.set(tmpKey, value);
                    }
                }

                if (tmpExpression.isAnyQuadraticFactorNonZero()) {
                    for (final IntRowColumn tmpKey : tmpExpression.getQuadraticKeySet()) {
                        tmpOldVal = myObjectiveExpression.get(tmpKey);
                        tmpDiff = tmpExpression.get(tmpKey);
                        tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                        final Number value = tmpNewVal;
                        myObjectiveExpression.set(tmpKey, value);
                    }
                }
            }
        }

        return myObjectiveExpression;
    }

    /**
     * @deprecated v39 Use {@link #objective()} and {@link Expression#toFunction()} instead.
     */
    @Deprecated
    public MultiaryFunction.TwiceDifferentiable<Double> getObjectiveFunction() {
        return this.objective().toFunction();
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include positive
     *         values and/or zero
     */
    public List<Variable> getPositiveVariables() {

        if (myPositiveIndices == null) {
            this.categoriseVariables();
        }

        return Collections.unmodifiableList(myPositiveVariables);
    }

    public Variable getVariable(final int index) {
        return myVariables.get(index);
    }

    public List<Variable> getVariables() {
        return Collections.unmodifiableList(myVariables);
    }

    public Optimisation.Result getVariableValues() {
        return this.getVariableValues(options.slack);
    }

    /**
     * Null variable values are replaced with 0.0. If any variable value is null the state is set to
     * INFEASIBLE even if zero would actually be a feasible value. The objective function value is not
     * calculated for infeasible variable values.
     */
    public Optimisation.Result getVariableValues(final NumberContext validationContext) {

        final int tmpNumberOfVariables = myVariables.size();

        State retState = State.UNEXPLORED;
        double retValue = Double.NaN;
        final Array1D<BigDecimal> retSolution = Array1D.BIG.makeZero(tmpNumberOfVariables);

        boolean tmpAllVarsSomeInfo = true;

        for (int i = 0; i < tmpNumberOfVariables; i++) {

            final Variable tmpVariable = myVariables.get(i);

            if (tmpVariable.getValue() != null) {

                retSolution.set(i, tmpVariable.getValue());

            } else {

                if (tmpVariable.isEqualityConstraint()) {
                    retSolution.set(i, tmpVariable.getLowerLimit());
                } else if (tmpVariable.isLowerLimitSet() && tmpVariable.isUpperLimitSet()) {
                    retSolution.set(i, DIVIDE.invoke(tmpVariable.getLowerLimit().add(tmpVariable.getUpperLimit()), TWO));
                } else if (tmpVariable.isLowerLimitSet()) {
                    retSolution.set(i, tmpVariable.getLowerLimit());
                } else if (tmpVariable.isUpperLimitSet()) {
                    retSolution.set(i, tmpVariable.getUpperLimit());
                } else {
                    retSolution.set(i, ZERO);
                    tmpAllVarsSomeInfo = false; // This var no info
                }
            }
        }

        if (tmpAllVarsSomeInfo) {
            if (this.validate(retSolution, validationContext)) {
                retState = State.FEASIBLE;
                retValue = this.getObjectiveExpression().evaluate(retSolution).doubleValue();
            } else {
                retState = State.APPROXIMATE;
            }

        } else {
            retState = State.INFEASIBLE;
        }

        return new Optimisation.Result(retState, retValue, retSolution);
    }

    public int indexOf(final Variable variable) {
        return variable.getIndex().index;
    }

    /**
     * @param globalIndex General, global, variable index
     * @return Local index among the free variables. -1 indicates the variable is not a positive variable.
     */
    public int indexOfFreeVariable(final int globalIndex) {
        return myFreeIndices[globalIndex];
    }

    public int indexOfFreeVariable(final IntIndex variableIndex) {
        return this.indexOfFreeVariable(variableIndex.index);
    }

    public int indexOfFreeVariable(final Variable variable) {
        return this.indexOfFreeVariable(this.indexOf(variable));
    }

    /**
     * @param globalIndex General, global, variable index
     * @return Local index among the integer variables. -1 indicates the variable is not an integer variable.
     */
    public int indexOfIntegerVariable(final int globalIndex) {
        return myIntegerIndices[globalIndex];
    }

    public int indexOfIntegerVariable(final IntIndex variableIndex) {
        return this.indexOfIntegerVariable(variableIndex.index);
    }

    public int indexOfIntegerVariable(final Variable variable) {
        return this.indexOfIntegerVariable(variable.getIndex().index);
    }

    /**
     * @param globalIndex General, global, variable index
     * @return Local index among the negative variables. -1 indicates the variable is not a negative variable.
     */
    public int indexOfNegativeVariable(final int globalIndex) {
        return myNegativeIndices[globalIndex];
    }

    public int indexOfNegativeVariable(final IntIndex variableIndex) {
        return this.indexOfNegativeVariable(variableIndex.index);
    }

    public int indexOfNegativeVariable(final Variable variable) {
        return this.indexOfNegativeVariable(this.indexOf(variable));
    }

    /**
     * @param globalIndex General, global, variable index
     * @return Local index among the positive variables. -1 indicates the variable is not a positive variable.
     */
    public int indexOfPositiveVariable(final int globalIndex) {
        return myPositiveIndices[globalIndex];
    }

    public int indexOfPositiveVariable(final IntIndex variableIndex) {
        return this.indexOfPositiveVariable(variableIndex.index);
    }

    public int indexOfPositiveVariable(final Variable variable) {
        return this.indexOfPositiveVariable(this.indexOf(variable));
    }

    /**
     * Objective or any constraint has quadratic part.
     */
    public boolean isAnyExpressionQuadratic() {

        boolean retVal = false;

        String tmpExpressionKey;
        for (final Iterator<String> tmpIterator = myExpressions.keySet().iterator(); !retVal && tmpIterator.hasNext();) {
            tmpExpressionKey = tmpIterator.next();
            final Expression tmpExpression = myExpressions.get(tmpExpressionKey);
            retVal |= tmpExpression.isAnyQuadraticFactorNonZero() && (tmpExpression.isConstraint() || tmpExpression.isObjective());
        }

        return retVal;
    }

    public boolean isAnyVariableFixed() {
        return myFixedVariables.size() >= 1;
    }

    public boolean isAnyVariableInteger() {

        boolean retVal = false;

        final int tmpLength = myVariables.size();

        for (int i = 0; !retVal && (i < tmpLength); i++) {
            retVal |= myVariables.get(i).isInteger();
        }

        return retVal;
    }

    public boolean isWorkCopy() {
        return myWorkCopy;
    }

    public void limitObjective(final BigDecimal lower, final BigDecimal upper) {

        Expression tmpEpression = myExpressions.get(OBJ_FUNC_AS_CONSTR_KEY);
        if (tmpEpression == null) {
            tmpEpression = this.getObjectiveExpression().copy(this, false);
            myExpressions.put(OBJ_FUNC_AS_CONSTR_KEY, tmpEpression);
        }

        tmpEpression.lower(lower).upper(upper);
    }

    public Optimisation.Result maximise() {

        this.setMaximisation();

        final Result tmpSolverResult = this.solve(this.getVariableValues());

        return this.handleResult(tmpSolverResult);
    }

    public Optimisation.Result minimise() {

        this.setMinimisation();

        final Result tmpSolverResult = this.solve(this.getVariableValues());

        return this.handleResult(tmpSolverResult);
    }

    /**
     * @return The aggregated objective "function"
     */
    public Expression objective() {
        return this.getObjectiveExpression();
    }

    public ExpressionsBasedModel relax(final boolean inPlace) {

        final ExpressionsBasedModel retVal = inPlace ? this : new ExpressionsBasedModel(this, true);

        for (final Variable tmpVariable : retVal.getVariables()) {
            tmpVariable.relax();
        }

        return retVal;
    }

    /**
     * Any constrained non-redundant expression
     *
     * @deprecated v39 Use {@link #constraints()} instead
     */
    @Deprecated
    public List<Expression> selectExpressions() {

        //        final List<Expression> retVal = new ArrayList<Expression>();
        //
        //        for (final Expression tmpExpression : myExpressions.values()) {
        //            if (tmpExpression.isConstraint() && !tmpExpression.isRedundant()) {
        //                retVal.add(tmpExpression);
        //            }
        //        }

        return this.constraints().collect(Collectors.toList());
    }

    /**
     * Linear equality constrained expressions.
     *
     * @deprecated v39 Use {@link #constraints()} instead
     */
    @Deprecated
    public List<Expression> selectExpressionsLinearEquality() {

        //        final List<Expression> retVal = new ArrayList<Expression>();
        //
        //        for (final Expression tmpExpression : myExpressions.values()) {
        //            if (tmpExpression.isEqualityConstraint() && !tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
        //                retVal.add(tmpExpression);
        //            }
        //        }
        //
        //        //Collections.sort(retVal, CE);
        //        return Collections.unmodifiableList(retVal);

        return this.constraints().filter((final Expression c) -> c.isEqualityConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
    }

    /**
     * Linear lower constrained expressions.
     *
     * @deprecated v39 Use {@link #constraints()} instead
     */
    @Deprecated
    public List<Expression> selectExpressionsLinearLower() {

        //        final List<Expression> retVal = new ArrayList<Expression>();
        //
        //        for (final Expression tmpExpression : myExpressions.values()) {
        //            if (tmpExpression.isLowerConstraint() && !tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
        //                retVal.add(tmpExpression);
        //            }
        //        }

        //Collections.sort(retVal, CE);
        //       return Collections.unmodifiableList(retVal);

        return this.constraints().filter((final Expression c) -> c.isLowerConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
    }

    /**
     * Linear upper constrained expressions.
     *
     * @deprecated v39 Use {@link #constraints()} instead
     */
    @Deprecated
    public List<Expression> selectExpressionsLinearUpper() {

        //        final List<Expression> retVal = new ArrayList<Expression>();
        //
        //        for (final Expression tmpExpression : myExpressions.values()) {
        //            if (tmpExpression.isUpperConstraint() && !tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
        //                retVal.add(tmpExpression);
        //            }
        //        }
        //
        //        //Collections.sort(retVal, CE);
        //        return Collections.unmodifiableList(retVal);

        return this.constraints().filter((final Expression c) -> c.isUpperConstraint() && !c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
    }

    /**
     * Quadratic (and/or compound) equality constrained expressions.
     *
     * @deprecated v39 Use {@link #constraints()} instead
     */
    @Deprecated
    public List<Expression> selectExpressionsQuadraticEquality() {

        //        final List<Expression> retVal = new ArrayList<Expression>();
        //
        //        for (final Expression tmpExpression : myExpressions.values()) {
        //            if (tmpExpression.isEqualityConstraint() && tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
        //                retVal.add(tmpExpression);
        //            }
        //        }
        //
        //        //Collections.sort(retVal, CE);
        //        return Collections.unmodifiableList(retVal);

        return this.constraints().filter((final Expression c) -> c.isEqualityConstraint() && c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
    }

    /**
     * Quadratic (and/or compound) lower constrained expressions.
     *
     * @deprecated v39 Use {@link #constraints()} instead
     */
    @Deprecated
    public List<Expression> selectExpressionsQuadraticLower() {

        //        final List<Expression> retVal = new ArrayList<Expression>();
        //
        //        for (final Expression tmpExpression : myExpressions.values()) {
        //            if (tmpExpression.isLowerConstraint() && tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
        //                retVal.add(tmpExpression);
        //            }
        //        }
        //
        //        //Collections.sort(retVal, CE);
        //        return Collections.unmodifiableList(retVal);

        return this.constraints().filter((final Expression c) -> c.isLowerConstraint() && c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
    }

    /**
     * Quadratic (and/or compound) upper constrained expressions.
     *
     * @deprecated v39 Use {@link #constraints()} instead
     */
    @Deprecated
    public List<Expression> selectExpressionsQuadraticUpper() {

        //        final List<Expression> retVal = new ArrayList<Expression>();
        //
        //        for (final Expression tmpExpression : myExpressions.values()) {
        //            if (tmpExpression.isUpperConstraint() && tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
        //                retVal.add(tmpExpression);
        //            }
        //        }
        //
        //        //Collections.sort(retVal, CE);
        //        return Collections.unmodifiableList(retVal);

        return this.constraints().filter((final Expression c) -> c.isUpperConstraint() && c.isAnyQuadraticFactorNonZero()).collect(Collectors.toList());
    }

    /**
     * @deprecated v39 Use {@link #bounds()} instead
     */
    @Deprecated
    public List<Variable> selectVariablesFreeLower() {

        //        final List<Variable> retVal = new ArrayList<Variable>();
        //
        //        for (final Variable tmpVariable : this.getFreeVariables()) {
        //            if (tmpVariable.isLowerConstraint()) {
        //                retVal.add(tmpVariable);
        //            }
        //        }
        //
        //        return Collections.unmodifiableList(retVal);

        return this.bounds().filter((final Variable c) -> c.isLowerConstraint()).collect(Collectors.toList());
    }

    /**
     * @deprecated v39 Use {@link #bounds()} instead
     */
    @Deprecated
    public List<Variable> selectVariablesFreeUpper() {

        //        final List<Variable> retVal = new ArrayList<Variable>();
        //
        //        for (final Variable tmpVariable : this.getFreeVariables()) {
        //            if (tmpVariable.isUpperConstraint()) {
        //                retVal.add(tmpVariable);
        //            }
        //        }
        //
        //        return Collections.unmodifiableList(retVal);

        return this.bounds().filter((final Variable c) -> c.isUpperConstraint()).collect(Collectors.toList());
    }

    /**
     * @deprecated v39 Use {@link #bounds()} instead
     */
    @Deprecated
    public List<Variable> selectVariablesNegativeLower() {

        //        final List<Variable> retVal = new ArrayList<Variable>();
        //
        //        for (final Variable tmpVariable : this.getNegativeVariables()) {
        //            if (tmpVariable.isLowerConstraint() && (tmpVariable.getLowerLimit().signum() == -1)) {
        //                retVal.add(tmpVariable);
        //            }
        //        }
        //
        //        return Collections.unmodifiableList(retVal);

        return this.bounds().filter((final Variable c) -> c.isNegative() && c.isLowerConstraint() && (c.getLowerLimit().signum() < 0))
                .collect(Collectors.toList());
    }

    /**
     * @deprecated v39 Use {@link #bounds()} instead
     */
    @Deprecated
    public List<Variable> selectVariablesNegativeUpper() {

        //        final List<Variable> retVal = new ArrayList<Variable>();
        //
        //        for (final Variable tmpVariable : this.getNegativeVariables()) {
        //            if (tmpVariable.isUpperConstraint() && (tmpVariable.getUpperLimit().signum() == -1)) {
        //                retVal.add(tmpVariable);
        //            }
        //        }
        //
        //        return Collections.unmodifiableList(retVal);

        return this.bounds().filter((final Variable c) -> c.isNegative() && c.isUpperConstraint() && (c.getUpperLimit().signum() < 0))
                .collect(Collectors.toList());
    }

    /**
     * @deprecated v39 Use {@link #bounds()} instead
     */
    @Deprecated
    public List<Variable> selectVariablesPositiveLower() {

        //        final List<Variable> retVal = new ArrayList<Variable>();
        //
        //        for (final Variable tmpVariable : this.getPositiveVariables()) {
        //            if (tmpVariable.isLowerConstraint() && (tmpVariable.getLowerLimit().signum() == 1)) {
        //                retVal.add(tmpVariable);
        //            }
        //        }
        //
        //        return Collections.unmodifiableList(retVal);

        return this.bounds().filter((final Variable c) -> c.isPositive() && c.isLowerConstraint() && (c.getLowerLimit().signum() > 0))
                .collect(Collectors.toList());
    }

    /**
     * @deprecated v39 Use {@link #bounds()} instead
     */
    @Deprecated
    public List<Variable> selectVariablesPositiveUpper() {

        //        final List<Variable> retVal = new ArrayList<Variable>();
        //
        //        for (final Variable tmpVariable : this.getPositiveVariables()) {
        //            if (tmpVariable.isUpperConstraint() && (tmpVariable.getUpperLimit().signum() == 1)) {
        //                retVal.add(tmpVariable);
        //            }
        //        }
        //
        //        return Collections.unmodifiableList(retVal);

        // return this.bounds().filter((final Variable c) -> c.isUpperConstraint() && c.isPositive()).collect(Collectors.toList());

        return this.bounds().filter((final Variable c) -> c.isPositive() && c.isUpperConstraint() && (c.getUpperLimit().signum() > 0))
                .collect(Collectors.toList());
    }

    /**
     * <p>
     * The general recommendation is to NOT call this method directly. Instead you should use/call
     * {@link #maximise()} or {@link #minimise()}.
     * </P>
     * <p>
     * The primary use case for this method is as a callback method for solvers that iteratively modifies the
     * model and solves at each iteration point.
     * </P>
     * <p>
     * With direct usage of this method:
     * </P>
     * <ul>
     * <li>Maximisation/Minimisation is undefined (you don't know which it is)</li>
     * <li>The solution is not written back to the model</li>
     * <li>The solution is not validated by the model</li>
     * </ul>
     */
    public Optimisation.Result solve(final Optimisation.Result initialSolution) {

        Optimisation.Result retVal = null;

        this.presolve();

        if (this.isInfeasible()) {

            final Optimisation.Result tmpSolution = this.getVariableValues();

            retVal = new Optimisation.Result(State.INFEASIBLE, tmpSolution);

        } else if (this.isFixed()) {

            final Optimisation.Result tmpSolution = this.getVariableValues();

            if (tmpSolution.getState().isFeasible()) {

                retVal = new Result(State.DISTINCT, tmpSolution);

            } else {

                retVal = new Result(State.INVALID, tmpSolution);
            }

        } else {

            // this.flushCaches();

            final Integration<?> tmpIntegration = this.getIntegration();
            final Solver tmpSolver = tmpIntegration.build(this);
            retVal = tmpIntegration.toSolverState(initialSolution, this);
            retVal = tmpSolver.solve(retVal);
            retVal = tmpIntegration.toModelState(retVal, this);

            tmpSolver.dispose();
        }

        return retVal;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder(START_END);

        for (final Variable tmpVariable : myVariables) {
            tmpVariable.appendToString(retVal);
            retVal.append(NEW_LINE);
        }

        for (final Expression tmpExpression : myExpressions.values()) {
            // if ((tmpExpression.isConstraint() && !tmpExpression.isRedundant()) || tmpExpression.isObjective()) {
            tmpExpression.appendToString(retVal, this.getVariableValues());
            retVal.append(NEW_LINE);
            // }
        }

        return retVal.append(START_END).toString();
    }

    public boolean validate() {

        boolean retVal = true;

        for (final Variable tmpVariable : myVariables) {
            retVal &= tmpVariable.validate(options.debug_appender);
        }

        for (final Expression tmpExpression : myExpressions.values()) {
            retVal &= tmpExpression.validate(options.debug_appender);
        }

        return retVal;
    }

    public boolean validate(final Access1D<BigDecimal> solution) {
        return this.validate(solution, options.slack);
    }

    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context) {

        final int tmpSize = myVariables.size();

        boolean retVal = tmpSize == solution.count();

        for (int i = 0; retVal && (i < tmpSize); i++) {
            retVal &= myVariables.get(i).validate(solution.get(i), context, options.debug_appender);
        }

        for (final Expression tmpExpression : myExpressions.values()) {
            retVal &= retVal && tmpExpression.validate(solution, context, options.debug_appender);
        }

        return retVal;
    }

    public boolean validate(final NumberContext context) {
        return this.getVariableValues(context).getState().isFeasible();
    }

    /**
     * @return A stream of variables that are not fixed
     */
    public Stream<Variable> variables() {
        return myVariables.stream().filter((final Variable v) -> (!v.isEqualityConstraint()));
    }

    private Set<IntIndex> identifyFixedVariables() {

        final int tmpLength = myVariables.size();

        for (int i = 0; i < tmpLength; i++) {

            final Variable tmpVariable = myVariables.get(i);

            if (tmpVariable.isEqualityConstraint()) {

                tmpVariable.setValue(tmpVariable.getLowerLimit());
                myFixedVariables.add(tmpVariable.getIndex());

            }
        }

        return this.getFixedVariables();
    }

    private Set<IntIndex> categoriseVariables() {

        final int tmpLength = myVariables.size();

        myFreeVariables.clear();
        myFreeIndices = new int[tmpLength];
        Arrays.fill(myFreeIndices, -1);

        myPositiveVariables.clear();
        myPositiveIndices = new int[tmpLength];
        Arrays.fill(myPositiveIndices, -1);

        myNegativeVariables.clear();
        myNegativeIndices = new int[tmpLength];
        Arrays.fill(myNegativeIndices, -1);

        myIntegerVariables.clear();
        myIntegerIndices = new int[tmpLength];
        Arrays.fill(myIntegerIndices, -1);

        for (int i = 0; i < tmpLength; i++) {

            final Variable tmpVariable = myVariables.get(i);

            if (tmpVariable.isEqualityConstraint()) {

                tmpVariable.setValue(tmpVariable.getLowerLimit());
                myFixedVariables.add(tmpVariable.getIndex());

            } else {

                myFreeVariables.add(tmpVariable);
                myFreeIndices[i] = myFreeVariables.size() - 1;

                if (!tmpVariable.isUpperLimitSet() || (tmpVariable.getUpperLimit().signum() == 1)) {
                    myPositiveVariables.add(tmpVariable);
                    myPositiveIndices[i] = myPositiveVariables.size() - 1;
                }

                if (!tmpVariable.isLowerLimitSet() || (tmpVariable.getLowerLimit().signum() == -1)) {
                    myNegativeVariables.add(tmpVariable);
                    myNegativeIndices[i] = myNegativeVariables.size() - 1;
                }

                if (tmpVariable.isInteger()) {
                    myIntegerVariables.add(tmpVariable);
                    myIntegerIndices[i] = myIntegerVariables.size() - 1;
                }
            }
        }

        return this.getFixedVariables();
    }

    private Optimisation.Result handleResult(final Result solverResult) {

        final NumberContext tmpSolutionContext = options.solution;

        final int tmpSize = myVariables.size();
        for (int i = 0; i < tmpSize; i++) {
            final Variable tmpVariable = myVariables.get(i);
            if (!myFixedVariables.contains(tmpVariable.getIndex())) {
                tmpVariable.setValue(tmpSolutionContext.enforce(solverResult.get(i)));
            }
        }

        final Access1D<BigDecimal> tmpSolution = this.getVariableValues();
        final Optimisation.State tmpState = solverResult.getState();
        final double tmpValue = this.getObjectiveExpression().evaluate(tmpSolution).doubleValue();

        if (options.validate) {
            // TODO && this.validate(tmpSolution, options.slack)
        }

        return new Optimisation.Result(tmpState, tmpValue, tmpSolution);
    }

    protected void flushCaches() {

        myFreeVariables.clear();
        myFreeIndices = null;

        myPositiveVariables.clear();
        myPositiveIndices = null;

        myNegativeVariables.clear();
        myNegativeIndices = null;

        myIntegerVariables.clear();
        myIntegerIndices = null;
    }

    ExpressionsBasedModel.Integration<?> getIntegration() {

        ExpressionsBasedModel.Integration<?> retVal = null;

        for (final ExpressionsBasedModel.Integration<?> tmpIntegration : INTEGRATIONS) {
            if (tmpIntegration.isCapable(this)) {
                retVal = tmpIntegration;
                break;
            }
        }

        if (retVal == null) {
            if (this.isAnyVariableInteger()) {
                retVal = new ExpressionsBasedIntegerIntegration();
            } else if (this.isAnyExpressionQuadratic()) {
                retVal = new ExpressionsBasedConvexIntegration();
            } else {
                retVal = new ExpressionsBasedLinearIntegration();
            }
        }

        return retVal;
    }

    boolean isFixed() {
        return myFixedVariables.size() == myVariables.size();
    }

    boolean isInfeasible() {
        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isInfeasible()) {
                return true;
            }
        }
        return false;
    }

    final void presolve() {

        boolean tmpNeedToRepeat = false;

        do {

            final Set<IntIndex> tmpFixedVariables = this.identifyFixedVariables();
            tmpNeedToRepeat = false;

            for (final Expression tmpExpr : this.getExpressions()) {
                if (!tmpNeedToRepeat && tmpExpr.isConstraint() && !tmpExpr.isInfeasible() && !tmpExpr.isRedundant() && (tmpExpr.countQuadraticFactors() == 0)) {
                    for (final Presolver tmpPreS : PRESOLVERS) {
                        tmpNeedToRepeat |= tmpPreS.simplify(tmpExpr, tmpFixedVariables);
                    }
                }
            }

        } while (tmpNeedToRepeat);

        this.categoriseVariables();
    }

}
