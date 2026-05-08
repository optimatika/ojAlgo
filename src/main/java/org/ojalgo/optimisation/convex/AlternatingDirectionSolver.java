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
package org.ojalgo.optimisation.convex;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.ArrayR256;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.MULTIPLY;
import org.ojalgo.array.operation.NRMINF;
import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ConstraintsMetaData;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.EntityMap;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Equilibrator;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.Structure2D.IntRowColumn;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.ReciprocalPair;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * This is an ojAlgo-based re-implementation of OSQP.
 * <p>
 * As such it targets problems of the form
 *
 * <pre>
 *   minimise   ½ x' P x + q' x
 *   subject to l ≤ A x ≤ u
 * </pre>
 *
 * using alternating-direction updates on a regularised KKT system. A sparse factorisation is reused across
 * iterations while primal/dual residuals and penalty parameters are updated in an ADMM-like fashion. Compared
 * to the C reference implementation, some things are done differently and not all features and configurations
 * are present/supported. It does however follow the core concepts described in the original paper.
 *
 * @see https://web.stanford.edu/~boyd/papers/pdf/osqp.pdf
 * @see https://osqp.org/
 */
final class AlternatingDirectionSolver extends ConvexSolver implements UpdatableSolver {

    static final class Composer<N extends Comparable<N>> {

        private final ColumnsSupplier<N> myA;
        private transient PhysicalStore<N> myFullP = null;
        private final PhysicalStore<N> myL;
        private final ColumnsSupplier<N> myP;
        private final PhysicalStore<N> myQ;
        private final Structure myStructure;
        private final PhysicalStore<N> myU;

        Composer(final PhysicalStore.Factory<N, ?> factory, final int m, final int n, final boolean mapped) {

            super();

            myP = factory.makeColumnsSupplier(n);
            myP.addColumns(n);
            myQ = factory.make(n, 1);

            myA = factory.makeColumnsSupplier(m);
            myA.addColumns(n);
            myL = factory.make(m, 1);
            myU = factory.make(m, 1);

            myStructure = new Structure(m, n, mapped);
        }

        MatrixStore<N> getA() {
            return myA;
        }

        MatrixStore<N> getL() {
            return myL;
        }

        MatrixStore<N> getP() {

            if (myFullP == null) {

                myFullP = myP.physical().make(myP);

                for (int j = 0, n = myP.getColDim(); j < n; j++) {
                    for (NonzeroView<N> nz : myP.getColumn(j).nonzeros()) {
                        long i = nz.index();
                        N value = nz.get();
                        myFullP.set(i, j, value);
                        if (i != j) {
                            myFullP.set(j, i, value);
                        }
                    }
                }
            }

            return myFullP;
        }

        MatrixStore<N> getQ() {
            return myQ;
        }

        Structure getStructure() {
            return myStructure;
        }

        MatrixStore<N> getU() {
            return myU;
        }

        void setConstraint(final int index, final BigDecimal lower, final ModelEntity<?> entity, final BigDecimal upper, final ConstraintType type) {
            myL.set(index, lower);
            myU.set(index, upper);
            myStructure.setConstraintEntry(index, entity, type);
        }

        void setInA(final int row, final int col, final BigDecimal value) {
            myA.set(row, col, value);
        }

        void setInP(final int row, final int col, final BigDecimal value) {
            if (row <= col) {
                myP.add(row, col, value);
            }
            if (col <= row) {
                myP.add(col, row, value);
            }
        }

        void setInQ(final int index, final BigDecimal value) {
            myQ.set(index, value);
        }

        void setVariable(final int index, final Variable variable) {
            myStructure.setVariableEntry(index, variable.getIndex().index);
        }

        Problem toProblem() {
            return new Problem(myP, myQ, myA, myL, myU);
        }

    }

    /**
     * Solver settings and algorithm parameters.
     *
     * @see https://osqp.org/docs/interfaces/solver_settings.html
     */
    abstract static class Configuration {

        /**
         * Absolute iteration ceiling independent of problem size.
         */
        private static final int MAX_ITERATIONS = 8_000;

        /**
         * How often (in iterations) to check termination and consider adapting rho. Higher values reduce
         * overhead but may delay convergence detection.
         */
        private static final int UPDATE_INTERVAL = 25;

        /**
         * Convergence tolerance for optimality: primal and dual residuals (normalised by magnitudes) must be
         * below {@code error(magnitude)} where error ≈ 1e-8 for large magnitudes. This matches the original
         * OSQP default eps_abs=eps_rel=1e-3 after the 1+magnitude scaling.
         */
        static final NumberContext ACCURACY = NumberContext.of(12, 8);

        /**
         * Minimum factor by which a candidate {@code rho} must differ from the current one before a new
         * factorisation is triggered. Increasing this reduces refactorisations but may slow adaptation.
         */
        static final double ADAPTIVE_RHO_TOLERANCE = 5.0;

        static final boolean ADJUSTED = true;

        /**
         * ADMM relaxation parameter α; must lie in (0, 2). Over-relaxation (α > 1) often improves
         * convergence. OSQP default is 1.6; √2 ≈ 1.414 is more conservative.
         */
        static final double ALPHA = PrimitiveMath.SQRT_TWO;
        /**
         * Relaxed accuracy for near-feasible solutions when strict convergence is not achieved. Problems
         * satisfying this tolerance may be returned as APPROXIMATE rather than failing.
         */
        static final NumberContext APPROXIMATE = NumberContext.of(4);

