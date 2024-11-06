package org.anticorruption.application;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ResourceBundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MainController implements Initializable {

    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab createReportTab;
    @FXML
    private Tab processReportsTab;
    @FXML
    private Tab adminTab;

    @FXML
    private DatePicker incidentDatePicker;
    @FXML
    private TextField incidentTimeField;
    @FXML
    private TextField incidentLocationField;
    @FXML
    private TextField involvedPersonsField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextArea evidenceDescriptionArea;
    @FXML
    private TextField witnessesField;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
// Устанавливаем форматтер для поля времени
        setupTimeField();
    }

    public void setupTabs() {
        try {
            UserSession userSession = UserSession.getInstance();

            createReportTab.setDisable(!userSession.hasGroup("CreateReport"));
            processReportsTab.setDisable(!userSession.hasGroup("ViewReport") && !userSession.hasGroup("AccessToAllReports"));
            adminTab.setDisable(!userSession.hasGroup("ManageUserGroups"));

            mainTabPane.getTabs().removeIf(Tab::isDisable);
        } catch (Exception e) {
            System.err.println("Error in setupTabs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onSubmitReport() {
        try {
            if (!validateForm()) {
                System.err.println("Пожалуйста, заполните все обязательные поля");
                return;
            }
            ObjectNode requestBody = mapper.createObjectNode();

            // Проверка на null для incidentDatePicker
            if (incidentDatePicker.getValue() != null) {
                requestBody.put("incidentDate", incidentDatePicker.getValue().toString());
            } else {
                requestBody.putNull("incidentDate");
                // Или можно выбросить исключение, если дата обязательна
                // throw new IllegalArgumentException("Дата инцидента не выбрана");
            }

            requestBody.put("incidentDate", incidentDatePicker.getValue().toString());
            requestBody.put("incidentTime", incidentTimeField.getText());
            requestBody.put("incidentLocation", incidentLocationField.getText());
            requestBody.put("involvedPersons", involvedPersonsField.getText());
            requestBody.put("description", descriptionArea.getText());
            requestBody.put("evidenceDescription", evidenceDescriptionArea.getText());
            requestBody.put("witnesses", witnessesField.getText());

            String token = UserSession.getInstance().getToken();
            if (token == null || token.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка аутентификации", "Токен авторизации отсутствует");
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/api/reports"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleReportResponse)
                    .exceptionally(e -> {
                        System.err.println("Ошибка при отправке доноса: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private void setupTimeField() {
        // Паттерн для формата ЧЧ:ММ
        String timePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";

        // TextFormatter для поля времени
        TextFormatter<String> timeFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // Разрешаем пустую строку для возможности ввода
            if (newText.isEmpty()) {
                return change;
            }

            // Разрешаем только цифры и двоеточие
            if (!newText.matches("[0-9:]*")) {
                return null;
            }

            // Автоматически добавляем двоеточие после двух цифр
            if (newText.length() == 2 && change.getText().matches("[0-9]")) {
                change.setText(change.getText() + ":");
                change.setCaretPosition(change.getCaretPosition() + 1);
                change.setAnchor(change.getAnchor() + 1);
            }

            // Ограничиваем длину до 5 символов (ЧЧ:ММ)
            if (newText.length() > 5) {
                return null;
            }

            return change;
        });

        incidentTimeField.setTextFormatter(timeFormatter);

        // Добавляем подсказку
        incidentTimeField.setPromptText("ЧЧ:ММ");

        // Добавляем слушатель для валидации при потере фокуса
        incidentTimeField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) { // При потере фокуса
                validateTimeField();
            }
        });
    }

    private void validateTimeField() {
        String timePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        String time = incidentTimeField.getText();

        if (!time.isEmpty() && !time.matches(timePattern)) {
            showAlert(Alert.AlertType.WARNING, "Неверный формат",
                    "Пожалуйста, введите время в формате ЧЧ:ММ (например, 09:30 или 14:45)");
            incidentTimeField.requestFocus();
        }
    }

    // Обновляем метод validateForm
    private boolean validateForm() {
        if (incidentDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Ошибка валидации", "Выберите дату инцидента");
            return false;
        }

        if (incidentTimeField.getText().isEmpty() ||
                !incidentTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showAlert(Alert.AlertType.WARNING, "Ошибка валидации",
                    "Введите корректное время в формате ЧЧ:ММ");
            return false;
        }

        if (incidentLocationField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Ошибка валидации", "Укажите место инцидента");
            return false;
        }

        if (involvedPersonsField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Ошибка валидации", "Укажите вовлеченных лиц");
            return false;
        }

        if (descriptionArea.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Ошибка валидации", "Добавьте описание инцидента");
            return false;
        }

        return true;
    }

    private void handleReportResponse(String responseBody) {
        try {
            JsonNode response = mapper.readTree(responseBody);

            // Добавим вывод ответа для отладки
            System.out.println("Получен ответ от сервера: " + responseBody);

            // Безопасное получение полей
            JsonNode statusNode = response.get("status");
            JsonNode messageNode = response.get("message");

            if (statusNode != null && "CREATED".equals(statusNode.asText())) {
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Донос успешно отправлен");
                clearReportForm();
            } else {
                String errorMessage = messageNode != null ?
                        messageNode.asText() :
                        "Неизвестная ошибка при отправке доноса";
                showAlert(Alert.AlertType.ERROR, "Ошибка", errorMessage);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке ответа: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Произошла ошибка при обработке ответа от сервера: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        // Запускаем в UI потоке
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void clearReportForm() {
        incidentDatePicker.setValue(null);
        incidentTimeField.clear();
        incidentLocationField.clear();
        involvedPersonsField.clear();
        descriptionArea.clear();
        evidenceDescriptionArea.clear();
        witnessesField.clear();
    }
}