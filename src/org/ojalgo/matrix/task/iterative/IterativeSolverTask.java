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

import java.math.MathContext;

import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.type.context.NumberContext;

abstract class IterativeSolverTask implements SolverTask<Double> {

    private static final int ITERATIONS_LIMIT = Integer.MAX_VALUE;
    private static final NumberContext TERMINATION_CONTEXT = NumberContext.getMath(MathContext.DECIMAL128);

    private final int myIterationsLimit;
    private final NumberContext myTerminationContext;

    IterativeSolverTask() {
        this(TERMINATION_CONTEXT, ITERATIONS_LIMIT);
    }

    IterativeSolverTask(final int iterationsLimit) {
        this(TERMINATION_CONTEXT, iterationsLimit);
    }

    IterativeSolverTask(final NumberContext terminationContext) {
        this(terminationContext, ITERATIONS_LIMIT);
    }

    IterativeSolverTask(final NumberContext terminationContext, final int iterationsLimit) {
        super();
        myTerminationContext = terminationContext;
        myIterationsLimit = iterationsLimit;
    }

    public final DecompositionStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        if (templateRHS.countColumns() != 1L) {
            throw new IllegalArgumentException("The RHS must have precisely 1 column!");
        }
        return PrimitiveDenseStore.FACTORY.makeZero(templateRHS.countRows(), 1L);
    }

    protected final int getIterationsLimit() {
        return myIterationsLimit;
    }

    protected final NumberContext getTerminationContext() {
        return myTerminationContext;
    }

}
