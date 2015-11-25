/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.ojalgo.access.AccessUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.CharacterRing;
import org.ojalgo.netio.CharacterRing.PrinterBuffer;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.TypeUtils;

/**
 * IntegerSolver
 *
 * @author apete
 */
public final class OldIntegerSolver extends IntegerSolver {

    final class BranchAndBoundNodeTask extends RecursiveTask<Boolean> {

        private final NodeKey myKey;

        private BranchAndBoundNodeTask(final NodeKey key) {

            super();

            myKey = key;
        }

        BranchAndBoundNodeTask() {

            super();

            myKey = new NodeKey(OldIntegerSolver.this.getModel());
        }

        @Override
        public String toString() {
            return myKey.toString();
        }

        @Override
        protected Boolean compute() {

            if (OldIntegerSolver.this.isDebug()) {
                OldIntegerSolver.this.debug("\nBranch&Bound Node");
                OldIntegerSolver.this.debug(myKey.toString());
                OldIntegerSolver.this.debug(OldIntegerSolver.this.toString());
            }

            if (!OldIntegerSolver.this.isIterationAllowed() || !OldIntegerSolver.this.isIterationNecessary()) {
                if (OldIntegerSolver.this.isDebug()) {
                    OldIntegerSolver.this.debug("Reached iterations or time limit - stop!");
                }
                return false;
            }

            if (OldIntegerSolver.this.isExplored(this)) {
                if (OldIntegerSolver.this.isDebug()) {
                    OldIntegerSolver.this.debug("Node previously explored!");
                }
                return true;
            } else {
                OldIntegerSolver.this.markAsExplored(this);
            }

            if (!OldIntegerSolver.this.isGoodEnoughToContinueBranching(myKey.objective)) {
                if (OldIntegerSolver.this.isDebug()) {
                    OldIntegerSolver.this.debug("No longer a relevant node!");
                }
                return true;
            }

            ExpressionsBasedModel tmpModel = this.getModel();
            final Optimisation.Result tmpResult = tmpModel.solve(OldIntegerSolver.this.getBestResultSoFar());

            OldIntegerSolver.this.incrementIterationsCount();

            if ((tmpModel.options.debug_appender != null) && (tmpModel.options.debug_appender instanceof PrinterBuffer)) {
                if (OldIntegerSolver.this.getModel().options.debug_appender != null) {
                    ((PrinterBuffer) tmpModel.options.debug_appender).flush(OldIntegerSolver.this.getModel().options.debug_appender);
                }
            }

            if (tmpResult.getState().isOptimal()) {
                if (OldIntegerSolver.this.isDebug()) {
                    OldIntegerSolver.this.debug("Node solved to optimality!");
                }

                if (OldIntegerSolver.this.options.validate && !tmpModel.validate(tmpResult)) {
                    // This should not be possible. There is a bug somewhere.
                    OldIntegerSolver.this.debug("Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!");
                    //                    IntegerSolver.this.logDebug(myKey.toString());
                    //                    IntegerSolver.this.logDebug(tmpModel.toString());
                    //                    final GenericSolver tmpDefaultSolver = tmpModel.getDefaultSolver();
                    //                    tmpDefaultSolver.solve();
                    //                    IntegerSolver.this.logDebug(tmpDefaultSolver.toString());
                    return false;
                }

                final int tmpBranchIndex = OldIntegerSolver.this.identifyNonIntegerVariable(tmpResult, myKey);
                final double tmpSolutionValue = OldIntegerSolver.this.evaluateFunction(tmpResult);

                if (tmpBranchIndex == -1) {
                    if (OldIntegerSolver.this.isDebug()) {
                        OldIntegerSolver.this.debug("Integer solution! Store it among the others, and stop this branch!");
                    }

                    final Optimisation.Result tmpIntegerSolutionResult = new Optimisation.Result(Optimisation.State.FEASIBLE, tmpSolutionValue, tmpResult);

                    OldIntegerSolver.this.markInteger(myKey, tmpIntegerSolutionResult);

                    if (OldIntegerSolver.this.isDebug()) {
                        OldIntegerSolver.this.debug(OldIntegerSolver.this.getBestResultSoFar().toString());
                        BasicLogger.debug();
                        BasicLogger.debug(OldIntegerSolver.this.toString());
                        // BasicLogger.debug(DaemonPoolExecutor.INSTANCE.toString());
                    }

                } else {
                    if (OldIntegerSolver.this.isDebug()) {
                        OldIntegerSolver.this.debug("Not an Integer Solution: " + tmpSolutionValue);
                    }

                    final double tmpVariableValue = tmpResult.doubleValue(OldIntegerSolver.this.getGlobalIndex(tmpBranchIndex));

                    if (OldIntegerSolver.this.isGoodEnoughToContinueBranching(tmpSolutionValue)) {
                        if (OldIntegerSolver.this.isDebug()) {
                            OldIntegerSolver.this.debug("Still hope, branching on {} @ {} >>> {}", tmpBranchIndex, tmpVariableValue,
                                    tmpModel.getVariable(OldIntegerSolver.this.getGlobalIndex(tmpBranchIndex)));
                        }

                        tmpModel.dispose();
                        tmpModel = null;

                        final BranchAndBoundNodeTask tmpLowerBranchTask = this.createLowerBranch(tmpBranchIndex, tmpVariableValue, tmpSolutionValue);
                        final BranchAndBoundNodeTask tmpUpperBranchTask = this.createUpperBranch(tmpBranchIndex, tmpVariableValue, tmpSolutionValue);

                        //   return tmpLowerBranchTask.compute() && tmpUpperBranchTask.compute();

                        tmpUpperBranchTask.fork();

                        final boolean tmpLowerBranchValue = tmpLowerBranchTask.compute();

                        if (tmpLowerBranchValue) {
                            return tmpUpperBranchTask.join();
                        } else {
                            tmpUpperBranchTask.tryUnfork();
                            tmpUpperBranchTask.cancel(true);
                            return false;
                        }

                    } else {
                        if (OldIntegerSolver.this.isDebug()) {
                            OldIntegerSolver.this.debug("Can't find better integer solutions - stop this branch!");
                        }
                    }
                }

            } else {
                if (OldIntegerSolver.this.isDebug()) {
                    OldIntegerSolver.this.debug("Failed to solve problem - stop this branch!");
                }
            }

            return true;
        }

