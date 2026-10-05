# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Added / Changed / Deprecated / Fixed / Removed / Security

## [Unreleased]

> Corresponds to changes in the `develop` branch since the last release

### Added

#### org.ojalgo.array

- `DensityTrackingArray` has `supplyTo(DensityTrackingArray)`, `axpy(double, double[])`, `dot(double[])`, `tighten(double)`, `reindex()`, `invalidateIndex()` and `setNonzeroCount(int)`, for code that works directly with the values and the index of nonzeros.

#### org.ojalgo.data.domain.finance

- `FinanceUtils` conversions between the expected values and covariances of returns and of growth rates (logarithmic returns), assuming log-normal growth factors: `toExpectedReturnsFromGrowthRates`, `toCovariancesFromGrowthRates`, `toExpectedGrowthRatesFromReturns` and `toGrowthRateCovariancesFromReturns`.

#### org.ojalgo.data.domain.finance.portfolio

- `BlackLittermanModel.Mode`: `RETURNS` (as before, the default), `FULL` (He & Litterman, posterior covariances) and `MARKET` (Meucci, conditional covariances). Each mode has a default confidence (tau).
- `BlackLittermanModel.addViewWithConfidenceLevel(...)`: Idzorek's percentage confidence.
- Static factory methods, `of(...)` (and `SimpleAsset.ofWeight`, `SimplePortfolio.ofWeights`), replacing the public constructors.

#### org.ojalgo.matrix.decomposition

- `SparseLU.ftranColumn(R064CSC, int, DensityTrackingArray)` and `btranUnit(int, DensityTrackingArray)`: sparse solves for a column of a sparse matrix and for a unit vector. They keep partial results that a following `updateColumn(int, R064CSC, int)` reuses.
- `MatrixDecomposition.Resizable`, implemented by `Cholesky`: updates the decomposition of a symmetric/Hermitian matrix when a row/column is appended (`appendColumn`) or removed (`removeColumn`), in O(n²) rather than recalculating it.

#### org.ojalgo.matrix.store

- `R064CSC.of(...)` and `R064CSR.of(...)` convert any matrix to compressed sparse form by the cheapest route, optionally keeping only the elements whose position a `Structure2D.IntRowColPredicate` accepts.
- `R064CSR.premultiply(DensityTrackingArray, DensityTrackingArray)`: a sparse vector times the matrix, visiting only the rows where the vector is nonzero.
- `R064CSC.supplyTo(int, DensityTrackingArray)` and `R064CSR.supplyTo(int, DensityTrackingArray)` copy one column or row into a `DensityTrackingArray`.

#### org.ojalgo.optimisation

