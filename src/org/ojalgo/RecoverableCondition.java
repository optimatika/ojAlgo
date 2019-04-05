/*
 * Copyright 1997-2019 Optimatika
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
package org.ojalgo;

import org.ojalgo.type.TypeUtils;

/**
 * Something that potentially could go wrong, actually did go wrong. The API user is expected to recover and
 * continue execution. Is always declared to be thrown, and must be caught.
 *
 * @author apete
 */
public final class RecoverableCondition extends Exception implements EffectiveThrowable {

    public static RecoverableCondition newEquationSystemNotSolvable() {
        return new RecoverableCondition("Equation System Not Solvable!");
    }

    public static RecoverableCondition newFailedToParseString(final String stringToParse, final Class<?> classToInstantiate) {
        return new RecoverableCondition(TypeUtils.format("Failed to parse \"{}\" to a {}!", String.valueOf(stringToParse),
                classToInstantiate != null ? classToInstantiate.getName() : "unspecified type"));
    }

    public static RecoverableCondition newMatrixNotInvertible() {
        return new RecoverableCondition("Matrix Not Invertible!");
    }

    public RecoverableCondition(final String message) {
        super(message);
    }

    RecoverableCondition() {
        super();
    }

    RecoverableCondition(final String message, final Throwable cause) {
        super(message, cause);
    }

    RecoverableCondition(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    RecoverableCondition(final Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        final String retVal = this.getClass().getSimpleName();
        final String tmpMessage = this.getLocalizedMessage();
        return (tmpMessage != null) ? (retVal + ": " + tmpMessage) : retVal;
    }

}
