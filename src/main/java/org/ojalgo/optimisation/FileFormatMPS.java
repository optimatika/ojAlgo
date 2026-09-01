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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.ojalgo.netio.ASCII;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;

/**
 * Mathematical Programming System (MPS) parser
 *
 * @author apete
 */
final class FileFormatMPS {

    /**
     * BoundType used with the BOUNDS section.
     *
     * <pre>
     *  type            meaning
     * ---------------------------------------------------
     *   LO    lower bound        b <= x (< +inf)
     *   UP    upper bound        (0 <=) x <= b
     *   FX    fixed variable     x = b
     *   FR    free variable      -inf < x < +inf
     *   MI    lower bound -inf   -inf < x (<= 0)
     *   PL    upper bound +inf   (0 <=) x < +inf
     *   BV    binary variable    x = 0 or 1
     *   LI    integer variable   b <= x (< +inf)
     *   UI    integer variable   (0 <=) x <= b
     *   SC    semi-cont variable x = 0 or l <= x <= b
     *         l is the lower bound on the variable
     *         If none set then defaults to 1
     * </pre>
     *
     * @author apete
     */
    enum BoundType {
        BV, FR, FX, LI, LO, MI, PL, SC, UI, UP;
    }

    final class Column {

        private boolean mySemicontinuous = false;
        private final Variable myVariable;

        Column(final String name) {

            super();

            myVariable = myModel.newVariable(name);

            myVariable.lower(ZERO);
        }

        Column bound(final BoundType type, final BigDecimal value) {

            switch (type) {

                case LO:

                    myVariable.lower(value);

                    break;

                case UP:

                    myVariable.upper(value);

                    break;

                case FX:

                    myVariable.level(value);

                    break;

                case FR:

                    myVariable.lower(null);
                    myVariable.upper(null);

                    break;

                case MI:

                    myVariable.lower(null);

                    if (!myVariable.isUpperLimitSet()) {
                        myVariable.upper(ZERO);
                    }

                    break;

                case PL:

                    myVariable.upper(null);

                    break;

                case BV:

                    myVariable.lower(ZERO).upper(ONE).integer(true);

                    break;

                case LI:

                    myVariable.lower(value).upper(null).integer(true);

                    break;

                case UI:

                    myVariable.upper(value).integer(true);

                    break;

                case SC:

                    mySemicontinuous = true;

                    myVariable.upper(value);

                    break;

                default:

                    break;
            }

            return this;
        }

        /**
         * @return the variable
         */
        Variable getVariable() {
            return myVariable;
        }

        Column integer(final boolean flag) {
            myVariable.setInteger(flag);
            return this;
        }

        boolean isSemicontinuous() {
            return mySemicontinuous;
        }

        void setRowValue(final String rowName, final BigDecimal value) {
            Row row = myRows.get(rowName);
            Expression expression = row.getExpression();
            expression.set(myVariable, value);
        }

    }

    /**
     * @author apete
     */
    enum ColumnMarker {
        INTEND, INTORG;
    }

    interface FieldPredicate {

        FieldPredicate BOUND_TYPE = (line, start, index, field) -> field != null && field.length() == 2;
        FieldPredicate COLUMN_NAME = (line, start, index, field) -> field != null && field.length() > 0 && Math.max(field.length(), index - start) >= 8;
        FieldPredicate EMPTY = (line, start, index, field) -> {
            if (field == null || field.length() == 0) {
                return true;
            }
            return false;
        };
        FieldPredicate NOT_USED = (line, start, index, field) -> false;
        FieldPredicate NUMBER = (line, start, index, field) -> field.length() > 0;
        FieldPredicate ROW_NAME = (line, start, index, field) -> {
            if (field.length() <= 0) {
                return false;
            }
            for (int i = index + 1; i < line.length(); i++) {
                if (!ASCII.isSpace(line.charAt(i))) {
                    return false;
                }
            }
            return true;
        };
        FieldPredicate ROW_TYPE = (line, start, index, field) -> field != null && field.length() == 1;

        /**
         * Test if the field is "correct".
         *
         * @param line  The full line being parsed
         * @param start The styart index of the current field as specified in the original MPS format
         * @param index The current index of that full line
         * @param field The part of the line that being investigated
         * @return true if the field is correct/complete
         */
        boolean test(String line, int start, int index, String field);

    }

