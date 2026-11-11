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

import static org.ojalgo.function.constant.BigMath.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;

/**
 * CPLEX LP format reader and writer.
 * <p>
 * Reading is strict. Anything that is not LP format is an {@link IllegalArgumentException} with the number of
 * the line it is on and what was expected there – never skipped, since a skipped token can change what the
 * model means without anything showing it. Two things beyond the bare format are read as CPLEX reads them:
 * {@code =<} and {@code =>} mean {@code <=} and {@code >=}, and a constant in the objective function is the
 * objective constant.
 *
 * @author apete
 */
abstract class FileFormatLP {

    /**
     * The tokens of an LP file, each with the number of the line it is on, and how far reading has come.
     */
    private static final class Tokens {

        private int[] myLines = new int[256];
        private int myPosition = 0;
        private final List<String> myTokens = new ArrayList<>();

        void add(final String token, final int line) {
            int index = myTokens.size();
            if (index == myLines.length) {
                myLines = Arrays.copyOf(myLines, 2 * index);
            }
            myLines[index] = line;
            myTokens.add(token);
        }

        /**
         * An error at the next token, or at the last one when everything has been read.
         */
        IllegalArgumentException error(final String message) {
            return this.errorAt(myPosition, message);
        }

        /**
         * An error at the given token, with the number of its line. When that line is only a word or two that
         * are not LP format, it may well be a section heading, misspelt or made up, and the message says
         * which headings there are.
         */
        IllegalArgumentException errorAt(final int index, final String message) {
            return this.errorAt(index, message, true);
        }

        /**
         * An error at the given token, with the number of its line, and – if asked for and the line looks
         * like one – the section headings there are.
         */
        IllegalArgumentException errorAt(final int index, final String message, final boolean headings) {

            if (myTokens.isEmpty()) {
                return new IllegalArgumentException(message);
            }

            int at = Math.max(0, Math.min(index, myTokens.size() - 1));

            StringBuilder retVal = new StringBuilder("Line ").append(myLines[at]).append(": ").append(message);

            if (headings && this.isOnlyWords(at)) {
                retVal.append(". If '").append(this.lineOf(at)).append("' is meant to start a section, the LP format has: ");
                retVal.append("Subject To, Bounds, General, Binary and End");
            }

            return new IllegalArgumentException(retVal.toString());
        }

        boolean hasNext() {
            return myPosition < myTokens.size();
        }

