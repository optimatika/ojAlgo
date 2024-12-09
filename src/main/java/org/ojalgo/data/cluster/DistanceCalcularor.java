package org.ojalgo.data.cluster;

@FunctionalInterface
public interface DistanceCalcularor<T> {

    double distance(T first, T other);

}
