/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.netio;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.netio.WhiteSpaceParser.IterationResult;
import org.ojalgo.random.Normal;
import org.ojalgo.type.keyvalue.KeyValue;

public class WhiteSpaceParser implements BasicParser<IterationResult> {

    public static class Evaluation {

        private final LibraryJVM myContestant;
        private final int mySize;

        public Evaluation(int dim, LibraryJVM contestant) {
            super();
            mySize = dim;
            myContestant = contestant;
        }

        public Evaluation(int dim, String library, String jVM) {
            super();
            mySize = dim;
            myContestant = new LibraryJVM(library, jVM);
        }

        public LibraryJVM getContestant() {
            return myContestant;
        }

        public int getSize() {
            return mySize;
        }
    }

    public static class LibraryJVM {

        private final String myJVM;
        private final String myLibrary;

        public LibraryJVM(String library, String jVM) {
            super();
            myLibrary = library;
            myJVM = jVM;
        }

        public String getJVM() {
            return myJVM;
        }

        public String getLibrary() {
            return myLibrary;
        }
    }

    public static class IterationResult implements KeyValue<Evaluation, Normal> {

        private final Evaluation myKey;
        private final Normal myValue;

        public IterationResult(Evaluation key, Normal value) {
            super();
            myKey = key;
            myValue = value;
        }

        public int compareTo(KeyValue<Evaluation, ?> other) {
            return Integer.compare(myKey.getSize(), other.getKey().getSize());
        }

        public Evaluation getKey() {
            return myKey;
        }

        public Normal getValue() {
            return myValue;
        }

    }

    private final String myJVM;

    private boolean myRunComplete = false;

    public WhiteSpaceParser(String jvm) {
        super();
        myJVM = jvm;
    }

    public IterationResult parse(String line) throws RecoverableCondition {

        if (!myRunComplete) {
            if (line.startsWith("Benchmark")) {
                myRunComplete = true;
            }
            return null;
        }

        String[] split = line.split("\\s+");

        int dim = Integer.parseInt(split[1]);
        String lib = split[2];

        double exp = Double.parseDouble(split[5]);
        double dev = Double.parseDouble(split[7]);

        Evaluation key = new Evaluation(dim, lib, myJVM);

        Normal value = new Normal(exp, dev);

        return new IterationResult(key, value);
    }

}
