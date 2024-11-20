package org.anticorruption.application;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.Objects;

/**
 * Утилитарный класс для отображения системных диалоговых окон (alerts)
 * в антикоррупционной информационной системе.
 * <p>
 * Предоставляет статический метод для безопасного и унифицированного
 * отображения предупреждений, ошибок и информационных сообщений.
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 10.10.2024
 */
public class AlertUtils {

    /**
     * Приватный конструктор для предотвращения создания экземпляров утилитарного класса.
     */
    private AlertUtils() {
        throw new IllegalStateException("Утилитарный класс");
    }

    /**
     * Отображает диалоговое окно с заданным типом, заголовком и содержимым.
     * <p>
     * Метод выполняется в потоке JavaFX Platform для корректной работы с UI.
     * Применяет стандартный CSS-стиль для визуального оформления.
     *
     * @param alertType Тип диалогового окна (INFORMATION, WARNING, ERROR и др.)
     * @param title     Заголовок диалогового окна
     * @param content   Текст сообщения в диалоговом окне
     * @throws NullPointerException если не найден файл стилей
     */
    public static void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);

            // Применение корпоративного стиля к диалоговому окну
            alert.getDialogPane().getStylesheets().add(
                    Objects.requireNonNull(
                            AlertUtils.class.getResource("/org/anticorruption/application/styles.css")
                    ).toExternalForm()
            );

            alert.showAndWait();
        });
    }

    /**
     * Отображает информационное сообщение.
     *
     * @param title   Заголовок информационного сообщения
     * @param content Текст информационного сообщения
     */
    public static void showInformation(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    /**
     * Отображает предупреждение.
     *
     * @param title   Заголовок предупреждения
     * @param content Текст предупреждения
     */
    public static void showWarning(String title, String content) {
        showAlert(Alert.AlertType.WARNING, title, content);
    }

    /**
     * Отображает сообщение об ошибке.
     *
     * @param title   Заголовок сообщения об ошибке
     * @param content Текст сообщения об ошибке
     */
    public static void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    /**
     * Отображает диалоговое окно с подтверждением.
     *
     * @param title   Заголовок диалога подтверждения
     * @param content Текст диалога подтверждения
     * @return true, если пользователь нажал "ОК", иначе false
     */
    public static boolean showConfirmation(String title, String content) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(title);
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(content);

        confirmAlert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(
                        AlertUtils.class.getResource("/org/anticorruption/application/styles.css")
                ).toExternalForm()
        );

        return confirmAlert.showAndWait()
                .map(buttonType -> buttonType == javafx.scene.control.ButtonType.OK)
                .orElse(false);
    }
}