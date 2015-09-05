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

import java.math.BigDecimal;
import java.util.*;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.BasicLogger.GenericAppender;
import org.ojalgo.netio.CharacterRing;
import org.ojalgo.optimisation.Expression.Index;
import org.ojalgo.optimisation.Expression.RowColumn;
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

            for (final Index tmpFixed : model.getFixedVariables()) {
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

    public static final List<ExpressionsBasedModel.Integration<?>> INTEGRATIONS = new ArrayList<>();

    private static final String NEW_LINE = "\n";

    private static final String OBJ_FUNC_AS_CONSTR_KEY = UUID.randomUUID().toString();
    private static final String OBJECTIVE = "Generated/Aggregated Objective";
    private static final String START_END = "############################################\n";
    static final Comparator<Expression> CE = new Comparator<Expression>() {

        public int compare(final Expression o1, final Expression o2) {
            return Integer.compare(o2.countLinearFactors(), o1.countLinearFactors());
        }

    };

    static final void presolve(final ExpressionsBasedModel model) {

        final Set<Index> tmpFixedVariables = model.getFixedVariables();

        int iters = 0;
        boolean stillSimplifying = true;
        while (stillSimplifying) {
            ++iters;
            if (iters % 100 == 0) {
                BasicLogger.debug("Done {} iterations of presolving", iters);
            }

            stillSimplifying = false;

            for (final Expression tmpExpression : model.getExpressions()) {

                final boolean tmpConstraint = tmpExpression.isConstraint();
                final boolean tmpInfeasible = tmpExpression.isInfeasible();
                final boolean tmpRedundant = tmpExpression.isRedundant();

                if (tmpConstraint && !tmpInfeasible && !tmpRedundant && tmpExpression.simplify(tmpFixedVariables)) {

                    BasicLogger.debug("Following expression is now redundant: {}", tmpExpression);
                    stillSimplifying = true;
                    break; // Restart the process after removing something.
                }
            }
        }
    }

    private transient BasicLogger.Appender myAppender = null;

    private final CharacterRing myBuffer = new CharacterRing();
    private boolean myDoPresolve = true;
    private final HashMap<String, Expression> myExpressions = new HashMap<String, Expression>();
    private final HashSet<Index> myFixedVariables = new HashSet<Index>();
    private transient int[] myFreeIndices = null;
    private transient List<Variable> myFreeVariables = null;
    private transient int[] myIntegerIndices = null;
    private transient List<Variable> myIntegerVariables = null;
    private transient int[] myNegativeIndices = null;
    private transient List<Variable> myNegativeVariables = null;
    private transient Expression myObjectiveExpression = null;
    private transient MultiaryFunction.TwiceDifferentiable<Double> myObjectiveFunction = null;
    private transient int[] myPositiveIndices = null;
    private transient List<Variable> myPositiveVariables = null;
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

    public ExpressionsBasedModel(final Variable[] variables) {

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

            myObjectiveExpression = modelToCopy.getObjectiveExpression();
            myObjectiveFunction = modelToCopy.getObjectiveFunction();

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
            variable.setIndex(new Expression.Index(myVariables.size() - 1));
        }
    }

    public void addVariables(final Collection<? extends Variable> variables) {
        for (final Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }
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
    public void destroy() {

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

    public Set<Index> getFixedVariables() {
        return Collections.unmodifiableSet(myFixedVariables);
    }

    /**
     * @return A list of the variables that are not fixed at a specific value
     */
    public List<Variable> getFreeVariables() {

        if (myFreeVariables == null) {
            this.categoriseVariables();
        }

        return myFreeVariables;
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and are marked as integer
     *         variables
     */
    public List<Variable> getIntegerVariables() {

        if (myIntegerVariables == null) {
            this.categoriseVariables();
        }

        return myIntegerVariables;
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include negative
     *         values
     */
    public List<Variable> getNegativeVariables() {

        if (myNegativeVariables == null) {
            this.categoriseVariables();
        }

        return myNegativeVariables;
    }

    /**
     * @return The full objective function
     */
    public Expression getObjectiveExpression() {

        if (myObjectiveExpression == null) {

            myObjectiveExpression = new Expression(OBJECTIVE, this);

            Variable tmpVariable;
            for (int i = 0; i < myVariables.size(); i++) {
                tmpVariable = myVariables.get(i);

                if (tmpVariable.isObjective()) {
                    myObjectiveExpression.setLinearFactor(i, tmpVariable.getContributionWeight());
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
                        for (final Expression.Index tmpKey : tmpExpression.getLinearFactorKeys()) {
                            tmpOldVal = myObjectiveExpression.getLinearFactor(tmpKey);
                            tmpDiff = tmpExpression.getLinearFactor(tmpKey);
                            tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                            myObjectiveExpression.setLinearFactor(tmpKey, tmpNewVal);
                        }
                    }

                    if (tmpExpression.isAnyQuadraticFactorNonZero()) {
                        for (final Expression.RowColumn tmpKey : tmpExpression.getQuadraticFactorKeys()) {
                            tmpOldVal = myObjectiveExpression.getQuadraticFactor(tmpKey);
                            tmpDiff = tmpExpression.getQuadraticFactor(tmpKey);
                            tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                            myObjectiveExpression.setQuadraticFactor(tmpKey, tmpNewVal);
                        }
                    }
                }
            }
        }

        return myObjectiveExpression;
    }

    /**
     * Parameters corresponding to fixed variables may or may not be included in the returned expression - the
     * receiver is expected to not care about those parameters. Parameters corresponding to bilinear
     * variables, where one is fixed and the other is not, must transform the expression correspondingly.
     *
     * @param fixed A set of (by the presolver) fixed variables
     * @return The reduced/modified objective function
     */
    public Expression getObjectiveExpression(final Set<Index> fixed) {

        final Expression tmpFull = this.getObjectiveExpression();

        if ((fixed.size() > 0) && tmpFull.isAnyQuadraticFactorNonZero()) {

            final Expression tmpMod = new Expression(OBJECTIVE, this);

            for (final Index tmpKey : tmpFull.getLinearFactorKeys()) {
                if (!fixed.contains(tmpKey)) {
                    tmpMod.set(tmpKey, tmpFull.get(tmpKey));
                }
            }

            for (final RowColumn tmpKey : tmpFull.getQuadraticFactorKeys()) {

                final int tmpRow = this.indexOfFreeVariable(tmpKey.row);
                final int tmpColumn = this.indexOfFreeVariable(tmpKey.column);

                if ((tmpRow >= 0) && (tmpColumn >= 0)) {

                    tmpMod.set(tmpKey, tmpFull.get(tmpKey));

                } else if ((tmpRow < 0) && (tmpColumn >= 0)) {

                    final Index tmpColKey = new Index(tmpColumn);

                    tmpMod.add(tmpColKey, this.getVariable(tmpKey.row).getValue().multiply(tmpFull.get(tmpKey)));

                } else if ((tmpColumn < 0) && (tmpRow >= 0)) {

                    final Index tmpRowKey = new Index(tmpRow);

                    tmpMod.add(tmpRowKey, this.getVariable(tmpKey.column).getValue().multiply(tmpFull.get(tmpKey)));

                } else {

                    // Both variables fixed
                }
            }

            return tmpMod;

        } else {

            return tmpFull;
        }
    }

    public MultiaryFunction.TwiceDifferentiable<Double> getObjectiveFunction() {

        if (myObjectiveFunction == null) {
            myObjectiveFunction = this.getObjectiveExpression().toFunction();
        }

        return myObjectiveFunction;
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include positive
     *         values and/or zero
     */
    public List<Variable> getPositiveVariables() {

        if (myPositiveVariables == null) {
            this.categoriseVariables();
        }

        return myPositiveVariables;
    }

    public String getValidationMessages() {
        return myBuffer.toString();
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
                    retSolution.set(i, BigFunction.DIVIDE.invoke(tmpVariable.getLowerLimit().add(tmpVariable.getUpperLimit()), BigMath.TWO));
                } else if (tmpVariable.isLowerLimitSet()) {
                    retSolution.set(i, tmpVariable.getLowerLimit());
                } else if (tmpVariable.isUpperLimitSet()) {
                    retSolution.set(i, tmpVariable.getUpperLimit());
                } else {
                    retSolution.set(i, BigMath.ZERO);
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

    public int indexOfFreeVariable(final Index variableIndex) {
        return this.indexOfFreeVariable(variableIndex.index);
    }

    /**
     * @param globalIndex General, global, variable index
     * @return Local index among the positive variables. -1 indicates the variable is not a positive variable.
     */
    public int indexOfFreeVariable(final int globalIndex) {
        return myFreeIndices[globalIndex];
    }

    public int indexOfFreeVariable(final Variable variable) {
        return this.indexOfFreeVariable(this.indexOf(variable));
    }

    /**
     * @param index General, global, variable index
     * @return Local index among the integer variables. -1 indicates the variable is not an integer variable.
     */
    public int indexOfIntegerVariable(final int index) {
        return myIntegerIndices[index];
    }

    public int indexOfIntegerVariable(final Variable variable) {
        return this.indexOfIntegerVariable(variable.getIndex().index);
    }

    /**
     * @param index General, global, variable index
     * @return Local index among the negative variables. -1 indicates the variable is not a negative variable.
     */
    public int indexOfNegativeVariable(final int index) {
        return myNegativeIndices[index];
    }

    public int indexOfNegativeVariable(final Variable variable) {
        return this.indexOfNegativeVariable(this.indexOf(variable));
    }

    /**
     * @param index General, global, variable index
     * @return Local index among the positive variables. -1 indicates the variable is not a positive variable.
     */
    public int indexOfPositiveVariable(final int index) {
        return myPositiveIndices[index];
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

    /**
     * @deprecated v38 Temporary feature. Presolving will be refactored
     */
    @Deprecated
    public boolean isDoPresolve() {
        return myDoPresolve;
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

    public ExpressionsBasedModel relax(final boolean inPlace) {

        final ExpressionsBasedModel retVal = inPlace ? this : new ExpressionsBasedModel(this, true);

        for (final Variable tmpVariable : retVal.getVariables()) {
            tmpVariable.relax();
        }

        return retVal;
    }

    /**
     * Any constrained non-redundant expression
     */
    public List<Expression> selectExpressions() {

        final List<Expression> retVal = new ArrayList<Expression>();

        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isConstraint() && !tmpExpression.isRedundant()) {
                retVal.add(tmpExpression);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    /**
     * Linear equality constrained expressions.
     */
    public List<Expression> selectExpressionsLinearEquality() {

        final List<Expression> retVal = new ArrayList<Expression>();

        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isEqualityConstraint() && !tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
                retVal.add(tmpExpression);
            }
        }

        //Collections.sort(retVal, CE);
        return Collections.unmodifiableList(retVal);
    }

    /**
     * Linear lower constrained expressions.
     */
    public List<Expression> selectExpressionsLinearLower() {

        final List<Expression> retVal = new ArrayList<Expression>();

        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isLowerConstraint() && !tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
                retVal.add(tmpExpression);
            }
        }

        //Collections.sort(retVal, CE);
        return Collections.unmodifiableList(retVal);
    }

    /**
     * Linear upper constrained expressions.
     */
    public List<Expression> selectExpressionsLinearUpper() {

        final List<Expression> retVal = new ArrayList<Expression>();

        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isUpperConstraint() && !tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
                retVal.add(tmpExpression);
            }
        }

        //Collections.sort(retVal, CE);
        return Collections.unmodifiableList(retVal);
    }

    /**
     * Quadratic (and/or compound) equality constrained expressions.
     */
    public List<Expression> selectExpressionsQuadraticEquality() {

        final List<Expression> retVal = new ArrayList<Expression>();

        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isEqualityConstraint() && tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
                retVal.add(tmpExpression);
            }
        }

        //Collections.sort(retVal, CE);
        return Collections.unmodifiableList(retVal);
    }

    /**
     * Quadratic (and/or compound) lower constrained expressions.
     */
    public List<Expression> selectExpressionsQuadraticLower() {

        final List<Expression> retVal = new ArrayList<Expression>();

        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isLowerConstraint() && tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
                retVal.add(tmpExpression);
            }
        }

        //Collections.sort(retVal, CE);
        return Collections.unmodifiableList(retVal);
    }

    /**
     * Quadratic (and/or compound) upper constrained expressions.
     */
    public List<Expression> selectExpressionsQuadraticUpper() {

        final List<Expression> retVal = new ArrayList<Expression>();

        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isUpperConstraint() && tmpExpression.isAnyQuadraticFactorNonZero() && !tmpExpression.isRedundant()) {
                retVal.add(tmpExpression);
            }
        }

        //Collections.sort(retVal, CE);
        return Collections.unmodifiableList(retVal);
    }

    public List<Variable> selectVariablesFreeLower() {

        final List<Variable> retVal = new ArrayList<Variable>();

        for (final Variable tmpVariable : this.getFreeVariables()) {
            if (tmpVariable.isLowerConstraint()) {
                retVal.add(tmpVariable);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public List<Variable> selectVariablesFreeUpper() {

        final List<Variable> retVal = new ArrayList<Variable>();

        for (final Variable tmpVariable : this.getFreeVariables()) {
            if (tmpVariable.isUpperConstraint()) {
                retVal.add(tmpVariable);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public List<Variable> selectVariablesNegativeLower() {

        final List<Variable> retVal = new ArrayList<Variable>();

        for (final Variable tmpVariable : this.getNegativeVariables()) {
            if (tmpVariable.isLowerConstraint() && (tmpVariable.getLowerLimit().signum() == -1)) {
                retVal.add(tmpVariable);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public List<Variable> selectVariablesNegativeUpper() {

        final List<Variable> retVal = new ArrayList<Variable>();

        for (final Variable tmpVariable : this.getNegativeVariables()) {
            if (tmpVariable.isUpperConstraint() && (tmpVariable.getUpperLimit().signum() == -1)) {
                retVal.add(tmpVariable);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public List<Variable> selectVariablesPositiveLower() {

        final List<Variable> retVal = new ArrayList<Variable>();

        for (final Variable tmpVariable : this.getPositiveVariables()) {
            if (tmpVariable.isLowerConstraint() && (tmpVariable.getLowerLimit().signum() == 1)) {
                retVal.add(tmpVariable);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public List<Variable> selectVariablesPositiveUpper() {

        final List<Variable> retVal = new ArrayList<Variable>();

        for (final Variable tmpVariable : this.getPositiveVariables()) {
            if (tmpVariable.isUpperConstraint() && (tmpVariable.getUpperLimit().signum() == 1)) {
                retVal.add(tmpVariable);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    /**
     * @deprecated v38 Temporary feature. Presolving will be refactored
     */
    @Deprecated
    public void setDoPresolve(final boolean doPresolve) {
        myDoPresolve = doPresolve;
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

            this.flushCaches();

            final Integration<?> tmpIntegration = this.getIntegration();
            final Solver tmpSolver = tmpIntegration.build(this);
            retVal = tmpIntegration.toSolverState(initialSolution, this);
            retVal = tmpSolver.solve(retVal);
            retVal = tmpIntegration.toModelState(retVal, this);
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
            if ((tmpExpression.isConstraint() && !tmpExpression.isRedundant()) || tmpExpression.isObjective()) {
                tmpExpression.appendToString(retVal, this.getVariableValues());
                retVal.append(NEW_LINE);
            }
        }

        return retVal.append(START_END).toString();
    }

    public boolean validate() {

        boolean retVal = true;

        for (final Variable tmpVariable : myVariables) {
            retVal &= tmpVariable.validate(this.appender());
        }

        for (final Expression tmpExpression : myExpressions.values()) {
            retVal &= tmpExpression.validate(this.appender());
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
            retVal &= myVariables.get(i).validate(solution.get(i), context, this.appender());
        }

        for (final Expression tmpExpression : myExpressions.values()) {
            retVal &= retVal && tmpExpression.validate(solution, context, this.appender());
        }

        return retVal;
    }

    public boolean validate(final NumberContext context) {
        return this.getVariableValues(context).getState().isFeasible();
    }

    private void categoriseVariables() {

        final int tmpLength = myVariables.size();

        myFreeVariables = new ArrayList<Variable>();
        myFreeIndices = new int[tmpLength];
        Arrays.fill(myFreeIndices, -1);

        myPositiveVariables = new ArrayList<Variable>();
        myPositiveIndices = new int[tmpLength];
        Arrays.fill(myPositiveIndices, -1);

        myNegativeVariables = new ArrayList<Variable>();
        myNegativeIndices = new int[tmpLength];
        Arrays.fill(myNegativeIndices, -1);

        myIntegerVariables = new ArrayList<Variable>();
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

        myFreeVariables = Collections.unmodifiableList(myFreeVariables);
        myPositiveVariables = Collections.unmodifiableList(myPositiveVariables);
        myNegativeVariables = Collections.unmodifiableList(myNegativeVariables);
        myIntegerVariables = Collections.unmodifiableList(myIntegerVariables);
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

        if (!myWorkCopy) {
            myObjectiveExpression = null;
            myObjectiveFunction = null;
        }

        myFreeVariables = null;
        myFreeIndices = null;

        myIntegerVariables = null;
        myIntegerIndices = null;

        myNegativeVariables = null;
        myNegativeIndices = null;

        myPositiveVariables = null;
        myPositiveIndices = null;
    }

    boolean addFixedVariable(final Index index) {
        return myFixedVariables.add(index);
    }

    BasicLogger.Appender appender() {
        if (myAppender == null) {
            myAppender = new GenericAppender(myBuffer);
        }
        return myAppender;
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

        this.categoriseVariables();

        if (this.isDoPresolve()) {
            ExpressionsBasedModel.presolve(this);
        }

    }

}
