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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.multiary.ConstantFunction;
import org.ojalgo.function.multiary.LinearFunction;
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
import org.ojalgo.type.TypeUtils;

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
 * {@linkplain Expression#lower(Number)}, {@linkplain Expression#upper(Number)} or
 * {@linkplain Expression#level(Number)}. An expression is made part of (contributing to) the objective
 * function by setting a contribution weight. Use {@linkplain Expression#weight(Number)}. The contribution
 * weight can be set to anything except zero (0.0). Often you may just want to set it to one (1.0). Other
 * values can be used to balance multiple expressions contributing to the objective function.
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

        ProgrammingError.throwIfNull(myModel, myLinear, myQuadratic);
    }

    public Expression add(final IntIndex key, final Comparable<?> value) {

        final BigDecimal tmpExisting = myLinear.get(key);

        if (tmpExisting != null) {
            this.set(key, TypeUtils.toBigDecimal(value).add(tmpExisting));
        } else {
            this.set(key, value);
        }

        return this;
    }

    public Expression add(final IntRowColumn key, final Comparable<?> value) {

        final BigDecimal tmpExisting = myQuadratic.get(key);

        if (tmpExisting != null) {
            this.set(key, TypeUtils.toBigDecimal(value).add(tmpExisting));
        } else {
            this.set(key, value);
        }

        return this;
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

        final Primitive64Store retVal = Primitive64Store.FACTORY.makeZero(myModel.countVariables(), 1);

        final BinaryFunction<Double> tmpBaseFunc = PrimitiveMath.ADD;
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
        final Primitive64Store retVal = Primitive64Store.FACTORY.makeZero(tmpCountVariables, tmpCountVariables);

        final BinaryFunction<Double> tmpBaseFunc = PrimitiveMath.ADD;
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

    public boolean isFunctionLinear() {
        return !this.isAnyQuadraticFactorNonZero() && this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionPureQuadratic() {
        return this.isAnyQuadraticFactorNonZero() && !this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionQuadratic() {
        return this.isAnyQuadraticFactorNonZero() && this.isAnyLinearFactorNonZero();
    }

    public boolean isFunctionConstant() {
        return !this.isAnyQuadraticFactorNonZero() && !this.isAnyLinearFactorNonZero();
    }

    /**
     * @return Are all the (linear) variables binary
     */
    public boolean isLinearAndAllBinary() {
        return (myQuadratic.size() == 0) && (myLinear.size() > 0) && myLinear.keySet().stream().allMatch(i -> myModel.getVariable(i).isBinary());
    }

    /**
     * @return Are all the (linear) variables integer
     */
    public boolean isLinearAndAllInteger() {
        return (myQuadratic.size() == 0) && (myLinear.size() > 0) && myLinear.keySet().stream().allMatch(i -> myModel.getVariable(i).isInteger());
    }

    /**
     * @return Are any of the (linear) variables binary
     */
    public boolean isLinearAndAnyBinary() {
        return (myQuadratic.size() == 0) && (myLinear.size() > 0) && myLinear.keySet().stream().anyMatch(i -> myModel.getVariable(i).isBinary());
    }

    /**
     * @return Are any of the (linear) variables integer
     */
    public boolean isLinearAndAnyInteger() {
        return (myQuadratic.size() == 0) && (myLinear.size() > 0) && myLinear.keySet().stream().anyMatch(i -> myModel.getVariable(i).isInteger());
    }

    public Expression set(final int row, final int column, final Comparable<?> value) {
        return this.set(new IntRowColumn(row, column), value);
    }

    public Expression set(final int index, final Comparable<?> value) {
        return this.set(myModel.getVariable(index), value);
    }

    public Expression set(final IntIndex row, final IntIndex column, final Comparable<?> value) {
        return this.set(new IntRowColumn(row, column), value);
    }

    public Expression set(final IntIndex key, final Comparable<?> value) {

        if (key != null) {

            final BigDecimal tmpValue = TypeUtils.toBigDecimal(value);

            if (tmpValue.signum() != 0) {
                myLinear.put(key, tmpValue);
                myModel.addReference(key);
            } else {
                myLinear.remove(key);
            }

        } else {

            throw new IllegalArgumentException();
        }

        return this;
    }

    public Expression set(final IntRowColumn key, final Comparable<?> value) {

        if (key != null) {

            final BigDecimal tmpValue = TypeUtils.toBigDecimal(value);

            if (tmpValue.signum() != 0) {
                myQuadratic.put(key, tmpValue);
                myModel.addReference(key.getRow());
                myModel.addReference(key.getColumn());
            } else {
                myQuadratic.remove(key);
            }

        } else {

            throw new IllegalArgumentException();
        }

        return this;
    }

    public Expression set(final Variable variable, final Comparable<?> value) {
        return this.set(variable.getIndex(), value);
    }

    public Expression set(final Variable variable1, final Variable variable2, final Comparable<?> value) {
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

        if (this.isFunctionQuadratic()) {
            return this.makeQuadraticFunction();
        } else if (this.isFunctionPureQuadratic()) {
            return this.makePureQuadraticFunction();
        } else if (this.isFunctionLinear()) {
            return this.makeLinearFunction();
        } else {
            return this.makeConstantFunction();
        }
    }

    private BigDecimal convert(final BigDecimal value, final boolean adjusted) {

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

    private ConstantFunction<Double> makeConstantFunction() {
        return ConstantFunction.makePrimitive(myModel.countVariables());
    }

    private LinearFunction<Double> makeLinearFunction() {

        final LinearFunction<Double> retVal = LinearFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyLinearFactorNonZero()) {
            for (Entry<IntIndex, BigDecimal> entry : myLinear.entrySet()) {
                retVal.linear().set(entry.getKey().index, entry.getValue().doubleValue());
            }
        }

        return retVal;
    }

    private PureQuadraticFunction<Double> makePureQuadraticFunction() {

        final PureQuadraticFunction<Double> retVal = PureQuadraticFunction.makePrimitive(myModel.countVariables());

        if (this.isAnyQuadraticFactorNonZero()) {
            for (Entry<IntRowColumn, BigDecimal> entry : myQuadratic.entrySet()) {
                retVal.quadratic().set(entry.getKey().row, entry.getKey().column, entry.getValue().doubleValue());
            }
        }

        return retVal;
    }

    private QuadraticFunction<Double> makeQuadraticFunction() {

        final QuadraticFunction<Double> retVal = QuadraticFunction.makePrimitive(myModel.countVariables());

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

        return retVal;
    }

    private BigDecimal toPositiveFraction(final BigDecimal noninteger) {
        BigDecimal intPart = noninteger.setScale(0, RoundingMode.FLOOR);
        return noninteger.subtract(intPart);
    }

    protected void appendMiddlePart(final StringBuilder builder, final Access1D<BigDecimal> currentSolution) {

        builder.append(this.getName());
        builder.append(": ");
        builder.append(ModelEntity.DISPLAY.enforce(this.toFunction().invoke(Access1D.asPrimitive1D(currentSolution))));

        if (this.isObjective()) {
            builder.append(" (");
            builder.append(ModelEntity.DISPLAY.enforce(this.getContributionWeight()));
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
    protected void doIntegerRounding() {

        BigInteger gcd = null;
        int maxScale = Integer.MIN_VALUE;
        for (BigDecimal coeff : myLinear.values()) {
            BigDecimal abs = coeff.stripTrailingZeros().abs();
            maxScale = Math.max(maxScale, abs.scale());
            if (gcd != null) {
                gcd = gcd.gcd(abs.unscaledValue());
            } else {
                gcd = abs.unscaledValue();
            }
            if (gcd.equals(BigInteger.ONE)) {
                return; // gcd == 1, no point
            }
        }

        BigDecimal divisor = new BigDecimal(gcd, maxScale);

        for (Entry<IntIndex, BigDecimal> entry : myLinear.entrySet()) {
            BigDecimal value = entry.getValue();
            entry.setValue(value.divide(divisor, 0, RoundingMode.UNNECESSARY));
        }

        BigDecimal lower = this.getLowerLimit();
        if (lower != null) {
            this.lower(lower.divide(divisor, 0, RoundingMode.CEILING));
        }

        BigDecimal upper = this.getUpperLimit();
        if (upper != null) {
            this.upper(upper.divide(divisor, 0, RoundingMode.FLOOR));
        }
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
     * Calculates this expression's value - the subset variables' part of this expression. Will never return
     * null.
     */
    BigDecimal calculateSetValue(final Collection<IntIndex> subset) {

        BigDecimal retVal = BigMath.ZERO;

        if (subset.size() > 0) {

            for (final IntIndex linKey : myLinear.keySet()) {
                if (subset.contains(linKey)) {
                    final BigDecimal coefficient = this.get(linKey);
                    final BigDecimal value = myModel.getVariable(linKey.index).getValue();
                    retVal = retVal.add(coefficient.multiply(value));
                }
            }

            for (final IntRowColumn quadKey : myQuadratic.keySet()) {
                if (subset.contains(new IntIndex(quadKey.row))) {
                    if (subset.contains(new IntIndex(quadKey.column))) {
                        final BigDecimal coefficient = this.get(quadKey);
                        final BigDecimal rowValue = myModel.getVariable(quadKey.row).getValue();
                        final BigDecimal colValue = myModel.getVariable(quadKey.column).getValue();
                        retVal = retVal.add(coefficient.multiply(rowValue).multiply(colValue));
                    }
                }
            }
        }

        return retVal;
    }

    Expression copy(final ExpressionsBasedModel destinationModel, final boolean deep) {
        return new Expression(this, destinationModel, deep);
    }

    long countIntegerFactors() {
        return myLinear.keySet().stream().map(this::resolve).filter(v -> v.isInteger()).count();
    }

    int countLinearFactors() {
        return myLinear.size();
    }

    int countQuadraticFactors() {
        return myQuadratic.size();
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

        Expression retVal = myModel.addExpression(this.getName() + "(MIR)");

        for (Entry<IntIndex, BigDecimal> entry : myLinear.entrySet()) {
            Variable variable = this.resolve(entry.getKey());

            if (!variable.isLowerLimitSet() || (variable.getLowerLimit().compareTo(BigMath.ZERO) < 0)) {
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

            } else {

                if (coeff.signum() == 1) {
                    retVal.set(variable, BigMath.DIVIDE.invoke(coeff, posFracLevel));
                } else if (coeff.signum() == -1) {
                    BigDecimal negCoeff = coeff.negate();
                    retVal.set(variable, BigMath.DIVIDE.invoke(negCoeff, cmpFracLevel));
                }
            }
        }

        return retVal.lower(BigMath.ONE);
    }

    Set<Variable> getBinaryVariables(final Set<IntIndex> subset) {

        final HashSet<Variable> retVal = new HashSet<>();

        for (final IntIndex varInd : myLinear.keySet()) {
            if (subset.contains(varInd)) {
                final Variable variable = myModel.getVariable(varInd.index);
                if (variable.isBinary()) {
                    retVal.add(variable);
                }
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

    ExpressionsBasedModel getModel() {
        return myModel;
    }

    HashMap<IntRowColumn, BigDecimal> getQuadratic() {
        return myQuadratic;
    }

    BigDecimal getQuadraticFactor(final IntRowColumn key, final boolean adjusted) {
        return this.convert(myQuadratic.get(key), adjusted);
    }

    boolean includes(final Variable variable) {
        final IntIndex tmpVarInd = variable.getIndex();
        return myLinear.containsKey(tmpVarInd) || ((myQuadratic.size() > 0) && myQuadratic.keySet().stream().anyMatch(k -> {
            return (k.row == tmpVarInd.index) || (k.column == tmpVarInd.index);
        }));
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
                final Variable setVar = myModel.getVariable(index);
                int signum = myLinear.get(index).signum();
                if ((signum < 0) && setVar.isLowerLimitSet() && (setVar.getLowerLimit().signum() >= 0)) {

                } else if ((signum > 0) && setVar.isUpperLimitSet() && (setVar.getUpperLimit().signum() <= 0)) {

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
                final Variable setVar = myModel.getVariable(index);
                int signum = myLinear.get(index).signum();
                if ((signum > 0) && setVar.isLowerLimitSet() && (setVar.getLowerLimit().signum() >= 0)) {

                } else if ((signum < 0) && setVar.isUpperLimitSet() && (setVar.getUpperLimit().signum() <= 0)) {

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

    void setInfeasible() {
        myInfeasible = true;
        myModel.setInfeasible();
    }

    void setRedundant() {
        myRedundant = true;
    }

    @Override
    void visitAllParameters(final VoidFunction<BigDecimal> largest, final VoidFunction<BigDecimal> smallest) {

        if (this.isAnyQuadraticFactorNonZero()) {
            for (final BigDecimal quadraticFactor : myQuadratic.values()) {
                largest.invoke(quadraticFactor);
                smallest.invoke(quadraticFactor);
            }
        } else if (this.isAnyLinearFactorNonZero()) {
            for (final BigDecimal linearFactor : myLinear.values()) {
                largest.invoke(linearFactor);
                smallest.invoke(linearFactor);
            }
        } else {
            super.visitAllParameters(largest, smallest);
        }
    }

}
