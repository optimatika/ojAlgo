/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.array.operation;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Locale;
import java.util.Optional;

import org.ojalgo.netio.BasicLogger;

/**
 * Opt-in access to a native BLAS/LAPACK implementation through the Foreign Function and Memory API. ojAlgo
 * has a pure Java implementation of everything that can be delegated to a native library. The native
 * implementation of an operation is used only when all of these hold:
 * <ol>
 * <li>The JVM grants ojAlgo native access: {@code --enable-native-access=ojalgo} when ojAlgo is on the module
 * path, {@code --enable-native-access=ALL-UNNAMED} when it is on the class path (or
 * {@code Enable-Native-Access: ALL-UNNAMED} in the manifest of an executable JAR). Without it, ojAlgo never
 * touches native code.
 * <li>A working library was found. Without the system property {@value #PROPERTY_LIBRARY} ojAlgo looks for a
 * standard implementation: Accelerate on macOS, OpenBLAS, FlexiBLAS, MKL, BLIS or the reference BLAS on
 * Linux, and OpenBLAS or MKL on Windows. The property names a library file to use instead, or several
 * separated by {@link File#pathSeparator}, or is {@value #NONE} to use no native library at all.
 * <li>The problem size passes the operation's native threshold, such as {@code GEMM.NATIVE_THRESHOLD}.
 * </ol>
 * The library is looked for once, when this class is initialised, so the system property must be set before
 * ojAlgo performs any of the operations concerned.
 * <p>
 * BLAS functions are called through the CBLAS interface, with 32-bit integers (LP64). A library that takes
 * 64-bit integers (ILP64) is detected and not used. On macOS the current Accelerate interface (symbols with a
 * {@code $NEWLAPACK} suffix) is preferred over the deprecated one.
 * <p>
 * The library is called with its default configuration. If you know which implementation is used, you can
 * configure it in the environment of the JVM process. For example, OpenBLAS as installed by Homebrew on macOS
 * (an OpenMP build) multi-threads {@code ddot} and {@code daxpy} above about 10 000 elements, which costs
 * some 50 µs per call – more than the pure Java implementation needs. {@code OMP_NUM_THREADS} (OpenMP builds)
 * or {@code OPENBLAS_NUM_THREADS} (pthreads builds) limits that, at the cost of multi-threaded matrix
 * multiplication. On macOS, simply use Accelerate, which is found by default.
 */
public final class NativeLibrary {

    /**
     * The value of {@link #PROPERTY_LIBRARY} that switches native code off, even if the JVM grants native
     * access.
     */
    public static final String NONE = "none";
    /**
     * The system property naming the native library file(s), separated by {@link File#pathSeparator}, or
     * {@value #NONE}. A library with BLAS and another with LAPACK may be combined. When set, no other library
     * is looked for.
     */
    public static final String PROPERTY_LIBRARY = "ojalgo.native.library";

    /**
     * Accelerate's current interface (macOS 13.3 and later) exports its LP64 functions with this suffix.
     */
    private static final String ACCELERATE_SUFFIX = "$NEWLAPACK";
    private static final String[] CANDIDATES_LINUX = { "libopenblas.so.0", "libopenblas.so", "libflexiblas.so.3", "libmkl_rt.so.2", "libmkl_rt.so",
            "libblis.so.4", "libblas.so.3" };
    private static final String[] CANDIDATES_MACOS = { "/System/Library/Frameworks/Accelerate.framework/Accelerate" };
    private static final String[] CANDIDATES_WINDOWS = { "libopenblas.dll", "openblas.dll", "mkl_rt.2.dll", "mkl_rt.dll" };
    private static final String DESCRIPTION;
    private static final SymbolLookup SYMBOLS;

    static {

        SymbolLookup symbols = null;
        String description = null;

        String configured = System.getProperty(PROPERTY_LIBRARY);

        if (NativeLibrary.class.getModule().isNativeAccessEnabled() && !NONE.equalsIgnoreCase(configured)) {

            if (configured != null && !configured.isBlank()) {
                symbols = NativeLibrary.open(configured.split(File.pathSeparator));
                description = configured;
                if (symbols == null) {
                    BasicLogger.error("ojAlgo native BLAS/LAPACK: {} could not be used. Using the Java implementations.", configured);
                }
            } else {
                for (String candidate : NativeLibrary.candidates()) {
                    symbols = NativeLibrary.open(candidate);
                    if (symbols != null) {
                        description = candidate;
                        break;
                    }
                }
                if (symbols == null) {
                    BasicLogger.debug("ojAlgo native BLAS/LAPACK: no library found. Using the Java implementations.");
                }
            }

            if (symbols != null) {
                BasicLogger.debug("ojAlgo native BLAS/LAPACK: {}", description);
            }
        }

        SYMBOLS = symbols;
        DESCRIPTION = symbols != null ? description : null;
    }

    /**
     * @return The library file(s) in use, or {@code null} if native code is not used
     */
    public static String describe() {
        return DESCRIPTION;
    }

    /**
     * Creates a handle to call a native function. Critical functions never call back into Java and may be
     * passed heap segments ({@link #ofArray(double[], int, long)}) so the Java arrays are used in place,
     * without copying. The JVM can't reach a safepoint during a critical call, so a long-running critical
     * function delays garbage collection in the whole JVM until it returns. Non-critical functions must be
     * passed native memory.
     *
     * @param name       The function's name, such as {@code "cblas_ddot"}
     * @param descriptor The function's signature
     * @param critical   Whether the function is critical (and may be passed heap segments)
     * @return The handle, or {@code null} if native code is not used or the library does not have the
     *         function
     */
    public static MethodHandle downcall(final String name, final FunctionDescriptor descriptor, final boolean critical) {

        if (SYMBOLS == null) {
            return null;
        }

        Optional<MemorySegment> symbol = NativeLibrary.find(SYMBOLS, name);

        if (symbol.isEmpty()) {
            BasicLogger.debug("ojAlgo native BLAS/LAPACK: {} not found. Using the Java implementation.", name);
            return null;
        }

        if (critical) {
            return Linker.nativeLinker().downcallHandle(symbol.get(), descriptor, Linker.Option.critical(true));
        } else {
            return Linker.nativeLinker().downcallHandle(symbol.get(), descriptor);
        }
    }

