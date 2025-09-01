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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

import org.ojalgo.data.proximity.DistanceMeasure;

/**
 * Facade for clustering objects represented by float feature vectors.
 * <p>
 * Provides utilities to cluster arbitrary data by mapping items to {@link Point}s (immutable float[] feature
 * vectors).
 * <p>
 * <b>Usage:</b> Use {@link #cluster(Collection, Function)} to cluster your own data by providing an extractor
 * that produces the feature vector for each item. The result is a list of clusters, each represented as a
 * {@code Map<T, float[]>} containing the original items and their extracted features.
 * <p>
 * <b>Available clustering algorithms:</b>
 * <ul>
 * <li>{@link #newAutomatic(DistanceMeasure)}: Automatic clustering that determines the number of clusters and
 * thresholds based on distance statistics.</li>
 * <li>{@link #newGreedy(DistanceMeasure, double)}: Greedy, single-pass clustering using a distance
 * threshold.</li>
 * <li>{@link #newKMeans(DistanceMeasure, int)}: K-means style clustering with a specified number of
 * clusters.</li>
 * <li>{@link #newSpectral(DistanceMeasure, int)}: Spectral clustering using a Gaussian kernel and Laplacian
 * embedding.</li>
 * </ul>
 * <p>
 * <b>Performance:</b> Internally, distances are cached for efficiency. All clustering is performed on
 * {@link Point} objects with unique ids and float[] coordinates.
 * <p>
 * <b>Extensibility:</b> Subclasses implement {@link #cluster(Collection)} to provide concrete clustering
 * strategies over {@link Point}s.
 * <p>
 * <b>Thread safety:</b> Not thread-safe. Each instance maintains internal state for distance caching.
 *
 * @author apete
 */
public abstract class FeatureBasedClusterer implements ClusteringAlgorithm<Point> {

    /**
     * Returns a new automatic clusterer using squared Euclidean distance. Equivalent to
     * {@link #newAutomatic(DistanceMeasure)} with {@link DistanceMeasure#SQUARED_EUCLIDEAN}.
     *
     * @return a new automatic clusterer
     */
    public static FeatureBasedClusterer newAutomatic() {
        return new AutomaticClusterer(DistanceMeasure.SQUARED_EUCLIDEAN);
    }

    /**
     * Returns a new automatic clusterer using the specified distance measure.
     * <p>
     * The algorithm:
     * <ol>
     * <li>Extracts features</li>
     * <li>Caches all pairwise distances</li>
     * <li>Performs statistical analysis to determine a distance threshold</li>
     * <li>Performs greedy clustering to get initial centroids</li>
     * <li>Filters out very small clusters (determining k)</li>
     * <li>Performs k-means clustering to refine clusters and centroids</li>
     * </ol>
     *
     * @param measure the distance measure to use
     * @return a new automatic clusterer
     */
    public static FeatureBasedClusterer newAutomatic(final DistanceMeasure measure) {
        return new AutomaticClusterer(measure);
    }

    /**
     * Returns a new greedy, single-pass clusterer using the supplied distance and threshold.
     * <p>
     * Each item is assigned to the nearest existing centroid if its distance is {@code <= threshold};
     * otherwise a new cluster is created. The threshold must be in the same units as the chosen distance
     * measure.
     *
     * @param measure   the distance measure
     * @param threshold the maximum allowed distance to join an existing cluster
     * @return a new greedy clusterer
     */
    public static FeatureBasedClusterer newGreedy(final DistanceMeasure measure, final double threshold) {
        return new GreedyClusterer(measure, threshold);
    }

    /**
     * Returns a new greedy, single-pass clusterer using squared Euclidean distance and the given threshold.
     *
     * @param threshold the maximum allowed distance to join an existing cluster
     * @return a new greedy clusterer
     */
    public static FeatureBasedClusterer newGreedy(final double threshold) {
        return new GreedyClusterer(DistanceMeasure.SQUARED_EUCLIDEAN, threshold);
    }

    /**
     * Returns a new k-means–style clusterer using the supplied distance measure and number of clusters.
     *
     * @param measure the distance function
     * @param k       the number of clusters (k >= 1)
     * @return a new k-means clusterer
     */
    public static FeatureBasedClusterer newKMeans(final DistanceMeasure measure, final int k) {
        return new KMeansClusterer(k, measure);
    }

