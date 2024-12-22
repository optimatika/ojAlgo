package org.ojalgo.data.cluster;

import static org.ojalgo.function.constant.PrimitiveMath.POSITIVE_INFINITY;
import static org.ojalgo.function.constant.PrimitiveMath.THIRD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

/**
 * Greedy clustering algorithm. Assigns each item to the nearest centroid, creating new centroids as needed.
 * Will only pass through the data once. The centroids are recalculated as the clusters are updated (not with
 * every single update, but continuously during the process).
 */
public final class GreedyClustering<T> implements ClusteringAlgorithm<T> {

    private final List<T> myCentroids = new ArrayList<>();
    private final List<AtomicInteger> myUpdates = new ArrayList<>();
    private final Function<Collection<T>, T> myCentroidUpdater;
    private final ToDoubleBiFunction<T, T> myDistanceCalculator;
    private final double myDistanceThreshold;

    /**
     * @param centroidUpdater The update function should return a new centroid based on a collection of points
     *        (the set of items in a cluster).
     * @param distanceCalculator A function that calculates the distance between two points.
     * @param distanceThreshold The maximum distance between a point and a centroid for the point to be
     *        assigned to that cluster. The points are always assigned to the cluster of the nearest centroid
     *        among the already existing clusters. This threshold determines when a new cluster should be
     *        created.
     */
    public GreedyClustering(final Function<Collection<T>, T> centroidUpdater, final ToDoubleBiFunction<T, T> distanceCalculator,
            final double distanceThreshold) {

        super();

        myCentroidUpdater = centroidUpdater;
        myDistanceCalculator = distanceCalculator;
        myDistanceThreshold = distanceThreshold;
    }

    @Override
    public List<Set<T>> cluster(final Collection<T> input) {

        List<Set<T>> clusters = new ArrayList<>();
        myCentroids.clear();

        for (T point : input) {

            int indexOfBestExisting = -1;
            double minDistance = POSITIVE_INFINITY;

            for (int i = 0; i < myCentroids.size(); i++) {
                T centroid = myCentroids.get(i);

                double distance = myDistanceCalculator.applyAsDouble(point, centroid);
                if (distance <= myDistanceThreshold && distance < minDistance) {
                    minDistance = distance;
                    indexOfBestExisting = i;
                }
            }

            if (indexOfBestExisting >= 0) {

                Set<T> cluster = clusters.get(indexOfBestExisting);
                cluster.add(point);
                int nbMembers = cluster.size();

                AtomicInteger clusterUpdates = myUpdates.get(indexOfBestExisting);
                double nbUpdates = clusterUpdates.incrementAndGet();

                if (nbUpdates / nbMembers >= THIRD) {
                    myCentroids.set(indexOfBestExisting, myCentroidUpdater.apply(cluster));
                    clusterUpdates.set(0);
                }

            } else {

                Set<T> newCluster = new HashSet<>();
                newCluster.add(point);
                clusters.add(newCluster);
                myCentroids.add(point);
                myUpdates.add(new AtomicInteger());
            }
        }

        return clusters;
    }

    List<T> getCentroids() {
        return myCentroids;
    }
}
