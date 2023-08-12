package org.ojalgo.array;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.RationalMath;
import org.ojalgo.scalar.RationalNumber;

public class CompatibleTest extends ArrayTests {

    @Test
    public void testFill2D() {

        Array2D<RationalNumber> column = Array2D.Q128.make(3, 1);
        Array2D<RationalNumber> row = Array2D.Q128.make(1, 5);

        Array2D<RationalNumber> matrix = Array2D.Q128.make(row, column);

        TestUtils.assertEquals(3, matrix.getRowDim());
        TestUtils.assertEquals(5, matrix.getColDim());

        for (int i = 0; i < 3; i++) {
            column.set(i, 0, i);
        }
        for (int j = 0; j < 5; j++) {
            row.set(0, j, j);
        }

        matrix.fillCompatible(row, RationalMath.ADD, column);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i + j, element.intValue());
            }
        }

        matrix.fillCompatible(column, RationalMath.MULTIPLY, row);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i * j, element.intValue());
            }
        }
    }

    @Test
    public void testModify2D() {

        Array2D<RationalNumber> matrix = Array2D.Q128.make(3, 5);

        Array2D<RationalNumber> column = Array2D.Q128.make(3, 1);
        Array2D<RationalNumber> row = Array2D.Q128.make(1, 5);

        for (int i = 0; i < 3; i++) {
            column.set(i, 0, i);
        }
        for (int j = 0; j < 5; j++) {
            row.set(0, j, j);
        }

        matrix.fillAll(RationalMath.ONE);
        matrix.modifyCompatible(RationalMath.ADD, row);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(1 + j, element.intValue());
            }
        }

        matrix.fillAll(RationalMath.TWO);
        matrix.modifyCompatible(row, RationalMath.ADD);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(2 + j, element.intValue());
            }
        }

        matrix.fillAll(RationalMath.THREE);
        matrix.modifyCompatible(RationalMath.ADD, column);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i + 3, element.intValue());
            }
        }

        matrix.fillAll(RationalMath.FOUR);
        matrix.modifyCompatible(column, RationalMath.ADD);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i + 4, element.intValue());
            }
        }
    }

    @Test
    public void testFillAnyD() {

        ArrayAnyD<RationalNumber> vec0 = ArrayAnyD.Q128.make(3, 1);
        ArrayAnyD<RationalNumber> vec2 = ArrayAnyD.Q128.make(1, 1, 5);

        ArrayAnyD<RationalNumber> compatible = ArrayAnyD.Q128.make(vec0, vec2);

        TestUtils.assertEquals(3, compatible.size(0));
        TestUtils.assertEquals(1, compatible.size(1));
        TestUtils.assertEquals(5, compatible.size(2));

        for (int i = 0; i < 3; i++) {
            vec0.set(i, i);
        }
        for (int i = 0; i < 5; i++) {
            vec2.set(i, i);
        }

        compatible.fillCompatible(vec2, RationalMath.ADD, vec0);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = compatible.get(i, 0, j);
                TestUtils.assertEquals(i + j, element.intValue());
            }
        }

        compatible.fillCompatible(vec0, RationalMath.MULTIPLY, vec2);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = compatible.get(i, 0, j);
                TestUtils.assertEquals(i * j, element.intValue());
            }
        }
    }

    @Test
    public void testMake1D() {

        ArrayAnyD<RationalNumber> vec0 = ArrayAnyD.Q128.make(3, 1);
        ArrayAnyD<RationalNumber> vec2 = ArrayAnyD.Q128.make(1, 1, 5);

        Array1D<RationalNumber> compatible = Array1D.Q128.make(vec0, vec2);

        TestUtils.assertEquals(15, compatible.size());
    }

    @Test
    public void testFailMake1D() {

        Array1D<RationalNumber> vec0 = Array1D.Q128.make(3);
        Array1D<RationalNumber> vec2 = Array1D.Q128.make(5);

        try {
            Array1D<RationalNumber> compatible = Array1D.Q128.make(vec0, vec2);
            TestUtils.fail("Should not get here!");
        } catch (IllegalArgumentException cause) {
            TestUtils.success();
        }
    }

}
