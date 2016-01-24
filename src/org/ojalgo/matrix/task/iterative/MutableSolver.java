/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.type.context.NumberContext;

/**
 * Access2D refers to the active (not null or zero) rows and columns only - useful for debugging.
 *
 * @author apete
 */
public abstract class MutableSolver<D extends IterativeSolverTask & IterativeSolverTask.SparseDelegate> extends IterativeSolverTask
        implements Access2D<Double> {

    private final D myDelegate;
    private final List<Equation> myRows = new ArrayList<>();
    private final long mySize;

    public MutableSolver(final D delegate, final long size) {

        super();

        myDelegate = delegate;
        mySize = size;
    }

    private MutableSolver() {
        super();
        myDelegate = null;
        mySize = 0L;
    }

    public boolean add(final Equation row) {
        if (row.getElements().count() != mySize) {
            throw new IllegalArgumentException();
        }
        final boolean retVal = myRows.add(row);
        Collections.sort(myRows);
        return retVal;
    }

    public long countColumns() {
        return mySize;
    }

    public long countRows() {
        return mySize;
    }

    public boolean remove(final Equation row) {
        return myRows.remove(row);
    }

    /**
     * A variation of {@linkplain #solve(Access2D, Access2D, DecompositionStore)} where you do not supply the
     * equation system <code>body</code>. It is assumed to have been set up beforehand.
     */
    public final MatrixStore<Double> resolve(final DecompositionStore<Double> preallocated) throws TaskException {

        this.getDelegate().resolve(myRows, preallocated);

        return preallocated;
    }

    /**
     * A variation of {@linkplain #solve(MatrixStore, MatrixStore)} where you do not supply the equation
     * system <code>body</code>. It is assumed to have been set up beforehand.
     */
    public final Optional<MatrixStore<Double>> resolve() {
        try {
            return Optional.of(this.resolve(this.preallocate()));
        } catch (final TaskException xcptn) {
            return Optional.empty();
        }
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final DecompositionStore<Double> preallocated) throws TaskException {
        return this.getDelegate().solve(body, rhs, preallocated);
    }

    protected double doubleValue(final int row, final int column) {
        final Equation tmpRow = myRows.get(row);
        return tmpRow.getElements().doubleValue(column);
    }

    protected final D getDelegate() {
        return myDelegate;
    }

    protected DecompositionStore<Double> preallocate() {
        return PrimitiveDenseStore.FACTORY.makeZero(mySize, 1L);
    }

    @Override
    protected void setIterationsLimit(final int iterationsLimit) {
        super.setIterationsLimit(iterationsLimit);
        this.getDelegate().setIterationsLimit(iterationsLimit);
    }

    @Override
    protected void setTerminationContext(final NumberContext terminationContext) {
        super.setTerminationContext(terminationContext);
        this.getDelegate().setTerminationContext(terminationContext);
    }

}
