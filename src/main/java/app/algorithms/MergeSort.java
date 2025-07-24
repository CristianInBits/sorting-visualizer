package app.algorithms;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MergeSort implements SortAlgorithm {

    private final SortingVisualizer visualizer;
    private final SortController controller;
    private final int[] values;
    private final Rectangle[] bars;
    private final int delay;

    public MergeSort(SortingVisualizer visualizer, SortController controller, int delay) {
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
                mergeSort(0, values.length - 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                Platform.runLater(controller::resetControlsAndState);
            }
        }).start();
    }

    private void mergeSort(int left, int right) throws InterruptedException {
        if (controller.isStopRequested())
            return;

        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(left, mid);
            mergeSort(mid + 1, right);
            merge(left, mid, right);
        }
    }

    private void merge(int left, int mid, int right) throws InterruptedException {
        if (controller.isStopRequested())
            return;

        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;

        while (i <= mid && j <= right) {
            if (controller.isStopRequested())
                return;

            highlight(i, Color.RED);
            highlight(j, Color.RED);
            controller.incrementComparisons();
            controller.waitForNextStep();
            Thread.sleep(delay);

            if (values[i] <= values[j]) {
                temp[k++] = values[i++];
            } else {
                temp[k++] = values[j++];
            }

            if (i - 1 >= 0 && i - 1 < bars.length)
                resetColor(i - 1);
            if (j - 1 >= 0 && j - 1 < bars.length)
                resetColor(j - 1);
        }

        while (i <= mid) {
            if (controller.isStopRequested())
                return;

            highlight(i, Color.RED);
            controller.waitForNextStep();
            Thread.sleep(delay);
            temp[k++] = values[i++];
            if (i - 1 >= 0 && i - 1 < bars.length)
                resetColor(i - 1);
        }

        while (j <= right) {
            if (controller.isStopRequested())
                return;

            highlight(j, Color.RED);
            controller.waitForNextStep();
            Thread.sleep(delay);
            temp[k++] = values[j++];
            if (j - 1 >= 0 && j - 1 < bars.length)
                resetColor(j - 1);
        }

        for (int m = 0; m < temp.length; m++) {
            if (controller.isStopRequested())
                return;

            values[left + m] = temp[m];
            controller.incrementSwaps();

            final int barIndex = left + m;
            final double height = temp[m];

            Platform.runLater(() -> bars[barIndex].setHeight(height));
            controller.waitForNextStep();
            Thread.sleep(delay);
        }
    }

    private void highlight(int index, Color color) {
        if (index >= 0 && index < bars.length) {
            Platform.runLater(() -> bars[index].setFill(color));
        }
    }

    private void resetColor(int index) {
        if (index >= 0 && index < bars.length) {
            Platform.runLater(() -> bars[index].setFill(Color.CORNFLOWERBLUE));
        }
    }
}