    enum FileSection {
        BOUNDS, COLUMNS, CSECTION, ENDATA, GENCONS, IMPORTANCES, INDICATORS, LAZYCONS, NAME, OBJNAME, OBJSEN, OBJSENSE, PWLOBJ, QCMATRIX, QMATRIX, QUADOBJ,
        QSECTION, RANGES, REFROW, RHS, ROWS, SCENARIOS, SOS, USERCUTS;
    }

    final class Row {

        private final Expression myExpression;
        private final RowType myType;

        Row(final String name, final RowType rowType, final String objName) {

            super();

            myExpression = myModel.newExpression(name);

            myType = rowType;

            if (myType == RowType.N && name.equals(objName)) {
                myExpression.weight(ONE);
            } else {
                myExpression.weight(null);
                // 0.0 is the default RHS value
                this.rhs(ZERO);
            }
        }

        /**
         * @return the expression
         */
        Expression getExpression() {
            return myExpression;
        }

        /**
         * @return the type
         */
        RowType getType() {
            return myType;
        }

        Row range(final BigDecimal value) {

            switch (myType) {

                case E:

                    int tmpSignum = value.signum();
                    if (tmpSignum == 1) {
                        myExpression.upper(myExpression.getLowerLimit().add(value));
                    } else if (tmpSignum == -1) {
                        myExpression.lower(myExpression.getUpperLimit().add(value));
                    }

                    break;

                case L:

                    myExpression.lower(myExpression.getUpperLimit().subtract(value.abs()));

                    break;

                case G:

                    myExpression.upper(myExpression.getLowerLimit().add(value.abs()));

                    break;

                default:

                    break;
            }

            return this;
        }

        Row rhs(final BigDecimal value) {

            switch (myType) {

                case E:

                    myExpression.level(value);

                    break;

                case L:

                    myExpression.upper(value);

                    break;

                case G:

                    myExpression.lower(value);

                    break;

                case N:

                    myExpression.addObjectiveConstant(value.negate());

                    break;

                default:

                    break;
            }

            return this;
        }

        void setColumnValue(final String columnName, final BigDecimal value) {
            myExpression.set(myColumns.get(columnName).getVariable(), value);
        }

    }

    /**
     * RowType used with the ROWS and RANGES sections.
     *
     * <pre>
     * type      meaning
     * ---------------------------
     *  E    equality
     *  L    less than or equal
     *  G    greater than or equal
     *  N    objective
     *  N    no restriction
     *
     * row type       sign of r       h          u
     * ----------------------------------------------
     *    G            + or -         b        b + |r|
     *    L            + or -       b - |r|      b
     *    E              +            b        b + |r|
     *    E              -          b - |r|      b
     * </pre>
     *
     * @author apete
     */
    enum RowType {
        E, G, L, N;
    }

    private static final String COMMENT = "*";
    /**
     * Seems to be used in problem headers/comment to mark references to authors and such
     */
    private static final String COMMENT_REF = "&";
    private static final int[] FIELD_START = { 1, 4, 14, 24, 39, 49, 64 };
    private static final String INTEND = "INTEND";
    private static final String INTORG = "INTORG";
    private static final String MARKER = "MARKER";
    private static final String MAX = "MAX";
    private static final String SPACE = " ";

    private static void writeBound(final BufferedWriter writer, final String type, final String bndId, final String varName, final BigDecimal value)
            throws IOException {
        if (value != null) {
            writer.write(String.format(" %-2s %-10s%-10s%s", type, bndId, varName, value.toPlainString()));
        } else {
            writer.write(String.format(" %-2s %-10s%s", type, bndId, varName));
        }
        writer.newLine();
    }

    /**
     * MPS names are limited to 8 characters (the fixed-format fields are 8 wide, and longer names run into the
     * following field). Names that fit are used as they are; longer (or duplicate/empty) ones are replaced by a
     * deterministic 8-character alias: the prefix ('V' for variables, 'E' for expressions) followed by 7
     * base-36 digits derived from the original name's hash. A hash collision is resolved by re-hashing, so the
     * result is still deterministic for a given model.
     */
    private static String mpsName(final String original, final char prefix, final Map<String, String> aliases, final java.util.Set<String> used) {

        String existing = aliases.get(original);
        if (existing != null) {
            return existing;
        }

        String name = original;
        if (name == null || name.length() == 0 || name.length() > 8 || !used.add(name)) {
            int hash = original == null ? 0 : original.hashCode();
            do {
                String digits = Long.toString(hash & 0xFFFFFFFFL, 36).toUpperCase();
                StringBuilder builder = new StringBuilder(8).append(prefix);
                for (int i = digits.length(); i < 7; i++) {
                    builder.append('0');
                }
                name = builder.append(digits).toString();
                hash = 31 * hash + 17;
            } while (!used.add(name));
        }

        aliases.put(original, name);
        return name;
    }

