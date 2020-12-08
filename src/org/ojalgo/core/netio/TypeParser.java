package org.ojalgo.core.netio;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class TypeParser {

    public static ToDoubleFunction<CharSequence> DOUBLE = value -> Double.parseDouble(value.toString());

    public static ToIntFunction<CharSequence> INT = value -> Integer.parseInt(value.toString());

    public static ToLongFunction<CharSequence> LONG = value -> Long.parseLong(value.toString());

}
