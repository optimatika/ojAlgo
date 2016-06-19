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
package org.ojalgo.matrix.decomposition;

import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class VerySmallCase extends MatrixDecompositionTests {

    static final NumberContext PRECISION = new NumberContext().newPrecision(12);

    static MatrixStore<Double> getVerySmall() {

        final long tmpDim = 5L;

        final PrimitiveDenseStore tmpRndm = PrimitiveDenseStore.FACTORY.makeZero(tmpDim, tmpDim);

        for (long j = 0L; j < tmpDim; j++) {
            for (long i = 0L; i < tmpDim; i++) {
                tmpRndm.set(i, j, Uniform.randomInteger(4));
            }
        }

        return tmpRndm.transpose().multiply(tmpRndm).multiply(1E-150);
    }

    public VerySmallCase() {
        super();
    }

    public VerySmallCase(final String arg0) {
        super(arg0);
    }

    public void testEvD() {

        final MatrixStore<Double> tmpProblematic = VerySmallCase.getVerySmall();

        final Eigenvalue<BigDecimal> tmpBig = Eigenvalue.BIG.make(true);
        final Eigenvalue<ComplexNumber> tmpComplex = Eigenvalue.COMPLEX.make(true);
        final Eigenvalue<Double> tmpPrimitive = Eigenvalue.PRIMITIVE.make();
        final Eigenvalue<Double> tmpJama = new RawEigenvalue.Dynamic();

        TestUtils.assertTrue("Big.compute()", tmpBig.decompose(MatrixStore.BIG.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(MatrixStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big: {}", tmpBig.getEigenvalues());
            BasicLogger.debug("Complex: {}", tmpComplex.getEigenvalues());
            BasicLogger.debug("Primitive: {}", tmpPrimitive.getEigenvalues());
            BasicLogger.debug("Jama: {}", tmpJama.getEigenvalues());
        }

        // TestUtils.assertEquals("QR.Q Big vs Complex", tmpBig.getQ(), tmpComplex.getQ());
        // TestUtils.assertEquals("QR.Q Complex vs Primitive", tmpComplex.getQ(), tmpPrimitive.getQ());
        // TestUtils.assertEquals("QR.Q Primitive vs Jama", tmpPrimitive.getQ(), tmpJama.getQ());

        TestUtils.assertEquals("EvD Big vs Complex", tmpBig.getEigenvalues().get(0), tmpComplex.getEigenvalues().get(0), PRECISION);
        TestUtils.assertEquals("EvD Complex vs Primitive", tmpComplex.getEigenvalues().get(0), tmpPrimitive.getEigenvalues().get(0), PRECISION);
        TestUtils.assertEquals("EvD Primitive vs Jama", tmpPrimitive.getEigenvalues().get(0), tmpJama.getEigenvalues().get(0), PRECISION);

        TestUtils.assertEquals("Big.reconstruct()", tmpProblematic, tmpBig.reconstruct(), PRECISION);
        TestUtils.assertEquals("Complex.reconstruct()", tmpProblematic, tmpComplex.reconstruct(), PRECISION);
        TestUtils.assertEquals("Primitive.reconstruct()", tmpProblematic, tmpPrimitive.reconstruct(), PRECISION);
        TestUtils.assertEquals("Jama.reconstruct()", tmpProblematic, tmpJama.reconstruct(), PRECISION);

        TestUtils.assertEquals("trace() Big vs Complex", tmpBig.getTrace(), tmpComplex.getTrace(), PRECISION);
        TestUtils.assertEquals("trace() Complex vs Primitive", tmpComplex.getTrace(), tmpPrimitive.getTrace(), PRECISION);
        TestUtils.assertEquals("trace() Primitive vs Jama", tmpPrimitive.getTrace(), tmpJama.getTrace(), PRECISION);

        TestUtils.assertEquals("det() Big vs Complex", tmpBig.getDeterminant(), tmpComplex.getDeterminant(), PRECISION);
        TestUtils.assertEquals("det() Complex vs Primitive", tmpComplex.getDeterminant(), tmpPrimitive.getDeterminant(), PRECISION);
        TestUtils.assertEquals("det() Primitive vs Jama", tmpPrimitive.getDeterminant(), tmpJama.getDeterminant(), PRECISION);

    }

    public void testLU() {

        final MatrixStore<Double> tmpProblematic = VerySmallCase.getVerySmall();

        final LU<BigDecimal> tmpBig = LU.BIG.make();
        final LU<ComplexNumber> tmpComplex = LU.COMPLEX.make();
        final LU<Double> tmpPrimitive = LU.PRIMITIVE.make();
        final LU<Double> tmpJama = new RawLU();

        TestUtils.assertTrue("Big.compute()", tmpBig.decompose(MatrixStore.BIG.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(MatrixStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big.L", tmpBig.getL());
            BasicLogger.debug("Complex.L", tmpComplex.getL());
            BasicLogger.debug("Primitive.L", tmpPrimitive.getL());
            BasicLogger.debug("Jama.L", tmpJama.getL());
        }

        TestUtils.assertEquals("L Big vs Complex", tmpBig.getL(), tmpComplex.getL(), PRECISION);
        TestUtils.assertEquals("L Complex vs Primitive", tmpComplex.getL(), tmpPrimitive.getL(), PRECISION);
        TestUtils.assertEquals("L Primitive vs Jama", tmpPrimitive.getL(), tmpJama.getL(), PRECISION);

        TestUtils.assertEquals("U Big vs Complex", tmpBig.getU(), tmpComplex.getU(), PRECISION);
        TestUtils.assertEquals("U Complex vs Primitive", tmpComplex.getU(), tmpPrimitive.getU(), PRECISION);
        TestUtils.assertEquals("U Primitive vs Jama", tmpPrimitive.getU(), tmpJama.getU(), PRECISION);

        TestUtils.assertEquals("Big.reconstruct()", tmpProblematic, tmpBig.reconstruct(), PRECISION);
        TestUtils.assertEquals("Complex.reconstruct()", tmpProblematic, tmpComplex.reconstruct(), PRECISION);
        TestUtils.assertEquals("Primitive.reconstruct()", tmpProblematic, tmpPrimitive.reconstruct(), PRECISION);
        TestUtils.assertEquals("Jama.reconstruct()", tmpProblematic, tmpJama.reconstruct(), PRECISION);

        final SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(tmpProblematic);

        TestUtils.assertEquals("rank() SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("rank() SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("rank() SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("rank() SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

    public void testQR() {

        final MatrixStore<Double> tmpProblematic = VerySmallCase.getVerySmall();

        final QR<BigDecimal> tmpBig = QR.BIG.make();
        final QR<ComplexNumber> tmpComplex = QR.COMPLEX.make();
        final QR<Double> tmpPrimitive = QR.PRIMITIVE.make();
        final QR<Double> tmpJama = new RawQR();

        TestUtils.assertTrue("Big.compute()", tmpBig.decompose(MatrixStore.BIG.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(MatrixStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big Q", tmpBig.getQ());
            BasicLogger.debug("Complex Q", tmpComplex.getQ());
            BasicLogger.debug("Primitive Q", tmpPrimitive.getQ());
            BasicLogger.debug("Jama Q", tmpJama.getQ());
        }

        TestUtils.assertEquals("QR.reconstruct() Big", tmpProblematic, tmpBig.reconstruct(), PRECISION);
        TestUtils.assertEquals("QR.reconstruct() Complex", tmpProblematic, tmpComplex.reconstruct(), PRECISION);
        TestUtils.assertEquals("QR.reconstruct() Primitive", tmpProblematic, tmpPrimitive.reconstruct(), PRECISION);
        TestUtils.assertEquals("QR.reconstruct() Jama", tmpProblematic, tmpJama.reconstruct(), PRECISION);

        final SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(tmpProblematic);

        TestUtils.assertEquals("rank() SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("rank() SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("rank() SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("rank() SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

}
