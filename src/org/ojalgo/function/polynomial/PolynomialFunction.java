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
package org.ojalgo.function.polynomial;

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.function.BasicFunction.Differentiable;
import org.ojalgo.function.BasicFunction.Integratable;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.series.NumberSeries;

public interface PolynomialFunction<N extends Number> extends UnaryFunction<N>, Access1D<N>, Differentiable<N, PolynomialFunction<N>>,
        Integratable<N, PolynomialFunction<N>> {

    int degree();

    void estimate(Access1D<?> x, Access1D<?> y);

    void estimate(List<? extends Number> x, List<? extends Number> y);

    void estimate(NumberSeries<?> samples);

    void set(Access1D<?> coefficients);

    void set(final int aPower, final double aCoefficient);

    void set(final int aPower, final N aCoefficient);

}
