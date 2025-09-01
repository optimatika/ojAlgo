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
package org.ojalgo.data.cluster;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.ojalgo.data.proximity.Distance;
import org.ojalgo.data.proximity.DistanceMeasure;

/**
 * Immutable coordinate point used by the clustering utilities. A {@code Point} wraps a {@code float[]} (to
 * minimise memory footprint) and an id. Distance calculations are central to most algorithms.
 */
public final class Point implements Comparable<Point> {

    /**
     * Simple factory that generates consecutive ids and ensures consistent dimensionality.
     */
    public static final class Factory {

        private final AtomicInteger myNextID = new AtomicInteger();

        public Factory() {
            super();
        }

        public Point newPoint(final float... coordinates) {
            return new Point(myNextID.getAndIncrement(), coordinates);
        }

        public void reset() {
            myNextID.set(0);
        }

    }

    public static double[] mean(final Collection<Point> points) {

        double[] retVal = null;
        int length = 0;
        for (Point point : points) {
            if (retVal == null) {
                length = point.coordinates.length;
                retVal = new double[length];
            }
            for (int i = 0; i < length; i++) {
                retVal[i] += point.coordinates[i];
            }
        }

        if (retVal == null) {
            retVal = new double[0];
        } else {
            double scale = ONE / points.size();
            for (int i = 0; i < length; i++) {
                retVal[i] *= scale;
            }
        }

        return retVal;
    }

    public static Point.Factory newFactory() {
        return new Point.Factory();
    }

    /**
     * Creates a point with the supplied id and coordinates (no defensive copy). Caller must ensure the
     * coordinate array is not mutated afterwards if logical immutability is desired.
     */
    public static Point of(final int id, final float... coordinates) {
        return new Point(id, coordinates);
    }

    /**
     * Features
     */
    public final float[] coordinates;

    /**
     * Index/Key
     */
    public final int id;

    Point(final int id, final float[] coordinates) {
        super();
        this.id = id;
        this.coordinates = coordinates;
    }

    @Override
    public int compareTo(final Point ref) {
        return Integer.compare(id, ref.id);
    }

    /**
     * Distance using the supplied {@link DistanceMeasure}.
     */
    public double distance(final DistanceMeasure measure, final Point other) {
        switch (measure) {
        case CHEBYSHEV:
            return Distance.chebyshev(coordinates, other.coordinates);
        case COSINE:
            return Distance.cosine(coordinates, other.coordinates);
        case ANGULAR:
            return Distance.angular(coordinates, other.coordinates);
        case CORRELATION:
            return Distance.correlation(coordinates, other.coordinates);
        case EUCLIDEAN:
            return Distance.euclidean(coordinates, other.coordinates);
        case CANBERRA:
            return Distance.canberra(coordinates, other.coordinates);
        case HAMMING:
            return Distance.hamming(coordinates, other.coordinates);
        case HELLINGER:
            return Distance.hellinger(coordinates, other.coordinates);
        case JACCARD:
            return Distance.jaccard(coordinates, other.coordinates);
        case MANHATTAN:
            return Distance.manhattan(coordinates, other.coordinates);
        case SQUARED_EUCLIDEAN:
            return Distance.squaredEuclidean(coordinates, other.coordinates);
        default:
            throw new IllegalArgumentException("Unknown measure: " + measure);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Point)) {
            return false;
        }
        Point other = (Point) obj;
        if (id != other.id || !Arrays.equals(coordinates, other.coordinates)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + Arrays.hashCode(coordinates);
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(coordinates);
    }

}