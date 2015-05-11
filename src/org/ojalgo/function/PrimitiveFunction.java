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
package org.ojalgo.function;

import static org.ojalgo.constant.PrimitiveMath.*;

import org.ojalgo.ProgrammingError;
import org.ojalgo.type.TypeUtils;

/**
 * Only the primitive parameter (double) methods are actually implemented. The methods with the reference type
 * parameters (Double) should delegate to the primitive methods (and do nothing else). The various
 * implementations should delegate as much as possible to {@link java.lang.Math} and/or built-in Java
 * operators.
 *
 * @author apete
 */
public final class PrimitiveFunction extends FunctionSet<Double> {

    @FunctionalInterface
    public static interface Binary extends BinaryFunction<Double> {

        default Double invoke(final Double arg1, final Double arg2) {
            return this.invoke(arg1.doubleValue(), arg2.doubleValue());
        }

    }

    @FunctionalInterface
    public static interface Parameter extends ParameterFunction<Double> {

        default Double invoke(final Double arg, final int param) {
            return this.invoke(arg.doubleValue(), param);
        }

    }

    @FunctionalInterface
    public static interface Unary extends UnaryFunction<Double> {

        default Double invoke(final Double arg) {
            return this.invoke(arg.doubleValue());
        }

    }

    public static final UnaryFunction<Double> ABS = new Unary() {

        public final double invoke(final double arg) {
            return Math.abs(arg);
        }

    };

    public static final UnaryFunction<Double> ACOS = new Unary() {

        public final double invoke(final double arg) {
            return Math.acos(arg);
        }

    };

    public static final UnaryFunction<Double> ACOSH = new Unary() {

        public final double invoke(final double arg) {
            return Math.log(arg + Math.sqrt((arg * arg) - ONE));
        }

    };

