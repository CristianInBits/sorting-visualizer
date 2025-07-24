package app.controller;

import app.algorithms.BubbleSort;
import app.algorithms.MergeSort;
import app.algorithms.QuickSort;
import app.algorithms.SelectionSort;
import app.view.SortingVisualizer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SortController extends HBox {

    private final Button newArrayButton;
    private final Button startButton;
    private final Slider speedSlider;
    private final ComboBox<String> algorithmSelector;

    private final SortingVisualizer visualizer;

    private final CheckBox stepModeCheck = new CheckBox("Step-by-step");
    private final Button nextStepButton = new Button("Next Step");
    private final Button stopButton = new Button("Stop");

    private final Object stepLock = new Object();
    private volatile boolean waitingForStep = false;
    private volatile boolean stopRequested = false;

    private int comparisons = 0;
    private int swaps = 0;

    private final Label comparisonsLabel = new Label("Comparisons: 0");
    private final Label swapsLabel = new Label("Swaps: 0");

    public SortController(SortingVisualizer visualizer) {
        this.visualizer = visualizer;

        this.setSpacing(10);
        this.setPadding(new Insets(10));
        // this.setStyle("-fx-padding: 10; -fx-alignment: center;");

        newArrayButton = new Button("New Array");
        startButton = new Button("Start");

        nextStepButton.setDisable(true);

        stepModeCheck.setOnAction(e -> {
            nextStepButton.setDisable(!stepModeCheck.isSelected());
        });

        nextStepButton.setOnAction(e -> triggerNextStep());

        stopButton.setOnAction(e -> requestStop());

        algorithmSelector = new ComboBox<>();
        algorithmSelector.getItems().addAll(
                "Bubble Sort",
                "Quick Sort",
                "Selection Sort",
                "Merge Sort");

        algorithmSelector.setPromptText("Select Algorithm");
        algorithmSelector.setPrefWidth(150);

        Label speedLabel = new Label("Speed:");

        // Slider configuration: 1 (fast) to 200 (slow)
        speedSlider = new Slider(1, 200, 30); // default = 30 ms
        speedSlider.setPrefWidth(200);

        newArrayButton.setOnAction(e -> visualizer.regenerateArray());

        startButton.setOnAction(e -> {
            String selected = algorithmSelector.getValue();
            int delay = (int) speedSlider.getValue();

            if (selected == null)
                return;

            switch (selected) {
                case "Bubble Sort" -> new BubbleSort(visualizer, this, delay).sort();
                case "Quick Sort" -> new QuickSort(visualizer, this, delay).sort();
                case "Selection Sort" -> new SelectionSort(visualizer, this, delay).sort();
                case "Merge Sort" -> new MergeSort(visualizer, this, delay).sort();
                default -> System.out.println("Unsupported algorithm: " + selected);
            }
        });

        VBox counterBox = new VBox(5, comparisonsLabel, swapsLabel);
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
                counterBox);

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

    public void incrementSwaps() {
        swaps++;
        Platform.runLater(() -> swapsLabel.setText("Swaps: " + swaps));
    }

    public void resetCounters() {
        comparisons = 0;
        swaps = 0;
        Platform.runLater(() -> {
            comparisonsLabel.setText("Comparisons: 0");
            swapsLabel.setText("Swaps: 0");
        });
    }

    public boolean isStepModeEnabled() {
        return stepModeCheck.isSelected();
    }

    public void waitForNextStep() throws InterruptedException {
        if (isStepModeEnabled()) {
            waitingForStep = true;
            synchronized (stepLock) {
                while (waitingForStep && !stopRequested) {
                    stepLock.wait();
                }
            }
        }
    }

    public void triggerNextStep() {
        synchronized (stepLock) {
            waitingForStep = false;
            stepLock.notify();
        }
    }

    public void requestStop() {
        stopRequested = true;
        triggerNextStep(); // For safety
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void resetControlsAndState() {
        setAllControlsDisabled(false);
        stopRequested = false;
        waitingForStep = false;
        resetCounters();
    }

}
