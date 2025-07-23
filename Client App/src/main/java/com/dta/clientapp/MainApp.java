/*
================================================================================
File: src/main/java/com/dta/clientapp/MainApp.java
Description: Main entry point. Manages app lifecycle and shutdown procedures.
================================================================================
*/
package com.dta.clientapp;

import com.dta.clientapp.services.SceneManager;
import com.dta.clientapp.services.SessionService;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private final SessionService sessionService = SessionService.getInstance();

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneManager sceneManager = new SceneManager(primaryStage);
        primaryStage.setTitle("DTA Capsule Client");
        sceneManager.showLoginScreen();
    }

    @Override
    public void stop() throws Exception {
        // This is the crucial shutdown hook.
        System.out.println("Application closing. Shredding all temporary session files...");
        sessionService.shredAllSessionData();
        System.out.println("Shredding complete. Exiting.");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}