    private static void writeBounds(final BufferedWriter writer, final String bndId, final Variable var, final String name) throws IOException {

        BigDecimal lower = var.getLowerLimit();
        BigDecimal upper = var.getUpperLimit();

        if (var.isBinary()) {
            FileFormatMPS.writeBound(writer, "BV", bndId, name, null);
            return;
        }

        if (lower != null && upper != null && lower.compareTo(upper) == 0) {
            FileFormatMPS.writeBound(writer, "FX", bndId, name, lower);
            return;
        }

        if (lower == null && upper == null) {
            FileFormatMPS.writeBound(writer, "FR", bndId, name, null);
            return;
        }

        boolean isDefault = lower != null && lower.signum() == 0 && upper == null && !var.isInteger();
        if (isDefault) {
            return;
        }

        if (lower == null) {
            FileFormatMPS.writeBound(writer, "MI", bndId, name, null);
        } else if (lower.signum() != 0) {
            FileFormatMPS.writeBound(writer, "LO", bndId, name, lower);
        }

        if (upper != null) {
            FileFormatMPS.writeBound(writer, "UP", bndId, name, upper);
        }
    }

    private static void writeField2(final BufferedWriter writer, final String f1, final String f2) throws IOException {
        writer.write(String.format(" %s  %s", f1, f2));
        writer.newLine();
    }

    private static void writeField3(final BufferedWriter writer, final String f1, final String f2, final BigDecimal value) throws IOException {
        writer.write(String.format("    %-10s%-10s%s", f1, f2, value.toPlainString()));
        writer.newLine();
    }

    private static void writeMarker(final BufferedWriter writer, final int count, final boolean start) throws IOException {
        writer.write(String.format("    %-10s  'MARKER'                 '%s'", String.format("M%07d", count), start ? INTORG : INTEND));
        writer.newLine();
    }

    static ExpressionsBasedModel read(final InputStream input, final Supplier<ExpressionsBasedModel> factory) {

        FileFormatMPS retVal = new FileFormatMPS(factory);

        String line;
        FileSection section = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            while ((line = reader.readLine()) != null) {

                if (line.length() == 0 || line.startsWith(COMMENT) || line.startsWith(COMMENT_REF)) {
                    // Skip this line
                } else if (line.startsWith(SPACE)) {
                    retVal.parseSectionLine(section, line);
                } else {
                    section = retVal.identifySection(line);
                    if (section == FileSection.ENDATA) {
                        break;
                    }
                }
            }

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

        if (retVal.myColumns.isEmpty()) {
            throw new IllegalArgumentException("MPS file produced no variables");
        }

        return retVal.getModel();
    }

