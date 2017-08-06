/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.linear;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.BaseSolver;
import org.ojalgo.optimisation.BaseSolver.AbstractBuilder;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;

public abstract class LinearSolver extends GenericSolver {

    public static final class Builder extends AbstractBuilder<LinearSolver.Builder, SimplexSolver> {

        public Builder(final MatrixStore<Double> C) {
            super(C);
        }

        Builder() {
            super();
        }

        Builder(final BaseSolver.AbstractBuilder<LinearSolver.Builder, SimplexSolver> matrices) {
            super(matrices);
        }

        Builder(final MatrixStore<Double> Q, final MatrixStore<Double> C) {
            super(Q, C);
        }

        Builder(final MatrixStore<Double>[] aMtrxArr) {
            super(aMtrxArr);
        }

        @Override
        public SimplexSolver build(final Optimisation.Options options) {

            this.validate();

            final SimplexTableau tableau = new DenseTableau(this);

            return new SimplexSolver(tableau, options);
        }

        @Override
        public LinearSolver.Builder equalities(final MatrixStore<Double> AE, final MatrixStore<Double> BE) {
            return super.equalities(AE, BE);
        }

        @Override
        public LinearSolver.Builder objective(final MatrixStore<Double> C) {
            return super.objective(C);
        }
    }

    protected LinearSolver(final Options solverOptions) {
        super(solverOptions);
    }

}
