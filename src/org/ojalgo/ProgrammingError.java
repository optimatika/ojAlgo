/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.BasicMatrix;

/**
 * Incorrect use of the API. The code needs to be changed. Typically execution can't continue. Is never
 * declared to be thrown, and should not be caught.
 *
 * @author apete
 */
public final class ProgrammingError extends RuntimeException implements EffectiveThrowable {

    /**
     * For hidden, not-to-be-used, constructors and methods.
     */
    public static void throwForIllegalInvocation() {
        throw new ProgrammingError("Don't use this method/constructor!");
    }

    public static void throwForTryingToModifyAnImmutableObject() {
        throw new ProgrammingError("This class is immutable!");
    }

    public static void throwForUnsupportedOptionalOperation() {
        throw new ProgrammingError("Unsupported optional operation!");
    }

    public static void throwIfMultiplicationNotPossible(final Access2D<?> aMtrxLeft, final Access2D<?> aMtrxRight) {

        if (aMtrxLeft.countColumns() != aMtrxRight.countRows()) {
            throw new ProgrammingError("The column dimension of the left matrix does not match the row dimension of the right matrix!");
        }
    }

    public static void throwIfNotEqualColumnDimensions(final Access2D<?> aMtrx1, final Access2D<?> aMtrx2) {

        if (aMtrx1.countColumns() != aMtrx2.countColumns()) {
            throw new ProgrammingError("Column dimensions are not equal!");
        }
    }

    public static void throwIfNotEqualDimensions(final Access2D<?> aMtrx1, final Access2D<?> aMtrx2) {

        ProgrammingError.throwIfNotEqualRowDimensions(aMtrx1, aMtrx2);

        ProgrammingError.throwIfNotEqualColumnDimensions(aMtrx1, aMtrx2);
    }

    public static void throwIfNotEqualRowDimensions(final Access2D<?> aMtrx1, final Access2D<?> aMtrx2) {

        if (aMtrx1.countRows() != aMtrx2.countRows()) {
            throw new ProgrammingError("Row dimensions are not equal!");
        }
    }

    public static void throwIfNotSquare(final BasicMatrix aMtrx) {

        if (aMtrx.countRows() != aMtrx.countColumns()) {
            throw new ProgrammingError("Matrix is not square!");
        }
    }

    ProgrammingError() {
        super();
    }

    public ProgrammingError(final String message) {
        super(message);
    }

    ProgrammingError(final String message, final Throwable cause) {
        super(message, cause);
    }

    ProgrammingError(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ProgrammingError(final Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        final String retVal = this.getClass().getSimpleName();
        final String tmpMessage = this.getLocalizedMessage();
        return (tmpMessage != null) ? (retVal + ": " + tmpMessage) : retVal;
    }

}
