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
import org.ojalgo.function.constant.QuaternionMath;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.Scalar;

public final class QuaternionAggregator extends AggregatorSet<Quaternion> {

    static abstract class QuaternionAggregatorFunction implements AggregatorFunction<Quaternion> {

        public final double doubleValue() {
            return this.get().doubleValue();
        }

        public final void invoke(final double anArg) {
            this.invoke(Quaternion.valueOf(anArg));
        }

        public final void invoke(final float anArg) {
            this.invoke(Quaternion.valueOf(anArg));
        }

        public final Scalar<Quaternion> toScalar() {
            return this.get();
        }

    }

    private static final ThreadLocal<AggregatorFunction<Quaternion>> AVERAGE = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private int myCount = 0;

                private Quaternion myNumber = Quaternion.ZERO;

                public Quaternion get() {
                    return myNumber.divide(myCount);
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myCount++;
                    myNumber = myNumber.add(anArg);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myCount = 0;
                    myNumber = Quaternion.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> CARDINALITY = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private int myCount = 0;

                public Quaternion get() {
                    return Quaternion.valueOf(myCount);
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final Quaternion anArg) {
                    if (!PrimitiveScalar.isSmall(PrimitiveMath.ONE, anArg.norm())) {
                        myCount++;
                    }
                }

                public AggregatorFunction<Quaternion> reset() {
                    myCount = 0;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> LARGEST = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ZERO;

                public Quaternion get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = QuaternionMath.MAX.invoke(myNumber, QuaternionMath.ABS.invoke(anArg));
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> MAX = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ZERO;

                public Quaternion get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = QuaternionMath.MAX.invoke(myNumber, anArg);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> MIN = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.INFINITY;

                public Quaternion get() {
                    if (Quaternion.isInfinite(myNumber)) {
                        return Quaternion.ZERO;
                    }
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = QuaternionMath.MIN.invoke(myNumber, anArg);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> NORM1 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ZERO;

                public Quaternion get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.add(anArg.norm());
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> NORM2 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ZERO;

                public Quaternion get() {
                    return Quaternion.valueOf(PrimitiveMath.SQRT.invoke(myNumber.norm()));
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    final double tmpMod = anArg.norm();
                    myNumber = myNumber.add(tmpMod * tmpMod);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> PRODUCT = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ONE;

                public Quaternion get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.multiply(anArg);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ONE;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> PRODUCT2 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ONE;

                public Quaternion get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.multiply(anArg.multiply(anArg));
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ONE;
                    return this;
                }

            };
        }
    };

    private static final QuaternionAggregator SET = new QuaternionAggregator();

    private static final ThreadLocal<AggregatorFunction<Quaternion>> SMALLEST = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.INFINITY;

                public Quaternion get() {
                    if (Quaternion.isInfinite(myNumber)) {
                        return Quaternion.ZERO;
                    }
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    if (!Quaternion.isSmall(PrimitiveMath.ONE, anArg)) {
                        myNumber = QuaternionMath.MIN.invoke(myNumber, QuaternionMath.ABS.invoke(anArg));
                    }
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.INFINITY;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> SUM = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ZERO;

                public Quaternion get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.add(anArg);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

            };
        }
    };

    private static final ThreadLocal<AggregatorFunction<Quaternion>> SUM2 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new QuaternionAggregatorFunction() {

                private Quaternion myNumber = Quaternion.ZERO;

                public Quaternion get() {
                    return myNumber;
                }

                public int intValue() {
                    return this.get().intValue();
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.add(anArg.multiply(anArg));
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

            };
        }
    };

    public static QuaternionAggregator getSet() {
        return SET;
    }

    private QuaternionAggregator() {
        super();
    }

    @Override
    public AggregatorFunction<Quaternion> average() {
        return AVERAGE.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> cardinality() {
        return CARDINALITY.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> largest() {
        return LARGEST.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> maximum() {
        return MAX.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> minimum() {
        return MIN.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> norm1() {
        return NORM1.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> norm2() {
        return NORM2.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> product() {
        return PRODUCT.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> product2() {
        return PRODUCT2.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> smallest() {
        return SMALLEST.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> sum() {
        return SUM.get().reset();
    }

    @Override
    public AggregatorFunction<Quaternion> sum2() {
        return SUM2.get().reset();
    }

}
