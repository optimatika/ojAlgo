/*
 * Copyright 1997-2023 Optimatika
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

import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.UpdatableSolver;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * LP (simplex tableau) meta data.
 *
 * @author apete
 */
final class LinearStructure implements UpdatableSolver.EntityMap {

    final int nbCnstrEq;
    final int nbCnstrLo;
    final int nbCnstrUp;
    final int nbVarsArt;
    final int nbVarsNeg;
    final int nbVarsPos;
    final int nbVarsSlk;

    final boolean[] negatedDual;
    final int[] negativePartVariables;
    final int[] positivePartVariables;
    final EntryPair<ModelEntity<?>, ConstraintType>[] slack;

    LinearStructure(final int constraints, final int variables) {
        this(0, 0, constraints, variables, 0, 0, 0);
    }

    LinearStructure(final int constrUp, final int constrLo, final int constrEq, final int varsPos, final int varsNeg, final int varsSlk, final int varsArt) {

        positivePartVariables = new int[varsPos];
        negativePartVariables = new int[varsNeg];
        slack = (EntryPair<ModelEntity<?>, ConstraintType>[]) new EntryPair<?, ?>[varsSlk];
        negatedDual = new boolean[constrUp + constrLo + constrEq];

        nbCnstrUp = constrUp;
        nbCnstrLo = constrLo;
        nbCnstrEq = constrEq;

        nbVarsPos = varsPos;
        nbVarsNeg = varsNeg;
        nbVarsSlk = varsSlk;
        nbVarsArt = varsArt;
    }

    public int countModelVariables() {
        return nbVarsPos + nbVarsNeg;
    }

    public int countSlackVariables() {
        return nbVarsSlk;
    }

    public EntryPair<ModelEntity<?>, ConstraintType> getSlack(final int idx) {
        return slack[idx];
    }

    public int indexOf(final int idx) {

        if (idx < 0) {
            throw new IllegalArgumentException();
        }

        if (idx < positivePartVariables.length) {
            return positivePartVariables[idx];
        }

        int negIdx = idx - positivePartVariables.length;

        if (negIdx < negativePartVariables.length) {
            return negativePartVariables[negIdx];
        }

        return -1;
    }

    public boolean isNegated(final int idx) {

        if (idx < 0) {
            throw new IllegalArgumentException();
        }

        if (idx < positivePartVariables.length) {
            return false;
        }

        if (idx - positivePartVariables.length < negativePartVariables.length) {
            return true;
        }

        return false;
    }

    int countConstraints() {
        return nbCnstrUp + nbCnstrLo + nbCnstrEq;
    }

    int countVariablesTotally() {
        return nbVarsPos + nbVarsNeg + nbVarsSlk + nbVarsArt;
    }

    boolean isFullSetOfArtificials() {
        return nbVarsArt == this.countConstraints();
    }

}
