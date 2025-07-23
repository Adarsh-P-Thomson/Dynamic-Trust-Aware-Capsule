/*
================================================================================
File: src/main/java/com/dta/adminapp/controllers/DashboardController.java
Description: Controller for the main dashboard view after login. (UPDATED)
================================================================================
*/
package com.dta.adminapp.controllers;

import com.dta.adminapp.MainApp;
import com.dta.adminapp.models.Capsule;
import com.dta.adminapp.services.CapsuleService;
import com.dta.adminapp.services.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private TableView<Capsule> capsuleTable;
    @FXML private TableColumn<Capsule, String> nameColumn;
    @FXML private TableColumn<Capsule, String> statusColumn;
    @FXML private TableColumn<Capsule, String> lifecycleColumn;
    @FXML private TableColumn<Capsule, String> createdColumn;
    @FXML private Button grantAccessButton;
    @FXML private Button deleteButton;
    @FXML private Button toggleLockButton;

    private String authToken;
    private SceneManager sceneManager;
    private final CapsuleService capsuleService = new CapsuleService();
    private final ObservableList<Capsule> capsuleList = FXCollections.observableArrayList();

    public void initialize(String token, SceneManager manager) {
        this.authToken = token;
        this.sceneManager = manager;

        setupTableColumns();
        setupSelectionListener();
        loadCapsules();
    }
    
    private void setupSelectionListener() {
        // Disable buttons initially
        grantAccessButton.setDisable(true);
        deleteButton.setDisable(true);
        toggleLockButton.setDisable(true);

        // Add a listener to the table's selection model
        capsuleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = (newSelection != null);
            grantAccessButton.setDisable(!isItemSelected);
            deleteButton.setDisable(!isItemSelected);
            toggleLockButton.setDisable(!isItemSelected);
        });
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().capsuleNameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        lifecycleColumn.setCellValueFactory(cellData -> cellData.getValue().lifecycleStatusProperty());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        createdColumn.setCellValueFactory(cellData -> {
            ZonedDateTime zdt = cellData.getValue().createdAtProperty().get();
            return new javafx.beans.property.SimpleStringProperty(zdt != null ? formatter.format(zdt) : "N/A");
        });

        capsuleTable.setItems(capsuleList);
    }

    private void loadCapsules() {
        new Thread(() -> {
            try {
                List<Capsule> capsules = capsuleService.getAllCapsules(authToken);
                Platform.runLater(() -> capsuleList.setAll(capsules));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Could not load capsules: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCreateCapsuleButton() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/CreateCapsuleDialog.fxml"));
            Parent root = loader.load();

            CreateCapsuleController controller = loader.getController();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create New CPSX Capsule");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(capsuleTable.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            controller.setDialogStage(dialogStage);
            controller.setAuthToken(authToken);

            dialogStage.showAndWait();

            if (controller.isCapsuleCreated()) {
                loadCapsules();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open the creation dialog.");
        }
    }

    @FXML
    private void handleGrantAccessButton() {
        Capsule selectedCapsule = capsuleTable.getSelectionModel().getSelectedItem();
        if (selectedCapsule == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Grant Access");
        dialog.setHeaderText("Grant access to capsule: " + selectedCapsule.capsuleNameProperty().get());
        dialog.setContentText("Enter client's email address:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            new Thread(() -> {
                try {
                    capsuleService.grantAccess(authToken, selectedCapsule.getCapsuleId(), email);
                    Platform.runLater(() -> showAlert("Success", "Access granted to " + email));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Failed to grant access: " + e.getMessage()));
                }
            }).start();
        });
    }

    @FXML
    private void handleInitiateDeleteButton() {
        Capsule selectedCapsule = capsuleTable.getSelectionModel().getSelectedItem();
        if (selectedCapsule == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Initiate deletion for: " + selectedCapsule.capsuleNameProperty().get());
        confirmation.setContentText("This will mark the capsule for deletion and set its expiry to 1 minute from now. Are you sure?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    capsuleService.initiateDelete(authToken, selectedCapsule.getCapsuleId());
                    Platform.runLater(() -> {
                        showAlert("Success", "Capsule marked for deletion.");
                        loadCapsules(); // Refresh to show updated status
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Failed to initiate delete: " + e.getMessage()));
                }
            }).start();
        }
    }

    @FXML
    private void handleToggleLockButton() {
        Capsule selectedCapsule = capsuleTable.getSelectionModel().getSelectedItem();
        if (selectedCapsule == null) return;

        new Thread(() -> {
            try {
                capsuleService.toggleLockStatus(authToken, selectedCapsule.getCapsuleId());
                Platform.runLater(() -> {
                    showAlert("Success", "Lock status toggled.");
                    loadCapsules(); // Refresh to show updated status
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "Failed to toggle lock status: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (title.equalsIgnoreCase("error")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}