package org.ojalgo.matrix.store;

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

public class RepeatCase extends NonPhysicalTest {

    static public Primitive64Matrix repmat(Primitive64Matrix X, int m, long n) {

        Primitive64Matrix.LogicalBuilder builder = X.logical();

        for (int i = 1; i < m; i++) {
            builder.below(X);
        }

        Primitive64Matrix firstCol = builder.get();

        for (int j = 1; j < n; j++) {
            builder.right(firstCol);
        }

        return builder.get();
    }

    @Override
    @BeforeEach
    public void setUp() {

        int baseRowDim = Uniform.randomInteger(1, 9);
        int baseColDim = Uniform.randomInteger(1, 9);

        int rowsRep = Uniform.randomInteger(1, 9);
        int colsRep = Uniform.randomInteger(1, 9);

        MatrixStore<ComplexNumber> base = NonPhysicalTest.makeRandomMatrix(baseRowDim, baseColDim);

        rationalStore = GenericStore.RATIONAL.copy(base).logical().repeat(rowsRep, colsRep).get();
        complexStore = GenericStore.COMPLEX.copy(base).logical().repeat(rowsRep, colsRep).get();
        primitiveStore = Primitive64Store.FACTORY.copy(base).logical().repeat(rowsRep, colsRep).get();

        numberOfRows = baseRowDim * rowsRep;
        numberOfColumns = baseColDim * colsRep;
    }

}
