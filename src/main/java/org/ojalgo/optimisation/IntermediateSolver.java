/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.optimisation;

import java.math.BigDecimal;

import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.structure.Access1D;

/**
 * A {@link Optimisation.Solver} implementation that wraps an {@link ExpressionsBasedModel}. Intended to be
 * used when implementing solvers that iteratively modify a model instance and delegate to other solvers. The
 * {@link IntegerSolver} makes use of this.
 *
 * @author apete
 */
public abstract class IntermediateSolver implements Optimisation.Solver {

    private boolean myInPlaceUpdatesOK = true;
    private transient ExpressionsBasedModel.Integration<?> myIntegration = null;
    private final ExpressionsBasedModel myModel;
    private transient Optimisation.Result myResult = null;
    private transient Optimisation.Solver mySolver = null;

    protected IntermediateSolver(final ExpressionsBasedModel model) {
        super();
        myModel = model;
        myIntegration = null;
        mySolver = null;
    }

    @Override
    public void dispose() {

        this.reset();

        Solver.super.dispose();
    }

    public Variable getVariable(final int globalIndex) {
        return myModel.getVariable(globalIndex);
    }

    /**
     * Force re-generation of cached/transient data
     */
    public void reset() {

        myResult = null;

        if (mySolver != null) {
            mySolver.dispose();
            mySolver = null;
        }

        myIntegration = null;
    }

    @Override
    public Optimisation.Result solve(final Optimisation.Result candidate) {

        if (mySolver == null && ExpressionsBasedModel.PRESOLVERS.size() > 0) {
            myModel.presolve();
        }

        if (myModel.isInfeasible()) {

            Optimisation.Result solution = candidate != null ? candidate : myModel.getVariableValues();

            return new Optimisation.Result(State.INFEASIBLE, solution);
        }

        if (myModel.isUnbounded()) {

            if (candidate != null && myModel.validate(candidate)) {
                return new Optimisation.Result(State.UNBOUNDED, candidate);
            }

            Optimisation.Result derivedSolution = myModel.getVariableValues();
            if (derivedSolution.getState().isFeasible()) {
                return new Optimisation.Result(State.UNBOUNDED, derivedSolution);
            }

        } else if (myModel.isFixed()) {

            Optimisation.Result derivedSolution = myModel.getVariableValues();

            if (derivedSolution.getState().isFeasible()) {
                return new Optimisation.Result(State.DISTINCT, derivedSolution);
            }
            return new Optimisation.Result(State.INVALID, derivedSolution);
        }

        ExpressionsBasedModel.Integration<?> integration = this.getIntegration();
        Optimisation.Solver solver = this.getSolver();

        Optimisation.Result retVal = candidate != null ? candidate : myModel.getVariableValues();
        retVal = integration.toSolverState(retVal, myModel);
        retVal = solver.solve(retVal);
        retVal = integration.toModelState(retVal, myModel);

        myResult = retVal;

        return retVal;
    }

    @Override
    public String toString() {
        return myModel.toString();
    }

    public void update(final Variable variable) {

        if (myInPlaceUpdatesOK && mySolver instanceof UpdatableSolver && variable.isFixed()) {
            UpdatableSolver updatableSolver = (UpdatableSolver) mySolver;

            int indexInSolver = this.getIntegration().getIndexInSolver(myModel, variable);
            double fixedValue = variable.getValue().doubleValue();

            if (updatableSolver.fixVariable(indexInSolver, fixedValue)) {
                // Solver updated in-place
                return;
            }
            myInPlaceUpdatesOK = false;
        }

        // Solver will be re-generated
        mySolver = null;
    }

    /**
     * Using the solver's {@link ExpressionsBasedModel.Validator} instance, if set. Otherwise no validation
     * performed.
     */
    public boolean validate(final Access1D<?> solution) {
        if (mySolver != null && mySolver instanceof GenericSolver) {
            boolean valid = ((GenericSolver) mySolver).validate(solution);
            if (!valid) {
                ((GenericSolver) mySolver).setState(State.FAILED); // TODO Should it be INVALID instead?
            }
            return valid;
        } else {
            return true;
        }
    }

    /**
     * Always performs validation directly using
     * {@link ExpressionsBasedModel#validate(Access1D, BasicLogger)}.
     */
    public boolean validate(final Access1D<BigDecimal> solution, final BasicLogger appender) {
        return myModel.validate(solution, appender);
    }

    /**
     * Using the solver's {@link ExpressionsBasedModel.Validator} instance, if set. Otherwise no validation
     * performed.
     */
    public boolean validate(final ExpressionsBasedModel model) {
        if (mySolver != null && mySolver instanceof GenericSolver) {
            boolean valid = ((GenericSolver) mySolver).validate(model);
            if (!valid) {
                ((GenericSolver) mySolver).setState(State.FAILED); // TODO Should it be INVALID instead?
            }
            return valid;
        } else {
            return true;
        }
    }

    protected int getIndexInSolver(final int globalModelIndex) {
        Variable variable = myModel.getVariable(globalModelIndex);
        ExpressionsBasedModel.Integration<?> integration = this.getIntegration();
        return integration.getIndexInSolver(myModel, variable);
    }

    protected ExpressionsBasedModel getModel() {
        return myModel;
    }

    protected Optimisation.Result getResult() {
        if (myResult == null) {
            myResult = this.solve();
        }
        return myResult;
    }

    protected Optimisation.Solver getSolver() {
        if (mySolver == null) {
            mySolver = this.getIntegration().build(myModel);
        }
        return mySolver;
    }

    protected boolean isSolved() {
        return mySolver != null && myResult != null;
    }

    ExpressionsBasedModel.Integration<?> getIntegration() {
        if (myIntegration == null) {
            myIntegration = myModel.getIntegration();
        }
        return myIntegration;
    }

}
