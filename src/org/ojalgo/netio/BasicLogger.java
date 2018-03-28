/*
 * Copyright 1997-2018 Optimatika
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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.Locale;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.ComplexMatrix;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
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
public abstract class BasicLogger {

    public static class AppendablePrinter implements Printer {

        private final Appendable myAppendable;
        private transient Formatter myFormatter;

        public AppendablePrinter(final Appendable appendable) {
            super();
            myAppendable = appendable;
        }

        public void print(final char c) {
            try {
                myAppendable.append(c);
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        public void print(final char[] ca) {
            try {
                for (int i = 0; i < ca.length; i++) {
                    myAppendable.append(ca[i]);
                }
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        public void print(final String str) {
            try {
                myAppendable.append(String.valueOf(str));
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        public Printer printf(final Locale locale, final String format, final Object... args) {

            synchronized (myAppendable) {
                if ((myFormatter == null) || (myFormatter.locale() != locale)) {
                    myFormatter = new Formatter(myAppendable, locale);
                }
                myFormatter.format(locale, format, args);
            }

            return this;
        }

        public Printer printf(final String format, final Object... args) {

            synchronized (myAppendable) {
                if ((myFormatter == null) || (myFormatter.locale() != Locale.getDefault())) {
                    myFormatter = new Formatter(myAppendable);
                }
                myFormatter.format(Locale.getDefault(), format, args);
            }

            return this;
        }

        public void println() {
            try {
                myAppendable.append(ASCII.LF);
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        /**
         * @return the appendable
         */
        Appendable getAppendable() {
            return myAppendable;
        }

    }

    /**
     * Temporarily store data/text. The buffer can be cleared and/or flushed to some receiver.
     *
     * @author apete
     */
    public static interface Buffer {

        void clear();

        default void flush(final Appendable receiver) {
            this.flush(new AppendablePrinter(receiver));
        }

        void flush(final BasicLogger.Printer receiver);

        default void flush(final PrintStream receiver) {
            this.flush(new PrintStreamPrinter(receiver));
        }

        default void flush(final PrintWriter receiver) {
            this.flush(new PrintWriterPrinter(receiver));
        }

    }

    public static interface Printer {

        /**
         * @see java.io.PrintWriter#print(char)
         * @see java.io.PrintStream#print(char)
         */
        public abstract void print(char c);

        /**
         * @see java.io.PrintWriter#print(char[])
         * @see java.io.PrintStream#print(char[])
         */
        public abstract void print(char[] ca);

        /**
         * @see java.io.PrintWriter#print(java.lang.String)
         * @see java.io.PrintStream#print(java.lang.String)
         */
        public abstract void print(String str);

        /**
         * @see java.io.PrintWriter#printf(java.util.Locale, java.lang.String, java.lang.Object[])
         * @see java.io.PrintStream#printf(java.util.Locale, java.lang.String, java.lang.Object[])
         */
        public abstract Printer printf(Locale locale, String format, Object... args);

        /**
         * @see java.io.PrintWriter#printf(java.lang.String, java.lang.Object[])
         * @see java.io.PrintStream#printf(java.lang.String, java.lang.Object[])
         */
        public abstract Printer printf(String format, Object... args);

        /**
         * @see java.io.PrintWriter#println()
         * @see java.io.PrintStream#println()
         */
        public abstract void println();

        /**
         * @see java.io.PrintWriter#print(boolean)
         * @see java.io.PrintStream#print(boolean)
         */
        default void print(final boolean b) {
            this.print(String.valueOf(b));
        }

        /**
         * @see java.io.PrintWriter#print(double)
         * @see java.io.PrintStream#print(double)
         */
        default void print(final double d) {
            this.print(String.valueOf(d));
        }

        /**
         * @see java.io.PrintWriter#print(float)
         * @see java.io.PrintStream#print(float)
         */
        default void print(final float f) {
            this.print(String.valueOf(f));
        }

        /**
         * @see java.io.PrintWriter#print(int)
         * @see java.io.PrintStream#print(int)
         */
        default void print(final int i) {
            this.print(String.valueOf(i));
        }

        /**
         * @see java.io.PrintWriter#print(long)
         * @see java.io.PrintStream#print(long)
         */
        default void print(final long l) {
            this.print(String.valueOf(l));
        }

        /**
         * @see java.io.PrintWriter#print(java.lang.Object)
         * @see java.io.PrintStream#print(java.lang.Object)
         */
        default void print(final Object obj) {
            this.print(String.valueOf(obj));
        }

        default void print(final String message, final Object... args) {
            this.print(TypeUtils.format(message, args));
        }

        /**
         * @see java.io.PrintWriter#println(boolean)
         * @see java.io.PrintStream#println(boolean)
         */
        default void println(final boolean b) {
            this.print(b);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(char)
         * @see java.io.PrintStream#println(char)
         */
        default void println(final char c) {
            this.print(c);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(char[])
         * @see java.io.PrintStream#println(char[])
         */
        default void println(final char[] ca) {
            this.print(ca);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(double)
         * @see java.io.PrintStream#println(double)
         */
        default void println(final double d) {
            this.print(d);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(float)
         * @see java.io.PrintStream#println(float)
         */
        default void println(final float f) {
            this.print(f);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(int)
         * @see java.io.PrintStream#println(int)
         */
        default void println(final int i) {
            this.print(i);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(long)
         * @see java.io.PrintStream#println(long)
         */
        default void println(final long l) {
            this.print(l);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(java.lang.Object)
         * @see java.io.PrintStream#println(java.lang.Object)
         */
        default void println(final Object obj) {
            this.print(obj);
            this.println();
        }

        /**
         * @see java.io.PrintWriter#println(java.lang.String)
         * @see java.io.PrintStream#println(java.lang.String)
         */
        default void println(final String str) {
            this.print(str);
            this.println();
        }

        default void println(final String message, final Object... args) {
            this.print(message, args);
            this.println();
        }

        default void printmtrx(final String message, final Access2D<?> matrix) {
            this.printmtrx(message, matrix, MATRIX_ELEMENT_CONTEXT);

        }

        default void printmtrx(final String message, final Access2D<?> matrix, final NumberContext context) {
            if (message != null) {
                this.println(message);
            }
            BasicLogger.printmtrx(this, matrix, context);
        }

    }

    public static final class PrintStreamPrinter implements Printer {

        private final PrintStream myStream;

        public PrintStreamPrinter(final PrintStream stream) {
            super();
            myStream = stream;
        }

        /**
         * @see java.io.PrintStream#print(boolean)
         */
        public void print(final boolean b) {
            myStream.print(b);
        }

        /**
         * @see java.io.PrintStream#print(char)
         */
        public void print(final char c) {
            myStream.print(c);
        }

        /**
         * @see java.io.PrintStream#print(char[])
         */
        public void print(final char[] ca) {
            myStream.print(ca);
        }

        /**
         * @see java.io.PrintStream#print(double)
         */
        public void print(final double d) {
            myStream.print(d);
        }

        /**
         * @see java.io.PrintStream#print(float)
         */
        public void print(final float f) {
            myStream.print(f);
        }

        /**
         * @see java.io.PrintStream#print(int)
         */
        public void print(final int i) {
            myStream.print(i);
        }

        /**
         * @see java.io.PrintStream#print(long)
         */
        public void print(final long l) {
            myStream.print(l);
        }

        /**
         * @see java.io.PrintStream#print(java.lang.Object)
         */
        public void print(final Object obj) {
            myStream.print(obj);
        }

        /**
         * @see java.io.PrintStream#print(java.lang.String)
         */
        public void print(final String str) {
            myStream.print(str);
        }

        public void print(final String message, final Object... args) {
            this.print(TypeUtils.format(message, args));
        }

        /**
         * @see java.io.PrintStream#printf(java.util.Locale, java.lang.String, java.lang.Object[])
         */
        public PrintStreamPrinter printf(final Locale locale, final String format, final Object... args) {
            myStream.printf(locale, format, args);
            return this;
        }

        /**
         * @see java.io.PrintStream#printf(java.lang.String, java.lang.Object[])
         */
        public PrintStreamPrinter printf(final String format, final Object... args) {
            myStream.printf(format, args);
            return this;
        }

        /**
         * @see java.io.PrintStream#println()
         */
        public void println() {
            myStream.println();
        }

        /**
         * @see java.io.PrintStream#println(boolean)
         */
        public void println(final boolean b) {
            myStream.println(b);
        }

        /**
         * @see java.io.PrintStream#println(char)
         */
        public void println(final char c) {
            myStream.println(c);
        }

        /**
         * @see java.io.PrintStream#println(char[])
         */
        public void println(final char[] ca) {
            myStream.println(ca);
        }

        /**
         * @see java.io.PrintStream#println(double)
         */
        public void println(final double d) {
            myStream.println(d);
        }

        /**
         * @see java.io.PrintStream#println(float)
         */
        public void println(final float f) {
            myStream.println(f);
        }

        /**
         * @see java.io.PrintStream#println(int)
         */
        public void println(final int i) {
            myStream.println(i);
        }

        /**
         * @see java.io.PrintStream#println(long)
         */
        public void println(final long l) {
            myStream.println(l);
        }

        /**
         * @see java.io.PrintStream#println(java.lang.Object)
         */
        public void println(final Object obj) {
            myStream.println(obj);
        }

        /**
         * @see java.io.PrintStream#println(java.lang.String)
         */
        public void println(final String str) {
            myStream.println(str);
        }

        public void println(final String message, final Object... args) {
            this.print(message, args);
            this.println();
        }

        public void printmtrx(final String message, final Access2D<?> matrix) {
            this.printmtrx(message, matrix, MATRIX_ELEMENT_CONTEXT);

        }

        public void printmtrx(final String message, final Access2D<?> matrix, final NumberContext context) {
            if (message != null) {
                this.println(message);
            }
            BasicLogger.printmtrx(this, matrix, context);
        }

        /**
         * @return the stream
         */
        PrintStream getStream() {
            return myStream;
        }

    }

    public static final class PrintWriterPrinter implements Printer {

        private final PrintWriter myWriter;

        public PrintWriterPrinter(final PrintWriter writer) {
            super();
            myWriter = writer;
        }

        /**
         * @see java.io.PrintWriter#print(boolean)
         */
        public void print(final boolean b) {
            myWriter.print(b);
        }

        /**
         * @see java.io.PrintWriter#print(char)
         */
        public void print(final char c) {
            myWriter.print(c);
        }

        /**
         * @see java.io.PrintWriter#print(char[])
         */
        public void print(final char[] ca) {
            myWriter.print(ca);
        }

        /**
         * @see java.io.PrintWriter#print(double)
         */
        public void print(final double d) {
            myWriter.print(d);
        }

        /**
         * @see java.io.PrintWriter#print(float)
         */
        public void print(final float f) {
            myWriter.print(f);
        }

        /**
         * @see java.io.PrintWriter#print(int)
         */
        public void print(final int i) {
            myWriter.print(i);
        }

        /**
         * @see java.io.PrintWriter#print(long)
         */
        public void print(final long l) {
            myWriter.print(l);
        }

        /**
         * @see java.io.PrintWriter#print(java.lang.Object)
         */
        public void print(final Object obj) {
            myWriter.print(obj);
        }

        /**
         * @see java.io.PrintWriter#print(java.lang.String)
         */
        public void print(final String str) {
            myWriter.print(str);
        }

        public void print(final String message, final Object... args) {
            this.print(TypeUtils.format(message, args));
        }

        /**
         * @see java.io.PrintWriter#printf(Locale, String, Object...)
         */
        public PrintWriterPrinter printf(final Locale locale, final String format, final Object... args) {
            myWriter.printf(locale, format, args);
            return this;
        }

        /**
         * @see java.io.PrintWriter#printf(String, Object...)
         */
        public PrintWriterPrinter printf(final String format, final Object... args) {
            myWriter.printf(format, args);
            return this;
        }

        /**
         * @see java.io.PrintWriter#println()
         */
        public void println() {
            myWriter.println();
        }

        /**
         * @see java.io.PrintWriter#println(boolean)
         */
        public void println(final boolean b) {
            myWriter.println(b);
        }

        /**
         * @see java.io.PrintWriter#println(char)
         */
        public void println(final char c) {
            myWriter.println(c);
        }

        /**
         * @see java.io.PrintWriter#println(char[])
         */
        public void println(final char[] ca) {
            myWriter.println(ca);
        }

        /**
         * @see java.io.PrintWriter#println(double)
         */
        public void println(final double d) {
            myWriter.println(d);
        }

        /**
         * @see java.io.PrintWriter#println(float)
         */
        public void println(final float f) {
            myWriter.println(f);
        }

        /**
         * @see java.io.PrintWriter#println(int)
         */
        public void println(final int i) {
            myWriter.println(i);
        }

        /**
         * @see java.io.PrintWriter#println(long)
         */
        public void println(final long l) {
            myWriter.println(l);
        }

        /**
         * @see java.io.PrintWriter#println(java.lang.Object)
         */
        public void println(final Object obj) {
            myWriter.println(obj);
        }

        /**
         * @see java.io.PrintWriter#println(java.lang.String)
         */
        public void println(final String str) {
            myWriter.println(str);
        }

        public void println(final String message, final Object... args) {
            this.print(message, args);
            this.println();
        }

        public void printmtrx(final String message, final Access2D<?> matrix) {
            this.printmtrx(message, matrix, MATRIX_ELEMENT_CONTEXT);

        }

        public void printmtrx(final String message, final Access2D<?> matrix, final NumberContext context) {
            if (message != null) {
                this.println(message);
            }
            BasicLogger.printmtrx(this, matrix, context);
        }

        /**
         * @return the writer
         */
        PrintWriter getWriter() {
            return myWriter;
        }

    }

    public static Printer DEBUG = new PrintStreamPrinter(System.out);

    public static Printer ERROR = new PrintStreamPrinter(System.err);

    public static final BasicLogger.Printer NULL = new BasicLogger.Printer() {

        public void print(final char c) {
        }

        public void print(final char[] ca) {
        }

        public void print(final String str) {
        }

        public Printer printf(final Locale locale, final String format, final Object... args) {
            return this;
        }

        public Printer printf(final String format, final Object... args) {
            return this;
        }

        public void println() {
        }

    };

    static final NumberContext MATRIX_ELEMENT_CONTEXT = NumberContext.getGeneral(6);

    public static void debug() {
        BasicLogger.println(DEBUG);
    }

    public static void debug(final Object message) {
        BasicLogger.println(DEBUG, message);
    }

    public static void debug(final String message, final Access2D<?> matrix) {
        BasicLogger.debug(message, matrix, MATRIX_ELEMENT_CONTEXT);
    }

    public static void debug(final String message, final Access2D<?> matrix, final NumberContext context) {
        if (message != null) {
            BasicLogger.println(DEBUG, message);
        }
        BasicLogger.printmtrx(DEBUG, matrix, context);
    }

    public static void debug(final String message, final Object... arguments) {
        BasicLogger.println(DEBUG, message, arguments);
    }

    public static void error() {
        BasicLogger.println(ERROR);
    }

    public static void error(final Object message) {
        BasicLogger.println(ERROR, message);
    }

    public static void error(final String message, final Access2D<?> matrix) {
        BasicLogger.error(message, matrix, MATRIX_ELEMENT_CONTEXT);
    }

    public static void error(final String message, final Access2D<?> matrix, final NumberContext context) {
        if (message != null) {
            BasicLogger.println(ERROR, message);
        }
        BasicLogger.printmtrx(ERROR, matrix, context);
    }

    public static void error(final String message, final Object... arguments) {
        BasicLogger.println(ERROR, message, arguments);
    }

    private static void printmtrx(final Printer appender, final BasicMatrix matrix, final NumberContext context, final boolean plain) {

        final int tmpRowDim = (int) matrix.countRows();
        final int tmpColDim = (int) matrix.countColumns();

        final String[][] tmpElements = new String[tmpRowDim][tmpColDim];

        int tmpWidth = 0;
        Scalar<?> tmpElementNumber;
        String tmpElementString;
        for (int j = 0; j < tmpColDim; j++) {
            for (int i = 0; i < tmpRowDim; i++) {
                tmpElementNumber = matrix.toScalar(i, j);
                if (plain) {
                    tmpElementString = tmpElementNumber.toPlainString(context);
                } else {
                    tmpElementString = tmpElementNumber.toString(context);
                }
                tmpWidth = Math.max(tmpWidth, tmpElementString.length());
                tmpElements[i][j] = tmpElementString;
            }
        }
        tmpWidth++;

        int tmpPadding;
        //appender.println();
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

    static void println(final Printer appender) {
        if (appender != null) {
            appender.println();
        }
    }

    static void println(final Printer appender, final Object message) {
        if (appender != null) {
            appender.println(message);
        }
    }

    static void println(final Printer appender, final String messagePattern, final Object... arguments) {
        if (appender != null) {
            appender.println(messagePattern, arguments);
        }
    }

    static void printmtrx(final Printer appender, final Access2D<?> matrix, final NumberContext context) {
        if ((appender != null) && (matrix.count() > 0L)) {
            if (matrix instanceof ComplexMatrix) {
                BasicLogger.printmtrx(appender, (ComplexMatrix) matrix, context, false);
            } else if (matrix instanceof BasicMatrix) {
                BasicLogger.printmtrx(appender, (BasicMatrix) matrix, context, true);
            } else if (matrix.get(0, 0) instanceof ComplexNumber) {
                BasicLogger.printmtrx(appender, ComplexMatrix.FACTORY.copy(matrix), context, false);
            } else {
                BasicLogger.printmtrx(appender, RationalMatrix.FACTORY.copy(matrix), context, true);
            }
        }
    }

    private BasicLogger() {
        super();
    }

}
