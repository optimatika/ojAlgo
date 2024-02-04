/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.constant.RationalMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;

public final class RationalAggregator extends AggregatorSet<RationalNumber> {

    static abstract class RationalAggregatorFunction implements AggregatorFunction<RationalNumber> {

        public final double doubleValue() {
            return this.get().doubleValue();
        }

        public final void invoke(final double anArg) {
            this.invoke(RationalNumber.valueOf(anArg));
        }

        public final void invoke(final float anArg) {
            this.invoke(RationalNumber.valueOf(anArg));
        }

        public final Scalar<RationalNumber> toScalar() {
            return this.get();
        }

    }

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> AVERAGE = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private int myCount = 0;

                private RationalNumber myNumber = RationalNumber.ZERO;

                public RationalNumber get() {
                    return myNumber.divide(myCount);
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myCount++;
                    myNumber = myNumber.add(anArg);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myCount = 0;
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> CARDINALITY = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private int myCount = 0;

                public RationalNumber get() {
                    return RationalNumber.valueOf(myCount);
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final RationalNumber anArg) {
                    if (!PrimitiveScalar.isSmall(PrimitiveMath.ONE, PrimitiveMath.ABS.invoke(anArg.doubleValue()))) {
                        myCount++;
                    }
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myCount = 0;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> LARGEST = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public RationalNumber get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = RationalMath.MAX.invoke(myNumber, RationalMath.ABS.invoke(anArg));
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> MAX = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.NEGATIVE_INFINITY;

                public RationalNumber get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = RationalMath.MAX.invoke(myNumber, anArg);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.NEGATIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> MIN = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.POSITIVE_INFINITY;

                public RationalNumber get() {
                    if (RationalNumber.isInfinite(myNumber)) {
                        return RationalNumber.ZERO;
                    }
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = RationalMath.MIN.invoke(myNumber, anArg);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> NORM1 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public RationalNumber get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.add(PrimitiveMath.ABS.invoke(anArg.doubleValue()));
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> NORM2 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public RationalNumber get() {
                    return RationalNumber.valueOf(PrimitiveMath.SQRT.invoke(PrimitiveMath.ABS.invoke(myNumber.doubleValue())));
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    final double tmpMod = PrimitiveMath.ABS.invoke(anArg.doubleValue());
                    myNumber = myNumber.add(tmpMod * tmpMod);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> PRODUCT = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.ONE;

                public RationalNumber get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.multiply(anArg);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ONE;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> PRODUCT2 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.ONE;

                public RationalNumber get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.multiply(anArg.multiply(anArg));
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ONE;
                    return this;
                }

            };
        }
    };

    private static final RationalAggregator SET = new RationalAggregator();

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> SMALLEST = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.POSITIVE_INFINITY;

                public RationalNumber get() {
                    if (RationalNumber.isInfinite(myNumber)) {
                        return RationalNumber.ZERO;
                    }
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    if (!RationalNumber.isSmall(PrimitiveMath.ONE, anArg)) {
                        myNumber = RationalMath.MIN.invoke(myNumber, RationalMath.ABS.invoke(anArg));
                    }
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> SUM = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public RationalNumber get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.add(anArg);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<RationalNumber>> SUM2 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new RationalAggregatorFunction() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public RationalNumber get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.add(anArg.multiply(anArg));
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

            };
        }
    };

    public static RationalAggregator getSet() {
        return SET;
    }

    private RationalAggregator() {
        super();
    }

    @Override
    public AggregatorFunction<RationalNumber> average() {
        return AVERAGE.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> cardinality() {
        return CARDINALITY.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> largest() {
        return LARGEST.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> maximum() {
        return MAX.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> minimum() {
        return MIN.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> norm1() {
        return NORM1.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> norm2() {
        return NORM2.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> product() {
        return PRODUCT.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> product2() {
        return PRODUCT2.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> smallest() {
        return SMALLEST.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> sum() {
        return SUM.get().reset();
    }

    @Override
    public AggregatorFunction<RationalNumber> sum2() {
        return SUM2.get().reset();
    }

}
