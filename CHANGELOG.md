# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Added / Changed / Deprecated / Fixed / Removed / Security


## [Unreleased]

> Corresponds to changes in the `develop` branch since the last release

### Added

#### org.ojalgo.ann

- Support for `float`.
- Possible to "get" all individual parameters of the network
- Possibility to save trained networks to disk (and then later read them back)
- Separate between building, training and invoking the network - 3 different classes to do that.
- Possible to have several network invokers used in different threads.
- Support for `dropouts` when training the network.

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


## [37.0.0] / [37.1.0]

### Changed

The last version to not require Java 8! (Targets Java 7) No real new features compared to v36.

v37.1 contains a backport of the optimisation packages from the upcoming v38 (as it was 2015-01-31). It has a number of important improvements. Apart from that it is identical to v37, and still targets Java 7.
