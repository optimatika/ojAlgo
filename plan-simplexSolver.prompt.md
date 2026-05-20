# Plan: SimplexSolver class improvements

Legend: [x] done, [ ] pending, [~] partial/in progress

Reference: [.github/copilot-instructions.md](.github/copilot-instructions.md)

## Scope

Improvements to `SimplexSolver` and its direct subclasses
(`PhasedSimplexSolver`, `DualSimplexSolver`, `PrimalSimplexSolver`,
`ClassicSimplexSolver`) that are:

- **Independent of `SimplexStore` choice** — apply equally to `RevisedStore`,
  `DenseTableau`, `SparseTableau`.
- **Not part of the `SimplexTableauSolver` path** — that older solver is being
  superseded by `SimplexSolver` and is out of scope here.

Store-internal work lives in [plan-simplexStore.prompt.md](plan-simplexStore.prompt.md).
Sparse LU / basis representation work lives in [plan-sparseLU.prompt.md](plan-sparseLU.prompt.md).

## Class layout

```
LinearSolver
 └── SimplexSolver (abstract)         <- this plan
      ├── PhasedSimplexSolver         (default; dual phase-1, primal phase-2)
      ├── DualSimplexSolver
      ├── PrimalSimplexSolver
      └── ClassicSimplexSolver        (incomplete; see §E.3)
```

`SimplexSolver` owns: setup, iteration loop, ratio tests
(`getDualExitCandidate`, `getPrimalEnterCandidate`), pivot selection, stall
detection, phase switching, shift/bound-flip orchestration, solution extraction.

---

## A. Numerical / tolerance issues

Source: `plan-betterlu.prompt.md` §5.

### A.1 Hardcoded pivot threshold `1e-10` in candidate selection — [ ]

`SimplexSolver.getDualExitCandidate()` and `getPrimalEnterCandidate()` use
`magnitude > 1e-10` as a fixed absolute threshold for candidate selection.
This is not data-scaled — it fails on problems with very large or very small
coefficients.

- [ ] Replace with data-scaled or relative threshold (e.g. relative to row/column norm)
- [ ] Survey ill-conditioned Netlib models for sensitivity

### A.2 Weak `PIVOT` tolerance — [ ]

`SimplexSolver.PIVOT = NumberContext.of(6)` allows only 6 significant digits.
By comparison, `SimplexTableauSolver.PIVOT = NumberContext.of(12, 8)` uses 12.

The revised simplex solver is the default path; the low precision allows
near-zero pivots that amplify errors through ftran/btran.

- [ ] Tighten `PIVOT` (try `NumberContext.of(10)` or `NumberContext.of(12)`)
- [ ] Re-run full Netlib suite to check for regressions

### A.3 No partial pivoting in ratio test — [ ]

`SimplexSolver`'s ratio test selects the smallest ratio but does not prefer
the row with the largest absolute pivot when ratios are close.
`SimplexTableauSolver` has explicit `maxPivot` selection logic; `SimplexSolver`
lacks this.

Small pivots amplify rounding error in every subsequent ftran/btran.

- [ ] Add tie-breaking by `|pivot|` in `getDualExitCandidate` and `getPrimalEnterCandidate`
- [ ] Tolerance for "close ratios" should be tied to A.1/A.2 above

---

## B. Stall detection and anti-degeneracy

**Status (2026-04-28): parked.** Detection works; the recovery action (forced
refactorisation) has bugs and unclear net benefit. WIP code lives on branch
`ss`, uncommitted. See "What was tried" below before resuming.

The previous overview claimed B.1 was implemented and `[x]` done from older
work, but verification found no `trackDegeneracy` / `STALL_LIMIT` /
`IterDescr.ratio` code on any branch. That earlier work was lost or never
landed. The current attempt rebuilt B.1 from scratch and added B.3.

### B.1 Detection — [x] in tree (2026-04-28)

After parking B.3, the diagnostic plumbing was kept on branch `ss`:

- `IterDescr.ratio` captures the winning step-size ratio from
  `testDualEnterRatio()` and `testPrimalExitRatio()`. Defaults to
  `Double.MAX_VALUE`; reset by `IterDescr.reset()`.
