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

## `SimplexTableau.resetBasis` - fix written, NOT applied (2026-09-05)

The tableau stores re-pivot row by row in the order of the given basis (`doPivot(i, newBasis[i])`), which
divides by zero as soon as the wanted column is basic in another row (the same basis with the rows permuted
is enough), and `SimplexStore.resetBasis` resets every non-basic column to LOWER without undoing the
upper-bound shifts in the tableau. A rewrite (partial-pivoting entry of the missing columns, row permutation,
shift-consistent bound placement, artificial recount, `TableauResetBasisTest` for all store types, verified
on f2gap40400 by resetting to the current basis before every re-solve) exists as a patch outside the repo;
it was taken out of the working tree to keep the change set focused on the failing tests. Not used by the
B&B today (the warm-restart refusal rebuilds instead), so nothing depends on it.

## Node LP ending FEASIBLE (not OPTIMAL) was treated as infeasible - FIXED (2026-09-05)

blend2 returned 7.692983 instead of 7.598985 about once per 50-150 solves when the machine was loaded
(reproduced with 12 busy threads in the same JVM, never without load). Tracking the known optimum through
the tree showed the node holding it warm-solved to state FEASIBLE at the correct optimal value: the warm
path in `PhasedSimplexSolver.solve` only runs dual iterations, and if the basis ends primal feasible but
marginally not dual feasible (`COST` tolerance) it returned FEASIBLE. `IntegerSolver.compute` treated every
non-OPTIMAL state as infeasible and dropped the subtree. Node order (hence which incumbent exists, which
cuts are added, and the warm-start history) depends on thread timing, which is why it only showed under
contention. Two fixes: the warm path now continues with primal iterations when the point is primal feasible
but not dual feasible (150 loaded solves afterwards: no FEASIBLE node result at all), and `compute` aborts
the search (`failed()`, result state FEASIBLE) on a non-optimal, non-failed LP result instead of pruning the
node. (A cold retry before aborting was tried and dropped again: it never triggered.) The
`DualSimplexSolver` still returns FEASIBLE in that situation; it is not used as node solver.

## Node bounds overwrote presolve-tightened bounds in the node model - FIXED (2026-09-05)

flugpl returned 1202100 or 1201800 instead of 1201500 in roughly 1 of 500 solves, without any load.
Tracking the optimum showed the node holding it solved (cold, right after a cut round) to an "OPTIMAL" LP
point that violates plain model equalities (ANZ4: 0.9 STM3 + ANM3 - STM4 = 0 off by 7) and is then
discarded by `validIntegerCandidate`, closing the node. The node model itself was inconsistent: presolve at
the first cold solve had fixed ANM3 from that row (STM3 and STM4 fixed) and marked the row redundant; then
`NodeKey.enforceBounds` / `setNodeState` wrote the node's own, looser bounds for ANM3 back into the
variable (`variable.lower(lb).upper(ub)`), so the rebuilt LP had the variable free and the row gone.
Fixed: node bounds are now applied tighten-only (`NodeKey.tightenBounds`), never loosening a bound the node
model already has. Afterwards 6000 flugpl solves: 0 wrong, no loss events. This very likely also explains
part of the "integer candidate discarded by validate()" history behind the snapping hack (TODO item on the
LP polish): a node LP built from an inconsistent node model can produce such points.

## Reduced-cost fixing int overflow, crossed bounds cycling the dual simplex - FIXED (2026-09-05)

Surfaced by the tighten-only change above: blend2 then hung (one worker at 100% CPU pivoting forever in
the warm dual iterations). `IntegerSolver.fixByReducedCost` computed `lower + (int) floor(gap / |rc|)`;
with a tiny reduced cost the step is `Integer.MAX_VALUE` and the sum overflows to `Integer.MIN_VALUE`, so the
node got bounds like [1, -2147483648] (seen ~3 times per blend2 solve). Before tighten-only the next
`enforceBounds` happened to overwrite such a bound; now it stays, and a basic variable whose bounds cross
can never become feasible, so the dual loop pivots until `time_abort` (15 min in the tests). Fixed: the step
is compared in double before converting, and all three `SimplexSolver.solve` variants return INFEASIBLE at
once when any variable has lower > upper (`SimplexStore.isAnyBoundCrossed`), which is also what a node whose
bound contradicts a presolve-derived one should get. Tests: `WarmRestartAfterInfeasibleTest.testCrossedBounds`
and `testCrossedBoundsInModel`.

## Dual simplex stalling on degenerate LPs - OPEN

Seen on blend2 under load after the fixes above: a node LP (150 rows, 417 columns, DenseTableau) took
20000+ dual iterations, leaving row 142 nine times in a row with different entering columns and 59 basics
still infeasible, in one case only ending at a 20 s time limit; with the 15 min limits of the tests that
looks like a hang. No crossed bounds involved: plain degenerate cycling, with only Devex pricing and the
Harris ratio test as safeguards. A first attempt (count consecutive degenerate steps, switch to Bland's
rule after max(50, m) of them) was reverted the same day: the primal "no progress" test (objective
unchanged to 7 digits) fires all the time in the late iterations of large LPs, so netlib cases such as
MODSZK1 locked into Bland's rule and crawled. The proper remedy is cost/bound perturbation with a clean-up
phase, which needs the original costs to be restorable (the tableau stores only keep the reduced row); a
cheaper interim option is an iteration cap for the warm path only, falling back to a cold rebuild through
the existing "retry non-optimal LP" logic in `IntegerSolver.compute`. Reproduction: loop blend2 under CPU
load (12 busy threads) with a temporary log when `countIterations()` passes 20000.

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

## PRIORITY (next after the cut work): primal heuristics and search order

Goal: the MIPLIB tests pass with the default configuration, and the per-test gap workarounds go away
(`testBell3b` and `IntegerUserFiles.testEnergyApp`/`testBigBinary` use `withGapTolerance`; the
`doTest(..., strategy)` overload in `MIPLIBTheEasySet` exists only for this).

Why: the default gap is 1e-4 relative, the same as CPLEX/Gurobi/HiGHS, and it is applied correctly. The
tests assert the optimum to 6-7 digits, so with a non-integral objective a run passes only when the exact
optimum is the first incumbent within the gap. bell3b with defaults (20 runs, 2026-09-05): the optimum
11786160.618 twice, 11786515.398 five times, 11786925.366 five times, eight other values, all within 0.007%
of the optimum and therefore legitimate stops. The other solvers hit the exact optimum with the same gap
because their primal heuristics find it before the bound closes; ojAlgo only has `tryRounding`.

What to build (in rough order of payoff):

- Diving heuristics at the root and periodically (fractional, guided by the incumbent, pseudo-cost diving):
  cheap, and where most exact optima are found on these instances.
- RINS / local branching sub-MIPs once an incumbent exists (fix variables that agree between the incumbent
  and the LP solution, solve the rest with a node limit).
- Feasibility pump for instances where no incumbent is found early.
- Search order: best-bound vs depth-first mix, and node selection that revisits the best-estimate nodes
  (the current worker priorities are a start).
- Objective integrality (done) keeps applying on top: for integral objectives the optimum is exact by
  construction once found.

Then: remove the `withGapTolerance` workarounds and the strategy overload, and re-tag any instance that
still needs it as a heuristics gap rather than a solver bug. Alternatives considered and rejected for the
tests: asserting within the configured gap (hides regressions), tightening the default gap (slower
everywhere; bell3b would need 1e-6), per-test gaps (the current workaround).

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
