package com.pgfinder;

import com.pgfinder.config.SchemaMigrator;
import com.pgfinder.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            SchemaMigrator.runMigration();
        } catch (Exception e) {
            System.err.println("Schema migration warning: " + e.getMessage());
        }
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("PGFinder - Accommodation Booking System");
        SceneManager.switchTo("Login.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
