/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.array;

import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;

@Deprecated
public abstract class ArrayUtils {

    /**
     * @deprecated v44 Use {@link Raw1D#copyOf(double[])} instead
     */
    @Deprecated
    public static double[] copyOf(final double[] original) {
        return Raw1D.copyOf(original);
    }

    /**
     * @deprecated v44 Use {@link Raw1D#copyOf(float[])} instead
     */
    @Deprecated
    public static float[] copyOf(final float[] original) {
        return Raw1D.copyOf(original);
    }

    /**
     * @deprecated v44 Use {@link Raw1D#copyOf(int[])} instead
     */
    @Deprecated
    public static int[] copyOf(final int[] original) {
        return Raw1D.copyOf(original);
    }

    /**
     * @deprecated v44 Use {@link Raw1D#copyOf(long[])} instead
     */
    @Deprecated
    public static long[] copyOf(final long[] original) {
        return Raw1D.copyOf(original);
    }

    /**
     * @deprecated v44 Use {@link Raw1D#copyOf(T[])} instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> T[] copyOf(final T[] original) {
        return Raw1D.copyOf(original);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#exchangeColumns(double[][],int,int)} instead
     */
    @Deprecated
    public static void exchangeColumns(final double[][] target, final int columnA, final int columnB) {
        Raw2D.exchangeColumns(target, columnA, columnB);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#exchangeRows(double[][],int,int)} instead
     */
    @Deprecated
    public static void exchangeRows(final double[][] target, final int rowA, final int rowB) {
        Raw2D.exchangeRows(target, rowA, rowB);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillAll(double[][],double)} instead
     */
    @Deprecated
    public static void fillAll(final double[][] target, final double value) {
        Raw2D.fillAll(target, value);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillAll(double[][],DoubleSupplier)} instead
     */
    @Deprecated
    public static void fillAll(final double[][] target, final DoubleSupplier supplier) {
        Raw2D.fillAll(target, supplier);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillColumn(double[][],int,int,double)} instead
     */
    @Deprecated
    public static void fillColumn(final double[][] target, final int row, final int column, final double value) {
        Raw2D.fillColumn(target, row, column, value);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillColumn(double[][],int,int,DoubleSupplier)} instead
     */
    @Deprecated
    public static void fillColumn(final double[][] target, final int row, final int column, final DoubleSupplier supplier) {
        Raw2D.fillColumn(target, row, column, supplier);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillDiagonal(double[][],int,int,double)} instead
     */
    @Deprecated
    public static void fillDiagonal(final double[][] target, final int row, final int column, final double value) {
        Raw2D.fillDiagonal(target, row, column, value);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillDiagonal(double[][],int,int,DoubleSupplier)} instead
     */
    @Deprecated
    public static void fillDiagonal(final double[][] target, final int row, final int column, final DoubleSupplier supplier) {
        Raw2D.fillDiagonal(target, row, column, supplier);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillMatching(double[][],double,DoubleBinaryOperator,double[][])}
     *             instead
     */
    @Deprecated
    public static void fillMatching(final double[][] target, final double left, final DoubleBinaryOperator function, final double[][] right) {
        Raw2D.fillMatching(target, left, function, right);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillMatching(double[][],double[][],DoubleBinaryOperator,double)}
     *             instead
     */
    @Deprecated
    public static void fillMatching(final double[][] target, final double[][] left, final DoubleBinaryOperator function, final double right) {
        Raw2D.fillMatching(target, left, function, right);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillMatching(double[][],double[][],DoubleBinaryOperator,double[][])}
     *             instead
     */
    @Deprecated
    public static void fillMatching(final double[][] target, final double[][] left, final DoubleBinaryOperator function, final double[][] right) {
        Raw2D.fillMatching(target, left, function, right);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillRange(double[][],int,int,double)} instead
     */
    @Deprecated
    public static void fillRange(final double[][] target, final int first, final int limit, final double value) {
        Raw2D.fillRange(target, first, limit, value);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillRange(double[][],int,int,DoubleSupplier)} instead
     */
    @Deprecated
    public static void fillRange(final double[][] target, final int first, final int limit, final DoubleSupplier supplier) {
        Raw2D.fillRange(target, first, limit, supplier);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillRow(double[][],int,int,double)} instead
     */
    @Deprecated
    public static void fillRow(final double[][] target, final int row, final int column, final double value) {
        Raw2D.fillRow(target, row, column, value);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#fillRow(double[][],int,int,DoubleSupplier)} instead
     */
    @Deprecated
    public static void fillRow(final double[][] target, final int row, final int column, final DoubleSupplier supplier) {
        Raw2D.fillRow(target, row, column, supplier);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#modifyAll(double[][],DoubleUnaryOperator)} instead
     */
    @Deprecated
    public static void modifyAll(final double[][] target, final DoubleUnaryOperator function) {
        Raw2D.modifyAll(target, function);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#modifyColumn(double[][],int,int,DoubleUnaryOperator)} instead
     */
    @Deprecated
    public static void modifyColumn(final double[][] target, final int row, final int column, final DoubleUnaryOperator function) {
        Raw2D.modifyColumn(target, row, column, function);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#modifyDiagonal(double[][],int,int,DoubleUnaryOperator)} instead
     */
    @Deprecated
    public static void modifyDiagonal(final double[][] target, final int row, final int column, final DoubleUnaryOperator function) {
        Raw2D.modifyDiagonal(target, row, column, function);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#modifyRow(double[][],int,int,DoubleUnaryOperator)} instead
     */
    @Deprecated
    public static void modifyRow(final double[][] target, final int row, final int column, final DoubleUnaryOperator function) {
        Raw2D.modifyRow(target, row, column, function);
    }

    /**
     * @deprecated v44 Use {@link Raw1D#sort(long[],double[])} instead
     */
    @Deprecated
    public static void sort(final long[] primary, final double[] secondary) {
        Raw1D.sort(primary, secondary);
    }

    /**
     * @deprecated v44 Use {@link Raw1D#sort(long[],Object[])} instead
     */
    @Deprecated
    public static void sort(final long[] primary, final Object[] secondary) {
        Raw1D.sort(primary, secondary);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#visitAll(double[][],DoubleConsumer)} instead
     */
    @Deprecated
    public static void visitAll(final double[][] target, final DoubleConsumer visitor) {
        Raw2D.visitAll(target, visitor);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#visitColumn(double[][],int,int,DoubleConsumer)} instead
     */
    @Deprecated
    public static void visitColumn(final double[][] target, final int row, final int column, final DoubleConsumer visitor) {
        Raw2D.visitColumn(target, row, column, visitor);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#visitDiagonal(double[][],int,int,DoubleConsumer)} instead
     */
    @Deprecated
    public static void visitDiagonal(final double[][] target, final int row, final int column, final DoubleConsumer visitor) {
        Raw2D.visitDiagonal(target, row, column, visitor);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#visitRange(double[][],int,int,DoubleConsumer)} instead
     */
    @Deprecated
    public static void visitRange(final double[][] target, final int first, final int limit, final DoubleConsumer visitor) {
        Raw2D.visitRange(target, first, limit, visitor);
    }

    /**
     * @deprecated v44 Use {@link Raw2D#visitRow(double[][],int,int,DoubleConsumer)} instead
     */
    @Deprecated
    public static void visitRow(final double[][] target, final int row, final int column, final DoubleConsumer visitor) {
        Raw2D.visitRow(target, row, column, visitor);
    }

    /**
     * @deprecated v44 Use {@link Access1D#wrapAccess1D(double[])} instead
     */
    @Deprecated
    public static Access1D<Double> wrapAccess1D(final double[] target) {
        return Access1D.wrapAccess1D(target);
    }

    /**
     * @deprecated v44 Use {@link Access1D#wrapAccess1D(List<? extends N>)} instead
     */
    @Deprecated
    public static <N extends Number> Access1D<N> wrapAccess1D(final List<? extends N> target) {
        return Access1D.wrapAccess1D(target);
    }

    /**
     * @deprecated v44 Use {@link Access1D#wrapAccess1D(N[])} instead
     */
    @Deprecated
    public static <N extends Number> Access1D<N> wrapAccess1D(final N[] target) {
        return Access1D.wrapAccess1D(target);
    }

    /**
     * @deprecated v44 Use {@link Access2D#wrapAccess2D(double[][])} instead
     */
    @Deprecated
    public static Access2D<Double> wrapAccess2D(final double[][] target) {
        return Access2D.wrapAccess2D(target);
    }

    /**
     * @deprecated v44 Use {@link Access2D#wrapAccess2D(N[][])} instead
     */
    @Deprecated
    public static <N extends Number> Access2D<N> wrapAccess2D(final N[][] target) {
        return Access2D.wrapAccess2D(target);
    }

    private ArrayUtils() {
        super();
    }

}
