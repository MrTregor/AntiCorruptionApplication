package org.anticorruption.application;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Setter
@Getter
public class HttpsClient {
    private static HttpsClient instance;

    private static HttpClient client;

    public static HttpClient getClient() {
        if (client == null) {
            client = HttpClient.newBuilder()
                    .sslContext(createUnsecureSSLContext())
                    .build();
        }
        return client;
    }

    // Метод создания незащищенного SSL-контекста
    private static SSLContext createUnsecureSSLContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsecure SSL context", e);
        }
    }

}
