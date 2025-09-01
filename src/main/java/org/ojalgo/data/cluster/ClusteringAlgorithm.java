/*
 * Copyright 1997-2025 Optimatika
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Strategy interface for clustering algorithms.
 * <p>
 * Implementations partition a collection of items into disjoint, non-empty clusters. Each cluster is
 * represented as a {@code Set<T>} containing the items assigned to that cluster; the result of
 * {@link #cluster(Collection)} is a {@code List<Set<T>>} with one set per cluster. Unless otherwise stated by
 * a specific implementation, the following apply:
 * <ul>
 * <li>Each input item appears in exactly one cluster (no overlap). - The order of clusters and the order of
 * items within a cluster are unspecified.
 * <li>The result contains no empty clusters.
 * <li>Implementations may be deterministic or randomized and may impose additional preconditions on the
 * input.
 * </ul>
 * Thread-safety: Implementations are not required to be thread-safe.
 *
 * @param <T> The element type being clustered.
 */
@FunctionalInterface
public interface ClusteringAlgorithm<T> {

    /**
     * Partitions the given items into clusters.
     *
     * @param input The items to cluster; must not be {@code null}. May be empty.
     * @return A list of clusters; each element of the list is a {@code Set<T>} representing one cluster and
     *         containing its members. Sets are non-empty and pairwise disjoint; ordering is unspecified.
     * @throws IllegalArgumentException If implementation-specific preconditions are violated.
     */
    List<Set<T>> cluster(Collection<T> input);

}