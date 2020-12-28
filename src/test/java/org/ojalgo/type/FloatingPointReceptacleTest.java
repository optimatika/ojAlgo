package org.ojalgo.type;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class FloatingPointReceptacleTest {

    @Test
    public void testConcatenations() {

        FloatingPointReceptacle receptacle = FloatingPointReceptacle.of(0, 1, 2, 3);
        receptacle.append(new float[] { 4, 5 });
        receptacle.append(FloatingPointReceptacle.of(6, 7, 8));
        receptacle.append(new double[] { 9 });
        receptacle.append(1, 0F);

        TestUtils.assertEquals(11, receptacle.size());

        double[] dbl = receptacle.toDoubles();
        float[] flt = receptacle.toFloats();

        TestUtils.assertEquals(11, dbl.length);
        TestUtils.assertEquals(11, flt.length);

        for (int i = 0; i < 11; i++) {
            TestUtils.assertEquals(i % 10, dbl[i], 1E-16);
            TestUtils.assertEquals(i % 10, flt[i], 1E-7);
        }

        double[] dbl2 = new double[111];
        receptacle.supplyTo(dbl2);
        for (int i = 0; i < 11; i++) {
            TestUtils.assertEquals(i % 10, dbl2[i], 1E-16);
        }

        double[] dbl3 = new double[1];
        receptacle.supplyTo(dbl3); // Just check no exception

    }

}
