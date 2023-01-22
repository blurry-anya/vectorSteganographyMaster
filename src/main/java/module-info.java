module com.diploma.stegovector {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    opens com.diploma.stegovector to javafx.fxml;
    exports com.diploma.stegovector;
    exports com.diploma.stegovector.controllers;
    opens com.diploma.stegovector.controllers to javafx.fxml;
}