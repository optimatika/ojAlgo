# ConicSolver Workplan (ojAlgo)

Purpose
 - Build a high-performance, numerically robust conic solver (LP/QP/SOCP first; SDP-lite later) that can compete with Clarabel and MOSEK on relevant classes.
 - Keep the code clean, modular, and maintainable; prefer clarity over cleverness unless measured performance gains justify it.
 - Scope constraint: avoid changes outside `org.ojalgo.optimisation.conic` unless strictly necessary.
 - Implementation guidance must follow `.github/copilot-instructions.md` (coding style, comments as Javadoc, logging rules, TestUtils usage).

Compliance reminders
- Every milestone/task should be executed in line with `.github/copilot-instructions.md`.
- When reviewing older code, rectify deviations (missing Javadoc, stray comments, style drift) while implementing functionality.

Roadmap & status (Nov 27, 2025)
 - Legend: [x] done, [ ] pending, [~] partial/in progress
 - Status note (reviewed Dec 7, 2025): (all items verified against `.github/copilot-instructions.md`)
   - Re-audited `ConicSolver.java`, `KKTSystem.java`, `DenseKKTSystem.java`, and helper tests.
   - Field hygiene: removed duplicate iteration/state trackers in favour of `GenericSolver`’s counters, restored cached work buffers (`myBaseGrad`, `myAx`, `myATz`, `myConeWork`, etc.), and reintroduced `RTOL` so tolerances align with `M0` specs.
   - Comment/style compliance: converted inline field comments in `ConicSolver` to proper Javadoc per `.github/copilot-instructions.md`; no stray unattached comments remain inside the package.
   - Predictor–corrector gating verified again via `ConicSolverOptionsFlagTest`, now using the public constructor (factory removed).
   - M1.3.a caching: current build caches `Qx` (inside `myBaseGrad`) and inequality `A x`/`A^T z` as `R064Store` instances; SOC derivative paths still use manual loops—remaining work is to push reuse into those helpers.

 - M1.2 — Implement corrected RHS, explicit (ds, dz), joint fraction-to-boundary; minimal KKTSystemTest — [x] Done. Implemented in `ConicSolver`: `buildKktRhsFromResiduals(mu, sigma, dxAff, dsAff)`, `reconstructDs(dx, outDs)`, `reconstructDzFromDs(s, ds, mu, outDz)`, `fractionToBoundaryDual(z, dz)`. `KKTSystemTest.java` added.
 - Post-cleanup verification (Dec 7, 2025): rebuilding after the field/Javadoc and caching sweep keeps all `org.ojalgo.optimisation.conic.*Test` passing.

 - M1.3 — Low-risk iteration improvements (matrix multiplies reuse, PD–merit).
  - All M1 tasks must explicitly check for instruction compliance (reuse existing buffers, add Javadoc to new fields/methods, maintain formatting).
 
F0 — Foundations (completed)
- [x] Data model and integration
  - [x] Immutable `ConicProblem` with A, b, Aeq, beq, c, Q and cone blocks
  - [x] `ModelIntegration` for build() / toModelState() / toSolverState()
- [x] Cones & mapping
  - [x] `NonnegativeCone` (R+) barrier, gradient, Hessian-times, projection
  - [x] `SecondOrderCone` barrier, gradient, Hessian-times, projection
  - [x] Mapping of bounds/linear constraints to R+; SOC detection for pure diagonal constraints (uniform scaling) and mapping rows `-x + s = 0`
- [x] Linear algebra & iteration scaffold
  - [x] Dense KKT solver (Cholesky/LU) with small regularisation
  - [x] Prototype primal barrier loop with line search and fraction-to-boundary
  - [~] Residuals and certificates (basic Rd/Rp; approximate z) — will be tightened in M0/M1
- [x] Tests and forward-compatibility guardrails
  - [~] Test classes present: `ConePrimitivesTest`, `ConicIntegrationMappingTest`, `ConicQPTest`, `ConicSOCPTest`, `ConicMixedLinearSOCTest`, `ConicResidualsTest`, `ConicSolver*` — extend and keep verifying

M0 — Baseline correctness and harness (refined)
- Residuals/merit (consistent with barrier representation)
  - [x] Primal residual equality-only: `rp = Aeq x − beq` (inequalities handled via slacks)
  - [x] Dual residual `rd = Qx + c − Aeq^T y − A^T z`
  - [x] `z` from cone barrier gradients; gap `s·z` as duality proxy
  - [x] Merit `phi(x) = f(x) − μ·Σ F(s) + w_r (||rd||_inf + ||rp||_inf)`; trial merit barrier-only with Armijo
