module org.anticorruption_application.anticorruptionapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;


    opens org.anticorruption_application.anticorruptionapplication to javafx.fxml;
    exports org.anticorruption_application.anticorruptionapplication;
}