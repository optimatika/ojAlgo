/*
 * Copyright 1997-2019 Optimatika
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class TridiagonalizeCase extends MatrixDecompositionTests {

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    /**
     * http://math.fullerton.edu/mathews/n2003/HouseholderMod.html
     * http://math.fullerton.edu/mathews/n2003/householder/HouseholderMod/Links/HouseholderMod_lnk_2.html
     * http://math.fullerton.edu/mathews/n2003/householder/HouseholderMod/Links/HouseholderMod_lnk_3.html
     */
    @Test
    public void testFullertonExample1and2() {

        final PhysicalStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4.0, 2.0, 2.0, 1.0 }, { 2.0, -3.0, 1.0, 1.0 }, { 2.0, 1.0, 3.0, 1.0 }, { 1.0, 1.0, 1.0, 2.0 } });
        final PhysicalStore<Double> tmpMtrxD = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4.0, -3.0, 0.0, 0.0 }, { -3.0, 2.0, 3.16227766, 0.0 }, { 0.0, 3.16227766, -1.4, -0.2 }, { 0.0, 0.0, -0.2, 1.4 } });

        this.doTheTest(tmpMtrxA, tmpMtrxD);
    }

    /**
     * http://math.fullerton.edu/mathews/n2003/HouseholderMod.html
     * http://math.fullerton.edu/mathews/n2003/householder/HouseholderMod/Links/HouseholderMod_lnk_4.html
     */
    @Test
    public void testFullertonExercise3() {

        final PhysicalStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 5.0, 1.0, 2.0, 2.0, 4.0 }, { 1.0, 1.0, 2.0, 1.0, 0.0 },
                { 2.0, 2.0, 0.0, 2.0, 1.0 }, { 2.0, 1.0, 2.0, 1.0, 2.0 }, { 4.0, 0.0, 1.0, 2.0, 4.0 } });
        final PhysicalStore<Double> tmpMtrxD = PrimitiveDenseStore.FACTORY.rows(
                new double[][] { { 5.0, -5.0, 0.0, 0.0, 0.0 }, { -5.0, 5.8, -0.8246211251, 0.0, 0.0 }, { 0.0, -0.8246211251, -0.8823529412, -1.577874704, 0.0 },
                        { 0.0, 0.0, -1.577874704, 1.373213515, 1.279015421 }, { 0.0, 0.0, 0.0, 1.279015421, -0.2908605737 } });

        this.doTheTest(tmpMtrxA, tmpMtrxD);
    }

    /**
     * http://math.fullerton.edu/mathews/n2003/HouseholderMod.html
     * http://math.fullerton.edu/mathews/n2003/householder/HouseholderMod/Links/HouseholderMod_lnk_5.html
     * http://math.fullerton.edu/mathews/n2003/householder/HouseholderMod/Links/HouseholderMod_lnk_6.html
     */
    @Test
    public void testFullertonExercise4and5() {

        final PhysicalStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4.0, 1.0, 2.0, -5.0, 1.0, 4.0 }, { 1.0, 2.0, 0.0, 4.0, 5.0, 3.0 }, { 2.0, 0.0, 3.0, -1.0, 2.0, 1.0 },
                        { -5.0, 4.0, -1.0, 1.0, 5.0, 2.0 }, { 1.0, 5.0, 2.0, 5.0, -2.0, 4.0 }, { 4.0, 3.0, 1.0, 2.0, 4.0, 1.0 } });
        final PhysicalStore<Double> tmpMtrxD = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4.0, -6.8556546, 0.0, 0.0, 0.0, 0.0 }, { -6.8556546, -0.1489361702, 2.924429193, 0.0, 0.0, 0.0 },
                        { 0.0, 2.924429193, 1.268510593, 4.758239905, 0.0, 0.0 }, { 0.0, 0.0, 4.758239905, 2.664908905, -7.994421195, 0.0 },
                        { 0.0, 0.0, 0.0, -7.994421195, 3.358186868, 1.759360415 }, { 0.0, 0.0, 0.0, 0.0, 1.759360415, -2.142670196 } });

        this.doTheTest(tmpMtrxA, tmpMtrxD);
    }

    @Test
    @Tag("unstable")
    public void testTypesWithRandom() {

        PrimitiveMatrix tmpSymmetricRandoml = PrimitiveMatrix.FACTORY.makeFilled(9, 9, new Normal());
        tmpSymmetricRandoml = tmpSymmetricRandoml.add(tmpSymmetricRandoml.transpose());

        final MatrixStore<Double> primitiveA = PrimitiveDenseStore.FACTORY.copy(tmpSymmetricRandoml);
        final MatrixStore<ComplexNumber> complexA = GenericDenseStore.COMPLEX.copy(tmpSymmetricRandoml);
        final MatrixStore<RationalNumber> rationalA = GenericDenseStore.RATIONAL.copy(tmpSymmetricRandoml);

        final Tridiagonal<Double> primitiveDecomp = Tridiagonal.PRIMITIVE.make();
        final Tridiagonal<ComplexNumber> complexDecomp = Tridiagonal.COMPLEX.make();
        final Tridiagonal<RationalNumber> rationalDecomp = Tridiagonal.RATIONAL.make();

        primitiveDecomp.decompose(primitiveA);
        complexDecomp.decompose(complexA);
        rationalDecomp.decompose(rationalA);

        if (MatrixDecompositionTests.DEBUG) {

            BasicLogger.debug("Primitive Q", primitiveDecomp.getQ());
            BasicLogger.debug("Complex Q", complexDecomp.getQ());
            BasicLogger.debug("Rational Q", rationalDecomp.getQ());

            BasicLogger.debug("Primitive D", primitiveDecomp.getD());
            BasicLogger.debug("Complex D", complexDecomp.getD());
            BasicLogger.debug("Rational D", rationalDecomp.getD());
        }

        final NumberContext precision = new NumberContext(7, 14);

        TestUtils.assertEquals(primitiveA, primitiveDecomp, precision);
        TestUtils.assertEquals(complexA, complexDecomp, precision);
        TestUtils.assertEquals(rationalA, rationalDecomp, precision);

        TestUtils.assertEquals(primitiveDecomp.getD().sliceDiagonal(), rationalDecomp.getD().sliceDiagonal(), precision);
        TestUtils.assertEquals(primitiveDecomp.getD().sliceDiagonal(), complexDecomp.getD().sliceDiagonal(), precision);

        //        TestUtils.assertEquals(primitiveDecomp.getD(), PrimitiveDenseStore.FACTORY.copy(rationalDecomp.getD()), precision);
        //        TestUtils.assertEquals(primitiveDecomp.getD(), PrimitiveDenseStore.FACTORY.copy(complexDecomp.getD()), precision);
        //
        //        TestUtils.assertEquals(primitiveDecomp.getQ(), PrimitiveDenseStore.FACTORY.copy(rationalDecomp.getQ()), precision);
        //        TestUtils.assertEquals(primitiveDecomp.getQ(), PrimitiveDenseStore.FACTORY.copy(complexDecomp.getQ()), precision);
    }

    /**
     * http://en.wikipedia.org/wiki/Householder_transformation#Tridiagonalization
     */
    @Test
    public void testWikipediaExample() {

        final PhysicalStore<Double> tmpMtrxA = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 4.0, 1.0, -2.0, 2.0 }, { 1.0, 2.0, 0.0, 1.0 }, { -2.0, 0.0, 3.0, -2.0 }, { 2.0, 1.0, -2.0, -1.0 } });
        final PhysicalStore<Double> tmpMtrxD = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4.0, -3.0, 0.0, 0.0 }, { -3.0, 10.0 / 3.0, -5.0 / 3.0, 0.0 },
                { 0.0, -5.0 / 3.0, -33.0 / 25.0, 68.0 / 75.0 }, { 0.0, 0.0, 68.0 / 75.0, 149.0 / 75.0 } });

        this.doTheTest(tmpMtrxA, tmpMtrxD);
    }

    private void doTheTest(final MatrixStore<Double> aMtrxA, final MatrixStore<Double> aMtrxD) {

        final Tridiagonal<Double> tmpDecomp = Tridiagonal.PRIMITIVE.make();
        // final Tridiagonal<Double> tmpDecomp = new TridiagonalAltDecomp();

        tmpDecomp.decompose(aMtrxA);

        TestUtils.assertEquals(aMtrxD, tmpDecomp.getD(), new NumberContext(7, 6));

        TestUtils.assertEquals(aMtrxA, tmpDecomp, new NumberContext(7, 6));
    }
}
