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
import org.ojalgo.matrix.BigMatrix;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class LUTest extends MatrixDecompositionTests {

    public LUTest() {
        super();
    }

    public LUTest(final String arg0) {
        super(arg0);
    }

    public void testP20061119Case() {

        final BigMatrix tmpProblematic = P20061119Case.getProblematic();

        final LU<BigDecimal> tmpBig = LU.BIG.make();
        tmpBig.decompose(tmpProblematic.toBigStore());

        final LU<ComplexNumber> tmpComplex = LU.COMPLEX.make();
        tmpComplex.decompose(tmpProblematic.toComplexStore());

        final LU<Double> tmpPrimitive = LU.PRIMITIVE.make();
        tmpPrimitive.decompose(tmpProblematic.toPrimitiveStore());

        final LU<Double> tmpJama = new RawLU();
        tmpJama.decompose(tmpProblematic.toPrimitiveStore());

        final NumberContext tmpPrintContext = NumberContext.getGeneral(20);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big L", tmpBig.getL(), tmpPrintContext);
            BasicLogger.debug("Complex L", tmpComplex.getL(), tmpPrintContext);
            BasicLogger.debug("Primitive L", tmpPrimitive.getL(), tmpPrintContext);
            BasicLogger.debug("Jama L", tmpJama.getL(), tmpPrintContext);
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big U", tmpBig.getU(), tmpPrintContext);
            BasicLogger.debug("Complex U", tmpComplex.getU(), tmpPrintContext);
            BasicLogger.debug("Primitive U", tmpPrimitive.getU(), tmpPrintContext);
            BasicLogger.debug("Jama U", tmpJama.getU(), tmpPrintContext);
        }

        final SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(tmpProblematic.toPrimitiveStore());

        TestUtils.assertEquals("LU.rank SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

}
