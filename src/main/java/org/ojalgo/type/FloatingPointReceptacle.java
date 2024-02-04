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
package org.ojalgo.type;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ojalgo.structure.Access1D;

/**
 * An array (double[] or float[]) builder/converter. The arrays are built by prepending/appending segments of
 * anything that can be converted to either double[] or float[] arrays. The total/aggregated arrays are
 * extracted by calling {@link #supplyTo(double[])} or {@link #supplyTo(float[])}. This also serves as a type
 * converter from any number type to double or float.
 *
 * @author apete
 */
public final class FloatingPointReceptacle {

    public static FloatingPointReceptacle of(final Access1D<?> values) {
        FloatingPointReceptacle retVal = new FloatingPointReceptacle();
        retVal.append(values);
        return retVal;
    }

    public static FloatingPointReceptacle of(final double... values) {
        FloatingPointReceptacle retVal = new FloatingPointReceptacle();
        retVal.append(values);
        return retVal;
    }

    public static FloatingPointReceptacle of(final float... values) {
        FloatingPointReceptacle retVal = new FloatingPointReceptacle();
        retVal.append(values);
        return retVal;
    }

    public static FloatingPointReceptacle of(final int count, final double value) {
        FloatingPointReceptacle retVal = new FloatingPointReceptacle();
        retVal.append(count, value);
        return retVal;
    }

    public static FloatingPointReceptacle of(final int count, final float value) {
        FloatingPointReceptacle retVal = new FloatingPointReceptacle();
        retVal.append(count, value);
        return retVal;
    }

    public static FloatingPointReceptacle of(final List<? extends Comparable<?>> values) {
        FloatingPointReceptacle retVal = new FloatingPointReceptacle();
        retVal.append(values);
        return retVal;
    }

    private final Deque<Object> myContents = new ArrayDeque<>();
    private int mySize = 0;

    public FloatingPointReceptacle() {
        super();
    }

    public void append(final Access1D<?> part) {
        myContents.addLast(part);
        mySize += part.size();
    }

    public void append(final double... part) {
        myContents.addLast(part);
        mySize += part.length;
    }

    public void append(final float... part) {
        myContents.addLast(part);
        mySize += part.length;
    }

    public void append(final FloatingPointReceptacle part) {
        myContents.addLast(part);
        mySize += part.size();
    }

    public void append(final int count, final double value) {
        double[] part = new double[count];
        Arrays.fill(part, value);
        this.append(part);
    }

    public void append(final int count, final float value) {
        float[] part = new float[count];
        Arrays.fill(part, value);
        this.append(part);
    }

    public void append(final List<? extends Comparable<?>> part) {
        myContents.addLast(part);
        mySize += part.size();
    }

    public void clear() {
        myContents.clear();
        mySize = 0;
    }

    public void prepend(final Access1D<?> part) {
        myContents.addFirst(part);
        mySize += part.size();
    }

    public void prepend(final double... part) {
        myContents.addFirst(part);
        mySize += part.length;
    }

    public void prepend(final float... part) {
        myContents.addFirst(part);
        mySize += part.length;
    }

    public void prepend(final FloatingPointReceptacle part) {
        myContents.addFirst(part);
        mySize += part.size();
    }

    public void prepend(final int count, final double value) {
        double[] part = new double[count];
        Arrays.fill(part, value);
        this.prepend(part);
    }

    public void prepend(final int count, final float value) {
        float[] part = new float[count];
        Arrays.fill(part, value);
        this.prepend(part);
    }

    public void prepend(final List<? extends Comparable<?>> part) {
        myContents.addFirst(part);
        mySize += part.size();
    }

    public int size() {
        return mySize;
    }

    public void supplyTo(final double[] destination) {
        this.supplyTo(destination, 0);
    }

    public void supplyTo(final float[] destination) {
        this.supplyTo(destination, 0);
    }

    /**
     * Will create a new array with each call. It would be much more efficient if possible to reuse array and
     * instead call {@link #supplyTo(double[])}.
     */
    public double[] toDoubles() {
        double[] retVal = new double[mySize];
        this.supplyTo(retVal);
        return retVal;
    }

    /**
     * Will create a new array with each call. It would be much more efficient if possible to reuse array and
     * instead call {@link #supplyTo(float[])}.
     */
    public float[] toFloats() {
        float[] retVal = new float[mySize];
        this.supplyTo(retVal);
        return retVal;
    }

    private int copy(final Access1D<?> source, final double[] destination, final int offset) {
        int limit = Math.min(source.size(), destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = source.doubleValue(s);
        }
        return offset + limit;
    }

    private int copy(final Access1D<?> source, final float[] destination, final int offset) {
        int limit = Math.min(source.size(), destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = source.floatValue(s);
        }
        return offset + limit;
    }

    private int copy(final double[] source, final double[] destination, final int offset) {
        int limit = Math.min(source.length, destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = source[s];
        }
        return offset + limit;
    }

    private int copy(final double[] source, final float[] destination, final int offset) {
        int limit = Math.min(source.length, destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = (float) source[s];
        }
        return offset + limit;
    }

    private int copy(final float[] source, final double[] destination, final int offset) {
        int limit = Math.min(source.length, destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = source[s];
        }
        return offset + limit;
    }

    private int copy(final float[] source, final float[] destination, final int offset) {
        int limit = Math.min(source.length, destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = source[s];
        }
        return offset + limit;
    }

    private int copy(final List<? extends Comparable<?>> source, final double[] destination, final int offset) {
        int limit = Math.min(source.size(), destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = NumberDefinition.doubleValue(source.get(s));
        }
        return offset + limit;
    }

    private int copy(final List<? extends Comparable<?>> source, final float[] destination, final int offset) {
        int limit = Math.min(source.size(), destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = NumberDefinition.floatValue(source.get(s));
        }
        return offset + limit;
    }

    @SuppressWarnings("unchecked")
    private int supplyTo(final double[] destination, int offset) {
        for (Object source : myContents) {
            if (source instanceof float[]) {
                offset = this.copy((float[]) source, destination, offset);
            } else if (source instanceof double[]) {
                offset = this.copy((double[]) source, destination, offset);
            } else if (source instanceof Access1D) {
                offset = this.copy((Access1D<?>) source, destination, offset);
            } else if (source instanceof List) {
                offset = this.copy((List<? extends Comparable<?>>) source, destination, offset);
            } else if (source instanceof FloatingPointReceptacle) {
                offset = ((FloatingPointReceptacle) source).supplyTo(destination, offset);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return offset;
    }

    @SuppressWarnings("unchecked")
    private int supplyTo(final float[] destination, int offset) {
        for (Object source : myContents) {
            if (source instanceof float[]) {
                offset = this.copy((float[]) source, destination, offset);
            } else if (source instanceof double[]) {
                offset = this.copy((double[]) source, destination, offset);
            } else if (source instanceof Access1D) {
                offset = this.copy((Access1D<?>) source, destination, offset);
            } else if (source instanceof List) {
                offset = this.copy((List<? extends Comparable<?>>) source, destination, offset);
            } else if (source instanceof FloatingPointReceptacle) {
                offset = ((FloatingPointReceptacle) source).supplyTo(destination, offset);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return offset;
    }

}