        BranchAndBoundNodeTask createLowerBranch(final int branchIndex, final double nonIntegerValue, final double parentObjectiveValue) {

            final NodeKey tmpKey = myKey.createLowerBranch(branchIndex, nonIntegerValue, parentObjectiveValue);

            return new BranchAndBoundNodeTask(tmpKey);
        }

        BranchAndBoundNodeTask createUpperBranch(final int branchIndex, final double nonIntegerValue, final double parentObjectiveValue) {

            final NodeKey tmpKey = myKey.createUpperBranch(branchIndex, nonIntegerValue, parentObjectiveValue);

            return new BranchAndBoundNodeTask(tmpKey);
        }

        NodeKey getKey() {
            return myKey;
        }

        ExpressionsBasedModel getModel() {

            final ExpressionsBasedModel retVal = OldIntegerSolver.this.getModel().relax(false);

            if (retVal.options.debug_appender != null) {
                retVal.options.debug_appender = new CharacterRing().asPrinter();
            }

            final int[] tmpIntegerIndeces = OldIntegerSolver.this.getIntegerIndeces();
            for (int i = 0; i < tmpIntegerIndeces.length; i++) {

                final BigDecimal tmpLowerBound = myKey.getLowerBound(i);
                final BigDecimal tmpUpperBound = myKey.getUpperBound(i);

                final Variable tmpVariable = retVal.getVariable(tmpIntegerIndeces[i]);
                tmpVariable.lower(tmpLowerBound);
                tmpVariable.upper(tmpUpperBound);

                BigDecimal tmpValue = tmpVariable.getValue();
                if (tmpValue != null) {
                    if (tmpLowerBound != null) {
                        tmpValue = tmpValue.max(tmpLowerBound);
                    }
                    if (tmpUpperBound != null) {
                        tmpValue = tmpValue.min(tmpUpperBound);
                    }
                    tmpVariable.setValue(tmpValue);
                }
            }

            if (OldIntegerSolver.this.isIntegerSolutionFound()) {
                final double tmpBestValue = OldIntegerSolver.this.getBestResultSoFar().getValue();
                final double tmpGap = Math.abs(tmpBestValue * OldIntegerSolver.this.options.mip_gap);
                if (retVal.isMinimisation()) {
                    retVal.limitObjective(null, TypeUtils.toBigDecimal(tmpBestValue - tmpGap, OldIntegerSolver.this.options.problem));
                } else {
                    retVal.limitObjective(TypeUtils.toBigDecimal(tmpBestValue + tmpGap, OldIntegerSolver.this.options.problem), null);
                }
            }

            return retVal;
        }

    }

