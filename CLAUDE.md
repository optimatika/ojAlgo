# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

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

- **Performance is paramount.** Minimize allocations, prefer primitives over boxed types, avoid streams/Optionals in hot paths. Measure with JMH before/after.
- **Use `TestUtils`** (not JUnit `Assertions`) for test assertions. Use tolerances for numeric comparisons.
- **Use `BasicLogger`** for diagnostic output, never `System.out`/`System.err`.
- **No commented-out code.** Use `TODO` with issue reference if needed.
- **Javadoc style:** Keep tidy HTML, avoid unnecessary tags (`</p>`, `</li>`, `<b>`, `<i>`), avoid HTML entities. Prefer Javadoc on declarations over inline comments.
- **Naming convention for array/matrix types:** letter+digits encoding — R064=double, R032=float, C128=complex, Q128=rational, H256=quaternion, Z0xx=integer widths.
- **Binary compatibility:** Deprecate before removal. Keep changes small and focused.
- **Ensure exactly one trailing newline** in files.
- For optimisation work, compare with reference solvers (HiGHS, GLOP, CLP, SCIP, CBC, OSQP, CLARABEL).
