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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

/**
 * @author apete
 */
public class VerySmallCase extends MatrixDecompositionTests {

    public VerySmallCase() {
        super();
    }

    public VerySmallCase(final String arg0) {
        super(arg0);
    }

    static MatrixStore<Double> getVerySmall() {

        final long tmpDim = 5L;

        final PrimitiveDenseStore tmpRndm = PrimitiveDenseStore.FACTORY.makeZero(tmpDim, tmpDim);

        for (long j = 0L; j < tmpDim; j++) {
            for (long i = 0L; i < tmpDim; i++) {
                tmpRndm.set(i, j, Uniform.randomInteger(4));
            }
        }

        return tmpRndm.multiplyLeft(tmpRndm.transpose()).scale(1E-150);
    }

    public void testLU() {

        final MatrixStore<Double> tmpProblematic = VerySmallCase.getVerySmall();

        final LU<BigDecimal> tmpBig = LUDecomposition.makeBig();
        final LU<ComplexNumber> tmpComplex = LUDecomposition.makeComplex();
        final LU<Double> tmpPrimitive = LUDecomposition.makePrimitive();
        final LU<Double> tmpJama = LUDecomposition.makeJama();

        TestUtils.assertTrue("Big compute()", tmpBig.compute(tmpProblematic));
        TestUtils.assertTrue("Complex compute()", tmpComplex.compute(tmpProblematic));
        TestUtils.assertTrue("Primitive compute()", tmpPrimitive.compute(tmpProblematic));
        TestUtils.assertTrue("Jama compute()", tmpJama.compute(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big L", tmpBig.getL());
            BasicLogger.debug("Complex L", tmpComplex.getL());
            BasicLogger.debug("Primitive L", tmpPrimitive.getL());
            BasicLogger.debug("Jama L", tmpJama.getL());
        }

        TestUtils.assertEquals("LU.L Big vs Complex", tmpBig.getL(), tmpComplex.getL());
        TestUtils.assertEquals("LU.L Complex vs Primitive", tmpComplex.getL(), tmpPrimitive.getL());
        TestUtils.assertEquals("LU.L Primitive vs Jama", tmpPrimitive.getL(), tmpJama.getL());

        TestUtils.assertEquals("LU.U Big vs Complex", tmpBig.getU(), tmpComplex.getU());
        TestUtils.assertEquals("LU.U Complex vs Primitive", tmpComplex.getU(), tmpPrimitive.getU());
        TestUtils.assertEquals("LU.U Primitive vs Jama", tmpPrimitive.getU(), tmpJama.getU());

        TestUtils.assertEquals("LU.reconstruct() Big", tmpProblematic, tmpBig.reconstruct());
        TestUtils.assertEquals("LU.reconstruct() Complex", tmpProblematic, tmpComplex.reconstruct());
        TestUtils.assertEquals("LU.reconstruct() Primitive", tmpProblematic, tmpPrimitive.reconstruct());
        TestUtils.assertEquals("LU.reconstruct() Jama", tmpProblematic, tmpJama.reconstruct());

        final SingularValue<Double> tmpSVD = SingularValueDecomposition.makeJama();
        tmpSVD.compute(tmpProblematic);

        TestUtils.assertEquals("LU.rank SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

    public void testQR() {

        final MatrixStore<Double> tmpProblematic = VerySmallCase.getVerySmall();

        final QR<BigDecimal> tmpBig = QRDecomposition.makeBig();
        final QR<ComplexNumber> tmpComplex = QRDecomposition.makeComplex();
        final QR<Double> tmpPrimitive = QRDecomposition.makePrimitive();
        final QR<Double> tmpJama = QRDecomposition.makeJama();

        TestUtils.assertTrue("Big compute()", tmpBig.compute(tmpProblematic));
        TestUtils.assertTrue("Complex compute()", tmpComplex.compute(tmpProblematic));
        TestUtils.assertTrue("Primitive compute()", tmpPrimitive.compute(tmpProblematic));
        TestUtils.assertTrue("Jama compute()", tmpJama.compute(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big Q", tmpBig.getQ());
            BasicLogger.debug("Complex Q", tmpComplex.getQ());
            BasicLogger.debug("Primitive Q", tmpPrimitive.getQ());
            BasicLogger.debug("Jama Q", tmpJama.getQ());
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big R", tmpBig.getR());
            BasicLogger.debug("Complex R", tmpComplex.getR());
            BasicLogger.debug("Primitive R", tmpPrimitive.getR());
            BasicLogger.debug("Jama R", tmpJama.getR());
        }

        // TestUtils.assertEquals("QR.Q Big vs Complex", tmpBig.getQ(), tmpComplex.getQ());
        // TestUtils.assertEquals("QR.Q Complex vs Primitive", tmpComplex.getQ(), tmpPrimitive.getQ());
        // TestUtils.assertEquals("QR.Q Primitive vs Jama", tmpPrimitive.getQ(), tmpJama.getQ());

        TestUtils.assertEquals("QR.R Big vs Complex", tmpBig.getR(), tmpComplex.getR());
        TestUtils.assertEquals("QR.R Complex vs Primitive", tmpComplex.getR(), tmpPrimitive.getR());
        TestUtils.assertEquals("QR.R Primitive vs Jama", tmpPrimitive.getR(), tmpJama.getR());

        TestUtils.assertEquals("QR.reconstruct() Big", tmpProblematic, tmpBig.reconstruct());
        TestUtils.assertEquals("QR.reconstruct() Complex", tmpProblematic, tmpComplex.reconstruct());
        TestUtils.assertEquals("QR.reconstruct() Primitive", tmpProblematic, tmpPrimitive.reconstruct());
        TestUtils.assertEquals("QR.reconstruct() Jama", tmpProblematic, tmpJama.reconstruct());

        final SingularValue<Double> tmpSVD = SingularValueDecomposition.makeJama();
        tmpSVD.compute(tmpProblematic);

        TestUtils.assertEquals("QR.rank SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("QR.rank SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("QR.rank SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("QR.rank SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

}
