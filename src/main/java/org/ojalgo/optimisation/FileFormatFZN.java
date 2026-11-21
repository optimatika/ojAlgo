/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.optimisation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.ojalgo.structure.Access1D;

/**
 * FlatZinc reader.
 * <p>
 * FlatZinc is what the MiniZinc compiler gives a solver: one flat model, each constraint a call to one of the
 * solver's built-in predicates. Which predicates there are depends on the library the model was compiled with.
 * ojAlgo reads the linear ones – what MiniZinc's own linear library (the one used for MIP solvers) leaves in
 * a model – together with a few others that are exactly linear over integer and bool variables. A constraint
 * that is anything else is an {@link IllegalArgumentException} with the number of the line it is on, never
 * skipped, since a skipped constraint makes a different model.
 * <p>
 * Bool variables are binary variables, int variables are integer variables and float variables are
 * continuous variables. An int variable whose domain has holes, such as {@code var {1,3,7}: x}, is encoded
 * with one binary variable per value. Set variables are not supported – MiniZinc's linear library rewrites
 * them as arrays of bools.
 * <p>
 * Apart from the model, the file says which variables make up a solution ({@code output_var} and
 * {@code output_array} annotations). {@link Parsed#printSolution(Access1D, StringBuilder)} writes a solution
 * that way, in the format a FlatZinc solver prints its solutions in.
 *
 * @author apete
 */
abstract class FileFormatFZN {

    /**
     * A constraint call – a FlatZinc built-in predicate – written as linear constraints on the model.
     */
    @FunctionalInterface
    interface Builtin {

        void post(Builder builder, List<Object> arguments);

    }

    /**
     * Turns parsed values into model entities. Values are {@link Variable}, {@link Long} (int),
     * {@link Boolean} (bool), {@link BigDecimal} (float), {@link IntSet} (set of int), {@link List} (array),
     * {@link String} (string literal) or {@link Annotation}.
     */
    static final class Builder {

        private int myCounter = 0;
        private final ExpressionsBasedModel myModel;
        private Variable myZero = null;

        Builder(final ExpressionsBasedModel model) {
            super();
            myModel = model;
        }

        /**
         * {@code sum(coefficients[i] * terms[i]) + constant} as an expression that is between lower and upper
         * (either may be null), with the constant moved to the limits.
         */
        void linear(final List<?> coefficients, final List<?> terms, final BigDecimal constant, final BigDecimal lower, final BigDecimal upper) {

            if (coefficients.size() != terms.size()) {
                throw new IllegalArgumentException("There are " + coefficients.size() + " coefficients but " + terms.size() + " variables");
            }

            BigDecimal fixed = constant;
            List<Variable> variables = new ArrayList<>(terms.size());
            List<BigDecimal> factors = new ArrayList<>(terms.size());

            for (int i = 0, limit = terms.size(); i < limit; i++) {
                BigDecimal coefficient = Builder.number(coefficients.get(i));
                Object term = terms.get(i);
                if (term instanceof Variable) {
                    if (coefficient.signum() != 0) {
                        variables.add((Variable) term);
                        factors.add(coefficient);
                    }
                } else {
                    fixed = fixed.add(coefficient.multiply(Builder.number(term)));
                }
            }

            BigDecimal lowerLimit = lower != null ? lower.subtract(fixed) : null;
            BigDecimal upperLimit = upper != null ? upper.subtract(fixed) : null;

            if (variables.isEmpty()) {
                // Nothing variable – either always true (no constraint), or the model is infeasible
                boolean satisfied = (lowerLimit == null || lowerLimit.signum() <= 0) && (upperLimit == null || upperLimit.signum() >= 0);
                if (!satisfied) {
                    Expression expression = this.newExpression();
                    expression.add(this.zero(), BigDecimal.ONE);
                    expression.lower(lowerLimit).upper(upperLimit);
                }
                return;
            }

            Expression expression = this.newExpression();
            for (int i = 0, limit = variables.size(); i < limit; i++) {
                expression.add(variables.get(i), factors.get(i));
            }

            if (lowerLimit != null) {
                expression.lower(lowerLimit);
            }
            if (upperLimit != null) {
                expression.upper(upperLimit);
            }
        }

        /**
         * {@code left + right * factor} between lower and upper
         */
        void pair(final Object left, final Object right, final long factor, final long lower, final Long upper) {
            this.linear(List.of(Long.valueOf(1L), Long.valueOf(factor)), List.of(left, right), BigDecimal.ZERO, BigDecimal.valueOf(lower),
                    upper != null ? BigDecimal.valueOf(upper.longValue()) : null);
        }

        void restrict(final Object term, final IntSet values) {
            if (term instanceof Variable) {
                Builder.tighten((Variable) term, values.min(), values.max());
                if (values.hasHoles()) {
                    this.encode((Variable) term, values);
                }
            } else if (!values.contains(Builder.integer(term))) {
                this.linear(List.of(Long.valueOf(1L)), List.of(term), BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO);
            }
        }

        /**
         * Restricts an integer variable to a set of values, with one binary variable per range of values:
         * exactly one range is chosen, and the variable is within the chosen range.
         */
        private void encode(final Variable variable, final IntSet values) {

            Expression choice = this.newExpression().level(BigDecimal.ONE);
            Expression lower = this.newExpression().lower(BigDecimal.ZERO);
            Expression upper = this.newExpression().upper(BigDecimal.ZERO);
            lower.add(variable, BigDecimal.ONE);
            upper.add(variable, BigDecimal.ONE);

            long[] bounds = values.bounds();
            for (int i = 0; i < bounds.length; i += 2) {
                Variable indicator = myModel.newVariable(variable.getName() + "#" + bounds[i] + ".." + bounds[i + 1]).binary();
                choice.add(indicator, BigDecimal.ONE);
                lower.add(indicator, BigDecimal.valueOf(-bounds[i]));
                upper.add(indicator, BigDecimal.valueOf(-bounds[i + 1]));
            }
        }

