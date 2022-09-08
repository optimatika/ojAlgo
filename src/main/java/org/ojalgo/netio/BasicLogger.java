/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.netio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import org.ojalgo.matrix.ComplexMatrix;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * BasicLogger is not meant to replace any other logging library. It is primarily used for debugging during
 * development. ojAlgo has zero dependencies, and does not force any specific logging framework on you. But,
 * that meant we had to create something a little better than plain {@link System#out} for internal use.
 * <ul>
 * <li>If you want to redirect whatever ojAlgo outputs then set {@link BasicLogger#DEBUG} and
 * {@link BasicLogger#ERROR} to something suitable. {@linkplain BasicLogger.Printer} is an interface so it
 * should be possible to create some implementation that wraps your logging system. ojAlgo supplies 3
 * implementations of that interface.</li>
 * <li>ojAlgo typically doesn't do much logging. There's really not much to redirect. The main/only area where
 * BasicLogger is used is for debugging the various optimisation solvers. This is not intended to be "on" in
 * production.</li>
 * <li>The logging you need, you do in your code. ojAlgo's main contribution here is to have useful toString()
 * methods.</li>
 * <li>If you want to stop any/all possible output from ojAlgo then set {@link BasicLogger#DEBUG} and
 * {@link BasicLogger#ERROR} to null.</li>
 * </ul>
 *
 * @author apete
 */
public interface BasicLogger {

    public static final class BasicWriter extends Writer implements BasicLogger {

        private final PrintWriter myPrintWriter;

        public BasicWriter(final OutputStream out) {
            super();
            myPrintWriter = new PrintWriter(out, true);
        }

        public BasicWriter(final Writer out) {
            super();
            myPrintWriter = new PrintWriter(out, true);
        }

        public Optional<Writer> asWriter() {
            return Optional.of(this);
        }

        @Override
        public void close() throws IOException {
            myPrintWriter.close();
        }

        @Override
        public void flush() throws IOException {
            myPrintWriter.flush();
        }

        public void print(final boolean value) {
            myPrintWriter.print(value);
        }

        public void print(final byte value) {
            myPrintWriter.print(value);
        }

        public void print(final char value) {
            myPrintWriter.print(value);
        }

        public void print(final double value) {
            myPrintWriter.print(value);
        }

        public void print(final float value) {
            myPrintWriter.print(value);
        }

        public void print(final int value) {
            myPrintWriter.print(value);
        }

        public void print(final long value) {
            myPrintWriter.print(value);
        }

        public void print(final Object object) {
            myPrintWriter.append(object.toString());
        }

        public void print(final short value) {
            myPrintWriter.print(value);
        }

        public void print(final Throwable throwable) {
            throwable.printStackTrace(myPrintWriter);
        }

        public void printf(final String format, final Object... args) {
            myPrintWriter.printf(format, args);
            myPrintWriter.println();
        }

        public void println() {
            myPrintWriter.println();
        }

        public void println(final Object object) {
            myPrintWriter.println(object);
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            myPrintWriter.write(cbuf, off, len);
        }

    }

    /**
     * Temporarily store data/text. The buffer can be cleared and/or flushed to some receiver.
     *
     * @author apete
     */
    public interface Buffer {

        void clear();

        void flush(Appendable receiver);

        void flush(BasicLogger receiver);

    }

    class NotNull {

        static void columns(final BasicLogger appender, final int width, final Object... columns) {
            if (appender != null) {
                appender.columns(width, columns);
            }
        }

        static void println(final BasicLogger appender) {
            if (appender != null) {
                appender.println();
            }
        }

        static void println(final BasicLogger appender, final int tabs, final String messagePattern, final Object... arguments) {
            if (appender != null) {
                appender.println(tabs, messagePattern, arguments);
            }
        }

        static void println(final BasicLogger appender, final Object message) {
            if (appender != null) {
                appender.println(message);
            }
        }

        static void println(final BasicLogger appender, final String messagePattern, final Object... arguments) {
            if (appender != null) {
                appender.println(messagePattern, arguments);
            }
        }

        static void println(final BasicLogger appender, final Throwable throwable, final String messagePattern, final Object... arguments) {
            if (appender != null) {
                appender.println(throwable, messagePattern, arguments);
            }
        }

        static void printmtrx(final BasicLogger appender, final String message, final Access2D<?> matrix, final NumberContext context) {
            if (message != null) {
                appender.printmtrx(message, matrix, context);
            }
        }

    }

    public interface Printable {

        void print(BasicLogger receiver);

    }

    class PrivateDetails {

        static final NumberContext MATRIX_ELEMENT_CONTEXT = NumberContext.ofScale(6);

        private static void printmtrx(final BasicLogger appender, final Access2D<?> matrix, final NumberContext context, final boolean plain) {

            final int tmpRowDim = (int) matrix.countRows();
            final int tmpColDim = (int) matrix.countColumns();

            final String[][] tmpElements = new String[tmpRowDim][tmpColDim];

            int tmpWidth = 0;
            Comparable<?> tmpElementNumber;
            String tmpElementString;
            for (int j = 0; j < tmpColDim; j++) {
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpElementNumber = matrix.get(i, j);
                    tmpElementString = PrivateDetails.toString(tmpElementNumber, context, plain);
                    tmpWidth = Math.max(tmpWidth, tmpElementString.length());
                    tmpElements[i][j] = tmpElementString;
                }
            }
            tmpWidth++;

            int tmpPadding;
            for (int i = 0; i < tmpRowDim; i++) {
                for (int j = 0; j < tmpColDim; j++) {
                    tmpElementString = tmpElements[i][j];
                    tmpPadding = tmpWidth - tmpElementString.length();
                    for (int p = 0; p < tmpPadding; p++) {
                        appender.print(ASCII.SP);
                    }
                    appender.print(tmpElementString);
                }
                appender.println();
            }

        }

        private static String toString(final Comparable<?> number, final NumberContext context, final boolean plain) {
            if (plain) {
                if (number instanceof Scalar<?>) {
                    return ((Scalar<?>) number).toPlainString(context);
                }
                return context.enforce(BigDecimal.valueOf(NumberDefinition.doubleValue(number))).toPlainString();
            }
            if (number instanceof Scalar<?>) {
                return ((Scalar<?>) number).toString(context);
            }
            return context.enforce(BigDecimal.valueOf(NumberDefinition.doubleValue(number))).toString();
        }

        static void printmtrx(final BasicLogger appender, final String message, final Access2D<?> matrix, final NumberContext context) {
            appender.println(message);
            if (matrix.count() > 0L) {
                if (matrix instanceof ComplexMatrix || matrix.get(0, 0) instanceof ComplexNumber) {
                    PrivateDetails.printmtrx(appender, matrix, context, false);
                } else {
                    PrivateDetails.printmtrx(appender, matrix, context, true);
                }
            }
        }

    }

    BasicLogger DEBUG = new BasicWriter(System.out);

    BasicLogger ERROR = new BasicWriter(System.err);

    BasicLogger NULL = new BasicLogger() {

        public Optional<Writer> asWriter() {
            return Optional.empty();
        }

        public void print(final boolean value) {
        }

        public void print(final byte value) {
        }

        public void print(final char value) {
        }

        public void print(final double value) {
        }

        public void print(final float value) {
        }

        public void print(final int value) {
        }

        public void print(final long value) {
        }

        public void print(final Object object) {
        }

        public void print(final short value) {
        }

        public void print(final Throwable throwable) {
        }

        public void printf(final String format, final Object... args) {
        }

        public void println() {
        }

    };

    static void debug() {
        NotNull.println(DEBUG);
    }

    static void debug(final int width, final Object... columns) {
        NotNull.columns(DEBUG, width, columns);
    }

    static void debug(final int tabs, final String message, final Object... arguments) {
        NotNull.println(DEBUG, tabs, message, arguments);
    }

    static void debug(final Object message) {
        NotNull.println(DEBUG, message);
    }

    static void debug(final String message, final Access2D<?> matrix) {
        NotNull.printmtrx(DEBUG, message, matrix, PrivateDetails.MATRIX_ELEMENT_CONTEXT);
    }

    static void debug(final String message, final Access2D<?> matrix, final NumberContext context) {
        NotNull.printmtrx(DEBUG, message, matrix, context);
    }

    static void debug(final String message, final Object... arguments) {
        NotNull.println(DEBUG, message, arguments);
    }

    static void debug(final Throwable throwable, final String message, final Object... arguments) {
        NotNull.println(DEBUG, throwable, message, arguments);
    }

    static void error() {
        NotNull.println(ERROR);
    }

    static void error(final int width, final Object... columns) {
        NotNull.columns(ERROR, width, columns);
    }

    static void error(final int tabs, final String message, final Object... arguments) {
        NotNull.println(ERROR, tabs, message, arguments);
    }

    static void error(final Object message) {
        NotNull.println(ERROR, message);
    }

    static void error(final String message, final Access2D<?> matrix) {
        NotNull.printmtrx(ERROR, message, matrix, PrivateDetails.MATRIX_ELEMENT_CONTEXT);
    }

    static void error(final String message, final Access2D<?> matrix, final NumberContext context) {
        NotNull.printmtrx(ERROR, message, matrix, context);
    }

    static void error(final String message, final Object... arguments) {
        NotNull.println(ERROR, message, arguments);
    }

    static void error(final Throwable throwable, final String message, final Object... arguments) {
        NotNull.println(ERROR, throwable, message, arguments);
    }

    Optional<Writer> asWriter();

    /**
     * Will print 1 line/row with the objects in fixed width columns
     *
     * @param width The exact witdth of each column
     * @param columns The column objects, {@link #toString()} and then fix the length/width
     */
    default void columns(final int width, final Object... columns) {

        char[] chars = new char[width];
        Arrays.fill(chars, ASCII.SP);
        String padder = new String(chars);

        String[] strings = new String[columns.length];

        for (int i = 0; i < strings.length; i++) {
            strings[i] = String.valueOf(columns[i]);
        }
        for (int i = 0; i < strings.length; i++) {
            strings[i] = strings[i] + padder;
        }
        for (int i = 0; i < strings.length; i++) {
            strings[i] = strings[i].substring(0, width);
        }
        for (int i = 0; i < strings.length; i++) {
            this.print(strings[i]);
        }
        this.println();
    }

    void print(boolean value);

    void print(byte value);

    void print(char value);

    void print(double value);

    void print(float value);

    void print(int value);

    void print(long value);

    void print(Object object);

    void print(short value);

    void print(Throwable throwable);

    void printf(String format, Object... args);

    void println();

    default void println(final int tabs, final String message, final Object... args) {
        for (int i = 0; i < tabs; i++) {
            this.print(ASCII.HT);
        }
        this.print(TypeUtils.format(message, args));
        this.println();
    }

    default void println(final Object object) {
        this.print(object);
        this.println();
    }

    default void println(final String message, final Object... args) {
        this.print(TypeUtils.format(message, args));
        this.println();
    }

    default void println(final Throwable throwable, final String message, final Object... args) {
        this.print(TypeUtils.format(message, args));
        this.println();
        this.print(throwable);
    }

    default void printmtrx(final String message, final Access2D<?> matrix) {
        BasicLogger.PrivateDetails.printmtrx(this, message, matrix, PrivateDetails.MATRIX_ELEMENT_CONTEXT);
    }

    default void printmtrx(final String message, final Access2D<?> matrix, final NumberContext context) {
        BasicLogger.PrivateDetails.printmtrx(this, message, matrix, context);
    }

}
