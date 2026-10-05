# oj! Algorithms
[![Build Status](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml)
[![CodeQL](https://github.com/optimatika/ojAlgo/workflows/CodeQL/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/codeql-analysis.yml)
[![Maven Central](https://img.shields.io/badge/dynamic/xml?label=Maven%20Central&url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fojalgo%2Fojalgo%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Frelease&color=blue&cacheSeconds=300)](https://central.sonatype.com/artifact/org.ojalgo/ojalgo/versions)

oj! Algorithms - ojAlgo - is Open Source Java code that has to do with mathematics, linear algebra and optimisation.

## High Performance on a Rich Feature Set with Zero Dependencies

- ojAlgo is the fastest pure Java linear algebra library available. That statement is backed by the latest Java Matrix Benchmark results – that’s a third party independent benchmark (not written by anyone associated with ojAlgo). 
- Optimisation (mathematical programming) tools including LP, QP and MIP solvers – again this is pure Java with zero dependencies. There are also integrations with third-party solvers.
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

### Documentation and Support

- [Optimisation Cookbook](https://www.ojalgo.org/optimisation-cookbook/) – complete models to copy and adapt, and the rules every model should follow
- [Documentation](https://www.ojalgo.org/documentation/) – a curated reading order through the articles: optimisation, linear algebra, arrays and data
- [API reference](https://javadoc.io/doc/org.ojalgo/ojalgo) (Javadoc)
- For AI coding assistants: [llms.txt](https://www.ojalgo.org/llms.txt) and the [ojAlgo skill](https://github.com/optimatika/ojAlgo-skills)

ojAlgo is Open Source, and you are strongly encouraged to clone or fork this repository and work directly with the source code. The source code is (part of) the documentation, and you should read it.

All example code (from the blog posts) in a multi-file gist: https://gist.github.com/apete/b3278dc2f8c2db6a00369c211ba321db

Where to ask questions and report bugs is covered in [SUPPORT.md](SUPPORT.md). If you'd like to contribute, see [CONTRIBUTING.md](CONTRIBUTING.md).

### Commercial Support

The [Optimatika subscription](https://www.optimatika.se/subscription/) gives you priority support and third-party solver integrations for ojAlgo.

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
