# oj! Algorithms
[![Build Status](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/maven.yml)
[![CodeQL](https://github.com/optimatika/ojAlgo/workflows/CodeQL/badge.svg)](https://github.com/optimatika/ojAlgo/actions/workflows/codeql-analysis.yml)
[![Maven Central](https://img.shields.io/badge/dynamic/xml?label=Maven%20Central&url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fojalgo%2Fojalgo%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Frelease&color=blue&cacheSeconds=300)](https://central.sonatype.com/artifact/org.ojalgo/ojalgo/versions)

oj! Algorithms - ojAlgo - is Open Source Java code that has to do with mathematics, linear algebra and optimisation.

## High Performance on a Rich Feature Set with Zero Dependencies

- ojAlgo is the fastest pure Java linear algebra library available. That statement is backed by the latest Java Matrix Benchmark results – that’s a third party independent benchmark (not written by anyone associated with ojAlgo). 
- Optimisation (mathematical programming) tools including LP, QP and MIP solvers – again this is pure Java with zero dependencies. For models that outgrow them there are integrations with third-party solvers, and the Optimatika Optimisation Service.
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

Programming questions related to ojAlgo are best asked at [stack overflow](https://stackoverflow.com/search?tab=relevance&q=ojalgo). Just remember to actually mention ojAlgo and tag the question using 'ojalgo' and whatever other tags you find suitable.

Bug reports, or any issue with existing code, should be posted at GitHub: https://github.com/optimatika/ojAlgo/issues

https://github.com/optimatika/ojAlgo/discussions may be used to discuss anything related to ojAlgo.

ojAlgo is Open Source, and you are strongly encouraged to clone or fork this repository and work directly with the source code. The source code is (part of) the documentation, and you should read it.

All example code (from the blog posts) in a multi-file gist: https://gist.github.com/apete/b3278dc2f8c2db6a00369c211ba321db

Commercial support for ojAlgo is available from [Optimatika](https://www.optimatika.se/), the company behind the project.

## When the Built-In Solvers Are Not Enough

The built-in solvers are pure Java with zero dependencies, and they handle the great majority of the problems put to them. Some models do eventually outgrow them, and there are no pure Java solvers stronger than ojAlgo's own. There are two ways forward, and neither requires changing your model code — the `ExpressionsBasedModel` you have already written stays as it is.

**Third-party solver integrations.** ojAlgo has integrations with a number of external solvers, covering both the leading open source alternatives and the major commercial ones — Gurobi, CPLEX and MOSEK among them. You bring the solvers and, where applicable, the licences; the integration connects them to `ExpressionsBasedModel`. The native libraries then run inside your own application process, which is what makes this the more demanding option to deploy and maintain.

**The Optimatika Optimisation Service.** A solver server you deploy in your own cluster, from the AWS, Azure or Google Cloud marketplace. A small pure Java client configures `ExpressionsBasedModel` to solve through it, so no native code enters your application. It is deliberately the middle ground: more capable than the built-in pure Java solvers, less costly and far less work than licensing, packaging and operating the commercial alternatives. What you get is a suite of the strongest open source solvers, built, tuned and tested to work as one, with the right one picked for each problem. Usage-based pricing, billed through the marketplace. See [optimatika.se](https://www.optimatika.se/optimisation-service/).

ojAlgo itself remains Open Source and free to use, and it always will be. These are options for models that need more, not a paywall on anything that works today.
