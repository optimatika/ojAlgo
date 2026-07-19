/*
 * Copyright 1997-2025 Optimatika
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;

/**
 * CPLEX LP format reader and writer.
 *
 * @author apete
 */
abstract class FileFormatLP {

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

    private static boolean hasLeadingBound(final List<String> tokens, final int[] pos) {
        int i = pos[0];
        if (i >= tokens.size()) {
            return false;
        }
        String token = tokens.get(i);
        if ("-".equals(token) || "+".equals(token)) {
            i++;
            if (i >= tokens.size()) {
                return false;
            }
            token = tokens.get(i);
        }
        if (!FileFormatLP.isNumber(token)) {
            return false;
        }
        i++;
        return i < tokens.size() && FileFormatLP.isComparisonOp(tokens.get(i));
    }

    private static boolean isComparisonOp(final String token) {
        return "<=".equals(token) || ">=".equals(token) || "=".equals(token) || "<".equals(token) || ">".equals(token);
    }

    private static boolean isConstraintSection(final String lower) {
        return "subject".equals(lower) || "st".equals(lower) || "s.t.".equals(lower) || "such".equals(lower);
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
        return "minimize".equals(lower) || "min".equals(lower) || "minimum".equals(lower) || "maximize".equals(lower) || "max".equals(lower)
                || "maximum".equals(lower) || FileFormatLP.isConstraintSection(lower) || "bounds".equals(lower) || "bound".equals(lower)
                || "generals".equals(lower) || "general".equals(lower) || "gen".equals(lower) || "binaries".equals(lower) || "binary".equals(lower)
                || "bin".equals(lower) || "end".equals(lower) || FileFormatLP.isUnsupportedSection(lower);
    }

    private static boolean isUnsupportedSection(final String lower) {
        return "sos".equals(lower) || "sos1".equals(lower) || "sos2".equals(lower) || "semi-continuous".equals(lower) || "semi".equals(lower)
                || "indicator".equals(lower) || "indicators".equals(lower) || "lazy".equals(lower) || "pwl".equals(lower);
    }

    private static boolean isSectionKeywordAt(final List<String> tokens, final int i) {
        if (i >= tokens.size()) {
            return false;
        }
        if (i + 1 < tokens.size() && ":".equals(tokens.get(i + 1))) {
            return false;
        }
        return FileFormatLP.isSectionKeyword(tokens.get(i));
    }

    private static boolean isVariableName(final String token) {
        if (token.isEmpty()) {
            return false;
        }
        char c = token.charAt(0);
        return (Character.isLetter(c) || c == '_') && !FileFormatLP.isSectionKeyword(token);
    }

    private static void parseBinaries(final List<String> tokens, final int[] pos, final Map<String, Variable> variableMap) {
        while (pos[0] < tokens.size()) {
            if (FileFormatLP.isSectionKeywordAt(tokens, pos[0])) {
                break;
            }
            String name = tokens.get(pos[0]);
            Variable var = variableMap.get(name);
            if (var != null) {
                var.lower(ZERO).upper(ONE).integer(true);
            }
            pos[0]++;
        }
    }

