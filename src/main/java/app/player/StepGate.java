package app.player;

/**
 * Paces a sorting run: step-by-step mode, the Next Step permit, and the stop
 * request.
 *
 * Deliberately free of JavaFX, so the worker thread never has to read a live
 * control and the whole thing can be tested with real threads. The controls
 * call the mutators from the JavaFX thread; the sorting worker calls
 * {@link #await()} and {@link #isStopRequested()}.
 *
 * All state is guarded by one monitor, including the flags themselves: the
 * previous version set its "waiting" flag outside the lock, so a Next Step
 * that landed just before the worker parked was silently dropped.
 */
public class StepGate {

    private final Object lock = new Object();

    private boolean stepMode;
    private boolean stepPending;
    private boolean stopRequested;

    /**
     * Turns step-by-step mode on or off. Switching it off releases a worker
     * that is already parked, which otherwise stayed frozen until the user
     * pressed Next Step or Stop.
     */
    public void setStepMode(boolean enabled) {
        synchronized (lock) {
            stepMode = enabled;
            if (!enabled) {
                lock.notifyAll();
            }
        }
    }

    public boolean isStepMode() {
        synchronized (lock) {
            return stepMode;
        }
    }

    /**
     * Grants one step. Recorded as a permit rather than a wake-up, so a click
     * that arrives before the worker parks still counts.
     */
    public void step() {
        synchronized (lock) {
            stepPending = true;
            lock.notifyAll();
        }
    }

    public void requestStop() {
        synchronized (lock) {
            stopRequested = true;
            lock.notifyAll();
        }
    }

    public boolean isStopRequested() {
        synchronized (lock) {
            return stopRequested;
        }
    }

    /** Clears the per-run state, leaving the step-mode setting alone. */
    public void reset() {
        synchronized (lock) {
            stepPending = false;
            stopRequested = false;
        }
    }

    /**
     * Blocks until the run may take its next step: at once when step mode is
     * off, otherwise when Next Step is pressed, step mode is switched off, or
     * the run is stopped.
     */
    public void await() throws InterruptedException {
        synchronized (lock) {
            while (stepMode && !stepPending && !stopRequested) {
                lock.wait();
            }
            stepPending = false;
        }
    }
}
