/*
================================================================================
File: src/main/java/module-info.java
Description: Java module definition file.
================================================================================
*/
module com.dta.adminapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310; // New dependency for Java 8+ time types

    opens com.dta.adminapp to javafx.fxml;
    opens com.dta.adminapp.controllers to javafx.fxml;
    opens com.dta.adminapp.models to com.fasterxml.jackson.databind; // Open models for reflection

    exports com.dta.adminapp;
}