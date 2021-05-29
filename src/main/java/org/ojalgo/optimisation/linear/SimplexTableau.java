/*
 * Copyright 1997-2021 Optimatika
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

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.machine.JavaType;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.SimplexSolver.AlgorithmStore;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.type.IndexSelector;
import org.ojalgo.type.NumberDefinition;

abstract class SimplexTableau implements AlgorithmStore, Access2D<Double> {

    static final class DenseTableau extends SimplexTableau {

        private final int myStructure;
        private final Primitive64Store myTransposed;

        DenseTableau(final int numberOfConstraints, final int numberOfProblemVariables, final int numberOfSlackVariables) {

            super(numberOfConstraints, numberOfProblemVariables, numberOfSlackVariables);

            final int numbRows = numberOfConstraints + 2;
            final int numbCols = numberOfProblemVariables + numberOfSlackVariables + numberOfConstraints + 1;

            myTransposed = Primitive64Store.FACTORY.makeZero(numbCols, numbRows);
            myStructure = (int) myTransposed.countRows();
        }

        DenseTableau(final LinearSolver.Builder matrices) {

            super(matrices.countConstraints(), matrices.countVariables(), 0);

            int constraintsCount = this.countConstraints();
            int variablesCount = this.countVariables();

            MatrixStore.LogicalBuilder<Double> tableauBuilder = MatrixStore.PRIMITIVE64.makeZero(1, 1);
            tableauBuilder = tableauBuilder
                    .left(matrices.getC().transpose().logical().right(MatrixStore.PRIMITIVE64.makeZero(1, constraintsCount).get()).get());

            if (constraintsCount >= 1) {
                tableauBuilder = tableauBuilder.above(matrices.getAE(), MatrixStore.PRIMITIVE64.makeIdentity(constraintsCount).get(), matrices.getBE());
            }
            tableauBuilder = tableauBuilder.below(MatrixStore.PRIMITIVE64.makeZero(1, variablesCount).get(),
                    Primitive64Store.FACTORY.makeFilled(1, constraintsCount, new NullaryFunction<Double>() {

                        public double doubleValue() {
                            return ONE;
                        }

                        public Double invoke() {
                            return ONE;
                        }

                    }));
            //myTransposedTableau = (PrimitiveDenseStore) tmpTableauBuilder.build().transpose().copy();
            myTransposed = tableauBuilder.transpose().collect(Primitive64Store.FACTORY);
            myStructure = (int) myTransposed.countRows();
            // myTableau = LinearSolver.make(myTransposedTableau);

            for (int i = 0; i < constraintsCount; i++) {

                myTransposed.caxpy(NEG, i, constraintsCount + 1, 0);

            }

        }

        DenseTableau(final SparseTableau sparse) {

            super(sparse.countConstraints(), sparse.countProblemVariables(), sparse.countSlackVariables());

            myTransposed = sparse.transpose();
            myStructure = (int) myTransposed.countRows();
        }

        public long countColumns() {
            return myTransposed.countRows();
        }

        public long countRows() {
            return myTransposed.countColumns();
        }

        public double doubleValue(final long row, final long col) {
            return myTransposed.doubleValue(col, row);
        }

        public Double get(final long row, final long col) {
            return myTransposed.get(col, row);
        }

        private void doPivot(final int row, final int col, final double[] dataX, final int baseX, final int structure) {
            for (int i = 0, limit = (int) myTransposed.countColumns(); i < limit; i++) {
                if (i != row) {
                    final double colVal = myTransposed.doubleValue(col, i);
                    if (colVal != ZERO) {
                        AXPY.invoke(myTransposed.data, i * structure, -colVal, dataX, baseX, 0, structure);
                    }
                }
            }
        }

        private double scale(final DenseArray<Double> pivotBody, final int pivotCol) {

            double pivotElement = pivotBody.doubleValue(pivotCol);

            if (ABS.invoke(pivotElement) < ONE) {
                final UnaryFunction<Double> tmpModifier = DIVIDE.second(pivotElement);
                pivotBody.modifyAll(tmpModifier);
            } else if (pivotElement != ONE) {
                final UnaryFunction<Double> tmpModifier = MULTIPLY.second(ONE / pivotElement);
                pivotBody.modifyAll(tmpModifier);
            }

            return pivotBody.doubleValue(pivotBody.count() - 1L);
        }

        @Override
        protected boolean fixVariable(final int index, final double value) {

            int row = this.getBasisRowIndex(index);

            if (row < 0) {
                return false;
            }

            // Diff begin

            Array1D<Double> currentRow = myTransposed.sliceColumn(row);
            double currentRHS = currentRow.doubleValue(myStructure - 1);

            final Primitive64Array auxiliaryRow = Primitive64Array.make(myStructure);
            if (currentRHS > value) {
                currentRow.axpy(NEG, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myStructure - 1, value - currentRHS);
            } else if (currentRHS < value) {
                currentRow.axpy(ONE, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myStructure - 1, currentRHS - value);
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

            this.scale(auxiliaryRow, pivotCol);

            this.doPivot(-1, pivotCol, auxiliaryRow.data, 0, myStructure);

            myTransposed.fillColumn(row, auxiliaryRow);

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            IterationPoint iterationPoint = new IterationPoint();
            iterationPoint.row = row;
            iterationPoint.col = pivotCol;

            this.update(iterationPoint);

            return true;
        }

        @Override
        protected long getOvercapacity() {
            return 0L;
        }

        @Override
        protected void pivot(final IterationPoint iterationPoint) {

            final int row = iterationPoint.row;
            final int col = iterationPoint.col;

            final double pivotElement = myTransposed.doubleValue(col, row);
            if (pivotElement != ONE) {
                myTransposed.modifyColumn(0, row, DIVIDE.second(pivotElement));
            }
            //            if (ABS.invoke(pivotElement) < ONE) {
            //                myTransposed.modifyColumn(0, row, DIVIDE.second(pivotElement));
            //            } else if (pivotElement != ONE) {
            //                myTransposed.modifyColumn(0, row, MULTIPLY.second(ONE / pivotElement));
            //            }

            int structure = (int) myTransposed.countRows();
            final double[] dataX = myTransposed.data;
            final int baseX = row * structure;

            this.doPivot(row, col, dataX, baseX, structure);

            this.update(iterationPoint);
        }

        @Override
        protected Array1D<Double> sliceConstraintsRHS() {
            return myTransposed.sliceRow(this.countVariablesTotally()).sliceRange(0, this.countConstraints());
        }

        @Override
        protected Access1D<Double> sliceDualVariables() {

            int numbVariables = this.countVariables();
            int numbConstraints = this.countConstraints();

            Array1D<Double> rowWithDuals = myTransposed.sliceColumn(numbConstraints);
            final Array1D<Double> dualsOnly = rowWithDuals.sliceRange(numbVariables, numbVariables + numbConstraints);

            return new Access1D<Double>() {

                public long count() {
                    return dualsOnly.count();
                }

                public double doubleValue(final long index) {
                    return -dualsOnly.doubleValue(index);
                }

                public Double get(final long index) {
                    return -dualsOnly.doubleValue(index);
                }

                @Override
                public String toString() {
                    return Access1D.toString(this);
                }

            };
        }

        @Override
        protected Array1D<Double> sliceTableauColumn(final int col) {
            return myTransposed.sliceRow(col).sliceRange(0, this.countConstraints());
        }

        @Override
        protected Array1D<Double> sliceTableauRow(final int row) {
            return myTransposed.sliceColumn(row).sliceRange(0, this.countVariablesTotally());
        }

        @Override
        protected DenseTableau toDense() {
            return this;
        }

        @Override
        Mutate2D newConstraintsBody() {
            return new Mutate2D() {

                public void add(final long row, final long col, final Comparable<?> addend) {
                    this.add(row, col, NumberDefinition.doubleValue(addend));
                }

                public void add(final long row, final long col, final double addend) {
                    //                    myRows[(int) row].add(col, addend);
                    //                    myPhase1Weights.add(col, -addend);
                    myTransposed.add(col, row, addend);
                    myTransposed.add(col, DenseTableau.this.countConstraints() + 1, -addend);
                }

                public long countColumns() {
                    return DenseTableau.this.countVariables();
                }

                public long countRows() {
                    return DenseTableau.this.countConstraints();
                }

                public void set(final long row, final long col, final Comparable<?> value) {
                    this.set(row, col, NumberDefinition.doubleValue(value));
                }

                public void set(final long row, final long col, final double value) {
                    //                    myRows[(int) row].set(col, value);
                    //                    myPhase1Weights.add(col, -value);
                    myTransposed.set(col, row, value);
                    myTransposed.add(col, DenseTableau.this.countConstraints() + 1, -value);
                }

            };
        }

        @Override
        Mutate1D newConstraintsRHS() {

            final int numbVar = this.countVariables();
            final int numbConstr = this.countConstraints();

            final int col = numbVar + numbConstr;

            return new Mutate1D() {

                public void add(final long index, final Comparable<?> addend) {
                    this.add(index, NumberDefinition.doubleValue(addend));
                }

                public void add(final long index, final double addend) {
                    //                    myRows[(int) index].set(SparseTableau.this.countVariables() + index, ONE);
                    //                    myRHS.add(index, addend);
                    //                    myInfeasibility -= addend;
                    myTransposed.set(numbVar + index, index, ONE);
                    myTransposed.add(col, index, addend);
                    myTransposed.add(col, numbConstr + 1, -addend);
                }

                public long count() {
                    return DenseTableau.this.countConstraints();
                }

                public void set(final long index, final Comparable<?> value) {
                    this.set(index, NumberDefinition.doubleValue(value));
                }

                public void set(final long index, final double value) {
                    //                    myRows[(int) index].set(SparseTableau.this.countVariables() + index, ONE);
                    //                    myRHS.set(index, value);
                    //                    myInfeasibility -= value;
                    myTransposed.set(numbVar + index, index, ONE);
                    myTransposed.set(col, index, value);
                    myTransposed.add(col, numbConstr + 1, -value);
                }

            };
        }

        @Override
        Mutate1D newObjective() {

            final int row = DenseTableau.this.countConstraints();

            return new Mutate1D() {

                public void add(final long index, final Comparable<?> addend) {
                    this.add(index, NumberDefinition.doubleValue(addend));
                }

                public void add(final long index, final double addend) {
                    // myObjectiveWeights.add(index, addend);
                    myTransposed.add(index, row, addend);
                }

                public long count() {
                    return DenseTableau.this.countVariables();
                }

                public void set(final long index, final Comparable<?> value) {
                    this.set(index, NumberDefinition.doubleValue(value));
                }

                public void set(final long index, final double value) {
                    // myObjectiveWeights.set(index, value);
                    myTransposed.set(index, row, value);
                }

            };
        }

    }

    static final class IterationPoint {

        private boolean myPhase1 = true;

        int col;
        int row;

        IterationPoint() {
            super();
            this.reset();
        }

        boolean isPhase1() {
            return myPhase1;
        }

        boolean isPhase2() {
            return !myPhase1;
        }

        void reset() {
            row = -1;
            col = -1;
        }

        void returnToPhase1() {
            myPhase1 = true;
        }

        void switchToPhase2() {
            myPhase1 = false;
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

        @SuppressWarnings("unchecked")
        SparseTableau(final int numberOfConstraints, final int numberOfProblemVariables, final int numberOfSlackVariables) {

            super(numberOfConstraints, numberOfProblemVariables, numberOfSlackVariables);

            long initial = Math.max(5L, Math.round(Math.sqrt(Math.min(numberOfConstraints, numberOfProblemVariables))));
            mySparseFactory = SparseArray.factory(Primitive64Array.FACTORY).initial(initial);

            // Including artificial variables
            final int totNumbVars = this.countVariablesTotally();

            myRows = new SparseArray[numberOfConstraints];
            for (int r = 0; r < numberOfConstraints; r++) {
                myRows[r] = mySparseFactory.limit(totNumbVars).make();
            }

            myRHS = ARRAY1D_FACTORY.makeZero(numberOfConstraints);

            myObjectiveWeights = ARRAY1D_FACTORY.makeZero(totNumbVars);
            myPhase1Weights = DENSE_FACTORY.make(totNumbVars);
        }

        SparseTableau(final LinearSolver.Builder matrices) {

            this(matrices.countConstraints(), matrices.countVariables(), 0);

            MatrixStore<Double> A = matrices.getAE();
            Mutate2D body = this.newConstraintsBody();
            for (int i = 0; i < A.countRows(); i++) {
                for (int j = 0; j < A.countColumns(); j++) {
                    final double value = A.doubleValue(i, j);
                    if (Math.abs(value) > MACHINE_EPSILON) {
                        body.set(i, j, value);
                    }
                }
            }

            MatrixStore<Double> b = matrices.getBE();
            Mutate1D rhs = this.newConstraintsRHS();
            for (int i = 0; i < b.count(); i++) {
                final double value = b.doubleValue(i);
                if (Math.abs(value) > MACHINE_EPSILON) {
                    rhs.set(i, value);
                }
            }

            MatrixStore<Double> c = matrices.getC();
            Mutate1D obj = this.newObjective();
            for (int i = 0; i < c.count(); i++) {
                final double value = c.doubleValue(i);
                if (Math.abs(value) > MACHINE_EPSILON) {
                    obj.set(i, value);
                }
            }
        }

        public long countColumns() {
            return this.countVariablesTotally() + 1L;
        }

        public long countRows() {
            return this.countConstraints() + 2L;
        }

        public double doubleValue(final long row, final long col) {

            final int myNumberOfConstraints = this.countConstraints();
            final int myNumberOfVariables = this.countVariables();

            if (row < myNumberOfConstraints) {
                if (col < (myNumberOfVariables + myNumberOfConstraints)) {
                    return myRows[(int) row].doubleValue(col);
                } else {
                    return myRHS.doubleValue(row);
                }
            } else if (row == myNumberOfConstraints) {
                if (col < (myNumberOfVariables + myNumberOfConstraints)) {
                    return myObjectiveWeights.doubleValue(col);
                } else {
                    return myValue;
                }
            } else {
                if (col < (myNumberOfVariables + myNumberOfConstraints)) {
                    return myPhase1Weights.doubleValue(col);
                } else {
                    return myInfeasibility;
                }
            }
        }

        public Double get(final long row, final long col) {
            return this.doubleValue(row, col);
        }

        private void doPivot(final int row, final int col, final SparseArray<Double> pivotedRow, final double pivotedRHS) {

            double colVal;

            for (int i = 0; i < myRows.length; i++) {
                if (i != row) {
                    final SparseArray<Double> rowY = myRows[i];
                    colVal = -rowY.doubleValue(col);
                    if (colVal != ZERO) {
                        pivotedRow.axpy(colVal, rowY);
                        myRHS.add(i, colVal * pivotedRHS);
                    }
                }
            }

            colVal = -myObjectiveWeights.doubleValue(col);
            if (colVal != ZERO) {
                pivotedRow.axpy(colVal, myObjectiveWeights);
                myValue += colVal * pivotedRHS;
            }

            colVal = -myPhase1Weights.doubleValue(col);
            if (colVal != ZERO) {
                pivotedRow.axpy(colVal, myPhase1Weights);
                myInfeasibility += colVal * pivotedRHS;
            }
        }

        private double scale(final SparseArray<Double> pivotBody, final int pivotCol, final double pivotRHS) {

            double pivotElement = pivotBody.doubleValue(pivotCol);

            if (pivotElement != ONE) {
                final UnaryFunction<Double> modifier = DIVIDE.second(pivotElement);
                pivotBody.modifyAll(modifier);
                return modifier.invoke(pivotRHS);
            } else {
                return pivotRHS;
            }

            //            if (ABS.invoke(pivotElement) < ONE) {
            //                final UnaryFunction<Double> tmpModifier = DIVIDE.second(pivotElement);
            //                pivotBody.modifyAll(tmpModifier);
            //                return tmpModifier.invoke(pivotRHS);
            //            } else if (pivotElement != ONE) {
            //                final UnaryFunction<Double> tmpModifier = MULTIPLY.second(ONE / pivotElement);
            //                pivotBody.modifyAll(tmpModifier);
            //                return tmpModifier.invoke(pivotRHS);
            //            } else {
            //                return pivotRHS;
            //            }
        }

        @Override
        protected boolean fixVariable(final int index, final double value) {

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

            auxiliaryRHS = this.scale(auxiliaryRow, pivotCol, auxiliaryRHS);

            this.doPivot(-1, pivotCol, auxiliaryRow, auxiliaryRHS);

            myRows[row] = auxiliaryRow;
            myRHS.set(row, auxiliaryRHS);

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            IterationPoint iterationPoint = new IterationPoint();
            iterationPoint.row = row;
            iterationPoint.col = pivotCol;

            this.update(iterationPoint);

            return true;
        }

        @Override
        protected long getOvercapacity() {
            long retVal = 0L;
            for (int r = 0; r < myRows.length; r++) {
                retVal += myRows[r].countZeros();
            }
            return retVal;
        }

        @Override
        protected void pivot(final IterationPoint iterationPoint) {

            final int row = iterationPoint.row;
            final int col = iterationPoint.col;

            final SparseArray<Double> pivotRow = myRows[row];
            double pivotRHS = myRHS.doubleValue(row);

            pivotRHS = this.scale(pivotRow, col, pivotRHS);
            myRHS.set(row, pivotRHS);

            this.doPivot(row, col, pivotRow, pivotRHS);

            this.update(iterationPoint);
        }

        @Override
        protected Array1D<Double> sliceConstraintsRHS() {
            return myRHS;
        }

        @Override
        protected Access1D<Double> sliceDualVariables() {
            final Array1D<Double> tmpSliceRange = myObjectiveWeights.sliceRange(this.countVariables(), this.countVariables() + this.countConstraints());
            return new Access1D<Double>() {

                public long count() {
                    return tmpSliceRange.count();
                }

                public double doubleValue(final long index) {
                    return -tmpSliceRange.doubleValue(index);
                }

                public Double get(final long index) {
                    return -tmpSliceRange.doubleValue(index);
                }

                @Override
                public String toString() {
                    return Access1D.toString(this);
                }

            };
        }

        @Override
        protected Access1D<Double> sliceTableauColumn(final int col) {
            if (col < this.countVariablesTotally()) {
                return new Access1D<Double>() {

                    public long count() {
                        return SparseTableau.this.countConstraints();
                    }

                    public double doubleValue(final long index) {
                        return myRows[(int) index].doubleValue(col);
                    }

                    public Double get(final long index) {
                        return myRows[(int) index].get(col);
                    }

                    @Override
                    public String toString() {
                        return Access1D.toString(this);
                    }

                };
            } else {
                return myRHS;
            }
        }

        @Override
        protected Access1D<Double> sliceTableauRow(final int row) {
            if (row < this.countConstraints()) {
                return myRows[row];
            } else if (row == this.countConstraints()) {
                return myObjectiveWeights;
            } else {
                return myPhase1Weights;
            }
        }

        @Override
        protected DenseTableau toDense() {
            return new DenseTableau(this);
        }

        /**
         * @return The phase 1 objective function value
         */
        double getInfeasibility() {
            return myInfeasibility;
        }

        @Override
        Mutate2D newConstraintsBody() {
            return new Mutate2D() {

                public void add(final long row, final long col, final Comparable<?> addend) {
                    this.add(row, col, NumberDefinition.doubleValue(addend));
                }

                public void add(final long row, final long col, final double addend) {
                    myRows[(int) row].add(col, addend);
                    myPhase1Weights.add(col, -addend);
                }

                public long countColumns() {
                    return SparseTableau.this.countVariables();
                }

                public long countRows() {
                    return SparseTableau.this.countConstraints();
                }

                public void set(final long row, final long col, final Comparable<?> value) {
                    this.set(row, col, NumberDefinition.doubleValue(value));
                }

                public void set(final long row, final long col, final double value) {
                    myRows[(int) row].set(col, value);
                    myPhase1Weights.add(col, -value);
                }

            };
        }

        @Override
        Mutate1D newConstraintsRHS() {
            return new Mutate1D() {

                public void add(final long index, final Comparable<?> addend) {
                    this.add(index, NumberDefinition.doubleValue(addend));
                }

                public void add(final long index, final double addend) {
                    myRows[(int) index].set(SparseTableau.this.countVariables() + index, ONE);
                    myRHS.add(index, addend);
                    myInfeasibility -= addend;
                }

                public long count() {
                    return SparseTableau.this.countConstraints();
                }

                public void set(final long index, final Comparable<?> value) {
                    this.set(index, NumberDefinition.doubleValue(value));
                }

                public void set(final long index, final double value) {
                    myRows[(int) index].set(SparseTableau.this.countVariables() + index, ONE);
                    myRHS.set(index, value);
                    myInfeasibility -= value;
                }

            };
        }

        @Override
        Mutate1D newObjective() {
            return new Mutate1D() {

                public void add(final long index, final Comparable<?> addend) {
                    this.add(index, NumberDefinition.doubleValue(addend));
                }

                public void add(final long index, final double addend) {
                    myObjectiveWeights.add(index, addend);
                }

                public long count() {
                    return SparseTableau.this.countVariables();
                }

                public void set(final long index, final Comparable<?> value) {
                    this.set(index, NumberDefinition.doubleValue(value));
                }

                public void set(final long index, final double value) {
                    myObjectiveWeights.set(index, value);
                }

            };
        }

        Primitive64Store transpose() {

            final Primitive64Store retVal = Primitive64Store.FACTORY.makeZero(this.countColumns(), this.countRows());

            for (int i = 0; i < myRows.length; i++) {
                for (final NonzeroView<Double> nz : myRows[i].nonzeros()) {
                    retVal.set(nz.index(), i, nz.doubleValue());
                }
            }
            retVal.fillColumn(myRows.length, myObjectiveWeights);
            retVal.fillColumn(myRows.length + 1, myPhase1Weights);

            retVal.fillRow(this.countVariables() + this.countConstraints(), myRHS);
            retVal.set(this.countVariables() + this.countConstraints(), this.countConstraints(), myValue);
            retVal.set(this.countVariables() + this.countConstraints(), this.countConstraints() + 1, myInfeasibility);

            return retVal;
        }

    }

    static final Array1D.Factory<Double> ARRAY1D_FACTORY = Array1D.factory(Primitive64Array.FACTORY);
    static final DenseArray.Factory<Double> DENSE_FACTORY = Primitive64Array.FACTORY;

    protected static SimplexTableau make(final int numberOfConstraints, final int numberOfProblemVariables, final int numberOfSlackVariables,
            final Optimisation.Options options) {

        final int numbRows = numberOfConstraints + 2;
        final int numbCols = numberOfProblemVariables + numberOfSlackVariables + numberOfConstraints + 1;
        final int totCount = numbRows * numbCols; //  Total number of elements in a dense tableau

        if (options.sparse == null) {

            // Max number of elements in CPU cache
            long maxCount = OjAlgoUtils.ENVIRONMENT.getCacheElements(JavaType.DOUBLE.memory());

            if ((totCount <= maxCount) || ((numberOfProblemVariables <= numberOfConstraints) && (totCount <= (2L * maxCount)))) {
                return new DenseTableau(numberOfConstraints, numberOfProblemVariables, numberOfSlackVariables);
            } else {
                return new SparseTableau(numberOfConstraints, numberOfProblemVariables, numberOfSlackVariables);
            }

        } else if (options.sparse) {

            return new SparseTableau(numberOfConstraints, numberOfProblemVariables, numberOfSlackVariables);

        } else {

            return new DenseTableau(numberOfConstraints, numberOfProblemVariables, numberOfSlackVariables);
        }
    }

    private final int[] myBasis;
    private transient Mutate2D myConstraintsBody = null;
    private transient Mutate1D myConstraintsRHS = null;
    private final int myNumberOfConstraints;
    private final int myNumberOfProblemVariables;
    private final int myNumberOfSlackVariables;
    private transient Mutate1D myObjective = null;
    private final IndexSelector mySelector;

    final boolean[] negative;

    protected SimplexTableau(final int numberOfConstraints, final int numberOfProblemVariables, final int numberOfSlackVariables) {

        super();

        myNumberOfConstraints = numberOfConstraints;
        myNumberOfProblemVariables = numberOfProblemVariables;
        myNumberOfSlackVariables = numberOfSlackVariables;

        mySelector = new IndexSelector(this.countVariables());
        myBasis = BasicArray.makeIncreasingRange(-numberOfConstraints, numberOfConstraints);

        negative = new boolean[numberOfConstraints];
    }

    protected final Mutate2D constraintsBody() {
        if (myConstraintsBody == null) {
            myConstraintsBody = this.newConstraintsBody();
        }
        return myConstraintsBody;
    }

    protected final Mutate1D constraintsRHS() {
        if (myConstraintsRHS == null) {
            myConstraintsRHS = this.newConstraintsRHS();
        }
        return myConstraintsRHS;
    }

    protected int countArtificialVariables() {
        return myNumberOfConstraints;
    }

    protected int countBasicArtificials() {
        int retVal = 0;
        final int tmpLength = myBasis.length;
        for (int i = 0; i < tmpLength; i++) {
            if (myBasis[i] < 0) {
                retVal++;
            }
        }
        return retVal;
    }

    protected final int countBasisDeficit() {
        return this.countConstraints() - mySelector.countIncluded();
    }

    protected int countConstraints() {
        return myNumberOfConstraints;
    }

    protected int countProblemVariables() {
        return myNumberOfProblemVariables;
    }

    protected int countSlackVariables() {
        return myNumberOfSlackVariables;
    }

    /**
     * problem + slack
     */
    protected int countVariables() {
        return myNumberOfProblemVariables + myNumberOfSlackVariables;
    }

    /**
     * problem + slack + artificial
     */
    protected int countVariablesTotally() {
        return myNumberOfProblemVariables + myNumberOfSlackVariables + myNumberOfConstraints;
    }

    protected boolean fixVariable(final int index, final double value) {

        int row = this.getBasisRowIndex(index);

        if (row < 0) {
            return false;
        }

        return false;
    }

    protected int getBasisColumnIndex(final int basisRowIndex) {
        return myBasis[basisRowIndex];
    }

    protected int getBasisRowIndex(final int basisColumnIndex) {
        return org.ojalgo.array.operation.IndexOf.indexOf(myBasis, basisColumnIndex);
    }

    protected final int[] getExcluded() {
        return mySelector.getExcluded();
    }

    protected final int[] getIncluded() {
        return mySelector.getIncluded();
    }

    protected abstract long getOvercapacity();

    protected boolean isBasicArtificials() {
        final int tmpLength = myBasis.length;
        for (int i = 0; i < tmpLength; i++) {
            if (myBasis[i] < 0) {
                return true;
            }
        }
        return false;
    }

    protected final Mutate1D objective() {
        if (myObjective == null) {
            myObjective = this.newObjective();
        }
        return myObjective;
    }

    protected abstract void pivot(IterationPoint iterationPoint);

    protected abstract Array1D<Double> sliceConstraintsRHS();

    /**
     * @return An array of the dual variable values (of the original problem, never phase 1).
     */
    protected abstract Access1D<Double> sliceDualVariables();

    protected abstract Access1D<Double> sliceTableauColumn(final int col);

    protected abstract Access1D<Double> sliceTableauRow(final int row);

    protected abstract DenseTableau toDense();

    protected void update(final IterationPoint point) {

        final int pivotRow = point.row;
        final int pivotCol = point.col;

        final int tmpOld = myBasis[pivotRow];
        if (tmpOld >= 0) {
            mySelector.exclude(tmpOld);
        }
        final int tmpNew = pivotCol;
        if (tmpNew >= 0) {
            mySelector.include(tmpNew);
        }
        myBasis[pivotRow] = pivotCol;
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

    int[] getBasis() {
        return myBasis.clone();
    }

    abstract Mutate2D newConstraintsBody();

    abstract Mutate1D newConstraintsRHS();

    abstract Mutate1D newObjective();

}
