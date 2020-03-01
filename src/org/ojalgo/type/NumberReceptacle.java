/*
 * Copyright 1997-2020 Optimatika
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

import java.util.LinkedList;
import java.util.List;

import org.ojalgo.structure.Access1D;

/**
 * An array (double[] or float[]) builder. The arrays are built by prepending/appending segements of anything
 * that can be converted to either double[] or float[] arrays. The total/aggregated arrays are extracted by
 * calling {@link #supplyTo(double[])} or {@link #supplyTo(float[])}.
 *
 * @author apete
 */
public final class NumberReceptacle {

    static final class Placeholder {

        final int count;
        final double value;

        Placeholder(int count, double value) {
            super();
            this.count = count;
            this.value = value;
        }

    }

    public static NumberReceptacle of(Access1D<?> values) {
        NumberReceptacle retVal = new NumberReceptacle();
        retVal.append(values);
        return retVal;
    }

    public static NumberReceptacle of(double... values) {
        NumberReceptacle retVal = new NumberReceptacle();
        retVal.append(values);
        return retVal;
    }

    public static NumberReceptacle of(float... values) {
        NumberReceptacle retVal = new NumberReceptacle();
        retVal.append(values);
        return retVal;
    }

    public static NumberReceptacle of(int count, double value) {
        NumberReceptacle retVal = new NumberReceptacle();
        retVal.append(count, value);
        return retVal;
    }

    public static NumberReceptacle of(int count, float value) {
        NumberReceptacle retVal = new NumberReceptacle();
        retVal.append(count, value);
        return retVal;
    }

    public static NumberReceptacle of(List<? extends Comparable<?>> values) {
        NumberReceptacle retVal = new NumberReceptacle();
        retVal.append(values);
        return retVal;
    }

    private final LinkedList<Object> myContents = new LinkedList<>();
    private int mySize = 0;

    public NumberReceptacle() {
        super();
    }

    public void append(Access1D<?> part) {
        myContents.addLast(part);
        mySize += part.size();
    }

    public void append(double value) {
        myContents.addLast(new double[] { value });
        mySize++;
    }

    public void append(double[] part) {
        myContents.addLast(part);
        mySize += part.length;
    }

    public void append(float value) {
        myContents.addLast(new float[] { value });
        mySize++;
    }

    public void append(float[] part) {
        myContents.addLast(part);
        mySize += part.length;
    }

    public void append(int count, double value) {
        myContents.addLast(new Placeholder(count, value));
        mySize += count;
    }

    public void append(int count, float value) {
        myContents.addLast(new Placeholder(count, value));
        mySize += count;
    }

    public void append(List<? extends Comparable<?>> part) {
        myContents.addLast(part);
        mySize += part.size();
    }

    public void append(NumberReceptacle part) {
        myContents.addLast(part);
        mySize += part.size();
    }

    public void clear() {
        myContents.clear();
        mySize = 0;
    }

    public void prepend(Access1D<?> part) {
        myContents.addFirst(part);
        mySize += part.size();
    }

    public void prepend(double value) {
        myContents.addFirst(new double[] { value });
        mySize++;
    }

    public void prepend(double[] part) {
        myContents.addFirst(part);
        mySize += part.length;
    }

    public void prepend(float value) {
        myContents.addFirst(new float[] { value });
        mySize++;
    }

    public void prepend(float[] part) {
        myContents.addFirst(part);
        mySize += part.length;
    }

    public void prepend(int count, double value) {
        myContents.addFirst(new Placeholder(count, value));
        mySize += count;
    }

    public void prepend(int count, float value) {
        myContents.addFirst(new Placeholder(count, value));
        mySize += count;
    }

    public void prepend(List<? extends Comparable<?>> part) {
        myContents.addFirst(part);
        mySize += part.size();
    }

    public void prepend(NumberReceptacle part) {
        myContents.addFirst(part);
        mySize += part.size();
    }

    public int size() {
        return mySize;
    }

    public void supplyTo(double[] destination) {
        this.supplyTo(destination, 0);
    }

    public void supplyTo(float[] destination) {
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

    private int copy(final Placeholder source, final double[] destination, final int offset) {
        int limit = Math.min(source.count, destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = source.value;
        }
        return offset + limit;
    }

    private int copy(final Placeholder source, final float[] destination, final int offset) {
        int limit = Math.min(source.count, destination.length - offset);
        for (int s = 0; s < limit; s++) {
            destination[offset + s] = (float) source.value;
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
            } else if (source instanceof NumberReceptacle) {
                offset = ((NumberReceptacle) source).supplyTo(destination, offset);
            } else if (source instanceof Placeholder) {
                offset = this.copy((Placeholder) source, destination, offset);
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
            } else if (source instanceof NumberReceptacle) {
                offset = ((NumberReceptacle) source).supplyTo(destination, offset);
            } else if (source instanceof Placeholder) {
                offset = this.copy((Placeholder) source, destination, offset);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return offset;
    }

}
