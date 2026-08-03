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

General information about ojAlgo is available at the project web site: http://ojalgo.org/

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

### Documentation and Support

User documentation is available in the form of blog posts at the ojAlgo web site: http://ojalgo.org/

ojAlgo is Open Source, and you are strongly encouraged to clone or fork this repository and work directly with the source code. The source code is (part of) the documentation, and you should read it.

All example code (from the blog posts) in a multi-file gist: https://gist.github.com/apete/b3278dc2f8c2db6a00369c211ba321db

Where to ask questions and report bugs is covered in [SUPPORT.md](SUPPORT.md). If you'd like to contribute, see [CONTRIBUTING.md](CONTRIBUTING.md).

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
