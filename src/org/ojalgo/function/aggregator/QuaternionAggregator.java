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

import static org.ojalgo.function.QuaternionFunction.*;

import org.ojalgo.ProgrammingError;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.QuaternionFunction;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

public abstract class QuaternionAggregator {

    public static final ThreadLocal<AggregatorFunction<Quaternion>> CARDINALITY = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private int myCount = 0;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return Quaternion.valueOf(myCount);
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    if (!TypeUtils.isZero(anArg.norm())) {
                        myCount++;
                    }
                }

                public void merge(final Quaternion result) {
                    myCount += result.intValue();
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myCount = 0;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> LARGEST = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = QuaternionFunction.MAX.invoke(myNumber, ABS.invoke(anArg));
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return QuaternionFunction.MAX.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };
    public static final ThreadLocal<AggregatorFunction<Quaternion>> MAX = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = QuaternionFunction.MAX.invoke(myNumber, anArg);
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return QuaternionFunction.MAX.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> MIN = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.INFINITY;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    if (Quaternion.isInfinite(myNumber)) {
                        return Quaternion.ZERO;
                    } else {
                        return myNumber;
                    }
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = QuaternionFunction.MIN.invoke(myNumber, anArg);
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return QuaternionFunction.MIN.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.INFINITY;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> NORM1 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.add(anArg.norm());
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> NORM2 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return Quaternion.valueOf(Math.sqrt(myNumber.norm()));
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    final double tmpMod = anArg.norm();
                    myNumber = myNumber.add(tmpMod * tmpMod);
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return HYPOT.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> PRODUCT = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ONE;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.multiply(anArg);
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return MULTIPLY.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ONE;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> PRODUCT2 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ONE;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.multiply(anArg.multiply(anArg));
                }

                public void merge(final Quaternion result) {
                    myNumber = myNumber.multiply(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return MULTIPLY.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ONE;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };
    public static final ThreadLocal<AggregatorFunction<Quaternion>> SMALLEST = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.INFINITY;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    if (Quaternion.isInfinite(myNumber)) {
                        return Quaternion.ZERO;
                    } else {
                        return myNumber;
                    }
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    if (!Quaternion.isSmall(PrimitiveMath.ONE, anArg)) {
                        myNumber = QuaternionFunction.MIN.invoke(myNumber, ABS.invoke(anArg));
                    }
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return QuaternionFunction.MIN.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.INFINITY;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> SUM = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.add(anArg);
                }

                public void merge(final Quaternion result) {
                    this.invoke(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<Quaternion>> SUM2 = new ThreadLocal<AggregatorFunction<Quaternion>>() {

        @Override
        protected AggregatorFunction<Quaternion> initialValue() {
            return new AggregatorFunction<Quaternion>() {

                private Quaternion myNumber = Quaternion.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public Quaternion getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(Quaternion.valueOf(anArg));
                }

                public void invoke(final Quaternion anArg) {
                    myNumber = myNumber.add(anArg.multiply(anArg));
                }

                public void merge(final Quaternion result) {
                    myNumber = myNumber.add(result);
                }

                public Quaternion merge(final Quaternion result1, final Quaternion result2) {
                    return ADD.invoke(result1, result2);
                }

                public AggregatorFunction<Quaternion> reset() {
                    myNumber = Quaternion.ZERO;
                    return this;
                }

                public Scalar<Quaternion> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    private static final AggregatorSet<Quaternion> SET = new AggregatorSet<Quaternion>() {

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

    };

    /**
     * @deprecated v38 Use {@link #getSet()} instead
     */
    @Deprecated
    public static AggregatorSet<Quaternion> getCollection() {
        return QuaternionAggregator.getSet();
    }

    public static AggregatorSet<Quaternion> getSet() {
        return SET;
    }

    private QuaternionAggregator() {

        super();

        ProgrammingError.throwForIllegalInvocation();
    }

}
