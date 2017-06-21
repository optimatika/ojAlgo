package org.ojalgo.netio;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class TypeParser {

    public static ToDoubleFunction<CharSequence> DOUBLE = new ToDoubleFunction<CharSequence>() {

        public double applyAsDouble(CharSequence value) {
            // TODO Auto-generated method stub
            return 0;
        }

    };

    public static ToLongFunction<CharSequence> LONG = new ToLongFunction<CharSequence>() {

        public long applyAsLong(CharSequence value) {
            // TODO Auto-generated method stub
            return 0;
        }

    };

    public static ToIntFunction<CharSequence> INT = new ToIntFunction<CharSequence>() {

        public int applyAsInt(CharSequence value) {
            // TODO Auto-generated method stub
            return 0;
        }

    };

}
