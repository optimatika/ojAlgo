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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;
import java.util.function.IntSupplier;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.function.special.PowerOf2;

/**
 * Divides a large file in segments and then reads those (in parallel) using memory mapped files (memory
 * mapped file segments). Basic usage:
 * <ol>
 * <li>Call {@link #newBuilder(File)} to get a {@link Builder} or {@link #of(File)} to get a
 * {@link SegmentedFile} directly with the default settings.
 * <li>Then for each of the segments obtained from {@link SegmentedFile#segments()}: Call
 * {@link SegmentedFile#newTextLineReader(Segment)} and using that read all the lines.
 * </ol>
 * Multiple threads can be used to read the segments in parallel. Each thread just works on one segment at a
 * time, and there's a new {@link TextLineReader} for each segment.
 */
public class SegmentedFile implements AutoCloseable {

    public static final class Builder {

        private static final long k = 1024L;

        private long myBytesPerSegment = Math.min(PowerOf2.MAX_INT, OjAlgoUtils.ENVIRONMENT.cache);
        private int myDelimiter = ASCII.LF;
        private final File myFile;
        private int myParallelism = Parallelism.CORES.getAsInt();

        Builder(final File file) {
            super();
            myFile = file;
        }

        public SegmentedFile build() {

            try {

                RandomAccessFile raf = new RandomAccessFile(myFile, "r");

                long nbBytesInFile = raf.length();

                int nbSegments = 1;
                long nbBytesPerSegment = nbBytesInFile;

                while (nbBytesPerSegment > myBytesPerSegment) {
                    if (nbSegments == 1) {
                        nbSegments = myParallelism;
                    } else {
                        nbSegments += myParallelism;
                    }
                    nbBytesPerSegment = nbBytesInFile / nbSegments;
                }

                Segment[] segments = new Segment[nbSegments];

                long start = 0L;
                long end = 0L;

                for (int s = 1; s <= nbSegments; s++) {

                    start = end;

                    if (s == nbSegments) {

                        end = nbBytesInFile;

                    } else {

                        end = Math.min(s * nbBytesPerSegment, nbBytesInFile);

                        raf.seek(end);
                        while (end < nbBytesInFile) {
                            end++;
                            if (raf.read() == myDelimiter) {
                                break;
                            }
                        }
                    }

                    segments[s - 1] = new Segment(start, end - start);
                }

                return new SegmentedFile(raf, segments);

            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        /**
         * The delimiter is used to determine where to split the file into segments. The default value is '\n'
         * (newline).
         */
        public Builder delimiter(final byte delimiter) {
            myDelimiter = delimiter;
            return this;
        }

        /**
         * @see #parallelism(IntSupplier)
         */
        public Builder parallelism(final int parallelism) {
            myParallelism = parallelism;
            return this;
        }

        /**
         * The expected number of threads that will be used to process the file. The number of segments will a
         * multiple of this number. The default value is the number of CPU cores.
         */
        public Builder parallelism(final IntSupplier parallelism) {
            return this.parallelism(parallelism.getAsInt());
        }

        /**
         * The target (max) size (bytes) of each segment. The actual size will be less than or equal to this
         * value. The last segment will be smaller than or equal to the other segments.
         */
        public Builder segmentBytes(final long bytesPerSegment) {
            myBytesPerSegment = bytesPerSegment;
            return this;
        }

        /**
         * @see #segmentBytes(long)
         */
        public Builder segmentKiloBytes(final long kiloBytesPerSegment) {
            return this.segmentBytes(k * kiloBytesPerSegment);
        }

        /**
         * @see #segmentBytes(long)
         */
        public Builder segmentMegaBytes(final long megaBytesPerSegment) {
            return this.segmentKiloBytes(k * megaBytesPerSegment);
        }
    }

    public static final class Segment implements Comparable<Segment> {

        public final long offset;
        public final long size;

        Segment(final long offset, final long size) {
            this.offset = offset;
            this.size = size;
        }

        @Override
        public int compareTo(final Segment ref) {
            return Long.compare(offset, ref.offset);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Segment)) {
                return false;
            }
            Segment other = (Segment) obj;
            if (offset != other.offset || size != other.size) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (offset ^ offset >>> 32);
            result = prime * result + (int) (size ^ size >>> 32);
            return result;
        }

        public long limit() {
            return offset + size;
        }

        @Override
        public String toString() {
            return "[" + offset + ", " + this.limit() + ")";
        }

    }

    public static SegmentedFile.Builder newBuilder(final File file) {
        return new Builder(file);
    }

    public static SegmentedFile of(final File file) {
        return SegmentedFile.newBuilder(file).build();
    }

    private final FileChannel myFileChannel;
    private final RandomAccessFile myRandomAccessFile;
    private final Segment[] mySegments;

    SegmentedFile(final RandomAccessFile file, final Segment[] segments) {
        super();
        myRandomAccessFile = file;
        mySegments = segments;
        myFileChannel = myRandomAccessFile.getChannel();
    }

    @Override
    public void close() {
        try {
            myRandomAccessFile.close();
        } catch (IOException cause) {
            throw new UncheckedIOException(cause);
        }
    }

    /**
     * Call this once for each file segment, and use the returned {@link TextLineReader} to read the file
     * segment. The {@link TextLineReader} is not thread safe, and should only be used by a single thread.
     * <p>
     * The segment is a range of bytes in the file. The {@link TextLineReader} will read the bytes in the
     * range [offset, offset + size). The segments are obtained from {@link #segments()}. The segments are
     * sorted in ascending order by offset.
     */
    public TextLineReader newTextLineReader(final Segment segment) {
        try {
            return new TextLineReader(new ByteBufferBackedInputStream(myFileChannel.map(MapMode.READ_ONLY, segment.offset, segment.size)));
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public List<Segment> segments() {
        return List.of(mySegments);
    }

}
