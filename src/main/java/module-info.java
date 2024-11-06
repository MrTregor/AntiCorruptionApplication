module org.anticorruption_application.anticorruptionapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires static lombok;


    opens org.anticorruption.application to javafx.fxml;
    exports org.anticorruption.application;
    exports org.anticorruption.application.Controllers;
    opens org.anticorruption.application.Controllers to javafx.fxml;
    exports org.anticorruption.application.Models;
    opens org.anticorruption.application.Models to javafx.fxml;
}