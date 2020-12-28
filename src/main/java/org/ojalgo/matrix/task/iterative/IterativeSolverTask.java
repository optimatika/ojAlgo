/*
 * Copyright 1997-2020 Optimatika
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
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

public abstract class IterativeSolverTask implements SolverTask<Double> {

    public static final class Configurator {

        private final IterativeSolverTask mySolver;

        @SuppressWarnings("unused")
        private Configurator() {
            this(null);
        }

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
        public Configurator debug(final BasicLogger.Printer printer) {
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

    }

    public interface SparseDelegate {

        double resolve(List<Equation> equations, final PhysicalStore<Double> solution);

    }

    static final NumberContext DEFAULT = NumberContext.ofMath(MathContext.DECIMAL128);

    static List<Equation> toListOfRows(final Access2D<?> body, final Access2D<?> rhs) {

        int numbEquations = (int) body.countRows();
        int numbVariables = (int) body.countColumns();

        List<Equation> retVal = new ArrayList<>(numbEquations);
        for (int i = 0; i < numbEquations; i++) {
            retVal.add(new Equation(i, numbVariables, rhs.doubleValue(i)));
        }

        if (body instanceof SparseStore) {

            ((SparseStore<?>) body).nonzeros().forEach(view -> retVal.get(Math.toIntExact(view.row())).set(view.column(), view.doubleValue()));

        } else {

            for (int i = 0; i < numbEquations; i++) {
                final Equation row = retVal.get(i);
                for (int j = 0; j < numbVariables; j++) {
                    final double tmpVal = body.doubleValue(i, j);
                    if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                        row.set(j, tmpVal);
                    }
                }
            }
        }

        return retVal;
    }

    private NumberContext myAccuracyContext = DEFAULT;
    private BasicLogger.Printer myDebugPrinter = null;
    private int myIterationsLimit = Integer.MAX_VALUE;

    IterativeSolverTask() {
        super();
    }

    public final Configurator configurator() {
        return new Configurator(this);
    }

    public final PhysicalStore<Double> preallocate(final Structure2D templateBody, final Structure2D templateRHS) {
        if (templateRHS.countColumns() != 1L) {
            throw new IllegalArgumentException("The RHS must have precisely 1 column!");
        }
        return Primitive64Store.FACTORY.make(templateBody.countColumns(), 1L);
    }

    public final Optional<MatrixStore<Double>> solve(final MatrixStore<Double> body, final MatrixStore<Double> rhs) {
        try {
            return Optional.of(this.solve(body, rhs, this.preallocate(body, rhs)));
        } catch (final RecoverableCondition xcptn) {
            return Optional.empty();
        }
    }

    protected final void debug(final int iteration, final double error, final Access1D<?> current) {
        if (myDebugPrinter != null) {
            myDebugPrinter.println("{}: {} – {}", iteration, error, Array1D.PRIMITIVE64.copy(current));
        }
    }

    protected final NumberContext getAccuracyContext() {
        return myAccuracyContext;
    }

    protected final int getIterationsLimit() {
        return myIterationsLimit;
    }

    protected final boolean isDebugPrinterSet() {
        return myDebugPrinter != null;
    }

    protected void setAccuracyContext(final NumberContext accuracyContext) {
        myAccuracyContext = accuracyContext;
    }

    protected void setDebugPrinter(final BasicLogger.Printer debugPrinter) {
        myDebugPrinter = debugPrinter;
    }

    protected void setIterationsLimit(final int iterationsLimit) {
        myIterationsLimit = iterationsLimit;
    }

}
