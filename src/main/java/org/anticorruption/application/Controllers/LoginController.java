package org.anticorruption.application.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Setter;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.HttpsClient;
import org.anticorruption.application.UserSession;


import static org.anticorruption.application.AlertUtils.showAlert;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private Pane root; // Добавьте это поле

    // Метод для установки stage извне, если потребуется
    @Setter
    private Stage stage; // Добавьте это поле

    private final ObjectMapper mapper = new ObjectMapper();
    private final String SERVER_URL = ConfigManager.getProperty("server.url");



    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("username", username);
            requestBody.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // Store the event source for later use
            Node source = (Node) event.getSource();
            stage = (Stage) source.getScene().getWindow();

            HttpsClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> handleResponse(response, stage))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            System.err.println("Ошибка при отправке запроса: " + e.getMessage());
                            showAlert(Alert.AlertType.ERROR, "Ошибка подключения",
                                    "Не удалось установить соединение с сервером: " + e.getMessage());
                        });
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    // Modified to accept Stage parameter
    private void handleResponse(String responseBody, Stage stage) {
        try {
            JsonNode response = mapper.readTree(responseBody);
            System.out.printf("response: %s\n", response.toString());
            if ("OK".equals(response.get("status").asText())) {
                String token = response.get("data").get("token").asText();

                // Сохраняем токен
                UserSession.getInstance().setToken(token);

                // Декодируем JWT токен
                String[] chunks = token.split("\\.");
                Base64.Decoder decoder = Base64.getUrlDecoder();
                String payload = new String(decoder.decode(chunks[1]));
                JsonNode tokenPayload = mapper.readTree(payload);

                // Получаем группы доступа
                List<String> groups = new ArrayList<>();
                JsonNode groupsArray = tokenPayload.get("groups");
                if (groupsArray.isArray()) {
                    for (JsonNode group : groupsArray) {
                        groups.add(group.get("authority").asText());
                    }
                }
                UserSession.getInstance().setGroups(groups);

                // Получаем username
                String username = tokenPayload.get("sub").asText();
                UserSession.getInstance().setUsername(username);

                System.out.println("Авторизация успешна");
                System.out.println("Группы доступа: " + groups);
                System.out.println("Username: " + username);

                // Pass the stage to openMainForm
                Platform.runLater(() -> openMainForm(stage));

            } else {
                String errorMessage = response.get("message").asText();
                System.err.println("Ошибка авторизации: " + errorMessage);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Ошибка авторизации", errorMessage)); // Показать ошибку
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке ответа: " + e.getMessage());
        }
    }

    // Modified to accept Stage parameter
    private void openMainForm(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/anticorruption/application/main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setupTabs();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Главная форма");
            stage.show();
        } catch (Exception e) {
            System.err.println("Ошибка при открытии главной формы: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    @FXML
    private void initialize() {
        // Создаем кастомный заголовок
        HBox titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: #3c3f41; -fx-padding: 5;"); // Цвет заголовка
        titleBar.setPrefHeight(30);

        Label titleLabel = new Label("Вход");
        titleLabel.setTextFill(Color.WHITE); // Цвет текста заголовка

        Region spacer = new Region(); // Используем Region вместо Spacer
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> {
            if (stage != null) {
                stage.close();
            }
        });

        titleBar.getChildren().addAll(titleLabel, spacer, closeButton);

        // Проверяем, что root не null перед добавлением
        if (root != null) {
            root.getChildren().addFirst(titleBar); // Добавляем заголовок в корень
        }
    }

}