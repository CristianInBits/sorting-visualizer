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
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SortController extends HBox {

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

    private final Button newArrayButton;
    private final Button startButton;
    private final Slider speedSlider;
    private final ComboBox<String> algorithmSelector;

    private final SortingVisualizer visualizer;

    private final CheckBox stepModeCheck = new CheckBox("Step-by-step");
    private final Button nextStepButton = new Button("Next Step");
    private final Button stopButton = new Button("Stop");

    /** Owns step-by-step mode and the stop request, off the JavaFX thread. */
    private final StepGate gate = new StepGate();

    private int comparisons = 0;
    private int moves = 0;

    private final Label comparisonsLabel = new Label("Comparisons: 0");
    private final Label movesLabel = new Label("Moves: 0");

    public SortController(SortingVisualizer visualizer) {
        this.visualizer = visualizer;

        Button aboutButton = new Button("About");
        aboutButton.setOnAction(e -> showAboutDialog());
        aboutButton.getStyleClass().add("about-button");

        this.setSpacing(10);
        this.setPadding(new Insets(10));
        // this.setStyle("-fx-padding: 10; -fx-alignment: center;");

        newArrayButton = new Button("New Array");
        startButton = new Button("Start");

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

        algorithmSelector = new ComboBox<>();
        algorithmSelector.getItems().addAll(ALGORITHMS.keySet());

        algorithmSelector.setPromptText("Select Algorithm");
        algorithmSelector.setPrefWidth(150);

        Label speedLabel = new Label("Speed:");

        // Slider configuration: 1 (fast) to 200 (slow)
        speedSlider = new Slider(1, 200, 30); // default = 30 ms
        speedSlider.setPrefWidth(200);

        newArrayButton.setOnAction(e -> visualizer.regenerateArray());

        startButton.setOnAction(e -> {
            SortAlgorithm algorithm = ALGORITHMS.get(algorithmSelector.getValue());
            if (algorithm != null) {
                startSort(algorithm, (int) speedSlider.getValue());
            }
        });

        VBox counterBox = new VBox(5, comparisonsLabel, movesLabel);
        counterBox.setStyle("-fx-alignment: center-left;");

        newArrayButton.setCursor(Cursor.HAND);
        startButton.setCursor(Cursor.HAND);
        algorithmSelector.setCursor(Cursor.HAND);
        speedSlider.setCursor(Cursor.HAND);

        this.getChildren().addAll(
                newArrayButton, startButton,
                algorithmSelector,
                stepModeCheck, nextStepButton, stopButton,
                speedLabel, speedSlider,
                counterBox,
                aboutButton);

    }

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
        Platform.runLater(() -> comparisonsLabel.setText("Comparisons: " + comparisons));
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
        Platform.runLater(() -> movesLabel.setText("Moves: " + moves));
    }

    public void resetCounters() {
        comparisons = 0;
        moves = 0;
        Platform.runLater(() -> {
            comparisonsLabel.setText("Comparisons: 0");
            movesLabel.setText("Moves: 0");
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

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Sorting Visualizer in JavaFX");
        alert.setContentText(
                """
                        This project visualizes classic sorting algorithms step-by-step to help students understand how they work.

                        👤 Author: Cristian Laurentiu Sindila
                        🛠️ Built: with Java 17, JavaFX 21, and Maven
                        💻 GitHub: https://github.com/CristianInBits/sorting-visualizer
                        """);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        alert.showAndWait();
    }

}
