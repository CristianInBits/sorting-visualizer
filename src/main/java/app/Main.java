package app;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        SortingVisualizer visualizer = new SortingVisualizer();
        SortController controller = new SortController(visualizer);

        BorderPane root = new BorderPane();

        ToggleButton themeToggle = new ToggleButton("Dark Mode");

        root.setCenter(visualizer);
        root.setBottom(controller);
        root.setTop(themeToggle);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        themeToggle.setOnAction(e -> {
            scene.getStylesheets().clear();
            if (themeToggle.isSelected()) {
                scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
                themeToggle.setText("Light Mode");
            } else {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                themeToggle.setText("Dark Mode");
            }
        });

        primaryStage.setTitle("Sorting Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
