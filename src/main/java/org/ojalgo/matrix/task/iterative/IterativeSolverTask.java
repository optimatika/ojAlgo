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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RowsSupplier;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.type.context.NumberContext;

/**
 * Base class for iterative solvers of large linear systems [A][x]=[b]. Subclasses provide concrete stationary
 * or Krylov algorithms while sharing input conversion, configuration, and stopping logic. Characteristics
 * <ul>
 * <li>Accepts both sparse and dense inputs by converting the system to a {@code List<Equation>} where rows
 * can be iterated efficiently without assuming dense, contiguous storage.
 * <li>Minimises allocations in hot loops by reusing buffers and operating directly on {@link PhysicalStore}
 * and {@link R064Store} vectors.
 * <li>Provides a common configuration and termination policy via {@link NumberContext} and an iteration
 * limit.
 * </ul>
 * Selected/reordered rows and columns
 * <ul>
 * <li>Implementations must work when the effective system is a selected or reordered subset of the original
 * problem. The equation list may represent only some rows, and column indices may need to be remapped or
 * compacted.
 * <li>Do not assume a dense, contiguous column space. Always form row products using
 * {@link Equation#dot(org.ojalgo.structure.Access1D)} and access the diagonal through
 * {@link Equation#getPivot()}.
 * <li>The provided {@code solution} vector defines the active variable subspace; form residuals against that
 * vector and the current row bodies only.
 * <li>RHS values are carried by each {@link Equation}; use
 * {@link #resolve(List, PhysicalStore, org.ojalgo.structure.Access1D)} to update RHS between solves.
 * </ul>
 * Preconditioners and solver compatibility
 * <ul>
 * <li>Left-preconditioned Krylov methods may apply the configured {@link Preconditioner} on residuals or
 * intermediate vectors. The preconditioner should be symmetric positive-definite when the algorithm assumes
 * this (e.g., for SPD problems).
 * <li>Right-preconditioned Krylov methods may call both
 * {@link Preconditioner#apply(org.ojalgo.structure.Access1D, PhysicalStore)} and
 * {@link Preconditioner#applyTranspose(org.ojalgo.structure.Access1D, PhysicalStore)}. If the preconditioner
 * is not symmetric, override the transpose variant.
 * <li>Stationary (fixed-point) methods typically ignore preconditioners; use the relaxation factor to control
 * convergence.
 * </ul>
 * Stopping criterion
 * <ul>
 * <li>Subclasses should measure a residual norm and stop when {@link NumberContext#isSmall(double, double)}
 * deems it small relative to the RHS norm (or absolutely small when the RHS is zero), or when the iteration
 * limit is reached.
 * </ul>
 */
public abstract class IterativeSolverTask implements SolverTask<Double> {

    public static final class Configurator {

        private final IterativeSolverTask mySolver;

        Configurator(final IterativeSolverTask solver) {
            super();
            mySolver = solver;
        }

        /**
         * Accuracy/termination context
         */
        public Configurator accuracy(final NumberContext accuray) {
            if (accuray != null) {
                mySolver.setAccuracyContext(accuray);
            } else {
                mySolver.setAccuracyContext(DEFAULT);
            }
            return this;
        }

        /**
         * To get debug print per iteration
         */
        public Configurator debug(final BasicLogger printer) {
            mySolver.setDebugPrinter(printer);
            return this;
        }

        /**
         * Max number of iterations
         */
        public Configurator iterations(final int iterations) {
            mySolver.setIterationsLimit(iterations);
            return this;
        }

        /**
         * Preconditioner for methods that support it. Defaults to {@link Preconditioner#IDENTITY}.
         */
        public Configurator preconditioner(final Preconditioner preconditioner) {
            mySolver.setPreconditioner(preconditioner != null ? preconditioner : Preconditioner.IDENTITY);
            return this;
        }

        /**
         * Relaxation factor (only used by some solvers)
         */
        public Configurator relaxation(final double factor) {
            mySolver.setRelaxationFactor(factor);
            return this;
        }

    }

    static final NumberContext DEFAULT = NumberContext.ofMath(MathContext.DECIMAL64);

