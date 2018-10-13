/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.matrix;

import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

/**
 * QuaternionMatrix
 *
 * @author apete
 */
public final class QuaternionMatrix extends BasicMatrix<Quaternion, QuaternionMatrix> {

    public static final MatrixFactory<Quaternion, QuaternionMatrix> FACTORY = new MatrixFactory<>(QuaternionMatrix.class, GenericDenseStore.QUATERNION);

    /**
     * This method is for internal use only - YOU should NOT use it!
     */
    QuaternionMatrix(final MatrixStore<Quaternion> aStore) {
        super(aStore);
    }

    @SuppressWarnings("unchecked")
    @Override
    ElementsSupplier<Quaternion> cast(final Access1D<?> matrix) {

        if (matrix instanceof QuaternionMatrix) {

            return ((QuaternionMatrix) matrix).getStore();

        } else if ((matrix instanceof ElementsSupplier) && (matrix.count() > 0L) && (matrix.get(0) instanceof Quaternion)) {

            return (ElementsSupplier<Quaternion>) matrix;

        } else if (matrix instanceof Access2D) {

            final Access2D<?> tmpAccess2D = (Access2D<?>) matrix;
            return this.getStore().physical().builder().makeWrapper(tmpAccess2D);

        } else {

            return this.getStore().physical().columns(matrix);
        }
    }

    @Override
    Eigenvalue<Quaternion> getDecompositionEigenvalue(Structure2D typical) {
        return Eigenvalue.QUATERNION.make(typical, this.isHermitian());
    }

    @Override
    LU<Quaternion> getDecompositionLU(Structure2D typical) {
        return LU.QUATERNION.make(typical);
    }

    @Override
    QR<Quaternion> getDecompositionQR(Structure2D typical) {
        return QR.QUATERNION.make(typical);
    }

    @Override
    SingularValue<Quaternion> getDecompositionSingularValue(Structure2D typical) {
        return SingularValue.QUATERNION.make(typical);
    }

    @Override
    MatrixFactory<Quaternion, QuaternionMatrix> getFactory() {
        return FACTORY;
    }

    @Override
    DeterminantTask<Quaternion> getTaskDeterminant(final MatrixStore<Quaternion> template) {
        return DeterminantTask.QUATERNION.make(template, this.isHermitian(), false);
    }

    @Override
    InverterTask<Quaternion> getTaskInverter(final MatrixStore<Quaternion> template) {
        return InverterTask.QUATERNION.make(template, this.isHermitian(), false);
    }

    @Override
    SolverTask<Quaternion> getTaskSolver(final MatrixStore<Quaternion> templateBody, final Access2D<?> templateRHS) {
        return SolverTask.QUATERNION.make(templateBody, templateRHS, this.isHermitian(), false);
    }

}