    public static final BinaryFunction<Double> ADD = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 + arg2;
        }

    };

    public static final UnaryFunction<Double> ASIN = new Unary() {

        public final double invoke(final double arg) {
            return Math.asin(arg);
        }

    };

    public static final UnaryFunction<Double> ASINH = new Unary() {

        public final double invoke(final double arg) {
            return Math.log(arg + Math.sqrt((arg * arg) + ONE));
        }

    };

    public static final UnaryFunction<Double> ATAN = new Unary() {

        public final double invoke(final double arg) {
            return Math.atan(arg);
        }

    };

    public static final UnaryFunction<Double> ATANH = new Unary() {

        public final double invoke(final double arg) {
            return Math.log((ONE + arg) / (ONE - arg)) / TWO;
        }

    };

    public static final UnaryFunction<Double> CARDINALITY = new Unary() {

        public final double invoke(final double arg) {
            return TypeUtils.isZero(arg) ? ZERO : ONE;
        }

    };

    public static final UnaryFunction<Double> CONJUGATE = new Unary() {

        public final double invoke(final double arg) {
            return arg;
        }

    };

    public static final UnaryFunction<Double> COS = new Unary() {

        public final double invoke(final double arg) {
            return Math.cos(arg);
        }

    };

    public static final UnaryFunction<Double> COSH = new Unary() {

        public final double invoke(final double arg) {
            return Math.cosh(arg);
        }

    };

    public static final BinaryFunction<Double> DIVIDE = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 / arg2;
        }

    };

    public static final UnaryFunction<Double> EXP = new Unary() {

        public final double invoke(final double arg) {
            return Math.exp(arg);
        }

    };

    public static final UnaryFunction<Double> EXPM1 = new Unary() {

        public final double invoke(final double arg) {
            return Math.expm1(arg);
        }

    };

    public static final BinaryFunction<Double> HYPOT = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            double retVal;
            if (Math.abs(arg1) > Math.abs(arg2)) {
                retVal = arg2 / arg1;
                retVal = Math.abs(arg1) * Math.sqrt(ONE + (retVal * retVal));
            } else if (arg2 != ZERO) {
                retVal = arg1 / arg2;
                retVal = Math.abs(arg2) * Math.sqrt(ONE + (retVal * retVal));
            } else {
                retVal = ZERO;
            }
            return retVal;
        }

    };

    public static final UnaryFunction<Double> INVERT = new Unary() {

        public final double invoke(final double arg) {
            return ONE / arg;
        }

    };

    public static final UnaryFunction<Double> LOG = new Unary() {

        public final double invoke(final double arg) {
            return Math.log(arg);
        }

    };

    public static final UnaryFunction<Double> LOG10 = new Unary() {

        public final double invoke(final double arg) {
            return Math.log10(arg);
        }

    };

    public static final UnaryFunction<Double> LOG1P = new Unary() {

        public final double invoke(final double arg) {
            return Math.log1p(arg);
        }

    };

    public static final BinaryFunction<Double> MAX = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            return Math.max(arg1, arg2);
        }

    };

    public static final BinaryFunction<Double> MIN = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            return Math.min(arg1, arg2);
        }

    };

    public static final BinaryFunction<Double> MULTIPLY = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 * arg2;
        }

    };

    public static final UnaryFunction<Double> NEGATE = new Unary() {

        public final double invoke(final double arg) {
            return -arg;
        }

    };

    public static final BinaryFunction<Double> POW = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            return Math.pow(arg1, arg2);
        }

    };

    public static final ParameterFunction<Double> POWER = new Parameter() {

        @Override
        public final double invoke(final double arg, int param) {

            if (param < 0) {

                return INVERT.invoke(POWER.invoke(arg, -param));

            } else {

                double retVal = ONE;

                while (param > 0) {
                    retVal = retVal * arg;
                    param--;
                }

                return retVal;
            }
        }

    };

    public static final ParameterFunction<Double> ROOT = new Parameter() {

        @Override
        public final double invoke(final double arg, final int param) {

            if (param != 0) {
                return Math.pow(arg, ONE / param);
            } else {
                throw new IllegalArgumentException();
            }
        }

    };

    public static final ParameterFunction<Double> SCALE = new Parameter() {

        @Override
        public final double invoke(final double arg, int param) {

            if (param < 0) {
                throw new ProgrammingError("Cannot have exponents smaller than zero.");
            }

            long tmpFactor = 1l;
            final long tmp10 = (long) TEN;

            while (param > 0) {
                tmpFactor *= tmp10;
                param--;
            }

            return Math.rint(tmpFactor * arg) / tmpFactor;
        }

    };

    public static final UnaryFunction<Double> SIGNUM = new Unary() {

        public final double invoke(final double arg) {
            return Math.signum(arg);
        }

    };

    public static final UnaryFunction<Double> SIN = new Unary() {

        public final double invoke(final double arg) {
            return Math.sin(arg);
        }

    };

    public static final UnaryFunction<Double> SINH = new Unary() {

        public final double invoke(final double arg) {
            return Math.sinh(arg);
        }

    };

    public static final UnaryFunction<Double> SQRT = new Unary() {

        public final double invoke(final double arg) {
            return Math.sqrt(arg);
        }

    };

    public static final UnaryFunction<Double> SQRT1PX2 = new Unary() {

        public final double invoke(final double arg) {
            return Math.sqrt(ONE + (arg * arg));
        }

    };

    public static final BinaryFunction<Double> SUBTRACT = new Binary() {

        @Override
        public final double invoke(final double arg1, final double arg2) {
            return arg1 - arg2;
        }

    };

    public static final UnaryFunction<Double> TAN = new Unary() {

        public final double invoke(final double arg) {
            return Math.tan(arg);
        }

    };

    public static final UnaryFunction<Double> TANH = new Unary() {

        public final double invoke(final double arg) {
            return Math.tanh(arg);
        }

    };

    public static final UnaryFunction<Double> VALUE = new Unary() {

        public final double invoke(final double arg) {
            return arg;
        }

    };

    private static final PrimitiveFunction SET = new PrimitiveFunction();

    public static PrimitiveFunction getSet() {
        return SET;
    }

    private PrimitiveFunction() {
        super();
    }

    @Override
    public UnaryFunction<Double> abs() {
        return ABS;
    }

    @Override
    public UnaryFunction<Double> acos() {
        return ACOS;
    }

    @Override
    public UnaryFunction<Double> acosh() {
        return ACOSH;
    }

    @Override
    public BinaryFunction<Double> add() {
        return ADD;
    }

    @Override
    public UnaryFunction<Double> asin() {
        return ASIN;
    }

    @Override
    public UnaryFunction<Double> asinh() {
        return ASINH;
    }

    @Override
    public UnaryFunction<Double> atan() {
        return ATAN;
    }

    @Override
    public UnaryFunction<Double> atanh() {
        return ATANH;
    }

    @Override
    public UnaryFunction<Double> cardinality() {
        return CARDINALITY;
    }

    @Override
    public UnaryFunction<Double> conjugate() {
        return CONJUGATE;
    }

    @Override
    public UnaryFunction<Double> cos() {
        return COS;
    }

    @Override
    public UnaryFunction<Double> cosh() {
        return COSH;
    }

    @Override
    public BinaryFunction<Double> divide() {
        return DIVIDE;
    }

    @Override
    public UnaryFunction<Double> exp() {
        return EXP;
    }

    @Override
    public UnaryFunction<Double> expm1() {
        return EXPM1;
    }

    @Override
    public BinaryFunction<Double> hypot() {
        return HYPOT;
    }

    @Override
    public UnaryFunction<Double> invert() {
        return INVERT;
    }

    @Override
    public UnaryFunction<Double> log() {
        return LOG;
    }

    @Override
    public UnaryFunction<Double> log10() {
        return LOG10;
    }

    @Override
    public UnaryFunction<Double> log1p() {
        return LOG1P;
    }

    @Override
    public BinaryFunction<Double> max() {
        return MAX;
    }

    @Override
    public BinaryFunction<Double> min() {
        return MIN;
    }

    @Override
    public BinaryFunction<Double> multiply() {
        return MULTIPLY;
    }

    @Override
    public UnaryFunction<Double> negate() {
        return NEGATE;
    }

    @Override
    public BinaryFunction<Double> pow() {
        return POW;
    }

    @Override
    public ParameterFunction<Double> power() {
        return POWER;
    }

    @Override
    public ParameterFunction<Double> root() {
        return ROOT;
    }

    @Override
    public ParameterFunction<Double> scale() {
        return SCALE;
    }

    @Override
    public UnaryFunction<Double> signum() {
        return SIGNUM;
    }

    @Override
    public UnaryFunction<Double> sin() {
        return SIN;
    }

    @Override
    public UnaryFunction<Double> sinh() {
        return SINH;
    }

    @Override
    public UnaryFunction<Double> sqrt() {
        return SQRT;
    }

    @Override
    public UnaryFunction<Double> sqrt1px2() {
        return SQRT1PX2;
    }

    @Override
    public BinaryFunction<Double> subtract() {
        return SUBTRACT;
    }

    @Override
    public UnaryFunction<Double> tan() {
        return TAN;
    }

    @Override
    public UnaryFunction<Double> tanh() {
        return TANH;
    }

    @Override
    public UnaryFunction<Double> value() {
        return VALUE;
    }

}
