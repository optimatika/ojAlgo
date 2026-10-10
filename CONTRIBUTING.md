# Contributing to ojAlgo

ojAlgo is a pure Java library for mathematics, linear algebra, and optimisation (LP, QP, MIP). Zero external dependencies. Targets Java 22+. Published to Maven Central as `org.ojalgo:ojalgo`.

## Build Commands

```bash
./mvnw compile                  # Compile
./mvnw test                     # Run tests (excludes @Tag("slow"), @Tag("unstable"), @Tag("network"))
./mvnw test -Dtest=ClassName    # Run a single test class
./mvnw test -Dtest=ClassName#methodName  # Run a single test method
./mvnw package -DskipTests      # Build JAR without tests
```

## Architecture

All source lives under `org.ojalgo` with these key packages:

- **`structure`** — Foundational interfaces (`Access1D/2D/AnyD`, `Mutate1D/2D`, `Structure1D/2D/AnyD`, `Factory1D/2D`) that define how data is accessed, mutated, and structured. Nearly everything implements these.
- **`array`** — Concrete 1D array implementations: dense (`ArrayR064`, `ArrayR032`), sparse (`SparseArray`), off-heap (`OffHeapR064`), and buffer-backed. Naming convention: `R064` = double, `R032` = float, `C128` = complex, `Q128` = rational, `H256` = quaternion, `Z0xx` = integer types.
- **`matrix`** — Matrix types and operations:
  - `MatrixR064`, `MatrixR032`, `MatrixC128`, etc. — user-facing immutable matrix types
  - `store/` — mutable matrix storage (`R064Store`, `GenericStore`, `SparseStore`, `RawStore`) plus logical/virtual stores (transposed, conjugated, sliced, composed)
  - `decomposition/` — LU, QR, Cholesky, SVD, Eigenvalue, LDL, Bidiagonal, Hessenberg, Tridiagonal with Dense/Raw/Sparse variants
  - `operation/` — low-level BLAS-like primitives
- **`optimisation`** — Mathematical programming:
  - `ExpressionsBasedModel` — the primary modelling API (variables + expressions/constraints)
  - `linear/` — Simplex solvers (primal, dual, phased, revised) with dense and sparse tableaux
  - `convex/` — QP solvers
  - `integer/` — Branch-and-bound MIP solver
- **`scalar`** — Number types: `ComplexNumber`, `RationalNumber`, `Quaternion`, `Quadruple` (double-double), `BigScalar`
- **`function`** — Mathematical functions, aggregators, constants (`PrimitiveMath`)
- **`concurrent`** — Threading utilities used internally
- **`data`** — Data science utilities (clustering, ANN)

## BLAS/LAPACK Operations

The low-level operations in `org.ojalgo.array.operation` and `org.ojalgo.matrix.operation` are moving towards the standard BLAS/LAPACK API. Each routine has a pure Java implementation and, optionally, a native one (FFM) used when available. The design assumes all of BLAS and the LAPACK routines ojAlgo needs; they are added incrementally, `double` first, other precisions as overloads later.

- One class per routine, named after it without the precision prefix: `GEMM`, `TRSM`, `DOT`, `GETRF`… Level 1 (vectors) belongs in `org.ojalgo.array.operation`; levels 2 and 3 and LAPACK – anything 2D – in `org.ojalgo.matrix.operation`.
- The canonical entry point is `invoke(...)` with the CBLAS argument order and BLAS names (`m`, `n`, `k`, `alpha`, `beta`, `lda`, `incX`), except:
  - always column-major, no layout argument;
  - each pointer becomes an array followed by an offset (`a, offsetA, lda`);
  - options are booleans (`transposeA`, `left`, `upper`, `unitDiagonal`), converted to the `CBLAS` constants (in `org.ojalgo.matrix.operation`) only when calling native code;
  - precision is chosen by overloading on the array type.
- Semantics follow reference BLAS: argument checks (`ArrayOperation.checkArgument`, `IllegalArgumentException`), the same quick returns, beta 0 means C is not read, alpha 0 means A and B are not read, and a negative increment walks the vector backwards with the offset still the lowest index accessed.
- `invoke` checks the arguments, quick-returns, and dispatches to `Native.invoke` (when available and above `NATIVE_THRESHOLD`) or to the package-private `invokeJava`. The pure Java implementation covers the full semantics and holds the general kernels (for `GEMM` one per transpose combination, each over a range of columns of C), multi-threaded above the routine's `THRESHOLD`.
- The call direction is from the ojAlgo-specific operations to the standard routines, never the reverse. `MultiplyNeither`, `SubstituteForwards`, `HouseholderLeft`… keep their special-case kernels (fixed small sizes, other number types) and call the standard routine's kernels for the general case. Such a change must not make their pure Java paths slower – check with JMH, and keep results bit-identical where possible.

Native code is used only when the JVM grants ojAlgo native access (`--enable-native-access=ojalgo` on the module path, `--enable-native-access=ALL-UNNAMED` on the class path), a working library is found, and the problem size passes the operation's `NATIVE_THRESHOLD`. Without native access ojAlgo never touches FFM.

- `org.ojalgo.array.operation.NativeLibrary` finds the library (`-Dojalgo.native.library=<file>[<path separator><file>]`, or auto-discovery: Accelerate on macOS, OpenBLAS/FlexiBLAS/MKL/BLIS/reference BLAS on Linux, OpenBLAS/MKL on Windows; `none` disables native code), checks it, and creates the downcall handles.
- Each routine class keeps its handle in a nested `Native` holder class, guarded by a `static final boolean NATIVE`, so FFM classes are never loaded unless used, and the disabled check folds away.
- BLAS is called through CBLAS with 32-bit integers (LP64). On macOS Accelerate's `$NEWLAPACK` symbols are preferred.
- Functions are linked as critical and passed the Java arrays in place (heap segments): zero-copy. The bounds are checked first (`NativeLibrary.ofArray`, `CBLAS.ofMatrix`), since native code would not notice. The JVM can't reach a safepoint during a critical call, so a long level 3 call delays garbage collection until it returns; that is accepted.
- Threading is left to the library's defaults.
- Thresholds are measured with JMH against the Java implementation.

