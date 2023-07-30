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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.ConvexData;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.type.context.NumberContext;

final class DualSimplex extends SimplexTableauSolver {

    /**
     * Variant of
     * {@link #build(org.ojalgo.optimisation.convex.ConvexSolver.Builder, org.ojalgo.optimisation.Optimisation.Options, boolean)}
     * that sets a RHS to correspond to the phase 1 objective of the primal.
     */
    private static SimplexTableau buildAlt(final ConvexData<Double> convex, final Optimisation.Options options, final boolean checkFeasibility) {

        int nbVars = convex.countVariables();
        int nbEqus = convex.countEqualityConstraints();
        int nbInes = convex.countInequalityConstraints();

        int nbProblemVariables = nbEqus + nbEqus + nbInes;

        LinearStructure structure = new LinearStructure(false, 0, nbVars, nbProblemVariables, 0, 0, 0, nbVars);

        SimplexTableau retVal = SimplexTableau.make(structure, options);

        Primitive2D constraintsBody = retVal.constraintsBody();
        Primitive1D constraintsRHS = retVal.constraintsRHS();
        Primitive1D objective = retVal.objective();

        MatrixStore<Double> convexC = convex.getObjective().getLinearFactors(true);
        MatrixStore<Double> convexAE = convex.getAE();
        MatrixStore<Double> convexBE = convex.getBE();

        double[] feasibilityC = new double[checkFeasibility ? nbVars : 0];
        if (checkFeasibility) {
            for (RowView<Double> rowAI : convex.getRowsAI()) {
                for (ElementView1D<Double, ?> element : rowAI.nonzeros()) {
                    feasibilityC[Math.toIntExact(element.index())] += element.doubleValue();
                }
            }
        }

        for (int i = 0; i < nbVars; i++) {
            double rhs = checkFeasibility ? i : convexC.doubleValue(i);
            boolean neg = structure.negated(i, NumberContext.compare(rhs, ZERO) < 0);
            for (int j = 0; j < nbEqus; j++) {
                double valE = convexAE.doubleValue(j, i);
                constraintsBody.set(i, j, neg ? -valE : valE);
                constraintsBody.set(i, nbEqus + j, neg ? valE : -valE);
            }
            constraintsRHS.set(i, neg ? -rhs : rhs);
        }

        for (RowView<Double> rowAI : convex.getRowsAI()) {
            int tabJ = Math.toIntExact(rowAI.row());

            for (ElementView1D<Double, ?> element : rowAI.nonzeros()) {
                int tabI = Math.toIntExact(element.index());

                double tabVal = element.doubleValue();
                constraintsBody.set(tabI, nbEqus + nbEqus + tabJ, structure.isConstraintNegated(tabI) ? -tabVal : tabVal);
            }
        }

        for (int j = 0; j < nbEqus; j++) {
            double valBE = convexBE.doubleValue(j);
            objective.set(j, valBE);
            objective.set(nbEqus + j, -valBE);
        }
        for (int j = 0; j < nbInes; j++) {
            double valBI = convex.getBI(j);
            objective.set(nbEqus + nbEqus + j, valBI);
        }

        return retVal;
    }

