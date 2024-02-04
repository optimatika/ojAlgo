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
package org.ojalgo.type.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.ojalgo.type.context.BooleanContext;

/**
 * BooleanFormat doesn't do anything useful, but it was needed for {@linkplain BooleanContext} (that doesn't
 * do much either).
 *
 * @author apete
 */
public final class BooleanFormat extends Format {

    private static final long serialVersionUID = 1L;

    private final String myFalseValue;
    private final String myNullValue;
    private final String myTrueValue;

    public BooleanFormat() {
        this(Boolean.TRUE.toString(), Boolean.FALSE.toString());
    }

    public BooleanFormat(final String trueValue, final String falseValue) {
        this(trueValue, falseValue, "?");
    }

    public BooleanFormat(final String trueValue, final String falseValue, final String nullValue) {
        super();
        myTrueValue = trueValue;
        myFalseValue = falseValue;
        myNullValue = nullValue;
    }

    @Override
    public StringBuffer format(final Object object, final StringBuffer buffer, final FieldPosition position) {

        if ((object == null) || !(object instanceof Boolean)) {
            buffer.append(myNullValue);
        } else if (((Boolean) object).booleanValue()) {
            buffer.append(myTrueValue);
        } else {
            buffer.append(myFalseValue);
        }

        return buffer;
    }

    @Override
    public Boolean parseObject(final String source, final ParsePosition position) {

        if (myTrueValue.equals(source)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

}
