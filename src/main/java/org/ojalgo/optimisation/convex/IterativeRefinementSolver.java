/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.optimisation.convex;

import java.util.List;
import java.util.Set;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Structure1D.IntIndex;

public final class IterativeRefinementSolver extends GenericSolver {

    static final class Builder extends GenericSolver.Builder<IterativeRefinementSolver.Builder, IterativeRefinementSolver> {

        protected Builder(final Factory<Quadruple, ?> factory) {
            super();
        }

        @Override
        protected IterativeRefinementSolver doBuild(final Options options) {

            ConvexData<Quadruple> data = ConvexSolver.copy(null, IterativeRefinementSolver::newInstance);

            return new IterativeRefinementSolver(options, data);
        }

    }

    static final class ModelIntegration extends ExpressionsBasedModel.Integration<IterativeRefinementSolver> {

        public IterativeRefinementSolver build(final ExpressionsBasedModel model) {

            ConvexData<Quadruple> data = ConvexSolver.copy(model, IterativeRefinementSolver::newInstance);

            return new IterativeRefinementSolver(model.options, data);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {
            return model.isAnyObjectiveQuadratic() && !model.isAnyVariableInteger();
        }

        @Override
        public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {

            List<Variable> freeVariables = model.getFreeVariables();
            Set<IntIndex> fixedVariables = model.getFixedVariables();
            int nbFreeVars = freeVariables.size();
            int nbModelVars = model.countVariables();

            ArrayR064 modelSolution = ArrayR064.make(nbModelVars);

            for (int i = 0; i < nbFreeVars; i++) {
                modelSolution.set(model.indexOf(freeVariables.get(i)), solverState.doubleValue(i));
            }

            for (IntIndex fixed : fixedVariables) {
                modelSolution.set(fixed.index, model.getVariable(fixed.index).getValue());
            }

            return new Result(solverState.getState(), modelSolution);
        }

        @Override
        public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {

            List<Variable> freeVariables = model.getFreeVariables();
            int nbFreeVars = freeVariables.size();

            ArrayR064 solverSolution = ArrayR064.make(nbFreeVars);

            for (int i = 0; i < nbFreeVars; i++) {
                Variable variable = freeVariables.get(i);
                int modelIndex = model.indexOf(variable);
                solverSolution.set(i, modelState.doubleValue(modelIndex));
            }

            return new Result(modelState.getState(), solverSolution);
        }

    }

    public static final ExpressionsBasedModel.Integration<IterativeRefinementSolver> INTEGRATION = new ModelIntegration();

    private static Optimisation.Result doIteration(final MatrixStore<Quadruple> H, final MatrixStore<Quadruple> g, final MatrixStore<Quadruple> AE,
            final MatrixStore<Quadruple> BE, final MatrixStore<Quadruple> AI, final MatrixStore<Quadruple> BI, final Optimisation.Options options) {

        ConvexSolver.Builder builder = ConvexSolver.newBuilder();

        builder.objective(H, g);

        if (AE != null && BE != null) {
            builder.equalities(AE, BE);
        }

        if (AI != null && BI != null) {
            builder.inequalities(AI, BI);
        }

        ConvexSolver solver = builder.build(options);
        Optimisation.Result startValue = Optimisation.Result.of(Optimisation.State.APPROXIMATE, new double[H.getColDim()]);
        return solver.solve(startValue);
    }

