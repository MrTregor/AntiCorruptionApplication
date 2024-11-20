package org.anticorruption.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Менеджер конфигурации для антикоррупционной информационной системы.
 * <p>
 * Обеспечивает централизованное управление конфигурационными параметрами
 * приложения через файл config.properties.
 * <p>
 * Основные возможности:
 * - Загрузка конфигурационных параметров при старте приложения
 * - Статический доступ к параметрам через ключи
 * - Безопасная обработка ошибок при загрузке конфигурации
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @since 10.10.2024
 */
public class ConfigManager {

    /**
     * Статический объект Properties для хранения конфигурационных параметров.
     * Загружается единожды при инициализации класса.
     */
    private static final Properties properties = new Properties();

    /**
     * Блок статической инициализации для загрузки конфигурационного файла.
     *
     * Выполняется один раз при первом обращении к классу.
     * Обрабатывает возможные ошибки при чтении файла конфигурации.
     */
    static {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                // Логирование отсутствия файла конфигурации
                System.err.println("Ошибка: Не найден файл конфигурации config.properties");
            } else {
                // Загрузка параметров из файла
                properties.load(input);
            }
        } catch (IOException ex) {
            // Обработка и логирование ошибок ввода-вывода
            System.err.println("Критическая ошибка при загрузке конфигурации: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Приватный конструктор для предотвращения создания экземпляров класса.
     * Класс предназначен только для статического использования.
     */
    private ConfigManager() {
        throw new IllegalStateException("Утилитарный класс не может быть инстанцирован");
    }

    /**
     * Получает значение конфигурационного параметра по ключу.
     *
     * @param key Ключ параметра в файле config.properties
     * @return Значение параметра или null, если параметр не найден
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Получает значение конфигурационного параметра с значением по умолчанию.
     *
     * @param key          Ключ параметра в файле config.properties
     * @param defaultValue Значение по умолчанию, если параметр не найден
     * @return Значение параметра или значение по умолчанию
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Получает целочисленное значение конфигурационного параметра.
     *
     * @param key Ключ параметра в файле config.properties
     * @return Целочисленное значение параметра или null, если параметр не найден или не является числом
     */
    public static Integer getIntProperty(String key) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Получает целочисленное значение конфигурационного параметра с значением по умолчанию.
     *
     * @param key          Ключ параметра в файле config.properties
     * @param defaultValue Значение по умолчанию, если параметр не найден
     * @return Целочисленное значение параметра или значение по умолчанию
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Получает булево значение конфигурационного параметра.
     *
     * @param key Ключ параметра в файле config.properties
     * @return Булево значение параметра или null, если параметр не найден
     */
    public static Boolean getBooleanProperty(String key) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    /**
     * Получает булево значение конфигурационного параметра с значением по умолчанию.
     *
     * @param key          Ключ параметра в файле config.properties
     * @param defaultValue Значение по умолчанию, если параметр не найден
     * @return Булево значение параметра или значение по умолчанию
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Проверяет наличие параметра в конфигурации.
     *
     * @param key Ключ параметра
     * @return true, если параметр существует, иначе false
     */
    public static boolean containsProperty(String key) {
        return properties.containsKey(key);
    }
}