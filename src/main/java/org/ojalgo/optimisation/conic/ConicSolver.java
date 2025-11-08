package org.ojalgo.optimisation.conic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.structure.Access1D;

/**
 * ConicSolver is a skeletal implementation of a general purpose conic solver (QP/LP/SOCP/SDP-lite) using a
 * primal-dual predictor-corrector interior point method with optional Homogeneous Self-Dual Embedding (HSDE).
 * <p>
 * This is NOT yet a functional solver; it documents the architecture and key algorithmic steps. Each TODO
 * marks a component to be implemented. The goal is to mirror the high-level design of modern conic solvers
 * (Clarabel, MOSEK) adapted to ojAlgo's data structures and performance guidelines.
 * </p>
 * <h2>Canonical Conic Form</h2>
 *
 * <pre>
 *   minimise   c^T x + 1/2 x^T Q x
 *   subject to A x + s = b,   s \in K = K_1 x K_2 x ... (product cone)
 * </pre>
 *
 * Dual variables: y associated with A x + s = b and z associated with s \in K (z \in K^*).
 * <h2>HSDE Embedding (optional)</h2> Adds (tau, kappa) to detect infeasibility/unboundedness uniformly.
 * <h2>Core Iteration (Predictor-Corrector IPM)</h2>
 * <ol>
 * <li>Presolve + Scaling</li>
 * <li>Initial strictly feasible (or HSDE start)</li>
 * <li>Loop until convergence: residuals, affine predictor, centering/corrector, line search, update scaling,
 * termination checks</li>
 * </ol>
 * <h2>Key Data Objects</h2>
 * <ul>
 * <li>{@link Cone} and {@link ConeBlock}: describe individual cone blocks.</li>
 * <li>{@link ConicProblem}: immutable view of (A, b, c, Q, coneBlocks).</li>
 * <li>Work buffers: x, y, s, z, tau, kappa, residual vectors, scaling metrics.</li>
 * <li>KKT workspace: factorisation structures (dense or sparse) reused each iteration.</li>
 * </ul>
 * <h2>Algorithmic TODO List</h2>
 * <ul>
 * <li>Presolve: row/column elimination, bound tightening, feasibility/infeasibility detection.</li>
 * <li>Scaling: Ruiz / symmetric equilibration of A, adaptive objective scaling.</li>
 * <li>Cone metrics: barrier value/gradient/Hessian, Nesterov–Todd scaling for SOC & SDP blocks.</li>
 * <li>KKT assembly: stable quasi-definite system; optional Schur complement path.</li>
 * <li>Factorisation: dense LDL^T with Bunch–Kaufman; sparse variant with fill-reducing ordering.</li>
 * <li>Predictor-corrector direction computation with dynamic sigma.</li>
 * <li>Fraction-to-boundary step selection; safeguarding & iterative refinement.</li>
 * <li>Termination & certificates (optimal, infeasible, unbounded).</li>
 * </ul>
 * <p>
 * All mutable work arrays should be preallocated; no allocation inside tight loops. Logging only when
 * detailed debugging is enabled. The outline returns an APPROXIMATE zero solution now.
 * </p>
 */
public final class ConicSolver extends GenericSolver {

    /** Lightweight configuration carried via Optimisation.Options#setConfigurator(...) */
    public static final class Configuration {
        /** Toggle full predictor–corrector; when false, use barrier-only (affine) step. */
        public boolean usePredictorCorrector = true;
        /** Placeholder for future PD‑merit gating (unused here). */
        public boolean usePrimalDualMerit = false;
    }

    /** Builder for assembling a minimal conic problem. */
    public static final class Builder {
        private MatrixStore<Double> myA;
        private MatrixStore<Double> myAeq;
        private MatrixStore<Double> myb;
        private MatrixStore<Double> myBeq;
        private MatrixStore<Double> myc;
        private final List<ConeBlock> myCones = new ArrayList<>();
        private MatrixStore<Double> myQ;

        public Builder A(final MatrixStore<Double> A) {
            myA = A;
            return this;
        }

        public Builder addCone(final Cone cone) {
            int offset = myCones.stream().mapToInt(cb -> cb.cone.size()).sum();
            myCones.add(new ConeBlock(cone, offset));
            return this;
        }

        public Builder Aeq(final MatrixStore<Double> Aeq) {
            myAeq = Aeq;
            return this;
        }

        public Builder b(final MatrixStore<Double> b) {
            myb = b;
            return this;
        }

        public Builder beq(final MatrixStore<Double> beq) {
            myBeq = beq;
            return this;
        }

        public ConicProblem build() {
            return new ConicProblem(myA, myb, myAeq, myBeq, myc, myQ, myCones);
        }

        public Builder c(final MatrixStore<Double> c) {
            myc = c;
            return this;
        }

        public Builder Q(final MatrixStore<Double> Q) {
            myQ = Q;
            return this;
        }
    }

    /** Cone interface: each cone type supplies feasibility, projection, and barrier operations. */
    public interface Cone {
        /** Barrier value F(v) (log barrier or specialized). */
        double barrier(Access1D<?> v);

        /** Gradient g(v) written into target. */
        void barrierGradient(Access1D<?> v, PhysicalStore<Double> target);

        /** Hessian application H(v)*dir written into target. */
        void barrierHessianTimes(Access1D<?> v, Access1D<?> dir, PhysicalStore<Double> target);

        /** @return true if v is strictly interior to the cone. */
        boolean isInterior(Access1D<?> v);

        /** Project v to K (used in presolve / fallback). */
        void project(PhysicalStore<Double> v);

        /** @return size of the cone block (length of s slice). */
        int size();

        /** Optional scaling update (e.g. NT scaling). */
        default void updateScaling(final Access1D<?> s, final Access1D<?> z) {
            // TODO(#CONIC-NT-SCALING): Implement per-cone scaling (SOC, SDP)
        }
    }

    /** Descriptor tying a cone to offsets in the stacked s and z vectors. */
    public static final class ConeBlock {
        public final Cone cone;
        public final int offset; // start index in s/z vectors

        public ConeBlock(final Cone cone, final int offset) {
            this.cone = Objects.requireNonNull(cone);
            this.offset = offset;
        }
    }

    /** Immutable problem data in canonical form. */
    public static final class ConicProblem {
        public final MatrixStore<Double> A; // Inequality constraints body (for slacks)
        public final MatrixStore<Double> Aeq; // Equality constraints body
        public final MatrixStore<Double> b; // Inequality constraints rhs
        public final MatrixStore<Double> beq; // Equality constraints rhs
        public final MatrixStore<Double> c;
        public final List<ConeBlock> cones;
        public final int m; // total inequality slack dimension
        public final int n;
        public final MatrixStore<Double> Q;

        ConicProblem(final MatrixStore<Double> A, final MatrixStore<Double> b, final MatrixStore<Double> Aeq, final MatrixStore<Double> beq,
                final MatrixStore<Double> c, final MatrixStore<Double> Q, final List<ConeBlock> cones) {
            this.A = A;
            this.b = b;
            this.Aeq = Aeq;
            this.beq = beq;
            this.c = c;
            this.Q = Q;
            this.cones = Collections.unmodifiableList(new ArrayList<>(cones));
            n = (int) (A != null ? A.countColumns() : (Aeq != null ? Aeq.countColumns() : c.countRows()));
            m = cones.stream().mapToInt(cb -> cb.cone.size()).sum();
        }
    }

    public static final class ModelIntegration extends ExpressionsBasedModel.Integration<ConicSolver> {

