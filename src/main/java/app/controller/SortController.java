package app.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import app.algorithms.BubbleSort;
import app.algorithms.MergeSort;
import app.algorithms.QuickSort;
import app.algorithms.SelectionSort;
import app.algorithms.SortAlgorithm;
import app.algorithms.SortTrace;
import app.algorithms.StoppedException;
import app.player.AnimatedTrace;
import app.player.StepGate;
import app.view.SortingVisualizer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The control panel below the bars.
 *
 * Laid out as two rows rather than one long strip: what to run on top, and
 * how to run it plus the resulting counts below, so related controls sit
 * together instead of being lined up in the order they were written.
 */
public class SortController extends VBox {

    /** What the picker offers, in listed order. The algorithms are stateless. */
    private static final Map<String, SortAlgorithm> ALGORITHMS = algorithms();

    private static Map<String, SortAlgorithm> algorithms() {
        Map<String, SortAlgorithm> byName = new LinkedHashMap<>();
        byName.put("Bubble Sort", new BubbleSort());
        byName.put("Quick Sort", new QuickSort());
        byName.put("Selection Sort", new SelectionSort());
        byName.put("Merge Sort", new MergeSort());
        return Collections.unmodifiableMap(byName);
    }

    /** Shared height for the controls on the setup row, so they line up. */
    private static final double CONTROL_HEIGHT = 32;

    private final Button newArrayButton = new Button("New array");
    private final Button startButton = new Button("Start");
    private final Button stopButton = new Button("Stop");
    private final Button nextStepButton = new Button("Next step");
    private final CheckBox stepModeCheck = new CheckBox("Step by step");
    private final ComboBox<String> algorithmSelector = new ComboBox<>();
    private final Slider speedSlider = new Slider(1, 200, 30);

    private final SortingVisualizer visualizer;

    /** Owns step-by-step mode and the stop request, off the JavaFX thread. */
    private final StepGate gate = new StepGate();

    private int comparisons = 0;
    private int moves = 0;

    private final Label comparisonsLabel = new Label("0");
    private final Label movesLabel = new Label("0");
    private final Label delayLabel = new Label();

    public SortController(SortingVisualizer visualizer) {
        this.visualizer = visualizer;

        getStyleClass().add("controls");

        buildAlgorithmSelector();
        buildSpeedSlider();
        wireActions();

        getChildren().addAll(setupRow(), runRow());
    }

    // ---------------------------------------------------------------- layout

    /** Top row: what to run. */
    private HBox setupRow() {
        HBox row = new HBox(field("Algorithm", algorithmSelector), speedField());
        row.getStyleClass().add("control-row");
        row.setAlignment(Pos.BOTTOM_LEFT);
        return row;
    }

