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
package org.ojalgo.type.context.awt;

import java.awt.HeadlessException;
import java.awt.TextField;

import org.ojalgo.ProgrammingError;
import org.ojalgo.type.context.TypeContext;

/**
 * @author apete
 */
public class ContextTextField<T> extends TextField {

    private final TypeContext<T> myContext;

    public ContextTextField(final T aModelObject, final TypeContext<T> aContext) {

        super(aContext.format(aModelObject));

        myContext = aContext;
    }

    public ContextTextField(final TypeContext<T> aContext) {

        super();

        myContext = aContext;
    }

    @SuppressWarnings("unused")
    private ContextTextField() throws HeadlessException {

        super();

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextTextField(final int someColumns) throws HeadlessException {

        super(someColumns);

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextTextField(final String someText) throws HeadlessException {

        super(someText);

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextTextField(final String someText, final int someColumns) throws HeadlessException {

        super(someText, someColumns);

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    public final T getModelObject() {
        return myContext.parse(this.getText());
    }

    public final void setModelObject(final T aModelObject) {
        this.setText(myContext.format(aModelObject));
    }

}
