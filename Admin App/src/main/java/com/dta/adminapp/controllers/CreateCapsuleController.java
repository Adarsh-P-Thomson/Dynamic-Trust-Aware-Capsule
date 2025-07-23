/*
================================================================================
File: src/main/java/com/dta/adminapp/controllers/CreateCapsuleController.java (NEW)
Description: Controller for the creation dialog.
================================================================================
*/
package com.dta.adminapp.controllers;

import com.dta.adminapp.services.CapsuleService;
import com.dta.adminapp.services.CpsxCreatorService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CreateCapsuleController {

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker expiryPicker;
    @FXML private TextField hourField;
    @FXML private TextField minuteField;
    @FXML private Label selectedFileLabel;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private String authToken;
    private File selectedFile;
    private boolean capsuleCreated = false;

    private final CpsxCreatorService creatorService = new CpsxCreatorService();
    private final CapsuleService capsuleService = new CapsuleService();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isCapsuleCreated() {
        return capsuleCreated;
    }

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Encapsulate");
        selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            selectedFileLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void handleCreate() {
        // --- Validation ---
        if (nameField.getText().isEmpty() || selectedFile == null) {
            errorLabel.setText("Capsule Name and a selected file are required.");
            return;
        }

        // --- Prepare Data ---
        String name = nameField.getText();
        String description = descriptionField.getText();
        LocalDate expiryDate = expiryPicker.getValue();
        ZonedDateTime expiryZonedDateTime = null;

        if (expiryDate != null) {
            try {
                int hour = hourField.getText().isEmpty() ? 0 : Integer.parseInt(hourField.getText());
                int minute = minuteField.getText().isEmpty() ? 0 : Integer.parseInt(minuteField.getText());

                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    errorLabel.setText("Invalid time. Hour must be 0-23, minute 0-59.");
                    return;
                }
                
                LocalTime expiryTime = LocalTime.of(hour, minute);
                expiryZonedDateTime = ZonedDateTime.of(expiryDate, expiryTime, ZoneId.systemDefault());

            } catch (NumberFormatException e) {
                errorLabel.setText("Hour and Minute must be numbers.");
                return;
            }
        }

        // --- Show Save Dialog for the .cpsx file ---
        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save CPSX Capsule File");
        saveChooser.setInitialFileName(name.endsWith(".cpsx") ? name : name + ".cpsx");
        saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Capsule Files (*.cpsx)", "*.cpsx"));
        File destinationFile = saveChooser.showSaveDialog(dialogStage);

        if (destinationFile == null) {
            return; // User cancelled the save dialog
        }

        errorLabel.setText("Creating capsule... please wait.");
        final ZonedDateTime finalExpiry = expiryZonedDateTime;

        // --- Background Thread for File I/O and Network ---
        new Thread(() -> {
            try {
                // 1. Create the .cpsx file locally
                CpsxCreatorService.CreationResult result = creatorService.createCapsuleFile(
                    selectedFile,
                    destinationFile,
                    name,
                    finalExpiry
                );

                // 2. Register the capsule with the server
                capsuleService.createCapsule(
                    authToken,
                    name,
                    description,
                    result.originalFileHash,
                    result.encryptedKeyHex,
                    finalExpiry
                );

                // 3. Close dialog on success
                Platform.runLater(() -> {
                    capsuleCreated = true;
                    dialogStage.close();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    errorLabel.setText("Creation failed: " + e.getMessage());
                    showAlert("Error", "Could not create capsule: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}