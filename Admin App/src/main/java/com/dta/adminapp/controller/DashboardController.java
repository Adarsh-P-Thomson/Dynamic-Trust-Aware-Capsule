/*
================================================================================
File: src/main/java/com/dta/adminapp/controllers/DashboardController.java
Description: Placeholder controller for the main dashboard view after login.
================================================================================
*/
package com.dta.adminapp.controllers;

import com.dta.adminapp.services.SceneManager;

public class DashboardController {

    private String authToken;
    private SceneManager sceneManager;

    // This method will be called by the SceneManager after a successful login
    public void initialize(String token, SceneManager manager) {
        this.authToken = token;
        this.sceneManager = manager;
        System.out.println("Dashboard loaded with token: " + authToken);
        // Next step: Use this token to fetch capsule data from the server.
    }
}