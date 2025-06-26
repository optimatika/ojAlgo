/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;

/**
 * Algorithm from: Solving quadratic programs to high precision using scaled iterative refinement <br>
 * Mathematical Programming Computation (2019) 11:421–455 https://doi.org/10.1007/s12532-019-00154-6
 *
 * @author Programmer-Magnus
 */
final class IterativeRefinementSolverDouble extends ConvexSolver {

    private static Result buildResult(final MatrixStore<Double> Q0, final MatrixStore<Double> C0, final MatrixStore<Double> x0,
            final MatrixStore<Double> y0, final State state) {
        double objectiveValue = Q0.multiplyBoth(x0) / 2.0 - x0.dot(C0);
        Result result = new Result(state, objectiveValue, x0);
        result.multipliers(y0);
        return result;
    }

    private static Result doIteration(final MatrixStore<Double> H, final MatrixStore<Double> g, final MatrixStore<Double> AE,
            final MatrixStore<Double> BE, final MatrixStore<Double> AI, final MatrixStore<Double> BI, final Options options,
            final Result startValue) {

        int nbVars = g.size();
        int nbEqus = BE.getRowDim();
        int nbIneq = BI.getRowDim();

        ConvexData<Double> data = new ConvexData<>(false, R064Store.FACTORY, nbVars, nbEqus, nbIneq);

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

    static Result doSolve(final MatrixStore<Double> Q_in, final MatrixStore<Double> C_in, final MatrixStore<Double> ae_in,
            final MatrixStore<Double> be_in, final MatrixStore<Double> ai_in, final MatrixStore<Double> bi_in, final Options options) {

        // Required threshold for final residuals
        double threshold = options.solution.epsilon();
        double epsPrimal = threshold;
        double epsDual = threshold;
        double epsSlack = threshold;
        boolean combinedScaleFactor = options.convex().combinedScaleFactor;

        //  Constants to modify
        double maxZoomFactor = 1.0E12;
        int maxRefinementIterations = 5;
        int maxTriesOnFailure = 3;
        double smallestNoneZeroHessian = 1e-10;

        // order of size of parameters in model
        double be_Size = be_in.aggregateAll(Aggregator.LARGEST);
        be_Size = be_Size > 1E-15 ? be_Size : 1;
        double C_Size = C_in.aggregateAll(Aggregator.LARGEST);
        C_Size = C_Size > 1.0 ? C_Size : 1;
        double Q_Size = Q_in.aggregateAll(Aggregator.LARGEST);
        if(Q_Size < smallestNoneZeroHessian){
//   Using dual parameters, do not work with tiny Hessians.
            combinedScaleFactor = true;
        }

        MatrixStore<Double> Q0 = Q_in;
        MatrixStore<Double> C0 = C_in;
        MatrixStore<Double> ae0 = ae_in;
        MatrixStore<Double> be0 = be_in;
        MatrixStore<Double> ai0 = ai_in;
        MatrixStore<Double> bi0 = bi_in;

        Result startValue = Result.of(State.APPROXIMATE, new double[Q_in.getColDim()]);

        Result x_y_double = IterativeRefinementSolverDouble.doIteration(Q_in, C_in, ae_in, be_in, ai_in, bi_in, options, startValue);
        if (x_y_double.getState() == State.INFEASIBLE) {
            //         trust solver and abort if infeasible.
            return x_y_double;
        }
        if (!x_y_double.getState().isOptimal()) {
            //         sometimes it works second time...?!
            x_y_double = IterativeRefinementSolverDouble.doIteration(Q_in, C_in, ae_in, be_in, ai_in, bi_in, options, startValue);
        }
        MatrixStore<Double> x0 = R064Store.FACTORY.column(x_y_double);
        MatrixStore<Double> y0 = R064Store.FACTORY.column(x_y_double.getMultipliers().get());
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
            // Compute residuals in Double precision
            MatrixStore<Double> be1 = be0.subtract(ae0.multiply(x0));
            double maxEqualityResidual = be1.aggregateAll(Aggregator.LARGEST);
            MatrixStore<Double> bi1 = bi0.subtract(ai0.multiply(x0));
            double maxInequalityResidual = bi1.negate().aggregateAll(Aggregator.MAXIMUM);
            double maxPrimalResidual = Math.max(maxEqualityResidual, maxInequalityResidual);
            double scaleP1 = Math.min(1 / maxPrimalResidual, maxZoomFactor * scaleP0);
            scaleP1 = Math.max(1, scaleP1);
            MatrixStore<Double> C1 = C0.subtract(Q0.multiply(x0)).subtract(ae0.below(ai0).transpose().multiply(y0));
            double maxGradientResidual = C1.negate().aggregateAll(Aggregator.LARGEST);
            // SUM_i ABS(C1_i * x_i) / |C1|
            double relativeComplementarySlackness1 = C1.multiply(x0).collect(R064Store.FACTORY).aggregateAll(Aggregator.LARGEST) / C_Size;
            // SUM_i ABS(y0_i * b_i) / |Be|
            double relativeComplementarySlackness2 = y0.multiply(be1.below(bi1)).collect(R064Store.FACTORY)
                    .aggregateAll(Aggregator.LARGEST) / be_Size;
            double relativeComplementarySlackness = Math.max(relativeComplementarySlackness1,
                    relativeComplementarySlackness2 * relativeComplementarySlackness2);
            double scaleD1 = Math.min(1 / maxGradientResidual, maxZoomFactor * scaleD0);
            scaleD1 = Math.max(1, scaleD1);
            double relativeGradientResidual = maxGradientResidual / C_Size;
            if (maxPrimalResidual < epsPrimal && relativeGradientResidual < epsDual && relativeComplementarySlackness < epsSlack) {
                //  Passed the threshold for final residuals
                break;
            }
            if (combinedScaleFactor) {
                scaleP1 = scaleD1 = Math.min(scaleP1, scaleD1);
            }else{
                double scaledHessianNorm = Q_Size * scaleD1 / scaleP1;
                if (scaledHessianNorm < smallestNoneZeroHessian) {
                    //  Avoid ojAlgo classifying the Hessian matrix as zero.
                    //  Should a different solver (LP) be used for this case instead?
                    scaleP1 = Q_Size * scaleD1 / scaledHessianNorm;
                }
            }
            if (iteration > maxRefinementIterations) {
                if (maxPrimalResidual < Math.sqrt(epsPrimal) && relativeGradientResidual < Math.sqrt(epsDual)
                        && relativeComplementarySlackness < Math.sqrt(epsSlack)) {
                }
                break;
            }
            // Prepare the approximate model
            int noTries = 0;
            do {
                noTries++;
                MatrixStore<Double> Q1_;
                if (!combinedScaleFactor) {
                    Q1_ = Q0.multiply(scaleD1 / scaleP1);
                } else {
                    Q1_ = Q0;
                }
                MatrixStore<Double> C1_ = C1.multiply(scaleD1);
                MatrixStore<Double> ae1_ = ae0;
                MatrixStore<Double> be1_ = be1.multiply(scaleP1);
                MatrixStore<Double> ai1_ = ai0;
                MatrixStore<Double> bi1_ = bi1.multiply(scaleP1);
                //solve updated QP
                x_y_double = IterativeRefinementSolverDouble.doIteration(Q1_, C1_, ae1_, be1_, ai1_, bi1_, options, startValue);
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
            MatrixStore<Double> x0_ = R064Store.FACTORY.column(x_y_double);
            MatrixStore<Double> y0_ = R064Store.FACTORY.column(x_y_double.getMultipliers().get());
            //  update tentative solution in high precision
            MatrixStore<Double> x1 = x0.add(x0_.divide(scaleP1));
            MatrixStore<Double> y1 = y0.add(y0_.divide(scaleD1));
            scaleP0 = scaleP1;
            scaleD0 = scaleD1;
            x0 = x1;
            y0 = y1;
        }
        Result result = IterativeRefinementSolverDouble.buildResult(Q0, C0, x0, y0, State.OPTIMAL);
        double improvement = (initialSolutionValue - result.getValue()) / initialSolutionValue;
        return result;
    }

    static ConvexData<Double> newInstance(final int nbVars, final int nbEqus, final int nbIneq) {
        return new ConvexData<>(false, R064Store.FACTORY, nbVars, nbEqus, nbIneq);
    }

    private final ConvexData<Double> myData;

    IterativeRefinementSolverDouble(final Options optimisationOptions, final ConvexData<Double> convexData) {
        super(optimisationOptions);
        myData = convexData;
    }

    @Override
    public Result solve(final Result kickStarter) {

        ConvexObjectiveFunction<Double> objective = myData.getObjective();
        MatrixStore<Double> mtrxQ = objective.quadratic();
        MatrixStore<Double> mtrxC = objective.linear();

        MatrixStore<Double> mtrxAE = myData.getAE();
        MatrixStore<Double> mtrxBE = myData.getBE();

        MatrixStore<Double> mtrxAI = myData.getAI();
        MatrixStore<Double> mtrxBI = myData.getBI();

        return IterativeRefinementSolverDouble.doSolve(mtrxQ, mtrxC, mtrxAE, mtrxBE, mtrxAI, mtrxBI, options);
    }

}
