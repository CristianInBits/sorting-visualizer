package app;

import app.controller.SortController;
import app.view.SortingVisualizer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SortingVisualizer visualizer = new SortingVisualizer();
        SortController controller = new SortController(visualizer);

        ToggleButton themeToggle = new ToggleButton("Dark mode");
        themeToggle.setId("themeToggle");
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.setCursor(Cursor.HAND);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(header(themeToggle));
        root.setCenter(canvas(visualizer));
        root.setBottom(controller);

        Scene scene = new Scene(root, 1100, 740);

        applyTheme(scene, themeToggle);
        themeToggle.setOnAction(e -> applyTheme(scene, themeToggle));

        primaryStage.setTitle("Sorting Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(780);
        primaryStage.setMinHeight(520);
        primaryStage.show();
    }

    /** Title on the left, the controls that are about the app on the right. */
    private HBox header(ToggleButton themeToggle) {
        Label title = new Label("Sorting Visualizer");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Classic sorting algorithms, one step at a time");
        subtitle.getStyleClass().add("app-subtitle");

        VBox titleBox = new VBox(title, subtitle);
        titleBox.getStyleClass().add("title-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button aboutButton = new Button("About");
        aboutButton.setId("aboutButton");
        aboutButton.getStyleClass().add("ghost");
        aboutButton.setCursor(Cursor.HAND);
        aboutButton.setOnAction(e -> showAboutDialog());

        HBox header = new HBox(titleBox, spacer, aboutButton, themeToggle);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /** The bars get a surface of their own, and all the room that is left. */
    private StackPane canvas(SortingVisualizer visualizer) {
        StackPane canvas = new StackPane(visualizer);
        canvas.getStyleClass().add("canvas");
        StackPane.setAlignment(visualizer, Pos.BOTTOM_CENTER);

        // A BorderPane centre takes no margin from CSS, so it is set here.
        BorderPane.setMargin(canvas, new Insets(0, 24, 0, 24));
        return canvas;
    }

    /**
     * Swaps the palette and relabels the button to match.
     *
     * base.css holds the shape and spacing and stays loaded; the theme file
     * only redefines the colours it looks up, so a theme is a short palette
     * rather than a second copy of the whole stylesheet.
     */
    private void applyTheme(Scene scene, ToggleButton themeToggle) {
        boolean dark = themeToggle.isSelected();
        String palette = dark ? "/dark.css" : "/light.css";

        scene.getStylesheets().setAll(
                getClass().getResource("/base.css").toExternalForm(),
                getClass().getResource(palette).toExternalForm());

        themeToggle.setText(dark ? "Light mode" : "Dark mode");
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Sorting Visualizer in JavaFX");
        alert.setContentText(
                """
                        This project visualizes classic sorting algorithms step by step to help \
                        students understand how they work.

                        Author: Cristian Laurentiu Sindila
                        Built with Java 17, JavaFX 21 and Maven
                        github.com/CristianInBits/sorting-visualizer
                        """);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 13px;");

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