    static SimplexTableau build(final ConvexData<Double> convex, final Optimisation.Options options, final boolean checkFeasibility) {

        int nbCvxVars = convex.countVariables();
        int nbCvxEqus = convex.countEqualityConstraints();
        int nbCvxInes = convex.countInequalityConstraints();

        LinearStructure structure = new LinearStructure(false, 0, nbCvxVars, nbCvxEqus + nbCvxEqus + nbCvxInes, 0, 0, 0, nbCvxVars);

        SimplexTableau retVal = SimplexTableau.make(structure, options);

        Primitive2D constraintsBody = retVal.constraintsBody();
        Primitive1D constraintsRHS = retVal.constraintsRHS();
        Primitive1D objective = retVal.objective();

        MatrixStore<Double> convexC = convex.getObjective().getLinearFactors(true);

        for (int i = 0; i < nbCvxVars; i++) {
            double rhs = checkFeasibility ? ZERO : convexC.doubleValue(i);
            boolean neg = structure.negated(i, NumberContext.compare(rhs, ZERO) < 0);
            constraintsRHS.set(i, neg ? -rhs : rhs);
        }

        if (nbCvxEqus > 0) {
            for (RowView<Double> rowAE : convex.getRowsAE()) {
                int j = Math.toIntExact(rowAE.row());

                for (ElementView1D<Double, ?> element : rowAE.nonzeros()) {
                    int i = Math.toIntExact(element.index());

                    boolean neg = structure.isConstraintNegated(i);

                    double valE = element.doubleValue();
                    constraintsBody.set(i, j, neg ? -valE : valE);
                    constraintsBody.set(i, nbCvxEqus + j, neg ? valE : -valE);

                }
            }
        }

        if (nbCvxInes > 0) {
            for (RowView<Double> rowAI : convex.getRowsAI()) {
                int j = Math.toIntExact(rowAI.row());

                for (ElementView1D<Double, ?> element : rowAI.nonzeros()) {
                    int i = Math.toIntExact(element.index());

                    double valI = element.doubleValue();
                    constraintsBody.set(i, nbCvxEqus + nbCvxEqus + j, structure.isConstraintNegated(i) ? -valI : valI);
                }
            }
        }

        for (int j = 0; j < nbCvxEqus; j++) {
            double valBE = convex.getBE(j);
            objective.set(j, valBE);
            objective.set(nbCvxEqus + j, -valBE);
        }
        for (int j = 0; j < nbCvxInes; j++) {
            double valBI = convex.getBI(j);
            objective.set(nbCvxEqus + nbCvxEqus + j, valBI);
        }

        return retVal;
    }

    static Optimisation.Result doSolve(final ConvexData<Double> convex, final Optimisation.Options options, final boolean zeroC) {

        SimplexTableau tableau = DualSimplex.build(convex, options, zeroC);

        DualSimplex solver = new DualSimplex(tableau, options);

        Result result = solver.solve();

        return DualSimplex.toConvexState(result, convex);
    }

    static int size(final ConvexData<?> convex) {

        int numbVars = convex.countVariables();
        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        return SimplexTableau.size(numbVars, numbEqus + numbEqus + numbInes, 0, 0, true);
    }

    static Optimisation.Result toConvexState(final Result result, final ConvexData<?> convex) {

        int nbEqus = convex.countEqualityConstraints();
        int nbInes = convex.countInequalityConstraints();

        Access1D<?> multipliers = result.getMultipliers().get();

        Optimisation.Result retVal = new Optimisation.Result(result.getState(), result.getValue(), result);

        retVal.multipliers(new Primitive1D() {

            @Override
            public int size() {
                return nbEqus + nbInes;
            }

            @Override
            double doubleValue(final int index) {
                if (index < nbEqus) {
                    return -(multipliers.doubleValue(index) - multipliers.doubleValue(nbEqus + index));
                }
                return -multipliers.doubleValue(nbEqus + index);
            }

            @Override
            void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

        });

        return retVal;
    }

    DualSimplex(final SimplexTableau tableau, final Options solverOptions) {
        super(tableau, solverOptions);
    }

    @Override
    protected double evaluateFunction(final Access1D<?> solution) {
        return -super.evaluateFunction(solution);
    }

    @Override
    protected Access1D<?> extractMultipliers() {

        Access1D<?> extracted = super.extractSolution();

        return new Primitive1D() {

            @Override
            public int size() {
                return extracted.size();
            }

            @Override
            void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            double doubleValue(final int index) {
                return -extracted.doubleValue(index);
            }
        };
    }

    @Override
    protected Access1D<?> extractSolution() {

        Access1D<?> extracted = super.extractMultipliers();

        return new Primitive1D() {

            @Override
            public int size() {
                return extracted.size();
            }

            @Override
            void set(final int index, final double value) {
                throw new IllegalArgumentException();
            }

            @Override
            double doubleValue(final int index) {
                return -extracted.doubleValue(index);
            }
        };
    }

    @Override
    protected State getState() {

        State state = super.getState();

        if (state == State.UNBOUNDED) {
            return State.INFEASIBLE;
        }
        if (!state.isFeasible()) {
            return State.UNBOUNDED;
        }
        return state;
    }

}
