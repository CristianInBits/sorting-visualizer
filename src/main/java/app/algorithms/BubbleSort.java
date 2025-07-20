package app.algorithms;

import app.view.SortingVisualizer;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BubbleSort implements SortAlgorithm {

    private final SortingVisualizer visualizer;
    private final int[] values;
    private final Rectangle[] bars;
    private final int delay;

    public BubbleSort(SortingVisualizer visualizer, int delay) {
        this.visualizer = visualizer;
        this.values = visualizer.getValues();
        this.bars = visualizer.getBars();
        this.delay = delay;
    }

    @Override
    public void sort() throws InterruptedException {
        new Thread(() -> {
            try {
                for (int i = 0; i < values.length - 1; i++) {
                    for (int j = 0; j < values.length - i - 1; j++) {
                        highlight(j, j + 1, Color.RED);
                        Thread.sleep(delay);

                        if (values[j] > values[j + 1]) {
                            swap(j, j + 1);
                            Thread.sleep(delay);
                        }

                        resetColor(j, j + 1);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void highlight(int i, int j, Color color) {
        Platform.runLater(() -> {
            bars[i].setFill(color);
            bars[j].setFill(color);
        });
    }

    private void resetColor(int i, int j) {
        Platform.runLater(() -> {
            bars[i].setFill(Color.CORNFLOWERBLUE);
            bars[j].setFill(Color.CORNFLOWERBLUE);
        });
    }

    private void swap(int i, int j) {
        int temp = values[i];
        values[i] = values[j];
        values[j] = temp;

        Platform.runLater(() -> {
            double heightTemp = bars[i].getHeight();
            bars[i].setHeight(bars[j].getHeight());
            bars[j].setHeight(heightTemp);
        });
    }
}
