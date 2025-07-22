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

    opens com.dta.adminapp to javafx.fxml;
    opens com.dta.adminapp.controllers to javafx.fxml;

    exports com.dta.adminapp;
}
