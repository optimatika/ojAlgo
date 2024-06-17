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

import java.util.Arrays;
import java.util.Objects;

import org.ojalgo.optimisation.ConstraintsMetaData;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * LP (simplex tableau) meta data.
 *
 * @author apete
 */
final class LinearStructure implements ExpressionsBasedModel.EntityMap {

    final ConstraintsMetaData constraints;
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
        constraints = ConstraintsMetaData.newInstance(constrIn + constrEq, inclMap);

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
    public EntryPair<ModelEntity<?>, ConstraintType> getConstraint(final int idc) {
        return constraints.getEntry(idc);
    }

    @Override
    public EntryPair<ModelEntity<?>, ConstraintType> getSlack(final int ids) {

        if (ids < nbSlck) {
            return this.getConstraint(nbIdty + ids);
        } else {
            return this.getConstraint(ids - nbSlck);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(negativePartVariables);
        result = prime * result + Arrays.hashCode(positivePartVariables);
        result = prime * result + Objects.hash(constraints, nbArti, nbEqus, nbIdty, nbInes, nbNegs, nbSlck, nbVars);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LinearStructure)) {
            return false;
        }
        LinearStructure other = (LinearStructure) obj;
        return Objects.equals(constraints, other.constraints) && nbArti == other.nbArti && nbEqus == other.nbEqus && nbIdty == other.nbIdty
                && nbInes == other.nbInes && nbNegs == other.nbNegs && nbSlck == other.nbSlck && nbVars == other.nbVars
                && Arrays.equals(negativePartVariables, other.negativePartVariables) && Arrays.equals(positivePartVariables, other.positivePartVariables);
    }

    @Override
    public String toString() {
        return this.countConstraints() + " x " + this.countVariables();
    }

}