        static final double INFINITY = 1E+32;

        static final BigDecimal INFINITY2 = BigMath.TEN.pow(64);

        /**
         * Base penalty parameter multiplying the constraint residuals. This acts as an immutable initial
         * setting; the runtime value is tracked separately in {@link Work#baseRho}. This value absolutely
         * must lie within {@code [RHO_MIN, RHO_MAX]}.
         */
        static final double RHO = 0.1;
        /**
         * Multiplier applied to base rho for equality constraints. Equality constraints benefit from a much
         * higher penalty to enforce them tightly.
         */
        static final double RHO_EQ_OVER_RHO_INEQ = 1E+4;

        static final double RHO_MAX = 1E+6;

        static final double RHO_MIN = 1E-6;

        /**
         * Tolerance for classifying two bounds as an equality constraint.
         */
        static final double RHO_TOL = 1E-4;

        /**
         * Number of internal Ruiz scaling iterations to apply.
         */
        static final int SCALING_ITERATIONS = 5;

        /**
         * Primal regularisation parameter used on the KKT block for {@code x}.
         */
        static final double SIGMA = 1E-06;

        static final double SMALL = 1e-10;

        /**
         * @return A size-dependent iteration limit. For small problems, a minimum of 400 iterations is
         *         allowed. For larger problems, 1000 × √(m + n) iterations, capped by the user's
         *         {@link Options#iterations_abort} and the absolute ceiling {@link #MAX_ITERATIONS}.
         */
        static int maxIterations(final int m, final int n, final Optimisation.Options options) {

            int max = options.iterations_abort;
            if (max == Integer.MAX_VALUE) {
                max = MAX_ITERATIONS;
            }

            int candidate = (int) (300.0 * Math.sqrt(m + n));
            return Math.min(candidate, max);
        }

        static int updateInterval(final int maxIterations, final Optimisation.Options options) {
            // return (int) Math.sqrt(maxIterations);
            return UPDATE_INTERVAL;
        }

    }

    static final class Integration extends ExpressionsBasedModel.Integration<ConvexSolver> {

        Integration() {
            super();
        }

