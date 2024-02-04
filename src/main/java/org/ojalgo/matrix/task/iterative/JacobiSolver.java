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

import org.ojalgo.RecoverableCondition;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * For solving [A][x]=[b] where [A] has non-zero elements on the diagonal.
 * <p>
 * It's most likely better to instead use {@link GaussSeidelSolver} or {@link ConjugateGradientSolver}.
 *
 * @author apete
 * @see https://en.wikipedia.org/wiki/Jacobi_method
 */
public final class JacobiSolver extends StationaryIterativeSolver {

    public JacobiSolver() {
        super();
    }

    @SuppressWarnings("unchecked")
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> current) throws RecoverableCondition {

        MatrixStore<Double> tmpBody = null;
        if (body instanceof MatrixStore<?> && body.get(0L) instanceof Double) {
            tmpBody = (MatrixStore<Double>) body;
        } else {
            tmpBody = Primitive64Store.FACTORY.makeWrapper(body);
        }
        MatrixStore<Double> tmpBodyDiagonal = Primitive64Store.FACTORY.columns(tmpBody.sliceDiagonal());

        MatrixStore<Double> tmpRHS = null;
        if (rhs instanceof MatrixStore<?> && rhs.get(0L) instanceof Double) {
            tmpRHS = (MatrixStore<Double>) rhs;
        } else {
            tmpRHS = Primitive64Store.FACTORY.makeWrapper(rhs);
        }

        PhysicalStore<Double> tmpIncrement = this.preallocate(body, rhs);
        TransformableRegion<Double> incremetReceiver = body.isFat() ? tmpIncrement.regionByLimits((int) body.countRows(), 1) : tmpIncrement;

        double tmpNormErr = POSITIVE_INFINITY;
        double tmpNormRHS = tmpRHS.aggregateAll(Aggregator.NORM2);

        int tmpIterations = 0;
        int tmpLimit = this.getIterationsLimit();
        NumberContext tmpCntxt = this.getAccuracyContext();
        double tmpRelaxation = this.getRelaxationFactor();
        do {

            current.premultiply(tmpBody).onMatching(tmpRHS, SUBTRACT).supplyTo(incremetReceiver);
            tmpNormErr = tmpIncrement.aggregateAll(Aggregator.NORM2);
            tmpIncrement.modifyMatching(DIVIDE, tmpBodyDiagonal);

            if (this.getAccuracyContext().isDifferent(ONE, tmpRelaxation)) {
                tmpIncrement.multiply(tmpRelaxation);
            }

            current.modifyMatching(ADD, tmpIncrement);

            tmpIterations++;

            if (this.isDebugPrinterSet()) {
                this.debug(tmpIterations, tmpNormErr / tmpNormRHS, current);
            }

        } while (tmpIterations < tmpLimit && !tmpCntxt.isSmall(tmpNormRHS, tmpNormErr));

        return current;
    }

}
