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

import org.ojalgo.function.special.MissingMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.IntermediateSolver;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutConfiguration;

public final class NodeSolver extends IntermediateSolver {

    abstract static class Separator {

        static final boolean DEBUG = false;

        final ExpressionsBasedModel model;

        Separator(final ExpressionsBasedModel ebm) {
            super();
            model = ebm;
        }

    }

    private static final boolean DEBUG = false;

    private boolean myCutRoundDone = false;
    private transient FlowCoverSeparator myFlowCoverSeparator = null;
    private transient GMISeparator myGMISeparator = null;
    private Boolean myInPlaceBoundUpdateSafe = null;
    private transient MIRSeparator myMIRSeparator = null;

    NodeSolver(final ExpressionsBasedModel model) {
        super(model);
    }

    private boolean generateCuts(final CutConfiguration configGMI, final CutConfiguration configMIR, final CutConfiguration configFC) {

        ExpressionsBasedModel model = this.getModel();
        Result result = this.getResult();

        int roundsFC = configFC != null ? configFC.iterations : 0;
        int roundsMIR = configMIR != null ? configMIR.iterations : 0;
        int roundsGMI = configGMI != null ? configGMI.iterations : 0;

        int countFC = 0;
        int countMIR = 0;
        int countGMI = 0;

        int maxRounds = MissingMath.max(roundsFC, roundsMIR, roundsGMI);

        boolean retVal = false;

        for (int round = 0; round < maxRounds; round++) {

            countFC = 0;
            countMIR = 0;
            countGMI = 0;

            if (!this.isSolved()) {
                break;
            }

            if (round < roundsFC) {
                if (myFlowCoverSeparator == null) {
                    myFlowCoverSeparator = new FlowCoverSeparator(model);
                }
                countFC = myFlowCoverSeparator.generateCuts(result, configFC);
                if (DEBUG) {
                    BasicLogger.debug("{} new FC cuts, iteration {}", countFC, 1 + round);
                }
            }

            if (round < roundsMIR) {
                if (myMIRSeparator == null) {
                    myMIRSeparator = new MIRSeparator(model);
                }
                countMIR = myMIRSeparator.generateCuts(result, configMIR);
                if (DEBUG) {
                    BasicLogger.debug("{} new MIR cuts, iteration {}", countMIR, 1 + round);
                }
            }

            if (round < roundsGMI && this.getSolver() instanceof UpdatableSolver) {
                if (myGMISeparator == null) {
                    myGMISeparator = new GMISeparator(model);
                }
                countGMI = myGMISeparator.generateCuts((UpdatableSolver) this.getSolver(), result, configGMI);
                if (DEBUG) {
                    BasicLogger.debug("{} new GMI cuts, iteration {}", countGMI, 1 + round);
                }
            }

            if ((countFC + countMIR + countGMI) > 0) {

                retVal = true;

                this.reset();
                result = this.getResult();

                if (result == null || !result.getState().isOptimal()) {
                    break;
                }

            } else {

                break;
            }
        }

        return retVal;
    }

    boolean generateCuts(final ModelStrategy strategy) {

        CutConfiguration gmi = strategy.getGMICutConfiguration();
        CutConfiguration mir = strategy.getMIRCutConfiguration();
        CutConfiguration fc = strategy.getFCCutConfiguration();

        if (this.generateCuts(gmi, mir, fc)) {
            this.reset();
            myCutRoundDone = true;
            return true;
        } else {
            return false;
        }
    }

    double getReducedGradient(final int globalModelIndex) {
        if (this.isSolved() && this.getSolver() instanceof UpdatableSolver) {
            int indexInSolver = this.getIndexInSolver(globalModelIndex);
            if (indexInSolver >= 0) {
                return ((UpdatableSolver) this.getSolver()).getReducedGradient(indexInSolver);
            }
        }
        return 0.0;
    }

    boolean isCutRoundDone() {
        return myCutRoundDone;
    }

    /**
     * Whether this node's relaxation can absorb a branch-induced bound change in place (via
     * {@code update(variable)}) instead of being rebuilt from scratch.
     * <p>
     * True only for the linear/simplex relaxation: there {@code SimplexSolver.updateRange} genuinely applies
     * the change in place. A quadratic relaxation is solved by a convex solver — ADMM drifts when
     * warm-started across bound changes and the active-set path effectively rebuilds anyway — so those must
     * keep the historical, numerically-stable behaviour of rebuilding fresh on every branch
     * ({@link NodeKey#enforceBounds(NodeSolver, ModelStrategy)} forces a {@code reset()} for them, as the
     * original over-broad sign-change condition implicitly did). The model's structure is fixed for this
     * solver's lifetime, so the answer is cached.
     */
    boolean isInPlaceBoundUpdateSafe() {

        if (myInPlaceBoundUpdateSafe == null) {
            myInPlaceBoundUpdateSafe = Boolean.valueOf(!this.getModel().isAnyExpressionQuadratic());
        }

        return myInPlaceBoundUpdateSafe.booleanValue();
    }

}
