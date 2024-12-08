package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

import org.ojalgo.type.context.NumberContext;

/**
 * Contains the outline of the k-means algorithm, but designed for customisation.
 * <ul>
 * <li>Works with any type of data
 * <li>Allows for custom distance calculations
 * <li>Allows for custom centroid initialisation and updating
 * </ul>
 */
public final class GeneralisedKMeans<T> implements ClusteringAlgorithm<T> {

    private static final NumberContext ACCURACY = NumberContext.of(4);

    private final Function<Collection<T>, T> myCentroidUpdater;
    private final ToDoubleBiFunction<T, T> myDistanceCalculator;
    private final Function<Collection<T>, List<T>> myCentroidInitialiser;

    /**
     * You have to configure how distances are measured and how centroids are derived.
     *
     * @param centroidInitialiser The initialisation function should return a list of k centroids. This
     *        function determines 'K'.
     * @param centroidUpdater The update function should return a new centroid based on a collection of points
     *        (the set of items in a cluster).
     * @param distanceCalculator A function that calculates the distance between two points.
     */
    public GeneralisedKMeans(final Function<Collection<T>, List<T>> centroidInitialiser,
            final Function<Collection<T>, T> centroidUpdater,
            final ToDoubleBiFunction<T, T> distanceCalculator) {

        super();

        myCentroidInitialiser = centroidInitialiser;
        myCentroidUpdater = centroidUpdater;
        myDistanceCalculator = distanceCalculator;
    }

    @Override
    public List<Set<T>> cluster(final Collection<T> input) {

        List<T> centroids = myCentroidInitialiser.apply(input);

        int k = centroids.size();
        int maxIterations = Math.max(5, Math.min((int) Math.round(Math.sqrt(input.size())), 50));

        List<Set<T>> clusters = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            clusters.add(i, new HashSet<>());
        }

        int iterations = 0;
        boolean converged = false;
        do {

            converged = true;

            for (Set<T> cluster : clusters) {
                cluster.clear();
            }

            for (T point : input) {

                int bestCluster = 0;
                double minDistance = Double.MAX_VALUE;

                for (int i = 0; i < k; i++) {
                    double distance = myDistanceCalculator.applyAsDouble(centroids.get(i), point);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestCluster = i;
                    }
                }

                clusters.get(bestCluster).add(point);
            }

            Set<T> cluster;
            T oldCenter;
            T newCenter;
            for (int i = 0; i < k; i++) {

                cluster = clusters.get(i);

                if (!cluster.isEmpty()) {

                    oldCenter = centroids.get(i);
                    newCenter = myCentroidUpdater.apply(cluster);

                    converged &= ACCURACY.isZero(myDistanceCalculator.applyAsDouble(oldCenter, newCenter));

                    centroids.set(i, newCenter);
                }
            }

        } while (++iterations < maxIterations && !converged);

        return clusters;
    }

}
