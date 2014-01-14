/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.mps;

/**
 * BoundType used with the BOUNDS section.
 * 
 *  type            meaning
 * ---------------------------------------------------
 *   LO    lower bound        b <= x (< +inf)
 *   UP    upper bound        (0 <=) x <= b
 *   FX    fixed variable     x = b
 *   FR    free variable      -inf < x < +inf
 *   MI    lower bound -inf   -inf < x (<= 0)
 *   PL    upper bound +inf   (0 <=) x < +inf
 *   BV    binary variable    x = 0 or 1
 *   LI    integer variable   b <= x (< +inf)
 *   UI    integer variable   (0 <=) x <= b
 *   SC    semi-cont variable x = 0 or l <= x <= b
 *         l is the lower bound on the variable
 *         If none set then defaults to 1
 *
 * @author apete
 */
public enum BoundType {

    BV(), FR(), FX(), LI(), LO(), MI(), PL(), SC(), UI(), UP();

}
