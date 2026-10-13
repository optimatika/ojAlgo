# Plan: Presolve improvements

Legend: [x] done, [ ] pending, [~] partial/in progress

Reference: [.github/copilot-instructions.md](.github/copilot-instructions.md)

## Scope

Presolve transformations applied to `ExpressionsBasedModel` before any
solver runs. Today's presolvers (`ZERO_ONE_TWO`, `INTEGER`,
`REDUNDANT_CONSTRAINT`, `UNREFERENCED`, `LINEAR_OBJECTIVE`) handle basic
cases. This plan adds row/column reductions that shrink both `m` and `n`
before simplex starts, with multiplicative impact on all per-iteration costs.

## 1. Doubleton equality presolve

### Goal

Eliminate rows and columns via substitution in doubleton equality constraints
where one variable is implied free. This reduces both m and n before simplex
starts, with multiplicative impact on all per-iteration costs.

## Background

### Doubleton equality substitution

Given a doubleton equality $a_1 x_1 + a_2 x_2 = b$ where $x_1$ is implied free (its explicit bounds are redundant given the constraint and $x_2$'s bounds):

1. Solve: $x_1 = (b - a_2 x_2) / a_1$
2. For every other expression $e$ where $x_1$ appears with coefficient $c_1$:
   - Remove $x_1$ from $e$
   - Update $x_2$'s coefficient: $c_2 \leftarrow c_2 - c_1 \cdot a_2 / a_1$
   - Shift bounds: $\text{limit} \leftarrow \text{limit} - c_1 \cdot b / a_1$
3. Remove the doubleton row
4. Remove $x_1$ from the model (fix at 0 or equivalent)
5. Post-solve: recover $x_1^* = (b - a_2 x_2^*) / a_1$

This is a zero-fill-in substitution: each affected expression replaces one variable ($x_1$) with another ($x_2$), keeping the nonzero count the same or reducing it (if $x_2$ was already present).

### Opportunity measured on Netlib

| Model | m | Eliminable | Reduction |
|-------|---|-----------|-----------|
| BNL2 | 2324 | 849 | 36.5% |
| PDS-02 | 2953 | 539 | 18.3% |
| CYCLE | 1903 | 321 | 16.9% |
| KEN-07 | 2426 | 397 | 16.4% |
| GREENBEA/B | 2392 | 371 | 15.5% |
| BNL1 | 643 | 86 | 13.4% |
| STOCFOR2 | 2157 | 252 | 11.7% |
| DEGEN3 | 1503 | 91 | 6.1% |
| PILOT-WE | 722 | 46 | 6.4% |
| PILOT-JA | 940 | 53 | 5.6% |
| PEROLD | 625 | 34 | 5.4% |

No benefit for MAROS-R7, QAP8/12, TRUSS, WOOD1P, FIT1P (zero doubleton equalities).

### Design considerations

**Not a standard presolver.** The current `Presolver` contract operates on one expression at a time and can only tighten bounds, fix variables, or mark expressions redundant/infeasible. Doubleton substitution modifies OTHER expressions (rewrites coefficients and bounds). This requires either:
- A new presolver type with model-wide access, or
- A separate pre-processing step in `simplify()` / `presolve()`, outside the per-expression presolver loop

The second approach is cleaner — it's a model transformation, not a per-expression simplification.

**Implied-free detection.** A variable $x_1$ is implied free in a doubleton equality if its explicit bounds are redundant given the constraint and $x_2$'s bounds. For the common case of $x_1 \ge 0$, the check reduces to verifying $(b - a_2 x_2)/a_1 \ge 0$ for all feasible $x_2$.

Restricting to variables declared totally unbounded captures almost nothing (Netlib models overwhelmingly use $x \ge 0$). The full implied-free check with bound arithmetic is necessary.

**Interaction with other presolvers.** Must run after `doCase1` (singleton row absorption into variable bounds) to have accurate variable bounds. The existing `ZERO_ONE_TWO` handles cases 0, 1, 2 in order within its fixpoint loop, so singleton absorption happens first naturally.

**Post-solve recovery.** Need to store $(a_1, a_2, b, \text{idx}_1, \text{idx}_2)$ for each eliminated doubleton to recover $x_1^*$ after solving.

**Numerical caution.** Prior experience with singleton column presolver and bound propagation showed that tighter bounds can make problems numerically harder. Doubleton substitution goes the other direction — it removes constraints and variables rather than tightening bounds. The coefficient rewriting does change numerical properties of remaining expressions, so monitoring for regressions is important.

## Tasks

### Presolver framework refactoring

- [ ] Add a model-level transformation hook in `simplify()` (between `scanEntities()` and `presolve()`, or as a new step)
- [ ] Define storage structure for post-solve recovery info (eliminated variable index, coefficients, rhs)
- [ ] Integrate post-solve recovery into `expandFreeToFull()` or equivalent

### Doubleton substitution

- [ ] Scan equality expressions with exactly 2 linear variables
- [ ] For each, check if either variable is implied free (bound comparison)
- [ ] Choose which variable to eliminate (prefer the one with fewer appearances in other expressions, to minimize rewriting)
- [ ] Perform substitution: update coefficients and bounds in all affected expressions
- [ ] Mark the doubleton expression as redundant; fix the eliminated variable
- [ ] Handle cascading: after one elimination, new doubletons may appear (an expression with 3 variables loses one → becomes doubleton). Consider a fixpoint loop or single-pass approach.

### Testing

- [ ] Unit test: small hand-crafted models with known optimal, verify solution after post-solve recovery
- [ ] Netlib regression: full suite, compare objective values and feasibility
- [ ] Benchmark: compare solve times before/after on models with high doubleton counts (BNL2, PDS-02, CYCLE, KEN-07)

## Key code locations

- `ExpressionsBasedModel.simplify()` — orchestrates presolver passes
- `ExpressionsBasedModel.presolve()` — fixpoint loop with `ZERO_ONE_TWO`
- `ExpressionsBasedModel.Integration.expandFreeToFull()` — post-solve variable recovery
- `Presolvers.doCase2()` — existing 2-variable bound propagation (does NOT substitute)
- `Expression.getLinearKeySet()` / `getLinearEntrySet()` — access coefficients
- `Variable.setFixed()` / `lower()` / `upper()` — bound manipulation

## Analysis tool

`src/test/java/org/ojalgo/optimisation/linear/AnalyseDoubletonRows.java` — standalone analysis tool that loads Netlib models and counts doubleton equality rows with implied-free variables. Used to produce the opportunity table above.

## Measurement

1. Run Netlib benchmark before/after with `Parallelism.ONE`.
2. Compare solve times, iteration counts, and objective values.
3. Track solved/failed status changes.
4. Monitor for numerical regressions (problems that were solved but become infeasible or unstable).

## 2. Future presolve items

Lower priority, no analysis yet. Cross-reference: production solvers (HiGHS,
CLP) include all of these.

- [ ] **Implied free columns** — remove redundant variable bounds when other
  constraints make them non-binding
- [ ] **Forcing constraints** — detect when a constraint forces all its
  variables to a single value, fix them
- [ ] **Tripleton equality presolve** — extension of doubleton substitution
  to 3-variable equalities (more bookkeeping, lower payoff per row)
- [ ] **Dominated columns** — detect and fix variables whose role is
  strictly worse than another's
- [ ] **Empty/duplicate constraint detection** — already partially in
  `REDUNDANT_CONSTRAINT`; verify completeness
