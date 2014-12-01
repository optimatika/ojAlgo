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
package org.ojalgo.optimisation;

import static org.ojalgo.constant.BigMath.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Mathematical Programming System (MPS) Model
 *
 * @author apete
 */
public final class MathProgSysModel extends AbstractModel<GenericSolver> {

    public static final class Column extends ModelEntity<MathProgSysModel.Column> {

        private boolean myActivator = false;

        private final HashMap<String, BigDecimal> myElements = new HashMap<String, BigDecimal>();
        private boolean myInteger = false;

        public Column(final String aName) {

            super(aName);

            this.bound(BoundType.PL, null);
        }

        protected Column(final Column entityToCopy) {

            super(entityToCopy);

            myActivator = entityToCopy.needsActivator();
            myInteger = entityToCopy.isInteger();

            myElements.putAll(entityToCopy.myElements);
        }

        public final Column bound(final BoundType aType, final BigDecimal aValue) {

            switch (aType) {

            case LO:

                this.lower(aValue);

                break;

            case UP:

                this.upper(aValue);

                if (!this.isLowerLimitSet()) {
                    this.lower(ZERO);
                }

                break;

            case FX:

                this.level(aValue);

                break;

            case FR:

                this.level(null);

                break;

            case MI:

                this.lower(null);

                if (!this.isUpperLimitSet()) {
                    this.upper(ZERO);
                }

                break;

            case PL:

                this.upper(null);

                if (!this.isLowerLimitSet()) {
                    this.lower(ZERO);
                }

                break;

            case BV:

                this.lower(ZERO).upper(ONE);
                myInteger = true;

                break;

            case LI:

                this.lower(aValue).upper(null);
                myInteger = true;

                break;

            case UI:

                this.upper(aValue);
                myInteger = true;

                if (!this.isLowerLimitSet()) {
                    this.lower(ZERO);
                }

                break;

            case SC:

                myActivator = true;

                this.upper(aValue);

                if (!this.isLowerLimitSet()) {
                    this.lower(ONE);
                }

                break;

            default:

                break;
            }

            return this;
        }

        public Set<String> getElementKeys() {
            return myElements.keySet();
        }

        public String getNameForActivator() {
            return this.getName() + "?";
        }

        public String getNameForNegativePart() {
            if (this.hasPositivePart()) {
                return this.getName() + "-";
            } else {
                return this.getName();
            }
        }

        public String getNameForPositivePart() {
            if (this.hasNegativePart()) {
                return this.getName() + "+";
            } else {
                return this.getName();
            }
        }

        public BigDecimal getRowValue(final String aRowName) {
            return myElements.get(aRowName);
        }

        public boolean hasNegativePart() {
            final BigDecimal tmpLowerLimit = this.getLowerLimit();
            return (tmpLowerLimit == null) || (tmpLowerLimit.signum() == -1);
        }

        public boolean hasPositivePart() {
            final BigDecimal tmpUpperLimit = this.getUpperLimit();
            return (tmpUpperLimit == null) || (tmpUpperLimit.signum() == 1);
        }

        public boolean isInteger() {
            return myInteger;
        }

        public boolean needsActivator() {
            return myActivator;
        }

        public final void setInteger(final boolean someInteger) {
            myInteger = someInteger;
        }

        public BigDecimal setRowValue(final String aRowName, final BigDecimal aValue) {
            return myElements.put(aRowName, aValue);
        }

        @Override
        protected int getAdjustmentExponent() {
            return 0;
        }

    }

    public static final class Row extends ModelEntity<MathProgSysModel.Row> {

        public final RowType type;

        public Row(final String aName, final RowType aType) {

            super(aName);

            type = aType;

            if (type == RowType.N) {
                this.weight(ONE);
            } else {
                this.weight(null);
            }

            // 0.0 is the default RHS value
            this.rhs(ZERO);
        }

        private Row(final String aName) {

            super(aName);

            type = RowType.N;
        }

        protected Row(final Row entityToCopy) {

            super(entityToCopy);

            type = entityToCopy.type;
        }

        public final Row range(final BigDecimal aValue) {

            switch (type) {

            case E:

                final int tmpSignum = aValue.signum();
                if (tmpSignum == 1) {
                    this.upper(this.getLowerLimit().add(aValue));
                } else if (tmpSignum == -1) {
                    this.lower(this.getUpperLimit().add(aValue));
                }

                break;

            case L:

                this.lower(this.getUpperLimit().subtract(aValue.abs()));

                break;

            case G:

                this.upper(this.getLowerLimit().add(aValue.abs()));

                break;

            case N:

                this.level(null);
                this.weight(ONE);

                break;

            default:

                break;
            }

            return this;
        }

        public final Row rhs(final BigDecimal aValue) {

            switch (type) {

            case E:

                this.level(aValue);

                break;

            case L:

                this.upper(aValue);

                break;

            case G:

                this.lower(aValue);

                break;

            case N:

                this.level(null);
                this.weight(ONE);

                break;

            default:

                break;
            }

            return this;
        }

        @Override
        protected int getAdjustmentExponent() {
            return 0;
        }

    }

