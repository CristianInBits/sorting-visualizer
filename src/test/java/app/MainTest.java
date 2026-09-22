package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Starts the real application window, headless through Monocle.
 *
 * The About dialog is left alone: it opens with showAndWait, which blocks the
 * JavaFX thread until a person closes it, and all it holds is fixed text.
 */
@Timeout(60)
class MainTest {

    private Stage stage;

    @BeforeAll
    static void toolkit() {
        Fx.startToolkit();
    }

    @AfterEach
    void closeWindow() {
        if (stage != null) {
            Fx.onFx(stage::close);
        }
    }

    @Test
    @DisplayName("opens titled, with a minimum size, the bars in the middle and the controls below")
    void buildsTheWindow() {
        launch();

        assertEquals("Sorting Visualizer", Fx.get(stage::getTitle));
        assertEquals(780.0, Fx.get(stage::getMinWidth));
        assertEquals(520.0, Fx.get(stage::getMinHeight));

        BorderPane root = root();
        Node centre = Fx.get(root::getCenter);
        assertInstanceOf(StackPane.class, centre, "the bars should sit on a panel of their own");
        assertInstanceOf(SortingVisualizer.class, Fx.get(() -> ((StackPane) centre).getChildren().get(0)));
        assertInstanceOf(SortController.class, Fx.get(root::getBottom));

        assertNotNull(lookup("#themeToggle"), "the header should carry the theme toggle");
        assertNotNull(lookup("#aboutButton"), "the header should carry the About button");
    }

    @Test
    @DisplayName("starts in the light theme, with the shared stylesheet loaded first")
    void startsLight() {
        launch();

        assertStylesheets("base.css", "light.css");
        assertEquals("Dark mode", toggleText());
    }

    @Test
    @DisplayName("the toggle swaps the palette and relabels itself, and back again")
    void toggleSwapsThePalette() {
        launch();

        toggle();
        assertStylesheets("base.css", "dark.css");
        assertEquals("Light mode", toggleText());

        toggle();
        assertStylesheets("base.css", "light.css");
        assertEquals("Dark mode", toggleText());
    }

    /**
     * Guards against the leak where every click on the toggle added its
     * style class again, so the list grew for as long as the app ran.
     */
    @Test
    @DisplayName("toggling over and over never piles up stylesheets or style classes")
    void toggleNeverAccumulates() {
        launch();

        for (int i = 0; i < 21; i++) {
            toggle();
        }

        assertEquals(2, Fx.get(() -> stage.getScene().getStylesheets().size()),
                "one shared stylesheet and one palette, never more");

        List<String> classes = Fx.get(() -> List.copyOf(themeToggle().getStyleClass()));
        assertEquals(1, classes.stream().filter("theme-toggle"::equals).count(),
                "theme-toggle should appear once, not once per click: " + classes);
    }

    /**
     * Loading the right file is not the same as the colours reaching the
     * screen: a palette that fails to parse, or lacks a colour the shared
     * stylesheet looks up, still loads without complaint. So this reads the
     * background JavaFX actually resolved for the window.
     */
    @Test
    @DisplayName("the dark palette really does paint a darker window")
    void darkPaintsDarker() {
        launch();

        double light = backgroundBrightness();
        toggle();
        double dark = backgroundBrightness();

        assertTrue(dark < light, "dark background brightness " + dark + " should be below light " + light);
    }

    // ------------------------------------------------------------- helpers

    private void launch() {
        stage = Fx.get(() -> {
            Stage window = new Stage();
            new Main().start(window);
            return window;
        });
    }

    private BorderPane root() {
        return (BorderPane) Fx.get(() -> stage.getScene().getRoot());
    }

    private Node lookup(String selector) {
        return Fx.get(() -> stage.getScene().getRoot().lookup(selector));
    }

    /** Call on the JavaFX thread. */
    private ToggleButton themeToggle() {
        return (ToggleButton) stage.getScene().getRoot().lookup("#themeToggle");
    }

    private void toggle() {
        Fx.onFx(() -> themeToggle().fire());
    }

    private String toggleText() {
        return Fx.get(() -> themeToggle().getText());
    }

    private double backgroundBrightness() {
        return Fx.get(() -> {
            Region root = (Region) stage.getScene().getRoot();
            root.applyCss();
            Background background = root.getBackground();
            assertNotNull(background, "no background resolved: the stylesheets did not apply");
            return ((Color) background.getFills().get(0).getFill()).getBrightness();
        });
    }

    private void assertStylesheets(String... expected) {
        List<String> sheets = Fx.get(() -> List.copyOf(stage.getScene().getStylesheets()));
        assertEquals(expected.length, sheets.size(), "stylesheets loaded: " + sheets);
        for (int i = 0; i < expected.length; i++) {
            assertTrue(sheets.get(i).endsWith("/" + expected[i]),
                    "position " + i + " should be " + expected[i] + ", was " + sheets.get(i));
        }
    }
}
