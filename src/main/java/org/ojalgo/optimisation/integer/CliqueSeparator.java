/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.optimisation.integer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map.Entry;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Generates clique cuts from a conflict graph over binary literals.
 * <p>
 * A literal is either a binary variable {@code x} or its complement {@code 1 - x}. Two literals conflict if
 * they cannot both be 1. Conflicts are extracted from the pure-integer knapsack rows of the model: after
 * complementing negative coefficients so that all are positive, literals {@code i} and {@code j} of a row
 * {@code sum(a_j * l_j) <= b} conflict when {@code a_i + a_j > b}. For each row the largest clique (a prefix
 * of the literals sorted by decreasing coefficient) is stored, together with the clique each remaining
 * literal forms with the prefix it conflicts with.
 * <p>
 * Separation greedily grows cliques from the literals with the largest LP values, combining conflicts from
 * different rows. A clique {@code C} yields the cut {@code sum_{C}(l_j) <= 1}, which is added when the LP
 * point violates it.
 */
final class CliqueSeparator extends NodeSolver.Separator {

    /**
     * Upper limit on the number of conflict pairs stored, to bound memory on models with very long rows. The
     * graph stays valid (only true conflicts are ever recorded) but may be incomplete.
     */
    private static final long MAX_PAIRS = 4_000_000L;
    private static final double TOL = 1e-6;
    private static final NumberContext TOLERANCE = NumberContext.of(4);

    static final IntegerStrategy.CutConfiguration CONFIGURATION = new IntegerStrategy.CutConfiguration().withIterations(3);

    private static boolean isComplemented(final int literal) {
        return (literal & 1) == 1;
    }

    private static int literal(final int variableIndex, final boolean complemented) {
        return 2 * variableIndex + (complemented ? 1 : 0);
    }

    private static int variableOf(final int literal) {
        return literal >> 1;
    }

    private BitSet[] myAdjacency = null;
    private long myNbPairs = 0L;

    CliqueSeparator(final ExpressionsBasedModel ebm) {
        super(ebm);
    }

