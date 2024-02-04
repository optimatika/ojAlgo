/*
 * Copyright 1997-2024 Optimatika
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

import java.util.Objects;

import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.TypeUtils;

/**
 * Incorrect use of the API. The code needs to be changed. Typically execution can't continue. Is never
 * declared to be thrown, and should not be caught.
 *
 * @author apete
 */
public class ProgrammingError extends RuntimeException implements EffectiveThrowable {

    private static final long serialVersionUID = 1L;

    /**
     * For hidden, not-to-be-used, constructors and methods.
     */
    public static void throwForIllegalInvocation() {
        throw new ProgrammingError("Don't use this method/constructor!");
    }

    public static void throwForMultiplicationNotPossible() {
        throw new ProgrammingError("The column dimension of the left matrix does not match the row dimension of the right matrix!");
    }

    public static void throwForTryingToModifyAnImmutableObject() {
        throw new ProgrammingError("This class is immutable!");
    }

    public static void throwForUnsupportedOptionalOperation() {
        throw new UnsupportedOperationException();
    }

    public static void throwIfMultiplicationNotPossible(final Access2D<?> left, final Access2D<?> right) {
        if (left.countColumns() != right.countRows()) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }
    }

    public static void throwIfNotEqualColumnDimensions(final Access2D<?> mtrx1, final Access2D<?> mtrx2) {
        if (mtrx1.countColumns() != mtrx2.countColumns()) {
            throw new ProgrammingError("Column dimensions are not equal!");
        }
    }

    public static void throwIfNotEqualDimensions(final Access2D<?> mtrx1, final Access2D<?> mtrx2) {
        ProgrammingError.throwIfNotEqualRowDimensions(mtrx1, mtrx2);
        ProgrammingError.throwIfNotEqualColumnDimensions(mtrx1, mtrx2);
    }

    public static void throwIfNotEqualRowDimensions(final Structure2D mtrx1, final Structure1D mtrx2) {
        if (mtrx2 instanceof Structure2D) {
            if (mtrx1.countRows() != ((Structure2D) mtrx2).countRows()) {
                throw new ProgrammingError("Row dimensions are not equal!");
            }
        } else if (mtrx1.countRows() != mtrx2.count()) {
            throw new ProgrammingError("Row dimensions are not equal!");
        }
    }

    public static void throwIfNotSquare(final Structure2D mtrx) {
        if (mtrx.countRows() != mtrx.countColumns()) {
            throw new ProgrammingError("Matrix is not square!");
        }
    }

    public static void throwIfNull(final Object obj) {
        Objects.requireNonNull(obj);
    }

    public static void throwIfNull(final Object... objs) {
        for (int i = 0; i < objs.length; i++) {
            Objects.requireNonNull(objs[i]);
        }
    }

    public static void throwIfNull(final Object obj1, final Object obj2) {
        Objects.requireNonNull(obj1);
        Objects.requireNonNull(obj2);
    }

    public static void throwIfNull(final Object obj1, final Object obj2, final Object obj3) {
        Objects.requireNonNull(obj1);
        Objects.requireNonNull(obj2);
        Objects.requireNonNull(obj3);
    }

    public static void throwWithMessage(final String messagePattern, final Object... args) {
        throw new ProgrammingError(TypeUtils.format(messagePattern, args));
    }

    public ProgrammingError(final String message) {
        super(message);
    }

    public ProgrammingError(final Throwable cause) {
        super(cause);
    }

    ProgrammingError() {
        super();
    }

    ProgrammingError(final String message, final Throwable cause) {
        super(message, cause);
    }

    ProgrammingError(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString() {
        final String retVal = this.getClass().getSimpleName();
        final String tmpMessage = this.getLocalizedMessage();
        return tmpMessage != null ? retVal + ": " + tmpMessage : retVal;
    }

}
