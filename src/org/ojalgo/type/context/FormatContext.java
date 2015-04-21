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

import java.io.Serializable;
import java.text.Format;
import java.text.ParseException;

import org.ojalgo.ProgrammingError;
import org.ojalgo.netio.ASCII;

/**
 * Abstract base class for {@linkplain TypeContext} implementations backed by {@linkplain Format}.
 *
 * @author apete
 */
abstract class FormatContext<T> implements TypeContext<T>, Serializable {

    /**
     * Use 'Non-Breaking SPace' character instead of ardinary 'space' character.
     */
    public static final boolean NBSP = true;
    private boolean myConfigured = false;
    private Format myFormat;

    @SuppressWarnings("unused")
    private FormatContext() {
        this((Format) null);
    }

    FormatContext(final Format format) {

        super();

        ProgrammingError.throwIfNull(format);

        myFormat = (Format) format.clone();
    }

    /**
     * @see org.ojalgo.type.context.TypeContext#format(java.lang.Object)
     */
    @Override
    public final String format(final Object object) {

        if (object != null) {

            try {

                if (!myConfigured) {
                    this.configureFormat(myFormat, object);
                    myConfigured = true;
                }

                if (NBSP) {
                    return myFormat.format(object).replace(ASCII.SP, ASCII.NBSP);
                } else {
                    return myFormat.format(object);
                }

            } catch (final IllegalArgumentException exception) {

                return this.handleFormatException(myFormat, object);
            }

        } else {

            return null;
        }
    }

    public final Format getFormat() {
        return (Format) myFormat.clone();
    }

    public final <G> TypeContext<G> newFormat(final Format format) {
        return new GenericContext<>(this, format);
    }

    /**
     * @see org.ojalgo.type.context.TypeContext#parse(java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public final T parse(final String string) {

        if (string != null) {

            try {
                return (T) myFormat.parseObject(NBSP ? string.replace(ASCII.NBSP, ASCII.SP) : string);
            } catch (final ParseException anException) {
                return this.handleParseException(myFormat, string);
            }

        } else {

            return null;
        }
    }

    protected abstract void configureFormat(Format format, Object object);

    protected abstract String handleFormatException(Format format, Object object);

    protected abstract T handleParseException(Format format, String string);

}
