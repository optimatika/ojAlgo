/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.core.function.aggregator;

import static org.ojalgo.core.function.constant.PrimitiveMath.*;

import org.ojalgo.core.ProgrammingError;
import org.ojalgo.core.function.constant.PrimitiveMath;
import org.ojalgo.core.scalar.PrimitiveScalar;
import org.ojalgo.core.scalar.Scalar;
import org.ojalgo.core.type.context.NumberContext;

public final class PrimitiveAggregator extends AggregatorSet<Double> {

    static abstract class PrimitiveAggregatorFunction implements AggregatorFunction<Double> {

        public final Double get() {
            return Double.valueOf(this.doubleValue());
        }

        public final void invoke(final Double arg) {
            this.invoke(arg.doubleValue());
        }

        public final void invoke(final float arg) {
            this.invoke((double) arg);
        }

        public final Scalar<Double> toScalar() {
            return PrimitiveScalar.of(this.doubleValue());
        }

    }

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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

                public AggregatorFunction<Double> reset() {
                    myCount = 0;
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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

                public AggregatorFunction<Double> reset() {
                    myCount = 0;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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
                    myValue = PrimitiveMath.MAX.invoke(myValue, PrimitiveMath.ABS.invoke(anArg));
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
    public static final ThreadLocal<AggregatorFunction<Double>> MAX = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = NEGATIVE_INFINITY;

                public double doubleValue() {
                    return myValue;
                }

                public int intValue() {
                    return (int) this.doubleValue();
                }

                public void invoke(final double anArg) {
                    myValue = PrimitiveMath.MAX.invoke(myValue, anArg);
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public AggregatorFunction<Double> reset() {
                    myValue = NEGATIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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
                    myValue = PrimitiveMath.MIN.invoke(myValue, anArg);
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public AggregatorFunction<Double> reset() {
                    myValue = POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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
                    myValue += PrimitiveMath.ABS.invoke(anArg);
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
    public static final ThreadLocal<AggregatorFunction<Double>> NORM2 = new ThreadLocal<AggregatorFunction<Double>>() {

        @Override
        protected AggregatorFunction<Double> initialValue() {
            return new PrimitiveAggregatorFunction() {

                private double myValue = ZERO;

                public double doubleValue() {
                    //return myValue; // more than 100x slower
                    return PrimitiveMath.SQRT.invoke(myValue);
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

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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

                public AggregatorFunction<Double> reset() {
                    myValue = ONE;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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

                public AggregatorFunction<Double> reset() {
                    myValue = ONE;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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
                    final double tmpArg = PrimitiveMath.ABS.invoke(anArg);
                    if (NumberContext.compare(tmpArg, ZERO) != 0) {
                        myValue = PrimitiveMath.MIN.invoke(myValue, tmpArg);
                    }
                }

                public void merge(final Double result) {
                    this.invoke(result.doubleValue());
                }

                public AggregatorFunction<Double> reset() {
                    myValue = POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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

                public AggregatorFunction<Double> reset() {
                    myValue = ZERO;
                    return this;
                }

            };
        }
    };

    /**
     * @deprecated v48 Use {@link AggregatorSet#getSet} instead. This will be made private.
     */
    @Deprecated
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