- Convergence & diagnostics
  - [x] Scale-aware tolerances: `atol=1e-8`, `rtol=1e-6`, scale from data (`computeDataScale()`)
  - [x] Terminate on `rdInf <= tol`, `rpInf <= tol`, and `gap <= rtol·(1+|f|)`
  - [x] Diagnostics via `BasicLogger` gated by `Options`
- Correctness targets
  - [~] Solve LP with bounds/equalities; simple QP; SOC feasibility maintained for mapped SOC blocks (unit tests present; continue validating)
  - [~] No allocations in the hot loop (visually good; confirm with profiler/JMH later)
- Acceptance: residuals ≤ 1e-6; iterations ≤ 100; toy LP/QP/SOCP tests pass

M1 — Interior-point core (refinements)
- M1.2 — Corrected RHS, explicit (ds, dz), joint FTB; minimal KKTSystemTest — [x] Done
  - Status: Implemented and in use; KKTSystemTest passes.
- M1.3 — Low-risk iteration improvements — [~] Partial
  - M1.3.a — Matrix multiplies reuse (Qx, A x, A^T z) — [~] In progress
    - [x] M1.3.a.1 — Qx caching
      - Implemented: `Qx` formed via `myProblem.Q.multiply(myX).supplyTo(myBaseGrad)`; `myBaseGrad` then adds `c` and serves as the cached base gradient `Qx + c`.
    - [~] M1.3.a.2 — Ax caching and reuse
      - [x] Introduce `R064Store myAx` and `refreshAx(Access1D<?> x)`; use it for slack refresh / line search / merit.
      - [ ] Extend reuse into `reconstructDs` and SOC helpers (`fractionToBoundary`, SOC FTB buffers) to avoid fresh `rowDot` loops for `A*dx`.
    - [~] M1.3.a.3 — ATz caching and reuse
      - [x] Add `R064Store myATz` and `refreshATz(Access1D<?> z)`; implement explicit `A^T z` accumulation into `myATz`.
      - [x] Replace manual `A^T z` accumulation in `computeResiduals()` with reads from `myATz`.
      - [ ] Reuse `myATz` in any future helpers that need `A^T z` (currently only residual assembly uses it; keep this item pending until additional users appear).
  - M1.3.b — Options flag to toggle predictor–corrector — [x] Done
    - Implemented: Configuration.usePredictorCorrector (default true) read from Options configurator; barrier-only path uses affine step and primal-only FTB.
    - Acceptance: both modes solve `KKTSystemTest` within tolerance; default path unchanged; no measurable perf regression on smoke benchmarks. Validated with `ConicSolverOptionsFlagTest`.
  - M1.3.c — Primal–dual merit in line search (PD–merit) — [ ] Planned
    - `Configuration.usePrimalDualMerit` exists as a placeholder; enabling it currently has no effect until PD–merit is implemented here.
    - Use composite PD–merit for step acceptance; keep barrier-only as fallback behind the same option.
    - Acceptance: backtracking enforces monotone decrease in PD–merit; unit test demonstrates PD–merit contraction on a crafted step; no acceptance regressions on smoke problems.
- Line search & termination
  - [ ] Backtracking on primal–dual merit; update `μ` with Mehrotra rule; floor at 1e−12
  - [x] Terminate by primal/dual residuals and gap; produce simple certificates when possible

M2 — Scaling and presolve (first pass)
- [ ] Symmetric Ruiz scaling for A and cone blocks; objective scaling; unscale on exit
- [ ] Simple presolve: fixed variables, empty/duplicate constraints, bound tightening, early infeasibility
- Execution note: do not introduce new helpers when an existing ojAlgo utility suffices; document any unavoidable deviations in Javadoc per the instructions.
- Acceptance: fewer iterations and improved conditioning on ill-scaled cases; scaling invariance tests

M3 — Cone scaling (NT) and rotated SOC
- [ ] Nesterov–Todd scaling for SOC (and rotated SOC); per-iteration updates
- [ ] Rotated SOC cone implementation and mapping
- Acceptance: improved step lengths/iterations across SOC instances vs M1/M2

M4 — HSDE (homogeneous self-dual embedding)
- [ ] HSDE system and variables (tau, kappa) with line search/termination updates
- [ ] Certificates (infeasible/unbounded) and mapping back
- Acceptance: correct detection on crafted infeasible/unbounded cases; stability comparable to Clarabel HSDE

