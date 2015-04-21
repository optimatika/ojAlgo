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
package org.ojalgo.type.context;

import java.text.Format;

/**
 * @author apete
 */
public final class GenericContext<T> extends FormatContext<T> {

    private final TypeContext<T> myDelegate;

    public GenericContext(final Format format) {

        super(format);

        myDelegate = null;
    }

    @SuppressWarnings("unchecked")
    GenericContext(final TypeContext<?> delegate, final Format format) {

        super(format);

        myDelegate = (TypeContext<T>) delegate;
    }

    public T enforce(final T object) {
        if (myDelegate != null) {
            return myDelegate.enforce(object);
        } else {
            return this.parse(this.format(object));
        }
    }

    @Override
    protected void configureFormat(final Format format, final Object object) {
    }

    @Override
    protected String handleFormatException(final Format format, final Object object) {
        return myDelegate.format(object);
    }

    @Override
    protected T handleParseException(final Format format, final String string) {
        return myDelegate.parse(string);
    }

}
