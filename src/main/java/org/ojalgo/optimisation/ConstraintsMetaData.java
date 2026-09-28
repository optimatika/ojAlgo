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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

public final class ConstraintsMetaData implements Structure1D {

    public static ConstraintsMetaData newEntityMap(final int nbConstraints) {
        return ConstraintsMetaData.newInstance(nbConstraints, true);
    }

    public static ConstraintsMetaData newInstance(final int nbConstraints, final boolean mapped) {

        EntryPair<ModelEntity<?>, ConstraintType>[] definitions = mapped ? (EntryPair<ModelEntity<?>, ConstraintType>[]) new EntryPair<?, ?>[nbConstraints]
                : null;

        BitSet negated = new BitSet(nbConstraints);

        return new ConstraintsMetaData(nbConstraints, definitions, negated);
    }

    public static ConstraintsMetaData newSimple(final int nbConstraints) {
        return ConstraintsMetaData.newInstance(nbConstraints, false);
    }

    private final int mySize;
    private final BitSet myNegated;
    /**
     * Rows built from the entity's adjusted (scaled) parameters. Only their multipliers are scaled by the
     * entity's adjustment factor.
     */
    private final BitSet myAdjusted;
    private final EntryPair<ModelEntity<?>, ConstraintType>[] myDefinitions;
    private double myMultiplierScale = 1D;
    /**
     * The multipliers are signed as for two-sided rows, {@code l <= a'x <= u}: positive when the upper limit
     * is active and negative when the lower limit is (as with an ADMM/OSQP style solver).
     */
    private boolean mySignedRows = false;

    private ConstraintsMetaData(final int nbConstraints, final EntryPair<ModelEntity<?>, ConstraintType>[] defs, final BitSet negs) {
        super();
        mySize = nbConstraints;
        myDefinitions = defs;
        myNegated = negs;
        myAdjusted = new BitSet(nbConstraints);
    }

    public EntryPair<ModelEntity<?>, ConstraintType> getEntry(final int i) {
        return myDefinitions != null ? myDefinitions[i] : null;
    }

    /**
     * @return true if row i was built from its entity's adjusted (scaled) parameters
     */
    public boolean isAdjusted(final int i) {
        return myAdjusted.get(i);
    }

    public boolean isEntityMap() {
        return myDefinitions != null;
    }

    public boolean isNegated(final int i) {
        return myNegated.get(i);
    }

    /**
     * Matches the multipliers to the model entities, in model units and with the sign convention described at
     * {@link Optimisation.Result#getDualValues()}. A row built from a compensated copy of an expression (see
     * {@link Expression#compensate(java.util.Set)}) is scaled by the copy's adjustment factor, but reported
     * with the model expression it was derived from. With signed rows (see {@link #setSignedRows(boolean)}) a
     * two-sided (RANGE) row is reported with the limit that is active.
     */
    public List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> match(final Access1D<?> multipliers) {

        List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> retVal = new ArrayList<>(mySize);

        for (int i = 0; i < mySize; i++) {
            EntryPair<ModelEntity<?>, ConstraintType> constraintKey = myDefinitions[i];
            ModelEntity<?> entity = constraintKey.left();
            ConstraintType type = constraintKey.right();
            double adjustmentFactor = myAdjusted.get(i) ? entity.getAdjustmentFactor() : 1D;
            double multiplierValue = multipliers.doubleValue(i) * adjustmentFactor / myMultiplierScale;
            if (mySignedRows) {
                if (type == ConstraintType.LOWER) {
                    multiplierValue = -multiplierValue;
                } else if (type == ConstraintType.RANGE) {
                    type = multiplierValue < 0D ? ConstraintType.LOWER : ConstraintType.UPPER;
                    multiplierValue = Math.abs(multiplierValue);
                }
            }
            ModelEntity<?> origin = entity.getOrigin();
            if (origin != entity || type != constraintKey.right()) {
                constraintKey = EntryPair.of(origin, type);
            }
            retVal.add(constraintKey.asKeyTo(multiplierValue));
        }

        return retVal;
    }

    public boolean negated(final int i, final boolean neg) {
        myNegated.set(i, neg);
        return neg;
    }

    /**
     * Without an explicit adjusted argument the row is assumed to be built from the entity's adjusted
     * parameters, see {@link #setEntry(int, ModelEntity, ConstraintType, boolean, boolean)}.
     */
    public void setEntry(final int i, final ModelEntity<?> entity, final ConstraintType type) {
        myDefinitions[i] = EntryPair.of(entity, type);
        myAdjusted.set(i);
    }

    public void setEntry(final int i, final ModelEntity<?> entity, final ConstraintType type, final boolean neg) {
        myDefinitions[i] = EntryPair.of(entity, type);
        myNegated.set(i, neg);
        myAdjusted.set(i);
    }

    /**
     * @param adjusted Whether the row was built from the entity's adjusted (scaled) parameters. If not, its
     *                 multiplier is already in model units and {@link #match(Access1D)} does not apply the
     *                 entity's adjustment factor.
     */
    public void setEntry(final int i, final ModelEntity<?> entity, final ConstraintType type, final boolean neg, final boolean adjusted) {
        myDefinitions[i] = EntryPair.of(entity, type);
        myNegated.set(i, neg);
        myAdjusted.set(i, adjusted);
    }

    public void setMultiplierScale(final double multiplierScale) {
        myMultiplierScale = multiplierScale;
    }

    public void setNegated(final int i, final boolean neg) {
        myNegated.set(i, neg);
    }

    /**
     * Signed rows: the multipliers are signed as for two-sided rows, {@code l <= a'x <= u}, positive when the
     * upper limit is active and negative when the lower limit is. Without signed rows (the default) the
     * multipliers of inequality rows are expected to be non-negative, in the sign convention described at
     * {@link Optimisation.Result#getDualValues()}.
     */
    public void setSignedRows(final boolean signed) {
        mySignedRows = signed;
    }

    @Override
    public int size() {
        return mySize;
    }

    /**
     * For solvers that can change a row's limits after it is built (a variable's bound row, updated with
     * {@link UpdatableSolver#updateRange(int, double, double)}): the metadata with the type of row i matching
     * the new limits. If the type changes, a modified copy is returned, and this instance is left unchanged as
     * results already returned may refer to it.
     */
    public ConstraintsMetaData withLimits(final int i, final double lower, final double upper) {

        if (myDefinitions == null) {
            return this;
        }

        boolean lowerSet = lower > Double.NEGATIVE_INFINITY;
        boolean upperSet = upper < Double.POSITIVE_INFINITY;

        ConstraintType type;
        if (lowerSet && upperSet) {
            type = lower == upper ? ConstraintType.EQUALITY : ConstraintType.RANGE;
        } else if (lowerSet) {
            type = ConstraintType.LOWER;
        } else if (upperSet) {
            type = ConstraintType.UPPER;
        } else {
            type = ConstraintType.NONE;
        }

        EntryPair<ModelEntity<?>, ConstraintType> entry = myDefinitions[i];
        if (entry.right() == type) {
            return this;
        }

        ConstraintsMetaData retVal = new ConstraintsMetaData(mySize, myDefinitions.clone(), (BitSet) myNegated.clone());
        retVal.myAdjusted.or(myAdjusted);
        retVal.myMultiplierScale = myMultiplierScale;
        retVal.mySignedRows = mySignedRows;
        retVal.myDefinitions[i] = EntryPair.of(entry.left(), type);
        return retVal;
    }

    public double getMultiplierScale() {
        return myMultiplierScale;
    }
}