    /**
     * @return true if the JVM grants native access and a working library was found
     */
    public static boolean isAvailable() {
        return SYMBOLS != null;
    }

    /**
     * A heap segment for passing {@code count} consecutive elements of a Java array, starting at
     * {@code offset}, to a critical function. The bounds are checked here; native code would read or write
     * past the end of the array without noticing.
     *
     * @throws IndexOutOfBoundsException if the elements are not all within the array
     */
    public static MemorySegment ofArray(final double[] array, final int offset, final long count) {
        long elementSize = ValueLayout.JAVA_DOUBLE.byteSize();
        return MemorySegment.ofArray(array).asSlice(offset * elementSize, count * elementSize);
    }

    /**
     * A heap segment for passing a vector of {@code count} elements, stored in a Java array with increment
     * {@code increment}, to a critical function. As in BLAS, {@code offset} is the lowest index accessed also
     * when the increment is negative. The bounds are checked here.
     *
     * @throws IndexOutOfBoundsException if the elements are not all within the array
     */
    public static MemorySegment ofVector(final double[] array, final int offset, final int count, final int increment) {
        long extent = count <= 0 ? 0L : 1L + (count - 1L) * Math.abs((long) increment);
        return NativeLibrary.ofArray(array, offset, extent);
    }

    private static String[] candidates() {

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        if (os.contains("mac")) {
            return CANDIDATES_MACOS;
        } else if (os.contains("win")) {
            return CANDIDATES_WINDOWS;
        } else {
            return CANDIDATES_LINUX;
        }
    }

    /**
     * The function {@code name}, as {@link #downcall(String, FunctionDescriptor, boolean)} links it: the
     * Accelerate symbol with the {@code $NEWLAPACK} suffix if there is one, otherwise the plain one.
     */
    private static Optional<MemorySegment> find(final SymbolLookup symbols, final String name) {
        return symbols.find(name + ACCELERATE_SUFFIX).or(() -> symbols.find(name));
    }

    /**
     * Opens the library files, combines their symbols, and checks that {@code cblas_ddot} (the symbol that
     * will be called) is there, takes 32-bit integers and computes correctly. The integer size is checked
     * first: the check that it computes correctly passes 32-bit integers, which a function that takes 64-bit
     * integers could misread.
     *
     * @return The combined symbols, or {@code null} if any file could not be opened or the check failed
     */
    private static SymbolLookup open(final String... files) {

        Arena arena = Arena.ofShared();

        try {

            SymbolLookup symbols = null;
            for (String file : files) {
                SymbolLookup lookup = SymbolLookup.libraryLookup(file.trim(), arena);
                symbols = symbols != null ? symbols.or(lookup) : lookup;
            }

            Optional<MemorySegment> ddot = NativeLibrary.find(symbols, "cblas_ddot");
            if (ddot.isEmpty()) {
                arena.close();
                return null;
            }

            if (!NativeLibrary.isLP64(ddot.get())) {
                BasicLogger.error("ojAlgo native BLAS/LAPACK: {} takes 64-bit integers (ILP64), ojAlgo needs a library with 32-bit integers (LP64).",
                        String.join(File.pathSeparator, files));
                arena.close();
                return null;
            }

            MethodHandle handle = Linker.nativeLinker().downcallHandle(ddot.get(), FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT), Linker.Option.critical(true));
            double check = (double) handle.invokeExact(3, MemorySegment.ofArray(new double[] { 1.0, 2.0, 3.0 }), 1,
                    MemorySegment.ofArray(new double[] { 4.0, 5.0, 6.0 }), 1);
            if (check != 32.0) {
                BasicLogger.error("ojAlgo native BLAS/LAPACK: {} computed cblas_ddot incorrectly.", String.join(File.pathSeparator, files));
                arena.close();
                return null;
            }

            return symbols;

        } catch (Throwable cause) {
            arena.close();
            return null;
        }
    }

    /**
     * Whether a {@code cblas_ddot} function takes 32-bit integers (LP64), as ojAlgo passes them. It is called
     * with n passed as a 64-bit integer that is negative, but has 3 in its low 32 bits. A function that takes
     * 32-bit integers reads only those, n = 3, and computes the dot product (32.0). One that takes 64-bit
     * integers (ILP64) reads a negative n and returns 0.0 without reading the vectors. Either way nothing is
     * read out of bounds. Integer arguments are passed in 64-bit registers, or 8-byte stack slots, on the
     * platforms that ojAlgo runs native code on.
     */
    static boolean isLP64(final MemorySegment ddot) throws Throwable {
        MethodHandle probe = Linker.nativeLinker().downcallHandle(ddot, FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), Linker.Option.critical(true));
        double value = (double) probe.invokeExact(0xFFFF_FFFF_0000_0003L, MemorySegment.ofArray(new double[] { 1.0, 2.0, 3.0 }), 1L,
                MemorySegment.ofArray(new double[] { 4.0, 5.0, 6.0 }), 1L);
        return value == 32.0;
    }

    private NativeLibrary() {
        super();
    }

}
