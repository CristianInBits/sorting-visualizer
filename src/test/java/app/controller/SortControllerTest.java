package app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import app.Fx;
import app.view.SortingVisualizer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Exercises the real control panel, headless through Monocle. */
@Timeout(60)
class SortControllerTest {

    @BeforeAll
    static void toolkit() {
        Fx.startToolkit();
    }

    @Test
    @DisplayName("the picker offers every algorithm, quadratic ones first")
    void pickerListsEveryAlgorithm() {
        Panel panel = Panel.build();

        List<String> offered = Fx.get(() -> List.copyOf(panel.picker().getItems()));

        assertEquals(List.of(
                "Bubble Sort",
                "Insertion Sort",
                "Selection Sort",
                "Merge Sort",
                "Quick Sort",
                "Heap Sort"), offered);
    }

    @Test
    @DisplayName("the counters add up and reset")
    void countersAddUp() {
        Panel panel = Panel.build();

        panel.controller().incrementComparisons();
        panel.controller().incrementComparisons();
        panel.controller().addMoves(2);
        panel.controller().addMoves(1);
        Fx.settle();

        assertEquals("2", panel.text("#comparisonsValue"));
        assertEquals("3", panel.text("#movesValue"));

        panel.controller().resetCounters();
        Fx.settle();

        assertEquals("0", panel.text("#comparisonsValue"));
        assertEquals("0", panel.text("#movesValue"));
    }

    @Test
    @DisplayName("a move that moved nothing is not counted")
    void ignoresEmptyMoves() {
        Panel panel = Panel.build();

        panel.controller().addMoves(0);
        panel.controller().addMoves(-3);
        Fx.settle();

        assertEquals("0", panel.text("#movesValue"));
    }

    @Test
    @DisplayName("a run disables the setup but leaves Stop and stepping reachable")
    void stoppingStaysAvailableDuringARun() {
        Panel panel = Panel.build();

        Fx.onFx(() -> panel.controller().setAllControlsDisabled(true));

        assertTrue(panel.disabled("#startButton"));
        assertTrue(panel.disabled("#newArrayButton"));
        assertTrue(panel.disabled("#algorithmSelector"));
        assertTrue(panel.disabled("#sizeSlider"));
        assertTrue(panel.disabled("#delaySlider"));

        // Losing these would leave a running sort with no way out.
        assertFalse(panel.disabled("#stopButton"), "Stop must stay usable");
        assertFalse(panel.disabled("#stepModeCheck"), "step mode must stay usable");

        Fx.onFx(() -> panel.controller().setAllControlsDisabled(false));
        assertFalse(panel.disabled("#startButton"));
    }

    @Test
    @DisplayName("pressing Start sorts the array and hands the controls back")
    void startSortsTheArray() {
        Panel panel = Panel.build();

        Fx.onFx(() -> {
            panel.slider("#sizeSlider").setValue(SortingVisualizer.MIN_BAR_COUNT);
            panel.slider("#delaySlider").setValue(1);
            panel.picker().setValue("Quick Sort");
        });
        Fx.settle();

        int[] before = panel.visualizer().getValues().clone();
        Fx.onFx(() -> panel.button("#startButton").fire());

        // startSort disables the controls before it returns, so the button
        // coming back is the signal that the worker has finished.
        assertTrue(panel.disabled("#startButton"), "the run should have started");
        panel.awaitIdle();

        int[] after = panel.visualizer().getValues();
        int[] expected = before.clone();
        Arrays.sort(expected);

        assertArrayEquals(expected, after);
        assertFalse(panel.disabled("#algorithmSelector"), "the setup should be usable again");
        assertTrue(Integer.parseInt(panel.text("#comparisonsValue")) > 0, "it should have counted something");
    }

    @Test
    @DisplayName("pressing Start with nothing selected does nothing at all")
    void startWithoutAnAlgorithmIsIgnored() {
        Panel panel = Panel.build();

        Fx.onFx(() -> panel.button("#startButton").fire());

        assertFalse(panel.disabled("#startButton"), "no run should have begun");
    }

    private static void assertArrayEquals(int[] expected, int[] actual) {
        assertTrue(Arrays.equals(expected, actual),
                "expected " + Arrays.toString(expected) + " but was " + Arrays.toString(actual));
    }

    /** The visualizer and its control panel, with lookups by id. */
    private record Panel(SortingVisualizer visualizer, SortController controller) {

        static Panel build() {
            return Fx.get(() -> {
                SortingVisualizer visualizer = new SortingVisualizer();
                return new Panel(visualizer, new SortController(visualizer));
            });
        }

        Node node(String id) {
            Node found = Fx.get(() -> controller.lookup(id));
            if (found == null) {
                throw new AssertionError("no control with id " + id);
            }
            return found;
        }

        boolean disabled(String id) {
            Node node = node(id);
            return Fx.get(node::isDisabled);
        }

        String text(String id) {
            return Fx.get(() -> ((Label) node(id)).getText());
        }

        Button button(String id) {
            return (Button) node(id);
        }

        Slider slider(String id) {
            return (Slider) node(id);
        }

        @SuppressWarnings("unchecked")
        ComboBox<String> picker() {
            return (ComboBox<String>) node("#algorithmSelector");
        }

        void awaitIdle() {
            long deadline = System.currentTimeMillis() + 40_000;
            while (System.currentTimeMillis() < deadline) {
                if (!disabled("#startButton")) {
                    return;
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
            }
            throw new AssertionError("the run never finished");
        }
    }
}
