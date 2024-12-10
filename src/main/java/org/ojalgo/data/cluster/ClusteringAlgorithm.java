package org.ojalgo.data.cluster;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ClusteringAlgorithm<T> {

    List<Set<T>> cluster(final Collection<T> input);

}
