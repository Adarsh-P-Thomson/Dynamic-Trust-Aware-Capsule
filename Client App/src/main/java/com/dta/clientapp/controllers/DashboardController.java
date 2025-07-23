/*
================================================================================
File: src/main/java/com/dta/clientapp/controllers/DashboardController.java
Description: Controller for the client dashboard. (UPDATED)
================================================================================
*/
package com.dta.clientapp.controllers;

import com.dta.clientapp.models.OpenedCapsule;
import com.dta.clientapp.services.CapsuleService;
import com.dta.clientapp.services.CpsxReaderService;
import com.dta.clientapp.services.SceneManager;
import com.dta.clientapp.services.SessionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;

public class DashboardController {

    @FXML private ListView<OpenedCapsule> openedCapsulesListView;

    private String authToken;
    private SceneManager sceneManager;
    private final CpsxReaderService readerService = new CpsxReaderService();
    private final SessionService sessionService = SessionService.getInstance();
    private final CapsuleService capsuleService = new CapsuleService();

    public void initialize(String token, SceneManager manager) {
        this.authToken = token;
        this.sceneManager = manager;

        openedCapsulesListView.setItems(sessionService.getOpenedCapsules());

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
            TextInputDialog idDialog = new TextInputDialog();
            idDialog.setTitle("Capsule ID Required");
            idDialog.setHeaderText("Enter the Capsule ID to request the decryption key.");
            idDialog.setContentText("Capsule ID:");

            idDialog.showAndWait().ifPresent(capsuleId -> {
                new Thread(() -> {
                    try {
                        // 1. Fetch the key from the server
                        String hexKey = capsuleService.fetchDecryptionKey(authToken, capsuleId)
                            .orElseThrow(() -> new SecurityException("Access denied or capsule not found."));

                        // 2. Decrypt and open the file
                        OpenedCapsule capsule = readerService.decryptAndOpen(selectedFile, hexKey);
                        sessionService.addOpenedCapsule(capsule);
                        
                        // 3. Open the temporary folder for the user
                        Platform.runLater(() -> {
                            try {
                                Desktop.getDesktop().open(capsule.getTempDirectory());
                            } catch (Exception e) {
                                showAlert("Error", "Could not open the temporary directory.");
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> showAlert("Decryption Failed", "Could not open capsule. " + e.getMessage()));
                    }
                }).start();
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