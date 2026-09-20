package app.player;

import app.algorithms.SortTrace;
import app.algorithms.StoppedException;
import app.controller.SortController;
import app.view.SortingVisualizer;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Turns the steps an algorithm reports into the on-screen animation.
 *
 * This is the only place that knows about JavaFX, timing, the counters and
 * the stop button; the algorithms themselves know none of it. Every method
 * runs on the sorting worker thread and hands bar updates to the JavaFX
 * thread through {@link Platform#runLater}.
 */
public class AnimatedTrace implements SortTrace {

    private final SortController controller;
    private final StepGate gate;
    private final Rectangle[] bars;
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
        this.controller = controller;
        this.gate = gate;
        this.bars = visualizer.getBars();
        this.values = visualizer.getValues();
        this.delay = delay;
        this.marked = new boolean[bars.length];
        this.displayed = this.values.clone();
    }

    @Override
    public void compared(int i, int j) throws StoppedException {
        paint(i, SortingVisualizer.COMPARE_COLOR);
        paint(j, SortingVisualizer.COMPARE_COLOR);
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
            bars[i].setHeight(heightI);
            bars[j].setHeight(heightJ);
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

        Platform.runLater(() -> bars[index].setHeight(value));

        frame();
    }

    @Override
    public void mark(int index) {
        marked[index] = true;
        paint(index, SortingVisualizer.MARK_COLOR);
    }

    @Override
    public void unmark(int index) {
        marked[index] = false;
        paint(index, SortingVisualizer.DEFAULT_BAR_COLOR);
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

    /** Returns a bar to its resting colour, keeping any mark it still carries. */
    private void restore(int index) {
        paint(index, marked[index]
                ? SortingVisualizer.MARK_COLOR
                : SortingVisualizer.DEFAULT_BAR_COLOR);
    }

    private void paint(int index, Color color) {
        Platform.runLater(() -> bars[index].setFill(color));
    }
}
