/*
================================================================================
File: src/main/java/com/dta/clientapp/services/SceneManager.java
Description: Manages switching between different scenes (views).
================================================================================
*/
package com.dta.clientapp.services;

import com.dta.clientapp.MainApp;
import com.dta.clientapp.controllers.DashboardController;
import com.dta.clientapp.controllers.LoginController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private final Stage stage;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/LoginScreen.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setSceneManager(this);
            stage.setScene(new Scene(root, 400, 300));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDashboard(String token) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/DashboardScreen.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.initialize(token, this);
            stage.setScene(new Scene(root, 700, 500));
            stage.setTitle("DTA Capsule Client - Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}