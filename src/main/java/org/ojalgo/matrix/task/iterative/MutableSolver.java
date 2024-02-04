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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.context.NumberContext;

/**
 * Maintains a list of {@link Equation}:s and delegates to a {@link SparseDelegate}.
 *
 * @author apete
 */
public abstract class MutableSolver<D extends IterativeSolverTask & IterativeSolverTask.SparseDelegate> extends IterativeSolverTask {

    private final D myDelegate;
    private final List<Equation> myRows = new ArrayList<>();
    private final long mySize;

    @SuppressWarnings("unused")
    private MutableSolver() {
        super();
        myDelegate = null;
        mySize = 0L;
    }

    protected MutableSolver(final D delegate, final long size) {

        super();

        myDelegate = delegate;
        mySize = size;
    }

    public boolean add(final Equation row) {
        if (row.count() != mySize) {
            BasicLogger.error("row.count(): {} != mySize: {}", row.count(), mySize);
            throw new IllegalArgumentException();
        }
        boolean retVal = myRows.add(row);
        Collections.sort(myRows);
        return retVal;
    }

    public void clear() {
        myRows.clear();
    }

    public double[] getRHS() {
        return myRows.stream().mapToDouble(Equation::getRHS).toArray();
    }

    public boolean remove(final Equation row) {
        return myRows.remove(row);
    }

    /**
     * A variation of {@linkplain #solve(Access2D, Access2D, PhysicalStore)} where you do not supply the
     * equation system <code>body</code>. It is assumed to have been set up beforehand.
     */
    public final double resolve(final PhysicalStore<Double> solution) {
        return myDelegate.resolve(myRows, solution);
    }

    public final double resolve(final PhysicalStore<Double> solution, final Access1D<?> rhs) {
        return myDelegate.resolve(myRows, solution, rhs);
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> current) throws RecoverableCondition {
        return myDelegate.solve(body, rhs, current);
    }

    protected double doubleValue(final int row, final int col) {
        return myRows.get(row).doubleValue(col);
    }

    protected final D getDelegate() {
        return myDelegate;
    }

    @Override
    protected void setAccuracyContext(final NumberContext accuracyContext) {
        super.setAccuracyContext(accuracyContext);
        myDelegate.setAccuracyContext(accuracyContext);
    }

    @Override
    protected void setDebugPrinter(final BasicLogger debugPrinter) {
        super.setDebugPrinter(debugPrinter);
        myDelegate.setDebugPrinter(debugPrinter);
    }

    @Override
    protected void setIterationsLimit(final int iterationsLimit) {
        super.setIterationsLimit(iterationsLimit);
        myDelegate.setIterationsLimit(iterationsLimit);
    }

}
