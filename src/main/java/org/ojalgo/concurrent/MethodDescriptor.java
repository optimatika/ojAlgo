/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.concurrent;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;

import org.ojalgo.machine.JavaType;

/**
 * Description of a (static) method to invoke in a child JVM. Uses type names internally to avoid ClassLoader
 * mismatches.
 * <p>
 * In Java, a method is identified for reflective lookup by its name and parameter types only. The return type
 * is not used to resolve a method and cannot disambiguate overloads (two methods cannot differ only by return
 * type).
 */
public final class MethodDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    public static MethodDescriptor of(final Class<?> owner, final String name, final Class<?>... parameters) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameters);
        return new MethodDescriptor(owner.getName(), name, MethodDescriptor.typeNames(parameters));
    }

    /**
     * Create a descriptor from a reflective {@link Method}. Parameter types are taken as declared (including
     * varargs represented as an array type). The return type is intentionally not included as it is not used
     * for reflective lookup.
     */
    public static MethodDescriptor of(final Method method) {
        Objects.requireNonNull(method);
        return MethodDescriptor.of(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    private static String[] typeNames(final Class<?>[] types) {

        if (types == null || types.length == 0) {
            return new String[0];
        }

        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].getName();
        }
        return names;
    }

    private final String myClassName;
    private transient Method myMethod = null;
    private final String myMethodName;
    private final String[] myParameterTypeNames;

    MethodDescriptor(final String className, final String methodName, final String[] parameterTypeNames) {
        myClassName = className;
        myMethodName = methodName;
        myParameterTypeNames = parameterTypeNames != null ? parameterTypeNames.clone() : new String[0];
    }

    public Method getMethod() throws ClassNotFoundException, NoSuchMethodException, SecurityException {

        if (myMethod == null) {

            Class<?> clazz = Class.forName(myClassName);

            Class<?>[] parameterTypes = new Class<?>[myParameterTypeNames.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypes[i] = JavaType.resolveType(myParameterTypeNames[i]);
            }
            myMethod = clazz.getDeclaredMethod(myMethodName, parameterTypes);
            myMethod.setAccessible(true);
        }

        return myMethod;
    }

}