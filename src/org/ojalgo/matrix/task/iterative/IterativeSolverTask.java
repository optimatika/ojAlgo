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

    private final NumberContext myTerminationContext;

    IterativeSolverTask() {
        this(NumberContext.getMath(MathContext.DECIMAL32));
    }

    IterativeSolverTask(final NumberContext terminationContext) {
        super();
        myTerminationContext = terminationContext;
    }

    public final DecompositionStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        if (templateRHS.countColumns() != 1L) {
            throw new IllegalArgumentException("The RHS must have precisely 1 column!");
        }
        return PrimitiveDenseStore.FACTORY.makeZero(templateRHS.countRows(), 1L);
    }

    protected final NumberContext getTerminationContext() {
        return myTerminationContext;
    }

}
