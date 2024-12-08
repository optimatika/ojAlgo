package org.ojalgo.data.cluster;

import org.ojalgo.type.NumberDefinition;

public class ClusteringPair<T> implements NumberDefinition {

    private final DistanceCalcularor<T> myDistanceCalcularor;
    private final T myFirst;
    private final T myOther;

    ClusteringPair(final T first, final T other, final DistanceCalcularor<T> distanceCalcularor) {
        super();
        myFirst = first;
        myOther = other;
        myDistanceCalcularor = distanceCalcularor;
    }

    @Override
    public double doubleValue() {
        return myDistanceCalcularor.distance(myFirst, myOther);
    }

}
