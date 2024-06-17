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

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

public final class ConstraintsMetaData implements Structure1D {

    public static ConstraintsMetaData newEntityMap(final int nbConstraints) {
        return new ConstraintsMetaData((EntryPair<ModelEntity<?>, ConstraintType>[]) new EntryPair<?, ?>[nbConstraints], new boolean[nbConstraints]);
    }

    public static ConstraintsMetaData newInstance(final int nbConstraints, final boolean inclDefs) {
        EntryPair<ModelEntity<?>, ConstraintType>[] definitions = inclDefs ? (EntryPair<ModelEntity<?>, ConstraintType>[]) new EntryPair<?, ?>[nbConstraints] : null;
        boolean[] negated = new boolean[nbConstraints];
        return new ConstraintsMetaData(definitions, negated);
    }

    public static ConstraintsMetaData newSimple(final int nbConstraints) {
        return new ConstraintsMetaData(null, new boolean[nbConstraints]);
    }

    public final boolean[] negated;
    private final EntryPair<ModelEntity<?>, ConstraintType>[] myDefinitions;
    private double myMultiplierScale = 1D;

    private ConstraintsMetaData(final EntryPair<ModelEntity<?>, ConstraintType>[] defs, final boolean[] negs) {
        super();
        myDefinitions = defs;
        negated = negs;
    }

    @Override
    public long count() {
        return negated.length;
    }

    public EntryPair<ModelEntity<?>, ConstraintType> getEntry(final int i) {
        return myDefinitions[i];
    }

    public boolean isEntityMap() {
        return myDefinitions != null;
    }

    public List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> match(final Access1D<?> multipliers) {

        int length = myDefinitions.length;

        List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> retVal = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            EntryPair<ModelEntity<?>, ConstraintType> constraintKey = myDefinitions[i];
            double adjustmentFactor = constraintKey.left().getAdjustmentFactor();
            double multiplierValue = multipliers.doubleValue(i);
            retVal.add(constraintKey.asKeyTo(multiplierValue * adjustmentFactor / myMultiplierScale));
        }

        return retVal;
    }

    public void setEntry(final int i, final ModelEntity<?> entity, final ConstraintType type) {
        myDefinitions[i] = EntryPair.of(entity, type);
    }

    public void setEntry(final int i, final ModelEntity<?> entity, final ConstraintType type, final boolean neg) {
        myDefinitions[i] = EntryPair.of(entity, type);
        negated[i] = neg;
    }

    public void setMultiplierScale(final double multiplierScale) {
        myMultiplierScale = multiplierScale;
    }

    @Override
    public int size() {
        return negated.length;
    }

    double getMultiplierScale() {
        return myMultiplierScale;
    }
}