    private static void parseBounds(final List<String> tokens, final int[] pos, final ExpressionsBasedModel model, final Map<String, Variable> variableMap) {

        while (pos[0] < tokens.size()) {
            if (FileFormatLP.isSectionKeywordAt(tokens, pos[0])) {
                break;
            }

            String token = tokens.get(pos[0]);
            String lower = token.toLowerCase();

            if ("free".equals(lower)
                    || (FileFormatLP.isVariableName(token) && pos[0] + 1 < tokens.size() && "free".equals(tokens.get(pos[0] + 1).toLowerCase()))) {
                // "variable free" or "free variable" — shouldn't happen as "free" first, but handle "var free"
                if (FileFormatLP.isVariableName(token)) {
                    Variable var = FileFormatLP.getOrCreate(token, model, variableMap);
                    pos[0] += 2; // skip name and "free"
                    var.lower(null).upper(null);
                } else {
                    pos[0]++;
                }
                continue;
            }

            // Try: number <= var [<= number]
            // Or: -inf <= var [<= number]
            // Or: var >= number
            // Or: var <= number
            // Or: var = number

            if (FileFormatLP.isNumber(token) || "-".equals(token) || "inf".equals(lower) || "infinity".equals(lower)) {
                BigDecimal lb = FileFormatLP.parseBoundValue(tokens, pos);

                if (pos[0] < tokens.size() && FileFormatLP.isComparisonOp(tokens.get(pos[0]))) {
                    pos[0]++; // skip <=

                    if (pos[0] < tokens.size() && FileFormatLP.isVariableName(tokens.get(pos[0]))) {
                        String varName = tokens.get(pos[0]);
                        Variable var = FileFormatLP.getOrCreate(varName, model, variableMap);
                        pos[0]++;

                        var.lower(lb);

                        if (pos[0] < tokens.size() && FileFormatLP.isComparisonOp(tokens.get(pos[0]))) {
                            pos[0]++; // skip <=
                            BigDecimal ub = FileFormatLP.parseBoundValue(tokens, pos);
                            if (ub != null) {
                                var.upper(ub);
                            }
                        }
                    }
                }
            } else if (FileFormatLP.isVariableName(token)) {
                String varName = token;
                Variable var = FileFormatLP.getOrCreate(varName, model, variableMap);
                pos[0]++;

                if (pos[0] < tokens.size()) {
                    String next = tokens.get(pos[0]).toLowerCase();
                    if ("free".equals(next)) {
                        var.lower(null).upper(null);
                        pos[0]++;
                    } else if (FileFormatLP.isComparisonOp(tokens.get(pos[0]))) {
                        String op = tokens.get(pos[0]);
                        pos[0]++;
                        BigDecimal val = FileFormatLP.parseBoundValue(tokens, pos);
                        if (val != null) {
                            if ("<=".equals(op) || "<".equals(op)) {
                                var.upper(val);
                            } else if (">=".equals(op) || ">".equals(op)) {
                                var.lower(val);
                            } else if ("=".equals(op)) {
                                var.level(val);
                            }
                        }
                    }
                }
            } else {
                pos[0]++;
            }
        }
    }

    private static BigDecimal parseBoundValue(final List<String> tokens, final int[] pos) {
        if (pos[0] >= tokens.size()) {
            return null;
        }

        int sign = 1;
        if ("-".equals(tokens.get(pos[0]))) {
            sign = -1;
            pos[0]++;
        } else if ("+".equals(tokens.get(pos[0]))) {
            pos[0]++;
        }

        if (pos[0] >= tokens.size()) {
            return null;
        }

        String token = tokens.get(pos[0]).toLowerCase();
        if ("inf".equals(token) || "infinity".equals(token)) {
            pos[0]++;
            return null; // null means unbounded
        }

        if (FileFormatLP.isNumber(tokens.get(pos[0]))) {
            BigDecimal value = new BigDecimal(tokens.get(pos[0])).multiply(BigDecimal.valueOf(sign));
            pos[0]++;
            return value;
        }

        return null;
    }

    private static void parseConstraints(final List<String> tokens, final int[] pos, final ExpressionsBasedModel model,
            final Map<String, Variable> variableMap) {

        int constraintCount = 0;

        while (pos[0] < tokens.size()) {
            if (FileFormatLP.isSectionKeywordAt(tokens, pos[0])) {
                break;
            }

            String name;
            if (pos[0] + 1 < tokens.size() && ":".equals(tokens.get(pos[0] + 1))) {
                name = tokens.get(pos[0]);
                pos[0] += 2;
            } else {
                name = "c" + constraintCount++;
            }

            BigDecimal lowerBound = null;
            if (FileFormatLP.hasLeadingBound(tokens, pos)) {
                lowerBound = FileFormatLP.parseSignedNumber(tokens, pos);
                if (pos[0] < tokens.size() && FileFormatLP.isComparisonOp(tokens.get(pos[0]))) {
                    pos[0]++;
                }
            }

            Expression expr = model.newExpression(name);
            FileFormatLP.parseExpression(tokens, pos, model, variableMap, expr);

            if (pos[0] < tokens.size() && FileFormatLP.isComparisonOp(tokens.get(pos[0]))) {
                String op = tokens.get(pos[0]);
                pos[0]++;

                BigDecimal rhs = FileFormatLP.parseSignedNumber(tokens, pos);

                if (rhs != null) {
                    if ("<=".equals(op) || "<".equals(op)) {
                        expr.upper(rhs);
                    } else if (">=".equals(op) || ">".equals(op)) {
                        expr.lower(rhs);
                    } else if ("=".equals(op)) {
                        expr.level(rhs);
                    }
                }
            }

            if (lowerBound != null) {
                expr.lower(lowerBound);
            }
        }
    }

