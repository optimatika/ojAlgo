/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064LSC;
import org.ojalgo.matrix.store.R064LSR;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.structure.Access1D.Collectable;

abstract class ForrestTomlin {

    static boolean updateCR(final Pivot pivot, final R064LSC l, final double[] diagU, final R064LSR u, final Pivot colPivot, final int columnIndex,
            final Collectable<Double, ? super TransformableRegion<Double>> newColumn, final PhysicalStore<Double> preallocated) {
        // TODO Auto-generated method stub
        return false;
    }

    static boolean updateRC(final Pivot pivot, final R064LSR l, final double[] diagU, final R064LSC u, final Pivot colPivot, final int columnIndex,
            final Collectable<Double, ? super TransformableRegion<Double>> newColumn, final PhysicalStore<Double> preallocated) {
        // TODO Auto-generated method stub
        return false;
    }

}
