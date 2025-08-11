module ojalgo {

    requires java.management;
    requires jdk.management;
    requires jdk.unsupported;

    requires transitive java.desktop;
    requires transitive java.net.http;

    exports org.ojalgo;
    exports org.ojalgo.algebra;
    exports org.ojalgo.ann;
    exports org.ojalgo.array;
    exports org.ojalgo.concurrent;
    exports org.ojalgo.data;
    exports org.ojalgo.data.batch;
    exports org.ojalgo.data.cluster;
    exports org.ojalgo.data.domain.finance;
    exports org.ojalgo.data.domain.finance.portfolio;
    exports org.ojalgo.data.domain.finance.portfolio.simulator;
    exports org.ojalgo.data.domain.finance.series;
    exports org.ojalgo.data.image;
    exports org.ojalgo.data.transform;
    exports org.ojalgo.equation;
    exports org.ojalgo.function;
    exports org.ojalgo.function.aggregator;
    exports org.ojalgo.function.constant;
    exports org.ojalgo.function.multiary;
    exports org.ojalgo.function.polynomial;
    exports org.ojalgo.function.series;
    exports org.ojalgo.function.special;
    exports org.ojalgo.machine;
    exports org.ojalgo.matrix;
    exports org.ojalgo.matrix.decomposition;
    exports org.ojalgo.matrix.store;
    exports org.ojalgo.matrix.task;
    exports org.ojalgo.matrix.task.iterative;
    exports org.ojalgo.matrix.transformation;
    exports org.ojalgo.netio;
    exports org.ojalgo.optimisation;
    exports org.ojalgo.optimisation.convex;
    exports org.ojalgo.optimisation.integer;
    exports org.ojalgo.optimisation.linear;
    exports org.ojalgo.optimisation.service;
    exports org.ojalgo.random;
    exports org.ojalgo.random.process;
    exports org.ojalgo.random.scedasticity;
    exports org.ojalgo.scalar;
    exports org.ojalgo.series;
    exports org.ojalgo.series.function;
    exports org.ojalgo.series.primitive;
    exports org.ojalgo.structure;
    exports org.ojalgo.tensor;
    exports org.ojalgo.type;
    exports org.ojalgo.type.collection;
    exports org.ojalgo.type.context;
    exports org.ojalgo.type.format;
    exports org.ojalgo.type.function;
    exports org.ojalgo.type.keyvalue;
    exports org.ojalgo.type.management;
    exports org.ojalgo.type.math;

}