    private static void parseExpression(final List<String> tokens, final int[] pos, final ExpressionsBasedModel model, final Map<String, Variable> variableMap,
            final Expression expr) {

        int sign = 1;
        boolean inBrackets = false;
        List<Object[]> quadTerms = new ArrayList<>();
        boolean divideBy2 = false;

        while (pos[0] < tokens.size()) {
            String token = tokens.get(pos[0]);

            if (FileFormatLP.isComparisonOp(token) || FileFormatLP.isSectionKeywordAt(tokens, pos[0])) {
                break;
            }

            if ("+".equals(token)) {
                sign = 1;
                pos[0]++;
                continue;
            }
            if ("-".equals(token)) {
                sign = -1;
                pos[0]++;
                continue;
            }
            if ("[".equals(token)) {
                inBrackets = true;
                pos[0]++;
                continue;
            }
            if ("]".equals(token)) {
                inBrackets = false;
                if (pos[0] + 2 < tokens.size() && "/".equals(tokens.get(pos[0] + 1)) && "2".equals(tokens.get(pos[0] + 2))) {
                    divideBy2 = true;
                    pos[0] += 3;
                } else {
                    pos[0]++;
                }
                continue;
            }

            BigDecimal coeff = BigDecimal.valueOf(sign);
            if (FileFormatLP.isNumber(token)) {
                coeff = new BigDecimal(token).multiply(BigDecimal.valueOf(sign));
                pos[0]++;
                if (pos[0] >= tokens.size()) {
                    break;
                }
                token = tokens.get(pos[0]);
            }

            if (FileFormatLP.isVariableName(token)) {
                String var1Name = token;
                Variable var1 = FileFormatLP.getOrCreate(var1Name, model, variableMap);
                pos[0]++;

                if (inBrackets && pos[0] + 1 < tokens.size() && "^".equals(tokens.get(pos[0])) && "2".equals(tokens.get(pos[0] + 1))) {
                    pos[0] += 2;
                    quadTerms.add(new Object[] { var1, var1, coeff });
                } else if (inBrackets && pos[0] + 1 < tokens.size() && "*".equals(tokens.get(pos[0]))) {
                    pos[0]++;
                    String var2Name = tokens.get(pos[0]);
                    Variable var2 = FileFormatLP.getOrCreate(var2Name, model, variableMap);
                    pos[0]++;
                    quadTerms.add(new Object[] { var1, var2, coeff });
                } else {
                    expr.set(var1, coeff);
                }
            }

            sign = 1;
        }

        for (Object[] qt : quadTerms) {
            Variable v1 = (Variable) qt[0];
            Variable v2 = (Variable) qt[1];
            BigDecimal val = (BigDecimal) qt[2];
            if (divideBy2) {
                val = val.divide(TWO);
            }
            expr.set(v1, v2, val);
        }
    }

    private static void parseGenerals(final List<String> tokens, final int[] pos, final Map<String, Variable> variableMap) {
        while (pos[0] < tokens.size()) {
            if (FileFormatLP.isSectionKeywordAt(tokens, pos[0])) {
                break;
            }
            String name = tokens.get(pos[0]);
            Variable var = variableMap.get(name);
            if (var != null) {
                var.integer(true);
            }
            pos[0]++;
        }
    }

    private static BigDecimal parseSignedNumber(final List<String> tokens, final int[] pos) {
        if (pos[0] >= tokens.size()) {
            return null;
        }

        int sign = 1;
        if ("-".equals(tokens.get(pos[0]))) {
            sign = -1;
            pos[0]++;
        } else if ("+".equals(tokens.get(pos[0]))) {
            pos[0]++;
        }

        if (pos[0] < tokens.size() && FileFormatLP.isNumber(tokens.get(pos[0]))) {
            BigDecimal value = new BigDecimal(tokens.get(pos[0])).multiply(BigDecimal.valueOf(sign));
            pos[0]++;
            return value;
        }

        return null;
    }

