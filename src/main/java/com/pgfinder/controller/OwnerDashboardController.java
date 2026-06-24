package com.pgfinder.controller;

import java.util.ResourceBundle;

import com.pgfinder.util.SceneManager;

import java.net.URL;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;





public class OwnerDashboardController {

  
@FXML
private ProgressIndicator occupancyIndicator;

public void initialize(URL location, ResourceBundle resources) {
    // This removes the built-in percentage text label entirely from Java code!
    occupancyIndicator.skinProperty().addListener((obs, oldSkin, newSkin) -> {
        if (newSkin != null) {
            javafx.scene.Node textNode = occupancyIndicator.lookup(".percentage");
            if (textNode != null) {
                textNode.setVisible(false);
            }
        }
    });
}

public void openMyPGs() {
    SceneManager.switchTo("MyPGs.fxml");
}

public void openBookingRequests() {
    SceneManager.switchTo("BookingRequests.fxml");
}

}


