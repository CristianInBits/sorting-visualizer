package app.controller;

import app.view.SortingVisualizer;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class SortController extends HBox {

    private final Button newArrayButton;
    private final Button startButton;

    public SortController(SortingVisualizer visualizer) {
        this.setSpacing(10);
        this.setStyle("-fx-padding: 10; -fx-alignment: center;");

        newArrayButton = new Button("New Array");
        startButton = new Button("Start");

        newArrayButton.setOnAction(e -> visualizer.regenerateArray());
        startButton.setOnAction(e -> {
            // Sorting functionality will be implemented in next issue
            System.out.println("Start sorting (not implemented yet)");
        });

        this.getChildren().addAll(newArrayButton, startButton);
    }
}
