package songs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static org.mockito.Mockito.*;

class CallbackServerTest {

	private CallbackServer server;
	private CallbackServer.OAuthCallbackListener mockListener;
	private static final int TEST_PORT = 8888;

	@BeforeEach
	void setUp() {
		server = new CallbackServer(TEST_PORT);
		mockListener = mock(CallbackServer.OAuthCallbackListener.class);
		server.addListener(mockListener);
	}

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stopCallbackServer();
		}
	}

	@Test
	void testDefaultConstructor() {
		CallbackServer defaultServer = new CallbackServer();
		assertEquals(8060, defaultServer.getPort());
		assertEquals("/callback", defaultServer.getCallbackPath());
	}

	@Test
	void testCustomPortConstructor() {
		assertEquals(TEST_PORT, server.getPort());
		assertEquals("/callback", server.getCallbackPath());
	}

	@Test
	void testFullConstructor() {
		CallbackServer customServer = new CallbackServer(9000, "https://example.com", "/oauth/callback");
		assertEquals(9000, customServer.getPort());
		assertEquals("/oauth/callback", customServer.getCallbackPath());
		assertEquals("https://example.com:9000/oauth/callback", customServer.getCallbackAddress());
	}

	@Test
	void testStartServer() throws IOException, InterruptedException {
		server.startCallbackServer();

		Thread.sleep(100);

		verify(mockListener, timeout(1000)).onServerStarted(anyString());
	}

	@Test
	void testStartServerTwice() throws IOException {
		server.startCallbackServer();
		server.startCallbackServer();

		verify(mockListener, times(1)).onServerStarted(anyString());
	}

	@Test
	void testStopServer() throws IOException, InterruptedException {
		server.startCallbackServer();
		Thread.sleep(100);

		server.stopCallbackServer();

		verify(mockListener, timeout(1000)).onServerStopped();
	}

	@Test
	void testStopServerWhenNotStarted() {
		assertDoesNotThrow(() -> server.stopCallbackServer());
		verify(mockListener, never()).onServerStopped();
	}

	@Test
	void testSuccessfulCallback() throws Exception {
		server.startCallbackServer();
		Thread.sleep(100);

		String callbackUrl = "http://localhost:" + TEST_PORT + "/callback?code=test_auth_code&state=test_state";
		HttpURLConnection connection = (HttpURLConnection) new URL(callbackUrl).openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();
		assertEquals(200, responseCode);

		ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
		verify(mockListener, timeout(1000)).onCodeReceived(paramsCaptor.capture());

		Map<String, String> capturedParams = paramsCaptor.getValue();
		assertEquals("test_auth_code", capturedParams.get("code"));
		assertEquals("test_state", capturedParams.get("state"));

		connection.disconnect();
	}

	@Test
	void testErrorCallback() throws Exception {
		server.startCallbackServer();
		Thread.sleep(100);

		String callbackUrl = "http://localhost:" + TEST_PORT
				+ "/callback?error=access_denied&error_description=User+denied+access";
		HttpURLConnection connection = (HttpURLConnection) new URL(callbackUrl).openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();
		assertEquals(400, responseCode);

		verify(mockListener, timeout(1000)).onError(eq("access_denied"), contains("denied"));

		connection.disconnect();
	}

	@Test
	void testParseQueryParams() {
		Map<String, String> params = server.parseQueryParams("code=abc123&state=xyz789&extra=value");

		assertEquals(3, params.size());
		assertEquals("abc123", params.get("code"));
		assertEquals("xyz789", params.get("state"));
		assertEquals("value", params.get("extra"));
	}

	@Test
	void testParseEmptyQuery() {
		Map<String, String> params = server.parseQueryParams("");
		assertTrue(params.isEmpty());

		params = server.parseQueryParams(null);
		assertTrue(params.isEmpty());
	}

	@Test
	void testParseMalformedQuery() {
		Map<String, String> params = server.parseQueryParams("invalid&key=value&novalue");

		assertEquals(1, params.size());
		assertEquals("value", params.get("key"));
	}

	@Test
	void testListenerManagement() throws IOException, InterruptedException {
		CallbackServer.OAuthCallbackListener listener2 = mock(CallbackServer.OAuthCallbackListener.class);
		server.addListener(listener2);

		server.startCallbackServer();
		Thread.sleep(100);

		verify(mockListener, timeout(1000)).onServerStarted(anyString());
		verify(listener2, timeout(1000)).onServerStarted(anyString());

		server.removeListener(listener2);
		server.stopCallbackServer();

		verify(mockListener, timeout(1000)).onServerStopped();
		verify(listener2, never()).onServerStopped();
	}

	@Test
	void testListenerExceptionHandling() throws Exception {
		CallbackServer.OAuthCallbackListener faultyListener = mock(CallbackServer.OAuthCallbackListener.class);
		doThrow(new RuntimeException("Test exception")).when(faultyListener).onCodeReceived(any());

		server.addListener(faultyListener);
		server.startCallbackServer();
		Thread.sleep(100);

		String callbackUrl = "http://localhost:" + TEST_PORT + "/callback?code=test_code";
		HttpURLConnection connection = (HttpURLConnection) new URL(callbackUrl).openConnection();
		connection.setRequestMethod("GET");
		connection.getResponseCode();

		verify(faultyListener, timeout(1000)).onCodeReceived(any());
		verify(mockListener, timeout(1000)).onCodeReceived(any());

		connection.disconnect();
	}

	@Test
	void testGetCallbackAddress() {
		assertEquals("http://localhost:" + TEST_PORT + "/callback", server.getCallbackAddress());
	}

	@Test
	void testCallbackWithMultipleParameters() throws Exception {
		server.startCallbackServer();
		Thread.sleep(100);

		String callbackUrl = "http://localhost:" + TEST_PORT + "/callback?code=abc&state=xyz&scope=read&extra=data";
		HttpURLConnection connection = (HttpURLConnection) new URL(callbackUrl).openConnection();
		connection.setRequestMethod("GET");
		connection.getResponseCode();

		ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
		verify(mockListener, timeout(1000)).onCodeReceived(paramsCaptor.capture());

		Map<String, String> params = paramsCaptor.getValue();
		assertEquals(4, params.size());
		assertEquals("abc", params.get("code"));
		assertEquals("xyz", params.get("state"));
		assertEquals("read", params.get("scope"));
		assertEquals("data", params.get("extra"));

		connection.disconnect();
	}

	@Test
	void testErrorWithoutDescription() throws Exception {
		server.startCallbackServer();
		Thread.sleep(100);

		String callbackUrl = "http://localhost:" + TEST_PORT + "/callback?error=invalid_request";
		HttpURLConnection connection = (HttpURLConnection) new URL(callbackUrl).openConnection();
		connection.setRequestMethod("GET");
		connection.getResponseCode();

		verify(mockListener, timeout(1000)).onError(eq("invalid_request"), eq("Unknown error"));

		connection.disconnect();
	}
}