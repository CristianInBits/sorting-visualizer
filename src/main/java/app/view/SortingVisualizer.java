package app.view;

import java.util.Random;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

/**
 * Draws the array as a row of bars.
 *
 * Values are logical, in MIN_VALUE..MAX_VALUE. A bar's size in pixels is
 * derived from the space actually available, so the drawing fills the window
 * and keeps filling it when the window is resized, rather than sitting at a
 * fixed 698px inside whatever room it is given.
 *
 * Callers work in values and never touch the Rectangles: the conversion to
 * pixels lives here, in the only class that knows how big the row is.
 */
public class SortingVisualizer extends HBox {

    /** How many bars the array starts with. */
    public static final int DEFAULT_BAR_COUNT = 50;

    /** Range the picker offers. Past ~150 the bars are thinner than the gaps. */
    public static final int MIN_BAR_COUNT = 10;
    public static final int MAX_BAR_COUNT = 150;

    private static final int MIN_VALUE = 10;
    private static final int MAX_VALUE = 300;

    /**
     * Widest gap between bars. It shrinks with the bars themselves, because a
     * fixed 3px gap swallows more than 40% of the width once there are 150 of
     * them.
     */
    private static final double MAX_GAP = 3;

    /** Never let a bar with a real value render as nothing at all. */
    private static final double MIN_BAR_HEIGHT = 2;

    // Bar colours live in style.css and dark.css so they follow the theme.
    // The code only moves style classes around; it never sets a fill.

    /** Every bar carries this. */
    public static final String BAR_CLASS = "sort-bar";

    /** Added while a bar takes part in the comparison being shown. */
    public static final String COMPARING_CLASS = "comparing";

    /** Added while the algorithm holds a bar: a pivot, or the current minimum. */
    public static final String MARKED_CLASS = "marked";

    private int barCount = DEFAULT_BAR_COUNT;
    private int[] values;
    private Rectangle[] bars;

    public SortingVisualizer() {
        getStyleClass().add("visualizer");
        setAlignment(Pos.BOTTOM_CENTER);

        // Let the parent shrink us; without this the fixed bar widths would
        // set a floor on how narrow the window can get.
        setMinSize(0, 0);

        // Lay the bars out on exact coordinates. Snapping rounds the gap to a
        // whole pixel, and at 150 bars a gap of 1.7px rounded up to 2 added
        // almost 50px of width the bars could not give back: they spilled out
        // of the panel on both sides, since the row is centred.
        setSnapToPixel(false);

        generateRandomArray();
        createBars();

        widthProperty().addListener((property, was, now) -> resizeBars());
        heightProperty().addListener((property, was, now) -> resizeBars());
    }

    private void generateRandomArray() {
        values = new int[barCount];
        Random rand = new Random();
        for (int i = 0; i < barCount; i++) {
            values[i] = MIN_VALUE + rand.nextInt(MAX_VALUE - MIN_VALUE + 1);
        }
    }

    private void createBars() {
        bars = new Rectangle[barCount];
        getChildren().clear();

        for (int i = 0; i < barCount; i++) {
            Rectangle bar = new Rectangle();
            bar.getStyleClass().add(BAR_CLASS);
            bars[i] = bar;
            getChildren().add(bar);
        }
        resizeBars();
    }

    /** Recomputes every bar from the current size of the row. */
    private void resizeBars() {
        // Each bar owns a slot and gives part of it to the gap on its right,
        // so the row always measures width - gap however tight it gets. There
        // is deliberately no floor on the width: clamping it to a minimum was
        // what pushed the bars back out of the panel once the slot fell below
        // that minimum.
        double slot = getWidth() / barCount;
        double gap = Math.min(MAX_GAP, slot / 4);
        double barWidth = Math.max(0, slot - gap);

        setSpacing(gap);
        for (int i = 0; i < bars.length; i++) {
            bars[i].setWidth(barWidth);
            bars[i].setHeight(pixelsFor(values[i]));
        }
    }

    /** Converts a logical value into the height it should occupy right now. */
    private double pixelsFor(int value) {
        double usable = getHeight();
        if (usable <= 0) {
            // Before the first layout pass, fall back to the logical scale so
            // the very first frame is not drawn flat.
            return value;
        }
        return Math.max(MIN_BAR_HEIGHT, usable * value / (double) MAX_VALUE);
    }

    public void regenerateArray() {
        generateRandomArray();
        resizeBars();
        for (Rectangle bar : bars) {
            bar.getStyleClass().removeAll(COMPARING_CLASS, MARKED_CLASS);
        }
    }

    public int getBarCount() {
        return barCount;
    }

    /**
     * Rebuilds the array at a new size. Clamped to the offered range, and a
     * no-op when the size has not actually changed, so dragging the picker
     * across a value it already holds does not reshuffle the bars.
     */
    public void setBarCount(int count) {
        int wanted = Math.max(MIN_BAR_COUNT, Math.min(MAX_BAR_COUNT, count));
        if (wanted == barCount) {
            return;
        }

        barCount = wanted;
        generateRandomArray();
        createBars();
    }

    /** Draws the given value at that position. */
    public void showValue(int index, int value) {
        bars[index].setHeight(pixelsFor(value));
    }

    /**
     * Puts a bar in exactly one visual state; null means idle. Removing both
     * state classes first also keeps the list from collecting duplicates over
     * a long run.
     */
    public void setBarState(int index, String styleClass) {
        var classes = bars[index].getStyleClass();
        classes.removeAll(COMPARING_CLASS, MARKED_CLASS);
        if (styleClass != null) {
            classes.add(styleClass);
        }
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
            bars[i].setHeight(pixelsFor(values[i]));
            bars[i].getStyleClass().removeAll(COMPARING_CLASS, MARKED_CLASS);
        }
    }

    public int[] getValues() {
        return values;
    }
}
