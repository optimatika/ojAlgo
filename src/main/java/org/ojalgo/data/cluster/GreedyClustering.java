package org.ojalgo.data.cluster;


import java.util.ArrayList;
import java.util.List;

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
public class GreedyClustering {

    private static class Point {

        double x, y;

        Point(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Calculates the Euclidean distance between this point and another point.
         *
         * @param other The other point to which the distance is calculated.
         * @return The Euclidean distance between this point and the other point.
         */
        double distance(final Point other) {
            return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    public static List<List<Point>> greedyClustering(final List<Point> points, final double threshold) {
        List<List<Point>> clusters = new ArrayList<>();

        for (Point point : points) {
            boolean addedToCluster = false;

            // Try to add the point to an existing cluster
            for (List<Point> cluster : clusters) {
                for (Point member : cluster) {
                    if (point.distance(member) <= threshold) {
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
                List<Point> newCluster = new ArrayList<>();
                newCluster.add(point);
                clusters.add(newCluster);
            }
        }

        return clusters;
    }

    public static void main(final String[] args) {

        // (1, 1), (2, 1), (10, 10), (11, 11), (2, 2), (12, 10)

        // With a threshold of  2.5 , the output could be:

        //    Cluster 1: [(1.0, 1.0), (2.0, 1.0), (2.0, 2.0)]
        //    Cluster 2: [(10.0, 10.0), (11.0, 11.0), (12.0, 10.0)]

        // Sample points
        List<Point> points = List.of(new Point(1, 1), new Point(2, 1), new Point(10, 10), new Point(11, 11), new Point(2, 2), new Point(12, 10));

        double threshold = 2.5; // Maximum distance for points to be in the same cluster

        List<List<Point>> clusters = GreedyClustering.greedyClustering(points, threshold);

        // Print clusters
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ": " + clusters.get(i));
        }
    }
}
