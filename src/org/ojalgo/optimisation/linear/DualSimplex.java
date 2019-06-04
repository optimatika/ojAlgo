/*
 * Copyright 1997-2019 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.type.context.NumberContext;

final class DualSimplex extends SimplexSolver {

    static SimplexTableau build(final ConvexSolver.Builder convex, final Optimisation.Options options) {

        int numbVars = convex.countVariables();
        int numbEqus = convex.countEqualityConstraints();
        int numbInes = convex.countInequalityConstraints();

        SimplexTableau retVal = SimplexTableau.make(numbVars, numbEqus + numbEqus + numbInes, 0, options);

        Mutate1D obj = retVal.objective();
        Mutate2D constrBody = retVal.constraintsBody();
        Mutate1D constrRHS = retVal.constraintsRHS();

        MatrixStore<Double> convexC = convex.getC();
        MatrixStore<Double> convexAE = convex.getAE();
        MatrixStore<Double> convexBE = convex.getBE();
        RowsSupplier<Double> convexAI = convex.getAI();
        MatrixStore<Double> convexBI = convex.getBI();

        for (int i = 0; i < numbVars; i++) {
            double rhs = convexC.doubleValue(i);
            boolean neg = NumberContext.compare(rhs, ZERO) < 0;

            for (int j = 0; j < numbEqus; j++) {
                double valE = convexAE.doubleValue(j, i);
                constrBody.set(i, j, neg ? -valE : valE);
                constrBody.set(i, numbEqus + j, neg ? valE : -valE);
            }
            for (int j = 0; j < numbInes; j++) {
                double valI = convexAI.doubleValue(j, i);
                constrBody.set(i, numbEqus + numbEqus + j, neg ? -valI : valI);
            }
            constrRHS.set(i, neg ? -rhs : rhs);
        }

        for (int j = 0; j < numbEqus; j++) {
            obj.set(j, convexBE.doubleValue(j));
            obj.set(numbEqus + j, -convexBE.doubleValue(j));
        }
        for (int j = 0; j < numbInes; j++) {
            obj.set(numbEqus + numbEqus + j, convexBI.doubleValue(j));
        }

        return retVal;
    }

    DualSimplex(final SimplexTableau tableau, final Options solverOptions) {
        super(tableau, solverOptions);
    }

    @Override
    protected Access1D<?> extractMultipliers() {
        return super.extractSolution();
    }

    @Override
    protected Access1D<?> extractSolution() {
        return super.extractMultipliers();
    }

}
