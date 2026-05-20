# SimplexSolver / DenseTableau Tuning Plan

Goal: Improve ojAlgo-LP-dual-D (SimplexSolver + DenseTableau) performance by analysing
NETLIB models with the highest slowdown vs HiGHS, one model at a time.

Reference solver: SimplexTableauSolver (less advanced but mature and highly tuned).
External references: HiGHS, GLOP, CLP.

## Slowdown Ranking: ojAlgo-LP-dual-D vs HiGHS

Baseline taken 2026-04-26 on aarch64, 8GB RAM, 10 threads.

| Rank | Model      | Slowdown | ojAlgo-D (ms) | HiGHS (ms) | Vars  | Exprs | Density |
|------|------------|----------|---------------|------------|-------|-------|---------|
| --   | D2Q06C     | FAILED   | UNSTABLE      | 694        | 5167  | 2172  | 0.630   |
| --   | GREENBEA   | FAILED   | UNSTABLE      | 234        | 5405  | 2393  | 0.115   |
| 1    | 80BAU3B    | 172.9x   | 22464         | 130        | 9799  | 2263  | 0.823   |
| 2    | BNL2       | 109.1x   | 5392          | 49         | 3489  | 2325  | 0.609   |
| 3    | STOCFOR2   | 107.2x   | 2906          | 27         | 2031  | 2158  | 0.566   |
| 4    | MAROS-R7   | 80.2x    | 55708         | 694        | 9408  | 3137  | 0.667   |
| 5    | GREENBEB   | 66.3x    | 23969         | 361        | 5405  | 2393  | 0.115   |
| 6    | PILOT-JA   | 39.5x    | 3417          | 87         | 1988  | 941   | 0.004   |
| 7    | CYCLE      | 35.6x    | 5184          | 145        | 2857  | 1904  | 0.211   |
| 8    | PILOTNOV   | 30.4x    | 2266          | 75         | 2172  | 976   | 0.033   |
| 9    | WOODW      | 26.0x    | 1872          | 72         | 8405  | 1099  | 0.000   |
| 10   | PILOT87    | 25.9x    | 85086         | 3288       | 4883  | 2031  | 0.134   |
| 11   | MODSZK1    | 20.5x    | 316           | 15         | 1620  | 688   | 0.611   |
| 12   | PILOT      | 20.3x    | 17726         | 872        | 3652  | 1442  | 0.015   |
| 13   | FIT2D      | 17.6x    | 2550          | 145        | 10500 | 26    | 0.857   |
| 14   | SCTAP3     | 16.8x    | 268           | 16         | 2480  | 1481  | 0.750   |
| 15   | CRE-A      | 15.9x    | 1021          | 64         | 4067  | 3517  | 1.000   |
| 16   | FIT1P      | 15.6x    | 570           | 37         | 1677  | 628   | 0.612   |
| 17   | DEGEN3     | 15.4x    | 2470          | 160        | 1818  | 1504  | 0.871   |
| 18   | 25FV47     | 14.5x    | 1599          | 110        | 1571  | 822   | 0.463   |
| 19   | PEROLD     | 12.8x    | 602           | 47         | 1376  | 626   | 0.006   |
| 20   | PILOT-WE   | 9.5x     | 1285          | 135        | 2789  | 723   | 0.033   |
| 21   | SEBA       | 8.1x     | 33            | 4          | 1028  | 516   | 0.508   |
| 22   | TRUSS      | 8.0x     | 22202         | 2767       | 8806  | 1001  | 1.000   |
| 23   | PDS-02     | 7.1x     | 530           | 74         | 7535  | 2954  | 0.645   |
| 24   | GANGES     | 7.1x     | 108           | 15         | 1681  | 1310  | 0.065   |
| 25   | KEN-07     | 6.8x     | 168           | 25         | 3602  | 2427  | 0.993   |
| 26   | SCTAP2     | 6.8x     | 84            | 12         | 1880  | 1091  | 0.750   |
| 27   | NESM       | 6.2x     | 1249          | 201        | 2923  | 663   | 0.239   |
| 28   | SCSD8      | 6.2x     | 247           | 40         | 2750  | 398   | 1.000   |
| 29   | WOOD1P     | 5.5x     | 432           | 79         | 2594  | 245   | 0.000   |
| 30   | MAROS      | 5.2x     | 198           | 38         | 1443  | 847   | 0.272   |
| 31   | GFRD-PNC   | 4.9x     | 33            | 7          | 1092  | 617   | 0.998   |
| 32   | BNL1       | 4.8x     | 127           | 26         | 1175  | 644   | 0.858   |
| 33   | SCRS8      | 4.5x     | 39            | 9          | 1169  | 491   | 0.725   |
| 34   | FINNIS     | 4.1x     | 20            | 5          | 614   | 498   | 0.658   |
| 35   | SCFXM3     | 4.0x     | 123           | 31         | 1371  | 991   | 0.050   |
| 36   | SCAGR25    | 3.9x     | 27            | 7          | 500   | 472   | 0.950   |
| 37   | D6CUBE     | 3.6x     | 495           | 137        | 6184  | 416   | 1.000   |
| 38   | PILOT4     | 3.4x     | 123           | 36         | 1000  | 411   | 0.004   |
| 39   | CZPROB     | 3.3x     | 116           | 35         | 3523  | 930   | 0.995   |
| 40   | FFFFF800   | 3.1x     | 40            | 13         | 854   | 525   | 0.009   |

