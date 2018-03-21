/*
 * Copyright 1997-2018 Optimatika
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

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.RationalMatrix;

/**
 * Qsd20030327P1Case
 *
 * @author apete
 */
public class Qsd20030327P1Case extends GenericQPSolverTest {

    @Override
    protected BasicMatrix[] getMatrices() {

        final BasicMatrix[] retVal = new RationalMatrix[8];

        // Equations/Equalities
        retVal[0] = RationalMatrix.FACTORY
                .rows(new double[][] { { 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 1, 0, 0 }, { 0, 0, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 1 } });

        // Levels/Values
        retVal[1] = RationalMatrix.FACTORY.rows(new double[][] { { 1 }, { 0.0000 }, { 0.0000 }, { 0.0000 } });

        // Quadratic
        retVal[2] = RationalMatrix.FACTORY.rows(new double[][] {
                { 1.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 1.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        1.000000000000000000000000, 0.000000000000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000,
                        0.000000000000000000000000, 1.000000000000000000000000 } });

        // Linear
        retVal[3] = RationalMatrix.FACTORY.rows(new double[][] { { -0.1100000000000000000000000000 }, { -0.1100000000000000000000000000 },
                { -0.5000000000000000000000000000 }, { -0.2800000000000000000000000000 }, { 0.0000000000000000000000000000 },
                { 0.0000000000000000000000000000 }, { 0.0000000000000000000000000000 } });

        // Inequalities/Differences
        retVal[4] = RationalMatrix.FACTORY.rows(new double[][] { { -1, 0, 0, 0, 0, 0, 0 }, { 0, -1, 0, 0, 0, 0, 0 }, { 0, 0, -1, 0, 0, 0, 0 },
                { 0, 0, 0, -1, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0, 0 }, { 0, 0, 1, 0, 0, 0, 0 }, { 0, 0, 0, 1, 0, 0, 0 } });

        // Limits
        retVal[5] = RationalMatrix.FACTORY
                .rows(new double[][] { { -0.0100 }, { -0.0100 }, { -0.4000 }, { -0.1600 }, { 0.2200 }, { 0.0668 }, { 0.6000 }, { 0.4000 } });

        // LagrangeSolver
        retVal[6] = retVal[3].negate();

        // ActiveSetSolver
        retVal[7] = RationalMatrix.FACTORY.rows(new double[][] { { 0.1244 }, { 0.0668 }, { 0.5144 }, { 0.2944 }, { 0.0000 }, { 0.0000 }, { 0.0000 } });

        return retVal;
    }

}
