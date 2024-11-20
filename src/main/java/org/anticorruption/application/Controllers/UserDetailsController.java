package org.anticorruption.application.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.HttpsClient;
import org.anticorruption.application.Models.AccessGroup;
import org.anticorruption.application.Models.User;
import org.anticorruption.application.UserSession;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления деталями пользователя в антикоррупционной информационной системе.
 * Обеспечивает отображение, редактирование и обновление информации о пользователе.
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 2024-10-10
 */
public class UserDetailsController {
    private final String SERVER_URL = ConfigManager.getProperty("server.url");
    // Основная информация
    @FXML
    public TextField passportSeriesField;
    @FXML
    public TextField passportNumberField;
    @FXML
    public CheckBox isFiredCheckBox;
    @FXML
    public TextArea educationArea;
    @FXML
    public TextArea workExperienceArea;
    @FXML
    public TextArea skillsArea;
    @FXML
    public TextField maritalStatusField;
    @FXML
    public TextField numberOfChildren;
    @FXML
    public TextField militaryServiceInfoField;
    @FXML
    public TextField innField;
    @FXML
    public TextField snilsField;
    @FXML
    public TextArea qualificationUpgradeArea;
    @FXML
    public TextArea awardsArea;
    @FXML
    public TextArea disciplinaryActionsArea;
    @FXML
    public TextArea attestationResultsArea;
    @FXML
    public TextArea medicalExamResultsArea;
    @FXML
    public TextArea bankDetailsArea;
    @FXML
    public TextArea emergencyContactArea;
    @FXML
    public TextArea notesArea;
    @FXML
    public TabPane tabPane;
    @FXML
    public Button saveButton;
    @FXML
    public Button cancelButton;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField middleNameField;
    @FXML
    private DatePicker dateOfBirthPicker;
    @FXML
    private ComboBox<String> genderComboBox;

    // Контактная информация
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField addressField;

    // Рабочая информация
    @FXML
    private TextField employeeIdField;
    @FXML
    private TextField positionField;
    @FXML
    private TextField departmentField;
    @FXML
    private DatePicker hireDatePicker;
    @FXML
    private TextField contractTypeField;
    @FXML
    private TextField salaryField;

    // Группы доступа
    @FXML
    private ListView<String> groupsListView;


    private User user;
    @Setter
    private Stage dialogStage;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Очищает все поля формы.
     * Используется при создании нового пользователя или сбросе формы.
     */
    private void clearFields() {
        usernameField.clear();
        lastNameField.clear();
        firstNameField.clear();
        middleNameField.clear();
        dateOfBirthPicker.setValue(null);
        genderComboBox.setValue(null);
        passportSeriesField.clear();
        passportNumberField.clear();
        emailField.clear();
        phoneNumberField.clear();
        addressField.clear();
        employeeIdField.clear();
        positionField.clear();
        hireDatePicker.setValue(null);
        departmentField.clear();
        contractTypeField.clear();
        salaryField.clear();
        educationArea.clear();
        workExperienceArea.clear();
        skillsArea.clear();
        maritalStatusField.clear();
        numberOfChildren.clear();
        militaryServiceInfoField.clear();
        innField.clear();
        snilsField.clear();
    }

