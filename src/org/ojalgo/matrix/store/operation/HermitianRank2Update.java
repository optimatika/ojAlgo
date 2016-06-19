/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.store.operation;

import java.math.BigDecimal;

import org.ojalgo.scalar.ComplexNumber;

/**
 * [A] -= ([a][b]<sup>c</sup>+[b][a]<sup>c</sup>) <br>
 * [A] is assumed to be hermitian (square symmetric) [A] = [A]<sup>C</sup>. <br>
 * <sup>C</sup> == conjugate transpose
 *
 * @author apete
 */
public final class HermitianRank2Update extends MatrixOperation {

    public static final HermitianRank2Update SETUP = new HermitianRank2Update();

    public static int THRESHOLD = 64;

    public static void invoke(final BigDecimal[] aData, final int aFirstCol, final int aColLimit, final BigDecimal[] aVector1, final BigDecimal[] aVector2) {

        final int tmpLength = aVector1.length; // Should be the same as aVector1.length and the "row-dim" of aData.

        BigDecimal tmpVal1j;
        BigDecimal tmpVal2j;

        int tmpIndex;
        for (int j = aFirstCol; j < aColLimit; j++) {

            tmpVal1j = aVector1[j];
            tmpVal2j = aVector2[j];

            tmpIndex = j + (j * tmpLength);
            for (int i = j; i < tmpLength; i++) {
                aData[tmpIndex] = aData[tmpIndex].subtract(aVector2[i].multiply(tmpVal1j).add(aVector1[i].multiply(tmpVal2j)));
                tmpIndex++;
            }
        }
    }

    public static void invoke(final ComplexNumber[] aData, final int aFirstCol, final int aColLimit, final ComplexNumber[] aVector1,
            final ComplexNumber[] aVector2) {

        final int tmpLength = aVector1.length; // Should be the same as aVector1.length and the "row-dim" of aData.

        ComplexNumber tmpVal1j;
        ComplexNumber tmpVal2j;

        int tmpIndex;
        for (int j = aFirstCol; j < aColLimit; j++) {

            tmpVal1j = aVector1[j].conjugate();
            tmpVal2j = aVector2[j].conjugate();

            tmpIndex = j + (j * tmpLength);
            for (int i = j; i < tmpLength; i++) {
                aData[tmpIndex] = aData[tmpIndex].subtract(aVector2[i].multiply(tmpVal1j).add(aVector1[i].multiply(tmpVal2j)));
                tmpIndex++;
            }
        }
    }

    public static void invoke(final double[] aData, final int aFirstCol, final int aColLimit, final double[] aVector1, final double[] aVector2) {

        final int tmpLength = aVector1.length; // Should be the same as aVector1.length and the "row-dim" of aData.

        double tmpVal1j;
        double tmpVal2j;

        int tmpIndex;
        for (int j = aFirstCol; j < aColLimit; j++) {

            tmpVal1j = aVector1[j];
            tmpVal2j = aVector2[j];

            tmpIndex = j + (j * tmpLength);
            for (int i = j; i < tmpLength; i++) {
                aData[tmpIndex++] -= ((aVector2[i] * tmpVal1j) + (aVector1[i] * tmpVal2j));
            }
        }
    }

    private HermitianRank2Update() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