Models where ojAlgo-D is FASTER than HiGHS:
AFIRO (0.08x), KB2 (0.19x), RECIPELP (0.20x), SC50B (0.20x), SC50A (0.23x),
ADLITTLE (0.22x), VTP-BASE (0.27x), BLEND (0.29x), GROW7 (0.33x),
STOCFOR1 (0.33x), SHARE2B (0.37x), QAP12 (0.39x), GROW15 (0.43x),
STANDATA (0.45x), STANDGUB (0.48x), SCAGR7 (0.49x), BEACONFD (0.51x),
GROW22 (0.54x), BOEING2 (0.54x), BORE3D (0.60x), SC105 (0.64x),
ISRAEL (0.71x), SCSD1 (0.74x), AGG3 (0.75x), STANDMPS (0.78x),
AGG (0.80x), AGG2 (0.78x), SHARE1B (0.84x).


## Solver Architecture Differences

### SimplexSolver (ojAlgo-LP-dual-D) — the target to improve

Source: src/main/java/org/ojalgo/optimisation/linear/SimplexSolver.java
Store:  DenseTableau (when -D) or RevisedStore (when -S)
Entry:  PhasedSimplexSolver.solve() → Phase-1 dual iterations → Phase-2 primal iterations

### SimplexTableauSolver — the reference (mature, tuned)

Source: src/main/java/org/ojalgo/optimisation/linear/SimplexTableauSolver.java
Store:  DenseTableau or SparseTableau
Entry:  solve() → Phase-1 primal + Phase-2 primal (classic 2-phase)


### Difference 1: Tolerance Contexts

SimplexSolver:
    PIVOT = NumberContext.of(6)           → isZero threshold ≈ 5e-7, relativeError = 1e-5
    RATIO = NumberContext.of(8)           → isZero threshold ≈ 5e-9, relativeError = 1e-7

SimplexTableauSolver:
    ACC       = NumberContext.of(12, 14)  → isZero threshold ≈ 5e-15, relativeError = 1e-11
    DEGENERATE= NumberContext.of(12, 8)   → isZero threshold ≈ 5e-9
    PHASE1    = NumberContext.of(12, 8)   → isZero threshold ≈ 5e-9
    PIVOT     = NumberContext.of(12, 8)   → isZero threshold ≈ 5e-9, relativeError = 1e-11
    RATIO     = NumberContext.of(12, 8)   → isZero threshold ≈ 5e-9, relativeError = 1e-11
    WEIGHT    = NumberContext.of(8, 10)   → isZero threshold ≈ 5e-11, relativeError = 1e-7

Impact: SimplexSolver's PIVOT.isZero(denom) rejects pivots with |denom| < 5e-7.
SimplexTableauSolver accepts pivots down to |denom| < 5e-9. This means SimplexSolver
may reject valid pivot candidates, especially in ill-conditioned problems.

Usage in SimplexSolver (3 locations):
  - testDualEnterRatio:   PIVOT.isZero(denom) to reject, PIVOT.isDifferent + RATIO.isDifferent for tie-break
  - testPrimalExitRatio:  PIVOT.isZero(denom) to reject, PIVOT.isDifferent + RATIO.isDifferent for tie-break