- `SimplexSolver.trackDegeneracy(IterDescr)` increments
  `myDegeneratePivotCount` for any basis update whose ratio falls below
  `DEGENERATE_RATIO = 1E-9`. Bound flips and non-basis-update steps are
  not counted.
- Counter exposed via package-private `countDegeneratePivots()` and
  reset in `SimplexSolver.prepareToIterate()`.
- Wired into both `doDualIterations` and `doPrimalIterations` after each
  successful pivot.

The earlier consecutive-degenerate counter (`myConsecutiveDegenerate`),
trigger counter (`myStallTriggerCount`), `STALL_LIMIT` knob, and
`checkForStall()` recovery action have all been removed along with the
trigger itself — they only made sense in conjunction with B.3.

The `StallDetectionTest` survey reads the counters and prints
per-(model, store) degenerate-pivot counts. Useful as the starting
harness for future stall-recovery work.

Survey on Netlib (5 high-degeneracy models, all stores) confirmed
detection works:

| Model | RevisedStore degenerate pivots |
|-------|-------------------------------:|
| BANDM | 20 |
| CZPROB | 598 |
| SCSD8 | 488 |
| BNL1 | 388 |
| GANGES | 312 |

### B.3 Stall-triggered refactorisation — [~] parked with known bugs (2026-04-28)

Implementation: `SimplexSolver.checkForStall()` calls
`mySimplex.prepareToIterate()` when `myConsecutiveDegenerate > STALL_LIMIT`,
then resets the counter. Wired into `doDualIterations` and
`doPrimalIterations` after each successful basis update.

For `RevisedStore` this re-factors and recomputes `x` and `d`. For tableau
stores `prepareToIterate()` is a no-op — the trigger fires structurally but
the refactor does nothing, so the trigger is RevisedStore-only in effect.

Initial test results were promising: at `STALL_LIMIT=50`, CZPROB / RevisedStore
saw 16 % fewer degenerate pivots (711 → 598) with same OPTIMAL state and
identical objective. The full `org.ojalgo.optimisation.**` test suite passed
unchanged (563 tests).

A targeted benchmark sweep over `STALL_LIMIT ∈ {50, 100, 200, MAX}` then
exposed three concrete bugs and showed the trigger's net wall-clock impact
is at best marginal.

#### Concrete bugs blocking ship

1. **PEROLD NaN bug.** At `STALL_LIMIT=50`, a single forced refactorisation
   leaves the solver in a state where it reports `state=OPTIMAL` but
   `value=NaN`. Reproducible. Cause is at the `SimplexSolver` /
   `SimplexStore` boundary: mid-iteration `prepareToIterate()` invalidates
   state that subsequent code (likely `extractValue()` or the cached
   `IterDescr` enter/exit) reads without re-deriving. Not a tuning problem
   — the fix is in how the solver re-enters the loop after a forced refactor.

2. **PILOT-WE timeout.** At `STALL_LIMIT=50`, 21 forced refactorisations fire
   during a 30 s budget and the model never converges. With the trigger
   effectively off, PILOT-WE solves in 2.7 s. The trigger is firing faster
   than the solver can recover from each refactorisation — a refactorisation
   storm.

3. **QAP12 wrong answer.** At `STALL_LIMIT=100`, a single trigger fire
   drives QAP12 to `state=INFEASIBLE` on a problem with a known optimal
   solution. At `LIMIT≥200` it solves correctly. Same root cause family as
   bug 1: the post-refactor state isn't consistent with the iteration the
   solver then continues.

#### Sweep data (RevisedStore, single-thread, `time_abort=30 s`)

