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

    /** Idle bar. */
    public static final Color DEFAULT_BAR_COLOR = Color.CORNFLOWERBLUE;

    /** A bar taking part in the comparison being shown. */
    public static final Color COMPARE_COLOR = Color.RED;

    /** A bar the algorithm is holding onto: a pivot, or the current minimum. */
    public static final Color MARK_COLOR = Color.ORANGE;

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
     * Puts every bar back in step with its value: idle colour, and the height
     * the array says it should have.
     *
     * A stopped run abandons its loops wherever it happens to be, leaving bars
     * highlighted and, when Merge Sort is interrupted during a copy-back, a
     * few heights not yet applied. Redrawing from the array settles both.
     * Must be called on the JavaFX application thread.
     */
    public void refreshBars() {
        for (int i = 0; i < bars.length; i++) {
            bars[i].setHeight(values[i]);
            bars[i].setFill(DEFAULT_BAR_COLOR);
        }
    }

    public int[] getValues() {
        return values;
    }

    public Rectangle[] getBars() {
        return bars;
    }
}
