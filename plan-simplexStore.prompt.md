# Plan: SimplexStore implementations

Legend: [x] done, [ ] pending, [~] partial/in progress, [-] abandoned/archived

Reference: [.github/copilot-instructions.md](.github/copilot-instructions.md)

## Scope

Internal data structures and per-iteration hot path of `SimplexStore`
implementations: `RevisedStore`, `DenseTableau`, `SparseTableau`. Solver-level
orchestration lives in [plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md).
Sparse LU / basis representation work lives in [plan-sparseLU.prompt.md](plan-sparseLU.prompt.md).

## Status

`RevisedStore` Phase 1 (data structure rebuild + inner-loop optimisations)
is **complete and merged to develop**:

- `R064CSC` for solve-time column access; `RowsSupplier` only at build time
- `BasisRepresentation.reset(R064CSC, int[])` / `update(R064CSC, int[], int, int)`
- Hot-path `AXPY.invoke(double[], double, double[])` instead of virtual dispatch
- `System.arraycopy` for RHS → x
- `Arrays.fill` + direct write for unit-vector init
- Reduced-cost refresh on refactorisation; `r[]` intermediate eliminated
- Row-scatter `doBodyRow` for sparse z

Headline results (full Netlib): Dense **7.7%** faster, Sparse **16.6%** faster,
several previously timing-out models now solve. Big sparse wins: CYCLE -45%,
MAROS -37%, FIT1D -32%, KEN-07 -24%, BNL2 -22%, WOODW -22%. Newly solved:
PILOT, PILOT-JA, PILOTNOV, CRE-C.

This plan covers what's left in the store-internal layer.

---

## 1. Hyper-sparse PRICE consumer (RevisedStore side of plan-sparseLU §4)

The hyper-sparse btran in `SparseLU` produces a sparse lambda with its
nonzero set. `RevisedStore` consumes it to produce the pivot row.

- [ ] **CSR representation** of the constraint body — `R064CSR myCSR` built
  once in `prepareToIterate()`. CSR already exists in ojAlgo.
- [ ] **Sparse PRICE path** gated by btran nonzero count: for each nonzero
  $\lambda_i$, scatter $\lambda_i \cdot A_{i,*}$ into a size-`n` work buffer
  via CSR. Extract excluded entries from the buffer.
- [ ] **`double[] myTranspProduct`** size-n work buffer, allocated once,
  zeroed per sparse PRICE call (only touched entries need clearing).
- [ ] **Dense PRICE path retained**: when lambda is dense, keep current
  column-oriented `SparseArray.dot` code unchanged.
- [ ] No `colToExcl` mapping in the hot path — the row-scatter writes to a
  full buffer, extraction picks excluded entries.

Code locations:
- [RevisedStore.calculateDualDirection](src/main/java/org/ojalgo/optimisation/linear/RevisedStore.java) — calls btran then PRICE
- `RevisedStore.doExclTranspMult` — current column-oriented PRICE

The btran-side work is in [plan-sparseLU.prompt.md](plan-sparseLU.prompt.md) §4.1.

---

## 2. Identity-basis fast path

Source: `plan-revisedSimplexOptimisation.prompt.md` steps 1–2.

When the initial basis is the identity matrix:

- [ ] Skip `myInvBasis.reset(myBasis)` in `prepareToIterate()`
- [ ] Set `x` directly to RHS without `ftran()`
- [ ] Detect via `boolean myBasisIsIdentity` flag (cleared on first basis update)
- [ ] In `shiftColumn()` and `calculateIteration()`: when shift occurs and
  basis is still identity, update `x` directly instead of recomputing via
  `ftran()`

The decision to skip is solver-level and tracked in
[plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md) §G; this entry
covers the store-side implementation.

---

## 3. Shift refactor — store side

Source: `plan-unshiftTableau.prompt.md` Phase 1.1, Phase 2.

The store should own *how* shifts are applied; the solver owns *what*
(see [plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md) §F).

### 3.1 Complete `SimplexStore.shiftColumn()` — [ ]

`SimplexStore.shiftColumn()` is currently a near-empty stub with
commented-out bounds adjustment (lines 193–195).

- [ ] Uncomment and finalise bounds adjustment in `SimplexStore.shiftColumn()`
- [ ] Subclass overrides (`DenseTableau`, `SparseTableau`, `RevisedStore`)
  call `super.shiftColumn()` to trigger bounds update