        /**
         * Mapping summary:
         * <ul>
         * <li>Variable lower/upper bounds and linear inequality expressions: Collected into A x + s = b with
         * s in a single {@link NonnegativeCone}. Each bound becomes a row with unit coefficient (upper) or -1
         * (lower) and corresponding rhs.</li>
         * <li>Linear equality expressions: Placed in Aeq x = beq (no cone involvement).</li>
         * <li>Quadratic objective terms: Assembled into symmetric Q (with sign flipped for maximise
         * models)</li>
         * <li>Pure diagonal quadratic inequality constraints of form -t^2 + u_1^2 + ... + u_k^2 <= 0 (no
         * linear part, rhs 0, uniform coefficient magnitudes): Mapped to individual {@link SecondOrderCone}
         * blocks with size k+1. For each SOC block we introduce rows -x_var + s_var = 0 so that s = x and s
         * is constrained to lie in the SOC.</li>
         * <li>Other quadratic constraints (cross terms, non-zero rhs, mixed signs, non-uniform scaling) are
         * currently ignored by this integration.</li>
         * </ul>
         */
        @Override
        public ConicSolver build(final ExpressionsBasedModel model) {

            ConvexSolver.Configuration cfg = new ConvexSolver.Configuration();
            // Removed ConvexSolver.copy(model, R064Store.FACTORY); to avoid unintended mutations of objective/constraints
            java.util.Set<org.ojalgo.structure.Structure1D.IntIndex> fixed = model.getFixedVariables();
            // Build explicit free variable index map
            java.util.List<org.ojalgo.optimisation.Variable> freeVars = model.getFreeVariables();
            java.util.Map<Integer, Integer> freeIndexMap = new java.util.HashMap<>();
            for (int i = 0; i < freeVars.size(); i++) {
                int modelIdx = model.getVariables().indexOf(freeVars.get(i));
                freeIndexMap.put(modelIdx, i);
            }
            int nbVars = freeVars.size();

            // Linear equalities (no quadratic terms)
            List<org.ojalgo.optimisation.Expression> eqExpr = model.constraints().filter(e -> e.isEqualityConstraint() && !e.isAnyQuadraticFactorNonZero())
                    .collect(Collectors.toList());
            PhysicalStore<Double> Aeq = null;
            PhysicalStore<Double> beq = null;
            if (!eqExpr.isEmpty()) {
                Aeq = R064Store.FACTORY.make(eqExpr.size(), nbVars);
                beq = R064Store.FACTORY.make(eqExpr.size(), 1);
                for (int r = 0; r < eqExpr.size(); r++) {
                    org.ojalgo.optimisation.Expression expr = eqExpr.get(r).compensate(fixed);
                    for (org.ojalgo.structure.Structure1D.IntIndex key : expr.getLinearKeySet()) {
                        Integer col = freeIndexMap.get(key.index);
                        if (col != null) {
                            Aeq.set((long) r, (long) col, expr.get(key, true).doubleValue());
                        }
                    }
                    beq.set(r, 0L, expr.getUpperLimit(true, org.ojalgo.function.constant.BigMath.ZERO).doubleValue());
                }
            }

            // Linear inequalities (upper & lower) and variable bounds -> NonnegativeCone
            List<double[]> linearRows = new java.util.ArrayList<>();
            List<Double> linearRhs = new java.util.ArrayList<>();
            // Upper expressions
            model.constraints().filter(e -> e.isUpperConstraint() && !e.isAnyQuadraticFactorNonZero()).forEach(expr -> {
                org.ojalgo.optimisation.Expression up = expr.compensate(fixed);
                double[] row = new double[nbVars];
                for (org.ojalgo.structure.Structure1D.IntIndex key : up.getLinearKeySet()) {
                    Integer col = freeIndexMap.get(key.index);
                    if (col != null) {
                        row[col] = up.get(key, true).doubleValue();
                    }
                }
                linearRows.add(row);
                linearRhs.add(up.getUpperLimit(true, org.ojalgo.function.constant.BigMath.ZERO).doubleValue());
            });
            // Variable upper bounds
            model.bounds().filter(Variable::isUpperConstraint).forEach(v -> {
                double[] row = new double[nbVars];
                Integer col = freeIndexMap.get(model.getVariables().indexOf(v));
                if (col != null) {
                    row[col] = 1.0;
                    linearRows.add(row);
                    linearRhs.add(v.getUpperLimit(false, org.ojalgo.function.constant.BigMath.ZERO).doubleValue());
                }
            });
            // Lower expressions
            model.constraints().filter(e -> e.isLowerConstraint() && !e.isAnyQuadraticFactorNonZero()).forEach(expr -> {
                org.ojalgo.optimisation.Expression lo = expr.compensate(fixed);
                double[] row = new double[nbVars];
                for (org.ojalgo.structure.Structure1D.IntIndex key : lo.getLinearKeySet()) {
                    Integer col = freeIndexMap.get(key.index);
                    if (col != null) {
                        row[col] = -lo.get(key, true).doubleValue();
                    }
                }
                linearRows.add(row);
                linearRhs.add(lo.getLowerLimit(true, org.ojalgo.function.constant.BigMath.ZERO).negate().doubleValue());
            });
            // Variable lower bounds
            model.bounds().filter(Variable::isLowerConstraint).forEach(v -> {
                double[] row = new double[nbVars];
                Integer col = freeIndexMap.get(model.getVariables().indexOf(v));
                if (col != null) {
                    row[col] = -1.0;
                    linearRows.add(row);
                    linearRhs.add(v.getLowerLimit(false, org.ojalgo.function.constant.BigMath.ZERO).negate().doubleValue());
                }
            });

            // Detect SOC constraints (pure diagonal quadratic inequality -t^2 + sum u_i^2 <= 0)
            List<org.ojalgo.optimisation.Expression> socExpr = model.constraints()
                    .filter(expr -> expr.isUpperConstraint() && !expr.isLowerConstraint() && expr.isAnyQuadraticFactorNonZero()
                            && !expr.isAnyLinearFactorNonZero() && expr.getUpperLimit(false, org.ojalgo.function.constant.BigMath.ZERO) != null
                            && Math.abs(expr.getUpperLimit(false, org.ojalgo.function.constant.BigMath.ZERO).doubleValue()) < 1e-14)
                    .map(expr -> expr.compensate(fixed)).collect(Collectors.toList());

            List<int[]> socBlocks = new java.util.ArrayList<>(); // each int[]: first t index then u indices
            for (org.ojalgo.optimisation.Expression expr : socExpr) {
                java.util.Map<Integer, Double> diag = new java.util.HashMap<>();
                for (org.ojalgo.structure.Structure2D.IntRowColumn key : expr.getQuadraticKeySet()) {
                    if (key.row != key.column) {
                        diag.clear();
                        break; // cross term disqualifies
                    }
                    diag.put(key.row, expr.get(key, true).doubleValue());
                }
                if (diag.isEmpty()) {
                    continue;
                }
                java.util.List<Integer> negative = new java.util.ArrayList<>();
                java.util.List<Integer> positive = new java.util.ArrayList<>();
                for (java.util.Map.Entry<Integer, Double> e : diag.entrySet()) {
                    if (e.getValue() < 0) {
                        negative.add(e.getKey());
                    } else if (e.getValue() > 0) {
                        positive.add(e.getKey());
                    }
                }
                if (negative.size() != 1 || positive.isEmpty()) {
                    continue;
                }
                double negCoeff = -diag.get(negative.get(0));
                if (!(negCoeff > 0)) {
                    continue;
                }
                boolean uniform = true;
                for (int p : positive) {
                    if (Math.abs(diag.get(p) - negCoeff) > 1e-9) {
                        uniform = false;
                        break;
                    }
                }
                if (!uniform) {
                    continue;
                }
                // Accept SOC; record block
                int tModelIndex = negative.get(0);
                int[] block = new int[positive.size() + 1];
                block[0] = tModelIndex;
                for (int i = 0; i < positive.size(); i++) {
                    block[i + 1] = positive.get(i);
                }
                socBlocks.add(block);
            }

            // Assemble A and b including linear inequalities and SOC rows
            int totalRows = linearRows.size();
            for (int[] blk : socBlocks) {
                totalRows += blk.length; // k+1 rows per SOC block
            }
            PhysicalStore<Double> Aineq = totalRows > 0 ? R064Store.FACTORY.make(totalRows, nbVars) : null;
            PhysicalStore<Double> bineq = totalRows > 0 ? R064Store.FACTORY.make(totalRows, 1) : null;
            int rowOffset = 0;
            for (int i = 0; i < linearRows.size(); i++) {
                double[] row = linearRows.get(i);
                for (int j = 0; j < nbVars; j++) {
                    double val = row[j];
                    if (val != 0.0) {
                        Aineq.set((long) rowOffset, (long) j, val);
                    }
                }
                bineq.set(rowOffset, 0L, linearRhs.get(i));
                rowOffset++;
            }
            // SOC rows: -x_var + s_var = 0 -> coefficients -1 for variable column, rhs 0
            for (int[] blk : socBlocks) {
                for (int k = 0; k < blk.length; k++) {
                    Integer col = freeIndexMap.get(blk[k]);
                    if (col != null) {
                        Aineq.set((long) rowOffset, (long) col, -1.0d);
                    }
                    bineq.set(rowOffset, 0L, 0.0d);
                    rowOffset++;
                }
            }

            // Objective Q and c
            PhysicalStore<Double> Q = R064Store.FACTORY.make(nbVars, nbVars);
            PhysicalStore<Double> c = R064Store.FACTORY.make(nbVars, 1);
            boolean max = model.getOptimisationSense() == Optimisation.Sense.MAX;

            // 1) Variable weights contribute to linear c
            for (Variable var : model.getVariables()) {
                if (var.isObjective()) {
                    int freeIdx = model.indexOfFreeVariable(var);
                    if (freeIdx >= 0) {
                        double val = var.getContributionWeight().doubleValue();
                        if (max) {
                            val = -val;
                        }
                        c.add(freeIdx, 0, val);
                    }
                }
            }
            // 2) Objective expressions (linear + quadratic)
            for (org.ojalgo.optimisation.Expression exprOrig : model.getExpressions()) {
                if (!exprOrig.isObjective()) {
                    continue;
                }
                org.ojalgo.optimisation.Expression expr = exprOrig.compensate(fixed); // remove fixed var parts
                double weight = expr.getContributionWeight() != null ? expr.getContributionWeight().doubleValue() : 1.0;
                if (weight == 0.0) {
                    continue;
                }
                // Quadratic terms
                if (expr.isAnyQuadraticFactorNonZero()) {
                    for (org.ojalgo.structure.Structure2D.IntRowColumn key : expr.getQuadraticKeySet()) {
                        org.ojalgo.optimisation.Variable varRow = model.getVariable(key.row);
                        org.ojalgo.optimisation.Variable varCol = model.getVariable(key.column);
                        int rIdxObj = model.indexOfFreeVariable(varRow);
                        int cIdxObj = model.indexOfFreeVariable(varCol);
                        if (rIdxObj < 0 || cIdxObj < 0) {
                            continue;
                        }
                        double val = expr.get(key, false).doubleValue() * weight; // raw coefficient
                        if (max) {
                            val = -val; // flip sign for max problems
                        }
                        if (rIdxObj == cIdxObj) {
                            Q.add(rIdxObj, cIdxObj, val);
                        } else {
                            Q.add(rIdxObj, cIdxObj, val);
                            Q.add(cIdxObj, rIdxObj, val);
                        }
                    }
                }
                // Linear terms
                if (expr.isAnyLinearFactorNonZero()) {
                    for (org.ojalgo.structure.Structure1D.IntIndex key : expr.getLinearKeySet()) {
                        org.ojalgo.optimisation.Variable var = model.getVariable(key.index);
                        int idxObj = model.indexOfFreeVariable(var);
                        if (idxObj < 0) {
                            continue;
                        }
                        double val = expr.get(key, false).doubleValue() * weight;
                        if (max) {
                            val = -val;
                        }
                        c.add(idxObj, 0, val);
                    }
                }
            }

            // Cones: first NonnegativeCone (if any linear rows), then one SecondOrderCone per SOC block
            List<ConeBlock> cones = new java.util.ArrayList<>();
            int offset = 0;
            if (!linearRows.isEmpty()) {
                cones.add(new ConeBlock(new NonnegativeCone(linearRows.size()), offset));
                offset += linearRows.size();
            }
            for (int[] blk : socBlocks) {
                cones.add(new ConeBlock(new SecondOrderCone(blk.length), offset));
                offset += blk.length;
            }

            ConicProblem problem = new ConicProblem(Aineq, bineq, Aeq, beq, c, Q, cones);
            return new ConicSolver(problem, model.options);
        }

        /**
         * This solver is capable to solve any model as long as there are no integer variables.
         */
        @Override
        public boolean isCapable(final ExpressionsBasedModel model) {
            return !model.isAnyVariableInteger();
        }

