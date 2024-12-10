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
public class RandomClustering<T> implements ClusteringAlgorithm<T> {

    private static final Random RANDOM = new Random();

    private final int myK;

    public RandomClustering(final int k) {
        super();
        myK = k;
    }

    @Override
    public List<Set<T>> cluster(final Collection<T> input) {

        Random random = RANDOM;

        List<Set<T>> clusters = new ArrayList<>(myK);
        for (int i = 0; i < myK; i++) {
            clusters.add(new HashSet<>());
        }

        for (T item : input) {
            int index = random.nextInt(myK);
            clusters.get(index).add(item);
        }

        return clusters;
    }
}
