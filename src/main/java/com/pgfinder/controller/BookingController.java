package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import javafx.event.ActionEvent;

public class BookingController {

    public void goBack(ActionEvent event) {
        SceneManager.switchTo("PGDetails.fxml");
    }
}

