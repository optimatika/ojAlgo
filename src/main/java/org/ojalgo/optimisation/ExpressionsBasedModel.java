/*
 * Copyright 1997-2025 Optimatika
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.InMemoryFile;
import org.ojalgo.netio.ToFileWriter;
import org.ojalgo.optimisation.Optimisation.Integration;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;
import org.ojalgo.type.EnumBitSet;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * Construct optimisation problems by combining {@link Variable}s and {@link Expression}s. Each model entity
 * becomes a constraint when lower/upper limits are set, and contributes to the objective function when a
 * weight is set.
 * <p>
 * Typical workflow:
 * <ol>
 * <li>Create a model
 * <li>Add variables via {@link #addVariable(String)} with bounds and/or weights
 * <li>Add expressions via {@link #addExpression(String)} with coefficients, bounds, and/or weights
 * <li>Solve with {@link #minimise()} or {@link #maximise()}
 * </ol>
 * <p>
 * The model automatically selects and configures a suitable solver, applies presolve simplifications, and
 * scales parameters for numerical stability.
 * <p>
 * Each model is associated with an {@link Optimisation.Environment} that holds solver integrations,
 * presolvers, and variable/expression factories. The no-arg constructor uses the default
 * {@link Optimisation#ENVIRONMENT}. To isolate configuration (e.g. different solver integrations for
 * different model types) create a separate environment and use {@link Optimisation.Environment#newModel()} as
 * the factory.
 *
 * @see Optimisation.Environment
 * @author apete
 */
public final class ExpressionsBasedModel implements Optimisation.Model {

    /**
     * Counts of different kinds of model entities.
     *
     * @author apete
     */
    public static final class Description implements ProblemStructure {

        public final int nbEqualityBounds;
        public final int nbEqualityConstraints;
        public final int nbIntegerVariables;
        public final int nbLowerBounds;
        public final int nbLowerConstraints;
        public final int nbNegativeVariables;
        public final int nbPositiveVariables;
        public final int nbUpperBounds;
        public final int nbUpperConstraints;
        public final int nbVariables;

        Description(final int varTotal, final int varPositive, final int varNegative, final int varInteger, final int nbLoBound, final int nbUpBound,
                final int nbEqBound, final int nbLoCnstr, final int nbUpCnstr, final int nbEqCnstr) {

            super();

            nbLowerBounds = nbLoBound;
            nbUpperBounds = nbUpBound;
            nbEqualityBounds = nbEqBound;

            nbLowerConstraints = nbLoCnstr;
            nbUpperConstraints = nbUpCnstr;
            nbEqualityConstraints = nbEqCnstr;

            nbIntegerVariables = varInteger;
            nbNegativeVariables = varNegative;
            nbPositiveVariables = varPositive;
            nbVariables = varTotal;
        }

        @Override
        public int countAdditionalConstraints() {
            return 0;
        }

        @Override
        public int countConstraints() {
            return nbEqualityConstraints + nbLowerConstraints + nbUpperConstraints;
        }

        @Override
        public int countEqualityConstraints() {
            return nbEqualityBounds;
        }

        @Override
        public int countInequalityConstraints() {
            return nbLowerConstraints + nbUpperConstraints;
        }

        @Override
        public int countVariables() {
            return nbVariables;
        }

        @Override
        public String toString() {
            return "Description [nbEqualityBounds=" + nbEqualityBounds + ", nbEqualityConstraints=" + nbEqualityConstraints + ", nbIntegerVariables="
                    + nbIntegerVariables + ", nbLowerBounds=" + nbLowerBounds + ", nbLowerConstraints=" + nbLowerConstraints + ", nbNegativeVariables="
                    + nbNegativeVariables + ", nbPositiveVariables=" + nbPositiveVariables + ", nbUpperBounds=" + nbUpperBounds + ", nbUpperConstraints="
                    + nbUpperConstraints + ", nbVariables=" + nbVariables + "]";
        }

    }

    /**
     * Connects solver constraints and variables back to model entities. Used for 2 things:
     * <ol>
     * <li>Solvers that manipulate models (like the {@link IntegerSolver}) need this to map between model
     * entities and solver indices.</li>
     * <li>Simplifies implementation of
     * {@link ExpressionsBasedModel.Integration#toModelState(org.ojalgo.optimisation.Optimisation.Result, ExpressionsBasedModel)}.</li>
     * </ol>
     */
    public interface EntityMap extends ProblemStructure {

        /**
         * The number of variables, in the solver, that directly correspond to a model variable. (Not slack or
         * artificial variables.)
         * <p>
         * This defines the range of the indices that can be used with the {@link #indexOf(int)} and
         * {@link #isNegated(int)} methods.
         */
        int countModelVariables();

        /**
         * The number of slack variables.
         * <p>
         * This defines the range of the indices that can be used with the {@link #getSlack(int)} method.
         */
        int countSlackVariables();

        EntryPair<ModelEntity<?>, ConstraintType> getConstraint(int idc);

        /**
         * Returns which model entity, and constraint type, that corresponds to the slack variable at the
         * supplied index.
         *
         * @param ids Index of solver slack variable (If there are 3 slack variables this input argument
         *            should be in the range [0.2].)
         */
        EntryPair<ModelEntity<?>, ConstraintType> getSlack(int ids);

        /**
         * Converts from a solver specific variable index to the corresponding index of the variable in the
         * model. Note that not all model variables are necessarily represented in the solver, and a model
         * variable may result in multiple solver variables. Further, slack variables, artificial variables
         * and such are typically not represented in the model.
         *
         * @param solverIndex Index of solver variable
         * @return Index of model variable (negative if no map)
         */
        int indexOf(int solverIndex);

        /**
         * Build an internal integer-variable mask matching the solver's variable ordering.
         * <p>
         * Given an {@link ExpressionsBasedModel} and this {@link EntityMap}, this method inspects the model's
         * variables and the slack entities to determine which internal variables are subject to integrality
         * restrictions. The result is typically passed to
         * {@link UpdatableSolver#generateCutCandidates(double, boolean[])} and used by integer algorithms
         * (for example {@link IntegerSolver} and {@link org.ojalgo.optimisation.integer.NodeSolver}) to
         * interpret the solver state.
         *
         * @param model the model that this solver instance is (or will be) solving
         * @return a boolean array where {@code true} marks an internal variable that must take integer
         *         values; the array length equals the sum of model and slack variables in this
         *         {@link EntityMap}
         */
        default boolean[] integers(final ExpressionsBasedModel model) {

            int nbProblVars = this.countModelVariables();
            int nbSlackVars = this.countSlackVariables();

            boolean[] integers = new boolean[nbProblVars + nbSlackVars];

            for (int i = 0; i < nbProblVars; i++) {
                int indexInModel = this.indexOf(i);
                Variable variable = model.getVariable(indexInModel);
                if (variable.isInteger()) {
                    integers[i] = true;
                }
            }

            for (int i = 0; i < nbSlackVars; i++) {
                EntryPair<ModelEntity<?>, ConstraintType> slack = this.getSlack(i);
                ModelEntity<?> entity = slack.getKey();
                if (entity.isInteger()) {
                    integers[nbProblVars + i] = true;
                }
            }

            return integers;
        }

