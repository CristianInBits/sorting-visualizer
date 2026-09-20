package app;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {

        SortingVisualizer visualizer = new SortingVisualizer();
        SortController controller = new SortController(visualizer);

        BorderPane root = new BorderPane();

        ToggleButton themeToggle = new ToggleButton("Dark Mode");
        themeToggle.setCursor(Cursor.HAND);

        HBox topBar = new HBox(themeToggle);
        topBar.getStyleClass().add("top-bar");
        root.setTop(topBar);

        root.setCenter(visualizer);
        root.setBottom(controller);

        Scene scene = new Scene(root, 1024, 768);

        // Set once. Re-adding it on every click appended another copy to the
        // node's style class list, which grew without bound as the user
        // toggled back and forth.
        themeToggle.getStyleClass().add("theme-toggle");

        applyTheme(scene, themeToggle);
        themeToggle.setOnAction(e -> applyTheme(scene, themeToggle));

        primaryStage.setTitle("Sorting Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Swaps the one active stylesheet and relabels the button to match. */
    private void applyTheme(Scene scene, ToggleButton themeToggle) {
        boolean dark = themeToggle.isSelected();
        String stylesheet = dark ? "/dark.css" : "/style.css";

        scene.getStylesheets().setAll(getClass().getResource(stylesheet).toExternalForm());
        themeToggle.setText(dark ? "Light Mode" : "Dark Mode");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
