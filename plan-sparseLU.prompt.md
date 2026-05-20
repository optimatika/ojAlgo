# Plan: Sparse LU and basis update machinery

Legend: [x] done, [ ] pending, [~] partial/in progress, [-] abandoned/archived

Reference: [.github/copilot-instructions.md](.github/copilot-instructions.md)

## Scope

`SparseLU`, `SparseDecomposition`, `ForrestTomlinFactors`, `ProductFormInverse`,
`DenseDecomposition` (`Fletcher-Matthews`), and the experimental `MarkowitzLU` /
`ForestTomlinFactor` port from HiGHS. Covers factorisation quality, basis
updates, hyper-sparse solves, and storage formats.

Solver-driven concerns (stall-triggered refactor, scaling, pricing) live in
[plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md). Store-internal
hot-path work lives in [plan-simplexStore.prompt.md](plan-simplexStore.prompt.md).

## Status snapshot

`SparseLU` has the `R064CSC`-direct factor / update path from the
revised-store work. Forrest-Tomlin uses void+append U with pivot-lookup and
`RMatrixEta` (from `betterlu`). HiGHS-style `MarkowitzLU` exists alongside as
a Java port; not yet wired into production routing.

`BasisRepresentation.newInstance` currently routes by `dim ≤ 1000 && density > HALF`
to `ProductFormInverse`, otherwise `SparseDecomposition`. Phase A–C of the
revised-store work showed `DenseDecomposition` (Fletcher-Matthews) is
structurally unviable for `dim ≥ 1000`; production solvers (CLP, HiGHS, GLOP)
use sparse LU + FT for everything.

Current Netlib (full, 97 models, dual-S / dual-D vs ORTools, after FT update
gate tuning April 2026):

- Baseline: 171 OPTIMAL, 23 FAIL
- Current: 165 OPTIMAL, 29 FAIL on the experimental branch
- Total time (shared successes): 306.8s → 133.4s (2.3× faster)
- Big wins: BNL1-S fixed; MAROS-R7-S 23×; ~1.6× on 80BAU3B/GANGES/MAROS/NESM/PILOT4

Open numerical regressions (FT instability, all dual-S): BNL2, CYCLE, FORPLAN,
GREENBEB, PILOT-JA, PILOT-WE, PILOTNOV. Root cause analysis in §3.

---

## 1. SparseLU — features to adopt from HiGHS HFactor

### 1.1 Packed array storage for L and U — [ ]

Replace `RowsSupplier` (one `SparseArray` per row) with flat `int[]` /
`double[]` / `int[]` arrays. Eliminates per-row object headers, virtual
dispatch, and binary search in `SparseArray`.

- [ ] Convert L to flat packed arrays
- [ ] Convert U to flat packed arrays (or keep CSC; see §1.5)
- [ ] Implement forward/backward substitution directly on packed arrays
- [ ] Benchmark — `MarkowitzLU` ftran/btran is ~2× faster than `SparseLU` due
  to packed-array advantage

### 1.2 Markowitz pivoting — [ ]

Replace partial pivoting (largest magnitude in column) with Markowitz pivot
selection plus threshold pivoting for stability.

