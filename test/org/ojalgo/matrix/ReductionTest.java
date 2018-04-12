package org.ojalgo.matrix;

import static org.ojalgo.function.aggregator.Aggregator.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.BasicMatrix.Builder;

public class ReductionTest {

    public ReductionTest() {
        super();
    }

    /**
     * https://stackoverflow.com/questions/49682219/how-to-get-average-of-elements-along-columns-or-rows-in-an-ojalgo-matrix
     * <br>
     * https://github.com/optimatika/ojAlgo/issues/91#issuecomment-379399975
     */
    @Test
    public void testReduction() {

        Builder<PrimitiveMatrix> builder = PrimitiveMatrix.FACTORY.getBuilder(2, 3);
        builder.set(0, 0, 1.0);
        builder.set(0, 1, 2.0);
        builder.set(0, 2, 3.0);
        builder.set(1, 0, 4.0);
        builder.set(1, 1, 5.0);
        builder.set(1, 2, 6.0);
        PrimitiveMatrix matrix = builder.get();

        PrimitiveMatrix reducedRows = matrix.reduceRows(AVERAGE);
        PrimitiveMatrix reducedColumns = matrix.reduceColumns(AVERAGE);

        TestUtils.assertEquals(2, reducedRows.count());
        TestUtils.assertEquals(2, reducedRows.countRows());
        TestUtils.assertEquals(1, reducedRows.countColumns());

        TestUtils.assertEquals(2.0, reducedRows.doubleValue(0, 0));
        TestUtils.assertEquals(5.0, reducedRows.doubleValue(1, 0));

        TestUtils.assertEquals(2.0, reducedRows.doubleValue(0));
        TestUtils.assertEquals(5.0, reducedRows.doubleValue(1));

        TestUtils.assertEquals(3, reducedColumns.count());
        TestUtils.assertEquals(1, reducedColumns.countRows());
        TestUtils.assertEquals(3, reducedColumns.countColumns());

        TestUtils.assertEquals(2.5, reducedColumns.doubleValue(0, 0));
        TestUtils.assertEquals(3.5, reducedColumns.doubleValue(0, 1));
        TestUtils.assertEquals(4.5, reducedColumns.doubleValue(0, 2));

        TestUtils.assertEquals(2.5, reducedColumns.doubleValue(0));
        TestUtils.assertEquals(3.5, reducedColumns.doubleValue(1));
        TestUtils.assertEquals(4.5, reducedColumns.doubleValue(2));
    }

}
