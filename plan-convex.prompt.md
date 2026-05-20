# Plan: Convex QP solver improvements

Legend: [x] done, [ ] pending, [~] partial/in progress

Reference: [.github/copilot-instructions.md](.github/copilot-instructions.md)

## Scope

Numerical stability improvements for `org.ojalgo.optimisation.convex` active-set
QP solvers (`QPESolver`, `DirectASS`, `IterativeASS`). Two related diagonal
modifications of the Schur complement:

- **Dual regularisation** (`ρ`) — adds a small constant to stabilise factorisation
- **Schur scaling** (Jacobi equilibration) — symmetric `d[i] = 1/√diag[i]` to
  improve conditioning

Both modify the Schur complement diagonal; they should be tested together to
avoid surprising interactions.

---

## 1. Dual regularisation

### Current state

Sources: [DualRegularisation.java](src/main/java/org/ojalgo/optimisation/convex/DualRegularisation.java),
[DualRegularisationStrategy.java](src/main/java/org/ojalgo/optimisation/convex/DualRegularisationStrategy.java),
[DualRegMetrics.java](src/main/java/org/ojalgo/optimisation/convex/DualRegMetrics.java).
Branch `regularization`, commit `8eb4c7a`. Disabled by default
(`-Dojalgo.dual.reg=true` to enable).

- [x] `DualRegularisationStrategy` interface with `compute(diagMax, diagMin, m,
  extendedPrecision, zeroQ)` signature
- [x] `DualRegularisation` factory: `DISABLED`, `BASELINE`; selection gated by
  system property `ojalgo.dual.reg`
- [x] `DualRegMetrics` thread-local diagnostics (gated by `ojalgo.dual.reg.metrics`)
- [x] Integration in `QPESolver.performIteration()` — applies `ρ` to negated
  Schur complement diagonal
- [x] Integration in `DirectASS.performIteration()` — same pattern
- [x] Integration in `IterativeASS.SchurComplementSolver.add()` and
  `resetActivator()` — incremental diagonal bumping

### Steps to ship

- [ ] **Decide enablement approach.** Currently gated via system property.
  Move to `ConvexSolver.Configuration.dualRegularisation(boolean)` for
  user-facing control and easier testing.
- [ ] **Add tests with regularisation enabled.** Parameterised variants of
  `CuteMarosMeszarosCase` / `ConvexProblems`; use `DualRegMetrics` to assert
  regularisation is applied when expected.
- [ ] **Apply regularisation to full-KKT fallback.**
  [BasePrimitiveSolver.solveFullKKT](src/main/java/org/ojalgo/optimisation/convex/BasePrimitiveSolver.java)
  does not currently add `ρ` to the KKT dual block. Add a call to
  `DualRegMetrics.recordKKT(rho)` and apply the same strategy for consistency.
- [ ] **Tune `BASELINE` formula.** Review the spread threshold (`1e8`) and cap
  (`1e-12`) against benchmark results; compare with OSQP / CLARABEL defaults.
  Document rationale in code comments.
- [ ] **Enable by default.** Once tests pass and impact is measured, flip the
  default or remove the system-property gate entirely.
- [ ] **CHANGELOG + Javadoc.** Document under `[Unreleased]`; add concise
  Javadoc on the public configuration method.

### Open design questions

- **Primal regularisation**: `AlternatingDirectionSolver` already uses a
  primal `σ` constant (`Configuration.SIGMA`). Should `DualRegularisation`
  also cover primal-side regularisation for the active-set solvers?
- **Extended-precision interaction**: `BASELINE` disables itself for
  `extendedPrecision` or `zeroQ`. Confirm this is desirable, or add separate
  override.
- **Performance benchmark**: JMH on ill-conditioned vs. well-conditioned
  problems to quantify overhead and benefit.

---

## 2. Schur complement scaling

### Current state

Source: [SchurScaling.java](src/main/java/org/ojalgo/optimisation/convex/SchurScaling.java),
[SchurScalingTest.java](src/test/java/org/ojalgo/optimisation/convex/SchurScalingTest.java).
Branch state: commit `6433faef`.

- [x] `SchurScaling` utility class with diagonal regularisation and
  symmetric scaling (`d[i] = 1/√diag[i]`)
- [x] Integrated into `DirectASS.performIteration()` with conditional
  activation based on diagonal spread (1e4–1e6 range)
- [x] Basic test comparing DirectASS / IterativeASS consistency
- [~] Partial scaffolding in `IterativeASS.SchurComplementSolver`:
  `setScaling()`, `getScaling()`, `applyScalingBackTo()` exist but scaling is
  **disabled** (`disableScaling = true`)

### Steps to complete

- [ ] **Wire scaling into `IterativeASS`.** In `resetActivator()` and
  `addConstraint()`, compute scale factors via `SchurScaling`, apply to
  equation bodies/RHS when adding rows to `SchurComplementSolver`, call
  `applyScalingBackTo()` after solving.
- [ ] **Configuration option.** Extend `ConvexSolver.Configuration` with
  `schurScaling(boolean)`; replace hard-coded `disableScaling` flags in
  both solver classes.
- [ ] **Expand test coverage.** Ill-conditioned problems, zero/tiny diagonal
  entries; verify scaling is actually applied when enabled.
- [ ] **Benchmark and tune thresholds.** Run convex benchmarks (`ojAlgo-
  mathematical-programming-benchmark`) with scaling on/off; adjust `REG_ABS`,
  `REG_REL`, and 1e4/1e6 spread heuristics.
- [ ] **CHANGELOG + Javadoc.** Update `[Unreleased]`; ensure Javadoc on
  `SchurScaling` is accurate and concise.

### Open design questions

- **Iterative preconditioner interaction**: combine with existing
  `SSORPreconditioner` or replace? Test both.
- **Default on or off**: conservative is opt-in via configuration until
  benchmarks confirm. Alternative: default-on only when condition number
  exceeds threshold.

---

## 3. Combined validation

Both features modify the Schur complement diagonal. They must be validated
together:

- [ ] Cross-product matrix: regularisation × scaling × extended-precision ×
  full-KKT-fallback
- [ ] Confirm metrics counters distinguish "regularised" from "scaled +
  regularised" cases
- [ ] Document the interaction order: scaling applied first, regularisation
  on the scaled diagonal? Or vice versa?

## Key files

- [QPESolver.java](src/main/java/org/ojalgo/optimisation/convex/QPESolver.java)
- [DirectASS.java](src/main/java/org/ojalgo/optimisation/convex/DirectASS.java)
- [IterativeASS.java](src/main/java/org/ojalgo/optimisation/convex/IterativeASS.java)
- [BasePrimitiveSolver.java](src/main/java/org/ojalgo/optimisation/convex/BasePrimitiveSolver.java)
- [ConvexSolver.java](src/main/java/org/ojalgo/optimisation/convex/ConvexSolver.java)
- [DualRegularisation.java](src/main/java/org/ojalgo/optimisation/convex/DualRegularisation.java)
- [SchurScaling.java](src/main/java/org/ojalgo/optimisation/convex/SchurScaling.java)

## Tests

- [SchurScalingTest.java](src/test/java/org/ojalgo/optimisation/convex/SchurScalingTest.java)
- `CuteMarosMeszarosCase`, `ConvexProblems`
