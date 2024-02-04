/*
 * Copyright 1997-2024 Optimatika
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.ojalgo.data.domain.finance.series.DatePriceParser;
import org.ojalgo.netio.TextLineReader.Parser;
import org.ojalgo.type.keyvalue.KeyValue;

/**
 * Will detect which delegate parser to use. Must be able to determine that from the first line read. You
 * supply a collection of parsers paired with logic to test if they can handle the proposed line or not.
 *
 * @see DatePriceParser
 * @author apete
 */
public abstract class DetectingParser<T> implements BasicParser<T> {

    private final List<KeyValue<Predicate<String>, TextLineReader.Parser<? extends T>>> myPotentialParsers = new ArrayList<>();
    private Parser<? extends T> mySelectedParser = null;
    private final TextLineReader.Parser<T> myDefaultParser;

    protected DetectingParser(final Parser<T> defaultParser) {
        super();
        myDefaultParser = defaultParser;
    }

    public T parse(final String line) {

        if (mySelectedParser == null) {

            for (KeyValue<Predicate<String>, Parser<? extends T>> pair : myPotentialParsers) {
                Predicate<String> predicate = pair.getKey();
                Parser<? extends T> parser = pair.getValue();

                if (predicate.test(line)) {
                    mySelectedParser = parser;
                    break;
                }
            }
        }

        if (mySelectedParser == null) {
            mySelectedParser = myDefaultParser;
        }

        return mySelectedParser.parse(line);
    }

    protected void addPotentialParser(final Predicate<String> predicate, final Parser<? extends T> parser) {
        myPotentialParsers.add(KeyValue.of(predicate, parser));
    }

}
