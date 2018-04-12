/*
 * Copyright 1997-2018 Optimatika
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
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

public final class PrimitiveAggregator extends AggregatorSet<Double> {

    static abstract class PrimitiveAggregatorFunction implements AggregatorFunction<Double> {

        public final Double get() {
            return Double.valueOf(this.doubleValue());
        }

        public final void invoke(final Double anArg) {
            this.invoke(anArg.doubleValue());
        }

        public final Scalar<Double> toScalar() {
            return PrimitiveScalar.of(this.doubleValue());
        }

    }

    public static final ThreadLocal<AggregatorFunction<Double>> AVERAGE = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private int myCount = 0;
                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue / myCount;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myCount++;
                    myValue += anArg;
                }

                public boolean isMergeable() {
                    return false;
                }

                public void merge(final Double result) {
                    ProgrammingError.throwForIllegalInvocation();
                }

                public Double merge(final Double result1, final Double result2) {
                    ProgrammingError.throwForIllegalInvocation();
                    return null;
                }

                public AggregatorFunction<Double> reset() {
                    myCount = 0;
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> CARDINALITY = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private int myCount = 0;

                public double doubleValue() {
                    return myCount;
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final double anArg) {
                    if (!PrimitiveScalar.isSmall(PrimitiveMath.ONE, anArg)) {
                        myCount++;
                    }
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

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> LARGEST = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue = PrimitiveFunction.MAX.invoke(myValue, PrimitiveFunction.ABS.invoke(anArg));
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return PrimitiveFunction.MAX.invoke(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> MAX = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue = PrimitiveFunction.MAX.invoke(myValue, anArg);
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return PrimitiveFunction.MAX.invoke(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> MIN = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = POSITIVE_INFINITY;

                public double doubleValue() {
                    if (Double.isInfinite(myValue)) {
                        return ZERO;
                    } else {
                        return myValue;
                    }
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue = PrimitiveFunction.MIN.invoke(myValue, anArg);
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return PrimitiveFunction.MIN.invoke(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> NORM1 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += PrimitiveFunction.ABS.invoke(anArg);
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return PrimitiveFunction.ABS.invoke(result1) + PrimitiveFunction.ABS.invoke(result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> NORM2 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ZERO;

                public double doubleValue() {
                    //return myValue; // more than 100x slower
                    return PrimitiveFunction.SQRT.invoke(myValue);
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += anArg * anArg;
                    //myValue = PrimitiveFunction.HYPOT.invoke(myValue, anArg); // more than 100x slower
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return PrimitiveFunction.HYPOT.invoke(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> PRODUCT = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ONE;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue *= anArg;
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

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> PRODUCT2 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ONE;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue *= anArg * anArg;
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

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> SMALLEST = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = POSITIVE_INFINITY;

                public double doubleValue() {
                    if (Double.isInfinite(myValue)) {
                        return ZERO;
                    } else {
                        return myValue;
                    }
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    final double tmpArg = PrimitiveFunction.ABS.invoke(anArg);
                    // if (tmpArg != ZERO) {
                    if (NumberContext.compare(tmpArg, ZERO) != 0) {
                        myValue = PrimitiveFunction.MIN.invoke(myValue, tmpArg);
                    }
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public Double merge(final Double result1, final Double result2) {
                    return PrimitiveFunction.MIN.invoke(result1, result2);
                }

                public AggregatorFunction<Double> reset() {
                    myValue = POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> SUM = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += anArg;
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

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Double>> SUM2 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ZERO;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue += anArg * anArg;
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

            };
        }
    };

    private static final PrimitiveAggregator SET = new PrimitiveAggregator();

    public static PrimitiveAggregator getSet() {
        return SET;
    }

    private PrimitiveAggregator() {
        super();
    }

    @Override
    public AggregatorFunction<Double> average() {
        return AVERAGE.get().reset();
    }

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

}
