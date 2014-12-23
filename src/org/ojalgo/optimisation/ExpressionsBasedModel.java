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
package org.ojalgo.optimisation;

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.util.*;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.BasicLogger.GenericAppender;
import org.ojalgo.netio.CharacterRing;
import org.ojalgo.optimisation.Expression.Index;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Lets you construct optimisation problems by combining (mathematical) expressions in terms of variables. Each
 * expression or variable can be a constraint and/or contribute to the objective function. An expression or variable is
 * turned into a constraint by setting a lower and/or upper limit. Use {@linkplain ModelEntity#lower(BigDecimal)},
 * {@linkplain ModelEntity#upper(BigDecimal)} or {@linkplain ModelEntity#level(BigDecimal)}. An expression or variable
 * is made part of (contributing to) the objective function by setting a contribution weight. Use
 * {@linkplain ModelEntity#weight(BigDecimal)}.
 * </p>
 * <p>
 * You may think of variables as simple (the simplest possible) expressions, and of expressions as weighted combinations
 * of variables. They are both model entities and it is as such they can be turned into constraints and set to
 * contribute to the objective function. Alternatively you may choose to disregard the fact that variables are model
 * entities and simply treat them as index values. In this case everything (constraints and objective) needs to be
 * defined using expressions.
 * </p>
 * <p>
 * Basic instructions:
 * <ol>
 * <li>Define (create) a set of variables. Set contribution weights and lower/upper limits as needed.</li>
 * <li>Create a model using that set of variables.</li>
 * <li>Add expressions to the model. The model is the expression factory. Set contribution weights and lower/upper
 * limits as needed.</li>
 * <li>Solve your problem using either minimise() or maximise()</li>
 * </ol>
 * </p>
 * <p>
 * This class actually does something for you compared to using the solvers directly:
 * <ol>
 * <li>You can model your problems without worrying about specific solver requirements.</li>
 * <li>It knows which solver to use.</li>
 * <li>It knows how to use that solver.</li>
 * <li>It has a presolver that tries to simplify the problem before invoking a solver (sometimes it turns out there is
 * no need to invoke a solver at all).</li>
 * <li>When/if needed it scales problem parameters, before creating solver specific data structures, to minimize
 * numerical problems in the solvers.</li>
 * <li>It's the only way to access the integer solver.</li>
 * </ol>
 * </p>
 * <p>
 * There are some restrictions on the models you can create:
 * <ul>
 * <li>No quadratic constraints</li>
 * </ul>
 * The plan is that future versions should not have any restrictions like these.
 * </p>
 *
 * @author apete
 */
public final class ExpressionsBasedModel extends AbstractModel<GenericSolver> {

    public static abstract class Integration<S extends Optimisation.Solver> implements Optimisation.Integration<ExpressionsBasedModel, S> {

    }

    public static ExpressionsBasedModel make(final MathProgSysModel mps) {

        final MathProgSysModel.Column[] tmpActCols = mps.getActivatorVariableColumns();
        final MathProgSysModel.Column[] tmpNegCols = mps.getNegativeVariableColumns();
        final MathProgSysModel.Column[] tmpPosCols = mps.getPositiveVariableColumns();
        final MathProgSysModel.Row[] tmpAllRows = mps.getExpressionRows();

        Arrays.sort(tmpActCols);
        Arrays.sort(tmpNegCols);
        Arrays.sort(tmpPosCols);
        Arrays.sort(tmpAllRows);

        final int tmpCountActCols = tmpActCols.length;
        final int tmpCountNegCols = tmpNegCols.length;
        final int tmpCountPosCols = tmpPosCols.length;
        final int tmpCountAllRows = tmpAllRows.length;

        // Define various local variables
        MathProgSysModel.Row tmpRow;
        MathProgSysModel.Column tmpCol;
        Variable tmpVar;
        Expression tmpExpr;
        int tmpIndex;

        // Create the LinearModel variables
        final Variable[] tmpVariables = new Variable[tmpCountActCols + tmpCountNegCols + tmpCountPosCols];
        for (int i = 0; i < tmpCountActCols; i++) {

            tmpCol = tmpActCols[i];
            tmpVar = Variable.makeBinary(tmpCol.getNameForActivator());

            tmpVariables[i] = tmpVar;
        }
        for (int i = 0; i < tmpCountNegCols; i++) {

            tmpCol = tmpNegCols[i];
            tmpVar = new Variable(tmpCol.getNameForNegativePart());

            final BigDecimal tmpLowerLimit = (tmpCol.isUpperLimitSet() && !tmpCol.needsActivator()) ? tmpCol.getUpperLimit().negate().max(ZERO) : ZERO;
            final BigDecimal tmpUpperLimit = tmpCol.isLowerLimitSet() ? tmpCol.getLowerLimit().negate() : null;

            tmpVar.lower(tmpLowerLimit).upper(tmpUpperLimit).integer(tmpCol.isInteger());

            tmpVariables[tmpCountActCols + i] = tmpVar;
        }
        for (int i = 0; i < tmpCountPosCols; i++) {

            tmpCol = tmpPosCols[i];
            tmpVar = new Variable(tmpCol.getNameForPositivePart());

            final BigDecimal tmpLowerLimit = (tmpCol.isLowerLimitSet() && !tmpCol.needsActivator()) ? tmpCol.getLowerLimit().max(ZERO) : ZERO;
            final BigDecimal tmpUpperLimit = tmpCol.getUpperLimit();

            tmpVar.lower(tmpLowerLimit).upper(tmpUpperLimit).integer(tmpCol.isInteger());

            tmpVariables[tmpCountActCols + tmpCountNegCols + i] = tmpVar;
        }

        // Instantiate the LinearModel
        final ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);

        final Expression[] tmpExpressions = new Expression[tmpCountAllRows];
        final String[] tmpExpressionNames = new String[tmpCountAllRows];

        for (int i = 0; i < tmpCountAllRows; i++) {
            tmpRow = tmpAllRows[i];
            tmpExpr = retVal.addExpression(tmpRow.getName());
            tmpExpr.lower(tmpRow.getLowerLimit());
            tmpExpr.upper(tmpRow.getUpperLimit());
            tmpExpr.weight(tmpRow.getContributionWeight());
            tmpExpressions[i] = tmpExpr;
            tmpExpressionNames[i] = tmpExpr.getName();
        }

        final Expression[] tmpActExpressions = new Expression[tmpCountActCols];
        final String[] tmpActExpressionNames = new String[tmpCountActCols];

        for (int i = 0; i < tmpCountActCols; i++) {
            tmpCol = tmpActCols[i];
            tmpExpr = retVal.addExpression(tmpCol.getName());
            tmpExpr.lower(ZERO);
            tmpActExpressions[i] = tmpExpr;
            tmpActExpressionNames[i] = tmpExpr.getName();

            tmpIndex = Arrays.binarySearch(tmpPosCols, tmpCol);
            if (tmpIndex != -1) {
                tmpExpr.setLinearFactor(i, tmpCol.getLowerLimit().negate());
                tmpExpr.setLinearFactor(tmpCountActCols + tmpCountNegCols + tmpIndex, ONE);
            }
        }

        for (int i = 0; i < tmpCountNegCols; i++) {
            tmpCol = tmpNegCols[i];
            tmpVar = tmpVariables[tmpCountActCols + i];
            for (final String tmpRowKey : tmpCol.getElementKeys()) {
                tmpIndex = Arrays.binarySearch(tmpExpressionNames, tmpRowKey);
                if (tmpIndex != -1) {
                    tmpExpressions[tmpIndex].setLinearFactor(tmpCountActCols + i, tmpCol.getRowValue(tmpRowKey).negate());
                }
            }
        }
        for (int i = 0; i < tmpCountPosCols; i++) {
            tmpCol = tmpPosCols[i];
            tmpVar = tmpVariables[tmpCountActCols + tmpCountNegCols + i];
            for (final String tmpRowKey : tmpCol.getElementKeys()) {
                tmpIndex = Arrays.binarySearch(tmpExpressionNames, tmpRowKey);
                if (tmpIndex != -1) {
                    tmpExpressions[tmpIndex].setLinearFactor(tmpCountActCols + tmpCountNegCols + i, tmpCol.getRowValue(tmpRowKey));
                }
            }
        }

        return retVal;
    }

    static final void presolve(final ExpressionsBasedModel model) {

        for (final Expression tmpExpression : model.getExpressions()) {
            if (tmpExpression.simplify()) {
                ExpressionsBasedModel.presolve(model);
                break;
            }
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
    private transient BasicLogger.Appender myAppender = null;

    private final CharacterRing myBuffer = new CharacterRing();

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

    @SuppressWarnings("unused")
    private ExpressionsBasedModel(final Options someOptions) {
        this();
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
     * @return A list of the variables that are not fixed at a specific value and are marked as integer variables
     */
    public List<Variable> getIntegerVariables() {

        if (myIntegerVariables == null) {
            this.categoriseVariables();
        }

        return myIntegerVariables;
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include negative values
     */
    public List<Variable> getNegativeVariables() {

        if (myNegativeVariables == null) {
            this.categoriseVariables();
        }

        return myNegativeVariables;
    }

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

    public MultiaryFunction.TwiceDifferentiable<Double> getObjectiveFunction() {

        if (myObjectiveFunction == null) {
            myObjectiveFunction = this.getObjectiveExpression().toFunction();
        }

        return myObjectiveFunction;
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include positive values
     *         and/or zero
     */
    public List<Variable> getPositiveVariables() {

        if (myPositiveVariables == null) {
            this.categoriseVariables();
        }

        return myPositiveVariables;
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
     * Null variable values are replaced with 0.0. If any variable value is null the state is set to INFEASIBLE even if
     * zero would actually be a feasible value. The objective function value is not calculated for infeasible variable
     * values.
     */
    public Optimisation.Result getVariableValues(final NumberContext validationContext) {

        final int tmpNumberOfVariables = myVariables.size();

        final Array1D<BigDecimal> tmpSolution = Array1D.BIG.makeZero(tmpNumberOfVariables);

        boolean tmpNoneNull = true;

        BigDecimal tmpVal;
        for (int i = 0; i < tmpNumberOfVariables; i++) {

            tmpVal = myVariables.get(i).getValue();

            if (tmpVal != null) {
                tmpSolution.set(i, tmpVal);
            } else {
                tmpSolution.set(i, BigMath.ZERO);
                tmpNoneNull = false;
            }
        }

        if (tmpNoneNull && this.validate(tmpSolution, validationContext)) {
            final double tmpValue = this.getObjectiveExpression().evaluate(tmpSolution).doubleValue();
            return new Optimisation.Result(State.FEASIBLE, tmpValue, tmpSolution);
        } else {
            return new Optimisation.Result(State.INFEASIBLE, Double.NaN, tmpSolution);
        }
    }

    public int indexOf(final Variable variable) {
        return variable.getIndex().index;
    }

    /**
     * @param index General, global, variable index
     * @return Local index among the positive variables. -1 indicates the variable is not a positive variable.
     */
    public int indexOfFreeVariable(final int index) {
        return myFreeIndices[index];
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

        final Result tmpSolverResult = this.solve();

        return this.handleResult(tmpSolverResult);
    }

    public Optimisation.Result minimise() {

        this.setMinimisation();

        final Result tmpSolverResult = this.solve();

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
     * The general recommendation is to NOT call this method directly. Instead you should use/call {@link #maximise()}
     * or {@link #minimise()}. If you do use this method you must first set {@link #setMinimisation()} or
     * {@link #setMaximisation()}. Also note that with this method the solver solution is not written back to the model
     * (or validated by the model).
     */
    public Optimisation.Result solve() {

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
            retVal = tmpSolver.solve();
            retVal = tmpIntegration.toModelState(this, retVal);
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
            tmpExpression.appendToString(retVal, this.getVariableValues());
            retVal.append(NEW_LINE);
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

    private ExpressionsBasedModel.Integration<?> getIntegration() {

        if (this.isAnyVariableInteger()) {
            return new ExpressionsBasedIntegerIntegration();
        } else if (this.isAnyExpressionQuadratic()) {
            return new ExpressionsBasedConvexIntegration();
        } else {
            return new ExpressionsBasedLinearIntegration();
        }
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

        ExpressionsBasedModel.presolve(this);
    }

}