| Model | LIMIT=50 | LIMIT=100 | LIMIT=200 | LIMIT=MAX |
|-------|---------|---------|---------|---------|
| DEGEN3 | 10× / 8966 ms | 0× / 7461 ms | 0× / 7551 ms | 0× / 7474 ms |
| 25FV47 | 0× / 2103 ms | 0× / 2060 ms | 0× / 2133 ms | 0× / 2054 ms |
| PEROLD | 1× / **NaN** | 0× / -8747 (correct) | 0× / -8747 | 0× / -8747 |
| FIT2D | 117× / 2231 ms | 46× / 2003 ms | 18× / **1980 ms** | 0× / 2337 ms |
| PDS-02 | 9× / 4789 ms | 4× / 4291 ms | 1× / 3957 ms | 0× / 3925 ms |
| BNL2 | 0× / 5957 ms | 0× / 5955 ms | 0× / 5912 ms | 0× / 5900 ms |
| BNL1 | 0× / 355 ms | 0× / 359 ms | 0× / 358 ms | 0× / 357 ms |
| CYCLE | 32× / 2974 ms | 10× / 2205 ms | 5× / 2140 ms | 0× / 2625 ms |
| WOOD1P | 6× / 147 ms | 3× / 113 ms | 1× / 116 ms | 0× / 111 ms |
| PILOT-WE | 21× / **TIMEOUT** | 0× / 2682 ms | 0× / 2658 ms | 0× / 2648 ms |
| QAP12 | 4× / **TIMEOUT** | 1× / **INFEASIBLE** | 0× / 19380 ms | 0× / 19079 ms |

#### Net assessment

- **`STALL_LIMIT=50` is too aggressive.** Causes wrong answers / timeouts
  on at least three models (PEROLD, PILOT-WE, QAP12).
- **`STALL_LIMIT=200` is mostly a no-op except for FIT2D.** On every
  other model, trigger fires drop to 0–1 and times converge to the
  no-trigger baseline.
- **FIT2D is the only model where the trigger measurably helps** — about
  15 % faster at `LIMIT=200` (1980 ms vs 2337 ms with no trigger).
- **DEGEN3, originally believed to be the canonical winner, is faster
  *without* the trigger.** The 2× DEGEN3 speedup observed in
  `netlib_inv_ss1.csv` vs baseline cannot be attributed to stall
  triggering — it must be JIT-warmup, measurement noise, or another change
  between the two runs.

#### To resume this work

In rough order:

1. **Fix the mid-iteration refactor state corruption** (PEROLD / QAP12).
   Hypotheses to check: `extractValue()` reads cached state that
   `prepareToIterate()` doesn't refresh; the `IterDescr` held by the
   iteration loop references basis indices that the refactor remaps; edge
   weights become stale and corrupt the next pivot's pricing. The fix is
   at the `SimplexSolver` / `SimplexStore` interaction layer, not in the
   trigger logic.
2. **Reconsider the recovery action.** Forced refactorisation alone may
   not be the right response. Alternatives:
   - Reduced-cost perturbation (B.2 below)
   - Lexicographic / Bland's-rule fall-back for the next K iterations
   - Edge-weight reset only (much cheaper than a full refactor)
3. **Adaptive `STALL_LIMIT` per model size.** A fixed constant is
   wrong — small problems can refactor cheaply, large ones cannot. Scale
   with `m` or with measured refactor cost.
4. **Investigate FIT2D specifically.** It's the one model where forced
   refactorisation measurably helps. Understanding why might suggest a
   more targeted heuristic than "every K consecutive degenerate pivots".

#### What remains in tree vs what was removed

Kept (diagnostic plumbing):

- `IterDescr.ratio` and its assignments in the two ratio tests
- `SimplexSolver.DEGENERATE_RATIO` constant (`1E-9`, `private static final`)
- `myDegeneratePivotCount` field and `countDegeneratePivots()` accessor
- `SimplexSolver.trackDegeneracy(IterDescr)` helper, called from both
  iteration loops
- `StallDetectionTest` survey, simplified to a single
  `surveyDegenerateModels` test

Removed (the trigger and everything specific to it):

- `STALL_LIMIT`, `STALL_RATIO`
- `myConsecutiveDegenerate`, `myStallTriggerCount`, `countStallTriggers()`
- `checkForStall(IterDescr)`
- The `compareWithAndWithoutStallTrigger` and `sweepStallLimit` test
  methods

