package songs;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public final class HttpClientSingleton {

    private static final CloseableHttpClient HTTPCLIENT = HttpClients.custom()
        .setDefaultRequestConfig(
            RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build()
        )
        .build();

    private HttpClientSingleton() {}

    public static CloseableHttpClient getInstance() {
        return HTTPCLIENT;
    }
}