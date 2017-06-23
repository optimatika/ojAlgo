package org.ojalgo.netio;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class TypeParser {

    public static ToDoubleFunction<CharSequence> DOUBLE = new ToDoubleFunction<CharSequence>() {

        public double applyAsDouble(final CharSequence value) {
            return Double.parseDouble(value.toString());
        }

    };

    public static ToIntFunction<CharSequence> INT = new ToIntFunction<CharSequence>() {

        public int applyAsInt(final CharSequence value) {
            return Integer.parseInt(value.toString());
        }

    };

    public static ToLongFunction<CharSequence> LONG = new ToLongFunction<CharSequence>() {

        public long applyAsLong(final CharSequence value) {
            return Long.parseLong(value.toString());
        }

    };

}
