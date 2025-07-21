package app.algorithms;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class QuickSort implements SortAlgorithm {

    private final SortingVisualizer visualizer;
    private final SortController controller;
    private final int[] values;
    private final Rectangle[] bars;
    private final int delay;

    public QuickSort(SortingVisualizer visualizer, SortController controller, int delay) {
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
                quickSort(0, values.length - 1);

            } catch (Exception e) {
                Thread.currentThread().interrupt();
            } finally {
                Platform.runLater(() -> controller.setAllControlsDisabled(false));
            }
        }).start();
    }

    private void quickSort(int low, int high) throws InterruptedException {
        if (low < high) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private int partition(int low, int high) throws InterruptedException {
        int pivot = values[high];
        highlight(high, Color.ORANGE); // highlight pivot
        int i = low - 1;

        for (int j = low; j < high; j++) {
            highlight(j, Color.RED);
            Thread.sleep(delay);

            if (values[j] < pivot) {
                i++;
                swap(i, j);
                Thread.sleep(delay);
            }

            resetColor(j);
        }

        swap(i + 1, high);
        Thread.sleep(delay);
        resetColor(high);
        resetColor(i + 1);

        return i + 1;
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

    private void highlight(int i, Color color) {
        Platform.runLater(() -> bars[i].setFill(color));
    }

    private void resetColor(int i) {
        Platform.runLater(() -> bars[i].setFill(Color.CORNFLOWERBLUE));
    }

}
