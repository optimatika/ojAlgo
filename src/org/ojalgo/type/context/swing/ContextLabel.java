/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.type.context.swing;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.ojalgo.ProgrammingError;
import org.ojalgo.type.context.TypeContext;

/**
 * 
 * @author apete
 */
public class ContextLabel<T> extends JLabel {

    private final TypeContext<T> myContext;

    public ContextLabel(final T aModelObject, final TypeContext<T> aContext) {

        super(aContext.format(aModelObject));

        myContext = aContext;
    }

    public ContextLabel(final TypeContext<T> aContext) {

        super();

        myContext = aContext;
    }

    @SuppressWarnings("unused")
    private ContextLabel() {

        super();

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextLabel(final Icon someImage) {

        super(someImage);

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextLabel(final Icon someImage, final int someHorizontalAlignment) {

        super(someImage, someHorizontalAlignment);

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextLabel(final String someText) {

        super(someText);

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextLabel(final String someText, final Icon someIcon, final int someHorizontalAlignment) {

        super(someText, someIcon, someHorizontalAlignment);

        myContext = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private ContextLabel(final String someText, final int someHorizontalAlignment) {

        super(someText, someHorizontalAlignment);

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
