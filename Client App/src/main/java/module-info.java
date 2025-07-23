/*
================================================================================
File: src/main/java/module-info.java
Description: Java module definition file.
================================================================================
*/
module com.dta.clientapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.bouncycastle.provider;
    requires java.desktop; // Required for Desktop.getDesktop().open()

    opens com.dta.clientapp to javafx.fxml;
    opens com.dta.clientapp.controllers to javafx.fxml;

    exports com.dta.clientapp;
}
