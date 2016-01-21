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

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.context.NumberContext;

abstract class IterativeSolverTask implements SolverTask<Double> {

    static interface SparseDelegate {

        void resolve(List<Row> body, Access2D<?> rhs, final PhysicalStore<Double> current);

    }

    static List<Row> toListOfRows(final Access2D<?> body) {

        final List<Row> retVal = new ArrayList<>();

        final long tmpDim = body.countRows();

        for (int i = 0; i < tmpDim; i++) {
            final Row tmpRow = new Row(i, tmpDim);
            for (int j = 0; j < tmpDim; j++) {
                final double tmpVal = body.doubleValue(i, j);
                if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                    tmpRow.set(j, tmpVal);
                }
            }
            retVal.add(tmpRow);
        }

        return retVal;
    }

    private int myIterationsLimit = Integer.MAX_VALUE;
    private NumberContext myTerminationContext = NumberContext.getMath(MathContext.DECIMAL128);

    IterativeSolverTask() {
        super();
    }

    public final DecompositionStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        if (templateRHS.countColumns() != 1L) {
            throw new IllegalArgumentException("The RHS must have precisely 1 column!");
        }
        return PrimitiveDenseStore.FACTORY.makeZero(templateRHS.countRows(), 1L);
    }

    public final Optional<MatrixStore<Double>> solve(final MatrixStore<Double> body, final MatrixStore<Double> rhs) {
        try {
            return Optional.of(this.solve(body, rhs, this.preallocate(body, rhs)));
        } catch (final TaskException xcptn) {
            return Optional.empty();
        }
    }

    protected final int getIterationsLimit() {
        return myIterationsLimit;
    }

    protected final NumberContext getTerminationContext() {
        return myTerminationContext;
    }

    protected void setIterationsLimit(final int iterationsLimit) {
        myIterationsLimit = iterationsLimit;
    }

    protected void setTerminationContext(final NumberContext terminationContext) {
        myTerminationContext = terminationContext;
    }

}
