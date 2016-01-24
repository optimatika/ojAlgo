/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.List;

import org.ojalgo.access.Access2D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.type.context.NumberContext;

public final class GaussSeidelSolver extends StationaryIterativeSolver implements IterativeSolverTask.SparseDelegate {

    public GaussSeidelSolver() {
        super();
    }

    public void resolve(final List<Equation> body, final PhysicalStore<Double> current) {

        double tmpCurrNorm = NEG;
        double tmpLastNorm = tmpCurrNorm;

        int tmpIterations = 0;
        final int tmpIterationsLimit = this.getIterationsLimit();
        final NumberContext tmpCntxt = this.getTerminationContext();
        final double tmpRelaxationFactor = this.getRelaxationFactor();

        do {

            final int tmpSize = body.size();
            for (int r = 0; r < tmpSize; r++) {
                final Equation tmpRow = body.get(r);
                tmpRow.adjust(current, tmpRelaxationFactor);
            }

            tmpLastNorm = tmpCurrNorm;
            tmpCurrNorm = current.aggregateAll(Aggregator.NORM2);

            tmpIterations++;

        } while ((tmpIterations < tmpIterationsLimit) && tmpCntxt.isDifferent(tmpLastNorm, tmpCurrNorm));

    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated) throws TaskException {

        final List<Equation> tmpRows = IterativeSolverTask.toListOfRows(body, rhs);

        this.resolve(tmpRows, preallocated);

        return preallocated;
    }

}