- [ ] Markowitz cost = (#unfilled in row − 1) · (#unfilled in col − 1)
- [ ] Threshold parameter to balance fill vs stability
- [ ] Count-linked lists for efficient pivot search

Benefits: sparser L/U → fewer entries to accumulate FT error (helps §3),
fewer ops per ftran/btran across the whole solve cycle.

### 1.3 Hyper-sparse DFS solves — [ ]

Two solve paths per direction:

- **Sparse sweep** (current): iterate all `n` steps
- **Hyper-sparse DFS**: visit only entries reachable from nonzero RHS positions

Critical for simplex where most ftran/btran calls have very sparse RHS.
Lambda density is 0.3–3% in >99% of iterations on ojAlgo Netlib runs.

- [ ] Implement DFS-based symbolic analysis (`solveHyper` equivalent)
- [ ] Density threshold to switch between full-sweep and hyper-sparse paths
- [ ] Use `HyperSparseVector.count` / density to pick path

### 1.4 Singleton pre-processing — [ ]

Add explicit singleton detection loop before kernel factorisation.

- [ ] Iteratively find row/column singletons; pivot on them without kernel structures
- [ ] Reduces kernel size; avoids building heavy structures for trivial pivots

### 1.5 U-factor CSC storage — [ ]

Currently each U row is a separate `SparseArray` with its own index/value
arrays. Cache-unfriendly; high allocation overhead during factorisation and
solve.

- [ ] Move U to CSC (column-oriented packed)
- [ ] Or hybrid CSC+CSR depending on direction (FTRAN vs BTRAN)
- [ ] Compare `ColumnsSupplier` / `RowsSupplier` mirroring vs flat arrays

Note: §1.1 + §1.5 are essentially the same change applied to two factors.

### 1.6 Dual L storage (column-wise + row-wise) — [ ]

Store L both column-wise and row-wise (LR) after factorisation.

- [ ] Column-wise L with scatter pattern for ftran
- [ ] Row-wise LR with scatter pattern for btran
- [ ] Retire single CSR representation for both directions

---

## 2. Forrest-Tomlin update — improvements

### 2.1 FT update — void+append with pivot-lookup — [x] done

Implemented in `betterlu`:

- Pivot-lookup table `myUPivotLookup` replaces physical row cycling
- New column stored at end of U
- `RMatrixEta` (ep-based) replaces `PermutationEta`
- Only U rows that have entries in the leaving row are touched (via UR mirror)

### 2.2 Row-pointer indirection (alternative) — [ ]

If §1.5 (CSC U) is not adopted: avoid physical element shifts in U rows by
maintaining an index mapping that allows logical reordering.

- [ ] Index map for logical row order
- [ ] Measure FT update cost reduction

Lower priority than §1.5; pick one.

### 2.3 Partial FT — [ ]

Only update rows of U that are actually affected by the pivot column (skip
rows with zero in the eta vector).

- [ ] Eta sparsity scan before update
- [ ] Skip rows with zero eta entry

The UR mirror from §2.1 provides the data structure; the skip is a one-line
conditional.

---

## 3. FT numerical stability — regression analysis

7 dual-S models regress (OPTIMAL → WRONG) with the FT update enabled: BNL2,
CYCLE, FORPLAN, GREENBEB, PILOT-JA, PILOT-WE, PILOTNOV. All dual-D unchanged.

**Error accumulation mechanism**: Each FT update stores intermediate values
in U computed through the growing R-factor chain. Small errors become
permanent U entries, corrupting subsequent btranU/ftranU. Feedback into the
next R-factor creates a positive feedback loop.

**Contributing factors**:
- Partial pivoting (largest in column) — numerically strong but produces
  denser L/U → more entries accumulate more error per solve
- `epPartial` computation (`btranU(e_p)`) is a full backward substitution
  that amplifies any existing U errors into the R-factor
- No iterative refinement or error monitoring

### Mitigations (priority order)

- [ ] **Numerical quality check after update** — recompute residual; trigger
  refactorisation if backward error exceeds threshold
- [ ] **Tighter refactorisation trigger for ill-conditioned models** —
  condition-number estimate from pivot-ratio history
- [ ] **Markowitz pivoting (§1.2)** — sparser factors = fewer entries to
  accumulate error
- [ ] **Iterative refinement** on ftran/btran results — expensive but effective

**Key observation**: FORPLAN is 84% dense with only 421 variables, yet still
fails. The FT error spiral overwhelms refactorisation triggers on
moderately-sized dense models too, not just ultra-sparse ill-conditioned ones.

### April 2026: FT update gate tuning — [x] done

- Diagnosed regressions/slowdowns (AGG2, GROW22, SCTAP1, STAIR, ISRAEL) after
  stricter FT gate (`NumberContext.of(4)`)
- Relaxed to `NumberContext.of(6)` (1e-6 relative) in `SparseLU.doUpdateColumn`
- Performance restored, no new failures in canary models (PEROLD, PILOT-WE)

Optionally try `NumberContext.of(5)` for marginal further tuning.

---

## 4. Hyper-sparse btran + row-oriented PRICE

Source: `plan-hyper-sparse-btran.prompt.md`. Targets the slowest Netlib
models (MAROS-R7, CYCLE, PILOT-WE) where lambda density is <5%.

### 4.1 Hyper-sparse btran (SparseLU) — [ ]

The btran $\lambda^T B = e_i^T$ starts from a single unit vector. Tracking
which entries become nonzero avoids touching zeros.

- [ ] **btranU**: maintain active set; process rows in order; scatter into
  new entries grows the active set
- [ ] **Eta chain**: skip `PermutationEta` factors whose pivot row is outside
  the active set
- [ ] **btranL**: process only rows reachable from the active set —
  precomputed dependency graph of L (built once at factorisation)
- [ ] **Density threshold** — fall back to dense btran if active set exceeds
  ~10–20% of m
- [ ] Return nonzero count/set alongside lambda values

### 4.2 Row-oriented PRICE (RevisedStore consumer) — see plan-simplexStore

The PRICE step ($a_j = \lambda^T A_j$) consumes the nonzero set produced by
4.1. Storage and call site live in `RevisedStore`. See
[plan-simplexStore.prompt.md](plan-simplexStore.prompt.md).

### 4.3 Hyper-sparse ftran (lower priority) — [ ]

Same principle, forward direction. Less impactful but follows naturally from
the btran infrastructure.

### Rejected / superseded approaches

- [-] **Unconditional CSR PRICE** — variant B gave 3% on dense models but
  regressed sparse 17–21%. `Arrays.fill` cost + full-matrix scatter dominate
  when columns are sparse.
- [-] **CSR with sparsity-switch (counting lambda nonzeros)** — O(m) counting
  scan adds overhead on every call; branch misprediction in the inner loop
  hurts.

**Conclusion**: row-oriented PRICE only works when btran itself produces the
nonzero set — no scanning.

---

## 5. Fill-reducing column ordering

Source: `plan-sparse-lu-improvements.prompt.md` Phase 3, §1.2 above.

Current ordering is "fullness sort" (last-index + count packed long key,
delivered in `revised-store` Phase 1). Production solvers use AMD/COLAMD.

| Feature | ojAlgo | HiGHS | CLP |
|---------|--------|-------|-----|
| Column ordering | Fullness sort | AMD/COLAMD | AMD |
| Symbolic factorisation | No | Yes | Yes |
| L/U storage | Row-oriented `SparseArray` | CSC | Packed arrays |
| FT update | Full row sweep* | Partial | Partial |
| Threshold pivoting | None (partial) | Markowitz | Markowitz |

*Now partial after `betterlu` UR-mirror work (§2.1).

### Tasks

- [ ] **Symbolic factorisation** — elimination tree + column counts;
  pre-allocate exact-size arrays; enables better ordering decisions
- [ ] **COLAMD** or approximate minimum degree (AMD) column ordering
  - `MinimumDegree` exists but only for symmetric input
- [ ] Compare fill-in and operation counts vs current fullness-sort on Netlib

---

## 6. Memory / lifecycle improvements (from MarkowitzLU comparison)

### 6.1 Pre-allocate with estimated sizes — [ ]

C++ HFactor pre-allocates generously in `setup()` using multiplied sizes,
avoiding nearly all dynamic growth during factorisation. Java currently grows
via `Arrays.copyOf` — copying potentially megabytes per call.

- [ ] Compute `basisMatrixLimitSize` from column-length distribution
- [ ] Allocate `mcIndex` / `mcValue` at `limitSize * MC_EXTRA_MULTIPLIER`
- [ ] Allocate `mrIndex` at `limitSize * MR_EXTRA_MULTIPLIER`
- [ ] Reserve L/U arrays at appropriate multipliers

### 6.2 Separate `setup()` from `build()` — reuse memory across calls — [ ]

In LP simplex `build()` is called hundreds of times — fresh allocation each
time is significant overhead.

- [ ] Move dimension-dependent allocation to `setup()` / constructor
- [ ] In `build()`, reset/clear arrays without reallocating (mirror C++
  `luClear()`)
- [ ] Keep `mwzArray`, `mwzMark`, `mwzIndex`, link-list arrays, pivot arrays
  as persistent fields

### 6.3 Reuse work array in ftran/btran — [ ]

- [ ] Persistent `double[] work` field
- [ ] Zero only after use; don't allocate fresh per call

---

## 7. MarkowitzLU specific gaps (HFactor port)

`MarkowitzLU` ftran/btran is ~2× faster than `SparseLU` (packed advantage)
but `MarkowitzLU` decompose is 37× faster at 500/0.5% density and 6× slower
at 2000/2% density. The Java port is missing several HFactor features.

### 7.1 Implement proper Forest-Tomlin updates (not PFI) — [ ]

`ForestTomlinFactor` currently stores the entire FTRAN'd entering column
(`aq`) as a product-form eta — this is PFI, not FT. The `ep` parameter is
accepted but never used. A proper FT update should:

- [ ] Modify U to replace the exiting column with the entering column
- [ ] Use a pivot-lookup table so "cycling" is a table update, not data movement
- [ ] Store R-matrix entries from `ep` (scaled by old pivot), not from `aq`
- [ ] R-matrix entries are much smaller (sparse `ep`) vs full-column `aq`

Single most impactful change for `MarkowitzLU`.

### 7.2 `colStoreN` / `mcCountN` dual-end column storage — [ ]

- [ ] Add `mcCountN` array tracking non-active entry count per column
- [ ] Add `colStoreN(col, row, value)` that stores at the back end
- [ ] In elimination, use `colDelete` + `colStoreN` to move pivot-row entries
  to non-active end
- [ ] Read U-row entries directly from non-active section (no `findColEntry`
  scan in step 4 / row-search pivot selection)

The C++ never linear-scans to retrieve pivot-row values — stores them
in-place during elimination.

### 7.3 Delete pivot column from `mr` during L-column processing — [ ]

- [ ] During step 2.2 (store L column), call `rowDelete(pivotCol, row)` inline
- [ ] Remove the separate post-elimination cleanup pass (lines ~600–614)
- [ ] Remove `rowActive` / `colActive` arrays if no longer needed

### 7.4 Singular pivot deferral — [ ]

When a pivot is below tolerance, skip it and continue searching.

- [ ] Increment `nwork` instead of accepting tiny pivot
- [ ] `zeroCol`-style cleanup for columns with rejected pivots
- [ ] Only record rank deficiency after exhausting all candidates

---

## 8. Deferred optimisation: ep reuse across BTRAN and FT update

Source: `plan-betterlu.prompt.md` §4.

### Background

In revised simplex, every pivot needs:
1. Tableau row for leaving variable: full `e_p^T B^{-1}` (full BTRAN result)
2. R-factor entry for FT update: `U^{-T} e_p` (intermediate, before R-factors and L^T)

These are two views of the same computation rooted in the same unit vector
`e_p`. HiGHS does one `BtranForUpdate` call producing both via two stages.

### Current ojAlgo state

`RevisedStore.doBodyRow` does a full BTRAN. `SparseLU.doUpdateColumn`
recomputes the intermediate from scratch via a second `btranU(epPartial)`.

This second solve is unnecessary if the intermediate had been preserved.

### Fix

- [ ] Split `SparseLU.btran` into two stages: `btranU` + `btranFactorsAndL`,
  or add a dedicated update-oriented path returning both values
- [ ] Replace generic `BasisRepresentation.btran` in `RevisedStore.doBodyRow`
  with an update-specific call that produces the intermediate (for
  `doUpdateColumn`) and the full row (for the tableau)
- [ ] Thread the intermediate into `BasisRepresentation.update` so
  `doUpdateColumn` skips its internal `btranU`

Scope: `SparseLU` and `SparseDecomposition` only; other
`BasisRepresentation` impls do not need the intermediate.

### Prerequisites

- Stable FT numerics (§3) — recycling wrong intermediates into bad R-factors
  is worse than recomputing
- Markowitz pivoting (§1.2) — sparser U makes each `btranU` cheaper; bigger
  relative gain from eliminating one

---

## 9. BasisRepresentation routing

Source: `plan-revised-store.prompt.md` Phase A–C.

**Conclusion** (after thorough study): production solvers use sparse LU +
Forrest-Tomlin for everything. PFI is only competitive for very small bases
(`dim ≤ ~200`).

Current routing: `dim ≤ 1000 && density > HALF` → PFI, else Sparse. The
density threshold rarely triggers in practice (internal density of constraint
body including slacks is much lower than CSV density).

- [ ] **Cleanup**: simplify `BasisRepresentation.newInstance` to dimension-only
  (`dim ≤ 200` → PFI, else Sparse). Drop the density check entirely.
- [-] DenseDecomposition (Fletcher-Matthews) is unviable for `dim ≥ 1000`
  due to structural rejection rate — preserved as code, not used in routing.

### Phase A–C results (kept for reference)

`FletcherMatthews.SAFE` tolerance has negligible effect on rejection at
`dim ≥ 1000`; the problem is structural (FM accumulates O(n²) rounding per
column replacement). Preserved infrastructure: `FletcherMatthews.update()`
accepts `NumberContext safe`; `DenseLU.setUpdateTolerance()` wired up;
`DenseDecomposition` has `getRejectionCount()` / `resetRejectionCount()`.

PFI solve cost growth is gentle (~15% over 200 updates at dim=1000). An
`UPDATES_LIMIT` of 100–200 is safe — marginal solve overhead is small
relative to one O(n³) reset.

---

## 10. Phase 1: Profile and measure

Always-applicable groundwork.

- [ ] Profile `doCyclicFT` under async-profiler on large Netlib (MAROS-R7,
  80BAU3B, PILOT87, BNL2)
- [ ] Time fraction: FT update vs eta chain traversal vs btran vs ftran vs
  full refactorisation
- [ ] Identify whether FT update or eta traversal dominates between
  refactorisations
- [ ] Fill-in growth curves: factor nnz vs pivot count for representative
  models

---

## Priority order

1. **§4 Hyper-sparse btran + PRICE** — direct attack on slow-Netlib models;
   moderate effort, high payoff
2. **§3 FT numerical stability** — must-fix to merge `betterlu` work; pursue
   alongside §1.2 Markowitz
3. **§1.2 Markowitz pivoting** — sparser factors help §3 *and* speed
4. **§1.1 + §1.5 Packed array storage** — 2× ftran/btran win
5. **§1.3 Hyper-sparse DFS solves** — pairs with §4
6. **§5 Fill-reducing ordering (COLAMD)** — large but well-known territory
7. **§9 Routing cleanup** — one-line simplification
8. **§6 Memory / lifecycle** — applies to whichever LU implementation wins
9. **§8 ep reuse** — small per-pivot win, gated on §3 stability
10. **§10 Profiling** — do continuously

## Key files

- [SparseLU.java](src/main/java/org/ojalgo/matrix/decomposition/SparseLU.java)
- [SparseDecomposition.java](src/main/java/org/ojalgo/optimisation/linear/SparseDecomposition.java)
- [DenseDecomposition.java](src/main/java/org/ojalgo/optimisation/linear/DenseDecomposition.java)
- [ProductFormInverse.java](src/main/java/org/ojalgo/optimisation/linear/ProductFormInverse.java)
- [BasisRepresentation.java](src/main/java/org/ojalgo/optimisation/linear/BasisRepresentation.java)
- [ForrestTomlinFactors.java](src/main/java/org/ojalgo/matrix/decomposition/ForrestTomlinFactors.java)
- `MarkowitzLU.java`, `ForestTomlinFactor.java`, `HyperSparseVector.java`
  (betterlu branch)

## Archived items (from `plan-sparseLuPerformance.prompt.md`)

- [x] Column sorting by fullness (last-index + count) via packed `long` key
  sort with `SortAll.sort(long[], int[])`. Full Netlib: 28 wins, 3 losses,
  geo-mean 1.14×, aggregate 1.18×.
- [x] `Pivot.setModified(boolean)` / `Pivot.isModified()` guards skip
  permutation overhead when sort is identity.
- [x] `ColumnsSupplier.Selection` made `Sliceable` — no dense materialisation
  in `SparseLU.cast()`.
- [x] Direct primitive gather in `updateDualsAndReducedCosts` —
  `l.data[ji] = objData[included[ji]]`, eliminating `RowsStore` wrapper.
- [-] Composed single-permutation `ForrestTomlinFactors` — reverted; sparse
  regression. Per-update path is current.
- [-] Markowitz-threshold pivot selection — reverted; hurt benchmarks.
  Revisit under §1.2 with proper threshold tuning.

Note: skip-sorting heuristic abandoned — `needsSorting()` returns true on
all test models. Identity-permutation guards (`Pivot.isModified`) eliminate
overhead instead.