    /**
     * Returns a new k-means–style clusterer using squared Euclidean distance and the given number of
     * clusters.
     *
     * @param k the number of clusters (k >= 1)
     * @return a new k-means clusterer
     */
    public static FeatureBasedClusterer newKMeans(final int k) {
        return new KMeansClusterer(k, DistanceMeasure.SQUARED_EUCLIDEAN);
    }

    /**
     * Returns a new spectral clusterer using the supplied distance measure and number of clusters.
     * <p>
     * Uses a Gaussian kernel and the symmetric normalised Laplacian.
     *
     * @param measure the distance measure for the kernel
     * @param k       the number of clusters (k >= 1)
     * @return a new spectral clusterer
     */
    public static FeatureBasedClusterer newSpectral(final DistanceMeasure measure, final int k) {
        return new SpectralClusterer(k, measure);
    }

    /**
     * Returns a new spectral clusterer using squared Euclidean distance and the given number of clusters.
     *
     * @param k the number of clusters (k >= 1)
     * @return a new spectral clusterer
     */
    public static FeatureBasedClusterer newSpectral(final int k) {
        return new SpectralClusterer(k, DistanceMeasure.SQUARED_EUCLIDEAN);
    }

    private final PointDistanceCache myCache = new PointDistanceCache();
    private final DistanceMeasure myMeasure;

    FeatureBasedClusterer(final DistanceMeasure measure) {
        super();
        myMeasure = measure == null ? DistanceMeasure.SQUARED_EUCLIDEAN : measure;
    }

    /**
     * Clusters arbitrary items by first extracting their float feature representation.
     * <p>
     * Each item is wrapped as a {@link Point} using the extractor output. Clustering is then performed by
     * {@link #cluster(Collection)}. The result mirrors the internal clusters but maps back to the original
     * items along with their feature vectors.
     *
     * @param <T>       the item type
     * @param input     the items to cluster (not null)
     * @param extractor a function that returns a non-null float[] feature vector for an item
     * @return a list of clusters, each as a map from the original item to its feature vector, sorted by
     *         decreasing size
     */
    public final <T> List<Map<T, float[]>> cluster(final Collection<T> input, final Function<T, float[]> extractor) {

        Map<Point, T> reverseMap = new HashMap<>(input.size());

        int id = 0;
        for (T item : input) {
            reverseMap.put(new Point(id++, extractor.apply(item)), item);
        }

        List<Set<Point>> internalResult = this.cluster(reverseMap.keySet());
        List<Map<T, float[]>> retVal = new ArrayList<>(internalResult.size());

        for (Set<Point> internalCluster : internalResult) {
            Map<T, float[]> cluster = new HashMap<>(internalCluster.size());

            for (Point internalPoint : internalCluster) {
                T key = reverseMap.get(internalPoint);
                cluster.put(key, internalPoint.coordinates);
            }

            retVal.add(cluster);
        }

        reverseMap.clear();

        retVal.sort(Comparator.comparing(Map<T, float[]>::size).reversed());

        return retVal;
    }

    /**
     * Returns a function that computes the centroid of a collection of points.
     *
     * @return centroid function
     */
    Function<Collection<Point>, Point> centroid() {
        return myCache::centroid;
    }

    /**
     * Returns a function that computes the distance between two points.
     *
     * @return distance function
     */
    ToDoubleBiFunction<Point, Point> distance() {
        return myCache::distance;
    }

    /**
     * Returns the distance between two points.
     *
     * @param point1 first point
     * @param point2 second point
     * @return distance between point1 and point2
     */
    double distance(final Point point1, final Point point2) {
        return myCache.distance(point1, point2);
    }

    /**
     * Returns the median distance threshold used for greedy clustering and initialisation.
     *
     * @return median distance threshold
     */
    double getThreshold() {
        return myCache.getThreshold();
    }

    /**
     * Returns a function that generates an initial set of centroids from the input points.
     *
     * @return initialiser function
     */
    Function<Collection<Point>, List<Point>> initialiser() {
        return myCache::initialiser;
    }

    /**
     * Returns true if the configured distance measure is squared Euclidean.
     *
     * @return true if squared Euclidean, false otherwise
     */
    boolean isSquared() {
        return myMeasure == DistanceMeasure.SQUARED_EUCLIDEAN;
    }

    /**
     * Prepares the internal distance cache for the given input points and distance measure.
     *
     * @param input the points to cache distances for
     */
    void setup(final Collection<Point> input) {
        myCache.setup(input, myMeasure);
    }

}
