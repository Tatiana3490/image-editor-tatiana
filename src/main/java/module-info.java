module com.svalero.aa1_imageeditor_tatiana {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires javafx.swing;


    opens com.svalero.aa1_imageeditor_tatiana.controller to javafx.fxml;
    exports com.svalero.aa1_imageeditor_tatiana;
}