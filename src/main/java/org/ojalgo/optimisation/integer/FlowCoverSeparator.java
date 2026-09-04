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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

/**
 * Generates single-node flow cover cuts for models with variable upper bound (VUB) structure.
 * <p>
 * Detects constraints of the form {@code x - M*y <= 0} where y is binary, finds the associated
 * flow-conservation equality containing x, and generates tightened inequalities that replace the big-M
 * capacity with the local demand.
 */
final class FlowCoverSeparator extends NodeSolver.Separator {

    static final class FlowArc {

        final BigDecimal coefficient;
        final IntIndex variable;

        FlowArc(final IntIndex index, final BigDecimal value) {
            variable = index;
            coefficient = value;
        }
    }

    /**
     * A detected VUB constraint: x <= M * y, with an associated flow-conservation equality.
     */
    static final class VUBNode {

        final BigDecimal bigM;
        final BigDecimal demand;
        final List<FlowArc> inflows;
        final List<FlowArc> outflows;
        final IntIndex xIndex;
        final IntIndex yIndex;

        VUBNode(final IntIndex xIndex, final IntIndex yIndex, final BigDecimal bigM, final BigDecimal demand, final List<FlowArc> inflows,
                final List<FlowArc> outflows) {
            this.xIndex = xIndex;
            this.yIndex = yIndex;
            this.bigM = bigM;
            this.demand = demand;
            this.inflows = inflows;
            this.outflows = outflows;
        }
    }

    private static final NumberContext TOLERANCE = NumberContext.of(4);

    static final IntegerStrategy.CutConfiguration CONFIGURATION = new IntegerStrategy.CutConfiguration().withIterations(3);

    private List<VUBNode> myNodes;

    FlowCoverSeparator(final ExpressionsBasedModel ebm) {
        super(ebm);
    }

    /**
     * Detect VUB + flow-balance structure. Called once per model; the result is cached.
     */
    private void detectStructure() {

        if (myNodes != null) {
            return;
        } else {
            myNodes = new ArrayList<>();
        }

        List<Variable> variables = model.getVariables();

        Map<IntIndex, List<Expression>> equalitiesByVar = new HashMap<>();
        List<Expression> candidates = new ArrayList<>();

        for (Expression expr : model.getExpressions()) {
            if (!expr.isConstraint() || expr.isAnyQuadraticFactorNonZero()) {
                continue;
            }

            if (expr.isEqualityConstraint()) {
                for (IntIndex key : expr.getLinearKeySet()) {
                    equalitiesByVar.computeIfAbsent(key, k -> new ArrayList<>()).add(expr);
                }
            } else if (expr.isUpperConstraint() && expr.getLinearKeySet().size() == 2) {
                candidates.add(expr);
            }
        }

        Set<IntIndex> vubXIndices = new HashSet<>();
        List<IntIndex[]> vubPairs = new ArrayList<>();
        Map<IntIndex, BigDecimal> vubBigMs = new HashMap<>();

        for (Expression candidate : candidates) {

            IntIndex xIdx = null;
            IntIndex yIdx = null;
            BigDecimal xCoeff = null;
            BigDecimal yCoeff = null;

            for (Entry<IntIndex, BigDecimal> entry : candidate.getLinearEntrySet()) {
                IntIndex idx = entry.getKey();
                BigDecimal coeff = entry.getValue();
                Variable var = variables.get(idx.index);

                if (var.isBinary()) {
                    yIdx = idx;
                    yCoeff = coeff;
                } else {
                    xIdx = idx;
                    xCoeff = coeff;
                }
            }

            if (xIdx == null || yIdx == null) {
                continue;
            }

            BigDecimal upperLimit = candidate.getUpperLimit();
            if (upperLimit == null) {
                upperLimit = BigDecimal.ZERO;
            }

            if (xCoeff.compareTo(BigDecimal.ONE) != 0 || yCoeff.signum() >= 0 || upperLimit.signum() != 0) {
                continue;
            }

            BigDecimal bigM = yCoeff.negate();
            if (bigM.signum() <= 0) {
                continue;
            }

            vubXIndices.add(xIdx);
            vubPairs.add(new IntIndex[] { xIdx, yIdx });
            vubBigMs.put(xIdx, bigM);
        }

        for (IntIndex[] pair : vubPairs) {
            IntIndex xIdx = pair[0];
            IntIndex yIdx = pair[1];
            BigDecimal bigM = vubBigMs.get(xIdx);

            if (bigM == null || bigM.signum() <= 0) {
                continue;
            }

            List<Expression> equalities = equalitiesByVar.get(xIdx);
            if (equalities == null || equalities.isEmpty()) {
                continue;
            }

            Expression flowBalance = null;
            BigDecimal xInFlow = null;

            for (Expression eq : equalities) {
                BigDecimal xInEq = eq.get(xIdx);
                if (xInEq == null || xInEq.abs().compareTo(BigDecimal.ONE) != 0) {
                    continue;
                }

                BigDecimal yInEq = eq.get(yIdx);
                if (yInEq != null && yInEq.signum() != 0) {
                    continue;
                }

                int vubXCount = 0;
                for (IntIndex key : eq.getLinearKeySet()) {
                    if (vubXIndices.contains(key)) {
                        vubXCount++;
                    }
                }

                if (vubXCount >= 1) {
                    flowBalance = eq;
                    xInFlow = xInEq;
                    break;
                }
            }

            if (flowBalance == null) {
                continue;
            }

            BigDecimal lowerLimit = flowBalance.getLowerLimit();
            BigDecimal fbUpperLimit = flowBalance.getUpperLimit();
            if (lowerLimit == null || fbUpperLimit == null) {
                continue;
            }

            BigDecimal adjustedDemand = xInFlow.signum() > 0 ? lowerLimit : lowerLimit.negate();
            if (adjustedDemand.signum() <= 0) {
                continue;
            }

            List<FlowArc> inflows = new ArrayList<>();
            List<FlowArc> outflows = new ArrayList<>();
            boolean validFlowArcs = true;

            for (Entry<IntIndex, BigDecimal> entry : flowBalance.getLinearEntrySet()) {
                IntIndex idx = entry.getKey();
                BigDecimal coeff = entry.getValue();

                if (idx.equals(xIdx)) {
                    continue;
                }

                Variable flowVar = variables.get(idx.index);
                if (flowVar.isInteger()) {
                    validFlowArcs = false;
                    break;
                }
                if (flowVar.getLowerLimit() == null || flowVar.getLowerLimit().signum() < 0) {
                    validFlowArcs = false;
                    break;
                }

                if (coeff.signum() == xInFlow.signum()) {
                    inflows.add(new FlowArc(idx, coeff));
                } else {
                    outflows.add(new FlowArc(idx, coeff));
                }
            }

            if (!validFlowArcs) {
                continue;
            }

            if (adjustedDemand.compareTo(bigM) >= 0) {
                continue;
            }

            myNodes.add(new VUBNode(xIdx, yIdx, bigM, adjustedDemand, inflows, outflows));
        }
    }

