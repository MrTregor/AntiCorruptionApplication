package org.anticorruption.application.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Setter;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.HttpsClient;
import org.anticorruption.application.Models.Report;
import org.anticorruption.application.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Контроллер для управления деталями отчета в антикоррупционной информационной системе.
 * Обеспечивает отображение, редактирование и обновление статуса отчета.
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 2024-10-10
 */
public class ReportDetailsController {
    @FXML
    private Label incidentDateLabel;
    @FXML
    private Label incidentTimeLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label involvedPersonsLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextArea evidenceArea;
    @FXML
    private Label witnessesLabel;
    @FXML
    private TextArea solutionArea;

    private Report report;
    @Setter
    private Stage stage;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private final String SERVER_URL = ConfigManager.getProperty("server.url");

    /**
     * Устанавливает отчет для отображения и заполняет поля интерфейса.
     *
     * @param report Отчет, детали которого будут показаны
     */
    public void setReport(Report report) {
        this.report = report;
        populateFields();
    }

    /**
     * Заполняет поля интерфейса данными из отчета.
     * Вызывается после установки отчета через {@link #setReport(Report)}.
     */
    private void populateFields() {
        incidentDateLabel.setText(report.getIncidentDate());
        incidentTimeLabel.setText(report.getIncidentTime());
        locationLabel.setText(report.getIncidentLocation());
        involvedPersonsLabel.setText(report.getInvolvedPersons());
        descriptionArea.setText(report.getDescription());
        evidenceArea.setText(report.getEvidenceDescription());
        witnessesLabel.setText(report.getWitnesses());
        solutionArea.setText(report.getSolution() != null ? report.getSolution() : "");
    }

    /**
     * Обработчик события взятия отчета в работу.
     * Изменяет статус отчета на "В процессе" (IN_PROGRESS).
     */
    @FXML
    private void onTakeToWork() {
        updateReportStatus("IN_PROGRESS");
    }

    /**
     * Обработчик события закрытия отчета.
     * Изменяет статус отчета на "Закрыт" (CLOSED).
     */
    @FXML
    private void onCloseReport() {
        updateReportStatus("CLOSED");
    }

    /**
     * Обновляет статус отчета на сервере.
     *
     * @param status Новый статус отчета (NEW, IN_PROGRESS, CLOSED)
     */
    private void updateReportStatus(String status) {
        try {
            if ("CLOSED".equals(status)) {
                onSave();
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/reports/" + report.getId() + "/status")) // Измените на PATCH
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .method("PATCH", HttpRequest.BodyPublishers.ofString("\"" + status + "\"")) // Используйте PATCH
                    .build();

            HttpsClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JsonNode response = mapper.readTree(responseBody);
                            if ("OK".equals(response.get("status").asText())) {
                                javafx.application.Platform.runLater(() -> {
                                    showAlert(Alert.AlertType.INFORMATION, "Успех",
                                            "Статус заявки успешно обновлен");
                                    // Закрыть окно только если статус "CLOSED"
                                    if ("CLOSED".equals(status)) {
                                        stage.close();
                                    }
                                });
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Ошибка",
                                        response.get("message").asText());
                            }
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Ошибка",
                                    "Ошибка при обработке ответа: " + e.getMessage());
                        }
                    })
                    .exceptionally(e -> {
                        showAlert(Alert.AlertType.ERROR, "Ошибка",
                                "Ошибка при отправке запроса: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Ошибка при обновлении статуса: " + e.getMessage());
        }
    }

    /**
     * Сохраняет решение по отчету на сервере.
     * Доступ к сохранению зависит от прав пользователя:
     * - Группа AccessToAllReports: полное обновление отчета
     * - Группа SolveReport: обновление только решения
     */
    @FXML
    private void onSave() {
        if (stage != null) {
            String solution = solutionArea.getText();
            report.setSolution(solution);

            try {
                UserSession userSession = UserSession.getInstance();

                HttpRequest request;

                // Проверяем, есть ли у пользователя группа AccessToAllReports
                if (userSession.hasGroup("AccessToAllReports")) {
                    // Полное обновление отчета
                    request = HttpRequest.newBuilder()
                            .uri(URI.create(SERVER_URL + "/api/reports/" + report.getId()))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + userSession.getToken())
                            .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(report)))
                            .build();
                } else if (userSession.hasGroup("SolveReport")) {
                    // Обновление только решения
                    request = HttpRequest.newBuilder()
                            .uri(URI.create(SERVER_URL + "/api/reports/" + report.getId() + "/solution"))
                            .header("Content-Type", "text/plain")
                            .header("Authorization", "Bearer " + userSession.getToken())
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(solution))
                            .build();
                } else {
                    // Если нет необходимых прав
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "У вас недостаточно прав для изменения отчета.");
                    return;
                }

                HttpsClient.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(responseBody -> {
                            try {
                                JsonNode response = mapper.readTree(responseBody);
                                if ("OK".equals(response.get("status").asText())) {
                                    javafx.application.Platform.runLater(() -> {
                                        showAlert(Alert.AlertType.INFORMATION, "Успех",
                                                "Успешно сохранено на сервере.");
                                        stage.close();
                                    });
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Ошибка",
                                            response.get("message").asText());
                                }
                            } catch (Exception e) {
                                showAlert(Alert.AlertType.ERROR, "Ошибка",
                                        "Ошибка при обработке ответа: " + e.getMessage());
                            }
                        })
                        .exceptionally(e -> {
                            showAlert(Alert.AlertType.ERROR, "Ошибка",
                                    "Ошибка при отправке запроса: " + e.getMessage());
                            return null;
                        });

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка",
                        "Ошибка при сохранении решения: " + e.getMessage());
            }
        }
    }

    /**
     * Закрывает окно деталей отчета.
     */
    @FXML
    private void onClose() {
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Отображает всплывающее диалоговое окно с сообщением.
     *
     * @param alertType Тип alerts (INFORMATION, WARNING, ERROR и т.д.)
     * @param title Заголовок диалогового окна
     * @param message Текст сообщения
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}