package org.anticorruption.application;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertUtils {
    public static void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.getDialogPane().getStylesheets().add(
                    AlertUtils.class.getResource("/org/anticorruption/application/styles.css").toExternalForm()
            );
            alert.showAndWait();
        });
    }
}
