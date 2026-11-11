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

import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.util.Locale;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.ojalgo.OjAlgoUtils;
import org.ojalgo.TestUtils;

/**
 * Checks that native code is, or is not, in use as the build environment expects. The CI job that installs a
 * native BLAS library sets {@value #EXPECTED} to "true", and the job that switches native code off with
 * {@code -Dojalgo.native.library=none} sets it to "false", so neither job can silently run the tests in the
 * other configuration. Without the variable there is nothing to check.
 */
public class NativeLibraryTest {

    private static final String EXPECTED = "OJALGO_NATIVE_EXPECTED";

    @Test
    @EnabledIfEnvironmentVariable(named = EXPECTED, matches = "true|false")
    public void testExpectedAvailability() {

        boolean expected = Boolean.parseBoolean(System.getenv(EXPECTED));

        TestUtils.assertTrue("Native library expected: " + expected + ", in use: " + NativeLibrary.describe(), NativeLibrary.isAvailable() == expected);
    }

    /**
     * The self-test tells a library that takes 32-bit integers (LP64) from one that takes 64-bit integers
     * (ILP64). Accelerate has both variants of {@code cblas_ddot}, so this runs on macOS (when native access
     * is granted, whatever library ojAlgo uses).
     */
    @Test
    public void testILP64Detection() throws Throwable {

        boolean macOS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
        Assumptions.assumeTrue(macOS && NativeLibrary.class.getModule().isNativeAccessEnabled());

        try (Arena arena = Arena.ofConfined()) {

            SymbolLookup accelerate = SymbolLookup.libraryLookup("/System/Library/Frameworks/Accelerate.framework/Accelerate", arena);

            Assumptions.assumeTrue(accelerate.find("cblas_ddot$NEWLAPACK$ILP64").isPresent());

            TestUtils.assertTrue(NativeLibrary.isLP64(accelerate.find("cblas_ddot$NEWLAPACK").orElseThrow()));
            TestUtils.assertFalse(NativeLibrary.isLP64(accelerate.find("cblas_ddot$NEWLAPACK$ILP64").orElseThrow()));
        }
    }

    /**
     * The public {@link OjAlgoUtils#getNativeLibrary()} reports the library in use, or nothing.
     */
    @Test
    public void testNativeLibraryReported() {

        TestUtils.assertTrue("Reported: " + OjAlgoUtils.getNativeLibrary(), OjAlgoUtils.getNativeLibrary().isPresent() == NativeLibrary.isAvailable());

        OjAlgoUtils.getNativeLibrary().ifPresent(library -> TestUtils.assertEquals(NativeLibrary.describe(), library));
    }

}
