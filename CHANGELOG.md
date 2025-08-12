# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Added / Changed / Deprecated / Fixed / Removed / Security

## [Unreleased]

> Corresponds to changes in the `develop` branch since the last release

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

#### org.ojalgo.matrix

- For symmetric matrices the Eigenvalue decompositions are now always sorted. Previously it varied depending which implementation the factory returned. You should still always check `Eigenvalue.isOrdered()` – that a general rule for both eigenvalue and singular value decompositions.
- Cleaned up the `InvertibleFactor` interface. The 2-arg ftran/btran alternatives are removed.

#### org.ojalgo.optimisation

- Tweaking to various parts of `ConvexSolver`.
- Multiple changes and improvements to the `LinearSolver`. The number of industry standard netlib models that are solved (fast enough to be included) as junit tests cases increased from 45 to 78. The `LinearSolver` has actually been improved over several versions, but with this version we see a lot of that coming together and working quite well.

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

- New `ImageData` class that wraps a `java.awt.image.BufferedImage` and implements `MatrixStore`. Further it adds a few utility methods to simplify working with image data - convert to grey scale, re-sample (change size), separate the colour channels...

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
