/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.algebra;

/**
 * @author apete
 */
public interface NormedVectorSpace<V, F extends Number> extends VectorSpace<V, F> {

    /**
     * <p>
     * <b>This method will (most likely) be moved to some other interface in the future! Just have to figure
     * out where it fits...</b>
     * </p>
     * <p>
     * The conjugate transpose of a matrix and/or the conjugate of a scalar/field like ComplexNumber or
     * Quaternion.
     * </p>
     * <p>
     * The conjugate transpose of a real matrix is simply its transpose.
     * </p>
     */
    V conjugate();

    /**
     * @return true if this is small compared to the magnitude of the input reference value.
     */
    boolean isSmall(double comparedTo);

    /**
     * <code>this == this.signum().multiply(this.norm())</code>
     */
    double norm();

    /**
     * <code>this == this.signum().multiply(this.norm())</code>
     *
     * @return A unit "vector"
     */
    V signum();

}
