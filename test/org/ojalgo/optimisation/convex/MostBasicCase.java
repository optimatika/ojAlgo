/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import org.ojalgo.matrix.BigMatrix;

/**
 * @author apete
 */
public class MostBasicCase extends GenericQPSolverTest {

    public MostBasicCase() {
        super();
    }

    public MostBasicCase(final String arg0) {
        super(arg0);
    }

    @Override
    protected BasicMatrix[] getMatrices() {

        final BasicMatrix[] retVal = new BigMatrix[8];

        // Equations/Equalities
        retVal[0] = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } });

        // Levels/Values
        retVal[1] = BigMatrix.FACTORY.rows(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } });

        // Quadratic
        retVal[2] = BigMatrix.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } });

        // Linear
        retVal[3] = BigMatrix.FACTORY.rows(new double[][] { { -2.0 }, { -3.0 }, { -4.0 } });

        // Inequalities/Differences
        retVal[4] = BigMatrix.FACTORY
                .rows(new double[][] { { -1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, -1.0 }, { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 } });

        // Limits
        retVal[5] = BigMatrix.FACTORY.rows(new double[][] { { -1.0 }, { -2.0 }, { -3.0 }, { 3.0 }, { 4.0 }, { 5.0 } });

        // LagrangeSolver
        retVal[6] = BigMatrix.FACTORY.rows(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } });

        // ActiveSetSolver
        retVal[7] = BigMatrix.FACTORY.rows(new double[][] { { 2.0 }, { 3.0 }, { 4.0 } });

        return retVal;
    }
}
