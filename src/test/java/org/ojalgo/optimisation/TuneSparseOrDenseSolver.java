/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.optimisation;

import static org.ojalgo.function.constant.BigMath.*;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.runner.RunnerException;

/**
 * MacBook Pro (16-inch, 2019) 2021-09-11:
 *
 * <pre>
Benchmark                            (dim)  (sparse)   Mode  Cnt          Score           Error    Units
TuneSparseOrDenseSolver.buildConvex      2      true  thrpt    3  172019368.101 ±   5051940.707  ops/min
TuneSparseOrDenseSolver.buildConvex      2     false  thrpt    3  225435905.418 ±  28491035.769  ops/min
TuneSparseOrDenseSolver.buildConvex      5      true  thrpt    3  163211308.544 ±  24106642.515  ops/min
TuneSparseOrDenseSolver.buildConvex      5     false  thrpt    3  222708944.031 ±  40872651.116  ops/min
TuneSparseOrDenseSolver.buildConvex     10      true  thrpt    3  169834615.796 ±  10650710.285  ops/min
TuneSparseOrDenseSolver.buildConvex     10     false  thrpt    3  233191458.664 ±  14219132.345  ops/min
TuneSparseOrDenseSolver.buildConvex     20      true  thrpt    3  164717099.717 ±   6630443.035  ops/min
TuneSparseOrDenseSolver.buildConvex     20     false  thrpt    3  227746812.485 ±  69381857.105  ops/min
TuneSparseOrDenseSolver.buildConvex     50      true  thrpt    3  145788464.329 ±  14091541.911  ops/min
TuneSparseOrDenseSolver.buildConvex     50     false  thrpt    3  199551987.825 ±   3607984.893  ops/min
TuneSparseOrDenseSolver.buildConvex    100      true  thrpt    3  115914653.768 ±   5731322.885  ops/min
TuneSparseOrDenseSolver.buildConvex    100     false  thrpt    3  153276696.735 ±  26406481.423  ops/min
TuneSparseOrDenseSolver.buildConvex    200      true  thrpt    3   63033961.359 ±   3211439.636  ops/min
TuneSparseOrDenseSolver.buildConvex    200     false  thrpt    3   85211667.889 ±  23915214.261  ops/min
TuneSparseOrDenseSolver.buildConvex    500      true  thrpt    3   38224857.496 ±   2705432.837  ops/min
TuneSparseOrDenseSolver.buildConvex    500     false  thrpt    3   52829734.470 ±   6727733.963  ops/min
TuneSparseOrDenseSolver.buildConvex   1000      true  thrpt    3   24749661.338 ±   3830739.077  ops/min
TuneSparseOrDenseSolver.buildConvex   1000     false  thrpt    3   33811200.849 ±  14617600.502  ops/min
TuneSparseOrDenseSolver.buildLinear      2      true  thrpt    3   60117755.377 ±   8443736.657  ops/min
TuneSparseOrDenseSolver.buildLinear      2     false  thrpt    3    5197439.493 ±   2230785.221  ops/min
TuneSparseOrDenseSolver.buildLinear      5      true  thrpt    3   27303309.187 ±   7001840.770  ops/min
TuneSparseOrDenseSolver.buildLinear      5     false  thrpt    3    1910116.898 ±   1135073.832  ops/min
TuneSparseOrDenseSolver.buildLinear     10      true  thrpt    3   12001672.284 ±   4738726.686  ops/min
TuneSparseOrDenseSolver.buildLinear     10     false  thrpt    3     604212.873 ±    191460.952  ops/min
TuneSparseOrDenseSolver.buildLinear     20      true  thrpt    3    5195888.700 ±   1312703.378  ops/min
TuneSparseOrDenseSolver.buildLinear     20     false  thrpt    3     164337.820 ±     98266.559  ops/min
TuneSparseOrDenseSolver.buildLinear     50      true  thrpt    3     818016.265 ±     40313.095  ops/min
TuneSparseOrDenseSolver.buildLinear     50     false  thrpt    3      53637.444 ±     28477.878  ops/min
TuneSparseOrDenseSolver.buildLinear    100      true  thrpt    3     231604.289 ±     63002.904  ops/min
TuneSparseOrDenseSolver.buildLinear    100     false  thrpt    3       8860.620 ±       373.226  ops/min
TuneSparseOrDenseSolver.buildLinear    200      true  thrpt    3      58612.512 ±     19207.829  ops/min
TuneSparseOrDenseSolver.buildLinear    200     false  thrpt    3       2407.229 ±       166.234  ops/min
TuneSparseOrDenseSolver.buildLinear    500      true  thrpt    3       9170.216 ±       929.161  ops/min
TuneSparseOrDenseSolver.buildLinear    500     false  thrpt    3        463.899 ±        61.248  ops/min
TuneSparseOrDenseSolver.buildLinear   1000      true  thrpt    3       1316.967 ±       221.578  ops/min
TuneSparseOrDenseSolver.buildLinear   1000     false  thrpt    3        109.654 ±         5.298  ops/min
TuneSparseOrDenseSolver.solveConvex      2      true  thrpt    3   14527786.673 ±   2330461.776  ops/min
TuneSparseOrDenseSolver.solveConvex      2     false  thrpt    3   14534321.737 ±    814846.615  ops/min
TuneSparseOrDenseSolver.solveConvex      5      true  thrpt    3    4674717.296 ±     70706.857  ops/min
TuneSparseOrDenseSolver.solveConvex      5     false  thrpt    3    3923546.006 ±    360318.119  ops/min
TuneSparseOrDenseSolver.solveConvex     10      true  thrpt    3    1283921.493 ±     33484.452  ops/min
TuneSparseOrDenseSolver.solveConvex     10     false  thrpt    3    1737296.950 ±    128423.547  ops/min
TuneSparseOrDenseSolver.solveConvex     20      true  thrpt    3     343810.148 ±     64148.545  ops/min
TuneSparseOrDenseSolver.solveConvex     20     false  thrpt    3     240976.548 ±     19747.574  ops/min
TuneSparseOrDenseSolver.solveConvex     50      true  thrpt    3      43497.051 ±      1511.039  ops/min
TuneSparseOrDenseSolver.solveConvex     50     false  thrpt    3      16823.027 ±       381.753  ops/min
TuneSparseOrDenseSolver.solveConvex    100      true  thrpt    3       7786.887 ±       312.523  ops/min
TuneSparseOrDenseSolver.solveConvex    100     false  thrpt    3       1951.109 ±       163.734  ops/min
TuneSparseOrDenseSolver.solveConvex    200      true  thrpt    3        666.187 ±        65.083  ops/min
TuneSparseOrDenseSolver.solveConvex    200     false  thrpt    3        332.643 ±        73.673  ops/min
TuneSparseOrDenseSolver.solveConvex    500      true  thrpt    3         93.128 ±         8.289  ops/min
TuneSparseOrDenseSolver.solveConvex    500     false  thrpt    3         14.915 ±         3.127  ops/min
TuneSparseOrDenseSolver.solveConvex   1000      true  thrpt    3          1.880 ±         0.204  ops/min
TuneSparseOrDenseSolver.solveConvex   1000     false  thrpt    3          3.383 ±         0.247  ops/min
TuneSparseOrDenseSolver.solveLinear      2      true  thrpt    3  366933780.498 ±  99076729.934  ops/min
TuneSparseOrDenseSolver.solveLinear      2     false  thrpt    3  374020230.104 ± 203115833.335  ops/min
TuneSparseOrDenseSolver.solveLinear      5      true  thrpt    3  329790448.957 ± 196228028.290  ops/min
TuneSparseOrDenseSolver.solveLinear      5     false  thrpt    3  336540211.947 ±  48860685.864  ops/min
TuneSparseOrDenseSolver.solveLinear     10      true  thrpt    3  208184855.823 ±  79025513.149  ops/min
TuneSparseOrDenseSolver.solveLinear     10     false  thrpt    3  296752086.998 ± 154179559.273  ops/min
TuneSparseOrDenseSolver.solveLinear     20      true  thrpt    3  136881408.778 ±  66263807.151  ops/min
TuneSparseOrDenseSolver.solveLinear     20     false  thrpt    3  235548335.526 ±  75617123.278  ops/min
TuneSparseOrDenseSolver.solveLinear     50      true  thrpt    3   74595407.045 ±   6977436.696  ops/min
TuneSparseOrDenseSolver.solveLinear     50     false  thrpt    3  124951498.857 ±   7804672.518  ops/min
TuneSparseOrDenseSolver.solveLinear    100      true  thrpt    3   43872140.381 ±  16168121.568  ops/min
TuneSparseOrDenseSolver.solveLinear    100     false  thrpt    3   76677255.216 ±   6184738.399  ops/min
TuneSparseOrDenseSolver.solveLinear    200      true  thrpt    3   18520030.199 ±    888215.677  ops/min
TuneSparseOrDenseSolver.solveLinear    200     false  thrpt    3   43587563.359 ±   6354032.146  ops/min
TuneSparseOrDenseSolver.solveLinear    500      true  thrpt    3    9246141.969 ±   1993920.215  ops/min
TuneSparseOrDenseSolver.solveLinear    500     false  thrpt    3   19476576.889 ±    898476.340  ops/min
TuneSparseOrDenseSolver.solveLinear   1000      true  thrpt    3    4629237.693 ±   1089890.534  ops/min
TuneSparseOrDenseSolver.solveLinear   1000     false  thrpt    3    8972129.799 ±   1287247.259  ops/min
 * </pre>
 *
 * 2021-09-12:
 *
 * <pre>
Benchmark                            (dim)  (sparse)   Mode  Cnt          Score          Error    Units
TuneSparseOrDenseSolver.buildConvex      2      true  thrpt    3   16733439.019 ±   849125.248  ops/min
TuneSparseOrDenseSolver.buildConvex      2     false  thrpt    3   20035196.560 ±   380535.329  ops/min
TuneSparseOrDenseSolver.buildConvex      5      true  thrpt    3    9696292.378 ±   631139.779  ops/min
TuneSparseOrDenseSolver.buildConvex      5     false  thrpt    3   13019361.014 ±  6646133.357  ops/min
TuneSparseOrDenseSolver.buildConvex     10      true  thrpt    3    6817160.372 ±   175232.212  ops/min
TuneSparseOrDenseSolver.buildConvex     10     false  thrpt    3    6639465.792 ±  1101924.793  ops/min
TuneSparseOrDenseSolver.buildConvex     20      true  thrpt    3    3813266.670 ±    22054.660  ops/min
TuneSparseOrDenseSolver.buildConvex     20     false  thrpt    3    3447649.152 ±   386600.315  ops/min
TuneSparseOrDenseSolver.buildConvex     50      true  thrpt    3    1424142.265 ±    68108.597  ops/min
TuneSparseOrDenseSolver.buildConvex     50     false  thrpt    3    1365491.166 ±    56066.632  ops/min
TuneSparseOrDenseSolver.buildConvex    100      true  thrpt    3     732375.537 ±    24184.048  ops/min
TuneSparseOrDenseSolver.buildConvex    100     false  thrpt    3     694645.909 ±   370542.781  ops/min
TuneSparseOrDenseSolver.buildConvex    200      true  thrpt    3     298753.584 ±    27956.387  ops/min
TuneSparseOrDenseSolver.buildConvex    200     false  thrpt    3     300005.901 ±    23335.929  ops/min
TuneSparseOrDenseSolver.buildConvex    500      true  thrpt    3     116545.376 ±     9387.457  ops/min
TuneSparseOrDenseSolver.buildConvex    500     false  thrpt    3     119211.270 ±    10147.195  ops/min
TuneSparseOrDenseSolver.buildConvex   1000      true  thrpt    3      51684.123 ±     3705.158  ops/min
TuneSparseOrDenseSolver.buildConvex   1000     false  thrpt    3      53303.688 ±     1491.762  ops/min
TuneSparseOrDenseSolver.buildLinear      2      true  thrpt    3   25499875.752 ±  1510103.708  ops/min
TuneSparseOrDenseSolver.buildLinear      2     false  thrpt    3   29037182.482 ±  2498971.580  ops/min
TuneSparseOrDenseSolver.buildLinear      5      true  thrpt    3   15534113.608 ±  1321725.266  ops/min
TuneSparseOrDenseSolver.buildLinear      5     false  thrpt    3   16729241.754 ±  1220447.259  ops/min
TuneSparseOrDenseSolver.buildLinear     10      true  thrpt    3    9816706.117 ±   429942.224  ops/min
TuneSparseOrDenseSolver.buildLinear     10     false  thrpt    3    9935737.340 ±   643471.165  ops/min
TuneSparseOrDenseSolver.buildLinear     20      true  thrpt    3    5250134.859 ±   204064.545  ops/min
TuneSparseOrDenseSolver.buildLinear     20     false  thrpt    3    5184449.985 ±   119511.661  ops/min
TuneSparseOrDenseSolver.buildLinear     50      true  thrpt    3    2098872.544 ±    44105.010  ops/min
TuneSparseOrDenseSolver.buildLinear     50     false  thrpt    3    2344133.419 ±   296497.936  ops/min
TuneSparseOrDenseSolver.buildLinear    100      true  thrpt    3    1052241.249 ±    67649.339  ops/min
TuneSparseOrDenseSolver.buildLinear    100     false  thrpt    3    1122179.817 ±    74759.539  ops/min
TuneSparseOrDenseSolver.buildLinear    200      true  thrpt    3     515615.504 ±    78335.805  ops/min
TuneSparseOrDenseSolver.buildLinear    200     false  thrpt    3     559865.996 ±    58875.528  ops/min
TuneSparseOrDenseSolver.buildLinear    500      true  thrpt    3     217407.818 ±     1839.589  ops/min
TuneSparseOrDenseSolver.buildLinear    500     false  thrpt    3     222170.494 ±    10435.630  ops/min
TuneSparseOrDenseSolver.buildLinear   1000      true  thrpt    3     107370.299 ±     8978.671  ops/min
TuneSparseOrDenseSolver.buildLinear   1000     false  thrpt    3     113738.764 ±     8039.863  ops/min
TuneSparseOrDenseSolver.solveConvex      2      true  thrpt    3   13882200.791 ±  1202078.106  ops/min
TuneSparseOrDenseSolver.solveConvex      2     false  thrpt    3   13946844.066 ±   326286.097  ops/min
TuneSparseOrDenseSolver.solveConvex      5      true  thrpt    3    4565478.581 ±  1384658.829  ops/min
TuneSparseOrDenseSolver.solveConvex      5     false  thrpt    3    3020538.356 ±   214433.535  ops/min
TuneSparseOrDenseSolver.solveConvex     10      true  thrpt    3    1537996.481 ±   130687.266  ops/min
TuneSparseOrDenseSolver.solveConvex     10     false  thrpt    3     855385.853 ±    44152.914  ops/min
TuneSparseOrDenseSolver.solveConvex     20      true  thrpt    3     441084.786 ±    50986.906  ops/min
TuneSparseOrDenseSolver.solveConvex     20     false  thrpt    3     275184.014 ±     8936.829  ops/min
TuneSparseOrDenseSolver.solveConvex     50      true  thrpt    3      43238.084 ±     1024.550  ops/min
TuneSparseOrDenseSolver.solveConvex     50     false  thrpt    3      21703.644 ±     1581.313  ops/min
TuneSparseOrDenseSolver.solveConvex    100      true  thrpt    3       7444.616 ±     1442.452  ops/min
TuneSparseOrDenseSolver.solveConvex    100     false  thrpt    3       2916.356 ±      175.382  ops/min
TuneSparseOrDenseSolver.solveConvex    200      true  thrpt    3       1634.110 ±      111.023  ops/min
TuneSparseOrDenseSolver.solveConvex    200     false  thrpt    3        212.432 ±        5.293  ops/min
TuneSparseOrDenseSolver.solveConvex    500      true  thrpt    3         86.363 ±        3.005  ops/min
TuneSparseOrDenseSolver.solveConvex    500     false  thrpt    3          9.084 ±        1.266  ops/min
TuneSparseOrDenseSolver.solveConvex   1000      true  thrpt    3          0.982 ±        0.030  ops/min
TuneSparseOrDenseSolver.solveConvex   1000     false  thrpt    3          7.568 ±        1.847  ops/min
TuneSparseOrDenseSolver.solveLinear      2      true  thrpt    3  387059314.725 ± 43375779.164  ops/min
TuneSparseOrDenseSolver.solveLinear      2     false  thrpt    3  367235344.847 ± 21244550.709  ops/min
TuneSparseOrDenseSolver.solveLinear      5      true  thrpt    3  374572812.687 ± 24117519.142  ops/min
TuneSparseOrDenseSolver.solveLinear      5     false  thrpt    3  358885532.288 ± 18612447.250  ops/min
TuneSparseOrDenseSolver.solveLinear     10      true  thrpt    3  341412755.440 ± 13752077.001  ops/min
TuneSparseOrDenseSolver.solveLinear     10     false  thrpt    3  354118616.091 ±  7990689.652  ops/min
TuneSparseOrDenseSolver.solveLinear     20      true  thrpt    3  313991071.634 ± 18706609.211  ops/min
TuneSparseOrDenseSolver.solveLinear     20     false  thrpt    3  324353271.768 ±  5210795.016  ops/min
TuneSparseOrDenseSolver.solveLinear     50      true  thrpt    3  206053038.053 ± 12128860.474  ops/min
TuneSparseOrDenseSolver.solveLinear     50     false  thrpt    3  248014392.584 ±  8592908.375  ops/min
TuneSparseOrDenseSolver.solveLinear    100      true  thrpt    3  148052403.625 ±  8805509.703  ops/min
TuneSparseOrDenseSolver.solveLinear    100     false  thrpt    3  150703583.999 ±  5826040.836  ops/min
TuneSparseOrDenseSolver.solveLinear    200      true  thrpt    3   77630372.539 ± 11388315.557  ops/min
TuneSparseOrDenseSolver.solveLinear    200     false  thrpt    3   92313165.160 ±  5211624.477  ops/min
TuneSparseOrDenseSolver.solveLinear    500      true  thrpt    3   22121815.751 ±  1087177.469  ops/min
TuneSparseOrDenseSolver.solveLinear    500     false  thrpt    3   42568505.732 ±  5142587.647  ops/min
TuneSparseOrDenseSolver.solveLinear   1000      true  thrpt    3   11832352.345 ±  1689935.724  ops/min
TuneSparseOrDenseSolver.solveLinear   1000     false  thrpt    3   24587042.198 ±   242773.744  ops/min
 * </pre>
 *
 * @author apete
 */
