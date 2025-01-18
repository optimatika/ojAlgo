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
package org.ojalgo.matrix.decomposition;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.matrix.SimpleEquationCase;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class CaseLU extends MatrixDecompositionTests {

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testP20061119Case() {

        MatrixR064 tmpProblematic = P20061119Case.getProblematic();

        LU<RationalNumber> tmpBig = LU.Q128.make();
        tmpBig.decompose(GenericStore.Q128.copy(tmpProblematic));

        LU<ComplexNumber> tmpComplex = LU.C128.make();
        tmpComplex.decompose(GenericStore.C128.copy(tmpProblematic));

        LU<Double> tmpPrimitive = LU.R064.make();
        tmpPrimitive.decompose(R064Store.FACTORY.copy(tmpProblematic));

        LU<Double> tmpJama = new RawLU();
        tmpJama.decompose(R064Store.FACTORY.copy(tmpProblematic));

        NumberContext tmpPrintContext = NumberContext.ofScale(20);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Big L", tmpBig.getL(), tmpPrintContext);
            BasicLogger.debugMatrix("Complex L", tmpComplex.getL(), tmpPrintContext);
            BasicLogger.debugMatrix("Primitive L", tmpPrimitive.getL(), tmpPrintContext);
            BasicLogger.debugMatrix("Jama L", tmpJama.getL(), tmpPrintContext);
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Big U", tmpBig.getU(), tmpPrintContext);
            BasicLogger.debugMatrix("Complex U", tmpComplex.getU(), tmpPrintContext);
            BasicLogger.debugMatrix("Primitive U", tmpPrimitive.getU(), tmpPrintContext);
            BasicLogger.debugMatrix("Jama U", tmpJama.getU(), tmpPrintContext);
        }

        SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(R064Store.FACTORY.copy(tmpProblematic));

        TestUtils.assertEquals("LU.rank SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

    @Test
    public void testReconstructWhenPivoted() {

        MatrixStore<Double> matrix = CaseLDL.newSpecialSchnabelEskow();

        LU<Double> decomp = LU.R064.make(matrix);
        decomp.decompose(matrix);

        if (DEBUG) {
            BasicLogger.debugMatrix("Original", matrix);
            BasicLogger.debugMatrix("L", decomp.getL());
            BasicLogger.debugMatrix("U", decomp.getU());
            BasicLogger.debugMatrix("Reconstructed", decomp.reconstruct());
        }

        TestUtils.assertEquals(matrix, decomp.reconstruct());
    }

    @Test
    public void testSolveBothWays() {

        MatrixR064 body = SimpleEquationCase.getBody();
        MatrixR064 rhs = SimpleEquationCase.getRHS();
        MatrixR064 solution = SimpleEquationCase.getSolution();

        R064Store expected = R064Store.FACTORY.make(solution.getRowDim(), solution.getColDim());
        R064Store actual = R064Store.FACTORY.make(solution.getRowDim(), solution.getColDim());

        for (LU<Double> decomp : MatrixDecompositionTests.getPrimitiveLU()) {

            decomp.decompose(body);

            if (DEBUG) {
                BasicLogger.debug("P: {}", Arrays.toString(decomp.getPivotOrder()));
                BasicLogger.debugMatrix("L", decomp.getL());
                BasicLogger.debugMatrix("U", decomp.getU());
            }

            decomp.ftran(rhs, actual);

            TestUtils.assertEquals(solution, actual);

            decomp.decompose(body.transpose());

            if (DEBUG) {
                BasicLogger.debug("P: {}", Arrays.toString(decomp.getPivotOrder()));
                BasicLogger.debugMatrix("L", decomp.getL());
                BasicLogger.debugMatrix("U", decomp.getU());
            }

            decomp.ftran(rhs, expected);

            decomp.decompose(body);

            if (DEBUG) {
                BasicLogger.debug("P: {}", Arrays.toString(decomp.getPivotOrder()));
                BasicLogger.debugMatrix("L", decomp.getL());
                BasicLogger.debugMatrix("U", decomp.getU());
            }

            decomp.btran(rhs, actual);

            TestUtils.assertEquals(expected, actual);
        }
    }

}