        private Expression newExpression() {
            return myModel.addExpression("C" + myCounter++);
        }

        /**
         * A variable fixed at 0, to put in constraints that are only constants.
         */
        private Variable zero() {
            if (myZero == null) {
                myZero = myModel.newVariable("#zero").level(BigDecimal.ZERO);
            }
            return myZero;
        }

        static long integer(final Object value) {
            if (value instanceof Long) {
                return ((Long) value).longValue();
            }
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue() ? 1L : 0L;
            }
            throw new IllegalArgumentException("Expected an int, but got " + Builder.describe(value));
        }

        static BigDecimal number(final Object value) {
            if (value instanceof Long) {
                return BigDecimal.valueOf(((Long) value).longValue());
            }
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
            }
            throw new IllegalArgumentException("Expected a number, but got " + Builder.describe(value));
        }

        static void tighten(final Variable variable, final long lower, final long upper) {
            BigDecimal low = BigDecimal.valueOf(lower);
            BigDecimal high = BigDecimal.valueOf(upper);
            if (variable.getLowerLimit() == null || variable.getLowerLimit().compareTo(low) < 0) {
                variable.lower(low);
            }
            if (variable.getUpperLimit() == null || variable.getUpperLimit().compareTo(high) > 0) {
                variable.upper(high);
            }
        }

        static List<?> array(final Object value) {
            if (value instanceof List) {
                return (List<?>) value;
            }
            throw new IllegalArgumentException("Expected an array, but got " + Builder.describe(value));
        }

        static String describe(final Object value) {
            if (value instanceof Variable) {
                return "the variable " + ((Variable) value).getName();
            }
            if (value instanceof List) {
                return "an array";
            }
            return String.valueOf(value);
        }

    }

    /**
     * A set of int: sorted, disjoint ranges.
     */
    static final class IntSet {

        static IntSet range(final long lower, final long upper) {
            return new IntSet(lower <= upper ? new long[] { lower, upper } : new long[0]);
        }

        static IntSet of(final List<long[]> ranges) {

            List<long[]> sorted = new ArrayList<>();
            for (long[] range : ranges) {
                if (range[0] <= range[1]) {
                    sorted.add(range);
                }
            }
            sorted.sort((a, b) -> Long.compare(a[0], b[0]));

            List<long[]> merged = new ArrayList<>();
            for (long[] range : sorted) {
                long[] last = merged.isEmpty() ? null : merged.get(merged.size() - 1);
                if (last != null && range[0] <= last[1] + 1L) {
                    last[1] = Math.max(last[1], range[1]);
                } else {
                    merged.add(new long[] { range[0], range[1] });
                }
            }

            long[] bounds = new long[2 * merged.size()];
            for (int i = 0; i < merged.size(); i++) {
                bounds[2 * i] = merged.get(i)[0];
                bounds[2 * i + 1] = merged.get(i)[1];
            }
            return new IntSet(bounds);
        }

        /**
         * lower0, upper0, lower1, upper1...
         */
        private final long[] myBounds;

        private IntSet(final long[] bounds) {
            super();
            myBounds = bounds;
        }

        @Override
        public String toString() {
            if (myBounds.length == 0) {
                return "{}";
            }
            if (myBounds.length == 2) {
                return myBounds[0] + ".." + myBounds[1];
            }
            StringBuilder retVal = new StringBuilder("{");
            for (long v : this.values()) {
                if (retVal.length() > 1) {
                    retVal.append(',');
                }
                retVal.append(v);
            }
            return retVal.append('}').toString();
        }

        /**
         * lower0, upper0, lower1, upper1...
         */
        long[] bounds() {
            return myBounds;
        }

        boolean contains(final long value) {
            for (int i = 0; i < myBounds.length; i += 2) {
                if (myBounds[i] <= value && value <= myBounds[i + 1]) {
                    return true;
                }
            }
            return false;
        }

        boolean hasHoles() {
            return myBounds.length > 2;
        }

        boolean isEmpty() {
            return myBounds.length == 0;
        }

        long max() {
            return myBounds[myBounds.length - 1];
        }

        long min() {
            return myBounds[0];
        }

        long size() {
            long retVal = 0L;
            for (int i = 0; i < myBounds.length; i += 2) {
                retVal += myBounds[i + 1] - myBounds[i] + 1L;
            }
            return retVal;
        }

        long[] values() {
            long size = this.size();
            if (size > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Too many values in " + this.toString());
            }
            long[] retVal = new long[(int) size];
            int index = 0;
            for (int i = 0; i < myBounds.length; i += 2) {
                for (long v = myBounds[i]; v <= myBounds[i + 1]; v++) {
                    retVal[index++] = v;
                }
            }
            return retVal;
        }

    }

    /**
     * What a FlatZinc file is: the model, and how to write a solution of it.
     */
    static final class Parsed {

        private final ExpressionsBasedModel myModel;
        private final List<Output> myOutput;
        private final Optimisation.Sense mySense;

        Parsed(final ExpressionsBasedModel model, final Optimisation.Sense sense, final List<Output> output) {
            super();
            myModel = model;
            mySense = sense;
            myOutput = output;
        }

        ExpressionsBasedModel getModel() {
            return myModel;
        }

        /**
         * MIN or MAX, or null for a satisfaction problem
         */
        Optimisation.Sense getSense() {
            return mySense;
        }

        /**
         * Writes the output variables, one per line, as {@code name = value;}. The solution has one value per
         * model variable, in the model's order – as a result from the model has.
         */
        void printSolution(final Access1D<?> solution, final StringBuilder destination) {
            for (Output output : myOutput) {
                output.print(myModel, solution, destination);
            }
        }

        /**
         * Writes the value of every FlatZinc variable, not only the output variables – to check a solution
         * against the FlatZinc model.
         */
        void printAll(final Access1D<?> solution, final StringBuilder destination, final Map<String, Object> symbols) {
            for (Map.Entry<String, Object> entry : symbols.entrySet()) {
                if (entry.getValue() instanceof Variable) {
                    destination.append(entry.getKey()).append(" = ");
                    destination.append(Output.format(myModel, solution, entry.getValue(), Type.FLOAT));
                    destination.append(";\n");
                }
            }
        }

    }

    /**
     * A FlatZinc annotation: a name and its (possibly no) arguments.
     */
    record Annotation(String name, List<Object> arguments) {

        static Annotation find(final List<Annotation> annotations, final String name) {
            for (Annotation annotation : annotations) {
                if (annotation.name().equals(name)) {
                    return annotation;
                }
            }
            return null;
        }

    }

    /**
     * A variable, or array of variables, that is part of a solution.
     */
    record Output(String name, Type type, List<IntSet> dimensions, List<Object> elements) {

        static String format(final ExpressionsBasedModel model, final Access1D<?> solution, final Object element, final Type type) {

            BigDecimal value;
            if (element instanceof Variable) {
                value = new BigDecimal(String.valueOf(solution.get(model.indexOf((Variable) element))));
            } else if (element instanceof IntSet) {
                return element.toString();
            } else {
                value = Builder.number(element);
            }

            switch (type) {
                case BOOL:
                    return value.compareTo(new BigDecimal("0.5")) > 0 ? "true" : "false";
                case INT:
                    return value.setScale(0, RoundingMode.HALF_EVEN).toPlainString();
                default:
                    String retVal = Double.toString(value.doubleValue());
                    return retVal;
            }
        }

        void print(final ExpressionsBasedModel model, final Access1D<?> solution, final StringBuilder destination) {

            destination.append(name).append(" = ");

            if (dimensions == null) {
                destination.append(Output.format(model, solution, elements.get(0), type));
            } else {
                destination.append("array").append(dimensions.size()).append("d(");
                for (IntSet dimension : dimensions) {
                    destination.append(dimension.isEmpty() ? "1..0" : dimension.min() + ".." + dimension.max()).append(',');
                }
                destination.append('[');
                for (int i = 0, limit = elements.size(); i < limit; i++) {
                    if (i > 0) {
                        destination.append(',');
                    }
                    destination.append(Output.format(model, solution, elements.get(i), type));
                }
                destination.append("])");
            }

            destination.append(";\n");
        }

    }

    /**
     * A FlatZinc base type
     */
    enum Type {
        BOOL, FLOAT, INT, SET;
    }

    /**
     * Reads FlatZinc one token at a time, keeping track of the line it is on.
     */
    private static final class Lexer {

        static final int EOF = 0;
        static final int FLOAT = 3;
        static final int IDENTIFIER = 1;
        static final int INT = 2;
        static final int PUNCTUATION = 5;
        static final int STRING = 4;

        private final char[] myBuffer = new char[1 << 16];
        private int myLimit = 0;
        private int myLine = 1;
        private int myPosition = 0;
        private final Reader myReader;
        private final StringBuilder myText = new StringBuilder();

        /**
         * The line of the current token
         */
        int line = 1;
        String text = null;
        int type = EOF;

        Lexer(final InputStream input) {
            super();
            myReader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            this.advance();
        }

        private int peek(final int ahead) {
            if (myPosition + ahead >= myLimit) {
                this.fill();
            }
            return myPosition + ahead < myLimit ? myBuffer[myPosition + ahead] : -1;
        }

        private int take() {
            int retVal = this.peek(0);
            if (retVal >= 0) {
                myPosition++;
                if (retVal == '\n') {
                    myLine++;
                }
            }
            return retVal;
        }

        private void fill() {
            int remaining = myLimit - myPosition;
            System.arraycopy(myBuffer, myPosition, myBuffer, 0, remaining);
            myPosition = 0;
            myLimit = remaining;
            try {
                int read;
                while (myLimit < myBuffer.length && (read = myReader.read(myBuffer, myLimit, myBuffer.length - myLimit)) >= 0) {
                    myLimit += read;
                    if (read == 0) {
                        break;
                    }
                }
            } catch (IOException cause) {
                throw new UncheckedIOException(cause);
            }
        }

        private void digits(final int radix) {
            while (Character.digit(this.peek(0), radix) >= 0) {
                myText.append((char) this.take());
            }
        }

        void advance() {

            for (int c = this.peek(0); c >= 0; c = this.peek(0)) {
                if (c == '%') {
                    while (c >= 0 && c != '\n') {
                        this.take();
                        c = this.peek(0);
                    }
                } else if (c == '/' && this.peek(1) == '*') {
                    this.take();
                    this.take();
                    while (this.peek(0) >= 0 && !(this.peek(0) == '*' && this.peek(1) == '/')) {
                        this.take();
                    }
                    this.take();
                    this.take();
                } else if (Character.isWhitespace(c)) {
                    this.take();
                } else {
                    break;
                }
            }

            line = myLine;
            myText.setLength(0);

            int c = this.peek(0);

            if (c < 0) {
                type = EOF;
                text = null;
                return;
            }

            if (Character.isLetter(c) || c == '_') {
                while (Character.isLetterOrDigit(this.peek(0)) || this.peek(0) == '_') {
                    myText.append((char) this.take());
                }
                type = IDENTIFIER;
                text = myText.toString();
                return;
            }

            if (Character.isDigit(c) || c == '-' && Character.isDigit(this.peek(1))) {

                if (c == '-') {
                    myText.append((char) this.take());
                }

                type = INT;

                if (this.peek(0) == '0' && (this.peek(1) == 'x' || this.peek(1) == 'o')) {
                    boolean hex = this.peek(1) == 'x';
                    this.take();
                    this.take();
                    int start = myText.length();
                    this.digits(hex ? 16 : 8);
                    String digits = myText.substring(start);
                    long value = Long.parseLong(digits, hex ? 16 : 8);
                    text = Long.toString(myText.charAt(0) == '-' ? -value : value);
                    return;
                }

                this.digits(10);

                if (this.peek(0) == '.' && Character.isDigit(this.peek(1))) {
                    type = FLOAT;
                    myText.append((char) this.take());
                    this.digits(10);
                }

                if ((this.peek(0) == 'e' || this.peek(0) == 'E')
                        && (Character.isDigit(this.peek(1)) || (this.peek(1) == '-' || this.peek(1) == '+') && Character.isDigit(this.peek(2)))) {
                    type = FLOAT;
                    myText.append((char) this.take());
                    if (this.peek(0) == '-' || this.peek(0) == '+') {
                        myText.append((char) this.take());
                    }
                    this.digits(10);
                }

                text = myText.toString();
                return;
            }

            if (c == '"') {
                this.take();
                for (int s = this.take(); s != '"'; s = this.take()) {
                    if (s < 0) {
                        throw this.error("A string that does not end");
                    }
                    if (s == '\\') {
                        int escaped = this.take();
                        switch (escaped) {
                            case 'n' -> myText.append('\n');
                            case 't' -> myText.append('\t');
                            default -> myText.append((char) escaped);
                        }
                    } else {
                        myText.append((char) s);
                    }
                }
                type = STRING;
                text = myText.toString();
                return;
            }

            this.take();
            type = PUNCTUATION;

            if (c == ':' && this.peek(0) == ':') {
                this.take();
                text = "::";
            } else if (c == '.' && this.peek(0) == '.') {
                this.take();
                text = "..";
            } else if ("()[]{},:;=".indexOf(c) >= 0) {
                text = String.valueOf((char) c);
            } else {
                throw this.error("'" + (char) c + "' is not FlatZinc");
            }
        }

        IllegalArgumentException error(final String message) {
            return new IllegalArgumentException("Line " + line + ": " + message);
        }

        void expect(final String punctuation) {
            if (!this.is(punctuation)) {
                throw this.error("Expected '" + punctuation + "', but found " + this.describe());
            }
            this.advance();
        }

        String describe() {
            if (type == EOF) {
                return "the end of the file";
            }
            return type == STRING ? "\"" + text + "\"" : "'" + text + "'";
        }

        String identifier() {
            if (type != IDENTIFIER) {
                throw this.error("Expected a name, but found " + this.describe());
            }
            String retVal = text;
            this.advance();
            return retVal;
        }

        long integer() {
            if (type != INT) {
                throw this.error("Expected an int, but found " + this.describe());
            }
            long retVal = Long.parseLong(text);
            this.advance();
            return retVal;
        }

        boolean is(final String punctuation) {
            return type == PUNCTUATION && text.equals(punctuation);
        }

        boolean isKeyword(final String keyword) {
            return type == IDENTIFIER && text.equals(keyword);
        }

    }

    /**
     * The domain of a declared variable or parameter
     */
    private record Domain(Type type, IntSet values, BigDecimal lower, BigDecimal upper) {

        static final Domain BOOL = new Domain(Type.BOOL, null, null, null);
        static final Domain FLOAT = new Domain(Type.FLOAT, null, null, null);
        static final Domain INT = new Domain(Type.INT, null, null, null);
        static final Domain SET = new Domain(Type.SET, null, null, null);

    }

    static final Map<String, Builtin> BUILTINS = new HashMap<>();

    private static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();

    static {

        Builtin linearEquality = (b, a) -> FileFormatFZN.linear(b, a, true);
        Builtin linearLessOrEqual = (b, a) -> FileFormatFZN.linear(b, a, false);

        BUILTINS.put("int_lin_eq", linearEquality);
        BUILTINS.put("int_lin_le", linearLessOrEqual);
        BUILTINS.put("float_lin_eq", linearEquality);
        BUILTINS.put("float_lin_le", linearLessOrEqual);
        BUILTINS.put("bool_lin_eq", linearEquality);
        BUILTINS.put("bool_lin_le", linearLessOrEqual);

        Builtin equality = (b, a) -> b.pair(a.get(0), a.get(1), -1L, 0L, 0L);
        BUILTINS.put("int_eq", equality);
        BUILTINS.put("float_eq", equality);
        BUILTINS.put("bool_eq", equality);
        BUILTINS.put("bool2int", equality);
        BUILTINS.put("int2float", equality);

        Builtin lessOrEqual = (b, a) -> b.linear(List.of(Long.valueOf(1L), Long.valueOf(-1L)), List.of(a.get(0), a.get(1)), BigDecimal.ZERO, null,
                BigDecimal.ZERO);
        BUILTINS.put("int_le", lessOrEqual);
        BUILTINS.put("float_le", lessOrEqual);
        BUILTINS.put("bool_le", lessOrEqual);

        Builtin lessThan = (b, a) -> b.linear(List.of(Long.valueOf(1L), Long.valueOf(-1L)), List.of(a.get(0), a.get(1)), BigDecimal.ZERO, null, MINUS_ONE);
        BUILTINS.put("int_lt", lessThan);
        BUILTINS.put("bool_lt", lessThan);

        Builtin plus = (b, a) -> b.linear(List.of(Long.valueOf(1L), Long.valueOf(1L), Long.valueOf(-1L)), List.of(a.get(0), a.get(1), a.get(2)),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        BUILTINS.put("int_plus", plus);
        BUILTINS.put("float_plus", plus);

        BUILTINS.put("bool_not", (b, a) -> b.pair(a.get(0), a.get(1), 1L, 1L, 1L));

        BUILTINS.put("bool_clause", (b, a) -> {
            List<?> positive = Builder.array(a.get(0));
            List<?> negative = Builder.array(a.get(1));
            List<Object> coefficients = new ArrayList<>();
            List<Object> terms = new ArrayList<>();
            for (Object term : positive) {
                coefficients.add(Long.valueOf(1L));
                terms.add(term);
            }
            for (Object term : negative) {
                coefficients.add(Long.valueOf(-1L));
                terms.add(term);
            }
            b.linear(coefficients, terms, BigDecimal.ZERO, BigDecimal.valueOf(1L - negative.size()), null);
        });

        BUILTINS.put("array_bool_or", (b, a) -> FileFormatFZN.or(b, Builder.array(a.get(0)), a.get(1)));
        BUILTINS.put("bool_or", (b, a) -> FileFormatFZN.or(b, List.of(a.get(0), a.get(1)), a.get(2)));
        BUILTINS.put("array_bool_and", (b, a) -> FileFormatFZN.and(b, Builder.array(a.get(0)), a.get(1)));
        BUILTINS.put("bool_and", (b, a) -> FileFormatFZN.and(b, List.of(a.get(0), a.get(1)), a.get(2)));

        BUILTINS.put("bool_xor", (b, a) -> {
            if (a.size() == 2) {
                b.pair(a.get(0), a.get(1), 1L, 1L, 1L);
            } else {
                Object x = a.get(0), y = a.get(1), r = a.get(2);
                List<Long> ones = List.of(Long.valueOf(1L), Long.valueOf(1L), Long.valueOf(1L));
                // r <= x + y, r >= x - y, r >= y - x, r <= 2 - x - y
                b.linear(List.of(Long.valueOf(1L), Long.valueOf(1L), Long.valueOf(-1L)), List.of(x, y, r), BigDecimal.ZERO, BigDecimal.ZERO, null);
                b.linear(List.of(Long.valueOf(-1L), Long.valueOf(1L), Long.valueOf(1L)), List.of(x, y, r), BigDecimal.ZERO, BigDecimal.ZERO, null);
                b.linear(List.of(Long.valueOf(1L), Long.valueOf(-1L), Long.valueOf(1L)), List.of(x, y, r), BigDecimal.ZERO, BigDecimal.ZERO, null);
                b.linear(ones, List.of(x, y, r), BigDecimal.ZERO, null, BigDecimal.valueOf(2L));
            }
        });

        BUILTINS.put("set_in", (b, a) -> {
            if (!(a.get(1) instanceof IntSet)) {
                throw new IllegalArgumentException("set_in with a set variable is not supported");
            }
            b.restrict(a.get(0), (IntSet) a.get(1));
        });
    }

    /**
     * r is the conjunction of terms: r <= t (each t), r >= sum(t) - (n - 1)
     */
    static void and(final Builder builder, final List<?> terms, final Object result) {
        for (Object term : terms) {
            builder.pair(term, result, -1L, 0L, null);
        }
        List<Object> coefficients = new ArrayList<>();
        List<Object> all = new ArrayList<>();
        for (Object term : terms) {
            coefficients.add(Long.valueOf(1L));
            all.add(term);
        }
        coefficients.add(Long.valueOf(-1L));
        all.add(result);
        builder.linear(coefficients, all, BigDecimal.ZERO, null, BigDecimal.valueOf(terms.size() - 1L));
    }

    /**
     * sum(a[i] * x[i]) = c or sum(a[i] * x[i]) <= c, where c (bool_lin_eq and bool_lin_le) may be a variable.
     */
    static void linear(final Builder builder, final List<Object> arguments, final boolean equality) {

        List<?> coefficients = Builder.array(arguments.get(0));
        List<?> terms = Builder.array(arguments.get(1));
        Object right = arguments.get(2);

        if (right instanceof Variable) {
            List<Object> allCoefficients = new ArrayList<>(coefficients);
            List<Object> allTerms = new ArrayList<>(terms);
            allCoefficients.add(Long.valueOf(-1L));
            allTerms.add(right);
            builder.linear(allCoefficients, allTerms, BigDecimal.ZERO, equality ? BigDecimal.ZERO : null, BigDecimal.ZERO);
        } else {
            BigDecimal limit = Builder.number(right);
            builder.linear(coefficients, terms, BigDecimal.ZERO, equality ? limit : null, limit);
        }
    }

    /**
     * r is the disjunction of terms: r >= t (each t), r <= sum(t)
     */
    static void or(final Builder builder, final List<?> terms, final Object result) {
        for (Object term : terms) {
            builder.pair(result, term, -1L, 0L, null);
        }
        List<Object> coefficients = new ArrayList<>();
        List<Object> all = new ArrayList<>();
        for (Object term : terms) {
            coefficients.add(Long.valueOf(1L));
            all.add(term);
        }
        coefficients.add(Long.valueOf(-1L));
        all.add(result);
        builder.linear(coefficients, all, BigDecimal.ZERO, BigDecimal.ZERO, null);
    }

    static Parsed parse(final InputStream input, final Supplier<ExpressionsBasedModel> factory) {
        return FileFormatFZN.parse(input, factory, new HashMap<>());
    }

    /**
     * @param symbols Filled in with every name the file declares, and its value – a {@link Variable} for a
     *        variable.
     */
    static Parsed parse(final InputStream input, final Supplier<ExpressionsBasedModel> factory, final Map<String, Object> symbols) {

        Lexer lexer = new Lexer(input);
        ExpressionsBasedModel model = factory.get();
        Builder builder = new Builder(model);
        List<Output> output = new ArrayList<>();
        Optimisation.Sense sense = null;
        boolean solved = false;

        while (lexer.type != Lexer.EOF) {

            if (solved) {
                throw lexer.error("Nothing comes after the solve item, but found " + lexer.describe());
            }

            if (lexer.isKeyword("predicate")) {
                FileFormatFZN.skipItem(lexer);
            } else if (lexer.isKeyword("constraint")) {
                FileFormatFZN.parseConstraint(lexer, builder, symbols);
            } else if (lexer.isKeyword("solve")) {
                sense = FileFormatFZN.parseSolve(lexer, model, symbols);
                solved = true;
            } else if (lexer.type == Lexer.IDENTIFIER) {
                FileFormatFZN.parseDeclaration(lexer, model, builder, symbols, output);
            } else {
                throw lexer.error("Expected a declaration, a constraint or the solve item, but found " + lexer.describe());
            }
        }

        if (!solved) {
            throw lexer.error("There is no solve item");
        }

        model.setOptimisationSense(sense != null ? sense : Optimisation.Sense.MIN);

        return new Parsed(model, sense, output);
    }

    static ExpressionsBasedModel read(final InputStream input, final Supplier<ExpressionsBasedModel> factory) {
        return FileFormatFZN.parse(input, factory).getModel();
    }

    private static List<Annotation> parseAnnotations(final Lexer lexer, final Map<String, Object> symbols) {

        if (!lexer.is("::")) {
            return Collections.emptyList();
        }

        List<Annotation> retVal = new ArrayList<>();
        while (lexer.is("::")) {
            lexer.advance();
            int line = lexer.line;
            Object annotation = FileFormatFZN.parseExpression(lexer, symbols, true);
            if (!(annotation instanceof Annotation)) {
                throw new IllegalArgumentException("Line " + line + ": An annotation is a name, possibly with arguments");
            }
            retVal.add((Annotation) annotation);
        }
        return retVal;
    }

    private static void parseConstraint(final Lexer lexer, final Builder builder, final Map<String, Object> symbols) {

        lexer.advance();
        int line = lexer.line;
        String name = lexer.identifier();

        lexer.expect("(");
        List<Object> arguments = new ArrayList<>();
        while (!lexer.is(")")) {
            if (!arguments.isEmpty()) {
                lexer.expect(",");
            }
            arguments.add(FileFormatFZN.parseExpression(lexer, symbols, false));
        }
        lexer.advance();

        FileFormatFZN.parseAnnotations(lexer, symbols);
        lexer.expect(";");

        Builtin builtin = BUILTINS.get(name);
        if (builtin == null) {
            throw new IllegalArgumentException("Line " + line + ": '" + name
                    + "' is not a constraint ojAlgo can model – it takes linear constraints, such as MiniZinc's linear library (-G linear) leaves in a model");
        }

        try {
            builtin.post(builder, arguments);
        } catch (IndexOutOfBoundsException cause) {
            throw new IllegalArgumentException("Line " + line + ": '" + name + "' with " + arguments.size() + " arguments", cause);
        } catch (IllegalArgumentException cause) {
            throw new IllegalArgumentException("Line " + line + ": " + name + ": " + cause.getMessage(), cause);
        }
    }

    private static void parseDeclaration(final Lexer lexer, final ExpressionsBasedModel model, final Builder builder, final Map<String, Object> symbols,
            final List<Output> output) {

        int line = lexer.line;

        boolean array = false;
        long size = 0L;
        if (lexer.isKeyword("array")) {
            lexer.advance();
            lexer.expect("[");
            if (lexer.isKeyword("int")) {
                lexer.advance();
            } else {
                long first = lexer.integer();
                lexer.expect("..");
                size = lexer.integer() - first + 1L;
            }
            lexer.expect("]");
            if (!lexer.isKeyword("of")) {
                throw lexer.error("Expected 'of', but found " + lexer.describe());
            }
            lexer.advance();
            array = true;
        }

        boolean variable = false;
        if (lexer.isKeyword("var")) {
            lexer.advance();
            variable = true;
        }

        Domain domain = FileFormatFZN.parseDomain(lexer);

        lexer.expect(":");
        String name = lexer.identifier();
        List<Annotation> annotations = FileFormatFZN.parseAnnotations(lexer, symbols);

        Object value = null;
        if (lexer.is("=")) {
            lexer.advance();
            value = FileFormatFZN.parseExpression(lexer, symbols, false);
        }
        lexer.expect(";");

        if (symbols.containsKey(name)) {
            throw new IllegalArgumentException("Line " + line + ": '" + name + "' is declared twice");
        }

        try {

            if (array) {

                if (value == null) {
                    throw new IllegalArgumentException("The array '" + name + "' has no elements");
                }
                List<Object> elements = new ArrayList<>();
                for (Object element : Builder.array(value)) {
                    elements.add(FileFormatFZN.convert(element, domain.type()));
                }
                if (elements.size() != size) {
                    throw new IllegalArgumentException("The array '" + name + "' has " + elements.size() + " elements, not " + size);
                }
                symbols.put(name, elements);

                Annotation annotation = Annotation.find(annotations, "output_array");
                if (annotation != null) {
                    List<IntSet> dimensions = new ArrayList<>();
                    for (Object dimension : Builder.array(annotation.arguments().get(0))) {
                        if (!(dimension instanceof IntSet)) {
                            throw new IllegalArgumentException("The dimensions of output_array are ranges, not " + Builder.describe(dimension));
                        }
                        dimensions.add((IntSet) dimension);
                    }
                    output.add(new Output(name, domain.type(), dimensions, elements));
                }

            } else {

                Object symbol;

                if (value != null) {
                    symbol = FileFormatFZN.convert(value, domain.type());
                    if (symbol instanceof Variable) {
                        FileFormatFZN.restrict(builder, (Variable) symbol, domain);
                    }
                } else if (!variable) {
                    throw new IllegalArgumentException("The parameter '" + name + "' has no value");
                } else if (domain.type() == Type.SET) {
                    throw new IllegalArgumentException("'" + name + "' is a set variable – compile with a library that has no set variables, such as -G linear");
                } else {
                    symbol = FileFormatFZN.newVariable(model, builder, name, domain);
                }

                symbols.put(name, symbol);

                if (Annotation.find(annotations, "output_var") != null) {
                    output.add(new Output(name, domain.type(), null, List.of(symbol)));
                }
            }

        } catch (IllegalArgumentException cause) {
            throw new IllegalArgumentException("Line " + line + ": " + cause.getMessage(), cause);
        }
    }

    /**
     * bool, int, float, 1..9, {1,3,5}, 1.0..9.0, set of int, set of 1..9, set of {1,3,5}
     */
    private static Domain parseDomain(final Lexer lexer) {

        if (lexer.isKeyword("bool")) {
            lexer.advance();
            return Domain.BOOL;
        }
        if (lexer.isKeyword("int")) {
            lexer.advance();
            return Domain.INT;
        }
        if (lexer.isKeyword("float")) {
            lexer.advance();
            return Domain.FLOAT;
        }
        if (lexer.isKeyword("set")) {
            lexer.advance();
            if (!lexer.isKeyword("of")) {
                throw lexer.error("Expected 'of', but found " + lexer.describe());
            }
            lexer.advance();
            FileFormatFZN.parseDomain(lexer);
            return Domain.SET;
        }
        if (lexer.is("{")) {
            return new Domain(Type.INT, FileFormatFZN.parseSet(lexer), null, null);
        }
        if (lexer.type == Lexer.INT || lexer.type == Lexer.FLOAT) {
            boolean real = lexer.type == Lexer.FLOAT;
            String lower = lexer.text;
            lexer.advance();
            lexer.expect("..");
            if (lexer.type != Lexer.INT && lexer.type != Lexer.FLOAT) {
                throw lexer.error("Expected a number, but found " + lexer.describe());
            }
            real |= lexer.type == Lexer.FLOAT;
            String upper = lexer.text;
            lexer.advance();
            if (real) {
                return new Domain(Type.FLOAT, null, new BigDecimal(lower), new BigDecimal(upper));
            }
            return new Domain(Type.INT, IntSet.range(Long.parseLong(lower), Long.parseLong(upper)), null, null);
        }

        throw lexer.error("Expected a type, but found " + lexer.describe());
    }

    /**
     * Literals, names (resolved to what they are), array elements, array literals and – in annotations –
     * annotations and strings.
     */
    private static Object parseExpression(final Lexer lexer, final Map<String, Object> symbols, final boolean annotation) {

        switch (lexer.type) {

            case Lexer.INT: {
                long value = lexer.integer();
                if (lexer.is("..")) {
                    lexer.advance();
                    return IntSet.range(value, lexer.integer());
                }
                return Long.valueOf(value);
            }

            case Lexer.FLOAT: {
                BigDecimal value = new BigDecimal(lexer.text);
                lexer.advance();
                if (lexer.is("..")) {
                    // A float range – only in annotations
                    lexer.advance();
                    lexer.advance();
                }
                return value;
            }

            case Lexer.STRING: {
                String value = lexer.text;
                lexer.advance();
                return value;
            }

            case Lexer.IDENTIFIER: {

                int line = lexer.line;
                String name = lexer.identifier();

                if ("true".equals(name)) {
                    return Boolean.TRUE;
                }
                if ("false".equals(name)) {
                    return Boolean.FALSE;
                }

                if (lexer.is("[")) {
                    lexer.advance();
                    long index = lexer.integer();
                    lexer.expect("]");
                    Object array = symbols.get(name);
                    if (!(array instanceof List)) {
                        throw new IllegalArgumentException("Line " + line + ": '" + name + "' is not an array");
                    }
                    List<?> elements = (List<?>) array;
                    if (index < 1L || index > elements.size()) {
                        throw new IllegalArgumentException("Line " + line + ": " + name + "[" + index + "] is outside the array");
                    }
                    return elements.get((int) index - 1);
                }

                if (lexer.is("(")) {
                    lexer.advance();
                    List<Object> arguments = new ArrayList<>();
                    while (!lexer.is(")")) {
                        if (!arguments.isEmpty()) {
                            lexer.expect(",");
                        }
                        arguments.add(FileFormatFZN.parseExpression(lexer, symbols, true));
                    }
                    lexer.advance();
                    return new Annotation(name, arguments);
                }

                Object value = symbols.get(name);
                if (value != null) {
                    return value;
                }
                if (annotation) {
                    return new Annotation(name, Collections.emptyList());
                }
                throw new IllegalArgumentException("Line " + line + ": '" + name + "' is not declared");
            }

            case Lexer.PUNCTUATION: {

                if (lexer.is("[")) {
                    lexer.advance();
                    List<Object> elements = new ArrayList<>();
                    while (!lexer.is("]")) {
                        if (!elements.isEmpty()) {
                            lexer.expect(",");
                        }
                        elements.add(FileFormatFZN.parseExpression(lexer, symbols, annotation));
                    }
                    lexer.advance();
                    return elements;
                }

                if (lexer.is("{")) {
                    return FileFormatFZN.parseSet(lexer);
                }

                break;
            }

            default:
                break;
        }

        throw lexer.error("Expected a value, but found " + lexer.describe());
    }

    /**
     * {}, {1,3,5}, possibly with ranges as elements
     */
    private static IntSet parseSet(final Lexer lexer) {

        lexer.expect("{");
        List<long[]> ranges = new ArrayList<>();
        while (!lexer.is("}")) {
            if (!ranges.isEmpty()) {
                lexer.expect(",");
            }
            long lower = lexer.integer();
            long upper = lower;
            if (lexer.is("..")) {
                lexer.advance();
                upper = lexer.integer();
            }
            ranges.add(new long[] { lower, upper });
        }
        lexer.advance();
        return IntSet.of(ranges);
    }

    private static Optimisation.Sense parseSolve(final Lexer lexer, final ExpressionsBasedModel model, final Map<String, Object> symbols) {

        lexer.advance();
        FileFormatFZN.parseAnnotations(lexer, symbols);

        int line = lexer.line;
        String goal = lexer.identifier();

        Optimisation.Sense retVal;
        if ("satisfy".equals(goal)) {
            retVal = null;
        } else if ("minimize".equals(goal)) {
            retVal = Optimisation.Sense.MIN;
        } else if ("maximize".equals(goal)) {
            retVal = Optimisation.Sense.MAX;
        } else {
            throw new IllegalArgumentException("Line " + line + ": Expected satisfy, minimize or maximize, but found '" + goal + "'");
        }

        if (retVal != null) {
            Object objective = FileFormatFZN.parseExpression(lexer, symbols, false);
            if (objective instanceof Variable) {
                ((Variable) objective).weight(BigDecimal.ONE);
            } else if (objective instanceof Long || objective instanceof BigDecimal) {
                model.addObjectiveConstant(Builder.number(objective));
            } else {
                throw new IllegalArgumentException("Line " + line + ": The objective is a variable or a number, not " + Builder.describe(objective));
            }
        }

        lexer.expect(";");

        return retVal;
    }

    /**
     * A bool value as true/false, everything else unchanged
     */
    private static Object convert(final Object value, final Type type) {
        if (type == Type.BOOL && value instanceof Long) {
            return Boolean.valueOf(((Long) value).longValue() != 0L);
        }
        if (type == Type.FLOAT && value instanceof Long) {
            return BigDecimal.valueOf(((Long) value).longValue());
        }
        return value;
    }

    private static Variable newVariable(final ExpressionsBasedModel model, final Builder builder, final String name, final Domain domain) {

        Variable retVal = model.newVariable(name);

        switch (domain.type()) {
            case BOOL:
                retVal.binary();
                break;
            case INT:
                retVal.integer();
                break;
            default:
                break;
        }

        FileFormatFZN.restrict(builder, retVal, domain);

        return retVal;
    }

    /**
     * Applies the domain to an existing variable – a new one, or one that is declared again under another
     * name.
     */
    private static void restrict(final Builder builder, final Variable variable, final Domain domain) {
        if (domain.values() != null) {
            if (domain.values().isEmpty()) {
                throw new IllegalArgumentException("The variable '" + variable.getName() + "' has an empty domain");
            }
            builder.restrict(variable, domain.values());
        }
        if (domain.lower() != null && (variable.getLowerLimit() == null || variable.getLowerLimit().compareTo(domain.lower()) < 0)) {
            variable.lower(domain.lower());
        }
        if (domain.upper() != null && (variable.getUpperLimit() == null || variable.getUpperLimit().compareTo(domain.upper()) > 0)) {
            variable.upper(domain.upper());
        }
    }

    /**
     * Skips to the end of the item, past its ';'
     */
    private static void skipItem(final Lexer lexer) {
        int depth = 0;
        while (lexer.type != Lexer.EOF) {
            if (lexer.is("(") || lexer.is("[") || lexer.is("{")) {
                depth++;
            } else if (lexer.is(")") || lexer.is("]") || lexer.is("}")) {
                depth--;
            } else if (depth == 0 && lexer.is(";")) {
                lexer.advance();
                return;
            }
            lexer.advance();
        }
        throw lexer.error("An item that does not end with ';'");
    }

}