    /**
     * Инициализирует компоненты интерфейса при загрузке.
     * Настраивает ComboBox для пола, ListView для групп доступа и загружает списки групп.
     */
    @FXML
    private void initialize() {
        // Настройка ComboBox для пола
        genderComboBox.getItems().addAll("MALE", "FEMALE");

        // Настройка ListView для групп доступа
        if (groupsListView != null) {
            groupsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Загрузка списка доступных групп
        loadAvailableGroups();

        // Настройка кнопок добавления и удаления групп
        addGroupButton.setOnAction(event -> addAccessGroup());
        removeGroupButton.setOnAction(event -> removeAccessGroup());
    }

    /**
     * Устанавливает пользователя для отображения и редактирования.
     *
     * @param user Пользователь, информацию которого необходимо показать
     */
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            populateFields(); // Заполните поля данными пользователя
        } else {
            // Если пользователь null, очистите поля для нового пользователя
            clearFields();
        }
    }

    /**
     * Заполняет поля интерфейса данными из модели пользователя.
     * Использует рефлексию для безопасного получения значений полей.
     */
    private void populateFields() {
        if (user == null) return;

        // Основная информация
        usernameField.setText(user.getUsername());
        lastNameField.setText(user.getLastName());
        firstNameField.setText(user.getFirstName());
        middleNameField.setText(user.getMiddleName());

        // Дата рождения
        if (user.getDateOfBirth() != null) {
            dateOfBirthPicker.setValue(user.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        // Пол
        genderComboBox.setValue(user.getGender());

        // Контактная информация
        emailField.setText(user.getEmail());
        phoneNumberField.setText(user.getPhoneNumber());
        addressField.setText(user.getAddress());

        // Рабочая информация
        employeeIdField.setText(user.getEmployeeId());
        positionField.setText(user.getPosition());
        departmentField.setText(user.getDepartment());

        // Дата найма
        if (user.getHireDate() != null) {
            hireDatePicker.setValue(user.getHireDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        contractTypeField.setText(user.getContractType());

        // Зарплата
        if (user.getSalary() != null) {
            salaryField.setText(user.getSalary().toString());
        }

        // Паспортные данные
        setFieldIfMethodExists("getPassportSeries", passportSeriesField);
        setFieldIfMethodExists("getPassportNumber", passportNumberField);

        // Личная информация
        setFieldIfMethodExists("getMaritalStatus", maritalStatusField);
        setFieldIfMethodExists("getMilitaryServiceInfo", militaryServiceInfoField);

        // Количество детей
        numberOfChildren.setText(user.getNumberOfChildren().toString());

        // Статус увольнения
        setBooleanFieldIfMethodExists("getIsFired", isFiredCheckBox);

        // Документы
        setFieldIfMethodExists("getInn", innField);
        setFieldIfMethodExists("getSnils", snilsField);

        // Образование и квалификация
        setTextAreaIfMethodExists("getEducation", educationArea);
        setTextAreaIfMethodExists("getWorkExperience", workExperienceArea);
        setTextAreaIfMethodExists("getSkills", skillsArea);

        // Профессиональное развитие
        setTextAreaIfMethodExists("getQualificationUpgrade", qualificationUpgradeArea);
        setTextAreaIfMethodExists("getAwards", awardsArea);
        setTextAreaIfMethodExists("getDisciplinaryActions", disciplinaryActionsArea);
        setTextAreaIfMethodExists("getAttestationResults", attestationResultsArea);

        // Дополнительная информация
        setTextAreaIfMethodExists("getMedicalExamResults", medicalExamResultsArea);
        setTextAreaIfMethodExists("getBankDetails", bankDetailsArea);
        setTextAreaIfMethodExists("getEmergencyContact", emergencyContactArea);
        setTextAreaIfMethodExists("getNotes", notesArea);
        // Группы доступа
        if (groupsListView != null) {
            groupsListView.getItems().clear();
            if (user.getGroups() != null) {
                user.getGroups().stream()
                        .map(AccessGroup::getName)
                        .forEach(groupsListView.getItems()::add);
            }
        }
    }

    /**
     * Обработчик события сохранения изменений пользователя.
     * Собирает измененные данные и отправляет запрос на обновление на сервер.
     * Проверяет изменения по каждому полю перед отправкой.
     */
    @FXML
    private void onSave() {
        try {
            // Создание JSON-объекта для обновления пользователя
            ObjectNode requestBody = mapper.createObjectNode();

            // Проверка и добавление измененных полей
            checkAndAddField("username", usernameField, requestBody);
            checkAndAddField("lastName", lastNameField, requestBody);
            checkAndAddField("firstName", firstNameField, requestBody);
            checkAndAddField("middleName", middleNameField, requestBody);

            // Дата рождения
            if (dateOfBirthPicker.getValue() != null) {
                requestBody.put("dateOfBirth",
                        dateOfBirthPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant().toString());
            } else if (user.getDateOfBirth() != null) {
                // Если дата рождения была установлена, но теперь она null, можно удалить это поле из запроса
                requestBody.putNull("dateOfBirth");
            }

            // Пол
            if (genderComboBox.getValue() != null && !genderComboBox.getValue().equals(user.getGender())) {
                requestBody.put("gender", genderComboBox.getValue());
            }

            // Контактная информация
            checkAndAddField("email", emailField, requestBody);
            checkAndAddField("phoneNumber", phoneNumberField, requestBody);
            checkAndAddField("address", addressField, requestBody);

            // Рабочая информация
            checkAndAddField("employeeId", employeeIdField, requestBody);
            checkAndAddField("position", positionField, requestBody);
            checkAndAddField("department", departmentField, requestBody);

            // Дата найма
            if (hireDatePicker.getValue() != null) {
                requestBody.put("hireDate",
                        hireDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant().toString());
            } else if (user.getHireDate() != null) {
                // Если дата найма была установлена, но теперь она null, можно удалить это поле из запроса
                requestBody.putNull("hireDate");
            }

            checkAndAddField("contractType", contractTypeField, requestBody);

            // Зарплата
            if (!salaryField.getText().isEmpty()) {
                try {
                    Double salary = Double.parseDouble(salaryField.getText());
                    if (!salary.equals(user.getSalary())) {
                        requestBody.put("salary", salary);
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Ошибка", "Некорректное значение зарплаты");
                    return;
                }
            }

            // Паспортные данные
            checkAndAddField("passportSeries", passportSeriesField, requestBody);
            checkAndAddField("passportNumber", passportNumberField, requestBody);

            // Личная информация
            checkAndAddField("maritalStatus", maritalStatusField, requestBody);

            // Количество детей
            if (!numberOfChildren.getText().isEmpty()) {
                try {
                    Integer children = Integer.parseInt(numberOfChildren.getText());
                    if (!children.equals(user.getNumberOfChildren())) {
                        requestBody.put("numberOfChildren", children);
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Ошибка", "Некорректное количество детей");
                    return;
                }
            }

            checkAndAddField("militaryServiceInfo", militaryServiceInfoField, requestBody);

            // Статус увольнения
            if (isFiredCheckBox.isSelected() != user.getIsFired()) {
                requestBody.put("isFired", isFiredCheckBox.isSelected());
            }

            // Документы
            checkAndAddField("inn", innField, requestBody);
            checkAndAddField("snils", snilsField, requestBody);

            // Образование и квалификация
            checkAndAddTextArea("education", educationArea, requestBody);
            checkAndAddTextArea("workExperience", workExperienceArea, requestBody);
            checkAndAddTextArea("skills", skillsArea, requestBody);

            // Профессиональное развитие
            checkAndAddTextArea("qualificationUpgrade", qualificationUpgradeArea, requestBody);
            checkAndAddTextArea("awards", awardsArea, requestBody);
            checkAndAddTextArea("disciplinaryActions", disciplinaryActionsArea, requestBody);
            checkAndAddTextArea("attestationResults", attestationResultsArea, requestBody);

            // Дополнительная информация
            checkAndAddTextArea("medicalExamResults", medicalExamResultsArea, requestBody);
            checkAndAddTextArea("bankDetails", bankDetailsArea, requestBody);
            checkAndAddTextArea("emergencyContact", emergencyContactArea, requestBody);
            checkAndAddTextArea("notes", notesArea, requestBody);

            // Группы доступа (если изменились)
            if (groupsListView.getItems() != null) {
                List<String> currentGroups = groupsListView.getItems();
                List<String> originalGroups = user.getGroups() != null
                        ? user.getGroups().stream().map(AccessGroup::getName).toList()
                        : new ArrayList<>();

                if (!currentGroups.equals(originalGroups)) {
                    requestBody.putArray("groups").addAll(
                            currentGroups.stream()
                                    .map(groupName -> {
                                        // Найдем ID группы по имени
                                        AccessGroup group = availableGroups.stream()
                                                .filter(g -> g.getName().equals(groupName))
                                                .findFirst()
                                                .orElse(null);

                                        ObjectNode groupNode = mapper.createObjectNode();
                                        if (group != null) {
                                            groupNode.put("id", group.getId());
                                        }
                                        groupNode.put("name", groupName);
                                        return groupNode;
                                    })
                                    .collect(Collectors.toList())
                    );
                }
            }

            // Если нет изменений, выходим
            if (requestBody.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Информация", "Нет изменений для обновления.");
                return;
            }

            // Отправка запроса на обновление пользователя
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/users/update/" + user.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpsClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Успех", "Данные пользователя успешно обновлены.");
                                if (dialogStage != null) {
                                    dialogStage.close();
                                }
                            });
                        } else {
                            Platform.runLater(() ->
                                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить данные пользователя.")
                            );
                        }
                    });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для проверки и добавления измененного текстового поля в запрос.
     *
     * @param fieldName   Имя поля
     * @param field       Текстовое поле для проверки
     * @param requestBody JSON-объект для добавления изменений
     */
    private void checkAndAddField(String fieldName, TextField field, ObjectNode requestBody) {
        String currentValue = field.getText();
        try {
            Method getter = user.getClass().getMethod("get" +
                    fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            String originalValue = (String) getter.invoke(user);

            if (currentValue != null && !currentValue.equals(originalValue)) {
                requestBody.put(fieldName, currentValue);
            }
        } catch (Exception e) {
            // Обработка ошибок рефлексии
        }
    }

    private void checkAndAddTextArea(String fieldName, TextArea area, ObjectNode requestBody) {
        String currentValue = area.getText();
        try {
            Method getter = user.getClass().getMethod("get" +
                    fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            String originalValue = (String) getter.invoke(user);

            if (currentValue != null && !currentValue.equals(originalValue)) {
                requestBody.put(fieldName, currentValue);
            }
        } catch (Exception e) {
            // Обработка ошибок рефлексии
        }
    }

    /**
     * Обработчик события отмены редактирования.
     * Закрывает диалоговое окно без сохранения изменений.
     */
    @FXML
    private void onCancel() {
        dialogStage.close();
    }


    /**
     * Вспомогательный метод для установки значения текстового поля с использованием рефлексии.
     *
     * @param methodName Имя метода геттера для получения значения
     * @param field      Текстовое поле для установки значения
     */
    private void setFieldIfMethodExists(String methodName, TextField field) {
        try {
            Method method = user.getClass().getMethod(methodName);
            String value = (String) method.invoke(user);
            field.setText(value);
        } catch (Exception e) {
            // Метод не существует или вызвал ошибку
        }
    }

    private void setTextAreaIfMethodExists(String methodName, TextArea area) {
        try {
            Method method = user.getClass().getMethod(methodName);
            String value = (String) method.invoke(user);
            area.setText(value);
        } catch (Exception e) {
            // Метод не существует или вызвал ошибку
        }
    }

    private void setBooleanFieldIfMethodExists(String methodName, CheckBox checkBox) {
        try {
            Method method = user.getClass().getMethod(methodName);
            Boolean value = (Boolean) method.invoke(user);
            checkBox.setSelected(value);
        } catch (Exception e) {
            // Метод не существует или вызвал ошибку
        }
    }

    /**
     * Отображает диалоговое окно с сообщением.
     *
     * @param alertType Тип сообщения (INFORMATION, WARNING, ERROR)
     * @param title     Заголовок сообщения
     * @param content   Текст сообщения
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private ComboBox<String> availableGroupsComboBox;
    @FXML
    private Button addGroupButton;
    @FXML
    private Button removeGroupButton;

    private final List<AccessGroup> availableGroups = new ArrayList<>();


    /**
     * Загружает список доступных групп доступа с сервера.
     * Используется для настройки прав пользователя.
     */
    private void loadAvailableGroups() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/access-groups"))
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .GET()
                    .build();

            HttpsClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                JsonNode groupsNode = mapper.readTree(response.body()).get("data");
                                List<String> groupNames = new ArrayList<>();
                                availableGroups.clear();

                                for (JsonNode groupNode : groupsNode) {
                                    Long id = groupNode.get("id").asLong();
                                    String name = groupNode.get("name").asText();
                                    availableGroups.add(new AccessGroup(id, name));
                                    groupNames.add(name);
                                }

                                Platform.runLater(() -> {
                                    availableGroupsComboBox.getItems().clear();
                                    availableGroupsComboBox.getItems().addAll(groupNames);
                                });
                            } catch (Exception e) {
                                e.printStackTrace(System.err);
                            }
                        }
                        return response;
                    });
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Добавляет новую группу доступа в список групп пользователя.
     */
    private void addAccessGroup() {
        String selectedGroup = availableGroupsComboBox.getValue();
        if (selectedGroup != null && !groupsListView.getItems().contains(selectedGroup)) {
            groupsListView.getItems().add(selectedGroup);
        }
    }

    /**
     * Удаляет выбранную группу доступа из списка групп пользователя.
     */
    private void removeAccessGroup() {
        String selectedGroup = groupsListView.getSelectionModel().getSelectedItem();
        if (selectedGroup != null) {
            groupsListView.getItems().remove(selectedGroup);
        }
    }
}