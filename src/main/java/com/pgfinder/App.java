package com.pgfinder;

import com.pgfinder.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("PGFinder - Accommodation Booking System");
        SceneManager.switchTo("Login.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