### Difference 2: Pivot Selection (Pricing)

SimplexSolver:
    Devex steepest-edge pricing. Candidates scored by (magnitude² / edgeWeight).
    Edge weights initialised to 1.0, grow monotonically via ratio²*w_p.
    Never reset during iterations (only at start of dual/primal phase).

SimplexTableauSolver:
    Dantzig pricing (most negative reduced cost).
    Tie-breaking via WEIGHT.isDifferent (effectively: pick significantly more negative).
    No edge weight tracking at all.

Impact: Devex pricing should reduce iteration count vs Dantzig, but the approximate
weights can degrade over many iterations, especially on degenerate problems. Weights
grow unboundedly (never shrink), potentially biasing selection toward stale candidates.


### Difference 3: Infeasibility / Reduced Cost Threshold

SimplexSolver:
    getDualExitCandidate:    magnitude > 1e-10  (hardcoded)
    getPrimalEnterCandidate: magnitude > 1e-10  (hardcoded)

SimplexTableauSolver:
    findNextPivotCol: tmpVal < -ACC.epsilon() (phase 2) or tmpVal < ZERO (phase 1)
    ACC.epsilon() = max(1e-11, 5e-15) ≈ 1e-11

Impact: SimplexSolver uses 1e-10 as a fixed threshold regardless of problem scale.
This may cause it to chase tiny infeasibilities that are really just numerical noise,
leading to unnecessary iterations on degenerate problems.


### Difference 4: Phase Strategy

SimplexSolver (PhasedSimplexSolver):
    Phase-1: dual simplex (drive to primal feasibility)
    Phase-2: primal simplex (optimise objective)

SimplexTableauSolver:
    Phase-1: primal simplex with artificial variables (drive artificials to zero)
    Phase-2: primal simplex (optimise objective)

Impact: The dual-then-primal strategy handles explicit bounds natively but may
struggle with highly degenerate problems where dual pivots make little progress.


### Difference 5: Degenerate Handling

SimplexTableauSolver has explicit degenerate handling in findNextPivotRow (lines 1133-1151):
    - When a phase-2 artificial variable has near-zero RHS, ratio is forced to ZERO
    - The denominator for tie-breaking is adjusted: max(denom, ONE)
    This prevents cycling and ensures degenerate artificials are pivoted out quickly.

SimplexSolver has no equivalent mechanism.


### Difference 6: Ratio Test Tie-Breaking

SimplexSolver (testDualEnterRatio, testPrimalExitRatio):
    Primary:   smallest ratio
    Secondary: prefer larger |pivot element| when ratio is within RATIO tolerance
    Logic:     ratio < iterationRatio || (scale > iterationScale && PIVOT.isDifferent(...) && !RATIO.isDifferent(...))

SimplexTableauSolver (findNextPivotRow):
    Primary:   smallest ratio (non-negative)
    Secondary: prefer larger pivot element when ratio is within RATIO tolerance
    Logic:     ratio < minRatio || (!RATIO.isDifferent(minRatio, ratio) && denom > curDenom)

The logic is similar in intent but differs in precision thresholds and details.


## Work Log

Each entry: one model, one problem identified, one fix applied.

### Model 1: 80BAU3B (172.9x slowdown)

Status: INVESTIGATING
Characteristics: 9799 vars, 2263 exprs, density 0.823 (very dense)

Analysis:
  TODO — run with iteration counting, compare dual phase-1 / primal phase-2 iteration counts

Root cause:
  TODO

Fix applied:
  TODO

Result:
  TODO

---

### Model 2: BNL2 (109.1x slowdown)

Status: PENDING

---

### Model 3: STOCFOR2 (107.2x slowdown)

Status: PENDING

---

### Model 4: MAROS-R7 (80.2x slowdown)

Status: PENDING

---

### Model 5: GREENBEB (66.3x slowdown)

Status: PENDING

---

### Model 6: PILOT-JA (39.5x slowdown)

Status: PENDING

---

### Model 7: CYCLE (35.6x slowdown)

Status: PENDING

---

### Model 8: PILOTNOV (30.4x slowdown)

Status: PENDING

---

### Model 9: WOODW (26.0x slowdown)

Status: PENDING

---

### Model 10: PILOT87 (25.9x slowdown)

Status: PENDING

---

(Further models to be added as work progresses)