    static List<Equation> toListOfRows(final Access2D<?> body, final Access2D<?> rhs) {

        int nbEquations = body.getRowDim();
        int nbVariables = body.getColDim();

        List<Equation> retVal = new ArrayList<>(nbEquations);

        if (body instanceof SparseStore) {

            for (int i = 0; i < nbEquations; i++) {
                Equation row = Equation.sparse(i, nbVariables);
                retVal.add(row);
                row.setRHS(rhs.doubleValue(i));
            }

            for (ElementView2D<?, ?> element : ((SparseStore<?>) body).nonzeros()) {
                int i = Math.toIntExact(element.row());
                long j = element.column();
                retVal.get(i).set(j, element.doubleValue());
            }

        } else if (body instanceof RowsSupplier) {

            for (int i = 0; i < nbEquations; i++) {
                retVal.add(Equation.wrap(((RowsSupplier<?>) body).getRow(i), i, rhs.doubleValue(i)));
            }

        } else if (body instanceof ColumnsSupplier) {

            for (int i = 0; i < nbEquations; i++) {
                Equation row = Equation.sparse(i, nbVariables);
                retVal.add(row);
                row.setRHS(rhs.doubleValue(i));
            }

            for (int j = 0; j < nbVariables; j++) {
                for (NonzeroView<?> element : ((ColumnsSupplier<?>) body).getColumn(j).nonzeros()) {
                    int i = Math.toIntExact(element.index());
                    retVal.get(i).set(j, element.doubleValue());
                }
            }

        } else {

            for (int i = 0; i < nbEquations; i++) {

                Equation row = Equation.dense(i, nbVariables);
                retVal.add(row);
                row.setRHS(rhs.doubleValue(i));

                for (int j = 0; j < nbVariables; j++) {
                    row.set(j, body.doubleValue(i, j));
                }
            }
        }

        return retVal;
    }

    static R064Store worker(final R064Store vector, final int size) {
        if (vector == null || vector.size() != size) {
            return R064Store.FACTORY.make(size, 1);
        } else {
            vector.fillAll(ZERO);
            return vector;
        }
    }

    private NumberContext myAccuracyContext = DEFAULT;
    private transient Configurator myConfigurator = null;
    private BasicLogger myDebugPrinter = null;
    private int myIterationsLimit = Integer.MAX_VALUE;
    private double myRelaxationFactor = ONE;
    private Preconditioner myPreconditioner = Preconditioner.IDENTITY;

    IterativeSolverTask() {
        super();
    }

    public final Configurator configurator() {
        if (myConfigurator == null) {
            myConfigurator = new Configurator(this);
        }
        return myConfigurator;
    }

    @Override
    public final PhysicalStore<Double> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        if (nbSolutions != 1) {
            throw new IllegalArgumentException("The RHS must have precisely 1 column!");
        } else {
            return R064Store.FACTORY.make(nbVariables, nbSolutions);
        }
    }

    public abstract double resolve(List<Equation> equations, PhysicalStore<Double> solution);

    public final double resolve(final List<Equation> equations, final PhysicalStore<Double> solution, final Access1D<?> rhs) {

        int nbEquations = equations.size();

        if (rhs.size() != nbEquations) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < nbEquations; i++) {
            equations.get(i).setRHS(rhs.doubleValue(i));
        }

        return this.resolve(equations, solution);
    }

    @Override
    public final MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated)
            throws RecoverableCondition {

        List<Equation> equations = IterativeSolverTask.toListOfRows(body, rhs);

        this.resolve(equations, preallocated);

        return preallocated;
    }

    public final Optional<MatrixStore<Double>> solve(final MatrixStore<Double> body, final MatrixStore<Double> rhs) {
        try {
            return Optional.of(this.solve(body, rhs, this.preallocate(body, rhs)));
        } catch (RecoverableCondition cause) {
            return Optional.empty();
        }
    }

    protected final void debug(final int iteration, final double error, final Access1D<?> current) {
        if (myDebugPrinter != null) {
            myDebugPrinter.println("{}: {} – {}", iteration, error, Array1D.R064.copy(current));
        }
    }

    protected final NumberContext getAccuracyContext() {
        return myAccuracyContext;
    }

    protected final int getIterationsLimit() {
        return myIterationsLimit;
    }

    protected final Preconditioner getPreconditioner() {
        return myPreconditioner;
    }

    protected final double getRelaxationFactor() {
        return myRelaxationFactor;
    }

    protected final boolean isDebugPrinterSet() {
        return myDebugPrinter != null;
    }

    protected final void setAccuracyContext(final NumberContext accuracyContext) {
        myAccuracyContext = accuracyContext;
    }

    protected final void setDebugPrinter(final BasicLogger debugPrinter) {
        myDebugPrinter = debugPrinter;
    }

    protected final void setIterationsLimit(final int iterationsLimit) {
        myIterationsLimit = iterationsLimit;
    }

    protected final void setPreconditioner(final Preconditioner preconditioner) {
        myPreconditioner = preconditioner != null ? preconditioner : Preconditioner.IDENTITY;
    }

    protected final void setRelaxationFactor(final double relaxation) {
        myRelaxationFactor = relaxation;
    }

}
