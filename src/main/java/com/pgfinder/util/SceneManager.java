package com.pgfinder.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlFile) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary Stage has not been set. Call setPrimaryStage first.");
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML scene: " + fxmlFile);
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", "Unable to Open Screen",
                    "The screen could not be loaded. Please try again or contact support.");
        }
    }
}
