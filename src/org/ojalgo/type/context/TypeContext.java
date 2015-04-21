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
 * A type context provides two basic services:
 * <ol>
 * <li>It enforces some sort of rule/limit regarding size, accuracy or similar. This feature is useful when
 * writing/reading data to/from a database where attributes are often very specifically typed. "enforcing" is
 * typically a one-way operation that cannot be undone.</li>
 * <li>It translates back and forth between some specific type and {@linkplain String} - essentially a
 * {@linkplain Format}.</li>
 * </ol>
 * 
 * @author apete
 */
public interface TypeContext<T> {

    /**
     * Will force the object to conform to the context's specification.
     * 
     * @param object
     * @return
     */
    public abstract T enforce(T object);

    public abstract String format(Object object);

    public abstract T parse(String string);

}
