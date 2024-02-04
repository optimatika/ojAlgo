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
package org.ojalgo.optimisation;

import static org.ojalgo.function.constant.BigMath.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.ojalgo.netio.ASCII;

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

            myVariable = new Variable(name);
            myModel.addVariable(myVariable);

            this.bound(BoundType.PL, null);
        }

        Column bound(final BoundType type, final BigDecimal value) {

            switch (type) {

            case LO:

                myVariable.lower(value);

                break;

            case UP:

                myVariable.upper(value);

                if (!myVariable.isLowerLimitSet()) {
                    myVariable.lower(ZERO);
                }

                break;

            case FX:

                myVariable.level(value);

                break;

            case FR:

                myVariable.level(null);

                break;

            case MI:

                myVariable.lower(null);

                if (!myVariable.isUpperLimitSet()) {
                    myVariable.upper(ZERO);
                }

                break;

            case PL:

                myVariable.upper(null);

                if (!myVariable.isLowerLimitSet()) {
                    myVariable.lower(ZERO);
                }

                break;

            case BV:

                myVariable.lower(ZERO).upper(ONE).integer(true);

                break;

            case LI:

                myVariable.lower(value).upper(null).integer(true);

                break;

            case UI:

                myVariable.upper(value).integer(true);

                if (!myVariable.isLowerLimitSet()) {
                    myVariable.lower(ZERO);
                }

                break;

            case SC:

                mySemicontinuous = true;

                myVariable.upper(value);

                if (!myVariable.isLowerLimitSet()) {
                    myVariable.lower(ONE);
                }

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
        FieldPredicate COLUMN_NAME = (line, start, index, field) -> {
            if (field == null || Math.max(field.length(), index - start) < 8) {
                return false;
            }
            return true;
        };
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
         * @param line The full line being parsed
         * @param start The styart index of the current field as specified in the original MPS format
         * @param index The current index of that full line
         * @param field The part of the line that being investigated
         * @return true if the field is correct/complete
         */
        boolean test(String line, int start, int index, String field);

    }

    enum FileSection {
        BOUNDS, COLUMNS, ENDATA, NAME, OBJNAME, OBJSENSE, QMATRIX, QUADOBJ, RANGES, RHS, ROWS, SOS;
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
    private static final int[] FIELD_START = new int[] { 1, 4, 14, 24, 39, 49, 64 };
    private static final String INTEND = "INTEND";
    private static final String INTORG = "INTORG";
    private static final String MARKER = "MARKER";
    private static final String MAX = "MAX";
    private static final String SPACE = " ";

    static ExpressionsBasedModel read(final InputStream input) {

        FileFormatMPS retVal = new FileFormatMPS();

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
                }
            }

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

        return retVal.getModel();
    }

    private final Map<String, Row> myRows = new HashMap<>();
    private final Map<String, Column> myColumns = new HashMap<>();
    private final FieldPredicate myExistingColumn = (line, start, index, field) -> myColumns.containsKey(field);
    private final FieldPredicate myExistingRow = (line, start, index, field) -> myRows.containsKey(field);
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

    FileFormatMPS() {

        super();

        myModel = new ExpressionsBasedModel();

        myVerifierROWS = new FieldPredicate[] { FieldPredicate.ROW_TYPE, FieldPredicate.ROW_NAME, FieldPredicate.NOT_USED, FieldPredicate.NOT_USED,
                FieldPredicate.NOT_USED, FieldPredicate.NOT_USED };

        myVerifierCOLUMNS = new FieldPredicate[] { FieldPredicate.EMPTY, FieldPredicate.COLUMN_NAME, myExistingRow, FieldPredicate.NUMBER, myExistingRow,
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

        FileSection retVal = FileSection.valueOf(tmpSection);

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

        Arrays.fill(myFields, null);

        switch (section) {

        case NAME:

            break;

        case OBJSENSE:

            if (line.contains(MAX)) {
                myModel.setOptimisationSense(Optimisation.Sense.MAX);
            } else {
                myModel.setOptimisationSense(Optimisation.Sense.MIN);
            }

            break;

        case OBJNAME:

            break;

        case ROWS:

            this.extractFields(line, myVerifierROWS);

            RowType rowType = RowType.valueOf(myFields[0]);
            String rowName = myFields[1].trim();
            if (myIdRowN == null && rowType == RowType.N) {
                myIdRowN = rowName;
            }

            myRows.put(myFields[1], new Row(rowName, rowType, myIdRowN));

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

                Column tmpColumn = myColumns.computeIfAbsent(myFields[1].trim(), Column::new);

                tmpColumn.setRowValue(myFields[2], new BigDecimal(myFields[3]));
                if (myFields[4] != null) {
                    tmpColumn.setRowValue(myFields[4], new BigDecimal(myFields[5]));
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

            myRows.get(myFields[2]).rhs(new BigDecimal(myFields[3]));

            if (myFields[4] != null) {
                myRows.get(myFields[4]).rhs(new BigDecimal(myFields[5]));
            }

            break;

        case RANGES:

            this.extractFields(line, myVerifierRANGES);

            if (myIdRANGES == null) {
                myIdRANGES = myFields[1];
            } else if (!myIdRANGES.equals(myFields[1])) {
                break;
            }

            myRows.get(myFields[2]).range(new BigDecimal(myFields[3]));

            if (myFields[4] != null) {
                myRows.get(myFields[4]).range(new BigDecimal(myFields[5]));
            }

            break;

        case BOUNDS:

            this.extractFields(line, myVerifierBOUNDS);

            if (myIdBOUNDS == null) {
                myIdBOUNDS = myFields[1];
            } else if (!myIdBOUNDS.equals(myFields[1])) {
                break;
            }

            BoundType boundType = BoundType.valueOf(myFields[0]);

            myColumns.get(myFields[2]).bound(boundType, myFields[3] != null ? new BigDecimal(myFields[3]) : null);

            break;

        case QUADOBJ:

            this.extractFields(line, myVerifierQ);

            if (myQuadObjExpr == null) {
                myQuadObjExpr = myModel.newExpression(section.name()).weight(HALF);
            }

            Variable var1 = myColumns.get(myFields[1]).getVariable();
            Variable var2 = myColumns.get(myFields[2]).getVariable();
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

            Variable varA = myColumns.get(myFields[1]).getVariable();
            Variable varB = myColumns.get(myFields[2]).getVariable();
            BigDecimal paramC = new BigDecimal(myFields[3]);

            myQuadObjExpr.set(varA, varB, paramC);

            break;

        case ENDATA:

            break;

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