M5 — Linear algebra upgrade (sparse + quasi-definite)
- [ ] Indefinite LDL^T (Bunch–Kaufman) and caching; iterative refinement
- [ ] Sparse path using ojAlgo sparse stores; Schur complement variants where beneficial
- Acceptance: speedups on medium/large problems; JMH for factorisation/solves

M6 — Features and polishing
- [ ] Warm-starts; crossover/polishing for LP/QP; feasibility polishing
- [ ] Optional additional cones (exponential/power); SDP-lite (diagonal or small block PSD)
- Acceptance: warm-start benefits; accuracy preserved; gated tests for new cones

M7 — Performance hardening
- [ ] Profiling-driven micro-optimisations (buffer reuse, cache locality, selective unrolling)
- [ ] Safe concurrency (block-wise cone ops) via `org.ojalgo.concurrent`
- Acceptance: documented speedups with JMH; no accuracy regressions

High-level design
- Core algorithm: primal–dual predictor–corrector IPM with optional HSDE for uniform infeasibility/unboundedness handling.
- Canonical form: minimise c^T x + 1/2 x^T Q x subject to A x + s = b, s in product cone K; equalities Aeq x = beq; duals y (eq) and z in K*.
- Cones: start with R+ (nonnegative) and SOC; add rotated SOC; later, SDP-lite; consider exponential/power cones as stretch goals.
- Linear algebra: stable quasi-definite KKT solves. Start dense; then introduce a sparse path (LDL^T/Bunch–Kaufman or Schur complement) with fill-reducing orderings.
- Presolve/scaling: symmetric equilibration (Ruiz), constraint elimination, bound tightening, and cone-aware scaling; keep it measurable and optional.

APIs to keep stable
- `ConicSolver` implements `Optimisation.Solver`.
- `ModelIntegration` for building from `ExpressionsBasedModel` and mapping results back.
- `ConicProblem`, `Cone`, `ConeBlock` remain the public/packaged descriptors under `conic`.
- No changes to classes outside the `conic` package (unless coordinated).

Testing strategy
- Deterministic unit tests using `TestUtils`:
  - Cone primitives: feasibility, projection, barrier derivatives (gradient and Hessian-times) with finite-difference checks.
  - KKT solves on synthetic quasi-definite systems; refinement effectiveness.
  - Integration mapping: bounds/linear constraints/SOC detection round-trips and result mapping.
  - Solver convergence: LP/QP/SOCP families; infeasible/unbounded detection post-HSDE.
- Fuzz/randomised property tests for scalability and stability (bounded size, seeded RNG).
- Benchmarks: JMH microbenches for KKT solves and cone ops; end-to-end timing vs existing `ConvexSolver` on overlapping classes.

NEW: Test evolution & forward compatibility
- Goal: Existing tests remain valid as algorithms improve (fewer iterations, better precision, new features) without needing rewrites.
- Principles:
  - Behaviour over implementation: assert on solution feasibility, objective value (within tolerance), and certificate correctness—not internal iteration counts or mu trajectories.
  - Stable invariants: use problem-level invariants (KKT conditions, cone membership, primal/dual residual norms) rather than transient algorithm details.
  - Tolerances adaptive: express numeric assertions with relative and absolute components: abs(residual) <= atol + rtol * scale, where scale derives from max(|A|, |b|, |c|, |Q|). This avoids brittleness when scaling logic changes.
  - Non-regression layering: keep baseline small canonical problem set (smoke tests) plus an extended suite. Improvements may replace expected values only if mathematically tighter (e.g. smaller gap) but never loosen tolerances.
  - Feature-focused additions: each new milestone adds tests only for its new capabilities (e.g. HSDE certificates) while re-running full prior suite; avoid modifying earlier tests unless fixing a genuine incorrect expectation.
  - No hard iteration ceilings: do not assert exact iteration counts; instead assert convergence within a reasonable maximum (e.g. < options.iterations_abort) and optionally record the achieved count for performance tracking (without failing).
  - Random tests reproducible: seed all RNG usage and log seed when a failure occurs.
  - Separation of concerns: split tests into categories (integration mapping, cones, KKT, solver convergence, certificates, scaling) so refactors affect only relevant subsets.
  - Certificate robustness: when HSDE implemented, tests should verify logical conditions with tolerance, not the exact embedded variables.
  - Backwards tolerance monotonicity: never increase permitted residual/gap thresholds in existing tests—only maintain or tighten.
