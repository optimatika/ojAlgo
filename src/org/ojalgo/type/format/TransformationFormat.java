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
package org.ojalgo.type.format;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.ojalgo.function.UnaryFunction;

public class TransformationFormat<N extends Number> extends NumberFormat {

    private final UnaryFunction<N> myTransfoFunc;
    private final UnaryFunction<N> myInverseFunc;
    private final NumberFormat myFormat;

    public TransformationFormat(final UnaryFunction<N> transformer, final NumberFormat format, final UnaryFunction<N> inverse) {

        super();

        myTransfoFunc = transformer;
        myFormat = format;
        myInverseFunc = inverse;
    }

    public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
        return myFormat.format(myTransfoFunc.invoke(number), toAppendTo, pos);
    }

    public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
        return myFormat.format(myTransfoFunc.invoke(number), toAppendTo, pos);
    }

    @SuppressWarnings("unchecked")
    @Override
    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
        return myFormat.format(myTransfoFunc.invoke((N) obj), toAppendTo, pos);
    }

    public Number parse(final String source, final ParsePosition parsePosition) {
        return myInverseFunc.invoke((N) myFormat.parseObject(source, parsePosition));
    }

}
