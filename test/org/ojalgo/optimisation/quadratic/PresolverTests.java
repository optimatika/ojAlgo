/* 
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.quadratic;

import java.util.Arrays;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.LUDecomposition;
import org.ojalgo.matrix.store.MatrixStore;

/**
 * @author apete
 */
public class PresolverTests extends OptimisationQuadraticTests {

    public PresolverTests() {
        super();
    }

    public PresolverTests(final String someName) {
        super(someName);
    }

    public void testPreSolver() {

        final BasicMatrix tmpBody = SimpleEquationCase.getBody();
        final BasicMatrix tmpRHS = SimpleEquationCase.getRHS();
        final BasicMatrix tmpSolution = SimpleEquationCase.getSolution();

        final MatrixStore<Double> tmpA = tmpBody.mergeColumns(tmpBody).toPrimitiveStore();
        final MatrixStore<Double> tmpX = tmpSolution.toPrimitiveStore();
        final MatrixStore<Double> tmpB = tmpRHS.mergeColumns(tmpRHS).toPrimitiveStore();

        final MatrixStore<Double> tmpCombo = tmpBody.mergeColumns(tmpBody).mergeRows(tmpRHS.mergeColumns(tmpRHS)).toPrimitiveStore();

        final LU<Double> tmpLU = LUDecomposition.makePrimitive();

        tmpLU.compute(tmpCombo);

        if (OptimisationQuadraticTests.DEBUG) {
            System.out.println("PivotOrder: " + Arrays.toString(tmpLU.getPivotOrder()));
            System.out.println("L: " + tmpLU.getL());
            System.out.println("U: " + tmpLU.getU());
            System.out.println("SOL: " + tmpSolution);
        }
    }
}