    static Optimisation.Result doSolve(final MatrixStore<Quadruple> Q_in, final MatrixStore<Quadruple> C_in, final MatrixStore<Quadruple> ae_in,
            final MatrixStore<Quadruple> be_in, final MatrixStore<Quadruple> ai_in, final MatrixStore<Quadruple> bi_in, final Optimisation.Options options) {
        // Algorithm from:
        // Solving quadratic programs to high precision using scaled iterative refinement
        // Mathematical Programming Computation (2019) 11:421–455
        // https://doi.org/10.1007/s12532-019-00154-6

        // Required threshold for final residuals
        double epsPrimal = 1E-15;
        double epsDual = 1E-15;
        double epsSlack = 1E-15;

        //  Constants to modify
        double maxZoomFactor = 1.0E12;
        int maxRefinementIterations = 5;
        int maxTriesOnFailure = 3;
        double smallestNoneZeroHessian = 1e-10;

        // order of size of parameters in model
        double be_Size = be_in.aggregateAll(Aggregator.LARGEST).doubleValue();
        be_Size = be_Size > 1E-15 ? be_Size : 1;
        double C_Size = C_in.aggregateAll(Aggregator.LARGEST).doubleValue();
        double Q_Size = Q_in.aggregateAll(Aggregator.LARGEST).doubleValue();

        MatrixStore<Quadruple> Q0 = Q_in;
        MatrixStore<Quadruple> C0 = C_in;
        MatrixStore<Quadruple> ae0 = ae_in;
        MatrixStore<Quadruple> be0 = be_in;
        MatrixStore<Quadruple> ai0 = ai_in;
        MatrixStore<Quadruple> bi0 = bi_in;

        Optimisation.Result x_y_double = IterativeRefinementSolver.doIteration(Q_in, C_in, ae_in, be_in, ai_in, bi_in, options);
        if (x_y_double.getState() == Optimisation.State.INFEASIBLE) {
            //         trust solver and abort if infeasible.
            return x_y_double;
        }
        if (!x_y_double.getState().isOptimal()) {
            //         sometimes it works second time...?!
            x_y_double = IterativeRefinementSolver.doIteration(Q_in, C_in, ae_in, be_in, ai_in, bi_in, options);
        }
        MatrixStore<Quadruple> x0 = GenericStore.R128.columns(x_y_double);
        MatrixStore<Quadruple> y0 = GenericStore.R128.columns(x_y_double.getMultipliers().get());
        double initialSolutionValue = x_y_double.getValue();

        //  Set initial values
        double scaleP0 = 1;
        double scaleD0 = 1;
        int iteration = 0;

        while (x_y_double.getState().isFeasible()) {
            /*
             * If set of active inequalities do not change between iterations. Then one can try to solve the
             * system of linear equations (KKT) using high precision. and return this answer if it is a
             * success, after checking inequality multipliers.
             */
            iteration++;
            // Compute residuals in Quadruple precision
            MatrixStore<Quadruple> be1 = be0.subtract(ae0.multiply(x0));
            double maxEqualityResidual = be1.aggregateAll(Aggregator.LARGEST).doubleValue();
            MatrixStore<Quadruple> bi1 = bi0.subtract(ai0.multiply(x0));
            double maxInequalityResidual = bi1.negate().aggregateAll(Aggregator.MAXIMUM).doubleValue();
            double maxPrimalResidual = Math.max(maxEqualityResidual, maxInequalityResidual);
            double scaleP1 = Math.min(1 / maxPrimalResidual, maxZoomFactor * scaleP0);
            scaleP1 = Math.max(1, scaleP1);
            MatrixStore<Quadruple> C1 = C0.subtract(Q0.multiply(x0)).subtract(ae0.below(ai0).transpose().multiply(y0));
            double maxGradientResidual = C1.negate().aggregateAll(Aggregator.LARGEST).norm();
            // SUM_i ABS(C1_i * x_i) / |C1|
            double relativeComplementarySlackness1 = C1.onMatching(QuadrupleMath.MULTIPLY, x0).collect(GenericStore.R128).aggregateAll(Aggregator.LARGEST)
                    .doubleValue() / C_Size;
            // SUM_i ABS(y0_i * b_i) / |Be|
            double relativeComplementarySlackness2 = y0.onMatching(QuadrupleMath.MULTIPLY, (be1.below(bi1))).collect(GenericStore.R128)
                    .aggregateAll(Aggregator.LARGEST).doubleValue() / be_Size;
            double relativeComplementarySlackness = Math.max(relativeComplementarySlackness1,
                    relativeComplementarySlackness2 * relativeComplementarySlackness2);
            double scaleD1 = Math.min(1 / maxGradientResidual, maxZoomFactor * scaleD0);
            scaleD1 = Math.max(1, scaleD1);
            double relativeGradientResidual = maxGradientResidual / C_Size;
            if (maxPrimalResidual < epsPrimal && relativeGradientResidual < epsDual && relativeComplementarySlackness < epsSlack) {
                //  Passed threshold for final residuals
                break;
            }
            double scaledHessianNorm = Q_Size * scaleD1 / scaleP1;
            if (scaledHessianNorm < smallestNoneZeroHessian) {
                //  avoid that ojAlgo classifies hessian as being ZERO. Should different solver be used for this case instead?
                scaleP1 = Q_Size * scaleD1 / scaledHessianNorm;
            }
            if (iteration > maxRefinementIterations) {
                if (maxPrimalResidual < Math.sqrt(epsPrimal) && relativeGradientResidual < Math.sqrt(epsDual)
                        && relativeComplementarySlackness < Math.sqrt(epsSlack)) {
                    //  solution not that bad. I use it in SQP where we will probably do another iteration anyway.
                    break;
                }
                Quadruple objectiveValue = Q0.multiplyBoth(x0).divide(2).subtract(x0.transpose().multiply(C0).get(0));
                Optimisation.State state = x_y_double.getState() == Optimisation.State.OPTIMAL ? Optimisation.State.APPROXIMATE : Optimisation.State.FAILED;
                Optimisation.Result result = Optimisation.Result.of(objectiveValue.doubleValue(), state, x0.toRawCopy1D());
                result.multipliers(y0);
                double v = (initialSolutionValue - objectiveValue.doubleValue()) / initialSolutionValue;
                return result;
            }
            // Prepare approximate model
            int noTries = 0;
            do {
                noTries++;
                MatrixStore<Quadruple> Q1_ = Q0.multiply(scaleD1 / scaleP1);
                MatrixStore<Quadruple> C1_ = C1.multiply(scaleD1);
                MatrixStore<Quadruple> ae1_ = ae0;
                MatrixStore<Quadruple> be1_ = be1.multiply(scaleP1);
                MatrixStore<Quadruple> ai1_ = ai0;
                MatrixStore<Quadruple> bi1_ = bi1.multiply(scaleP1);
                //solve updated QP
                x_y_double = IterativeRefinementSolver.doIteration(Q1_, C1_, ae1_, be1_, ai1_, bi1_, options);
                if (x_y_double.getState().isFailure()) {
                    // on failure try a smaller zoom factor
                    double increaseP = scaleP1 / scaleP0;
                    double newIncreaseP = Math.sqrt(increaseP);
                    scaleP1 = scaleP0 * newIncreaseP;
                    double increaseD = scaleD1 / scaleD0;
                    double newIncreaseD = Math.sqrt(increaseD);
                    scaleD1 = scaleD0 * newIncreaseD;
                }
            } while (x_y_double.getState().isFailure() && noTries < maxTriesOnFailure);

            MatrixStore<Quadruple> x1 = GenericStore.R128.columns(x_y_double);
            MatrixStore<Quadruple> y1 = GenericStore.R128.columns(x_y_double.getMultipliers().get());
            //  update tentative solution in high precision
            x0 = x0.add(x1.divide(scaleP1));
            y0 = y0.add(y1.divide(scaleD1));
            scaleP0 = scaleP1;
            scaleD0 = scaleD1;
        }
        Quadruple objectiveValue = Q0.multiplyBoth(x0).divide(2).subtract(x0.transpose().multiply(C0).get(0));
        Optimisation.Result result = Optimisation.Result.of(objectiveValue.doubleValue(), x_y_double.getState(), x0.toRawCopy1D());
        result.multipliers(y0);
        double improvement = (initialSolutionValue - objectiveValue.doubleValue()) / initialSolutionValue;
        return result;
    }

    static ConvexData<Quadruple> newInstance(final int nbVars, final int nbEqus, final int nbIneq) {
        return new ConvexData<>(GenericStore.R128, nbVars, nbEqus, nbIneq);
    }

    private final ConvexData<Quadruple> myData;

    IterativeRefinementSolver(final Options solverOptions, final ConvexData<Quadruple> data) {
        super(solverOptions);
        myData = data;
    }

    public Result solve(final Result kickStarter) {

        ConvexObjectiveFunction<Quadruple> objective = myData.getObjective();
        MatrixStore<Quadruple> mtrxQ = objective.quadratic();
        MatrixStore<Quadruple> mtrxC = objective.linear();

        MatrixStore<Quadruple> mtrxAE = myData.getAE();
        MatrixStore<Quadruple> mtrxBE = myData.getBE();

        MatrixStore<Quadruple> mtrxAI = myData.getAI();
        MatrixStore<Quadruple> mtrxBI = myData.getBI();

        return IterativeRefinementSolver.doSolve(mtrxQ, mtrxC, mtrxAE, mtrxBE, mtrxAI, mtrxBI, options);
    }

}
