package songs;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

abstract class HttpClientSingleton {
	public static final CloseableHttpClient HTTPCLIENT = HttpClients.createDefault();
	
	
}
