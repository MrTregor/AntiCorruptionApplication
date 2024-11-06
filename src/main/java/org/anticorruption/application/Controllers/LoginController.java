package org.anticorruption.application.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.stage.Stage;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.UserSession;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String SERVER_URL = ConfigManager.getProperty("server.url");

    @FXML
    protected void onLoginButtonClick() {
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

            // Отправляем запрос асинхронно
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleResponse)
                    .exceptionally(e -> {
                        System.err.println("Ошибка при отправке запроса: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private void handleResponse(String responseBody) {
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

                // Переход на главную форму в основном потоке
                Platform.runLater(this::openMainForm);

            } else {
                System.err.println("Ошибка авторизации: " + response.get("message").asText());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке ответа: " + e.getMessage());
        }
    }

    private void openMainForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/anticorruption/application/main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setupTabs();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Главная форма");
            stage.show();
        } catch (Exception e) {
            System.err.println("Ошибка при открытии главной формы: " + e.getMessage());
            e.printStackTrace();
        }
    }
}