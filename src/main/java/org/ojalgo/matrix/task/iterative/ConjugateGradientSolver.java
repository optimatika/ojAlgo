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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.context.NumberContext;

/**
 * For solving [A][x]=[b] when [A] is symmetric and positive-definite.
 * <p>
 * This implementation is (Jacobi) preconditioned – using the diagonal elements to scale the residual.
 *
 * @author apete
 * @see https://en.wikipedia.org/wiki/Conjugate_gradient_method
 * @see https://optimization.cbe.cornell.edu/index.php?title=Conjugate_gradient_methods
 */
public final class ConjugateGradientSolver extends KrylovSubspaceSolver implements IterativeSolverTask.SparseDelegate {

    private transient Primitive64Store myDirection = null;
    private transient Primitive64Store myPreconditioned = null;
    private transient Primitive64Store myResidual = null;
    private transient Primitive64Store myVector = null;

    public ConjugateGradientSolver() {
        super();
    }

    public double resolve(final List<Equation> equations, final PhysicalStore<Double> solution) {

        int nbEquations = equations.size();

        int iterations = 0;
        int limit = this.getIterationsLimit();
        NumberContext accuracy = this.getAccuracyContext();

        double normErr = POSITIVE_INFINITY;
        double normRHS = ONE;

        Primitive64Store residual = this.residual(solution);
        Primitive64Store direction = this.direction(solution);
        Primitive64Store preconditioned = this.preconditioned(solution);
        Primitive64Store vector = this.vector(solution);

        double stepLength; // alpha
        double gradientCorrectionFactor; // beta

        double zr0 = 1;
        double zr1;
        double pAp0 = 0;

        for (int r = 0; r < nbEquations; r++) {
            Equation row = equations.get(r);
            double tmpVal = row.getRHS();
            normRHS = HYPOT.invoke(normRHS, tmpVal);
            tmpVal -= row.dot(solution);
            residual.set(row.index, tmpVal);
            double pivot = row.getPivot();
            preconditioned.set(row.index, tmpVal / pivot);
        }

        direction.fillMatching(preconditioned);

        zr1 = preconditioned.dot(residual);

        do {

            zr0 = zr1;

            for (int i = 0; i < nbEquations; i++) {
                Equation row = equations.get(i);
                vector.set(row.index, row.dot(direction));
            }

            pAp0 = direction.dot(vector);

            stepLength = zr0 / pAp0;

            if (!Double.isNaN(stepLength)) {

                direction.axpy(stepLength, solution);

                vector.axpy(-stepLength, residual);
            }

            normErr = ZERO;

            for (int r = 0; r < nbEquations; r++) {
                Equation row = equations.get(r);
                double tmpVal = residual.doubleValue(row.index);
                normErr = HYPOT.invoke(normErr, tmpVal);
                double pivot = row.getPivot();
                preconditioned.set(row.index, tmpVal / pivot);
            }

            zr1 = preconditioned.dot(residual);
            gradientCorrectionFactor = zr1 / zr0;

            direction.modifyAll(MULTIPLY.second(gradientCorrectionFactor));
            direction.modifyMatching(ADD, preconditioned);

            iterations++;

            if (this.isDebugPrinterSet()) {
                this.debug(iterations, normErr / normRHS, solution);
            }

        } while ((iterations < limit) && !Double.isNaN(normErr) && !accuracy.isSmall(normRHS, normErr));

        // BasicLogger.debug("Done in {} iterations on problem size {}", iterations, solution.count());

        return normErr / normRHS;
    }

    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        List<Equation> equations = IterativeSolverTask.toListOfRows(body, rhs);

        this.resolve(equations, preallocated);

        return preallocated;
    }

    private Primitive64Store direction(final Structure1D structure) {
        if ((myDirection == null) || (myDirection.count() != structure.count())) {
            myDirection = Primitive64Store.FACTORY.make(structure.count(), 1L);
        } else {
            myDirection.fillAll(ZERO);
        }
        return myDirection;
    }

    private Primitive64Store preconditioned(final Structure1D structure) {
        if ((myPreconditioned == null) || (myPreconditioned.count() != structure.count())) {
            myPreconditioned = Primitive64Store.FACTORY.make(structure.count(), 1L);
        } else {
            myPreconditioned.fillAll(ZERO);
        }
        return myPreconditioned;
    }

    private Primitive64Store residual(final Structure1D structure) {
        if ((myResidual == null) || (myResidual.count() != structure.count())) {
            myResidual = Primitive64Store.FACTORY.make(structure.count(), 1L);
        } else {
            myResidual.fillAll(ZERO);
        }
        return myResidual;
    }

    private Primitive64Store vector(final Structure1D structure) {
        if ((myVector == null) || (myVector.count() != structure.count())) {
            myVector = Primitive64Store.FACTORY.make(structure.count(), 1L);
        } else {
            myVector.fillAll(ZERO);
        }
        return myVector;
    }

}
