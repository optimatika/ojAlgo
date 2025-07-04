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

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Access1D;

/**
 * Algorithm from: Solving quadratic programs to high precision using scaled iterative refinement <br>
 * Mathematical Programming Computation (2019) 11:421–455 https://doi.org/10.1007/s12532-019-00154-6
 *
 * @author Programmer-Magnus
 */
final class IterativeRefinementSolver2 extends ConvexSolver {

    static Optimisation.Result doSolve(final MatrixStore<Quadruple> modQ, final MatrixStore<Quadruple> modC, final MatrixStore<Quadruple> modAE,
            final MatrixStore<Quadruple> modBE, final MatrixStore<Quadruple> modAI, final MatrixStore<Quadruple> modBI, final Optimisation.Options options) {

        int nbVars = modQ.getMinDim();
        int nbEqus = modBE.getRowDim();
        int nbIneq = modBI.getRowDim();

        ConvexData<Double> iterationData = new ConvexData<>(false, R064Store.FACTORY, nbVars, nbEqus, nbIneq);
        BasePrimitiveSolver iterationSolver = BasePrimitiveSolver.newSolver(iterationData, options);

        iterationData.getObjective().quadratic().fillMatching(modQ);

        for (int i = 0; i < nbEqus; i++) {
            for (int j = 0; j < nbVars; j++) {
                double tmpVal = modAE.doubleValue(i, j);
                if (tmpVal != PrimitiveMath.ZERO) {
                    iterationData.setAE(i, j, tmpVal);
                }
            }
        }

        for (int i = 0; i < nbIneq; i++) {
            for (int j = 0; j < nbVars; j++) {
                double tmpVal = modAI.doubleValue(i, j);
                if (tmpVal != PrimitiveMath.ZERO) {
                    iterationData.setAI(i, j, tmpVal);
                }
            }
        }

        Optimisation.State retState = Optimisation.State.UNEXPLORED;
        double retValue = PrimitiveMath.ZERO;
        PhysicalStore<Quadruple> retX = GenericStore.R128.make(nbVars, 1);
        PhysicalStore<Quadruple> retLE = GenericStore.R128.make(nbEqus, 1);
        PhysicalStore<Quadruple> retLI = GenericStore.R128.make(nbIneq, 1);

        PhysicalStore<Quadruple> iterC = modC.copy();
        PhysicalStore<Quadruple> iterBE = modBE.copy();
        PhysicalStore<Quadruple> iterBI = modBI.copy();
        PhysicalStore<Quadruple> iterX = GenericStore.R128.make(nbVars, 1);
        PhysicalStore<Quadruple> iterLE = GenericStore.R128.make(nbEqus, 1);
        PhysicalStore<Quadruple> iterLI = GenericStore.R128.make(nbIneq, 1);

        Quadruple zoom = QuadrupleMath.ONE;
        double error = PrimitiveMath.MACHINE_LARGEST;
        Result approximate = Optimisation.Result.of(Optimisation.State.APPROXIMATE).withSolution(R064Store.FACTORY.makeZero(nbVars, 1));

        int iteration = 0;

        do {

            ++iteration;

            boolean zoomed = !zoom.equals(QuadrupleMath.ONE);

            if (zoomed) {
                UnaryFunction<Quadruple> multiplier = QuadrupleMath.MULTIPLY.second(zoom);
                iterC.modifyAll(multiplier);
                iterBE.modifyAll(multiplier);
                iterBI.modifyAll(multiplier);
            }

            iterationSolver.update(iterC, iterBE, iterBI);

            Result iterationResult = null;
            if (zoomed) {
                iterationResult = iterationSolver.solve(approximate);
            } else {
                iterationResult = iterationSolver.solve();
            }

            Access1D<?> multipliers = iterationResult.getMultipliers().get();

            iterX.fillMatching(iterationResult);
            for (int i = 0; i < nbEqus; i++) {
                iterLE.set(i, multipliers.doubleValue(i));
            }
            for (int i = 0; i < nbIneq; i++) {
                iterLI.set(i, multipliers.doubleValue(nbEqus + i));
            }

            if (zoomed) {
                UnaryFunction<Quadruple> divider = QuadrupleMath.DIVIDE.second(zoom);
                iterX.modifyAll(divider);
                iterLE.modifyAll(divider);
                iterLI.modifyAll(divider);
            }

            retX.modifyMatching(QuadrupleMath.ADD, iterX);
            retLE.modifyMatching(QuadrupleMath.ADD, iterLE);
            retLI.modifyMatching(QuadrupleMath.ADD, iterLI);

            retState = iterationResult.getState();
            retValue += (iterationResult.getValue() / zoom.doubleValue());

            retX.premultiply(modQ).onMatching(modC, QuadrupleMath.SUBTRACT).supplyTo(iterC);
            if (nbEqus > 0) {
                retX.premultiply(modAE).onMatching(modBE, QuadrupleMath.SUBTRACT).supplyTo(iterBE);
            }
            if (nbIneq > 0) {
                retX.premultiply(modAI).onMatching(modBI, QuadrupleMath.SUBTRACT).supplyTo(iterBI);
            }
            for (int i = 0; i < nbEqus; i++) {
                Quadruple tmpLE = retLE.get(i).negate();
                if (!tmpLE.isZero()) {
                    Access1D<Quadruple> rowAE = modAE.sliceRow(i);
                    for (int j = 0; j < nbVars; j++) {
                        Quadruple tmpAE = rowAE.get(j);
                        if (!tmpAE.isZero()) {
                            iterC.add(j, tmpAE.multiply(tmpLE));
                        }
                    }
                }
            }
            for (int i = 0; i < nbIneq; i++) {
                Quadruple tmpLI = retLI.get(i).negate();
                if (!tmpLI.isZero()) {
                    Access1D<Quadruple> rowAI = modAI.sliceRow(i);
                    for (int j = 0; j < nbVars; j++) {
                        Quadruple tmpAI = rowAI.get(j);
                        if (!tmpAI.isZero()) {
                            iterC.add(j, tmpAI.multiply(tmpLI));
                        }
                    }
                }
            }

            double errorC = iterC.aggregateAll(Aggregator.LARGEST).doubleValue();
            double errorE = iterBE.aggregateAll(Aggregator.LARGEST).doubleValue();
            double errorI = Math.max(PrimitiveMath.ZERO, -iterBI.aggregateAll(Aggregator.MINIMUM).doubleValue());
            error = MissingMath.max(1e-34, errorC, errorE, errorI);

            zoom = zoom.divide(Math.sqrt(error));

        } while (iteration < 10 && retState.isFeasible() && error > options.solution.epsilon());

        if (retState.isOptimal() && error > options.solution.epsilon()) {
            retState = Optimisation.State.APPROXIMATE;
        }

        return new Optimisation.Result(retState, retValue, retX).multipliers(retLE.below(retLI));
    }

    private final ConvexData<Quadruple> myData;

    IterativeRefinementSolver2(final Optimisation.Options optimisationOptions, final ConvexData<Quadruple> convexData) {
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

        return IterativeRefinementSolver2.doSolve(mtrxQ, mtrxC, mtrxAE, mtrxBE, mtrxAI, mtrxBI, options);
    }

}