        /**
         * Is this solver variable negated relative to the corresponding model variable?
         *
         * @param solverIndex Index of solver variable
         * @return true if this solver variable represents a negated model variable
         */
        boolean isNegated(int solverIndex);

    }

    public static abstract class ExpressionAnalyser extends Simplifier<Expression, ExpressionAnalyser> {

        protected ExpressionAnalyser(final int executionOrder) {
            super(executionOrder);
        }

        public abstract void simplify(Expression target, ExpressionsBasedModel model);

    }

    public enum FileFormat {

        EBM, MPS;

        /**
         * Apart from the "native" EBM file format, currently only supports the MPS file format, but with some
         * of the various extensions. In particular it is possible to parse QP models using QUADOBJ or QMATRIX
         * file sections.
         */
        public static FileFormat from(final File file) {
            return FileFormat.from(file.getPath());
        }

        public static FileFormat from(final String path) {

            String lowerCasePath = path.toLowerCase();

            if (lowerCasePath.endsWith("mps") || lowerCasePath.endsWith("sif")) {
                return FileFormat.MPS;
            }

            if (lowerCasePath.endsWith("ebm")) {
                return FileFormat.EBM;
            }

            throw new IllegalArgumentException();
        }
    }

    /**
     * Simplifies creating {@link Integration}s by providing some default implementations and helper methods
     * for common cases.
     * <p>
     * The defaults assume that the solver state and model state (variable indices) are identical.
     * <p>
     * The helper methods are for the very common case when the solver only works with free (not eliminated by
     * the pre-solver) variables, and need to map between the solver state and the full model state.
     */
    public static abstract class Integration<S extends Optimisation.Solver> implements Optimisation.Integration<ExpressionsBasedModel, S> {

        /**
         * Reconstructs a model-level reduced cost from first principles: {@code rc_v = c_v - Σ a_iv · λ_i},
         * using the variable's objective coefficient and the constraint multipliers reported on
         * {@code solverState}. Useful for variables that the solver doesn't see (eliminated by presolve), and
         * for solver paths whose internal index space prevents direct pass-through of the rc.
         * <p>
         * Returned value is in the same sense the multipliers are expressed in (i.e. typically MIN, the
         * solver's internal sense). Callers should negate for MAX models if appropriate.
         */
        protected static double computeReducedCostFromMultipliers(final ExpressionsBasedModel model, final int variableIndex, final Result solverState) {

            IntIndex key = new IntIndex(variableIndex);
            double rc = model.objective().doubleValue(key, false);

            for (EntryPair.KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> kp : solverState.getMatchedMultipliers()) {
                ModelEntity<?> entity = kp.getKey().getKey();
                if (entity instanceof Expression) {
                    Expression expression = (Expression) entity;
                    if (expression.getLinearKeySet().contains(key)) {
                        rc -= expression.doubleValue(key, false) * kp.doubleValue();
                    }
                }
            }

            return rc;
        }

        protected static Result expandFreeToFull(final Result solverState, final ExpressionsBasedModel model, final DenseArray.Factory<?, ?> factory) {
            return ExpressionsBasedModel.Integration.expandFreeToFull(solverState, model, factory, Optional.empty(), Optimisation.Sense.MIN);
        }

        protected static Result expandFreeToFull(final Result solverState, final ExpressionsBasedModel model, final DenseArray.Factory<?, ?> factory,
                final Optional<Supplier<Access1D<?>>> reducedGradient) {
            return ExpressionsBasedModel.Integration.expandFreeToFull(solverState, model, factory, reducedGradient, Optimisation.Sense.MIN);
        }

        /**
         * @param solverSense the {@link Optimisation.Sense} the solver internally optimises in (usually fixed
         *                    per solver — most simplex/QP solvers minimise). If this differs from the model's
         *                    {@link ExpressionsBasedModel#getOptimisationSense() optimisation sense}, the
         *                    reduced gradient values are negated when mapped back to the model so that
         *                    callers always see them in the model's sense.
         */
        protected static Result expandFreeToFull(final Result solverState, final ExpressionsBasedModel model, final DenseArray.Factory<?, ?> factory,
                final Optional<Supplier<Access1D<?>>> reducedGradient, final Optimisation.Sense solverSense) {

            List<Variable> freeVariables = model.getFreeVariables();
            Set<IntIndex> fixedVariables = model.getFixedVariables();
            int nbFreeVars = freeVariables.size();
            int nbModelVars = model.countVariables();

            DenseArray<?> modelSolution = factory.make(nbModelVars);

            if (modelSolution.isPrimitive()) {
                for (int i = 0; i < nbFreeVars; i++) {
                    modelSolution.set(model.indexOf(freeVariables.get(i)), solverState.doubleValue(i));
                }
            } else {
                for (int i = 0; i < nbFreeVars; i++) {
                    modelSolution.set(model.indexOf(freeVariables.get(i)), solverState.get(i));
                }
            }

            for (IntIndex fixed : fixedVariables) {
                modelSolution.set(fixed.index, model.getVariable(fixed.index).getValue());
            }

            Result retVal = solverState.withSolution(modelSolution);

            if (reducedGradient.isPresent()) {

                Optimisation.Sense modelSense = model.getOptimisationSense();
                boolean negate = solverSense != null && modelSense != null && solverSense != modelSense;

                Supplier<Access1D<?>> gradientSupplier = () -> {
                    DenseArray<?> fullGradient = factory.make(nbModelVars);
                    Access1D<?> freeGradient = reducedGradient.get().get();
                    for (int i = 0; i < nbFreeVars; i++) {
                        double v = freeGradient.doubleValue(i);
                        fullGradient.set(model.indexOf(freeVariables.get(i)), negate ? -v : v);
                    }
                    // Variables eliminated by presolve aren't seen by the solver, so their reduced cost
                    // must be reconstructed from first principles: rc_v = c_v - Σ a_iv · λ_i.
                    for (IntIndex fixed : fixedVariables) {
                        double rc = ExpressionsBasedModel.Integration.computeReducedCostFromMultipliers(model, fixed.index, solverState);
                        fullGradient.set(fixed.index, negate ? -rc : rc);
                    }
                    return fullGradient;
                };

                retVal = retVal.withReducedGradient(gradientSupplier);
            }

            return retVal;
        }

