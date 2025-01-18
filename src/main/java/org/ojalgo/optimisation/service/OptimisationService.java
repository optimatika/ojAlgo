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
package org.ojalgo.optimisation.service;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.FileFormat;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.optimisation.integer.IntegerStrategy;
import org.ojalgo.type.ForgetfulMap;

/**
 * Basic usage:
 * <ol>
 * <li>Put optimisation problems on the solve queue by calling {@link #putOnQueue(Sense, byte[], FileFormat)}
 * <li>Check the status of the optimisation by calling {@link #getStatus(String)} – is it {@link Status#DONE}
 * or still {@link Status#PENDING}?
 * <li>Get the result of the optimisation by calling {@link #getResult(String)} – when {@link Status#DONE}
 */
public final class OptimisationService {

    public enum Status {
        DONE, PENDING;
    }

    static final class Problem {

        private final byte[] myContents;
        private final FileFormat myFormat;
        private final String myKey;
        private final Optimisation.Sense mySense;

        Problem(final String key, final Sense sense, final byte[] contents, final FileFormat format) {
            super();
            myKey = key;
            mySense = sense;
            myContents = contents;
            myFormat = format;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Problem)) {
                return false;
            }
            Problem other = (Problem) obj;
            return Arrays.equals(myContents, other.myContents) && myFormat == other.myFormat && Objects.equals(myKey, other.myKey) && mySense == other.mySense;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(myContents);
            result = prime * result + Objects.hash(myFormat, myKey, mySense);
            return result;
        }

        byte[] getContents() {
            return myContents;
        }

        FileFormat getFormat() {
            return myFormat;
        }

        String getKey() {
            return myKey;
        }

        Optimisation.Sense getSense() {
            return mySense;
        }

    }

    private static final Result FAILED = Optimisation.Result.of(PrimitiveMath.NaN, Optimisation.State.FAILED);

    public static ServiceIntegration newIntegration(final String host) {
        return ServiceIntegration.newInstance(host);
    }

    private static Optimisation.Result doOptimise(final Sense sense, final byte[] contents, final FileFormat format) throws RecoverableCondition {
        try (ByteArrayInputStream input = new ByteArrayInputStream(contents)) {
            ExpressionsBasedModel model = ExpressionsBasedModel.parse(input, format);
            model.options.progress(IntegerSolver.class);
            return sense == Sense.MAX ? model.maximise() : model.minimise();
        } catch (Exception cause) {
            throw new RecoverableCondition(cause);
        }
    }

    private static String generateKey() {
        return ASCII.generateRandom(16, ASCII::isAlphanumeric);
    }

    private final int myNumberOfWorkers;
    private final Optimisation.Options myOptimisationOptions;
    private final ProcessingService myProcessingService = ProcessingService.newInstance("optimisation-worker");
    private final BlockingQueue<Problem> myQueue = new LinkedBlockingQueue<>(128);
    private final ForgetfulMap<String, Optimisation.Result> myResultCache = ForgetfulMap.newBuilder().expireAfterAccess(Duration.ofHours(1)).build();
    private final ForgetfulMap<String, Status> myStatusCache = ForgetfulMap.newBuilder().expireAfterAccess(Duration.ofHours(1)).build();

    public OptimisationService() {

        super();

        Parallelism baseParallelism = Parallelism.THREADS;
        int nbStrategies = IntegerStrategy.DEFAULT.countUniqueStrategies();

        int targetNbWorkers = baseParallelism.divideBy(2 * nbStrategies).getAsInt();
        myNumberOfWorkers = Math.max(2, targetNbWorkers);

        IntegerStrategy integerStrategy = IntegerStrategy.DEFAULT.withParallelism(baseParallelism.divideBy(targetNbWorkers));

        myOptimisationOptions = new Optimisation.Options();
        myOptimisationOptions.integer(integerStrategy);

        myProcessingService.take(myQueue, myNumberOfWorkers, this::doOptimise);
    }

    public Optimisation.Result getResult(final String key) {
        return myResultCache.getIfPresent(key);
    }

    public Status getStatus(final String key) {
        return myStatusCache.getIfPresent(key);
    }

    public Optimisation.Result optimise(final Sense sense, final byte[] contents, final FileFormat format) {
        try {
            return OptimisationService.doOptimise(sense, contents, format);
        } catch (RecoverableCondition cause) {
            BasicLogger.error("Optimisation failed!", cause);
            return FAILED;
        }
    }

    public String putOnQueue(final Optimisation.Sense sense, final byte[] contents, final FileFormat format) throws RecoverableCondition {

        String key = OptimisationService.generateKey();

        Problem problem = new Problem(key, sense, contents, format);

        if (myQueue.offer(problem)) {

            myStatusCache.put(key, Status.PENDING);

            return key;

        } else {

            throw new RecoverableCondition("Queue is full!");
        }
    }

    private void doOptimise(final Problem problem) {

        try {

            Sense sense = problem.getSense();
            byte[] contents = problem.getContents();
            FileFormat format = problem.getFormat();

            Optimisation.Result result = OptimisationService.doOptimise(sense, contents, format);

            myResultCache.put(problem.getKey(), result);

        } catch (RecoverableCondition cause) {

            BasicLogger.error("Optimisation failed!", cause);

            myResultCache.put(problem.getKey(), FAILED);

        } finally {

            myStatusCache.put(problem.getKey(), Status.DONE);
        }

    }

}