## Coding Conventions

- **Performance is paramount.** Minimize allocations and object churn. Prefer primitives and ojAlgo's specialised data structures (array/matrix stores) over boxed types or generic collections in hot paths. Avoid streams/Optionals in critical loops. Mind cache locality; reuse buffers; avoid unnecessary copying; precompute sizes and strides. Choose algorithms with the right asymptotics; measure before/after using the existing JMH/bench harnesses. Be concurrency-aware: leverage `org.ojalgo.concurrent` utilities; avoid synchronized on hot paths; prefer immutability.
- **Identify and exploit existing ojAlgo features** before adding new code. Prefer enhancements that compose with current APIs and data structures instead of creating parallel abstractions.
- **Use descriptive, meaningful names.** Avoid cryptic abbreviations. Match the surrounding ojAlgo code style (formatting, ordering, idioms) when writing new code.
- **Naming convention for array/matrix types:** letter+digits encoding — R064=double, R032=float, C128=complex, Q128=rational, H256=quaternion, Z0xx=integer widths.
- **Binary compatibility:** Deprecate before removal. Keep changes small and focused.
- **No commented-out code.** Use `TODO` with an issue reference when needed.
- **Ensure exactly one trailing newline** in files. Keep formatting consistent with the existing codebase. Preserve imports order.

## Javadoc and Code Comments

- Javadoc is read in source far more than as generated HTML. Use HTML for layout but keep it tidy and readable in source — write it as close to Markdown as HTML allows.
- Write `<`, `>` and `&` as plain characters. Avoid HTML entities like `&lt;` `&gt;` `&amp;` or `&nbsp;` (the resulting javadoc warnings are accepted).
- Use `{@code ...}` for inline code (not `<code>`), and `<pre>{@code ... }</pre>` for code or other preformatted blocks.
- Put `<p>` on its own line between paragraphs — not before the first paragraph, at the end of a comment, or before a list or `<pre>` block.
- Avoid unnecessary end tags like `</p>` and `</li>`.
- Use a list or a new paragraph instead of `<br>` line breaks.
- Avoid unnecessary formatting tags like `<b>` and `<i>`; keep `<em>` for a single emphasised word. `<sup>` and `<sub>` are fine for math notation like `[A]<sup>T</sup>`.
- In `{@link ...}` and `@see` references use erased parameter types (`#method(Collection, Supplier)`) and `Outer.Nested` for nested types. External links in `@see` are quoted strings: `@see "https://..."`.
- Comment classes, methods, fields, and constants as proper Javadoc; avoid unattached line or block comments.
- Treat inline line/block comments as a last resort — when the code needs explanation, attach concise Javadoc to the relevant declaration instead.
- Prefer concise lists and paragraphs; link APIs with `{@link ...}` where it adds clarity.
- Avoid single-line block comments, and line comments on the same line as code.

## Testing

- Use `TestUtils` (not JUnit `Assertions`) for test assertions.
- Prefer deterministic tests. For numerics, use tolerances via `TestUtils` and avoid brittle equality checks.
- Keep tests fast and focused.
- `MIPLIBTheEasySet` mirrors the MIPLIB benchmark, where every solver runs with its default settings: no per-model configuration there. A test that the defaults do not solve reliably is tagged `slow` or `unstable` (both excluded from the default run). Tests on user-supplied models (`IntegerUserFiles`) use whatever configuration solves the model best.

### Diagnosing flaky solver tests

- Loop the model many times in one JVM. Results depend on static counters (cut names, hash orders), so a failing run index is stable across repetitions of the same loop.
- Failures that only appear in the full suite usually need CPU contention: start a dozen max-priority busy threads in the same JVM.
- For a hang, a daemon watchdog thread that dumps all `org.ojalgo` stack traces to a file and halts the JVM once a solve exceeds a limit gives a usable stack; a thread dump of a halted surefire fork is lost.
- To find where an optimum is lost, record a known optimal solution from a good run and log every event (polled, solved, pruned, discarded by validation, branched) at the nodes whose bounds contain it; also evaluate every cut of the node model at that point.
- Timing comparisons need a same-session baseline and the best of several runs; the noise on the MIPLIB easy set is about 10%, and some instances vary 10x between runs of the same code. A change that lands inside that noise is reverted, not kept.

## Logging and Diagnostics

- Use `BasicLogger` for output/logging. Never use `System.out` or `System.err`.
- Keep logs terse; avoid logging in tight loops unless gated.

## Optimisation Specifics

When working on LP, QP, or MIP solvers, compare design and behaviour with high-quality open-source solvers (HiGHS, GLOP, CLP, SCIP, CBC, OSQP, CLARABEL, qpOASES). Strive for numerically robust implementations (scaling, presolve, pivot rules, tolerance handling) with measured impact.

## Modules and Dependencies

- Keep module boundaries tight. Update `module-info.java` when adding/removing exports.
- Avoid adding new dependencies; prefer internal utilities. If unavoidable, justify and keep scope minimal.

## PR Checklist

- Evidence of performance impact (numbers or profiling) for performance-motivated changes.
- Adequate tests using `TestUtils`; updated Javadoc where API changes.
- `BasicLogger` used for diagnostic output; no `System.out`/`System.err`.
- `module-info.java` updated if necessary; no unintended transitive exposure.
