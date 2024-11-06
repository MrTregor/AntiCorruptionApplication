package org.anticorruption.application.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.anticorruption.application.ConfigManager;
import org.anticorruption.application.Models.Report;
import org.anticorruption.application.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
    private Stage stage;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private final String SERVER_URL = ConfigManager.getProperty("server.url");

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setReport(Report report) {
        this.report = report;
        populateFields();
    }

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

    @FXML
    private void onTakeToWork() {
        updateReportStatus("IN_PROGRESS");
    }

    @FXML
    private void onCloseReport() {
        updateReportStatus("CLOSED");
    }

    private void updateReportStatus(String status) {
        try {
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("status", status);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/api/reports/" + report.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JsonNode response = mapper.readTree(responseBody);
                            if ("OK".equals(response.get("status").asText())) {
                                javafx.application.Platform.runLater(() -> {
                                    showAlert(Alert.AlertType.INFORMATION, "Успех",
                                            "Статус заявки успешно обновлен");
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
                    "Ошибка при обновлении статуса: " + e.getMessage());
        }
    }

    @FXML
    private void onSave() {
        if (stage != null) {
            String solution = solutionArea.getText();
            report.setSolution(solution);

            try {
                ObjectNode requestBody = mapper.createObjectNode();
                requestBody.put("solution", solution);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL + "/api/reports/" + report.getId()))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(responseBody -> {
                            try {
                                JsonNode response = mapper.readTree(responseBody);
                                if ("OK".equals(response.get("status").asText())) {
                                    javafx.application.Platform.runLater(() -> {
                                        showAlert(Alert.AlertType.INFORMATION, "Успех",
                                                "Решение успешно сохранено на сервере.");
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

    @FXML
    private void onClose() {
        if (stage != null) {
            stage.close();
        }
    }

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