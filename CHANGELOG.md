# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Added / Changed / Deprecated / Fixed / Removed / Security

## [Unreleased]

> Corresponds to changes in the `develop` branch since the last release

### Added

#### org.ojalgo.data

- New package `org.ojalgo.data.proximity` containing various distance and similarity calculation utilities.
- Spectral clustering: New `org.ojalgo.data.cluster.SpectralClusterer` implementing spectral clustering over feature vectors using an RBF similarity graph and the symmetric normalised Laplacian. Factory methods `FeatureBasedClusterer.newSpectral(int)` and `newSpectral(DistanceMeasure,int)` create instances.
- Clustering facade: New `org.ojalgo.data.cluster.FeatureBasedClusterer` facade with factory methods `newAutomatic(...)`, `newGreedy(...)`, `newKMeans(...)`, and `newSpectral(...)`. Adds a generic `cluster(Collection<T>, Function<T,float[]>)` that maps arbitrary items to feature vectors and returns clusters as `List<Map<T,float[]>>`.
- Automatic k selection: New `org.ojalgo.data.cluster.AutomaticClusterer` that derives thresholds from distance statistics to seed/refine clusters (k-means under the hood).

#### org.ojalgo.matrix

- Spectral decomposition: New `Eigenvalue.Spectral` interface (extends both `Eigenvalue` and `SingularValue`) and factory convenience `Eigenvalue.Factory#makeSpectral(int)` for normal (in particular symmetric / Hermitian) matrices, exposing a decomposition that can simultaneously be treated as an eigenvalue- and singular value decomposition. Includes `isSPD()` convenience check.
- Static utility helpers: `Eigenvalue.reconstruct(Eigenvalue)` plus `SingularValue.invert(...)`, `SingularValue.solve(...)` and `SingularValue.reconstruct(...)` centralise pseudoinverse / solve / reconstruction logic.
- New Quasi-Minimal Residual (QMR) iterative solver for general nonsymmetric square systems. Contributed by @Programmer-Magnus.

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

### Deprecated

#### org.ojalgo.matrix

- `SingularValue#getD()` deprecated; use `getS()` instead. (Existing code continues to work; plan to remove in a future major release.)

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

