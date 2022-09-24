/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.CorePrimitiveOperation;
import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.equation.Equation;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.optimisation.linear.SimplexSolver.Primitive1D;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.IndexSelector;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

abstract class SimplexTableau extends SimplexSolver.Primitive2D {

    static final class DenseRawTableau extends DenseTableau {

        private final int myColDim;
        private final double[][] myRaw;

        DenseRawTableau(final int nbConstraints, final int nbPositiveProblemVariables, final int nbNegativeProblemVariables, final int nbSlackVariables,
                final int nbIdentitySlackVariables, final boolean needDual) {

            super(nbConstraints, nbPositiveProblemVariables, nbNegativeProblemVariables, nbSlackVariables, nbIdentitySlackVariables, needDual);

            int nbRows = this.countConstraints() + 2;
            int nbCols = this.countVariablesTotally() + 1;

            // myTransposed = Primitive64Store.FACTORY.make(nbCols, nbRows);
            myColDim = nbCols;

            myRaw = new double[nbRows][nbCols];

        }

        DenseRawTableau(final SimplexTableau toCopy) {

            super(toCopy.countConstraints(), toCopy.countProblemVariables(), 0, toCopy.countSlackVariables() - toCopy.countIdentitySlackVariables(),
                    toCopy.countIdentitySlackVariables(), toCopy.isArtificials());

            myColDim = toCopy.getColDim();

            myRaw = toCopy.toRawCopy2D();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myRaw[row][col];
        }

        @Override
        public int getColDim() {
            return myColDim;
        }

        @Override
        public int getRowDim() {
            return myRaw.length;
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myRaw[row][col] = value;
        }

        private void doPivot(final int row, final int col, final double[] pivotRow) {

            for (int i = 0, limit = myRaw.length; i < limit; i++) {
                if (i != row) {
                    double[] dataRow = myRaw[i];
                    double colVal = dataRow[col];
                    if (colVal != ZERO) {
                        AXPY.invoke(dataRow, 0, -colVal, pivotRow, 0, 0, myColDim);
                    }
                }
            }
        }

        private void scale(final double[] pivotRow, final int col) {
            double pivotElement = pivotRow[col];
            if (pivotElement != ONE) {
                CorePrimitiveOperation.divide(pivotRow, 0, myColDim, 1, pivotRow, pivotElement);
            }
        }

        @Override
        boolean fixVariable(final int index, final double value) {

            int row = this.getBasisRowIndex(index);

            if (row < 0) {
                return false;
            }

            // Diff begin

            // Array1D<Double> currentRow = myTransposed.sliceColumn(row);
            ArrayR064 currentRow = ArrayR064.wrap(myRaw[row]);
            double currentRHS = currentRow.doubleValue(myColDim - 1);

            final ArrayR064 auxiliaryRow = ArrayR064.make(myColDim);
            if (currentRHS > value) {
                currentRow.axpy(NEG, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, value - currentRHS);
            } else if (currentRHS < value) {
                currentRow.axpy(ONE, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, currentRHS - value);
            } else {
                return true;
            }

            // Diff end

            Access1D<Double> objectiveRow = this.sliceTableauRow(this.countConstraints());

            int pivotCol = this.findNextPivotColumn(auxiliaryRow, objectiveRow);

            if (pivotCol < 0) {
                // TODO Problem infeasible?
                // Probably better to return true here, and have the subsequest solver.solve() return INFEASIBLE
                return false;
            }

            // Diff begin

            this.scale(auxiliaryRow.data, pivotCol);

            this.doPivot(-1, pivotCol, auxiliaryRow.data);

            // myTransposed.fillColumn(row, auxiliaryRow);
            myRaw[row] = auxiliaryRow.data;

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            this.update(row, pivotCol);

            return true;
        }

        @Override
        double getInfeasibility() {
            return myRaw[this.countConstraints() + 1][this.countVariablesTotally()];
        }

        @Override
        double getValue() {
            return myRaw[this.countConstraints()][this.countVariablesTotally()];
        }

        @Override
        SimplexSolver.Primitive2D newConstraintsBody() {

            double[][] store = myRaw;

            int nbConstraints = DenseRawTableau.this.countConstraints();
            int nbVariables = DenseRawTableau.this.countVariables();

            int nbIdentitySlackVariables = this.countIdentitySlackVariables();
            int dualIdentityBase = this.getDualIdentityBase();

            return new SimplexSolver.Primitive2D() {

                @Override
                public double doubleValue(final int row, final int col) {
                    return store[row][col];
                }

                @Override
                public int getColDim() {
                    return nbVariables;
                }

                @Override
                public int getRowDim() {
                    return nbConstraints;
                }

                @Override
                public void set(final int row, final int col, final double value) {

                    store[row][col] = value;

                    if (row < nbIdentitySlackVariables) {
                        if (col >= dualIdentityBase && value == 1D) {
                            DenseRawTableau.this.update(row, col);
                        }
                    } else {
                        store[nbConstraints + 1][col] -= value;
                    }
                }

            };
        }

