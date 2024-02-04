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

public final class ConstraintsMap implements Structure1D {

    public static ConstraintsMap newEntityMap(final int nbConstraints) {
        return new ConstraintsMap((EntryPair<ModelEntity<?>, ConstraintType>[]) new EntryPair<?, ?>[nbConstraints], new boolean[nbConstraints]);
    }

    public static ConstraintsMap newInstance(final int nbConstraints, final boolean inclMap) {
        EntryPair<ModelEntity<?>, ConstraintType>[] map = inclMap ? (EntryPair<ModelEntity<?>, ConstraintType>[]) new EntryPair<?, ?>[nbConstraints] : null;
        boolean[] negated = new boolean[nbConstraints];
        return new ConstraintsMap(map, negated);
    }

    public static ConstraintsMap newSimple(final int nbConstraints) {
        return new ConstraintsMap(null, new boolean[nbConstraints]);
    }

    public final boolean[] negated;
    private final EntryPair<ModelEntity<?>, ConstraintType>[] myMap;
    private double myMultiplierScale = 1D;

    private ConstraintsMap(final EntryPair<ModelEntity<?>, ConstraintType>[] map, final boolean[] negs) {
        super();
        myMap = map;
        negated = negs;
    }

    @Override
    public long count() {
        return negated.length;
    }

    public EntryPair<ModelEntity<?>, ConstraintType> getEntry(final int i) {
        return myMap[i];
    }

    public boolean isEntityMap() {
        return myMap != null;
    }

    public List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> match(final Access1D<?> multipliers) {

        int length = myMap.length;

        List<KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>>> retVal = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            EntryPair<ModelEntity<?>, ConstraintType> constraintKey = myMap[i];
            double adjustmentFactor = constraintKey.left().getAdjustmentFactor();
            double multiplierValue = multipliers.doubleValue(i);
            retVal.add(constraintKey.asKeyTo(multiplierValue * adjustmentFactor / myMultiplierScale));
        }

        return retVal;
    }

    public void setEntry(final int i, final ModelEntity<?> entity, final ConstraintType type) {
        myMap[i] = EntryPair.of(entity, type);
    }

    public void setEntry(final int i, final ModelEntity<?> entity, final ConstraintType type, final boolean neg) {
        myMap[i] = EntryPair.of(entity, type);
        negated[i] = neg;
    }

    public void setMultiplierScale(final double multiplierScale) {
        myMultiplierScale = multiplierScale;
    }

    double getMultiplierScale() {
        return myMultiplierScale;
    }
}