        /**
         * Map solver state back to model variable values, restoring objective value sign for maximise models.
         */
        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
            if (solverState == null) {
                return solverState;
            }
            long solverDim = solverState.count();
            int nbVars = model.countVariables();
            R064Store allVars = R064Store.FACTORY.make(nbVars, 1);
            Result current = model.getVariableValues();
            if (current != null) {
                for (int i = 0; i < nbVars; i++) {
                    allVars.set(i, current.get(i).doubleValue());
                }
            }
            List<Variable> freeVars = model.getFreeVariables();
            for (int freeIndex = 0; freeIndex < freeVars.size(); freeIndex++) {
                if (freeIndex < solverDim) {
                    Variable v = freeVars.get(freeIndex);
                    int modelIndex = model.getVariables().indexOf(v); // fall back linear search
                    if (modelIndex >= 0) {
                        allVars.set(modelIndex, solverState.doubleValue(freeIndex));
                    }
                }
            }
            // Populate fixed (bound) variable values when lower==upper and variable not free
            for (Variable v : model.getVariables()) {
                if (!freeVars.contains(v) && v.isLowerConstraint() && v.isUpperConstraint()) {
                    try {
                        double lo = v.getLowerLimit(false, org.ojalgo.function.constant.BigMath.ZERO).doubleValue();
                        double up = v.getUpperLimit(false, org.ojalgo.function.constant.BigMath.ZERO).doubleValue();
                        if (Math.abs(lo - up) <= 0.0) { // exact fixed
                            int idx = model.getVariables().indexOf(v);
                            if (idx >= 0) {
                                allVars.set(idx, lo);
                            }
                        }
                    } catch (Exception ignore) {
                        // Ignore any issues fetching limits
                    }
                }
            }
            // Simple SOC post-processing: detect constraints sum(u_i^2) - t^2 <= 0 and enforce t >= ||u||
            model.constraints().forEach(expr -> {
                if (!expr.isAnyQuadraticFactorNonZero()) {
                    return; // need quadratic terms
                }
                if (!expr.isUpperConstraint() || expr.isLowerConstraint()) {
                    return; // of form <= 0
                }
                if (expr.isAnyLinearFactorNonZero()) {
                    return; // no linear part
                }
                java.math.BigDecimal upLim = expr.getUpperLimit(false, null);
                if (upLim == null || Math.abs(upLim.doubleValue()) > 1e-12) {
                    return; // must be <= 0
                }
                java.util.Map<Integer, Double> diag = new java.util.HashMap<>();
                for (org.ojalgo.structure.Structure2D.IntRowColumn key : expr.getQuadraticKeySet()) {
                    if (key.row != key.column) {
                        return; // cross term -> not canonical SOC form
                    }
                    diag.put(key.row, expr.get(key, true).doubleValue());
                }
                if (diag.isEmpty()) {
                    return;
                }
                java.util.List<Integer> negative = new java.util.ArrayList<>();
                java.util.List<Integer> positive = new java.util.ArrayList<>();
                for (java.util.Map.Entry<Integer, Double> e : diag.entrySet()) {
                    if (e.getValue() < 0) {
                        negative.add(e.getKey());
                    } else if (e.getValue() > 0) {
                        positive.add(e.getKey());
                    }
                }
                if (negative.size() != 1 || positive.isEmpty()) {
                    return; // exactly one negative term (t), rest positive (u components)
                }
                double negCoeff = -diag.get(negative.get(0)); // magnitude of t^2 coefficient
                if (negCoeff <= 0) {
                    return;
                }
                for (int p : positive) { // ensure uniform scaling
                    double coeff = diag.get(p);
                    if (Math.abs(coeff - negCoeff) > 1e-9) {
                        return;
                    }
                }
                double sumSq = 0.0;
                for (int p : positive) {
                    int modelIndex = p; // direct model variable index
                    if (modelIndex < 0 || modelIndex >= allVars.countRows()) {
                        return;
                    }
                    double val = allVars.doubleValue(modelIndex);
                    sumSq += val * val;
                }
                double radius = Math.sqrt(sumSq / negCoeff); // adjust for scaling
                int tModelIndex = negative.get(0);
                if (tModelIndex >= 0 && tModelIndex < allVars.countRows()) {
                    double currentT = allVars.doubleValue(tModelIndex);
                    if (currentT + 1e-9 < radius) {
                        allVars.set(tModelIndex, radius);
                    }
                }
            });
            double value = solverState.getValue();
            if (model.getOptimisationSense() == Optimisation.Sense.MAX) {
                value = -value;
            }
            return new Result(solverState.getState(), value, allVars);
        }

