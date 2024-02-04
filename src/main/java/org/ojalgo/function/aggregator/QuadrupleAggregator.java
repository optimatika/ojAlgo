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
import org.ojalgo.function.constant.QuadrupleMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Scalar;

public final class QuadrupleAggregator extends AggregatorSet<Quadruple> {

    static abstract class QuadrupleAggregatorFunction implements AggregatorFunction<Quadruple> {

        public final double doubleValue() {
            return this.get().doubleValue();
        }

        public final void invoke(final double anArg) {
            this.invoke(Quadruple.valueOf(anArg));
        }

        public final void invoke(final float anArg) {
            this.invoke(Quadruple.valueOf(anArg));
        }

        public final Scalar<Quadruple> toScalar() {
            return this.get();
        }

    }

    private static final ThreadLocal<AggregatorFunction<Quadruple>> AVERAGE = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private int myCount = 0;

                private Quadruple myNumber = Quadruple.ZERO;

                public Quadruple get() {
                    return myNumber.divide(myCount);
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myCount++;
                    myNumber = myNumber.add(anArg);
                }

                public AggregatorFunction<Quadruple> reset() {
                    myCount = 0;
                    myNumber = Quadruple.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> CARDINALITY = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private int myCount = 0;

                public Quadruple get() {
                    return Quadruple.valueOf(myCount);
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final Quadruple anArg) {
                    if (!PrimitiveScalar.isSmall(PrimitiveMath.ONE, PrimitiveMath.ABS.invoke(anArg.doubleValue()))) {
                        myCount++;
                    }
                }

                public AggregatorFunction<Quadruple> reset() {
                    myCount = 0;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> LARGEST = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.ZERO;

                public Quadruple get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = QuadrupleMath.MAX.invoke(myNumber, QuadrupleMath.ABS.invoke(anArg));
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> MAX = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.NEGATIVE_INFINITY;

                public Quadruple get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = QuadrupleMath.MAX.invoke(myNumber, anArg);
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.NEGATIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> MIN = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.POSITIVE_INFINITY;

                public Quadruple get() {
                    if (Quadruple.isInfinite(myNumber)) {
                        return Quadruple.ZERO;
                    }
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = QuadrupleMath.MIN.invoke(myNumber, anArg);
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> NORM1 = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.ZERO;

                public Quadruple get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = myNumber.add(PrimitiveMath.ABS.invoke(anArg.doubleValue()));
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> NORM2 = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.ZERO;

                public Quadruple get() {
                    return Quadruple.valueOf(PrimitiveMath.SQRT.invoke(PrimitiveMath.ABS.invoke(myNumber.doubleValue())));
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    final double tmpMod = PrimitiveMath.ABS.invoke(anArg.doubleValue());
                    myNumber = myNumber.add(tmpMod * tmpMod);
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> PRODUCT = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.ONE;

                public Quadruple get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = myNumber.multiply(anArg);
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.ONE;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> PRODUCT2 = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.ONE;

                public Quadruple get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = myNumber.multiply(anArg.multiply(anArg));
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.ONE;
                    return this;
                }

            };
        }
    };

    private static final QuadrupleAggregator SET = new QuadrupleAggregator();

    private static final ThreadLocal<AggregatorFunction<Quadruple>> SMALLEST = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.POSITIVE_INFINITY;

                public Quadruple get() {
                    if (Quadruple.isInfinite(myNumber)) {
                        return Quadruple.ZERO;
                    }
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    if (!Quadruple.isSmall(PrimitiveMath.ONE, anArg)) {
                        myNumber = QuadrupleMath.MIN.invoke(myNumber, QuadrupleMath.ABS.invoke(anArg));
                    }
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.POSITIVE_INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> SUM = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.ZERO;

                public Quadruple get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = myNumber.add(anArg);
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quadruple>> SUM2 = new ThreadLocal<>() {

        @Override
        protected AggregatorFunction<Quadruple> initialValue() {
            return new QuadrupleAggregatorFunction() {

                private Quadruple myNumber = Quadruple.ZERO;

                public Quadruple get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quadruple anArg) {
                    myNumber = myNumber.add(anArg.multiply(anArg));
                }

                public AggregatorFunction<Quadruple> reset() {
                    myNumber = Quadruple.ZERO;
                    return this;
                }

            };
        }
    };

    public static QuadrupleAggregator getSet() {
        return SET;
    }

    private QuadrupleAggregator() {
        super();
    }

    @Override
    public AggregatorFunction<Quadruple> average() {
        return AVERAGE.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> cardinality() {
        return CARDINALITY.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> largest() {
        return LARGEST.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> maximum() {
        return MAX.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> minimum() {
        return MIN.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> norm1() {
        return NORM1.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> norm2() {
        return NORM2.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> product() {
        return PRODUCT.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> product2() {
        return PRODUCT2.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> smallest() {
        return SMALLEST.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> sum() {
        return SUM.get().reset();
    }

    @Override
    public AggregatorFunction<Quadruple> sum2() {
        return SUM2.get().reset();
    }

}
