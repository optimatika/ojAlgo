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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.IntIndex;
import org.ojalgo.access.IntRowColumn;
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

    private transient boolean myInfeasible = false;
    private final HashMap<IntIndex, BigDecimal> myLinear;
    private final ExpressionsBasedModel myModel;
    private final HashMap<IntRowColumn, BigDecimal> myQuadratic;
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

    protected Expression(final Expression expressionToCopy, final ExpressionsBasedModel destinationModel, final boolean deep) {

        super(expressionToCopy);

        myModel = destinationModel;

        if (deep) {

            myShallowCopy = false;

            myLinear = new HashMap<>();
            myLinear.putAll(expressionToCopy.getLinear());

            myQuadratic = new HashMap<>();
            myQuadratic.putAll(expressionToCopy.getQuadratic());

        } else {

            myShallowCopy = true;

            myLinear = expressionToCopy.getLinear();
            myQuadratic = expressionToCopy.getQuadratic();
        }
    }

    Expression(final String name, final ExpressionsBasedModel model) {

        super(name);

        myModel = model;

        myShallowCopy = false;

        myLinear = new HashMap<>();
        myQuadratic = new HashMap<>();

        ProgrammingError.throwIfNull(myModel);
        ProgrammingError.throwIfNull(myLinear);
        ProgrammingError.throwIfNull(myQuadratic);
    }

    public Expression add(final IntIndex key, final Number value) {

        final BigDecimal tmpExisting = myLinear.get(key);

        if (tmpExisting != null) {
            this.set(key, TypeUtils.toBigDecimal(value).add(tmpExisting));
        } else {
            this.set(key, value);
        }

        return this;
    }

    public Expression add(final IntRowColumn key, final Number value) {

        final BigDecimal tmpExisting = myQuadratic.get(key);

        if (tmpExisting != null) {
            this.set(key, TypeUtils.toBigDecimal(value).add(tmpExisting));
        } else {
            this.set(key, value);
        }

        return this;
    }

    /**
     * Will return an Expression with factors corresponding to fixed variables removed, and lower/upper limits
     * compensated for the fixed part of the expression. Factors corresponding to bilinear variables, where
     * one is fixed and the other is not, are linearized.
     *
     * @param fixedVariables A set of (by the presolver) fixed variable indices
     * @return The reduced/modified expression
     */
    public Expression compensate(final Set<IntIndex> fixedVariables) {

        if ((fixedVariables.size() == 0) || (!this.isAnyQuadraticFactorNonZero() && Collections.disjoint(fixedVariables, this.getLinearKeySet()))) {

            return this; // No need to copy/compensate anything

        } else {

            final ExpressionsBasedModel tmpModel = this.getModel();

            final Expression retVal = new Expression(this.getName(), tmpModel);

            BigDecimal tmpFixedValue = BigMath.ZERO;

            for (final Entry<IntIndex, BigDecimal> tmpEntry : myLinear.entrySet()) {

                final IntIndex tmpKey = tmpEntry.getKey();
                final BigDecimal tmpFactor = tmpEntry.getValue();

                if (fixedVariables.contains(tmpKey)) {
                    // Fixed

                    final BigDecimal tmpValue = tmpModel.getVariable(tmpKey.index).getValue();

                    tmpFixedValue = tmpFixedValue.add(tmpFactor.multiply(tmpValue));

                } else {
                    // Not fixed

                    retVal.set(tmpKey, tmpFactor);
                }
            }

            for (final Entry<IntRowColumn, BigDecimal> tmpEntry : myQuadratic.entrySet()) {

                final IntRowColumn tmpKey = tmpEntry.getKey();
                final BigDecimal tmpFactor = tmpEntry.getValue();

                final Variable tmpRowVariable = tmpModel.getVariable(tmpKey.row);
                final Variable tmpColVariable = tmpModel.getVariable(tmpKey.column);

                final IntIndex tmpRowKey = tmpRowVariable.getIndex();
                final IntIndex tmpColKey = tmpColVariable.getIndex();

                if (fixedVariables.contains(tmpRowKey)) {

                    final BigDecimal tmpRowValue = tmpRowVariable.getValue();

                    if (fixedVariables.contains(tmpColKey)) {
                        // Both fixed

                        final BigDecimal tmpColValue = tmpColVariable.getValue();

                        tmpFixedValue = tmpFixedValue.add(tmpFactor.multiply(tmpRowValue).multiply(tmpColValue));

                    } else {
                        // Row fixed

                        retVal.add(tmpColKey, tmpFactor.multiply(tmpRowValue));
                    }

                } else {

                    if (fixedVariables.contains(tmpColKey)) {
                        // Column fixed

                        final BigDecimal tmpColValue = tmpColVariable.getValue();

                        retVal.add(tmpRowKey, tmpFactor.multiply(tmpColValue));

                    } else {
                        // Neither fixed

                        retVal.set(tmpKey, tmpFactor);
                    }
                }
            }

            if (this.isLowerLimitSet()) {
                retVal.lower(this.getLowerLimit().subtract(tmpFixedValue));
            }

            if (this.isUpperLimitSet()) {
                retVal.upper(this.getUpperLimit().subtract(tmpFixedValue));
            }

            return retVal;
        }

    }

    public BigDecimal evaluate(final Access1D<BigDecimal> point) {

        BigDecimal retVal = BigMath.ZERO;

        BigDecimal tmpFactor;

        for (final IntRowColumn tmpKey : this.getQuadraticKeySet()) {
            tmpFactor = this.get(tmpKey);
            retVal = retVal.add(tmpFactor.multiply(point.get(tmpKey.row)).multiply(point.get(tmpKey.column)));
        }

        for (final IntIndex tmpKey : this.getLinearKeySet()) {
            tmpFactor = this.get(tmpKey);
            retVal = retVal.add(tmpFactor.multiply(point.get(tmpKey.index)));
        }

        return retVal;
    }

    public BigDecimal get(final IntIndex key) {
        return this.getLinearFactor(key, false);
    }

    public BigDecimal get(final IntRowColumn key) {
        return this.getQuadraticFactor(key, false);
    }

    public BigDecimal get(final Variable variable) {
        final IntIndex tmpIndex = variable.getIndex();
        if (tmpIndex != null) {
            return this.get(tmpIndex);
        } else {
            throw new IllegalStateException("Variable not part of (this) model!");
        }
    }

    public MatrixStore<Double> getAdjustedGradient(final Access1D<?> point) {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(myModel.countVariables(), 1);

        final BinaryFunction<Double> tmpBaseFunc = PrimitiveFunction.ADD;
        double tmpAdjustedFactor;
        UnaryFunction<Double> tmpModFunc;
        for (final IntRowColumn tmpKey : this.getQuadraticKeySet()) {
            tmpAdjustedFactor = this.getAdjustedQuadraticFactor(tmpKey);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor * point.doubleValue(tmpKey.column));
            retVal.modifyOne(tmpKey.row, 0, tmpModFunc);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor * point.doubleValue(tmpKey.row));
            retVal.modifyOne(tmpKey.column, 0, tmpModFunc);
        }

        for (final IntIndex tmpKey : this.getLinearKeySet()) {
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
        for (final IntRowColumn tmpKey : this.getQuadraticKeySet()) {
            tmpModFunc = tmpBaseFunc.second(this.getAdjustedQuadraticFactor(tmpKey));
            retVal.modifyOne(tmpKey.row, tmpKey.column, tmpModFunc);
            retVal.modifyOne(tmpKey.column, tmpKey.row, tmpModFunc);
        }

        return retVal;
    }

    public double getAdjustedLinearFactor(final int aVar) {
        return this.getAdjustedLinearFactor(new IntIndex(aVar));
    }

    public double getAdjustedLinearFactor(final IntIndex key) {
        return this.getLinearFactor(key, true).doubleValue();
    }

    public double getAdjustedLinearFactor(final Variable aVar) {
        return this.getAdjustedLinearFactor(aVar.getIndex());
    }

    public double getAdjustedQuadraticFactor(final int aVar1, final int aVar2) {
        return this.getAdjustedQuadraticFactor(new IntRowColumn(aVar1, aVar2));
    }

    public double getAdjustedQuadraticFactor(final IntRowColumn key) {
        return this.getQuadraticFactor(key, true).doubleValue();
    }

    public double getAdjustedQuadraticFactor(final Variable aVar1, final Variable aVar2) {
        return this.getAdjustedQuadraticFactor(myModel.indexOf(aVar1), myModel.indexOf(aVar2));
    }

    public Set<Entry<IntIndex, BigDecimal>> getLinearEntrySet() {
        return myLinear.entrySet();
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public BigDecimal getLinearFactor(final int index) {
        return this.get(myModel.getVariable(index));
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public BigDecimal getLinearFactor(final IntIndex key) {
        return this.get(key);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public BigDecimal getLinearFactor(final Variable variable) {
        return this.get(variable);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public Set<IntIndex> getLinearFactorKeys() {
        return this.getLinearKeySet();
    }

    public Set<IntIndex> getLinearKeySet() {
        return myLinear.keySet();
    }

    public Set<Entry<IntRowColumn, BigDecimal>> getQuadraticEntrySet() {
        return myQuadratic.entrySet();
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public BigDecimal getQuadraticFactor(final int row, final int column) {
        return this.get(new IntRowColumn(row, column));
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public BigDecimal getQuadraticFactor(final IntRowColumn key) {
        return this.get(key);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public BigDecimal getQuadraticFactor(final Variable variable1, final Variable variable2) {
        return this.get(new IntRowColumn(variable1.getIndex().index, variable2.getIndex().index));
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public Set<IntRowColumn> getQuadraticFactorKeys() {
        return this.getQuadraticKeySet();
    }

    public Set<IntRowColumn> getQuadraticKeySet() {
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

    public Expression set(final int row, final int column, final Number value) {
        return this.set(new IntRowColumn(row, column), value);
    }

    public Expression set(final int index, final Number value) {
        return this.set(myModel.getVariable(index), value);
    }

    public Expression set(final IntIndex key, final Number value) {

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

        return this;
    }

    public Expression set(final IntRowColumn key, final Number value) {

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

        return this;
    }

    public Expression set(final Variable variable, final Number value) {
        return this.set(variable.getIndex(), value);
    }

    public Expression set(final Variable variable1, final Variable variable2, final Number value) {
        return this.set(variable1.getIndex().index, variable2.getIndex().index, value);
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

            this.set(tmpVariable, tmpVariable, BigMath.ONE);

            this.set(tmpVariable, tmpVal.multiply(tmpLinearWeight));
        }
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void setLinearFactor(final int index, final Number value) {
        this.set(index, value);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void setLinearFactor(final IntIndex key, final Number value) {
        this.set(key, value);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void setLinearFactor(final Variable variable, final Number value) {
        this.set(variable, value);
    }

    public void setLinearFactors(final List<Variable> variables, final Access1D<?> factors) {

        final int tmpLimit = variables.size();

        if (factors.count() != tmpLimit) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < tmpLimit; i++) {
            this.set(variables.get(i), factors.get(i));
        }
    }

    /**
     * Will set the linear factors to a simple sum expression - all factors equal 1.0.
     *
     * @param variables The relevant variables
     */
    public void setLinearFactorsSimple(final List<Variable> variables) {
        for (final Variable tmpVariable : variables) {
            this.set(tmpVariable, BigMath.ONE);
        }
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void setQuadraticFactor(final int row, final int column, final Number value) {
        this.set(row, column, value);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void setQuadraticFactor(final IntRowColumn key, final Number value) {
        this.set(key, value);
    }

    /**
     * @deprecated v39
     */
    @Deprecated
    public void setQuadraticFactor(final Variable variable1, final Variable variable2, final Number value) {
        this.set(variable1, variable2, value);
    }

    public void setQuadraticFactors(final List<Variable> variables, final Access2D<?> factors) {

        final int tmpLimit = variables.size();

        if ((factors.countRows() != tmpLimit) || (factors.countColumns() != tmpLimit)) {
            throw new IllegalArgumentException();
        }

        for (int j = 0; j < tmpLimit; j++) {
            final Variable tmpVar2 = variables.get(j);
            for (int i = 0; i < tmpLimit; i++) {
                this.set(variables.get(i), tmpVar2, factors.get(i, j));
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

        for (final IntRowColumn tmpKey : this.getQuadraticKeySet()) {
            tmpAdjustedFactor = this.getAdjustedQuadraticFactor(tmpKey);
            retVal += point.doubleValue(tmpKey.row) * tmpAdjustedFactor * point.doubleValue(tmpKey.column);
        }

        for (final IntIndex tmpKey : this.getLinearKeySet()) {
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

    protected boolean validate(final Access1D<BigDecimal> solution, final NumberContext context, final BasicLogger.Printer appender) {

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
     * Calculates this expression's fixed value - the fixed variables' part of this expression. Will never
     * return null.
     */
    BigDecimal calculateFixedValue(final Collection<IntIndex> fixedVariables) {

        BigDecimal retVal = BigMath.ZERO;

        if (fixedVariables.size() > 0) {

            for (final IntIndex tmpKey : myLinear.keySet()) {
                if (fixedVariables.contains(tmpKey)) {
                    final BigDecimal tmpFactor = this.get(tmpKey);
                    final BigDecimal tmpValue = myModel.getVariable(tmpKey.index).getValue();
                    retVal = retVal.add(tmpFactor.multiply(tmpValue));
                }
            }

            for (final IntRowColumn tmpKey : myQuadratic.keySet()) {
                if (fixedVariables.contains(new IntIndex(tmpKey.row))) {
                    if (fixedVariables.contains(new IntIndex(tmpKey.column))) {
                        final BigDecimal tmpFactor = this.get(tmpKey);
                        final BigDecimal tmpRowValue = myModel.getVariable(tmpKey.row).getValue();
                        final BigDecimal tmpColValue = myModel.getVariable(tmpKey.column).getValue();
                        retVal = retVal.add(tmpFactor.multiply(tmpRowValue).multiply(tmpColValue));
                    }
                }
            }
        }

        return retVal;
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
            for (final Entry<IntRowColumn, BigDecimal> tmpEntry : myQuadratic.entrySet()) {
                retVal.quadratic().set(tmpEntry.getKey().row, tmpEntry.getKey().column, tmpEntry.getValue().doubleValue());
            }
        }

        if (this.isAnyLinearFactorNonZero()) {
            for (final Entry<IntIndex, BigDecimal> tmpEntry : myLinear.entrySet()) {
                retVal.linear().set(tmpEntry.getKey().index, tmpEntry.getValue().doubleValue());
            }
        }

        return retVal;
    }

    HashMap<IntIndex, BigDecimal> getLinear() {
        return myLinear;
    }

    BigDecimal getLinearFactor(final IntIndex key, final boolean adjusted) {
        return this.convert(myLinear.get(key), adjusted);
    }

    LinearFunction<Double> getLinearFunction() {

        final LinearFunction<Double> retVal = LinearFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyLinearFactorNonZero()) {
            for (final Entry<IntIndex, BigDecimal> tmpEntry : myLinear.entrySet()) {
                retVal.linear().set(tmpEntry.getKey().index, tmpEntry.getValue().doubleValue());
            }
        }

        return retVal;
    }

    ExpressionsBasedModel getModel() {
        return myModel;
    }

    HashMap<IntRowColumn, BigDecimal> getQuadratic() {
        return myQuadratic;
    }

    BigDecimal getQuadraticFactor(final IntRowColumn key, final boolean adjusted) {
        return this.convert(myQuadratic.get(key), adjusted);
    }

    QuadraticFunction<Double> getQuadraticFunction() {

        final QuadraticFunction<Double> retVal = QuadraticFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyQuadraticFactorNonZero()) {
            for (final Entry<IntRowColumn, BigDecimal> tmpEntry : myQuadratic.entrySet()) {
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
    boolean isNegative(final Set<IntIndex> fixedVariables) {

        boolean retVal = !this.isAnyQuadraticFactorNonZero();

        if (retVal) {
            for (final Entry<IntIndex, BigDecimal> tmpLinear : this.getLinearEntrySet()) {
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
    boolean isPositive(final Set<IntIndex> fixedVariables) {

        boolean retVal = !this.isAnyQuadraticFactorNonZero();

        if (retVal) {
            for (final Entry<IntIndex, BigDecimal> tmpLinear : this.getLinearEntrySet()) {
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
