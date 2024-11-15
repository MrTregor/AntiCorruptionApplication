package org.anticorruption.application.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.Models.Report;
import org.anticorruption.application.Models.User;
import org.anticorruption.application.UserSession;

import static org.anticorruption.application.AlertUtils.showAlert;

public class MainController implements Initializable {
    private final String SERVER_URL = ConfigManager.getProperty("server.url");

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
        setupTimeField();
        setupReportsTable();
        setupUsersTable(); // Добавьте этот метод
        loadReports(); // Загружаем данные при инициализации
        loadUsers(); // Загружаем пользователей при инициализации
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
                    .uri(URI.create(SERVER_URL + "/api/reports"))
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

    private void clearReportForm() {
        incidentDatePicker.setValue(null);
        incidentTimeField.clear();
        incidentLocationField.clear();
        involvedPersonsField.clear();
        descriptionArea.clear();
        evidenceDescriptionArea.clear();
        witnessesField.clear();
    }

    @FXML
    private TableView<Report> reportsTable;
    @FXML
    private TableColumn<Report, Long> idColumn;
    @FXML
    private TableColumn<Report, String> statusColumn;
    @FXML
    private TableColumn<Report, String> solutionColumn;

    private ObservableList<Report> reportsData = FXCollections.observableArrayList();


