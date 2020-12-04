package org.ojalgo.optimisation.convex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;

class MatrixReader {

    private static double[] parseLine(final String line) {
        return Arrays.stream(line.split(",")).mapToDouble(Double::parseDouble).toArray();
    }

    static MatrixStore<Double> readMatrix(final String resourceName) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(GitHubIssue300.class.getResourceAsStream(resourceName)))) {
            final double[][] rows = r.lines().map(MatrixReader::parseLine).toArray(double[][]::new);
            return Primitive64Store.FACTORY.rows(rows).get();
        }
    }

}
