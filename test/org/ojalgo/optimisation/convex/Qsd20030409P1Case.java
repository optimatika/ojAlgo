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

public class Qsd20030409P1Case extends GenericQPSolverTest {

    @Override
    protected BasicMatrix[] getMatrices() {

        final BasicMatrix[] retVal = new RationalMatrix[8];

        // Equations/Equalities
        retVal[0] = RationalMatrix.FACTORY.rows(new double[][] { { 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 0, 0, 0, 0, 0 }, { 0, 0, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 0, 1, 0, 0 }, { 0, 0, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 1 } });

        // Levels/Values
        retVal[1] = RationalMatrix.FACTORY.rows(new double[][] { { 1 }, { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.8709 }, { 0.0000 }, { 0.1291 } });

        // Quadratic
        retVal[2] = RationalMatrix.FACTORY.rows(new double[][] {
                { 1.001190250000000000000000, 0.001421400000000000000000, 0.002546100000000000000000, 0.004443600000000000000000, 0.000000000000000000000000,
                        0.001380000000000000000000, 0.002400337500000000000000 },
                { 0.001421400000000000000000, 1.001697440000000000000000, 0.003040560000000000000000, 0.005306560000000000000000, 0.000000000000000000000000,
                        0.001648000000000000000000, 0.002866490000000000000000 },
                { 0.002546100000000000000000, 0.003040560000000000000000, 1.005446440000000000000000, 0.009505440000000000000000, 0.000000000000000000000000,
                        0.002952000000000000000000, 0.005134635000000000000000 },
                { 0.004443600000000000000000, 0.005306560000000000000000, 0.009505440000000000000000, 1.016589440000000000000000, 0.000000000000000000000000,
                        0.005152000000000000000000, 0.008961260000000000000000 },
                { 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 0.000000000000000000000000, 1.000000000000000000000000,
                        0.000000000000000000000000, 0.000000000000000000000000 },
                { 0.001380000000000000000000, 0.001648000000000000000000, 0.002952000000000000000000, 0.005152000000000000000000, 0.000000000000000000000000,
                        1.001600000000000000000000, 0.002783000000000000000000 },
                { 0.002400337500000000000000, 0.002866490000000000000000, 0.005134635000000000000000, 0.008961260000000000000000, 0.000000000000000000000000,
                        0.002783000000000000000000, 1.004840680625000000000000 } });

        // Linear
        retVal[3] = RationalMatrix.FACTORY.rows(new double[][] { { -0.00059512500000000000000000000000000000000000 },
                { -0.00071070000000000000000000000000000000000000 }, { -0.00127305000000000000000000000000000000000000 },
                { -0.00222180000000000000000000000000000000000000 }, { -1.00000000000000000000000000000000000000000000 },
                { -0.00069000000000000000000000000000000000000000 }, { -0.00120016875000000000000000000000000000000000 } });

        // Inequalities/Differences
        retVal[4] = RationalMatrix.FACTORY.rows(new double[][] { { -1, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0 } });

        // Limits
        retVal[5] = RationalMatrix.FACTORY.rows(new double[][] { { 0.0000 }, { 1.0000 } });

        // LagrangeSolver
        retVal[6] = RationalMatrix.FACTORY.rows(new double[][] { { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.8709 }, { 0.0000 }, { 0.1291 } });

        // ActiveSetSolver
        retVal[7] = RationalMatrix.FACTORY.rows(new double[][] { { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.0000 }, { 0.8709 }, { 0.0000 }, { 0.1291 } });

        return retVal;
    }

}
