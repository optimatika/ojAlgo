package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * The K-Means algorithm is a simple and widely used clustering algorithm that groups data into K clusters
 * based on similarity.
 *
 * <pre>
Steps of the K-Means Algorithm:

    1.  Initialization: Randomly initialize  K  cluster centroids.
    2.  Assignment Step: Assign each data point to the nearest cluster centroid.
    3.  Update Step: Recompute the centroids as the mean of the points assigned to each cluster.
    4.  Repeat: Repeat the assignment and update steps until centroids do not change significantly or a maximum number of iterations is reached.
 * </pre>
 *
 * <pre>
Explanation of Code

    1.  Point Class:
    •   Represents a 2D point with methods for calculating Euclidean distance and computing the mean of a list of points.
    2.  kMeansClustering Method:
    •   Initializes centroids randomly.
    •   Assigns points to the nearest centroid.
    •   Updates centroids based on the mean of points in each cluster.
    •   Repeats until convergence or maximum iterations are reached.
    3.  Main Method:
    •   Defines sample points and invokes the clustering function.
    •   Prints the resulting clusters.
 * </pre>
 *
 * This is a simple implementation and assumes 2D points for clarity. It can be extended to higher dimensions
 * or optimized further for larger datasets.
 *
 * <pre>
K-Nearest Neighbors in K-Means Initialization

    •   Why it helps: K-Means++ initialization requires finding the distance of each point to its nearest center. Pre-computed distances allow quick access to this information, speeding up the seeding process.
    •   Impact: Accelerates initialization, which can be critical for large datasets.
 * </pre>
 */
public class KMeans {

    public static <T> List<Set<T>> cluster(final List<T> points, final Function<Collection<T>, T> averageCalculator,
            final DistanceCalcularor<T> distanceCalculator, final int k, final int maxIterations) {

        Random random = new Random();
        List<T> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            centroids.add(points.get(random.nextInt(points.size())));
        }

        List<Set<T>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new HashSet<>());
        }

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Clear clusters
            for (Set<T> cluster : clusters) {
                cluster.clear();
            }

            // Assign points to the nearest centroid
            for (T p : points) {
                int nearestCluster = 0;
                double minDistance = Double.MAX_VALUE;
                for (int i = 0; i < k; i++) {
                    double distance = distanceCalculator.distance(p, centroids.get(i));
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestCluster = i;
                    }
                }
                clusters.get(nearestCluster).add(p);
            }

            // Update centroids
            List<T> newCentroids = new ArrayList<>();
            for (Set<T> cluster : clusters) {
                if (cluster.isEmpty()) {
                    newCentroids.add(centroids.get(clusters.indexOf(cluster))); // Retain old centroid
                } else {
                    newCentroids.add(averageCalculator.apply(cluster));
                }
            }

            // Check for convergence
            if (centroids.equals(newCentroids)) {
                break;
            }
            centroids = newCentroids;
        }

        return clusters;
    }
}
