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
package org.ojalgo.netio;

import java.io.PrintStream;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BigMatrix;
import org.ojalgo.matrix.ComplexMatrix;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * An extremely simple/basic logging system that uses {@linkplain System#out}, {@linkplain System#err} or any other
 * {@linkplain PrintStream}. This is NOT a logging framework for your end application. It is primarly used within ojAlgo
 * when debugging. By supplying suitable {@linkplain PrintStream}:s you may connect this with whatever logging framework
 * you use, and thus get debug info from (for instance) the optimisation algorithms in log files of your choice.
 * 
 * @author apete
 */
public abstract class BasicLogger {

    public static PrintStream DEBUG = System.out;
    public static PrintStream ERROR = System.err;

    private static final NumberContext MATRIX_ELEMENT_CONTEXT = NumberContext.getGeneral(6);

    public static void debug() {
        BasicLogger.println(DEBUG);
    }

    public static void debug(final String message) {
        BasicLogger.println(DEBUG, message);
    }

    public static void debug(final String message, final Access2D<?> access2D) {
        BasicLogger.println(DEBUG, message);
        BasicLogger.printMtrx(DEBUG, access2D, MATRIX_ELEMENT_CONTEXT);
    }

    public static void debug(final String messagePattern, final Object... arguments) {
        BasicLogger.println(DEBUG, messagePattern, arguments);
    }

    public static void debugStackTrace(final String message) {
        BasicLogger.printStackTrace(DEBUG, message);
    }

    public static void debugStackTrace(final Throwable throwable) {
        BasicLogger.printStackTrace(DEBUG, throwable);
    }

    public static void error() {
        BasicLogger.println(ERROR);
    }

    public static void error(final String message) {
        BasicLogger.println(ERROR, message);
    }

    public static void error(final String message, final Access2D<?> access2D) {
        BasicLogger.println(ERROR, message);
        BasicLogger.printMtrx(ERROR, access2D, MATRIX_ELEMENT_CONTEXT);
    }

    public static void error(final String messagePattern, final Object... arguments) {
        BasicLogger.println(ERROR, messagePattern, arguments);
    }

    public static void errorStackTrace(final String message) {
        BasicLogger.printStackTrace(ERROR, message);
    }

    public static void errorStackTrace(final Throwable throwable) {
        BasicLogger.printStackTrace(ERROR, throwable);
    }

    public static void println(final PrintStream stream) {
        if (stream != null) {
            stream.println();
        }
    }

    public static void println(final PrintStream stream, final String message) {
        if (stream != null) {
            stream.println(message);
        }
    }

    public static void println(final PrintStream stream, final String messagePattern, final Object... arguments) {
        if (stream != null) {
            stream.println(TypeUtils.format(messagePattern, arguments));
        }
    }

    public static void printMtrx(final PrintStream stream, final Access2D<?> access2D, final NumberContext context) {
        if (stream != null) {
            if (access2D instanceof ComplexMatrix) {
                BasicLogger.printMtrx(stream, (ComplexMatrix) access2D, context, false);
            } else if (access2D instanceof BasicMatrix) {
                BasicLogger.printMtrx(stream, (BasicMatrix<?>) access2D, context, true);
            } else if (access2D.get(0, 0) instanceof ComplexNumber) {
                BasicLogger.printMtrx(stream, ComplexMatrix.FACTORY.copy(access2D), context, false);
            } else {
                BasicLogger.printMtrx(stream, BigMatrix.FACTORY.copy(access2D), context, true);
            }
        }
    }

    public static void printStackTrace(final PrintStream stream, final String message) {
        if (stream != null) {
            try {
                throw new RecoverableCondition(message);
            } catch (final RecoverableCondition exception) {
                BasicLogger.printStackTrace(stream, exception);
            }
        }
    }

    public static void printStackTrace(final PrintStream stream, final Throwable throwable) {
        if (stream != null) {
            throwable.printStackTrace(stream);
        }
    }

    private static void printMtrx(final PrintStream stream, final BasicMatrix<?> matrix, final NumberContext context, final boolean plainString) {
        if (stream != null) {

            final int tmpRowDim = (int) matrix.countRows();
            final int tmpColDim = (int) matrix.countColumns();

            final String[][] tmpElements = new String[tmpRowDim][tmpColDim];

            int tmpWidth = 0;
            Scalar<?> tmpElementNumber;
            String tmpElementString;
            for (int j = 0; j < tmpColDim; j++) {
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpElementNumber = matrix.toScalar(i, j);
                    if (plainString) {
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
            //stream.println();
            for (int i = 0; i < tmpRowDim; i++) {
                for (int j = 0; j < tmpColDim; j++) {
                    tmpElementString = tmpElements[i][j];
                    tmpPadding = tmpWidth - tmpElementString.length();
                    for (int p = 0; p < tmpPadding; p++) {
                        stream.print(ASCII.SP);
                    }
                    stream.print(tmpElementString);
                }
                stream.println();
            }
        }
    }

    private BasicLogger() {
        super();
    }

}