        @Override
        public ConvexSolver build(final ExpressionsBasedModel model) {

            if (model.options.convex().isExtendedPrecision()) {
                Composer<Quadruple> composer = AlternatingDirectionSolver.build(model, GenericStore.R128);
                return new IterativeRefinementSolver22(model.options, composer);
            } else {
                Composer<Double> composer = AlternatingDirectionSolver.build(model, R064Store.FACTORY);
                return new AlternatingDirectionSolver(composer.toProblem(), model.options, composer.getStructure());
            }
        }

        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger() && model.isAnyObjectiveQuadratic() && !model.isAnyConstraintQuadratic();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            if (model.options.convex().isExtendedPrecision()) {
                return ExpressionsBasedModel.Integration.expandFreeToFull(solverState, model, ArrayR256.FACTORY, solverState.getReducedGradient());
            } else {
                return ExpressionsBasedModel.Integration.expandFreeToFull(solverState, model, ArrayR064.FACTORY, solverState.getReducedGradient());
            }
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            return ExpressionsBasedModel.Integration.reduceFullToFree(modelState, model, ArrayR064.FACTORY);
        }

    }

    /**
     * Immutable problem data: P, q, A, l, u as described in the OSQP formulation.
     */
    static final class Problem implements Structure2D {

        /**
         * Linear constraint matrix A (dimension m × n).
         */
        final R064CSC A;
        /**
         * Lower bounds l on the constraints (length m).
         */
        final double[] l;
        /**
         * Upper-triangular part of the quadratic cost matrix P in CSC format (dimension n × n).
         */
        final R064CSC P;
        /**
         * Linear cost vector q (length n).
         */
        final double[] q;
        /**
         * Upper bounds u on the constraints (length m).
         */
        final double[] u;

        Problem(final ColumnsSupplier<?> P, final PhysicalStore<?> q, final ColumnsSupplier<?> A, final PhysicalStore<?> l, final PhysicalStore<?> u) {

            super();

            this.P = P.toCSC();
            this.q = q.toRawCopy1D();

            this.A = A.toCSC();
            this.l = l.toRawCopy1D();
            this.u = u.toRawCopy1D();
        }

        Problem(final Problem src) {

            super();

            P = src.P.copyCSC();
            q = src.q.clone();

            A = src.A.copyCSC();
            l = src.l.clone();
            u = src.u.clone();
        }

        Problem(final R064CSC P, final double[] q, final R064CSC A, final double[] l, final double[] u) {

            super();

            this.P = P.copyCSC();
            this.q = q.clone();

            this.A = A.copyCSC();
            this.l = l.clone();
            this.u = u.clone();
        }

        Problem(final R064CSC.Builder P, final double[] q, final R064CSC.Builder A, final double[] l, final double[] u) {

            super();

            this.P = P.build();
            this.q = q;

            this.A = A.build();
            this.l = l;
            this.u = u;
        }

        @Override
        public int getColDim() {
            return A.getColDim();
        }

        @Override
        public int getRowDim() {
            return A.getRowDim();
        }
    }

    /**
     * Primal–dual solution pair (x, y).
     */
    static final class Solution {

        /** Primal variables x. */
        final double[] x;
        /** Dual variables y associated with {@code l ≤ A x ≤ u}. */
        final double[] y;

        Solution(final int m, final int n) {
            super();
            x = new double[n];
            y = new double[m];
        }

        Solution(final Structure2D dimensions) {
            this(dimensions.getRowDim(), dimensions.getColDim());
        }

        Optimisation.Result compose(final double value, final Optimisation.State state) {

            Optimisation.Result result = Optimisation.Result.of(value, state, x);

            if (state.isFeasible() && y != null && y.length > 0) {
                result = result.multipliers(y);
            }

            return result;
        }

    }

    /**
     * Maps solver indices to model entities.
     */
    static final class Structure implements EntityMap {

        private final ConstraintsMetaData myConstraintsMetaData;
        private final int[] myModelIndices;
        /**
         * Number of constraints.
         */
        final int m;

        /**
         * Number of variables.
         */
        final int n;

        Structure(final int m, final int n, final boolean mapped) {

            super();

            this.m = m;
            this.n = n;
            myConstraintsMetaData = ConstraintsMetaData.newInstance(m, mapped);
            myModelIndices = new int[n];
        }

        Structure(final Structure2D dimensions, final boolean mapped) {
            this(dimensions.getRowDim(), dimensions.getColDim(), mapped);
        }

        @Override
        public int countAdditionalConstraints() {
            return 0;
        }

        @Override
        public int countEqualityConstraints() {
            return 0;
        }

        @Override
        public int countInequalityConstraints() {
            return m;
        }

        @Override
        public int countModelVariables() {
            return n;
        }

        @Override
        public int countSlackVariables() {
            return 0;
        }

        @Override
        public int countVariables() {
            return n;
        }

        @Override
        public EntryPair<ModelEntity<?>, ConstraintType> getConstraint(final int idc) {
            return myConstraintsMetaData.getEntry(idc);
        }

        @Override
        public EntryPair<ModelEntity<?>, ConstraintType> getSlack(final int ids) {
            return null;
        }

        @Override
        public int indexOf(final int solverIndex) {
            return myModelIndices[solverIndex];
        }

        @Override
        public boolean isNegated(final int solverIndex) {
            return false;
        }

        void setConstraintEntry(final int i, final ModelEntity<?> entity, final ConstraintType type) {
            myConstraintsMetaData.setEntry(i, entity, type);
        }

        void setVariableEntry(final int j, final int index) {
            myModelIndices[j] = index;
        }

    }

    /**
     * Mutable workspace for ADMM iterates and intermediate vectors.
     */
    static final class Work {

        /**
         * A · x, used when forming primal residuals.
         */
        final double[] Ax;

        /**
         * A · Δx, used in dual- and infeasibility checks.
         */
        final double[] Axd;

        /**
         * Current base penalty parameter ρ used to build {@link #rho}. This is the runtime counterpart of
         * {@link Configuration#RHO} and may be adapted during the solve.
         */
        double baseRho;

        /**
         * Cached dual magnitude (max of norm(q), norm(yA), norm(Px)) from last checkTermination call.
         */
        double cachedDualMagnitude = Double.NaN;

        /**
         * Cached dual residual norm from last checkTermination call.
         */
        double cachedDualResidual = Double.NaN;

        /**
         * Cached primal magnitude (max of norm(z), norm(Ax)) from last checkTermination call.
         */
        double cachedPrimalMagnitude = Double.NaN;

        /**
         * Cached primal residual norm from last checkTermination call.
         */
        double cachedPrimalResidual = Double.NaN;

        /**
         * Classification of each constraint:
         * <ul>
         * <li>equality (1) l[i] == u[i]
         * <li>inequality (0) at least one of l[i], u[i] is finite
         * <li>loose (-1) both bounds are infinite (effectively disabled)
         * </ul>
         */
        final byte[] constraintType;

        /**
         * P · x, used when forming dual residuals.
         */
        final double[] Px;

        /**
         * P · Δx, used when checking dual infeasibility.
         */
        final double[] Pxd;

        /**
         * Vector of current penalty parameters ρ, one per constraint.
         */
        final ReciprocalPair rho;

        /**
         * Current primal iterate x.
         */
        double[] x;

        /**
         * Previous primal iterate x. After ADMM updates, holds the dual residual vector for use by
         * {@link #estimateRho()}.
         */
        double[] x0;

        /**
         * Difference between consecutive primal iterates.
         */
        final double[] xd;

        /**
         * Stacked intermediate iterate [x̃; z̃] appearing in the KKT solve.
         */
        final double[] xz;

        /**
         * Current dual iterate y.
         */
        final double[] y;

        /**
         * A' · y, used when forming dual residuals.
         */
        final double[] yA;

        /**
         * Difference between consecutive dual iterates.
         */
        final double[] yd;

        /**
         * A' · Δy, used in primal infeasibility checks.
         */
        final double[] ydA;

        /**
         * Current auxiliary iterate z enforcing the constraints.
         */
        double[] z;

        /**
         * Previous auxiliary iterate z. After ADMM updates, holds the primal residual vector for use by
         * {@link #estimateRho()}.
         */
        double[] z0;

        Work(final int m, final int n) {

            super();

            baseRho = Configuration.RHO;
            rho = new ReciprocalPair(m);

            constraintType = new byte[m];
            x = new double[n];
            z = new double[m];
            xz = new double[n + m];
            x0 = new double[n];
            z0 = new double[m];
            y = new double[m];
            Ax = new double[m];
            Px = new double[n];
            yA = new double[n];
            yd = new double[m];
            ydA = new double[n];
            xd = new double[n];
            Pxd = new double[n];
            Axd = new double[m];
        }

        Work(final Structure2D dimensions) {
            this(dimensions.getRowDim(), dimensions.getColDim());
        }

        /**
         * Resets working iterates x, z, y to zero (cold-start).
         */
        void resetSolution() {
            Arrays.fill(x, 0);
            Arrays.fill(z, 0);
            Arrays.fill(y, 0);
        }
    }

    private static final double SCALED_INFINITY = Configuration.INFINITY * Equilibrator.MIN;

    static final Integration INTEGRATION = new Integration();

    static <N extends Comparable<N>> Composer<N> build(final ExpressionsBasedModel model, final PhysicalStore.Factory<N, ?> factory) {

        List<Variable> freeVariables = model.getFreeVariables();
        Set<IntIndex> fixedVariables = model.getFixedVariables();

        Expression objective = model.objective().compensate(fixedVariables);

        Expression[] constraints = model.constraints().map(constr -> constr.compensate(fixedVariables)).filter(Expression::isAnyLinearFactorNonZero)
                .toArray(Expression[]::new);

        Variable[] bounds = freeVariables.stream().filter(Variable::isConstraint).toArray(Variable[]::new);

        int n = freeVariables.size();
        int m = constraints.length + bounds.length;

        Composer<N> retVal = new Composer<>(factory, m, n, true);

        for (int j = 0; j < n; j++) {
            retVal.setVariable(j, freeVariables.get(j));
        }

        boolean max = model.getOptimisationSense() == Optimisation.Sense.MAX;

        for (IntIndex key : objective.getLinearKeySet()) {
            int index = model.indexOfFreeVariable(key.index);
            if (index >= 0 && index < n) {
                BigDecimal val = objective.get(key, Configuration.ADJUSTED);
                // q[index] = max ? -val : val;
                retVal.setInQ(index, max ? val.negate() : val);
            }
        }

        for (IntRowColumn key : objective.getQuadraticKeySet()) {
            int row = model.indexOfFreeVariable(key.row);
            int col = model.indexOfFreeVariable(key.column);
            if (row >= 0 && row < n && col >= 0 && col < n) {
                BigDecimal val = objective.get(key, Configuration.ADJUSTED);
                retVal.setInP(row, col, max ? val.negate() : val);
            }
        }

        // R064CSC.Builder aBuilder = R064CSC.newBuilder(m, n);
        // double[] l = new double[m];
        // double[] u = new double[m];

        int row = 0;
        for (Expression constraint : constraints) {

            for (IntIndex key : constraint.getLinearKeySet()) {
                int modelCol = key.index;
                int solverCol = model.indexOfFreeVariable(modelCol);
                if (solverCol >= 0 && solverCol < n) {
                    BigDecimal value = constraint.get(key, Configuration.ADJUSTED);
                    retVal.setInA(row, solverCol, value);
                    // aBuilder.set(row, solverCol, value);
                }
            }

            BigDecimal lower = constraint.getLowerLimit(Configuration.ADJUSTED, Configuration.INFINITY2.negate());
            BigDecimal upper = constraint.getUpperLimit(Configuration.ADJUSTED, Configuration.INFINITY2);
            retVal.setConstraint(row, lower, constraint, upper, constraint.getConstraintType());
            row++;
        }

        for (Variable bound : bounds) {

            int index = model.indexOfFreeVariable(bound);

            // aBuilder.set(row, index, PrimitiveMath.ONE);
            retVal.setInA(row, index, BigMath.ONE);

            BigDecimal lower = bound.getLowerLimit(false, Configuration.INFINITY2.negate());
            BigDecimal upper = bound.getUpperLimit(false, Configuration.INFINITY2);
            retVal.setConstraint(row, lower, bound, upper, bound.getConstraintType());
            row++;
        }

        // Problem data = new Problem(pBuilder, q, aBuilder, l, u);

        // return new AlternatingDirectionSolver(data, model.options, structure);

        return retVal;
    }

    private transient double[] myCachedReducedGradient = null;

    /** Problem data */
    private final Problem myData;

    /** KKT System body */
    private final FactorKKT myKKT;

    /** scaling vectors */
    private final RuizScaling myScaling;

    /** problem solution */
    private final Solution mySolution;

    private final Structure myStructure;

    /** Working arrays for the solver */
    private final Work myWork;

    AlternatingDirectionSolver(final Problem data, final Optimisation.Options options) {
        this(data, options, new Structure(data, false));
    }

    AlternatingDirectionSolver(final Problem data, final Optimisation.Options options, final Structure structure) {

        super(options);

        // options.debug(AlternatingDirectionSolver.class);

        myData = data;
        myStructure = structure;
        myScaling = new RuizScaling(Configuration.SCALING_ITERATIONS, data);
        myWork = new Work(data);
        mySolution = new Solution(data);

        myScaling.update(myData);

        this.updateRho(true, false);

        myKKT = FactorKKT.of(data.P, data.A, Configuration.SIGMA, myWork.rho);

        this.setState(State.UNEXPLORED);
    }

    @Override
    public boolean fixVariable(final int index, final double value) {
        return this.updateRange(index, value, value);
    }

    @Override
    public Collection<Equation> generateCutCandidates(final double fractionality, final boolean[] integer) {
        return Set.of();
    }

    @Override
    public double getDualMultiplier(final int index) {
        return mySolution.y[index];
    }

    @Override
    public Optional<ExpressionsBasedModel.EntityMap> getEntityMap() {
        return myStructure != null ? Optional.of(myStructure) : Optional.empty();
    }

    @Override
    public double getReducedGradient(final int index) {
        if (myCachedReducedGradient == null) {
            myCachedReducedGradient = this.computeReducedGradient();
        }
        return myCachedReducedGradient[index];
    }

    @Override
    public Result solve(final Result kickStarter) {

        myCachedReducedGradient = null;

        if (kickStarter != null && kickStarter.getState().isFeasible()) {
            this.initialisePrimal(kickStarter);
            kickStarter.getMultipliers().ifPresent(this::initialiseDual);
        }

        double value = PrimitiveMath.NaN;
        Optimisation.State state = Optimisation.State.UNEXPLORED;

        try {

            this.resetIterationsCount();
            boolean checkTermination = false;
            boolean debug = this.isLogDebug();

            if (debug) {
                this.printHeader();
                this.printRow(myWork);
            }

            int maxIterations = Configuration.maxIterations(myData.getRowDim(), myData.getColDim(), options);
            int updateInterval = Configuration.updateInterval(maxIterations, options);

            for (int iter = 1; iter <= maxIterations; iter++) {
                this.incrementIterationsCount();

                this.performIteration();

                checkTermination = (iter == 1 || (iter % updateInterval == 0));
                if (checkTermination) {

                    if (this.checkTermination(false)) {
                        break;
                    }
                    if (!this.isIterationAllowed()) {
                        break;
                    }

                    if (debug && iter % updateInterval == 0) {
                        this.printRow(myWork);
                    }
                }

                if ((iter % updateInterval == 0) && !this.adaptRho()) {
                    this.setState(State.FAILED);
                    break;
                }
            }

            if (!checkTermination) {
                this.checkTermination(false);
            }

            if (!this.getState().isFeasible() && !this.checkTermination(true)) {
                this.setState(State.APPROXIMATE);
            }

            this.storeSolution();

            if (debug) {
                this.printRow(myWork);
            }

            state = this.getState();
            value = this.calculateObjectiveValue();

        } catch (Exception cause) {
            BasicLogger.error(cause, "OSQP solve failed!");
            value = PrimitiveMath.NaN;
            state = Optimisation.State.FAILED;
        }

        Supplier<Access1D<?>> reducedGradient = () -> ArrayR064.wrap(this.computeReducedGradient());

        return mySolution.compose(value, state).withReducedGradient(reducedGradient);
    }

    @Override
    public boolean updateRange(final int index, final double lower, final double upper) {

        myCachedReducedGradient = null;

        double scalar = myScaling.dual.values[index];

        myData.l[index] = lower * scalar;
        myData.u[index] = upper * scalar;

        this.setState(State.UNEXPLORED);

        return this.updateRho(true, true);
    }

    /**
     * Iterative refinement update
     */
    public boolean updateRefined(final MatrixStore<Quadruple> q, final MatrixStore<Quadruple> l, final MatrixStore<Quadruple> u) {

        myWork.resetSolution();

        q.supplyTo(myData.q);
        MULTIPLY.invoke(myData.q, myScaling.primal.values);
        MULTIPLY.invoke(myData.q, myScaling.cost);

        l.supplyTo(myData.l);
        MULTIPLY.invoke(myData.l, myScaling.dual.values);

        u.supplyTo(myData.u);
        MULTIPLY.invoke(myData.u, myScaling.dual.values);

        this.setState(State.UNEXPLORED);

        return this.updateRho(true, true);
    }

    /**
     * Adapts the penalty parameter ρ based on current primal/dual residuals.
     * <p>
     * If {@link #estimateRho()} differs from the current {@link Work#baseRho} by more than
     * {@link Configuration#ADAPTIVE_RHO_TOLERANCE}, updates ρ and refactorises the KKT system.
     *
     * @return {@code true} if successful or no update needed; {@code false} if refactorisation failed
     */
    private boolean adaptRho() {

        double candidateRho = this.estimateRho();

        boolean exitflag = true;

        if ((candidateRho > myWork.baseRho * Configuration.ADAPTIVE_RHO_TOLERANCE) || (candidateRho < myWork.baseRho / Configuration.ADAPTIVE_RHO_TOLERANCE)) {
            myWork.baseRho = candidateRho;
            exitflag = this.updateRho(false, true);
        }

        return exitflag;
    }

    /**
     * Computes the dual residual {@code ‖P x + q + A' y‖∞} and stores the residual vector in {@link Work#x0}
     * for {@link #estimateRho()}.
     */
    private double calculateDualResidual() {

        System.arraycopy(myData.q, 0, myWork.x0, 0, myData.getColDim());

        R064CSC.multiplySymmetric(myWork.Px, myData.P, myWork.x);

        AXPY.invoke(myWork.x0, PrimitiveMath.ONE, myWork.Px, myWork.x0);

        R064CSC.multiply(myWork.yA, myWork.y, myData.A);
        AXPY.invoke(myWork.x0, 1, myWork.yA, myWork.x0);

        return NRMINF.invoke(myWork.x0);
    }

    /**
     * Computes the primal residual {@code ‖A x - z‖∞} and stores the residual vector in {@link Work#z0} for
     * {@link #estimateRho()}.
     */
    private double calculatePrimalResidual() {

        R064CSC.multiply(myWork.Ax, myData.A, myWork.x);
        AXPY.invoke(myWork.z0, PrimitiveMath.NEG, myWork.z, myWork.Ax);

        return NRMINF.invoke(myWork.z0);
    }

    /**
     * Evaluates termination criteria based on primal/dual residuals and infeasibility certificates.
     *
     * @param approximate if {@code true}, relaxes tolerances for near-feasible iterates
     * @return {@code true} if a terminal condition is detected; {@code false} otherwise
     */
    private boolean checkTermination(final boolean approximate) {

        boolean exitflag = false;
        boolean iterationPrimalFeasible = false, iterationDualFeasible = false, problemPrimalInfeasible = false, problemDualInfeasible = false;

        NumberContext accuracy = approximate ? Configuration.APPROXIMATE : Configuration.ACCURACY;

        double primalResidual = this.calculatePrimalResidual();
        double dualResidual = this.calculateDualResidual();

        myWork.cachedPrimalResidual = primalResidual;
        myWork.cachedDualResidual = dualResidual;

        if (primalResidual > Configuration.INFINITY || dualResidual > Configuration.INFINITY) {
            this.setState(State.INVALID);
            return true;
        }

        double primMagnitude = Math.max(NRMINF.invoke(myWork.z), NRMINF.invoke(myWork.Ax));
        myWork.cachedPrimalMagnitude = primMagnitude;
        double primTolerance = accuracy.error(primMagnitude);

        if (primalResidual < primTolerance) {
            iterationPrimalFeasible = true;
        } else {
            problemPrimalInfeasible = this.isPrimalInfeasible(accuracy);
        }

        double dualMagnitude = MissingMath.max(NRMINF.invoke(myData.q), NRMINF.invoke(myWork.yA), NRMINF.invoke(myWork.Px));
        myWork.cachedDualMagnitude = dualMagnitude;
        double dualTolerance = accuracy.error(dualMagnitude);

        if (dualResidual < dualTolerance) {
            iterationDualFeasible = true;
        } else {
            problemDualInfeasible = this.isDualInfeasible(Configuration.APPROXIMATE);
        }

        if (iterationPrimalFeasible && iterationDualFeasible) {
            this.setState(State.OPTIMAL);
            exitflag = true;
        } else if (problemPrimalInfeasible) {
            this.setState(State.INFEASIBLE);
            exitflag = true;
        } else if (problemDualInfeasible) {
            this.setState(State.INFEASIBLE);
            exitflag = true;
        } else {
            this.setState(State.APPROXIMATE);
        }

        return exitflag;
    }

    /**
     * Computes the reduced gradient (gradient of the Lagrangian) in original (unscaled) coordinates.
     * <p>
     * Evaluates {@code P_s x_s + q_s + A_s' y_s} using scaled data and work arrays, then unscales each
     * component by {@code primal.inverse[j] / cost}.
     */
    private double[] computeReducedGradient() {
        int n = myData.getColDim();
        double[] gradient = new double[n];
        double[] Px = new double[n];
        double[] yA = new double[n];
        R064CSC.multiplySymmetric(Px, myData.P, myWork.x);
        R064CSC.multiply(yA, myWork.y, myData.A);
        double invCost = PrimitiveMath.ONE / myScaling.cost;
        for (int j = 0; j < n; j++) {
            gradient[j] = (Px[j] + myData.q[j] + yA[j]) * myScaling.primal.inverse[j] * invCost;
        }
        return gradient;
    }

    /**
     * Estimates a primal–dual balanced penalty parameter ρ.
     * <p>
     * Uses the current primal residual {@code ‖z0‖∞} and dual residual {@code ‖x0‖∞}, normalised by typical
     * problem magnitudes ({@code z}, {@code A x}, {@code q}, {@code yA}, {@code P x}), to form a
     * scale-invariant ratio of primal to dual error. The resulting estimate is then clamped to
     * [{@link Configuration#RHO_MIN}, {@link Configuration#RHO_MAX}].
     *
     * @return a penalty parameter ρ adapted to the current primal/dual residual balance
     */
    private double estimateRho() {

        double primalResidual = NRMINF.invoke(myWork.z0);
        double primScale = Double.isNaN(myWork.cachedPrimalMagnitude) ? Math.max(NRMINF.invoke(myWork.z), NRMINF.invoke(myWork.Ax))
                : myWork.cachedPrimalMagnitude;
        primalResidual /= (primScale + Configuration.SMALL);

        double dualResidual = NRMINF.invoke(myWork.x0);
        double dualScale = Double.isNaN(myWork.cachedDualMagnitude)
                ? MissingMath.max(NRMINF.invoke(myData.q), NRMINF.invoke(myWork.yA), NRMINF.invoke(myWork.Px))
                : myWork.cachedDualMagnitude;
        dualResidual /= (dualScale + Configuration.SMALL);

        double estimate = myWork.baseRho * Math.sqrt(primalResidual / (dualResidual + Configuration.SMALL));
        return Math.min(Math.max(Configuration.RHO_MIN, estimate), Configuration.RHO_MAX);
    }

    private void initialiseDual(final Access1D<?> y) {

        y.supplyTo(myWork.y);

        MULTIPLY.invoke(myWork.y, myScaling.dual.inverse);
        MULTIPLY.invoke(myWork.y, myScaling.cost);
    }

    private void initialisePrimal(final Access1D<?> x) {

        x.supplyTo(myWork.x);

        MULTIPLY.invoke(myWork.x, myScaling.primal.inverse);

        R064CSC.multiply(myWork.z, myData.A, myWork.x);
    }

    /**
     * Tests dual infeasibility by checking if Δx certifies an unbounded ray: q'Δx < 0 with P Δx ≈ 0 and A Δx
     * respecting bound structure.
     *
     * @param tolerance infeasibility tolerance
     * @return {@code true} if the problem is certified dual infeasible
     */
    private boolean isDualInfeasible(final NumberContext tolerance) {

        double normDeltaX = NRMINF.invoke(myWork.xd);

        if (!tolerance.isZero(normDeltaX)) {

            double error = tolerance.error(normDeltaX);

            if (DOT.invoke(myData.q, myWork.xd) < -error) {

                R064CSC.multiplySymmetric(myWork.Pxd, myData.P, myWork.xd);

                if (NRMINF.invoke(myWork.Pxd) < error) {
                    R064CSC.multiply(myWork.Axd, myData.A, myWork.xd);

                    for (int i = 0, m = myData.getRowDim(); i < m; i++) {
                        if (((myData.u[i] < SCALED_INFINITY) && (myWork.Axd[i] > error)) || ((myData.l[i] > -SCALED_INFINITY) && (myWork.Axd[i] < -error))) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tests primal infeasibility by checking if Δy certifies an infeasible constraint system: u'Δy⁺ + l'Δy⁻ <
     * 0 with A'Δy ≈ 0.
     *
     * @param tolerance infeasibility tolerance
     * @return {@code true} if the problem is certified primal infeasible
     */
    private boolean isPrimalInfeasible(final NumberContext tolerance) {

        for (int i = 0, m = myData.getRowDim(); i < m; i++) {
            if (myData.u[i] > SCALED_INFINITY) {
                if (myData.l[i] < -SCALED_INFINITY) {
                    myWork.yd[i] = 0.0;
                } else {
                    myWork.yd[i] = Math.min(myWork.yd[i], 0.0);
                }
            } else if (myData.l[i] < -SCALED_INFINITY) {
                myWork.yd[i] = Math.max(myWork.yd[i], 0.0);
            }
        }

        double normDeltaY = NRMINF.invoke(myWork.yd);

        double error = tolerance.error(normDeltaY);

        if (!tolerance.isZero(normDeltaY)) {

            double lhs = 0.0;
            for (int i = 0, m = myData.getRowDim(); i < m; i++) {
                lhs += myData.u[i] * Math.max(myWork.yd[i], 0) + myData.l[i] * Math.min(myWork.yd[i], 0);
            }

            if (lhs < -error) {
                R064CSC.multiply(myWork.ydA, myWork.yd, myData.A);

                return NRMINF.invoke(myWork.ydA) < error;
            }
        }

        return false;
    }

    /**
     * Performs one ADMM iteration: forms the KKT system right-hand side from current iterates, solves for the
     * intermediate iterate [x̃; z̃], applies relaxation, updates the auxiliary variable z by projection, and
     * updates the dual variable y by gradient ascent.
     */
    private void performIteration() {

        double[] temp = myWork.x0;
        myWork.x0 = myWork.x;
        myWork.x = temp;

        temp = myWork.z0;
        myWork.z0 = myWork.z;
        myWork.z = temp;

        int m = myData.getRowDim();
        int n = myData.getColDim();

        // 1. Solve KKT-like system
        for (int j = 0; j < n; j++) {
            myWork.xz[j] = Configuration.SIGMA * myWork.x0[j] - myData.q[j];
        }
        for (int i = 0; i < m; i++) {
            myWork.xz[n + i] = myWork.z0[i] - myWork.rho.inverse[i] * myWork.y[i];
        }
        myKKT.ftran(myWork.xz);

        double tmp;

        // Apply relaxation and compute the step difference
        for (int j = 0; j < n; j++) {
            tmp = myWork.xz[j] - myWork.x0[j];
            myWork.xd[j] = Configuration.ALPHA * tmp;
            myWork.x[j] = myWork.x0[j] + myWork.xd[j];
        }

        // 2. Project onto box
        for (int i = 0; i < m; i++) {
            tmp = Configuration.ALPHA * myWork.xz[i + n] + (PrimitiveMath.ONE - Configuration.ALPHA) * myWork.z0[i] + myWork.rho.inverse[i] * myWork.y[i];
            myWork.z[i] = Math.min(Math.max(myData.l[i], tmp), myData.u[i]);
        }

        // 3. Dual update
        for (int i = 0; i < m; i++) {
            myWork.yd[i] = myWork.rho.values[i]
                    * (Configuration.ALPHA * myWork.xz[i + n] + (PrimitiveMath.ONE - Configuration.ALPHA) * myWork.z0[i] - myWork.z[i]);
            myWork.y[i] += myWork.yd[i];
        }
    }

    private void printHeader() {
        this.log();
        this.printf("%s\t%12s\t%12s\t%12s\t%12s\t%12s\t%12s", "Iter", "Objective", "PrimResidual", "DualResidual", "rho", "Time (ms)", "State");
        this.log("----\t------------\t------------\t------------\t------------\t------------\t------------");
    }

    private void printRow(final Work work) {

        double primalRes = Double.isNaN(work.cachedPrimalResidual) ? this.calculatePrimalResidual() : work.cachedPrimalResidual;
        double dualRes = Double.isNaN(work.cachedDualResidual) ? this.calculateDualResidual() : work.cachedDualResidual;

        this.printf("%d\t%12.5e\t%12.5e\t%12.5e\t%12.5e\t%12.3f\t%12s", this.countIterations(), this.calculateObjectiveValue(), primalRes, dualRes,
                work.baseRho, this.getDuration(CalendarDateUnit.MILLIS).measure, this.getState());
    }

    /**
     * Finalises and stores the solution; unscales if feasible, resets otherwise.
     */
    private void storeSolution() {

        State state = this.getState();

        if (state.isFeasible()) {

            System.arraycopy(myWork.x, 0, mySolution.x, 0, myData.getColDim());
            System.arraycopy(myWork.y, 0, mySolution.y, 0, myData.getRowDim());

            myScaling.unscale(mySolution);

        } else {

            Arrays.fill(mySolution.x, 0, myData.getColDim(), Double.NaN);
            Arrays.fill(mySolution.y, 0, myData.getRowDim(), Double.NaN);

            if (state == State.INFEASIBLE) {
                MULTIPLY.invoke(myWork.yd, PrimitiveMath.ONE / NRMINF.invoke(myWork.yd));
                MULTIPLY.invoke(myWork.xd, PrimitiveMath.ONE / NRMINF.invoke(myWork.xd));
            }

            myWork.resetSolution();
        }
    }

    /**
     * Maintains the per-constraint penalty vector ρ and its reciprocals.
     *
     * @param recomputeConstraintTypes if {@code true}, reclassify constraints from current bounds
     * @param notifyKKT                if {@code true}, trigger KKT refactorisation when ρ changes
     * @return {@code true} if successful; {@code false} if KKT update failed
     */
    private boolean updateRho(final boolean recomputeConstraintTypes, final boolean notifyKKT) {

        boolean penaltyChanged = false;
        boolean constrTypeChanged = false;

        for (int i = 0, m = myData.getRowDim(); i < m; i++) {

            byte oldType = myWork.constraintType[i];
            byte newType = oldType;

            if (recomputeConstraintTypes) {
                if ((myData.l[i] < -Configuration.INFINITY * Equilibrator.MIN) && (myData.u[i] > SCALED_INFINITY)) {
                    newType = -1;
                } else if (myData.u[i] - myData.l[i] < Configuration.RHO_TOL) {
                    newType = 1;
                } else {
                    newType = 0;
                }
            }

            double newRho;
            if (newType == -1) {
                newRho = Configuration.RHO_MIN;
            } else if (newType == 1) {
                newRho = Configuration.RHO_EQ_OVER_RHO_INEQ * myWork.baseRho;
            } else {
                newRho = myWork.baseRho;
            }

            if (newType != oldType) {
                myWork.constraintType[i] = newType;
                constrTypeChanged = true;
            }

            if (myWork.rho.values[i] != newRho) {
                myWork.rho.values[i] = newRho;
                penaltyChanged = true;
            }
        }

        if (penaltyChanged) {
            myWork.rho.invert();
        }

        if (notifyKKT && (penaltyChanged || constrTypeChanged)) {
            return myKKT.updateDualWeights(myWork.rho);
        }

        return true;
    }

    /**
     * Computes {@code ½ x' P x + q' x} in original (unscaled) coordinates.
     */
    double calculateObjectiveValue() {

        double retVal = PrimitiveMath.ZERO;

        int[] pointers = myData.P.pointers;
        int[] indices = myData.P.indices;
        double[] values = myData.P.values;

        double[] x = myWork.x;

        for (int j = 0, n = myData.getColDim(); j < n; j++) {
            for (int ptr = pointers[j], lim = pointers[j + 1]; ptr < lim; ptr++) {
                int i = indices[ptr];

                if (i == j) {
                    retVal += PrimitiveMath.HALF * values[ptr] * x[i] * x[j];
                } else if (i < j) {
                    retVal += values[ptr] * x[i] * x[j];
                }
            }
        }

        retVal += DOT.invoke(myData.q, x);

        retVal /= myScaling.cost;

        return retVal;
    }

}
