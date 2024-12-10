package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Greedy clustering is a simpler alternative to K-Means. Instead of globally optimizing the cluster
 * assignments, it incrementally builds clusters by adding points to the nearest compatible cluster. This
 * approach can be faster and easier to implement, especially for scenarios where the number of clusters is
 * not predefined.
 *
 * <pre>
Steps of Greedy Clustering

    1.  Initialization: Start with an empty list of clusters.
    2.  Iterate Over Points: For each point:
    •   Assign it to the nearest cluster if the point satisfies a certain condition (e.g., distance threshold).
    •   Otherwise, create a new cluster with the point as its first member.
    3.  Output Clusters: Return the list of clusters.
 * </pre>
 *
 * <pre>
Explanation of Code

    1.  Point Class:
    •   Represents a 2D point and provides a method for computing Euclidean distance.
    2.  Greedy Clustering Method:
    •   Iterates through each point and tries to assign it to an existing cluster based on a distance threshold.
    •   If no cluster satisfies the condition, a new cluster is created.
    3.  Main Method:
    •   Defines sample points, sets a distance threshold, and invokes the clustering function.
    •   Prints the resulting clusters.
 * </pre>
 *
 * <pre>
Advantages of Greedy Clustering

    1.  Simplicity: Easy to implement and understand.
    2.  Efficiency: Works well for small to medium datasets, especially when the number of clusters isn’t predefined.
    3.  Flexibility: Allows for custom conditions (e.g., distance thresholds) to define clusters.

Disadvantages

    1.  Local Decisions: It may not yield globally optimal clusters.
    2.  Sensitivity: The algorithm is sensitive to the choice of the distance threshold.
    3.  Order-Dependence: The results depend on the order in which points are processed.
 * </pre>
 *
 * This approach is suitable for quick clustering tasks, especially when you need simplicity or don’t know the
 * number of clusters in advance.
 *
 * <pre>
Greedy Clustering

    •   Why it helps: Greedy clustering assigns points to the nearest cluster. With pre-calculated distances, you can directly assign points based on sorted distances without recalculating each time.
    •   Impact: Can make the algorithm more efficient for large datasets with many clusters.
 * </pre>
 */
public class GreedyClustering<T> implements ClusteringAlgorithm<T> {

    public static <T> List<Set<T>> cluster(final Collection<T> points, final DistanceCalcularor<T> distanceCalculator, final double threshold) {

        List<Set<T>> clusters = new ArrayList<>();

        for (T point : points) {
            boolean addedToCluster = false;

            // Try to add the point to an existing cluster
            for (Set<T> cluster : clusters) {
                for (T member : cluster) {
                    if (distanceCalculator.distance(point, member) <= threshold) {
                        cluster.add(point);
                        addedToCluster = true;
                        break;
                    }
                }
                if (addedToCluster) {
                    break;
                }
            }

            // If no suitable cluster is found, create a new one
            if (!addedToCluster) {
                Set<T> newCluster = new HashSet<>();
                newCluster.add(point);
                clusters.add(newCluster);
            }
        }

        return clusters;
    }

    @Override
    public List<Set<T>> cluster(final Collection<T> input) {
        // TODO Auto-generated method stub
        return null;
    }
}
