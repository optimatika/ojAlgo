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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ojalgo.data.proximity.DistanceMeasure;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;

/**
 * Spectral clustering for {@link Point}s using a fully connected RBF similarity graph and the symmetric
 * normalised Laplacian. Pairwise distances are computed with the configured {@link DistanceMeasure}; the
 * kernel scale is set from the median distance. The algorithm computes the k eigenvectors corresponding to
 * the smallest eigenvalues of L = I - D^{-1/2} W D^{-1/2}, row-normalises the embedding, runs k-means, and
 * maps clusters back to the original points. For n == 0 it returns empty; for n <= k it returns singletons.
 * Requires point ids contiguous in [0,n). Complexity dominated by O(n^2) pairwise work and an
 * eigen-decomposition of the n×n Laplacian.
 */
final class SpectralClusterer extends FeatureBasedClusterer {

    private final int myK;
    private final FeatureBasedClusterer myClusterer;

    SpectralClusterer(final int k, final DistanceMeasure measure) {

        super(measure);

        if (k < 1) {
            throw new IllegalArgumentException("k >= 1");
        }
        myK = k;
        myClusterer = FeatureBasedClusterer.newKMeans(k);
    }

    @Override
    public List<Set<Point>> cluster(final Collection<Point> input) {

        int n = input.size();

        if (n == 0) {
            return List.of();
        } else if (n <= myK) {
            List<Set<Point>> trivial = new ArrayList<>(n);
            for (Point p : input) {
                trivial.add(Set.of(p));
            }
            return trivial;
        }

        Point[] originalById = new Point[n];
        for (Point p : input) {
            int id = p.id;
            if (id >= 0 && id < n) {
                originalById[id] = p;
            } else {
                throw new IllegalArgumentException();
            }
        }

        this.setup(input);

        double median = this.getThreshold();
        double denom = this.isSquared() ? median : median * median;

        double[][] w = new double[n][n];
        double[] deg = new double[n];

        for (int i = 0; i < n; i++) {
            Point pi = originalById[i];

            for (int j = 0; j < i; j++) {
                Point pj = originalById[j];

                double distance = this.distance(pi, pj);
                double dist2 = this.isSquared() ? distance : distance * distance;
                double sim = Math.exp(-dist2 / denom);
                w[i][j] = sim;
                w[j][i] = sim;
                deg[i] += sim;
                deg[j] += sim;
            }
        }

        double[] invSqrtDeg = new double[n];
        for (int i = 0; i < n; i++) {
            invSqrtDeg[i] = deg[i] > ZERO ? ONE / Math.sqrt(deg[i]) : ZERO;
        }
        PhysicalStore<Double> laplacian = R064Store.FACTORY.make(n, n);
        for (int i = 0; i < n; i++) {
            double invDi = invSqrtDeg[i];
            double[] wRow = w[i];
            for (int j = 0; j < n; j++) {
                double val = -wRow[j] * invDi * invSqrtDeg[j];
                if (i == j) {
                    val += ONE;
                }
                laplacian.set(i, j, val);
            }
        }

        Eigenvalue.Spectral<Double> spectral = Eigenvalue.R064.makeSpectral(n);
        spectral.decompose(laplacian);
        MatrixStore<Double> mtrxV = spectral.getV();

        int effectiveK = Math.max(n - spectral.getRank(), myK);

        int start = n - effectiveK;

        float[][] embed = new float[n][effectiveK];
        for (int i = 0; i < n; i++) {
            double norm = ZERO;
            for (int c = 0; c < effectiveK; c++) {
                double v = mtrxV.doubleValue(i, start + c);
                embed[i][c] = (float) v;
                norm += v * v;
            }
            norm = Math.sqrt(norm);
            if (norm > ZERO) {
                float inv = (float) (ONE / norm);
                for (int c = 0; c < effectiveK; c++) {
                    embed[i][c] *= inv;
                }
            }
        }

        List<Point> spectralPoints = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            spectralPoints.add(Point.of(i, embed[i]));
        }

        List<Set<Point>> spectralClusters = myClusterer.cluster(spectralPoints);

        List<Set<Point>> mapped = new ArrayList<>(spectralClusters.size());
        for (Set<Point> spectralCluster : spectralClusters) {
            Set<Point> originalCluster = new HashSet<>(spectralCluster.size());
            for (Point sp : spectralCluster) {
                Point original = originalById[sp.id];
                if (original != null) {
                    originalCluster.add(original);
                } else {
                    throw new IllegalStateException();
                }
            }
            mapped.add(originalCluster);
        }
        return mapped;
    }

}