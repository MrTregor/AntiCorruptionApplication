package org.anticorruption.application.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.HttpsClient;
import org.anticorruption.application.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Контроллер для регистрации новых пользователей в антикоррупционной информационной системе.
 * Обеспечивает процесс создания нового пользовательского аккаунта с базовой аутентификацией.
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 2024-10-10
 */
public class UserRegistrationController {

    /**
     * URL сервера для выполнения HTTP-запросов, получаемый из конфигурационного файла.
     */
    private final String SERVER_URL = ConfigManager.getProperty("server.url");

    /**
     * Ссылка на основной контроллер для обновления списка пользователей после регистрации.
     */
    @Setter
    private MainController mainController;

    /**
     * Текстовое поле для ввода имени пользователя.
     */
    @FXML
    private TextField usernameField;

    /**
     * Поле для ввода пароля с маскировкой символов.
     */
    @FXML
    private PasswordField passwordField;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Обработчик события регистрации нового пользователя.
     * Отправляет HTTP-запрос на сервер для создания нового пользователя.
     * В случае успеха обновляет список пользователей в главном контроллере.
     */
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

            HttpsClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь успешно зарегистрирован.");

                                // Вызываем loadUsers() в MainController, если он установлен
                                if (mainController != null) {
                                    mainController.loadUsers();
                                }

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

    /**
     * Закрывает окно регистрации без сохранения данных.
     * Вызывается при отмене процесса регистрации.
     */
    @FXML
    private void onCancel() {
        ((Stage) usernameField.getScene().getWindow()).close();
    }

    /**
     * Отображает диалоговое окно с информационным или предупреждающим сообщением.
     *
     * @param alertType Тип сообщения (INFORMATION, WARNING, ERROR)
     * @param title Заголовок диалогового окна
     * @param content Текст сообщения
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}