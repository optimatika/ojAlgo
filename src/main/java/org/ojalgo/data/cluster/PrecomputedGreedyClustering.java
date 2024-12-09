package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>
Potential Downsides of Pre-calculating Distances

    1.  Memory Usage: Storing all pairwise distances for  n  points requires  O(n^2)  memory, which can quickly become infeasible for large datasets.
    2.  Overhead of Sorting: Sorting distances adds an initial computational cost. While this may pay off later, the upfront cost might not always justify it, especially for small datasets or algorithms that don’t benefit much from pre-sorted distances.
 * </pre>
 *
 * <pre>
When Sorting Specifically Helps

Sorting distances is especially helpful for algorithms that:
    1.  Have Fixed Neighborhoods: E.g., finding the  k -nearest neighbors.
    2.  Work With Thresholds: E.g., clustering points within a fixed radius.
    3.  Require Greedy Choices: E.g., choosing the nearest cluster or merging the closest clusters.
 * </pre>
 *
 * <pre>
Implementation Strategy

    •   Pre-computation: Store pairwise distances in a matrix or compressed format.
    •   Sorting: Use efficient sorting and indexing to keep track of neighbors.
    •   Lazy Evaluation: For very large datasets, compute and sort distances only when required for a specific point or cluster.
 * </pre>
 *
 * <pre>
Conclusion

Pre-calculated and sorted distances are most beneficial for algorithms that repeatedly query or merge based on pairwise distances. However, consider memory and computational trade-offs, especially for large datasets or algorithms that dynamically modify cluster configurations.
 * </pre>
 */
public class PrecomputedGreedyClustering {

    public static List<Set<Integer>> cluster(final double[][] distances, final double threshold) {
        int n = distances.length;
        boolean[] visited = new boolean[n];
        List<Set<Integer>> clusters = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                Set<Integer> cluster = new HashSet<>();
                cluster.add(i);
                visited[i] = true;

                for (int j = 0; j < n; j++) {
                    if (!visited[j] && distances[i][j] <= threshold) {
                        cluster.add(j);
                        visited[j] = true;
                    }
                }

                clusters.add(cluster);
            }
        }

        return clusters;
    }
}
