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

import java.util.Optional;

import org.ojalgo.access.Access2D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.type.context.NumberContext;

/**
 * StationaryIterativeSolver
 *
 * @see https://en.wikipedia.org/wiki/Iterative_method#Stationary_iterative_methods
 * @see http://mathworld.wolfram.com/StationaryIterativeMethod.html
 * @author apete
 */
abstract class StationaryIterativeSolver extends IterativeSolverTask {

    private double myRelaxationFactor = ONE;

    StationaryIterativeSolver() {
        super();
    }

    StationaryIterativeSolver(final NumberContext terminationContext) {
        super(terminationContext);
    }

    public final double getRelaxationFactor() {
        return myRelaxationFactor;
    }

    public abstract MatrixStore<Double> iterate(final PhysicalStore<Double> current, double relaxation);

    public final void setRelaxationFactor(final double relaxation) {
        myRelaxationFactor = relaxation;
    }

    public abstract void setup(final Access2D<?> body, final Access2D<?> rhs);

    public final MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated) throws TaskException {

        this.setup(body, rhs);

        double tmpCurrNorm = NEG;
        double tmpLastNorm = tmpCurrNorm;

        final NumberContext tmpCntxt = this.getTerminationContext();
        do {

            this.iterate(preallocated, myRelaxationFactor);

            tmpLastNorm = tmpCurrNorm;
            tmpCurrNorm = preallocated.aggregateAll(Aggregator.NORM2);

        } while (tmpCntxt.isDifferent(tmpLastNorm, tmpCurrNorm));

        return preallocated;
    }

    public final Optional<MatrixStore<Double>> solve(final MatrixStore<Double> body, final MatrixStore<Double> rhs) {
        try {
            return Optional.of(this.solve(body, rhs, this.preallocate(body, rhs)));
        } catch (final TaskException xcptn) {
            return Optional.empty();
        }
    }

}
