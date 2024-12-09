package org.ojalgo.data.cluster;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PrecomputedGreedyClusteringTest extends ClusterTests {

    @Test
    void testBasic() {
        // Example distance matrix
        double[][] distances = { { 0, 1, 10, 11 }, { 1, 0, 9, 12 }, { 10, 9, 0, 1 }, { 11, 12, 1, 0 } };

        double threshold = 2.0; // Threshold for clustering
        List<Set<Integer>> clusters = PrecomputedGreedyClustering.cluster(distances, threshold);

        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ": " + clusters.get(i));
        }
    }

}
