package app.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import app.Fx;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Exercises the real node, headless through Monocle. */
class SortingVisualizerTest {

    @BeforeAll
    static void toolkit() {
        Fx.startToolkit();
    }

    @Test
    @DisplayName("starts at the default size with every value inside the range")
    void startsWithSaneValues() {
        int[] values = Fx.get(() -> new SortingVisualizer().getValues());

        assertEquals(SortingVisualizer.DEFAULT_BAR_COUNT, values.length);
        for (int value : values) {
            assertTrue(value >= 10 && value <= 300, "value out of range: " + value);
        }
    }

    @Test
    @DisplayName("the count is clamped to the range the picker offers")
    void clampsTheCount() {
        SortingVisualizer visualizer = Fx.get(SortingVisualizer::new);

        Fx.onFx(() -> visualizer.setBarCount(-5));
        assertEquals(SortingVisualizer.MIN_BAR_COUNT, visualizer.getBarCount());

        Fx.onFx(() -> visualizer.setBarCount(10_000));
        assertEquals(SortingVisualizer.MAX_BAR_COUNT, visualizer.getBarCount());
    }

    @Test
    @DisplayName("asking for the size it already has leaves the array untouched")
    void sameSizeDoesNotReshuffle() {
        SortingVisualizer visualizer = Fx.get(SortingVisualizer::new);
        int[] before = visualizer.getValues().clone();

        Fx.onFx(() -> visualizer.setBarCount(SortingVisualizer.DEFAULT_BAR_COUNT));

        assertArrayUnchanged(before, visualizer.getValues());
    }

    @Test
    @DisplayName("a new size rebuilds both the values and the bars")
    void newSizeRebuilds() {
        SortingVisualizer visualizer = Fx.get(SortingVisualizer::new);

        Fx.onFx(() -> visualizer.setBarCount(23));

        assertEquals(23, visualizer.getBarCount());
        assertEquals(23, visualizer.getValues().length);
        assertEquals(23, Fx.get(() -> visualizer.getChildrenUnmodifiable().size()));
    }

    @Test
    @DisplayName("a bar holds one state at a time and never collects duplicates")
    void barStateIsExclusive() {
        SortingVisualizer visualizer = Fx.get(SortingVisualizer::new);

        Fx.onFx(() -> {
            for (int i = 0; i < 50; i++) {
                visualizer.setBarState(0, SortingVisualizer.COMPARING_CLASS);
                visualizer.setBarState(0, SortingVisualizer.MARKED_CLASS);
            }
        });

        List<String> classes = Fx.get(() -> List.copyOf(
                visualizer.getChildrenUnmodifiable().get(0).getStyleClass()));

        assertEquals(1, count(classes, SortingVisualizer.MARKED_CLASS), "marked should appear once: " + classes);
        assertEquals(0, count(classes, SortingVisualizer.COMPARING_CLASS), "comparing should be gone: " + classes);
        assertEquals(1, count(classes, SortingVisualizer.BAR_CLASS), "the base class should survive: " + classes);

        Fx.onFx(() -> visualizer.setBarState(0, null));
        List<String> cleared = Fx.get(() -> List.copyOf(
                visualizer.getChildrenUnmodifiable().get(0).getStyleClass()));
        assertFalse(cleared.contains(SortingVisualizer.MARKED_CLASS));
    }

    @Test
    @DisplayName("refreshBars redraws the heights the values imply")
    void refreshRestoresHeights() {
        SortingVisualizer visualizer = Fx.get(SortingVisualizer::new);
        layoutAt(visualizer, 900, 400);

        double before = barWidth(visualizer, 0) > 0 ? height(visualizer, 3) : -1;
        Fx.onFx(() -> visualizer.showValue(3, 300));
        assertNotEquals(before, height(visualizer, 3), "the bar should have changed");

        Fx.onFx(visualizer::refreshBars);
        assertEquals(before, height(visualizer, 3), 0.01, "refresh should put it back");
    }

    /**
     * The one that matters. It measures where the bars actually end up after a
     * layout pass, not what the width formula intended, because the formula was
     * right when the bars last spilled out of the panel: HBox was rounding the
     * gap up to a whole pixel and the excess had nowhere to go.
     */
    @Test
    @DisplayName("the bars stay inside the row at every size and every width")
    void barsStayInsideTheRow() {
        SortingVisualizer visualizer = Fx.get(SortingVisualizer::new);

        for (double width : new double[] {380, 700, 1006, 1400}) {
            for (int count : new int[] {10, 50, 100, 117, 140, 150}) {
                Fx.onFx(() -> visualizer.setBarCount(count));
                layoutAt(visualizer, width, 420);

                List<Node> bars = Fx.get(() -> List.copyOf(visualizer.getChildrenUnmodifiable()));
                double rowWidth = Fx.get(visualizer::getWidth);

                double left = Fx.get(() -> bars.get(0).getBoundsInParent().getMinX());
                double right = Fx.get(() -> bars.get(bars.size() - 1).getBoundsInParent().getMaxX());

                String where = count + " bars in " + (int) rowWidth + "px";
                assertTrue(left >= -0.5, "bars start " + (-left) + "px past the left edge: " + where);
                assertTrue(right <= rowWidth + 0.5,
                        "bars run " + (right - rowWidth) + "px past the right edge: " + where);
            }
        }
    }

    // ------------------------------------------------------------- helpers

    private static void layoutAt(SortingVisualizer visualizer, double width, double height) {
        Fx.onFx(() -> {
            StackPane root = new StackPane(visualizer);
            new Scene(root, width, height);
            root.resize(width, height);
            root.applyCss();
            // Twice on purpose: the first pass gives the row its width, which
            // is what the bars are then sized from; the second places them.
            root.layout();
            root.layout();
        });
    }

    private static double barWidth(SortingVisualizer visualizer, int index) {
        return Fx.get(() -> visualizer.getChildrenUnmodifiable().get(index).getBoundsInParent().getWidth());
    }

    private static double height(SortingVisualizer visualizer, int index) {
        return Fx.get(() -> visualizer.getChildrenUnmodifiable().get(index).getBoundsInParent().getHeight());
    }

    private static int count(List<String> classes, String wanted) {
        return (int) classes.stream().filter(wanted::equals).count();
    }

    private static void assertArrayUnchanged(int[] before, int[] after) {
        assertTrue(Arrays.equals(before, after),
                "expected the array to be left alone, was " + Arrays.toString(after));
    }
}
