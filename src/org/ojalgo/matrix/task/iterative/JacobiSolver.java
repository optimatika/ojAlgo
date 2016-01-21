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
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.access.Access2D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.type.context.NumberContext;

public final class JacobiSolver extends StationaryIterativeSolver {

    public JacobiSolver() {
        super();
    }

    @SuppressWarnings("unchecked")
    public final MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated) throws TaskException {

        MatrixStore<Double> tmpBody = null;
        if ((body instanceof MatrixStore<?>) && (body.get(0L) instanceof Double)) {
            tmpBody = (MatrixStore<Double>) body;
        } else {
            tmpBody = MatrixStore.PRIMITIVE.makeWrapper(body).get();
        }
        final MatrixStore<Double> tmpBodyDiagonal = PrimitiveDenseStore.FACTORY.columns(tmpBody.sliceDiagonal(0L, 0L));

        MatrixStore<Double> tmpRHS = null;
        if ((rhs instanceof MatrixStore<?>) && (rhs.get(0L) instanceof Double)) {
            tmpRHS = (MatrixStore<Double>) rhs;
        } else {
            tmpRHS = MatrixStore.PRIMITIVE.makeWrapper(rhs).get();
        }

        final PhysicalStore<Double> tmpIncrement = this.preallocate(body, rhs);

        double tmpCurrNorm = NEG;
        double tmpLastNorm = tmpCurrNorm;

        int tmpIterations = 0;
        final int tmpIterationsLimit = this.getIterationsLimit();
        final NumberContext tmpCntxt = this.getTerminationContext();
        final double tmpRelaxation = this.getRelaxationFactor();
        do {

            preallocated.multiplyLeft(tmpBody).operateOnMatching(tmpRHS, SUBTRACT).operateOnMatching(DIVIDE, tmpBodyDiagonal).supplyTo(tmpIncrement);

            if (this.getTerminationContext().isDifferent(ONE, tmpRelaxation)) {
                tmpIncrement.multiply(tmpRelaxation);
            }

            preallocated.modifyMatching(ADD, tmpIncrement);
            tmpLastNorm = tmpCurrNorm;
            tmpCurrNorm = preallocated.aggregateAll(Aggregator.NORM2);

            tmpIterations++;

        } while ((tmpIterations < tmpIterationsLimit) && tmpCntxt.isDifferent(tmpLastNorm, tmpCurrNorm));

        return preallocated;
    }

}