- [ ] `RevisedStore.shiftColumn()` complexity: has additional `ftran` calls
  to update the basis inverse representation; verify still correct after
  relocation. Note: redundant ftran in `shiftColumn` was already eliminated
  in Phase 1 (the ftran in `calculateIteration`'s `shift != ZERO` branch
  is the single source).

### 3.2 Solution extraction — [ ]

- [ ] Verify `SimplexStore.extractSolution()` correctly unshifts values
  (line 222+). Non-basic variable values should use actual bounds, not
  shifted zeros.
- [ ] Verify `extractValue()` in each tableau:
  - `DenseTableau.extractValue()` — bound offset handling
  - `SparseTableau.extractValue()` — bound offset handling
  - `RevisedStore.extractValue()` — objective calculation

### 3.3 Validation

- [ ] Run linear solver tests: `RevisedSimplexSolverTest`, `CuteNetlibCase`,
  `MeszarosCase`, `BurkardtDatasetsMps`, `PrimalDualTest`
- [ ] Run MIP tests: `IntegerProblems`, `MIPLIBTheEasySet`, `KnapsackTest`
  (`IntegerSolver` relies on `SimplexSolver` relaxations)
- [ ] Benchmark comparison via `ojAlgo-mathematical-programming-benchmark`

---

## 4. Incremental phase-1 → phase-2 conversion (store side)

Source: `plan-incrementalPhase1Conversion.prompt.md`.

Solver-side timing fix is in [plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md) §E.2.
Store-side does the actual coefficient mutation.

### Per-store status

- [~] **DenseTableau** — partial impl in `calculateIteration` (lines 133–139):
  copies actual objective coefficient to phase-1 row for entering variable
  column. Currently after pivot — wrong; needs to move to pre-pivot hook.
- [ ] **SparseTableau** — impl is **commented out** (lines 200–206). Uncomment
  and connect to pre-pivot hook with correct timing.
- [ ] **RevisedStore** — no impl. Needs:
  - Switch `myPhase1Objective` coefficients to `myObjective` values for
    entering columns
  - Update `l` (dual variables) and `d` (reduced costs) consistently with
    the switch — care needed since `d` is maintained incrementally

### Test parity — [ ]

- [ ] Add tests verifying incremental conversion produces identical results
  to full-switch across all stores

---

## 5. Reduced-cost refresh and degeneracy interactions — done in Phase 1

Captured here for context:

- [x] **Full d[] recompute after refactorisation**. `BasisRepresentation.update()`
  returns `boolean` indicating refactorisation. When true,
  `RevisedStore.calculateIteration` calls `updateDualsAndReducedCosts()`
  instead of incremental AXPY.
- [x] **Eliminated `r[]` intermediate** — `d[je] = objData[col] - myConstraintsCSC.dot(col, l)`
  directly.
- [x] **Row-scatter `doBodyRow`** — `RowsSupplier` row-scatter for sparse z;
  `updateDualsAndReducedCosts` retains CSC column-dot for dense l.

Known regression: **FIT2D** — extreme degeneracy (m=26, ~10k variables).
More accurate d[] alters pivot path, leading to suboptimal vertex. Tagged
`@Tag("unstable")` pending anti-degeneracy work (driven from solver,
[plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md) §B).

S-path regressions (GANGES +24%, PEROLD +24%, PDS-02 +24%, BNL1 +23%)
persist after Phase 1. **Not** from `SparseDecomposition` parameters
(verified by reset to baseline values). Likely from step 3 reduced-cost
refresh altering pivot sequences. Stall recovery in solver (§B) is the
likely fix.

---

## 6. Stall infrastructure — store side — [x] mostly done

Source: `plan-revised-store.prompt.md` P1.

- [x] `SimplexStore.perturbDegenerateSolution(PERTURBATION_FACTOR, epsilon)`
  implemented for both `RevisedStore` and `SimplexTableau`. Pushes basic
  variables at bounds by `1e-10 * (1 + |x_i|)` toward interior.
- [x] `SimplexLU.countEtaNonzeros()` incremental `myEtaNonzeros` counter
  (O(1) per pivot vs O(k) before) for stall-triggered refactor decisions.

The recovery trigger (when to call these) is in
[plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md) §B.

---

## 7. SparseTableau / DenseTableau remaining items

Source: `plan-betterlu.prompt.md` §4.4.

### 7.1 Mixed zero-check patterns in DenseTableau — [ ]

`DenseTableau.doPivot()` mixes raw `!= ZERO` with tolerance-aware
`!PRECISION.isZero()`. Inconsistent — tiny but non-zero values pass the
raw check but may be numerically zero.

