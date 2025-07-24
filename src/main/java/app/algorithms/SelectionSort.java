package app.algorithms;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SelectionSort implements SortAlgorithm {

    private final SortingVisualizer visualizer;
    private final SortController controller;
    private final int[] values;
    private final Rectangle[] bars;
    private final int delay;

    public SelectionSort(SortingVisualizer visualizer, SortController controller, int delay) {
        this.visualizer = visualizer;
        this.controller = controller;
        this.values = visualizer.getValues();
        this.bars = visualizer.getBars();
        this.delay = delay;
    }

    @Override
    public void sort() {
        new Thread(() -> {
            try {
                Platform.runLater(() -> controller.setAllControlsDisabled(true));
                controller.resetCounters();

                for (int i = 0; i < values.length - 1; i++) {
                    if (controller.isStopRequested())
                        return;

                    int minIndex = i;
                    highlight(minIndex, Color.ORANGE); // current min
                    controller.waitForNextStep();
                    Thread.sleep(delay);

                    for (int j = i + 1; j < values.length; j++) {
                        if (controller.isStopRequested())
                            return;

                        highlight(j, Color.RED);
                        controller.incrementComparisons();
                        controller.waitForNextStep();
                        Thread.sleep(delay);

                        if (values[j] < values[minIndex]) {
                            resetColor(minIndex);
                            minIndex = j;
                            highlight(minIndex, Color.ORANGE);
                        } else {
                            resetColor(j);
                        }
                    }

                    if (minIndex != i) {
                        swap(i, minIndex);
                        controller.incrementSwaps();
                        controller.waitForNextStep();
                        Thread.sleep(delay);
                    }

                    resetColor(i);
                    resetColor(minIndex);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                Platform.runLater(controller::resetControlsAndState);
            }
        }).start();
    }

    private void highlight(int i, Color color) {
        if (i >= 0 && i < bars.length) {
            Platform.runLater(() -> bars[i].setFill(color));
        }
    }

    private void resetColor(int i) {
        if (i >= 0 && i < bars.length) {
            Platform.runLater(() -> bars[i].setFill(Color.CORNFLOWERBLUE));
        }
    }

    private void swap(int i, int j) {
        int temp = values[i];
        values[i] = values[j];
        values[j] = temp;

        Platform.runLater(() -> {
            double h = bars[i].getHeight();
            bars[i].setHeight(bars[j].getHeight());
            bars[j].setHeight(h);
        });
    }
}
