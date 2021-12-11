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

import static org.ojalgo.function.constant.BigMath.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.BasicLogger.Printer;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Lets you construct optimisation problems by combining (mathematical) expressions in terms of variables.
 * Each expression or variable can be a constraint and/or contribute to the objective function. An expression
 * or variable is turned into a constraint by setting a lower and/or upper limit. Use
 * {@linkplain Expression#lower(Comparable)}, {@linkplain Expression#upper(Comparable)} or
 * {@linkplain Expression#level(Comparable)}. An expression or variable is made part of (contributing to) the
 * objective function by setting a contribution weight. Use {@linkplain Expression#weight(Comparable)}.
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
 * Different solvers can be used, and ojAlgo comes with collection built in. The default built-in solvers can
 * handle anythimng you can model with a couple of restrictions:
 * </p>
 * <ul>
 * <li>No quadratic constraints (The plan is that future versions should not have this limitation.)</li>
 * <li>If you use quadratic expressions make sure they're convex. This is most likely a requirement even with
 * 3:d party solvers.</li>
 * </ul>
 *
 * @author apete
 */
public final class ExpressionsBasedModel extends AbstractModel {

    public enum FileFormat {
        MPS;
    }

    /**
     * {@link Optimisation.Solver}:s that should be usabale from {@link ExpressionsBasedModel} needs to
     * implement a subclass of this.
     *
     * @author apete
     */
    public static abstract class Integration<S extends Optimisation.Solver> implements Optimisation.Integration<ExpressionsBasedModel, S> {

        /**
         * @see org.ojalgo.optimisation.Optimisation.Integration#extractSolverState(org.ojalgo.optimisation.Optimisation.Model)
         */
        public final Result extractSolverState(final ExpressionsBasedModel model) {
            return this.toSolverState(model.getVariableValues(), model);
        }

        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            final int numbVariables = model.countVariables();

            if (this.isSolutionMapped()) {

                final List<Variable> freeVariables = model.getFreeVariables();
                final Set<IntIndex> fixedVariables = model.getFixedVariables();

                if (solverState.count() != freeVariables.size()) {
                    throw new IllegalStateException();
                }

                final Primitive64Array modelSolution = Primitive64Array.make(numbVariables);

                for (final IntIndex fixedIndex : fixedVariables) {
                    modelSolution.set(fixedIndex.index, model.getVariable(fixedIndex.index).getValue());
                }

                for (int f = 0; f < freeVariables.size(); f++) {
                    final int freeIndex = model.indexOf(freeVariables.get(f));
                    modelSolution.set(freeIndex, solverState.doubleValue(f));
                }

                return new Result(solverState.getState(), modelSolution);

            }
            if (solverState.count() != numbVariables) {
                throw new IllegalStateException();
            }

            return solverState;
        }

        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            if (!this.isSolutionMapped()) {

                return modelState;
            }
            final List<Variable> tmpFreeVariables = model.getFreeVariables();
            final int numbFreeVars = tmpFreeVariables.size();

            final Primitive64Array solverSolution = Primitive64Array.make(numbFreeVars);

            for (int i = 0; i < numbFreeVars; i++) {
                final Variable variable = tmpFreeVariables.get(i);
                final int modelIndex = model.indexOf(variable);
                solverSolution.set(i, modelState.doubleValue(modelIndex));
            }

            return new Result(modelState.getState(), solverSolution);
        }

        /**
         * @return The index with which one can reference parameters related to this variable in the solver.
         */
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            return model.indexOfFreeVariable(variable);
        }

        /**
         * @return true if the set of variables present in the solver is not precisely the same as in the
         *         model. If fixed variables are omitted or if variables are split into a positive and
         *         negative part, then this method must return true
         */
        protected abstract boolean isSolutionMapped();

    }

    public static final class Intermediate implements Optimisation.Solver {

        private boolean myInPlaceUpdatesOK = true;
        private transient ExpressionsBasedModel.Integration<?> myIntegration = null;
        private final ExpressionsBasedModel myModel;
        private transient Optimisation.Solver mySolver = null;

        Intermediate(final ExpressionsBasedModel model) {
            super();
            myModel = model;
            myIntegration = null;
            mySolver = null;
        }

        public void dispose() {

            Solver.super.dispose();

            this.reset();
        }

        public ExpressionsBasedModel getModel() {
            return myModel;
        }

        public Variable getVariable(final int globalIndex) {
            return myModel.getVariable(globalIndex);
        }

        public Variable getVariable(final IntIndex globalIndex) {
            return myModel.getVariable(globalIndex);
        }

        /**
         * Force re-generation of cached/transient data
         */
        public void reset() {

            if (mySolver != null) {
                mySolver.dispose();
                mySolver = null;
            }

            myIntegration = null;
        }

        public Optimisation.Result solve(final Optimisation.Result candidate) {

            if (mySolver == null && PRESOLVERS.size() > 0) {
                myModel.presolve();
            }

            if (myModel.isInfeasible()) {

                Optimisation.Result solution = candidate != null ? candidate : myModel.getVariableValues();

                return new Optimisation.Result(State.INFEASIBLE, solution);
            }

            if (myModel.isUnbounded()) {

                if (candidate != null && myModel.validate(candidate)) {
                    return new Optimisation.Result(State.UNBOUNDED, candidate);
                }

                Optimisation.Result derivedSolution = myModel.getVariableValues();
                if (derivedSolution.getState().isFeasible()) {
                    return new Optimisation.Result(State.UNBOUNDED, derivedSolution);
                }

            } else if (myModel.isFixed()) {

                Optimisation.Result derivedSolution = myModel.getVariableValues();

                if (derivedSolution.getState().isFeasible()) {
                    return new Optimisation.Result(State.DISTINCT, derivedSolution);
                }
                return new Optimisation.Result(State.INVALID, derivedSolution);
            }

            ExpressionsBasedModel.Integration<?> integration = this.getIntegration();
            Optimisation.Solver solver = this.getSolver();

            Optimisation.Result retVal = candidate != null ? candidate : myModel.getVariableValues();
            retVal = integration.toSolverState(retVal, myModel);
            retVal = solver.solve(retVal);
            retVal = integration.toModelState(retVal, myModel);

            return retVal;
        }

        @Override
        public String toString() {
            return myModel.toString();
        }

        public void update(final int index) {
            this.update(myModel.getVariable(index));
        }

        public void update(final IntIndex index) {
            this.update(myModel.getVariable(index));
        }

        public void update(final Variable variable) {

            if (myInPlaceUpdatesOK && mySolver != null && mySolver instanceof UpdatableSolver && variable.isFixed()) {
                UpdatableSolver updatableSolver = (UpdatableSolver) mySolver;

                int indexInSolver = this.getIntegration().getIndexInSolver(myModel, variable);
                double fixedValue = variable.getValue().doubleValue();

                if (updatableSolver.fixVariable(indexInSolver, fixedValue)) {
                    // Solver updated in-place
                    return;
                }
                myInPlaceUpdatesOK = false;
            }

            // Solver will be re-generated
            mySolver = null;
        }

        public boolean validate(final Access1D<BigDecimal> solution, final Printer appender) {
            return myModel.validate(solution, appender);
        }

        public boolean validate(final Result solution) {
            return myModel.validate(solution);
        }

        ExpressionsBasedModel.Integration<?> getIntegration() {
            if (myIntegration == null) {
                myIntegration = myModel.getIntegration();
            }
            return myIntegration;
        }

        Optimisation.Solver getSolver() {
            if (mySolver == null) {
                mySolver = this.getIntegration().build(myModel);
            }
            return mySolver;
        }

    }

    public static abstract class Presolver extends Simplifier<Expression, Presolver> {

        protected Presolver(final int executionOrder) {
            super(executionOrder);
        }

        /**
         * @param remaining TODO
         * @param lower TODO
         * @param upper TODO
         * @return True if any model entity was modified so that a re-run of the presolvers is necessary -
         *         typically when/if a variable was fixed.
         */
        public abstract boolean simplify(Expression expression, Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, NumberContext precision);

        @Override
        boolean isApplicable(final Expression target) {
            return target.isConstraint() && !target.isInfeasible() && !target.isRedundant() && target.countQuadraticFactors() == 0;
        }

    }

    static abstract class Simplifier<ME extends ModelEntity<?>, S extends Simplifier<?, ?>> implements Comparable<S> {

        private final int myExecutionOrder;
        private final UUID myUUID = UUID.randomUUID();

        Simplifier(final int executionOrder) {
            super();
            myExecutionOrder = executionOrder;
        }

        public final int compareTo(final S reference) {
            return Integer.compare(myExecutionOrder, reference.getExecutionOrder());
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof Simplifier)) {
                return false;
            }
            final Simplifier<?, ?> other = (Simplifier<?, ?>) obj;
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
        public final int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myUUID == null ? 0 : myUUID.hashCode());
            return result;
        }

        final int getExecutionOrder() {
            return myExecutionOrder;
        }

        abstract boolean isApplicable(final ME target);

    }

    static abstract class VariableAnalyser extends Simplifier<Variable, VariableAnalyser> {

        protected VariableAnalyser(final int executionOrder) {
            super(executionOrder);
        }

        public abstract boolean simplify(Variable variable, ExpressionsBasedModel model);

        @Override
        boolean isApplicable(final Variable target) {
            return true;
        }

    }

    static final class VariablesCategorisation {

        private transient int[] myFreeIndices = null;
        private final List<Variable> myFreeVariables = new ArrayList<>();
        private transient int[] myIntegerIndices = null;
        private final List<Variable> myIntegerVariables = new ArrayList<>();
        private transient int[] myNegativeIndices = null;
        private final List<Variable> myNegativeVariables = new ArrayList<>();
        private transient int[] myPositiveIndices = null;
        private final List<Variable> myPositiveVariables = new ArrayList<>();

        private void free(final ArrayList<Variable> variables) {
            if (myFreeIndices == null || myFreeIndices.length != variables.size()) {
                this.update(variables);
            }
        }

        private void integer(final ArrayList<Variable> variables) {
            if (myIntegerIndices == null || myIntegerIndices.length != variables.size()) {
                this.update(variables);
            }
        }

        private void negative(final ArrayList<Variable> variables) {
            if (myNegativeIndices == null || myNegativeIndices.length != variables.size()) {
                this.update(variables);
            }
        }

        private void positive(final ArrayList<Variable> variables) {
            if (myPositiveIndices == null || myPositiveIndices.length != variables.size()) {
                this.update(variables);
            }
        }

        List<Variable> getFreeVariables(final ArrayList<Variable> variables) {
            this.free(variables);
            return myFreeVariables;
        }

        List<Variable> getIntegerVariables(final ArrayList<Variable> variables) {
            this.integer(variables);
            return myIntegerVariables;
        }

        List<Variable> getNegativeVariables(final ArrayList<Variable> variables) {
            this.negative(variables);
            return myNegativeVariables;
        }

        List<Variable> getPositiveVariables(final ArrayList<Variable> variables) {
            this.positive(variables);
            return myPositiveVariables;
        }

        int indexOfFreeVariable(final int globalIndex, final ArrayList<Variable> variables) {
            this.free(variables);
            return myFreeIndices[globalIndex];
        }

        int indexOfIntegerVariable(final int globalIndex, final ArrayList<Variable> variables) {
            this.integer(variables);
            return myIntegerIndices[globalIndex];
        }

        int indexOfNegativeVariable(final int globalIndex, final ArrayList<Variable> variables) {
            this.negative(variables);
            return myNegativeIndices[globalIndex];
        }

        int indexOfPositiveVariable(final int globalIndex, final ArrayList<Variable> variables) {
            this.positive(variables);
            return myPositiveIndices[globalIndex];
        }

        void reset() {

            myFreeVariables.clear();
            myFreeIndices = null;

            myPositiveVariables.clear();
            myPositiveIndices = null;

            myNegativeVariables.clear();
            myNegativeIndices = null;

            myIntegerVariables.clear();
            myIntegerIndices = null;
        }

        void update(final ArrayList<Variable> variables) {

            int nbVariables = variables.size();

            myFreeVariables.clear();
            myFreeIndices = new int[nbVariables];
            Arrays.fill(myFreeIndices, -1);

            myPositiveVariables.clear();
            myPositiveIndices = new int[nbVariables];
            Arrays.fill(myPositiveIndices, -1);

            myNegativeVariables.clear();
            myNegativeIndices = new int[nbVariables];
            Arrays.fill(myNegativeIndices, -1);

            myIntegerVariables.clear();
            myIntegerIndices = new int[nbVariables];
            Arrays.fill(myIntegerIndices, -1);

            for (int i = 0; i < nbVariables; i++) {

                Variable tmpVariable = variables.get(i);

                if (!tmpVariable.isFixed()) {

                    myFreeVariables.add(tmpVariable);
                    myFreeIndices[i] = myFreeVariables.size() - 1;

                    if (!tmpVariable.isUpperLimitSet() || tmpVariable.getUpperLimit().signum() == 1) {
                        myPositiveVariables.add(tmpVariable);
                        myPositiveIndices[i] = myPositiveVariables.size() - 1;
                    }

                    if (!tmpVariable.isLowerLimitSet() || tmpVariable.getLowerLimit().signum() == -1) {
                        myNegativeVariables.add(tmpVariable);
                        myNegativeIndices[i] = myNegativeVariables.size() - 1;
                    }

                    if (tmpVariable.isInteger()) {
                        myIntegerVariables.add(tmpVariable);
                        myIntegerIndices[i] = myIntegerVariables.size() - 1;
                    }
                }
            }
        }
    }

    private static final List<ExpressionsBasedModel.Integration<?>> FALLBACK_INTEGRATIONS = new ArrayList<>();
    private static final String NEW_LINE = "\n";
    private static final String OBJ_FUNC_AS_CONSTR_KEY = UUID.randomUUID().toString();
    private static final String OBJECTIVE = "Generated/Aggregated Objective";
    private static final List<ExpressionsBasedModel.Integration<?>> PREFERRED_INTEGRATIONS = new ArrayList<>();
    private static final TreeSet<Presolver> PRESOLVERS = new TreeSet<>();
    private static final String START_END = "############################################\n";

    static {
        ExpressionsBasedModel.addPresolver(Presolvers.ZERO_ONE_TWO);
        ExpressionsBasedModel.addPresolver(Presolvers.REDUNDANT_CONSTRAINT);
    }

    /**
     * Add a solver that will be used for problem types the built-in solvers cannot handle as defined by
     * {@link Integration#isCapable(org.ojalgo.optimisation.Optimisation.Model)}.
     */
    public static boolean addFallbackSolver(final Integration<?> integration) {
        return FALLBACK_INTEGRATIONS.add(integration);
    }

    /**
     * Add a solver that will be used rather than the built-in solvers
     */
    public static boolean addPreferredSolver(final Integration<?> integration) {
        return PREFERRED_INTEGRATIONS.add(integration);
    }

    public static boolean addPresolver(final Presolver presolver) {
        return PRESOLVERS.add(presolver);
    }

    public static void clearIntegrations() {
        PREFERRED_INTEGRATIONS.clear();
        FALLBACK_INTEGRATIONS.clear();
    }

    public static void clearPresolvers() {
        PRESOLVERS.clear();
    }

    /**
     * Currently only supports the MPS file format, but with some of the various extensions. In particular it
     * is possible to parse QP models using QUADOBJ or QMATRIX file sections.
     */
    public static ExpressionsBasedModel parse(final File file) {

        String lowerCasePath = file.getPath().toLowerCase();

        if (!lowerCasePath.endsWith("mps") && !lowerCasePath.endsWith("sif")) {
            throw new IllegalArgumentException();
        }
        try (FileInputStream input = new FileInputStream(file)) {
            return ExpressionsBasedModel.parse(input, FileFormat.MPS);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static ExpressionsBasedModel parse(final InputStream input, final FileFormat format) {
        switch (format) {
        case MPS:
            return MathProgSysModel.parse(input).getExpressionsBasedModel();
        default:
            throw new IllegalArgumentException();
        }
    }

    public static boolean removeIntegration(final Integration<?> integration) {
        return PREFERRED_INTEGRATIONS.remove(integration) | FALLBACK_INTEGRATIONS.remove(integration);
    }

    public static boolean removePresolver(final Presolver presolver) {
        return PRESOLVERS.remove(presolver);
    }

    private final Map<String, Expression> myExpressions = new HashMap<>();
    private final Set<IntIndex> myFixedVariables = new HashSet<>();
    private transient boolean myInfeasible = false;
    private BigDecimal myObjectiveConstant = BigMath.ZERO;
    private final Set<IntIndex> myReferences;
    private boolean myRelaxed;
    /**
     * A shallow copy may share complex/large data structures with other models - typically the Map:s holding
     * Expression parameters.
     */
    private final boolean myShallowCopy;
    /**
     * Temporary storage for some expresssion specific subset of variables
     */
    private final Set<IntIndex> myTemporary = new HashSet<>();
    private final ArrayList<Variable> myVariables = new ArrayList<>();
    private final VariablesCategorisation myVariablesCategorisation = new VariablesCategorisation();

    public ExpressionsBasedModel() {
        this(new Optimisation.Options());
    }

    public ExpressionsBasedModel(final Collection<? extends Variable> variables) {

        this();

        for (Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }
    }

    public ExpressionsBasedModel(final Optimisation.Options someOptions) {

        super(someOptions);

        myReferences = new HashSet<>();

        myShallowCopy = false;
        myRelaxed = false;
    }

    public ExpressionsBasedModel(final Variable... variables) {

        this();

        for (Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }
    }

    ExpressionsBasedModel(final ExpressionsBasedModel modelToCopy, final boolean shallow, final boolean prune) {

        super(modelToCopy.options);

        this.setOptimisationSense(modelToCopy.getOptimisationSense());
        this.addObjectiveConstant(modelToCopy.getObjectiveConstant());

        for (Variable tmpVar : modelToCopy.getVariables()) {
            myVariables.add(tmpVar.clone());
        }

        Set<IntIndex> fixedVariables = modelToCopy.getFixedVariables();

        for (Expression tmpExpr : modelToCopy.getExpressions()) {
            if (shallow) {
                if (prune) {
                    if (tmpExpr.isObjective() || tmpExpr.isConstraint() && !tmpExpr.isRedundant()) {
                        myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, false));
                    }
                } else {
                    myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, false));
                }
            } else if (prune) {
                if (tmpExpr.isObjective() || tmpExpr.isConstraint() && !tmpExpr.isRedundant()) {
                    myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, true).compensate(fixedVariables));
                }
            } else {
                myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, true));
            }
        }

        myReferences = modelToCopy.getReferences();

        myShallowCopy = shallow || modelToCopy.isShallowCopy();
        myRelaxed = modelToCopy.isRelaxed();
    }

    public Expression addExpression() {
        return this.addExpression("EXPR" + myExpressions.size());
    }

    public Expression addExpression(final String name) {

        final Expression retVal = new Expression(name, this);

        myExpressions.put(name, retVal);

        return retVal;
    }

    /**
     * Creates a special ordered set (SOS) presolver instance and links that to the supplied expression.
     * When/if the presolver concludes that the SOS "constraints" are not possible the linked expression is
     * marked as infeasible.
     */
    public void addSpecialOrderedSet(final Collection<Variable> orderedSet, final int type, final Expression linkedTo) {

        if (type <= 0) {
            throw new ProgrammingError("Invalid SOS type!");
        }

        if (!linkedTo.isConstraint()) {
            throw new ProgrammingError("The linked to expression needs to be a constraint!");
        }

        final IntIndex[] sequence = new IntIndex[orderedSet.size()];
        int index = 0;
        for (final Variable variable : orderedSet) {
            if (variable == null || variable.getIndex() == null) {
                throw new ProgrammingError("Variables must be already inserted in the model!");
            }
            sequence[index++] = variable.getIndex();
        }

        ExpressionsBasedModel.addPresolver(new SpecialOrderedSet(sequence, type, linkedTo));
    }

    /**
     * Calling this method will create 2 things:
     * <ol>
     * <li>A simple expression meassuring the sum of the (binary) variable values (the number of binary
     * variables that are "ON"). The upper, and optionally lower, limits are set as defined by the
     * <code>max</code> and <code>min</code> parameter values.</li>
     * <li>A custom presolver (specific to this SOS) to be used by the MIP solver. This presolver help to keep
     * track of which combinations of variable values or feasible, and is the only thing that enforces the
     * order.</li>
     * </ol>
     *
     * @param orderedSet The set members in correct order. Each of these variables must be binary.
     * @param min The minimum number of binary varibales in the set that must be "ON" (Set this to 0 if there
     *        is no minimum.)
     * @param max The SOS type or maximum number of binary varibales in the set that may be "ON"
     */
    public void addSpecialOrderedSet(final Collection<Variable> orderedSet, final int min, final int max) {

        if (max <= 0 || min > max) {
            throw new ProgrammingError("Invalid min/max number of ON variables!");
        }

        final String name = "SOS" + max + "-" + orderedSet.toString();

        final Expression expression = this.addExpression(name);

        for (final Variable variable : orderedSet) {
            if (variable == null || variable.getIndex() == null || !variable.isBinary()) {
                throw new ProgrammingError("Variables must be binary and already inserted in the model!");
            }
            expression.set(variable.getIndex(), ONE);
        }

        expression.upper(BigDecimal.valueOf(max));
        if (min > 0) {
            expression.lower(BigDecimal.valueOf(min));
        }

        this.addSpecialOrderedSet(orderedSet, max, expression);
    }

    public Variable addVariable() {
        return this.addVariable("X" + myVariables.size());
    }

    public Variable addVariable(final String name) {
        final Variable retVal = new Variable(name);
        this.addVariable(retVal);
        return retVal;
    }

    public void addVariable(final Variable variable) {
        if (myShallowCopy) {
            throw new IllegalStateException("This model is a work copy - its set of variables cannot be modified!");
        }
        myVariables.add(variable);
        variable.setIndex(new IntIndex(myVariables.size() - 1));
    }

    public void addVariables(final Collection<? extends Variable> variables) {
        for (final Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }
    }

    public void addVariables(final Variable[] variables) {
        for (final Variable tmpVariable : variables) {
            this.addVariable(tmpVariable);
        }
    }

    /**
     * @return A stream of variables that are constraints and not fixed
     */
    public Stream<Variable> bounds() {
        return this.variables().filter(v -> v.isConstraint() && !v.isFixed());
    }

    /**
     * @return A prefiltered stream of expressions that are constraints and have not been markes as redundant
     */
    public Stream<Expression> constraints() {
        return myExpressions.values().stream().filter(c -> c.isConstraint() && !c.isRedundant());
    }

    public ExpressionsBasedModel copy() {
        return new ExpressionsBasedModel(this, false, false);
    }

    public ExpressionsBasedModel copy(final boolean relax) {
        ExpressionsBasedModel copy = new ExpressionsBasedModel(this, false, false);
        if (relax) {
            copy.relax(false);
        }
        return copy;
    }

    public ExpressionsBasedModel copy(final boolean shallow, final boolean prune) {
        return new ExpressionsBasedModel(this, shallow, prune);
    }

    public int countExpressions() {
        return myExpressions.size();
    }

    public int countVariables() {
        return myVariables.size();
    }

    @Override
    public void dispose() {

        for (final Expression tmpExprerssion : myExpressions.values()) {
            tmpExprerssion.destroy();
        }
        myExpressions.clear();

        for (final Variable tmpVariable : myVariables) {
            tmpVariable.destroy();
        }
        myVariables.clear();

        myFixedVariables.clear();

        myVariablesCategorisation.reset();
    }

    public Expression generateCut(final Expression constraint, final Optimisation.Result solution) {

        return null;
    }

    public Expression getExpression(final String name) {
        return myExpressions.get(name);
    }

    public Collection<Expression> getExpressions() {
        return Collections.unmodifiableCollection(myExpressions.values());
    }

    public Set<IntIndex> getFixedVariables() {
        myFixedVariables.clear();
        for (Variable tmpVar : myVariables) {
            if (tmpVar.isFixed()) {
                myFixedVariables.add(tmpVar.getIndex());
            }
        }
        return Collections.unmodifiableSet(myFixedVariables);
    }

    /**
     * @return A list of the variables that are not fixed at a specific value
     */
    public List<Variable> getFreeVariables() {
        return Collections.unmodifiableList(myVariablesCategorisation.getFreeVariables(myVariables));
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and are marked as integer
     *         variables
     */
    public List<Variable> getIntegerVariables() {
        return Collections.unmodifiableList(myVariablesCategorisation.getIntegerVariables(myVariables));
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include negative
     *         values
     */
    public List<Variable> getNegativeVariables() {
        return Collections.unmodifiableList(myVariablesCategorisation.getNegativeVariables(myVariables));
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whos range include positive
     *         values and/or zero
     */
    public List<Variable> getPositiveVariables() {
        return Collections.unmodifiableList(myVariablesCategorisation.getPositiveVariables(myVariables));
    }

    public Variable getVariable(final int index) {
        return myVariables.get(index);
    }

    public Variable getVariable(final IntIndex index) {
        return myVariables.get(index.index);
    }

    public List<Variable> getVariables() {
        return Collections.unmodifiableList(myVariables);
    }

    public Optimisation.Result getVariableValues() {
        return this.getVariableValues(options.feasibility);
    }

    /**
     * Null variable values are replaced with 0.0. If any variable value is null the state is set to
     * INFEASIBLE even if zero would actually be a feasible value. The objective function value is not
     * calculated for infeasible variable values.
     */
    public Optimisation.Result getVariableValues(final NumberContext validationContext) {

        final int numberOfVariables = myVariables.size();

        State retState = State.UNEXPLORED;
        double retValue = Double.NaN;
        final Array1D<BigDecimal> retSolution = Array1D.BIG.make(numberOfVariables);

        boolean allVarsSomeInfo = true;
        boolean shouldCheckGradient = false;

        for (int i = 0; i < numberOfVariables; i++) {
            final Variable tmpVariable = myVariables.get(i);

            if (tmpVariable.getValue() != null) {

                retSolution.set(i, tmpVariable.getValue());

            } else if (tmpVariable.isEqualityConstraint()) {
                retSolution.set(i, tmpVariable.getLowerLimit());
            } else if (tmpVariable.isLowerLimitSet() && tmpVariable.isUpperLimitSet()) {
                retSolution.set(i, BigMath.DIVIDE.invoke(tmpVariable.getLowerLimit().add(tmpVariable.getUpperLimit()), TWO));
                shouldCheckGradient = true;
            } else if (tmpVariable.isLowerLimitSet()) {
                retSolution.set(i, tmpVariable.getLowerLimit());
            } else if (tmpVariable.isUpperLimitSet()) {
                retSolution.set(i, tmpVariable.getUpperLimit());
            } else {
                retSolution.set(i, ZERO);
                allVarsSomeInfo = false; // This var no info
            }
        }

        if (shouldCheckGradient && this.isAnyObjectiveQuadratic()) {

            MatrixStore<Double> gradient = this.objective().getAdjustedGradient(retSolution);

            double grad = 0.0;
            for (int i = 0; i < numberOfVariables; i++) {
                final Variable tmpVariable = myVariables.get(i);

                if (tmpVariable.isLowerLimitSet() && tmpVariable.isUpperLimitSet()) {
                    grad = gradient.doubleValue(i);
                    if (this.isMinimisation()) {
                        grad = -grad;
                    }
                    if (grad < 0.0) {
                        retSolution.set(i, tmpVariable.getLowerLimit());
                    } else {
                        retSolution.set(i, tmpVariable.getUpperLimit());
                    }
                }
            }

        }

        if (allVarsSomeInfo) {
            if (this.validate(retSolution, validationContext)) {
                retState = State.FEASIBLE;
                retValue = this.objective().evaluate(retSolution).doubleValue();
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
     * @return Local index among the free variables. -1 indicates the variable is not a free variable.
     */
    public int indexOfFreeVariable(final int globalIndex) {
        return myVariablesCategorisation.indexOfFreeVariable(globalIndex, myVariables);
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
        return myVariablesCategorisation.indexOfIntegerVariable(globalIndex, myVariables);
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
        return myVariablesCategorisation.indexOfNegativeVariable(globalIndex, myVariables);
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
        return myVariablesCategorisation.indexOfPositiveVariable(globalIndex, myVariables);
    }

    public int indexOfPositiveVariable(final IntIndex variableIndex) {
        return this.indexOfPositiveVariable(variableIndex.index);
    }

    public int indexOfPositiveVariable(final Variable variable) {
        return this.indexOfPositiveVariable(this.indexOf(variable));
    }

    public boolean isAnyConstraintQuadratic() {

        boolean retVal = false;

        for (Expression expr : myExpressions.values()) {
            retVal |= expr.isConstraint() && !expr.isRedundant() && expr.isAnyQuadraticFactorNonZero();
        }

        return retVal;
    }

    /**
     * Objective or any constraint has quadratic part.
     */
    public boolean isAnyExpressionQuadratic() {

        boolean retVal = false;

        for (Expression expr : myExpressions.values()) {
            retVal |= expr.isAnyQuadraticFactorNonZero() && (expr.isObjective() || expr.isConstraint() && !expr.isRedundant());
        }

        return retVal;
    }

    public boolean isAnyObjectiveQuadratic() {

        boolean retVal = false;

        for (Expression expr : myExpressions.values()) {
            retVal |= expr.isObjective() && expr.isAnyQuadraticFactorNonZero();
        }

        return retVal;
    }

    public boolean isAnyVariableFixed() {
        return myVariables.stream().anyMatch(Variable::isFixed);
    }

    public boolean isAnyVariableInteger() {

        if (myRelaxed) {
            return false;
        }

        boolean retVal = false;

        for (int i = 0, limit = myVariables.size(); !retVal && i < limit; i++) {
            Variable variable = myVariables.get(i);
            retVal |= variable.isInteger() && !variable.isFixed();
        }

        return retVal;
    }

    public Expression limitObjective(final BigDecimal lower, final BigDecimal upper) {

        Expression constrExpr = myExpressions.get(OBJ_FUNC_AS_CONSTR_KEY);

        if (constrExpr == null) {
            Expression objExpr = this.objective();
            if (!objExpr.isAnyQuadraticFactorNonZero()) {
                constrExpr = objExpr.copy(this, false);
                myExpressions.put(OBJ_FUNC_AS_CONSTR_KEY, constrExpr);
            }
        }

        if (constrExpr != null) {

            constrExpr.lower(lower).upper(upper);

            if (constrExpr.isLinearAndAllInteger()) {
                constrExpr.doIntegerRounding();
            }
        }

        return constrExpr != null ? constrExpr : this.objective();
    }

    public Optimisation.Result maximise() {

        this.setMaximisation();

        return this.optimise();
    }

    public Optimisation.Result minimise() {

        this.setMinimisation();

        return this.optimise();
    }

    /**
     * This is generated on demend – you should not cache this. More specifically, modifications made to this
     * expression will not be part of the optimisation model. You define the objective by setting the
     * {@link Variable#weight(Comparable)}/{@link Expression#weight(Comparable)} on one or more variables
     * and/or expressions.
     *
     * @return The generated/aggregated objective function
     */
    public Expression objective() {

        Expression retVal = new Expression(OBJECTIVE, this);

        Variable tmpVariable;
        for (int i = 0; i < myVariables.size(); i++) {
            tmpVariable = myVariables.get(i);

            if (tmpVariable.isObjective()) {
                retVal.set(i, tmpVariable.getContributionWeight());
            }
        }

        BigDecimal tmpOldVal = null;
        BigDecimal tmpDiff = null;
        BigDecimal tmpNewVal = null;

        retVal.setConstant(this.getObjectiveConstant());

        for (Expression tmpExpression : myExpressions.values()) {

            if (tmpExpression.isObjective()) {

                BigDecimal tmpContributionWeight = tmpExpression.getContributionWeight();
                boolean tmpNotOne = tmpContributionWeight.compareTo(ONE) != 0; // To avoid multiplication by 1.0

                if (tmpExpression.isAnyLinearFactorNonZero()) {
                    for (IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                        tmpOldVal = retVal.get(tmpKey);
                        tmpDiff = tmpExpression.get(tmpKey);
                        tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                        retVal.set(tmpKey, tmpNewVal);
                    }
                }

                if (tmpExpression.isAnyQuadraticFactorNonZero()) {
                    for (IntRowColumn tmpKey : tmpExpression.getQuadraticKeySet()) {
                        tmpOldVal = retVal.get(tmpKey);
                        tmpDiff = tmpExpression.get(tmpKey);
                        tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                        retVal.set(tmpKey, tmpNewVal);
                    }
                }
            }
        }

        return retVal;
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
    public ExpressionsBasedModel.Intermediate prepare() {
        return new ExpressionsBasedModel.Intermediate(this);
    }

    public void relax() {
        this.relax(false);
    }

    /**
     * @param soft If true the integer variables are still identified as such, but the model is flagged as
     *        non-integer (will not use the {@link IntegerSolver}, but presolve and validation may still
     *        recognise the variables' integer property). If false the integer property of any/all variables
     *        are removed.
     */
    public void relax(final boolean soft) {
        if (soft) {
            myRelaxed = true;
        } else {
            for (Variable variable : myVariables) {
                variable.relax();
            }
        }
    }

    /**
     * Will perform presolve and then create a copy removing redundant constraint expressions, and pruning the
     * remaining ones to no longer include fixed variables.
     */
    public ExpressionsBasedModel simplify() {

        this.scanEntities();

        this.presolve();

        return new ExpressionsBasedModel(this, true, true);
    }

    /**
     * Will create a shallow copy flagged as relaxed.
     */
    public ExpressionsBasedModel snapshot() {
        ExpressionsBasedModel shallowCopy = this.copy(true, false);
        shallowCopy.relax(true);
        return shallowCopy;
    }

    @Override
    public String toString() {

        StringBuilder retVal = new StringBuilder(START_END);

        for (Variable tmpVariable : myVariables) {
            tmpVariable.appendToString(retVal);
            retVal.append(NEW_LINE);
        }

        for (Expression tmpExpression : myExpressions.values()) {
            tmpExpression.appendToString(retVal, this.getVariableValues());
            retVal.append(NEW_LINE);
        }

        return retVal.append(START_END).toString();
    }

    /**
     * This methods validtes model construction only. All the other validate(...) method validates the
     * solution (one way or another).
     *
     * @see org.ojalgo.optimisation.Optimisation.Model#validate()
     */
    public boolean validate() {

        final Printer appender = options.logger_detailed ? options.logger_appender : BasicLogger.NULL;

        boolean retVal = true;

        for (final Variable tmpVariable : myVariables) {
            retVal &= tmpVariable.validate(appender);
        }

        for (final Expression tmpExpression : myExpressions.values()) {
            retVal &= tmpExpression.validate(appender);
        }

        return retVal;
    }

    public boolean validate(final Access1D<BigDecimal> solution) {
        final NumberContext context = options.feasibility;
        final Printer appender = options.logger_detailed && options.logger_appender != null ? options.logger_appender : BasicLogger.NULL;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context) {
        final Printer appender = options.logger_detailed && options.logger_appender != null ? options.logger_appender : BasicLogger.NULL;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context, final Printer appender) {

        ProgrammingError.throwIfNull(solution, context, appender);

        int size = myVariables.size();

        boolean retVal = size == solution.count();

        for (int i = 0; retVal && i < size; i++) {
            Variable tmpVariable = myVariables.get(i);
            BigDecimal value = solution.get(i);
            retVal &= tmpVariable.validate(value, context, appender, myRelaxed);
        }

        if (retVal) {
            for (Expression tmpExpression : myExpressions.values()) {
                BigDecimal value = tmpExpression.evaluate(solution);
                retVal &= tmpExpression.validate(value, context, appender);
            }
        }

        return retVal;
    }

    public boolean validate(final Access1D<BigDecimal> solution, final Printer appender) {
        final NumberContext context = options.feasibility;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final NumberContext context) {
        final Result solution = this.getVariableValues(context);
        final Printer appender = options.logger_detailed && options.logger_appender != null ? options.logger_appender : BasicLogger.NULL;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final NumberContext context, final Printer appender) {
        final Access1D<BigDecimal> solution = this.getVariableValues(context);
        return this.validate(solution, context, appender);
    }

    public boolean validate(final Printer appender) {
        final NumberContext context = options.feasibility;
        final Result solution = this.getVariableValues(context);
        return this.validate(solution, context, appender);
    }

    /**
     * @return A stream of variables that are not fixed
     */
    public Stream<Variable> variables() {
        return myVariables.stream().filter(v -> !v.isEqualityConstraint());
    }

    /**
     * Will indentify constarints with equal variables set, and check if those can be combined or not.
     */
    private void identifyRedundantConstraints() {

        for (Entry<String, Expression> refEntry : myExpressions.entrySet()) {

            String refName = refEntry.getKey();
            Expression refExpression = refEntry.getValue();
            if (refExpression.isConstraint() && !refExpression.isRedundant()) {
                Set<IntIndex> refLinearKeySet = refExpression.getLinearKeySet();

                for (Entry<String, Expression> subEntry : myExpressions.entrySet()) {

                    String subName = subEntry.getKey();
                    Expression subExpression = subEntry.getValue();
                    if (subExpression.isConstraint() && !subExpression.isRedundant()) {
                        Set<IntIndex> subLinearKeySet = subExpression.getLinearKeySet();

                        if (!refName.equals(subName) && refLinearKeySet.equals(subLinearKeySet)) {

                            BigDecimal fctVal = null;
                            BigDecimal tmpVal = null;

                            for (IntIndex index : refLinearKeySet) {

                                tmpVal = BigMath.DIVIDE.invoke(refExpression.get(index), subExpression.get(index));

                                if (fctVal == null) {
                                    fctVal = tmpVal;
                                } else if (tmpVal.compareTo(fctVal) != 0) {
                                    fctVal = null;
                                    break;
                                }
                            }

                            if (fctVal != null) {

                                BasicLogger.debug("Match! {}", fctVal);
                                BasicLogger.debug("Ref: {}", refExpression);
                                BasicLogger.debug("Sub: {}", subExpression);

                                boolean pos = fctVal.signum() == 1;

                                BigDecimal refLo = refExpression.getLowerLimit();
                                BigDecimal refUp = refExpression.getUpperLimit();

                                BigDecimal subLo = pos ? subExpression.getLowerLimit() : subExpression.getUpperLimit();
                                BigDecimal subUp = pos ? subExpression.getUpperLimit() : subExpression.getLowerLimit();

                                if (fctVal.compareTo(ONE) != 0) {
                                    if (subLo != null) {
                                        subLo = subLo.multiply(fctVal);
                                    }
                                    if (subUp != null) {
                                        subUp = subUp.multiply(fctVal);
                                    }
                                }

                                if (subLo != null) {
                                    if (refLo != null) {
                                        refExpression.lower(subLo.max(refLo));
                                    } else {
                                        refExpression.lower(subLo);
                                    }
                                }

                                if (subUp != null) {
                                    if (refUp != null) {
                                        refExpression.upper(subUp.min(refUp));
                                    } else {
                                        refExpression.upper(subUp);
                                    }
                                }

                                subExpression.setRedundant();
                            }
                        }
                    }
                }
            }
        }
    }

    private void scanEntities() {

        boolean anyVarInt = this.isAnyVariableInteger();

        for (Expression tmpExpr : myExpressions.values()) {

            Set<IntIndex> allVars = tmpExpr.getLinearKeySet();
            BigDecimal lower = tmpExpr.getLowerLimit();
            BigDecimal upper = tmpExpr.getUpperLimit();

            if (tmpExpr.isObjective()) {
                Presolvers.LINEAR_OBJECTIVE.simplify(tmpExpr, allVars, lower, upper, options.feasibility);
            }

            if (tmpExpr.isConstraint()) {
                Presolvers.ZERO_ONE_TWO.simplify(tmpExpr, allVars, lower, upper, options.feasibility);
                if (anyVarInt) {
                    Presolvers.INTEGER_EXPRESSION_ROUNDING.simplify(tmpExpr, allVars, lower, upper, options.feasibility);
                }
            }
        }

        for (Variable tmpVar : myVariables) {
            Presolvers.UNREFERENCED.simplify(tmpVar, this);
            if (anyVarInt && tmpVar.isConstraint()) {
                Presolvers.INTEGER_VARIABLE_ROUNDING.simplify(tmpVar, this);
            }
        }
    }

    void addObjectiveConstant(final BigDecimal addition) {
        if (addition != null && addition.signum() != 0) {
            myObjectiveConstant = myObjectiveConstant.add(addition);
        }
    }

    void addReference(final IntIndex index) {
        myReferences.add(index);
    }

    int deriveAdjustmentRange(final Expression expression) {

        int retVal = 0;

        for (IntIndex linear : expression.getLinearKeySet()) {
            retVal = Math.max(retVal, Math.abs(myVariables.get(linear.index).getAdjustmentExponent()));
        }

        for (IntRowColumn quadratic : expression.getQuadraticKeySet()) {
            retVal = Math.max(retVal, Math.abs(myVariables.get(quadratic.row).getAdjustmentExponent()));
            retVal = Math.max(retVal, Math.abs(myVariables.get(quadratic.column).getAdjustmentExponent()));
        }

        return retVal;
    }

    Stream<Expression> expressions() {
        return myExpressions.values().stream();
    }

    ExpressionsBasedModel.Integration<?> getIntegration() {

        ExpressionsBasedModel.Integration<?> retVal = null;

        for (final ExpressionsBasedModel.Integration<?> preferred : PREFERRED_INTEGRATIONS) {
            if (preferred.isCapable(this)) {
                retVal = preferred;
                break;
            }
        }

        if (retVal == null) {
            if (this.isAnyVariableInteger()) {
                if (IntegerSolver.INTEGRATION.isCapable(this)) {
                    retVal = IntegerSolver.INTEGRATION;
                }
            } else if (ConvexSolver.INTEGRATION.isCapable(this)) {
                retVal = ConvexSolver.INTEGRATION;
            } else if (LinearSolver.INTEGRATION.isCapable(this)) {
                retVal = LinearSolver.INTEGRATION;
            }
        }

        if (retVal == null) {
            for (final ExpressionsBasedModel.Integration<?> fallback : FALLBACK_INTEGRATIONS) {
                if (fallback.isCapable(this)) {
                    retVal = fallback;
                    break;
                }
            }
        }

        if (retVal == null) {
            throw new ProgrammingError("No solver integration available that can handle this model!");
        }

        return retVal;
    }

    BigDecimal getObjectiveConstant() {
        return myObjectiveConstant;
    }

    Set<IntIndex> getReferences() {
        return myReferences;
    }

    boolean isFixed() {
        return myVariables.stream().allMatch(Variable::isFixed);
    }

    boolean isInfeasible() {
        if (myInfeasible) {
            return true;
        }
        for (final Expression tmpExpression : myExpressions.values()) {
            if (tmpExpression.isInfeasible()) {
                return myInfeasible = true;
            }
        }
        for (final Variable tmpVariable : myVariables) {
            if (tmpVariable.isInfeasible()) {
                return myInfeasible = true;
            }
        }
        return false;
    }

    boolean isReferenced(final Variable variable) {
        return myReferences.contains(variable.getIndex());
    }

    boolean isRelaxed() {
        return myRelaxed;
    }

    boolean isShallowCopy() {
        return myShallowCopy;
    }

    boolean isUnbounded() {
        return myVariables.stream().anyMatch(Variable::isUnbounded);
    }

    Optimisation.Result optimise() {

        if (!myShallowCopy && PRESOLVERS.size() > 0) {
            this.scanEntities();
        }

        Intermediate prepared = this.prepare();

        Optimisation.Result result = prepared.solve(null);

        for (int i = 0, limit = myVariables.size(); i < limit; i++) {
            Variable tmpVariable = myVariables.get(i);
            if (!tmpVariable.isFixed()) {
                tmpVariable.setValue(options.solution.toBigDecimal(result.doubleValue(i)));
            }
        }

        Result retSolution = this.getVariableValues();
        double retValue = this.objective().evaluate(retSolution).doubleValue();
        Optimisation.State retState = result.getState();

        prepared.dispose();

        return new Optimisation.Result(retState, retValue, retSolution);
    }

    void presolve() {

        //  myExpressions.values().forEach(expr -> expr.reset());

        boolean needToRepeat = false;

        BigDecimal compensatedLowerLimit;
        BigDecimal compensatedUpperLimit;

        do {

            Set<IntIndex> fixedVariables = this.getFixedVariables();
            needToRepeat = false;

            for (Expression expr : this.getExpressions()) {
                if (!needToRepeat && expr.isConstraint() && !expr.isInfeasible() && !expr.isRedundant() && expr.countQuadraticFactors() == 0) {

                    BigDecimal calculateSetValue = expr.calculateSetValue(fixedVariables);

                    compensatedLowerLimit = expr.getCompensatedLowerLimit(calculateSetValue);
                    compensatedUpperLimit = expr.getCompensatedUpperLimit(calculateSetValue);

                    myTemporary.clear();
                    myTemporary.addAll(expr.getLinearKeySet());
                    myTemporary.removeAll(fixedVariables);

                    for (Presolver presolver : PRESOLVERS) {
                        if (!needToRepeat) {
                            needToRepeat |= presolver.simplify(expr, myTemporary, compensatedLowerLimit, compensatedUpperLimit, options.feasibility);
                        }
                    }

                }
            }

        } while (needToRepeat);

        if (!this.isInfeasible()) {
            Set<IntIndex> fixedVariables = this.getFixedVariables();
            for (Expression expr : this.getExpressions()) {
                if (expr.isConstraint() && expr.isRedundant() && expr.countQuadraticFactors() == 0) {
                    // Specifically need to check that constraints that have been determined redundant
                    // are not infeasible

                    BigDecimal calculateSetValue = expr.calculateSetValue(fixedVariables);

                    compensatedLowerLimit = expr.getCompensatedLowerLimit(calculateSetValue);
                    compensatedUpperLimit = expr.getCompensatedUpperLimit(calculateSetValue);

                    myTemporary.clear();
                    myTemporary.addAll(expr.getLinearKeySet());
                    myTemporary.removeAll(fixedVariables);

                    Presolvers.checkFeasibility(expr, myTemporary, compensatedLowerLimit, compensatedUpperLimit, options.feasibility, myRelaxed);
                }
            }
        }

        if (!myShallowCopy) {
            // this.identifyRedundantConstraints();
        }

        myVariablesCategorisation.update(myVariables);
    }

    void setInfeasible() {
        myInfeasible = true;
    }

}
