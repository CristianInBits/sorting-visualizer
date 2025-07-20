package app.controller;

import app.algorithms.BubbleSort;
import app.view.SortingVisualizer;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

public class SortController extends HBox {

    private final Button newArrayButton;
    private final Button startButton;
    private final Slider speedSlider;

    public SortController(SortingVisualizer visualizer) {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-padding: 10; -fx-alignment: center;");

        newArrayButton = new Button("New Array");
        startButton = new Button("Start");

        Label speedLabel = new Label("Speed:");

        // Slider configuration: 1 (fast) to 200 (slow)
        speedSlider = new Slider(1, 200, 30); // default = 30 ms
        speedSlider.setPrefWidth(200);

        newArrayButton.setOnAction(e -> visualizer.regenerateArray());

        startButton.setOnAction(e -> {
            int delay = (int) speedSlider.getValue();
            BubbleSort bubbleSort = new BubbleSort(visualizer, this, delay);
            try {
                bubbleSort.sort();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        this.getChildren().addAll(newArrayButton, startButton, speedLabel, speedSlider);
    }

    public void setAllControlsDisabled(boolean disabled) {
        newArrayButton.setDisable(disabled);
        startButton.setDisable(disabled);
        speedSlider.setDisable(disabled);
    }

}
