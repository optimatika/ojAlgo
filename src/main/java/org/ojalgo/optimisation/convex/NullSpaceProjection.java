/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.structure.Access1D;

public class NullSpaceProjection {

    static NullSpaceProjection reduce(final ConvexData<Double> fullModel) {

        int nbVars = fullModel.countVariables();
        int nbEqus = fullModel.countEqualityConstraints();

        if (nbVars <= 0) {
            throw new IllegalArgumentException("Model has no variables!");
        }

        if (nbEqus <= 0 || nbEqus >= nbVars) {
            throw new IllegalArgumentException("Model has no equality constraints or too many equality constraints!");
        }

        ElementsSupplier<Double> mAEt = fullModel.getSupplierAE().transpose();
        QR<Double> decomposition = QR.R064.make(mAEt, true);
        decomposition.decompose(mAEt);

        int rank = decomposition.getRank();

        MatrixStore<Double> Z = decomposition.getQ().offsets(0, rank);

        PhysicalStore<Double> x0 = R064Store.FACTORY.make(nbVars, 1);
        x0.fillMatching(fullModel.getBE());
        decomposition.btran(x0);

        return new NullSpaceProjection(fullModel, decomposition, Z, x0);
    }

    private final ConvexData<Double> myOriginal;
    private final QR<Double> myDecomposition;
    private final MatrixStore<Double> myZ;
    private final MatrixStore<Double> myX0;
    private transient ConvexData<Double> myReduced = null;

    NullSpaceProjection(final ConvexData<Double> original, final QR<Double> decomposition, final MatrixStore<Double> Z, final MatrixStore<Double> x0) {

        super();

        myOriginal = original;
        myDecomposition = decomposition;
        myZ = Z;
        myX0 = x0;
    }

    ConvexData<Double> getReduced() {

        if (myReduced == null) {

            int nbOrgVars = myOriginal.countVariables();
            int nbOrgEqus = myOriginal.countEqualityConstraints();
            int nbOrgInes = myOriginal.countInequalityConstraints();

            int rank = myDecomposition.getRank();

            int nbRedVars = nbOrgVars - rank;
            int nbRedEqus = 0;
            int nbRedInes = nbOrgInes;

            ConvexObjectiveFunction<Double> orgObj = myOriginal.getObjective();
            PhysicalStore<Double> orgQ = orgObj.quadratic();
            MatrixStore<Double> orgC = orgObj.linear();

            MatrixStore<Double> tmpZt = myZ.transpose();

            MatrixStore<Double> tmpQ = tmpZt.multiply(orgQ).multiply(myZ);
            MatrixStore<Double> redQ = tmpQ.add(tmpQ.transpose()).multiply(HALF);
            MatrixStore<Double> redC = tmpZt.multiply(orgC.subtract(orgQ.multiply(myX0)));

            myReduced = new ConvexData<>(false, R064Store.FACTORY, nbRedVars, nbRedEqus, nbRedInes);

            ConvexObjectiveFunction<Double> redObj = myReduced.getObjective();
            redObj.quadratic().fillMatching(redQ);
            redObj.linear().fillMatching(redC);

            for (int i = 0; i < nbOrgInes; i++) {
                SparseArray<Double> row = myOriginal.getAI(i);
                myReduced.setBI(i, myOriginal.getBI(i) - row.dot(myX0));
                for (int j = 0; j < nbRedVars; j++) {
                    double sum = row.dot(myZ.sliceColumn(j));
                    if (!PrimitiveScalar.isSmall(ONE, sum)) {
                        myReduced.setAI(i, j, sum);
                    }
                }
            }
        }

        return myReduced;
    }

    Optimisation.Result toFullModelState(final Optimisation.Result reducedlState) {

        int nbOrgVars = myOriginal.countVariables();
        int nbOrgEqus = myOriginal.countEqualityConstraints();
        int nbOrgInes = myOriginal.countInequalityConstraints();

        int nbRedVars = reducedlState.size();

        PhysicalStore<Double> orgQ = myOriginal.getObjective().quadratic();
        MatrixStore<Double> orgC = myOriginal.getObjective().linear();

        // Map back to x = x0 + Z y
        PhysicalStore<Double> ystar = R064Store.FACTORY.make(nbRedVars, 1);
        for (int i = 0; i < nbRedVars; i++) {
            ystar.set(i, reducedlState.doubleValue(i));
        }

        PhysicalStore<Double> x = R064Store.FACTORY.make(nbOrgVars, 1);
        x.fillMatching(myX0);
        myZ.multiply(ystar).axpy(ONE, x);

        // Recover multipliers using stationarity: Qx - C + AE^T λE + AI^T λI = 0
        // Build rhsStation = C - Qx - AI^T λI in a few dense/sparse ops for clarity
        PhysicalStore<Double> rhsStation = R064Store.FACTORY.make(nbOrgVars, 1);
        orgC.onMatching(orgQ.multiply(x), SUBTRACT).supplyTo(rhsStation);

        PhysicalStore<Double> lambdaI = null;
        if (nbOrgInes > 0 && reducedlState.getMultipliers().isPresent()) {
            Access1D<?> yI = reducedlState.getMultipliers().get();
            int copyLen = Math.min(nbOrgInes, yI.size());
            lambdaI = R064Store.FACTORY.make(nbOrgInes, 1);
            for (int i = 0; i < copyLen; i++) {
                lambdaI.set(i, yI.doubleValue(i));
            }
            // Subtract AI^T * lambdaI
            MatrixStore<Double> aitLambda = myOriginal.getAI().transpose().multiply(lambdaI);
            rhsStation.onMatching(aitLambda, SUBTRACT).supplyTo(rhsStation);
        }

        // Solve AE^T * lambdaE = rhsStation using the same QR(AE^T)
        MatrixStore<Double> lambdaE = myDecomposition.getSolution(rhsStation);

        // Assemble combined multipliers [lambdaE; lambdaI]
        PhysicalStore<Double> multipliers = R064Store.FACTORY.make(nbOrgEqus + nbOrgInes, 1);
        if (lambdaE != null) {
            multipliers.regionByLimits(nbOrgEqus, 1).fillMatching(lambdaE);
        }
        if (lambdaI != null) {
            multipliers.regionByOffsets(nbOrgEqus, 0).fillMatching(lambdaI);
        }

        Optimisation.Result retVal = new Optimisation.Result(reducedlState.getState(), NaN, x);
        retVal = retVal.multipliers(myOriginal.getConstraintsMetaData(), multipliers);
        return retVal;
    }

    Optimisation.Result toReducedState(final Optimisation.Result fullModelState) {

        if (fullModelState != null && fullModelState.getState().isFeasible()) {

            // Compute d = x - x0
            PhysicalStore<Double> d = R064Store.FACTORY.column(fullModelState);
            d.modifyMatching(SUBTRACT, myX0);

            // y = Z^T * d
            R064Store y = d.premultiply(myZ.transpose()).collect(R064Store.FACTORY);

            return fullModelState.withSolution(y);

        } else {

            return null;
        }
    }

}