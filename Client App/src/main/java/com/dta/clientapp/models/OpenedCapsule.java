/*
================================================================================
File: src/main/java/com/dta/clientapp/models/OpenedCapsule.java
Description: Data model for a capsule that is currently open in a session.
================================================================================
*/
package com.dta.clientapp.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class OpenedCapsule {
    private final StringProperty name;
    private final StringProperty status;
    private final File tempDirectory;
    private final ZonedDateTime expiresAt;

    public OpenedCapsule(String name, File tempDirectory, ZonedDateTime expiresAt) {
        this.name = new SimpleStringProperty(name);
        this.tempDirectory = tempDirectory;
        this.expiresAt = expiresAt;
        
        String statusText;
        if (expiresAt == null) {
            statusText = "Active (No Expiry)";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            statusText = "Expires: " + expiresAt.format(formatter);
        }
        this.status = new SimpleStringProperty(statusText);
    }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public StringProperty statusProperty() { return status; }
    public File getTempDirectory() { return tempDirectory; }
    public ZonedDateTime getExpiresAt() { return expiresAt; }
}