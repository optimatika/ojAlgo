package org.ojalgo.data.cluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class MoreClusterTests extends ClusterTests {

    static final class Item {
        final int id;
        final double x, y;

        Item(final int id, final double x, final double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    @Test
    public void testAutomaticOnIdenticalPoints() {
        Point.Factory f = Point.newFactory();
        List<Point> pts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            pts.add(f.newPoint(1f, 1f));
        }
        List<Set<Point>> clusters = FeatureBasedClusterer.newAutomatic().cluster(pts);
        // Should yield a single cluster with all points
        TestUtils.assertEquals(1, clusters.size());
        TestUtils.assertEquals(10, clusters.get(0).size());

        if (DEBUG) {
            clusters.sort(Comparator.comparing(Set<Point>::size).reversed());
            this.printClusters("auto-identical", clusters);
        }
    }

    @Test
    public void testFeatureMappingAndSorting() {
        // 3 near origin, 1 far away
        List<Item> items = new ArrayList<>();
        items.add(new Item(0, 0.0, 0.1));
        items.add(new Item(1, -0.1, 0.0));
        items.add(new Item(2, 0.1, -0.1));
        items.add(new Item(3, 10.0, 10.0));

        Function<Item, float[]> extractor = it -> new float[] { (float) it.x, (float) it.y };

        // Use greedy with a squared Euclidean threshold tuned to join the near points but exclude the far one
        List<Map<Item, float[]>> clusters = FeatureBasedClusterer.newGreedy(1.0).cluster(items, extractor);

        // Sorted by decreasing size
        TestUtils.assertEquals(2, clusters.size());
        TestUtils.assertEquals(3, clusters.get(0).size());
        TestUtils.assertEquals(1, clusters.get(1).size());

        // Features are what the extractor produced
        for (Map.Entry<Item, float[]> e : clusters.get(0).entrySet()) {
            Item it = e.getKey();
            float[] feat = e.getValue();
            Assertions.assertEquals((float) it.x, feat[0]);
            Assertions.assertEquals((float) it.y, feat[1]);
        }
    }

    @Test
    public void testGreedyThresholdExtremes() {
        Point.Factory f = Point.newFactory();
        // 6 distinct points spread out so that with threshold=0 they cannot join
        List<Point> pts = List.of(f.newPoint(0f), f.newPoint(2f), f.newPoint(4f), f.newPoint(100f), f.newPoint(102f), f.newPoint(104f));

        // threshold 0 -> every point forms its own cluster
        List<Set<Point>> singletons = FeatureBasedClusterer.newGreedy(0.0).cluster(pts);
        TestUtils.assertEquals(6, singletons.size());
        int total = singletons.stream().mapToInt(Set::size).sum();
        TestUtils.assertEquals(6, total);

        // very large threshold -> all points in one cluster
        List<Set<Point>> one = FeatureBasedClusterer.newGreedy(1.0e9).cluster(pts);
        TestUtils.assertEquals(1, one.size());
        TestUtils.assertEquals(6, one.get(0).size());
    }

    @Test
    public void testSpectralWhenNLessThanK() {
        Point.Factory f = Point.newFactory();
        List<Point> pts = List.of(f.newPoint(0f, 0f), f.newPoint(1f, 0f), f.newPoint(0f, 1f));

        List<Set<Point>> clusters = FeatureBasedClusterer.newSpectral(5).cluster(pts);
        TestUtils.assertEquals(3, clusters.size());
        for (Set<Point> c : clusters) {
            TestUtils.assertEquals(1, c.size());
        }
    }
}