        protected static int getIndexOfFreeInSolver(final ExpressionsBasedModel model, final Variable variable) {
            return model.indexOfFreeVariable(variable);
        }

        protected final static boolean isSwitch(final ExpressionsBasedModel model, final IntegrationProperty property) {
            return model.isIntegrationSwitch(property);
        }

        protected static Result reduceFullToFree(final Result modelState, final ExpressionsBasedModel model, final DenseArray.Factory<?, ?> factory) {

            List<Variable> freeVariables = model.getFreeVariables();
            int nbFreeVars = freeVariables.size();

            DenseArray<?> solverSolution = factory.make(nbFreeVars);

            for (int i = 0; i < nbFreeVars; i++) {
                Variable variable = freeVariables.get(i);
                int modelIndex = model.indexOf(variable);
                solverSolution.set(i, modelState.doubleValue(modelIndex));
            }

            return modelState.withSolution(solverSolution);
        }

        protected final static void setSwitch(final ExpressionsBasedModel model, final IntegrationProperty property, final boolean value) {
            model.setIntegrationSwitch(property, value);
        }

        /**
         * @deprecated v57 Not needed. Use
         *             {@link #prepareSolverCandidate(org.ojalgo.optimisation.Optimisation.Result, ExpressionsBasedModel)}
         *             instead
         */
        @Deprecated
        @Override
        public final Result extractSolverState(final ExpressionsBasedModel model) {
            return this.toSolverState(model.getVariableValues(), model);
        }

        /**
         * Preserves the historical kick-starter behaviour: when no candidate is supplied one is derived
         * (cheaply) from the model, then converted to solver state. Solvers that ignore the kick-starter
         * override this to always return {@code null}.
         */
        @Override
        public Result prepareSolverCandidate(final Result candidateModelState, final ExpressionsBasedModel model) {
            Result modelState = candidateModelState != null ? candidateModelState : model.getVariableValues();
            return this.toSolverState(modelState, model);
        }

        /**
         * The reverse of {@link #toSolverState(Optimisation.Result, ExpressionsBasedModel)}.
         *
         * @see #reduceFullToFree(Optimisation.Result, ExpressionsBasedModel, DenseArray.Factory)
         * @see #expandFreeToFull(Optimisation.Result, ExpressionsBasedModel, DenseArray.Factory, Optional)
         */
        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            return solverState;
        }

