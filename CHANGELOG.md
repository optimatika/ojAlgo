# v47 [Not yet released; corresponds to changes in the `develop` branch]

## org.ojalgo.array

* SparseArray: It is now possible to visit the nonzero elements in an index range with a functional callback interface - you can have a lambda called by each nonzero element in a range.

## org.ojalgo.matrix

* The BasicMatrix interface has been removed. More precisely it has been merged with the abstract package private class AbstractMatrix (and that class was then renamed BasicMatrix). Further all supporting (nested) classes and interfaces as well as the MatrixFactory class has been refactored in a similar way. The various matrix implementations (PrimitiveMatrix, ComplexMatrix, RationalMatrix and QuaternionMatrix) are now entirely self-contained - not generic in any way.
* The MatrixUtils class has been deprecated. Everything in it has been moved to other (different) places. No features or functionality are removed.
* Reshuffled the declarations in Mutate1D, Mutate2D and MutateAnyD. Nothing is removed. This only affects those doing low-level ojAlgo programming.
* Internal refactoring and performance improvements in SparseStore.

## org.ojalgo.netio

* Complete rewrite of ResourceLocator. It can still be used the same was as before (roughly the same API for that) but it now also support more advanced usage. ResourceLocator has taken steps towards becoming an "http client" with support for cookies, sessions, requests and responses.
* New interface BasicLogger.Printable to be used by classes that need detailed debug logging.

## org.ojalgo.optimisation

* Iterations and time limits are now enforced for the LP solver. (Only worked for the QP and MIP solver before.)
* Changed how the initial solution, extracted from the model and supplied to the solvers, is derived. This primarily affects the ConvexSolver.
* Changed how the ConvexSolver utilises the supplied initial solution to derive a better set of initially active inequalities.
* Modified the ConvexSolver/ActiveSetSolver behaviour regarding failed subproblems and iteration limits. Re-solving a failed subproblem now counts as a separate iteration, and iteration and time limits are enforced when determining if a failed subproblem should be re-solved or not.
* ConvexSolver validation is now slightly less strict. (Tiny negative eigenvalues are now allowed.)

## org.ojalgo.series

* The ´resample´ methods have been removed from the NaturallySequenced interface. Resampling is now only possible with the CalendarDateSeries implementation.

## org.ojalgo.type

* Refactoring to CalendarDate, CalendarDateUnit and CalendarDateDuration; simplified implementations that are better aligned with the java.time classes.


# v46.3 2018-10-12

* Now builds a separate test-jar artefact. It contains a test utilities class that can be used by libraries that extend ojAlgo to help test some ojAlgo specific types.
* Added a main method in org.ojalgo.OjAlgoUtils that output info about the environment.

## org.ojalgo.matrix

* The BasicMatrix interface has been deprecated!!! The various implementations will remain - all of them - but you should use those implementations directly/explicitly rather than the interface.

# v46.2 2018-10-04

Nothing in ojAlgo implements Serializable - a few odd classes used to declare that they did, but serialization has never been supported.

## org.ojalgo.matrix

* Fixed a bug related to transposing ElementConsumer/Supplier
* Performance improvements on some SparseStore operations

## org.ojalgo.netio

* Refactored the various IDX.print(...) methods

## org.ojalgo.structure

* Access1D no longer extends Iterable. To iterate over the elements use `elements()`.

# v46.1 2018-09-17

## org.ojalgo.ann

* Refactoring, tuning and improvements.

## org.ojalgo.array

* The Array1D, Array2D and ArrayAnyD factories now have specific makeSparse(...) methods. Previously the makeZero(...) methods would automatically switch to creating sparse internals when it was large enough. Users were not aware of this (didn't want/need sparse) and performance was very poor.

## org.ojalgo.netio

* New class IDX with utilities to read, parse and print contents of IDX-files. (To access the MNIST database of handwritten digits: http://yann.lecun.com/exdb/mnist/)

## org.ojalgo.scalar

* A new abstract class, ExactDecimal, to help implement exact decimal numbers (fixed scale) as well as an example implementation Money.

## org.ojalgo.structure (previously org.ojalgo.access)

* Added default methods to get all primitive number types from Access1D, Access2D and AccessAnyD and to somewhat modified the ones in AccessScalar: `byteValue(), shortValue(), intValue(), longValue(), floatValue(), doubleValue()`.
* AccessAnyD now has a method `matrices()` that return a `Iterable<MatrixView<N>>`. On a multi-dimensional data structure you can iterate over its 2D (sub)matrices. Useful when you have a 3-dimensional (or more) data structure that is actually a collection of 2D matrices.

# v46 2018-08-19

## org.ojalgo.access

* package renamed org.ojalgo.structure

## org.ojalgo.ann

* Rudimentary support for ArtificialNeuralNetwork. You can build, train and use feedforward neural networks.

## org.ojalgo.array

* The BasicArray.factory(?) method has been removed. It should never have been public
* New indexOf(array, value) utility methods in Raw1D

## org.ojalgo.constant

* New constants in BigMath and PrimitiveMath: TWO_THIRDS and THREE_QUARTERS

## org.ojalgo.function

* New PredicateFunction interface.
* New PlainUnary interface.

## org.ojalgo.matrix

* BigMatrix, BigDenseStore and ComplexDenseStore are removed. They were deprecated before - now they're actually gone!
* Fixed bug: The getEigenpair(int) method of the Eigenvalue interface threw an ArrayIndexOutOfBoundsException
* Fixed a couple of issues related to calculating the nullspace (using QR or SVD decompositions).
* Revised the BasicMatrix interface - cleaned up some old deprecated stuff.
* BasicMatrix.Builder interface has been renamed BasicMatrix.PhysicalBuilder and its feature set extended. In addition there is now a BasicMatrix.LogicalBuilder. Stuff that should be done via those builders are now deprecated in the main, BasicMatrix, interface.
* The various BasicMatrix implementations now implement Access2D.Collectable which allows them to be more efficiently used with MatrixDecomposition:s.

## org.ojalgo.netio

* Improvements/extensions to the BasicParser interface - it is now possible to skip a header line.

## org.ojalgo.optimisation

* New interface UpdatableSolver that will contain a selection of optional methods to do in-place updates of solvers. Currently there is only one such method - fixVariable(?).
* New class ExpressionsBasedModel.Intermediate used by the IntegerSolver to exploit the capabilities exposed by UpdatableSolver.
* Many thing internal to the IntegerSolver (MIP solver) have changed. In general things are cached, re-used and updated in-place more than before.
* New option 'mip_defer' that control an aspect of the branch-and-bound algorithm in IntegerSolver. (Read its javadoc.)

## org.ojalgo.scalar

* There is now a ComplexNumber.NaN constant.

## org.ojalgo.structure (previously org.ojalgo.access)

* New default methods aggregateDiagonal(..), sliceDiagonal(...), visitDiagonal(...), fillDiagonal(...) and modifyDiagonal(..) targeting the main diagonal.
* New default methods in Factory2D to make it easier to create 2D-structures of single rows or columns.
* Mutate2D.BiModifiable now (again) extends Mutate2D.Modifiable and defines additional methods modifyMatchingInRows(...) and modifyMatchingInColumns(...)

## org.ojalgo.type

* New class ObjectPool
* The generics type parameter of NumberContext.Enforceable changed from `<N extends Number>` to `<T>`.
