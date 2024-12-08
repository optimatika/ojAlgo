package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.ojalgo.function.constant.PrimitiveMath;

public final class Point implements Comparable<Point> {

    /**
     * Primarily used when constructing test cases and similar. For real world applications you should use
     * {@link Point#convert(List, Function)} instead.
     */
    public static final class Factory {

        private final int myDimensions;
        private final AtomicInteger myNextID = new AtomicInteger();

        public Factory(final int dimensions) {
            super();
            myDimensions = dimensions;
        }

        public Point newPoint(final float... coordinates) {
            if (coordinates.length != myDimensions) {
                throw new IllegalArgumentException();
            }
            return new Point(myNextID.getAndIncrement(), coordinates);
        }

        public void reset() {
            myNextID.set(0);
        }

    }

    /**
     * Essentially works like this:
     * <ol>
     * <li>Calculate, and store, distances between all the points (to enable statistical analysis, and speed
     * up the following steps)
     * <li>Perform statistical analysis of the distances to determine a suitable distance threshold (to get
     * the threshold needed for greedy clustering)
     * <li>Perform greedy clustering to get an initial set of centroids
     * <li>Filter out centroids/clusters corresponding to extremely small clusters (This determines the 'k')
     * <li>Perform k-means clustering to refine the clusters and centroids
     * </ol>
     */
    public static List<Set<Point>> cluster(final Collection<Point> input) {

        PointDistanceCache cache = new PointDistanceCache();
        cache.setup(input, Point::distance);

        GeneralisedKMeans<Point> clusterar = new GeneralisedKMeans<>(cache::initialiser, cache::centroid, cache::distance);

        return clusterar.cluster(input);
    }

    /**
     * Converts a list of objects to a list of points using the provided converter to derive the coordinates.
     * There will be one point for each object in the input list, at matching positions. Further the point id
     * will be the index of the object in the input list.
     *
     * @param <T> The type of the objects in the input list
     * @param input The list of objects to convert
     * @param converter The function to convert the objects to coordinates
     * @return A list of points
     */
    public static <T> List<Point> convert(final List<T> input, final Function<T, float[]> converter) {
        int size = input.size();
        List<Point> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(new Point(i, converter.apply(input.get(i))));
        }
        return points;
    }

    public static Point mean(final Collection<Point> points) {

        double[] sum = null;
        int length = 0;
        for (Point point : points) {
            if (sum == null) {
                length = point.coordinates.length;
                sum = new double[length];
            }
            for (int i = 0; i < length; i++) {
                sum[i] += point.coordinates[i];
            }
        }

        float[] retVal = new float[length];

        for (int i = 0; i < length; i++) {
            retVal[i] = (float) (sum[i] / points.size());
        }

        return new Point(-1, retVal);
    }

    public static Point.Factory newFactory(final int dimensions) {
        return new Point.Factory(dimensions);
    }

    /**
     * Greedy algorithm. The distance measurement is the same as for k-means ({@link #distance(Point)}) and
     * the threshold must match that.
     */
    public static ClusteringAlgorithm<Point> newGreedyClusterer(final double distanceThreshold) {
        return new GreedyClustering<>(Point::mean, Point::distance, distanceThreshold);
    }

    /**
     * Standard k-means clustering
     */
    public static ClusteringAlgorithm<Point> newKMeansClusterer(final int k) {
        RandomClustering<Point> initialiser = new RandomClustering<>(k);
        return new GeneralisedKMeans<>(initialiser::centroids, Point::mean, Point::distance);
    }

    public static Point of(final int id, final float... coordinates) {
        return new Point(id, coordinates);
    }

    public final float[] coordinates;
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
     * The sum of the squared differences between the coordinates of this and the other point. (Not the
     * Euclidean distance. This is the squared Euclidean distance.)
     */
    public double distance(final Point other) {

        double retVal = PrimitiveMath.ZERO;

        int limit = Math.min(coordinates.length, other.coordinates.length);

        float diff;
        for (int i = 0; i < limit; i++) {
            diff = coordinates[i] - other.coordinates[i];
            retVal += diff * diff;
        }

        return retVal;
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
