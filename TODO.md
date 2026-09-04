# TODO

Temporary list of known issues found while working on MIP cut generation (2026-09). To be dealt with later.

## Revised simplex: corrupt state after an INFEASIBLE termination

If a (dual) simplex run ends INFEASIBLE (dual unbounded) and the same `SimplexSolver`/`RevisedStore` instance
then receives in-place bound updates (`updateRange`/`fixVariable`) and is re-solved, the result can be a
primal-infeasible point labelled OPTIMAL (rows violated by ~1e-3, objective below the true optimum), and
subsequent warm solves drift into spurious INFEASIBLE. Rebuilding the solver (`IntermediateSolver.reset()` +
cold solve) recovers; merely resuming the dual iterations from the existing basis (state APPROXIMATE) does not.

- Observed on f2gap40400 root strong-branching probes: down-probe INFEASIBLE, restore, up-probe wrong.
- Mitigated only at the call site: `IntegerSolver.recoverRootSolver` rebuilds after a non-optimal probe.
  The dive path is not affected (an INFEASIBLE node ends the dive and the solver is disposed).
- Root cause in `SimplexSolver`/`RevisedStore` (basis, `x`, or factorisation state after the infeasible
  exit) not yet identified. No small standalone reproduction yet; a deterministic replay of the captured
  f2gap operation sequence reproduced it. When building one, compare warm solves against a reference with
  the same relaxation mode (hard `relax(false)` skips the integer-rounding presolve, soft does not).

## `IntermediateSolver.update(int, lower, upper)` silently drops solver-only bounds

Once any in-place update falls through (variable not present in the solver, e.g. presolve-fixed, or
`updateRange` returning false) `myInPlaceUpdatesOK` becomes false for the lifetime of the
`IntermediateSolver`, the solver is discarded, and every later solver-only `update(int, lower, upper)` is
ignored: the next solve runs cold from the model's variable bounds. `update(Variable)` is unaffected since the
model already carries the change. Documented behaviour, but a trap for probe-style code; consider either
re-applying pending solver-only bounds after a rebuild, or failing loudly.

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

## MIP gap tolerance semantics: tests should pass with standard configuration

`testBell3b` (and `IntegerUserFiles.testEnergyApp`) only assert their expected optimum reliably with a
tightened gap tolerance (`withGapTolerance(NumberContext.of(8))`). With the default `NumberContext.of(5, 7)`
the gap check (`ModelStrategy.isGoodEnough`, `IntegerSolver.isOptimalityProven`) is rounding-based on 5
significant digits, so the effective relative gap grows with the magnitude of the objective (about 1e-4 for
an objective near 1e7) and the returned solution depends on search order. Decide what the default gap should
mean (a proper relative and/or absolute tolerance, as other solvers define it), implement it consistently in
both places, and then drop the per-test tightened strategies so all of these tests pass with the standard
configuration.
