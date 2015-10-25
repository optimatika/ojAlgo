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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.multiary.CompoundFunction;
import org.ojalgo.function.multiary.ConstantFunction;
import org.ojalgo.function.multiary.LinearFunction;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.multiary.QuadraticFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Think of an Expression as one constraint or a component to the objective function. An expression becomes a
 * linear expression as soon as you set a linear factor. Setting a quadratic factor turns it into a quadratic
 * expression. If you set both linear and quadratic factors it is a compound expression, and if you set
 * neither it is an empty expression. Currently the solvers supplied by ojAlgo can only handle linear
 * constraint expressions. The objective function can be linear, quadratic or compound. Empty expressions
 * makes no sense...
 * </p>
 * <p>
 * An expression is turned into a constraint by setting a lower and/or upper limit. Use
 * {@linkplain ModelEntity#lower(Number)}, {@linkplain ModelEntity#upper(Number)} or
 * {@linkplain ModelEntity#level(Number)}. An expression is made part of (contributing to) the objective
 * function by setting a contribution weight. Use {@linkplain ModelEntity#weight(Number)}. The contribution
 * weight can be set to anything except zero (0.0). Often you may just want to set it to one (1.0). Other
 * values can be used to balance multiple expressions Contributing to the objective function.
 * </p>
 *
 * @author apete
 */
public final class Expression extends ModelEntity<Expression> {

    public static final class Index implements Comparable<Index> {

        public final int index;

        public Index(final int anIndex) {

            super();

            index = anIndex;
        }

        @SuppressWarnings("unused")
        private Index() {
            this(-1);
        }

        public int compareTo(final Index ref) {
            return index - ref.index;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Index)) {
                return false;
            }
            final Index other = (Index) obj;
            if (index != other.index) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + index;
            return result;
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

    }

    public static final class RowColumn implements Comparable<RowColumn> {

        public final int column;
        public final int row;

        public RowColumn(final int aRow, final int aCol) {

            super();

            row = aRow;
            column = aCol;
        }

        @SuppressWarnings("unused")
        private RowColumn() {
            this(-1, -1);
        }

        public int compareTo(final RowColumn ref) {

            if (column == ref.column) {

                return row - ref.row;

            } else {

                return column - ref.column;
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final RowColumn other = (RowColumn) obj;
            if (column != other.column) {
                return false;
            }
            if (row != other.row) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + column;
            result = (prime * result) + row;
            return result;
        }

        @Override
        public String toString() {
            return "<" + Integer.toString(row) + "," + Integer.toString(column) + ">";
        }

    }

    private transient boolean myInfeasible = false;
    private final HashMap<Index, BigDecimal> myLinear;
    private final ExpressionsBasedModel myModel;
    private final HashMap<RowColumn, BigDecimal> myQuadratic;
    private transient boolean myRedundant = false;
    private final boolean myShallowCopy;

    @SuppressWarnings("unused")
    private Expression(final Expression entityToCopy) {

        this(entityToCopy, null, false);

        ProgrammingError.throwForIllegalInvocation();
    }

    @SuppressWarnings("unused")
    private Expression(final String aName) {

        this(aName, null);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected Expression(final Expression entityToCopy, final ExpressionsBasedModel destinationModel, final boolean deep) {

        super(entityToCopy);

        myModel = destinationModel;

        if (deep) {

            myShallowCopy = false;

            myLinear = new HashMap<>();
            myLinear.putAll(entityToCopy.getLinear());

            myQuadratic = new HashMap<>();
            myQuadratic.putAll(entityToCopy.getQuadratic());

        } else {

            myShallowCopy = true;

            myLinear = entityToCopy.getLinear();
            myQuadratic = entityToCopy.getQuadratic();
        }
    }

    Expression(final String aName, final ExpressionsBasedModel model) {

        super(aName);

        myModel = model;

        myShallowCopy = false;

        myLinear = new HashMap<>();
        myQuadratic = new HashMap<>();

        ProgrammingError.throwIfNull(myModel);
        ProgrammingError.throwIfNull(myLinear);
        ProgrammingError.throwIfNull(myQuadratic);
    }

    public void add(final Index key, final Number value) {

        final BigDecimal tmpExisting = this.get(key);

        if (tmpExisting != null) {
            this.set(key, TypeUtils.toBigDecimal(value).add(tmpExisting));
        } else {
            this.set(key, value);
        }
    }

    public void add(final RowColumn key, final Number value) {

        final BigDecimal tmpExisting = this.get(key);

        if (tmpExisting != null) {
            this.set(key, TypeUtils.toBigDecimal(value).add(tmpExisting));
        } else {
            this.set(key, value);
        }
    }

    public BigDecimal evaluate(final Access1D<BigDecimal> point) {

        BigDecimal retVal = BigMath.ZERO;

        BigDecimal tmpFactor;

        for (final RowColumn tmpKey : this.getQuadraticFactorKeys()) {
            tmpFactor = this.getQuadraticFactor(tmpKey);
            retVal = retVal.add(point.get(tmpKey.row).multiply(tmpFactor).multiply(point.get(tmpKey.column)));
        }

        for (final Index tmpKey : this.getLinearFactorKeys()) {
            tmpFactor = this.getLinearFactor(tmpKey);
            retVal = retVal.add(point.get(tmpKey.index).multiply(tmpFactor));
        }

        return retVal;
    }

    public BigDecimal get(final Index key) {
        return this.getLinearFactor(key, false);
    }

    public BigDecimal get(final RowColumn key) {
        return this.getQuadraticFactor(key, false);
    }

    public MatrixStore<Double> getAdjustedGradient(final Access1D<?> point) {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(myModel.countVariables(), 1);

        final BinaryFunction<Double> tmpBaseFunc = PrimitiveFunction.ADD;
        double tmpAdjustedFactor;
        UnaryFunction<Double> tmpModFunc;
        for (final RowColumn tmpKey : this.getQuadraticFactorKeys()) {
            tmpAdjustedFactor = this.getAdjustedQuadraticFactor(tmpKey);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor * point.doubleValue(tmpKey.column));
            retVal.modifyOne(tmpKey.row, 0, tmpModFunc);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor * point.doubleValue(tmpKey.row));
            retVal.modifyOne(tmpKey.column, 0, tmpModFunc);
        }

        for (final Index tmpKey : this.getLinearFactorKeys()) {
            tmpAdjustedFactor = this.getAdjustedLinearFactor(tmpKey);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor);
            retVal.modifyOne(tmpKey.index, 0, tmpModFunc);
        }

        return retVal;
    }

    public MatrixStore<Double> getAdjustedHessian() {

        final int tmpCountVariables = myModel.countVariables();
        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(tmpCountVariables, tmpCountVariables);

        final BinaryFunction<Double> tmpBaseFunc = PrimitiveFunction.ADD;
        UnaryFunction<Double> tmpModFunc;
        for (final RowColumn tmpKey : this.getQuadraticFactorKeys()) {
            tmpModFunc = tmpBaseFunc.second(this.getAdjustedQuadraticFactor(tmpKey));
            retVal.modifyOne(tmpKey.row, tmpKey.column, tmpModFunc);
            retVal.modifyOne(tmpKey.column, tmpKey.row, tmpModFunc);
        }

        return retVal;
    }

    public double getAdjustedLinearFactor(final Index key) {
        return this.getLinearFactor(key, true).doubleValue();
    }

    public double getAdjustedLinearFactor(final int aVar) {
        return this.getAdjustedLinearFactor(new Index(aVar));
    }

    public double getAdjustedLinearFactor(final Variable aVar) {
        return this.getAdjustedLinearFactor(aVar.getIndex());
    }

    public double getAdjustedQuadraticFactor(final int aVar1, final int aVar2) {
        return this.getAdjustedQuadraticFactor(new RowColumn(aVar1, aVar2));
    }

    public double getAdjustedQuadraticFactor(final RowColumn key) {
        return this.getQuadraticFactor(key, true).doubleValue();
    }

    public double getAdjustedQuadraticFactor(final Variable aVar1, final Variable aVar2) {
        return this.getAdjustedQuadraticFactor(myModel.indexOf(aVar1), myModel.indexOf(aVar2));
    }

    /**
     * @return compensated for fixed varables and adjusted
     */
    public double getCompensatedLowerLimit(final Collection<Index> fixedVariables) {
        return this.convert(this.compensateLowerLimit(fixedVariables), true).doubleValue();
    }

    /**
     * @return compensated for fixed varables and adjusted
     */
    public double getCompensatedUpperLimit(final Collection<Index> fixedVariables) {
        return this.convert(this.compensateUpperLimit(fixedVariables), true).doubleValue();
    }

    public BigDecimal getLinearFactor(final Index key) {
        return this.get(key);
    }

    public BigDecimal getLinearFactor(final int globalIndex) {
        return this.getLinearFactor(new Index(globalIndex));
    }

    public BigDecimal getLinearFactor(final Variable variable) {
        return this.getLinearFactor(variable.getIndex());
    }

    public Set<Expression.Index> getLinearFactorKeys() {
        return myLinear.keySet();
    }

    public BigDecimal getQuadraticFactor(final int aVar1, final int aVar2) {
        return this.getQuadraticFactor(new RowColumn(aVar1, aVar2));
    }

    public BigDecimal getQuadraticFactor(final RowColumn key) {
        return this.get(key);
    }

    public BigDecimal getQuadraticFactor(final Variable aRowVar, final Variable aColVar) {
        return this.getQuadraticFactor(myModel.indexOf(aRowVar), myModel.indexOf(aColVar));
    }

    public Set<Expression.RowColumn> getQuadraticFactorKeys() {
        return myQuadratic.keySet();
    }

    public boolean isAnyLinearFactorNonZero() {
        return myLinear.size() > 0;
    }

    public boolean isAnyQuadraticFactorNonZero() {
        return myQuadratic.size() > 0;
    }

    public boolean isFunctionCompound() {
        return this.isAnyQuadraticFactorNonZero() && this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionLinear() {
        return !this.isAnyQuadraticFactorNonZero() && this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionQuadratic() {
        return this.isAnyQuadraticFactorNonZero() && !this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionZero() {
        return !this.isAnyQuadraticFactorNonZero() && !this.isAnyLinearFactorNonZero();
    }

    public void set(final Index key, final Number value) {

        if (key != null) {

            final BigDecimal tmpValue = TypeUtils.toBigDecimal(value);

            if (tmpValue.signum() != 0) {
                myLinear.put(key, tmpValue);
            } else {
                myLinear.remove(key);
            }

        } else {

            throw new IllegalArgumentException();
        }
    }

    public void set(final RowColumn key, final Number value) {

        if (key != null) {

            final BigDecimal tmpValue = TypeUtils.toBigDecimal(value);

            if (tmpValue.signum() != 0) {
                myQuadratic.put(key, tmpValue);
            } else {
                myQuadratic.remove(key);
            }

        } else {

            throw new IllegalArgumentException();
        }
    }

    /**
     * Will set the quadratic and linear factors to an expression that measures (the square of) the distance
     * from the given point.
     *
     * @param variables The relevant variables
     * @param point The point to measure from
     */
    public void setCompoundFactorsOffset(final List<Variable> variables, final Access1D<?> point) {

        final int tmpLength = variables.size();

        if (point.count() != tmpLength) {
            throw new IllegalArgumentException();
        }

        final BigDecimal tmpLinearWeight = BigMath.TWO.negate();

        Variable tmpVariable;
        BigDecimal tmpVal;
        for (int ij = 0; ij < tmpLength; ij++) {

            tmpVariable = variables.get(ij);
            tmpVal = TypeUtils.toBigDecimal(point.get(ij));

            this.setQuadraticFactor(tmpVariable, tmpVariable, BigMath.ONE);

            this.setLinearFactor(tmpVariable, tmpVal.multiply(tmpLinearWeight));
        }
    }

    public void setLinearFactor(final Index key, final Number value) {
        this.set(key, value);
    }

    public void setLinearFactor(final int globalIndex, final Number value) {
        this.setLinearFactor(new Index(globalIndex), value);
    }

    public void setLinearFactor(final Variable variable, final Number value) {
        this.setLinearFactor(variable.getIndex(), value);
    }

    public void setLinearFactors(final List<Variable> variables, final Access1D<?> factors) {

        final int tmpLimit = variables.size();

        if (factors.count() != tmpLimit) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < tmpLimit; i++) {
            this.setLinearFactor(variables.get(i), factors.get(i));
        }
    }

    /**
     * Will set the linear factors to a simple sum expression - all factors equal 1.0.
     *
     * @param variables The relevant variables
     */
    public void setLinearFactorsSimple(final List<Variable> variables) {
        for (final Variable tmpVariable : variables) {
            this.setLinearFactor(tmpVariable, BigMath.ONE);
        }
    }

    public void setQuadraticFactor(final int globalIndex1, final int globalIndex2, final Number value) {
        this.setQuadraticFactor(new RowColumn(globalIndex1, globalIndex2), value);
    }

    public void setQuadraticFactor(final RowColumn key, final Number value) {
        this.set(key, value);
    }

    public void setQuadraticFactor(final Variable variable1, final Variable variable2, final Number value) {
        this.setQuadraticFactor(myModel.indexOf(variable1), myModel.indexOf(variable2), value);
    }

    public void setQuadraticFactors(final List<Variable> variables, final Access2D<?> factors) {

        final int tmpLimit = variables.size();

        if ((factors.countRows() != tmpLimit) || (factors.countColumns() != tmpLimit)) {
            throw new IllegalArgumentException();
        }

        for (int j = 0; j < tmpLimit; j++) {
            final Variable tmpVar2 = variables.get(j);
            for (int i = 0; i < tmpLimit; i++) {
                this.setQuadraticFactor(variables.get(i), tmpVar2, factors.get(i, j));
            }
        }
    }

    public MultiaryFunction.TwiceDifferentiable<Double> toFunction() {

        if (this.isFunctionCompound()) {
            return this.getCompoundFunction();
        } else if (this.isFunctionQuadratic()) {
            return this.getQuadraticFunction();
        } else if (this.isFunctionLinear()) {
            return this.getLinearFunction();
        } else {
            return this.getZeroFunction();
        }
    }

    private final BigDecimal convert(final BigDecimal value, final boolean adjusted) {

        if (value != null) {

            if (adjusted) {

                final int tmpAdjExp = this.getAdjustmentExponent();

                if (tmpAdjExp != 0) {

                    return value.movePointRight(tmpAdjExp);

                } else {

                    return value;
                }

            } else {

                return value;
            }

        } else {

            return BigMath.ZERO;
        }
    }

    private double evaluateBody(final Access1D<?> point) {

        double retVal = PrimitiveMath.ZERO;

        double tmpAdjustedFactor;

        for (final RowColumn tmpKey : this.getQuadraticFactorKeys()) {
            tmpAdjustedFactor = this.getAdjustedQuadraticFactor(tmpKey);
            retVal += point.doubleValue(tmpKey.row) * tmpAdjustedFactor * point.doubleValue(tmpKey.column);
        }

        for (final Index tmpKey : this.getLinearFactorKeys()) {
            tmpAdjustedFactor = this.getAdjustedLinearFactor(tmpKey);
            retVal += point.doubleValue(tmpKey.index) * tmpAdjustedFactor;
        }

        return retVal;
    }

    protected void appendMiddlePart(final StringBuilder builder, final Access1D<BigDecimal> currentSolution) {

        builder.append(this.getName());
        builder.append(": ");
        builder.append(OptimisationUtils.DISPLAY.enforce(this.toFunction().invoke(AccessUtils.asPrimitive1D(currentSolution))));

        if (this.isObjective()) {
            builder.append(" (");
            builder.append(OptimisationUtils.DISPLAY.enforce(this.getContributionWeight()));
            builder.append(")");
        }
    }

    @Override
    protected void destroy() {

        super.destroy();

        if (!myShallowCopy) {
            myLinear.clear();
            myQuadratic.clear();
        }
    }

    protected boolean validate(final Access1D<BigDecimal> solution, final NumberContext context, final BasicLogger.Appender appender) {

        final BigDecimal tmpValue = this.evaluate(solution);

        return this.validate(tmpValue, context, appender);
    }

    void appendToString(final StringBuilder aStringBuilder, final Access1D<BigDecimal> aCurrentState) {

        this.appendLeftPart(aStringBuilder);
        if (aCurrentState != null) {
            this.appendMiddlePart(aStringBuilder, aCurrentState);
        } else {
            this.appendMiddlePart(aStringBuilder);
        }
        this.appendRightPart(aStringBuilder);
    }

    /**
     * Will return null if none of the fixed variables affect the value of this expression. If any fixed
     * variable does affect the value of the expressions the returned value could be zero ( 0.0 ) but must be
     * assumed to be different from zero.
     */
    BigDecimal calculateFixedValue(final Collection<Index> fixedVariables) {

        BigDecimal retVal = null;

        for (final Index tmpIndex : fixedVariables) {

            final BigDecimal tmpFactor = this.getLinearFactor(tmpIndex);
            final BigDecimal tmpValue = myModel.getVariable(tmpIndex.index).getValue();

            BigDecimal tmpContribution = null;
            if ((tmpFactor != null) && (tmpValue.signum() != 0)) {
                tmpContribution = tmpFactor.multiply(tmpValue);
            }

            if (retVal != null) {
                if (tmpContribution != null) {
                    retVal = retVal.add(tmpContribution);
                }
            } else {
                if (tmpContribution != null) {
                    retVal = tmpContribution;
                }
            }
        }

        return retVal;
    }

    BigDecimal compensateLowerLimit(final Collection<Index> fixedVariables) {

        BigDecimal tmpFixed = null;

        if (this.isLowerLimitSet() && ((tmpFixed = this.calculateFixedValue(fixedVariables)) != null)) {

            return this.getLowerLimit().subtract(tmpFixed);

        } else {

            return this.getLowerLimit();
        }
    }

    BigDecimal compensateUpperLimit(final Collection<Index> fixedVariables) {

        BigDecimal tmpFixed = null;

        if (this.isUpperLimitSet() && ((tmpFixed = this.calculateFixedValue(fixedVariables)) != null)) {

            return this.getUpperLimit().subtract(tmpFixed);

        } else {

            return this.getUpperLimit();
        }
    }

    Expression copy(final ExpressionsBasedModel destinationModel, final boolean deep) {
        return new Expression(this, destinationModel, deep);
    }

    int countLinearFactors() {
        return myLinear.size();
    }

    int countQuadraticFactors() {
        return myQuadratic.size();
    }

    CompoundFunction<Double> getCompoundFunction() {

        final CompoundFunction<Double> retVal = CompoundFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyQuadraticFactorNonZero()) {
            for (final Entry<RowColumn, BigDecimal> tmpEntry : myQuadratic.entrySet()) {
                retVal.quadratic().set(tmpEntry.getKey().row, tmpEntry.getKey().column, tmpEntry.getValue().doubleValue());
            }
        }

        if (this.isAnyLinearFactorNonZero()) {
            for (final Entry<Index, BigDecimal> tmpEntry : myLinear.entrySet()) {
                retVal.linear().set(tmpEntry.getKey().index, tmpEntry.getValue().doubleValue());
            }
        }

        return retVal;
    }

    HashMap<Index, BigDecimal> getLinear() {
        return myLinear;
    }

    BigDecimal getLinearFactor(final Index key, final boolean adjusted) {
        return this.convert(myLinear.get(key), adjusted);
    }

    LinearFunction<Double> getLinearFunction() {

        final LinearFunction<Double> retVal = LinearFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyLinearFactorNonZero()) {
            for (final Entry<Index, BigDecimal> tmpEntry : myLinear.entrySet()) {
                retVal.linear().set(tmpEntry.getKey().index, tmpEntry.getValue().doubleValue());
            }
        }

        return retVal;
    }

    ExpressionsBasedModel getModel() {
        return myModel;
    }

    HashMap<RowColumn, BigDecimal> getQuadratic() {
        return myQuadratic;
    }

    BigDecimal getQuadraticFactor(final RowColumn key, final boolean adjusted) {
        return this.convert(myQuadratic.get(key), adjusted);
    }

    QuadraticFunction<Double> getQuadraticFunction() {

        final QuadraticFunction<Double> retVal = QuadraticFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyQuadraticFactorNonZero()) {
            for (final Entry<RowColumn, BigDecimal> tmpEntry : myQuadratic.entrySet()) {
                retVal.quadratic().set(tmpEntry.getKey().row, tmpEntry.getKey().column, tmpEntry.getValue().doubleValue());
            }
        }

        return retVal;
    }

    ConstantFunction<Double> getZeroFunction() {
        return ConstantFunction.makePrimitive(myModel.countVariables());
    }

    boolean isInfeasible() {
        return myInfeasible;
    }

    /**
     * @param fixedVariables The indices of the fixed variables
     * @return true if none of the free (not fixed) variables can make a positve contribution to the
     *         expression value
     */
    boolean isNegative(final Set<Index> fixedVariables) {

        boolean retVal = !this.isAnyQuadraticFactorNonZero();

        if (retVal) {
            for (final Entry<Index, BigDecimal> tmpLinear : this.getLinear().entrySet()) {
                if (retVal && !fixedVariables.contains(tmpLinear.getKey())) {
                    final Variable tmpFreeVariable = myModel.getVariable(tmpLinear.getKey().index);
                    if ((tmpLinear.getValue().signum() < 0) && tmpFreeVariable.isLowerLimitSet() && (tmpFreeVariable.getLowerLimit().signum() >= 0)) {
                        retVal &= true;
                    } else if ((tmpLinear.getValue().signum() > 0) && tmpFreeVariable.isUpperLimitSet() && (tmpFreeVariable.getUpperLimit().signum() <= 0)) {
                        retVal &= true;
                    } else {
                        retVal &= false;
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * @param fixedVariables The indices of the fixed variables
     * @return true if none of the free (not fixed) variables can make a negative contribution to the
     *         expression value
     */
    boolean isPositive(final Set<Index> fixedVariables) {

        boolean retVal = !this.isAnyQuadraticFactorNonZero();

        if (retVal) {
            for (final Entry<Index, BigDecimal> tmpLinear : this.getLinear().entrySet()) {
                if (retVal && !fixedVariables.contains(tmpLinear.getKey())) {
                    final Variable tmpFreeVariable = myModel.getVariable(tmpLinear.getKey().index);
                    if ((tmpLinear.getValue().signum() > 0) && tmpFreeVariable.isLowerLimitSet() && (tmpFreeVariable.getLowerLimit().signum() >= 0)) {
                        retVal &= true;
                    } else if ((tmpLinear.getValue().signum() < 0) && tmpFreeVariable.isUpperLimitSet() && (tmpFreeVariable.getUpperLimit().signum() <= 0)) {
                        retVal &= true;
                    } else {
                        retVal &= false;
                    }
                }
            }
        }

        return retVal;
    }

    boolean isRedundant() {
        return myRedundant;
    }

    void setInfeasible(final boolean infeasible) {
        myInfeasible = infeasible;
    }

    void setRedundant(final boolean redundant) {
        myRedundant = redundant;
    }

    @Override
    void visitAllParameters(final VoidFunction<BigDecimal> largest, final VoidFunction<BigDecimal> smallest) {

        if (this.isAnyQuadraticFactorNonZero()) {
            for (final BigDecimal tmpQuadraticFactor : myQuadratic.values()) {
                largest.invoke(tmpQuadraticFactor);
                smallest.invoke(tmpQuadraticFactor);
            }
        } else if (this.isAnyLinearFactorNonZero()) {
            for (final BigDecimal tmpLinearFactor : myLinear.values()) {
                largest.invoke(tmpLinearFactor);
                smallest.invoke(tmpLinearFactor);
            }
        } else {
            super.visitAllParameters(largest, smallest);
        }
    }

}