        /**
         * Map model variable values to the solver free variable vector, adjusting objective value sign for
         * maximisation.
         */
        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
            if (modelState == null) {
                return modelState;
            }
            List<Variable> freeVars = model.getFreeVariables();
            R064Store solverX = R064Store.FACTORY.make(freeVars.size(), 1);
            for (int freeIndex = 0; freeIndex < freeVars.size(); freeIndex++) {
                Variable v = freeVars.get(freeIndex);
                int modelIndex = model.getVariables().indexOf(v);
                if (modelIndex >= 0 && modelIndex < modelState.count()) {
                    solverX.set(freeIndex, modelState.doubleValue(modelIndex));
                }
            }
            double value = modelState.getValue();
            if (model.getOptimisationSense() == Optimisation.Sense.MAX) {
                value = -value; // always minimise inside solver
            }
            return new Result(modelState.getState(), value, solverX);
        }
    }

    /** Simple nonnegative orthant cone implementation outline. */
    public static final class NonnegativeCone implements Cone {
        private final int mySize;

        public NonnegativeCone(final int size) {
            mySize = size;
        }

        @Override
        public double barrier(final Access1D<?> v) {
            double sum = 0.0;
            for (int i = 0; i < mySize; i++) {
                sum -= Math.log(v.doubleValue(i));
            }
            return sum;
        }

        @Override
        public void barrierGradient(final Access1D<?> v, final PhysicalStore<Double> target) {
            for (int i = 0; i < mySize; i++) {
                target.set(i, -1.0 / v.doubleValue(i));
            }
        }

        @Override
        public void barrierHessianTimes(final Access1D<?> v, final Access1D<?> dir, final PhysicalStore<Double> target) {
            for (int i = 0; i < mySize; i++) {
                target.set(i, dir.doubleValue(i) / (v.doubleValue(i) * v.doubleValue(i)));
            }
        }

        @Override
        public boolean isInterior(final Access1D<?> v) {
            for (int i = 0; i < mySize; i++) {
                if (v.doubleValue(i) <= 0.0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void project(final PhysicalStore<Double> v) {
            for (int i = 0; i < mySize; i++) {
                if (v.doubleValue(i) < 0.0) {
                    v.set(i, 0.0);
                }
            }
        }

        @Override
        public int size() {
            return mySize;
        }
    }

    /** Simple second-order (Lorentz) cone of dimension >= 2. */
    public static final class SecondOrderCone implements Cone {
        // Simple per-block scale gamma such that delta(s_scaled)=1 for improved conditioning
        private double myGamma = 1.0;
        private final int mySize;

        public SecondOrderCone(final int size) {
            if (size < 2) {
                throw new IllegalArgumentException("SOC size must be >= 2");
            }
            mySize = size;
        }

        @Override
        public double barrier(final Access1D<?> v) {
            double t = v.doubleValue(0);
            double norm2 = 0.0;
            for (int i = 1; i < mySize; i++) {
                norm2 += v.doubleValue(i) * v.doubleValue(i);
            }
            double delta = t * t - norm2;
            if (delta <= 0.0) {
                return Double.POSITIVE_INFINITY;
            }
            return -Math.log(delta);
        }

        @Override
        public void barrierGradient(final Access1D<?> v, final PhysicalStore<Double> target) {
            double t = v.doubleValue(0);
            double norm2 = 0.0;
            for (int i = 1; i < mySize; i++) {
                norm2 += v.doubleValue(i) * v.doubleValue(i);
            }
            double delta = Math.max(PrimitiveMath.MACHINE_EPSILON, t * t - norm2);
            target.set(0, (-2.0 * t) / delta);
            for (int i = 1; i < mySize; i++) {
                target.set(i, (2.0 * v.doubleValue(i)) / delta);
            }
        }

        @Override
        public void barrierHessianTimes(final Access1D<?> v, final Access1D<?> dir, final PhysicalStore<Double> target) {
            // Exact Hessian-vector for SOC barrier at v = [t; u]
            double t = v.doubleValue(0);
            int k = mySize;
            double uNorm2 = 0.0;
            for (int i = 1; i < k; i++) {
                uNorm2 += v.doubleValue(i) * v.doubleValue(i);
            }
            double delta = Math.max(PrimitiveMath.MACHINE_EPSILON, t * t - uNorm2);
            double invDelta = 1.0 / delta;
            double invDelta2 = invDelta * invDelta;
            // Prepare u and d components
            double dt = dir.doubleValue(0);
            double duDotU = 0.0;
            for (int i = 1; i < k; i++) {
                duDotU += dir.doubleValue(i) * v.doubleValue(i);
            }
            double HttTimes = 2.0 * (t * t + uNorm2) * invDelta2 * dt - 4.0 * t * invDelta2 * duDotU;
            target.set(0, HttTimes);
            for (int i = 1; i < k; i++) {
                double ui = v.doubleValue(i);
                double dui = dir.doubleValue(i);
                double Hi = (-4.0 * t * ui) * invDelta2 * dt + (2.0 * invDelta) * dui + (4.0 * invDelta2) * duDotU * ui;
                target.set(i, Hi);
            }
        }

        public double gamma() {
            return myGamma;
        }

        @Override
        public boolean isInterior(final Access1D<?> v) {
            double t = v.doubleValue(0);
            double norm2 = 0.0;
            for (int i = 1; i < mySize; i++) {
                norm2 += v.doubleValue(i) * v.doubleValue(i);
            }
            return t * t - norm2 > 0.0 && t > 0.0;
        }

        @Override
        public void project(final PhysicalStore<Double> v) {
            double t = v.doubleValue(0);
            double uNorm = 0.0;
            for (int i = 1; i < mySize; i++) {
                uNorm += v.doubleValue(i) * v.doubleValue(i);
            }
            uNorm = Math.sqrt(uNorm);
            if (uNorm <= -t) {
                for (int i = 0; i < mySize; i++) {
                    v.set(i, 0.0);
                }
            } else if (uNorm <= t) {
                return;
            } else {
                double scale = 0.5 * (1.0 + t / uNorm);
                v.set(0, uNorm * scale);
                for (int i = 1; i < mySize; i++) {
                    v.set(i, v.doubleValue(i) * scale);
                }
            }
        }

        @Override
        public int size() {
            return mySize;
        }

        /**
         * Update simple per-block scaling so that delta(s_scaled) = 1. This is a lightweight NT-like scaling
         * improving conditioning; full NT scaling requires duals which are not tracked yet.
         */
        @Override
        public void updateScaling(final Access1D<?> s, final Access1D<?> z) {
            double t = s.doubleValue(0);
            double norm2 = 0.0;
            for (int i = 1; i < mySize; i++) {
                norm2 += s.doubleValue(i) * s.doubleValue(i);
            }
            double delta = t * t - norm2;
            if (delta <= PrimitiveMath.MACHINE_EPSILON) {
                myGamma = 1.0; // fallback
            } else {
                myGamma = 1.0 / Math.sqrt(delta);
            }
        }
    }

    /**
     * Lightweight dense KKT solver for equality-constrained Newton steps. Solves the saddle system [ H A^T; A
     * 0 ] [dx; y] = [rx; ry]. Uses Cholesky when A is empty and H is SPD; otherwise LU on the full block.
     * Adds small diagonal regularisation to H if needed.
     */

    public static final ModelIntegration INTEGRATION = new ModelIntegration();

    private static final double ATOL = 1.0e-8;

    private static final double MERIT_RES_COEFF = 1.0e-3; // weight for residuals in merit function

    private static final double RTOL = 1.0e-6;

    /**
     * Factory method for convenience.
     *
     * @deprecated Use {@link ModelIntegration#build(ExpressionsBasedModel)} instead via the integration.
     */
    @Deprecated
    public static ConicSolver newSolver(final ExpressionsBasedModel model) {
        return INTEGRATION.build(model);
    }

    /**
     * @deprecated Prefer building from an {@link ExpressionsBasedModel} using {@link ModelIntegration}.
     */
    @Deprecated
    public static ConicSolver of(final ConicProblem problem, final Options options) {
        return new ConicSolver(problem, options);
    }

    private static double avg(final double[] data) {
        double sum = 0;
        for (double v : data) {
            sum += v;
        }
        return data.length == 0 ? 0 : sum / data.length;
    }

    private final R064Store myBaseGrad; // Qx + c (gradient of objective)
    private ConvexSolver myDelegate; // Temporary delegation for solving until PDIPM implemented
    private final double[] myDsAff; // affine slack direction
    private final R064Store myDx; // solution direction for x
    // Predictor-corrector (inactive skeleton) buffers
    private final R064Store myDxAff; // affine primal direction
    private final R064Store myDyAff; // affine equality dual direction
    private final double[] myDzAff; // affine dual cone direction
    private final R064Store myG; // full primal gradient including barrier parts
    private final R064Store myH; // Hessian approximation (objective + barrier)
    // New: corrected step buffers for joint FTB and diagnostics
    private final double[] myDs; // corrected slack direction (computed from myDx)
    private final double[] myDz; // corrected dual-cone direction (Jacobian * ds)
    // Scratch: reusable vector for cone Hessian-times for SOC dz reconstruction
    private final R064Store myConeWork; // sized to max cone block
    private final R064Store myConeDir;  // direction slice for SOC Hessian-times

    private int myIter = 0; // iteration counter
    // Iteration counters
    private final int myIterations = 0;

    private final double myKappa = 0.0;
    // New: KKT system abstraction (dense for now)
    private final KKTSystem myKKT;
    private double myLastGap = Double.POSITIVE_INFINITY;
    private double myLastRdInf = Double.POSITIVE_INFINITY;
    private double myLastRpInf = Double.POSITIVE_INFINITY;
    private double myMu = 0.0; // adaptive barrier parameter

    private final double myMuAffine = Double.NaN; // predicted mu after affine step
    private final Options myOptions;
    private final Configuration myConfig; // New: per-solver configuration from Options configurator

    private final ConicProblem myProblem;

    private final R064Store myRd; // dual residual Q x + c - A^T y - z

    // Workspace residuals
    private final R064Store myRp; // primal residual A x + s - b

    private final R064Store myRx; // KKT RHS (primal part)

    private final R064Store myRy; // KKT RHS (dual equality part)

    private final R064Store myS; // primal slacks
    private final double myScaleData; // problem data scaling factor

    private final double mySigma = 0.0; // centering parameter (sigma = (mu_aff/mu)^3)

    // Added: Preallocated work buffers & scaling/merit parameters
    private final double[] mySlacks; // inequality slacks b - A x (length mineq)
    private final java.util.List<double[]> mySocDu = new java.util.ArrayList<>(); // length k-1 per SOC (fraction-to-boundary)
    // SOC scratch buffers (preallocated per SecondOrderCone block to avoid per-iteration allocations)
    private final java.util.List<double[]> mySocGradU = new java.util.ArrayList<>(); // length k-1 per SOC
    private final java.util.List<double[][]> mySocHs = new java.util.ArrayList<>(); // k x k per SOC

    private final java.util.List<double[]> mySocU0 = new java.util.ArrayList<>(); // length k-1 per SOC (fraction-to-boundary)
    private State myState = State.UNEXPLORED;
    // HSDE scalars (tau, kappa) outline
    private final double myTau = 1.0; // TODO(#CONIC-HSDE): initialize per HSDE rules
    private final R064Store myX; // primal variables
    private final R064Store myYEq; // duals for equality constraints only
    private final R064Store myYWork; // work dual equality vector
    private final R064Store myZ; // dual cone variables

    public ConicSolver(final ConicProblem problem, final Options options) {

        super(options);

        myProblem = Objects.requireNonNull(problem);
        myOptions = Objects.requireNonNull(options);
        // Fetch configuration from options configurator, defaulting if not set
        myConfig = options.getConfigurator(Configuration.class).orElseGet(Configuration::new);
        int n = problem.n;
        int meq = problem.Aeq != null ? (int) problem.Aeq.countRows() : 0;
        int mineq = problem.A != null ? (int) problem.A.countRows() : 0;
        myX = R064Store.FACTORY.make(n, 1);
        myYEq = R064Store.FACTORY.make(meq, 1);
        myS = R064Store.FACTORY.make(problem.m, 1);
        myZ = R064Store.FACTORY.make(problem.m, 1);
        myRp = R064Store.FACTORY.make(meq + mineq, 1);
        myRd = R064Store.FACTORY.make(n, 1);
        // New preallocations
        mySlacks = new double[mineq];
        myBaseGrad = R064Store.FACTORY.make(n, 1);
        myG = R064Store.FACTORY.make(n, 1);
        myH = R064Store.FACTORY.make(n, n);
        myRx = R064Store.FACTORY.make(n, 1);
        myRy = R064Store.FACTORY.make(meq > 0 ? meq : 1, 1); // size>=1 to avoid 0-dim edge cases
        myDx = R064Store.FACTORY.make(n, 1);
        myYWork = R064Store.FACTORY.make(meq > 0 ? meq : 1, 1);
        myDxAff = R064Store.FACTORY.make(n, 1);
        myDyAff = R064Store.FACTORY.make(meq > 0 ? meq : 1, 1);
        myDsAff = new double[mineq];
        myDzAff = new double[problem.m];
        myScaleData = this.computeDataScale();
        // Initialise KKT system (dense variant)
        myKKT = new DenseKKTSystem(n, meq);

        // Preallocate SOC buffers
        int maxCone = 0;
        for (ConeBlock block : problem.cones) {
            if (block.cone instanceof SecondOrderCone) {
                int k = block.cone.size();
                mySocGradU.add(new double[k - 1]);
                double[][] hs = new double[k][k];
                mySocHs.add(hs);
                mySocU0.add(new double[k - 1]);
                mySocDu.add(new double[k - 1]);
            }
            maxCone = Math.max(maxCone, block.cone.size());
        }
        // New: allocate corrected step buffers and cone work vector
        myDs = new double[mineq];
        myDz = new double[problem.m];
        myConeWork = R064Store.FACTORY.make(maxCone > 0 ? maxCone : 1, 1);
        myConeDir = R064Store.FACTORY.make(maxCone > 0 ? maxCone : 1, 1);
    }

    /** Convenience overload to align with historical tests. */
    @Override
    public Result solve() {
        return this.solve(null);
    }

    @Override
    public Result solve(final Result kickStarter) {
        // Fast path: unconstrained convex quadratic -> solve Q x = -c
        if (myProblem.A == null && myProblem.Aeq == null && myProblem.Q != null) {
            int n = myProblem.n;
            Cholesky<Double> chol = Cholesky.R064.make(myProblem.Q);
            if (chol.compute(myProblem.Q)) {
                R064Store rhs = R064Store.FACTORY.make(n, 1);
                for (int i = 0; i < n; i++) {
                    rhs.set(i, -myProblem.c.doubleValue(i));
                }
                MatrixStore<Double> xopt = chol.getSolution(rhs);
                for (int i = 0; i < n; i++) {
                    myX.set(i, xopt.doubleValue(i));
                }
                myState = State.OPTIMAL;
                double value = this.objectiveValue(myX);
                return new Result(myState, value, myX.copy());
            }
        }
        this.initialiseStart();
        int maxIter = Math.min(100, myOptions.iterations_abort);
        for (int iter = 0; iter < maxIter; iter++) {
            if (!this.barrierIteration(iter)) {
                break;
            }
        }
        if (myState != State.OPTIMAL) {
            myState = this.isFeasible() ? State.FEASIBLE : State.APPROXIMATE;
        }
        double value = this.objectiveValue(myX);
        return new Result(myState, value, myX.copy());
    }

    private boolean barrierIteration(final int iter) {
        final int n = myProblem.n;
        final int mineq = myProblem.A != null ? (int) myProblem.A.countRows() : 0;
        final int meq = myProblem.Aeq != null ? (int) myProblem.Aeq.countRows() : 0;

        // Recompute slacks strictly into preallocated array
        if (mineq > 0) {
            for (int i = 0; i < mineq; i++) {
                double si = myProblem.b.doubleValue(i) - this.rowDot(myProblem.A, i, myX);
                mySlacks[i] = si;
                myS.set(i, si); // mirror into store for gap computation later
            }
            double minSlack = Double.POSITIVE_INFINITY;
            for (int i = 0; i < mineq; i++) {
                minSlack = Math.min(minSlack, mySlacks[i]);
            }
            if (!(minSlack > 0)) {
                // Simple push interior if needed (single small step)
                for (int i = 0; i < n; i++) {
                    myX.add(i, 0, 1e-2);
                }
                for (int i = 0; i < mineq; i++) {
                    double si = myProblem.b.doubleValue(i) - this.rowDot(myProblem.A, i, myX);
                    mySlacks[i] = si;
                    myS.set(i, si);
                    if (si <= 0) {
                        return false; // failed to get interior
                    }
                }
            }
        }

        // Adaptive mu init/update
        double avgSlack = 0.0;
        if (mineq > 0) {
            int cnt = 0;
            for (int i = 0; i < mineq; i++) {
                double s = mySlacks[i];
                if (s > 0) {
                    avgSlack += s;
                    cnt++;
                }
            }
            avgSlack = cnt > 0 ? avgSlack / cnt : 1.0;
        } else {
            avgSlack = 1.0;
        }
        final double minMu = 1e-12;
        if (myIter == 0) {
            double base = (myProblem.Q == null) ? 1e-2 : 0.1;
            myMu = Math.max(minMu, base * avgSlack);
        }

        // Build base gradient Qx + c (reuse myBaseGrad)
        myBaseGrad.fillAll(0.0);
        if (myProblem.Q != null) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < n; j++) {
                    double q = myProblem.Q.doubleValue(i, j);
                    if (q != 0.0) {
                        sum += q * myX.doubleValue(j);
                    }
                }
                if (sum != 0.0) {
                    myBaseGrad.set(i, sum);
                }
            }
        }
        for (int i = 0; i < n; i++) {
            myBaseGrad.add(i, 0, myProblem.c.doubleValue(i));
        }

        // Build gradient/Hessian at current mu and compute affine predictor
        this.buildGradientAndHessian(myMu); // fills myG,myH
        this.buildKktRhsFromResiduals(/*mu*/ myMu, /*sigma*/ 0.0, /*dxAff*/ null, /*dsAff*/ null);
        myDxAff.fillAll(0.0);
        myDyAff.fillAll(0.0);
        if (!myKKT.solve(myH, myProblem.Aeq, myRx, myRy, myDxAff, meq > 0 ? myDyAff : null)) {
            return false;
        }

        // Default to affine step if predictor–corrector disabled
        boolean usePC = myConfig.usePredictorCorrector;
        double sigma = 0.0;
        double muCentered = myMu;
        if (usePC) {
            // Compute ds_aff and sigma, then rebuild centered system and solve corrector
            if (mineq > 0) {
                this.reconstructDs(myDxAff, myDsAff);
            }
            double alphaAff = mineq > 0 ? this.fractionToBoundaryFromDs(mySlacks, myDsAff) : 1.0;
            if (!(alphaAff > 0 && Double.isFinite(alphaAff))) {
                alphaAff = 1.0;
            }
            double muAff = myMu;
            if (mineq > 0 && myProblem.m > 0) {
                double gapAff = this.computeGapForSlacks(mySlacks, myDsAff, alphaAff, myMu);
                muAff = gapAff / Math.max(1, myProblem.m);
            }
            if (myMu > minMu) {
                double ratio = muAff / Math.max(minMu, myMu);
                ratio = Math.max(0.0, Math.min(1.0, ratio));
                sigma = ratio * ratio * ratio;
            }
            muCentered = Math.max(minMu, sigma * myMu);
            this.buildGradientAndHessian(muCentered);
            this.buildKktRhsFromResiduals(muCentered, sigma, myDxAff, mineq > 0 ? myDsAff : null);
            myDx.fillAll(0.0);
            myYWork.fillAll(0.0);
            if (!myKKT.solve(myH, myProblem.Aeq, myRx, myRy, myDx, meq > 0 ? myYWork : null)) {
                return false;
            }
        } else {
            // Barrier-only: reuse affine step as final direction
            for (int i = 0; i < n; i++) {
                myDx.set(i, myDxAff.doubleValue(i));
            }
            if (meq > 0) {
                for (int r = 0; r < meq; r++) {
                    myYWork.set(r, myDyAff.doubleValue(r));
                }
            }
        }

        // Newton decrement lambda^2 = dx^T H dx (with the H used for this direction)
        double lambda2 = 0.0;
        for (int i = 0; i < n; i++) {
            double di = myDx.doubleValue(i);
            if (di == 0.0) continue;
            double rowSum = 0.0;
            for (int j = 0; j < n; j++) {
                double hij = myH.doubleValue(i, j);
                if (hij != 0.0) rowSum += hij * myDx.doubleValue(j);
            }
            lambda2 += di * rowSum;
        }
        double newtonDec = lambda2 > 0 ? Math.sqrt(lambda2) : 0.0;

        // Step length: primal-only in barrier-only mode; joint in PC mode
        double alphaPri = mineq > 0 ? this.fractionToBoundary(mySlacks, myDx) : 1.0;
        double alphaDual = 1.0;
        if (usePC && mineq > 0 && myProblem.m > 0) {
            this.reconstructDs(myDx, myDs);
            this.reconstructDzFromDs(mySlacks, myDs, muCentered, myDz);
            // Compute current z from slacks and centered mu (explicit)
            int off = 0;
            for (ConeBlock block : myProblem.cones) {
                int k = block.cone.size();
                if (block.cone instanceof NonnegativeCone) {
                    for (int i = 0; i < k; i++) {
                        double s = mySlacks[off + i];
                        myZ.set(off + i, s > 0 ? muCentered / s : 0.0);
                    }
                } else if (block.cone instanceof SecondOrderCone) {
                    double t = mySlacks[off];
                    double u2 = 0.0;
                    for (int i = 1; i < k; i++) {
                        double ui = mySlacks[off + i];
                        u2 += ui * ui;
                    }
                    double delta = t * t - u2;
                    if (delta <= PrimitiveMath.MACHINE_EPSILON || t <= 0.0) {
                        for (int i = 0; i < k; i++) myZ.set(off + i, 0.0);
                    } else {
                        double invDelta = 1.0 / delta;
                        myZ.set(off, muCentered * (-2.0 * t) * invDelta);
                        for (int i = 1; i < k; i++) {
                            double ui = mySlacks[off + i];
                            myZ.set(off + i, muCentered * (2.0 * ui) * invDelta);
                        }
                    }
                }
                off += k;
            }
            alphaDual = this.fractionToBoundaryDual(myZ, myDz);
        }
        double alphaFeas = Math.min(alphaPri, alphaDual);
        if (!(alphaFeas > 0 && Double.isFinite(alphaFeas))) {
            return false;
        }

        final double phi0 = this.meritValue(myX, mySlacks, myMu, myLastRdInf, myLastRpInf);
        double dphi = 0.0; // directional derivative g^T dx (with current myG)
        for (int i = 0; i < n; i++) dphi += myG.doubleValue(i) * myDx.doubleValue(i);
        final double c1 = 1.0e-4;
        final double beta = 0.5;
        double alpha = Math.min(1.0, alphaFeas);
        if (dphi >= 0.0) {
            alpha = Math.min(alpha, 1.0e-3);
        } else {
            int bt = 0;
            while (bt < 20) {
                boolean interiorOk = true;
                for (int i = 0; i < mineq; i++) {
                    double si = myProblem.b.doubleValue(i) - this.rowDot(myProblem.A, i, myX, myDx, alpha);
                    if (!(si > 0)) { interiorOk = false; break; }
                }
                if (!interiorOk) { alpha *= beta; bt++; continue; }
                double phiTrial = this.meritValueTrial(myX, myDx, alpha, myMu);
                if (phiTrial <= phi0 + c1 * alpha * dphi) break;
                alpha *= beta; bt++;
            }
        }
        if (!(alpha > 0 && Double.isFinite(alpha))) return false;

        // Update primal & dual equality variables
        for (int i = 0; i < n; i++) myX.add(i, 0, alpha * myDx.doubleValue(i));
        if (meq > 0) {
            for (int r = 0; r < meq; r++) myYEq.set(r, myYWork.doubleValue(r));
        }

        // Refresh slacks after step
        if (mineq > 0) {
            for (int i = 0; i < mineq; i++) {
                double si = myProblem.b.doubleValue(i) - this.rowDot(myProblem.A, i, myX);
                mySlacks[i] = si;
                myS.set(i, si);
            }
        }
        // Compute residuals & dual approximation (updates myRd, myRp, myZ)
        this.computeResiduals(myBaseGrad, mySlacks, myMu);
        // Evaluate termination
        this.checkConvergenceAndCertificates();
        // Extract norms & gap
        myLastRdInf = 0.0;
        for (int i = 0; i < n; i++) myLastRdInf = Math.max(myLastRdInf, Math.abs(myRd.doubleValue(i)));
        myLastRpInf = 0.0;
        if (meq > 0) for (int i = 0; i < meq; i++) myLastRpInf = Math.max(myLastRpInf, Math.abs(myRp.doubleValue(i)));
        myLastGap = 0.0;
        for (int i = 0; i < myProblem.m; i++) myLastGap += myS.doubleValue(i) * myZ.doubleValue(i);
        myLastGap = Math.abs(myLastGap);
        // Adaptive mu update
        if (newtonDec < 0.1 && myMu > minMu * 1.01) {
            myMu = Math.max(minMu, 0.5 * myMu);
        }

        this.debug("iter=" + myIter + " phi=" + phi0 + " mu=" + myMu + " sigma=" + sigma + " dec=" + newtonDec + " alpha=" + alpha + " rdInf="
                + myLastRdInf + " rpInf=" + myLastRpInf + " gap=" + myLastGap);

        myIter++;
        double rdTol = ATOL + RTOL * myScaleData;
        double rpTol = ATOL + RTOL * myScaleData;
        double gapTol = RTOL * (1.0 + Math.abs(this.objectiveValue(myX)));
        if ((myLastRdInf <= rdTol && myLastRpInf <= rpTol && myLastGap <= gapTol) || myMu <= 1e-12) {
            myState = State.OPTIMAL;
            return false;
        }
        return true;
    }

    /** Build gradient (myG) and Hessian (myH) for a given barrier parameter mu. */
    private void buildGradientAndHessian(final double muUse) {
        final int n = myProblem.n;
        // Start g and H from objective parts using myBaseGrad and Q
        myG.fillMatching(myBaseGrad);
        myH.fillAll(0.0);
        if (myProblem.Q != null) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double q = myProblem.Q.doubleValue(i, j);
                    if (q != 0.0) {
                        myH.add(i, j, q);
                    }
                }
            }
        }
        // Barrier contributions from slacks (mySlacks)
        final int mineq = myProblem.A != null ? (int) myProblem.A.countRows() : 0;
        int offset = 0;
        int socIdx = 0; // index into SOC buffers
        if (mineq > 0) {
            for (ConeBlock block : myProblem.cones) {
                int k = block.cone.size();
                if (block.cone instanceof NonnegativeCone) {
                    for (int r = 0; r < k; r++) {
                        int row = offset + r;
                        double s = mySlacks[row];
                        double coeff = muUse / s; // gradient coeff
                        for (int c = 0; c < n; c++) {
                            double a = myProblem.A.doubleValue(row, c);
                            if (a != 0.0) {
                                myG.add(c, 0, coeff * a);
                            }
                        }
                        double hcoeff = muUse / (s * s);
                        for (int i = 0; i < n; i++) {
                            double ai = myProblem.A.doubleValue(row, i);
                            if (ai == 0.0) {
                                continue;
                            }
                            double scaled = hcoeff * ai;
                            for (int j = 0; j < n; j++) {
                                double aj = myProblem.A.doubleValue(row, j);
                                if (aj != 0.0) {
                                    myH.add(i, j, scaled * aj);
                                }
                            }
                        }
                    }
                } else if (block.cone instanceof SecondOrderCone) {
                    double t = mySlacks[offset];
                    double uNorm2 = 0.0;
                    for (int i = 1; i < k; i++) {
                        double ui = mySlacks[offset + i];
                        uNorm2 += ui * ui;
                    }
                    double delta = t * t - uNorm2;
                    if (delta > PrimitiveMath.MACHINE_EPSILON && t > 0.0) {
                        double invDelta = 1.0 / delta;
                        double grad_t = (-2.0 * t) * invDelta;
                        double[] grad_u = mySocGradU.get(socIdx);
                        for (int i = 0; i < k - 1; i++) {
                            grad_u[i] = (2.0 * mySlacks[offset + 1 + i]) * invDelta;
                        }
                        // Gradient accumulate
                        for (int col = 0; col < n; col++) {
                            double acc = 0.0;
                            double a0 = myProblem.A.doubleValue(offset, col);
                            if (a0 != 0.0) {
                                acc += a0 * grad_t;
                            }
                            for (int i = 1; i < k; i++) {
                                double ai = myProblem.A.doubleValue(offset + i, col);
                                if (ai != 0.0) {
                                    acc += ai * grad_u[i - 1];
                                }
                            }
                            if (acc != 0.0) {
                                myG.add(col, 0, -muUse * acc);
                            }
                        }
                        double invDelta2 = invDelta * invDelta;
                        double[][] Hs = mySocHs.get(socIdx);
                        for (int p = 0; p < k; p++) {
                            for (int q = 0; q < k; q++) {
                                Hs[p][q] = 0.0;
                            }
                        }
                        Hs[0][0] = 2.0 * (t * t + uNorm2) * invDelta2;
                        for (int i = 1; i < k; i++) {
                            double ui = mySlacks[offset + i];
                            double val = -4.0 * t * ui * invDelta2;
                            Hs[0][i] = val;
                            Hs[i][0] = val;
                        }
                        for (int i = 1; i < k; i++) {
                            for (int j = 1; j < k; j++) {
                                double ui = mySlacks[offset + i];
                                double uj = mySlacks[offset + j];
                                double add = (i == j ? 2.0 * invDelta : 0.0) + 4.0 * invDelta2 * ui * uj;
                                Hs[i][j] = add;
                            }
                        }
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < n; j++) {
                                double sum = 0.0;
                                for (int p = 0; p < k; p++) {
                                    double ap_i = myProblem.A.doubleValue(offset + p, i);
                                    if (ap_i == 0.0) {
                                        continue;
                                    }
                                    for (int q = 0; q < k; q++) {
                                        double aq_j = myProblem.A.doubleValue(offset + q, j);
                                        if (aq_j == 0.0) {
                                            continue;
                                        }
                                        double hs = Hs[p][q];
                                        if (hs != 0.0) {
                                            sum += ap_i * hs * aq_j;
                                        }
                                    }
                                }
                                if (sum != 0.0) {
                                    myH.add(i, j, muUse * sum);
                                }
                            }
                        }
                    }
                    socIdx++;
                }
                offset += k;
            }
        }
        // Ensure Hessian has some diagonal regularisation if near-zero
        double maxAbsH = 0.0;
        for (int i = 0; i < n; i++) {
            double diag = Math.abs(myH.doubleValue(i, i));
            if (diag > maxAbsH) {
                maxAbsH = diag;
            }
        }
        if (maxAbsH < 1e-14) {
            for (int i = 0; i < n; i++) {
                myH.add(i, i, 1e-8);
            }
        }
    }

    /**
     * Check convergence (primal/dual residuals + duality gap) and set state/certificates using scaled
     * tolerances.
     */
    private void checkConvergenceAndCertificates() {
        int n = myProblem.n;
        int rpLen = (int) myRp.countRows();
        double rdInf = 0.0;
        for (int i = 0; i < n; i++) {
            rdInf = Math.max(rdInf, Math.abs(myRd.doubleValue(i)));
        }
        double rpInf = 0.0; // equality-only portion (first meq entries); remaining rows are zero
        int meq = myProblem.Aeq != null ? (int) myProblem.Aeq.countRows() : 0;
        for (int i = 0; i < meq && i < rpLen; i++) {
            rpInf = Math.max(rpInf, Math.abs(myRp.doubleValue(i)));
        }
        double gap = 0.0;
        for (int i = 0; i < myProblem.m; i++) {
            gap += myS.doubleValue(i) * myZ.doubleValue(i);
        }
        gap = Math.abs(gap);
        double rdTol = ATOL + RTOL * myScaleData;
        double rpTol = ATOL + RTOL * myScaleData;
        double gapTol = RTOL * (1.0 + Math.abs(this.objectiveValue(myX)));
        if (rdInf <= rdTol && rpInf <= rpTol && gap <= gapTol) {
            myState = State.OPTIMAL;
        }
    }

    // New: compute scaling factor for tolerances (max absolute among problem data)
    private double computeDataScale() {
        double scale = 1.0;
        if (myProblem.Q != null) {
            for (int i = 0; i < myProblem.Q.countRows(); i++) {
                for (int j = 0; j < myProblem.Q.countColumns(); j++) {
                    scale = Math.max(scale, Math.abs(myProblem.Q.doubleValue(i, j)));
                }
            }
        }
        if (myProblem.A != null) {
            for (int i = 0; i < myProblem.A.countRows(); i++) {
                for (int j = 0; j < myProblem.A.countColumns(); j++) {
                    scale = Math.max(scale, Math.abs(myProblem.A.doubleValue(i, j)));
                }
            }
            for (int i = 0; i < myProblem.b.countRows(); i++) {
                scale = Math.max(scale, Math.abs(myProblem.b.doubleValue(i, 0)));
            }
        }
        if (myProblem.Aeq != null) {
            for (int i = 0; i < myProblem.Aeq.countRows(); i++) {
                for (int j = 0; j < myProblem.Aeq.countColumns(); j++) {
                    scale = Math.max(scale, Math.abs(myProblem.Aeq.doubleValue(i, j)));
                }
            }
            for (int i = 0; i < myProblem.beq.countRows(); i++) {
                scale = Math.max(scale, Math.abs(myProblem.beq.doubleValue(i, 0)));
            }
        }
        for (int i = 0; i < myProblem.c.countRows(); i++) {
            scale = Math.max(scale, Math.abs(myProblem.c.doubleValue(i, 0)));
        }
        return scale;
    }

    /**
     * Compute gap s·z for s + alpha*ds using the same cone z mapping as in computeResiduals with a given mu.
     */
    private double computeGapForSlacks(final double[] sAll, final double[] ds, final double alpha, final double muUse) {
        double gap = 0.0;
        int offset = 0;
        for (ConeBlock block : myProblem.cones) {
            int k = block.cone.size();
            if (block.cone instanceof NonnegativeCone) {
                for (int i = 0; i < k; i++) {
                    double sAff = sAll[offset + i] + alpha * ds[offset + i];
                    if (!(sAff > 0.0)) {
                        return Double.POSITIVE_INFINITY;
                    }
                    double zAff = muUse / sAff;
                    gap += sAff * zAff;
                }
            } else if (block.cone instanceof SecondOrderCone) {
                double t = sAll[offset] + alpha * ds[offset];
                double norm2 = 0.0;
                for (int i = 1; i < k; i++) {
                    double ui = sAll[offset + i] + alpha * ds[offset + i];
                    norm2 += ui * ui;
                }
                double delta = t * t - norm2;
                if (!(t > 0.0 && delta > 0.0)) {
                    return Double.POSITIVE_INFINITY;
                }
                double invDelta = 1.0 / delta;
                double zt = muUse * (-2.0 * t) * invDelta;
                gap += t * zt;
                for (int i = 1; i < k; i++) {
                    double ui = sAll[offset + i] + alpha * ds[offset + i];
                    double zi = muUse * (2.0 * ui) * invDelta;
                    gap += ui * zi;
                }
            }
            offset += k;
        }
        return Math.abs(gap);
    }

    private void computeResiduals(final R064Store baseGrad, final double[] sAll, final double mu) {
        int n = myProblem.n;
        int meq = myProblem.Aeq != null ? (int) myProblem.Aeq.countRows() : 0;
        int mineq = myProblem.A != null ? (int) myProblem.A.countRows() : 0;
        // Dual cone parts z from barrier gradients
        int offset = 0;
        for (ConeBlock block : myProblem.cones) {
            int k = block.cone.size();
            if (block.cone instanceof NonnegativeCone) {
                for (int i = 0; i < k; i++) {
                    double si = sAll[offset + i];
                    double zi = si > 0 ? mu / si : 0.0;
                    myZ.set(offset + i, zi);
                }
            } else if (block.cone instanceof SecondOrderCone) {
                double t = sAll[offset];
                double uNorm2 = 0.0;
                for (int i = 1; i < k; i++) {
                    double ui = sAll[offset + i];
                    uNorm2 += ui * ui;
                }
                double delta = t * t - uNorm2;
                if (delta <= PrimitiveMath.MACHINE_EPSILON || t <= 0.0) {
                    for (int i = 0; i < k; i++) {
                        myZ.set(offset + i, 0.0);
                    }
                } else {
                    double invDelta = 1.0 / delta;
                    myZ.set(offset, mu * (-2.0 * t) * invDelta);
                    for (int i = 1; i < k; i++) {
                        double ui = sAll[offset + i];
                        myZ.set(offset + i, mu * (2.0 * ui) * invDelta);
                    }
                }
            }
            offset += k;
        }
        // Dual residual rd = Qx + c - Aeq^T y - A^T z (already passed baseGrad = Qx + c)
        for (int i = 0; i < n; i++) {
            double rd = baseGrad.doubleValue(i);
            if (meq > 0) {
                for (int r = 0; r < meq; r++) {
                    double a = myProblem.Aeq.doubleValue(r, i);
                    if (a != 0.0) {
                        rd -= a * myYEq.doubleValue(r);
                    }
                }
            }
            if (mineq > 0) {
                for (int r = 0; r < mineq; r++) {
                    double a = myProblem.A.doubleValue(r, i);
                    if (a != 0.0) {
                        rd -= a * myZ.doubleValue(r);
                    }
                }
            }
            myRd.set(i, rd);
        }
        // Primal residual rp: equality-only Aeq x - beq. (Inequalities via s = b - A x)
        int rpIndex = 0;
        if (meq > 0) {
            for (int r = 0; r < meq; r++, rpIndex++) {
                double val = this.rowDot(myProblem.Aeq, r, myX) - myProblem.beq.doubleValue(r);
                myRp.set(rpIndex, val);
            }
        }
        for (; rpIndex < myRp.countRows(); rpIndex++) {
            myRp.set(rpIndex, 0.0);
        }
    }

    /** Logging helper. */
    private void debug(final String msg) {
        if (myOptions.logger_appender != null && (myOptions.logger_solver == null || myOptions.logger_solver.isInstance(this))) {
            BasicLogger.DEBUG.println("[ConicSolver] " + msg);
        }
    }

    /** Fraction-to-boundary max feasible step for current slack representation using dx. */
    private double fractionToBoundary(final double[] sAll, final Access1D<?> dx) {
        if (myProblem.A == null) {
            return 1.0;
        }
        final int mineq = (int) myProblem.A.countRows();
        double alpha = 1.0;
        int offset = 0;
        int socIdx = 0;
        for (ConeBlock block : myProblem.cones) {
            int k = block.cone.size();
            if (block.cone instanceof NonnegativeCone) {
                for (int r = 0; r < k; r++) {
                    int row = offset + r;
                    // Directional slack change ds = -A_row * dx
                    double Adx = 0.0;
                    for (int c = 0; c < myProblem.n; c++) {
                        double a = myProblem.A.doubleValue(row, c);
                        if (a != 0.0) {
                            Adx += a * dx.doubleValue(c);
                        }
                    }
                    double ds = -Adx;
                    if (ds < 0) {
                        double s = sAll[row];
                        double cand = 0.99 * s / (-ds);
                        if (cand < alpha) {
                            alpha = cand;
                        }
                    }
                }
            } else if (block.cone instanceof SecondOrderCone) {
                double Adx_t = 0.0;
                for (int c = 0; c < myProblem.n; c++) {
                    double a = myProblem.A.doubleValue(offset, c);
                    if (a != 0.0) {
                        Adx_t += a * dx.doubleValue(c);
                    }
                }
                double dt = -Adx_t;
                double t0 = sAll[offset];
                double[] u0 = mySocU0.get(socIdx);
                double[] du = mySocDu.get(socIdx);
                for (int i = 1; i < k; i++) {
                    double Adx_ui = 0.0;
                    int row = offset + i;
                    for (int c = 0; c < myProblem.n; c++) {
                        double a = myProblem.A.doubleValue(row, c);
                        if (a != 0.0) {
                            Adx_ui += a * dx.doubleValue(c);
                        }
                    }
                    du[i - 1] = -Adx_ui;
                    u0[i - 1] = sAll[row];
                }
                double u0Norm2 = 0.0, duNorm2 = 0.0, u0du = 0.0;
                for (int i = 0; i < k - 1; i++) {
                    double ui0 = u0[i];
                    double dui = du[i];
                    u0Norm2 += ui0 * ui0;
                    duNorm2 += dui * dui;
                    u0du += ui0 * dui;
                }
                double a = dt * dt - duNorm2;
                double b = 2.0 * (t0 * dt - u0du);
                double c = t0 * t0 - u0Norm2;
                double alphaSOC = 1.0;
                if (a == 0) {
                    if (b < 0) {
                        double root = -c / b; // linear case
                        if (root > 0) {
                            alphaSOC = 0.99 * root;
                        }
                    }
                } else {
                    double disc = b * b - 4.0 * a * c;
                    if (disc > 0) {
                        double sqrtD = Math.sqrt(disc);
                        double r1 = (-b + sqrtD) / (2.0 * a);
                        double r2 = (-b - sqrtD) / (2.0 * a);
                        double limit = Double.POSITIVE_INFINITY;
                        if (r1 > 0) {
                            limit = Math.min(limit, r1);
                        }
                        if (r2 > 0) {
                            limit = Math.min(limit, r2);
                        }
                        if (Double.isFinite(limit)) {
                            alphaSOC = 0.99 * limit;
                        }
                    }
                }
                if (dt < 0) {
                    double limitT = -t0 / dt;
                    if (limitT > 0 && limitT < alphaSOC) {
                        alphaSOC = 0.99 * limitT;
                    }
                }
                if (alphaSOC < alpha) {
                    alpha = alphaSOC;
                }
                socIdx++;
            }
            offset += k;
        }
        if (!(alpha > 0)) {
            alpha = 1.0e-3;
        }
        return Math.min(1.0, alpha);
    }

    /** Fraction-to-boundary using explicit ds = -A*dx for each cone block. */
    private double fractionToBoundaryFromDs(final double[] sAll, final double[] ds) {
        if (myProblem.A == null) {
            return 1.0;
        }
        double alpha = 1.0;
        int offset = 0;
        int socIdx = 0;
        for (ConeBlock block : myProblem.cones) {
            int k = block.cone.size();
            if (block.cone instanceof NonnegativeCone) {
                for (int r = 0; r < k; r++) {
                    double dsi = ds[offset + r];
                    if (dsi < 0) {
                        double si = sAll[offset + r];
                        double cand = 0.99 * si / (-dsi);
                        if (cand < alpha) {
                            alpha = cand;
                        }
                    }
                }
            } else if (block.cone instanceof SecondOrderCone) {
                double t0 = sAll[offset];
                double dt = ds[offset];
                double[] u0 = mySocU0.get(socIdx);
                double[] du = mySocDu.get(socIdx);
                for (int i = 1; i < k; i++) {
                    u0[i - 1] = sAll[offset + i];
                    du[i - 1] = ds[offset + i];
                }
                double u0Norm2 = 0.0, duNorm2 = 0.0, u0du = 0.0;
                for (int i = 0; i < k - 1; i++) {
                    double ui0 = u0[i];
                    double dui = du[i];
                    u0Norm2 += ui0 * ui0;
                    duNorm2 += dui * dui;
                    u0du += ui0 * dui;
                }
                double a = dt * dt - duNorm2;
                double b = 2.0 * (t0 * dt - u0du);
                double c = t0 * t0 - u0Norm2;
                double alphaSOC = 1.0;
                if (a == 0) {
                    if (b < 0) {
                        double root = -c / b; // linear case
                        if (root > 0) {
                            alphaSOC = 0.99 * root;
                        }
                    }
                } else {
                    double disc = b * b - 4.0 * a * c;
                    if (disc > 0) {
                        double sqrtD = Math.sqrt(disc);
                        double r1 = (-b + sqrtD) / (2.0 * a);
                        double r2 = (-b - sqrtD) / (2.0 * a);
                        double limit = Double.POSITIVE_INFINITY;
                        if (r1 > 0) {
                            limit = Math.min(limit, r1);
                        }
                        if (r2 > 0) {
                            limit = Math.min(limit, r2);
                        }
                        if (Double.isFinite(limit)) {
                            alphaSOC = 0.99 * limit;
                        }
                    }
                }
                if (dt < 0) {
                    double limitT = -t0 / dt;
                    if (limitT > 0 && limitT < alphaSOC) {
                        alphaSOC = 0.99 * limitT;
                    }
                }
                if (alphaSOC < alpha) {
                    alpha = alphaSOC;
                }
                socIdx++;
            }
            offset += k;
        }
        if (!(alpha > 0)) {
            alpha = 1.0e-3;
        }
        return Math.min(1.0, alpha);
    }

    private void initialiseStart() {
        // Start at midpoint (0.5) to ensure strictly interior for box constraints 0<=x<=1
        for (int i = 0; i < myX.countRows(); i++) {
            myX.set(i, 0.5);
        }
        // Ensure SOC blocks start strictly interior: t > ||u||, choose t=1.0, u=0.0
        if (myProblem.A != null && !myProblem.cones.isEmpty()) {
            int offset = 0;
            for (ConeBlock block : myProblem.cones) {
                if (block.cone instanceof SecondOrderCone) {
                    int k = block.cone.size();
                    // For each row in this SOC block find the single column with -1 coefficient
                    int tCol = -1;
                    List<Integer> uCols = new java.util.ArrayList<>();
                    for (int r = 0; r < k; r++) {
                        int globalRow = offset + r;
                        for (int c = 0; c < myProblem.n; c++) {
                            double a = myProblem.A.doubleValue(globalRow, c);
                            if (a == -1.0) { // mapping row -> variable
                                if (r == 0) {
                                    tCol = c;
                                } else {
                                    uCols.add(c);
                                }
                                break; // exactly one -1 per row
                            }
                        }
                    }
                    if (tCol >= 0) {
                        myX.set(tCol, 1.0); // t
                    }
                    for (int uc : uCols) {
                        myX.set(uc, 0.0); // u components
                    }
                }
                offset += block.cone.size();
            }
        }
        if (myProblem.Aeq != null && myProblem.Aeq.countRows() > 0) {
            // Solve min ||Aeq x - beq|| using normal equations (Aeq^T Aeq) x = Aeq^T beq
            int n = myProblem.n;
            int meq = (int) myProblem.Aeq.countRows();
            R064Store ata = R064Store.FACTORY.make(n, n);
            R064Store atb = R064Store.FACTORY.make(n, 1);
            for (int r = 0; r < meq; r++) {
                double br = myProblem.beq.doubleValue(r);
                for (int c = 0; c < n; c++) {
                    double a_rc = myProblem.Aeq.doubleValue(r, c);
                    atb.add(c, 0, a_rc * br);
                    for (int k = 0; k < n; k++) {
                        ata.add(c, k, a_rc * myProblem.Aeq.doubleValue(r, k));
                    }
                }
            }
            // Regularise
            for (int i = 0; i < n; i++) {
                ata.add(i, i, 1e-8);
            }
            Cholesky<Double> chol = Cholesky.R064.make(ata);
            if (chol.compute(ata)) {
                PhysicalStore<Double> sol = (PhysicalStore<Double>) chol.getInverse().multiply(atb);
                for (int i = 0; i < n; i++) {
                    // Only overwrite if not part of a SOC we just set (keep interior assignment for SOC)
                    if (myX.doubleValue(i) == 0.5) {
                        myX.set(i, sol.doubleValue(i));
                    }
                }
            }
        }
    }

    private boolean isFeasible() {
        if (myProblem.A != null) {
            // Check cone-wise feasibility (strict interior)
            int offset = 0;
            for (ConeBlock block : myProblem.cones) {
                int k = block.cone.size();
                boolean blockOK = true;
                for (int r = 0; r < k; r++) {
                    double slack = myProblem.b.doubleValue(offset + r) - this.rowDot(myProblem.A, offset + r, myX);
                    if (slack <= 0.0) {
                        blockOK = false;
                        break;
                    }
                }
                if (!blockOK) {
                    return false;
                }
                offset += k;
            }
        }
        if (myProblem.Aeq != null) {
            for (int i = 0; i < myProblem.Aeq.countRows(); i++) {
                double r = this.rowDot(myProblem.Aeq, i, myX) - myProblem.beq.doubleValue(i);
                if (Math.abs(r) > 1e-6) {
                    return false;
                }
            }
        }
        return true;
    }

    // New: Merit function at current point using stored slacks & residual norms
    private double meritValue(final Access1D<?> x, final double[] sAll, final double mu, final double rdInf, final double rpInf) {
        double val = this.objectiveValue(x);
        if (myProblem.A != null) {
            int offset = 0;
            for (ConeBlock block : myProblem.cones) {
                int k = block.cone.size();
                if (block.cone instanceof NonnegativeCone) {
                    for (int i = 0; i < k; i++) {
                        double s = sAll[offset + i];
                        if (!(s > 0.0)) {
                            return Double.POSITIVE_INFINITY;
                        }
                        val -= mu * Math.log(s);
                    }
                } else if (block.cone instanceof SecondOrderCone) {
                    double t = sAll[offset];
                    double norm2 = 0.0;
                    for (int i = 1; i < k; i++) {
                        norm2 += sAll[offset + i] * sAll[offset + i];
                    }
                    double delta = t * t - norm2;
                    if (!(t > 0.0 && delta > 0.0)) {
                        return Double.POSITIVE_INFINITY;
                    }
                    val -= mu * Math.log(delta);
                }
                offset += k;
            }
        }
        // residual penalty using equality-only rpInf and dual rdInf
        val += MERIT_RES_COEFF * (rdInf + rpInf);
        return val;
    }

    // Trial merit for line search (barrier only, ignore residual changes for cheap evaluation)
    private double meritValueTrial(final Access1D<?> x, final Access1D<?> dx, final double alpha, final double mu) {
        double val = 0.0;
        int n = myProblem.n;
        if (myProblem.Q != null) {
            for (int i = 0; i < n; i++) {
                double xi = x.doubleValue(i) + alpha * dx.doubleValue(i);
                double quad = 0.0;
                for (int j = 0; j < n; j++) {
                    double xj = x.doubleValue(j) + alpha * dx.doubleValue(j);
                    quad += myProblem.Q.doubleValue(i, j) * xj;
                }
                val += 0.5 * xi * quad;
            }
        }
        for (int i = 0; i < n; i++) {
            val += myProblem.c.doubleValue(i) * (x.doubleValue(i) + alpha * dx.doubleValue(i));
        }
        if (myProblem.A != null) {
            int mineq = (int) myProblem.A.countRows();
            int offset = 0;
            for (ConeBlock block : myProblem.cones) {
                int k = block.cone.size();
                if (block.cone instanceof NonnegativeCone) {
                    for (int r = 0; r < k; r++) {
                        double s = myProblem.b.doubleValue(offset + r) - this.rowDot(myProblem.A, offset + r, x, dx, alpha);
                        if (!(s > 0.0)) {
                            return Double.POSITIVE_INFINITY;
                        }
                        val -= mu * Math.log(s);
                    }
                } else if (block.cone instanceof SecondOrderCone) {
                    double t = myProblem.b.doubleValue(offset) - this.rowDot(myProblem.A, offset, x, dx, alpha);
                    double norm2 = 0.0;
                    for (int r = 1; r < k; r++) {
                        double sr = myProblem.b.doubleValue(offset + r) - this.rowDot(myProblem.A, offset + r, x, dx, alpha);
                        norm2 += sr * sr;
                    }
                    double delta = t * t - norm2;
                    if (!(t > 0.0 && delta > 0.0)) {
                        return Double.POSITIVE_INFINITY;
                    }
                    val -= mu * Math.log(delta);
                }
                offset += k;
                if (offset >= mineq) {
                    break;
                }
            }
        }
        return val; // residual penalty omitted intentionally for speed
    }

    /** Outline objective evaluation (supports optional Q). */
    private double objectiveValue(final Access1D<?> x) {
        double val = 0.0;
        if (myProblem.Q != null) {
            for (int i = 0; i < myProblem.n; i++) {
                double xi = x.doubleValue(i);
                double quad = 0.0;
                for (int j = 0; j < myProblem.n; j++) {
                    quad += myProblem.Q.doubleValue(i, j) * x.doubleValue(j);
                }
                val += 0.5 * xi * quad;
            }
        }
        for (int i = 0; i < myProblem.n; i++) {
            val += myProblem.c.doubleValue(i) * x.doubleValue(i);
        }
        return val;
    }

    /** Predictor direction (affine scaling). */
    @SuppressWarnings("unused")
    private void predictorDirection() {
        // TODO(#CONIC-AFFINE-PREDICTOR): Assemble KKT (without centering) and solve for search direction
    }

    /** Variant using a trial step: row_r(A) * (x + alpha*dx). */
    private double rowDot(final MatrixStore<Double> A, final int r, final Access1D<?> x, final Access1D<?> dx, final double alpha) {
        double sum = 0.0;
        for (int c = 0; c < A.countColumns(); c++) {
            double xc = x.doubleValue(c) + alpha * dx.doubleValue(c);
            sum += A.doubleValue(r, c) * xc;
        }
        return sum;
    }

    private double rowDot(final MatrixStore<Double> A, final int r, final R064Store x) {
        double sum = 0.0;
        for (int c = 0; c < A.countColumns(); c++) {
            sum += A.doubleValue(r, c) * x.doubleValue(c);
        }
        return sum;
    }

    /** Compute affine sigma parameter (Mehrotra). */
    @SuppressWarnings("unused")
    private double sigma(final double muAffine, final double muCurrent) {
        double ratio = muAffine / Math.max(PrimitiveMath.MACHINE_EPSILON, muCurrent);
        return ratio * ratio * ratio; // (mu_aff/mu)^3
    }

    /** Update iterate variables. */
    @SuppressWarnings("unused")
    private void updateIterates() {
        // TODO(#CONIC-UPDATE): x += alpha*dx, y += alpha*dy, s += alpha*ds, z += alpha*dz
    }

    private ConicSolver withDelegate(final ExpressionsBasedModel model) {
        return this;
    }

    /** Package-private accessor: last (approximate) duality gap s·z. */
    double gapValue() {
        return myLastGap;
    }

    /** Package-private access to immutable problem data for tests and diagnostics. */
    ConicProblem problem() {
        return myProblem;
    }

    /** Package-private accessor: last dual residual infinity norm. */
    double rdInf() {
        return myLastRdInf;
    }

    /** Package-private accessor: last primal residual infinity norm (equality constraints only). */
    double rpInf() {
        return myLastRpInf;
    }

    /** Build KKT RHS vectors from current gradient/residuals and (optionally) affine terms with sigma. */
    private void buildKktRhsFromResiduals(final double muUse, final double sigma, final Access1D<?> dxAff, final double[] dsAff) {
        final int n = myProblem.n;
        // rx = -g (g already contains barrier parts via buildGradientAndHessian(muUse))
        for (int i = 0; i < n; i++) {
            myRx.set(i, -myG.doubleValue(i));
        }
        // ry = beq - Aeq x
        if (myProblem.Aeq != null) {
            final int meq = (int) myProblem.Aeq.countRows();
            for (int r = 0; r < meq; r++) {
                myRy.set(r, myProblem.beq.doubleValue(r) - this.rowDot(myProblem.Aeq, r, myX));
            }
        } else {
            myRy.fillAll(0.0);
        }
        // Simple sigma-based damping (kept tiny) to bias towards central path if desired
        if (sigma > 0.0 && dxAff != null) {
            final double damp = 1.0e-12 * sigma;
            for (int i = 0; i < n; i++) {
                myRx.add(i, 0, -damp * dxAff.doubleValue(i));
            }
        }
    }

    /** Compute ds = -A * dx into outDs (size mineq). */
    private void reconstructDs(final Access1D<?> dx, final double[] outDs) {
        if (myProblem.A == null) {
            return;
        }
        final int mineq = (int) myProblem.A.countRows();
        final int n = myProblem.n;
        for (int r = 0; r < mineq; r++) {
            double Adx = 0.0;
            for (int c = 0; c < n; c++) {
                double a = myProblem.A.doubleValue(r, c);
                if (a != 0.0) {
                    Adx += a * dx.doubleValue(c);
                }
            }
            outDs[r] = -Adx;
        }
    }

    /** Compute dz = mu * H_barrier(s) * ds per cone into outDz (length m). */
    private void reconstructDzFromDs(final double[] sAll, final double[] dsAll, final double muUse, final double[] outDz) {
        int offset = 0;
        for (ConeBlock block : myProblem.cones) {
            final int k = block.cone.size();
            if (block.cone instanceof NonnegativeCone) {
                for (int i = 0; i < k; i++) {
                    double si = sAll[offset + i];
                    double dsi = dsAll[offset + i];
                    outDz[offset + i] = (si > PrimitiveMath.MACHINE_EPSILON) ? (-muUse * dsi / (si * si)) : 0.0;
                }
            } else if (block.cone instanceof SecondOrderCone) {
                // Fill work and dir stores (length k)
                for (int i = 0; i < k; i++) {
                    myConeWork.set(i, sAll[offset + i]);
                    myConeDir.set(i, dsAll[offset + i]);
                }
                block.cone.barrierHessianTimes(myConeWork, myConeDir, myConeWork); // H(s)*ds into work
                for (int i = 0; i < k; i++) {
                    outDz[offset + i] = muUse * myConeWork.doubleValue(i);
                }
            }
            offset += k;
        }
    }

    /** Dual joint fraction-to-boundary: max alpha such that z + alpha*dz stays in K*. */
    private double fractionToBoundaryDual(final Access1D<?> zAll, final double[] dzAll) {
        double alpha = 1.0;
        int offset = 0;
        for (ConeBlock block : myProblem.cones) {
            int k = block.cone.size();
            if (block.cone instanceof NonnegativeCone) {
                for (int i = 0; i < k; i++) {
                    double zi = zAll.doubleValue(offset + i);
                    double dzi = dzAll[offset + i];
                    if (dzi < 0) {
                        double cand = 0.99 * zi / (-dzi);
                        if (cand < alpha) alpha = cand;
                    }
                }
            } else if (block.cone instanceof SecondOrderCone) {
                double t0 = zAll.doubleValue(offset);
                double dt = dzAll[offset];
                double u0Norm2 = 0.0, duNorm2 = 0.0, u0du = 0.0;
                for (int i = 1; i < k; i++) {
                    double ui0 = zAll.doubleValue(offset + i);
                    double dui = dzAll[offset + i];
                    u0Norm2 += ui0 * ui0;
                    duNorm2 += dui * dui;
                    u0du += ui0 * dui;
                }
                double a = dt * dt - duNorm2;
                double b = 2.0 * (t0 * dt - u0du);
                double c = t0 * t0 - u0Norm2;
                double alphaSOC = 1.0;
                if (a == 0) {
                    if (b < 0) {
                        double root = -c / b;
                        if (root > 0) alphaSOC = 0.99 * root;
                    }
                } else {
                    double disc = b * b - 4.0 * a * c;
                    if (disc > 0) {
                        double sqrtD = Math.sqrt(disc);
                        double r1 = (-b + sqrtD) / (2.0 * a);
                        double r2 = (-b - sqrtD) / (2.0 * a);
                        double limit = Double.POSITIVE_INFINITY;
                        if (r1 > 0) limit = Math.min(limit, r1);
                        if (r2 > 0) limit = Math.min(limit, r2);
                        if (Double.isFinite(limit)) alphaSOC = 0.99 * limit;
                    }
                }
                if (dt < 0) {
                    double limitT = -t0 / dt;
                    if (limitT > 0 && limitT < alphaSOC) alphaSOC = 0.99 * limitT;
                }
                if (alphaSOC < alpha) alpha = alphaSOC;
            }
            offset += k;
        }
        if (!(alpha > 0)) alpha = 1.0e-3;
        return Math.min(1.0, alpha);
    }
}
