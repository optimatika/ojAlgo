package org.ojalgo.data.cluster;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.NumberList;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.SampleSet;

final class PointDistanceCache {

    private double[][] myDistances;
    private final SampleSet mySampleSet = SampleSet.make();
    private final NumberList<Double> myValues = NumberList.factory(ArrayR064.FACTORY).make();

    PointDistanceCache() {
        super();
    }

    private double distance(final int i, final int j) {
        if (i == j) {
            return PrimitiveMath.ZERO;
        } else if (i < j) {
            return myDistances[j][i];
        } else {
            return myDistances[i][j];
        }
    }

    Point centroid(final Collection<Point> cluster) {

        Point retVal = null;
        double minSum = Double.POSITIVE_INFINITY;

        for (Point candidate : cluster) {

            double sum = PrimitiveMath.ZERO;

            for (Point member : cluster) {
                sum += this.distance(candidate, member);
            }

            if (sum < minSum) {
                minSum = sum;
                retVal = candidate;
            }
        }

        return retVal;
    }

    double distance(final Point point1, final Point point2) {
        return this.distance(point1.id, point2.id);
    }

    double getThreshold() {
        return mySampleSet.swap(myValues).getMedian();
    }

    List<Point> initialiser(final Collection<Point> input) {

        GreedyClustering<Point> greedy = new GreedyClustering<>(this::centroid, this::distance, this.getThreshold());
        List<Set<Point>> clusters = greedy.cluster(input);
        List<Point> centroids = greedy.getCentroids();

        double total = myDistances.length;
        double largest = clusters.stream().mapToInt(Set::size).max().orElse(0);

        return IntStream.range(0, centroids.size()).filter(i -> {
            double size = clusters.get(i).size();
            return size > 1D && size / total > 0.01D && size / largest > 0.02D;
        }).mapToObj(centroids::get).collect(Collectors.toList());
    }

    void setup(final Collection<Point> input, final ToDoubleBiFunction<Point, Point> distanceCalculator) {

        int nbPoints = input.size();

        if (myDistances == null || myDistances.length != nbPoints) {
            myDistances = new double[nbPoints][];
            for (int i = 0; i < nbPoints; i++) {
                myDistances[i] = new double[i];
            }
        }

        myValues.clear();

        for (Point pointR : input) {
            int row = pointR.id;
            for (Point pointC : input) {
                int col = pointC.id;
                if (row > col) {
                    myValues.add(myDistances[row][col] = distanceCalculator.applyAsDouble(pointR, pointC));
                }
            }
        }
    }

}
