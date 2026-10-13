# oj! Algorithms
[![Build Status](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml)
[![CodeQL](https://github.com/optimatika/ojAlgo/workflows/CodeQL/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/codeql-analysis.yml)

[![Downloads](https://api.scarf.sh/v2/packages/Optimatika/feb3f99a-23d0-4752-8bc2-156bf8a564a3/downloads-badge)](https://central.sonatype.com/artifact/org.ojalgo/ojalgo)
[![Commercial Users](https://api.scarf.sh/v2/packages/Optimatika/feb3f99a-23d0-4752-8bc2-156bf8a564a3/commercial-users-badge)](https://central.sonatype.com/artifact/org.ojalgo/ojalgo)
[![Maven Central](https://img.shields.io/badge/dynamic/xml?label=Maven%20Central&url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fojalgo%2Fojalgo%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Frelease&color=blue&cacheSeconds=300)](https://central.sonatype.com/artifact/org.ojalgo/ojalgo/versions)

oj! Algorithms - ojAlgo - is a pure Java library, with zero dependencies, for mathematical optimisation and linear algebra: LP, QP and MIP solvers, dense and sparse matrices with the standard decompositions, and arrays, statistics and time series to go with them. Open source, MIT licensed, in continuous development since 2003.

## High Performance on a Rich Feature Set with Zero Dependencies

- Optimisation (mathematical programming): LP, QP and MIP solvers with a modelling API, `ExpressionsBasedModel` – pure Java with zero dependencies. The same models can also run on native solvers such as Gurobi, CPLEX, MOSEK, COPT, Xpress, HiGHS, SCIP and CP-SAT, through integration modules on Maven Central.
- Linear algebra: dense and sparse matrices, LU, QR, Cholesky, LDL, singular value and eigenvalue decompositions, equation solvers, least squares and iterative solvers. ojAlgo is the fastest pure Java linear algebra library available. That statement is backed by the latest Java Matrix Benchmark results – that’s a third party independent benchmark (not written by anyone associated with ojAlgo).
- A collection of “array” classes that can be sparse or dense and arbitrarily large. They can be used as 1-, 2- or N/Any-dimensional arrays, and may contain/handle a multitude of different number types including complex numbers, rational numbers and quaternions. The memory for the arrays can alternatively be allocated off heap or in a file. The linear algebra part of ojAlgo builds on these arrays – they’re fast and efficient.
- A growing collection of utilities for data science, including Artificial Neural Networks, clustering and a collection of tools for reading/writing/processing data
- Various other things like time series, random numbers, stochastic processes, descriptive statistics…

General information about ojAlgo is available at the project web site: https://www.ojalgo.org/

### Artifacts

ojAlgo is available at [The Central (Maven) Repository](https://mvnrepository.com/artifact/org.ojalgo/ojalgo) to be used with your favourite dependency management tool. The latest version for each Java version:

| Java | Released | ojAlgo |
|------|----------|--------|
| 22+ | Future | on the `develop` branch |
| 11 | 2026-10-28 | 57.6.1 |
| 8 | 2022-08-26 | 51.4.1 |
| 7 | 2015-08-21 | 37.1.1 |
| 6 | 2011-12-05 | 31.0 |

```xml
<!-- https://mvnrepository.com/artifact/org.ojalgo/ojalgo -->
<dependency>
    <groupId>org.ojalgo</groupId>
    <artifactId>ojalgo</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

The latest version is shown in the Maven Central badge at the top of this page.

### A First Optimisation Model

A chair earns a profit of 45 and a table 80. Decide how many of each to make to maximise total profit, with 400 machine hours available:

```java
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

public class FirstModel {

    public static void main(final String[] args) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        // Variables: bounds, and a weight. The weight is the objective coefficient,
        // here the profit per unit.
        Variable chairs = model.newVariable("Chairs").lower(0).upper(100).weight(45);
        Variable tables = model.newVariable("Tables").lower(0).upper(40).weight(80);

        // A constraint: 2 hours per chair + 5 hours per table <= 400 hours
        Expression hours = model.newExpression("Machine hours").upper(400);
        hours.set(chairs, 2).set(tables, 5);

        Optimisation.Result result = model.maximise();

        if (result.getState().isOptimal()) {
            System.out.println("Profit: " + result.getValue());
            System.out.println("Chairs: " + chairs.getValue().toPlainString());
            System.out.println("Tables: " + tables.getValue().toPlainString());
        }
    }
}
```

The same pattern covers LP, QP and MIP: call `integer()` or `binary()` on a variable for integer models, and set quadratic terms on an expression for quadratic ones. The [Optimisation Cookbook](https://www.ojalgo.org/optimisation-cookbook/) has complete, runnable models for common problem shapes – assignment, bin packing, shift scheduling, blending, facility location, portfolio optimisation and more.

### A First Linear Algebra Example

Solve a system of linear equations, then ask the same matrix for its determinant, eigenvalues and condition number:

```java
import org.ojalgo.matrix.MatrixR064;

public class FirstLinearAlgebra {

    public static void main(final String[] args) {

        MatrixR064 A = MatrixR064.FACTORY.rows(new double[][] { { 4, -2, 1 }, { -2, 4, -2 }, { 1, -2, 4 } });
        MatrixR064 b = MatrixR064.FACTORY.column(11.0, -16.0, 17.0);

        // Solve A x = b
        MatrixR064 x = A.solve(b);
        System.out.println("x = " + x); // [1, -2, 3]

        // The decompositions needed are chosen, computed and reused internally
        System.out.println("Determinant: " + A.getDeterminant()); // 36
        System.out.println("Eigenvalues: " + A.getEigenvalues());
        System.out.println("Condition number: " + A.getCondition());

        // Immutable: every operation returns a new matrix
        MatrixR064 residual = A.multiply(x).subtract(b);
        System.out.println("|Ax - b| = " + residual.norm());
    }
}
```

`MatrixR064` is the place to start: an immutable matrix where every operation returns a new matrix, and the decompositions needed to solve, invert or calculate the determinant, rank, condition number or eigenvalues are chosen, cached and reused for you. The same API exists for other element types: `MatrixR032` (`float`), `MatrixR128` (128-bit floating-point), `MatrixC128` (complex), `MatrixQ128` (rational, exact arithmetic) and `MatrixH256` (quaternion).

For full control – mutable matrices filled and updated in place, reused memory, and a decomposition you choose yourself – use the matrix stores (`R064Store`, `SparseStore`…) and the decompositions (`LU`, `QR`, `Cholesky`, `SingularValue`, `Eigenvalue` and more) directly. The [Linear Algebra Introduction](https://www.ojalgo.org/2026/10/linear-algebra-introduction/) covers both. See also [Linear Algebra](https://www.ojalgo.org/linear-algebra/).

### Optional Native BLAS and LAPACK

> Not yet in a released version – only in the development version, on the `develop` branch.

ojAlgo is pure Java and needs nothing else, but it can also call a native BLAS/LAPACK library – Accelerate on macOS, or for example OpenBLAS on Linux and Windows – for the larger matrix operations and the dense `double` decompositions (`LU`, `Cholesky`, `QR`, `SingularValue`, `Eigenvalue` and more). This is opt-in: native code is used only when the JVM grants ojAlgo native access,

```bash
java --enable-native-access=ALL-UNNAMED ...   # ojAlgo on the class path
java --enable-native-access=ojalgo ...        # ojAlgo on the module path
```

or with `Enable-Native-Access: ALL-UNNAMED` in the manifest of an executable JAR. Note that granting `ALL-UNNAMED` native access, for whatever reason, turns it on.

- With native access, ojAlgo looks for a library: Accelerate on macOS; OpenBLAS, FlexiBLAS, MKL, BLIS or the reference BLAS on Linux; OpenBLAS or MKL on Windows. The system property `ojalgo.native.library` names the library file(s) to use instead, or switches native code off with `none`.
- BLIS and the reference BLAS have no LAPACK, so with them the decompositions stay pure Java – unless `ojalgo.native.library` names a LAPACK library as well (files separated by the platform's path separator).
- `OjAlgoUtils.getNativeLibrary()` tells which library is used, if any. It is also logged when ojAlgo first looks for it.
- Native code is used only for operations large enough to gain from it, and every operation also has a pure Java implementation. Results can differ from the pure Java ones in the last digits.
- Long, compute-heavy calls (large matrix multiplications and decompositions) work on copies of the matrices in native memory, so they don't hold up garbage collection. Other calls use the Java arrays in place.
- The library's own threading is used. OpenBLAS, with its default settings, splits even vector operations such as dot products over all cores, from about 10 000 elements, which can make them much slower. Set `OPENBLAS_NUM_THREADS=1` (or a small number) to avoid that.
- Accelerate's dot product doesn't always give the same last digits from one run to the next.
- The library must use 32-bit integers (the standard LP64 interface). One with 64-bit integers (ILP64) is detected and not used.

### Documentation and Support

- [Optimisation Cookbook](https://www.ojalgo.org/optimisation-cookbook/) – complete models to copy and adapt, and the rules every model should follow
- [Documentation](https://www.ojalgo.org/documentation/) – a curated reading order through the articles: optimisation, linear algebra, arrays and data
- [FAQ](https://www.ojalgo.org/faq/) – common questions, including how ojAlgo compares with OR-Tools, Commons Math and Hipparchus
- [API reference](https://javadoc.io/doc/org.ojalgo/ojalgo) (Javadoc)
- For AI coding assistants: [llms.txt](https://www.ojalgo.org/llms.txt) and the [ojAlgo skills](https://github.com/optimatika/ojAlgo-skills)

ojAlgo is Open Source, and you are strongly encouraged to clone or fork this repository and work directly with the source code. The source code is (part of) the documentation, and you should read it.

All example code (from the blog posts) in a multi-file gist: https://gist.github.com/apete/b3278dc2f8c2db6a00369c211ba321db

Where to ask questions and report bugs is covered in [SUPPORT.md](SUPPORT.md). If you'd like to contribute, see [CONTRIBUTING.md](CONTRIBUTING.md).

### Commercial Support

The [Optimatika subscription](https://www.optimatika.se/subscription/) gives you priority support, access to the source repositories of the solver integrations, and the right to request new builds of them.

## Building from Source

ojAlgo requires Java 22+ and uses the Maven Wrapper, so no separate Maven install is needed.

```bash
git clone https://github.com/optimatika/ojAlgo.git
cd ojAlgo
./mvnw compile              # Compile
./mvnw test                 # Run tests (pure Java)
./mvnw test -DargLine=--enable-native-access=ojalgo,ALL-UNNAMED  # Run tests with native BLAS/LAPACK
./mvnw package -DskipTests  # Build JAR without tests
```

## License

ojAlgo is Open Source, released under the [MIT License](LICENSE).
