# Plan Overview: ojAlgo Development Tasks

Cross-reference: [.github/copilot-instructions.md](.github/copilot-instructions.md)

Legend: [x] done, [ ] pending, [~] partial/in progress, [-] abandoned/archived

Reorganised 2026-04-27: 15 fragmented plan documents consolidated into 7
topic-grouped plans below. Each plan has its own status, priority list, and
file references. This overview is the index.

---

## The plans

| # | Plan | Topic | Status |
|---|------|-------|--------|
| 1 | [plan-simplexSolver.prompt.md](plan-simplexSolver.prompt.md) | `SimplexSolver` and subclasses (store-independent) | active |
| 2 | [plan-simplexStore.prompt.md](plan-simplexStore.prompt.md) | `SimplexStore` impls (`RevisedStore`, tableaux) | Phase 1 done; Phase 2 active |
| 3 | [plan-sparseLU.prompt.md](plan-sparseLU.prompt.md) | `SparseLU`, `BasisRepresentation`, FT updates, hyper-sparse | active |
| 4 | [plan-presolve.prompt.md](plan-presolve.prompt.md) | Doubleton + future presolve | not started |
| 5 | [plan-convex.prompt.md](plan-convex.prompt.md) | Convex QP: dual regularisation + Schur scaling | implemented; needs enabling |
| 6 | [plan-conic.prompt.md](plan-conic.prompt.md) | Conic solver (LP/QP/SOCP) | M1.3 in progress |
| 7 | This file | Index | — |

## Recommended priority order

1. **`simplexSolver` §B.3** — stall-triggered refactor, small change, immediate stability win
2. **`sparseLU` §3 + §1.2** — FT numerical stability + Markowitz pivoting (couple)
3. **`presolve` §1** — doubleton presolve, well-scoped, multiplicative
4. **`simplexSolver` §C** — partial pricing + steepest-edge (closes ORTools gap)
5. **`simplexSolver` §D** — scaling (couples with C)
6. **`sparseLU` §4** + **`simplexStore` §1** — hyper-sparse btran + PRICE
7. **`convex` §1 + §2** — finish dual regularisation and Schur scaling, validate together
8. **`conic` M1.3 → M2** — continue conic solver development
9. **`sparseLU` §1.1 + §1.5** — packed array storage for L and U
10. Everything else as time permits

## Cross-plan dependency map

```
plan-simplexSolver
  ├─ depends on: plan-simplexStore (shift refactor cleanup)
  ├─ depends on: plan-sparseLU (stall-triggered refactor needs the LU hook)
  └─ drives:    pricing strategy, scaling, anti-degeneracy

plan-simplexStore
  ├─ depends on: plan-sparseLU (hyper-sparse btran nonzero set)
  └─ Phase 1 done; remaining: hyper-sparse PRICE consumer, shift store side

plan-sparseLU
  ├─ Markowitz (§1.2) ↔ FT stability (§3) — same root cause for some regressions
  ├─ Hyper-sparse btran (§4) ↔ PRICE consumer in plan-simplexStore §1
  └─ ep-reuse (§8) gated on FT stability (§3)

plan-presolve
  └─ independent — benefits all LP solvers

plan-convex
  └─ self-contained in convex package

plan-conic
  └─ self-contained in conic package
```

## What changed in this reorganisation

### Files removed (consolidated)

| Removed | Absorbed into |
|---------|---------------|
| `plan-betterlu.prompt.md` | §1–§4 → `plan-sparseLU.prompt.md`; §5 → `plan-simplexSolver.prompt.md` (§A) |
| `plan-classicSimplexSolver.prompt.md` | `plan-simplexSolver.prompt.md` §E.3 |
| `plan-doubleton-presolve.prompt.md` | renamed → `plan-presolve.prompt.md` |
| `plan-dualRegularisation.prompt.md` | `plan-convex.prompt.md` §1 |
| `plan-hyper-sparse-btran.prompt.md` | LU side → `plan-sparseLU.prompt.md` §4; store side → `plan-simplexStore.prompt.md` §1 |
| `plan-incrementalPhase1Conversion.prompt.md` | timing → `plan-simplexSolver.prompt.md` §E.2; per-store → `plan-simplexStore.prompt.md` §4 |
| `plan-revisedSimplexOptimisation.prompt.md` | identity-basis → `plan-simplexStore.prompt.md` §2; lazy artificials → `plan-simplexSolver.prompt.md` §E.1 |
| `plan-revised-store.prompt.md` | renamed → `plan-simplexStore.prompt.md`; solver-level items P1/P5/P6 → `plan-simplexSolver.prompt.md` |
| `plan-schurScale.prompt.md` | `plan-convex.prompt.md` §2 |
| `plan-shiftVariables.prompt.md` | done parts archived; remaining `SimplexTableauSolver` parts dropped (superseded) |
| `plan-sparse-lu-improvements.prompt.md` | `plan-sparseLU.prompt.md` |
| `plan-sparseLuPerformance.prompt.md` | archived results section in `plan-sparseLU.prompt.md` |
| `plan-unshiftTableau.prompt.md` | solver side → `plan-simplexSolver.prompt.md` §F; store side → `plan-simplexStore.prompt.md` §3 |

### Files kept

- [plan-conic.prompt.md](plan-conic.prompt.md) — already self-contained and current
- [plan-overview.prompt.md](plan-overview.prompt.md) — this file (rewritten as index)

### Why this grouping

- **By owner class** rather than by branch / by feature. Plans now correspond
  to the code package they affect.
- **`SimplexSolver` is store-independent** — extracted as its own document
  per request, so issues that improve the algorithm regardless of storage
  are visible together.
- **`SimplexStore` is the storage layer** — `RevisedStore` plus the two
  tableau impls. Inner-loop hot path lives here.
- **`SparseLU` is the basis machinery** — used by `RevisedStore` but
  conceptually independent; multiple solvers could use it.
- **Convex QP** stays grouped (regularisation + scaling are coupled).
- **Conic** stays self-contained — separate package, separate algorithm
  family.
- **Presolve** is upstream of all solvers — stays its own document.
