/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.function.aggregator;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.ProgrammingError;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

public abstract class PrimitiveAggregator {

    public static final ThreadLocal<AggregatorFunction<Double>> CARDINALITY = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private int myCount = 0;

                public double doubleValue() {
                    return myCount;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final double anArg) {
                    if (!TypeUtils.isZero(anArg)) {
                        myCount++;
                    }
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    myCount += result.intValue();
                }

                public Double merge(final Double result1, final Double result2) {
                    return (double) (result1.intValue() + result2.intValue());
                }

                public AggregatorFunction<Double> reset() {
                    myCount = 0;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> LARGEST = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue = Math.max(myValue, Math.abs(anArg));
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return Math.max(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> MAX = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue = Math.max(myValue, anArg);
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return Math.max(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> MIN = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = POSITIVE_INFINITY;

                public double doubleValue() {
                    if (Double.isInfinite(myValue)) {
                        return ZERO;
                    } else {
                        return myValue;
                    }
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue = Math.min(myValue, anArg);
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return Math.min(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = POSITIVE_INFINITY;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> NORM1 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += Math.abs(anArg);
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return Math.abs(result1) + Math.abs(result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> NORM2 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ZERO;

                public double doubleValue() {
                    //return myValue; // more than 100x slower
                    return Math.sqrt(myValue);
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += anArg * anArg;
                    //myValue = Math.hypot(myValue, anArg); // more than 100x slower
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return Math.hypot(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> PRODUCT = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ONE;

                public double doubleValue() {
                    return myValue;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue *= anArg;
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return result1 * result2;
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ONE;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> PRODUCT2 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ONE;

                public double doubleValue() {
                    return myValue;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue *= anArg * anArg;
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    myValue *= result;
                }

                public Double merge(final Double result1, final Double result2) {
                    return result1 * result2;
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ONE;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> SMALLEST = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = POSITIVE_INFINITY;

                public double doubleValue() {
                    if (Double.isInfinite(myValue)) {
                        return ZERO;
                    } else {
                        return myValue;
                    }
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    final double tmpArg = Math.abs(anArg);
                    if (tmpArg != ZERO) {
                        myValue = Math.min(myValue, tmpArg);
                    }
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return Math.min(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = POSITIVE_INFINITY;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> SUM = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += anArg;
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return result1 + result2;
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> SUM2 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new AggregatorFunction<Double>() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public Double getNumber() {
                    return Double.valueOf(this.doubleValue());
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += anArg * anArg;
                }

                public void invoke(final Double anArg) {
                    this.invoke(anArg.doubleValue());
                }

                public void merge(final Double result) {
                    myValue += result;
                }

                public Double merge(final Double result1, final Double result2) {
                    return result1 + result2;
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

                public Scalar<Double> toScalar() {
                    return PrimitiveScalar.of(this.doubleValue());
                }
            };
        }
    };

    private static final AggregatorSet<Double> SET = new AggregatorSet<Double>() {

        @Override
        public AggregatorFunction<Double> cardinality() {
            return CARDINALITY.get().reset();
        }

        @Override
        public AggregatorFunction<Double> largest() {
            return LARGEST.get().reset();
        }

        @Override
        public AggregatorFunction<Double> maximum() {
            return MAX.get().reset();
        }

        @Override
        public AggregatorFunction<Double> minimum() {
            return MIN.get().reset();
        }

        @Override
        public AggregatorFunction<Double> norm1() {
            return NORM1.get().reset();
        }

        @Override
        public AggregatorFunction<Double> norm2() {
            return NORM2.get().reset();
        }

        @Override
        public AggregatorFunction<Double> product() {
            return PRODUCT.get().reset();
        }

        @Override
        public AggregatorFunction<Double> product2() {
            return PRODUCT2.get().reset();
        }

        @Override
        public AggregatorFunction<Double> smallest() {
            return SMALLEST.get().reset();
        }

        @Override
        public AggregatorFunction<Double> sum() {
            return SUM.get().reset();
        }

        @Override
        public AggregatorFunction<Double> sum2() {
            return SUM2.get().reset();
        }

    };

    /**
     * @deprecated v38 Use {@link #getSet()} instead
     */
    @Deprecated
    public static AggregatorSet<Double> getCollection() {
        return PrimitiveAggregator.getSet();
    }

    public static AggregatorSet<Double> getSet() {
        return SET;
    }

    private PrimitiveAggregator() {

        super();

        ProgrammingError.throwForIllegalInvocation();
    }

}
