package org.ojalgo.data.proximity;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.structure.Access1D;

public class SimilarityTest extends DataProximityTests {

    @Test
    public void testCosineAccess1D() {
        Access1D<?> a = ArrayR064.wrap(new double[] { 1.0, 0.0, 1.0 });
        Access1D<?> b = ArrayR064.wrap(new double[] { 0.0, 1.0, 0.0 });
        Access1D<?> c = ArrayR064.wrap(new double[] { 2.0, 0.0, 2.0 });

        TestUtils.assertEquals(0.0, Similarity.cosine(a, b));
        TestUtils.assertEquals(1.0, Similarity.cosine(a, c));
    }

    @Test
    public void testCosineDoubleArrays() {
        double[] a = new double[] { 1.0, 0.0 };
        double[] b = new double[] { 0.0, 1.0 };
        double[] c = new double[] { 2.0, 0.0 };
        double[] d = new double[] { -1.0, 0.0 };

        TestUtils.assertEquals(0.0, Similarity.cosine(a, b));
        TestUtils.assertEquals(1.0, Similarity.cosine(a, c));
        TestUtils.assertEquals(-1.0, Similarity.cosine(a, d));
    }

    @Test
    public void testCosineExceptionsAccess1D() {
        Access1D<?> a = ArrayR064.wrap(new double[] { 1.0, 0.0 });
        Access1D<?> z = ArrayR064.wrap(new double[] { 0.0, 0.0 });
        Access1D<?> diff = ArrayR064.wrap(new double[] { 1.0 });

        TestUtils.assertThrows(NullPointerException.class, () -> Similarity.cosine((Access1D<?>) null, a));
        TestUtils.assertThrows(NullPointerException.class, () -> Similarity.cosine(a, (Access1D<?>) null));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, diff));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, z));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(z, a));
    }

    @Test
    public void testCosineExceptionsDoubleArrays() {
        double[] a = new double[] { 1.0, 0.0 };
        double[] z = new double[] { 0.0, 0.0 };
        double[] diff = new double[] { 1.0 };

        TestUtils.assertThrows(NullPointerException.class, () -> Similarity.cosine((double[]) null, a));
        TestUtils.assertThrows(NullPointerException.class, () -> Similarity.cosine(a, (double[]) null));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, diff));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(a, z));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> Similarity.cosine(z, a));
    }
}