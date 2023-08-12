package org.ojalgo.matrix;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.RationalMath;
import org.ojalgo.matrix.MatrixQ128.DenseReceiver;
import org.ojalgo.scalar.RationalNumber;

public class CompatibleTest extends MatrixTests {

    @Test
    public void testFill() {

        DenseReceiver column = MatrixQ128.FACTORY.newBuilder(3, 1);
        DenseReceiver row = MatrixQ128.FACTORY.newBuilder(1, 5);

        DenseReceiver matrix = MatrixQ128.FACTORY.make(row, column).copy();

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
    public void testModify() {

        DenseReceiver matrix = MatrixQ128.FACTORY.newBuilder(3, 5);

        DenseReceiver column = MatrixQ128.FACTORY.newBuilder(3, 1);
        DenseReceiver row = MatrixQ128.FACTORY.newBuilder(1, 5);

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
    public void testOperate() {

        DenseReceiver builder = null;
        MatrixQ128 matrix = null;

        DenseReceiver column = MatrixQ128.FACTORY.newBuilder(3, 1);
        DenseReceiver row = MatrixQ128.FACTORY.newBuilder(1, 5);

        for (int i = 0; i < 3; i++) {
            column.set(i, 0, i);
        }
        for (int j = 0; j < 5; j++) {
            row.set(0, j, j);
        }

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.ONE);
        matrix = builder.build().onCompatible(RationalMath.ADD, row);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(1 + j, element.intValue());
            }
        }

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.TWO);
        matrix = builder.build().onCompatible(row, RationalMath.ADD);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(2 + j, element.intValue());
            }
        }

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.THREE);
        matrix = builder.build().onCompatible(RationalMath.ADD, column);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i + 3, element.intValue());
            }
        }

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.FOUR);
        matrix = builder.build().onCompatible(column, RationalMath.ADD);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i + 4, element.intValue());
            }
        }
    }

    @Test
    public void testAdd() {

        DenseReceiver builder = null;
        MatrixQ128 matrix = null;

        DenseReceiver columnReceiver = MatrixQ128.FACTORY.newBuilder(3, 1);
        DenseReceiver rowReceiver = MatrixQ128.FACTORY.newBuilder(1, 5);

        for (int i = 0; i < 3; i++) {
            columnReceiver.set(i, 0, i);
        }
        for (int j = 0; j < 5; j++) {
            rowReceiver.set(0, j, j);
        }

        MatrixQ128 column = columnReceiver.build();
        MatrixQ128 row = rowReceiver.build();

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.ONE);
        matrix = builder.build().add(row);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(1 + j, element.intValue());
            }
        }

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.TWO);
        matrix = builder.build().add(row);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(2 + j, element.intValue());
            }
        }

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.THREE);
        matrix = builder.build().add(column);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i + 3, element.intValue());
            }
        }

        builder = MatrixQ128.FACTORY.newBuilder(3, 5);
        builder.fillAll(RationalMath.FOUR);
        matrix = builder.build().add(column);

        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                RationalNumber element = matrix.get(i, j);
                TestUtils.assertEquals(i + 4, element.intValue());
            }
        }
    }

}