        @Override
        SimplexSolver.Primitive1D newConstraintsRHS() {

            double[][] store = myRaw;

            int nbConstraints = DenseRawTableau.this.countConstraints();
            int nbVariablesTotally = DenseRawTableau.this.countVariablesTotally();
            int nbIdentitySlackVariables = this.countIdentitySlackVariables();
            int dualIdentityBase = DenseRawTableau.this.getDualIdentityBase();
            boolean artificials = DenseRawTableau.this.isArtificials();

            return new SimplexSolver.Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return store[index][nbVariablesTotally];
                }

                @Override
                public void set(final int index, final double value) {

                    if (artificials) {
                        store[index][dualIdentityBase + index] = ONE;
                    }

                    store[index][nbVariablesTotally] = value;

                    if (index >= nbIdentitySlackVariables) {
                        store[nbConstraints + 1][nbVariablesTotally] -= value;
                    }
                }

                @Override
                public int size() {
                    return nbConstraints;
                }

            };
        }

        @Override
        SimplexSolver.Primitive1D newObjective() {

            double[][] store = myRaw;

            int nbConstraints = DenseRawTableau.this.countConstraints();

            return new SimplexSolver.Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return store[nbConstraints][index];
                }

                @Override
                public void set(final int index, final double value) {
                    store[nbConstraints][index] = value;
                }

                @Override
                public int size() {
                    return DenseRawTableau.this.countProblemVariables();
                }

            };
        }

        @Override
        void pivot(final SimplexSolver.IterationPoint iterationPoint) {

            int row = iterationPoint.row;
            int col = iterationPoint.col;

            double[] pivotRow = myRaw[row];

            this.scale(pivotRow, col);

            this.doPivot(row, col, pivotRow);

            this.update(row, col);
        }

        @Override
        DenseTableau toDense() {
            return this;
        }

    }

    static abstract class DenseTableau extends SimplexTableau {

        DenseTableau(final int nbConstraints, final int nbPositiveProblemVariables, final int nbNegativeProblemVariables, final int nbSlackVariables,
                final int nbIdentitySlackVariables, final boolean needDual) {
            super(nbConstraints, nbPositiveProblemVariables, nbNegativeProblemVariables, nbSlackVariables, nbIdentitySlackVariables, needDual);
        }

    }

    static final class DenseTransposedTableau extends DenseTableau {

        private final int myColDim;
        private final Primitive64Store myTransposed;

        DenseTransposedTableau(final int nbConstraints, final int nbPositiveProblemVariables, final int nbNegativeProblemVariables, final int nbSlackVariables,
                final int nbIdentitySlackVariables, final boolean needDual) {

            super(nbConstraints, nbPositiveProblemVariables, nbNegativeProblemVariables, nbSlackVariables, nbIdentitySlackVariables, needDual);

            int nbRows = this.countConstraints() + 2;
            int nbCols = this.countVariablesTotally() + 1;

            myTransposed = Primitive64Store.FACTORY.make(nbCols, nbRows);
            myColDim = myTransposed.getRowDim();
        }

        DenseTransposedTableau(final SimplexTableau toCopy) {

            super(toCopy.countConstraints(), toCopy.countProblemVariables(), 0, toCopy.countSlackVariables() - toCopy.countIdentitySlackVariables(),
                    toCopy.countIdentitySlackVariables(), toCopy.isArtificials());

            myTransposed = Primitive64Store.FACTORY.transpose(toCopy);
            myColDim = myTransposed.getRowDim();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myTransposed.doubleValue(col, row);
        }

        @Override
        public int getColDim() {
            return myColDim;
        }

        @Override
        public int getRowDim() {
            return myTransposed.getColDim();
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myTransposed.set(col, row, value);
        }

        private void doPivot(final int row, final int col, final double[] pivotRowData, final int pivotRowIndexBase) {

            double[] data = myTransposed.data;

            for (int i = 0, limit = myTransposed.getColDim(); i < limit; i++) {
                if (i != row) {
                    int dataIndexBase = i * myColDim;
                    double colVal = data[dataIndexBase + col];
                    if (colVal != ZERO) {
                        AXPY.invoke(data, dataIndexBase, -colVal, pivotRowData, pivotRowIndexBase, 0, myColDim);
                    }
                }
            }
        }

        private void scale(final double[] pivotRowData, final int pivotRowIndexBase, final int col) {
            double pivotElement = pivotRowData[pivotRowIndexBase + col];
            if (pivotElement != ONE) {
                CorePrimitiveOperation.divide(pivotRowData, pivotRowIndexBase, pivotRowIndexBase + myColDim, 1, pivotRowData, pivotElement);
            }
        }

        @Override
        boolean fixVariable(final int index, final double value) {

            int row = this.getBasisRowIndex(index);

            if (row < 0) {
                return false;
            }

            // Diff begin

            Array1D<Double> currentRow = myTransposed.sliceColumn(row);
            double currentRHS = currentRow.doubleValue(myColDim - 1);

            final ArrayR064 auxiliaryRow = ArrayR064.make(myColDim);
            if (currentRHS > value) {
                currentRow.axpy(NEG, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, value - currentRHS);
            } else if (currentRHS < value) {
                currentRow.axpy(ONE, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, currentRHS - value);
            } else {
                return true;
            }

            // Diff end

            Access1D<Double> objectiveRow = this.sliceTableauRow(this.countConstraints());

            int pivotCol = this.findNextPivotColumn(auxiliaryRow, objectiveRow);

            if (pivotCol < 0) {
                // TODO Problem infeasible?
                // Probably better to return true here, and have the subsequest solver.solve() return INFEASIBLE
                return false;
            }

            // Diff begin

            this.scale(auxiliaryRow.data, 0, pivotCol);

            this.doPivot(-1, pivotCol, auxiliaryRow.data, 0);

            myTransposed.fillColumn(row, auxiliaryRow);

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            this.update(row, pivotCol);

            return true;
        }

        @Override
        double getInfeasibility() {
            return myTransposed.doubleValue(this.countVariablesTotally(), this.countConstraints() + 1);
        }

        Primitive64Store getTransposed() {
            return myTransposed;
        }

        @Override
        double getValue() {
            return myTransposed.doubleValue(this.countVariablesTotally(), this.countConstraints());
        }

        @Override
        SimplexSolver.Primitive2D newConstraintsBody() {

            Primitive64Store transposed = DenseTransposedTableau.this.getTransposed();

            int nbConstraints = DenseTransposedTableau.this.countConstraints();
            int nbVariables = DenseTransposedTableau.this.countVariables();

            int nbIdentitySlackVariables = this.countIdentitySlackVariables();
            int dualIdentityBase = this.getDualIdentityBase();

            return new SimplexSolver.Primitive2D() {

                @Override
                public double doubleValue(final int row, final int col) {
                    return transposed.doubleValue(col, row);
                }

                @Override
                public int getColDim() {
                    return nbVariables;
                }

                @Override
                public int getRowDim() {
                    return nbConstraints;
                }

                @Override
                public void set(final int row, final int col, final double value) {

                    transposed.set(col, row, value);

                    if (row < nbIdentitySlackVariables) {
                        if (col >= dualIdentityBase && value == 1D) {
                            DenseTransposedTableau.this.update(row, col);
                        }
                    } else {
                        transposed.add(col, nbConstraints + 1, -value);
                    }
                }

            };
        }

        @Override
        SimplexSolver.Primitive1D newConstraintsRHS() {

            Primitive64Store transposed = DenseTransposedTableau.this.getTransposed();

            int nbConstraints = DenseTransposedTableau.this.countConstraints();
            int nbVariablesTotally = DenseTransposedTableau.this.countVariablesTotally();
            int nbIdentitySlackVariables = this.countIdentitySlackVariables();
            int dualIdentityBase = DenseTransposedTableau.this.getDualIdentityBase();
            boolean artificials = DenseTransposedTableau.this.isArtificials();

            return new SimplexSolver.Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return transposed.doubleValue(nbVariablesTotally, index);
                }

                @Override
                public void set(final int index, final double value) {

                    if (artificials) {
                        transposed.set(dualIdentityBase + index, index, ONE);
                    }

                    transposed.set(nbVariablesTotally, index, value);

                    if (index >= nbIdentitySlackVariables) {
                        transposed.add(nbVariablesTotally, nbConstraints + 1, -value);
                    }
                }

                @Override
                public int size() {
                    return nbConstraints;
                }

            };
        }

        @Override
        SimplexSolver.Primitive1D newObjective() {

            Primitive64Store transposed = DenseTransposedTableau.this.getTransposed();

            int nbConstraints = DenseTransposedTableau.this.countConstraints();

            return new SimplexSolver.Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return transposed.doubleValue(index, nbConstraints);
                }

                @Override
                public void set(final int index, final double value) {
                    transposed.set(index, nbConstraints, value);
                }

                @Override
                public int size() {
                    return DenseTransposedTableau.this.countProblemVariables();
                }

            };
        }

        @Override
        void pivot(final SimplexSolver.IterationPoint iterationPoint) {

            int row = iterationPoint.row;
            int col = iterationPoint.col;

            double[] data = myTransposed.data;
            int pivotRowIndexBase = row * myColDim;

            this.scale(data, pivotRowIndexBase, col);

            this.doPivot(row, col, data, pivotRowIndexBase);

            this.update(iterationPoint);
        }

        @Override
        DenseTableau toDense() {
            return this;
        }

    }

    static final class MetaData implements UpdatableSolver.EntityMap {

        final boolean[] negatedDual;
        final int[] negativePartVariables;
        final int[] positivePartVariables;
        final EntryPair<ModelEntity<?>, ConstraintType>[] slack;

        MetaData(final int nbConstr, final int nbPos, final int nbNeg, final int nbSlack) {
            positivePartVariables = new int[nbPos];
            negativePartVariables = new int[nbNeg];
            slack = (EntryPair<ModelEntity<?>, ConstraintType>[]) new EntryPair<?, ?>[nbSlack];
            negatedDual = new boolean[nbConstr];
        }

        public int countSlackVariables() {
            return slack.length;
        }

        public int countVariables() {
            return positivePartVariables.length + negativePartVariables.length;
        }

        public EntryPair<ModelEntity<?>, ConstraintType> getSlack(final int idx) {
            return slack[idx];
        }

        public int indexOf(final int idx) {

            if (idx < 0) {
                throw new IllegalArgumentException();
            }

            if (idx < positivePartVariables.length) {
                return positivePartVariables[idx];
            }

            int negIdx = idx - positivePartVariables.length;

            if (negIdx < negativePartVariables.length) {
                return negativePartVariables[negIdx];
            }

            return -1;
        }

        public boolean isNegated(final int idx) {

            if (idx < 0) {
                throw new IllegalArgumentException();
            }

            if (idx < positivePartVariables.length) {
                return false;
            }

            if (idx - positivePartVariables.length < negativePartVariables.length) {
                return true;
            }

            return false;
        }

    }

    static final class SparseTableau extends SimplexTableau {

        private double myInfeasibility = ZERO;
        private final Array1D<Double> myObjectiveWeights;
        private final DenseArray<Double> myPhase1Weights;
        private final Array1D<Double> myRHS;
        private final SparseArray<Double>[] myRows;
        private final SparseArray.SparseFactory<Double> mySparseFactory;
        private double myValue = ZERO;

        SparseTableau(final int nbConstraints, final int nbPositiveProblemVariables, final int nbNegativeProblemVariables, final int nbSlackVariables,
                final int nbIdentitySlackVariables, final boolean needDual) {

            super(nbConstraints, nbPositiveProblemVariables, nbNegativeProblemVariables, nbSlackVariables, nbIdentitySlackVariables, needDual);

            int nbProblemVariables = nbPositiveProblemVariables + nbNegativeProblemVariables;

            long initial = Math.max(5L, Math.round(Math.sqrt(Math.min(nbConstraints, nbProblemVariables))));
            mySparseFactory = SparseArray.factory(ArrayR064.FACTORY).initial(initial);

            // Including artificial variables
            final int totNumbVars = this.countVariablesTotally();

            myRows = new SparseArray[nbConstraints];
            for (int r = 0; r < nbConstraints; r++) {
                myRows[r] = mySparseFactory.limit(totNumbVars).make();
            }

            myRHS = ARRAY1D_FACTORY.make(nbConstraints);

            myObjectiveWeights = ARRAY1D_FACTORY.make(totNumbVars);
            myPhase1Weights = DENSE_FACTORY.make(totNumbVars);
        }

        @Override
        public double doubleValue(final int row, final int col) {

            int nbConstraints = this.countConstraints();
            int nbVariables = this.countVariablesTotally();

            if (row < nbConstraints) {
                if (col < nbVariables) {
                    return myRows[row].doubleValue(col);
                }
                return myRHS.doubleValue(row);
            }
            if (row == nbConstraints) {
                if (col < nbVariables) {
                    return myObjectiveWeights.doubleValue(col);
                }
                return myValue;
            }
            if (col < nbVariables) {
                return myPhase1Weights.doubleValue(col);
            }
            return myInfeasibility;
        }

        @Override
        public int getColDim() {
            return this.countVariablesTotally() + 1;
        }

        @Override
        public int getRowDim() {
            return this.countConstraints() + 2;
        }

        @Override
        public void set(final int row, final int col, final double value) {

            int nbConstraints = this.countConstraints();
            int nbVariables = this.countVariablesTotally();

            if (row < nbConstraints) {
                if (col < nbVariables) {
                    myRows[row].set(col, value);
                } else {
                    myRHS.set(row, value);
                }
            } else if (row == nbConstraints) {
                if (col < nbVariables) {
                    myObjectiveWeights.set(col, value);
                } else {
                    myValue = value;
                }
            } else if (col < nbVariables) {
                myPhase1Weights.set(col, value);
            } else {
                myInfeasibility = value;
            }
        }

        private void doPivot(final int row, final int col, final SparseArray<Double> pivotRowBody, final double pivotRowRHS) {

            double colVal;

            for (int i = 0; i < myRows.length; i++) {
                if (i != row) {
                    SparseArray<Double> rowY = myRows[i];
                    colVal = -rowY.doubleValue(col);
                    if (colVal != ZERO) {
                        pivotRowBody.axpy(colVal, rowY);
                        myRHS.add(i, colVal * pivotRowRHS);
                    }
                }
            }

            colVal = -myObjectiveWeights.doubleValue(col);
            if (colVal != ZERO) {
                pivotRowBody.axpy(colVal, myObjectiveWeights);
                myValue += colVal * pivotRowRHS;
            }

            colVal = -myPhase1Weights.doubleValue(col);
            if (colVal != ZERO) {
                pivotRowBody.axpy(colVal, myPhase1Weights);
                myInfeasibility += colVal * pivotRowRHS;
            }
        }

        private double scale(final SparseArray<Double> pivotRowBody, final double pivotRowRHS, final int col) {

            double pivotElement = pivotRowBody.doubleValue(col);

            if (pivotElement != ONE) {
                UnaryFunction<Double> modifier = DIVIDE.second(pivotElement);
                pivotRowBody.modifyAll(modifier);
                return modifier.invoke(pivotRowRHS);
            }

            return pivotRowRHS;

        }

        @Override
        boolean fixVariable(final int index, final double value) {

            int row = this.getBasisRowIndex(index);

            if (row < 0) {
                return false;
            }

            // Diff begin

            SparseArray<Double> currentRow = myRows[row];
            double currentRHS = myRHS.doubleValue(row);

            final int totNumbVars = this.countVariablesTotally();

            SparseArray<Double> auxiliaryRow = mySparseFactory.limit(totNumbVars).make();
            double auxiliaryRHS = ZERO;

            if (currentRHS > value) {
                currentRow.axpy(NEG, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRHS = value - currentRHS;
            } else if (currentRHS < value) {
                currentRow.axpy(ONE, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRHS = currentRHS - value;
            } else {
                return true;
            }

            // Diff end

            Access1D<Double> objectiveRow = this.sliceTableauRow(this.countConstraints());

            int pivotCol = this.findNextPivotColumn(auxiliaryRow, objectiveRow);

            if (pivotCol < 0) {
                // TODO Problem infeasible?
                // Probably better to return true here, and have the subsequest solver.solve() return INFEASIBLE
                return false;
            }

            // Diff begin

            auxiliaryRHS = this.scale(auxiliaryRow, auxiliaryRHS, pivotCol);

            this.doPivot(-1, pivotCol, auxiliaryRow, auxiliaryRHS);

            myRows[row] = auxiliaryRow;
            myRHS.set(row, auxiliaryRHS);

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            this.update(row, pivotCol);

            return true;
        }

        /**
         * @return The phase 1 objective function value
         */
        @Override
        double getInfeasibility() {
            return myInfeasibility;
        }

        Array1D<Double> getObjectiveWeights() {
            return myObjectiveWeights;
        }

        DenseArray<Double> getPhase1Weights() {
            return myPhase1Weights;
        }

        Array1D<Double> getRHS() {
            return myRHS;
        }

        SparseArray<Double> getRow(final int row) {
            return myRows[row];
        }

        SparseArray<Double> getRow(final long row) {
            return myRows[Math.toIntExact(row)];
        }

        SparseArray<Double>[] getRows() {
            return myRows;
        }

        @Override
        double getValue() {
            return myValue;
        }

        @Override
        SimplexSolver.Primitive2D newConstraintsBody() {

            int nbIdentitySlackVariables = this.countIdentitySlackVariables();
            int dualIdentityBase = this.getDualIdentityBase();

            return new SimplexSolver.Primitive2D() {

                @Override
                public double doubleValue(final int row, final int col) {
                    return SparseTableau.this.getRow(row).doubleValue(col);
                }

                @Override
                public int getColDim() {
                    return SparseTableau.this.countVariables();
                }

                @Override
                public int getRowDim() {
                    return SparseTableau.this.countConstraints();
                }

                @Override
                public void set(final int row, final int col, final double value) {

                    SparseTableau.this.getRow(row).set(col, value);

                    if (row < nbIdentitySlackVariables) {
                        if (col >= dualIdentityBase && value == 1D) {
                            SparseTableau.this.update(row, col);
                        }
                    } else {
                        SparseTableau.this.getPhase1Weights().add(col, -value);
                    }
                }

            };
        }

        @Override
        SimplexSolver.Primitive1D newConstraintsRHS() {

            Array1D<Double> rhs = SparseTableau.this.getRHS();

            int nbIdentitySlackVariables = this.countIdentitySlackVariables();
            int dualIdentityBase = SparseTableau.this.getDualIdentityBase();
            boolean artificials = SparseTableau.this.isArtificials();

            return new SimplexSolver.Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return rhs.doubleValue(index);
                }

                @Override
                public void set(final int index, final double value) {

                    if (artificials) {
                        SparseTableau.this.getRow(index).set(dualIdentityBase + index, ONE);
                    }

                    rhs.set(index, value);

                    if (index >= nbIdentitySlackVariables) {
                        SparseTableau.this.subtractInfeasibility(value);
                    }
                }

                @Override
                public int size() {
                    return SparseTableau.this.countConstraints();
                }

            };
        }

        @Override
        SimplexSolver.Primitive1D newObjective() {

            Array1D<Double> objectiveWeights = SparseTableau.this.getObjectiveWeights();

            return new SimplexSolver.Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return objectiveWeights.doubleValue(index);
                }

                @Override
                public void set(final int index, final double value) {
                    objectiveWeights.set(index, value);
                }

                @Override
                public int size() {
                    return SparseTableau.this.countProblemVariables();
                }

            };
        }

        @Override
        void pivot(final SimplexSolver.IterationPoint iterationPoint) {

            int row = iterationPoint.row;
            int col = iterationPoint.col;

            SparseArray<Double> pivotRowBody = myRows[row];
            double pivotRowRHS = myRHS.doubleValue(row);

            pivotRowRHS = this.scale(pivotRowBody, pivotRowRHS, col);
            myRHS.set(row, pivotRowRHS);

            this.doPivot(row, col, pivotRowBody, pivotRowRHS);

            this.update(iterationPoint);
        }

        void subtractInfeasibility(final double infeasibility) {
            myInfeasibility -= infeasibility;
        }

        @Override
        DenseTableau toDense() {
            return new DenseTransposedTableau(this);
        }

    }

    static final Array1D.Factory<Double> ARRAY1D_FACTORY = Array1D.factory(ArrayR064.FACTORY);
    static final DenseArray.Factory<Double> DENSE_FACTORY = ArrayR064.FACTORY;

    static void copy(final LinearSolver.Builder builder, final SimplexTableau tableau) {

        MatrixStore<Double> mtrxAE = builder.getAE();
        Mutate2D body = tableau.constraintsBody();
        for (int i = 0; i < mtrxAE.getRowDim(); i++) {
            for (int j = 0; j < mtrxAE.getColDim(); j++) {
                double value = mtrxAE.doubleValue(i, j);
                if (Math.abs(value) > MACHINE_EPSILON) {
                    body.set(i, j, value);
                }
            }
        }

        MatrixStore<Double> mtrxBE = builder.getBE();
        Mutate1D rhs = tableau.constraintsRHS();
        for (int i = 0; i < mtrxBE.size(); i++) {
            rhs.set(i, mtrxBE.doubleValue(i));
        }

        MatrixStore<Double> mtrxC = builder.getC();
        Mutate1D obj = tableau.objective();
        for (int i = 0; i < mtrxC.size(); i++) {
            obj.set(i, mtrxC.doubleValue(i));
        }

    }

    static boolean isSparse(final Optimisation.Options options) {
        return options.sparse != null && options.sparse.booleanValue();
    }

    static SimplexTableau make(final int nbConstraints, final int nbPositiveProblemVariables, final int nbNegativeProblemVariables, final int nbSlackVariables,
            final int nbIdentitySlackVariables, final boolean needDual, final Optimisation.Options options) {

        if (SimplexTableau.isSparse(options)) {
            return new SparseTableau(nbConstraints, nbPositiveProblemVariables, nbNegativeProblemVariables, nbSlackVariables, nbIdentitySlackVariables,
                    needDual);
        }

        return new DenseRawTableau(nbConstraints, nbPositiveProblemVariables, nbNegativeProblemVariables, nbSlackVariables, nbIdentitySlackVariables, needDual);
    }

    static SimplexTableau make(final LinearSolver.Builder builder, final Optimisation.Options options) {

        int nbConstraints = builder.countConstraints();
        int nbProblemVariables = builder.countVariables();
        int nbSlackVariables = 0;
        int nbIdentitySlackVariables = 0;
        boolean needDual = true;

        SimplexTableau tableau = SimplexTableau.make(nbConstraints, nbProblemVariables, 0, nbSlackVariables, nbIdentitySlackVariables, needDual, options);
        SimplexTableau.copy(builder, tableau);
        return tableau;
    }

    static SimplexTableau newDense(final LinearSolver.Builder matrices) {

        SimplexTableau tableau = new DenseTransposedTableau(matrices.countConstraints(), matrices.countVariables(), 0, 0, 0, true);

        SimplexTableau.copy(matrices, tableau);

        return tableau;
    }

    static SparseTableau newSparse(final LinearSolver.Builder matrices) {

        SparseTableau tableau = new SparseTableau(matrices.countConstraints(), matrices.countVariables(), 0, 0, 0, true);

        SimplexTableau.copy(matrices, tableau);

        return tableau;
    }

    static int size(final int nbConstraints, final int nbProblemVariables, final int nbSlackVariables, final int nbIdentitySlackVariables,
            final boolean needDual) {

        int numbRows = nbConstraints + 2;
        int numbCols = nbProblemVariables + nbSlackVariables + (needDual ? nbConstraints : nbIdentitySlackVariables) + 1;

        return numbRows * numbCols; //  Total number of elements in a dense tableau
    }

    private final int[] myBasis;
    private transient SimplexSolver.Primitive2D myConstraintsBody = null;
    private transient SimplexSolver.Primitive1D myConstraintsRHS = null;
    private final int myNumberOfArtificialVariables;
    private final int myNumberOfConstraints;
    private final int myNumberOfIdentitySlackVariables;
    private final int myNumberOfProblemVariables;
    private final int myNumberOfSlackVariables;
    private transient SimplexSolver.Primitive1D myObjective = null;
    private final IndexSelector mySelector;

    final MetaData meta;

    /**
     * @param nbConstraints The number of constraints.
     * @param nbPositiveProblemVariables The number of positive problem variables.
     * @param nbNegativeProblemVariables The number of negative problem variables.
     * @param nbSlackVariables The number of slack variables (the number of inequality constraints). The
     *        actual/full set of slack variables is nbSlackVariables + nbIdentitySlackVariables
     * @param nbIdentitySlackVariables The number of slack variables that form an identity matrix and can
     *        reduce the need for artificial variables. If this is != 0 the nbSlackVariables must be reduced
     *        and the constraints corresponding to these slack variables must be the top/first rows in the
     *        tableau.
     * @param needDual Should there be an explicit full set of artificial variables? (to extract the dual). If
     *        this is set to true and nbIdentitySlackVariables > 0 then special care needs to taken when
     *        constructing the tableau. In that case the identity slack variables and the artificial variables
     *        must form an identity matrix in the initial tableau (towards the right hand side).
     */
    SimplexTableau(final int nbConstraints, final int nbPositiveProblemVariables, final int nbNegativeProblemVariables, final int nbSlackVariables,
            final int nbIdentitySlackVariables, final boolean needDual) {

        super();

        myNumberOfConstraints = nbConstraints;
        myNumberOfProblemVariables = nbPositiveProblemVariables + nbNegativeProblemVariables;
        myNumberOfSlackVariables = nbSlackVariables;
        myNumberOfIdentitySlackVariables = nbIdentitySlackVariables;
        myNumberOfArtificialVariables = needDual ? nbConstraints - nbIdentitySlackVariables : 0;

        mySelector = new IndexSelector(this.countVariables());
        myBasis = Structure1D.newIncreasingRange(-nbConstraints, nbConstraints);

        meta = new MetaData(nbConstraints, nbPositiveProblemVariables, nbNegativeProblemVariables, nbSlackVariables + nbIdentitySlackVariables);
    }

    /**
     * The area of the tableau corresponding to the constraints' body (excluding any artificial variables).
     */
    final SimplexSolver.Primitive2D constraintsBody() {
        if (myConstraintsBody == null) {
            myConstraintsBody = this.newConstraintsBody();
        }
        return myConstraintsBody;
    }

    /**
     * The area of the tableau corresponding to the constraints' RHS.
     */
    final SimplexSolver.Primitive1D constraintsRHS() {
        if (myConstraintsRHS == null) {
            myConstraintsRHS = this.newConstraintsRHS();
        }
        return myConstraintsRHS;
    }

    final int countArtificialVariables() {
        return myNumberOfArtificialVariables;
    }

    /**
     * {@link #countBasisDeficit()} should return the same number, and is a faster alternative.
     *
     * @return The number of artificial variables in the basis.
     */
    final int countBasicArtificials() {
        int retVal = 0;
        for (int i = 0, limit = myBasis.length; i < limit; i++) {
            if (myBasis[i] < 0) {
                retVal++;
            }
        }
        return retVal;
    }

    /**
     * {@link #countBasicArtificials()} should return the same number, but this is a faster alternative since
     * it's a simple lookup.
     *
     * @return The number of variables (not artificial) that can be added to the basis.
     */
    final int countBasisDeficit() {
        return myNumberOfConstraints - mySelector.countIncluded();
    }

    int countConstraints() {
        return myNumberOfConstraints;
    }

    int countIdentitySlackVariables() {
        return myNumberOfIdentitySlackVariables;
    }

    int countProblemVariables() {
        return myNumberOfProblemVariables;
    }

    int countSlackVariables() {
        return myNumberOfSlackVariables + myNumberOfIdentitySlackVariables;
    }

    /**
     * problem + slack
     */
    int countVariables() {
        return myNumberOfProblemVariables + myNumberOfSlackVariables + myNumberOfIdentitySlackVariables;
    }

    /**
     * problem + slack + artificial
     */
    int countVariablesTotally() {
        return myNumberOfProblemVariables + myNumberOfSlackVariables + myNumberOfIdentitySlackVariables + myNumberOfArtificialVariables;
    }

    int findNextPivotColumn(final Access1D<Double> auxiliaryRow, final Access1D<Double> objectiveRow) {

        int retVal = -1;
        double minQuotient = MACHINE_LARGEST;

        for (ElementView1D<Double, ?> nz : auxiliaryRow.nonzeros()) {
            final int i = (int) nz.index();
            if (i >= this.countVariables()) {
                break;
            }
            final double denominator = nz.doubleValue();
            if (denominator < -1E-8) {
                double numerator = objectiveRow.doubleValue(i);
                double quotient = Math.abs(numerator / denominator);
                if (quotient < minQuotient) {
                    minQuotient = quotient;
                    retVal = i;
                }
            }
        }

        return retVal;
    }

    abstract boolean fixVariable(final int index, final double value);

    Collection<Equation> generateCutCandidates(final boolean[] integer, final NumberContext accuracy, final double fractionality) {

        int nbConstraints = this.countConstraints();
        int nbProblemVariables = this.countProblemVariables();

        Primitive1D constraintsRHS = this.sliceConstraintsRHS();

        List<Equation> retVal = new ArrayList<>();

        for (int i = 0; i < nbConstraints; i++) {
            int variableIndex = this.getBasisColumnIndex(i);

            double rhs = constraintsRHS.doubleValue(i);

            if (variableIndex >= 0 && variableIndex < nbProblemVariables && integer[variableIndex] && !accuracy.isInteger(rhs)) {

                Equation maybe = TableauCutGenerator.doGomoryMixedInteger(this.sliceBodyRow(i), variableIndex, rhs, integer, fractionality);

                if (maybe != null) {
                    retVal.add(maybe);
                }
            }
        }

        return retVal;
    }

    int[] getBasis() {
        return myBasis.clone();
    }

    int getBasisColumnIndex(final int basisRowIndex) {
        return myBasis[basisRowIndex];
    }

    int getBasisRowIndex(final int basisColumnIndex) {
        return IndexOf.indexOf(myBasis, basisColumnIndex);
    }

    int getDualIdentityBase() {
        return this.countVariablesTotally() - this.countConstraints();
    }

    final int[] getExcluded() {
        return mySelector.getExcluded();
    }

    final int[] getIncluded() {
        return mySelector.getIncluded();
    }

    /**
     * @return The phase 1 objective function value
     */
    abstract double getInfeasibility();

    /**
     * @return The (phase 2) objective function value
     */
    abstract double getValue();

    boolean isAbleToExtractDual() {
        return myNumberOfIdentitySlackVariables + myNumberOfArtificialVariables == myNumberOfConstraints;
    }

    boolean isArtificials() {
        return myNumberOfArtificialVariables > 0;
    }

    /**
     * Are there any artificial variables in the basis?
     */
    final boolean isBasicArtificials() {
        return myNumberOfConstraints > mySelector.countIncluded();
    }

    final boolean isExcluded(final int index) {
        return mySelector.isExcluded(index);
    }

    final boolean isIncluded(final int index) {
        return mySelector.isIncluded(index);
    }

    abstract SimplexSolver.Primitive2D newConstraintsBody();

    abstract SimplexSolver.Primitive1D newConstraintsRHS();

    abstract SimplexSolver.Primitive1D newObjective();

    /**
     * The area of the tableau corresponding to the objective function.
     */
    final SimplexSolver.Primitive1D objective() {
        if (myObjective == null) {
            myObjective = this.newObjective();
        }
        return myObjective;
    }

    abstract void pivot(SimplexSolver.IterationPoint iterationPoint);

    final SimplexSolver.Primitive1D sliceBodyColumn(final int col) {

        return new SimplexSolver.Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(index, col);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(index, col, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.countConstraints();
            }

        };
    }

    final SimplexSolver.Primitive1D sliceBodyRow(final int row) {

        return new SimplexSolver.Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(row, index);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(row, index, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.countVariables();
            }

        };
    }

    final SimplexSolver.Primitive1D sliceConstraintsRHS() {
        return this.constraintsRHS();
    }

    /**
     * @return An array of the dual variable values (of the original problem, never phase 1).
     */
    final SimplexSolver.Primitive1D sliceDualVariables() {

        int nbConstraints = this.countConstraints();
        int nbVariables = this.countVariablesTotally();

        int dualIdentityBase = this.getDualIdentityBase();

        return new SimplexSolver.Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return -SimplexTableau.this.doubleValue(nbConstraints, dualIdentityBase + index);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(nbConstraints, dualIdentityBase + index, -value);
            }

            @Override
            public int size() {
                return nbVariables - dualIdentityBase;
            }

        };
    }

    final SimplexSolver.Primitive1D sliceTableauColumn(final int col) {

        return new SimplexSolver.Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(index, col);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(index, col, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.getRowDim();
            }

        };
    }

    final SimplexSolver.Primitive1D sliceTableauRow(final int row) {

        return new SimplexSolver.Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(row, index);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(row, index, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.getColDim();
            }

        };
    }

    abstract DenseTableau toDense();

    void update(final int pivotRow, final int pivotCol) {

        int tmpOld = myBasis[pivotRow];
        if (tmpOld >= 0) {
            mySelector.exclude(tmpOld);
        }

        int tmpNew = pivotCol;
        if (tmpNew >= 0) {
            mySelector.include(tmpNew);
        }

        myBasis[pivotRow] = pivotCol;
    }

    void update(final long pivotRow, final long pivotCol) {
        this.update(Math.toIntExact(pivotRow), Math.toIntExact(pivotCol));
    }

    void update(final SimplexSolver.IterationPoint point) {
        this.update(point.row, point.col);
    }

    /**
     * The current, phase 1 or 2, objective function value
     */
    final double value(final boolean phase1) {
        if (phase1) {
            return this.getInfeasibility();
        }
        return this.getValue();
    }

}