    /** Bottom row: how to run it, and what it cost. */
    private HBox runRow() {
        HBox primary = new HBox(startButton, stopButton, newArrayButton);
        primary.getStyleClass().add("button-group");
        primary.setAlignment(Pos.CENTER_LEFT);

        HBox stepping = new HBox(stepModeCheck, nextStepButton);
        stepping.getStyleClass().add("button-group");
        stepping.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(stat("Comparisons", comparisonsLabel), stat("Moves", movesLabel));
        stats.getStyleClass().add("stats");

        HBox row = new HBox(primary, divider(), stepping, spacer(), stats);
        row.getStyleClass().add("control-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** A control with a small caption above it. */
    private VBox field(String caption, Node control) {
        Label label = new Label(caption);
        label.getStyleClass().add("field-caption");

        VBox box = new VBox(label, control);
        box.getStyleClass().add("field");
        return box;
    }

    private VBox speedField() {
        HBox sliderRow = new HBox(speedSlider, delayLabel);
        sliderRow.setAlignment(Pos.CENTER_LEFT);
        sliderRow.getStyleClass().add("slider-row");
        // Same height as the combo box, so both fields line up on the row.
        sliderRow.setMinHeight(CONTROL_HEIGHT);

        // Named "Delay", not "Speed": the value is the pause between frames,
        // so dragging right makes the animation slower, not faster.
        return field("Delay", sliderRow);
    }

    /** A counter shown as a caption with its number underneath. */
    private VBox stat(String caption, Label value) {
        Label label = new Label(caption);
        label.getStyleClass().add("stat-caption");
        value.getStyleClass().add("stat-value");

        VBox box = new VBox(label, value);
        box.getStyleClass().add("stat");
        return box;
    }

    private Separator divider() {
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separator.getStyleClass().add("divider");
        return separator;
    }

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    // ----------------------------------------------------------------- setup

    private void buildAlgorithmSelector() {
        algorithmSelector.getItems().addAll(ALGORITHMS.keySet());
        algorithmSelector.setPromptText("Select algorithm");
        algorithmSelector.setPrefWidth(180);
        algorithmSelector.setPrefHeight(CONTROL_HEIGHT);
        algorithmSelector.setCursor(Cursor.HAND);
    }

    private void buildSpeedSlider() {
        speedSlider.setPrefWidth(180);
        speedSlider.setCursor(Cursor.HAND);

        delayLabel.getStyleClass().add("slider-value");
        delayLabel.textProperty().bind(speedSlider.valueProperty().asString("%.0f ms"));
    }

    private void wireActions() {
        startButton.getStyleClass().add("primary");
        stopButton.getStyleClass().add("danger");
        newArrayButton.getStyleClass().add("ghost");
        nextStepButton.getStyleClass().add("ghost");

        newArrayButton.setCursor(Cursor.HAND);
        startButton.setCursor(Cursor.HAND);
        stopButton.setCursor(Cursor.HAND);
        nextStepButton.setCursor(Cursor.HAND);
        stepModeCheck.setCursor(Cursor.HAND);

        nextStepButton.setDisable(true);

        stepModeCheck.setOnAction(e -> {
            boolean enabled = stepModeCheck.isSelected();
            nextStepButton.setDisable(!enabled);
            // Tell the gate too: switching the mode off has to release a run
            // that is already parked waiting for a step.
            gate.setStepMode(enabled);
        });

        nextStepButton.setOnAction(e -> gate.step());
        stopButton.setOnAction(e -> gate.requestStop());
        newArrayButton.setOnAction(e -> visualizer.regenerateArray());

        startButton.setOnAction(e -> {
            SortAlgorithm algorithm = ALGORITHMS.get(algorithmSelector.getValue());
            if (algorithm != null) {
                startSort(algorithm, (int) speedSlider.getValue());
            }
        });
    }

    // ------------------------------------------------------------------- run

    /**
     * Runs the algorithm on a worker thread, animating it through an
     * {@link AnimatedTrace}. Called from the JavaFX thread, so the controls
     * are disabled directly rather than through Platform.runLater.
     */
    private void startSort(SortAlgorithm algorithm, int delay) {
        setAllControlsDisabled(true);
        resetCounters();

        gate.reset();

        int[] values = visualizer.getValues();
        SortTrace trace = new AnimatedTrace(visualizer, this, gate, delay);

        Thread worker = new Thread(() -> {
            try {
                algorithm.sort(values, trace);
            } catch (StoppedException stopped) {
                // The user pressed Stop; the finally block below tidies up.
            } finally {
                Platform.runLater(this::resetControlsAndState);
            }
        }, "sort-worker");

        // Daemon, so a run in progress cannot keep the JVM alive after the
        // window is closed.
        worker.setDaemon(true);
        worker.start();
    }

    public void setAllControlsDisabled(boolean disabled) {
        newArrayButton.setDisable(disabled);
        startButton.setDisable(disabled);
        speedSlider.setDisable(disabled);
        algorithmSelector.setDisable(disabled);
    }

    public void incrementComparisons() {
        comparisons++;
        Platform.runLater(() -> comparisonsLabel.setText(Integer.toString(comparisons)));
    }

    /**
     * Records array writes that actually changed a value, i.e. bars that
     * changed height. A swap of two distinct values is 2 moves, one merge
     * copy is 1 move.
     *
     * Counting moves rather than swaps keeps the four algorithms on the same
     * axis: Merge Sort performs no swaps at all, it copies through a buffer,
     * so a "swap" count made it look like it moved more data than Quick Sort.
     */
    public void addMoves(int count) {
        if (count <= 0) {
            return;
        }
        moves += count;
        Platform.runLater(() -> movesLabel.setText(Integer.toString(moves)));
    }

    public void resetCounters() {
        comparisons = 0;
        moves = 0;
        Platform.runLater(() -> {
            comparisonsLabel.setText("0");
            movesLabel.setText("0");
        });
    }

    public void resetControlsAndState() {
        setAllControlsDisabled(false);
        gate.reset();

        // The counters are deliberately left standing: every algorithm calls
        // resetCounters() when it starts, so zeroing them here only wiped the
        // result before it could be read.

        // A stopped run abandons its highlights, and may have left a bar
        // height unapplied, so redraw the bars from the array.
        visualizer.refreshBars();
    }
}
