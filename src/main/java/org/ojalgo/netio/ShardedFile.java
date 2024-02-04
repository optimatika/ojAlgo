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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

import org.ojalgo.type.format.NumberStyle;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

public final class ShardedFile implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ShardedFile of(final File template, final int nbShards) {
        return new ShardedFile(template, nbShards);
    }

    public static ShardedFile of(final File templateFolder, final String templateFile, final int nbShards) {
        return new ShardedFile(new File(templateFolder, templateFile), nbShards);
    }

    private static File[] splitToShards(final File file, final int numberOfShards) {

        File parentDir = file.getParentFile();
        String templateName = file.getName();

        LongFunction<String> converter = NumberStyle.newUniformFormatter(numberOfShards);

        File[] retVal = new File[numberOfShards];

        int indexOfDot = templateName.indexOf('.');
        if (indexOfDot >= 0) {

            String base = templateName.substring(0, indexOfDot);
            String ending = templateName.substring(indexOfDot);

            for (int i = 0; i < numberOfShards; i++) {
                retVal[i] = new File(parentDir, base + converter.apply(i) + ending);
            }

        } else {

            for (int i = 0; i < numberOfShards; i++) {
                retVal[i] = new File(parentDir, templateName + converter.apply(i));
            }
        }

        return retVal;
    }

    public final int numberOfShards;

    /**
     * A valid single file used as a template when creating the shards. Can also be used for meta data or
     * merged/aggregated data from the shards.
     */
    public final File single;

    private transient File[] myShards = null;

    ShardedFile(final File template, final int nbShards) {
        super();
        single = template;
        numberOfShards = nbShards;
        ToFileWriter.mkdirs(template.getParentFile());
    }

    /**
     * Explicitly delete all files/shards as well as the parent directory (make sure there is nothing else in
     * that directory)
     */
    public void delete() {
        FromFileReader.delete(this.directory());
    }

    /**
     * @return A parent directory to all the shards
     */
    public File directory() {
        return single.getParentFile();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ShardedFile)) {
            return false;
        }
        ShardedFile other = (ShardedFile) obj;
        if (numberOfShards != other.numberOfShards) {
            return false;
        }
        if (single == null) {
            if (other.single != null) {
                return false;
            }
        } else if (!single.equals(other.single)) {
            return false;
        }
        return true;
    }

    /**
     * @return Same as {@link #shards()} but as a {@link List}.
     */
    public List<File> files() {
        return Arrays.asList(this.shards());
    }

    /**
     * @return Same as {@link #files()} but paired with the shard index.
     */
    public List<KeyedPrimitive<File>> filesWithShardIndex() {

        File[] shards = this.shards();

        List<KeyedPrimitive<File>> retVal = new ArrayList<>(shards.length);

        for (int s = 0; s < shards.length; s++) {
            retVal.add(EntryPair.of(shards[s], s));
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + numberOfShards;
        return prime * result + ((single == null) ? 0 : single.hashCode());
    }

    public File shard(final int index) {
        File[] shards = this.shards();
        return shards[index];
    }

    public File[] shards() {
        if (myShards == null) {
            myShards = ShardedFile.splitToShards(single, numberOfShards);
        }
        return myShards;
    }

    @Override
    public String toString() {
        return single.getParent() + "-" + numberOfShards + "-" + single.getName();
    }

}
