package com.pgfinder.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlFile) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary Stage has not been set. Call setPrimaryStage first.");
        }
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource("/fxml/" + fxmlFile));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML scene: " + fxmlFile);
        }
    }
}
