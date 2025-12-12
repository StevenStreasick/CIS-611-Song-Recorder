package songs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;

public class HttpClientSingletonTest {

    @Test
    void testGetInstanceNotNull() {
        CloseableHttpClient client = HttpClientSingleton.getInstance();
        assertNotNull(client, "HttpClient instance should not be null");
    }

    @Test
    void testSingletonReturnsSameInstance() {
        CloseableHttpClient client1 = HttpClientSingleton.getInstance();
        CloseableHttpClient client2 = HttpClientSingleton.getInstance();
        
        assertEquals(client1, client2, "HttpClientSingleton should return the same instance");
    }

    @Test
    void testDefaultRequestConfigUsesStandardCookieSpec() throws Exception {
        CloseableHttpClient client = HttpClientSingleton.getInstance();

        Field field = client.getClass().getDeclaredField("defaultConfig");
        field.setAccessible(true);

        RequestConfig config = (RequestConfig) field.get(client);

        assertNotNull(config, "Default RequestConfig should not be null");

        assertEquals(
            CookieSpecs.STANDARD,
            config.getCookieSpec(),
            "Cookie spec should be STANDARD"
        );
    }
}