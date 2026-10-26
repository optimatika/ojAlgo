/*
 * Copyright 1997-2026 Optimatika
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
import java.util.Random;

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

    /**
     * i*[[1,2],[3,4]] is non-singular, and the diagonal of U is purely imaginary.
     */
    @Test
    public void testImaginaryMatrix() {

        GenericStore<ComplexNumber> matrix = GenericStore.C128.make(2, 2);
        matrix.set(0, 0, ComplexNumber.of(0.0, 1.0));
        matrix.set(0, 1, ComplexNumber.of(0.0, 2.0));
        matrix.set(1, 0, ComplexNumber.of(0.0, 3.0));
        matrix.set(1, 1, ComplexNumber.of(0.0, 4.0));

        LU<ComplexNumber> decomposition = LU.C128.make(matrix);
        decomposition.decompose(matrix);

        TestUtils.assertEquals(2, decomposition.getRank());
        TestUtils.assertTrue(decomposition.isFullRank());
        TestUtils.assertTrue(decomposition.isSolvable());
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
            rhs.supplyTo(actual);
            decomp.ftran(actual);

            TestUtils.assertEquals(solution, actual);

            decomp.decompose(body.transpose());

            if (DEBUG) {
                BasicLogger.debug("P: {}", Arrays.toString(decomp.getPivotOrder()));
                BasicLogger.debugMatrix("L", decomp.getL());
                BasicLogger.debugMatrix("U", decomp.getU());
            }
            rhs.supplyTo(expected);
            decomp.ftran(expected);

            decomp.decompose(body);

            if (DEBUG) {
                BasicLogger.debug("P: {}", Arrays.toString(decomp.getPivotOrder()));
                BasicLogger.debugMatrix("L", decomp.getL());
                BasicLogger.debugMatrix("U", decomp.getU());
            }
            rhs.supplyTo(actual);
            decomp.btran(actual);

            TestUtils.assertEquals(expected, actual);
        }
    }

    /**
     * A single right hand side: from one R064Store to another, in place, from some other MatrixStore, and
     * with ftran – on both sides of the substitution kernels' thresholds (40 for TRSV, 80 for the row-major
     * kernels).
     */
    @Test
    public void testSolveSingleColumn() {

        NumberContext accuracy = NumberContext.of(8);

        for (int n : new int[] { 1, 2, 7, 40, 41, 80, 81, 130 }) {

            Random random = new Random(n);

            R064Store body = R064Store.FACTORY.make(n, n);
            R064Store rhs = R064Store.FACTORY.make(n, 1);
            for (int i = 0; i < n * n; i++) {
                body.set(i, 2.0 * random.nextDouble() - 1.0);
            }
            for (int i = 0; i < n; i++) {
                rhs.set(i, 2.0 * random.nextDouble() - 1.0);
            }

            for (LU<Double> decomp : MatrixDecompositionTests.getPrimitiveLU()) {

                decomp.decompose(body);

                String message = decomp.getClass().getSimpleName() + " n=" + n;

                MatrixStore<Double> solution = decomp.getSolution(rhs, decomp.preallocate(n, n, 1));
                TestUtils.assertEquals(message, rhs, body.multiply(solution), accuracy);

                R064Store inPlace = rhs.copy();
                decomp.getSolution(inPlace, inPlace);
                TestUtils.assertEquals(message, solution, inPlace, accuracy);

                MatrixStore<Double> fromOther = decomp.getSolution(rhs.limits(n, 1), decomp.preallocate(n, n, 1));
                TestUtils.assertEquals(message, solution, fromOther, accuracy);

                double[] ftran = rhs.toRawCopy1D();
                decomp.ftran(ftran);
                TestUtils.assertEquals(message, solution, R064Store.FACTORY.column(ftran), accuracy);
            }
        }
    }

}
