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
package org.ojalgo.optimisation.system;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.optimisation.Optimisation;

public class AllInOneSolver extends KKTSystem {

    public AllInOneSolver() {
        super();
    }

    @Override
    public KKTSystem.Output solve(final KKTSystem.Input input, final Optimisation.Options options) {

        boolean tmpSolvable = false;
        MatrixStore<Double> tmpSolution = null;

        final MatrixStore<Double> tmpKKT = input.getKKT();
        final MatrixStore<Double> tmpRHS = input.getRHS();
        final SolverTask<Double> tmpSolver = SolverTask.PRIMITIVE.make(tmpKKT, tmpRHS);

        try {
            tmpSolution = tmpSolver.solve(tmpKKT, tmpRHS);
            tmpSolvable = true;
        } catch (final TaskException exception) {
            tmpSolvable = false;
        }

        if (tmpSolvable && (tmpSolution != null)) {
            return null;
        } else {
            return KKTSystem.FAILURE;
        }

    }

}
