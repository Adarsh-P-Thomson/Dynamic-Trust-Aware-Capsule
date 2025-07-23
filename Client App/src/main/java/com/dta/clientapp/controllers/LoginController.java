/*
================================================================================
File: src/main/java/com/dta/clientapp/controllers/LoginController.java
Description: Controller for the client login screen.
================================================================================
*/
package com.dta.clientapp.controllers;

import com.dta.clientapp.services.AuthService;
import com.dta.clientapp.services.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private SceneManager sceneManager;
    private final AuthService authService = new AuthService();

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    protected void handleLoginButtonAction() {
        // Using client credentials from the seed data
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email and password cannot be empty.");
            return;
        }

        new Thread(() -> {
            try {
                authService.login(email, password)
                    .ifPresentOrElse(
                        token -> Platform.runLater(() -> sceneManager.showDashboard(token)),
                        () -> Platform.runLater(() -> errorLabel.setText("Login failed. Check credentials."))
                    );
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> errorLabel.setText("Error connecting to server."));
                e.printStackTrace();
            }
        }).start();
    }
}