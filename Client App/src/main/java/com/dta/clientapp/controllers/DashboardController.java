/*
================================================================================
File: src/main/java/com/dta/clientapp/controllers/DashboardController.java
Description: Controller for the client dashboard.
================================================================================
*/
package com.dta.clientapp.controllers;

import com.dta.clientapp.models.OpenedCapsule;
import com.dta.clientapp.services.CpsxReaderService;
import com.dta.clientapp.services.SceneManager;
import com.dta.clientapp.services.SessionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class DashboardController {

    @FXML private ListView<OpenedCapsule> openedCapsulesListView;

    private String authToken;
    private SceneManager sceneManager;
    private final CpsxReaderService readerService = new CpsxReaderService();
    private final SessionService sessionService = SessionService.getInstance();

    public void initialize(String token, SceneManager manager) {
        this.authToken = token;
        this.sceneManager = manager;

        // Bind the list view to the session service's list
        openedCapsulesListView.setItems(sessionService.getOpenedCapsules());

        // Customize how each item in the list is displayed
        openedCapsulesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(OpenedCapsule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.statusProperty().get() + ")");
                }
            }
        });
    }

    @FXML
    private void handleOpenCapsule() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open DTA Capsule File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Capsule Files (*.cpsx)", "*.cpsx"));
        File selectedFile = fileChooser.showOpenDialog(openedCapsulesListView.getScene().getWindow());

        if (selectedFile != null) {
            // For this demo, we need to ask the user for the decryption key.
            TextInputDialog keyDialog = new TextInputDialog();
            keyDialog.setTitle("Decryption Key Required");
            keyDialog.setHeaderText("Enter the hex-encoded AES key for this capsule.");
            keyDialog.setContentText("Key:");

            keyDialog.showAndWait().ifPresent(hexKey -> {
                try {
                    OpenedCapsule capsule = readerService.decryptAndOpen(selectedFile, hexKey);
                    sessionService.addOpenedCapsule(capsule);
                    // Open the temporary folder for the user
                    Desktop.getDesktop().open(capsule.getTempDirectory());
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Decryption Failed", "Could not open capsule. Check the key and file integrity.");
                }
            });
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}