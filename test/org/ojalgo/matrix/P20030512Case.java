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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * The problem was/is to calculate a numerically correct (6 decimals) inverse.
 *
 * @author apete
 * @see org.ojalgo.matrix.P20030422Case
 */
public class P20030512Case extends BasicMatrixTest {

    public static RationalMatrix getProblematic() {
        final RationalMatrix tmpMtrx = RationalMatrix.FACTORY.rows(new double[][] {
                { -0.9739496281920735, 0.13212842225762753, -0.009493226825028579, 0.05293424713580207, -0.06924760059060892, 0.015657944731764042,
                        -0.008564346745847575, 0.004549185362729688 },
                { -0.006968800104298036, -0.8297418413337506, -0.0362355854907016, 0.16177736191417533, -0.2100891795366892, 0.047384677993178616,
                        -0.025882286895646086, 0.013745556592708215 },
                { 1.4340062938542994E-4, 0.006439566493351905, -0.9984450518018704, -0.01672049883731471, 0.021092888666976884, -0.004711245310661291,
                        0.0025599032294949626, -0.0013585613015492993 },
                { -3.622792387280378E-5, -0.0014079037419939427, 7.516302232185491E-4, -0.9556877762343504, -0.16949291959105728, 0.027512985877230767,
                        -0.013046496305597954, 0.006810504406440988 },
                { 2.0250340119662656E-5, 7.831989621712412E-4, -4.0564464234332884E-4, 0.058420413927192494, -0.9102354879995057, -0.07415158327826854,
                        0.02334549865104649, -0.011573691137110015 },
                { -2.6229700444995062E-6, -1.0131708540961972E-4, 5.206744734581627E-5, -0.0061256425235909915, 0.031006541861310723, -0.9932086467025747,
                        -0.006143996013045335, 0.007870602318658423 },
                { 2.229928791772293E-7, 8.605627003794518E-6, -4.397793602224121E-6, 4.5796561576393096E-4, -0.0017021220200960682, 0.002139079626942216,
                        -0.9466510330795804, 0.21994580987160817 },
                { 4.2787387020086915E-8, 1.6510244524754327E-6, -8.431008420700906E-7, 8.524039438893604E-5, -3.878467110438371E-4, -0.00423012863453722,
                        -0.22206406664227266, 0.05162429369962791 } });
        return tmpMtrx.enforce(DEFINITION);
    }

    @BeforeEach
    @Override
    public void setUp() {

        DEFINITION = new NumberContext(7, 12);
        EVALUATION = new NumberContext(5, 6);

        myBigAA = P20030512Case.getProblematic();
        myBigAX = BasicMatrixTest.getIdentity(myBigAA.countColumns(), myBigAA.countColumns(), DEFINITION);
        myBigAB = myBigAA;

        myBigI = BasicMatrixTest.getIdentity(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);
        myBigSafe = BasicMatrixTest.getSafe(myBigAA.countRows(), myBigAA.countColumns(), DEFINITION);

        super.setUp();
    }

    @Test
    public void testProblem() {

        myExpMtrx = P20030512Case.getProblematic();
        myActMtrx = myExpMtrx.invert().invert();

        // RationalMatrix can do this, but not JamaMatrix and/or JampackMatrix
        TestUtils.assertEquals(myExpMtrx, myActMtrx, EVALUATION);
    }

}
