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
import java.text.Format;
import java.text.ParsePosition;

import org.ojalgo.type.context.BooleanContext;

/**
 * BooleanFormat doesn't do anything useful, but it was needed for {@linkplain BooleanContext} (that doesn't
 * do anything either).
 *
 * @author apete
 */
public final class BooleanFormat extends Format {

    public BooleanFormat() {
        super();
    }

    @Override
    public StringBuffer format(final Object anObject, final StringBuffer aBuffer, final FieldPosition aPosition) {

        if ((anObject instanceof Boolean) && ((Boolean) anObject).booleanValue()) {
            aBuffer.append(true);
        } else {
            aBuffer.append(false);
        }

        return aBuffer;
    }

    @Override
    public Boolean parseObject(final String aSource, final ParsePosition aPosition) {
        return Boolean.valueOf(aSource);
    }

}
