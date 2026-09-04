# TODO

Temporary list of known issues found while working on MIP cut generation (2026-09). To be dealt with later.

## Revised simplex: warm restart after an INFEASIBLE termination - FIXED (2026-09-05)

Investigated with the bm23 (binding cutoff) and f2gap40400 (root probes) reproductions. Two separate bugs:

1. **Invalid GMI cuts** (the bm23 case, and the reason a binding objective cutoff row lost the optimum).
   `EntityMap.integers()` marked the slack of every integer row (integer coefficients on integer variables,
   `Expression.isInteger()`) as integer, ignoring the right-hand side. The cutoff row `c'x <= 34.000001` is
   such a row with a non-integer limit, so its slack is not integer valued; the GMI generator then produced a
   cut like `x2+x8+x19+x25+x26-x10 >= 4.000001`, cutting off integer points where the sum is 4, including
   the optimum. Fixed: a slack is integer only if the row's limits are integers too. Related:
   `Expression.tighten()` only rounded limits on the first call (`isInteger()` caches), so a cutoff row
   created with `limitObjective(null, null)` never had its later limits rounded. Fixed by having
   `doIntegerRounding` re-round the limits of an already analysed integer expression.
2. **Warm restart from a failed run** (the f2gap case, ~12% of runs returned INFEASIBLE). When a dual run
   ends INFEASIBLE it has moved along a dual ray: basic values in the thousands for binaries and a poorly
   conditioned tableau. `updateRange` then left the state INFEASIBLE, `PhasedSimplexSolver.solve` took the
   "cold" branch on that basis, and the next (feasible) probe ended INFEASIBLE too; with both probes
   infeasible `processRoot` declared the whole problem infeasible. Fixed as suggested: `SimplexSolver.updateRange`
   now returns `false` (nothing modified) while `state.isFailure()`, and `IntermediateSolver` treats that
   as "rebuild this time" rather than "in-place updates unsupported". Restarting from the last optimal basis
   inside the solver was tried first and does not work: `SimplexTableau.resetBasis` re-pivots row by row
   and produces NaN for any basis but the initial one (`resetBasis` is only sound for `RevisedStore`), see
   the next item.

Regression tests: `WarmRestartAfterInfeasibleTest`, `ObjectiveIntegralityTest`; the bm23 and f2gap loops
ran 0/100 wrong afterwards.

## `SimplexTableau.resetBasis` / cold restart from a non-initial basis

`SimplexSolver.basis(int[])` and `SimplexStore.resetBasis` are supposed to restart from a given basis, but
for the tableau stores the row-by-row `doPivot` re-pivoting yields NaN even when the given basis is the
current one (experiment: reset to `included.clone()` before every re-solve on f2gap40400 gives NaN
solutions labelled OPTIMAL). `RevisedStore.resetBasis` refactorises and is fine. Not used by the B&B today
(the refusal above rebuilds instead), but it blocks the cheaper alternative of restarting from the parent's
optimal basis after a failed probe.

## `IntermediateSolver.update(int, lower, upper)` - FIXED (2026-09-05)

Solver-only bounds are now remembered per variable and re-applied whenever the solver is regenerated for
any reason other than `reset()` (which still discards them, as documented). Bounds of variables eliminated
by presolve still cannot be applied and are dropped silently.

## Integer candidate acceptance: replace "validate either vector" with an LP polish

`IntegerSolver.validIntegerCandidate` accepts a relaxation solution that passed the integrality test if either
the solution as is, or the same solution with its integer variables snapped to integers, validates against
the model. This papers over two kinds of LP noise (p0291: integer variable at 1 + 1.7e-11 fails the bound
check; 22433: snapping by 1e-9 breaks a row with large coefficients) but is not principled: which vector gets
stored depends on which one happens to validate, and the objective stored with it is the node's LP value.

Better: fix the integer variables at their rounded values, re-solve the LP for the continuous variables
(warm start from the node basis), validate that point, and store it with its exactly evaluated objective. If
that LP is infeasible the candidate is genuinely infeasible and the node can be closed. `tryRounding` could
share the same routine.

## MIP gap tolerance vs test expectations: tests should pass with standard configuration

**Decision (2026-09-04): do not tune the gap per test. Implement objective integrality (next item, done
2026-09-05) so the integer-objective instances are exact by construction with the default 1e-4 gap.
Instances with non-integral objectives that still miss the exact optimum (bell3b, EnergyApp, BigBinary: all
non-integral) are a primal-heuristics matter; their `withGapTolerance` workarounds remain for now.**

The default gap tolerance `NumberContext.of(5, 7)` is a relative gap of 1e-4 (`10^(1-5)`; absolute
0.5e-7), applied consistently by `ModelStrategy.isGoodEnough` (node pruning) and
`IntegerSolver.isOptimalityProven` (early stop). That is the same default as CPLEX/Gurobi, so the
implementation itself is fine. The problem is that the tests assert the exact optimum to 6-7 digits while
the solver is configured to accept anything within 1e-4: tests pass only when the first solution found
happens to be the exact optimum, and `testBell3b` (3e-5 off) / `IntegerUserFiles.testEnergyApp` (1.5e-4 off
with its 1e-3 gap) currently work around it with `withGapTolerance(NumberContext.of(8))`.

Alternatives considered and rejected: asserting within the configured gap (hides real regressions),
tightening the default gap (slower everywhere), a shared tight test strategy (still not the standard
configuration).

