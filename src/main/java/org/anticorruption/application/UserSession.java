package org.anticorruption.application;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для управления сессией пользователя в антикоррупционной информационной системе.
 * <p>
 * Реализует паттерн Singleton для обеспечения единого глобального доступа
 * к информации о текущей пользовательской сессии.
 * <p>
 * Основные возможности:
 * - Хранение аутентификационного токена
 * - Управление группами пользователя
 * - Потокобезопасный доступ к информации о сессии
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 10.10.2024
 */
@Setter
@Getter
public class UserSession {

    /**
     * Статический экземпляр сессии пользователя (потоконебезопасный).
     * Рекомендуется использовать синхронизированный метод getInstance().
     */
    private static volatile UserSession instance;

    /**
     * Аутентификационный токен пользователя.
     * Используется для авторизации запросов к backend-сервисам.
     */
    private String token;

    /**
     * Список групп, к которым принадлежит пользователь.
     * Определяет права доступа и роли в системе.
     */
    private List<String> groups;

    /**
     * Имя пользователя в системе.
     */
    private String username;

    /**
     * Приватный конструктор для реализации паттерна Singleton.
     * Инициализирует пустой список групп.
     */
    private UserSession() {
        groups = new ArrayList<>();
    }

    /**
     * Потокобезопасный метод получения единственного экземпляра сессии.
     * Реализует Double-Checked Locking для оптимизации многопоточного доступа.
     *
     * @return Экземпляр UserSession
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession();
                }
            }
        }
        return instance;
    }

    /**
     * Очищает данные текущей сессии.
     * Используется при выходе пользователя из системы.
     * <p>
     * Обнуляет токен, имя пользователя и очищает список групп.
     */
    public void clear() {
        token = null;
        groups.clear(); // Безопасная очистка списка групп
        username = null;
    }

    /**
     * Проверяет наличие пользователя в указанной группе.
     *
     * @param group Название группы для проверки
     * @return true, если пользователь состоит в указанной группе
     */
    public boolean hasGroup(String group) {
        return groups != null && groups.contains(group);
    }

    /**
     * Добавляет пользователя в группу.
     *
     * @param group Название группы для добавления
     */
    public void addGroup(String group) {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        if (!groups.contains(group)) {
            groups.add(group);
        }
    }

    /**
     * Удаляет пользователя из группы.
     *
     * @param group Название группы для удаления
     */
    public void removeGroup(String group) {
        if (groups != null) {
            groups.remove(group);
        }
    }

    /**
     * Проверяет, авторизован ли пользователь.
     *
     * @return true, если пользователь имеет действующий токен
     */
    public boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }

    /**
     * Возвращает количество групп пользователя.
     *
     * @return Количество групп
     */
    public int getGroupCount() {
        return groups != null ? groups.size() : 0;
    }

    /**
     * Создает защищенную копию текущей сессии.
     *
     * @return Копия UserSession
     */
    public UserSession copy() {
        UserSession copy = new UserSession();
        copy.token = this.token;
        copy.username = this.username;
        copy.groups = new ArrayList<>(this.groups);
        return copy;
    }
}