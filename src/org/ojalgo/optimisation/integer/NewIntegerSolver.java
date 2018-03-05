/*
 * Copyright 1997-2018 Optimatika
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import org.ojalgo.concurrent.DaemonPoolExecutor;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.CharacterRing.PrinterBuffer;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.TypeUtils;

/**
 * IntegerSolver
 *
 * @author apete
 */
public final class NewIntegerSolver extends IntegerSolver {

    class NodeWorker implements Callable<Boolean> {

        public Boolean call() throws Exception {

            NodeKey tmpNodeKey = null;

            while (normal && NewIntegerSolver.this.isStillNodesToTry() && ((tmpNodeKey = NewIntegerSolver.this.getNextNode()) != null)) {
                NewIntegerSolver.this.compute(tmpNodeKey);
            }

            return normal;
        }
    }

    private final PriorityBlockingQueue<NodeKey> myNodesToTry = new PriorityBlockingQueue<>();

    boolean normal = true;

    NewIntegerSolver(final ExpressionsBasedModel model, final Options solverOptions) {
        super(model, solverOptions);
    }

    public Result solve(final Result kickStarter) {

        // Must verify that it actually is an integer solution
        // The kickStarter may be user-supplied
        if ((kickStarter != null) && kickStarter.getState().isFeasible() && this.getIntegerModel().validate(kickStarter)) {
            this.markInteger(null, null, kickStarter);
        }

        this.resetIterationsCount();

        this.setup();

        //     final boolean tmpNormalExit = DaemonPoolExecutor.INSTANCE.invoke(tmpTask);

        //   this.add(new NodeKey(this.getModel()));

        Optimisation.Result retVal = this.getBestResultSoFar();

        if (retVal.getState().isFeasible()) {

            if (normal) {
                retVal = new Optimisation.Result(State.OPTIMAL, retVal);
            } else {
                retVal = new Optimisation.Result(State.FEASIBLE, retVal);
            }

        } else {

            if (normal) {
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

            if (!(retVal = this.getIntegerModel().validate())) {
                retVal = false;
                this.setState(State.INVALID);
            }

        } catch (final Exception ex) {

            retVal = false;
            this.setState(State.FAILED);
        }

        return retVal;
    }

    boolean add(final NodeKey e) {
        return myNodesToTry.add(e);
    }

    void compute(final NodeKey nodeKey) {

        if (NewIntegerSolver.this.isDebug()) {
            NewIntegerSolver.this.log("\nBranch&Bound Node");
            NewIntegerSolver.this.log(nodeKey.toString());
            NewIntegerSolver.this.log(NewIntegerSolver.this.toString());
        }

        if (!NewIntegerSolver.this.isIterationAllowed() || !NewIntegerSolver.this.isIterationNecessary()) {
            if (NewIntegerSolver.this.isDebug()) {
                NewIntegerSolver.this.log("Reached iterations or time limit - stop!");
            }
            normal &= false;
        }

        if (!NewIntegerSolver.this.isGoodEnoughToContinueBranching(nodeKey.objective)) {
            if (NewIntegerSolver.this.isDebug()) {
                NewIntegerSolver.this.log("No longer a relevant node!");
            }
            normal &= true;
        }

        ExpressionsBasedModel tmpModel = NewIntegerSolver.this.makeNodeModel(nodeKey);
        final Optimisation.Result tmpResult = tmpModel.solve(NewIntegerSolver.this.getBestResultSoFar());

        NewIntegerSolver.this.incrementIterationsCount();

        if ((tmpModel.options.logger_appender != null) && (tmpModel.options.logger_appender instanceof PrinterBuffer)) {
            if (NewIntegerSolver.this.getIntegerModel().options.logger_appender != null) {
                ((PrinterBuffer) tmpModel.options.logger_appender).flush(NewIntegerSolver.this.getIntegerModel().options.logger_appender);
            }
        }
        if (tmpResult.getState().isOptimal()) {
            if (NewIntegerSolver.this.isDebug()) {
                NewIntegerSolver.this.log("Node solved to optimality!");
            }

            if (NewIntegerSolver.this.options.validate && !tmpModel.validate(tmpResult)) {
                // This should not be possible. There is a bug somewhere.
                NewIntegerSolver.this.log("Node solution marked as OPTIMAL, but is actually INVALID/INFEASIBLE/FAILED. Stop this branch!");
                //                    IntegerSolver.this.logDebug(myKey.toString());
                //                    IntegerSolver.this.logDebug(tmpModel.toString());
                //                    final GenericSolver tmpDefaultSolver = tmpModel.getDefaultSolver();
                //                    tmpDefaultSolver.solve();
                //                    IntegerSolver.this.logDebug(tmpDefaultSolver.toString());
                normal &= false;
            }

            final int tmpBranchIndex = NewIntegerSolver.this.identifyNonIntegerVariable(tmpResult, nodeKey);
            final double tmpSolutionValue = NewIntegerSolver.this.evaluateFunction(tmpResult);

            if (tmpBranchIndex == -1) {
                if (NewIntegerSolver.this.isDebug()) {
                    NewIntegerSolver.this.log("Integer solution! Store it among the others, and stop this branch!");
                }

                final Optimisation.Result tmpIntegerSolutionResult = new Optimisation.Result(Optimisation.State.FEASIBLE, tmpSolutionValue, tmpResult);

                NewIntegerSolver.this.markInteger(nodeKey, null, tmpIntegerSolutionResult);

                if (NewIntegerSolver.this.isDebug()) {
                    NewIntegerSolver.this.log(NewIntegerSolver.this.getBestResultSoFar().toString());
                }

                BasicLogger.debug();
                BasicLogger.debug(NewIntegerSolver.this.toString());
                // BasicLogger.debug(DaemonPoolExecutor.INSTANCE.toString());

            } else {
                if (NewIntegerSolver.this.isDebug()) {
                    NewIntegerSolver.this.log("Not an Integer Solution: " + tmpSolutionValue);
                }

                final double tmpVariableValue = tmpResult.doubleValue(NewIntegerSolver.this.getGlobalIndex(tmpBranchIndex));

                if (NewIntegerSolver.this.isGoodEnoughToContinueBranching(tmpSolutionValue)) {
                    if (NewIntegerSolver.this.isDebug()) {
                        NewIntegerSolver.this.log("Still hope, branching on {} @ {} >>> {}", tmpBranchIndex, tmpVariableValue,
                                tmpModel.getVariable(NewIntegerSolver.this.getGlobalIndex(tmpBranchIndex)));
                    }

                    tmpModel.dispose();
                    tmpModel = null;

                    final NodeKey tmpLowerBranchTask = nodeKey.createLowerBranch(tmpBranchIndex, tmpVariableValue, tmpResult.getValue());
                    final NodeKey tmpUpperBranchTask = nodeKey.createUpperBranch(tmpBranchIndex, tmpVariableValue, tmpResult.getValue());

                    this.add(tmpLowerBranchTask);
                    this.add(tmpUpperBranchTask);

                    if (DaemonPoolExecutor.isDaemonAvailable()) {
                        DaemonPoolExecutor.invoke(new NodeWorker());
                    }

                    normal &= true;

                } else {
                    if (NewIntegerSolver.this.isDebug()) {
                        NewIntegerSolver.this.log("Can't find better integer solutions - stop this branch!");
                    }
                }
            }

        } else {
            if (NewIntegerSolver.this.isDebug()) {
                NewIntegerSolver.this.log("Failed to solve problem - stop this branch!");
            }
        }

        normal &= true;
    }

    int countExploredNodes() {
        return this.countIterations();
    }

    NodeKey getNextNode() {
        return myNodesToTry.poll();
        //        if (myMinimisation) {
        //            final NodeKey retVal = myNodesToTry.first();
        //            myNodesToTry.remove(retVal);
        //            return retVal;
        //        } else {
        //            final NodeKey retVal = myNodesToTry.last();
        //            myNodesToTry.remove(retVal);
        //            return retVal;
        //        }
    }

    boolean isStillNodesToTry() {
        return !myNodesToTry.isEmpty();
    }

    ExpressionsBasedModel makeNodeModel(final NodeKey nodeKey) {

        final ExpressionsBasedModel retVal = this.getIntegerModel().relax(false);

        final int[] tmpIntegerIndeces = this.getIntegerIndices();
        for (int i = 0; i < tmpIntegerIndeces.length; i++) {

            final BigDecimal tmpLowerBound = nodeKey.getLowerBound(i);
            final BigDecimal tmpUpperBound = nodeKey.getUpperBound(i);

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

        if (this.isIntegerSolutionFound()) {
            final double tmpBestValue = this.getBestResultSoFar().getValue();
            final double tmpGap = PrimitiveFunction.ABS.invoke(tmpBestValue * options.mip_gap);
            if (retVal.isMinimisation()) {
                retVal.limitObjective(null, TypeUtils.toBigDecimal(tmpBestValue - tmpGap, options.feasibility));
            } else {
                retVal.limitObjective(TypeUtils.toBigDecimal(tmpBestValue + tmpGap, options.feasibility), null);
            }
        }

        return retVal;
    }

    void pruneNodes(final NodeKey integerNode) {
        //        if (myMinimisation) {
        //            final SortedSet<NodeKey> tmpTail = myNodesToTry.tailSet(integerNode);
        //            myNodesToTry.removeAll(tmpTail);
        //        } else {
        //            final SortedSet<NodeKey> tmpHead = myNodesToTry.headSet(integerNode);
        //            myNodesToTry.removeAll(tmpHead);
        //        }
    }

    void setup() {

        normal = true;

        final NodeKey[] retVal = new NodeKey[2];

        final ExpressionsBasedModel tmpIntegerModel = NewIntegerSolver.this.getIntegerModel();
        final List<Variable> tmpIntegerVariables = tmpIntegerModel.getIntegerVariables();
        NodeKey myKey;
        myKey = new NodeKey(tmpIntegerModel);

        final ExpressionsBasedModel tmpRootModel = NewIntegerSolver.this.makeNodeModel(myKey);
        final Result tmpRootResult = tmpRootModel.solve(tmpIntegerModel.getVariableValues());
        final double tmpRootValue = tmpRootResult.getValue();

        double tmpMinValue = PrimitiveMath.MACHINE_LARGEST;
        double tmpMaxValue = -PrimitiveMath.MACHINE_LARGEST;

        final double tmpBestValue = tmpRootModel.isMinimisation() ? PrimitiveMath.MACHINE_LARGEST : -PrimitiveMath.MACHINE_LARGEST;

        final double[] tmpSignificance = new double[tmpIntegerVariables.size()];

        for (int i = 0; i < tmpIntegerVariables.size(); i++) {

            final int tmpGlobalIndex = NewIntegerSolver.this.getGlobalIndex(i);
            final double tmpVariableValue = tmpRootResult.doubleValue(tmpGlobalIndex);

            final NodeKey tmpLowerNodeKey = myKey.createLowerBranch(i, tmpVariableValue, tmpRootValue);
            final ExpressionsBasedModel tmpLowerModel = NewIntegerSolver.this.makeNodeModel(tmpLowerNodeKey);
            final Result tmpLowerResult = tmpLowerModel.solve(tmpRootResult);
            final double tmpLowerValue = tmpLowerResult.getValue();

            if (tmpLowerValue < tmpMinValue) {
                tmpMinValue = tmpLowerValue;
            }
            if (tmpLowerValue > tmpMaxValue) {
                tmpMaxValue = tmpLowerValue;
            }

            final NodeKey tmpUpperNodeKey = myKey.createUpperBranch(i, tmpVariableValue, tmpRootValue);
            final ExpressionsBasedModel tmpUpperModel = NewIntegerSolver.this.makeNodeModel(tmpUpperNodeKey);
            final Result tmpUpperResult = tmpUpperModel.solve(tmpRootResult);
            final double tmpUpperValue = tmpUpperResult.getValue();

            if (tmpUpperValue < tmpMinValue) {
                tmpMinValue = tmpUpperValue;
            }
            if (tmpUpperValue > tmpMaxValue) {
                tmpMaxValue = tmpUpperValue;
            }

            if (tmpLowerResult.getState().isFeasible() && tmpUpperResult.getState().isFeasible()) {
                if (tmpRootModel.isMinimisation() && ((tmpLowerValue < tmpBestValue) || (tmpUpperValue < tmpBestValue))) {
                    retVal[0] = tmpLowerNodeKey;
                    retVal[1] = tmpUpperNodeKey;
                } else if (tmpRootModel.isMaximisation() && ((tmpLowerValue > tmpBestValue) || (tmpUpperValue > tmpBestValue))) {
                    retVal[0] = tmpLowerNodeKey;
                    retVal[1] = tmpUpperNodeKey;
                }
            }

            if (!Double.isNaN(tmpUpperValue) && !Double.isNaN(tmpLowerValue)) {
                tmpSignificance[i] = PrimitiveFunction.ABS.invoke(tmpUpperValue - tmpLowerValue);
            }
        }

        double tmpScale = tmpMaxValue - tmpMinValue;
        final double value = tmpScale;
        if (PrimitiveScalar.isSmall(PrimitiveMath.ONE, value)) {
            tmpScale = PrimitiveMath.ONE;
        }
        for (int i = 0; i < tmpSignificance.length; i++) {
            final int index = i;
            NewIntegerSolver.this.addIntegerSignificance(NewIntegerSolver.this.getGlobalIndex(index), 0.5 + (tmpSignificance[i] / tmpScale));
        }

        if ((retVal[0] != null) && (retVal[1] != null)) {
            NewIntegerSolver.this.add(retVal[0]);
            NewIntegerSolver.this.add(retVal[1]);
        } else {
            NewIntegerSolver.this.add(new NodeKey(tmpIntegerModel));
        }

        final Future<Boolean> tmpFuture = DaemonPoolExecutor.invoke(new NodeWorker());

        try {
            normal = normal && tmpFuture.get();
        } catch (InterruptedException | ExecutionException anException) {
            normal &= false;
        }
    }

}
