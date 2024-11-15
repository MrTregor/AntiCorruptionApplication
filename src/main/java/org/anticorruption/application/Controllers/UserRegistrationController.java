package org.anticorruption.application.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.Models.User;
import org.anticorruption.application.UserSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UserRegistrationController {
    private final String SERVER_URL = ConfigManager.getProperty("server.url");

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private void onRegister() {
        try {
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("username", usernameField.getText());
            requestBody.put("password", passwordField.getText());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь успешно зарегистрирован.");
                                onCancel();
                            });
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось зарегистрировать пользователя."));
                        }
                    });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при регистрации: " + e.getMessage());
        }
    }


    @FXML
    private void onCancel() {
        ((Stage) usernameField.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}