    private void addClique(final int[] literals, final int size) {
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                this.addConflict(literals[i], literals[j]);
            }
        }
    }

    private void addConflict(final int l1, final int l2) {
        if (myNbPairs >= MAX_PAIRS) {
            return;
        }
        if (myAdjacency[l1] == null) {
            myAdjacency[l1] = new BitSet();
        }
        if (myAdjacency[l2] == null) {
            myAdjacency[l2] = new BitSet();
        }
        if (!myAdjacency[l1].get(l2)) {
            myAdjacency[l1].set(l2);
            myAdjacency[l2].set(l1);
            myNbPairs++;
        }
    }

    private void buildConflictGraph() {

        myAdjacency = new BitSet[2 * model.countVariables()];

        for (Expression constraint : model.getExpressions()) {

            if (constraint.isObjective() || !constraint.isConstraint() || !constraint.isInteger() || constraint.isAnyQuadraticFactorNonZero()) {
                continue;
            }

            if (constraint.isUpperLimitSet()) {
                this.extractCliques(constraint, true);
            }
            if (constraint.isLowerLimitSet()) {
                this.extractCliques(constraint, false);
            }
        }
    }

    private boolean conflicts(final int l1, final int l2) {
        if (l1 == (l2 ^ 1)) {
            return true;
        }
        BitSet adjacent = myAdjacency[l1];
        return adjacent != null && adjacent.get(l2);
    }

    private boolean conflictsWithAll(final int literal, final int[] clique, final int size) {
        for (int i = 0; i < size; i++) {
            if (!this.conflicts(literal, clique[i])) {
                return false;
            }
        }
        return true;
    }

    private void extractCliques(final Expression constraint, final boolean useUpper) {

        BigDecimal rhs = useUpper ? constraint.getUpperLimit() : constraint.getLowerLimit();
        if (rhs == null) {
            return;
        }

        boolean negate = !useUpper;
        BigDecimal effectiveRHS = negate ? rhs.negate() : rhs;

        List<Integer> literals = new ArrayList<>();
        List<BigDecimal> coeffs = new ArrayList<>();

        for (Entry<IntIndex, BigDecimal> entry : constraint.getLinearEntrySet()) {
            int idx = entry.getKey().index;
            Variable var = model.getVariable(idx);
            BigDecimal coeff = negate ? entry.getValue().negate() : entry.getValue();

            if (var.isBinary()) {
                if (coeff.signum() < 0) {
                    literals.add(Integer.valueOf(CliqueSeparator.literal(idx, true)));
                    coeffs.add(coeff.negate());
                    effectiveRHS = effectiveRHS.subtract(coeff);
                } else if (coeff.signum() > 0) {
                    literals.add(Integer.valueOf(CliqueSeparator.literal(idx, false)));
                    coeffs.add(coeff);
                }
            } else {
                BigDecimal minContrib;
                if (coeff.signum() > 0) {
                    BigDecimal lb = var.getLowerLimit();
                    if (lb == null) {
                        return;
                    }
                    minContrib = coeff.multiply(lb);
                } else if (coeff.signum() < 0) {
                    BigDecimal ub = var.getUpperLimit();
                    if (ub == null) {
                        return;
                    }
                    minContrib = coeff.multiply(ub);
                } else {
                    continue;
                }
                effectiveRHS = effectiveRHS.subtract(minContrib);
            }
        }

        int n = literals.size();
        if (n < 2 || effectiveRHS.signum() <= 0) {
            return;
        }

        double rhsD = effectiveRHS.doubleValue();
        double minExcess = Math.max(10.0 * TOL, TOL * rhsD);

        Integer[] order = new Integer[n];
        double[] a = new double[n];
        for (int i = 0; i < n; i++) {
            order[i] = Integer.valueOf(i);
            a[i] = coeffs.get(i).doubleValue();
        }
        Arrays.sort(order, (p, q) -> Double.compare(a[q], a[p]));

        int[] sorted = new int[n];
        double[] sortedCoeff = new double[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = literals.get(order[i]).intValue();
            sortedCoeff[i] = a[order[i]];
        }

        // Largest prefix clique: the prefix of size k is a clique iff its two smallest members conflict.
        int k = 0;
        for (int i = 1; i < n; i++) {
            if (sortedCoeff[i - 1] + sortedCoeff[i] - rhsD > minExcess) {
                k = i + 1;
            } else {
                break;
            }
        }

        if (k < 2) {
            return;
        }

        this.addClique(sorted, k);

        // Each remaining literal j forms a clique with the prefix of members it conflicts with.
        int[] buffer = new int[k + 1];
        int m = k - 1;
        for (int j = k; j < n; j++) {
            while (m > 0 && sortedCoeff[m - 1] + sortedCoeff[j] - rhsD <= minExcess) {
                m--;
            }
            if (m == 0) {
                break;
            }
            System.arraycopy(sorted, 0, buffer, 0, m);
            buffer[m] = sorted[j];
            this.addClique(buffer, m + 1);
        }
    }

    int generateCuts(final Optimisation.Result solution, final CutConfiguration configuration) {

        int nbBefore = model.countExpressions();

        if (myAdjacency == null) {
            this.buildConflictGraph();
        }

        int maxCuts = configuration.getMaxCuts(model.countVariables());
        int nbAdded = 0;

        int nbVariables = model.countVariables();

        // Candidate literals: those with a positive LP value and at least one known conflict
        List<Integer> candidates = new ArrayList<>();
        double[] values = new double[2 * nbVariables];
        for (int v = 0; v < nbVariables; v++) {
            int pos = CliqueSeparator.literal(v, false);
            int neg = CliqueSeparator.literal(v, true);
            if (myAdjacency[pos] == null && myAdjacency[neg] == null) {
                continue;
            }
            if (!model.getVariable(v).isBinary()) {
                continue;
            }
            double x = solution.doubleValue(v);
            values[pos] = x;
            values[neg] = 1.0 - x;
            if (x > TOL && myAdjacency[pos] != null) {
                candidates.add(Integer.valueOf(pos));
            }
            if (1.0 - x > TOL && myAdjacency[neg] != null) {
                candidates.add(Integer.valueOf(neg));
            }
        }

        if (candidates.size() < 2) {
            return 0;
        }

        candidates.sort((p, q) -> Double.compare(values[q.intValue()], values[p.intValue()]));

        int nbCandidates = candidates.size();
        int[] candidate = new int[nbCandidates];
        for (int i = 0; i < nbCandidates; i++) {
            candidate[i] = candidates.get(i).intValue();
        }

        boolean[] used = new boolean[2 * nbVariables];
        int[] clique = new int[2 * nbVariables];

        for (int s = 0; s < nbCandidates && nbAdded < maxCuts; s++) {

            int start = candidate[s];
            if (used[start] || values[start] >= 1.0 - TOL) {
                continue;
            }

            int size = 0;
            clique[size++] = start;
            double sum = values[start];

            for (int t = s + 1; t < nbCandidates; t++) {
                int literal = candidate[t];
                if (this.conflictsWithAll(literal, clique, size)) {
                    clique[size++] = literal;
                    sum += values[literal];
                }
            }

            double violation = sum - 1.0;
            if (violation <= 0 || TOLERANCE.isZero(violation)) {
                continue;
            }

            // Strengthen with zero-valued literals that conflict with every member
            BitSet extension = (BitSet) myAdjacency[start].clone();
            for (int i = 1; i < size && !extension.isEmpty(); i++) {
                BitSet adjacent = myAdjacency[clique[i]];
                if (adjacent == null) {
                    extension.clear();
                } else {
                    extension.and(adjacent);
                }
            }
            for (int literal = extension.nextSetBit(0); literal >= 0; literal = extension.nextSetBit(literal + 1)) {
                if (values[literal] <= TOL && model.getVariable(CliqueSeparator.variableOf(literal)).isBinary()
                        && this.conflictsWithAll(literal, clique, size)) {
                    clique[size++] = literal;
                }
            }

            String name = "CUT_CL_" + COUNTER.incrementAndGet();
            Expression cut = model.newExpression(name);

            int nbComplemented = 0;
            for (int i = 0; i < size; i++) {
                int literal = clique[i];
                int idx = CliqueSeparator.variableOf(literal);
                if (CliqueSeparator.isComplemented(literal)) {
                    cut.add(idx, BigDecimal.ONE.negate());
                    nbComplemented++;
                } else {
                    cut.add(idx, BigDecimal.ONE);
                }
            }
            cut.upper(BigDecimal.valueOf(1 - nbComplemented));

            if (model.checkSimilarity(cut)) {
                model.removeExpression(name);
                continue;
            }

            for (int i = 0; i < size; i++) {
                used[clique[i]] = true;
            }
            nbAdded++;
        }

        return model.countExpressions() - nbBefore;
    }

    /**
     * CLique
     */
    @Override
    String type() {
        return "CL";
    }

}