        /**
         * This default implementation assumes the solver state and model state are identical, and simply
         * returns the supplied model state.
         * <p>
         * In any case where the set of variables present in the solver does not match what's in the model
         * one-to-one, this method and its reciprocal
         * {@link #toModelState(Optimisation.Result, ExpressionsBasedModel)} needs to be overridden with
         * custom mapping implementations.
         * <p>
         * A very common case is when the solver only works with free (not eliminated by the pre-solver)
         * variables. There are helper methods to do just that.
         *
         * @see #reduceFullToFree(Optimisation.Result, ExpressionsBasedModel, DenseArray.Factory)
         * @see #expandFreeToFull(Optimisation.Result, ExpressionsBasedModel, DenseArray.Factory, Optional)
         */
        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            return modelState;
        }

        /**
         * Use this to limit the cases where this {@link Integration} would be used.
         * <p>
         * Returns a new Integration instance where the supplied {@link Predicate} needs to test true in
         * addition to the underlying {@link #isCapable(Optimisation.Model)}.
         */
        public final ExpressionsBasedModel.Integration<S> withCapabilityPredicate(final Predicate<ExpressionsBasedModel> capabilityPredicate) {
            return new ConfiguredIntegration<>(this, capabilityPredicate, null);
        }

        /**
         * Intercept and modify the {@link Optimisation.Options} instance before building the solver.
         */
        public final ExpressionsBasedModel.Integration<S> withOptionsModifier(final Consumer<Optimisation.Options> optionsModifier) {
            return new ConfiguredIntegration<>(this, null, optionsModifier);
        }

        /**
         * This implementation returns the index of the variable in the model. Override if you need something
         * else. The utility {@link #getIndexOfFreeInSolver(ExpressionsBasedModel, Variable)} can be used if
         * the solver only works with free variables.
         *
         * @return The index with which one can reference parameters related to this variable in the solver.
         */
        protected int getIndexInSolver(final ExpressionsBasedModel model, final Variable variable) {
            return variable.getIndex().index;
        }

        /**
         * Some solvers are hard-wired to solve either min or max problems. (Typically ojAlgo's built-in
         * solvers are implemented that way.) If that's the case this method should indicate what the
         * convention is. Returning null indicates there is no convention/hard-wire, and the solver will
         * either min or max depending what's specified.
         * <p>
         * When the solver's sense is hard-wired some things, like the sign of the optimal value or the
         * reduced costs, may have to be adjusted.
         * <p>
         * Since this was a late addition to this class a default implementation was needed, and null is
         * commonly the correct return value for 3:d party solvers.
         */
        protected Optimisation.Sense getSolverSense() {
            return null;
        }

    }

    /**
     * Various switches that can be set by solver integrations to control its own behaviour. Typically used
     * when a single integration is a facade delegating to a set of (switching between a pair of) other
     * integrations.
     * <p>
     * The various properties are here for very specific use cases, but have been given generic names to
     * encourage reuse.
     * <p>
     * Solver integrations are (absolutely have to be) stateless. If they contain logic that cause them to
     * take different paths, information about that needs to be stored in the model (or solver) instance.
     * That's what these properties are for.
     *
     * @see ExpressionsBasedModel.Integration#setSwitch(ExpressionsBasedModel, IntegrationProperty, boolean)
     * @see ExpressionsBasedModel.Integration#isSwitch(ExpressionsBasedModel, IntegrationProperty)
     */
    public enum IntegrationProperty {

        /**
         * Any integration that can switch between an active-set method and an ADMM method.
         * <p>
         * ACTIVE_SET==false, ADMM==true
         */
        ACTIVE_SET_OR_ADMM,
        /**
         * Any integration that can switch between Java and native code solvers.
         * <p>
         * JAVA==false, NATIVE==true
         */
        JAVA_OR_NATIVE_CODE,
        /**
         * Any integration that can handle both LP and QP models.
         * <p>
         * LP==false, QP==true
         */
        LP_OR_QP,
        /**
         * Any LP solver integration that can switch between primal and dual algorithm implementations.
         * <p>
         * PRIMAL==false, DUAL==true
         */
        PRIMAL_OR_DUAL_LP,
        /**
         * Something temporary or experimental that does not yet have a specific constant.
         */
        TEMPORARY;

    }

    public static abstract class Presolver extends Simplifier<Expression, Presolver> {

        protected Presolver(final int executionOrder) {
            super(executionOrder);
        }

        /**
         * @return True if any model entity was modified so that a re-run of the presolvers is necessary -
         *         typically when/if a variable was fixed.
         */
        public abstract boolean simplify(Expression expression, Set<IntIndex> remaining, BigDecimal lower, BigDecimal upper, NumberContext precision);

        @Override
        protected boolean isApplicable(final Expression target) {
            return target.isConstraint() && !target.isInfeasible() && !target.isRedundant() && target.countQuadraticFactors() == 0;
        }

    }

    public static abstract class Simplifier<ME extends ModelEntity<?>, S extends Simplifier<?, ?>> implements Comparable<S> {

        private final int myExecutionOrder;
        private final UUID myUUID = UUID.randomUUID();

        Simplifier(final int executionOrder) {
            super();
            myExecutionOrder = executionOrder;
        }

        @Override
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
            return prime * result + (myUUID == null ? 0 : myUUID.hashCode());
        }

        protected abstract boolean isApplicable(final ME target);

        final int getExecutionOrder() {
            return myExecutionOrder;
        }

    }

    public static abstract class VariableAnalyser extends Simplifier<Variable, VariableAnalyser> {

        protected VariableAnalyser(final int executionOrder) {
            super(executionOrder);
        }

        public abstract void simplify(Variable target, ExpressionsBasedModel model);

    }

    static final class DefaultIntermediate extends IntermediateSolver {

        DefaultIntermediate(final ExpressionsBasedModel model) {
            super(model);
        }

    }

    static final class IntegrationWrapper extends ExpressionsBasedModel.Integration<Optimisation.Solver> {

        private final Optimisation.Integration<ExpressionsBasedModel, ?> myDelegate;

        IntegrationWrapper(final Optimisation.Integration<ExpressionsBasedModel, ?> delegate) {
            super();
            myDelegate = delegate;
        }

        @Override
        public Solver build(final ExpressionsBasedModel model) {
            return myDelegate.build(model);
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return myDelegate.isCapable(model);
        }

        @Override
        public Result prepareSolverCandidate(final Result candidateModelState, final ExpressionsBasedModel model) {
            return myDelegate.prepareSolverCandidate(candidateModelState, model);
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            return myDelegate.toModelState(solverState, model);
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            return myDelegate.toSolverState(modelState, model);
        }

        @Override
        protected Sense getSolverSense() {
            return null;
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

    private static final String NEW_LINE = "\n";
    private static final String OBJ_FUNC_AS_CONSTR_KEY = UUID.randomUUID().toString();
    private static final String OBJECTIVE = "Generated/Aggregated Objective";
    private static final String START_END = "############################################\n";

    static {
        ExpressionsBasedModel.resetPresolvers();
    }

    /**
     * Register a solver integration with the default {@link Optimisation#ENVIRONMENT}. Models created with
     * the no-arg constructor will use this integration. For isolated configuration, register integrations on
     * a separate {@link Optimisation.Environment} instead.
     *
     * @see Optimisation.Environment#addIntegration(Integration)
     */
    public static boolean addIntegration(final Optimisation.Integration<ExpressionsBasedModel, ?> integration) {
        if (integration instanceof ExpressionsBasedModel.Integration<?>) {
            return Optimisation.ENVIRONMENT.addIntegration((ExpressionsBasedModel.Integration<?>) integration);
        } else {
            return Optimisation.ENVIRONMENT.addIntegration(new IntegrationWrapper(integration));
        }
    }

    /**
     * Delegates to {@link Optimisation#ENVIRONMENT}. For isolated configuration use a separate
     * {@link Optimisation.Environment}.
     *
     * @see Optimisation.Environment#addPresolver(Presolver)
     */
    public static boolean addPresolver(final ExpressionsBasedModel.Simplifier<?, ?> presolver) {
        return Optimisation.ENVIRONMENT.addPresolver(presolver);
    }

    /**
     * Delegates to {@link Optimisation#ENVIRONMENT}.
     *
     * @see Optimisation.Environment#clearIntegrations()
     */
    public static void clearIntegrations() {
        Optimisation.ENVIRONMENT.clearIntegrations();
    }

    /**
     * Delegates to {@link Optimisation#ENVIRONMENT}.
     *
     * @see Optimisation.Environment#clearPresolvers()
     */
    public static void clearPresolvers() {
        Optimisation.ENVIRONMENT.clearPresolvers();
    }

    /**
     * Internal heuristic for solver selection. Not intended for external use.
     */
    public static boolean isNative(final ExpressionsBasedModel model) {

        boolean mip = model.isAnyVariableInteger();

        boolean qp = model.isAnyExpressionQuadratic();

        if (mip) {
            if (qp) {
                return false;
            } else {
                return model.countVariables() > 50 || model.countExpressions() > 40;
            }
        } else if (qp) {
            return model.countVariables() > 200 || model.countExpressions() > 200;
        } else {
            return model.countVariables() > 500 || model.countExpressions() > 400;
        }
    }

    /**
     * Apart from the "native" EBM file format, currently only supports the MPS file format, but with some of
     * the various extensions. In particular it is possible to parse QP models using QUADOBJ or QMATRIX file
     * sections.
     */
    public static ExpressionsBasedModel parse(final File file) {

        FileFormat fileFormat = FileFormat.from(file);

        try (FileInputStream input = new FileInputStream(file)) {
            return ExpressionsBasedModel.parse(input, fileFormat);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static ExpressionsBasedModel parse(final InputStream input, final FileFormat format) {
        switch (format) {
            case MPS:
                return FileFormatMPS.read(input);
            case EBM:
                return FileFormatEBM.read(input);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Delegates to {@link Optimisation#ENVIRONMENT}.
     *
     * @see Optimisation.Environment#removeIntegration(Integration)
     */
    public static boolean removeIntegration(final Integration<?> integration) {
        return Optimisation.ENVIRONMENT.removeIntegration(integration);
    }

    /**
     * Delegates to {@link Optimisation#ENVIRONMENT}.
     *
     * @see Optimisation.Environment#removePresolver(Presolver)
     */
    public static boolean removePresolver(final Presolver presolver) {
        return Optimisation.ENVIRONMENT.removePresolver(presolver);
    }

    /**
     * Resets the default environment's presolvers to the built-in set, removing any previously registered
     * presolvers.
     */
    public static void resetPresolvers() {
        Optimisation.ENVIRONMENT.resetPresolvers();
    }

    /**
     * Delegates to {@link Optimisation#ENVIRONMENT}.
     *
     * @see Optimisation.Environment#setConfigurator(Object)
     */
    public static void setConfigurator(final Object configurator) {
        Optimisation.ENVIRONMENT.setConfigurator(configurator);
    }

    public final Optimisation.Options options;

    private final Optimisation.Environment myEnvironment;
    private final Map<String, Expression> myExpressions = new HashMap<>();
    private final Set<IntIndex> myFixedVariables = new HashSet<>();
    private transient ExpressionsBasedModel.Integration<?> myForcedIntegration = null;
    private transient boolean myInfeasible = false;
    private final EnumBitSet<IntegrationProperty> myIntegrationProperties = new EnumBitSet<>();
    private Optimisation.Result myKnownSolution = null;
    private BigDecimal myObjectiveConstant = BigMath.ZERO;
    private Optimisation.Sense myOptimisationSense = null;
    private final Set<IntIndex> myReferences;
    private boolean myRelaxed;
    /**
     * A shallow copy may share complex/large data structures with other models - typically the Maps holding
     * Expression parameters.
     */
    private final boolean myShallowCopy;
    /**
     * Temporary storage for some expression specific subset of variables
     */
    private final Set<IntIndex> myTemporary = new HashSet<>();
    private final ArrayList<Variable> myVariables = new ArrayList<>();
    private final VariablesCategorisation myVariablesCategorisation = new VariablesCategorisation();

    /**
     * Creates a model using the default {@link Optimisation#ENVIRONMENT} and default
     * {@link Optimisation.Options}. For isolated configuration use
     * {@link Optimisation.Environment#newModel()}.
     */
    public ExpressionsBasedModel() {
        this(Optimisation.ENVIRONMENT, new Optimisation.Options());
    }

    /**
     * Creates a model using the default {@link Optimisation#ENVIRONMENT} with the supplied options. For
     * isolated configuration use {@link Optimisation.Environment#newModel(Optimisation.Options)}.
     */
    public ExpressionsBasedModel(final Optimisation.Options optimisationOptions) {
        this(Optimisation.ENVIRONMENT, optimisationOptions);
    }

    ExpressionsBasedModel(final ExpressionsBasedModel modelToCopy, final boolean shallow, final boolean prune) {

        super();

        myEnvironment = modelToCopy.getEnvironment();
        options = modelToCopy.options;

        this.setOptimisationSense(modelToCopy.getOptimisationSense());
        this.addObjectiveConstant(modelToCopy.getObjectiveConstant());

        for (Variable tmpVar : modelToCopy.getVariables()) {
            myVariables.add(tmpVar.copy());
        }

        Set<IntIndex> fixedVariables = modelToCopy.getFixedVariables();

        myReferences = modelToCopy.getReferences();

        myShallowCopy = shallow || modelToCopy.isShallowCopy();
        myRelaxed = modelToCopy.isRelaxed();
        myKnownSolution = modelToCopy.getKnownSolution(); // TODO Should this be copied?

        for (Expression tmpExpr : modelToCopy.getExpressions()) {
            if (shallow) {
                if (prune) {
                    if (tmpExpr.isObjective() || tmpExpr.isConstraint() && (!tmpExpr.isRedundant() || tmpExpr.isInfeasible())) {
                        myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, false));
                    }
                } else {
                    myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, false));
                }
            } else {
                if (prune) {
                    if (tmpExpr.isObjective() || tmpExpr.isConstraint() && (!tmpExpr.isRedundant() || tmpExpr.isInfeasible())) {
                        myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, true).compensate(fixedVariables));
                    }
                } else {
                    myExpressions.put(tmpExpr.getName(), tmpExpr.copy(this, true));
                }
            }
        }
    }

    ExpressionsBasedModel(final Optimisation.Environment optimisationEnvironment, final Optimisation.Options optimisationOptions) {

        super();

        myEnvironment = optimisationEnvironment;

        options = optimisationOptions;

        myReferences = new HashSet<>();

        myShallowCopy = false;
        myRelaxed = false;
    }

    public Expression addExpression() {
        return this.newExpression("EXPR" + myExpressions.size());
    }

    public Expression addExpression(final String name) {
        return this.newExpression(name);
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

        IntIndex[] sequence = new IntIndex[orderedSet.size()];
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
     * <li>A simple expression measuring the sum of the (binary) variable values (the number of binary
     * variables that are "ON"). The upper, and optionally lower, limits are set as defined by the
     * <code>max</code> and <code>min</code> parameter values.
     * <li>A custom presolver (specific to this SOS) to be used by the MIP solver. This presolver helps to
     * keep track of which combinations of variable values are feasible, and is the only thing that enforces
     * the order.
     * </ol>
     *
     * @param orderedSet The set members in correct order. Each of these variables must be binary.
     * @param min        The minimum number of binary variables in the set that must be "ON" (Set this to 0 if
     *                   there is no minimum.)
     * @param max        The SOS type or maximum number of binary variables in the set that may be "ON"
     */
    public void addSpecialOrderedSet(final Collection<Variable> orderedSet, final int min, final int max) {

        if (max <= 0 || min > max) {
            throw new ProgrammingError("Invalid min/max number of ON variables!");
        }

        final String name = "SOS" + max + "-" + orderedSet.toString();

        final Expression expression = this.newExpression(name);

        for (final Variable variable : orderedSet) {
            if (variable == null || variable.getIndex() == null || !variable.isBinary()) {
                throw new ProgrammingError("Variables must be binary and already inserted in the model!");
            }
            expression.doSet(variable.getIndex(), ONE);
        }

        expression.upper(BigDecimal.valueOf(max));
        if (min > 0) {
            expression.lower(BigDecimal.valueOf(min));
        }

        this.addSpecialOrderedSet(orderedSet, max, expression);
    }

    public Variable addVariable() {
        return this.newVariable("X" + myVariables.size());
    }

    public Variable addVariable(final String name) {
        return this.newVariable(name);
    }

    /**
     * @return A prefiltered stream of variables that are constraints and not fixed
     */
    public Stream<Variable> bounds() {
        return this.variables().filter(v -> v.isConstraint() && !v.isFixed());
    }

    /**
     * See {@link Presolvers#checkSimilarity(Collection, Expression)}.
     */
    public boolean checkSimilarity(final Expression potential) {
        return Presolvers.checkSimilarity(myExpressions.values(), potential);
    }

    /**
     * Returns a prefiltered stream of expressions that are constraints and have not been marked as redundant.
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

    /**
     * Counts variables and expressions of different categories.
     */
    public Description describe() {

        int nbUpCnstr = 0;
        int nbUpBound = 0;
        int nbLoCnstr = 0;
        int nbLoBound = 0;
        int nbEqCnstr = 0;
        int nbEqBound = 0;

        for (Variable variable : myVariables) {
            if (variable.isLowerConstraint()) {
                nbLoBound++;
            }
            if (variable.isUpperConstraint()) {
                nbUpBound++;
            }
            if (variable.isEqualityConstraint()) {
                nbEqBound++;
            }
        }

        for (Expression expression : myExpressions.values()) {
            if (expression.isLowerConstraint()) {
                nbLoCnstr++;
            }
            if (expression.isUpperConstraint()) {
                nbUpCnstr++;
            }
            if (expression.isEqualityConstraint()) {
                nbEqCnstr++;
            }
        }

        VariablesCategorisation variablesCategorisation = new VariablesCategorisation();
        int nbTotVars = myVariables.size();
        int nbIntVars = variablesCategorisation.getIntegerVariables(myVariables).size();
        int nbPosVars = variablesCategorisation.getPositiveVariables(myVariables).size();
        int nbNegVars = variablesCategorisation.getNegativeVariables(myVariables).size();

        return new Description(nbTotVars, nbPosVars, nbNegVars, nbIntVars, nbLoBound, nbUpBound, nbEqBound, nbLoCnstr, nbUpCnstr, nbEqCnstr);
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

    /**
     * Returns a configurator that is of the same type as {@code defaultValue} or a subclass thereof. If no
     * such configurator exists, returns {@code defaultValue}.
     */
    public <T> T getConfigurator(final T defaultValue) {
        return myEnvironment.getConfigurator(defaultValue);
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
     * @return A list of the variables that are not fixed at a specific value and whose range includes
     *         negative values
     */
    public List<Variable> getNegativeVariables() {
        return Collections.unmodifiableList(myVariablesCategorisation.getNegativeVariables(myVariables));
    }

    /**
     * <ol>
     * <li>The default optimisation sense is {@link Optimisation.Sense#MIN}
     * <li>If this model was read from a file and that file format contained information about being a
     * minimisation or maximisation model, that info is reflected here.
     * <li>In general you are expected to know whether to call {@link #minimise()} or {@link #maximise()}.
     * Once you have called one of those methods this method's return value will match that.
     */
    public Optimisation.Sense getOptimisationSense() {
        return myOptimisationSense;
    }

    /**
     * @return A list of the variables that are not fixed at a specific value and whose range includes
     *         positive values and/or zero
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

    /**
     * Cheaply extract the current variable values (null replaced by a sensible bound/derived value). Does NOT
     * validate against the constraints and does NOT evaluate the objective — the returned state is always
     * {@link State#UNEXPLORED} and the value {@code NaN}. Use {@link #getVariableValuesValidated()} (or
     * {@link #getVariableValues(NumberContext)}) when the feasibility state / objective value matter.
     */
    public Optimisation.Result getVariableValues() {

        int nbVars = myVariables.size();

        State retState = State.UNEXPLORED;
        double retValue = Double.NaN;
        final Array1D<BigDecimal> retSolution = Array1D.R256.make(nbVars);

        boolean allVarsSomeInfo = true;

        for (int i = 0; i < nbVars; i++) {
            Variable variable = myVariables.get(i);

            if (variable.getValue() != null) {
                retSolution.set(i, variable.getValue());
            } else if (variable.isEqualityConstraint()) {
                retSolution.set(i, variable.getLowerLimit());
            } else if (variable.isLowerLimitSet() && variable.isUpperLimitSet()) {
                retSolution.set(i, BigMath.DIVIDE.invoke(variable.getLowerLimit().add(variable.getUpperLimit()), TWO));
            } else if (variable.isLowerLimitSet()) {
                retSolution.set(i, variable.getLowerLimit());
            } else if (variable.isUpperLimitSet()) {
                retSolution.set(i, variable.getUpperLimit());
            } else {
                retSolution.set(i, ZERO);
                allVarsSomeInfo = false; // This variable no info
            }
        }

        if (allVarsSomeInfo) {
            retState = State.APPROXIMATE;
        } else {
            retState = State.UNEXPLORED;
        }

        return new Optimisation.Result(retState, retValue, retSolution);
    }

    /**
     * Null variable values are replaced with 0.0. If any variable value is null the state is set to
     * INFEASIBLE even if zero would actually be a feasible value. The objective function value is not
     * calculated for infeasible variable values.
     */
    public Optimisation.Result getVariableValues(final NumberContext validationContext) {

        Optimisation.Result result = this.getVariableValues();
        boolean approximate = result.getState().isApproximate();

        if (approximate) {
            if (this.validate(result, validationContext, BasicLogger.NULL)) {
                return result.withState(State.FEASIBLE).withValue(this.objective().evaluate(result).doubleValue());
            } else {
                return result.withState(State.APPROXIMATE);
            }
        } else {
            return result.withState(State.INFEASIBLE);
        }
    }

    /**
     * The validated counterpart of {@link #getVariableValues()}: derives the values, then validates them
     * against the constraints and evaluates the objective, returning the corresponding FEASIBLE / APPROXIMATE
     * / INFEASIBLE state and objective value. This is the behaviour {@code getVariableValues()} had before it
     * was split into a cheap extractor and this validated variant.
     */
    public Optimisation.Result getVariableValuesValidated() {
        return this.getVariableValues(options.feasibility);
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

    /**
     * Return true if any variable is (was originally) declared integer. When/if the model relaxes the integer
     * constraints, this will still return true.
     */
    public boolean isAnyVariableDeclaredInteger() {

        boolean retVal = false;

        for (int i = 0, limit = myVariables.size(); !retVal && i < limit; i++) {
            Variable variable = myVariables.get(i);
            retVal |= variable.isInteger() && !variable.isFixed();
        }

        return retVal;
    }

    public boolean isAnyVariableFixed() {
        return myVariables.stream().anyMatch(Variable::isFixed);
    }

    /**
     * If the model has relaxed the integer constraints this will return false, otherwise the same as
     * {@link #isAnyVariableDeclaredInteger()}.
     */
    public boolean isAnyVariableInteger() {

        if (myRelaxed) {
            return false;
        } else {
            return this.isAnyVariableDeclaredInteger();
        }
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
            constrExpr.tighten();
        }

        return constrExpr != null ? constrExpr : this.objective();
    }

    @Override
    public Optimisation.Result maximise() {
        return this.optimise(Optimisation.Sense.MAX, null);
    }

    public <S extends Optimisation.Solver> Optimisation.Result maximise(final Integration<S> forcedIntegration) {
        return this.optimise(Optimisation.Sense.MAX, forcedIntegration);
    }

    @Override
    public Optimisation.Result minimise() {
        return this.optimise(Optimisation.Sense.MIN, null);
    }

    public <S extends Optimisation.Solver> Optimisation.Result minimise(final Integration<S> forcedIntegration) {
        return this.optimise(Optimisation.Sense.MIN, forcedIntegration);
    }

    public Expression newExpression(final String name) {
        return this.newExpression(name, myEnvironment.getExpressionFactory());
    }

    public <E extends Expression> E newExpression(final String name, final Expression.Factory<E> factory) {

        E retVal = factory.make(name, this);

        myExpressions.put(name, retVal);

        return retVal;
    }

    public Variable newVariable(final String name) {
        return this.newVariable(name, myEnvironment.getVariableFactory());
    }

    public <V extends Variable> V newVariable(final String name, final Variable.Factory<V> factory) {

        if (myShallowCopy) {
            throw new IllegalStateException("This model is a work copy - its set of variables cannot be modified!");
        }

        V retVal = factory.make(name, myVariables.size());
        myVariables.add(retVal);
        return retVal;
    }

    /**
     * This is generated on demand – you should not cache this. More specifically, modifications made to this
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
                boolean tmpNotOne = tmpContributionWeight.compareTo(ONE) != 0; // To avoid multiplication by
                                                                               // 1.0

                if (tmpExpression.isAnyLinearFactorNonZero()) {
                    for (IntIndex tmpKey : tmpExpression.getLinearKeySet()) {
                        tmpOldVal = retVal.get(tmpKey);
                        tmpDiff = tmpExpression.get(tmpKey);
                        tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                        retVal.doSet(tmpKey, tmpNewVal);
                    }
                }

                if (tmpExpression.isAnyQuadraticFactorNonZero()) {
                    for (IntRowColumn tmpKey : tmpExpression.getQuadraticKeySet()) {
                        tmpOldVal = retVal.get(tmpKey);
                        tmpDiff = tmpExpression.get(tmpKey);
                        tmpNewVal = tmpOldVal.add(tmpNotOne ? tmpContributionWeight.multiply(tmpDiff) : tmpDiff);
                        retVal.doSet(tmpKey, tmpNewVal);
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
     * <p>
     * The primary use case for this method is as a callback method for solvers that iteratively modifies the
     * model and solves at each iteration point.
     * <p>
     * With direct usage of this method:
     * <ul>
     * <li>Maximisation/Minimisation is undefined (you don't know which it is)
     * <li>The solution is not written back to the model
     * <li>The solution is not validated by the model
     * </ul>
     */
    public <T extends IntermediateSolver> T prepare(final Function<ExpressionsBasedModel, T> factory) {
        return factory.apply(this);
    }

    /**
     * Will try to identify constraints with equal variable sets, and check if those can be combined or not.
     * This is a relatively slow process with small chance to actually achieve anything. Therefore it is not
     * part of the default pre-solve and {@link #simplify()} functionality.
     * <p>
     * This is an in-place operation. The returned model is the same as this – just to allow chained
     * invocation.
     *
     * @see Presolvers#reduce(Collection)
     */
    public ExpressionsBasedModel reduce() {

        List<Expression> compensated = this.constraints().map(expr -> expr.compensate(this.getFixedVariables())).collect(Collectors.toList());

        if (Presolvers.reduce(compensated)) {
            for (Expression compensatedExpression : compensated) {
                if (compensatedExpression.isRedundant()) {
                    myExpressions.get(compensatedExpression.getName()).setRedundant();
                }
            }
        }

        return this;
    }

    public void relax() {
        this.relax(false);
    }

    /**
     * @param soft If true the integer variables are still identified as such, but the model is flagged as
     *             non-integer (will not use the {@link IntegerSolver}, but presolve and validation may still
     *             recognise the variables' integer property). If false the integer property of any/all
     *             variables are removed.
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

    public void removeExpression(final String name) {
        myExpressions.remove(name);
    }

    /**
     * Will perform pre-solve and then create a copy removing redundant constraint expressions, and pruning
     * the remaining ones to no longer include fixed variables.
     * <p>
     * Note that the fixed variables themselves are not removed. They are still present, but fixed, and not
     * used in any expression.
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

        for (Variable variable : myVariables) {
            variable.appendToString(retVal, options.print);
            retVal.append(NEW_LINE);
        }

        Result solution = this.getVariableValues();
        for (Expression expression : myExpressions.values()) {
            expression.appendToString(retVal, solution, options.print);
            retVal.append(NEW_LINE);
        }

        return retVal.append(START_END).toString();
    }

    /**
     * Validates model construction only. The other validate(...) methods validate the solution.
     *
     * @see Optimisation.Model#validate()
     */
    @Override
    public boolean validate() {

        final BasicLogger appender = options.logger_detailed ? options.logger_appender : BasicLogger.NULL;

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
        NumberContext context = options.feasibility;
        BasicLogger appender = options.logger_detailed && options.logger_appender != null ? options.logger_appender : BasicLogger.NULL;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final Access1D<BigDecimal> solution, final BasicLogger appender) {
        NumberContext context = options.feasibility;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context) {
        BasicLogger appender = options.logger_detailed && options.logger_appender != null ? options.logger_appender : BasicLogger.NULL;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context, final BasicLogger appender) {

        ProgrammingError.throwIfNull(solution, context);

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

    public boolean validate(final BasicLogger appender) {
        final NumberContext context = options.feasibility;
        final Result solution = this.getVariableValues(context);
        return this.validate(solution, context, appender);
    }

    public boolean validate(final NumberContext context) {
        Result solution = this.getVariableValues(context);
        BasicLogger appender = options.logger_detailed && options.logger_appender != null ? options.logger_appender : BasicLogger.NULL;
        return this.validate(solution, context, appender);
    }

    public boolean validate(final NumberContext context, final BasicLogger appender) {
        final Access1D<BigDecimal> solution = this.getVariableValues(context);
        return this.validate(solution, context, appender);
    }

    /**
     * Returns a stream of variables that are not fixed.
     */
    public Stream<Variable> variables() {
        return myVariables.stream().filter(v -> !v.isEqualityConstraint());
    }

    /**
     * Save this instance to file. The file format is {@link FileFormat#EBM} and the file name is therefore
     * recommended to end with ".ebm".
     *
     * @param file The path/name of the file to write.
     */
    public void writeTo(final File file) {
        ToFileWriter.mkdirs(file.getParentFile());
        try (FileOutputStream output = new FileOutputStream(file)) {
            FileFormatEBM.write(this, output);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public void writeTo(final InMemoryFile file) {
        try (OutputStream output = file.newOutputStream()) {
            FileFormatEBM.write(this, output);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    private Optimisation.Result optimise(final Optimisation.Sense sense, final Integration<?> forcedIntegration) {

        this.setOptimisationSense(sense);
        myForcedIntegration = forcedIntegration;

        if (!myShallowCopy && myEnvironment.countPresolvers() > 0) {
            this.scanEntities();
        }

        DefaultIntermediate prepared = this.prepare(DefaultIntermediate::new);

        Optimisation.Result result = prepared.solve(null);

        if (result.getState().isApproximate()) {
            for (int i = 0, limit = myVariables.size(); i < limit; i++) {
                Variable tmpVariable = myVariables.get(i);
                if (!tmpVariable.isFixed()) {
                    tmpVariable.setValue(options.solution.enforce(result.get(i)));
                }
            }
        }

        Optimisation.Result retSolution = this.getVariableValues();

        double retValue = this.objective().evaluate(retSolution).doubleValue();

        prepared.dispose();

        return result.withSolution(retSolution).withValue(retValue);
    }

    private void scanEntities() {

        boolean mip = this.isAnyVariableInteger();

        if (mip) {
            for (Variable tmpVar : myVariables) {
                if (tmpVar.isInteger() && tmpVar.isConstraint()) {
                    tmpVar.doIntegerRounding();
                }
            }
        }

        Set<IntIndex> fixedVariables = this.getFixedVariables();

        for (Simplifier<?, ?> simplifier : myEnvironment.getPresolvers()) {

            if (simplifier instanceof VariableAnalyser) {
                VariableAnalyser analyser = (VariableAnalyser) simplifier;

                for (Variable tmpVar : myVariables) {
                    if (analyser.isApplicable(tmpVar)) {
                        analyser.simplify(tmpVar, this);
                    }
                }

            } else if (simplifier instanceof ExpressionAnalyser) {
                ExpressionAnalyser analyser = (ExpressionAnalyser) simplifier;

                for (Expression tmpExpr : myExpressions.values()) {
                    if (analyser.isApplicable(tmpExpr)) {
                        analyser.simplify(tmpExpr, this);
                    }
                }

            } else if (simplifier instanceof Presolver) {
                Presolver presolver = (Presolver) simplifier;

                for (Expression tmpExpr : myExpressions.values()) {
                    if (presolver.isApplicable(tmpExpr)) {

                        BigDecimal setValue = tmpExpr.calculateSetValue(fixedVariables);

                        BigDecimal lower = tmpExpr.getCompensatedLowerLimit(setValue);
                        BigDecimal upper = tmpExpr.getCompensatedUpperLimit(setValue);

                        myTemporary.clear();
                        myTemporary.addAll(tmpExpr.getLinearKeySet());
                        myTemporary.removeAll(fixedVariables);

                        ((Presolver) simplifier).simplify(tmpExpr, myTemporary, lower, upper, options.feasibility);

                        myTemporary.clear();
                    }
                }

            } else {

                throw new ProgrammingError("Unknown simplifier type: " + simplifier.getClass().getName());
            }
        }

        if (mip) {
            for (Expression tmpExpr : myExpressions.values()) {
                tmpExpr.isInteger();
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

    Optimisation.Environment getEnvironment() {
        return myEnvironment;
    }

    ExpressionsBasedModel.Integration<?> getIntegration() {

        ExpressionsBasedModel.Integration<?> retVal = myForcedIntegration;

        if (retVal == null) {
            for (ExpressionsBasedModel.Integration<?> preferred : myEnvironment.getIntegrations()) {
                if (preferred.isCapable(this)) {
                    retVal = preferred;
                    break;
                }
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
            throw new ProgrammingError("No solver integration available that can handle this model!");
        }

        return retVal;
    }

    Optimisation.Result getKnownSolution() {
        return myKnownSolution;
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
        for (Expression expression : myExpressions.values()) {
            if (expression.isInfeasible()) {
                return myInfeasible = true;
            }
        }
        for (Variable variable : myVariables) {
            if (variable.isInfeasible()) {
                return myInfeasible = true;
            }
        }
        return false;
    }

    boolean isInteger(final Set<IntIndex> variables) {

        if (variables.size() <= 0) {
            return false;
        }

        for (IntIndex index : variables) {
            if (!myVariables.get(index.index).isInteger()) {
                return false;
            }
        }

        return true;
    }

    boolean isIntegrationSwitch(final IntegrationProperty property) {
        return myIntegrationProperties.get(property);
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

    void presolve() {

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

                    needToRepeat |= Presolvers.ZERO_ONE_TWO.simplify(expr, myTemporary, compensatedLowerLimit, compensatedUpperLimit, options.feasibility);
                }
            }
        } while (needToRepeat);

        // Used to be additional code here to specifically check that constraints that have been determined redundant
        // are not infeasible - as that would hide the infeasibility.  Believe this is now handled elsewhere.

        myVariablesCategorisation.update(myVariables);
    }

    void setInfeasible() {
        myInfeasible = true;
    }

    void setIntegrationSwitch(final IntegrationProperty property, final boolean value) {
        myIntegrationProperties.set(property, value);
    }

    void setOptimisationSense(final Optimisation.Sense optimisationSense) {
        myOptimisationSense = optimisationSense;
    }

    IntIndex toIntIndex(final int index) {
        return myVariables.get(index).getIndex();
    }

    IntRowColumn toIntRowColumn(final int row, final int column) {
        return new IntRowColumn(myVariables.get(row).getIndex(), myVariables.get(column).getIndex());
    }

}
