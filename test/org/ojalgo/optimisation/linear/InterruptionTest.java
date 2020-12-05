package org.ojalgo.optimisation.linear;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileMPS;

public class InterruptionTest {

    private static class ThreadInterrupter implements Runnable {

        private final Thread threadToInterrupt;

        public ThreadInterrupter(final Thread threadToInterrupt) {
            this.threadToInterrupt = Objects.requireNonNull(threadToInterrupt);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(TIMEOUT_DURATION * 500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            threadToInterrupt.interrupt();
        }
    }

    public static final int TIMEOUT_DURATION = 4;

    private void launchSlowMinimization() {
        final ExpressionsBasedModel model = ModelFileMPS.makeModel("netlib", "25FV47.SIF", false);
        model.minimise();
    }

    @Test
    void slowMinimisationShouldBeSlow() throws InterruptedException {
        final Thread minimizer = new Thread(this::launchSlowMinimization);

        minimizer.start();
        minimizer.join(TIMEOUT_DURATION * 1_000);
        Assertions.assertTrue(minimizer.isAlive());

        minimizer.interrupt();
    }

    @Test
    @Timeout(value = TIMEOUT_DURATION, unit = TimeUnit.SECONDS)
    void slowMinimizationShouldBeInterrupted() throws InterruptedException {
        final Thread minimizer = new Thread(this::launchSlowMinimization);
        final Thread interrupter = new Thread(new ThreadInterrupter(minimizer));

        minimizer.start();
        interrupter.start();

        minimizer.join();
    }
}
