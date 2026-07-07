# Contributing to ojAlgo

ojAlgo is a pure Java library for mathematics, linear algebra, and optimisation (LP, QP, MIP). Zero external dependencies. Targets Java 11+. Published to Maven Central as `org.ojalgo:ojalgo`.

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

## Coding Conventions

- **Performance is paramount.** Minimize allocations and object churn. Prefer primitives and ojAlgo's specialised data structures (array/matrix stores) over boxed types or generic collections in hot paths. Avoid streams/Optionals in critical loops. Mind cache locality; reuse buffers; avoid unnecessary copying; precompute sizes and strides. Choose algorithms with the right asymptotics; measure before/after using the existing JMH/bench harnesses. Be concurrency-aware: leverage `org.ojalgo.concurrent` utilities; avoid synchronized on hot paths; prefer immutability.
- **Identify and exploit existing ojAlgo features** before adding new code. Prefer enhancements that compose with current APIs and data structures instead of creating parallel abstractions.
- **Use descriptive, meaningful names.** Avoid cryptic abbreviations. Match the surrounding ojAlgo code style (formatting, ordering, idioms) when writing new code.
- **Naming convention for array/matrix types:** letter+digits encoding — R064=double, R032=float, C128=complex, Q128=rational, H256=quaternion, Z0xx=integer widths.
- **Binary compatibility:** Deprecate before removal. Keep changes small and focused.
- **No commented-out code.** Use `TODO` with an issue reference when needed.
- **Ensure exactly one trailing newline** in files. Keep formatting consistent with the existing codebase. Preserve imports order.

## Javadoc and Code Comments

- Use HTML for layout but keep it tidy and readable in source.
- Avoid unnecessary end tags like `</p>` and `</li>`.
- Avoid unnecessary formatting tags like `<b>` and `<i>`.
- Avoid HTML entities and symbols like `&lt;` `&gt;` `&nbsp;` or `&copy;`.
- Comment classes, methods, fields, and constants as proper Javadoc; avoid unattached line or block comments.
- Treat inline line/block comments as a last resort — when the code needs explanation, attach concise Javadoc to the relevant declaration instead.
- Prefer concise lists and paragraphs; link APIs with `{@link ...}` where it adds clarity.
- Avoid single-line block comments, and line comments on the same line as code.

## Testing

- Use `TestUtils` (not JUnit `Assertions`) for test assertions.
- Prefer deterministic tests. For numerics, use tolerances via `TestUtils` and avoid brittle equality checks.
- Keep tests fast and focused.

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