        /**
         * Whether the line of the given token is only one or two words, none of them LP format.
         */
        boolean isOnlyWords(final int index) {

            if (index < 0 || index >= myTokens.size()) {
                return false;
            }

            int first = this.firstOnLine(index);
            int last = this.lastOnLine(index);

            if (last - first > 1) {
                return false;
            }

            for (int i = first; i <= last; i++) {
                if (!FileFormatLP.isVariableName(myTokens.get(i))) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Whether the next token is on the same line as the one before it.
         */
        boolean isNextOnSameLine() {
            return myPosition > 0 && myPosition < myTokens.size() && myLines[myPosition] == myLines[myPosition - 1];
        }

        String next() {
            if (myPosition >= myTokens.size()) {
                throw this.error("The model ends where more was expected");
            }
            return myTokens.get(myPosition++);
        }

        /**
         * The next token, or an empty string when everything has been read.
         */
        String peek() {
            return this.peek(0);
        }

        /**
         * The token that far from the next one – negative for those already read – or an empty string when
         * there is none.
         */
        String peek(final int offset) {
            int index = myPosition + offset;
            return index >= 0 && index < myTokens.size() ? myTokens.get(index) : "";
        }

        int position() {
            return myPosition;
        }

        void skip(final int count) {
            myPosition += count;
        }

        private int firstOnLine(final int index) {
            int retVal = index;
            while (retVal > 0 && myLines[retVal - 1] == myLines[index]) {
                retVal--;
            }
            return retVal;
        }

        private int lastOnLine(final int index) {
            int retVal = index;
            while (retVal + 1 < myTokens.size() && myLines[retVal + 1] == myLines[index]) {
                retVal++;
            }
            return retVal;
        }

        private String lineOf(final int index) {
            return String.join(" ", myTokens.subList(this.firstOnLine(index), this.lastOnLine(index) + 1));
        }

    }

    /**
     * A bound's value: a number, or null for an infinite one, which is no bound.
     */
    private static BigDecimal boundValue(final Tokens tokens) {

        boolean negative = FileFormatLP.signs(tokens);

        String lower = tokens.peek().toLowerCase();
        if ("inf".equals(lower) || "infinity".equals(lower)) {
            tokens.skip(1);
            return null;
        }

        if (!FileFormatLP.isNumber(tokens.peek())) {
            throw tokens.error("A bound is a number, or inf, not '" + tokens.peek() + "'");
        }

        BigDecimal value = FileFormatLP.number(tokens);

        return negative ? value.negate() : value;
    }

    /**
     * The comparison a token means, as one of {@code <=}, {@code >=} and {@code =}.
     */
    private static String comparison(final String token) {
        return switch (token) {
            case "<=", "<", "=<" -> "<=";
            case ">=", ">", "=>" -> ">=";
            default              -> "=";
        };
    }

    private static String formatNumber(final BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static Variable getOrCreate(final String name, final ExpressionsBasedModel model, final Map<String, Variable> map) {
        Variable var = map.get(name);
        if (var == null) {
            var = model.newVariable(name);
            var.lower(ZERO); // CPLEX LP default: 0 <= x
            map.put(name, var);
        }
        return var;
    }

    /**
     * Whether a constraint starts with a number and a comparison, as in {@code 2 <= x + y <= 10}.
     */
    private static boolean hasLeadingBound(final Tokens tokens) {
        int offset = 0;
        while ("+".equals(tokens.peek(offset)) || "-".equals(tokens.peek(offset))) {
            offset++;
        }
        return FileFormatLP.isNumber(tokens.peek(offset)) && FileFormatLP.isComparison(tokens.peek(offset + 1));
    }

    private static boolean isComparison(final String token) {
        return switch (token) {
            case "<=", "<", "=<", ">=", ">", "=>", "=" -> true;
            default                                    -> false;
        };
    }

    private static boolean isConstraintSection(final String lower) {
        return "subject".equals(lower) || "st".equals(lower) || "s.t.".equals(lower) || "such".equals(lower);
    }

    /**
     * Whether the next token is a name followed by a colon: the name of a constraint, or of the objective.
     */
    private static boolean isLabel(final Tokens tokens) {
        return FileFormatLP.isName(tokens.peek()) && ":".equals(tokens.peek(1));
    }

    private static boolean isMaximise(final String lower) {
        return "maximize".equals(lower) || "maximise".equals(lower) || "maximum".equals(lower) || "max".equals(lower);
    }

    private static boolean isMinimise(final String lower) {
        return "minimize".equals(lower) || "minimise".equals(lower) || "minimum".equals(lower) || "min".equals(lower);
    }

    private static boolean isName(final String token) {
        if (token.isEmpty()) {
            return false;
        }
        char c = token.charAt(0);
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isNumber(final String token) {
        if (token.isEmpty()) {
            return false;
        }
        char c = token.charAt(0);
        return Character.isDigit(c) || c == '.';
    }

    private static boolean isSectionKeyword(final String token) {
        String lower = token.toLowerCase();
        return FileFormatLP.isMinimise(lower) || FileFormatLP.isMaximise(lower) || FileFormatLP.isConstraintSection(lower) || "bounds".equals(lower)
                || "bound".equals(lower) || "generals".equals(lower) || "general".equals(lower) || "gen".equals(lower) || "binaries".equals(lower)
                || "binary".equals(lower) || "bin".equals(lower) || "end".equals(lower) || FileFormatLP.isUnsupportedSection(lower);
    }

    /**
     * Whether the next token is a section heading – a keyword not followed by a colon, which would make it a
     * name.
     */
    private static boolean isSectionKeywordAt(final Tokens tokens) {
        return FileFormatLP.isSectionKeyword(tokens.peek()) && !":".equals(tokens.peek(1));
    }

    private static boolean isUnsupportedSection(final String lower) {
        return "sos".equals(lower) || "sos1".equals(lower) || "sos2".equals(lower) || "semi-continuous".equals(lower) || "semi".equals(lower)
                || "indicator".equals(lower) || "indicators".equals(lower) || "lazy".equals(lower) || "pwl".equals(lower);
    }

    /**
     * Whether a token can name a variable: a name that is neither a section keyword nor infinity.
     */
    private static boolean isVariableName(final String token) {
        if (!FileFormatLP.isName(token) || FileFormatLP.isSectionKeyword(token)) {
            return false;
        }
        String lower = token.toLowerCase();
        return !"inf".equals(lower) && !"infinity".equals(lower);
    }

    /**
     * Whether the next token names a variable, rather than a constraint.
     */
    private static boolean isVariableNameAt(final Tokens tokens) {
        return FileFormatLP.isVariableName(tokens.peek()) && !":".equals(tokens.peek(1));
    }

    /**
     * Sets the limit that a comparison with a value gives. A null value is infinite: no limit.
     */
    private static void limit(final ModelEntity<?> entity, final String comparison, final BigDecimal value) {
        switch (comparison) {
            case "<=" -> entity.upper(value);
            case ">=" -> entity.lower(value);
            default   -> entity.level(value);
        }
    }

    private static BigDecimal number(final Tokens tokens) {
        String token = tokens.peek();
        try {
            BigDecimal retVal = new BigDecimal(token);
            tokens.skip(1);
            return retVal;
        } catch (NumberFormatException cause) {
            throw tokens.error("'" + token + "' is not a number");
        }
    }

    /**
     * The value after a variable and a comparison in Bounds, and the limit it gives.
     */
    private static void parseBoundLimit(final Tokens tokens, final Variable variable, final String comparison) {

        int at = tokens.position();
        BigDecimal value = FileFormatLP.boundValue(tokens);

        if (value == null && "=".equals(comparison)) {
            throw tokens.errorAt(at, "A variable cannot equal infinity");
        }

        FileFormatLP.limit(variable, comparison, value);
    }

    private static void parseBounds(final Tokens tokens, final ExpressionsBasedModel model, final Map<String, Variable> variables) {

        while (tokens.hasNext() && !FileFormatLP.isSectionKeywordAt(tokens)) {

            int start = tokens.position();

            if (FileFormatLP.isVariableNameAt(tokens)) {

                String name = tokens.next();
                Variable variable = FileFormatLP.getOrCreate(name, model, variables);

                if ("free".equalsIgnoreCase(tokens.peek())) {
                    tokens.skip(1);
                    variable.lower(null).upper(null);
                } else if (FileFormatLP.isComparison(tokens.peek())) {
                    String comparison = FileFormatLP.comparison(tokens.next());
                    FileFormatLP.parseBoundLimit(tokens, variable, comparison);
                } else {
                    throw tokens.errorAt(start, "<=, >=, = or free expected after '" + name + "' in Bounds, not '" + tokens.peek() + "'");
                }

            } else if (FileFormatLP.isNumber(tokens.peek()) || "-".equals(tokens.peek()) || "+".equals(tokens.peek()) || "inf".equalsIgnoreCase(tokens.peek())
                    || "infinity".equalsIgnoreCase(tokens.peek())) {

                BigDecimal value = FileFormatLP.boundValue(tokens);

                if (!FileFormatLP.isComparison(tokens.peek())) {
                    throw tokens.error("<=, >= or = expected after a bound, not '" + tokens.peek() + "'");
                }
                String comparison = FileFormatLP.reversed(FileFormatLP.comparison(tokens.next()));

                if (!FileFormatLP.isVariableNameAt(tokens)) {
                    throw tokens.error("A variable expected in Bounds, not '" + tokens.peek() + "'");
                }
                Variable variable = FileFormatLP.getOrCreate(tokens.next(), model, variables);

                if (value == null && "=".equals(comparison)) {
                    throw tokens.errorAt(start, "A variable cannot equal infinity");
                }
                FileFormatLP.limit(variable, comparison, value);

                if (FileFormatLP.isComparison(tokens.peek())) {
                    String second = FileFormatLP.comparison(tokens.next());
                    FileFormatLP.parseBoundLimit(tokens, variable, second);
                }

            } else {
                throw tokens.error(FileFormatLP.unexpected(tokens.peek()));
            }
        }
    }

    private static void parseConstraints(final Tokens tokens, final ExpressionsBasedModel model, final Map<String, Variable> variables) {

        int unnamed = 0;

        while (tokens.hasNext() && !FileFormatLP.isSectionKeywordAt(tokens)) {

            int start = tokens.position();

            String name;
            if (FileFormatLP.isLabel(tokens)) {
                name = tokens.next();
                tokens.skip(1);
                if (model.getExpression(name) != null) {
                    throw tokens.errorAt(start, "There is already a constraint named '" + name + "'");
                }
            } else {
                do {
                    name = "c" + unnamed++;
                } while (model.getExpression(name) != null);
            }

            String leadingComparison = null;
            BigDecimal leading = null;
            if (FileFormatLP.hasLeadingBound(tokens)) {
                leading = FileFormatLP.signedNumber(tokens, "A number expected");
                leadingComparison = FileFormatLP.reversed(FileFormatLP.comparison(tokens.next()));
            }

            Expression expression = model.newExpression(name);
            FileFormatLP.parseExpression(tokens, model, variables, expression, false);

            if (FileFormatLP.isComparison(tokens.peek())) {

                String comparison = FileFormatLP.comparison(tokens.next());
                BigDecimal value = FileFormatLP.signedNumber(tokens,
                        "The right-hand side of " + name + " has to be a number, not '" + tokens.peek() + "' – variables go on the left-hand side");
                FileFormatLP.limit(expression, comparison, value);

                if (tokens.isNextOnSameLine() && !FileFormatLP.isLabel(tokens) && !FileFormatLP.isSectionKeywordAt(tokens)) {
                    throw tokens.error("'" + tokens.peek() + "' after the right-hand side of " + name
                            + ": the right-hand side is a single number, and variables go on the left-hand side");
                }

            } else if (leading == null) {
                throw tokens.error("<=, >= or = expected after the left-hand side of " + name + ", not '" + tokens.peek() + "'");
            }

            if (leading != null) {
                FileFormatLP.limit(expression, leadingComparison, leading);
            }
        }
    }

    /**
     * Reads terms up to a comparison, a section heading, a constraint name or the end: a linear part, and a
     * quadratic part in square brackets. A variable is created where it is first named.
     *
     * @return The constant terms added up – only the objective function may have any
     */
    private static BigDecimal parseExpression(final Tokens tokens, final ExpressionsBasedModel model, final Map<String, Variable> variables,
            final Expression expression, final boolean objective) {

        BigDecimal constant = ZERO;
        boolean first = true;

        while (tokens.hasNext() && !FileFormatLP.isComparison(tokens.peek()) && !FileFormatLP.isSectionKeywordAt(tokens) && !FileFormatLP.isLabel(tokens)) {

            if (!first && !"+".equals(tokens.peek()) && !"-".equals(tokens.peek())) {
                if (!FileFormatLP.isName(tokens.peek()) && !FileFormatLP.isNumber(tokens.peek()) && !"[".equals(tokens.peek())) {
                    throw tokens.error(FileFormatLP.unexpected(tokens.peek()));
                }
                int at = tokens.position();
                int index = !tokens.isNextOnSameLine() && tokens.isOnlyWords(at - 1) ? at - 1 : at;
                throw tokens.errorAt(index, "+ or - missing between '" + tokens.peek(-1) + "' and '" + tokens.peek() + "'");
            }
            first = false;

            boolean negative = FileFormatLP.signs(tokens);
            String token = tokens.peek();

            if ("[".equals(token)) {

                tokens.skip(1);
                FileFormatLP.parseQuadratic(tokens, model, variables, expression, negative);

            } else if (FileFormatLP.isNumber(token)) {

                int at = tokens.position();
                BigDecimal value = FileFormatLP.number(tokens);
                if (negative) {
                    value = value.negate();
                }

                if (FileFormatLP.isVariableNameAt(tokens)) {
                    expression.add(FileFormatLP.parseVariable(tokens, model, variables), value);
                } else if ("*".equals(tokens.peek())) {
                    throw tokens.error("A coefficient and its variable are written with a space between them, as in '3 x', not '3 * x'");
                } else if (objective) {
                    constant = constant.add(value);
                } else {
                    throw tokens.errorAt(at, "A constant, " + token + ", in the left-hand side – constants go on the right-hand side");
                }

            } else if (FileFormatLP.isVariableNameAt(tokens)) {

                expression.add(FileFormatLP.parseVariable(tokens, model, variables), negative ? -1L : 1L);

            } else {
                throw tokens.error(FileFormatLP.unexpected(token));
            }
        }

        return constant;
    }

    private static void parseIntegers(final Tokens tokens, final ExpressionsBasedModel model, final Map<String, Variable> variables, final boolean binary) {

        while (tokens.hasNext() && !FileFormatLP.isSectionKeywordAt(tokens)) {

            if (!FileFormatLP.isVariableNameAt(tokens)) {
                throw tokens.error("A variable name expected in " + (binary ? "Binary" : "General") + ", not '" + tokens.peek() + "'");
            }

            Variable variable = FileFormatLP.getOrCreate(tokens.next(), model, variables);

            if (binary) {
                variable.lower(ZERO).upper(ONE).integer(true);
            } else {
                variable.integer(true);
            }
        }
    }

    /**
     * The terms of one pair of square brackets, and the {@code / 2} that may follow it.
     */
    private static void parseQuadratic(final Tokens tokens, final ExpressionsBasedModel model, final Map<String, Variable> variables,
            final Expression expression, final boolean negative) {

        List<Variable> left = new ArrayList<>();
        List<Variable> right = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        while (!"]".equals(tokens.peek())) {

            if (!tokens.hasNext()) {
                throw tokens.error("'[' without ']'");
            }
            if (!values.isEmpty() && !"+".equals(tokens.peek()) && !"-".equals(tokens.peek())) {
                throw tokens.error("+ or - missing between '" + tokens.peek(-1) + "' and '" + tokens.peek() + "'");
            }

            BigDecimal value = FileFormatLP.signs(tokens) ? ONE.negate() : ONE;
            if (FileFormatLP.isNumber(tokens.peek())) {
                value = value.multiply(FileFormatLP.number(tokens));
            }

            if (!FileFormatLP.isVariableNameAt(tokens)) {
                throw tokens.error("Inside [ ], a term is a square, as in 3 x ^2, or a product, as in 3 x * y – not '" + tokens.peek() + "'");
            }
            Variable variable = FileFormatLP.getOrCreate(tokens.next(), model, variables);

            if ("^".equals(tokens.peek())) {
                tokens.skip(1);
                if (!FileFormatLP.isNumber(tokens.peek()) || FileFormatLP.number(tokens).compareTo(TWO) != 0) {
                    throw tokens.errorAt(tokens.position() - 1, "Only squares, ^2, are allowed");
                }
                left.add(variable);
                right.add(variable);
            } else if ("*".equals(tokens.peek())) {
                tokens.skip(1);
                if (!FileFormatLP.isVariableNameAt(tokens)) {
                    throw tokens.error("A variable expected after '*', not '" + tokens.peek() + "'");
                }
                left.add(variable);
                right.add(FileFormatLP.getOrCreate(tokens.next(), model, variables));
            } else {
                throw tokens.error("Inside [ ], a term is a square, as in 3 x ^2, or a product, as in 3 x * y – '" + variable.getName() + "' alone is not");
            }

            values.add(value);
        }

        tokens.skip(1);

        boolean half = false;
        if ("/".equals(tokens.peek())) {
            tokens.skip(1);
            if (!FileFormatLP.isNumber(tokens.peek()) || FileFormatLP.number(tokens).compareTo(TWO) != 0) {
                throw tokens.errorAt(tokens.position() - 1, "Only / 2 may follow ]");
            }
            half = true;
        }

        for (int i = 0; i < values.size(); i++) {
            BigDecimal value = negative ? values.get(i).negate() : values.get(i);
            expression.add(left.get(i), right.get(i), half ? value.divide(TWO) : value);
        }
    }

    /**
     * A variable in a linear term, which is not followed by anything that would make it part of a quadratic
     * one.
     */
    private static Variable parseVariable(final Tokens tokens, final ExpressionsBasedModel model, final Map<String, Variable> variables) {

        String name = tokens.next();

        if ("^".equals(tokens.peek())) {
            throw tokens.error("Squares go in square brackets, as in [ " + name + " ^2 ]");
        }
        if ("*".equals(tokens.peek())) {
            if (FileFormatLP.isNumber(tokens.peek(1))) {
                throw tokens.error(
                        "A coefficient goes before its variable, as in '" + tokens.peek(1) + " " + name + "', not '" + name + " * " + tokens.peek(1) + "'");
            }
            throw tokens.error("Products of variables go in square brackets, as in [ " + name + " * " + tokens.peek(1) + " ]");
        }

        return FileFormatLP.getOrCreate(name, model, variables);
    }

    /**
     * The comparison that holds with its two sides swapped.
     */
    private static String reversed(final String comparison) {
        return switch (comparison) {
            case "<=" -> ">=";
            case ">=" -> "<=";
            default   -> comparison;
        };
    }

    /**
     * A number with any signs before it.
     *
     * @param expected The message when there is no number
     */
    private static BigDecimal signedNumber(final Tokens tokens, final String expected) {

        boolean negative = FileFormatLP.signs(tokens);

        if (!FileFormatLP.isNumber(tokens.peek())) {
            throw tokens.error(expected);
        }

        BigDecimal value = FileFormatLP.number(tokens);

        return negative ? value.negate() : value;
    }

    /**
     * Reads any signs.
     *
     * @return true if they make a minus
     */
    private static boolean signs(final Tokens tokens) {
        boolean retVal = false;
        while ("+".equals(tokens.peek()) || "-".equals(tokens.peek())) {
            if ("-".equals(tokens.next())) {
                retVal = !retVal;
            }
        }
        return retVal;
    }

    /**
     * The rest of a constraints section heading: "Subject" is followed by "To", and "Such" by "That".
     */
    private static void skipHeading(final Tokens tokens, final String keyword) {
        if ("subject".equals(keyword) && !"to".equalsIgnoreCase(tokens.next())) {
            throw tokens.errorAt(tokens.position() - 1, "'Subject' is followed by 'To'");
        }
        if ("such".equals(keyword) && !"that".equalsIgnoreCase(tokens.next())) {
            throw tokens.errorAt(tokens.position() - 1, "'Such' is followed by 'That'");
        }
    }

    private static Tokens tokenize(final InputStream input) {

        Tokens tokens = new Tokens();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            int number = 0;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                number++;
                int comment = line.indexOf('\\');
                FileFormatLP.tokenize(comment >= 0 ? line.substring(0, comment) : line, number, tokens);
            }
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

        return tokens;
    }

    /**
     * Splits one line into tokens. A character that is not part of the LP format becomes a token of its own,
     * for the reader to refuse.
     */
    private static void tokenize(final String text, final int line, final Tokens tokens) {

        int i = 0;
        int len = text.length();

        while (i < len) {
            char c = text.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Two-character operators
            if (i + 1 < len) {
                char next = text.charAt(i + 1);
                if ((c == '<' || c == '>') && next == '=' || c == '=' && (next == '<' || next == '>')) {
                    tokens.add(String.valueOf(c) + next, line);
                    i += 2;
                    continue;
                }
            }

            // Numbers
            if (Character.isDigit(c) || (c == '.' && i + 1 < len && Character.isDigit(text.charAt(i + 1)))) {
                StringBuilder num = new StringBuilder();
                while (i < len && (Character.isDigit(text.charAt(i)) || text.charAt(i) == '.')) {
                    num.append(text.charAt(i));
                    i++;
                }
                if (i < len && (text.charAt(i) == 'e' || text.charAt(i) == 'E')) {
                    num.append(text.charAt(i));
                    i++;
                    if (i < len && (text.charAt(i) == '+' || text.charAt(i) == '-')) {
                        num.append(text.charAt(i));
                        i++;
                    }
                    while (i < len && Character.isDigit(text.charAt(i))) {
                        num.append(text.charAt(i));
                        i++;
                    }
                }
                tokens.add(num.toString(), line);
                continue;
            }

            // Names (identifiers)
            if (Character.isLetter(c) || c == '_') {
                StringBuilder name = new StringBuilder();
                while (i < len && (Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '_' || text.charAt(i) == '.')) {
                    name.append(text.charAt(i));
                    i++;
                }
                tokens.add(name.toString(), line);
                continue;
            }

            // Single-character operators, and anything else
            tokens.add(String.valueOf(c), line);
            i++;
        }
    }

    /**
     * What to say about a token that cannot be where it is.
     */
    private static String unexpected(final String token) {
        return switch (token) {
            case ""       -> "The model ends where more was expected";
            case "*"      ->
                "Unexpected '*': a coefficient and its variable are written with a space between them, as in 3 x, and products of variables go in square brackets, as in [ x * y ]";
            case "/"      -> "Unexpected '/': the LP format has no division – write 0.5 x, not x / 2";
            case "^"      -> "Unexpected '^': squares go in square brackets, as in [ x ^2 ]";
            case "(", ")" -> "Unexpected '" + token + "': the LP format has no parentheses – multiply out";
            default       -> FileFormatLP.isComparison(token) ? "A term expected before '" + token + "'" : "Unexpected '" + token + "'";
        };
    }

    private static void writeBound(final BufferedWriter writer, final Variable var) throws IOException {

        BigDecimal lower = var.getLowerLimit();
        BigDecimal upper = var.getUpperLimit();
        String name = var.getName();

        if (lower == null && upper == null) {
            writer.write("  ");
            writer.write(name);
            writer.write(" Free");
            writer.newLine();
            return;
        }

        boolean isDefault = lower != null && lower.signum() == 0 && upper == null;
        if (isDefault) {
            return;
        }

        writer.write("  ");
        if (lower != null) {
            writer.write(FileFormatLP.formatNumber(lower));
        } else {
            writer.write("-Inf");
        }
        writer.write(" <= ");
        writer.write(name);
        if (upper != null) {
            writer.write(" <= ");
            writer.write(FileFormatLP.formatNumber(upper));
        }
        writer.newLine();
    }

    private static void writeCoefficient(final BufferedWriter writer, final BigDecimal absValue) throws IOException {
        if (absValue.compareTo(BigDecimal.ONE) != 0) {
            writer.write(FileFormatLP.formatNumber(absValue));
            writer.write(' ');
        }
    }

    private static void writeConstraint(final BufferedWriter writer, final List<Variable> variables, final Expression expr) throws IOException {

        Optimisation.ConstraintType type = expr.getConstraintType();

        writer.write("  ");
        writer.write(expr.getName());
        writer.write(":");

        switch (type) {
            case RANGE:
                writer.write(' ');
                writer.write(FileFormatLP.formatNumber(expr.getLowerLimit()));
                writer.write(" <=");
                FileFormatLP.writeLinearTerms(writer, variables, expr);
                if (expr.isAnyQuadraticFactorNonZero()) {
                    FileFormatLP.writeQuadraticTerms(writer, variables, expr, false);
                }
                writer.write(" <= ");
                writer.write(FileFormatLP.formatNumber(expr.getUpperLimit()));
                break;
            case EQUALITY:
                FileFormatLP.writeExpressionTerms(writer, variables, expr);
                writer.write(" = ");
                writer.write(FileFormatLP.formatNumber(expr.getLowerLimit()));
                break;
            case LOWER:
                FileFormatLP.writeExpressionTerms(writer, variables, expr);
                writer.write(" >= ");
                writer.write(FileFormatLP.formatNumber(expr.getLowerLimit()));
                break;
            case UPPER:
                FileFormatLP.writeExpressionTerms(writer, variables, expr);
                writer.write(" <= ");
                writer.write(FileFormatLP.formatNumber(expr.getUpperLimit()));
                break;
            default:
                return;
        }
        writer.newLine();
    }

    private static void writeExpressionTerms(final BufferedWriter writer, final List<Variable> variables, final Expression expr) throws IOException {
        FileFormatLP.writeLinearTerms(writer, variables, expr);
        if (expr.isAnyQuadraticFactorNonZero()) {
            FileFormatLP.writeQuadraticTerms(writer, variables, expr, false);
        }
    }

    private static void writeLinearTerms(final BufferedWriter writer, final List<Variable> variables, final Expression expression) throws IOException {
        boolean first = true;
        for (Entry<IntIndex, BigDecimal> entry : expression.getLinearEntrySet()) {
            BigDecimal value = entry.getValue();
            if (value.signum() == 0) {
                continue;
            }

            String varName = variables.get(entry.getKey().index).getName();

            if (first) {
                if (value.signum() < 0) {
                    writer.write(" -");
                }
                writer.write(" ");
                FileFormatLP.writeCoefficient(writer, value.abs());
                writer.write(varName);
                first = false;
            } else {
                writer.write(value.signum() > 0 ? " + " : " - ");
                FileFormatLP.writeCoefficient(writer, value.abs());
                writer.write(varName);
            }
        }
    }

    private static void writeQuadraticTerms(final BufferedWriter writer, final List<Variable> variables, final Expression expression,
            final boolean objectiveHalf) throws IOException {

        writer.write(" + [");

        boolean first = true;
        for (Entry<IntRowColumn, BigDecimal> entry : expression.getQuadraticEntrySet()) {
            int row = entry.getKey().row;
            int col = entry.getKey().column;

            if (row > col) {
                continue;
            }

            BigDecimal value = entry.getValue();

            if (row == col) {
                if (objectiveHalf) {
                    value = value.multiply(TWO);
                }
            } else {
                BigDecimal transpose = expression.get(new IntRowColumn(col, row));
                if (objectiveHalf) {
                    value = value.add(transpose).multiply(TWO);
                } else {
                    value = value.add(transpose);
                }
            }

            if (value.signum() == 0) {
                continue;
            }

            String var1 = variables.get(row).getName();
            String var2 = variables.get(col).getName();

            if (first) {
                if (value.signum() < 0) {
                    writer.write(" -");
                }
                writer.write(" ");
                first = false;
            } else {
                writer.write(value.signum() > 0 ? " + " : " - ");
            }

            FileFormatLP.writeCoefficient(writer, value.abs());

            if (row == col) {
                writer.write(var1);
                writer.write(" ^2");
            } else {
                writer.write(var1);
                writer.write(" * ");
                writer.write(var2);
            }
        }

        writer.write(" ]");
        if (objectiveHalf) {
            writer.write(" / 2");
        }
    }

    static ExpressionsBasedModel read(final InputStream input, final Supplier<ExpressionsBasedModel> factory) {

        Tokens tokens = FileFormatLP.tokenize(input);

        if (!tokens.hasNext()) {
            throw new IllegalArgumentException("Nothing to read: an LP model starts with Minimize or Maximize");
        }

        ExpressionsBasedModel model = factory.get();
        Map<String, Variable> variables = new LinkedHashMap<>();

        String sense = tokens.peek().toLowerCase();
        if (FileFormatLP.isMinimise(sense)) {
            model.setOptimisationSense(Optimisation.Sense.MIN);
        } else if (FileFormatLP.isMaximise(sense)) {
            model.setOptimisationSense(Optimisation.Sense.MAX);
        } else {
            throw tokens.errorAt(tokens.position(), "An LP model starts with Minimize or Maximize, not '" + tokens.peek() + "'", false);
        }
        tokens.skip(1);

        if (FileFormatLP.isLabel(tokens)) {
            tokens.skip(2);
        }

        Expression objective = model.newExpression("obj");
        objective.weight(ONE);
        BigDecimal constant = FileFormatLP.parseExpression(tokens, model, variables, objective, true);
        if (constant.signum() != 0) {
            objective.addObjectiveConstant(constant);
        }

        while (tokens.hasNext()) {

            if (!FileFormatLP.isSectionKeywordAt(tokens)) {
                if (FileFormatLP.isLabel(tokens)) {
                    throw tokens.error("'" + tokens.peek() + ":' starts a constraint, but there is no Subject To before it");
                }
                if (FileFormatLP.isComparison(tokens.peek())) {
                    throw tokens.error("The objective function has no '" + tokens.peek() + "' – constraints go under Subject To");
                }
                throw tokens.error(FileFormatLP.unexpected(tokens.peek()));
            }

            String keyword = tokens.next().toLowerCase();

            if ("end".equals(keyword)) {
                if (tokens.hasNext()) {
                    throw tokens.error("Nothing may follow End, but '" + tokens.peek() + "' does");
                }
            } else if (FileFormatLP.isConstraintSection(keyword)) {
                FileFormatLP.skipHeading(tokens, keyword);
                FileFormatLP.parseConstraints(tokens, model, variables);
            } else if ("bounds".equals(keyword) || "bound".equals(keyword)) {
                FileFormatLP.parseBounds(tokens, model, variables);
            } else if ("generals".equals(keyword) || "general".equals(keyword) || "gen".equals(keyword)) {
                FileFormatLP.parseIntegers(tokens, model, variables, false);
            } else if ("binaries".equals(keyword) || "binary".equals(keyword) || "bin".equals(keyword)) {
                FileFormatLP.parseIntegers(tokens, model, variables, true);
            } else if (FileFormatLP.isUnsupportedSection(keyword)) {
                throw tokens.errorAt(tokens.position() - 1, "Unsupported LP file section: " + tokens.peek(-1));
            } else {
                throw tokens.errorAt(tokens.position() - 1, "A second objective function: '" + tokens.peek(-1) + "'");
            }
        }

        return model;
    }

    static void write(final ExpressionsBasedModel model, final OutputStream output) {

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {

            List<Variable> variables = model.getVariables();
            Expression objective = model.objective();
            Optimisation.Sense sense = model.getOptimisationSense();

            // Sense
            writer.write(sense == Optimisation.Sense.MAX ? "Maximize" : "Minimize");
            writer.newLine();

            // Objective
            writer.write("  obj:");
            FileFormatLP.writeLinearTerms(writer, variables, objective);
            if (objective.isAnyQuadraticFactorNonZero()) {
                FileFormatLP.writeQuadraticTerms(writer, variables, objective, true);
            }
            BigDecimal constant = objective.getConstant();
            if (constant.signum() != 0) {
                writer.write(constant.signum() > 0 ? " + " : " - ");
                writer.write(FileFormatLP.formatNumber(constant.abs()));
            }
            writer.newLine();

            // Constraints
            writer.write("Subject To");
            writer.newLine();

            for (Expression expr : model.getExpressions()) {
                if (!expr.isConstraint()) {
                    continue;
                }
                FileFormatLP.writeConstraint(writer, variables, expr);
            }

            // Bounds
            writer.write("Bounds");
            writer.newLine();
            for (Variable var : variables) {
                FileFormatLP.writeBound(writer, var);
            }

            // Generals and Binary
            List<Variable> generals = new ArrayList<>();
            List<Variable> binaries = new ArrayList<>();
            for (Variable var : variables) {
                if (var.isBinary()) {
                    binaries.add(var);
                } else if (var.isInteger()) {
                    generals.add(var);
                }
            }

            if (!generals.isEmpty()) {
                writer.write("Generals");
                writer.newLine();
                StringBuilder sb = new StringBuilder(" ");
                for (Variable var : generals) {
                    sb.append(' ').append(var.getName());
                }
                writer.write(sb.toString());
                writer.newLine();
            }

            if (!binaries.isEmpty()) {
                writer.write("Binary");
                writer.newLine();
                StringBuilder sb = new StringBuilder(" ");
                for (Variable var : binaries) {
                    sb.append(' ').append(var.getName());
                }
                writer.write(sb.toString());
                writer.newLine();
            }

            writer.write("End");
            writer.newLine();

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

}
