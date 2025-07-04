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
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Quadruple;

/**
 * Algorithm from: Solving quadratic programs to high precision using scaled iterative refinement <br>
 * Mathematical Programming Computation (2019) 11:421–455 https://doi.org/10.1007/s12532-019-00154-6
 *
 * @author Programmer-Magnus
 */
final class IterativeRefinementSolver extends ConvexSolver {

    private static Result buildResult(final MatrixStore<Quadruple> Q, final MatrixStore<Quadruple> C, final MatrixStore<Quadruple> x,
            final MatrixStore<Quadruple> y, final State state) {
        Quadruple objectiveValue = Q.multiplyBoth(x).divide(2).subtract(x.transpose().multiply(C).get(0));
        Result result = new Result(state, objectiveValue.doubleValue(), x);
        result.multipliers(y);
        return result;
    }

    private static ConvexData<Double> getDoubleConvexData(final ConvexData<Double> data, final MatrixStore<Quadruple> Q, final MatrixStore<Quadruple> C,
            final MatrixStore<Quadruple> Ae, final MatrixStore<Quadruple> Be, final MatrixStore<Quadruple> Ai, final MatrixStore<Quadruple> Bi) {
        int nbVars1 = C.size();

        data.getObjective().quadratic().fillMatching(Q);
        data.getObjective().linear().fillMatching(C);

        for (int i1 = 0; i1 < Be.getRowDim(); i1++) {

            for (int j1 = 0; j1 < nbVars1; j1++) {
                double tmpVal1 = Ae.doubleValue(i1, j1);
                if (tmpVal1 != 0.0) {
                    data.setAE(i1, j1, tmpVal1);
                }
            }

            data.setBE(i1, Be.doubleValue(i1));
        }

        for (int i1 = 0; i1 < Bi.getRowDim(); i1++) {

            for (int j1 = 0; j1 < nbVars1; j1++) {
                double tmpVal1 = Ai.doubleValue(i1, j1);
                if (tmpVal1 != 0.0) {
                    data.setAI(i1, j1, tmpVal1);
                }
            }

            data.setBI(i1, Bi.doubleValue(i1));
        }
        return data;
    }

    private static void updateConvexDoubleData(final ConvexData<Double> data, final MatrixStore<Quadruple> C, final MatrixStore<Quadruple> Be,
            final MatrixStore<Quadruple> Bi) {
        data.getObjective().linear().fillMatching(C);

        for (int i = 0; i < Be.getRowDim(); i++) {
            data.setBE(i, Be.doubleValue(i));
        }

        for (int i = 0; i < Bi.getRowDim(); i++) {
            data.setBI(i, Bi.doubleValue(i));
        }
    }

    private static void updateConvexDoubleData(final ConvexData<Double> data, final MatrixStore<Quadruple> Q, final MatrixStore<Quadruple> C,
            final MatrixStore<Quadruple> Be, final MatrixStore<Quadruple> Bi) {
        data.getObjective().quadratic().fillMatching(Q);
        IterativeRefinementSolver.updateConvexDoubleData(data, C, Be, Bi);
    }

