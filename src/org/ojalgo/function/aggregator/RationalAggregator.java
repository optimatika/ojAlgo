/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import static org.ojalgo.function.RationalFunction.*;

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.RationalFunction;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.TypeUtils;

public abstract class RationalAggregator {

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> CARDINALITY = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private int myCount = 0;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return new RationalNumber(myCount);
                }

                public int intValue() {
                    return myCount;
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    if (!TypeUtils.isZero(Math.abs(anArg.doubleValue()))) {
                        myCount++;
                    }
                }

                public void merge(final RationalNumber anArg) {
                    myCount += anArg.intValue();
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return ADD.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myCount = 0;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }

            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> LARGEST = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = RationalFunction.MAX.invoke(myNumber, ABS.invoke(anArg));
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return RationalFunction.MAX.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };
    public static final ThreadLocal<AggregatorFunction<RationalNumber>> MAX = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = RationalFunction.MAX.invoke(myNumber, anArg);
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return RationalFunction.MAX.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> NORM1 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.add(Math.abs(anArg.doubleValue()));
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return ADD.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> NORM2 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return new RationalNumber(Math.sqrt(Math.abs(myNumber.doubleValue())));
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    final double tmpMod = Math.abs(anArg.doubleValue());
                    myNumber = myNumber.add(tmpMod * tmpMod);
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return HYPOT.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> PRODUCT = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ONE;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.multiply(anArg);
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return MULTIPLY.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ONE;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> PRODUCT2 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ONE;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.multiply(anArg.multiply(anArg));
                }

                public void merge(final RationalNumber anArg) {
                    myNumber = myNumber.multiply(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return MULTIPLY.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ONE;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> SMALLEST = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.POSITIVE_INFINITY;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    if (myNumber.isInfinite()) {
                        return RationalNumber.ZERO;
                    } else {
                        return myNumber;
                    }
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    if (!anArg.isZero()) {
                        myNumber = RationalFunction.MIN.invoke(myNumber, ABS.invoke(anArg));
                    }
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return RationalFunction.MIN.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.POSITIVE_INFINITY;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> MIN = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.POSITIVE_INFINITY;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    if (myNumber.isInfinite()) {
                        return RationalNumber.ZERO;
                    } else {
                        return myNumber;
                    }
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = RationalFunction.MIN.invoke(myNumber, anArg);
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return RationalFunction.MIN.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.POSITIVE_INFINITY;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> SUM = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.add(anArg);
                }

                public void merge(final RationalNumber anArg) {
                    this.invoke(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return ADD.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    public static final ThreadLocal<AggregatorFunction<RationalNumber>> SUM2 = new ThreadLocal<AggregatorFunction<RationalNumber>>() {

        @Override
        protected AggregatorFunction<RationalNumber> initialValue() {
            return new AggregatorFunction<RationalNumber>() {

                private RationalNumber myNumber = RationalNumber.ZERO;

                public double doubleValue() {
                    return this.getNumber().doubleValue();
                }

                public RationalNumber getNumber() {
                    return myNumber;
                }

                public int intValue() {
                    return this.getNumber().intValue();
                }

                public void invoke(final double anArg) {
                    this.invoke(new RationalNumber(anArg));
                }

                public void invoke(final RationalNumber anArg) {
                    myNumber = myNumber.add(anArg.multiply(anArg));
                }

                public void merge(final RationalNumber anArg) {
                    myNumber = myNumber.add(anArg);
                }

                public RationalNumber merge(final RationalNumber aResult1, final RationalNumber aResult2) {
                    return ADD.invoke(aResult1, aResult2);
                }

                public AggregatorFunction<RationalNumber> reset() {
                    myNumber = RationalNumber.ZERO;
                    return this;
                }

                public Scalar<RationalNumber> toScalar() {
                    return this.getNumber();
                }
            };
        }
    };

    private static final AggregatorCollection<RationalNumber> COLLECTION = new AggregatorCollection<RationalNumber>() {

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

    };

    public static AggregatorCollection<RationalNumber> getCollection() {
        return COLLECTION;
    }

    private RationalAggregator() {

        super();

        ProgrammingError.throwForIllegalInvocation();
    }

}
