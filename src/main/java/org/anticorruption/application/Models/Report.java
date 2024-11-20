package org.anticorruption.application.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Модель отчета о коррупционном инциденте в антикоррупционной информационной системе.
 * Содержит детальную информацию о зарегистрированном сообщении о нарушении.
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 10.10.2024
 */
@Getter
@Setter
public class Report {
    /**
     * Уникальный идентификатор отчета в базе данных.
     */
    private Long id;

    /**
     * Дата подачи отчета в формате даты.
     */
    private String dateSubmitted;

    /**
     * Идентификатор лица, подавшего отчет.
     */
    private String reporterId;

    /**
     * Дата совершения инцидента.
     */
    private String incidentDate;

    /**
     * Время совершения инцидента.
     */
    private String incidentTime;

    /**
     * Место совершения инцидента.
     */
    private String incidentLocation;

    /**
     * Лица, вовлеченные в инцидент.
     */
    private String involvedPersons;

    /**
     * Подробное описание инцидента.
     */
    private String description;

    /**
     * Описание доказательств инцидента.
     */
    private String evidenceDescription;

    /**
     * Свидетели инцидента.
     */
    private String witnesses;

    /**
     * Текущий статус отчета (например, "Новый", "В работе", "Закрыт").
     */
    private String status;

    /**
     * Идентификатор сотрудника, которому назначен отчет.
     */
    private String assignedTo;

    /**
     * Полное имя сотрудника, которому назначен отчет.
     */
    private String assignedToFullName;

    /**
     * Дата последнего обновления отчета.
     */
    private String lastUpdated;

    /**
     * Решение или резолюция по инциденту.
     */
    private String solution;

    /**
     * Конструктор по умолчанию для создания пустого отчета.
     */
    public Report() {
    }

    /**
     * Создает полную копию существующего отчета.
     *
     * @param other Исходный отчет для копирования
     */
    public Report(Report other) {
        this.id = other.id;
        this.dateSubmitted = other.dateSubmitted;
        this.reporterId = other.reporterId;
        this.incidentDate = other.incidentDate;
        this.incidentTime = other.incidentTime;
        this.incidentLocation = other.incidentLocation;
        this.involvedPersons = other.involvedPersons;
        this.description = other.description;
        this.evidenceDescription = other.evidenceDescription;
        this.witnesses = other.witnesses;
        this.status = other.status;
        this.assignedTo = other.assignedTo;
        this.assignedToFullName = other.assignedToFullName;
        this.lastUpdated = other.lastUpdated;
        this.solution = other.solution;
    }

    /**
     * Проверяет, является ли отчет актуальным.
     *
     * @return true, если отчет не закрыт и не устарел
     */
    public boolean isActive() {
        return !"Закрыт".equals(status);
    }

    /**
     * Генерирует строковое представление отчета.
     *
     * @return Краткая информация об отчете
     */
    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", incidentDate='" + incidentDate + '\'' +
                ", status='" + status + '\'' +
                ", assignedToFullName='" + assignedToFullName + '\'' +
                '}';
    }

    /**
     * Сравнивает два отчета по их идентификаторам.
     *
     * @param o Объект для сравнения
     * @return true, если отчеты имеют одинаковый идентификатор
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(id, report.id);
    }

    /**
     * Генерирует хеш-код отчета на основе его идентификатора.
     *
     * @return Хеш-код отчета
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}