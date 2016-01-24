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
import org.ojalgo.access.Structure1D;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.type.context.NumberContext;

public final class ConjugateGradientSolver extends KrylovSubspaceSolver implements IterativeSolverTask.SparseDelegate {

    private transient PrimitiveDenseStore myDirection = null;
    private transient PrimitiveDenseStore myPreconditioned = null;
    private transient PrimitiveDenseStore myResidual = null;
    private transient PrimitiveDenseStore myVector = null;

    public ConjugateGradientSolver() {
        super();
    }

    public void resolve(final List<Equation> body, final PhysicalStore<Double> current) {

        final int tmpCountRows = body.size();

        final PrimitiveDenseStore tmpResidual = this.residual(current);
        final PrimitiveDenseStore tmpDirection = this.direction(current);
        final PrimitiveDenseStore tmpPreconditioned = this.preconditioned(current);
        final PrimitiveDenseStore tmpVector = this.vector(current);

        double tmpStepLength;
        double tmpGradientCorrectionFactor;

        double zr0 = 1;
        double zr1 = 1;
        double pAp0 = 0;

        for (int i = 0; i < tmpCountRows; i++) {
            final Equation tmpRow = body.get(i);
            double tmpVal = tmpRow.getRHS();
            tmpVal -= tmpRow.getElements().dot(current);
            tmpResidual.set(tmpRow.index, tmpVal);
            tmpPreconditioned.set(tmpRow.index, tmpVal / tmpRow.getPivot()); // precondition
        }

        tmpDirection.fillMatching(tmpPreconditioned);

        double tmpCurrNorm = NEG;
        double tmpLastNorm = tmpCurrNorm;

        final int tmpIterations = 0;
        final int tmpIterationsLimit = this.getIterationsLimit();
        final NumberContext tmpCntxt = this.getTerminationContext();

        zr1 = tmpPreconditioned.transpose().multiply(tmpResidual).doubleValue(0L);

        do {

            zr0 = zr1;

            for (int i = 0; i < tmpCountRows; i++) {
                final Equation tmpRow = body.get(i);
                final double tmpVal = tmpRow.getElements().dot(tmpDirection);
                tmpVector.set(tmpRow.index, tmpVal);
            }

            pAp0 = tmpVector.multiplyLeft(tmpDirection.transpose()).get().doubleValue(0L);

            tmpStepLength = zr0 / pAp0;

            current.maxpy(tmpStepLength, tmpDirection);
            tmpResidual.maxpy(-tmpStepLength, tmpVector);

            // tmpPreconditioned.fillMatching(tmpResidual);
            for (int i = 0; i < tmpCountRows; i++) {
                final Equation tmpRow = body.get(i);
                tmpPreconditioned.set(tmpRow.index, tmpResidual.doubleValue(tmpRow.index) / tmpRow.getPivot());
            }

            zr1 = tmpPreconditioned.transpose().multiply(tmpResidual).doubleValue(0L);
            tmpGradientCorrectionFactor = zr1 / zr0;

            tmpDirection.modifyAll(PrimitiveFunction.MULTIPLY.second(tmpGradientCorrectionFactor));
            tmpDirection.modifyMatching(PrimitiveFunction.ADD, tmpPreconditioned);

            tmpLastNorm = tmpCurrNorm;
            tmpCurrNorm = current.aggregateAll(Aggregator.NORM2);

        } while ((tmpIterations < tmpIterationsLimit)
                && !(!tmpCntxt.isDifferent(tmpLastNorm, tmpCurrNorm) || tmpCntxt.isSmall(tmpCurrNorm, tmpResidual.aggregateAll(Aggregator.NORM2))));

    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated) throws TaskException {

        final List<Equation> tmpRows = IterativeSolverTask.toListOfRows(body, rhs);

        this.resolve(tmpRows, preallocated);

        return preallocated;
    }

    private PrimitiveDenseStore direction(final Structure1D structure) {
        if ((myDirection == null) || (myDirection.count() != structure.count())) {
            myDirection = PrimitiveDenseStore.FACTORY.makeZero(structure.count(), 1L);
        } else {
            myDirection.fillAll(ZERO);
        }
        return myDirection;
    }

    private PrimitiveDenseStore preconditioned(final Structure1D structure) {
        if ((myPreconditioned == null) || (myPreconditioned.count() != structure.count())) {
            myPreconditioned = PrimitiveDenseStore.FACTORY.makeZero(structure.count(), 1L);
        } else {
            myPreconditioned.fillAll(ZERO);
        }
        return myPreconditioned;
    }

    private PrimitiveDenseStore residual(final Structure1D structure) {
        if ((myResidual == null) || (myResidual.count() != structure.count())) {
            myResidual = PrimitiveDenseStore.FACTORY.makeZero(structure.count(), 1L);
        } else {
            myResidual.fillAll(ZERO);
        }
        return myResidual;
    }

    private PrimitiveDenseStore vector(final Structure1D structure) {
        if ((myVector == null) || (myVector.count() != structure.count())) {
            myVector = PrimitiveDenseStore.FACTORY.makeZero(structure.count(), 1L);
        } else {
            myVector.fillAll(ZERO);
        }
        return myVector;
    }

}