## Objective integrality - DONE (2026-09-05)

HiGHS (`HighsMipSolverData::computeNewUpperLimit`) and the commercial solvers exploit an integral objective:
when all objective coefficients are integers on integer variables (more generally, share a scale), every
node that could hold a strictly better solution stays open and the search cannot stop while one exists.

Implemented: `Expression.getIntegerStep()` exposes the step (gcd of the coefficients, scales aligned) that
`doIntegerRounding` already computed for bound rounding; `ModelStrategy` reads it off `model.objective()`
once per model (offset = objective evaluated at the origin). `toLatticeBound`/`toLatticeValue`/
`isLatticeGapClosed` use an absolute tolerance `max(1e-6 * unit, 1e-7, 1e-9 * |value|)` for LP noise
(a larger tolerance only weakens the rules, never invalidates them). Used in two places:
`ModelStrategy.isGoodEnough` prunes when the rounded node bound cannot beat the incumbent, and
`IntegerSolver.isOptimalityProven` stops when the rounded gap is below one unit. The relative-gap rule is
kept alongside, as in HiGHS.

The objective cutoff row (`markInteger`) is `incumbent - (1 - 1e-6)` lattice units for integral objectives
(and, since `tighten()` now re-rounds, lands exactly on the lattice). It was first held back because it
lost the bm23 optimum; that turned out to be the invalid-GMI-cut bug above, not the cutoff.

Consequences / limits:

- Exact by construction only while `1e-4 * |optimum| < 1 lattice unit`, i.e. |objective| below 10^4 for a unit
  lattice (p0201 7615, bm23 34, stein27 18, mod008, lseu, vpm1, f2gap...). Above that the default relative gap
  still allows a few units (HiGHS has the same limit; it rounds `rel_gap * |ub|` up to whole units).
- Not integral, so unaffected: 22433 and misc03 (continuous variables in the objective), gen, bell3b, pk1,
  pp08a, flugpl, egout, rgn, markshare1, EnergyApp, BigBinary. p0291 gets a 1e-4 lattice. The
  `withGapTolerance` workarounds in `testBell3b`/`testEnergyApp`/`testBigBinary` therefore stay until primal
  heuristics improve (diving, RINS, feasibility pump; ojAlgo only has `tryRounding`). HiGHS additionally
  detects continuous columns that are implied integer (all their rows have integer coefficients and rhs on
  integer variables); that would cover more instances and could be added later.
- Side effect of sharing the logic: `Expression.doIntegerRounding` now aligns the coefficient scales before
  taking the gcd (0.5 and 0.25 give step 0.25, previously 0.05), so integer-row bound rounding in presolve
  can be slightly tighter than before. Still valid: every coefficient is a whole multiple of the step.

## Separators: share the common code and rethink which ones run when

The six `NodeSolver.Separator` implementations (FC, MIR, GMI, KC, CL, IB) each carry their own copy of the
same plumbing: turning a row into knapsack form (complementing negative binary coefficients, folding
non-binary variables at their bound into the right-hand side), violation/efficacy thresholds (each with its own
`NumberContext`/tolerance constant), density and max-cuts limits from `CutConfiguration`, the
`model.checkSimilarity(cut)` duplicate filter, cut naming/counting, and the "skip objective / quadratic /
non-integer rows" selection. Refactor so quality filtering and row preprocessing live in one place (the
`Separator` base or a helper), with one definition of efficacy (violation normalised by cut norm, as
SCIP/HiGHS do) instead of per-class absolute tolerances.

Also rethink the strategy of which separators run when (root vs node, how many rounds, and which rows they
may be separated from: original rows only, or also GMI/MIR cut rows). Observed on p0201: the instrumented KC
separator attempted on the order of a thousand covers per solve but a large share were discarded by
`checkSimilarity`, and cut-sourced covers roughly doubled the volume without a clear benefit; on
f2gap40400 clique cuts never fired at all. Cut generation should be measured per separator (attempted /
accepted / LP bound improvement per round) and the schedule chosen from that, e.g. cheap
structural cuts (CL, IB, KC from original rows) at the root, tableau cuts (GMI) with a bounded number of
rounds, and node separation only where the root rounds showed gain.

Two specifics for the above (2026-09-05):

- Root cuts do not propagate: `IntegerSolver` generates root cuts into `rootModel = myIntegerModel.snapshot()`,
  and every deferred node starts from a fresh `myIntegerModel.snapshot()` without them. Each deferred node
  therefore re-derives the root-level cuts of the row-based separators from scratch (same rows, similar LP
  points, same cuts) and the root's bound improvement is lost. Note: simply adding cuts to `myIntegerModel`
  was tried before and did not work well (possibly because of bugs fixed since). SCIP and HiGHS keep cuts in
  a pool with an age/activity mechanism and drop cuts that stop being tight, so the LP does not grow without
  bound; whatever propagation is done needs the same kind of lifetime management.
- Row-based separators (KC, CL, IB, MIR, FC) draw from a finite family per row: the LP point only selects the
  cover / clique / complementation / implication, and the same selection gives the identical cut. Repeated
  rounds mostly re-derive the same cuts, which `checkSimilarity` then rejects or merges (bound tightening on
  the existing expression). Remember per row which selections have been emitted, and only re-separate a row
  when the LP values on its support changed. GMI is the exception (basis-dependent).
