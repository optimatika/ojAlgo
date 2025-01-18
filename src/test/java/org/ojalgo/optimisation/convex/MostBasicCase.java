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
package org.ojalgo.optimisation.convex;

import org.ojalgo.matrix.MatrixQ128;
import org.ojalgo.matrix.store.RawStore;

/**
 * @author apete
 */
public class MostBasicCase extends GenericQPSolverTest {

    @Override
    protected MatrixQ128[] getMatrices() {

        final MatrixQ128[] retVal = new MatrixQ128[8];

        // Equations/Equalities
        retVal[0] = MatrixQ128.FACTORY.copy(RawStore.wrap(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } }));

        // Levels/Values
        retVal[1] = MatrixQ128.FACTORY.copy(RawStore.wrap(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } }));

        // Quadratic
        retVal[2] = MatrixQ128.FACTORY.copy(RawStore.wrap(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } }));

        // Linear
        retVal[3] = MatrixQ128.FACTORY.copy(RawStore.wrap(new double[][] { { -2.0 }, { -3.0 }, { -4.0 } }));

        // Inequalities/Differences
        retVal[4] = MatrixQ128.FACTORY.copy(RawStore
                .wrap(new double[][] { { -1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, -1.0 }, { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } }));

        // Limits
        retVal[5] = MatrixQ128.FACTORY.copy(RawStore.wrap(new double[][] { { -1.0 }, { -2.0 }, { -3.0 }, { 3.0 }, { 4.0 }, { 5.0 } }));

        // LagrangeSolver
        retVal[6] = MatrixQ128.FACTORY.copy(RawStore.wrap(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } }));

        // ActiveSetSolver
        retVal[7] = MatrixQ128.FACTORY.copy(RawStore.wrap(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } }));

        return retVal;
    }
}
