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
package org.ojalgo.machine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * MemoryEstimator
 *
 * @author apete
 */
public final class MemoryEstimator {

    private static final long FINAL_ALIGNEMENT = 8L;
    private static final long PARENT_ALIGNEMENT = 4L;
    private static final long WORD = 8L;
    private static final long ZERO = 0L;

    public static long estimateArray(final Class<?> componentType, final int length) {

        MemoryEstimator estimator = MemoryEstimator.makeForClassExtendingObject();

        estimator.add(JavaType.INT.memory());

        JavaType match = JavaType.match(componentType);
        estimator.add(length * match.memory());

        return estimator.estimate();
    }

    public static long estimateObject(final Class<?> type) {
        return MemoryEstimator.make(type).estimate();
    }

    public static MemoryEstimator makeForClassExtendingObject() {
        return new MemoryEstimator(WORD + JavaType.REFERENCE.memory());
    }

    public static MemoryEstimator makeForSubclass(final MemoryEstimator parentEstimation) {
        return new MemoryEstimator(parentEstimation.align(PARENT_ALIGNEMENT));
    }

    static MemoryEstimator make(final Class<?> type) {

        MemoryEstimator retVal = null;

        Class<?> tmpParent = type.getSuperclass();

        if (Object.class.equals(tmpParent)) {
            retVal = MemoryEstimator.makeForClassExtendingObject();
        } else {
            MemoryEstimator tmpParentEstimation = MemoryEstimator.make(tmpParent);
            retVal = MemoryEstimator.makeForSubclass(tmpParentEstimation);
        }

        for (Field tmpField : type.getDeclaredFields()) {

            int tmpModifier = tmpField.getModifiers();
            if (!Modifier.isStatic(tmpModifier)) {

                Class<?> tmpType = tmpField.getType();
                retVal.add(JavaType.match(tmpType));
            }
        }

        return retVal;
    }

    private long myShallowSize = ZERO;

    @SuppressWarnings("unused")
    private MemoryEstimator() {
        this(ZERO);
    }

    MemoryEstimator(final long aBase) {

        super();

        myShallowSize = aBase;
    }

    public MemoryEstimator add(final Class<?> type) {
        return this.add(JavaType.match(type));
    }

    public MemoryEstimator add(final JavaType type) {
        return this.add(type.memory());
    }

    public long estimate() {
        return this.align(FINAL_ALIGNEMENT);
    }

    private MemoryEstimator add(final long bytes) {
        myShallowSize += bytes;
        return this;
    }

    private long align(final long alignement) {

        long remainder = myShallowSize % alignement;

        if (remainder != ZERO) {
            return myShallowSize + (alignement - remainder);
        } else {
            return myShallowSize;
        }
    }

}
