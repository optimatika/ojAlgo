package org.ojalgo.optimisation.linear;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation.Result;

/**
 * Now and then the model used with these tests needs to be changed – ojAlgo gets better and computers faster,
 * causing the model to solve too fast for these interruption tests.
 */
@Tag("unstable")
public class InterruptionTest extends OptimisationLinearTests implements ModelFileTest {

    private static class ThreadInterrupter implements Runnable {

        private final Thread threadToInterrupt;

        public ThreadInterrupter(final Thread threadToInterrupt) {
            this.threadToInterrupt = Objects.requireNonNull(threadToInterrupt);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(4 * 500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            threadToInterrupt.interrupt();
        }
    }

    private static ExpressionsBasedModel makeModel() {
        return ModelFileTest.makeModel("netlib", "CRE-D.SIF", false);
    }

    private void launchSlowMinimization() {
        ExpressionsBasedModel model = InterruptionTest.makeModel();
        Result result = model.minimise();
        TestUtils.assertStateNotLessThanFeasible(result);
    }

    @Test
    void slowMinimisationShouldBeSlow() throws InterruptedException {

        Thread minimizer = new Thread(this::launchSlowMinimization);

        minimizer.start();
        minimizer.join(7 * 100); // Wait some time, but not so long that the solver finishes
        Assertions.assertTrue(minimizer.isAlive());

        minimizer.interrupt();
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void slowMinimizationShouldBeInterrupted() throws InterruptedException {

        Thread minimizer = new Thread(this::launchSlowMinimization);
        Thread interrupter = new Thread(new ThreadInterrupter(minimizer));

        minimizer.start();
        interrupter.start();

        minimizer.join();
    }
}
