/*
 * Copyright 1997-2025 Optimatika
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
import java.util.stream.Stream;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

/**
 * Lightweight mutable wrapper around a list of {@link Equation} rows that delegates solving to an
 * {@link IterativeSolverTask}.
 * <p>
 * Purpose
 * <ul>
 * <li>Build and update an equation system incrementally by adding/removing {@link Equation} rows.
 * <li>Reuse an iterative solver across solves without rebuilding matrices.
 * <li>Support workflows where the active rows/columns change between iterations.
 * </ul>
 * How it works
 * <ul>
 * <li>Subclass to define the problem size and to provide the delegate iterative solver.
 * <li>Call {@link #add(Equation)} and {@link #remove(Equation)} to maintain the active set of rows. Rows are
 * kept sorted by their {@link Equation#index}.
 * <li>Call {@link #resolve(PhysicalStore)} (or {@link #resolve(PhysicalStore, Access1D)}) to solve [A][x]=[b]
 * using the current rows and right-hand sides stored in each {@link Equation}.
 * <li>All methods that solve or preallocate simply forward to the delegate.
 * </ul>
 * Contract
 * <ul>
 * <li>Every added {@link Equation} must have {@link Equation#size()} equal to the problem size passed to the
 * constructor; otherwise an {@link IllegalArgumentException} is thrown.
 * <li>The provided {@code solution} vector represents the active variable subspace; residuals are formed
 * against that vector and the current row bodies only.
 * <li>Thread-safety: instances are not thread-safe; do not mutate the row set while solving.
 * </ul>
 * Example usage
 * <ul>
 * <li>A subclass can also implement {@link MatrixStore} and dynamically assemble a Schur-complement-style
 * system by adding/removing rows, and then call {@link #resolve(PhysicalStore)} to compute the unknowns.
 * </ul>
 */
public abstract class MutableSolver implements SolverTask<Double> {

    private final IterativeSolverTask myDelegate;
    private final List<Equation> myRows = new ArrayList<>();
    private final int mySize;

    protected MutableSolver(final IterativeSolverTask delegate, final int size) {

        super();

        myDelegate = delegate;
        mySize = size;
    }

    public boolean add(final Equation row) {
        if (row.size() != mySize) {
            BasicLogger.error("row.count(): {} != mySize: {}", row.size(), mySize);
            throw new IllegalArgumentException();
        }
        boolean retVal = myRows.add(row);
        Collections.sort(myRows);
        return retVal;
    }

    public void clear() {
        myRows.clear();
    }

    @Override
    public PhysicalStore<Double> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return myDelegate.preallocate(nbEquations, nbVariables, nbSolutions);
    }

    public boolean remove(final Equation row) {
        return myRows.remove(row);
    }

    /**
     * A variation of {@linkplain #solve(Access2D, Access2D, PhysicalStore)} where the system body has already
     * been set up using {@link #add(Equation)}. Solves the current [A][x]=[b] using the delegate iterative
     * solver.
     */
    public double resolve(final PhysicalStore<Double> solution) {
        return myDelegate.resolve(myRows, solution);
    }

    /**
     * Same as {@link #resolve(PhysicalStore)} but replaces the RHS values before solving.
     */
    public double resolve(final PhysicalStore<Double> solution, final Access1D<?> rhs) {
        return myDelegate.resolve(myRows, solution, rhs);
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> current) throws RecoverableCondition {
        return myDelegate.solve(body, rhs, current);
    }

    protected double doubleValue(final int row, final int col) {
        return myRows.get(row).doubleValue(col);
    }

    protected Stream<Equation> equations() {
        return myRows.stream();
    }

    protected final IterativeSolverTask getDelegate() {
        return myDelegate;
    }

}