- [ ] Standardise on tolerance-aware checks throughout `doPivot()`
- [ ] Verify `SparseTableau` for similar mixed patterns

### 7.2 Build-time shift for tableaux — [-] archive

The "build-time variable shifting in `SimplexTableauSolver.build()` to
reduce constraints/slacks" idea applies only to `SimplexTableauSolver`
which is being superseded by `SimplexSolver`. Effort discarded; runtime
shifting via `SimplexSolver.shift()` is the path forward.

`SimplexTableau` (the storage class for `DenseTableau` / `SparseTableau`)
is still used by the new solver, so the shift work absorbed by §3 above
covers the relevant cases.

---

## 8. Phase 2 algorithmic items (still open)

These are in `plan-revised-store.prompt.md` Phase 2 but are mostly
solver-level concerns. Listed here for cross-reference; tracked in
[plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md):

- **P3 Dynamic refactorisation policy** — partly LU-side (residual monitoring,
  in [plan-sparseLU.prompt.md](plan-sparseLU.prompt.md)), partly solver-side
  (stall-triggered, in plan-simplexSolver §B.3)
- **P5 Scaling** — solver-orchestrated, see plan-simplexSolver §D
- **P6 Partial pricing / steepest-edge** — solver-side, see plan-simplexSolver §C
- **P10 Sparse pivot row computation** — exploit CSC structure for sparse
  pivot rows; alternative to full btran + matrix-vector. Could be a store
  optimisation if the solver passes a hint about expected sparsity. Open.
- **P11 Warm-start / basis reuse** — store-level support for resuming a
  solve with previous basis. Critical for MIP performance, low impact for
  standalone Netlib benchmarks.

---

## 9. Phase 1 — remaining benchmark item

- [ ] **Run benchmarks after each store-level change** — compare against
  develop baseline using Netlib suite. Phase 1 cumulative results captured
  in commit history; future changes need fresh baselines.

---

## Priority order

1. **§1 Hyper-sparse PRICE consumer** — pairs with plan-sparseLU §4 for
   biggest sparse-model win
2. **§3 Shift refactor (store side)** — unlocks the solver-side cleanup
3. **§7.1 DenseTableau zero-check normalisation** — small numerical hygiene
4. **§4 Incremental phase-1 (store side)** — only after solver-side timing
   fix is in place
5. **§2 Identity-basis fast path** — small win, opportunistic

## Key files

- [RevisedStore.java](src/main/java/org/ojalgo/optimisation/linear/RevisedStore.java)
- [SimplexStore.java](src/main/java/org/ojalgo/optimisation/linear/SimplexStore.java)
- [DenseTableau.java](src/main/java/org/ojalgo/optimisation/linear/DenseTableau.java)
- [SparseTableau.java](src/main/java/org/ojalgo/optimisation/linear/SparseTableau.java)
- [SimplexTableau.java](src/main/java/org/ojalgo/optimisation/linear/SimplexTableau.java)
- [LinearStructure.java](src/main/java/org/ojalgo/optimisation/linear/LinearStructure.java)

## Phase 1 archive — completed work for reference

Phase 1 of the revised-store rebuild delivered (already on develop):

| Step | Status | Impact |
|------|--------|--------|
| RowsSupplier + R064CSC in RevisedStore | [x] | foundation |
| BasisRepresentation reset/update on CSC | [x] | foundation |
| SparseLU `factor(R064CSC, int[])` direct | [x] | no Selection wrapper |
| Eliminate redundant ftran on shift | [x] | HIGH / LOW |
| Raw `double[]` AXPY in calculateIteration | [x] | MEDIUM / LOW |
| `System.arraycopy` for RHS → x | [x] | LOW-MEDIUM / TRIVIAL |
| `Arrays.fill` for z reset | [x] | LOW-MEDIUM / TRIVIAL |
| Reduced-cost refresh on refactorisation | [x] | HIGH / MEDIUM |
| Eliminated `r[]` intermediate | [x] | LOW / LOW |
| Row-scatter `doBodyRow` | [x] | MEDIUM / MEDIUM |
| Plain `double[]` working vectors | [x] | LOW / MEDIUM |

Headline: Dense 7.7% faster, Sparse 16.6% faster (excluding MAROS-R7);
PILOT, PILOT-JA, PILOTNOV, CRE-C newly solved.

Known sensitive models post-Phase 1: FIT2D (`@Tag("unstable")`), BNL1
(non-deterministic OPTIMAL/WRONG — numerical fragility, not a code bug).
Both should improve with stall detection / recovery once §B in
plan-simplexSolver lands.
