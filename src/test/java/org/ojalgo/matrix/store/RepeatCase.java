package org.ojalgo.matrix.store;

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

public class RepeatCase extends NonPhysicalTest {

    @Override
    @BeforeEach
    public void setUp() {

        int baseRowDim = Uniform.randomInteger(1, 9);
        int baseColDim = Uniform.randomInteger(1, 9);

        int rowsRep = Uniform.randomInteger(1, 9);
        int colsRep = Uniform.randomInteger(1, 9);

        MatrixStore<ComplexNumber> base = NonPhysicalTest.makeRandomMatrix(baseRowDim, baseColDim);

        rationalStore = GenericStore.Q128.copy(base).repeat(rowsRep, colsRep);
        complexStore = GenericStore.C128.copy(base).repeat(rowsRep, colsRep);
        primitiveStore = R064Store.FACTORY.copy(base).repeat(rowsRep, colsRep);

        numberOfRows = baseRowDim * rowsRep;
        numberOfColumns = baseColDim * colsRep;
    }

}
