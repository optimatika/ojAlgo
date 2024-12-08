package org.ojalgo.data.cluster;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private static class Point {

        double x, y;

        Point(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        double distance(final Point other) {
            return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
        }

        static Point mean(final List<Point> points) {
            double sumX = 0, sumY = 0;
            for (Point p : points) {
                sumX += p.x;
                sumY += p.y;
            }
            return new Point(sumX / points.size(), sumY / points.size());
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    public static List<List<Point>> kMeansClustering(final List<Point> points, final int k, final int maxIterations) {
        Random random = new Random();
        List<Point> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            centroids.add(points.get(random.nextInt(points.size())));
        }

        List<List<Point>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Clear clusters
            for (List<Point> cluster : clusters) {
                cluster.clear();
            }

            // Assign points to the nearest centroid
            for (Point p : points) {
                int nearestCluster = 0;
                double minDistance = Double.MAX_VALUE;
                for (int i = 0; i < k; i++) {
                    double distance = p.distance(centroids.get(i));
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestCluster = i;
                    }
                }
                clusters.get(nearestCluster).add(p);
            }

            // Update centroids
            List<Point> newCentroids = new ArrayList<>();
            for (List<Point> cluster : clusters) {
                if (cluster.isEmpty()) {
                    newCentroids.add(centroids.get(clusters.indexOf(cluster))); // Retain old centroid
                } else {
                    newCentroids.add(Point.mean(cluster));
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

    public static void main(final String[] args) {

        // (1, 1), (2, 1), (4, 3), (5, 4), (8, 7), (7, 8)

        // k = 2

        //    Cluster 1: [(1.0, 1.0), (2.0, 1.0), (4.0, 3.0), (5.0, 4.0)]
        //    Cluster 2: [(8.0, 7.0), (7.0, 8.0)]

        // Sample points
        List<Point> points = List.of(new Point(1, 1), new Point(2, 1), new Point(4, 3), new Point(5, 4), new Point(8, 7), new Point(7, 8));

        int k = 2; // Number of clusters
        int maxIterations = 100;

        List<List<Point>> clusters = KMeans.kMeansClustering(points, k, maxIterations);

        // Print clusters
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ": " + clusters.get(i));
        }
    }
}