    static void write(final ExpressionsBasedModel model, final OutputStream output) {

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {

            List<Variable> variables = model.getVariables();
            Expression objective = model.objective();

            List<Expression> constraints = new ArrayList<>();
            for (Expression expr : model.getExpressions()) {
                if (expr.isConstraint()) {
                    constraints.add(expr);
                }
            }

            String objRow = "obj";
            String rhsId = "rhs";
            String bndId = "bnd";
            String rngId = "rng";

            java.util.Set<String> usedNames = new java.util.HashSet<>(Arrays.asList(objRow, rhsId, bndId, rngId));
            Map<String, String> rowNames = new HashMap<>();
            Map<String, String> columnNames = new HashMap<>();
            for (Expression expr : constraints) {
                FileFormatMPS.mpsName(expr.getName(), 'E', rowNames, usedNames);
            }
            for (Variable var : variables) {
                FileFormatMPS.mpsName(var.getName(), 'V', columnNames, usedNames);
            }

            // NAME
            writer.write("NAME");
            writer.newLine();

            // OBJSENSE
            Optimisation.Sense sense = model.getOptimisationSense();
            if (sense != null) {
                writer.write("OBJSENSE");
                writer.newLine();
                writer.write("    ");
                writer.write(sense == Optimisation.Sense.MAX ? MAX : "MIN");
                writer.newLine();
            }

            // ROWS
            writer.write("ROWS");
            writer.newLine();
            FileFormatMPS.writeField2(writer, "N", objRow);
            for (Expression expr : constraints) {
                String type;
                switch (expr.getConstraintType()) {
                    case EQUALITY:
                        type = "E";
                        break;
                    case LOWER:
                        type = "G";
                        break;
                    case RANGE:
                    case UPPER:
                        type = "L";
                        break;
                    default:
                        continue;
                }
                FileFormatMPS.writeField2(writer, type, rowNames.get(expr.getName()));
            }

            // COLUMNS
            writer.write("COLUMNS");
            writer.newLine();

            boolean integerBlock = false;
            int markerCount = 0;

            for (int v = 0; v < variables.size(); v++) {
                Variable var = variables.get(v);

                if (var.isInteger() && !integerBlock) {
                    FileFormatMPS.writeMarker(writer, markerCount++, true);
                    integerBlock = true;
                } else if (!var.isInteger() && integerBlock) {
                    FileFormatMPS.writeMarker(writer, markerCount++, false);
                    integerBlock = false;
                }

                String varName = columnNames.get(var.getName());
                IntIndex varIndex = new IntIndex(v);

                BigDecimal objCoeff = objective.get(varIndex);
                if (objCoeff.signum() != 0) {
                    FileFormatMPS.writeField3(writer, varName, objRow, objCoeff);
                }

                for (Expression expr : constraints) {
                    BigDecimal coeff = expr.get(varIndex);
                    if (coeff.signum() != 0) {
                        FileFormatMPS.writeField3(writer, varName, rowNames.get(expr.getName()), coeff);
                    }
                }
            }

            if (integerBlock) {
                FileFormatMPS.writeMarker(writer, markerCount, false);
            }

            // RHS
            writer.write("RHS");
            writer.newLine();

            BigDecimal objConstant = model.getObjectiveConstant();
            if (objConstant != null && objConstant.signum() != 0) {
                FileFormatMPS.writeField3(writer, rhsId, objRow, objConstant.negate());
            }

            for (Expression expr : constraints) {
                BigDecimal rhs;
                switch (expr.getConstraintType()) {
                    case EQUALITY:
                        rhs = expr.getLowerLimit();
                        break;
                    case LOWER:
                        rhs = expr.getLowerLimit();
                        break;
                    case RANGE:
                    case UPPER:
                        rhs = expr.getUpperLimit();
                        break;
                    default:
                        rhs = null;
                        break;
                }
                if (rhs != null) {
                    FileFormatMPS.writeField3(writer, rhsId, rowNames.get(expr.getName()), rhs);
                }
            }

            // RANGES
            boolean hasRanges = false;
            for (Expression expr : constraints) {
                if (expr.getConstraintType() == Optimisation.ConstraintType.RANGE) {
                    hasRanges = true;
                    break;
                }
            }
            if (hasRanges) {
                writer.write("RANGES");
                writer.newLine();
                for (Expression expr : constraints) {
                    if (expr.getConstraintType() == Optimisation.ConstraintType.RANGE) {
                        BigDecimal range = expr.getUpperLimit().subtract(expr.getLowerLimit());
                        FileFormatMPS.writeField3(writer, rngId, rowNames.get(expr.getName()), range);
                    }
                }
            }

            // BOUNDS
            writer.write("BOUNDS");
            writer.newLine();

            for (Variable var : variables) {
                FileFormatMPS.writeBounds(writer, bndId, var, columnNames.get(var.getName()));
            }

            // QUADOBJ
            if (objective.isAnyQuadraticFactorNonZero()) {
                writer.write("QUADOBJ");
                writer.newLine();
                for (Entry<IntRowColumn, BigDecimal> entry : objective.getQuadraticEntrySet()) {
                    int row = entry.getKey().row;
                    int col = entry.getKey().column;
                    if (row <= col) {
                        BigDecimal qValue;
                        if (row == col) {
                            qValue = entry.getValue().multiply(TWO);
                        } else {
                            BigDecimal transpose = objective.get(new IntRowColumn(col, row));
                            qValue = entry.getValue().add(transpose);
                        }
                        FileFormatMPS.writeField3(writer, columnNames.get(variables.get(row).getName()), columnNames.get(variables.get(col).getName()), qValue);
                    }
                }
            }

            writer.write("ENDATA");
            writer.newLine();

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    private final Map<String, Row> myRows = new HashMap<>();
    private final Map<String, Column> myColumns = new HashMap<>();
    private final FieldPredicate myColumnName = (line, start, index, field) -> {
        if (field == null || field.length() == 0) {
            return false;
        }
        if (Math.max(field.length(), index - start) >= 8) {
            return true;
        }
        for (int i = index + 1; i < line.length(); i++) {
            if (!ASCII.isSpace(line.charAt(i))) {
                int end = i;
                while (end < line.length() && !ASCII.isSpace(line.charAt(end))) {
                    end++;
                }
                return myRows.containsKey(line.substring(i, end).trim());
            }
        }
        return true;
    };
    private final FieldPredicate myExistingColumn = (line, start, index, field) -> field != null && myColumns.containsKey(field.trim());
    private final FieldPredicate myExistingRow = (line, start, index, field) -> field != null && myRows.containsKey(field.trim());
    private final String[] myFields = new String[6];
    private String myIdBOUNDS = null;
    private String myIdRANGES = null;
    private String myIdRHS = null;
    private String myIdRowN = null;
    private boolean myIntegerMarker = false;
    private final FieldPredicate myMatchingBOUNDS = (line, start, index, field) -> {

        if (myIdBOUNDS != null) {
            if (myIdBOUNDS.equals(field)) {
                return true;
            }
            return false;
        }

        return this.nameColumns(line, field);
    };
    private final FieldPredicate myMatchingRANGES = (line, start, index, field) -> {

        if (myIdRANGES != null) {
            if (myIdRANGES.equals(field)) {
                return true;
            }
            return false;
        }

        return this.nameRows(line, field);
    };
    private final FieldPredicate myMatchingRHS = (line, start, index, field) -> {

        if (myIdRHS != null) {
            if (myIdRHS.equals(field)) {
                return true;
            }
            return false;
        }

        return this.nameRows(line, field);
    };
    private final ExpressionsBasedModel myModel;
    private String myName;
    private Expression myQuadObjExpr = null;
    private final FieldPredicate[] myVerifierBOUNDS;
    private final FieldPredicate[] myVerifierCOLUMNS;
    private final FieldPredicate[] myVerifierQ;
    private final FieldPredicate[] myVerifierRANGES;
    private final FieldPredicate[] myVerifierRHS;
    private final FieldPredicate[] myVerifierROWS;

    FileFormatMPS(final Supplier<ExpressionsBasedModel> factory) {

        super();

        myModel = factory.get();

        myVerifierROWS = new FieldPredicate[] { FieldPredicate.ROW_TYPE, FieldPredicate.ROW_NAME, FieldPredicate.NOT_USED, FieldPredicate.NOT_USED,
                FieldPredicate.NOT_USED, FieldPredicate.NOT_USED };

        myVerifierCOLUMNS = new FieldPredicate[] { FieldPredicate.EMPTY, myColumnName, myExistingRow, FieldPredicate.NUMBER, myExistingRow,
                FieldPredicate.NUMBER };

        myVerifierRHS = new FieldPredicate[] { FieldPredicate.EMPTY, myMatchingRHS, myExistingRow, FieldPredicate.NUMBER, myExistingRow,
                FieldPredicate.NUMBER };

        myVerifierRANGES = new FieldPredicate[] { FieldPredicate.EMPTY, myMatchingRANGES, myExistingRow, FieldPredicate.NUMBER, myExistingRow,
                FieldPredicate.NUMBER };

        myVerifierBOUNDS = new FieldPredicate[] { FieldPredicate.BOUND_TYPE, myMatchingBOUNDS, myExistingColumn, FieldPredicate.NUMBER, myExistingColumn,
                FieldPredicate.NUMBER };

        myVerifierQ = new FieldPredicate[] { FieldPredicate.EMPTY, myExistingColumn, myExistingColumn, FieldPredicate.NUMBER, FieldPredicate.NOT_USED,
                FieldPredicate.NOT_USED };
    }

    @Override
    public String toString() {
        return myModel.toString();
    }

    private void extractFields(final String line, final FieldPredicate[] verifiers) {

        char tecken;
        int first = -1;
        int limit = -1;
        boolean word = false;

        for (int i = 1, length = line.length(), f = 0; i < length; i++) {

            tecken = line.charAt(i);

            if (i == 4) {
                f = Math.max(f, 1);
            } else if (!word && i == 14) {
                f = Math.max(f, 2);
            }

            if (!word && !ASCII.isSpace(tecken)) {
                word = true;
                first = i;
            } else if (word && ASCII.isSpace(tecken)) {
                word = false;
                limit = i;
            }
            if (word && i + 1 == length) {
                word = false;
                limit = i + 1;
            }

            if (limit > first) {
                String field = line.substring(first, limit);
                if (!verifiers[f].test(line, FIELD_START[f], i, field)) {
                    word = true;
                } else {
                    myFields[f++] = field;
                    first = -1;
                }
                limit = -1;
            }
        }
    }

    private ExpressionsBasedModel getModel() {
        return myModel;
    }

    private FileSection identifySection(final String line) {

        int tmpSplit = line.indexOf(SPACE);
        String tmpSection;
        String tmpArgument;
        if (tmpSplit != -1) {
            tmpSection = line.substring(0, tmpSplit).trim();
            tmpArgument = line.substring(tmpSplit).trim();
        } else {
            tmpSection = line.trim();
            tmpArgument = "";
        }

        FileSection retVal;
        try {
            retVal = FileSection.valueOf(tmpSection);
        } catch (IllegalArgumentException cause) {
            return null;
        }

        switch (retVal) {

            case NAME:

                myName = tmpArgument;

                break;

            default:

                break;
        }

        return retVal;
    }

    private void parseSectionLine(final FileSection section, final String line) {

        if (section == null) {
            return;
        }

        Arrays.fill(myFields, null);

        switch (section) {

            case NAME:

                break;

            case OBJSENSE:
            case OBJSEN:

                if (line.contains(MAX)) {
                    myModel.setOptimisationSense(Optimisation.Sense.MAX);
                } else {
                    myModel.setOptimisationSense(Optimisation.Sense.MIN);
                }

                break;

            case OBJNAME:

                String tmpObjName = line.trim();
                if (!tmpObjName.isEmpty()) {
                    myIdRowN = tmpObjName;
                }

                break;

            case ROWS:

                this.extractFields(line, myVerifierROWS);

                RowType rowType = RowType.valueOf(myFields[0]);
                String rowName = myFields[1].trim();
                if (myIdRowN == null && rowType == RowType.N) {
                    myIdRowN = rowName;
                }

                myRows.put(rowName, new Row(rowName, rowType, myIdRowN));

                break;

            case COLUMNS:

                if (line.contains(MARKER)) {

                    if (line.contains(INTORG)) {
                        myIntegerMarker = true;
                    } else if (line.contains(INTEND)) {
                        myIntegerMarker = false;
                    }

                } else {

                    this.extractFields(line, myVerifierCOLUMNS);

                    if (myFields[1] == null || myFields[2] == null || myFields[3] == null) {
                        throw new IllegalArgumentException("Could not parse COLUMNS line: " + line);
                    }

                    Column tmpColumn = myColumns.computeIfAbsent(myFields[1].trim(), Column::new);

                    tmpColumn.setRowValue(myFields[2].trim(), new BigDecimal(myFields[3]));
                    if (myFields[4] != null && myFields[5] != null) {
                        tmpColumn.setRowValue(myFields[4].trim(), new BigDecimal(myFields[5]));
                    }

                    if (myIntegerMarker) {
                        tmpColumn.integer(myIntegerMarker);
                    }
                }

                break;

            case RHS:

                this.extractFields(line, myVerifierRHS);

                if (myIdRHS == null) {
                    myIdRHS = myFields[1];
                } else if (!myIdRHS.equals(myFields[1])) {
                    break;
                }

                if (myFields[2] == null || myFields[3] == null) {
                    throw new IllegalArgumentException("Could not parse RHS line: " + line);
                }

                myRows.get(myFields[2].trim()).rhs(new BigDecimal(myFields[3]));

                if (myFields[4] != null && myFields[5] != null) {
                    myRows.get(myFields[4].trim()).rhs(new BigDecimal(myFields[5]));
                }

                break;

            case RANGES:

                this.extractFields(line, myVerifierRANGES);

                if (myIdRANGES == null) {
                    myIdRANGES = myFields[1];
                } else if (!myIdRANGES.equals(myFields[1])) {
                    break;
                }

                if (myFields[2] == null || myFields[3] == null) {
                    throw new IllegalArgumentException("Could not parse RANGES line: " + line);
                }

                myRows.get(myFields[2].trim()).range(new BigDecimal(myFields[3]));

                if (myFields[4] != null && myFields[5] != null) {
                    myRows.get(myFields[4].trim()).range(new BigDecimal(myFields[5]));
                }

                break;

            case BOUNDS:

                this.extractFields(line, myVerifierBOUNDS);

                if (myIdBOUNDS == null) {
                    myIdBOUNDS = myFields[1];
                } else if (!myIdBOUNDS.equals(myFields[1])) {
                    break;
                }

                if (myFields[0] == null || myFields[2] == null) {
                    throw new IllegalArgumentException("Could not parse BOUNDS line: " + line);
                }

                BoundType boundType = BoundType.valueOf(myFields[0]);

                myColumns.get(myFields[2].trim()).bound(boundType, myFields[3] != null ? new BigDecimal(myFields[3]) : null);

                break;

            case QUADOBJ:

                this.extractFields(line, myVerifierQ);

                if (myQuadObjExpr == null) {
                    myQuadObjExpr = myModel.newExpression(section.name()).weight(HALF);
                }

                Variable var1 = myColumns.get(myFields[1].trim()).getVariable();
                Variable var2 = myColumns.get(myFields[2].trim()).getVariable();
                BigDecimal param3 = new BigDecimal(myFields[3]);

                myQuadObjExpr.set(var1, var2, param3);
                if (!var1.equals(var2)) {
                    myQuadObjExpr.set(var2, var1, param3);
                }

                break;

            case QMATRIX:

                this.extractFields(line, myVerifierQ);

                if (myQuadObjExpr == null) {
                    myQuadObjExpr = myModel.newExpression(section.name()).weight(HALF);
                }

                Variable varA = myColumns.get(myFields[1].trim()).getVariable();
                Variable varB = myColumns.get(myFields[2].trim()).getVariable();
                BigDecimal paramC = new BigDecimal(myFields[3]);

                myQuadObjExpr.set(varA, varB, paramC);

                break;

            case ENDATA:

                break;

            case INDICATORS:
            case IMPORTANCES:
            case REFROW:
            case USERCUTS:

                break;

            case SOS:
            case LAZYCONS:
            case QCMATRIX:
            case PWLOBJ:
            case GENCONS:
            case CSECTION:
            case QSECTION:
            case SCENARIOS:

                throw new IllegalArgumentException("Unsupported MPS section: " + section);

            default:

                break;
        }
    }

    boolean nameColumns(final String line, final String field) {

        String[] parts = line.split("\\s+");

        if (parts.length == 7 && field.equals(parts[parts.length - 5]) && myColumns.containsKey(parts[parts.length - 4])
                && myColumns.containsKey(parts[parts.length - 2])) {
            return true;
        }

        if (parts.length == 5 && field.equals(parts[parts.length - 3]) && myColumns.containsKey(parts[parts.length - 2])) {
            return true;
        }

        if (parts.length == 6 && myColumns.containsKey(parts[parts.length - 4]) && myColumns.containsKey(parts[parts.length - 2])) {
            return true;
        }

        if (parts.length == 4 && myColumns.containsKey(parts[parts.length - 2])) {
            return true;
        }

        return line.substring(FIELD_START[1], FIELD_START[2]).trim().equals(field);
    }

    boolean nameRows(final String line, final String field) {

        String[] parts = line.split("\\s+");

        if (parts.length == 6 && field.equals(parts[parts.length - 5]) && myRows.containsKey(parts[parts.length - 4])
                && myRows.containsKey(parts[parts.length - 2])) {
            return true;
        }

        if (parts.length == 4 && field.equals(parts[parts.length - 3]) && myRows.containsKey(parts[parts.length - 2])) {
            return true;
        }

        if (parts.length == 5 && myRows.containsKey(parts[parts.length - 4]) && myRows.containsKey(parts[parts.length - 2])) {
            return true;
        }

        if (parts.length == 3 && myRows.containsKey(parts[parts.length - 2])) {
            return true;
        }

        return line.substring(FIELD_START[1], FIELD_START[2]).trim().equals(field);
    }

}
