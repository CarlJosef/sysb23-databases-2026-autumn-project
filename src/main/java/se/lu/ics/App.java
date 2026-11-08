package se.lu.ics;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Starts the Marvix JavaFX application and displays the main application view.
 */
public class App extends Application {

    /**
     * Loads the main Marvix view and displays the primary application window.
     *
     * @param primaryStage primary JavaFX application stage
     * @throws IOException if the main FXML view cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/se/lu/ics/views/MarvixMainView.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setTitle("MARVIX - Database Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(750);
        primaryStage.show();
    }

    /**
     * Launches the Marvix JavaFX application.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }
}
