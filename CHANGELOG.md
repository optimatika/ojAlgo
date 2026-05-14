# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Added / Changed / Deprecated / Fixed / Removed / Security

## [Unreleased]

> Corresponds to changes in the `develop` branch since the last release

### Added

#### org.ojalgo.optimisation

- New ADMM (OSQP-style) QP-solver named `AlternatingDirectionSolver`. The `ConvexSolver.Configuration` now lets you specify either the `ACTIVE_SET` or `ADMM` algorithm. If you don't specify which, simple logic will select for you. The new solver also works with iterative-refinement.
- New `Optimisation.Environment` class that holds solver integrations, presolvers, variable/expression factories, and 3rd-party configurators. Use `Optimisation.newEnvironment()` to create isolated configurations and `Environment.newModel()` as the model factory.
- New `FactorKKT` – a dedicated KKT factorisation helper used by the convex solvers.
- New `RuizScaling` – Ruiz equilibration for KKT/constraint systems in the convex solver pipeline.
- New `Equilibrator` abstract class for matrix equilibration (row and column scaling). Provides utility methods for clamping scaling factors to safe bounds and is used by scaling implementations across the optimisation pipeline.
- `UpdatableSolver` gained `getDualMultiplier(int)` and `getReducedGradient(int)` for querying dual variables and reduced gradients after a solve.
- `Optimisation.Result` now carries an optional reduced gradient via `getReducedGradient()` / `withReducedGradient(Supplier)`.
- `ExpressionsBasedModel.Simplifier`, `ExpressionAnalyser`, and `VariableAnalyser` are now `public`, enabling custom presolver-like hooks that plug into the standard presolve pipeline.
- New `ExpressionsBasedModel.getVariableValuesValidated()` – the validated/state-resolving counterpart of `getVariableValues()` (see the corresponding behaviour change below).
- New `Optimisation.Integration.prepareSolverCandidate(Result, Model)` – maps an optional kick-starter from model state to solver state, and may return `null`. Integrations whose solver ignores the kick-starter (e.g. the linear/simplex solver) return `null`, letting callers skip candidate extraction and conversion entirely.

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

### Deprecated

#### org.ojalgo.algebra

- `NormedVectorSpace.signum()` – use `normalised()` instead (scheduled for removal in v57).

### Removed

#### org.ojalgo.optimisation

- The `ExpressionsBasedModel.Validator` class and all functionality related to it. The related `ExpressionsBasedModel.setKnownSolution(...)` methods are also removed.
- `Presolvers.checkFeasibility(...)` static method removed.

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

