package com.dta.adminapp;

import com.dta.adminapp.services.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneManager sceneManager = new SceneManager(primaryStage);
        primaryStage.setTitle("DTA Capsule Admin Console");
        sceneManager.showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}