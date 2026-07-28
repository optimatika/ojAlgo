/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