    int countVUBNodes() {
        this.detectStructure();
        return myNodes.size();
    }

    /**
     * Generate violated flow cover cuts for the current LP solution. Returns true if any cuts were added.
     *
     * @param configuration TODO
     */
    int generateCuts(final Optimisation.Result solution, final CutConfiguration configuration) {

        int nbBefore = model.countExpressions();

        this.detectStructure();

        if (myNodes.isEmpty()) {
            return 0;
        }

        boolean added = false;

        for (VUBNode node : myNodes) {

            double xVal = solution.doubleValue(node.xIndex.index);
            double yVal = solution.doubleValue(node.yIndex.index);
            double demand = node.demand.doubleValue();

            // Cut Type B (tightened VUB): x <= demand * y + sum(|outflow|)
            double typeB_lhs = xVal - demand * yVal;
            for (FlowArc arc : node.outflows) {
                typeB_lhs -= arc.coefficient.abs().doubleValue() * solution.doubleValue(arc.variable.index);
            }

            if (typeB_lhs > 0 && !TOLERANCE.isZero(typeB_lhs)) {
                String name = "CUT_FC_B_" + COUNTER.incrementAndGet();
                Expression cut = model.newExpression(name);

                cut.add(node.xIndex.index, BigDecimal.ONE);
                cut.add(node.yIndex.index, node.demand.negate());
                for (FlowArc arc : node.outflows) {
                    cut.add(arc.variable.index, arc.coefficient.abs().negate());
                }
                cut.upper(BigDecimal.ZERO);

                if (!model.checkSimilarity(cut)) {
                    added = true;
                } else {
                    model.removeExpression(name);
                }
            }

            // Cut Type A (demand coverage): sum(inflow) >= demand * (1 - y)
            double typeA_lhs = demand * (1.0 - yVal);
            for (FlowArc arc : node.inflows) {
                typeA_lhs -= arc.coefficient.abs().doubleValue() * solution.doubleValue(arc.variable.index);
            }

            if (typeA_lhs > 0 && !TOLERANCE.isZero(typeA_lhs)) {
                String name = "CUT_FC_A_" + COUNTER.incrementAndGet();
                Expression cut = model.newExpression(name);

                for (FlowArc arc : node.inflows) {
                    cut.add(arc.variable.index, arc.coefficient.abs());
                }
                cut.add(node.yIndex.index, node.demand);
                cut.lower(node.demand);

                if (!model.checkSimilarity(cut)) {
                    added = true;
                } else {
                    model.removeExpression(name);
                }
            }
        }

        return model.countExpressions() - nbBefore;
    }

    /**
     * Flow Cover
     */
    @Override
    String type() {
        return "FC";
    }

}
