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
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * For solving [A][x]=[b] where [A] has non-zero elements on the diagonal.
 * <p>
 * To guarantee convergence [A] needs to be either strictly diagonally dominant, or symmetric and positive
 * definite.
 *
 * @author apete
 * @see https://en.wikipedia.org/wiki/Gauss–Seidel_method
 */
public final class GaussSeidelSolver extends StationaryIterativeSolver implements IterativeSolverTask.SparseDelegate {

    public GaussSeidelSolver() {
        super();
    }

    public double resolve(final List<Equation> equations, final PhysicalStore<Double> solution) {

        double tmpNormErr = POSITIVE_INFINITY;
        double tmpNormRHS = ZERO;

        final int tmpCountRows = equations.size();
        for (int r = 0; r < tmpCountRows; r++) {
            tmpNormRHS = HYPOT.invoke(tmpNormRHS, equations.get(r).getRHS());
        }

        int tmpIterations = 0;
        final int tmpLimit = this.getIterationsLimit();
        final NumberContext tmpCntxt = this.getAccuracyContext();
        final double tmpRelaxationFactor = this.getRelaxationFactor();

        do {

            tmpNormErr = ZERO;

            for (int r = 0; r < tmpCountRows; r++) {
                tmpNormErr = HYPOT.invoke(tmpNormErr, equations.get(r).adjust(solution, tmpRelaxationFactor));
            }

            tmpIterations++;

            if (this.isDebugPrinterSet()) {
                this.debug(tmpIterations, tmpNormErr / tmpNormRHS, solution);
            }

        } while ((tmpIterations < tmpLimit) && !tmpCntxt.isSmall(tmpNormRHS, tmpNormErr));

        return tmpNormErr / tmpNormRHS;
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> current) throws RecoverableCondition {

        final List<Equation> equations = IterativeSolverTask.toListOfRows(body, rhs);

        this.resolve(equations, current);

        return current;
    }

}
