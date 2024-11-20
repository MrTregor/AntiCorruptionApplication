package org.anticorruption.application.Models;

import lombok.Getter;
import lombok.Setter;

/**
 * Представляет группу доступа в системе антикоррупционного контроля.
 * Используется для определения прав и ролей пользователей в приложении.
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 2024-10-10
 */
@Getter
@Setter
public class AccessGroup {
    /**
     * Уникальный идентификатор группы доступа в базе данных.
     */
    private Long id;

    /**
     * Название группы доступа, определяющее набор разрешений.
     * Например: "AdminGroup", "UserGroup", "ManagerGroup".
     */
    private String name;

    /**
     * Конструктор для создания группы доступа с указанием идентификатора и названия.
     *
     * @param id Уникальный идентификатор группы
     * @param name Название группы доступа
     */
    public AccessGroup(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Конструктор по умолчанию для создания пустого объекта группы доступа.
     * Используется в случаях, когда требуется создание объекта с последующим заполнением.
     */
    public AccessGroup() {
    }
}
