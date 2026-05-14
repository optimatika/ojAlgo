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
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.structure.Access1D;

/**
 * Algorithm from: Solving quadratic programs to high precision using scaled iterative refinement
 * <p>
 * Mathematical Programming Computation (2019) 11:421–455 https://doi.org/10.1007/s12532-019-00154-6
 */
final class IterativeRefinementSolver22 extends ConvexSolver {

    private static final Quadruple ZOOM_STEP = Quadruple.valueOf(1E4);

    private final AlternatingDirectionSolver.Composer<Quadruple> myData;

    IterativeRefinementSolver22(final Optimisation.Options optimisationOptions,
            final AlternatingDirectionSolver.Composer<Quadruple> alternatingDirectionSolverComposer) {

        super(optimisationOptions);

        myData = alternatingDirectionSolverComposer;
    }

    @Override
    public Result solve(final Result kickStarter) {

        MatrixStore<Quadruple> probP = myData.getP();
        MatrixStore<Quadruple> probQ = myData.getQ();

        MatrixStore<Quadruple> probA = myData.getA();
        MatrixStore<Quadruple> probL = myData.getL();
        MatrixStore<Quadruple> probU = myData.getU();

        int m = probA.getRowDim();
        int n = probA.getColDim();

        AlternatingDirectionSolver iterSolver = new AlternatingDirectionSolver(myData.toProblem(), options, myData.getStructure());

        PhysicalStore<Quadruple> iterQ = probQ.copy();
        PhysicalStore<Quadruple> iterL = probL.copy();
        PhysicalStore<Quadruple> iterU = probU.copy();
        PhysicalStore<Quadruple> iterX = GenericStore.R128.make(n, 1);
        PhysicalStore<Quadruple> iterY = GenericStore.R128.make(m, 1);

        PhysicalStore<Quadruple> aggrX = GenericStore.R128.make(n, 1);
        PhysicalStore<Quadruple> aggrY = GenericStore.R128.make(m, 1);

        state = Optimisation.State.UNEXPLORED;
        double retValue = PrimitiveMath.ZERO;
        Optimisation.State iterState = Optimisation.State.UNEXPLORED;

        PhysicalStore<Quadruple> retX = GenericStore.R128.make(n, 1);
        PhysicalStore<Quadruple> retY = GenericStore.R128.make(m, 1);

        Quadruple zoom = QuadrupleMath.ONE;
        double currPrimError = PrimitiveMath.MACHINE_LARGEST;
        double lastPrimError = PrimitiveMath.MACHINE_LARGEST;
        double currDualError = PrimitiveMath.MACHINE_LARGEST;
        double lastDualError = PrimitiveMath.MACHINE_LARGEST;
        boolean didImprove = true;

        int iteration = 0;
        do {

            ++iteration;

            boolean zoomed = !zoom.equals(QuadrupleMath.ONE);

            if (zoomed) {
                UnaryFunction<Quadruple> multiplier = QuadrupleMath.MULTIPLY.second(zoom);
                iterQ.modifyAll(multiplier);
                iterL.modifyAll(multiplier);
                iterU.modifyAll(multiplier);
            }

            iterSolver.updateRefined(iterQ, iterL, iterU);

            if (this.isLogDebug()) {
                this.log();
                this.log("I.R. Iteration {} with zoom factor {}", iteration, zoom);
            }

            Result iterationResult = iterSolver.solve();

            iterState = iterationResult.getState();

            if (!iterState.isFeasible() || iterationResult.getMultipliers().isEmpty()) {
                didImprove = false;
                break;
            }

            Access1D<?> multipliers = iterationResult.getMultipliers().get();

            for (int j = 0; j < n; j++) {
                iterX.set(j, iterationResult.doubleValue(j));
            }
            for (int i = 0; i < m; i++) {
                iterY.set(i, multipliers.doubleValue(i));
            }

            if (zoomed) {
                UnaryFunction<Quadruple> divider = QuadrupleMath.MULTIPLY.second(zoom.invert());
                iterX.modifyAll(divider);
                iterY.modifyAll(divider);
            }

            aggrX.modifyMatching(QuadrupleMath.ADD, iterX);
            aggrY.modifyMatching(QuadrupleMath.ADD, iterY);

            iterState = iterationResult.getState();

            retValue += (iterationResult.getValue() / zoom.doubleValue());

            aggrX.premultiply(probP).onMatching(probQ, QuadrupleMath.ADD).supplyTo(iterQ);

            aggrX.premultiply(probA).onMatching(probL, QuadrupleMath.SUBTRACT).supplyTo(iterL);
            aggrX.premultiply(probA).onMatching(probU, QuadrupleMath.SUBTRACT).supplyTo(iterU);

            for (int i = 0; i < m; i++) {
                Quadruple tmpY = aggrY.get(i);
                if (tmpY.isAbsolute()) {
                    tmpY = tmpY.negate();
                }
                if (!tmpY.isZero()) {
                    Access1D<Quadruple> rowA = probA.sliceRow(i);
                    for (int j = 0; j < n; j++) {
                        Quadruple tmpA = rowA.get(j);
                        if (!tmpA.isZero()) {
                            iterQ.add(j, tmpA.multiply(tmpY));
                        }
                    }
                }
            }

            Quadruple magnDualResidual = iterQ.aggregateAll(Aggregator.LARGEST);

            Quadruple magnPrimResidual = Quadruple.ZERO;
            for (int i = 0; i < m; i++) {
                magnPrimResidual = QuadrupleMath.MAX.invoke(magnPrimResidual,
                        QuadrupleMath.MIN.invoke(QuadrupleMath.ABS.invoke(iterL.get(i)), QuadrupleMath.ABS.invoke(iterU.get(i))));
            }

            lastPrimError = currPrimError;
            lastDualError = currDualError;

            currPrimError = magnPrimResidual.doubleValue();
            currDualError = magnDualResidual.doubleValue();

            if (this.isLogDebug()) {
                this.log("I.R. Errors (prim & dual): {} & {} ", magnPrimResidual, magnDualResidual);
            }

            if ((currPrimError <= lastPrimError || currDualError <= lastDualError) && iterState.isFeasible()) {
                didImprove = true;
                retX.fillMatching(aggrX);
                retY.fillMatching(aggrY);
                state = Optimisation.State.FEASIBLE;
                if (this.isLogDebug()) {
                    this.log("I.R. Solution: {}", retX.asList());
                }
                zoom = zoom.multiply(ZOOM_STEP);
            } else {
                didImprove = false;
            }

        } while (didImprove && iteration < 8 && this.isIterationAllowed());

        if (iterState.isOptimal()) {
            state = Optimisation.State.OPTIMAL;
        }

        return new Optimisation.Result(state, retValue, retX).multipliers(retY);
    }

}