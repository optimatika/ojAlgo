package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class GeneralisedKMeansClustering<T> implements ClusteringAlgorithm<T> {

    private final Function<Collection<T>, T> myAverageCalculator;
    private final DistanceCalcularor<T> myDistanceCalculator;
    private final int myK;
    private final int myMaxIterations;
    private final ClusteringAlgorithm<T> myInitialiser;

    public GeneralisedKMeansClustering(final ClusteringAlgorithm<T> initialiser, final int k, final Function<Collection<T>, T> averageCalculator,
            final DistanceCalcularor<T> distanceCalculator, final int maxIterations) {

        super();

        myInitialiser = initialiser;
        myK = k;
        myAverageCalculator = averageCalculator;
        myDistanceCalculator = distanceCalculator;
        myMaxIterations = maxIterations;
    }

    @Override
    public List<Set<T>> cluster(final Collection<T> input) {

        List<Set<T>> clusters = myInitialiser.cluster(input);

        int k = clusters.size();

        List<T> centroids = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            centroids.add(myAverageCalculator.apply(clusters.get(i)));
        }

        for (int iter = 0; iter < myMaxIterations; iter++) {

            for (Set<T> cluster : clusters) {
                cluster.clear();
            }

            for (T point : input) {

                int bestCluster = 0;
                double minDistance = Double.MAX_VALUE;

                for (int i = 0; i < k; i++) {
                    double distance = myDistanceCalculator.distance(point, centroids.get(i));
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestCluster = i;
                    }
                }

                clusters.get(bestCluster).add(point);
            }

            boolean converged = true;
            Set<T> cluster;
            T oldAvg;
            T newAvg;
            for (int i = 0; i < k; i++) {

                cluster = clusters.get(i);

                if (!cluster.isEmpty()) {

                    oldAvg = centroids.get(i);
                    newAvg = myAverageCalculator.apply(cluster);

                    converged &= myDistanceCalculator.distance(oldAvg, newAvg) < 1e-6;

                    centroids.set(i, newAvg);
                }
            }

            if (converged) {
                break;
            }
        }

        return clusters;
    }

}
