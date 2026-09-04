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
