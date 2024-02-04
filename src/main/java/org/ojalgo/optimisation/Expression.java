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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.aggregator.BigAggregator;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.multiary.AffineFunction;
import org.ojalgo.function.multiary.ConstantFunction;
import org.ojalgo.function.multiary.MultiaryFunction;
import org.ojalgo.function.multiary.PureQuadraticFunction;
import org.ojalgo.function.multiary.QuadraticFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;
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
 * {@linkplain Expression#lower(Comparable)}, {@linkplain Expression#upper(Comparable)} or
 * {@linkplain Expression#level(Comparable)}. An expression is made part of (contributing to) the objective
 * function by setting a contribution weight. Use {@linkplain Expression#weight(Comparable)}. The contribution
 * weight can be set to anything except zero (0.0). Often you may just want to set it to one (1.0). Other
 * values can be used to balance multiple expressions contributing to the objective function.
 * </p>
 *
 * @author apete
 */
public final class Expression extends ModelEntity<Expression> {

    private BigDecimal myConstant = null;
    private transient boolean myInfeasible = false;
    private transient Boolean myInteger = null;
    private final Map<IntIndex, BigDecimal> myLinear;
    private final ExpressionsBasedModel myModel;
    private final Map<IntRowColumn, BigDecimal> myQuadratic;
    private transient boolean myRedundant = false;
    /**
     * A shallow copy (typically created by presolver or integer solver) shares the Map:s holding the
     * paramaters with other Expressions. They will only differ on the lower/upper limits and on meta data
     * like flags indicating redundancy or infeasibility.
     */
    private final boolean myShallowCopy;

    @SuppressWarnings("unused")
    private Expression(final Expression entityToCopy) {

        this(entityToCopy, null, false);

        ProgrammingError.throwForIllegalInvocation();
    }

    protected Expression(final Expression expressionToCopy, final ExpressionsBasedModel destinationModel, final boolean deep) {

        super(expressionToCopy);

        myModel = destinationModel;

        myConstant = expressionToCopy.getConstant();

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

        if (expressionToCopy.isInteger()) {
            myInteger = Boolean.TRUE;
        } else {
            myInteger = null;
        }
    }

    Expression(final String name, final ExpressionsBasedModel model) {

        super(name);

        ProgrammingError.throwIfNull(name, model);

        myModel = model;

        myShallowCopy = false;

        myLinear = new HashMap<>();
        myQuadratic = new HashMap<>();
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final int index, final Comparable<?> value) {
        return this.add(myModel.getVariable(index), value);
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final int index, final double value) {
        return this.add(index, BigDecimal.valueOf(value));
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final int row, final int column, final Comparable<?> value) {
        return this.add(new IntRowColumn(row, column), value);
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final int row, final int column, final double value) {
        return this.add(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final int row, final int column, final long value) {
        return this.add(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final int index, final long value) {
        return this.add(index, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression add(final IntIndex key, final Comparable<?> value) {

        BigDecimal existing = myLinear.get(key);

        if (existing != null) {
            this.set(key, ModelEntity.toBigDecimal(value).add(existing));
        } else {
            this.set(key, value);
        }

        return this;
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression add(final IntIndex key, final double value) {
        return this.add(key, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression add(final IntIndex row, final IntIndex column, final Comparable<?> value) {
        return this.add(new IntRowColumn(row, column), value);
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression add(final IntIndex row, final IntIndex column, final double value) {
        return this.add(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression add(final IntIndex row, final IntIndex column, final long value) {
        return this.add(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression add(final IntIndex key, final long value) {
        return this.add(key, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with {@link Variable}:s or int:s as the key instead.
     */
    @Deprecated
    public Expression add(final IntRowColumn key, final Comparable<?> value) {

        BigDecimal existing = myQuadratic.get(key);

        if (existing != null) {
            this.set(key, ModelEntity.toBigDecimal(value).add(existing));
        } else {
            this.set(key, value);
        }

        return this;
    }

    /**
     * @deprecated Use the alternatives with {@link Variable}:s or int:s as the key instead.
     */
    @Deprecated
    public Expression add(final IntRowColumn key, final double value) {
        return this.add(key, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with {@link Variable}:s or int:s as the key instead.
     */
    @Deprecated
    public Expression add(final IntRowColumn key, final long value) {
        return this.add(key, BigDecimal.valueOf(value));
    }

    /**
     * Will add the value to this variable's factor.
     */
    public Expression add(final Variable variable, final Comparable<?> value) {
        return this.add(variable.getIndex(), value);
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final Variable variable, final double value) {
        return this.add(variable, BigDecimal.valueOf(value));
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final Variable variable, final long value) {
        return this.add(variable, BigDecimal.valueOf(value));
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final Variable variable1, final Variable variable2, final Comparable<?> value) {
        return this.add(variable1.getIndex().index, variable2.getIndex().index, value);
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final Variable variable1, final Variable variable2, final double value) {
        return this.add(variable1, variable2, BigDecimal.valueOf(value));
    }

    /**
     * @see #add(Variable, Comparable)
     */
    public Expression add(final Variable variable1, final Variable variable2, final long value) {
        return this.add(variable1, variable2, BigDecimal.valueOf(value));
    }

    @Override
    public void addTo(final Expression target, final BigDecimal scale) {

        for (Entry<IntIndex, BigDecimal> entry : myLinear.entrySet()) {
            BigDecimal value = entry.getValue().multiply(scale);
            target.add(entry.getKey(), value);
        }

        for (Entry<IntRowColumn, BigDecimal> entry : myQuadratic.entrySet()) {
            BigDecimal value = entry.getValue().multiply(scale);
            target.add(entry.getKey(), value);
        }
    }

    public int compareTo(final Expression obj) {
        return this.getName().compareTo(obj.getName());
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

        if (fixedVariables.size() == 0 || !this.isAnyQuadraticFactorNonZero() && Collections.disjoint(fixedVariables, this.getLinearKeySet())) {

            return this; // No need to copy/compensate anything

        }
        ExpressionsBasedModel tmpModel = this.getModel();

        Expression retVal = new Expression(this.getName(), tmpModel);

        BigDecimal tmpFixedValue = BigMath.ZERO;

        for (Entry<IntIndex, BigDecimal> tmpEntry : myLinear.entrySet()) {

            IntIndex tmpKey = tmpEntry.getKey();
            BigDecimal tmpFactor = tmpEntry.getValue();

            if (fixedVariables.contains(tmpKey)) {
                // Fixed

                Variable variable = tmpModel.getVariable(tmpKey.index);
                BigDecimal tmpValue = variable.getValue();

                tmpFixedValue = tmpFixedValue.add(tmpFactor.multiply(tmpValue));

            } else {
                // Not fixed

                retVal.set(tmpKey, tmpFactor);
            }
        }

        for (Entry<IntRowColumn, BigDecimal> tmpEntry : myQuadratic.entrySet()) {

            IntRowColumn tmpKey = tmpEntry.getKey();
            BigDecimal tmpFactor = tmpEntry.getValue();

            Variable tmpRowVariable = tmpModel.getVariable(tmpKey.row);
            Variable tmpColVariable = tmpModel.getVariable(tmpKey.column);

            IntIndex tmpRowKey = tmpRowVariable.getIndex();
            IntIndex tmpColKey = tmpColVariable.getIndex();

            if (fixedVariables.contains(tmpRowKey)) {

                BigDecimal tmpRowValue = tmpRowVariable.getValue();

                if (fixedVariables.contains(tmpColKey)) {
                    // Both fixed

                    BigDecimal tmpColValue = tmpColVariable.getValue();

                    tmpFixedValue = tmpFixedValue.add(tmpFactor.multiply(tmpRowValue).multiply(tmpColValue));

                } else {
                    // Row fixed

                    retVal.add(tmpColKey, tmpFactor.multiply(tmpRowValue));
                }

            } else if (fixedVariables.contains(tmpColKey)) {
                // Column fixed

                BigDecimal tmpColValue = tmpColVariable.getValue();

                retVal.add(tmpRowKey, tmpFactor.multiply(tmpColValue));

            } else {
                // Neither fixed

                retVal.set(tmpKey, tmpFactor);
            }
        }

        if (this.isLowerLimitSet()) {
            retVal.lower(this.getLowerLimit().subtract(tmpFixedValue));
        }

        if (this.isUpperLimitSet()) {
            retVal.upper(this.getUpperLimit().subtract(tmpFixedValue));
        }

        if (this.isInteger()) {
            retVal.setInteger();
        }

        return retVal;

    }

    public double doubleValue(final IntIndex key, final boolean adjusted) {
        return this.get(key, adjusted).doubleValue();
    }

    public double doubleValue(final IntRowColumn key, final boolean adjusted) {
        return this.get(key, adjusted).doubleValue();
    }

    public void enforce(final NumberContext enforcer) {

        myLinear.replaceAll((key, value) -> enforcer.enforce(value));

        myQuadratic.replaceAll((key, value) -> enforcer.enforce(value));

        if (this.isLowerLimitSet()) {
            this.lower(enforcer.withMode(RoundingMode.FLOOR).enforce(this.getLowerLimit()));
        }

        if (this.isUpperLimitSet()) {
            this.upper(enforcer.withMode(RoundingMode.CEILING).enforce(this.getUpperLimit()));
        }
    }

    public BigDecimal evaluate(final Access1D<BigDecimal> point) {

        BigDecimal retVal = this.getConstant();

        BigDecimal factor;

        for (IntRowColumn quadKey : this.getQuadraticKeySet()) {
            factor = this.get(quadKey);
            retVal = retVal.add(factor.multiply(point.get(quadKey.row)).multiply(point.get(quadKey.column)));
        }

        for (IntIndex linKey : this.getLinearKeySet()) {
            factor = this.get(linKey);
            retVal = retVal.add(factor.multiply(point.get(linKey.index)));
        }

        return retVal;
    }

    public BigDecimal get(final IntIndex key) {
        return this.get(key, false);
    }

    public BigDecimal get(final IntIndex key, final boolean adjusted) {
        return this.convert(myLinear.get(key), adjusted);
    }

    public BigDecimal get(final IntRowColumn key) {
        return this.get(key, false);
    }

    public BigDecimal get(final IntRowColumn key, final boolean adjusted) {
        return this.convert(myQuadratic.get(key), adjusted);
    }

    public BigDecimal get(final Variable variable) {
        IntIndex index = variable.getIndex();
        if (index != null) {
            return this.get(index);
        } else {
            throw new IllegalStateException("Variable not part of (this) model!");
        }
    }

    public MatrixStore<Double> getAdjustedGradient(final Access1D<?> point) {

        Primitive64Store retVal = Primitive64Store.FACTORY.make(myModel.countVariables(), 1);

        BinaryFunction<Double> tmpBaseFunc = PrimitiveMath.ADD;
        double tmpAdjustedFactor;
        UnaryFunction<Double> tmpModFunc;
        for (IntRowColumn tmpKey : this.getQuadraticKeySet()) {
            tmpAdjustedFactor = this.getAdjustedQuadraticFactor(tmpKey);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor * point.doubleValue(tmpKey.column));
            retVal.modifyOne(tmpKey.row, 0, tmpModFunc);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor * point.doubleValue(tmpKey.row));
            retVal.modifyOne(tmpKey.column, 0, tmpModFunc);
        }

        for (IntIndex tmpKey : this.getLinearKeySet()) {
            tmpAdjustedFactor = this.getAdjustedLinearFactor(tmpKey);
            tmpModFunc = tmpBaseFunc.second(tmpAdjustedFactor);
            retVal.modifyOne(tmpKey.index, 0, tmpModFunc);
        }

        return retVal;
    }

    public MatrixStore<Double> getAdjustedHessian() {

        int tmpCountVariables = myModel.countVariables();
        Primitive64Store retVal = Primitive64Store.FACTORY.make(tmpCountVariables, tmpCountVariables);

        BinaryFunction<Double> tmpBaseFunc = PrimitiveMath.ADD;
        UnaryFunction<Double> tmpModFunc;
        for (IntRowColumn tmpKey : this.getQuadraticKeySet()) {
            tmpModFunc = tmpBaseFunc.second(this.getAdjustedQuadraticFactor(tmpKey));
            retVal.modifyOne(tmpKey.row, tmpKey.column, tmpModFunc);
            retVal.modifyOne(tmpKey.column, tmpKey.row, tmpModFunc);
        }

        return retVal;
    }

    /**
     * @deprecated v52 Use {@link #doubleValue(IntIndex, boolean)} instead.
     */
    @Deprecated
    public double getAdjustedLinearFactor(final int aVar) {
        return this.getAdjustedLinearFactor(new IntIndex(aVar));
    }

    /**
     * @deprecated v52 Use {@link #doubleValue(IntIndex, boolean)} instead.
     */
    @Deprecated
    public double getAdjustedLinearFactor(final IntIndex key) {
        return this.get(key, true).doubleValue();
    }

    /**
     * @deprecated v52 Use {@link #doubleValue(IntIndex, boolean)} instead.
     */
    @Deprecated
    public double getAdjustedLinearFactor(final Variable aVar) {
        return this.getAdjustedLinearFactor(aVar.getIndex());
    }

    /**
     * @deprecated v52 Use {@link #doubleValue(IntRowColumn, boolean)} instead.
     */
    @Deprecated
    public double getAdjustedQuadraticFactor(final int aVar1, final int aVar2) {
        return this.getAdjustedQuadraticFactor(new IntRowColumn(aVar1, aVar2));
    }

    /**
     * @deprecated v52 Use {@link #doubleValue(IntRowColumn, boolean)} instead.
     */
    @Deprecated
    public double getAdjustedQuadraticFactor(final IntRowColumn key) {
        return this.get(key, true).doubleValue();
    }

    /**
     * @deprecated v52 Use {@link #doubleValue(IntRowColumn, boolean)} instead.
     */
    @Deprecated
    public double getAdjustedQuadraticFactor(final Variable aVar1, final Variable aVar2) {
        return this.getAdjustedQuadraticFactor(myModel.indexOf(aVar1), myModel.indexOf(aVar2));
    }

    public Set<Entry<IntIndex, BigDecimal>> getLinearEntrySet() {
        return myLinear.entrySet();
    }

    public Set<IntIndex> getLinearKeySet() {
        return myLinear.keySet();
    }

    public Set<Entry<IntRowColumn, BigDecimal>> getQuadraticEntrySet() {
        return myQuadratic.entrySet();
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

    public boolean isFunctionConstant() {
        return !this.isAnyQuadraticFactorNonZero() && !this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionLinear() {
        return !this.isAnyQuadraticFactorNonZero() && this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionPureQuadratic() {
        return this.isAnyQuadraticFactorNonZero() && !this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionQuadratic() {
        return this.isAnyQuadraticFactorNonZero() && this.isAnyLinearFactorNonZero();
    }

    @Override
    public boolean isInteger() {
        if (myInteger == null) {
            this.doIntegerRounding();
        }
        return myInteger.booleanValue();
    }

    /**
     * @return Are all the (linear) variables binary
     */
    public boolean isLinearAndAllBinary() {
        return myQuadratic.size() == 0 && myLinear.size() > 0 && myLinear.keySet().stream().allMatch(i -> myModel.getVariable(i).isBinary());
    }

    /**
     * @return Are all the (linear) variables integer
     */
    public boolean isLinearAndAllInteger() {
        return myQuadratic.size() == 0 && myLinear.size() > 0 && myLinear.keySet().stream().allMatch(i -> myModel.getVariable(i).isInteger());
    }

    /**
     * @return Are any of the (linear) variables binary
     */
    public boolean isLinearAndAnyBinary() {
        return myQuadratic.size() == 0 && myLinear.size() > 0 && myLinear.keySet().stream().anyMatch(i -> myModel.getVariable(i).isBinary());
    }

    /**
     * @return Are any of the (linear) variables integer
     */
    public boolean isLinearAndAnyInteger() {
        return myQuadratic.size() == 0 && myLinear.size() > 0 && myLinear.keySet().stream().anyMatch(i -> myModel.getVariable(i).isInteger());
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final int index, final Comparable<?> value) {
        return this.set(myModel.getVariable(index), value);
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final int index, final double value) {
        return this.set(index, BigDecimal.valueOf(value));
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final int row, final int column, final Comparable<?> value) {
        return this.set(new IntRowColumn(row, column), value);
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final int row, final int column, final double value) {
        return this.set(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final int row, final int column, final long value) {
        return this.set(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final int index, final long value) {
        return this.set(index, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression set(final IntIndex key, final Comparable<?> value) {

        if (key == null) {
            throw new IllegalArgumentException();
        }

        BigDecimal tmpValue = ModelEntity.toBigDecimal(value);

        if (tmpValue.signum() != 0) {
            myLinear.put(key, tmpValue);
            myModel.addReference(key);
        } else {
            myLinear.remove(key);
        }

        return this;
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression set(final IntIndex key, final double value) {
        return this.set(key, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression set(final IntIndex row, final IntIndex column, final Comparable<?> value) {
        return this.set(new IntRowColumn(row, column), value);
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression set(final IntIndex row, final IntIndex column, final double value) {
        return this.set(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression set(final IntIndex row, final IntIndex column, final long value) {
        return this.set(row, column, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with a {@link Variable} or an int as the key/index instead.
     */
    @Deprecated
    public Expression set(final IntIndex key, final long value) {
        return this.set(key, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with {@link Variable}:s or int:s as the key instead.
     */
    @Deprecated
    public Expression set(final IntRowColumn key, final Comparable<?> value) {

        if (key == null) {

            throw new IllegalArgumentException();
        }
        BigDecimal tmpValue = ModelEntity.toBigDecimal(value);

        if (tmpValue.signum() != 0) {
            myQuadratic.put(key, tmpValue);
            myModel.addReference(key.row());
            myModel.addReference(key.column());
        } else {
            myQuadratic.remove(key);
        }

        return this;
    }

    /**
     * @deprecated Use the alternatives with {@link Variable}:s or int:s as the key instead.
     */
    @Deprecated
    public Expression set(final IntRowColumn key, final double value) {
        return this.set(key, BigDecimal.valueOf(value));
    }

    /**
     * @deprecated Use the alternatives with {@link Variable}:s or int:s as the key instead.
     */
    @Deprecated
    public Expression set(final IntRowColumn key, final long value) {
        return this.set(key, BigDecimal.valueOf(value));
    }

    /**
     * Will set (replace) the variable's factor to this value
     */
    public Expression set(final Variable variable, final Comparable<?> value) {
        return this.set(variable.getIndex(), value);
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final Variable variable, final double value) {
        return this.set(variable, BigDecimal.valueOf(value));
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final Variable variable, final long value) {
        return this.set(variable, BigDecimal.valueOf(value));
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final Variable variable1, final Variable variable2, final Comparable<?> value) {
        return this.set(variable1.getIndex().index, variable2.getIndex().index, value);
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final Variable variable1, final Variable variable2, final double value) {
        return this.set(variable1, variable2, BigDecimal.valueOf(value));
    }

    /**
     * @see #set(Variable, Comparable)
     */
    public Expression set(final Variable variable1, final Variable variable2, final long value) {
        return this.set(variable1, variable2, BigDecimal.valueOf(value));
    }

    /**
     * Will set the quadratic and linear factors to an expression that measures (the square of) the distance
     * from the given point.
     *
     * @param variables The relevant variables
     * @param point The point to measure from
     */
    public void setCompoundFactorsOffset(final List<Variable> variables, final Access1D<?> point) {

        int tmpLength = variables.size();

        if (point.count() != tmpLength) {
            throw new IllegalArgumentException();
        }

        BigDecimal tmpLinearWeight = BigMath.TWO.negate();

        Variable tmpVariable;
        BigDecimal tmpVal;
        for (int ij = 0; ij < tmpLength; ij++) {

            tmpVariable = variables.get(ij);
            tmpVal = ModelEntity.toBigDecimal(point.get(ij));

            this.set(tmpVariable, tmpVariable, BigMath.ONE);

            this.set(tmpVariable, tmpVal.multiply(tmpLinearWeight));
        }
    }

    public void setLinearFactors(final List<Variable> variables, final Access1D<?> factors) {

        int tmpLimit = variables.size();

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
        for (Variable tmpVariable : variables) {
            this.set(tmpVariable, BigMath.ONE);
        }
    }

    public void setQuadraticFactors(final List<Variable> variables, final Access2D<?> factors) {

        int tmpLimit = variables.size();

        if (factors.countRows() != tmpLimit || factors.countColumns() != tmpLimit) {
            throw new IllegalArgumentException();
        }

        for (int j = 0; j < tmpLimit; j++) {
            Variable tmpVar2 = variables.get(j);
            for (int i = 0; i < tmpLimit; i++) {
                this.set(variables.get(i), tmpVar2, factors.get(i, j));
            }
        }
    }

    /**
     * Will attempt to exploit integer property to tighten the lower and/or upper limits (integer rounding).
     */
    public void tighten() {
        if (this.isConstraint()) {
            this.isInteger();
        }
    }

    public MultiaryFunction.TwiceDifferentiable<Double> toFunction() {

        if (this.isFunctionQuadratic()) {
            return this.makeQuadraticFunction();
        }

        if (this.isFunctionPureQuadratic()) {
            return this.makePureQuadraticFunction();
        }

        if (this.isFunctionLinear()) {
            return this.makeAffineFunction();
        }

        return this.makeConstantFunction();
    }

    private BigDecimal convert(final BigDecimal value, final boolean adjusted) {

        if (value == null) {
            return BigMath.ZERO;
        }

        if (!adjusted) {
            return value;
        }

        int tmpAdjExp = this.getAdjustmentExponent();
        if (tmpAdjExp != 0) {
            return value.movePointRight(tmpAdjExp);
        }

        return value;
    }

    private BigDecimal getConstant() {
        return myConstant != null ? myConstant : BigMath.ZERO;
    }

    private AffineFunction<Double> makeAffineFunction() {

        AffineFunction<Double> retVal = AffineFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyLinearFactorNonZero()) {
            for (Entry<IntIndex, BigDecimal> entry : myLinear.entrySet()) {
                retVal.linear().set(entry.getKey().index, entry.getValue().doubleValue());
            }
        }

        retVal.setConstant(this.getConstant());

        return retVal;
    }

    private ConstantFunction<Double> makeConstantFunction() {
        return ConstantFunction.makePrimitive(myModel.countVariables(), this.getConstant());
    }

    private PureQuadraticFunction<Double> makePureQuadraticFunction() {

        PureQuadraticFunction<Double> retVal = PureQuadraticFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyQuadraticFactorNonZero()) {
            for (Entry<IntRowColumn, BigDecimal> entry : myQuadratic.entrySet()) {
                retVal.quadratic().set(entry.getKey().row, entry.getKey().column, entry.getValue().doubleValue());
            }
        }

        retVal.setConstant(this.getConstant());

        return retVal;
    }

    private QuadraticFunction<Double> makeQuadraticFunction() {

        QuadraticFunction<Double> retVal = QuadraticFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyQuadraticFactorNonZero()) {
            for (Entry<IntRowColumn, BigDecimal> entry : myQuadratic.entrySet()) {
                retVal.quadratic().set(entry.getKey().row, entry.getKey().column, entry.getValue().doubleValue());
            }
        }

        if (this.isAnyLinearFactorNonZero()) {
            for (Entry<IntIndex, BigDecimal> entry : myLinear.entrySet()) {
                retVal.linear().set(entry.getKey().index, entry.getValue().doubleValue());
            }
        }

        retVal.setConstant(this.getConstant());

        return retVal;
    }

    private BigDecimal toPositiveFraction(final BigDecimal noninteger) {
        BigDecimal intPart = noninteger.setScale(0, RoundingMode.FLOOR);
        return noninteger.subtract(intPart);
    }

    protected void appendMiddlePart(final StringBuilder builder, final Access1D<BigDecimal> solution, final NumberContext display) {

        builder.append(this.getName());
        builder.append(": ");
        builder.append(display.enforce(this.toFunction().invoke(Access1D.asPrimitive1D(solution))));

        if (this.isObjective()) {
            builder.append(" (");
            builder.append(display.enforce(this.getContributionWeight()));
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

    void addObjectiveConstant(final BigDecimal value) {

        BigDecimal weight = this.getContributionWeight();

        if (weight != null && weight.signum() != 0) {
            myModel.addObjectiveConstant(value.multiply(weight));
        } else {
            myModel.addObjectiveConstant(value);
        }
    }

    void appendToString(final StringBuilder builder, final Access1D<BigDecimal> solution, final NumberContext display) {

        this.appendLeftPart(builder, display);
        if (solution != null) {
            this.appendMiddlePart(builder, solution, display);
        } else {
            this.appendMiddlePart(builder, display);
        }
        this.appendRightPart(builder, display);
    }

    /**
     * Calculates this expression's value - the subset variables' part of this expression. Will never return
     * null.
     */
    BigDecimal calculateSetValue(final Collection<IntIndex> subset) {

        BigDecimal retVal = BigMath.ZERO;

        if (subset.size() > 0) {

            for (IntIndex linKey : myLinear.keySet()) {
                if (subset.contains(linKey)) {
                    BigDecimal coefficient = this.get(linKey);
                    BigDecimal value = myModel.getVariable(linKey.index).getValue();
                    retVal = retVal.add(coefficient.multiply(value));
                }
            }

            for (IntRowColumn quadKey : myQuadratic.keySet()) {
                if (subset.contains(quadKey.row()) && subset.contains(quadKey.column())) {
                    BigDecimal coefficient = this.get(quadKey);
                    BigDecimal rowValue = myModel.getVariable(quadKey.row).getValue();
                    BigDecimal colValue = myModel.getVariable(quadKey.column).getValue();
                    retVal = retVal.add(coefficient.multiply(rowValue).multiply(colValue));
                }
            }
        }

        return retVal;
    }

    Expression copy(final ExpressionsBasedModel destinationModel, final boolean deep) {
        return new Expression(this, destinationModel, deep);
    }

    long countIntegerFactors() {
        return myLinear.keySet().stream().map(this::resolve).filter(Variable::isInteger).count();
    }

    int countLinearFactors() {
        return myLinear.size();
    }

    int countQuadraticFactors() {
        return myQuadratic.size();
    }

    @Override
    int deriveAdjustmentExponent() {

        if (this.isInteger()) {
            return 0;
        }

        AggregatorSet<BigDecimal> aggregators = BigAggregator.getSet();
        AggregatorFunction<BigDecimal> largest = aggregators.largest();
        AggregatorFunction<BigDecimal> smallest = aggregators.smallest();

        if (this.isAnyQuadraticFactorNonZero()) {

            for (BigDecimal quadraticFactor : myQuadratic.values()) {
                largest.invoke(quadraticFactor);
                smallest.invoke(quadraticFactor);
            }

            return ModelEntity.deriveAdjustmentExponent(largest, smallest, RANGE);

        } else if (this.isAnyLinearFactorNonZero()) {

            for (BigDecimal linearFactor : myLinear.values()) {
                largest.invoke(linearFactor);
                smallest.invoke(linearFactor);
            }

            return ModelEntity.deriveAdjustmentExponent(largest, smallest, RANGE);

        } else {

            return 0;
        }
    }

    /**
     * Assumes at least 1 variable, and all variables integer!
     *
     * @see org.ojalgo.optimisation.ModelEntity#doIntegerRounding()
     */
    @Override
    void doIntegerRounding() {
        this.doIntegerRounding(this.getLinearKeySet(), this.getLowerLimit(), this.getUpperLimit());
    }

    void doIntegerRounding(final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper) {

        if (myInteger != null) {
            return;
        }

        if (remaining.size() == 0 || !myModel.isInteger(remaining) || myQuadratic.size() > 0) {
            myInteger = Boolean.FALSE;
            return;
        }

        BigInteger gcd = null;
        int maxScale = Integer.MIN_VALUE;
        for (IntIndex index : remaining) {
            BigDecimal coeff = myLinear.get(index);
            BigDecimal abs = coeff.stripTrailingZeros().abs();
            maxScale = Math.max(maxScale, abs.scale());
            if (gcd != null) {
                gcd = gcd.gcd(abs.unscaledValue());
            } else {
                gcd = abs.unscaledValue();
            }
            if (maxScale > 8 || (gcd.equals(BigInteger.ONE) && maxScale > 0)) {
                myInteger = Boolean.FALSE;
                return;
            }
        }

        BigDecimal divisor = new BigDecimal(gcd, maxScale);

        boolean full = myLinear.size() == remaining.size();

        BigDecimal newLower = null, newUpper = null;

        if (lower != null) {
            BigDecimal tmpVal = lower.divide(divisor, 0, RoundingMode.CEILING);
            newLower = tmpVal.multiply(divisor);
            if (full) {
                this.lower(newLower);
            }
        }

        if (upper != null) {
            BigDecimal tmpVal = upper.divide(divisor, 0, RoundingMode.FLOOR);
            newUpper = tmpVal.multiply(divisor);
            if (full) {
                this.upper(newUpper);
            }
        }

        if (ModelEntity.isInfeasible(newLower, newUpper)) {
            this.setInfeasible();
        }

        myInteger = Boolean.TRUE;
    }

    Expression doMixedIntegerRounding() {

        if (!this.isEqualityConstraint()) {
            return null;
        }

        BigDecimal posFracLevel = this.toPositiveFraction(this.getLowerLimit());
        if (posFracLevel.signum() <= 0) {
            return null;
        }
        BigDecimal cmpFracLevel = BigMath.ONE.subtract(posFracLevel);

        Expression retVal = myModel.newExpression(this.getName() + "(MIR)");

        for (Entry<IntIndex, BigDecimal> entry : myLinear.entrySet()) {
            Variable variable = this.resolve(entry.getKey());

            if (!variable.isLowerLimitSet() || variable.getLowerLimit().compareTo(BigMath.ZERO) < 0) {
                return null;
            }

            BigDecimal coeff = entry.getValue();

            if (variable.isInteger()) {

                BigDecimal posFracCoeff = this.toPositiveFraction(coeff);

                if (posFracCoeff.compareTo(posFracLevel) <= 0) {
                    retVal.set(variable, BigMath.DIVIDE.invoke(posFracCoeff, posFracLevel));
                } else {
                    BigDecimal cmpFracCoeff = BigMath.ONE.subtract(posFracCoeff);
                    retVal.set(variable, BigMath.DIVIDE.invoke(cmpFracCoeff, cmpFracLevel));
                }

            } else if (coeff.signum() == 1) {
                retVal.set(variable, BigMath.DIVIDE.invoke(coeff, posFracLevel));
            } else if (coeff.signum() == -1) {
                BigDecimal negCoeff = coeff.negate();
                retVal.set(variable, BigMath.DIVIDE.invoke(negCoeff, cmpFracLevel));
            }
        }

        return retVal.lower(BigMath.ONE);
    }

    Set<Variable> getBinaryVariables(final Set<IntIndex> subset) {

        HashSet<Variable> retVal = new HashSet<>();

        for (IntIndex varInd : myLinear.keySet()) {
            if (subset.contains(varInd)) {
                Variable variable = myModel.getVariable(varInd.index);
                if (variable.isBinary()) {
                    retVal.add(variable);
                }
            }
        }

        return retVal;
    }

    Map<IntIndex, BigDecimal> getLinear() {
        return myLinear;
    }

    ExpressionsBasedModel getModel() {
        return myModel;
    }

    Map<IntRowColumn, BigDecimal> getQuadratic() {
        return myQuadratic;
    }

    boolean includes(final Variable variable) {
        IntIndex tmpVarInd = variable.getIndex();
        return myLinear.containsKey(tmpVarInd)
                || myQuadratic.size() > 0 && myQuadratic.keySet().stream().anyMatch(k -> (k.row == tmpVarInd.index || k.column == tmpVarInd.index));
    }

    boolean isConstantSet() {
        return myConstant != null && myConstant.signum() != 0;
    }

    @Override
    boolean isInfeasible() {
        return myInfeasible || super.isInfeasible();
    }

    /**
     * @param subset The indices of a variable subset
     * @return true if none of the variables in the subset can make a positve contribution to the expression
     *         value
     */
    boolean isNegativeOn(final Set<IntIndex> subset) {

        if (!this.isAnyQuadraticFactorNonZero()) {
            for (IntIndex index : subset) {
                Variable setVar = myModel.getVariable(index);
                int signum = myLinear.get(index).signum();
                if (signum < 0 && setVar.isLowerLimitSet() && setVar.getLowerLimit().signum() >= 0) {

                } else if (signum > 0 && setVar.isUpperLimitSet() && setVar.getUpperLimit().signum() <= 0) {

                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @param subset The indices of a variable subset
     * @return true if none of the variables in the subset can make a negative contribution to the expression
     *         value
     */
    boolean isPositiveOn(final Set<IntIndex> subset) {

        if (!this.isAnyQuadraticFactorNonZero()) {
            for (IntIndex index : subset) {
                Variable setVar = myModel.getVariable(index);
                int signum = myLinear.get(index).signum();
                if (signum > 0 && setVar.isLowerLimitSet() && setVar.getLowerLimit().signum() >= 0) {

                } else if (signum < 0 && setVar.isUpperLimitSet() && setVar.getUpperLimit().signum() <= 0) {

                } else {
                    return false;
                }
            }
        }

        return true;
    }

    boolean isRedundant() {
        return myRedundant;
    }

    Variable resolve(final Structure1D.IntIndex index) {
        return myModel.getVariable(index);
    }

    void setConstant(final Comparable<?> value) {
        myConstant = ModelEntity.toBigDecimal(value);
    }

    void setConstant(final double value) {
        myConstant = BigDecimal.valueOf(value);
    }

    void setConstant(final long value) {
        myConstant = BigDecimal.valueOf(value);
    }

    void setInfeasible() {
        myInfeasible = true;
        myModel.setInfeasible();
    }

    void setInteger() {
        myInteger = Boolean.TRUE;
    }

    void setRedundant() {
        myRedundant = true;
    }

}
