package app.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Exercises the pacing gate with real threads. No JavaFX involved. */
@Timeout(10)
class StepGateTest {

    private final StepGate gate = new StepGate();

    @Test
    @DisplayName("with step mode off, a step never blocks")
    void passesStraightThroughWhenStepModeIsOff() throws InterruptedException {
        gate.await();
        gate.await();
    }

    @Test
    @DisplayName("a Next Step that arrives before the worker parks is not lost")
    void grantedStepIsRemembered() throws InterruptedException {
        gate.setStepMode(true);

        // The click lands first; the worker only reaches await() afterwards.
        gate.step();
        gate.await();

        // ...and it was a single permit, not a standing one.
        assertTrue(parks(), "the permit should have been consumed by the first step");
    }

    @Test
    @DisplayName("in step mode the worker waits until Next Step is pressed")
    void blocksUntilStepped() throws InterruptedException {
        gate.setStepMode(true);
        assertTrue(parks(), "the worker should be parked in step mode");

        AtomicBoolean released = new AtomicBoolean();
        CountDownLatch done = new CountDownLatch(1);
        Thread worker = worker(released, done);

        waitUntilParked(worker);
        assertFalse(released.get(), "released before Next Step was pressed");

        gate.step();
        assertTrue(done.await(5, TimeUnit.SECONDS), "Next Step did not release the worker");
        assertTrue(released.get());
    }

    @Test
    @DisplayName("switching step mode off releases a worker already parked")
    void leavingStepModeReleasesTheWorker() throws InterruptedException {
        gate.setStepMode(true);

        AtomicBoolean released = new AtomicBoolean();
        CountDownLatch done = new CountDownLatch(1);
        Thread worker = worker(released, done);

        waitUntilParked(worker);
        gate.setStepMode(false);

        assertTrue(done.await(5, TimeUnit.SECONDS), "unchecking step mode left the run frozen");
    }

    @Test
    @DisplayName("stopping releases a worker already parked")
    void stopReleasesTheWorker() throws InterruptedException {
        gate.setStepMode(true);

        AtomicBoolean released = new AtomicBoolean();
        CountDownLatch done = new CountDownLatch(1);
        Thread worker = worker(released, done);

        waitUntilParked(worker);
        gate.requestStop();

        assertTrue(done.await(5, TimeUnit.SECONDS), "Stop did not release the worker");
        assertTrue(gate.isStopRequested());
    }

    @Test
    @DisplayName("reset clears the stop and the pending step, but not the mode")
    void resetClearsRunState() throws InterruptedException {
        gate.setStepMode(true);
        gate.step();
        gate.requestStop();

        gate.reset();

        assertFalse(gate.isStopRequested(), "stop survived the reset");
        assertTrue(gate.isStepMode(), "the step-mode setting should outlive a run");
        assertTrue(parks(), "a pending step survived the reset");
    }

    /** True when await() blocks rather than returning promptly. */
    private boolean parks() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        Thread t = worker(new AtomicBoolean(), done);
        boolean returned = done.await(300, TimeUnit.MILLISECONDS);
        t.interrupt();
        t.join(1000);
        return !returned;
    }

    private Thread worker(AtomicBoolean released, CountDownLatch done) {
        Thread t = new Thread(() -> {
            try {
                gate.await();
                released.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void waitUntilParked(Thread worker) throws InterruptedException {
        for (int i = 0; i < 200 && worker.getState() != Thread.State.WAITING; i++) {
            Thread.sleep(10);
        }
    }
}
