package app;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        SortingVisualizer visualizer = new SortingVisualizer();
        SortController controller = new SortController(visualizer);

        BorderPane root = new BorderPane();
        root.setCenter(visualizer);
        root.setBottom(controller);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Sorting Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