- Degenerate coefficient detection in presolving. A new `DEGENERATE` analyser scans expression coefficients for near-zero values and filters them from presolver logic, preventing false infeasibility from floating-point noise. Controlled by `Options.degeneracy`. (Discussion [#691](https://github.com/optimatika/ojAlgo/discussions/691))
- `Options.getConfiguredGapTolerance()`: the MIP gap, but only when it is configured to something other than the default. 3rd party solver integrations pass it on to their solvers.
- For solver integrations: `ExpressionsBasedModel.Integration.getObjectiveConstant(ExpressionsBasedModel)`, `expandFreeToFull(...)` with the solver's `Optimisation.Sense`, and `ConstraintsMetaData` support for signed (two-sided) row multipliers, rows built from unadjusted parameters, and `withLimits(int, double, double)`.

#### org.ojalgo.structure

- `Structure2D.IntRowColPredicate`, a predicate on (row, column) positions.

### Changed

#### org.ojalgo.array

- `DensityTrackingArray.countNonzeros()` and `density()` count the listed positions, which may include values that have become zero (set to zero, or cancelled when adding), until the next `reset()`, `reindex()` or `tighten(double)`. Previously setting a value to zero made the next call rescan, so the count was exact.

#### org.ojalgo.data.domain.finance

- `FinanceUtils.calculateValueAtRisk(...)` uses a geometric Brownian motion (log-normal), not a normal distribution.

#### org.ojalgo.data.domain.finance.portfolio

- Value at Risk uses the same geometric Brownian motion model as `forecast()` and `getLossProbability()`, not a normal distribution.
- `MarkowitzModel` target return is solved as one QP (minimum variance with return at least the target), and the target variance search no longer depends on the initial risk aversion. Targets outside the attainable range give the minimum variance or maximum return portfolio.
- The risk aversion, and the Black-Litterman confidence, must be positive (`IllegalArgumentException`, previously silently changed). `calibrate(...)` returns `boolean`, and leaves the risk aversion unchanged if the implied one is not positive.
- The new `PortfolioOptimiser` is now the return type of `optimiser()` (was the nested `OptimisedPortfolio.Optimiser`, which could not be named outside the package).
- `MarkowitzModel.addConstraint` and `PortfolioMixer.add*Constraint` return `void`, and a constraint on the same set of indices replaces the previous one.
- An unbounded optimisation gives zero weights, as other failures do.
- `CharacteristicLine` is reimplemented – an instance is the line of one asset against the market, created with `assumingCAPM(...)`, `of(...)` or `estimate(...)`. The previous, unfinished, API is removed.

#### org.ojalgo.matrix.decomposition

- `SparseLU` is rewritten. Simplex bases are factorised with a triangular (singleton) pass followed by Markowitz pivoting, the sparse solves are hyper-sparse when the right hand side and the result are sparse enough, and Forrest-Tomlin updates reuse the partial results of the preceding solves.
- `MinimumDegree` computes the same ordering as before, by a counting sort on the degrees, in linear instead of quadratic time.
- `MatrixDecomposition.Updatable.updateColumn(...)` is an optional operation – a default method that returns false.

#### org.ojalgo.optimisation

- `ExpressionsBasedModel.newExpression` (and `addExpression`) now throws an `IllegalArgumentException` if the model already contains an expression with that name. Previously the new expression silently replaced the first – a constraint was lost without notice. `addExpression()` generates a name that does not clash with existing ones.
- `Expression` now allocates the quadratic coefficient map lazily — only when quadratic terms are actually added. Models with only linear expressions use less memory.
- The dual values and the reduced gradient of a `Result` follow one documented convention, whatever the solver: multipliers of the minimisation form Lagrangian (non-negative for inequalities), and reduced costs at the variables' bounds, in model units and relative to the (presolve tightened) bounds.
- Model parameters are scaled by the adjustment exponent using `BigDecimal.scaleByPowerOfTen` instead of `movePointLeft`/`movePointRight`, so no digits are expanded when the scale becomes negative. `ModelEntity.adjust(BigDecimal)`, `reverseAdjustment(BigDecimal)`, `getLowerLimit(boolean, BigDecimal)`, `getUpperLimit(boolean, BigDecimal)` and `Expression.get(..., boolean)` may therefore return numbers with a negative scale (the values are the same).

#### org.ojalgo.optimisation.convex

- The active set solver is faster and more robust. The direct variant maintains a Cholesky decomposition of the Schur complement (updated, not recalculated, as constraints are included/excluded), linearly dependent constraints no longer make it cycle, and there is an iteration limit.
- Default solver choice: the direct active set solver unless `Options.sparse` is `TRUE`, the null space variant for more problems (when there are no more than 64 variables per equality constraint), and ADMM when m + n ≥ 826 (was 750).
- The iterative active set solver's default accuracy is `NumberContext.of(16, 12)` (was `of(10, 16)`, with which the iterative solutions were practically never used). The precision and scale are used differently – see `ConvexSolver.Configuration.iterative(NumberContext)`.
- ADMM: at most 10 000 iterations (was 20 000), extended to twice that while predicted to converge, with convergence accuracy `NumberContext.of(9, 8)`.

#### org.ojalgo.optimisation.linear

- The revised (sparse) dual simplex is considerably faster, using the new `SparseLU`, and is used much more often. Unless `Options.sparse` says otherwise, the dual simplex (MIP node solves, or when asked for) uses it when m·n is at least 150 000, n/m at least 5, or m·n at least 40 000 with n/m at least 3, and the dense tableau otherwise (m constraints, n variables including slacks). Previously the revised simplex was only used for very large problems, or ones with at least 11 times as many variables as constraints.

#### org.ojalgo.type.context

- `NumberContext.isLessThan(BigDecimal, BigDecimal)` and `isMoreThan(BigDecimal, BigDecimal)` now use full `BigDecimal` tolerance comparison instead of converting to `double`. Added corresponding `double` overloads.
- `NumberContext.isDifferent(BigDecimal, BigDecimal)` added: tolerance-aware comparison that uses the larger absolute value of the two as the reference, as the `double` version does.
- The `BigDecimal` versions of `NumberContext.isSmall`, `isDifferent`, `isLessThan` and `isMoreThan` now use the same relative tolerance as the `double` versions, `|value| < |reference| · relative error`, so with the default rounding mode the two agree. Previously they checked whether rounding to the context changed the reference, which depended on where the reference was between two rounding steps, and also applied the scale to non-zero references. Comparisons with a reference that is zero (to the context's scale) are still decided by rounding.
- `NumberContext.isZero(BigDecimal)` rounds to the scale only, using the context's rounding mode. It no longer rounds to the precision first, which could make a number slightly larger than half a unit in the last decimal count as zero.
- `NumberContext.enforce(BigDecimal)` and `toBigDecimal(double)` no longer strip trailing zeros, and only round to the scale when the number has more decimals than the scale allows. Trailing zeros are neither added nor removed, as the class documentation describes (for example, 3.0 stays 3.0 rather than becoming 3). `enforce(double)` returns the same values as before, faster.
- Minor performance: `NumberContext` caches its `MathContext.getPrecision()` value and uses multiplication instead of division in the `double` version of `isSmall`.

### Deprecated

#### org.ojalgo.data.domain.finance.portfolio

- The public constructors of `BlackLittermanModel`, `EfficientFrontier`, `FixedReturnsPortfolio`, `FixedWeightsPortfolio`, `MarketEquilibrium`, `MarkowitzModel`, `PortfolioContext`, `PortfolioMixer`, `PortfolioSimulator`, `SimpleAsset` and `SimplePortfolio`. Use the static factory methods.

#### org.ojalgo.matrix.decomposition

- `SparseLU.getMaxPivotMagnitude()`, `getFactorMaxPivotMagnitude()` and `getFactorMinPivotMagnitude()` are no longer used internally. `getMinPivotMagnitude()` remains.

#### org.ojalgo.optimisation

- `ExpressionsBasedModel.Integration.expandFreeToFull(...)` without an `Optimisation.Sense` (assumes the solver minimises), and `computeReducedCostFromMultipliers(ExpressionsBasedModel, int, Result)` (ignores quadratic objective terms).

### Fixed

#### org.ojalgo.data.domain.finance.portfolio

- `getAssetVolatilities()` of the equilibrium models (including Markowitz and Black-Litterman) returned the correlations.
- Black-Litterman views and confidence, and Markowitz constraints, added after a calculation were ignored.
- `EfficientFrontier` gave wrong weights after `setShortingAllowed(true)`.
- `MarkowitzModel` target return/variance could miss the target, silently, when the risk aversion was not 1.
- `PortfolioMixer` applied asset and component constraints to the wrong variables.
- `BlackLittermanModel` without views threw an exception, `NormalisedPortfolio` could have a negative volatility, and the `SimplePortfolio` simulator's covariances were slightly off.

#### org.ojalgo.matrix.decomposition

- `SparseQDLDL.getSolution(...)` for a 1x1 matrix solved only the first of several right hand sides.

#### org.ojalgo.optimisation

- Presolver infeasibility checks (`REDUNDANT_CONSTRAINT`) now use tolerance-aware `isMoreThan`/`isLessThan` instead of raw `compareTo`, consistent with the model's feasibility context. (Discussion [#691](https://github.com/optimatika/ojAlgo/discussions/691))
- `SpecialOrderedSet` loop always read `mySequence[1]` instead of `mySequence[i]` — the loop variable was never used as the array index.
- Integrations with interface-based configurators could not find the registered configurator. Added `ExpressionsBasedModel.getConfigurator(Class)` to look up by declared type. (Issue [#692](https://github.com/optimatika/ojAlgo/issues/692))
- `ModelEntity.toAdjusted(BigDecimal)` and `reverseAdjustment(BigDecimal)` used the adjustment exponent without making sure it had been derived.
- Dual values, reduced gradients and objective function values could be wrong – not unscaled, of the wrong sign, or missing the objective constant – mostly with scaled (adjusted) model parameters, maximisation or presolve fixed variables. The wrong objective function values could make the `IntegerSolver` return non-optimal MIQP solutions.

#### org.ojalgo.optimisation.convex

- `AlternatingDirectionSolver.updateRange(...)` updated the wrong row, and the extended precision (iterative refinement) variant returned wrong dual values. The null space solver computed wrong multipliers and a `NaN` objective function value.
- The direct active set solver could return a non-optimal solution as optimal, and cycle (not terminate) when active constraints became linearly dependent.
- Unbounded problems with a singular [Q] were reported as optimal, with huge solutions, and ADMM reported them as infeasible.
- With a singular [Q] the solutions, multipliers and reduced gradients had an error proportional to the size of the solution (from the small constant added to the diagonal of [Q]).
- The null space (projection) variant gave meaningless solutions when the projected [Q] was zero apart from rounding errors.
- The active set solver could stop at a non-optimal solution when variables differed much in magnitude.
- Extended precision failed with problems without constraints.

#### org.ojalgo.optimisation.integer

- A node LP that failed (`FAILED`, such as on a singular basis) was treated as infeasible, and its branch pruned. Now the search stops without claiming optimality. Strong branching no longer takes a probe that fails, or stops short of optimality, as proof that the branch direction is infeasible.

#### org.ojalgo.optimisation.linear

- The LP solver could report an optimal solution that violated a constraint (netlib PILOT-JA). If the primal simplex iterations leave basic variables outside their bounds, by more than `Options.feasibility` allows, dual simplex iterations now restore feasibility.
- The primal ratio test no longer pivots on elements that are tiny relative to the rest of the entering column, unless nothing else limits the step. Such pivots could make the basis nearly singular, and the solve wrongly end as unbounded.
- A numerically singular basis in the revised simplex ends the solve as `FAILED`, rather than continuing with meaningless solves.

#### org.ojalgo.random.process

- `GeometricBrownianMotion.convert(double)` reset the current value to 1.0.

#### org.ojalgo.type

- `CalendarDate.valueOf(OffsetDateTime)` and `CalendarDate.valueOf(ZonedDateTime)` retain millisecond precision, matching the `Instant` overload. (Issue [#694](https://github.com/optimatika/ojAlgo/issues/694))
- `CalendarDate.toLocalDateTime(...)`, `toOffsetDateTime(...)` and `toZonedDateTime(...)` turned the milliseconds into nanoseconds (123 ms became 123 ns), and `toLocalTime(...)` returned an arbitrary time (`int` overflow).
- `CalendarDate` and `CalendarDateUnit` truncated instead of rounding down for instants before 1970: `getLong(INSTANT_SECONDS)`, `with(...)` and `adjustInto(Temporal)` (which threw an exception), and `adjustInto(long)`/`filter(...)`, which mapped pre-1970 instants to the next period (day, hour…).
- `CalendarDate.getLong(MILLI_OF_SECOND)` threw although `isSupported(MILLI_OF_SECOND)` returned `true`. `NANO_OF_SECOND` is now supported as well, so `Instant.from(CalendarDate)` and `until(CalendarDate, ChronoUnit)` work. `with(MILLI_OF_SECOND, ...)` and `with(NANO_OF_SECOND, ...)` reject out of range values, as `Instant` does.
- `CalendarDate.plus(long, ChronoUnit)` returned an `Instant` (now a `CalendarDate`), and `plus(long, CalendarDateUnit)` cast the amount to `int`, so `minus(CalendarDateDuration)` of more than about 25 days in milliseconds was wrong. `CalendarDateUnit.addTo(CalendarDate, long)` ignored the amount.
- `CalendarDate.compareTo(...)` could overflow.

#### org.ojalgo.type.context

- `NumberContext.equals(Object)` and `hashCode()` compared object identity. They now compare precision, rounding mode and scale (not the format).
- `NumberContext.isZero(BigDecimal)` treated any number smaller than a tenth of the last decimal as zero, although with the rounding modes `UP`, `CEILING` and `FLOOR` it may round to a non-zero value. With a negative scale, numbers that round to zero (such as 49 with scale -2) were reported as non-zero.

## [57.3.1] – 2026-09-21

### Fixed

#### org.ojalgo.optimisation

- `Optimisation.Result.parse(String)` handles empty solution vectors without throwing.

## [57.3.0] – 2026-09-11

### Added

#### org.ojalgo.optimisation

- New cut separators in the `IntegerSolver`: mixed integer rounding (MIR), knapsack cover, clique and implied bounds cuts, in addition to the GMI and flow cover cuts. All are on by default. `IntegerStrategy.CutType` enumerates the cut types.
- Integral-objective pruning: when all objective coefficients are integers the solver computes the GCD lattice step and uses it to widen the incumbent cutoff, pruning nodes that cannot improve by a full lattice unit.
- `ExpressionsBasedModel.findSimilar(Expression)` and `Presolvers.findSimilar(Collection, Expression)`: the similarity test behind `checkSimilarity`, without its side effects. Returns the similar constraint and the factor between the coefficients, or null.

#### org.ojalgo.concurrent

- `InterruptForwarder`: forwards `Thread.interrupt()` of the calling thread as a repeated invocation of a stop action. Useful when the calling thread is blocked inside code that cannot observe Java's interrupt flag on its own. Use it in a try-with-resources block around the blocking call.

### Changed

#### org.ojalgo.optimisation

- `IntegerStrategy.GMICutConfiguration` is replaced by `IntegerStrategy.CutConfiguration`, a single configuration shared by all cut types: the quality filters (efficacy, dynamism, density, count) and the number of separation rounds (`withIterations()`) apply to every separator, while `fractionality`, `violation` and the new `mirRelaxation` flag only affect the GMI and MIR cuts. `withTypes(CutType...)` selects which cut types to use and the order in which they are attempted within a round; with no arguments cut generation is turned off. The default order is implied bounds, clique, knapsack cover, flow cover, MIR, GMI. `IntegerStrategy` has the single accessor `getCutConfiguration()` and `ConfigurableStrategy` the single `withCutConfiguration()`.
- `UpdatableSolver.generateCutCandidates(boolean[], CutConfiguration)` replaces the `GMICutConfiguration` overload.

### Deprecated

#### org.ojalgo.optimisation

- `UpdatableSolver.generateCutCandidates(double, boolean[])`: use `generateCutCandidates(boolean[], CutConfiguration)` instead.

### Fixed

#### org.ojalgo.optimisation

- The presolver rounds bounds it derives for integer variables to the feasibility precision before taking the ceiling or floor. Exact rounding amplified sub-tolerance noise in a constraint (a cut row generated by the `IntegerSolver`, or an objective cutoff) into a wrong fixing, which could cut off the optimal solution.
- GMI/MIR cut coefficients for non-basic variables at their upper bound: the generator now queries the simplex basis state instead of using the sign of the upper bound as a proxy, which was wrong for variables with a positive upper bound at that bound.
- MIR cuts no longer drop tiny coefficients without compensating the right-hand side.
- A cut candidate similar to an existing constraint no longer tightens that constraint in place (the LP solver may have been built from it, and GMI cuts expand its slack variable from its limits); it is added beside it if tighter, or replaces it if it is a cut of the same node.
- Reduced-cost bound tightening no longer overflows when `gap / reducedCost` exceeds the `int` range.

## [57.2.0] – 2026-08-31

### Added

#### org.ojalgo.optimisation

- Flow cover cut separator for models with variable upper bound (VUB) structure. Detects `x <= M*y` constraints paired with flow-conservation equalities and generates tightened cuts that replace big-M capacities with local demands. Runs automatically at the root node before branching begins.
- GMI cut quality filters: efficacy, dynamism, density, and violation thresholds control which Gomory cuts are accepted. Cuts are ranked by efficacy and capped proportionally to problem size. Configurable via `GMICutConfiguration`.

### Changed

#### org.ojalgo.optimisation

- `NodeKey` is now (completely) immutable. The `tightenLower`/`tightenUpper` mutators have been replaced with `withTightenedLower`/`withTightenedUpper` factory methods that return new instances.
- `GMICutConfiguration` now exposes `dynanism` and `efficacy` as `NumberContext` fields, with builder methods `withDynanism()`/`withEfficacy()`. Cut count and density limits scale with problem size via `getMaxCuts(int)` and `getMaxElements(int)`, configurable through `withMaxCuts(floor, divisor, ceiling)` and `withMaxElements(floor, divisor, ceiling)`.

### Fixed

#### org.ojalgo.optimisation

- Fixed integer rounding of constraints whose coefficients share a non-integer GCD. The expression is now marked "integer" only when all coefficients are themselves integers; non-integer GCD cases still tighten bounds but no longer incorrectly flag the expression as integer. (Issue [#682](https://github.com/optimatika/ojAlgo/issues/682))
- Fixed model-solver desync in the B&B solver. Permanent bound tightenings from reduced-cost fixing and root probing now update both the model and the solver via `enforceBounds`, preventing "obviously infeasible value" warnings after solver regeneration.

## [57.1.1] – 2026-08-19

### Added

#### org.ojalgo.optimisation

- Added a general parallelism setting in `Optimisation.Options` and then removed the same thing from the `IntegerStrategy` implementations.

### Changed

#### org.ojalgo.netio

- `BasicJson` parsing is now strict — malformed input throws `IllegalArgumentException`.

#### org.ojalgo.optimisation

- The `IntegerStrategy#getWorkerPriorities` now takes an int parallelism parameter as input.

### Fixed

#### org.ojalgo.netio

- Various `BasicJson` parsing and writing bugs.

#### org.ojalgo.optimisation

- Reworked Devex pricing and the feasibility/ratio-test tolerances in the dual simplex — the default `LinearSolver` path. Models that previously returned an incorrect `OPTIMAL` now solve correctly, and most solve faster.

### Removed

#### org.ojalgo.netio

- `BasicJson.toStringList(String)` and `BasicJson.toStringListMap(String)`.

## [57.1.0] – 2026-07-29

### Added

#### org.ojalgo.netio

- New `BasicJson` — minimal JSON parser and writer with no external dependencies. Supports objects, arrays, strings, numbers, booleans, and null. Convenience methods `toStringList(String)` and `toStringListMap(String)` for common patterns.

#### org.ojalgo.optimisation

- `ExpressionsBasedModel.FileFormat.LP` — CPLEX LP format, supporting linear, quadratic, integer, and binary models. Full reader and writer for the CPLEX LP format, including quadratic objectives (`[ ... ] / 2` syntax), named constraints, bounds, generals/binaries sections, and comment stripping. 
- `ExpressionsBasedModel.FileFormat.MPS` — Now also supports writing MPS files.
- `Optimisation.ModelSubmitter` and `Optimisation.ResultPoller` functional interfaces for asynchronous remote solving.
- `Optimisation.Environment.setRemoteSolver(ModelSubmitter, ResultPoller)` to configure a remote solver.
- `ExpressionsBasedModel.submit(Sense)` — submits a model for solving asynchronously and returns a `Future<Result>`. Uses the environment's `ModelSubmitter` and `ResultPoller`; defaults to solving locally.
- `Optimisation.Environment` gained `parse(File)` and `parse(InputStream, FileFormat)` methods — model parsing now goes through the environment, so parsed models inherit the environment's solver integrations, presolvers, and configurators.
- `IntegerSolver` now performs reduced-cost fixing at every B&B node: after solving the LP relaxation and when an incumbent exists, each non-fixed integer variable's reduced gradient is compared against the incumbent gap to derive tighter bounds — variables whose reduced cost exceeds the gap are fixed, potentially pruning large subtrees.
- `IntegerSolver` gained a rounding heuristic that fires at nodes where no incumbent has been found yet. If all integer variables in the LP solution are within a quarter-unit of an integer value, the rounded candidate is validated against the original model and registered as an incumbent when feasible.

### Changed

- Removed everything related to JMH from this repository. All benchmarks remain, but are now in the ojAlgo-linear-algebra-benchmark repository.

#### org.ojalgo.optimisation

- `ExpressionsBasedModel.parse(File)` and `parse(InputStream, FileFormat)` now delegate to the default environment, so parsed models use its configuration.
- `ExpressionsBasedModel.writeTo(File)` and `writeTo(InMemoryFile)` now auto-detect the output format from the file name ending (previously always wrote EBM). Supported formats: `.ebm`, `.mps`/`.sif`/`.qps`, and `.lp`.
- `ExpressionsBasedModel.FileFormat` now recognises `.qps` as an alias for MPS.

### Removed

- Removed the `org.ojalgo.optimisation.service` package (`OptimisationService`, `ServiceIntegration`, `ServiceSolver`).
- Removed `exports org.ojalgo.optimisation.service` from `module-info.java`.
- Removed `Optimisation.ENVIRONMENT` — the default environment is now internal to `ExpressionsBasedModel`. Use the existing static convenience methods or create a separate `Optimisation.Environment`.
- Removed deprecated `Optimisation.Options.getConfigurator(Class)` and `Options.setConfigurator(Object)` — use the corresponding methods on `ExpressionsBasedModel` or `Optimisation.Environment` instead.

#### org.ojalgo.matrix

- `MatrixStore.isHermitian()` now uses element-magnitude-relative comparison instead of absolute-`ONE`-relative, avoiding false negatives on matrices with large entries.

#### org.ojalgo.optimisation

- Changed the cut generation strategy: dedicated root cut loop runs up to 10 rounds of GMI cut generation before branching begins, with tailing-off detection. Non-root cut generation is deferred until an incumbent exists.
- Reduced-cost fixing and the rounding heuristic now also run at non-root B&B nodes (previously only at the root during the probing phase).

## [57.0.0] – 2026-06-20

### Added

#### org.ojalgo.optimisation

- New ADMM (OSQP-style) QP-solver named `AlternatingDirectionSolver`. The `ConvexSolver.Configuration` now lets you specify either the `ACTIVE_SET` or `ADMM` algorithm. If you don't specify which, simple logic will select for you. The new solver also works with iterative-refinement.
- New `Optimisation.Environment` class that holds solver integrations, presolvers, variable/expression factories, and 3rd-party configurators. Use `Optimisation.newEnvironment()` to create isolated configurations and `Environment.newModel()` as the model factory.
- New `FactorKKT` – a dedicated KKT factorisation helper used by the convex solvers.
- New `RuizScaling` – Ruiz equilibration for KKT/constraint systems in the convex solver pipeline.
- New `Equilibrator` abstract class for matrix equilibration (row and column scaling). Provides utility methods for clamping scaling factors to safe bounds and is used by scaling implementations across the optimisation pipeline.
- `UpdatableSolver` gained `getDualMultiplier(int)` and `getReducedGradient(int)` for querying dual variables and reduced gradients after a solve.
- `Optimisation.Result` now carries an optional reduced gradient via `getReducedGradient()` / `withReducedGradient(Supplier)`.
- `Optimisation.Result` gained `getDualSolution()` returning `Optional<Supplier<Access1D<?>>>` (lazy dual variables), `getDualValues()` returning the dual values matched to their respective constraints, and the corresponding builder methods `withDualSolution(Supplier)` / `withDualValues(ConstraintsMetaData, Supplier)`. These replace the previous `getMultipliers()` / `getMatchedMultipliers()` / `multipliers(...)` API.
- `ExpressionsBasedModel.Simplifier`, `ExpressionAnalyser`, and `VariableAnalyser` are now `public`, enabling custom presolver-like hooks that plug into the standard presolve pipeline.
- New `ExpressionsBasedModel.getVariableValuesValidated()` – the validated/state-resolving counterpart of `getVariableValues()` (see the corresponding behaviour change below).
- New `Optimisation.Integration.prepareSolverCandidate(Result, Model)` – maps an optional kick-starter from model state to solver state, and may return `null`. Integrations whose solver ignores the kick-starter (e.g. the linear/simplex solver) return `null`, letting callers skip candidate extraction and conversion entirely.
- New `LinearSolver.Builder.equalities(double[])` and `inequalities(double[])` overloads — supply the RHS as a `double[]` and get back a `Mutate2D` to fill in the constraint body matrix.
- New `LinearSolver.Builder.lower(int...)` and `upper(int...)` convenience overloads for setting variable bounds from int arrays.
- New `LinearSolver.Configuration.equilibration(int)` — configure the number of Ruiz-style equilibration iterations for the LP solver (default 0; enable only when measured beneficial).
- New `ExpressionsBasedModel.prepare(Optimisation.Sense, Function)` — explicitly specifies the optimisation sense when preparing an `IntermediateSolver`.
- New `Optimisation.Sense.solve(ExpressionsBasedModel, Integration)` overload — solve with a specific integration.
- New `ExpressionsBasedModel.isAnyVariableDeclaredInteger()` — checks whether any variable is declared integer regardless of the model's relaxation flag.
- New `Expression.Factory` and `Variable.Factory` functional interfaces, and corresponding `ExpressionsBasedModel.newExpression(String, Expression.Factory)` / `newVariable(String, Variable.Factory)` overloads enabling custom expression/variable subtypes in combination with `Optimisation.Environment`.
- New `ExpressionsBasedModel.setConfigurator(Object)` static convenience and `getConfigurator(T defaultValue)` instance method — delegates to the model's `Optimisation.Environment`.
- `ConstraintType` gained `isLower()` and `isUpper()` convenience methods that return `true` for any type that implies a lower or upper bound (including `RANGE` and `EQUALITY`).
- `IntegerSolver` gained an explicit root-processing phase that solves the root LP and runs a one-shot strong-branching probe pass over the most fractional candidates — seeding pseudo-costs, accepting integer-feasible probe results as free incumbents, detecting root infeasibility from both-direction-infeasible probes, and applying single-direction probing fixings (permanent root-bound tightening when one branch direction is LP-infeasible).
- `IntegerSolver` now computes a true global dual bound across the deferred frontier, terminating early when the gap to the incumbent closes within tolerance. A `logProgress` override prints incumbent / dual bound / absolute gap / relative gap / tolerance for diagnostics.
- New `IntermediateSolver.update(int globalIndex, double lowerBound, double upperBound)` — primitive bound-update path that doesn't allocate `Variable`/`BigDecimal` and doesn't mutate the model. Suitable for transient probe-style updates.
- `NodeKey` gained primitive bound accessors `getLower(int)` / `getUpper(int)` (returning `double` with `Integer.MIN/MAX_VALUE` sentinels mapped to `±Infinity`) and tightening mutators `tightenLower(int, int)` / `tightenUpper(int, int)` for root-phase probing fixings.
- New `MultiviewSet.PrioritisedView.peek()` — O(1) head accessor on the shared concurrent set of deferred nodes.
- `ModelStrategy.observeBranch(int idx, boolean upper, double observation)` — hook for injecting strong-branching probe observations into pseudo-cost arrays. Default no-op; `DefaultStrategy` overrides to feed its pseudo-cost weight arrays.

#### org.ojalgo.matrix

- Decompositions now expose individual factors via `MatrixDecomposition.Solver.getFactors()` returning `List<InvertibleFactor>`. Each factor supports `ftran`/`btran` operations on both `PhysicalStore` and raw `double[]` arrays.
- New `MatrixDecomposition.Factor` interface extending `InvertibleFactor` with a `get()` method returning the factor as a `MatrixStore`.
- Enhanced `SubstituteBackwards`/`SubstituteForwards` with `double[]`-based overloads, supporting the new ftran/btran machinery.
- `InvertibleFactor` gained static helper methods for composing ftran/btran over a list of factors.
- `SparseArray.firstIndex()` and `lastIndex()` are now `public` (were package-private) and return `int` instead of `long`, with safe `-1` on empty arrays.
- New `SortAll.sort(long[], int[])` overload for co-sorting a long key array with an int permutation array.

#### org.ojalgo.array

- New `DensityTrackingArray` – a primitive 1D array that incrementally tracks its nonzero pattern alongside stored values. Backed by a plain `double[]` with an explicit index list for nonzero positions, enabling efficient traversal of sparse arrays while supporting direct indexed access to all elements.

#### org.ojalgo.scalar

- `RationalNumber.getNumerator()` and `RationalNumber.getDenominator()` are now `public`.

#### org.ojalgo.type

- `NumberContext.common(BigDecimal, BigDecimal)` returns the average of two values if they are within precision, otherwise null.

### Changed

#### org.ojalgo.matrix

- `SparseLU` gained `factor(R064CSC, int[])` for direct CSC-based basis factorisation, and `updateColumn(int, R064CSC, int)` for Forrest-Tomlin updates reading from CSC — avoiding intermediate wrapping.
- `R064CSC` and `R064CSR` gained `axpy(int, double, double[])`, `dot(int, double[])`, `supplyTo(int, double[])`, and `capacity(int)` methods for efficient row/column-level operations on compressed sparse matrices.

#### org.ojalgo.optimisation

- Internal refactoring of variable-bound shifting in the simplex solvers — shifting logic moved from `SimplexSolver` into `SimplexStore` for better separation of concerns.
- Revised simplex (`RevisedStore`) now freezes the constraint matrix to `R064CSC` before solving and uses raw `double[]` working vectors, reducing per-iteration allocations and improving cache locality.
- Cleaned up the `UpdatableSolver` interface – everything is now optional with default implementations that do nothing. All the quirky stuff is moved to `ExpressionsBasedModel.EntityMap`. This also required `ConstraintsMetaData` to be somewhat refactored, and the `Optimisation.ConstraintType` enum gained another instance `RANGE`.
- Deprecated `Constraint.isLowerConstraint()` and `Constraint.isUpperConstraint()` in favour of `Constraint.getConstraintType()`.
- `LinearSolver.Builder` now auto-selects the revised simplex (dual) solver when variable bounds have been modified; tableau (primal) remains the default for unchanged bounds. An explicit `Configuration` override still takes precedence.
- `RevisedStore.updateDualsAndReducedCosts()` rewritten to use raw `double[]` operations instead of logical row/column selections, avoiding intermediate store allocations on every simplex iteration.
- `Optimisation.Environment.addPresolver()`/`removePresolver()` now accept any `Simplifier` — not just `Presolver` — so `VariableAnalyser` and `ExpressionAnalyser` instances can be registered.
- The default presolver set (via `resetPresolvers()`) now includes `LINEAR_OBJECTIVE` and `UNREFERENCED`; execution order among built-in presolvers has been revised.
- `VariableAnalyser.simplify()` return type changed from `boolean` to `void`.
- The presolve pipeline in `ExpressionsBasedModel` now dispatches through the unified `Simplifier` hierarchy, making the scan/simplify phase extensible.
- Simplex solver pivot selection now uses Harris ratio test in both passes for more numerically stable entry and exit pivots. Tuned thresholds for dense versus sparse tableau selection based on problem dimensions to optimise performance.
- `ExpressionsBasedModel.getVariableValues()` is now a cheap value extraction only — it no longer validates the solution or evaluates the objective, and returns `State.UNEXPLORED`. Code that relied on the previous validated state/objective behaviour must call the new `getVariableValuesValidated()` instead. (The `getVariableValues(NumberContext)` overload is unchanged.)
- `IntermediateSolver.solve(...)` now has a cold/warm split: only the first solve (or one after `reset()`) runs presolve and the degenerate-model pre-checks; warm re-solves after bound-only `update(...)` calls skip those scans and, for solvers that ignore the kick-starter (LP/simplex), skip candidate extraction and `toSolverState` conversion as well. Substantially reduces per-solve overhead for `IntegerSolver` and other solvers that iteratively modify a model.
- The `LinearSolver` model integration maps the solver reduced gradient back to model space, reconstructing reduced costs for variables eliminated by presolve so they are available in the returned `Optimisation.Result`.
- `IntegerSolver.markInteger`'s LP cutoff is now always strict-improvement (one ULP), decoupled from the gap tolerance. The previous gap-sized cutoff could excise the true optimum when an early incumbent landed within gap of it (e.g. `P20140819#testOriginalFullModel`). Gap tolerance still drives `isOptimalityProven` (early termination) and per-node `isGoodEnough` (bound-fathoming) — only the LP-cutoff portion is decoupled.
- `IntegerStrategy.DEFAULT` gap tolerance loosened from `NumberContext.of(7, 8)` (relative 1e-6) to `NumberContext.of(5, 7)` (relative 1e-4), to the industry-standard 1e-4.
- `DefaultStrategy.scoreBranch` — pre-reliability, pre-incumbent fallback switched from "closest-to-integer" (`max(distanceDown, distanceUp)`) to the gradient-seeded `productScore`, letting initial pseudo-cost seeds drive branching immediately.

#### org.ojalgo.matrix

- Major internal refactoring of dense decompositions (`DenseCholesky`, `DenseLDL`, `DenseLU`, `DenseQR`, `DenseSingularValue`) and raw decompositions (`RawLU`, `RawQR`, `RawSingularValue`, `RawCholesky`, `RawEigenvalue`) to use the new factor-based infrastructure.
- Reworked `SparseLU` and `SparseQDLDL` for better performance and integration with `InvertibleFactor`.
- `SparseLU` is now `public` and accepts `ColumnsSupplier.Selection` directly via a new `factor(...)` method that applies column-ordering by sparsity/last-index to reduce fill-in.
- `SparseLU` gained `countFactorNonzeros()` and `countEtaNonzeros()` for querying L+U and eta-chain fill-in, used by the adaptive refactorisation heuristic in `SparseDecomposition`.
- New internal `DualSparse` structure for sparse matrix decompositions that maintains both row and column views alongside a diagonal, supporting efficient dual representation of sparse matrices.
- Refined `MatrixStore.norm()` calculation and added a default `normalised()` implementation.
- `LogicalStore` hierarchy: exposed `base` as a direct field reference, eliminating the `base()` accessor call in all logical store subclasses. `ColumnsStore` and `RowsStore` simplified — removed the negative-index-as-zeros indirection and opened for subclassing.
- New `ColumnsSupplier.Selection` and `RowsSupplier.Selection` inner classes that provide sparse-aware `supplyTo` and `sliceColumn`/`sliceRow` implementations, iterating nonzeros directly instead of delegating to the dense path.
- `ColumnsSupplier.selectColumns(int[])` and `RowsSupplier.selectRows(int[])` now pre-size the internal `ArrayList` and avoid a redundant copy.
- The `columns(int...)` and `rows(int...)` contracts no longer accept negative indices as "zero row/column" placeholders.

#### org.ojalgo.optimisation

- Replaced `DecomposedInverse` with two focused implementations: `SparseDecomposition` (backed by `SparseLU`, the new default) and `DenseDecomposition` (dense LU baseline).
- `SparseDecomposition` now uses adaptive refactorisation: a fill-in heuristic triggers re-decomposition when eta-chain nonzeros exceed 1.5× the L+U factor nonzeros, with a dimension-scaled ceiling (`min(300, 3×m)`) as a safety net. Benchmarked across the Netlib LP suite — ~9% median speedup with 50 models improved vs 22 regressed.
- `RevisedStore` now uses `SparseDecomposition` by default; dimension parameter removed from the factory method.
- `UpdatableSolver.getEntityMap()` now returns `Optional<EntityMap>` instead of a bare `EntityMap`. Callers that accessed the entity map directly must now unwrap the `Optional`.
- `ExpressionsBasedModel.isAnyVariableInteger()` now returns `false` when the model is relaxed; use `isAnyVariableDeclaredInteger()` to check the underlying declaration regardless of relaxation.
- `IntermediateSolver.getIntegration()` changed from package-private to `protected`, allowing subclasses outside the package to access the integration.
- `Expression.compensate(...)` now stashes the model's objective adjustment when applied to the (aggregated) objective expression — any solver integration that compensates the objective automatically gets the additive offset (objective constant + presolve-fixed-variable contribution) captured for `toModelState` to apply.
- `SimplexSolver` / `SimplexTableauSolver` now un-scale the reported objective value and reduced costs by the objective adjustment factor in result extraction, mirroring the existing un-scaling of dual multipliers — the LP solver reports value and reduced costs in model space directly.
- `IntegerSolver` no longer holds an internal `MultiaryFunction` reference and no longer re-evaluates the objective per node; it reads the (now correct) model-space `getValue()` from each node result directly. Dead `buildResult()` / `extractSolution()` removed from both `IntegerSolver` and `GomorySolver`.
- `ModelStrategy.initialise(...)` signature simplified to no-arg — pseudo-cost seeds are now derived once at construction from each integer variable's own `Variable.getContributionWeight()` rather than from a per-solve objective-gradient evaluation. **Breaking change** for any third-party `ModelStrategy` subclass.

#### org.ojalgo.algebra

- Renamed `NormedVectorSpace.signum()` to `normalised()`. The old `signum()` method is retained as a deprecated default that delegates to `normalised()`.

#### org.ojalgo.scalar

- The new method `Scalar.normalised()` behaves the way `Scalar.signum()` used to (always return something of unit magnitude) and `Scalar.signum()` has been redefined to mirror `Math.signum`/`BigDecimal.signum` behaviour (returns zero for zero input). All scalar implementations updated accordingly. 

### Fixed

#### org.ojalgo.scalar

- Improved `Quadruple` division accuracy.

#### org.ojalgo.matrix

- Fixed potential infinite loop in `DenseSingularValue` decomposition for certain matrices (GitHub [#661](https://github.com/optimatika/ojAlgo/issues/661)). The problem was with the Householder transformations and related to the `Scalar.normalised()`/`Scalar.signum()` changes.

#### org.ojalgo.optimisation

- Branch-and-bound performance: the `NodeKey` variable sign-change condition was over-broad and fired on routine branching (e.g. every binary `[0,1]→[0,0]` branch), forcing a spurious full solver rebuild at each node instead of a cheap in-place bound update — badly degrading MIP solve times. It now triggers only on an actual column-negation sign change.
- Quadratic models are no longer subjected to in-place bound updates during branch-and-bound (which could yield wrong results); `NodeSolver` forces a solver reset for any model with a quadratic expression.
- Simplex warm-start: after bound-only changes from an optimal basis the solver restarts from the retained basis via dual iterations instead of re-solving cold.
- Fixed objective function value sign: when the solver's internal sense (e.g. always-minimise) differs from the model's optimisation sense, the returned objective value is now correctly negated.
- `LinearSolver.Builder.newSimplexStore` no longer materialises the inequality matrix as a dense `m × n` block — it walks the source rows sparsely (matching `newSimplexTableau`). Avoids `ArrayIndexOutOfBoundsException` / `NegativeArraySizeException` on large LPs where `m · n` overflows a Java `int`.
- The LP solver's reported objective value now includes the model's objective constant and the contribution of any presolve-fixed variables — both were previously silently dropped because the compensated objective only fed its linear part into the simplex's cost row. Affected direct consumers of `Optimisation.Result.getValue()` reached through `IntermediateSolver` / `Integration.toModelState(...)`; `IntegerSolver` and `ExpressionsBasedModel.optimise(...)` were unaffected because they re-evaluate the objective from the solution vector.
- The LP solver's reported objective value and reduced costs are now in model space — the objective's numerical-stability adjustment factor is un-applied at the result boundary (matching the existing dual-multiplier un-scaling). Fixes a latent `value ÷ 10^exponent` bug for any direct reader through `LinearSolver.ModelIntegration` whenever the objective coefficients triggered scaling.

### Deprecated

#### org.ojalgo.algebra

- `NormedVectorSpace.signum()` – use `normalised()` instead (scheduled for removal in v57).

#### org.ojalgo.optimisation

- `ExpressionsBasedModel.prepare(Function)` – use `prepare(Optimisation.Sense, Function)` instead.
- `Optimisation.Options.getConfigurator(Class)` and `Options.setConfigurator(Object)` – use `Optimisation.Environment.getConfigurator(Class)` or the model-level `getConfigurator(T)` / `setConfigurator(Object)` instead.
- `Optimisation.Integration.extractSolverState(M)` – no longer needed internally.
- `Optimisation.Result.getMultipliers()`, `getMatchedMultipliers()`, and the `multipliers(...)` builder overloads – use `getDualSolution()`, `getDualValues()`, `withDualSolution(Supplier)` and `withDualValues(ConstraintsMetaData, Supplier)` instead (scheduled for removal in v57).

### Removed

#### org.ojalgo.optimisation

- The `ExpressionsBasedModel.Validator` class and all functionality related to it. The related `ExpressionsBasedModel.setKnownSolution(...)` methods are also removed.
- `Presolvers.checkFeasibility(...)` static method removed.
- `UpdatableSolver.integers(ExpressionsBasedModel)` and `isMapped()` – removed (integer mask logic moved to `ExpressionsBasedModel.EntityMap`).

## [56.2.1] – 2026-01-21

### Added

#### org.ojalgo.matrix

- More `R064CSC` utilities

### Fixed

#### org.ojalgo.optimisation

- Fixed problems related to simplifying/reducing `ExpressionsBasedModel`

## [56.2.0] – 2025-12-29

### Added

#### org.ojalgo.optimisation

- New `NullSpaceProjection` and `NullSpaceASS`. Modifies the problem before delegating to `ActiveSetSolver` eliminating equality constraints and reducing the number of variables. Turn this feature On/Off via configuration option. The default is to use when the model is big enough and has a significant number equality constraints.
- It is now possible to call `maximise` or `minimise` on an `ExpressionsBasedModel` explicitly supplying the `ExpressionsBasedModel.Integration` to use, bypassing the usual mechanism of selecting this.
- `Expression`:s can now be constructed as weigthed combinations of other `Expression`:s –  a convenience that makes it much easier to reason about how to construct certain types of constraints.

#### org.ojalgo.matrix

- New sparse LDL decomposition `SparseQDLDL` based on the QDLDL factorisation algorithm. Designed for large, sparse KKT systems in convex QP problems and integrates with the existing decomposition/factorisation APIs.
- Improved sparse matrix infrastructure for R064 CSC/CSR stores and suppliers: `RowsSupplier`/`ColumnsSupplier` and compressed sparse stores (`R064CSC`, `R064CSR`, `CompressedSparseR064`) now support more efficient copying and supply operations, reducing temporary allocations when building or transforming sparse matrices.
- An approximate Minimum Degree calculator – not quite a full/correct Approximate Minimum Degree (AMD) implementation, but a simplified alternative.
- `InvertibleFactor` now has overloaded `ftran` and `btran` methods with `double[]` arguments.

### Changed

#### org.ojalgo.optimisation

- Modified to the active set in `ActiveSetSolver` is initialised – limited the number of inequalities that can be set to active.
- Tweaked the default behaviour when selecting either dense/direct or sparse/iterative `ActiveSetSolver` – now favour dense/direct in some cases. Previously always chose sparse/iterative.
- Slight change to how quadratic expressions are scaled when constructing solver data. Now primarily uses the diagonal elements.
- The default preconditioner is now SSORPreconditioner rather than JacobiPreconditioner.

#### org.ojalgo.structure

- `ColumnView` and `RowView` now support directly updating the underlying data structure (provided that implements `Mutate2D`).

### Fixed

#### org.ojalgo.optimisation

- Presolving could in some cases incorrectly mark models, with quadratic constraints, as `INFEASIBLE`.

## [56.1.1] – 2025-11-09

### Added

#### org.ojalgo.matrix

- There was a problem with `GenericStore` factory type declarations and usage.

## [56.1.0] – 2025-11-04

### Added

#### org.ojalgo.data

- New package `org.ojalgo.data.proximity` containing various distance and similarity calculation utilities.
- Spectral clustering: New `org.ojalgo.data.cluster.SpectralClusterer` implementing spectral clustering over feature vectors using an RBF similarity graph and the symmetric normalised Laplacian. Factory methods `FeatureBasedClusterer.newSpectral(int)` and `newSpectral(DistanceMeasure,int)` create instances.
- Clustering facade: New `org.ojalgo.data.cluster.FeatureBasedClusterer` facade with factory methods `newAutomatic(...)`, `newGreedy(...)`, `newKMeans(...)`, and `newSpectral(...)`. Adds a generic `cluster(Collection<T>, Function<T,float[]>)` that maps arbitrary items to feature vectors and returns clusters as `List<Map<T,float[]>>`.
- Automatic k selection: New `org.ojalgo.data.cluster.AutomaticClusterer` that derives thresholds from distance statistics to seed/refine clusters (k-means under the hood).

#### org.ojalgo.matrix

- Spectral decomposition: New `Eigenvalue.Spectral` interface (extends both `Eigenvalue` and `SingularValue`) and factory convenience `Eigenvalue.Factory#makeSpectral(int)` for normal (in particular symmetric / Hermitian) matrices, exposing a decomposition that can simultaneously be treated as an eigenvalue- and singular value decomposition. Includes `isSPD()` convenience check.
- Static utility helpers: `Eigenvalue.reconstruct(Eigenvalue)` plus `SingularValue.invert(...)`, `SingularValue.solve(...)` and `SingularValue.reconstruct(...)` centralise pseudoinverse / solve / reconstruction logic.
- New Quasi-Minimal Residual (QMR) and Minimal Residual (MINRES) iterative solvers for general nonsymmetric square and symmetric (possibly indefinite) systems, respectively. Contributed by @Programmer-Magnus.

#### org.ojalgo.concurrent

- Execute tasks in a separate JVM: New `ExternalProcessExecutor` that runs a specified static method or a `Serializable` `Callable`/`Runnable` in an external OS process (child JVM). Provides:
  - Hard cancellation and timeouts by killing the process tree.
  - Binary IPC framing (MAGIC/VER/LEN/CRC32) over stdio; stdout is reserved for protocol frames to avoid corruption.
  - Configurable `ProcessOptions` builder for heap (`-Xmx`), additional JVM args, system properties, environment and classpath; sensible defaults for Maven/Gradle test/main classpaths.
  - Overloads `execute(...)`, `call(...)` and `run(...)` to target methods by `Method`, `MethodDescriptor` or owner/name/parameter types.
  - `ProcessWorker` main class (child entrypoint) and `MethodDescriptor` describing methods across classloaders.
  - `ProcessAwareThread` and a process-aware thread factory used so that interrupting an owner thread forcibly tears down the child process.
- Thread factory: `DaemonPoolExecutor` exposes an internal process-aware `ThreadFactory` used by `ExternalProcessExecutor` (threads remain daemon and identifiably named).
- Collections: `MultiviewSet` adds `isAnyContents()` to cheaply detect if any backed priority view still has queued entries.

#### org.ojalgo.machine

- `JavaType` adds utilities `box(Class<?>)`, `unbox(Class<?>)` and `resolveType(String)` to convert between primitive/wrapper types and resolve primitive/array/class names (e.g. "int[]", "java.lang.String[]").

### Changed

#### org.ojalgo.data

- Clustering refactor and performance: Greedy and k-means implementations now share a `PointDistanceCache` for pairwise distances, centroids and initialisation (median-based threshold), reducing allocations and repeated work.
- Consistent factories and results: All clusterers are constructed via `FeatureBasedClusterer` factories and return clusters sorted by decreasing size when using the generic `cluster(Collection<T>, Function<T,float[]>)` entry point.

- Singular Value Decomposition API now uses standard nomenclature: diagonal singular value matrix accessor changed from `getD()` to `getS()` (see Deprecated). Internal implementations (`DenseSingularValue`, `RawSingularValue`) updated accordingly.
- Performance & allocation improvements in `DenseSingularValue` and `RawSingularValue`: direct use of internal singular value array (`s[]`), deferred/cached construction of `S` and inverse, centralised solve/invert logic reducing temporary object creation.
- Eigenvalue decomposition updated to integrate spectral variant; minor javadoc clarifications and shared reconstruction via new static helper.

#### org.ojalgo.matrix

- Internal refactoring in the `org.ojalgo.matrix.task.iterative` package. If you only used what was there these changes shouldn't affect you, but if you have implemented your own solver it does.
- Created a `Preconditioner` interface. Factored the Jacobi-presolving out of the `ConjugateGradientSolver` and additionally implemented a Symmetric Successive Over-Relaxation (SSOR) preconditioner.

#### org.ojalgo.optimisation

- In `ConvexSolver`, the iterative Schur complement solver used in the active set solver, is now configurable (which implementation to use). Use either the `ConjugateGradientSolver` or `QMRSolver`, or some other implementation.

#### org.ojalgo.concurrent

- `DivideAndConquer` now uses a safer split-and-join: sibling tasks are cancelled on failure, causes are propagated, and interruption is preserved. The configurable `Divider` exposes `threshold(int)` and `parallelism(IntSupplier)`; `ProcessingService#newDivider()` returns one bound to its executor. Default worker count uses `OjAlgoUtils.ENVIRONMENT.threads` consistently.
- `ProcessingService#divider()` is deprecated in favour of `newDivider()` (same behaviour); javadocs clarified for `compute/map/reduce*` regarding uniqueness and hashing requirements.
- `DaemonPoolExecutor`: internal addition of a process-aware thread factory; no behavioural change for existing `new*ThreadPool(...)` helpers.

### Deprecated

#### org.ojalgo.matrix

- `SingularValue#getD()` deprecated; use `getS()` instead. (Existing code continues to work; plan to remove in a future major release.)

#### org.ojalgo.concurrent

- `ProcessingService#divider()` in favour of `newDivider()`.

### Fixed

#### org.ojalgo.optimisation

- Fixed a couple of presolve issues with `ExpressionsBasedModel` and quadratic constraints.

## [56.0.0] – 2025-08-23

ojAlgo is now modularised into a JPMS named module, and that module is named "ojalgo".

### Added

#### org.ojalgo.matrix

- New `MatrixStore`:s compressed sparse column (CSC) and compressed sparse row (CSR) implementations named `R064CSC` and `R064CSR` (primitive double, R064, only). Any/all of the previously existing sparse `MatrixStore` implementations `SparseStore`, `RowsSupplier` and `ColumnsSupplier` can be converted to either of these new formats.
- New utility `Eigenvalue#sort` function that allow to sort eigenvalue-vector pairs in descending order. This existed before as a private method, and was used internally. Now it's publicly available.
- New sparse `LU` decomposition, and it's updatable using Forrest-Tomlin.

#### org.ojalgo.optimisation

- Many improvements to the LP solvers (improved basis representation, candidate selection and more).

#### org.ojalgo.scalar

- The `Scalar` interface now defines a `isZero()` method.

### Changed

#### org.ojalgo.array

- Refactored the constructors, factories and builders for the classes in the `org.ojalgo.array` package. Most things should work as before, but the generics signature of `DenseArray.Factory` has changed. Instead of `DenseArray.Factory<ELEMENT_TYPE>` it is now `DenseArray.Factory<ELEMENT_TYPE, ARRAY_TYPE>`. What you may want to do is to instead use a subclass like `PrimitiveArray.Factory`.
- `SparseArray` is now restricted to `Integer#MAX_VALUE` size (used to be `Long#MAX_VALUE`). There's been a number of changes internally in order to make it perform better at smaller sizes. Also added a bunch of new stuff to support sparse functionality in the `org.ojalgo.matrix` package.

#### org.ojalgo.machine

- The (use of the) system property "shut.up.ojAlgo" is removed. Users will no longer be warned about missing hardware profiles. Instead this is now always estimated. The previous fallback estimation logic has been improved and promoted to be the primary solution. Users are still encouraged to provide hardware profile instances, but these are now instead used as test cases for the estimation logic.

#### org.ojalgo.matrix

- For symmetric matrices the Eigenvalue decompositions are now always sorted. Previously it varied depending which implementation the factory returned. You should still always check `Eigenvalue.isOrdered()` – that a general rule for both eigenvalue and singular value decompositions.
- Cleaned up the `InvertibleFactor` interface. The 2-arg ftran/btran alternatives are removed.

#### org.ojalgo.optimisation

- Tweaking to various parts of `ConvexSolver`.
- Multiple changes and improvements to the `LinearSolver`. The number of industry standard netlib models that are solved (fast enough to be included) as junit tests cases increased from 45 to 78. The `LinearSolver` has actually been improved over several versions, but with this version we see a lot of that coming together and working quite well.
- Reworked the `IntegerSolver`'s branching strategy. It now uses pseudo-costs when selecting which variable to branch on. The `NodeKey` now keeps track of the branch depth. This is used to implement proper depth-first and breadth-first strategies for the deferred nodes. The default set of node worker priorities have changed.
- Reworked the `IntegerSolver`'s cut generation strategy. 

#### org.ojalgo.type

- The relative error (epsilon) derived from the precision of a `NumberContext` was previously never smaller than the machine epsilon. Now it can be arbitrarily small depending only on the specified precision.

### Fixed

#### org.ojalgo.matrix

- Matrix multiplication performance using `RawStore` has been much improved.

#### org.ojalgo.optimisation

- When using extended precision with the `ConvexSolver` there was a problem when extracting the solution. The solver uses `Quadruple` and in the end the solution is using `BigDecimal` but at an intermediate step it was converted to `double`.

## [55.2.0] – 2025-04-25

### Added

#### org.ojalgo.optimisation

- The EBM file format parser now handles comments and empty lines in the model file (contributed by Magnus Jansson).

### Changed

#### org.ojalgo.array

- Better implementation of `SparseArray.supplyTo(Mutate1D)`.

#### org.ojalgo.matrix

- General `refactoring` in the decomposition package. Shouldn't be any api-breaking changes.
- New interface `MatrixDecomposition.Updatable` for partial updates to decompositions. The existing LU decomposition implementations implement this.

#### org.ojalgo.optimisation

- Refactoring to SimplexSolver (significant performance improvements with larger/sparse instances).
- Improved feasibility check for the QP `ActiveSetSolver`.
- Removed some unnecessary work, and garbage creation, when validation models (contributed by @hallstromsamuel)

#### org.ojalgo.scalar

- When creating rotation `Quaternion`s the angle is now halved in the factory method as is standard elsewhere. (contributed by @twistedtwin)

## [55.1.2] – 2025-02-08

### Changed

#### org.ojalgo.matrix

- Minor change to progress logging of the `ConjugateGradientSolver` – sparse iterative equation system solver.

#### org.ojalgo.optimisation

- Minor change to method signature of `ExpressionsBasedModel#addIntegration`.

## [55.1.1] – 2025-01-18

### Added

#### org.ojalgo.type

- The `ForgetfulMap` gained support for disposer-hooks – code that is called when objects are invalidated and removed from the cache. Also streamlined the code to create single `ForgetfulMap.ValueCache<T>` instances.

### Changed

#### org.ojalgo.optimisation

- The configuration option `options.experimental` is no longer used to switch between the old/classic and new/dual simplex solvers. Instead there are specific configurations for this – `options.linear().dual()` and `options.linear().primal()`. If you don't specify which to use, there is internal logic that switches implementation based on problem size.
- Various internal refactoring and numerical tuning to the LP solvers.

#### org.ojalgo.type

- `NumberContext` now explicitly exposes the relative and absolute errors as `getRelativeError()` and `getAbsoluteError()`, and the `epsilon()` method has been redefined to return the maximum of those two values.

## [55.1.0] – 2024-12-22

### Added

#### org.ojalgo.data

- New package `org.ojalgo.data.cluster` with k-means and greedy clustering algorithms implemented, as well as generalisations, specialisations and combinations of those.
- `DataProcessors` now has a method `Transformation2D newRowsTransformer(Function<SampleSet, UnaryFunction>)` to complement the existing `newColumnsTransformer`.

#### org.ojalgo.random

- `SampleSet` gained a couple of methods; `getMidrange()` and `getRange()`.

### Changed

#### org.ojalgo.array

- Sorting is no longer parallel/multi-threaded. The previous implementations made use of the common `ForkJoinPool`.

#### org.ojalgo.data

- Creating a JMX bean (to monitor throughput) with `BatchNode` is now optional, and the default is to not create them. (Used to always create them.)

#### org.ojalgo.netio

- The `FromFileReader` and `ToFileWriter` interfaces and their implementations used to extend and delegate to code in the `org.ojalgo.type.function` package. Much of what was in that package has been moved to and merged with stuff in the `org.ojalgo.netio` package.
- The `FromFileReader.Builder` and `ToFileWriter.Builder` builders now use generic "file" types. They used to be implemented in terms of Java's `File`, but can now be anything like `Path` or ojAlgo's own `SegmentedFile`, `ShardedFile` or `InMemoryFile`.
- The `DataInterpreter` gained some additional standard interpreters, as well as utilities to convert back and forth between `byte[]`.

#### org.ojalgo.random

- `SamleSet` no longer makes use of parallel/multi-threaded sorting – to avoid making use of the common `ForkJoinPool`.

#### org.ojalgo.type

- The `AutoSupplier` and `AutoConsumer` interfaces are removed. They used to provide abstract/generalised functionality for `FromFileReader` and `ToFileWriter` in the `org.ojalgo.netio` package. All features and functionality still exists, but in terms of the more specific/concrete `FromFileReader` and `ToFileWriter`. If you directly referenced any of the various utility methods in `AutoSupplier` or `AutoConsumer` they're now gone. They primarily existed so that `FromFileReader` and `ToFileWriter` could access them (from another package). The features and functionality they provided are now available through other classes in the `org.ojalgo.netio` package – like `FromFileReader.Builder` and `ToFileWriter.Builder`.

### Fixed

#### org.ojalgo.data

- In `DataProcessors`, the `CENTER_AND_SCALE` transformation didn't do exactly what the documentation said it. That's been fixed.

## [55.0.2] – 2024-11-30

### Added

#### org.ojalgo.type

- A new cache implementation named `ForgetfulMap`. To save you adding a dependency on Caffeine or similar.

#### org.ojalgo.optimisation

- There is a new `OptimisationService` implementation with which you can queue up optimisation problems to have them solved. This service is (will be) configurable regarding how many problems to work on concurrently, and how many threads the solvers can use, among other things.

### Changed

#### org.ojalgo.optimisation

- Refactoring and additions to what's in the `org.ojalgo.optimisation.service` package. They're breaking changes, but most likely no one outside Optimatika used this.
- Minor internal changes to how the `IntegerSolver` works. There's now an equal number of worker threads per worker-strategy.

### Deprecated

#### org.ojalgo.type

- `TypeCache` is replaced by `ForgetfulMap.ValueCache`.

## [55.0.1] – 2024-11-17

### Added

#### org.ojalgo.concurrent

- Addition to `ProcessingService` that simplify concurrently taking items from a `BlockingQueue`.

### Changed

#### org.ojalgo.random

- Refactored `SampleSet` (internally) to be better aligned with recent changes to other parts of the library. Primarily the class now uses `int` indices for all internal calculations. Also added a new `java.util.stream.Collector` implementation to simplify `SampleSet` creation from streams.

### Fixed

#### org.ojalgo.optimisation

- Fixed a bug where the (new) LP solver would fail to recognise unbounded problems and instead return a solution with infinite values, stating it to be optimal.

## [55.0.0] – 2024-09-28

### Changed

#### org.ojalgo.function

- The `BigDecimal` valued constants in `BigMath` for `E`, `PI` and `GOLDEN_RATIO` are redefined with more decimal digits.
- The `BigDecimal` valued functions in `BigMath` for `EXP`, `LOG`, `SIN` and `COS` are re-implemented to produce much more accurate results.

#### org.ojalgo.optimisation

- The new LP solver implementation is now the default alternative.
- The two classes `LinearSolver.GeneralBuilder` and `LinearSolver.StandardBuilder` are replaced by a common `LinearSolver.Builder`. Consequently the methods `newGeneralBuilder()` and `newStandardBuilder()` are deprecated and replaced by `newBuilder()`.

#### org.ojalgo.scalar

- Re-implemented `Quadruple`'s divide method using primitives only. (Previously delegated to `BigDecimal`.)

### Deprecated

#### org.ojalgo.data

- Everything related to downloading (historical) financial data is deprecated. It's already broken (due to changes in the external "service") and we're not going to try fix it.

### Removed

#### org.ojalgo.structure

- Removed a bunch of stuff that was deprecated in `Factory1D`, `Factory2D` and `FactoryAnyD` and/or some of their sub-interfaces.

## [54.0.0] – 2024-06-06

### Added

#### org.ojalgo.matrix

- Added the ability to specify the initial capacity of a `SparseStore`.
- More efficient sparse to sparse copying of `SparseStore`.
- New option to use a builder when initially setting the elements of a `SparseStore`.
- When setting (or adding to) elements there used to be internally synchronized code. This is no longer the case. Instead you must call `synchronised()` to get a synchronized mutating wrapper. This should make common single threaded usage faster.
- The sparse `BasicMatrix` builder no longer has all the methods that really only made sense with dense implementation. The sparse builder implementation changed to use that new `SparseStore.Builder`.

#### org.ojalgo.structure

- There are new methods fillCompatile(...), modifyCompatile(...) and onCompatile(...) to complement the already existing "fill", "modify" and "on" methods. (Inspired by MATLAB's concept of compatible array sizes for binary operation.)
- The factory interfaces got methods to construct instances of compatible sizes/shapes. (Inspired by MATLAB's concept of compatible array sizes for binary operation.)

### Changed

#### org.ojalgo.matrix

- The classes `Primitive64Store` and `Primitive32Store` have been renamed `R064Store` and `R032Store`. These classes are central to ojAlgo. The effect of this name change is widespread. ojAlgo has been transitioning to a new naming scheme over several versions now, but these classes were left untouched so far (in part because of how central they are). Now, to complete the work, it's done! No deprecations, just did it. Apart from the name change the classes are identical.
- Vector space method like "add" and "subtract" have been redefined to no longer throw exceptions if dimensions are not equal, but instead broadcast/repeat rows or columns. (Inspired by MATLAB's concept of compatible array sizes for binary operation.)

#### org.ojalgo.optimisation

- Refactored, cleaned up deprecations, primarily regarding how model variables are created. They can no longer be instantiated independent of a model. You have to first create the model, and then use that as the variable (and expression) factory.

#### org.ojalgo.structure

- Refactored the builder/factory interfaces to better support creating immutable 1D, 2D or AnyD structures. This has implications for most ojAlgo data structures. There are deprecations in all factory classes, but everything that worked before still works (I believe).
- The `Structure*D`, `Access*D`, `Mutate*D` and `Factory*D` interfaces have all been refactored to make working with `int` indices (rather than `long`) the primary alternative. Huge data structures, that require `long` indices, are still suported – no change in this regard. In reality only a few implementations actually allowed to create such large instances. This change is to allow all the other classes to be implemented using `int` and not bother (so much) with `long`. Users of the various classes in ojAlgo should see no difference. If you have created your own classes implementing some of ojAlgo's interfaces you will probably need to implement some additional methods in those classes.

## [53.3.0] – 2024-02-04

### Added

#### org.ojalgo.function

- Some additional `PowerOf2` utilities.

#### org.ojalgo.netio

- `SegmentedFile` that divides a large file in segments to enable reading those in parallel using memory mapped files (memory mapped file segments).

#### org.ojalgo.type

- New primitive number parsers in `NumberDefinition`. They take `CharSequence` rather than `String` as input and do not create any intermediate objects. In particular the `parseDouble` performs much better than its counterpart in `Double`. The downside is that it only handles a limited plain format.

### Changed

#### org.ojalgo.concurrent

- The `reduce(...)` methods of `ProcessingService` are renamed (deprecated) `reduceMergeable(...)`. In addition there are now also `reduceCombineable(...)`.

#### org.ojalgo.data

- The `processMapped(...)` method of `BatchNode` is renamed (deprecated) `processMergeable(...)`. In addition there is now also `processCombineable(...)`.
- The `reduceMapped(...)` method of `BatchNode` is renamed (deprecated) `reduceByMerging(...)`. In addition there is now also `reduceByCombining(...)`.

#### org.ojalgo.type

- The `merge(Object)` method of `TwoStepMapper` has been removed. Instead there are two new sub-interfaces `TwoStepMapper.Mergeable` and `TwoStepMapper.Combineable`.

## [53.2.0] – 2023-12-29

### Added

#### org.ojalgo.data

- There is a new package `org.ojalgo.data.transform` that (so far) contain implementations of the `ZTransform` and the `DiscreteFourierTransform`. Most notably ojAlgo now contains an implementation of the FFT algorithm.
- `ImageData` has been extended with features to work with Fourier transforms of images, as well as a Gaussian blur function.

#### org.ojalgo.function

- The `PolynomialFunction` interface now extends `org.ojalgo.algebra.Ring` which means it is now possible to `add` and `multiply` polynomials. There's also been some refactoring of the internals of the polynomial implementations.
- Two new `PrimitiveFunction.Unary` implementations `PeriodicFunction` and `FourierSeries`. It is now possible to take any (periodic) function and, via FFT, get a Fourier series approximation of it (1 method call).

#### org.ojalgo.scalar

- Some minor additions to `ComplexNumber`. It is now possible to do complex number multiplication without creating new instances (calculate the real and imaginary parts separately). Added utilities to get the unit roots.

## [53.1.1] – 2023-10-16

### Added

#### org.ojalgo.data

- New `ImageData` class that wraps a `java.awt.image.BufferedImage` and implements `MatrixStore`. Further, it adds a few utility methods to simplify working with image data - convert to grey scale, re-sample (change size), separate the colour channels...

#### org.ojalgo.matrix

- It is now possible to create a matrix decomposition instance and calculate the decomposition with 1 method call. Previously you had to first call `make` on the factory instance and then `decompose` on the decomposition instance. Now it is possible to call `decomposed` directly on the factory instance.
- The `SingularValue` interface now defines a method to reconstruct a matrix using a specified number of singular values. 

## [53.1.0] – 2023-09-17

### Added

#### org.ojalgo.optimisation

- New solver validation mechanism - a tool for solver debugging.
- When using `ExpressionsBasesModel` the dual variables or Lagrange multipliers are now present in the `Optimisation.Result`. It's a map of `ModelEntity` and `Optimisation.ConstraintType` pairs to the dual variable value for that pair.
- New optimisation option "experimental", `Optimisation.Options#experimental`, that turns on experimental features (if there are any). Currently this will switch the LP solver to a new implementation that scales better. This new LP solver will become the default, and it already works quite well, but will remain a configuration option for a while.

### Fixed

#### org.ojalgo.optimisation

- There was a case when the `IntegerSolver` returned an incorrect solution (constraint breaking) but reported it to be optimal. It was actually the `LinearSolver` that malfunctioned, but behaviour in the `IntegerSolver` that generated the problematic node model. Fixed this problem by 1) making sure the `LinearSolver` handles that case, and 2) altered the problematic behaviour in the `IntegerSolver` to be "safer".

### Changed

#### org.ojalgo.array

- More efficient implementation of `reduce(int,int,Aggregator)` in `ArrayAnyD` (reduce to a 2D structure).

#### org.ojalgo.equation

- Changed the internal (equation body) delegate type from `BasicArray<Double>` to `BasicArray<?>`. The only resulting (breaking) API change is the return type of the `getBody()` method.

#### org.ojalgo.matrix

- More efficient setup of the `IterativeSolverTask.SparseDelegate` solvers when the equation system body is any kind of sparse `MatrixStore`.

#### org.ojalgo.optimisation

- Tweaked how the MIP cut generation works.
- Moved the nested `EntityMap` interface from `UpdatableSolver` to `ExpressionsBasedModel`.
- When invoking `solve()` directly on a `GenericSolver.Builder` the solution now has the slack variables removed from the results.

### Removed

#### org.ojalgo.optimisation

- Cleaned up among classes and interfaces for optimisation data modelling. `OptimisationData` and `Optimisation.SolverData` are both gone. Making `ConvexData` public covers whatever those interfaces where used for.

#### org.ojalgo.structure

- Deprecated `loop(Predicate<long[]>,IndexCallback)` in favour of the new `loopReferences(Predicate<long[]>,ReferenceCallback)` method in `StructureAnyD`.

## [53.0.0] – 2023-04-16

### Added

#### org.ojalgo.array

- Implementations to support the new `Quadruple` element type.

#### org.ojalgo.equation

- It is now possible to wrap an existing `BasicArray` instance in an `Equation`, as the equation body, and then later retrieve that instance to be recycled/reused.

#### org.ojalgo.function

- Implementations to support the new `Quadruple` type. In most cases these delegate to BigDecimal implementations. Proper `Quadruple` implementations can be done later.
- New `BigMath` constants `SMALLEST_POSITIVE_INFINITY` and `SMALLEST_NEGATIVE_INFINITY`.

#### org.ojalgo.machine

- Support for AARCH64 and Apple M1 Pro

#### org.ojalgo.matrix

- All sorts of additions – many many – to fully support the new `Quadruple` element type.
- New names for the top-level (immutable) BasicMatrix classes. The old ones are still there, but deprecated. The new ones are purely renamed copies of the old.
- Modified LDL (Cholesky) decomposition – set a threshold value on the diagonal elements while decomposing.
- New interface `InvertibleFactor` that represent chainable and reversible in-place (equation system) solvers. Suitable for product form representation. The `MatrixDecomposition.Solver` interface now extends this new interface. That means it is now also possible to solve the transposed system (or solve from the left).
- `RowsSupplier` and `ColumnsSupplier` now implements `MatrixStore` and `Mutate2D` rather than just `Access2D` and `ElementsSupplier`.

#### org.ojalgo.optimisation

- New alternatives for the various solver builders to simplify building small test case models - just cleaner api. Now also possible to specify matrices of any element type.
- New structure in `Optimisation.Options`. Options for the LP- and QP-solvers are now clearly separated. Some important parts/parameters of the ConvexSolver (QP) are now configurable.
- `OptimisationData`: This class existed before but was package private. It is used as the underlying data of the solver builders, and as a solver data interchange format.
- New set of `LinearSolver` implementations meant to replace the existing ones. This already works better than the old/existing ones in many ways, but does not yet have all the features required to replace them. For now add the `LinearSolver#NEW_INTEGRATION` if yo want o to use this. (For pure LP this new solver scales better, but as a subsolver for MIP it lacks necessary features.)
- New experimental extended precision `ConvexSolver`. It's implemented using the `Quadruple` and iteratively solves a sequence of refined (zoomed and scaled) QP problems. This enables to correctly/exactly solve problems with very detailed/accurate constraints. This new solver is contributed by Magnus Jansson (@Programmer-Magnus).

#### org.ojalgo.scalar

- New `Scalar` type `Quadruple` emulating quadruple precision using 2 `double`s.

#### org.ojalgo.structure

- `Factory1D`, `Factory2D` and `FactoryAnyD` instances now have to declare what MathType the structures they create contains. The factory implementations now have a `getMathType` method.
- Added the ability to get/set values of 1D- and 2D-data structure using `Keyed1D` and `Keyed2D`. Using `IndexMapper` to map back and forth between an index and a key of any type.

### Changed

#### org.ojalgo.array

- The `ArrayR128` class changed from being `BigDecimal` based to `Quadruple` based. Instead there is a new `ArrayR256` class that is `BigDecimal` based.

#### org.ojalgo.function

- New set of factory methods for `MultiaryFunction`:s. The old ones are deprecated.
- Renamed the existing `PolynomialFunction` implementations. The old classes are still there, but deprecated. Also added a few new subclasses/element types.

#### org.ojalgo.matrix

- New names for the top-level (immutable) BasicMatrix classes. The old ones are still there, but deprecated. The new ones are purely renamed copies of the old.
- Various factories have been renamed to match the `MathType` enum constants, and the old one deprecetd. This relates to matrices, matrix "stores", decompositions, tasks...

#### org.ojalgo.optimisation

- Changes to how parameter scaling is done.
- When constructing convex (QP) solver, simple variable bounds are no longer scaled.
- It is now possible to extract both adjusted and unadjusted model parameters as `BigDecimal`. This is to allow individual solver integrations to do type conversion to different types without intermediate loss of precision.
- Modified the EBM file format to also include known variable values. Format (reader/writer) compatible with both old and new variants.
- Refactoring of the `ConvexSolver` class hierarchy. In particular with the `ActiveSetSolver` there should now be a lot less copying of data.
- There used to be 2 different `NumberContext`:s used for print/display/toString formatting in `ExpressionsBasesModel`. Now there is only one. The configurable `Optimisation.Options.print` value, and the default value is `NumberContext.of(8)`.
- Usage of the `Optimisation.Options.print` configurable value is any solver has been removed. This option still remains but is only used in `ExpressionsBasesModel`. The various solvers that made use of it now have their own definitions, that may or may not be configurable.
- The `IntegerStrategy` interface gained a new method – `getIntegralityTolerance()`. It returns a `NumberContext` used to check variable integrality.

#### org.ojalgo.type

- The definition of `MathType.R128` changed. It used to refer to a `BigDecimal` based Real number. Now `MathType.R128` refers to implementations using the new `Quadruple` class, and the `BigDecimal` based stuff is referred to as `R256`.

### Deprecated

#### org.ojalgo.array

- The `limit` and `fixed` methods of `ListFactory`, `MapFactory` and `SparseFactory` is deprecated. There's been, primarily internal, refactoring of how these factories work. This is the only change to the public API.

#### org.ojalgo.optimisation

- Any/all ways to create `Variable` or `Expression` instances separate from (and then add them to) an `ExpressionsBasedModel` is deprecated. You should first create the model, and then use that as a factory for the variables and expressions.

#### org.ojalgo.type

- `IntCount`
- A few methods in `Hardware` and `VirtualMachine` (actually in `CommonMachine`)

#### org.ojalgo.structure

- All the various "loop" methods in `Structure1D`, `Structure2D` and `StructureAnyD` are deprecated. They performed terribly bad. Everything in ojAlgo that made use of them have already been refactored to perform better.

### Fixed

#### org.ojalgo.matrix

- Ordering of eigenvalues. Sometimes, with real negative eigenvalues, the eigenvalues/vectors where not ordered (correctly) although the decomposition instance reported they should be.
- The `reconstruct()` methods of the `LU` and `LDL` decompositions did not correctly handle pivoting, resulting in incorrect reconstructed matrices.

## [52.0.1] – 2022-10-20

### Added

#### org.ojalgo.optimisation

- New method `describe()` in `ExpressionsBasedModel` that returns an object with various descriptive counts (number of variables, constraints, integers...)

#### org.ojalgo.type

- New utility `EnumPartition` which is a generalised alternative to `IndexSelector`.

### Changed

#### org.ojalgo.matrix

- Slight modification to how the preconditioning in `ConjugateGradientSolver` works, this is a revert of a change in the last release.

#### org.ojalgo.type

- The `getIncluded()` and `getExcluded()` methods of `IndexSelector` now return cached/reused int[]:s when possible.

### Fixed

#### org.ojalgo.array

- Optimised implementation of `indexOfLargest` in `SparseArray`.

#### org.ojalgo.matrix

- Optimised implementation of `indexOfLargest` in `SparseStore`.

## [52.0.0] – 2022-09-27

### Added

#### org.ojalgo.algebra

- New enum `NumberSet` outlining the number sets used within ojAlgo.

#### org.ojalgo.array

- Restored support for native/off-heap memory based array implementations, `OffHeapArray`. For a while now this was supported via an extension artifact, ojAlgo-unsafe.That artifact is now deprecated and will not be updated further.
- New primitives based array classes for `long`, `int`, `short` and `byte` elements. This includes native off-heap and buffer based variants - even the memory mapped file-backed variants.
- New naming scheme for the various "array" classes and the factories. Most (if not all) the old/previous classes, factories and factory methods sill exist but are deprecated. The new names are based on the members of the `org.ojalgo.type.math.MathType` enum.

#### org.ojalgo.data

- New `DatePriceParser` parser that will analyse the contents (first header line) to determine the file format, and then choose an appropriate parser to use.

#### org.ojalgo.matrix

- Experimental `ParallelGaussSeidelSolver`. The name says what it is. Seems to work and improve performance.

#### org.ojalgo.netio

- New class `InMemoryFile` to be used when writing to and/or reading from "files" that never actually exist on disk – for dynamically creating files for downloading or parsing uploaded "files". The `TextLineWriter` and `TextLineReader`, in particular, gained support for this.
- The `TextLineReader` now support filtered parsing – text lines that do not match the filter are skipped.
- New abstract class `DetectingParser`. It's a single parser that can switch between a collection of internal delegate parsers. Create a subclass to specify which parsers are avalable, as well as logic to choose between them. The new `org.ojalgo.data.domain.finance.series.DatePriceParser` makes use of this.
- New `ServiceClient`. It's an http client based on Java 11's `HttpClient` designed to replace `ResourceLocator`.

#### org.ojalgo.structure

- Support for getting/setting elements of all (numeric) primitive types.

#### org.ojalgo.type

- The `MappedSupplier` now supports an optional filter. Items that don't pass this filter are not mapped, instead the `MappedSupplier` moves on to the next item.
- New enum `MathType` outlining the types used in ojAlgo. It's the mathematical number set paired with info about how it is implemented (ComplexNumber = 2 * double)
- `NativeMemory` now support initialising and filling off-heap arrays.

### Changed

#### org.ojalgo.array

- New generalised way to create memory-mapped file-based array classes
- Quite a bit of refactory to support everything that's new - better support for any/all primitive type, off-heap arrays and more.

#### org.ojalgo.data

- The `DataFetcher` interface had some additions and deprecations, and all the implementations are refactored to use the new `ServiceClient` rather than `ResourceLocator`.

#### org.ojalgo.equation

- The RHS property of `Equation` is now mutable – there is both get- and a set-method.
- Bunch of new factory methods for `Equation` (variants of the previously existing ones with the array factory set to `Primitive64Array.FACTORY`).

#### org.ojalgo.matrix

- New method in `IterativeSolverTask.SparseDelegate` that lets you (re)solve with a supplied RHS.
- Slight modification to how the preconditioning in `ConjugateGradientSolver` works.

#### org.ojalgo.optimisation

- Some refactoring regarding how parameters are extracted from `ExpressionsBasedModel`, `Expression` and `variable`.

#### org.ojalgo.structure

- The `Mutate*D.Fillable` interfaces now extend their respective `Mutate*D` interface.
- The method `StructureAnyD.loopAll(ReferenceCallback)` has been renamed `StructureAnyD.loopAllReferences(ReferenceCallback)` to better differentiate it from `Structure1D.loopAll(IndexCallback)`. (It also got a more efficient default implementation.)

#### org.ojalgo.type

- If a `QueuedConsumer` delegates to a `Consumer` that is also an `AutoConsumer` the `QueuedConsumer` will call the `AutoConsumer`'s `writeBatch(Iterable)` method rather than the `write(Object)` method – it will push batches, rather than individual items, to the delegate.
- The `KeyValue` interface was deprecated, but is no longer. Instead `EntryPair` now extends `KeyValue`, and `KeyValue` gained a collection of factory mehods to create pairs. Further the definition of `Dual` moved from `EntryPair` to `KeyValue`.

### Deprecated

#### org.ojalgo.structure

- All the various `fillOne(...)` methods in the `Mutate*D.Fillable` interfaces are deprecated. Just use `set(...)` instead.

### Fixed

#### org.ojalgo.data

- Downloading historical financial data from Yahoo Finance works again!

#### org.ojalgo.optimisation

- Fixed rare case of inconsistencies between branches in the IntegerSolver. (Could result in wrong solutions!)

### Removed

#### org.ojalgo.type

- A bunch of stuff in `org.ojalgo.type.keyvalue` that has been deprecated for a while is now actually removed.
- Some old code in `org.ojalgo.netio` that was deprecated is now removed.

## [51.4.1] – 2022-08-26

### Fixed

#### org.ojalgo.optimisation

- Fixed a problem with the `IntegerSolver` where you could get an `ArrayIndexOutOfBoundsException` when concurrently solving multiple problem instances sharing the same `IntegerStrategy`.

#### org.ojalgo.random

- Fixed a regression with `RandomNumber` where it was no longer possible to set a seed for the underlying `java.util.Random` instance.

## [51.4.0] – 2022-07-05

Last version to target Java 8!

### Added

#### org.ojalgo.data

- New class `org.ojalgo.data.domain.finance.series.FinanceDataReader` that implements both `FinanceData` and `DataFetcher`. Instead of fetching data from some (web) service it reads and and parses files. Already had some parsers, and since it implements both `FinanceData` and `DataFetcher`, it can be used with much of the existing code. In particular `DataSource` has been updated to include this reader option.
- New alternatives to calculate correlations and covariance matrices in `DataProcessors`. 

#### org.ojalgo.function

- `AggregatorFunction` gained a new method `filter(PredicateFunction)` that allows to define a filter for which values will be considered in the aggregation.

#### org.ojalgo.netio

- New methods in `BasicLogger` to handle logging of exceptions with stacktrace.
- With a `TextLineWriter` it is now possible to instantiate a CSV line/row builder to help create "text lines" that are delimited data.

#### org.ojalgo.random

- New package `org.ojalgo.random.scedasticity` containing `ARCH` and `GARCH` models as well as stochatstic processes based on those. 

#### org.ojalgo.series

- New class `SimpleSeries` that is the simplest possible `BasicSeries` implementation.
- Added capabilities to do things like `quotients`, `log` and `differences` on a `CoordinatedSet` (of `PrimitiveSeries`) as well as possibility to get a correlations or cocariance matrix directly.

### Changed

#### org.ojalgo.data

- Refactoring of the `org.ojalgo.data.domain.finance.series` package. This package very much depends on `org.ojalgo.series` which is extensively refactored.
- `FinanceData` is now generic and the `getHistoricalPrices()` method is now declared to contain a specified subclass of `DatePrice`.
- `DatePrice` now implements `EntryPair.KeyedPrimitive` rather than the deprecated `KeyValue` interface. The public `key` field has been renamed `date` (the date is the key). All `DatePrice` subclasses are now immutable.
- The `getPriceSeries()` method of `FinanceData` changed the return type from `BasicSeries<LocalDate, Double>` to `BasicSeries<LocalDate, PrimitiveNumber>` and the `BasicSeries` implementation used is also changed.

#### org.ojalgo.random

- The `Process1D` class is now final. It used to have some subclasses that had been depreceted for a while, they're now removed.

#### org.ojalgo.series

- Extensive refactoring of all `BasicSeries` subinterfaces and implementations, including some breaking name changes. The `BasicSeries.NaturallySequenced` interface still exists, but is not really used for anything. All the useful stuff is in `BasicSeries`. All methods that used `long` keys to interact with entries are removed.

#### org.ojalgo.type

- The interface `CalendarDate.Resolution` now also extends `Structure1D.IndexMapper` and defines a method to `adjustInto` for `CalendarDate`.

### Fixed

#### org.ojalgo.matrix

- Fixed problems related to extracting eigenpairs and calculating generalised `Eigenvalue` decompositions for complex matrices (`ComplexNumber` elements).

## [51.3.0] – 2022-05-15

### Added

#### org.ojalgo.data

- New batch processising tool `BatchNode` to do processing of huge data sets on a single machine.

#### org.ojalgo.netio

- New interfaces `FromFileReader` and `ToFileWriter` paired with a wide range of implementations, builders, parsers, interpreters... There is also a new class `ShardedFile` that describes a set of shards and allow creations of readers and writers of the total set of files.

#### org.ojalgo.structure

- `Access1D`, `Access2D` and `AccessAnyD` each gained additional features to `select` (view) subsets of the elements and/or to iterate over elements/rows/columns/vectors/matruces...

#### org.ojalgo.type

- New utilities `CloseableList` and `CloseableMap` to simplify handing (closing) multiple readers/writers.
- Whole new package, `org.ojalgo.type.function`, with lots of utlities for consumers/suppliers (or readers/writers). A lot of the new stuff in `org.ojalgo.netio` build on this.
- Additions to EntryPair. Primarily to allow creation of key-value "pairs" with dual keys.

### Changed

#### org.ojalgo.concurrent

- The `ParallelismSupplier` interface had the `min` and `max` methods renamed `limit` and `require` to better dscribe what they do.
- Refactoring and additions to `ProcessingService`.

#### org.ojalgo.netio

- Reimplemented the IDX file parser in terms of `DataInterpreter` and `DataReader`.
- Refactored the `BasicParser` interface to make use of the new `FromFileReader` and `ToFileWriter`.
- Refactored `BasicLogger` and everything associated with it. There are API-breaking changes, but with stuff mostly used internally.

#### org.ojalgo.optimisation

- The default value of the (MIP) gap property in `IntegerStrategy` changed from `NumberContext.of(6,8)` to `NumberContext.of(7,8)`. This is to match what the recently deprecated mip_gap option used to be. The default MIP gap used to 1E-6, and that corresponfs to 7 digits precision (not 6).

#### org.ojalgo.random

- Refactoring and additions to `FrequencyMap`.

#### org.ojalgo.structure

- The various `row`, `rows`, `column` and `columns` methods in `Structure2D.Logical` have been signature-refactored to be more logical. If you used the `row` or `column` alternatives to reference more than 1 row/column you need to change your code to instead use `rows` or `columns`.
- The previously existing `Access2D.RowView`, `Access2D.ColumnView`, `AccessAnyD.VectorView` and `AccessAnyD.MatrixView` gained support to "goTo" directly to a specified row/column/vector/matrix

### Deprecated

#### org.ojalgo.netio

- A bunch of old useless stuff... will be removed eventually.

### Fixed

#### org.ojalgo.matrix

- Calling `indexOfLargest()` on a `Primitive64Store`, `Primitive32Store` or `GenericStore` would result in a StackOverflowError.

#### org.ojalgo.optimisation

- There was a problem with integer rounding of lower/upper bounds of integer expressions - since it was applied too late the presolver would sometimes fail to detect infeasible nodes as such, and instead generate an incorrect problem for the main solver. This was not a very common problem, but did happen sometimes, and the fix made the presolver generally more efficient.

## [51.2.0] – 2022-04-20

### Added

#### org.ojalgo.function

- `MissingMath` can now find the greatest common denominator of multiple int:s or long:s.

#### org.ojalgo.matrix

- Added support for creating diagonal matrices in `MatrixFactory`.

#### org.ojalgo.optimisation

- There is now a new solver, `GomorySolver`, for (mixed) integer models. It implements Gomory's cutting plane method. This solver is primarily used to test the cut generation feature used in `IntegerSolver`, which is the solver to use for (mixed) integer problems. The real news here is that `IntegerSolver` now generates GMI cuts as part of the solve process.
- Utilities in `Expression` (actually in `ModelEntity`) to simplify creating `Expression`:s from combinations of other `Expression`:s (`ModelEntity`.s).

#### org.ojalgo.type

- Additional utilities in `NumberContext` like `isInteger(double)`, `isSmall(BigDecimal,BigDecimal)` and more.

### Changed

#### org.ojalgo.optimisation

- The `IntegerSolver` is no longer a pure branch-and-bound algorithm. It now also generates GMI cuts.
- A whole lot of refactoring to enable cut generation for the `IntegerSolver`. This touches almost everything in the optimisation package, but the public API:s for normal/recommended usage should be unchanged.

### Deprecated

#### org.ojalgo.optimisation

- Deprecated the separation between preferred and fallback solver integrations in `ExpressionsBasedModel`. Instead, if you want add a solver integration, you simply call `addIntegration(Integration<?>)`.

### Fixed

#### org.ojalgo.optimisation

- The MPS file parser of `ExpressionsBasedModel` has been refactored and can now handle more format variants. In particular some instances from MIPLIB2017 had problems.

### Removed

- The methods `isMinimisation()` and `isMaximisation()` from `ExpressionsBasedModel`. They've been deprecated for a while and are replaced by `getOptimisationSense()`. There is no way to set the optimisation sense – you simply call `minimise()` or `maximise()`;

## [51.1.0] – 2022-03-17

### Added

#### org.ojalgo.optimisation

- `Expression` gained `add` methods corresponding to each of the existing `set` methods.

### Deprecated

#### org.ojalgo.optimisation

- The `IntIndex` and `IntRowColumn` variants of the `Expression` `add` and `set` methods are deprecated. You should use the alternatives taking a `Variable` or simply an `int` instead.

#### org.ojalgo.concurrent

- New class `MultiviewSet` that combines a `Set` with multiple `PriorityQueue`:s. This allows to have multiple task queues, with different priorities, all backed by a common set of tasks.

### Changed

#### org.ojalgo.optimisation

Big changes for the `IntegerSolver`!

- New way to multi-thread the `IntegerSolver`. It no longer does fork-join, but instead makes use of ojAlgo's `ProcessingService`.
- The `Optimisation.Options.mip_defer` and `Optimisation.Options.mip_gap` configurations are no longer used. Instead there is a whole new framework for how to control the `IntegerSolver`. This framework will be a work in progress for quite some time. Please use it, and give feedback, but don't expect it to be a stable API.

### Fixed

#### org.ojalgo.optimisation

- Calling `model.simplify()` no longer discards constraints flagged both redundant and infeasible – info about the model being infeasible is no longer lost.

### Removed

- A bunch of stuff that's been deprecated for a while is now removed. Only some of which is specifically mentioned below.

#### org.ojalgo.structure

- The interfaces `Access*D.Elements` and `Access*D.IndexOf` have been removed. Some parts of what they defined are still available via other interfaces. Like for instance the `Access1D.Aggregatable` interface took over the `indexOfLargest()` method.

## [51.0.0] – 2022-02-21

### Added

#### org.ojalgo.data

- ojAlgo-finance is no longer maintained as a separate repository. Most of its contents have been moved here:

- ojAlgo-finance:org.ojalgo.finance -> org.ojalgo.data.domain.finance
- ojAlgo-finance:org.ojalgo.finance.portfolio -> org.ojalgo.data.domain.finance.portfolio
- ojAlgo-finance:org.ojalgo.finance.portfolio.simulator -> org.ojalgo.data.domain.finance.portfolio.simulator
- ojAlgo-finance:org.ojalgo.finance.data -> org.ojalgo.data.domain.finance.
- ojAlgo-finance:org.ojalgo.finance.data.fetcher -> org.ojalgo.data.domain.finance.
- ojAlgo-finance:org.ojalgo.finance.data.parser -> org.ojalgo.data.domain.finance.
- ojAlgo-finance:org.ojalgo.finance.scalar -> org.ojalgo.scalar
- ojAlgo-finance:org.ojalgo.finance.business -> *not moved, wont be maintained*

#### org.ojalgo.optimisation

- Now possible to save and reload optimisation models from files - new `ExpressionsBasedModel` specific file format.

### Changed

#### org.ojalgo.optimisation

- Cleanup and refactoring of `LinearSolver` and related classes.
- Improved numerical stability of `ConvexSolver`.

## [50.0.2] – 2022-01-26

### Changed

#### org.ojalgo.matrix

- Refactoring and re-tuning of Householder related code. The QR decomposition in particular.

#### org.ojalgo.structure

- Modified the behaviour of the `Access1D.equals(Access1D<?>,Access1D<?>,NumberContext)` utility method. It's used to determine if two 1D data structures are numerically similar/equal. This change also affects the behaviour of corresponding functionality in `Access2D` and `AccessAnyD` as well as various `TestUtils` methods.

## [50.0.1] – 2022-01-09

### Fixed

#### org.ojalgo.matrix

- Matrix multiplication performance regression introduced with v49.0.0.

## [50.0.0] – 2022-01-02

### Added

#### org.ojalgo.matrix

- New interface `Provider2D` with a set of nested functional interfaces defining matrix properties and operations.

#### org.ojalgo.optimisation

- Possibility to read the QPS file format (QP related extensions to the MPS file format). More precisely added the ability to parse QUADOBJ and QMATRIX sections in "MPS" files.
- A bunch of convex test cases from https://www.cuter.rl.ac.uk/Problems/marmes.shtml

#### org.ojalgo.random

- New `FrequencyMap` class as well as a factory method in `SampleSet` that counts occurrences of different values.

#### org.ojalgo.structure

- Added a `nonzeros()` method to `Access2D` that returns a `ElementView2D<N, ?>`.

### Changed

#### org.ojalgo.function

- The `MIN` and `MAX` `BinaryFunction` constants of `ComplexMath` and `QuaternionMath` are changed to align with the scalar's compareTo methods (that are also changed).

#### org.ojalgo.matrix

- `MatrixStore` now implements `Structure2D.Logical` directly. No need to call `logical()` to get a `LogicalBuilder`.
- `BasicMatrix` now implements `Structure2D.Logical` as well as `Operate2D` directly. No need to call `logical()` to get a `LogicalBuilder`.
- A lot of refactoring among the package private code.

#### org.ojalgo.optimisation

- Minor change regarding `LinearSolver` pivot point selection.

#### org.ojalgo.scalar

- `ComplexNumber` and `Quaternion` had their `compareTo` methods changed to first just compare then real part and only if they're equal compare the imaginary parts.

### Deprecated

#### org.ojalgo.matrix

- The `logical()` method in `MatrixStore` is deprecated. No need for it as `MatrixStore`:s are now "logical".
- The `logical()` method in `BasicMatrix` is deprecated. No need for it as `BasicMatrix`:s are now "logical".

### Fixed

#### org.ojalgo.matrix

- Fixed rare multiplication problem when all involved matrices were `RawStore` instances and the left multiplcation matrix was a vector, but a column vector when a row vector was expected; in that case the multiplication code would fail.

### Removed

#### org.ojalgo.matrix

- The `MatrixStore.Factory` interface has been removed. Corresponding functionality have instead been added to `PhysicalStore.Factory`. This also mean that the various static factory instances in `MatrixStore` have been removed. Instead use the instances available in each of the `PhysicalStore` instances.
- The `MatrixStore.LogicalBuilder` class has been removed. Instead `MatrixStore` now implements `Structure2D.Logical` directly. No need to call `logical()` to get a `LogicalBuilder`.
- The `BasicMatrix.LogicalBuilder` class has been removed...

#### org.ojalgo.structure

- The `Factory*D` interfaces had their `makeZero` methods removed. These had been deprecated for while, and are now removed.
- The `Structure*D.Logical` interfaces had their `get` methods removed. Most implementors still have a `get` method. This is just to make it more flexible regarding what type is returned.

## [49.2.1] – 2021-10-26

### Changed

#### org.ojalgo.optimisation

- The LinearSolver (simplex) pivot selection code has been refined.

## [49.2.0] – 2021-10-05

### Changed

#### org.ojalgo.optimisation

- Changed what alternatives (method signatures) are available to copy and/or relax optimisation models. These are API (behaviour) breaking changes, but of features primarily used within ojAlgo (when writing test cases and such). If you've used methods named `copy`, `relax`, `snapshot` or `simplify` in `ExpressionsBasedModel` then be aware. Even if your code still compiles it may not do exactly what it did before.
- Refactoring of `ExpressionsBasedModel` primarily affecting how presolving deals with integer variables - increased the number of cases when integer rounding will actually occur.

### Fixed

#### org.ojalgo.optimisation

- Fixed problem with progress/debug logging of `IntegerSolver`. The node id and count were not correct. (Only an issue with log output.)

## [49.1.0] – 2021-09-19

### Changed

#### org.ojalgo.matrix

- Slight change in how shifting is done when calculating the EvD for non-symmetric. Not strictly a bug fix but this solved GitHub issue 366 (there is a chance this may introduce problems for other cases).

#### org.ojalgo.optimisation

- The hierarchy of solver builders have been refactored. The most important change is that there are now 2 different `LinearSolver` builders – `StandardBuilder` and `GeneralBuilder`.
- Some very very small inequality parameters used to be removed (rounded to 0.0) when instantiating `ConvexSolver`. This is no longer done.
- The `ActiveSetSolver` (`ConvexSolver`) no longer makes use of Lagrange multipliers obtained when finding an initial feasible solution (they're mostly 0.0 anyway).
- Refactoring to reduce copying (memory garbage) when initialising solvers
- Changed the default behaviour when `Optimisation.Options.sparse` is not set. If you don't set this the dense LinearSolver and the iterative ConvexSolver will be used.

### Deprecated

#### org.ojalgo.optimisation

- The solver builders have been refactored. All previous public constructors and factory methods are now deprecated. What you should do now is call `ConvexSolver.newBuilder`, `LinearSolver.newStandardBuilder` or `LinearSolver.newGeneralBuilder`. The only thing you absolutely have to change now is if you explicitly/directly called the `LinearSolver.Builder` constructor. That needs to be replaced by `LinearSolver.StandardBuilder`. Don't forget! The recommendation is to use `ExpressionsBasedModel` and let it instantiate the solvers for you. In that case you do not need to worry about any of these changes.

## [49.0.3] – 2021-09-05

### Changed

#### org.ojalgo.optimisation

- Changed how the ConvexSolver (with inequality constraints) finds a first feasible solution. It should be both faster and more resilient now.

## [49.0.2] – 2021-08-24

### Changed

#### org.ojalgo.tensor

- Changed the TensorFactory API a bit. (This package contains new functionality that may see more changes before the API stabilizes.)

### Fixed

#### org.ojalgo.matrix

- Continued operations on a transposed `ElementsSupplier` in some cases reversed the transposition.

## [49.0.1] – 2021-08-11

### Fixed

#### org.ojalgo.matrix

- Sparse-sparse matrix multiplication didn't work for non-primitive matrixes: https://github.com/optimatika/ojAlgo/issues/360

## [49.0.0] – 2021-08-07

- Many things that have been deprecated for a while are now actually removed. Not all are mentioned specifically below.

### Added

#### org.ojalgo.ann

- Now possible to train and invoke/evaluate neural networks in batches.

#### org.ojalgo.array

- `Array2D` and `ArrayAnyD` are now reshapable

#### org.ojalgo.concurrent

- Additions to `DaemonPoolExecutor`: A `ThreadFactory` factory method, as well as a set of `ExecutorService` factory methods that makes use of that.
- New utility `ProcessingService` standardise/simplify some `ExecutorService` usage.

#### org.ojalgo.data

- New `DataBatch` class. It's a resuable component to help collect 1D data in a 2D structure. Can be used with neural networks (and other things) to batch data.

#### org.ojalgo.matrix

- New interface `Matrix2D` common to both `BasicMatrix` (implements it) and `MatrixStore` (extends it).
- Additional matrix multiplication variants implemented.

#### org.ojalgo.structure

- A few additions to `Structure2D.Logical` like `symmetric(...)` and `superimpose(...)`
- New interface `Structure2D.Reshapable` and `StructureAnyD.Reshapable`
- `AccessAnyD` is now vector-terable in the same way it was already matrix-iterable – it now has a method `vectors()` and there is a new utility class `VectorView`.
- `Structure2D` now directlty define the `int` valued `getRowDim()`, `getColDim()`, `getMinDim()` and `getMaxDim()` methods.

#### org.ojalgo.tensor

- This package existed before but didn't really contain anything functional/useful – now it does. Now it contains 1D, 2D, and AnyD tensor implementations. These are not just (multi dimensional) arrays, but mathematical tensors as used by physicists and engineers. They are instatiated via special factories that implement various tensor products and direct sums. Further these factories are implemented as wrappers of (they delegate to) other 1D, 2D or AnyD factories. This means that just about any other data structure in ojAlgo can be created using the tensor product or direct sum implemenatations of these factories.

### Changed

#### org.ojalgo.ann

- Internal refactoring to `ArtificialNeuralNetwork` primarily to improve performance.
- The activator RECTIFIER has been deprecated/renamed RELU which is more in line with what users expect.

#### org.ojalgo.concurrent

- Changed the `Parallelism` enum. Changed which instances are available but increased flexibility by implemention the new `ParallelismSupplier` interface. 

#### org.ojalgo.matrix

- `ElementsSupplier` no longer extends `Supplier<MatrixStore<N>>` and no longer defines the method `PhysicalStore.Factory<N, ?> physical()`. Instead subinterfaces/implementors define corresponding functionality as needed.
- According to the docs `ShadingStore`:s (`LogicalStore`:s that shade some elements) are not allowed to alter the size/shape of the matrix they shade, but several implementations did that anyway. This is now corrected. Some matrix decomposition implementations relied on that faulty behaviour when constructing various component matrices. That had to be changed as well.
- Changed, re-tuned, the matrix multiplication concurrency thresholds. 

#### org.ojalgo.structure

- The nested interfaces `Mutate1D.ModifiableReceiver`, `Mutate2D.ModifiableReceiver` and `MutateAnyD.ModifiableReceiver` now also extend `Access*D` which makes them aligned with the requirements of the `Transformation*D` interfaces.

### Fixed

#### org.ojalgo.ann

- Some `ArtificialNeuralNetwork` input would cause matrix calculation problems. The input is typed as `Access1D<Double>` which essentially means any ojAlgo data structure. In the case where the actual/specific type used mached the internal types, but with wrong shape (transposed), there would be matrix multiplication problems. This no longer happens. 

### Removed

#### org.ojalgo.function

- `FunctionUtils`, that only contained deprecated (moved) utility functions, has been deleted.

#### org.ojalgo.matrix

- The entire package `org.ojalgo.matrix.geometry` has been removed. Not deprecated, removed directly. It was never finished, not tested, and now it was in the way of refactoring other matrix stuff. Anything it did can just as well be done with the normal matrix classes.
- `MatrixUtils`, that only contained deprecated (moved) utility functions, has been deleted.

#### org.ojalgo.random

- `RandomUtils`, that only contained deprecated (moved) utility functions, has been deleted.

#### org.ojalgo.structure

- The interfaces `Stream*D` have been removed – they were redundant. The `Operate*D` interfaces replace them.

## [48.4.2] – 2021-04-22

### Added

#### org.ojalgo.function

- Additional default methods for primitive arguments

### Deprecated

#### org.ojalgo.structure

- The various `operateOn*(...)` methods have been deprecated and replaced by simply `on*(...)`

### Fixed

#### org.ojalgo.matrix

- Fixed bug in LowerTriangularStore and UpperTriangularStore regarding shape/range information: https://github.com/optimatika/ojAlgo/issues/330
- Fixed problem regarding extraction of the Q and R matrices from QR decomposition for fat matrices. (One of the QR decomposition implementations had this problem.)


## [48.4.1] – 2021-03-18

### Added

- Added (moved here) JMH benchmarks

### Changed

- Project layout change to match standard Maven
- Update copyright statement to cover 2021

### Fixed

#### org.ojalgo.optimisation

- ExpressionsBasedModel now calls `dispose` on solvers it created, when done
- Optimisation model on file, for test, are now loaded using `getResourceAsStream` which makes it easier to access these from the ojAlgo-test jar


## [48.4.0] – 2020-12-27

### Added

#### org.ojalgo.optimisation

- Better support for building optimisation model with primitive valued parameters - overloaded methods for `long` and `double` values.
- Various minor additions and changes.

### Changed

#### org.ojalgo.optimisation

- Changed the default mip_gap from 1E-4 to 1E-6
- Major rewrite/update to the presolver functionality of `ExpressionsBasedModel` which greatly affects the `IntegerSolver`.

#### org.ojalgo.structure

- The `add` methods of the `Mutate1D`, `Mutate2D` and `MutateAnyD` interfaces have been moved to `Mutate1D.Modifiable`, `Mutate2D.Modifiable` and `MutateAnyD.Modifiable` respectively. With most hgher level interfaces or implemenattions this makes no difference as they typically extend or implement both these interfaces.
- In `Access1D` the `axpy` method had an element of its signture (one of the input parameters) changed from `Mutate1D` to `Mutate1D.Modifiable<?>`.

### Deprecated

#### org.ojalgo.equation

- The public constructors of `Equation` are replaced by various factory methods.

### Removed

#### org.ojalgo.array

- The `NumberList` class had the `add` methods (the ones with a `long` index parameter) previously specified in the `Mutate1D` removed.

#### org.ojalgo.type

- The `IndexedMap` class had the `add` methods previously specified in the `Mutate1D` removed.


## [48.3.2] – 2020-12-05

### Added

#### org.ojalgo.concurrent

- New set of standard levels of parallelism defined in enum Parallelism.

#### org.ojalgo.function

- Additions to PowerOf2 utilities

### Changed

#### org.ojalgo.matrix

- Improved the copying to internal representation for iterative equation system solvers (IterativeSolverTask).

#### org.ojalgo.netio

- Password now encrypts using SHA-512 rather than MD5 (existing passwords need to be reset)

#### org.ojalgo.optimisation

- Slight changes to parameter scaling (presolver functionality in ExpressionsBasedModel)
- Minor numerical tweaks to both LinearSolver and ConvexSolver

### Deprecated

#### org.ojalgo.optimisation

- MathProgSysModel is deprecated - direct usage of that class. Instead there is a `parse(File)` method in ExpressionsBasedModel

#### org.ojalgo.type.context

- Clean up of constructors and factories in NumberContext. Almost all of them are deprecated and replaced by new alternatives.

### Fixed

#### org.ojalgo.optimisation

- GitHUb Issue 300: https://github.com/optimatika/ojAlgo/issues/300


## [48.3.1] – 2020-10-01

### Changed

#### org.ojalgo.optimisation

- Minor internal change to SimplexSolver regarding when phase 1 is regarded done.

### Fixed

#### org.ojalgo.function

- Aggregator.MAXIMUM was initialised/reset incorrectly which caused wrong results with negative numbers

#### org.ojalgo.optimisation

- ConvexSolver results now include the Lagrange multipliers.


## [48.3.0] – 2020-09-03

### Added

#### org.ojalgo.ann

- Support for `float`.
- Possible to "get" all individual parameters of the network
- Possibility to save trained networks to disk (and then later read them back)
- Separate between building, training and invoking the network - 3 different classes to do that.
- Possible to have several network invokers used in different threads.
- Support for `dropouts` as well as `L1` and `L2` regularisation when training the network.

### Changed

#### org.ojalgo.ann

- The NetworkBuilder has been split into a NetworkBuilder and a NetworkTrainer. Most of the previous API is still in place, but deprecated, and in many of those cases old code referencing NetworkBuilder needs to instead use the new NetworkTrainer. The new NetworkBuilder primarily enables a better way to construct the network. Most of the previously existing stuff is in the new NetworkTrainer;

### Deprecated

#### org.ojalgo.ann

- Several things regarding how to build/train and invoke a neural network has been redesigned resulting in deprecations of specific methods.


## [48.2.0] – 2020-06-22

### Added

#### org.ojalgo.function

- New atan2 approximation that is about 10x faster than the ordinary Math.atan2
- The lower/upper incomplete Gamma functions

#### org.ojalgo.random

- Implemented the ChiSquare distribution
- Implemented the T distribution

#### org.ojalgo.structure

- New method repeat(int,int) in Structure2D.Logical implemented in MatrixStore.LogicalBuilder and BasicMatrix.LogicalBuilder.

#### org.ojalgo.type

- New array builder and (type) converter class named FloatingPointReceptacle.
- PrimitiveNumber implementations for all primitive number types.
- New classes EntryPair, EntryList, EntrySet and IndexedMap to deal with key-value pairs in various ways.

### Changed

#### org.ojalgo.optimisation

- Changed how the IntegerSolver instantiates its ForkJoinPool; using Java 9's more expressive constructor if it's available.
- Modifications to the parameter scaling functionality of ExpressionsBasedModel

### Deprecated

#### org.ojalgo.type

- Everything, previously existing, in the org.ojalgo.type.keyvalue package has been deprecated. Instead there is a new interface EntryPair, as well as a collection of implementations, that replace it. The functionality of the old and new stuff only partially overlap. There are also matching classes EntryList, EntrySet and others.

### Fixed

#### org.ojalgo.matrix

- Fixed a problem in SparseStore when concurrently adding different elements
- Reviewed and potentially fixed various problems regarding matrix multiplication with more Than `Integer.MAX_VALUE` elements.

#### org.ojalgo.optimisation

- Fixed a problem where `time_abort` would be ignored if the solver had found a feasible solution. (In that case it would only check `time_suffice`.)

### Removed

#### org.ojalgo.structure

- The all `int` version of the `Structure2D.index(...)` method. With larger 2D structures this would overflow.


## [48.1.0] – 2020-01-15

### Changed

- A number of minor changes to improve interoperability with other JVM languages. Essentially tried to remove all cases with public methods declared in non-public abstract classes.

#### org.ojalgo.array

- Reviewed equals() and hashCode() implementations for most classes
- Explicitly/correctly implemented doubleValue(long) and floatValue(long) methods in more classes

#### org.ojalgo.type

- It is now possible to "stop" and "reset" the Stopwatch with a single method call.

### Fixed

#### org.ojalgo.array

- A case of infinitite loop with (some) fillOne(...) methods


## [48.0.0] – 2019-11-24

### Added

- Improved support for float throughout the library, and specifically added matrices with float elements.

#### org.ojalgo.algebra

- ScalarOperation has been extended with support for float arguments.

#### org.ojalgo.function

- New special functions: beta (complete, incomplete and regularized), gamma (logarithmic), Hypergeometric and Pochhammer symbol. Inluding complex valued variants where applicable. The complete gamma function existed previously, and the upper/lower incomplete gamma functions are only implemented for the integer case.
- All the function interaces now have float specific methods.

#### org.ojalgo.matrix

- There is a new float based matrix store implementation, Primitive32Store.

#### org.ojalgo.random

- The `getDistribution()` method in the TDistribution is now implemented for the general case. Previously it was only implemented for a few distinct degrees of freedom.

### Changed

- Generic declarations in interfaces and abstract classes (everywhere) that used to be `<N extends Number>` are now `<N extends Comparable<N>>`. Code that extends/implements ojAlgo classes and interaces will most likely need to be updated. Simple usage may not require any changes at all. Please note that `java.lang.Number` is NOT `Comparable` but all the speciic subclasses are.
- Everything (classes/interfaces, constants...) named "Primitive" -something now separates between "Primitive32" and "Primitive64".

#### org.ojalgo.matrix

- PrimitiveDenseStore has been renamed Primitive64Store (and there is now also a Primitive32Store). GenericDenseStore was also renamed GenericStore. Likewise PrimitiveMatrix is repalced by Primitive64Matrix and Primitive32Matrix.

#### org.ojalgo.scalar

- ComplexNumber and Quaternion are now final. That means there are no longer special normalised subclasses (no Versor).

#### org.ojalgo.structure

- The methods in the Mutate*D.Fillable interaces that take a NullaryFunction as input has changed the generic declaration from `NullaryFunction<N>` to `NullaryFunction<?>`.
- Reftactoring of the Factory*D interfaces.


## [47.3.1] – 2019-09-29

### Added

#### org.ojalgo.function

- New special function utility class PowerOf2. It replaces what was in PrimitiveMath, made some improvements and addition and added support or 'int' (used to be only 'long').

### Changed

#### org.ojalgo.matrix

- The multithreaded implementations of aggregateAll in PrimkitiveDenseStoree and GenericDenseStore are removed.

#### org.ojalgo.optimisation

- The iterative version of the ActiveSetSolver now enforce an iterations limit on its internal subsolver.

#### org.ojalgo.structure

- The stream(boolean) methods of ElementView, RowView and ColumnView are deprecated and replaced with a simple stream() method. You no longer have the option to use parallel streams.

### Deprecated

#### org.ojalgo.function

- The merge functionality of AggregatorFunction is deprecated.
- Everything related to "power of 2" has been deprecetd in PrimitiveMath. 
- FunctionUtils has been deprecated. Everything in it has been moved elsewhere – mostly to MissingMath.

### Fixed

- The compareTo method of CalendarDateDuration didn't work when the unit of either instances was "nanos".


## [47.3.0] – 2019-08-08

### Changed

#### org.ojalgo

* The `OjAlgoUtils.ENVIRONMENT` can now be modified to limit the parallelism of ojAlgo.

#### org.ojalgo.algebra

* Added a power(int) method to the Operation.Multiplication interface.

#### org.ojalgo.array

* The package org.ojalgo.array.blas has been renamed org.ojalgo.array.operation
* The utility classes Raw1D, Raw2D and RawAnyD have been removed and their contents moved to various classes in the new package org.ojalgo.array.operation
* Everything in org.ojalgo.matrix.store.operation has been moved to org.ojalgo.array.operation

#### org.ojalgo.data

* Added a variant of the covariances method in DataProcessors that take `double[]...` as input.

#### org.ojalgo.function

* Refactoring in the org.ojalgo.function.multiary package including (api-breaking) name changes to some interfaces and classes. The previous QuadraticFunction has been renamed PureQuadraticFunction, and CompoundFunction renamed QuadraticFunction. Further there is now both a LinearFunction and an AffineFunction.

#### org.ojalgo.matrix

* Various deprecations in MatrixStore.LogicalBuilder and the corresponding LogicalBuilder:s of PrimitiveMatrix, ComplexMatrix & RationalMatrix. Everything in the LogicalBuilder:s are now either defined in org.ojalgo.structure.Structure2D.Logical or deprecated.
* Tweaked the isSolvable() method implementations of the Cholesky decompositions to return `true` slightly less often.
* The debug logging of the iterative solvers now output the relative error at each iteration.
* Implemented the power(int) method defined in Operation.Multiplication.
* New method getCovariance in the SingularValue interface
* Q1 and Q2 in the SingularValue decomposition have been renamed U and V to match denominations commonly used elsewhere. In Bidiagonal Q1 and Q2 have been renamed LQ and RQ.
* New MatrixStore implementation DiagonalStore to be used for diagonal, bidiagonal and tridiagonal matrices. Replaces two different previous (package private) implementations.
* MatrixStore.Factory has a new method makeDiagonal(...)
* MatrixStore.LogicalBuilder has new implementations for the diagonal(), bidiagonal(boolean) and tridiagonal() methods.
* Some improvements to TransposedStore – more efficient use of the underlying store. Particular in the case when it is a RawStore.
* Some general cleanup/refactoring among the Eigenvalue related code.
* Added support for generalised eigenvalue problems.
* Fixed a bug in RawStore - visitRow/Column were interchanged
* Everything in org.ojalgo.matrix.store.operation has been moved to org.ojalgo.array.operation

#### org.ojalgo.optimisation

* Minor rounding/precision related change to how ExpressionsBasedModel receives the solution from the solver and then returns it. The `options.solution` property is now enforced.
* Internal refactoring of ConvexSolver and its subclasses. This includes changes in behaviour (handling of not-so-convex or otherwise difficult problems).
* The IntegerSolver now uses its own ForkJoinPool instance rather than the default `commonPool()`. The parallelism is derived from `OjAlgoUtils.ENVIRONMENT`.
* Internal refactoring related to the LinearSolver.Builder as well as the ConvexSolver.Builder.

#### org.ojalgo.random

* SampleSet can now swap in a `double[]`

#### org.ojalgo.scalar

* Implemented the power(int) method defined in Operation.Multiplication.

#### org.ojalgo.structure

* Additions to Structure2D.Logical (Moved definitions from MatrixStore.LogicalBuilder to here).
* Refactoring to Factory1D, Factory2D and FactoryAnyD – makeZero(...) is renamed make(...) and everything else is moved to a nested subinterface Dense.


## [47.2.0] – 2019-05-03

### Changed

#### org.ojalgo.data

* Renamed DataPreprocessors to DataProcessors, and added methods to create a covariance matrix from an SVD.

#### org.ojalgo.function

* Added a couple more utilities to MissingMath

#### org.ojalgo.matrix

* New LU decomposition implementation that is faster for small matrices.
* Fixed a bug related to solving equation systems and inverting matrices using SingularValue at certain matrix sizes.
* Renamed checkAndCompute to checkAndDecompose in the MatrixDecomposition.Hermitian interface
* Extended the MatrixStore.LogicalBuilder rows() and columns() methods to allow for negative indexes that refer to all zero rows/columns.
* New more efficient implementation of MatrixStore.LogicalBuilder.diagonal() data structure.
* The MatrixDecomposition interface now extends Structure2D so that you can get the size/shape of the original matrix.
* Additions to the MatrixDecomposition.RankRevealing interface. It is now possible to estimate the rank using a custom threshold. Also reworked all code related to estimating rank (in all implementations of that interface).

#### org.ojalgo.netio

* New funcion to generate random strings of ASCII characters, of specified length.

#### org.ojalgo.optimisation

* The optimisation model parameter scaling functionality has been tweaked to be more in line with the improved "rank revealing" of matrices.
* Minor improvements to ConvexSolver

#### org.ojalgo.structure

* The Stream2D interface now has methods operateOnColumns(...) and operateOnRows(...)


## [47.1.2] – 2019-04-23

### Changed

#### org.ojalgo.matrix

* Fixed bug related to LDL – stackoverflow if you called isSolvable() on some LDL instances.
* Various tweaks and cleanup with MatrixDecompostion:s


## [47.1.1] – 2019-04-12

### Changed

#### org.ojalgo.constant

* The stuff that was deprecated with v47.1.0 are now removed. (Keeping it around may cause problems with some IDE:s) Just update your import statements to org.ojalgo.function.constant rather than org.ojalgo.constant.

#### org.ojalgo.data

* Additions and improvements to DataPreprocessors, but they're now column oriented only.

#### org.ojalgo.matrix

* The ElementsConsumer interface has been renamed TransformableRegion. This is an intermediate definitions interface - subinterfaces and concrete implementations are not affected other than with their implements and extends declarations.

#### org.ojalgo.structure

* The interfaces Mutate1D.Transformable, Mutate2D.Transformable and MutateAnyD.Transformable introduced with v47.1.0 are removed again – bad idea.
* The Stream1D, Stream2D and StreamAnyD each got a new method named operateOnAny that takes a Transformation?D as input.
* The Mutate1D.ModifiableReceiver, Mutate2D.ModifiableReceiver and MutateAnyD.ModifiableReceiver each got a new method named modifyAny that takes a Transformation?D as input.


## [47.1.0] – 2019-04-09

### Changed

#### org.ojalgo.constant

* Everything in this package has been moved to org.ojalgo.function.constant

#### org.ojalgo.data

* New package that currently only contains an (also new) class DataPreprocessors. The intention is that this is where various data preprocessors utilities will go.

#### org.ojalgo.function

* New package org.ojalgo.function.special with currently 4 new classes: CombinatorialFunctions, ErrorFunction, GammaFunction and MissingMath.
* New package org.ojalgo.function.constant with constants related to doing basic maths on different types.
* Moved constants defined in various classes to the new org.ojalgo.function.constant package, and deprecated a ffew others that will be made private.

#### org.ojalgo.matrix

* Fixed a problem with one of the Eigenvalue implementations - it would check for symmetric/hermitian but then failed to make use of the results.
* Fixed a problem with some of the Eigenvalue implementations regarding ordering of the eigenvales/eigenvectors – they were not always ordered in descending absolut order (used to consider the sign).
* The interface TransformationMatrix has been removed. Partly this functionality has been revoked, and partially replaced by the new Transformation2D interface.

#### org.ojalgo.optimisation

* Refactoring of the presolvers. Among other things there are now presolvers that perform integer rounding. Previously existing presolvers have also been improved. There is also a new presolver that removes (obviously) redundant constraints.
* Modified what happends when you relax an integer model. Now the variables are kept as integer variables, but the model set a flag that it is relaxed. This means the presolvers can now make use of the integer property and thus perform better.
* The MPS file parser, MathProgSysModel, has been modified to not strictly use field index ranges, but instead more freely interpret whitespace as a delimiter. This is in line with commonly used MPS format extensions and allows ojAlgo to correctly parse/use a larger set of models. Further MathProgSysModel is no longer a model – it no longer implements Optimisation.Model. This should/will be a model file parser, and nothinng else.
* Major extension and refactoring of optimisation test cases. In particular the test cases in ojAlgo are now available to the various solver integration modules in ojAlgo-extensions.
* When adding a solver integration to ExpressionsBasedModel it is now possible to differentiate between preferred and fallback solvers - addIntegration() has been replaced with addPreferredSolver() and addFallbackSolver().
* Fixed a concurrency related problem with the sparse simplex solver.
* There is now an options 'options.sparse' that control if sparse/iterative solvers should be favoured over dense/direct solvers, or not.
* Tweaking of ConvexSolver internals – several small changes that in combination makes a big difference on some numerically challenging problems.
* Fixed a bug in ConvexSolver (ActiveSetSolver) that caused the `suffice` stopping conditions to not be considered since the state was set to APPROXIMATE raher than FEASIBLE.

#### org.ojalgo.random

* New Cauchy distribution RandomNumber.
* Partial implementation of Student's TDistribution
* Deprecated the RandomUtils class and moved its various methods to classes in the new org.ojalgo.function.special package.
* For continuous distributions the getProbability methods has been renamed getDensity.
* Minor performance improvement to SampleSet when calling getStandardDeviation() repeatedly.

#### org.ojalgo.structure

* New interfaces Transformation1D, Transformation2D and TransformationAnyD as well as Mutate1D.Transformable, Mutate2D.Transformable and MutateAnyD.Transformable. The Transformation?D interfaces are functional. With them you can write custom "mutators" that can be used on everything that implements the Transformable interfaces.
* The interfaces Access1D.Elements, Access1D.IndexOf, Access2D.Elements, Access2D.IndexOf, AccessAnyD.Elements and AccessAnyD.IndexOf have been deprecated.


#### org.ojalgo.type

* Additions to Stopwatch that make it easier to compare with time limits in various forms.


## [47.0.0] – 2018-12-16

### Changed

#### org.ojalgo.array

* SparseArray: It is now possible to visit the nonzero elements in an index range with a functional callback interface - you can have a lambda called by each nonzero element in a range.

#### org.ojalgo.matrix

* The BasicMatrix interface has been removed. More precisely it has been merged with the abstract package private class AbstractMatrix (and that class was then renamed BasicMatrix). Further all supporting (nested) classes and interfaces as well as the MatrixFactory class has been refactored in a similar way. The various matrix implementations (PrimitiveMatrix, ComplexMatrix, RationalMatrix and QuaternionMatrix) are now entirely self-contained - not generic in any way.
* The MatrixUtils class has been deprecated. Everything in it has been moved to other (different) places. No features or functionality are removed.
* Reshuffled the declarations in Mutate1D, Mutate2D and MutateAnyD. Nothing is removed. This only affects those doing low-level ojAlgo programming.
* Internal refactoring and performance improvements in SparseStore.

#### org.ojalgo.netio

* Complete rewrite of ResourceLocator. It can still be used the same was as before (roughly the same API for that) but it now also support more advanced usage. ResourceLocator has taken steps towards becoming an "http client" with support for cookies, sessions, requests and responses.
* New interface BasicLogger.Printable to be used by classes that need detailed debug logging.

#### org.ojalgo.optimisation

* Iterations and time limits are now enforced for the LP solver. (Only worked for the QP and MIP solver before.)
* Changed how the initial solution, extracted from the model and supplied to the solvers, is derived. This primarily affects the ConvexSolver.
* Changed how the ConvexSolver utilises the supplied initial solution to derive a better set of initially active inequalities.
* Modified the ConvexSolver/ActiveSetSolver behaviour regarding failed subproblems and iteration limits. Re-solving a failed subproblem now counts as a separate iteration, and iteration and time limits are enforced when determining if a failed subproblem should be re-solved or not.
* ConvexSolver validation is now slightly less strict. (Tiny negative eigenvalues are now allowed.)

#### org.ojalgo.series

* The ´resample´ methods have been removed from the NaturallySequenced interface. Resampling is now only possible with the CalendarDateSeries implementation.

#### org.ojalgo.type

* Refactoring to CalendarDate, CalendarDateUnit and CalendarDateDuration; simplified implementations that are better aligned with the java.time classes.


## [46.3.0] – 2018-10-12

### Changed

* Now builds a separate test-jar artefact. It contains a test utilities class that can be used by libraries that extend ojAlgo to help test some ojAlgo specific types.
* Added a main method in org.ojalgo.OjAlgoUtils that output info about the environment.

#### org.ojalgo.matrix

* The BasicMatrix interface has been deprecated!!! The various implementations will remain - all of them - but you should use those implementations directly/explicitly rather than the interface.


## [46.2.0] – 2018-10-04

### Changed

Nothing in ojAlgo implements Serializable - a few odd classes used to declare that they did, but serialization has never been supported.

#### org.ojalgo.matrix

* Fixed a bug related to transposing ElementConsumer/Supplier
* Performance improvements on some SparseStore operations

#### org.ojalgo.netio

* Refactored the various IDX.print(...) methods

#### org.ojalgo.structure

* Access1D no longer extends Iterable. To iterate over the elements use `elements()`.


## [46.1.0] – 2018-09-17

### Changed

#### org.ojalgo.ann

* Refactoring, tuning and improvements.

#### org.ojalgo.array

* The Array1D, Array2D and ArrayAnyD factories now have specific makeSparse(...) methods. Previously the makeZero(...) methods would automatically switch to creating sparse internals when it was large enough. Users were not aware of this (didn't want/need sparse) and performance was very poor.

#### org.ojalgo.netio

* New class IDX with utilities to read, parse and print contents of IDX-files. (To access the MNIST database of handwritten digits: http://yann.lecun.com/exdb/mnist/)

#### org.ojalgo.scalar

* A new abstract class, ExactDecimal, to help implement exact decimal numbers (fixed scale) as well as an example implementation Money.

#### org.ojalgo.structure (previously org.ojalgo.access)

* Added default methods to get all primitive number types from Access1D, Access2D and AccessAnyD and to somewhat modified the ones in AccessScalar: `byteValue(), shortValue(), intValue(), longValue(), floatValue(), doubleValue()`.
* AccessAnyD now has a method `matrices()` that return a `Iterable<MatrixView<N>>`. On a multi-dimensional data structure you can iterate over its 2D (sub)matrices. Useful when you have a 3-dimensional (or more) data structure that is actually a collection of 2D matrices.


## [46.0.0] – 2018-08-19

### Changed

#### org.ojalgo.access

* package renamed org.ojalgo.structure

#### org.ojalgo.ann

* Rudimentary support for ArtificialNeuralNetwork. You can build, train and use feedforward neural networks.

#### org.ojalgo.array

* The BasicArray.factory(?) method has been removed. It should never have been public
* New indexOf(array, value) utility methods in Raw1D

#### org.ojalgo.constant

* New constants in BigMath and PrimitiveMath: TWO_THIRDS and THREE_QUARTERS

#### org.ojalgo.function

* New PredicateFunction interface.
* New PlainUnary interface.

#### org.ojalgo.matrix

* BigMatrix, BigDenseStore and ComplexDenseStore are removed. They were deprecated before - now they're actually gone!
* Fixed bug: The getEigenpair(int) method of the Eigenvalue interface threw an ArrayIndexOutOfBoundsException
* Fixed a couple of issues related to calculating the nullspace (using QR or SVD decompositions).
* Revised the BasicMatrix interface - cleaned up some old deprecated stuff.
* BasicMatrix.Builder interface has been renamed BasicMatrix.PhysicalBuilder and its feature set extended. In addition there is now a BasicMatrix.LogicalBuilder. Stuff that should be done via those builders are now deprecated in the main, BasicMatrix, interface.
* The various BasicMatrix implementations now implement Access2D.Collectable which allows them to be more efficiently used with MatrixDecomposition:s.

#### org.ojalgo.netio

* Improvements/extensions to the BasicParser interface - it is now possible to skip a header line.

#### org.ojalgo.optimisation

* New interface UpdatableSolver that will contain a selection of optional methods to do in-place updates of solvers. Currently there is only one such method - fixVariable(?).
* New class ExpressionsBasedModel.Intermediate used by the IntegerSolver to exploit the capabilities exposed by UpdatableSolver.
* Many thing internal to the IntegerSolver (MIP solver) have changed. In general things are cached, re-used and updated in-place more than before.
* New option 'mip_defer' that control an aspect of the branch-and-bound algorithm in IntegerSolver. (Read its javadoc.)

#### org.ojalgo.scalar

* There is now a ComplexNumber.NaN constant.

#### org.ojalgo.structure (previously org.ojalgo.access)

* New default methods aggregateDiagonal(..), sliceDiagonal(...), visitDiagonal(...), fillDiagonal(...) and modifyDiagonal(..) targeting the main diagonal.
* New default methods in Factory2D to make it easier to create 2D-structures of single rows or columns.
* Mutate2D.BiModifiable now (again) extends Mutate2D.Modifiable and defines additional methods modifyMatchingInRows(...) and modifyMatchingInColumns(...)

#### org.ojalgo.type

* New class ObjectPool
* The generics type parameter of NumberContext.Enforceable changed from `<N extends Number>` to `<T>`.


## [45.1.0] – 2018-04-13

### Changed

#### org.ojalgo.access

* 3 new interfaces Structure2D.ReducibleTo1D, StructureAnyD.ReducibleTo1D and StructureAnyD.ReducibleTo2D
* Most (all) of the *AnyD interfaces got new or updated methods "set" methods: aggregateSet(...), visitSet(...), fillSet(..), modifySet(...), sliceSet(...)
* New functional interface Structure1D.LoopCallback to be used when iterating through a set of subloops.

#### org.ojalgo.array

* Array1D now implements Access1D.Aggregatable
* Array2D now implements Access2D.Aggregatable as well as Structure2D.ReducibleTo1D
* ArrayAnyD now implements AccessAnyD.Aggregatable as well as StructureAnyD.ReducibleTo1D and StructureAnyD.ReducibleTo2D
* Additions or cleanup of the various methods relating subsets of elements: aggregateSet(...), visitSet(...), fillSet(..), modifySet(...), sliceSet(...)
* Fixed a bug regarding fillAll(...) on a SparseArray - it didn't fill all, just the currently set (already existing) elements. (Performing fillAll on a SparseArray is stupid.)

#### org.ojalgo.function

* New aggregator function AVERAGE. (The Aggregator enum has a new member, and the AggregatorSet class has new method.)

#### org.ojalgo.matrix

* BasicMatrix now implements Structure2D.ReducibleTo1D
* Improved firstInRow/Column and limitOfRow/Column logic of AboveBelowStore and LeftRightStore.
* MatrixStore now implements Structure2D.ReducibleTo1D. The rows/columns are reduced to ElementSupplier:s so you can continue working with the numbers before supplying the elements to some ElementsConsumer.

#### org.ojalgo.netio

* New class LineSplittingParser - a very simple csv parser
* New class TableData - used to create a "table" of values that can then be exported to a csv file.


## [45.0.0] – 2018-03-30

### Changed

>Switced to using JUnit 5!

#### org.ojalgo.access

* Methods Access1D.wrapAccess1D(...) has been renamed simply Access1D.wrap(...), and similarly Access2D.wrap(...)
* Add/restore fillMatching(...) methods to the Mutate1D.Fillable interface.

#### org.ojalgo.function

* New method getLinearFactors() in MultiaryFunction.TwiceDifferentiable that returns the gradient at 0.

#### org.ojalgo.matrix

>> ###### **Major Changes!!**

* Changed matrix multiplication code to be better on modern hardware & JVM:s (not fully verified to be better).
* Everything "Big" has been deprecated: BigMatrix, BigDenseStore and everything else using BigDecimal elements. This is intended to be replaced by the new RationalNumber based implementations. 
* The ComplexNumber related code (all of it) has been refactored/generalised to handle any Scalar implementation (ComplexNumber, RationalNumber, Quaternion and any other future implementations). ComplexDenseStore has been deprecated and replaced by GenericDenseStore<ComplexNumber>.
* New interfaces MatrixTransformation and MatrixTransformation.Transformable - an attempt to unify matrix transformation implementations.
* New package org.ojalgo.matrix.geometry containing mutable 3D vector math classes.
* Fixed a bug with sparse matrix multiplication – in some cases the result was wrong (depending on the internal structure of the nonzero elements). The bug was actually in the SparseArray class.
* Fixed a bug with incorrect pseudoinverse in RawSingularValue.
* Fixed correct sign on determinant in QR decompositions

#### org.ojalgo.netio

* Added default methods to BasicLogger.Printer to simplify creating custom implementations.

#### org.ojalgo.optimisation

* Added new methods getUnadjustedLowerLimit() and getUnadjustedUpperLimit() to the Variable and Expression classes.
* Optimisation.Options now allow to specify a solver specific configurator method. This is useful when creating 3:d party solvers.
* Experimental support for Special Ordered Sets (SOS) in ExpressionsBasedModel.
* Variable:s are now ordered (compared using) their indices rather than their names.
* New variants of addVariable() and addExpression() methods that don't require you to supply an entity name. (A name is generated for you.)
* Changes to presolver and validation code. Fixed a whole bunch of problems, mainly related to rounding errors.
* Refactoring in the IntegerSolver. It should now be more memory efficient. Also, somewhat, modified the branching strategy.
* Added the possibility to get progress logging from the solvers (Currently only implemented in IntegerSolver). `model.options.progress(IntegerSolver.class);`

>> **API breaking!**

* The 'slack', 'problem', 'objective' and 'integer' properties of Optimisation.Options have been unified and replaced by a new property 'feasibility'.

#### org.ojalgo.random

* It is now possible to setSeed(long) on any RandomNumber and Random1D instance.

#### org.ojalgo.scalar

* RationalNumber has been re-implemented using primitive long and numerator and denominator (they used to be BigInteger).
* RationalNumber can now be instatiated from a double without creating any objevts other than the resulting RationalNumber instance. There is also an alternative implementation using rational approximation.

#### org.ojalgo.type

* New TypeContext implementation, named TemporalContext, to handle the classes from Java's new date time API. The older DateContext is still available.
* Bug fixed in a NumberContext factory method - getPercent(int,Locale)


## [44.0.0] – 2017-09-27

### Changed

#### org.ojalgo.access

* Deprecated AccessUtils. All the various utility methods have been moved to other places - primarily to the Acess1D, Access2D or AccessAnyD interfaces.
* The generic declaration of the IndexMapper interface has been changed from <T extends Comparable<? super T>> to simply <T>, and it is now a nested interface of Structure1D. Similarly there are now nested interfaces RowColumnMapper and ReferenceMapper in Structure2D and StructureAnyD respectively.
* The ElementView1D interface no longer extends ListIterator, but only Iterator. Instead it now extends Spliterator, and there is now a metod stream(boolean).
* The Mutate1D interface now declares a method reset() that should reset the mutable structure to an (all zeros) initial state.
* The Factory_D interfaces now has new variants of the makeZero(...) and makeFilled(...) methods that take Structure_D instances as input.

#### org.ojalgo.array

* Deprecated ArrayUtils. All the various utility methods have been moved to other places - primarily to the new Raw1D, Raw2D or RawAnyD classes. (ArrayUtils was split into Raw1D, Raw2D and RawAnyD.)

#### org.ojalgo.concurency

* The DivideAndConquer class now has a limit on the total number of threads it will create. (If a program had multiple threads that each invoked some multithreaded part of ojAlgo, the total number of threads would multiply and grow out of control.)

#### org.ojalgo.constant

* New constant GOLDEN_RATIO

#### org.ojalgo.finance

* This entire package has been moved to the ojAlgo-finance repository/project/artefact !!!
* Fixed a bug related to downloading historical data from Google and/or Yahoo Finance. When parsing the initial header line failed it was still translated to an actual data item with all zero values at a default/dummy date.
* New version of YahooSymbol that works with Yahoo Finance's changes. (They deliberately changed/broke the previously existing method of downloading.)

#### org.ojalgo.matrix

* org.ojalgo.matrix.task.TaskException has been removed and replaced with org.ojalgo.RecoverableCondition.
* org.ojalgo.matrix.MatrixError has been removed and replaced with various standard java.lang.RuntimeException subclasses instantiated via factory methods in org.ojalgo.ProgrammingError.
* New interface MatrixDecomposition.RankRevealing that define two methods: getRank() and isFullRank(). Several of the existing decompositions now implement this interface. This also caused a review of the implementations of the isSolvable() method of the MatrixDecomposition.Solver interface.
* Deprecated isSquareAndNotSingular() of the LU interface.
* Improved the multiplication code of SparseStore, ZeroStore and IdentityStore. In particular sparse matrix multiplication is now parallelised.
* Moved (deprecated) the matrix size thresholds methods from org.ojalgo.matrix.MatrixUtils to org.ojalgo.matrix.store.operation.MatrixOperation.
* Altered the RowsSupplier and ColumnsSupplier classes to (always) use sparse rows/columns.

#### org.ojalgo.netio

* The parse(String) method of BasicParser now declares to throw a RecoverableCondition. Failure to parse an individual line now just results in that line being skipped, and the process moves on to the next line.
* The ResourceLocator class has been refactored. Essentially it's a URI/URL builder and it is now implemented using the builder pattern. The changes are API breaking this class is rarely used directly. It is mostly used indirectly via GoogleSymbol and YahooSymbol, and those classes' public API:s are intact.

#### org.ojalgo.optimisation

* Improved the ConvexSolver to better handle cases with not positive definite and/or rank deficient covariance matrices (the quadratic term).
* Improved the IntegerSolver to better handle cases with problem parameters with significant magnitude differences (it affected the branching strategy unfavourably).
* Fixed a problem with debug printing in the IntegerSolver.
* Refactored major parts of the LinearSolver. Among other things the internal data structures are now sometimes sparse depending on the problem size (and sparsity).
* Refactored parts of the ConvexSolver to make better use of sparsity (and other things).

#### org.ojalgo.series

* The declaration of the resample(...) methods of CalendarDateSeries have been moved to the BasicSeries.NaturallySequenced interface with generalised signatures.
* The TimeSeriesBuilder can now be configured using CalendarDate.Resolution rather than only CalendarDateDuration.

#### org.ojalgo.type

* New Stopwatch class
* The CalendarDate constructor with a String argument/parameter now declares to throw a RecoverableCondition.
* NumberContext now has specific format(double) and format(long) methods, and now formats decimals with a variable number of fraction digits.
* NumberContext now has new compare(double,double) and compare(float,float) that are alternatives to the compare(...) methods in Double and Float - the only difference is that these return 0 when the two input args are ==.


## [43.0.0] – 2017-04-22

### Changed

* It is now possible to turn off warnings related to missing hardware profiles. Set a system property 'shut.up.ojAlgo' to anything, not null, and you won't see those warnings. (It would be must better if you contributed your hardware profile to ojAlgo.)

#### org.ojalgo.access

* Added new interfaces Access_D.Collectable to be used as super interfaces to the Stream_D interfaces, and used as input parameter type for matrix tasks and decompositions. The new interfaces interacts with the Mutate_D.Receiver interfaces and to some extent replaces the Supplier_D interfaces removed with v42.
* There's a new package private interface FactorySupplement used as a common superinterface to the Facory_D interfaces. It declares methods that give access to FunctionSet, AggregatorSet and Scalar.Factory instances - all 1D, 2D and AnyD factories now have this.
* The Stream_D interfaces got some new utility variants of operateOnAll-methods.
* New interfaces Mutate_D.Mixable that allow aggregating individual elements using the supplied binary "mixer" function.
* New interface Mutate1D.Sortable

#### org.ojalgo.array

* Addedd a Collector (Java 8 streams) that creates NumberList instances.
* Deprecated all previously existing factory methods in NumberList, LongToNumberMap and SparseArray. They are replaced with new alternatives (1 each). The new factories are configurable using builder pattern.
* SegmentedArray now package private. If you had explicit references to this class you have a compilation error. Switch to using some of the BasicArray pr DenseArray factories. If necessary things will be segmented for you.
* BufferArray is now abstract and has two package private subclasses DoubleBufferArray and FloatBufferArray. You instantiate them via factories in BufferArray.

#### org.ojalgo.finance

* Yahoo now requires to use https rather http when downloading historical financial data. Both YahooSymbol and GoogleSymbol now use https.

#### org.ojalgo.function

* Two new additions to FunctionSet - "logistic" and "logit".

#### org.ojalgo.matrix

* Replaced all usage of ElementsSupplier as input parameter type with Access2D.Collectable.
* Removed all public/external usage of DecompositionStore and replaced it with PhysicalStore.
* Moved (deprecated) the various equals(...) and reconstruct(...) methods from MatrixUtils to the respective matrix decomposition interfaces.
* Deprecated Eigenvalue.getEigenvector(int) and replaced it with Eigenvalue.getEigenpair(int)
* Various internal improvements to the Eigenvalue and Singular Value implementations.
* Deprecated the Schur decomposition. Eigenvalue decompositions are of course still supported. It's just Schur "on its own" that is deprecated.

#### org.ojalgo.netio

* The default scheme of ResourceLocator is changed from http to https.  

#### org.ojalgo.optimisation

* Extremely large (absolute value) lower/upper limits on variables or expressions are now treated as "no limit" (null).
* Extremely small (absolute value) lower/upper limits on variables or expressions are now treated as exactly 0.0.

#### org.ojalgo.random

* Fixed a minor bug in SampleSet - calculations would fail when repeatedly using SampleSet with empty sample sets 

#### org.ojalgo.series

* Various refactoring to the BasicSeries implementations. There is also a new nested interface BasicSeries.NaturallySequenced and some declarations have been moved there.
* Added the concept of an accumulator that may be used to accumulate the values of multiple put operations on the same key.
* New class CoordinatedSet - a set of coordinated (same start, end & resolution) series.

#### org.ojalgo.tensor

* New package!
* New interface Tensor with an implementation and a factory method. Not much you can do with this yet - functionality will be expanded slowly.

#### org.ojalgo.type

* Refactoring and additions to CalendarDate, CalendarDateUnit and CalendarDateDuration. Among other things there is a now an interface CalendarDate.Resolution that both CalendarDateUnit and CalendarDateDuration implement.


## [42.0.0] – 2017-02-03

### Changed

#### org.ojalgo.access

* Added a method aggregateRange(...) to Access1D.Aggregatable and created new Access2D.Aggregatable and AccessAnyD.Aggregatable interfaces. Their set of methods now match what's available in the Visitable interfaces.
* The interfaces Consumer1D, Consumer2D, ConsumerAnyD, Supplier1D, Supplier2D and SupplierAnyD have been removed - they didn't add anything.
* New interfaces Stream1D, Stream2D and StreamAnyD. They're not real streams, they don't (yet) extend BaseStream, but are intended to be stream-like.
* New interfaces Mutate1D.Receiver, Mutate2D.Receiver and MutateAnyD.Receiver. They extend Mutate_D and all their nested interfaces respectively and extends Consumer. In part these are replacements for the removed Consumer_D interfaces.
* The Callback_D interfaces as well as the passMatching(...) methods in the Access_D and Mutate_D interfaces are deprecated. They're replaced by a collection of messages named loop_(...) in the Structure_D interfaces.
* The method indexOfLargestInDiagonal(long,long) in Access2D.IndexOf is deprecated and replaced by indexOfLargestOnDiagonal(long). The new alternative is restricted to work on the main diagonal only, but the returned index is easier to understand and use.

#### org.ojalgo.array

* PrimitveArray is now abstract but has 2 instantiable subclasses Primitive32Array and Primitive64Array.
* New factory methods in Array1D, Array2D and ArrayAnyD that can delegate to any BasicMatrix factory that you supply.
* NumberList now implements Access1D.Visitable.
* New package org.ojalgo.array.blas: The aim is to refactor the code base so that methods matching BLAS functionality should be moved to this new package.
* The Unsafe/off-heap array implementations have been moved to the new ojAlgo-unsafe project (part of ojAlgo-extensions).

#### org.ojalgo.constant

* The POWERS_OF_2 in PrimitiveMath were incorrectly calculated - it's fixed now. At the same time the type was changed from int[] to long[] and the number of entries extended.

#### org.ojalgo.finance

* New constructor in SimplePortfolio taking an array of double as input (representing individual asset weights).

#### org.ojalgo.function

* The AggregatorSet class has a new nethod get(...) that will return the correct AggregatorFunction for a specified Aggregator instance.
* BigAggregator, ComplexAggregator, PrimitiveAggregator, QuaternionAggregator and RationalAggregator all now extend AggregatorSet.
* All previously existing variations of getXXFunction(...) in Aggregator has been deprecated and are replaced by by 1 new variant.
* Added a (NumberContext) enforce method to the FunctionSet class. It returns a UnaryFunction that enforces a NumberContext on the the function's input argument.
* Added andThen(...) and compose(...) methods, where applicable, to all BasicFunction subinterfaces.

#### org.ojalgo.matrix

* Both BasicMatrix and MatrixStore now extends the new Access2D.Aggregatable rather than Access1D.Aggregatable.
* Tweaking of several of the matrix decomposition (and task) implementations to improve numerical stability.
* New classes RowsSupplier and ColumnsSupplier that can be instantiated from the PhysicalStore.Factory:s (experimental design)

#### org.ojalgo.type

* All previously existing variations of getXXFunction(...) in NumberContext has been deprecated and are replaced by by 1 new variant that takes a FunctionSet as input.
* New class NativeMemory used as single point to allocate, read or write native memory.


## [41.0.0] – 2016-11-13

### Changed

#### org.ojalgo.access

* Moved the modifyMatching(...) methods from Mutate1D.Modifiable to a new interface Mutate1D.BiModifiable and only those classes that absolutely need to (to preserve existing functionality) implements that new interface. (Potentially api-breaking, but most likely not.) There are also corresponding interfaces Mutate2D.BiModifiable and MutateAnyD.BiModifiable
* The fillMatching(..) methods in Mutate1D.Fillable are deprecated.
* New (functional) interfaces Callback1D, Callback2D & CallbackAnyD. The Access?D and Mutate?D interfaces have also gotten new default methods named passMathing(...) that makes use of those new interfaces.
* There's a new method elements() in Access1D that returns an Iterable of ElementView1D - it allows to iterate over the instance's element "positions" without actually extracting the elements (unless you explicitly do so). There a corresponding method in Access2D. That interface also has methods rows() and columns() that does similar things but with rows and columns.
* ElementView1D now implements ListIterator rather than Iterator.
* New interface IndexMapper translates back and forth between an arbitrary "key" and a long index. Among other things used to implement the new (time) series classes.

#### org.ojalgo.array

* The previously package private class ArrayFactory is now public, and the static factory instances of BigArray, ComplexArray, PrimitiveArray, QuaternionArray and RationalArray are now also public.
* There's been additions to the ArrayFactory regarding how to create sparse or segmented arrays.
* New class NumberList - essentially an "ArrayList" backed by ojAlgo's BasicArray hierarchy of classes.
* New class LongToNumberMap - a long -> Number map - backed by ojAlgo's array classes.
* The previously deprecated methods searchAscending() and searchDescending() are now actually deleted, but the corresponding sortAscending() and sortDescending() got new implementation and had their deprecations removed instead.

#### org.ojalgo.finance

* There is now a new class EfficientFrontier to complement MarkowitzModel. If you don't want/need to be able set constraints and/or a target return/variance (like you can with the MarkowitzModel) then this new class is more efficient. Particular in regards to reusing partial results when calculating several points along the efficient frontier.
* The MarkowitzModel class now has a method optimiser() that return an instance of Optimiser that enable turning validation and debugging of the underlying optimization algorithm on/off.
* It is now possible to normalize any FinancePortfolio to the precision and scale (NumberContext) of your choice.
* The optional "cleaning" functionality of FinanceUtils' toCorrelations(...) and toVolatilities(...) methods have been improved.
* The DataSource class now implements the new org.ojalgo.netio.BasicParser interface.

#### org.ojalgo.function

* Additions to FunctionSet: atan2, cbrt, ceil, floor & rint.
* Made sure ojAlgo consistently (internally) uses PrimitiveFunction rather than java.lang.Math directly
* Improved the BigDecimal implementations of sqrt, root and the new cbrt functions.

#### org.ojalgo.matrix

* The resolve methods in IterativeSolverTask.SparseDelegate and MutableSolver, respectively, now return double rather than void or MatrixStore<Double> - they return the magnitude of the solution error.
* The method factory() in ElementsSupplier is renamed (deprecated) physical(). In MatrixStore you now have methods logical() and physical() returning MatrixStore.LogicalBuilder and PhysicalStore.Factory respectively.
* The nested class org.ojalgo.matrix.decomposition.DecompositionStore.HouseholderReference has been moved to the org.ojalgo.matrix.transformation package. Further it is now an interface rather than a class.
* The method copyToBuilder() in BasicMatrix has been renamed copy()
* It is now possible to extract complex valued eigenvectors (actually having ComplexNumber elements) using the getEigenvetors() and getEigenvetor(int) methods.
* The eigenvalue array returned by getEigenvalues() is no longer required to always be sorted. If it is sorted or not is indicated by the isSorted() method.
* The solve(...) methods in MatrixDecomposition.Solver are renamed getSolution(...)

#### org.ojalgo.netio

* The 2 classes BufferedInputStreamReader and BufferedOutputStreamWriter have been removed - they didn't do anything other/more than the usual streams and reader/writer classes.
* The getStreamReader() method of ResourceLocator now simply return a Reader rather than a BufferedReader.

#### org.ojalgo.optimisation

* The model parameter rescaling feature of ExpressionsBasedModel has been modified. Previously it didn't work very well with extremely large or small model parameters. Now with very large or small model parameters the rescaling functionality is turned off.
* Improved ExpressionsBasedModel's presolve functionality to identify and handle some cases of unbounded and/or uncorrelated variables.

#### org.ojalgo.random

* Added quartiles to SampleSet: getQuartile1(), getQuartile2(), getQuartile3() and getInterquartileRange()

#### org.ojalgo.series

* New builder instances in the BasicSeries interface. If you use them they will return implementations, new to v41, backed by array classes from the org.ojalgo.array package. It is now possible to use just about any date/time related class as a time series key.
* The methods getDataSeries() and getPrimitiveValues() are deprecated, both replaced by the new method getPrimitiveSeries(). Further the modifyAll(UnaryFunction) method is deprecated. You should do modifications on the series returned by getPrimitiveSeries().


## [40.0.0] – 2016-06-20

### Changed

#### org.ojalgo.access

* The Access1D.Builder, Access2D.Builder and AccessAnyD.Builder interfaces have been removed. The API of the BasicMatrix builder have changed slightly as a consequence of this.
* Many of the nested interfaces within Access1D, Access2D and AccessAnyD have been moved/renamed to be normal top level interfaces (declared in their own files). Typically this just means that import, implements and extends declarations within ojAlgo have changed.
* New nested interface Access1D.Aggregatable defining a new method N aggregateAll(Aggregator)
* New methods in Access1D - dot(Access1D<?>) and daxpy(double,Mutate1D) that bring basic (primitive double) linear algebra functionality to any/all Access1D implementation. Those are very useful operations to "always" have available.

#### org.ojalgo.array

* It is now possible to specify the initial capacity (the number of nonzeros) of a SparseArray, and a SparseArray can now be emptied / reset to all zeros.

#### org.ojalgo.finance

* Fixed a bug that erroneously set null constraints (unbounded) to zero with the MarkowitzModel and PortfolioMixer classes. This was mostly a problem when defining custom constraints on MarkowitzModel instances with lower limits only (the upper limit was erroneously set to 0.0).
* Fixed a bug in MarkowitzModel: Portfolios with shorting allowed and a target return/variance were not always calculated correctly.

#### org.ojalgo.function

* Deprecated org.ojalgo.function.aggregator.AggregationResults as well as the snapshot() method of org.ojalgo.function.aggregator.AggregationFunction.

#### org.ojalgo.matrix

* There is a new interface BasicMatrix.Builder that specifies the API of the BasicMatrix builder. Previously this was specified by Access2D.Builder that is now removed. The API changed in that the various builder methods no longer return the builder instance, but void.
* Improved performance with sparse and/or structured matrices - refactored existing multiplication code to actually make use of the firstInRow/Column and limitOfRow/Column methods. (Also fixed a couple of bugs related to implementations of those methods.)
* New package org.ojalgo.matrix.task.iterative containing interative equation system solvers such as JacobiSolver, GaussSeidelSolver and ConjugateGradientSolver.
* Two new methods in MatrixStore.Builder - limits(int,int) and offsets(int,int) - that lets you treat a subregion of a matrix as a (full) matrix.
* Changes to BasicMatrix:
  * The method add(int,int,Number) has been removed. BasicMatrix instances are immutable! Use copyToBuilder() instead.
  * It now extends as much as possible from the org.ojalgo.access and org.ojalgo.algebra packages. Similar methods previously defined in BasicMatrix are now replaced by whatever is in the superinterfaces resulting in some (api-breaking) signature changes.
  * As much as possible has been moved up from BasicMatrix to the various interfaces in org.ojalgo.access
  * Lots of things have been deprecated to enabled changes (possible) changes in the next version.
* New matrix decomposition factory instances. There is now one factory interface for each of the matrix decompositions as well as standard instantiations for "BIG", "COMPLEX" and "PRIMITIVE".
* The method MatrixStore#multiplyLeft(Access1D) has been renamed premultiply(Access1D), and the signature of MatrixStore#multiply(Access1D) has changed to multiply(MatrixStore). That's because it is now declared in org.ojalgo.algebra.Opreation.Multiplication.
* The class MatrixStore.Builder has been renamed MatrixStore.LogicalBuilder and the method in MatrixStore that returned an instance of it has been renamed from MatrixStore.builder() to MatrixStore.logical(). Further the MatrixStore.LogicalBuilder#build() method has been deprecated in favor of get().
* Fixed accuracy problem with the SVD and pseudoinverse for larger matrices, as well as a problem that could cause indefinite iterations with the SVD decomposition.
* The multiply-method where you supply a target (product) matrix has changed signature. It now returns void, and the target is an ElementsConsumer rather than a PhysicalStore.
* SparseStore now implements ElementsConsumer.
* The signature of the MatrixTask factory methods have changed. You now have to specify boolean flags for symmetric/hermitian and positive definite independantly.

#### org.ojalgo.optimisation

* Improved ConvexSolver. It now has much better performance with larger models. Current tests indicate a 50x speed improvement on a model of roughly 4k variables. With smaller models, < 100 variables, there's no significant difference between the old and new versions. These are internal changes only, but "significant". All unit test pass, but you should expect some changed behaviour.
* Some internal modifications to LinearSolver.

#### org.ojalgo.series

* Fixed bugs in CoordinationSet related to pruning and resampling when the series did not have specified names.

#### org.ojalgo.type

* Renamed Colour to ColourData
* Fixed a bug in CalendarDate#toSqlDate(). The Date was 70 years off. (The bug was introduced in v39.)
* CalendarDate now implements Temporal
* CalendarDateUnit now implements TemporalUnit
* CalendarDateDuration now implements TemporalAmount


## [39.0.0] – 2015-11-28

### Changed

>Everything (wasn't much) that made use of code outside the JRE profile "compact1" has been removed from ojAlgo. In terms of library functionality nothing has been removed, but there could be incompatibilities.

#### org.ojalgo.access

* Each of Access1D, Access2D and AccessAnyD now has a new nested interface Settable. The "set" methods of the Fillable interfaces are moved to Settable. Fillable and Modifiable now both extend Settable. The Settable interface declares "add" methods to complement "set". The Fillable interface now declares a set of "fillMatching" methods (moved up from some lower level implementations).
* The structure() method of AccessAnyD is deprecated and replaced with shape() that does exactly the same thing.
* The previously package private interfaces Structure1D, Structure2D and StructureAnyD are now public.
* New interfaces Access1D.Sliceable, Access2D.Sliceable and AccessAnyD.Sliceable.

#### org.ojalgo.algebra

* New package containing abstract algebra interfaces. This doesn't add any new features to ojAlgo. It just formalises and unifies some of the definitions. There are interfaces for Group, Ring, Field, VectorSpace...

#### org.ojalgo.matrix

* BasicMatrix now extends NormedVectorSpace from the new algebra package.
* MatrixStore now extends NormedVectorSpace from the new algebra package.
  * The method scale(Number) is deprecated and replaced by multiply(Number).
  * Refactoring of the MatrixStore hierarchy (their methods) due to api changes caused by the new algebra package. There are some new implementations and methods have been moved up and down the hierarchy.
* The methods isLowerLeftShaded() and isUpperRightShaded() of MatrixStore are deprecated and replaced by firstInRow(int), firstInColumn(int), limitOfRow(int) and limitOfColumn(int).
* The roles of the ElementsConsumer and ElementsSupplier interfaces have been greatly expanded. ElementsSupplier is now a superinterface to MatrixStore and it gained additional features. The ElementsConsumer largely defines the extensions to MatrixStore that make out the PhysicalStore interface.
* Refactoring of the MatrixDecompostion hierarchy (the org.ojalgo.matrix.decomposition package):
  * The capabilities of the various decompositions have been factored out to new/separate interfaces.
  * Integrated/merged the decomposition implementations from JAMA. (They've always been part of ojAlgo, but they were now moved, renamed and refactored.)
  * Performance tuning.
  * Matrix decompositions can now accept ElementsSupplier:s as input.

>API-breaking!

* All MatrixStore implementations (except the PhysicalStore implementations) are now package private. All constructors, factory methods or instances are now either removed och made package private.
* There is a new MatrixStore.Factory interface (as well as implementations for BIG, COMPLEX and PRIMITIVE). Through this new factory, and the previously existing, MatrixStore.Builder, you can access all features available with/by the various MatrixStore implementations.
* The MatrixStore.Builder has been cleaned of any methods that would require calculations on the matrix elements.
* The method multiplyLeft now returns an ElementsSupplier rather than a MatrixStore (a MatrixStore is an ElementsSupplier and you get a MatrixStore from any ElementsSupplier by calling "get")
* Some methods defined in the MatrixDecomposition interface now take an ElementsSupplier as input rather than an Access2D or a MatrixStore.
* MatrixStore now extends Access2D.Sliceable.

>API-breaking!

* There is a new SparseStore class (a MatrixStore implementation)
* Additions to MatrixUtils that get/copy the real, imaginary, modulus and argument properties of ComplexNumber matrices into primitive double valued matrices.

#### org.ojalgo.netio

* Refactoring related to BasicLogger and CharacterRing. In particular it is now possible (easier) to use a CharacterRing as a buffering BasicLogger.Printer.

#### org.ojalgo.optimisation

* Fixed a problem where you could get a NullPointerException with debug logging in the ConvexSolver.
* Changed the behaviour of the ConvexSolver (ActiveSetSolver) initialisation so that it now initiates to the optimal solution of the linear part of the model rather than any other feasible solution.
* Improved the presolve functionality of ExpressionsBasedModel to identify and eliminate some degenerate constraints.
* Modified how the initial solution extracted from the ExpressionsBasedModel is composed. Variable constraints are now considered to ensure feasibility. This primarily effects how the ConvexSolver is initialised.
* It is now possible to register solver integrations with the ExpressionsBasedModel. In other words; it is now possible to use third party solvers with ojAlgo's modelling tools. We've already built a basic CPLEX integration. The plan is to build a couple more and to release them all as Open Source here at GitHub.
* Optimisation.Options.slack changed from 14,8 to 10,8 and the logic for how the ExpressionsBasedModel validates a solution changed slightly.
* The ConvexSolver is now deterministic! For many years the ConvexSolver (previously QuadraticSolver) incorporated an element of randomness to break out of possible indefinite cycles. With numerically difficult problems this "feature" could result in varying solutions between subsequent solves - confusing. This strategy has now been replaced by a deterministic one that seems to work equally well, and being deterministic it is obviously much better.
* Slightly modified how the model parameters are scaled before being sent to a solver.
* ExpressionsBasedModel now accepts presolver plugins. The existing presolve functionality has been refactored to plugin implementations that can be individually turned on/off. It is possible for anyone to write an additional plugin.
* Refactoring and deprecations in ExpressionsBasedModel. Among other things all the get/set-linear/quadratic-factor methods are deprecated. There simply called get/set now.
* All select-methods are deprecated and (will be) replaced by the new methods variables(), constraints() and bounds().

>API-breaking!

* The entire package org.ojalgo.optimisation.system has been deleted. Its functionality is now provided by the various solvers directly.

>API-breaking!

#### org.ojalgo.random

* SampleSet now has a method getStandardScore(index) that returns the normalized standard score (z-score) of that particular sample.
* It is now possible to swap/change the underlying data of a SampleSet using the swap(Access1D<?>) method.

#### org.ojalgo.scalar
* Scalar now extends the interfaces from the new algebra package
* Improved numerical accuracy of complex number division

>API-breaking!

* All public constructors are removed in favour of factory methods.

>API-breaking!


## [38.0.0]

### Changed

The first version to require Java 8!


## [37.0.0] / [37.1.0] / [37.1.1]

### Changed

The last version to not require Java 8! (Targets Java 7) No real new features compared to v36.

v37.1 contains a backport of the optimisation packages from the upcoming v38 (as it was 2015-01-31). It has a number of important improvements. Apart from that it is identical to v37, and still targets Java 7.