    private final Set<NodeKey> myExploredNodes = Collections.synchronizedSet(new HashSet<NodeKey>());
    private final int[] myIntegerIndeces;

    OldIntegerSolver(final ExpressionsBasedModel model, final Options solverOptions) {

        super(model, solverOptions);

        final List<Variable> tmpIntegerVariables = model.getIntegerVariables();

        myIntegerIndeces = new int[tmpIntegerVariables.size()];

        for (int i = 0; i < myIntegerIndeces.length; i++) {
            final Variable tmpVariable = tmpIntegerVariables.get(i);
            myIntegerIndeces[i] = model.indexOf(tmpVariable);
        }

        //options.debug = System.out;
    }

    public Result solve(final Result kickStarter) {

        // Must verify that it actually is an integer solution
        // The kickStarter may be user-supplied
        if ((kickStarter != null) && kickStarter.getState().isFeasible() && this.getModel().validate(kickStarter)) {
            this.markInteger(null, kickStarter);
        }

        this.resetIterationsCount();

        final BranchAndBoundNodeTask tmpNodeTask = new BranchAndBoundNodeTask();

        final boolean tmpNormalExit = ForkJoinPool.commonPool().invoke(tmpNodeTask);

        Optimisation.Result retVal = this.getBestResultSoFar();

        if (retVal.getState().isFeasible()) {

            if (tmpNormalExit) {
                retVal = new Optimisation.Result(State.OPTIMAL, retVal);
            } else {
                retVal = new Optimisation.Result(State.FEASIBLE, retVal);
            }

        } else {

            if (tmpNormalExit) {
                retVal = new Optimisation.Result(State.INFEASIBLE, retVal);
            } else {
                retVal = new Optimisation.Result(State.FAILED, retVal);
            }
        }

        return retVal;
    }

    @Override
    public String toString() {
        return TypeUtils.format("Solutions={} Nodes/Iterations={} {}", this.countIntegerSolutions(), this.countExploredNodes(), this.getBestResultSoFar());
    }

    @Override
    protected MatrixStore<Double> extractSolution() {
        return PrimitiveDenseStore.FACTORY.columns(this.getBestResultSoFar());
    }

    @Override
    protected boolean initialise(final Result kickStarter) {
        return true;
    }

    @Override
    protected boolean needsAnotherIteration() {
        return !this.getState().isOptimal();
    }

    @Override
    protected boolean validate() {

        boolean retVal = true;
        this.setState(State.VALID);

        try {

            if (!(retVal = this.getModel().validate())) {
                retVal = false;
                this.setState(State.INVALID);
            }

        } catch (final Exception ex) {

            retVal = false;
            this.setState(State.FAILED);
        }

        return retVal;
    }

    int countExploredNodes() {
        return myExploredNodes.size();
    }

    int getGlobalIndex(final int integerIndex) {
        return myIntegerIndeces[integerIndex];
    }

    final int[] getIntegerIndeces() {
        return myIntegerIndeces;
    }

    int identifyNonIntegerVariable(final Optimisation.Result nodeResult, final NodeKey nodeKey) {

        final MatrixStore<Double> tmpGradient = this.getGradient(AccessUtils.asPrimitive1D(nodeResult));

        int retVal = -1;

        double tmpFraction, tmpWeightedFraction;
        double tmpMaxFraction = ZERO;

        for (int i = 0; i < myIntegerIndeces.length; i++) {

            tmpFraction = nodeKey.getFraction(i, nodeResult.doubleValue(myIntegerIndeces[i]));
            tmpWeightedFraction = tmpFraction * (PrimitiveMath.ONE + Math.abs(tmpGradient.doubleValue(myIntegerIndeces[i])));

            if ((tmpWeightedFraction > tmpMaxFraction) && !options.integer.isZero(tmpWeightedFraction)) {
                retVal = i;
                tmpMaxFraction = tmpWeightedFraction;
            }

        }

        return retVal;
    }

    boolean isExplored(final BranchAndBoundNodeTask aNodeTask) {
        return myExploredNodes.contains(aNodeTask.getKey());
    }

    void markAsExplored(final BranchAndBoundNodeTask aNodeTask) {
        myExploredNodes.add(aNodeTask.getKey());
    }

}
