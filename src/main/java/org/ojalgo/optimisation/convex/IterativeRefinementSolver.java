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
package org.ojalgo.optimisation.convex;

import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Quadruple;

/**
 * Algorithm from: Solving quadratic programs to high precision using scaled iterative refinement <br>
 * Mathematical Programming Computation (2019) 11:421–455 https://doi.org/10.1007/s12532-019-00154-6
 *
 * @author Programmer-Magnus
 */
final class IterativeRefinementSolver extends ConvexSolver {

    private static Optimisation.Result doIteration(final MatrixStore<Quadruple> H, final MatrixStore<Quadruple> g, final MatrixStore<Quadruple> AE,
            final MatrixStore<Quadruple> BE, final MatrixStore<Quadruple> AI, final MatrixStore<Quadruple> BI, final Optimisation.Options options,
            final Optimisation.Result startValue) {

        int nbVars = g.size();
        int nbEqus = BE.getRowDim();
        int nbIneq = BI.getRowDim();

        ConvexData<Double> data = new ConvexData<>(false, Primitive64Store.FACTORY, nbVars, nbEqus, nbIneq);

        data.getObjective().quadratic().fillMatching(H);
        data.getObjective().linear().fillMatching(g);

        for (int i = 0; i < nbEqus; i++) {

            for (int j = 0; j < nbVars; j++) {
                double tmpVal = AE.doubleValue(i, j);
                if (tmpVal != 0.0) {
                    data.setAE(i, j, tmpVal);
                }
            }

            data.setBE(i, BE.doubleValue(i));
        }

        for (int i = 0; i < nbIneq; i++) {

            for (int j = 0; j < nbVars; j++) {
                double tmpVal = AI.doubleValue(i, j);
                if (tmpVal != 0.0) {
                    data.setAI(i, j, tmpVal);
                }
            }

            data.setBI(i, BI.doubleValue(i));
        }

        return BasePrimitiveSolver.newSolver(data, options).solve(startValue);
    }

    static Optimisation.Result doSolve(final MatrixStore<Quadruple> Q_in, final MatrixStore<Quadruple> C_in, final MatrixStore<Quadruple> ae_in,
            final MatrixStore<Quadruple> be_in, final MatrixStore<Quadruple> ai_in, final MatrixStore<Quadruple> bi_in, final Optimisation.Options options) {

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
        C_Size = C_Size > 1.0 ? C_Size : 1;
        double Q_Size = Q_in.aggregateAll(Aggregator.LARGEST).doubleValue();

        MatrixStore<Quadruple> Q0 = Q_in;
        MatrixStore<Quadruple> C0 = C_in;
        MatrixStore<Quadruple> ae0 = ae_in;
        MatrixStore<Quadruple> be0 = be_in;
        MatrixStore<Quadruple> ai0 = ai_in;
        MatrixStore<Quadruple> bi0 = bi_in;

        Optimisation.Result startValue = Optimisation.Result.of(Optimisation.State.APPROXIMATE, new double[Q_in.getColDim()]);

        Optimisation.Result x_y_double = IterativeRefinementSolver.doIteration(Q_in, C_in, ae_in, be_in, ai_in, bi_in, options, startValue);
        if (x_y_double.getState() == Optimisation.State.INFEASIBLE) {
            //         trust solver and abort if infeasible.
            return x_y_double;
        }
        if (!x_y_double.getState().isOptimal()) {
            //         sometimes it works second time...?!
            x_y_double = IterativeRefinementSolver.doIteration(Q_in, C_in, ae_in, be_in, ai_in, bi_in, options, startValue);
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
            double relativeComplementarySlackness2 = y0.onMatching(QuadrupleMath.MULTIPLY, be1.below(bi1)).collect(GenericStore.R128)
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
                }
                break;
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
                x_y_double = IterativeRefinementSolver.doIteration(Q1_, C1_, ae1_, be1_, ai1_, bi1_, options, startValue);
                if (!x_y_double.getState().isOptimal()) {
                    // on failure try a smaller zoom factor
                    double increaseP = scaleP1 / scaleP0;
                    double newIncreaseP = Math.sqrt(increaseP);
                    scaleP1 = scaleP0 * newIncreaseP;
                    double increaseD = scaleD1 / scaleD0;
                    double newIncreaseD = Math.sqrt(increaseD);
                    scaleD1 = scaleD0 * newIncreaseD;
                }
            } while (!x_y_double.getState().isOptimal() && noTries < maxTriesOnFailure);
            if (noTries == maxTriesOnFailure) {
                //  Cant solve this sub problem. Abort.
                break;
            }
            MatrixStore<Quadruple> x1 = GenericStore.R128.columns(x_y_double);
            MatrixStore<Quadruple> y1 = GenericStore.R128.columns(x_y_double.getMultipliers().get());
            if (x1.aggregateAll(Aggregator.LARGEST).compareTo(Quadruple.ZERO) == 0 && y1.aggregateAll(Aggregator.LARGEST).compareTo(Quadruple.ZERO) == 0) {
                // No progress if x1 and y1 = 0, abort.
                break;
            }
            //  update tentative solution in high precision
            x0 = x0.add(x1.divide(scaleP1));
            y0 = y0.add(y1.divide(scaleD1));
            scaleP0 = scaleP1;
            scaleD0 = scaleD1;
        }
        Result result = IterativeRefinementSolver.buildResult(Q0, C0, x0, y0, State.OPTIMAL);
        double improvement = (initialSolutionValue - result.getValue()) / initialSolutionValue;
        return result;
    }

    private static Result buildResult(final MatrixStore<Quadruple> Q0, final MatrixStore<Quadruple> C0, final MatrixStore<Quadruple> x0,
            final MatrixStore<Quadruple> y0, final State state) {
        Quadruple objectiveValue = Q0.multiplyBoth(x0).divide(2).subtract(x0.transpose().multiply(C0).get(0));
        Result result = new Result(state, objectiveValue.doubleValue(), x0);
        result.multipliers(y0);
        return result;
    }

    static ConvexData<Quadruple> newInstance(final int nbVars, final int nbEqus, final int nbIneq) {
        return new ConvexData<>(false, GenericStore.R128, nbVars, nbEqus, nbIneq);
    }

    private final ConvexData<Quadruple> myData;

    IterativeRefinementSolver(final Options solverOptions, final ConvexData<Quadruple> data) {
        super(solverOptions);
        myData = data;
    }

    @Override
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
