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
import org.ojalgo.access.Structure2D;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
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
    private final long mySize;
    private Access2D<?> myRHS;
    private final List<Row> myRows = new ArrayList<>();

    public void setup(final Access2D<?> rhs) {
        myRHS = rhs;
    }

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

    public boolean add(final Row row) {
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

    public boolean remove(final Row row) {
        return myRows.remove(row);
    }

    protected final D getDelegate() {
        return myDelegate;
    }

    protected abstract DecompositionStore<Double> preallocate(final Structure2D templateRHS);

    /**
     * A variation of {@linkplain #solve(Access2D, Access2D, DecompositionStore)} where you do not supply the
     * equation system <code>body</code>. It is assumed to have been set up beforehand.
     */
    public final MatrixStore<Double> resolve(final Access2D<?> rhs, final DecompositionStore<Double> preallocated) throws TaskException {

        this.setup(rhs);

        this.getDelegate().resolve(myRows, rhs, preallocated);

        return preallocated;
    }

    /**
     * A variation of {@linkplain #solve(MatrixStore, MatrixStore)} where you do not supply the equation
     * system <code>body</code>. It is assumed to have been set up beforehand.
     */
    public final Optional<MatrixStore<Double>> resolve(final MatrixStore<Double> rhs) {
        try {
            return Optional.of(this.resolve(rhs, this.preallocate(rhs)));
        } catch (final TaskException xcptn) {
            return Optional.empty();
        }
    }

    protected double doubleValue(final int row, final int column) {
        final Row tmpRow = myRows.get(row);
        return tmpRow.getElements().doubleValue(column);
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
