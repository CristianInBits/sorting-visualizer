package app.view;

import java.util.Random;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SortingVisualizer extends HBox {

    private static final int NUM_BARS = 50;
    private static final int MAX_HEIGHT = 300;
    private static final int BAR_WIDTH = 12;

    private static final Color DEFAULT_BAR_COLOR = Color.CORNFLOWERBLUE;

    private int[] values;
    private Rectangle[] bars;

    public SortingVisualizer() {
        this.setSpacing(2);
        this.setStyle("-fx-alignment: bottom-center;");
        generateRandomArray();
        createBars();
    }

    private void generateRandomArray() {
        values = new int[NUM_BARS];
        Random rand = new Random();
        for (int i = 0; i < NUM_BARS; i++) {
            values[i] = rand.nextInt(MAX_HEIGHT) + 10;
        }
    }

    private void createBars() {
        bars = new Rectangle[NUM_BARS];
        this.getChildren().clear();

        for (int i = 0; i < NUM_BARS; i++) {
            Rectangle bar = new Rectangle();
            bar.setWidth(BAR_WIDTH);
            bar.setHeight(values[i]);
            bar.setFill(DEFAULT_BAR_COLOR);
            bars[i] = bar;
            this.getChildren().add(bar);
        }
    }

    public void regenerateArray() {
        generateRandomArray();
        createBars();
    }

    /**
     * Restores every bar to its idle color.
     *
     * A run that is stopped early returns straight out of its loops, skipping
     * the pending resetColor calls, so highlighted bars would otherwise stay
     * red (or orange, for the Quick Sort pivot) until the array is regenerated.
     * Must be called on the JavaFX application thread.
     */
    public void resetBarColors() {
        for (Rectangle bar : bars) {
            bar.setFill(DEFAULT_BAR_COLOR);
        }
    }

    public int[] getValues() {
        return values;
    }

    public Rectangle[] getBars() {
        return bars;
    }
}