    private static List<String> readLines(final InputStream input) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int commentIdx = line.indexOf('\\');
                if (commentIdx >= 0) {
                    line = line.substring(0, commentIdx);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
        return lines;
    }

    private static void skipSectionHeader(final List<String> tokens, final int[] pos) {
        String lower = tokens.get(pos[0]).toLowerCase();
        pos[0]++;
        if ("subject".equals(lower) && pos[0] < tokens.size() && "to".equals(tokens.get(pos[0]).toLowerCase())) {
            pos[0]++;
        } else if ("such".equals(lower) && pos[0] < tokens.size() && "that".equals(tokens.get(pos[0]).toLowerCase())) {
            pos[0]++;
        }
    }

    private static List<String> tokenize(final List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append(' ');
        }
        return FileFormatLP.tokenize(sb.toString());
    }

    private static List<String> tokenize(final String text) {
        List<String> tokens = new ArrayList<>();
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
                if ((c == '<' && next == '=') || (c == '>' && next == '=')) {
                    tokens.add(String.valueOf(c) + next);
                    i += 2;
                    continue;
                }
            }

            // Single-character operators
            if ("+-*/^=:[]<>".indexOf(c) >= 0) {
                tokens.add(String.valueOf(c));
                i++;
                continue;
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
                tokens.add(num.toString());
                continue;
            }

            // Names (identifiers)
            if (Character.isLetter(c) || c == '_') {
                StringBuilder name = new StringBuilder();
                while (i < len && (Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '_' || text.charAt(i) == '.')) {
                    name.append(text.charAt(i));
                    i++;
                }
                tokens.add(name.toString());
                continue;
            }

            i++;
        }

        return tokens;
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

        List<String> lines = FileFormatLP.readLines(input);
        List<String> tokens = FileFormatLP.tokenize(lines);

        ExpressionsBasedModel model = factory.get();
        Map<String, Variable> variableMap = new LinkedHashMap<>();

        int[] pos = { 0 };

        // Objective sense
        Optimisation.Sense sense = Optimisation.Sense.MIN;
        while (pos[0] < tokens.size()) {
            String lower = tokens.get(pos[0]).toLowerCase();
            if ("minimize".equals(lower) || "min".equals(lower) || "minimum".equals(lower)) {
                sense = Optimisation.Sense.MIN;
                pos[0]++;
                break;
            } else if ("maximize".equals(lower) || "max".equals(lower) || "maximum".equals(lower)) {
                sense = Optimisation.Sense.MAX;
                pos[0]++;
                break;
            }
            pos[0]++;
        }
        model.setOptimisationSense(sense);

        // Objective expression
        if (pos[0] + 1 < tokens.size() && ":".equals(tokens.get(pos[0] + 1))) {
            pos[0] += 2;
        }

        Expression objExpr = model.newExpression("obj");
        objExpr.weight(ONE);
        FileFormatLP.parseExpression(tokens, pos, model, variableMap, objExpr);

        // Remaining sections
        while (pos[0] < tokens.size()) {
            String lower = tokens.get(pos[0]).toLowerCase();

            if ("end".equals(lower)) {
                break;
            }

            if (FileFormatLP.isConstraintSection(lower)) {
                FileFormatLP.skipSectionHeader(tokens, pos);
                FileFormatLP.parseConstraints(tokens, pos, model, variableMap);
            } else if ("bounds".equals(lower) || "bound".equals(lower)) {
                pos[0]++;
                FileFormatLP.parseBounds(tokens, pos, model, variableMap);
            } else if ("generals".equals(lower) || "general".equals(lower) || "gen".equals(lower)) {
                pos[0]++;
                FileFormatLP.parseGenerals(tokens, pos, variableMap);
            } else if ("binaries".equals(lower) || "binary".equals(lower) || "bin".equals(lower)) {
                pos[0]++;
                FileFormatLP.parseBinaries(tokens, pos, variableMap);
            } else if (FileFormatLP.isUnsupportedSection(lower)) {
                throw new IllegalArgumentException("Unsupported LP file section: " + tokens.get(pos[0]));
            } else {
                pos[0]++;
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