    private void setupReportsTable() {
        // Настраиваем колонки
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        solutionColumn.setCellValueFactory(new PropertyValueFactory<>("solution"));

        // Привязываем данные
        reportsTable.setItems(reportsData);

        // Добавляем обработчик двойного клика
        reportsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Report selectedReport = reportsTable.getSelectionModel().getSelectedItem();
                if (selectedReport != null) {
                    showReportDetails(selectedReport);
                }
            }
        });
    }

    @FXML
    private void refreshReports() {
        loadReports();
    }

    private void loadReports() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/api/reports"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleReportsResponse)
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Ошибка",
                                    "Ошибка при загрузке данных: " + e.getMessage())
                    );
                    return null;
                });
    }

    @FXML
    private Label accessMessageLabel; // Добавьте это поле

    private void handleReportsResponse(String responseBody) {
        try {
            JsonNode response = mapper.readTree(responseBody);

            // Добавим проверку на null перед вызовом asText()
            JsonNode statusNode = response.get("status");
            if (statusNode == null) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Ошибка",
                                "Некорректный ответ сервера: отсутствует статус")
                );
                return;
            }

            // Проверяем статус с безопасным получением текста
            String status = statusNode.asText("");
            if ("OK".equals(status)) {
                JsonNode dataNode = response.get("data");
                if (dataNode != null && dataNode.isArray()) {
                    List<Report> reports = mapper.readValue(
                            dataNode.toString(),
                            mapper.getTypeFactory().constructCollectionType(List.class, Report.class)
                    );

                    Platform.runLater(() -> {
                        reportsData.clear();
                        reportsData.addAll(reports);
                        accessMessageLabel.setText(""); // Скрываем сообщение
                    });
                } else {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Ошибка",
                                    response.get("message").asText())
                    );
                }
            } else if ("UNAUTHORIZED".equals(status)) {
                // Обработка статуса UNAUTHORIZED
                Platform.runLater(() -> {
                    accessMessageLabel.setText("Запросите у администратора доступ.");
                });
            } else {
                // Безопасное получение сообщения об ошибке
                JsonNode messageNode = response.get("message");
                String errorMessage = messageNode != null ? messageNode.asText("Неизвестная ошибка") : "Неизвестная ошибка";

                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Ошибка", errorMessage)
                );
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                System.err.println("Ошибка при обработке ответа: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Ошибка",
                        "Ошибка при обработке данных: " + e.getMessage());
            });
        }
    }

    private void showReportDetails(Report report) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/anticorruption/application/report_details.fxml"));
            Parent root = loader.load();

            ReportDetailsController controller = loader.getController();

            Stage stage = new Stage();
            // Устанавливаем stage в контроллер
            controller.setStage(stage);
            // Устанавливаем report в контроллер
            controller.setReport(report);

            stage.setTitle("Детали доноса #" + report.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // После закрытия окна обновляем данные
            loadReports();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Ошибка при открытии окна деталей: " + e.getMessage() + "\n" +
                            "Причина: " + (e.getCause() != null ? e.getCause().getMessage() : "неизвестна"));
        }
    }

    @FXML
    private TableView<User> usersTable; // Добавьте это поле
    @FXML
    private TableColumn<User, String> usernameColumn; // Колонка для UserName
    @FXML
    private TableColumn<User, String> fullNameColumn; // Колонка для ФИО

    private ObservableList<User> usersData = FXCollections.observableArrayList(); // Данные пользователей

    private void loadUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/api/users"))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleUsersResponse)
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Ошибка",
                                    "Ошибка при загрузке пользователей: " + e.getMessage())
                    );
                    return null;
                });
    }

    private void handleUsersResponse(String responseBody) {
        try {
            JsonNode response = mapper.readTree(responseBody);
            JsonNode dataNode = response.get("data");
            if (dataNode != null && dataNode.isArray()) {
                List<User> users = mapper.readValue(
                        dataNode.toString(),
                        mapper.getTypeFactory().constructCollectionType(List.class, User.class)
                );
                List<User> finalUsers = users.stream().peek(user -> user.setFullName((user.getFirstName() + " " + user.getLastName() + " " + user.getMiddleName()).equals("null null null") ? "" : user.getFirstName() + " " + user.getLastName() + " " + user.getMiddleName())).toList();
                Platform.runLater(() -> {
                    usersData.clear();
                    usersData.addAll(finalUsers);
                    usersTable.setItems(usersData);
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                System.err.println("Ошибка при обработке ответа: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Ошибка",
                        "Ошибка при обработке данных: " + e.getMessage());
            });
        }
    }


    private void setupUsersTable() {
        // Настраиваем колонки
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        // Привязываем данные
        usersTable.setItems(usersData);

        // Добавляем обработчик двойного клика (если нужен)
        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                User selectedUser = usersTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    editUser();
                }
            }
        });
    }


    @FXML
    private void editUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/anticorruption/application/user_details.fxml"));
                Parent root = loader.load();

                UserDetailsController controller = loader.getController();
                controller.setUser(selectedUser); // Передаем выбранного пользователя контроллеру

                Stage stage = new Stage();
                controller.setDialogStage(stage);
                stage.setTitle("Редактирование пользователя");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();


                // После закрытия окна обновляем данные
                loadUsers();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Ошибка",
                        "Ошибка при открытии окна редактирования: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Выберите пользователя для редактирования.");
        }
    }

    @FXML
    private void refreshUsers() {
        loadUsers(); // Загружаем пользователей заново
    }

    @FXML
    private void deleteUser() {
        // Получаем выбранного пользователя из таблицы
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите пользователя для удаления.");
            return;
        }

        // Показываем диалог подтверждения
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Вы уверены, что хотите удалить пользователя " + selectedUser.getUsername() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Выполняем удаление
            deleteUserFromServer(selectedUser);
        }
    }

    private void deleteUserFromServer(User user) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/users/delete/" + user.getId()))
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .DELETE()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                // Успешное удаление
                                showAlert(Alert.AlertType.INFORMATION, "Успех",
                                        "Пользователь " + user.getUsername() + " успешно удален.");

                                // Обновляем список пользователей
                                loadUsers();
                            } else {
                                // Ошибка при удалении
                                showAlert(Alert.AlertType.ERROR, "Ошибка",
                                        "Не удалось удалить пользователя. Код ответа: " + response.statusCode());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Ошибка",
                                    "Ошибка при удалении пользователя: " + ex.getMessage());
                        });
                        return null;
                    });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Ошибка при подготовке запроса на удаление: " + e.getMessage());
        }
    }

    @FXML
    private void addUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/anticorruption/application/user_registration.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Регистрация пользователя");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при открытии окна регистрации: " + e.getMessage());
        }
    }

    @FXML
    private void updatePassword() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите пользователя для обновления пароля.");
            return;
        }

        // Открываем форму для ввода нового пароля
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Обновить пароль");
        dialog.setHeaderText("Введите новый пароль для пользователя: " + selectedUser.getUsername());
        dialog.setContentText("Новый пароль:");
        // Применяем CSS стиль
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/anticorruption/application/styles.css").toExternalForm());

        // Получаем новый пароль
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newPassword -> {
            updateUserPassword(selectedUser.getId(), newPassword);
        });
    }

    private void updateUserPassword(Long userId, String newPassword) {
        try {
            // Создаем JSON-объект с новым паролем
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("newPassword", newPassword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/auth/update-password/" + userId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                showAlert(Alert.AlertType.INFORMATION, "Успех", "Пароль успешно обновлен.");
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить пароль. Код ответа: " + response.statusCode());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при обновлении пароля: " + ex.getMessage());
                        });
                        return null;
                    });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при подготовке запроса на обновление пароля: " + e.getMessage());
        }
    }
}