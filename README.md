# oj! Algorithms
[![Build Status](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml)
[![CodeQL](https://github.com/optimatika/ojAlgo/workflows/CodeQL/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/codeql-analysis.yml)
[![Maven Central](https://img.shields.io/badge/dynamic/xml?label=Maven%20Central&url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fojalgo%2Fojalgo%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Frelease&color=blue&cacheSeconds=300)](https://central.sonatype.com/artifact/org.ojalgo/ojalgo/versions)

Linear algebra and optimisation for Java — LP, QP and MIP solvers, matrices, and
a solver-agnostic modelling layer. One dependency, no native libraries, no
licence server.

```xml
<dependency>
    <groupId>org.ojalgo</groupId>
    <artifactId>ojalgo</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

Maximise `143·wheat + 60·barley`, subject to three constraints:

```java
ExpressionsBasedModel model = new ExpressionsBasedModel();

Variable wheat = model.newVariable("wheat").lower(0).weight(143);
Variable barley = model.newVariable("barley").lower(0).weight(60);

model.newExpression("budget").upper(15000).add(wheat, 120).add(barley, 210);
model.newExpression("storage").upper(4000).add(wheat, 110).add(barley, 30);
model.newExpression("acreage").upper(75).add(wheat, 1).add(barley, 1);

Optimisation.Result result = model.maximise();

System.out.println(result);
System.out.println("wheat: " + result.get(model.indexOf(wheat)));
```

That is the whole setup — no solver to install, no bindings to compile.

## What is in it

- **Optimisation** — LP, QP and MIP solvers, and `ExpressionsBasedModel`, a
  modelling layer that is not tied to any particular solver.
- **Linear algebra** — dense and sparse matrices and decompositions, built on
  array classes that can be 1-, 2- or N-dimensional, allocated on heap, off heap
  or in a file, holding anything from `double` to complex numbers, rational
  numbers and quaternions.
- **Data science** — artificial neural networks, clustering, and tools for
  reading, writing and processing data.
- **Other** — time series, random numbers, stochastic processes, descriptive
  statistics.

Zero dependencies, MIT licensed, in continuous development since 2003.

## Pure Java, and what that is worth

ojAlgo is the fastest pure-Java linear algebra library available, according to
the [Java Matrix Benchmark](https://lessthanoptimal.github.io/Java-Matrix-Benchmark/)
— a third-party benchmark not written by anyone associated with the project.

The distinction that matters is *pure Java*. Libraries such as OR-Tools expose a
Java API, but it is a JNI wrapper over native binaries, so you inherit the
native build, the platform matrix and the packaging. ojAlgo has no native code
at all, and deploys wherever a JVM does.

Against commercial native solvers — CPLEX, Gurobi, Mosek — ojAlgo is slower on
large models. The
[mathematical programming benchmark](https://github.com/optimatika/ojAlgo-mathematical-programming-benchmark)
publishes where and by how much, including the cases ojAlgo loses; it runs
Netlib, Maros–Meszaros and MIPLIB 2017, with code and raw results in the open.

## When you outgrow it

Models are written against `ExpressionsBasedModel`, which is deliberately not
tied to a solver. The model you write against ojAlgo's own solvers is the same
model you hand to a stronger one later — the code describing the problem does
not change.

The [Optimatika Optimisation Service](https://optimatika.se/optimisation-service/)
is the commercial route for that step: a container you deploy in your own
infrastructure, configured and tuned to solve linear, quadratic and mixed-integer
problems at a scale pure Java is not meant for, with the building, packaging and
operating already handled. Develop locally against pure Java; solve remotely when
the problem outgrows it.

## Documentation and support

- **API** — [javadoc.io/doc/org.ojalgo/ojalgo/latest](https://javadoc.io/doc/org.ojalgo/ojalgo/latest)
- **Articles and examples** — [ojalgo.org](https://www.ojalgo.org/)
- **Example code** from the articles, as a
  [multi-file gist](https://gist.github.com/apete/b3278dc2f8c2db6a00369c211ba321db)
- **Changelog** — [CHANGELOG.md](CHANGELOG.md)

Questions are best asked on
[Stack Overflow](https://stackoverflow.com/search?tab=relevance&q=ojalgo) tagged
`ojalgo`, or in
[Discussions](https://github.com/optimatika/ojAlgo/discussions). Bugs and issues
with existing code belong in
[Issues](https://github.com/optimatika/ojAlgo/issues).

ojAlgo is open source, and you are encouraged to clone or fork this repository
and work with the source directly. The source is part of the documentation.

## Who maintains it

ojAlgo is developed and maintained by [Optimatika](https://optimatika.se), a
Swedish company that has been building numerical software since 1997. ojAlgo
itself has been in continuous development since 2003.

It is MIT licensed, so the code you have already shipped keeps working — that is
a property of the licence, not a promise. What a licence cannot give you is help
using it well, and that is what
[ojAlgo Support](https://optimatika.se/ojalgo-support/) exists to provide.