    static Optimisation.Result doSolve(final MatrixStore<Quadruple> Q, final MatrixStore<Quadruple> C, final MatrixStore<Quadruple> Ae,
            final MatrixStore<Quadruple> Be, final MatrixStore<Quadruple> Ai, final MatrixStore<Quadruple> Bi, final Optimisation.Options options) {

        // Required threshold for final residuals
        double threshold = options.solution.epsilon();
        double epsPrimal = threshold;
        double epsDual = threshold;
        double epsSlack = threshold;
        boolean combinedScaleFactor = options.convex().isCombinedScaleFactor();

        //  Constants to modify
        double maxZoomFactor = 1.0E12;
        int maxRefinementIterations = 5;
        int maxTriesOnFailure = 3;
        double smallestNoneZeroHessian = 1e-10;

        // Magnitude of parameters in the model
        double magnitude_Be = Be.aggregateAll(Aggregator.LARGEST).doubleValue();
        double magnitude_Bi = Bi.aggregateAll(Aggregator.LARGEST).doubleValue();
        double magnitude_B = Math.max(magnitude_Be, Math.max(magnitude_Bi, 1E-15));
        double magnitude_C = C.aggregateAll(Aggregator.LARGEST).doubleValue();
        magnitude_C = Math.max(magnitude_C, 1E-15);
        double magnitude_Q = Q.aggregateAll(Aggregator.LARGEST).doubleValue();
        if (magnitude_Q < smallestNoneZeroHessian) {
            //   Dual parameters do not work with tiny Hessians.
            combinedScaleFactor = true;
        }
        ConvexData<Double> data = new ConvexData<>(false, R064Store.FACTORY, C.getRowDim(), Be.getRowDim(), Bi.getRowDim());

        data = IterativeRefinementSolver.getDoubleConvexData(data, Q, C, Ae, Be, Ai, Bi);

        Optimisation.Result x_y_double = BasePrimitiveSolver.newSolver(data, options).solve();
        if (x_y_double.getState() == Optimisation.State.INFEASIBLE) {
            //         trust solver and abort if infeasible.
            return x_y_double;
        }

        MatrixStore<Quadruple> x0 = GenericStore.R128.column(x_y_double);
        MatrixStore<Quadruple> y0 = GenericStore.R128.column(x_y_double.getMultipliers().orElseThrow());
        double initialSolutionValue = x_y_double.getValue();

        //  Set initial values
        double scaleP0 = 1;
        double scaleD0 = 1;
        int iteration = 0;
        State exitState = State.FEASIBLE;

        refinement: while (x_y_double.getState().isFeasible()) {
            /*
             * If set of active inequalities do not change between iterations. Then one can try to solve the
             * system of linear equations (KKT) using high precision and return this answer if it is a
             * success, after checking inequality multipliers.
             */
            iteration++;

            // Compute residuals in Quadruple precision
            MatrixStore<Quadruple> residual_Be = Be.subtract(Ae.multiply(x0));
            double maxEqualityResidual = residual_Be.aggregateAll(Aggregator.LARGEST).doubleValue();
            MatrixStore<Quadruple> residual_Bi = Bi.subtract(Ai.multiply(x0));
            double maxInequalityResidual = residual_Bi.negate().aggregateAll(Aggregator.MAXIMUM).doubleValue();
            double maxPrimalResidual = Math.max(maxEqualityResidual, maxInequalityResidual);
            MatrixStore<Quadruple> residual_C = C.subtract(Q.multiply(x0)).subtract(Ae.below(Ai).transpose().multiply(y0));
            double maxGradientResidual = residual_C.negate().aggregateAll(Aggregator.LARGEST).norm();
            // SUM_i ABS(C1_i * x_i) / |residual_C|
            double relativeComplementarySlackness1 = residual_C.onMatching(QuadrupleMath.MULTIPLY, x0).collect(GenericStore.R128)
                    .aggregateAll(Aggregator.LARGEST).doubleValue() / magnitude_C;
            // SUM_i ABS(y0_i * b_i) / |Be|
            double relativeComplementarySlackness2 = y0.onMatching(QuadrupleMath.MULTIPLY, residual_Be.below(residual_Bi)).collect(GenericStore.R128)
                    .aggregateAll(Aggregator.LARGEST).doubleValue() / magnitude_B;
            double relativeComplementarySlackness = Math.max(relativeComplementarySlackness1,
                    relativeComplementarySlackness2 * relativeComplementarySlackness2);
            double relativeGradientResidual = maxGradientResidual / magnitude_C;
            if (maxPrimalResidual < epsPrimal && relativeGradientResidual < epsDual && relativeComplementarySlackness < epsSlack) {
                //  Solution fulfils the use given thresholds for residuals
                exitState = State.OPTIMAL;
                break refinement;
            }
            if (iteration > maxRefinementIterations) {
                if (maxPrimalResidual < Math.sqrt(epsPrimal) && relativeGradientResidual < Math.sqrt(epsDual)
                        && relativeComplementarySlackness < Math.sqrt(epsSlack)) {
                    exitState = State.APPROXIMATE;
                } else {
                    exitState = State.FEASIBLE;
                }
                break refinement;
            }

            // Compute refinement scale factor(s)
            double scaleP1 = Math.min(1 / maxPrimalResidual, maxZoomFactor * scaleP0);
            scaleP1 = Math.max(1, scaleP1);
            double scaleD1 = Math.min(1 / maxGradientResidual, maxZoomFactor * scaleD0);
            scaleD1 = Math.max(1, scaleD1);
            if (combinedScaleFactor) {
                scaleP1 = scaleD1 = Math.min(scaleP1, scaleD1);
            } else {
                double scaledHessianNorm = magnitude_Q * scaleD1 / scaleP1;
                if (scaledHessianNorm < smallestNoneZeroHessian) {
                    //  Avoid ojAlgo classifying the Hessian matrix as zero.
                    //  Should a different solver (LP) be used for this case instead?
                    scaleP1 = magnitude_Q * scaleD1 / scaledHessianNorm;
                }
            }

            int noTries = 0;
            do {
                noTries++;
                // Prepare the correction model
                MatrixStore<Quadruple> Q1_;
                if (!combinedScaleFactor) {
                    Q1_ = Q.multiply(scaleD1 / scaleP1);
                } else {
                    Q1_ = Q;
                }
                MatrixStore<Quadruple> C1_ = residual_C.multiply(scaleD1);
                MatrixStore<Quadruple> ae1_ = Ae;
                MatrixStore<Quadruple> be1_ = residual_Be.multiply(scaleP1);
                MatrixStore<Quadruple> ai1_ = Ai;
                MatrixStore<Quadruple> bi1_ = residual_Bi.multiply(scaleP1);

                //solve correction model
                if (!combinedScaleFactor) {
                    IterativeRefinementSolver.updateConvexDoubleData(data, Q1_, C1_, be1_, bi1_);
                } else {
                    IterativeRefinementSolver.updateConvexDoubleData(data, C1_, be1_, bi1_);
                }

                x_y_double = BasePrimitiveSolver.newSolver(data, options).solve();
                if (!x_y_double.getState().isOptimal()) {
                    // on failure try a smaller zoom factor
                    double increaseP = scaleP1 / scaleP0;
                    double newIncreaseP = Math.sqrt(increaseP);
                    scaleP1 = scaleP0 * newIncreaseP;
                    double increaseD = scaleD1 / scaleD0;
                    double newIncreaseD = Math.sqrt(increaseD);
                    scaleD1 = scaleD0 * newIncreaseD;
                }

                if (noTries == maxTriesOnFailure) {
                    //  Can't solve this subproblem. Abort.
                    if (maxPrimalResidual < Math.sqrt(epsPrimal) && relativeGradientResidual < Math.sqrt(epsDual)
                            && relativeComplementarySlackness < Math.sqrt(epsSlack)) {
                        exitState = State.APPROXIMATE;
                    } else {
                        exitState = State.FEASIBLE;
                    }
                    break refinement;
                }

            } while (!x_y_double.getState().isOptimal());

            MatrixStore<Quadruple> x0_ = GenericStore.R128.column(x_y_double);
            MatrixStore<Quadruple> y0_ = GenericStore.R128.column(x_y_double.getMultipliers().orElseThrow());
            // refine the Quadruple precision solution
            MatrixStore<Quadruple> x1 = x0.add(x0_.divide(scaleP1));
            MatrixStore<Quadruple> y1 = y0.add(y0_.divide(scaleD1));
            scaleP0 = scaleP1;
            scaleD0 = scaleD1;
            x0 = x1;
            y0 = y1;
        }
        Result result = IterativeRefinementSolver.buildResult(Q, C, x0, y0, exitState);
        double improvement = (initialSolutionValue - result.getValue()) / initialSolutionValue;
        return result;
    }

    static ConvexData<Quadruple> newInstance(final int nbVars, final int nbEqus, final int nbIneq) {
        return new ConvexData<>(false, GenericStore.R128, nbVars, nbEqus, nbIneq);
    }

    private final ConvexData<Quadruple> myData;

    IterativeRefinementSolver(final Optimisation.Options optimisationOptions, final ConvexData<Quadruple> convexData) {
        super(optimisationOptions);
        myData = convexData;
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
