package org.anticorruption.application;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Утилитарный класс для создания HTTP-клиента с отключенной проверкой SSL-сертификатов.
 * <p>
 * ВНИМАНИЕ: Использование данного класса снижает безопасность соединения
 * и НЕ РЕКОМЕНДУЕТСЯ для production-среды.
 * <p>
 * Основные возможности:
 * - Создание HTTP-клиента с отключенной проверкой сертификатов
 * - Синглтон-реализация HTTP-клиента
 * - Поддержка подключения к серверам с самоподписанными сертификатами
 *
 * @author Гордейчик Е.А.
 * @version 1.0
 * @apiNote Использовать с крайней осторожностью только в отладочных целях
 * @since 10.10.2024
 */
@Setter
@Getter
public class HttpsClient {

    /**
     * Статический экземпляр HTTP-клиента.
     * Реализация потокобезопасного синглтона с ленивой инициализацией.
     */
    private static volatile HttpClient client;

    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     * Класс предназначен только для статического использования.
     */
    private HttpsClient() {
        throw new IllegalStateException("Утилитарный класс не может быть инстанцирован");
    }

    /**
     * Потокобезопасный метод получения единственного экземпляра HTTP-клиента.
     * <p>
     * ПрименяетDoubleCheckedLocking для оптимизации многопоточной инициализации.
     *
     * @return Экземпляр HttpClient с отключенной проверкой SSL
     * @throws RuntimeException при ошибках создания SSL-контекста
     */
    public static HttpClient getClient() {
        if (client == null) {
            synchronized (HttpsClient.class) {
                if (client == null) {
                    client = HttpClient.newBuilder()
                            .sslContext(createUnsecureSSLContext())
                            .build();
                }
            }
        }
        return client;
    }

    /**
     * Создает незащищенный SSL-контекст с отключенной проверкой сертификатов.
     * <p>
     * КРИТИЧЕСКОЕ ПРЕДУПРЕЖДЕНИЕ:
     * - Полностью отключает проверку достоверности SSL-сертификатов
     * - КРАЙНЕ НЕ РЕКОМЕНДУЕТСЯ использовать в production
     * - Применимо только для отладки и локальной разработки
     *
     * @return Незащищенный SSL-контекст
     * @throws RuntimeException при ошибках инициализации SSL-контекста
     */
    private static SSLContext createUnsecureSSLContext() {
        try {
            // Создание трастового менеджера, принимающего любые сертификаты
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            // Отключение проверки клиентского сертификата
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            // Отключение проверки серверного сертификата
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            // Возврат пустого массива доверенных сертификатов
                            return new X509Certificate[0];
                        }
                    }
            };

            // Создание SSL-контекста с отключенной проверкой
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            // Логирование и преобразование исключения
            System.err.println("Критическая ошибка создания незащищенного SSL-контекста: " + e.getMessage());
            throw new RuntimeException("Не удалось создать незащищенный SSL-контекст", e);
        }
    }

    /**
     * Создает HTTP-клиента с кастомной конфигурацией.
     *
     * @param connectTimeout Таймаут подключения в секундах
     * @return Сконфигурированный HTTP-клиент
     */
    public static HttpClient getConfiguredClient(int connectTimeout) {
        return HttpClient.newBuilder()
                .sslContext(createUnsecureSSLContext())
                .connectTimeout(java.time.Duration.ofSeconds(connectTimeout))
                .build();
    }

    /**
     * Проверяет, является ли текущий SSL-контекст незащищенным.
     *
     * @return true, если используется незащищенный SSL-контекст
     */
    public static boolean isUnsecureContextActive() {
        return client != null &&
                client.sslContext().getProtocol().equals("TLS");
    }
}