Reference data still in tree (under `src/test/resources/tmp/`):

- `netlib_inv_ss1.{csv,log}` — the benchmark run that prompted the
  deeper investigation
- `netlib_baseline.{csv,log}` — the comparison baseline

### B.2 Recovery (perturbation) — [ ] not started

The previous plan claimed `perturbDegenerateSolution` infrastructure
existed on `SimplexStore`. Verified: it does not. When/if a recovery
mechanism beyond plain refactorisation is needed:

- [ ] Reduced-cost perturbation for dual simplex (not x-perturbation —
  that would push variables the wrong direction)
- [ ] Validate any primal perturbation on phased models like MODSZK1
- [ ] Couple with adaptive thresholds (see B.3 follow-up #3)

Should be tackled together with the B.3 fix — the trigger and the
recovery action are not independent design choices.

---

## C. Pricing strategy

Source: `plan-revised-store.prompt.md` P6. Highest-leverage remaining item
together with B.

### C.1 Partial pricing — [-] tried 2026-04-29; reverted

Implemented and benchmarked. Net result was bad enough to revert. Documenting
here so we don't try the same approach again without changes.

#### What was tried

Partition `excluded[]` (or `included[]`) into K sections, scan one section
per call with cascade-fallback to remaining sections if no candidate is
found. Section state advances after each successful pick; full cascade
runs for `iteration == null` feasibility checks. Implementation lived in
`SimplexSolver.java` with helpers `partialPricingSections(int)` and section
fields `myDualPricingSection`, `myPrimalPricingSection`. Constants:
`PARTIAL_PRICING_MAX_SECTIONS = 8`, `PARTIAL_PRICING_MIN_TOTAL = 400`.

Three placements were tried:

| Method | Role | Outcome |
|--------|------|---------|
| `testDualEnterRatio` | dual *ratio test* | Reverted immediately — partial scan breaks dual feasibility (must find globally min ratio). 11 wrong-answer regressions on first run (FFFFF800, BANDM, CRE_A, CRE_C, FIT1P, SCRS8, SCSD8, FORPLAN, WOODW, etc.). |
| `getDualExitCandidate` | dual *pricing* (leaving var, scans `included`) | Algorithmically valid but numerically destructive on wide problems. Section-local steepest-edge picks less-infeasible variables, leading to small dual step sizes and reduced-cost drift. 4 new test failures: CRE_A and CRE_C reach OPTIMAL with values off by 11–12 orders of magnitude; 80BAU3B never reaches OPTIMAL (FEASIBLE 1e21). Reverted. |
| `getPrimalEnterCandidate` | primal *pricing* (entering var, scans `excluded`) | Theoretically the safest case. Kept long enough to benchmark. Two pre-existing fragilities (MODSZK1, testEnergyApp) reproduced, no new failures from primal partial pricing alone. |

#### Benchmark (full netlib, primal partial pricing only)

`netlib_partial1.csv` vs `netlib_baseline.csv`. Threshold-based wins/losses
(>10 % difference, >1 ms absolute):

|        | Wins  | Losses | Flat | New failures | New solves |
|--------|------:|-------:|-----:|-------------:|-----------:|
| dual-D | 3 (-21 s total) | 19 (+5 s) | 59 | BNL2, CYCLE, PILOT87 | — |
| dual-S | 9 (-8 s) | 7 (+28 s) | 69 | CYCLE, PILOTNOV | GREENBEB, PILOT, QAP8 |

Headline numbers:

- **dual-D net regression**: 19 slowdowns vs 3 speedups. The single big
  win was QAP12 (-18 s, 1.19×). Spread of −10 to −30 % slowdowns across
  AGG, BANDM, CRE-C, DEGEN3 (-47 %!), FIT1P, GANGES, KEN-07, MAROS,
  MODSZK1 (3.15× slower), NESM, PEROLD, PILOT-WE, PILOT4, SCAGR25,
  SCTAP3, SHIP08L, SIERRA, STOCFOR2, WOODW.
- **dual-S net regression in seconds**: only 9 wins for −8 s but 7 losses
  for +28 s. The killer: PEROLD dual-S 12.47× slower (+15 s). MAROS-R7
  dual-S 1.61× slower (+12 s). MODSZK1 dual-S 2.76× slower (+870 ms).
- **Some real wins** were preserved: DEGEN3 dual-S 1.30× (-1.7 s), QAP12
  dual-D 1.19× (-18 s), QAP12 dual-S 1.31× (-4.6 s), PILOT-WE dual-S
  2.38× (-1.5 s), GROW22 dual-S 1.56×, DEGEN2 dual-S 1.59×, E226 dual-S
  1.23×, TUFF dual-S 1.25×, WOOD1P dual-S 1.25×.
- **The original target was unmet**: 80BAU3B (the 173× gap to HiGHS)
  was unchanged. PILOT87 dual-D went from solving (85 s) to FAIL.
  KEN-07, BNL1, SCTAP3, MAROS-R7 all slower. The "wide problems → primal
  pricing helps" theory did not hold up.

#### Why this didn't work

1. **Most wide netlib LPs are dual-phase-1 dominated.** PhasedSimplexSolver
   does dual phase-1 (artificial start → primal feasibility) then primal
   phase-2 (feasibility → optimum). Partial pricing in
   `getPrimalEnterCandidate` only affects phase 2. The `testDualEnterRatio`
   scan that dominates phase-1 cost on wide problems must stay full for
   correctness.

2. **Section-local pricing changes the pivot path** in ways that interact
   badly with reduced-cost edge weights. The score function
   `magnitude² / weight` was tuned for global selection. Picking a
   locally-best column with a poor edge-weight ratio leads to
   slow-converging or numerically unstable pivot sequences. PEROLD
   dual-S 12× slowdown is the worst case.

3. **Per-section reset of `largest` ignores edge-weight signal across
   sections.** A column with high steepest-edge score in section S+1 may
   never be picked while we're chewing through marginal candidates in
   section S. This compounds (1).

#### What to try next, if anyone resumes this

- **Partial pricing only on the second call onwards.** Always do a full
  pricing scan at the start of phase 2 to set `largest` baseline; then
  switch to partial. Section-local `largest` would be compared against
  a remembered global threshold.
- **Steepest-edge weight initialisation matters.** Devex / approximate
  steepest-edge has different score scales per column. Partial pricing
  should be evaluated alongside that, not on top of the current
  edge-weight code.
- **Primal-only partial pricing should be tested in isolation on a
  primal-feasible-from-start corpus.** Most netlib LPs need phase 1, so
  the test isn't representative of the C.1 hypothesis.
- **Don't attempt dual partial pricing without anti-degeneracy.** The
  dual ratio test's interaction with leaving-variable choice means
  section-local picks routinely produce small step sizes and
  numerical drift. Steepest-edge alone isn't enough; need bounds-shifting
  perturbation (Harris) or expanding ratio test.

### C.2 Steepest-edge pricing — [ ]

Selects pivots by reduced cost weighted by the norm of the column in the
current basis. Reduces total iteration count, not just per-iteration cost.

- [ ] Implement steepest-edge weights with incremental update
- [ ] Approximate variants (Devex) as a cheaper alternative
- [ ] Required for closing the 77× MAROS-R7 gap to ORTools

### C.3 Pivot selection independent of store — [ ]

The pricing decision lives in `SimplexSolver` (or a strategy plugged into it).
Store-side cost is reading column data via `BasisRepresentation` /
`SimplexStore` accessors. Keep the strategy interface store-agnostic.

---

## D. Scaling orchestration

Source: `plan-revised-store.prompt.md` P5.

Existing per-expression power-of-10 row scaling lives in `ModelEntity` (model
side, before solver). Adding column / equilibrium scaling (Curtis-Reid, Ruiz)
is partly model-side, but the solver decides when to apply it and unscales
results.

- [ ] Add a scaling step in `SimplexSolver.build()` or a pre-solve hook
- [ ] Curtis-Reid or Ruiz iterative equilibration
- [ ] Unscale on solution extraction
- [ ] Verify interaction with shift / bound-flip

Direct numerical-conditioning attack on MAROS-R7 (77× ORTools gap) and BNL1
numerical divergence.

---

## E. Phase-1 / phase-2 / artificial variables

### E.1 Lazy artificial variable creation — [ ]

Source: `plan-revisedSimplexOptimisation.prompt.md` step 3.

`SimplexSolver.build()` and `LinearStructure` always allocate full
artificial / dual-variable space. For models that do not need phase-1, this
is wasted memory and setup time.

- [ ] Allocate artificials lazily only when phase-1 is needed
- [ ] Adjust `LinearStructure` so `nbArtificials = 0` is a valid initial state
- [ ] Verify dual extraction (`sliceDualVariables`) still works without
  pre-allocated artificial slots

### E.2 Incremental phase-1 → phase-2 coefficient conversion — [~]

Source: `plan-incrementalPhase1Conversion.prompt.md`.

Instead of switching the entire objective at the end of phase-1, switch one
coefficient at a time (the entering variable's column) during pivots.
Potentially improves stability and reduces the discontinuity at phase
boundary.

**Critical timing**: the switch must happen **before** the pivot, not after.
The reduced cost of the entering variable must be zero post-pivot.

- [ ] Add a pre-pivot hook in `SimplexSolver.pivot()` (or `SimplexStore.pivot()`)
- [ ] Solver-side: drive the switch from `calculateIteration` / pre-pivot hook
- [ ] Existing partial impl in `DenseTableau` runs after pivot — wrong timing,
  must be moved to the pre-pivot point
- [ ] Existing `SparseTableau` code is commented out; uncomment and fix timing
- [ ] Add solver-level test that incremental switching matches full-switch
  behaviour across all store types

The store-side coefficient mutation (which row/cell to update) is described
in [plan-simplexStore.prompt.md](plan-simplexStore.prompt.md). The hook point
and timing are solver concerns.

### E.3 ClassicSimplexSolver completion — [~]

Source: `plan-classicSimplexSolver.prompt.md`. Branch `classic`,
commit `5f9e4a1d`. Currently does not compile.

Compilation errors:
1. `SimplexStore.newClassicSimplexSolver(Options)` — method missing
2. `SimplexStore.switchToPhase2()` — method missing
3. `SimplexTableau.newPhase1()` — abstract, not implemented
4. `DenseTableau.phase1()` / `SparseTableau.phase1()` — cannot override final

Steps:

- [ ] Add `newClassicSimplexSolver(Options, int...)` to `SimplexStore`
- [ ] Unify phase-1 objective API:
  - Option A: make `SimplexTableau.phase1()` non-final, delegate to `newPhase1()`
  - Option B: drop abstract `newPhase1()`, keep existing `phase1()` impls
- [ ] Add `switchToPhase2()` abstract on `SimplexStore`; `RevisedStore` already
  has `removePhase1()`
- [ ] Complete `ClassicSimplexSolver.setup()` and
  `setupClassicPhase1Objective()`
- [ ] Investigate `DecomposedInverse` failure on `BurkardtDatasetsMps#testMPSadlittle`
- [ ] Investigate `gr4x6` artificial-variable issue (likely phase-1 setup)
- [ ] Enable `ClassicSimplexSolver` tests in `RevisedSimplexSolverTest` across
  all `STORE_FACTORIES`

**Value**: LOW — the phased approach (dual phase-1, primal phase-2) already
works. A classic primal 2-phase solver adds variety but may not improve
practical performance. Deprioritise unless a specific use case appears.

---

## F. Setup / shift refactor

Source: `plan-unshiftTableau.prompt.md` Phase 1.2 + 3.2.

Today, each solver subclass's `setup()` calls `this.shift(j, lb/ub, rc)`
directly. The shifting logic is duplicated across `PhasedSimplexSolver`,
`DualSimplexSolver`, `PrimalSimplexSolver`. The store should own the *how*;
the solver should own the *what* (which bound to use, when).

### F.1 Solver-side delegation — [ ]

- [ ] `PhasedSimplexSolver.setup()` — delegate column shift to store
- [ ] `DualSimplexSolver.setup()` — delegate
- [ ] `PrimalSimplexSolver.setup()` — delegate
- [ ] Solver only chooses bound; store performs data manipulation

### F.2 Bound-flip handling — [ ]

`SimplexSolver.update()` (lines 1207–1230) handles bound-flip via
`this.shift(j, ColumnState.UPPER/LOWER)`. Decide whether this stays in the
solver or moves to the store like F.1.

- [ ] Inventory all `shift(...)` call sites in `SimplexSolver` and subclasses
- [ ] Decide the boundary; document the convention
- [ ] Remove obsolete `mySolutionShift[]` / `myValueShift` fields if no longer
  needed (already commented out per `plan-unshiftTableau.prompt.md`)

The store-side `SimplexStore.shiftColumn()` work is in
[plan-simplexStore.prompt.md](plan-simplexStore.prompt.md).

---

## G. Identity-basis fast path

Source: `plan-revisedSimplexOptimisation.prompt.md` steps 1–2.

When the initial basis is the identity matrix, the initial `reset()` /
`ftran()` is wasted work. Detection is a one-line condition; the savings
multiply across `prepareToIterate()` and the first few iterations.

The detection (and the decision to skip) is solver-level — the solver knows
the structure being built. The actual skip lives in the store. Tracked here
because the orchestration is solver-side; the implementation note is in
[plan-simplexStore.prompt.md](plan-simplexStore.prompt.md).

- [ ] Detect identity-basis state in `SimplexSolver.build()` /
  `prepareToIterate()`
- [ ] `boolean myBasisIsIdentity` flag, cleared on first basis update
- [ ] Skip initial `reset` / `ftran` while flag is true

---

## Priority order (within this plan)

1. **B.3 Stall-triggered refactorisation** — [~] parked 2026-04-28
   pending fix of mid-iteration state corruption (PEROLD NaN, QAP12 INFEASIBLE
   bugs). See §B for details.
2. **A.2 + A.3** Tighten PIVOT, add tie-breaking — cheap, helps numerics broadly
3. ~~**C.1 Partial pricing**~~ — [-] tried and reverted 2026-04-29. See §C.1.
4. **C.2 Steepest-edge** — closes the iteration-count gap to ORTools
5. **D Scaling** — couples with C; conditioning-driven iteration reduction
6. **B.2 Recovery** — only if B.3 alone proves insufficient (couple with B.3 redesign)
7. **E.1 Lazy artificials** — cleanup
8. **E.2 Incremental phase-1** — only if numerical-stability evidence emerges
9. **F Shift refactor** — code hygiene; do alongside store work
10. **A.1 Data-scaled threshold** — needs design work on scaling
11. **G Identity basis** — small win, do opportunistically
12. **E.3 ClassicSimplexSolver** — only if a use case appears

## Key files

- [SimplexSolver.java](src/main/java/org/ojalgo/optimisation/linear/SimplexSolver.java)
- [PhasedSimplexSolver.java](src/main/java/org/ojalgo/optimisation/linear/PhasedSimplexSolver.java)
- [DualSimplexSolver.java](src/main/java/org/ojalgo/optimisation/linear/DualSimplexSolver.java)
- [PrimalSimplexSolver.java](src/main/java/org/ojalgo/optimisation/linear/PrimalSimplexSolver.java)
- [ClassicSimplexSolver.java](src/main/java/org/ojalgo/optimisation/linear/ClassicSimplexSolver.java)
- [LinearStructure.java](src/main/java/org/ojalgo/optimisation/linear/LinearStructure.java)

## Tests

- [RevisedSimplexSolverTest.java](src/test/java/org/ojalgo/optimisation/linear/RevisedSimplexSolverTest.java)
- [StallDetectionTest.java](src/test/java/org/ojalgo/optimisation/linear/StallDetectionTest.java)
- [BurkardtDatasetsMps.java](src/test/java/org/ojalgo/optimisation/linear/BurkardtDatasetsMps.java)
- Full Netlib regression run for any tolerance / ratio / pricing change
