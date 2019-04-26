/*
 * Copyright 1997-2019 Optimatika
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.ojalgo.netio.ASCII;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * Mathematical Programming System (MPS) Model
 *
 * @author apete
 */
public final class MathProgSysModel {

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

        BV(), FR(), FX(), LI(), LO(), MI(), PL(), SC(), UI(), UP();

    }
    final class Column extends Object {

        private boolean mySemicontinuous = false;
        private final Variable myVariable;

        Column(final String name) {

            super();

            myVariable = new Variable(name);
            myDelegate.addVariable(myVariable);

            this.bound(BoundType.PL, null);
        }

        public Column bound(final BoundType type, final BigDecimal value) {

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

        public Column integer(final boolean flag) {
            myVariable.setInteger(flag);
            return this;
        }

        public void setRowValue(final String rowName, final BigDecimal value) {
            Row row = myRows.get(rowName);
            Expression expression = row.getExpression();
            expression.set(myVariable, value);
        }

        /**
         * @return the variable
         */
        Variable getVariable() {
            return myVariable;
        }

        boolean isSemicontinuous() {
            return mySemicontinuous;
        }

    }
    /**
     * @author apete
     */
    enum ColumnMarker {

        INTEND(), INTORG();

    }

    enum FileSection {

        BOUNDS(), COLUMNS(), ENDATA(), NAME(), OBJNAME(), OBJSENSE(), RANGES(), RHS(), ROWS(), SOS();

    }

    final class Row extends Object {

        private final Expression myExpression;

        private final RowType myType;

        Row(final String name, final RowType rowType) {

            super();

            myExpression = myDelegate.addExpression(name);

            myType = rowType;

            if (myType == RowType.N) {
                myExpression.weight(ONE);
            } else {
                myExpression.weight(null);
            }

            // 0.0 is the default RHS value
            this.rhs(ZERO);
        }

        public Row range(final BigDecimal value) {

            switch (myType) {

            case E:

                final int tmpSignum = value.signum();
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

            case N:

                myExpression.level(null);
                myExpression.weight(ONE);

                break;

            default:

                break;
            }

            return this;
        }

        public Row rhs(final BigDecimal value) {

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

                myExpression.level(null);
                myExpression.weight(ONE);

                break;

            default:

                break;
            }

            return this;
        }

        public void setColumnValue(final String columnName, final BigDecimal value) {
            myExpression.set(myColumns.get(columnName).getVariable(), value);
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

        E(), G(), L(), N();

    }

    private static final String COMMENT = "*";
    /**
     * Seems to be used in problem headers/comment to mark references to authors and such
     */
    private static final String COMMENT_REF = "&";
    private static final String EMPTY = "";
    private static final int[] FIELD_FIRSTS = new int[] { 1, 4, 14, 24, 39, 49 };
    private static final int[] FIELD_LIMITS = new int[] { 4, 14, 24, 39, 49, 64 };
    private static final String INTEND = "INTEND";
    private static final String INTORG = "INTORG";
    private static final String MARKER = "MARKER";
    private static final String MAX = "MAX";
    private static final String SPACE = " ";

    public static MathProgSysModel make(final File file) {

        final MathProgSysModel retVal = new MathProgSysModel();

        String tmpLine;
        FileSection tmpSection = null;

        try {

            final BufferedReader tmpBufferedFileReader = new BufferedReader(new FileReader(file));

            //readLine is a bit quirky :
            //it returns the content of a line MINUS the newline.
            //it returns null only for the END of the stream.
            //it returns an empty String if two newlines appear in a row.
            while ((tmpLine = tmpBufferedFileReader.readLine()) != null) {

                // BasicLogger.debug("Line: {}", tmpLine);

                if ((tmpLine.length() == 0) || tmpLine.startsWith(COMMENT) || tmpLine.startsWith(COMMENT_REF)) {
                    // Skip this line
                } else if (tmpLine.startsWith(SPACE)) {
                    retVal.parseSectionLine(tmpSection, tmpLine);
                } else {
                    tmpSection = retVal.identifySection(tmpLine);
                }
            }

            tmpBufferedFileReader.close();

        } catch (final FileNotFoundException anException) {
            anException.printStackTrace();
        } catch (final IOException anException) {
            anException.printStackTrace();
        }

        return retVal;
    }

    private final Map<String, Column> myColumns = new HashMap<>();
    private final ExpressionsBasedModel myDelegate;
    private final String[] myFields = new String[6];
    private String myIdBOUNDS = null;
    private String myIdRANGES = null;
    private String myIdRHS = null;
    private boolean myIntegerMarker = false;
    private String myName;

    private final Map<String, Row> myRows = new HashMap<>();

    MathProgSysModel() {

        super();

        myDelegate = new ExpressionsBasedModel();
    }

    public void dispose() {
        myDelegate.dispose();
        myRows.clear();
        myColumns.clear();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MathProgSysModel)) {
            return false;
        }
        final MathProgSysModel other = (MathProgSysModel) obj;
        if (myDelegate == null) {
            if (other.myDelegate != null) {
                return false;
            }
        } else if (!myDelegate.equals(other.myDelegate)) {
            return false;
        }
        return true;
    }

    /**
     * @return The delegate {@linkplain ExpressionsBasedModel}
     */
    public ExpressionsBasedModel getExpressionsBasedModel() {
        return myDelegate;
    }

    public String getName() {
        return myName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((myDelegate == null) ? 0 : myDelegate.hashCode());
        return result;
    }

    /**
     * Will disregard the OBJSENSE and maximise.
     *
     * @see org.ojalgo.optimisation.Optimisation.Model#maximise()
     */
    public Optimisation.Result maximise() {
        return myDelegate.maximise();
    }

    /**
     * Will disregard the OBJSENSE and minimise.
     *
     * @see org.ojalgo.optimisation.Optimisation.Model#minimise()
     */
    public Optimisation.Result minimise() {
        return myDelegate.minimise();
    }

    /**
     * <p>
     * If the OBJSENSE was specified in the file it is used otherwise the default is to minimise.
     * </p>
     * <p>
     * The solution (variable values) are in the order the columns were defined in the MPS-file.
     * </p>
     */
    public Optimisation.Result solve() {
        if (myDelegate.isMinimisation()) {
            return myDelegate.minimise();
        } else {
            return myDelegate.maximise();
        }
    }

    @Override
    public String toString() {
        return myDelegate.toString();
    }

    public boolean validate() {
        return myDelegate.validate();
    }

    /**
     * @see org.ojalgo.optimisation.ExpressionsBasedModel#validate(org.ojalgo.structure.Access1D,
     *      org.ojalgo.type.context.NumberContext)
     */
    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context) {
        return myDelegate.validate(solution, context);
    }

    private String[] extractFields(final String line, final Map<String, ?> verifier) {

        myFields[0] = line.substring(FIELD_FIRSTS[0], FIELD_LIMITS[0]).trim();
        myFields[1] = line.substring(FIELD_FIRSTS[1], FIELD_LIMITS[1]).trim();

        int first = -1;
        int limit = -1;

        int field = 2;

        char tecken;

        boolean word = false;

        for (int i = FIELD_FIRSTS[field]; i < line.length(); i++) {
            tecken = line.charAt(i);
            if (!word && !ASCII.isSpace(tecken)) {
                word = true;
                first = i;
            } else if (word && ASCII.isSpace(tecken)) {
                word = false;
                limit = i;
            }
            if (word && ((i + 1) == line.length())) {
                word = false;
                limit = i + 1;
            }
            if (limit > first) {
                String key = line.substring(first, limit);
                if (((field % 2) == 0) && !verifier.containsKey(key)) {
                    word = true;
                } else {
                    myFields[field++] = key;
                    first = -1;
                }
                limit = -1;
            }
        }

        return myFields;
    }

    FileSection identifySection(final String line) {

        final int tmpSplit = line.indexOf(SPACE);
        String tmpSection;
        String tmpArgument;
        if (tmpSplit != -1) {
            tmpSection = line.substring(0, tmpSplit).trim();
            tmpArgument = line.substring(tmpSplit).trim();
        } else {
            tmpSection = line.trim();
            tmpArgument = EMPTY;
        }

        // BasicLogger.debug("Section: {},\tArgument: {}.", tmpSection, tmpArgument);

        final FileSection retVal = FileSection.valueOf(tmpSection);

        switch (retVal) {

        case NAME:

            myName = tmpArgument;

            break;

        default:

            break;
        }

        return retVal;
    }

    void parseSectionLine(final FileSection section, final String line) {

        Arrays.fill(myFields, null);

        switch (section) {

        case NAME:

            break;

        case OBJSENSE:

            if (line.contains(MAX)) {
                myDelegate.setMaximisation();
            } else {
                myDelegate.setMinimisation();
            }

            break;

        case OBJNAME:

            break;

        case ROWS:

            myFields[0] = line.substring(FIELD_FIRSTS[0], FIELD_LIMITS[0]).trim();
            myFields[1] = line.substring(FIELD_FIRSTS[1]).trim();

            final Row newRow = new Row(myFields[1], RowType.valueOf(myFields[0]));
            myRows.put(myFields[1], newRow);

            break;

        case COLUMNS:

            if (line.contains(MARKER)) {

                if (line.contains(INTORG)) {
                    myIntegerMarker = true;
                } else if (line.contains(INTEND)) {
                    myIntegerMarker = false;
                }

            } else {

                this.extractFields(line, myRows);

                final Column tmpColumn = myColumns.computeIfAbsent(myFields[1], key -> new Column(key));

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

            this.extractFields(line, myRows);

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

            this.extractFields(line, myRows);

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

            this.extractFields(line, myColumns);

            if (myIdBOUNDS == null) {
                myIdBOUNDS = myFields[1];
            } else if (!myIdBOUNDS.equals(myFields[1])) {
                break;
            }

            myColumns.get(myFields[2]).bound(BoundType.valueOf(myFields[0]), myFields[3] != null ? new BigDecimal(myFields[3]) : null);

            break;

        case ENDATA:

            break;

        default:

            break;
        }

        // BasicLogger.debug("{}: {}", section, Arrays.toString(myFields));
    }

}
