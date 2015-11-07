/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.BigMath.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

import org.ojalgo.access.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * Mathematical Programming System (MPS) Model
 *
 * @author apete
 */
public final class MathProgSysModel extends AbstractModel<GenericSolver> {

    public static abstract class Integration<S extends Optimisation.Solver> implements Optimisation.Integration<MathProgSysModel, S> {

    }

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
    static enum BoundType {

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
            myRows.get(rowName).getExpression().set(myVariable, value);
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
    static enum ColumnMarker {

        INTEND(), INTORG();

    }

    static enum FileSection {

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
    static enum RowType {

        E(), G(), L(), N();

    }

    private static final String COMMENT = "*";
    private static final String EMPTY = "";
    private static final int[] FIELD_LIMITS = new int[] { 3, 12, 22, 36, 47, 61 };
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

                //        BasicLogger.logDebug("Line: {}.", tmpLine);

                if ((tmpLine.length() == 0) || tmpLine.startsWith(COMMENT)) {
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

    private final HashMap<String, Column> myColumns = new HashMap<String, Column>();
    private final ExpressionsBasedModel myDelegate;
    private final String[] myFields = new String[6];
    private boolean myIntegerMarker = false;
    private String myName;

    private final HashMap<String, Row> myRows = new HashMap<String, Row>();

    MathProgSysModel() {

        super();

        myDelegate = new ExpressionsBasedModel(options);

        this.setMinimisation();
    }

    @Override
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
        if (this.isMaximisation()) {
            return myDelegate.maximise();
        } else {
            return myDelegate.minimise();
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
     * @param solution
     * @param context
     * @return
     * @see org.ojalgo.optimisation.ExpressionsBasedModel#validate(org.ojalgo.access.Access1D,
     *      org.ojalgo.type.context.NumberContext)
     */
    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context) {
        return myDelegate.validate(solution, context);
    }

    private void extractFields(final String line) {

        final int tmpLength = line.length();

        int tmpFirst = 0;
        int tmpLimit = tmpFirst;
        for (int i = 0; i < myFields.length; i++) {
            tmpLimit = Math.min(FIELD_LIMITS[i], tmpLength);
            myFields[i] = line.substring(tmpFirst, tmpLimit).trim();
            tmpFirst = tmpLimit;
        }
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

        //      BasicLogger.logDebug("Section: {},\tArgument: {}.", tmpSection, tmpArgument);

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

        this.extractFields(line);

        //      BasicLogger.logDebug("{}: {}.", aSection, Arrays.toString(myFields));

        switch (section) {

        case NAME:

            break;

        case OBJSENSE:

            if (myFields[0].equals("MAX")) {
                this.setMaximisation();
            } else {
                this.setMinimisation();
            }

            break;

        case OBJNAME:

            break;

        case ROWS:

            final Row tmpRow = new Row(myFields[1], RowType.valueOf(myFields[0]));

            myRows.put(myFields[1], tmpRow);

            break;

        case COLUMNS:

            if (myFields[2].indexOf("MARKER") != -1) {

                if (myFields[4].indexOf("INTORG") != -1) {
                    myIntegerMarker = true;
                } else if (myFields[4].indexOf("INTEND") != -1) {
                    myIntegerMarker = false;
                }

            } else {

                if (!myColumns.containsKey(myFields[1])) {
                    myColumns.put(myFields[1], new Column(myFields[1]));
                }

                final Column tmpColumn = myColumns.get(myFields[1]);

                tmpColumn.setRowValue(myFields[2], new BigDecimal(myFields[3]));
                if (myFields[4].length() != 0) {
                    tmpColumn.setRowValue(myFields[4], new BigDecimal(myFields[5]));
                }

                if (myIntegerMarker) {
                    tmpColumn.integer(myIntegerMarker);
                }

            }

            break;

        case RHS:

            myRows.get(myFields[2]).rhs(new BigDecimal(myFields[3]));

            if (myFields[4].length() != 0) {
                myRows.get(myFields[4]).rhs(new BigDecimal(myFields[5]));
            }

            break;

        case RANGES:

            myRows.get(myFields[2]).range(new BigDecimal(myFields[3]));

            if (myFields[4].length() != 0) {
                myRows.get(myFields[4]).range(new BigDecimal(myFields[5]));
            }

            break;

        case BOUNDS:

            myColumns.get(myFields[2]).bound(BoundType.valueOf(myFields[0]), myFields[3].length() == 0 ? null : new BigDecimal(myFields[3]));

            break;

        case ENDATA:

            break;

        default:

            break;
        }
    }

}
