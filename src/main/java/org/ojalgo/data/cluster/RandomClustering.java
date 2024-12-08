package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Randomly assigns each item to one of k clusters.
 */
final class RandomClustering<T> implements ClusteringAlgorithm<T> {

    private static final Random RANDOM = new Random();

    private final int myK;

    RandomClustering(final int k) {
        super();
        myK = k;
    }

    @Override
    public List<Set<T>> cluster(final Collection<T> input) {

        List<Set<T>> clusters = new ArrayList<>(myK);
        for (int i = 0; i < myK; i++) {
            clusters.add(new HashSet<>());
        }

        for (T item : input) {
            int index = RANDOM.nextInt(myK);
            clusters.get(index).add(item);
        }

        return clusters;
    }

    List<T> centroids(final Collection<T> input) {

        List<T> centroids = new ArrayList<>(myK);

        do {

            centroids.clear();
            List<Set<T>> clusters = this.cluster(input);

            for (Set<T> cluster : clusters) {
                if (!cluster.isEmpty()) {
                    T centroid = cluster.iterator().next();
                    centroids.add(centroid);
                }
            }

        } while (centroids.size() != myK);

        return centroids;
    }
}
