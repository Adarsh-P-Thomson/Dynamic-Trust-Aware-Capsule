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
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML
    private TableView<Capsule> capsuleTable;
    @FXML
    private TableColumn<Capsule, String> nameColumn;
    @FXML
    private TableColumn<Capsule, String> statusColumn;
    @FXML
    private TableColumn<Capsule, String> lifecycleColumn;
    @FXML
    private TableColumn<Capsule, String> createdColumn;

    private String authToken;
    private SceneManager sceneManager;
    private final CapsuleService capsuleService = new CapsuleService();
    private final ObservableList<Capsule> capsuleList = FXCollections.observableArrayList();

    public void initialize(String token, SceneManager manager) {
        this.authToken = token;
        this.sceneManager = manager;

        setupTableColumns();
        loadCapsules();
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

            // Refresh the table if a capsule was created
            if (controller.isCapsuleCreated()) {
                loadCapsules();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open the creation dialog.");
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