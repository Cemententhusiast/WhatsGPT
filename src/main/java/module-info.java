module com.example.whatsgpt_ {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens whatsgpt to javafx.fxml;
    exports whatsgpt.model;
    exports whatsgpt.view;
    opens whatsgpt.view to javafx.fxml;
    opens whatsgpt.model;
    exports whatsgpt.controller;
    opens whatsgpt.controller to javafx.fxml;
}