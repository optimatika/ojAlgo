/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.core.type.context;

import java.text.Format;
import java.text.ParseException;

import org.ojalgo.core.ProgrammingError;
import org.ojalgo.core.netio.ASCII;

/**
 * Abstract base class for {@linkplain TypeContext} implementations backed by {@linkplain Format}.
 *
 * @author apete
 */
public abstract class FormatContext<T> implements TypeContext<T> {

    /**
     * Use 'Non-Breaking SPace' character instead of ardinary 'space' character.
     */
    public static final boolean NBSP = true;
    private boolean myConfigured = false;
    private final Format myFormat;

    FormatContext(final Format format) {

        super();

        ProgrammingError.throwIfNull(format);

        myFormat = (Format) format.clone();
    }

    /**
     * @see org.ojalgo.core.type.context.TypeContext#format(java.lang.Object)
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

    public Format getFormat() {
        return (Format) this.format().clone();
    }

    /**
     * @deprecated v49 Use {@link #withFormat(F)} instead
     */
    @Deprecated
    public final <G> TypeContext<G> newFormat(final Format format) {
        return this.withFormat(format);
    }

    /**
     * @see org.ojalgo.core.type.context.TypeContext#parse(CharSequence)
     */
    @Override
    @SuppressWarnings("unchecked")
    public final T parse(final CharSequence string) {

        if (string != null) {

            try {
                return (T) myFormat.parseObject(NBSP ? string.toString().replace(ASCII.NBSP, ASCII.SP) : string.toString());
            } catch (final ParseException anException) {
                return this.handleParseException(myFormat, string.toString());
            }

        } else {

            return null;
        }
    }

    public final <G> TypeContext<G> withFormat(final Format format) {
        return new GenericContext<>(this, format);
    }

    protected abstract void configureFormat(Format format, Object object);

    protected abstract String handleFormatException(Format format, Object object);

    protected abstract T handleParseException(Format format, String string);

    final Format format() {
        return myFormat;
    }

    final boolean isConfigured() {
        return myConfigured;
    }

}
