package com.pgfinder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    public void initialize() {
        System.out.println("LoginController initialized.");
    }

    @FXML
    private void handleLogin() {
        System.out.println("Login button clicked. Email: " + emailField.getText());
    }
}
