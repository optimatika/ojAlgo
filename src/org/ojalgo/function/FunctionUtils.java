/*
 * Copyright 1997-2019 Optimatika
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
package org.ojalgo.function;

import org.ojalgo.function.special.MissingMath;

/**
 * @deprecated v48
 */
@Deprecated
public abstract class FunctionUtils {

    /**
     * @deprecated v48 Use {@link UnaryFunction#isZeroModified(UnaryFunction<N>)} instead
     */
    @Deprecated
    public static <N extends Comparable<N>> boolean isZeroModified(final UnaryFunction<N> function) {
        return UnaryFunction.isZeroModified(function);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(double...)} instead
     */
    @Deprecated
    public static double max(final double... values) {
        return MissingMath.max(values);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(double,double)} instead
     */
    @Deprecated
    public static double max(final double a, final double b) {
        return MissingMath.max(a, b);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(double,double,double)} instead
     */
    @Deprecated
    public static double max(final double a, final double b, final double c) {
        return MissingMath.max(a, b, c);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(double,double,double,double)} instead
     */
    @Deprecated
    public static double max(final double a, final double b, final double c, final double d) {
        return MissingMath.max(a, b, c, d);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(int...)} instead
     */
    @Deprecated
    public static int max(final int... values) {
        return MissingMath.max(values);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(int,int)} instead
     */
    @Deprecated
    public static int max(final int a, final int b) {
        return MissingMath.max(a, b);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(int,int,int)} instead
     */
    @Deprecated
    public static int max(final int a, final int b, final int c) {
        return MissingMath.max(a, b, c);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(int,int,int,int)} instead
     */
    @Deprecated
    public static int max(final int a, final int b, final int c, final int d) {
        return MissingMath.max(a, b, c, d);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(long...)} instead
     */
    @Deprecated
    public static long max(final long... values) {
        return MissingMath.max(values);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(long,long)} instead
     */
    @Deprecated
    public static long max(final long a, final long b) {
        return MissingMath.max(a, b);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(long,long,long)} instead
     */
    @Deprecated
    public static long max(final long a, final long b, final long c) {
        return MissingMath.max(a, b, c);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#max(long,long,long,long)} instead
     */
    @Deprecated
    public static long max(final long a, final long b, final long c, final long d) {
        return MissingMath.max(a, b, c, d);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(double...)} instead
     */
    @Deprecated
    public static double min(final double... values) {
        return MissingMath.min(values);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(double,double)} instead
     */
    @Deprecated
    public static double min(final double a, final double b) {
        return MissingMath.min(a, b);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(double,double,double)} instead
     */
    @Deprecated
    public static double min(final double a, final double b, final double c) {
        return MissingMath.min(a, b, c);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(double,double,double,double)} instead
     */
    @Deprecated
    public static double min(final double a, final double b, final double c, final double d) {
        return MissingMath.min(a, b, c, d);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(int...)} instead
     */
    @Deprecated
    public static int min(final int... values) {
        return MissingMath.min(values);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(int,int)} instead
     */
    @Deprecated
    public static int min(final int a, final int b) {
        return MissingMath.min(a, b);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(int,int,int)} instead
     */
    @Deprecated
    public static int min(final int a, final int b, final int c) {
        return MissingMath.min(a, b, c);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(int,int,int,int)} instead
     */
    @Deprecated
    public static int min(final int a, final int b, final int c, final int d) {
        return MissingMath.min(a, b, c, d);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(long...)} instead
     */
    @Deprecated
    public static long min(final long... values) {
        return MissingMath.min(values);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(long,long)} instead
     */
    @Deprecated
    public static long min(final long a, final long b) {
        return MissingMath.min(a, b);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(long,long,long)} instead
     */
    @Deprecated
    public static long min(final long a, final long b, final long c) {
        return MissingMath.min(a, b, c);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#min(long,long,long,long)} instead
     */
    @Deprecated
    public static long min(final long a, final long b, final long c, final long d) {
        return MissingMath.min(a, b, c, d);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#norm(double...)} instead
     */
    @Deprecated
    public static double norm(final double... values) {
        return MissingMath.norm(values);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#norm(double,double)} instead
     */
    @Deprecated
    public static double norm(final double a, final double b) {
        return MissingMath.norm(a, b);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#norm(double,double,double)} instead
     */
    @Deprecated
    public static double norm(final double a, final double b, final double c) {
        return MissingMath.norm(a, b, c);
    }

    /**
     * @deprecated v48 Use {@link MissingMath#norm(double,double,double,double)} instead
     */
    @Deprecated
    public static double norm(final double a, final double b, final double c, final double d) {
        return MissingMath.norm(a, b, c, d);
    }

    private FunctionUtils() {
        super();
    }

}
