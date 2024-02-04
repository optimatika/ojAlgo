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
package org.ojalgo.optimisation.linear;

import org.ojalgo.optimisation.ConstraintsMap;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * LP (simplex tableau) meta data.
 *
 * @author apete
 */
final class LinearStructure implements ExpressionsBasedModel.EntityMap {

    final ConstraintsMap constraints;
    /**
     * The number of artificial variables
     */
    final int nbArti;
    /**
     * The number of equality constraints
     */
    final int nbEqus;
    /**
     * The number of slack variables that also form an identity sub-matrix (in the tableau).
     */
    final int nbIdty;
    /**
     * The number of inequality constraints
     */
    final int nbInes;
    /**
     * The number of negated model variables
     */
    final int nbNegs;
    /**
     * The number of slack variables (not known to be "identity")
     */
    final int nbSlck;
    /**
     * The number of positive (as-is) model variables
     */
    final int nbVars;
    final int[] negativePartVariables;
    final int[] positivePartVariables;

    LinearStructure(final boolean inclMap, final int constrIn, final int constrEq, final int varsPos, final int varsNeg, final int varsSlk, final int varsEye) {

        positivePartVariables = new int[varsPos];
        negativePartVariables = new int[varsNeg];
        constraints = ConstraintsMap.newInstance(constrIn + constrEq, inclMap);

        nbInes = constrIn;
        nbEqus = constrEq;

        nbVars = varsPos;
        nbNegs = varsNeg;
        nbSlck = varsSlk;
        nbIdty = varsEye;
        nbArti = constrIn + constrEq - varsEye;
    }

    LinearStructure(final int nbConstraints, final int nbVariables) {
        this(false, 0, nbConstraints, nbVariables, 0, 0, 0);
    }

    @Override
    public int countAdditionalConstraints() {
        return 0;
    }

    @Override
    public int countConstraints() {
        return nbInes + nbEqus;
    }

    @Override
    public int countEqualityConstraints() {
        return nbEqus;
    }

    @Override
    public int countInequalityConstraints() {
        return nbInes;
    }

    @Override
    public int countModelVariables() {
        return nbVars + nbNegs;
    }

    @Override
    public int countSlackVariables() {
        return nbSlck + nbIdty;
    }

    @Override
    public int countVariables() {
        return nbVars + nbNegs + nbSlck + nbIdty;
    }

    @Override
    public EntryPair<ModelEntity<?>, ConstraintType> getConstraintMap(final int i) {
        return constraints.getEntry(i);
    }

    @Override
    public EntryPair<ModelEntity<?>, ConstraintType> getSlack(final int idx) {

        if (idx < nbSlck) {
            return this.getConstraintMap(nbIdty + idx);
        } else {
            return this.getConstraintMap(idx - nbSlck);
        }
    }

    @Override
    public int indexOf(final int j) {

        if (j < 0) {
            throw new IllegalArgumentException();
        }

        if (j < positivePartVariables.length) {
            return positivePartVariables[j];
        }

        int jn = j - positivePartVariables.length;

        if (jn < negativePartVariables.length) {
            return negativePartVariables[jn];
        }

        return -1;
    }

    public boolean isConstraintNegated(final int i) {
        return constraints.negated[i];
    }

    @Override
    public boolean isNegated(final int j) {

        if (j < 0) {
            throw new IllegalArgumentException();
        }

        if (j < positivePartVariables.length) {
            return false;
        }

        if (j - positivePartVariables.length < negativePartVariables.length) {
            return true;
        }

        // TODO This case depends on the solver
        // return slack[idx - positivePartVariables.length - negativePartVariables.length].getValue() == ConstraintType.LOWER;
        return false;
    }

    public boolean negated(final int i, final boolean negated) {
        return constraints.negated[i] = negated;
    }

    public void setConstraintMap(final int i, final ModelEntity<?> entity, final ConstraintType type) {
        constraints.setEntry(i, entity, type);
    }

    public void setConstraintMap(final int i, final ModelEntity<?> entity, final ConstraintType type, final boolean negated) {
        constraints.setEntry(i, entity, type);
        constraints.negated[i] = negated;
    }

    public void setConstraintNegated(final int i, final boolean negated) {
        constraints.negated[i] = negated;
    }

    int countVariablesTotally() {
        return nbVars + nbNegs + nbSlck + nbIdty + nbArti;
    }

    boolean isAnyArtificials() {
        return nbArti > 0;
    }

    boolean isArtificialVariable(final int variableIndex) {
        return variableIndex < 0 || variableIndex >= this.countVariables();
    }

    boolean isFullSetOfArtificials() {
        return nbArti == this.countConstraints();
    }

    boolean isModelVariable(final int variableIndex) {
        return variableIndex >= 0 && variableIndex < this.countModelVariables();
    }

    void setObjectiveAdjustmentFactor(final double multiplierScale) {
        constraints.setMultiplierScale(multiplierScale);
    }

}
