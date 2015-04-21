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

import org.ojalgo.ProgrammingError;
import org.ojalgo.type.format.BooleanFormat;

/**
 * BooleanContext
 * 
 * @author apete
 */
public final class BooleanContext extends FormatContext<Boolean> {

    private static final Format DEFAULT_FORMAT = new BooleanFormat();

    public BooleanContext() {
        super(DEFAULT_FORMAT);
    }

    private BooleanContext(final Format format) {
        super(format);
        ProgrammingError.throwForIllegalInvocation();
    }

    @Override
    public Boolean enforce(final Boolean object) {
        return object;
    }

    @Override
    protected void configureFormat(final Format format, final Object object) {

    }

    @Override
    protected String handleFormatException(final Format format, final Object object) {
        return "";
    }

    @Override
    protected Boolean handleParseException(final Format format, final String string) {
        return false;
    }
}
