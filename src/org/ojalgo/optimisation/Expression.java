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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorCollection;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.function.multiary.CompoundFunction;
import org.ojalgo.function.multiary.ConstantFunction;
import org.ojalgo.function.multiary.LinearFunction;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.multiary.QuadraticFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * <p>
 * Think of an Expression as one constraint or a component to the objective function. An expression becomes a linear
 * expression as soon as you set a linear factor. Setting a quadratic factor turns it into a quadratic expression. If
 * you set both linear and quadratic factors it is a compound expression, and if you set neither it is an empty
 * expression. Currently the solvers supplied by ojAlgo can only handle linear constraint expressions. The objective
 * function can be linear, quadratic or compound. Empty expressions makes no sense...
 * </p>
 * <p>
 * An expression is turned into a constraint by setting a lower and/or upper limit. Use
 * {@linkplain ModelEntity#lower(BigDecimal)}, {@linkplain ModelEntity#upper(BigDecimal)} or
 * {@linkplain ModelEntity#level(BigDecimal)}. An expression is made part of (contributing to) the objective function by
 * setting a contribution weight. Use {@linkplain ModelEntity#weight(BigDecimal)}. The contribution weight can be set to
 * anything except zero (0.0). Often you may just want to set it to one (1.0). Other values can be used to balance
 * multiple expressions Contributing to the objective function.
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

    private transient int myAdjustmentExponent = Integer.MIN_VALUE;
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

    Expression(final String aName, final ExpressionsBasedModel aModel) {

        super(aName);

        myModel = aModel;

        myShallowCopy = false;

        myLinear = new HashMap<>();
        myQuadratic = new HashMap<>();

        ProgrammingError.throwIfNull(myModel);
        ProgrammingError.throwIfNull(myLinear);
        ProgrammingError.throwIfNull(myQuadratic);
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

    public double evaluateLessThanZero(final Access1D<?> point) {

        final double tmpBody = this.evaluateBody(point);

        if (this.isUpperLimitSet()) {
            return tmpBody - this.getAdjustedUpperLimit();
        } else if (this.isLowerLimitSet()) {
            return this.getAdjustedLowerLimit() - tmpBody;
        } else {
            return tmpBody;
        }
    }

    public double evaluateMoreThanZero(final Access1D<?> point) {

        final double tmpBody = this.evaluateBody(point);

        if (this.isLowerLimitSet()) {
            return tmpBody - this.getAdjustedLowerLimit();
        } else if (this.isUpperLimitSet()) {
            return this.getAdjustedUpperLimit() - tmpBody;
        } else {
            return tmpBody;
        }
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

    public double getAdjustedLinearFactor(final Index aKey) {
        return this.getLinearFactor(aKey, true).doubleValue();
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

    public double getAdjustedQuadraticFactor(final RowColumn aKey) {
        return this.getQuadraticFactor(aKey, true).doubleValue();
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

    public BigDecimal getLinearFactor(final Index aKey) {
        return this.getLinearFactor(aKey, false);
    }

    public BigDecimal getLinearFactor(final int aVar) {
        return this.getLinearFactor(new Index(aVar));
    }

    public BigDecimal getLinearFactor(final Variable aVar) {
        return this.getLinearFactor(aVar.getIndex());
    }

    public Set<Expression.Index> getLinearFactorKeys() {
        return myLinear.keySet();
    }

    public BigDecimal getQuadraticFactor(final int aVar1, final int aVar2) {
        return this.getQuadraticFactor(new RowColumn(aVar1, aVar2));
    }

    public BigDecimal getQuadraticFactor(final RowColumn aKey) {
        return this.getQuadraticFactor(aKey, false);
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

    /**
     * Will set the quadratic and linear factors to an expression that measures (the square of) the distance from the
     * given point.
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

    public void setLinearFactor(final Index aKey, final Number aValue) {

        if (aKey != null) {

            final BigDecimal tmpValue = TypeUtils.toBigDecimal(aValue);

            if (tmpValue.signum() != 0) {
                myLinear.put(aKey, tmpValue);
            } else {
                myLinear.remove(aKey);
            }

            myAdjustmentExponent = Integer.MIN_VALUE;

        } else {

            throw new IllegalArgumentException();
        }
    }

    public void setLinearFactor(final int aVar, final Number aValue) {
        this.setLinearFactor(new Index(aVar), aValue);
    }

    public void setLinearFactor(final Variable aVar, final Number aValue) {
        this.setLinearFactor(aVar.getIndex(), aValue);
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

    public void setQuadraticFactor(final int aVar1, final int aVar2, final Number aValue) {
        this.setQuadraticFactor(new RowColumn(aVar1, aVar2), aValue);
    }

    public void setQuadraticFactor(final RowColumn aKey, final Number aValue) {

        if (aKey != null) {

            final BigDecimal tmpValue = TypeUtils.toBigDecimal(aValue);

            if (tmpValue.signum() != 0) {
                myQuadratic.put(aKey, tmpValue);
            } else {
                myQuadratic.remove(aKey);
            }

            myAdjustmentExponent = Integer.MIN_VALUE;

        } else {

            throw new IllegalArgumentException();
        }
    }

    public void setQuadraticFactor(final Variable aVar1, final Variable aVar2, final Number aValue) {
        this.setQuadraticFactor(myModel.indexOf(aVar1), myModel.indexOf(aVar2), aValue);
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

    public boolean validate(final Access1D<BigDecimal> solution, final NumberContext context) {

        final BigDecimal tmpValue = this.evaluate(solution);

        return this.validate(tmpValue, context);
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

    @Override
    protected int getAdjustmentExponent() {

        if (myAdjustmentExponent == Integer.MIN_VALUE) {

            final AggregatorCollection<BigDecimal> tmpCollection = BigAggregator.getCollection();
            final AggregatorFunction<BigDecimal> tmpLargestAggr = tmpCollection.largest();
            final AggregatorFunction<BigDecimal> tmpSmallestAggr = tmpCollection.smallest();

            for (final BigDecimal tmpLinearFactor : myLinear.values()) {
                tmpLargestAggr.invoke(tmpLinearFactor);
                tmpSmallestAggr.invoke(tmpLinearFactor);
            }

            for (final BigDecimal tmpQuadraticFactor : myQuadratic.values()) {
                tmpLargestAggr.invoke(tmpQuadraticFactor);
                tmpSmallestAggr.invoke(tmpQuadraticFactor);
            }

            final BigDecimal tmpLowerLimit = this.getLowerLimit();
            if (tmpLowerLimit != null) {
                tmpLargestAggr.invoke(tmpLowerLimit);
                tmpSmallestAggr.invoke(tmpLowerLimit);
            }

            final BigDecimal tmpUpperLimit = this.getUpperLimit();
            if (tmpUpperLimit != null) {
                tmpLargestAggr.invoke(tmpUpperLimit);
                tmpSmallestAggr.invoke(tmpUpperLimit);
            }

            myAdjustmentExponent = OptimisationUtils.getAdjustmentFactorExponent(tmpLargestAggr, tmpSmallestAggr);
        }

        return myAdjustmentExponent;
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

    Expression copy(final ExpressionsBasedModel aModel, final boolean deep) {
        return new Expression(this, aModel, deep);
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

    boolean isRedundant() {
        return myRedundant;
    }

    /**
     * @return true if this expression was changed from not redundant to redundant
     */
    boolean simplify() {

        myRedundant = myRedundant || !this.isConstraint();
        final boolean tmpInitiallyRedundant = myRedundant;

        final Set<Index> tmpFixedVariables = myModel.getFixedVariables();
        if (!tmpInitiallyRedundant && (this.countQuadraticFactors() == 0) && (this.countLinearFactors() <= (tmpFixedVariables.size() + 1))) {
            // This constraint can possibly be reduced to 0 or 1 remaining linear factors

            BigDecimal tmpFixedValue = this.calculateFixedValue(tmpFixedVariables);
            if (tmpFixedValue == null) {
                tmpFixedValue = BigMath.ZERO;
            }

            if (tmpFixedValue != null) {
                // The fixed variables are part of this expression

                final HashSet<Index> tmpLinear = new HashSet<Index>(this.getLinearFactorKeys());
                tmpLinear.removeAll(tmpFixedVariables);

                if (tmpLinear.size() == 0) {
                    // This constraint has 0 remaining free variable
                    // It is entirely redundant

                    myInfeasible = !this.validate(tmpFixedValue, myModel.options.slack);
                    if (!myInfeasible) {
                        myRedundant = true;
                        this.level(tmpFixedValue);
                    } else {
                        myRedundant = false;
                    }

                } else if (tmpLinear.size() == 1) {
                    // This constraint has 1 remaining free variable
                    // The lower/upper limits can be transferred to that variable, and the expression marked as redundant

                    final Index tmpIndex = (Index) tmpLinear.toArray()[0];
                    final Variable tmpVariable = myModel.getVariable(tmpIndex.index);
                    final BigDecimal tmpFactor = this.getLinearFactor(tmpIndex);

                    if (this.isEqualityConstraint()) {
                        // Simple case with equality constraint

                        final BigDecimal tmpCompensatedLevel = BigFunction.SUBTRACT.invoke(this.getUpperLimit(), tmpFixedValue);
                        final BigDecimal tmpSolutionValue = BigFunction.DIVIDE.invoke(tmpCompensatedLevel, tmpFactor);

                        myInfeasible = !tmpVariable.validate(tmpSolutionValue, myModel.options.slack);
                        if (!myInfeasible) {
                            myRedundant = true;
                            tmpVariable.level(tmpSolutionValue);
                        } else {
                            myRedundant = false;
                        }

                    } else {
                        // More general case

                        final BigDecimal tmpLowerLimit = this.getLowerLimit();
                        final BigDecimal tmpUpperLimit = this.getUpperLimit();

                        final BigDecimal tmpCompensatedLower = tmpLowerLimit != null ? BigFunction.SUBTRACT.invoke(tmpLowerLimit, tmpFixedValue)
                                : tmpLowerLimit;
                        final BigDecimal tmpCompensatedUpper = tmpUpperLimit != null ? BigFunction.SUBTRACT.invoke(tmpUpperLimit, tmpFixedValue)
                                : tmpUpperLimit;

                        BigDecimal tmpLowerSolution = tmpCompensatedLower != null ? BigFunction.DIVIDE.invoke(tmpCompensatedLower, tmpFactor)
                                : tmpCompensatedLower;
                        BigDecimal tmpUpperSolution = tmpCompensatedUpper != null ? BigFunction.DIVIDE.invoke(tmpCompensatedUpper, tmpFactor)
                                : tmpCompensatedUpper;
                        if (tmpFactor.signum() < 0) {
                            final BigDecimal tmpVal = tmpLowerSolution;
                            tmpLowerSolution = tmpUpperSolution;
                            tmpUpperSolution = tmpVal;
                        }

                        final BigDecimal tmpOldLower = tmpVariable.getLowerLimit();
                        final BigDecimal tmpOldUpper = tmpVariable.getUpperLimit();

                        BigDecimal tmpNewLower = tmpOldLower;
                        if (tmpLowerSolution != null) {
                            if (tmpOldLower != null) {
                                tmpNewLower = tmpOldLower.max(tmpLowerSolution);
                            } else {
                                tmpNewLower = tmpLowerSolution;
                            }
                        }

                        BigDecimal tmpNewUpper = tmpOldUpper;
                        if (tmpUpperSolution != null) {
                            if (tmpOldUpper != null) {
                                tmpNewUpper = tmpOldUpper.min(tmpUpperSolution);
                            } else {
                                tmpNewUpper = tmpUpperSolution;
                            }
                        }

                        if (tmpVariable.isInteger()) {
                            if (tmpNewLower != null) {
                                tmpNewLower = tmpNewLower.setScale(0, RoundingMode.CEILING);
                            }
                            if (tmpNewUpper != null) {
                                tmpNewUpper = tmpNewUpper.setScale(0, RoundingMode.FLOOR);
                            }
                        }

                        myInfeasible = (tmpNewLower != null) && (tmpNewUpper != null) && (tmpNewLower.compareTo(tmpNewUpper) > 0);
                        if (!myInfeasible) {
                            myRedundant = true;
                            tmpVariable.lower(tmpNewLower).upper(tmpNewUpper);
                        } else {
                            myRedundant = false;
                        }

                        //                        BasicLogger.logDebug("{} < {} -> {} < {} ( {} < {} )", tmpOldLower, tmpOldUpper, tmpNewLower, tmpNewUpper, tmpLowerSolution,
                        //                                tmpUpperSolution);
                    }

                    if (tmpVariable.isEqualityConstraint()) {
                        tmpVariable.setValue(tmpVariable.getLowerLimit());
                        myModel.addFixedVariable(tmpIndex);
                    }
                }

                return !tmpInitiallyRedundant && myRedundant;

            } else {

                // Didn't change anything: No fixed value
                return false;
            }

        } else {

            // Didn't change anything: Already redundant, quadratic or not enough fixed variables
            return false;
        }
    }

}
