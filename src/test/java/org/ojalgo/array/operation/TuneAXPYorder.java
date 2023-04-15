/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.array.operation;

import java.util.Random;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

@State(Scope.Benchmark)
public class TuneAXPYorder {

    private static final Random RANDOM = new Random();

    public static void invokeBIS(final double[] y, final int basey, final int incy, final double a, final double[] x, final int basex, final int incx,
            final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            y[basey + i * incy] += a * x[basex + i * incx]; // y += a*x
        }
    }

    public static void invokeBSI(final double[] y, final int basey, final int incy, final double a, final double[] x, final int basex, final int incx,
            final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            y[basey + incy * i] += a * x[basex + incx * i]; // y += a*x
        }
    }

    public static void invokeISB(final double[] y, final int basey, final int incy, final double a, final double[] x, final int basex, final int incx,
            final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            y[i * incy + basey] += a * x[i * incx + basex]; // y += a*x
        }
    }

    public static void invokeSIB(final double[] y, final int basey, final int incy, final double a, final double[] x, final int basex, final int incx,
            final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            y[incy * i + basey] += a * x[incx * i + basex]; // y += a*x
        }
    }

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(TuneAXPYorder.class);
    }

    double[] mat;
    double a;
    int row;
    int col;

    @Param({ "100", "200", "500", "1000" })
    public int dim;

    public double[] bis() {

        TuneAXPYorder.invokeBIS(mat, row, dim, a, mat, dim * col, 1, 0, dim);

        return mat;
    }

    public double[] bsi() {

        TuneAXPYorder.invokeBSI(mat, row, dim, a, mat, dim * col, 1, 0, dim);

        return mat;
    }

    public double[] isb() {

        TuneAXPYorder.invokeISB(mat, row, dim, a, mat, dim * col, 1, 0, dim);

        return mat;
    }

    @Benchmark
    public double raw1() {

        double retVal = 0.0;

        for (int i = 0; i < mat.length; i++) {
            retVal += mat[i] * mat[i];
        }

        return retVal;
    }

    @Benchmark
    public double raw2() {

        double retVal = 0.0;

        final int limit = mat.length;
        final int base = 0;
        final int stride = 1;

        for (int i = 0; i < limit; i++) {
            retVal += mat[base + stride * i] * mat[base + stride * i];
        }

        return retVal;
    }

    @Setup
    public void setup() {

        mat = new double[dim * dim];
        for (int i = 0; i < mat.length; i++) {
            mat[i] = RANDOM.nextDouble();
        }

        a = RANDOM.nextDouble();

        row = Uniform.randomInteger(dim);
        col = Uniform.randomInteger(dim);

    }

    public double[] sib() {

        TuneAXPYorder.invokeSIB(mat, row, dim, a, mat, dim * col, 1, 0, dim);

        return mat;
    }

}