    private static final String COMMENT = "*";
    private static final String EMPTY = "";
    private static final String SPACE = " ";

    public static MathProgSysModel makeFromFile(final File aFile) {

        String tmpLine;
        FileSection tmpSection = null;
        final MathProgSysModel retVal = new MathProgSysModel();

        try {

            final BufferedReader tmpBufferedFileReader = new BufferedReader(new FileReader(aFile));

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
    private final int[] myFieldLimits = new int[] { 3, 12, 22, 36, 47, 61 };
    private final String[] myFields = new String[6];
    private boolean myIntegerMarker = false;
    private String myNameInFile;
    private final HashMap<String, Row> myRows = new HashMap<String, Row>();

    public MathProgSysModel() {

        super();

        this.setMinimisation();
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /**
     * @see #getActivatorVariableColumns()
     * @see #getNegativeVariableColumns()
     * @see #getPositiveVariableColumns()
     */
    public Column[] getActivatorVariableColumns() {

        final HashSet<Column> tmpSelection = new HashSet<Column>();

        for (final Column tmpColumn : myColumns.values()) {
            if (tmpColumn.needsActivator()) {
                tmpSelection.add(tmpColumn);
            }
        }

        return tmpSelection.toArray(new Column[tmpSelection.size()]);
    }

    public Row[] getConstraintRows() {

        final HashSet<Row> tmpSelection = new HashSet<Row>();

        final Collection<Row> tmpValues = myRows.values();
        for (final Row tmpRow : tmpValues) {
            if (tmpRow.isConstraint()) {
                tmpSelection.add(tmpRow);
            }
        }

        return tmpSelection.toArray(new Row[tmpSelection.size()]);
    }

    public GenericSolver getDefaultSolver() {
        return ExpressionsBasedModel.make(this).getDefaultSolver();
    }

    public Row[] getExpressionRows() {

        final Collection<Row> tmpValues = myRows.values();

        return tmpValues.toArray(new Row[tmpValues.size()]);
    }

    public final String getName() {
        return myNameInFile;
    }

    /**
     * @see #getActivatorVariableColumns()
     * @see #getNegativeVariableColumns()
     * @see #getPositiveVariableColumns()
     */
    public Column[] getNegativeVariableColumns() {

        final HashSet<Column> tmpSelection = new HashSet<Column>();

        for (final Column tmpColumn : myColumns.values()) {
            if (tmpColumn.hasNegativePart()) {
                tmpSelection.add(tmpColumn);
            }
        }

        return tmpSelection.toArray(new Column[tmpSelection.size()]);
    }

    public Row getObjectiveRow() {

        for (final Row tmpRow : myRows.values()) {
            if (tmpRow.isObjective()) {
                return tmpRow;
            }
        }

        return null;
    }

    /**
     * @see #getActivatorVariableColumns()
     * @see #getNegativeVariableColumns()
     * @see #getPositiveVariableColumns()
     */
    public Column[] getPositiveVariableColumns() {

        final HashSet<Column> tmpSelection = new HashSet<Column>();

        for (final Column tmpColumn : myColumns.values()) {
            if (tmpColumn.hasPositivePart()) {
                tmpSelection.add(tmpColumn);
            }
        }

        return tmpSelection.toArray(new Column[tmpSelection.size()]);
    }

    public Optimisation.Result maximise() {
        return ExpressionsBasedModel.make(this).maximise();
    }

    public Optimisation.Result minimise() {
        return ExpressionsBasedModel.make(this).minimise();
    }

    public boolean validate() {
        return true;
    }

    private void extractFields(final String aLine) {

        final int tmpLength = aLine.length();

        int tmpFirst = 0;
        int tmpLimit = tmpFirst;
        for (int i = 0; i < myFields.length; i++) {
            tmpLimit = Math.min(myFieldLimits[i], tmpLength);
            myFields[i] = aLine.substring(tmpFirst, tmpLimit).trim();
            tmpFirst = tmpLimit;
        }
    }

    FileSection identifySection(final String aLine) {

        final int tmpSplit = aLine.indexOf(SPACE);
        String tmpSection;
        String tmpArgument;
        if (tmpSplit != -1) {
            tmpSection = aLine.substring(0, tmpSplit).trim();
            tmpArgument = aLine.substring(tmpSplit).trim();
        } else {
            tmpSection = aLine.trim();
            tmpArgument = EMPTY;
        }

        //      BasicLogger.logDebug("Section: {},\tArgument: {}.", tmpSection, tmpArgument);

        final FileSection retVal = FileSection.valueOf(tmpSection);

        switch (retVal) {

        case NAME:

            myNameInFile = tmpArgument;

            break;

        default:

            break;
        }

        return retVal;
    }

    void parseSectionLine(final FileSection aSection, final String aLine) {

        this.extractFields(aLine);

        //      BasicLogger.logDebug("{}: {}.", aSection, Arrays.toString(myFields));

        switch (aSection) {

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
            final Row aConstraint = new Row(myFields[1], RowType.valueOf(myFields[0]));

            myRows.put(aConstraint.getName(), aConstraint);

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
                    tmpColumn.setInteger(myIntegerMarker);
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
