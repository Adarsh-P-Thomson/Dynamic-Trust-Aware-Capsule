/*
================================================================================
File: src/main/java/com/dta/adminapp/models/Capsule.java (NEW)
Description: Data model for a capsule, using JavaFX properties.
================================================================================
*/
package com.dta.adminapp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.ZonedDateTime;

public class Capsule {
    private final StringProperty capsuleId = new SimpleStringProperty();
    private final StringProperty capsuleName = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty lifecycleStatus = new SimpleStringProperty();
    private final ObjectProperty<ZonedDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<ZonedDateTime> expiresAt = new SimpleObjectProperty<>();

    // Getters for JavaFX properties
    public StringProperty capsuleIdProperty() { return capsuleId; }
    public StringProperty capsuleNameProperty() { return capsuleName; }
    public StringProperty statusProperty() { return status; }
    public StringProperty lifecycleStatusProperty() { return lifecycleStatus; }
    public ObjectProperty<ZonedDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<ZonedDateTime> expiresAtProperty() { return expiresAt; }

    // **FIX:** Added standard getter for the capsule ID string.
    public String getCapsuleId() { return capsuleId.get(); }

    // Setters that will be used by Jackson for JSON deserialization
    @JsonProperty("capsule_id")
    public void setCapsuleId(String id) { this.capsuleId.set(id); }
    @JsonProperty("capsule_name")
    public void setCapsuleName(String name) { this.capsuleName.set(name); }
    @JsonProperty("status")
    public void setStatus(String status) { this.status.set(status); }
    @JsonProperty("lifecycle_status")
    public void setLifecycleStatus(String status) { this.lifecycleStatus.set(status); }
    @JsonProperty("created_at")
    public void setCreatedAt(ZonedDateTime date) { this.createdAt.set(date); }
    @JsonProperty("expires_at")
    public void setExpiresAt(ZonedDateTime date) { this.expiresAt.set(date); }
}