- Suggested structure (under `src/test/java/org/ojalgo/optimisation/conic/`):
  - `ConePrimitivesTest`, `KKTSystemTest`, `ModelIntegrationTest`
  - `ConicLPTest`, `ConicQPTest`, `ConicSOCPTest`
  - `ScalingPresolveTest` (M2+), `NesterovToddScalingTest` (M3+), `HSDETest` (M4+), `SparseLinearAlgebraTest` (M5+)
- Performance guard tests: optional non-failing performance snapshots using JMH baseline logs; only gates on CI if variance is within defined bands (future enhancement).

Numerical details and defaults
- Tolerances: start with 1e-6 for residuals and gap; configurable via `Options`.
- Line search: backtracking with Armijo condition; fraction-to-boundary 0.99; safeguards at 1e-12.
- Mu update: Mehrotra-style using affine-scaling predictor; reduce when near central path; floor at 1e-12.
- Regularisation: dynamic diagonal shifts to maintain quasi-definiteness; iterative refinement post solve.

Implementation notes (map to code TODO tags)
- #CONIC-AFFINE-PREDICTOR: build affine KKT without centering and compute (dx, ds, dy, dz).
- #CONIC-UPDATE: consistent update of all primal/dual variables.
- #CONIC-NT-SCALING: per-cone scaling; start with SOC; extend to rotated SOC.
- #CONIC-HSDE: embedding variables (tau, kappa) and system; certificates.
- Residuals/merit tightening touches: `computeResiduals`, `checkConvergenceAndCertificates`, `meritValue`, `meritValueTrial`, and line search block in `barrierIteration`.

Gaps found in audit (Nov 14, 2025)
- Still open: scaling/equilibration; presolve (bounds/row/col reductions); HSDE/infeasibility certificates and robust termination; sparse KKT path and data structures; warm starts/crossover strategy.
- Planned in M1.3: manual multiply loops removal; PD‑merit for line search. (Options toggle implemented.)

Status snapshot (Nov 27, 2025)
- Implemented in code:
  - `ConicProblem`, `ModelIntegration`, `NonnegativeCone`, `SecondOrderCone`, `KKTSystem`/`DenseKKTSystem`.
  - Primal barrier loop with barrier-gradient/Hessian contributions, fraction-to-boundary, Armijo backtracking, scaled termination, and gated diagnostics.
  - Residuals: equality-only `rp`, dual `rd`, `z` from cone barrier gradients, gap `s·z`.
  - Predictor–corrector skeleton (M1.1): affine predictor `(dx_aff, dy_aff)`, `ds_aff` and `α_aff`, `μ_aff` and `σ`, centered-μ corrector KKT solve `(dx, dy)`; tests for cones/integration/LP/QP/SOCP present.
  - M1.2 additions: corrected RHS helper, explicit `(ds, dz)` reconstruction, and joint fraction-to-boundary. Added `KKTSystemTest`.
  - M1.3.b additions: options flag and gating; `ConicSolverOptionsFlagTest` validates both modes.
  - M1.3.a first step: `Qx` computed via `MatrixStore#fillByMultiplying`, keeping the gradient build allocation-free while relying on ojAlgo’s tuned BLAS path.
- Not yet implemented:
  - Presolve/scaling (Ruiz), rotated SOC, HSDE, sparse/indefinite linear algebra, warm-starts; remaining M1.3.a work for `A x`/`A^T z`; M1.3.c PD-merit.

## M1.3.b implementation details (Nov 14, 2025)
- Added `ConicSolver.Configuration` with `usePredictorCorrector` and `usePrimalDualMerit` (placeholder) fields.
- Read configuration via `options.getConfigurator(Configuration.class)`, defaulting to enabled PC.
- In `barrierIteration`, when disabled: reuse affine `(dx, dy)` as final step and primal-only fraction‑to‑boundary; when enabled: full corrector path as before (joint `(s,z)` safeguarding).
- New test: `ConicSolverOptionsFlagTest` runs a tiny LP in both modes and asserts non-failure and sensible solution/value bounds.

## Recommended next step
- M1.3.a — cache `A x` and `A^T z`
- Rationale: Extends the same multiply reuse pattern to the inequality stacks, unlocking larger speedups because `rowDot` appears in slacks (twice per iteration) and residual assembly. Preallocating `Ax` and `ATz` also simplifies future PD-merit work where residual norms are recomputed frequently.
- Changes (contained within `ConicSolver`; no external deps):
  - Introduce reusable `R064Store myAx` and `R064Store myATz`; replace `rowDot` usage in slack refresh and residual build with `fillByMultiplying` and `transpose().multiply(...)` backed by the existing stores (or `TransformableRegion` views) to avoid reallocation.
  - Ensure SOC fraction-to-boundary logic reads from the cached slacks/dual products instead of re-dotting rows.
