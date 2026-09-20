package app.player;

import app.algorithms.SortTrace;
import app.algorithms.StoppedException;
import app.controller.SortController;
import app.view.SortingVisualizer;

import javafx.application.Platform;

/**
 * Turns the steps an algorithm reports into the on-screen animation.
 *
 * This is the only place that knows about JavaFX, timing, the counters and
 * the stop button; the algorithms themselves know none of it. Every method
 * runs on the sorting worker thread and hands bar updates to the JavaFX
 * thread through {@link Platform#runLater}.
 */
public class AnimatedTrace implements SortTrace {

    private final SortingVisualizer visualizer;
    private final SortController controller;
    private final StepGate gate;
    private final int[] values;
    private final int delay;

    /** Positions currently flagged by the algorithm, so a reset restores their mark. */
    private final boolean[] marked;

    /**
     * What each bar is currently showing. Kept here rather than read back from
     * the Rectangles, because reading a live node off the JavaFX thread is not
     * supported and would also race with the updates still queued behind us.
     */
    private final int[] displayed;

    public AnimatedTrace(SortingVisualizer visualizer, SortController controller, StepGate gate, int delay) {
        this.visualizer = visualizer;
        this.controller = controller;
        this.gate = gate;
        this.values = visualizer.getValues();
        this.delay = delay;
        this.marked = new boolean[this.values.length];
        this.displayed = this.values.clone();
    }

    @Override
    public void compared(int i, int j) throws StoppedException {
        comparing(i);
        comparing(j);
        controller.incrementComparisons();

        frame();

        restore(i);
        restore(j);
    }

    @Override
    public void swapped(int i, int j) throws StoppedException {
        // The algorithm has already exchanged the values, so the new heights
        // are read and captured now; reading them inside the runLater would
        // pick up whatever the array looks like by the time it runs.
        int heightI = values[i];
        int heightJ = values[j];

        int moved = 0;
        if (displayed[i] != heightI) {
            displayed[i] = heightI;
            moved++;
        }
        if (displayed[j] != heightJ) {
            displayed[j] = heightJ;
            moved++;
        }
        controller.addMoves(moved);

        Platform.runLater(() -> {
            visualizer.showValue(i, heightI);
            visualizer.showValue(j, heightJ);
        });

        frame();
    }

    @Override
    public void wrote(int index, int value) throws StoppedException {
        // A write that stores the value already there moves nothing on screen
        // and must not be counted.
        int moved = displayed[index] != value ? 1 : 0;
        displayed[index] = value;
        controller.addMoves(moved);

        Platform.runLater(() -> visualizer.showValue(index, value));

        frame();
    }

    @Override
    public void mark(int index) {
        marked[index] = true;
        state(index, SortingVisualizer.MARKED_CLASS);
    }

    @Override
    public void unmark(int index) {
        marked[index] = false;
        state(index, null);
    }

    /**
     * One animation frame: honour step-by-step mode, wait out the delay, and
     * give the user a chance to stop. Every reported step passes through here,
     * so a stop is noticed within one step no matter where the algorithm is.
     */
    private void frame() throws StoppedException {
        if (gate.isStopRequested()) {
            throw new StoppedException();
        }

        try {
            gate.await();
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StoppedException();
        }

        if (gate.isStopRequested()) {
            throw new StoppedException();
        }
    }

    /**
     * Shows a bar as taking part in the comparison, unless the algorithm is
     * holding it. A pivot compared against every element would otherwise
     * spend the whole partition flashing red instead of staying marked.
     */
    private void comparing(int index) {
        if (!marked[index]) {
            state(index, SortingVisualizer.COMPARING_CLASS);
        }
    }

    /** Returns a bar to its resting look, keeping any mark it still carries. */
    private void restore(int index) {
        state(index, marked[index] ? SortingVisualizer.MARKED_CLASS : null);
    }

    /** Puts a bar in exactly one visual state; null means idle. */
    private void state(int index, String styleClass) {
        Platform.runLater(() -> visualizer.setBarState(index, styleClass));
    }
}
