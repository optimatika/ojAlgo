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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.NumberList;
import org.ojalgo.data.proximity.DistanceMeasure;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.SampleSet;

final class PointDistanceCache {

    private double[][] myDistances;
    private final SampleSet mySampleSet = SampleSet.make();
    private final NumberList<Double> myValues = NumberList.factory(ArrayR064.FACTORY).make();

    PointDistanceCache() {
        super();
    }

    private double distance(final int i, final int j) {
        if (i == j) {
            return PrimitiveMath.ZERO;
        } else if (i < j) {
            return myDistances[j][i];
        } else {
            return myDistances[i][j];
        }
    }

    /**
     * Pick the centroid for this cluster (one of its current members)
     */
    Point centroid(final Collection<Point> cluster) {

        Point retVal = null;
        double minSum = Double.POSITIVE_INFINITY;

        for (Point candidate : cluster) {

            double sum = PrimitiveMath.ZERO;

            for (Point member : cluster) {
                sum += this.distance(candidate, member);
            }

            if (sum < minSum) {
                minSum = sum;
                retVal = candidate;
            }
        }

        return retVal;
    }

    /**
     * Get the distance between these two points.
     */
    double distance(final Point point1, final Point point2) {
        return this.distance(point1.id, point2.id);
    }

    /**
     * The distance threshold used in {@link #initialiser(Collection)} to determine when the greedy clustering
     * algorithm should not place a point in any of the existing clusters, but rather create a new cluster.
     */
    double getThreshold() {
        return mySampleSet.swap(myValues).getMedian();
    }

    /**
     * Generate an initial set of centroids
     */
    List<Point> initialiser(final Collection<Point> input) {

        GreedyClustering<Point> greedy = new GreedyClustering<>(this::centroid, this::distance, this.getThreshold());
        List<Set<Point>> clusters = greedy.cluster(input);
        List<Point> centroids = greedy.getCentroids();

        double total = myDistances.length;
        double largest = clusters.stream().mapToInt(Set::size).max().orElse(0);

        return IntStream.range(0, centroids.size()).filter(i -> {
            double size = clusters.get(i).size();
            return size > 1D && size / total > 0.01D && size / largest > 0.02D;
        }).mapToObj(centroids::get).collect(Collectors.toList());
    }

    /**
     * Set up the cache (configure this instance)
     */
    void setup(final Collection<Point> input, final DistanceMeasure measure) {

        int nbPoints = input.size();

        if (myDistances == null || myDistances.length != nbPoints) {
            myDistances = new double[nbPoints][];
            for (int i = 0; i < nbPoints; i++) {
                myDistances[i] = new double[i];
            }
        }

        myValues.clear();

        for (Point pointR : input) {
            int row = pointR.id;
            for (Point pointC : input) {
                int col = pointC.id;
                if (row > col) {
                    myValues.add(myDistances[row][col] = pointR.distance(measure, pointC));
                }
            }
        }
    }

}
