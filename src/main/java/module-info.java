module org.anticorruption_application.anticorruptionapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;


    opens org.anticorruption.application to javafx.fxml;
    exports org.anticorruption.application;
}