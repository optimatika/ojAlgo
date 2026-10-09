# oj! Algorithms
[![Build Status](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml)
[![CodeQL](https://github.com/optimatika/ojAlgo/workflows/CodeQL/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/codeql-analysis.yml)
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

ojAlgo is available at [The Central (Maven) Repository](https://mvnrepository.com/artifact/org.ojalgo/ojalgo) to be used with your favourite dependency management tool.

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

Build a matrix, solve a system of linear equations, then compute the eigenvalues of the same matrix:

```java
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;

public class FirstLinearAlgebra {

    public static void main(final String[] args) {

        int n = 5;

        // A symmetric tridiagonal matrix: 4 on the diagonal, 1 next to it
        PhysicalStore<Double> A = R064Store.FACTORY.make(n, n);
        A.fillDiagonal(4.0);
        A.fillDiagonal(0, 1, 1.0);
        A.fillDiagonal(1, 0, 1.0);

        // A right-hand side of ones
        PhysicalStore<Double> b = R064Store.FACTORY.make(n, 1);
        b.fillAll(1.0);

        // Solve A x = b using an LU decomposition
        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);
        MatrixStore<Double> x = lu.getSolution(b);
        System.out.println("x = " + x);

        // Eigenvalues (and eigenvectors) of the same matrix
        Eigenvalue<Double> evd = Eigenvalue.R064.make(A, true);
        evd.decompose(A);
        System.out.println("Eigenvalues: " + evd.getEigenvalues());
    }
}
```

Matrices are created with the factories and filled in place – there is no need to go through `double[][]`. Declare them with the interfaces `MatrixStore` (read) and `PhysicalStore` (mutable). The decompositions (`LU`, `QR`, `Cholesky`, `SingularValue`, `Eigenvalue` and more) work on any `MatrixStore`. See [Linear Algebra](https://www.ojalgo.org/linear-algebra/).

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

ojAlgo requires Java 11+ and uses the Maven Wrapper, so no separate Maven install is needed.

```bash
git clone https://github.com/optimatika/ojAlgo.git
cd ojAlgo
./mvnw compile              # Compile
./mvnw test                 # Run tests
./mvnw package -DskipTests  # Build JAR without tests
```

## License

ojAlgo is Open Source, released under the [MIT License](LICENSE).