- Acceptance: tests green; no allocation regressions; measurable speedup on dense problems (or no regression if too small to measure).

Immediate next steps (iteration 3 → 4)
- [ ] M1.3.a — Opportunistic reuse: swap manual `Qx`, `A x`, `A^T z` accumulations for `MatrixStore` ops into preallocated buffers; verify with microbench
- [ ] M1.3.c — PD‑merit line search gated by the option; add a tiny unit test asserting merit decrease on accepted step

Definition of Done (first usable solver)
- Solves LP/QP/SOCP instances up to a few thousand variables with iterations ≤ 80 and residuals/gap ≤ 1e-6.
- Detects simple infeasible/unbounded cases (HSDE or reliable heuristics) and returns meaningful states.
- Outperforms `ConvexSolver` on SOC-heavy problems of moderate size; parity on LP/QP.

References
- Nesterov–Todd scaling; Mehrotra predictor–corrector; HSDE (Ye, Saigal; and modern Clarabel notes).
- Cross-check behaviour vs Clarabel/MOSEK where applicable (no runtime dependencies; just benchmarks/targets).

Chosen next step (actionable, small, high impact)
- Step: Continue M1.3.a — cache `A^T z` and reuse cached multiplies inside ds/dz reconstruction
- Rationale: With `Ax` cached, the remaining high-frequency dot products come from forming `A^T z` in `computeResiduals` and recomputing `A x` inside `reconstructDs`, `fractionToBoundary`, and SOC helpers. Introducing a persistent `myATz` buffer (filled via `A.transpose().multiply(z)`) removes the dual residual row loops, and teaching `reconstructDs` (and SOC FTB helpers) to read from cached `Ax` reduces redundant multiplies. This clears the path for the upcoming PD-merit work where residuals are evaluated repeatedly.
- Changes (contained within `ConicSolver`; no external deps):
  - Add `refreshATz()` mirroring `refreshAx()` and call it before `computeResiduals` to populate `myATz`; use the cached column contributions when assembling `myRd`.
  - Refactor `reconstructDs` (and the SOC fraction-to-boundary helpers) to reuse cached `Ax` and/or incremental updates rather than recomputing `A x` via `rowDot` loops.
  - Ensure helpers guard against `null` buffers so degenerate cases (no inequalities) remain allocation-free; keep tests green.
- Step: Continue M1.3.a — finish multiply reuse (`A^T z`, derivative helpers)
- Rationale: `Ax` caching removed the highest-volume rowDot loops but dual residual assembly (`A^T z`) and derivative helpers (`reconstructDs`, SOC FTB) still churn through row-wise products. Caching `A^T z` via `myATz` and reusing the existing `Ax` buffer (or incremental ds) inside helper routines trims the remaining hot-loop allocations and prepares the ground for PD-merit evaluations that repeatedly query residuals.
- Changes (contained within `ConicSolver`; no external deps):
  - Introduce `refreshATz()` to populate `myATz` (if `A` exists) using `A.transpose().multiply(z)`; reuse it when forming `myRd`.
  - Teach `reconstructDs`, `fractionToBoundary`, and SOC derivative helpers to read from cached `Ax`/`ds` buffers rather than recomputing row dots, only falling back when caches are stale.
  - Re-run `ConicSolver*` and `KKTSystemTest` to confirm no behavioural regressions and collect quick before/after timing on a dense SOCP toy for sanity.

## Iteration Loop & bookkeeping
- * Core iteration loop in `ConicSolver` (primal-dual predictor-corrector); calls `barrierIteration` for each IPM step.
- * Track predictor-corrector iteration counts locally; consider wiring up GenericSolver utilities later.
+ * Iteration counting now delegates to `GenericSolver` (`isIterationAllowed`, `incrementIterationsCount`). No duplicate counters in `ConicSolver`. Future enhancements (progress logging, adaptive stopping) should leverage the shared base class infrastructure.
 - * `barrierIteration` performs a single IPM step: computes search directions (dx, ds, dy, dz), predicts centering parameter (σ), and updates primal/dual variables.
 - * Line search (backtracking) on merit function; updates `μ` (barrier parameter) with Mehrotra rule.
 - * Termination checks: primal/dual residuals, gap, and optional custom criteria.
 - * Remove unused placeholder fields (delegates, tau/kappa scalars) once HSDE implementation is ready.
  - Sanity check: when new HSDE state is added, ensure documentation/comments follow the `.github/copilot-instructions.md` Javadoc-first rule.