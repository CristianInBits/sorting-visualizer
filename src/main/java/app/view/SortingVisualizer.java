package app.view;

import java.util.Random;

import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SortingVisualizer extends HBox {

    private static final int NUM_BARS = 50;
    private static final int MAX_HEIGHT = 300;
    private static final int BAR_WIDTH = 10;

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
            bar.setFill(Color.CORNFLOWERBLUE);
            bars[i] = bar;
            this.getChildren().add(bar);
        }
    }

    public void regenerateArray() {
        generateRandomArray();
        createBars();
    }

    public int[] getValues() {
        return values;
    }

    public Rectangle[] getBars() {
        return bars;
    }
}
