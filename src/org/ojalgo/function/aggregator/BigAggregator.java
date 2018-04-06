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

import static org.ojalgo.constant.BigMath.*;
import static org.ojalgo.function.BigFunction.*;

import java.math.BigDecimal;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.BigFunction;
import org.ojalgo.scalar.BigScalar;
import org.ojalgo.scalar.Scalar;

public final class BigAggregator extends AggregatorSet<BigDecimal> {

    static abstract class BigAggregatorFunction implements AggregatorFunction<BigDecimal> {

        public final double doubleValue() {
            return this.get().doubleValue();
        }

        public final void invoke(final double anArg) {
            this.invoke(new BigDecimal(anArg));
        }

        public final Scalar<BigDecimal> toScalar() {
            return BigScalar.of(this.get());
        }

    }

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> AVERAGE = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private int myCount = 0;
                private BigDecimal myNumber = ZERO;

                public BigDecimal get() {
                    return DIVIDE.invoke(myNumber, BigDecimal.valueOf(myCount));
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myCount++;
                    myNumber = ADD.invoke(myNumber, anArg);
                }

                public boolean isMergeable() {
                    return false;
                }

                public void merge(final BigDecimal result) {
                    ProgrammingError.throwForIllegalInvocation();
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    ProgrammingError.throwForIllegalInvocation();
                    return null;
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myCount = 0;
                    myNumber = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> CARDINALITY = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private int myCount = 0;

                public BigDecimal get() {
                    return new BigDecimal(myCount);
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final BigDecimal anArg) {
                    if (anArg.signum() != 0) {
                        myCount++;
                    }
                }

                public void merge(final BigDecimal result) {
                    myCount += result.intValue();
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myCount = 0;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> LARGEST = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ZERO;

                public BigDecimal get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = BigFunction.MAX.invoke(myNumber, ABS.invoke(anArg));
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return result1.max(result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> MAX = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ZERO;

                public BigDecimal get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = BigFunction.MAX.invoke(myNumber, anArg);
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return result1.max(result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> MIN = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = VERY_POSITIVE;

                public BigDecimal get() {
                    if (myNumber.compareTo(VERY_POSITIVE) == 0) {
                        return ZERO;
                    } else {
                        return myNumber;
                    }
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = BigFunction.MIN.invoke(myNumber, anArg);
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return BigFunction.MIN.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = VERY_POSITIVE;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> NORM1 = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ZERO;

                public BigDecimal get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = ADD.invoke(myNumber, anArg.abs());
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> NORM2 = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ZERO;

                public BigDecimal get() {
                    return SQRT.invoke(myNumber);
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = ADD.invoke(myNumber, MULTIPLY.invoke(anArg, anArg));
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return HYPOT.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> PRODUCT = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ONE;

                public BigDecimal get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = MULTIPLY.invoke(myNumber, anArg);
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return MULTIPLY.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ONE;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> PRODUCT2 = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ONE;

                public BigDecimal get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = MULTIPLY.invoke(myNumber, MULTIPLY.invoke(anArg, anArg));
                }

                public void merge(final BigDecimal result) {
                    myNumber = MULTIPLY.invoke(myNumber, result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return MULTIPLY.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ONE;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> SMALLEST = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = VERY_POSITIVE;

                public BigDecimal get() {
                    if (myNumber.compareTo(VERY_POSITIVE) == 0) {
                        return ZERO;
                    } else {
                        return myNumber;
                    }
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    if (anArg.signum() != 0) {
                        myNumber = BigFunction.MIN.invoke(myNumber, ABS.invoke(anArg));
                    }
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return BigFunction.MIN.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = VERY_POSITIVE;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> SUM = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ZERO;

                public BigDecimal get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = ADD.invoke(myNumber, anArg);
                }

                public void merge(final BigDecimal result) {
                    this.invoke(result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ZERO;
                    return this;
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<BigDecimal>> SUM2 = new ThreadLocal<AggregatorFunction<BigDecimal>>() {

        @Override
        protected AggregatorFunction<BigDecimal> initialValue() {
            return new BigAggregatorFunction() {

                private BigDecimal myNumber = ZERO;

                public BigDecimal get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final BigDecimal anArg) {
                    myNumber = ADD.invoke(myNumber, MULTIPLY.invoke(anArg, anArg));
                }

                public void merge(final BigDecimal result) {
                    myNumber = ADD.invoke(myNumber, result);
                }

                public BigDecimal merge(final BigDecimal result1, final BigDecimal result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<BigDecimal> reset() {
                    myNumber = ZERO;
                    return this;
                }

            };
        }
    };

    private static final BigAggregator SET = new BigAggregator();

    public static BigAggregator getSet() {
        return SET;
    }

    private BigAggregator() {
        super();
    }

    @Override
    public AggregatorFunction<BigDecimal> average() {
        return AVERAGE.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> cardinality() {
        return CARDINALITY.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> largest() {
        return LARGEST.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> maximum() {
        return MAX.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> minimum() {
        return MIN.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> norm1() {
        return NORM1.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> norm2() {
        return NORM2.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> product() {
        return PRODUCT.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> product2() {
        return PRODUCT2.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> smallest() {
        return SMALLEST.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> sum() {
        return SUM.get().reset();
    }

    @Override
    public AggregatorFunction<BigDecimal> sum2() {
        return SUM2.get().reset();
    }

}
