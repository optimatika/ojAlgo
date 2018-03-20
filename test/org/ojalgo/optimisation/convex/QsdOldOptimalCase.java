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
 * @author apete
 */
public class QsdOldOptimalCase extends GenericQPSolverTest {

    @Override
    protected BasicMatrix[] getMatrices() {

        final BasicMatrix[] retVal = new RationalMatrix[8];

        // Equations/Equalities
        retVal[0] = RationalMatrix.FACTORY
                .rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0 }, { 0.0345, 0.0412, 0.0738, 0.1288, 0.069575 }, { 0.0, 0.0, 0.0, 0.0, 1.0 } });

        // Levels/Values
        retVal[1] = RationalMatrix.FACTORY.rows(new double[][] { { 1.0 }, { 0.043807039117990006 }, { 0.0 } });

        // Quadratic
        retVal[2] = RationalMatrix.FACTORY.rows(new double[][] { { 2.005994, -0.077922, -0.041957999999999995, -0.17982, 0.0 },
                { -0.077922, 2.95904, 0.50949, 2.17782, 0.0 }, { -0.041957999999999995, 0.50949, 35.454511999999994, 29.804166, 0.0 },
                { -0.17982, 2.17782, 29.804166, 139.150712, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 16.747238000000003 } });

        // Linear
        retVal[3] = RationalMatrix.FACTORY.rows(new double[][] { { -0.5 }, { -0.5 }, { -0.5 }, { -0.5 }, { -0.0 } });

        // Inequalities/Differences
        retVal[4] = RationalMatrix.FACTORY
                .rows(new double[][] { { -1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, -1.0, 0.0 },
                        { 1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 1.0, 0.0 } });

        // Limits
        retVal[5] = RationalMatrix.FACTORY.rows(new double[][] { { -0.0 }, { -0.0 }, { -0.0 }, { -0.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 } });

        // LagrangeSolver
        retVal[6] = RationalMatrix.FACTORY.rows(new double[][] { { 0.4506664080256741780 }, { 0.4388067927187100669 }, { 0.0737389738732712572 },
                { 0.0367878253823444979 }, { 0.0000000000000000000 } });

        // ActiveSetSolver
        retVal[7] = RationalMatrix.FACTORY.rows(
                new double[][] { { 0.4506664080256748 }, { 0.4388067927187099 }, { 0.0737389738732711 }, { 0.036787825382344326 }, { 2.651716120891472E-17 } });

        return retVal;
    }
}
