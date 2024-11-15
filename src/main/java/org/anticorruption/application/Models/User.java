package org.anticorruption.application.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
public class User {
    // Уникальный идентификатор пользователя в системе
    private Long id;

    // Логин пользователя для входа в систему
    private String username;

    // Хэшированный пароль пользователя
    private String password;

    // Группы доступа, к которым принадлежит пользователь
    private Set<AccessGroup> groups = new HashSet<>();

    // Табельный номер сотрудника
    private String employeeId;

    // ФИО сотрудника
    private String lastName;
    private String firstName;
    private String middleName;

    private String fullName;

    // Персональные данные
    private Date dateOfBirth;     // Дата рождения
    private String gender;        // Пол

    // Фотография сотрудника
    private byte[] photo;

    // Паспортные данные
    private String passportSeries;    // Серия паспорта
    private String passportNumber;    // Номер паспорта

    // Контактная информация
    private String address;           // Адрес проживания
    private String phoneNumber;       // Контактный телефон
    private String email;             // Электронная почта

    // Информация о работе
    private String position;          // Должность
    private String department;        // Отдел/подразделение
    private Date hireDate;           // Дата приема на работу
    private String contractType;      // Тип трудового договора
    private Double salary;            // Размер заработной платы

    // Образование и квалификация
    private String education;         // Образование (уровень, учебное заведение, специальность)
    private String workExperience;    // Опыт работы
    private String skills;            // Профессиональные навыки и квалификации

    // Личная информация
    private String maritalStatus;     // Семейное положение

    public Integer getNumberOfChildren() {
        return Objects.requireNonNullElse(numberOfChildren, 0);
    }

    private Integer numberOfChildren; // Количество детей
    private String militaryServiceInfo; // Данные о воинском учете

    // Документы и идентификаторы
    private String inn;               // ИНН
    private String snils;             // СНИЛС

    // Профессиональное развитие
    private String qualificationUpgrade;   // Сведения о повышении квалификации
    private String awards;                 // Награды и поощрения
    private String disciplinaryActions;    // Дисциплинарные взыскания
    private String attestationResults;     // Результаты аттестаций

    // Дополнительная информация
    private String medicalExamResults;     // Результаты медосмотров
    private String bankDetails;            // Банковские реквизиты
    private String emergencyContact;       // Контакты для экстренной связи
    private String notes;                  // Примечания

    // Статус сотрудника
    private Boolean isFired;          // Признак увольнения
    public String getFullName() {
        return lastName + " " + firstName + " " + middleName;
    }

    @Override
    public String toString() {
        return this.getFullName();
    }
}