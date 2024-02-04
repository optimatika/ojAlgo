/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.scalar.Scalar;

/**
 * [A] -= ([a][b]<sup>c</sup>+[b][a]<sup>c</sup>) <br>
 * [A] is assumed to be hermitian (square symmetric) [A] = [A]<sup>C</sup>. <br>
 * <sup>C</sup> == conjugate transpose
 *
 * @author apete
 */
public abstract class HermitianRank2Update implements ArrayOperation {

    public static int THRESHOLD = 256;

    //    public static void invoke( ComplexNumber[] data,  int firstColumn,  int columnLimit,  ComplexNumber[] vector1,
    //             ComplexNumber[] vector2) {
    //
    //         int structure = vector1.length;
    //
    //        ComplexNumber tmpVal1j;
    //        ComplexNumber tmpVal2j;
    //
    //        int tmpIndex;
    //        for (int j = firstColumn; j < columnLimit; j++) {
    //
    //            tmpVal1j = vector1[j].conjugate();
    //            tmpVal2j = vector2[j].conjugate();
    //
    //            tmpIndex = j + (j * structure);
    //            for (int i = j; i < structure; i++) {
    //                data[tmpIndex] = data[tmpIndex].subtract(vector2[i].multiply(tmpVal1j).add(vector1[i].multiply(tmpVal2j)));
    //                tmpIndex++;
    //            }
    //        }
    //    }

    public static void invoke(final double[] data, final int firstColumn, final int columnLimit, final double[] vector1, final double[] vector2) {

        int structure = vector1.length;

        double tmpVal1j;
        double tmpVal2j;

        int tmpIndex;
        for (int j = firstColumn; j < columnLimit; j++) {

            tmpVal1j = vector1[j];
            tmpVal2j = vector2[j];

            tmpIndex = j + j * structure;
            for (int i = j; i < structure; i++) {
                data[tmpIndex++] -= vector2[i] * tmpVal1j + vector1[i] * tmpVal2j;
            }
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int firstColumn, final int columnLimit, final N[] vector1, final N[] vector2) {

        int structure = vector1.length;

        Scalar<N> tmpVal1j;
        Scalar<N> tmpVal2j;

        int tmpIndex;
        for (int j = firstColumn; j < columnLimit; j++) {

            tmpVal1j = vector1[j].conjugate();
            tmpVal2j = vector2[j].conjugate();

            tmpIndex = j + j * structure;
            for (int i = j; i < structure; i++) {
                data[tmpIndex] = data[tmpIndex].subtract(vector2[i].multiply(tmpVal1j).add(vector1[i].multiply(tmpVal2j))).get();
                tmpIndex++;
            }
        }
    }

}