@org.openjdk.jmh.annotations.State(Scope.Benchmark)
public class TuneSparseOrDenseSolver extends OptimisationTests {

    /**
     * Random number [0.0%,20.0%)
     */
    private static Uniform UNIFORM_20 = new Uniform(0.0, 0.2);

    public static ExpressionsBasedModel generateModelQP(final int numberOfVariables) {

        Variable[] variables = new Variable[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            variables[i] = Variable.make("V" + Integer.toString(i)).lower(ZERO).weight(-UNIFORM_20.doubleValue());
        }

        ExpressionsBasedModel retVal = new ExpressionsBasedModel(variables);

        Expression exp100P = retVal.addExpression("Balance");
        for (Variable tmpVariable : variables) {
            exp100P.set(tmpVariable, ONE);
        }
        exp100P.level(ONE);

        Expression expVar = retVal.addExpression("Variance");
        for (Variable tmpVariable : variables) {
            expVar.set(tmpVariable, tmpVariable, UNIFORM_20.get());
        }
        expVar.weight(TWO);

        return retVal;
    }

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(TuneSparseOrDenseSolver.class);
    }

    @Param({ "2", "5", "10", "20", "50", "100", "200", "500", "1000" })
    public int dim;

    @Param({ "true", "false" })
    public boolean sparse;

    private ConvexSolver convexSolver;
    private LinearSolver linearSolver;
    private ExpressionsBasedModel model;

    @Benchmark
    public ConvexSolver buildConvex() {
        ConvexSolver.Builder convexBuilder = ConvexSolver.newBuilder();
        ConvexSolver.copy(model, convexBuilder);
        return convexBuilder.build(model.options);
    }

    @Benchmark
    public LinearSolver buildLinear() {
        return LinearSolver.newSolver(model);
    }

    @Setup
    public void setup() {

        model = TuneSparseOrDenseSolver.generateModelQP(dim);
        model.options.sparse = Boolean.valueOf(sparse);

        convexSolver = this.buildConvex();
        linearSolver = this.buildLinear();
    }

    @Benchmark
    public Optimisation.Result solveConvex() {
        return convexSolver.solve();
    }

    @Benchmark
    public Optimisation.Result solveLinear() {
        return linearSolver.solve();
    }

}
