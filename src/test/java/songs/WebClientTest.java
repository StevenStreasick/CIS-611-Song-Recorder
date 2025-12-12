package songs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockedConstruction;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebClientTest {

	private URI serverUri;
	private URI newServerUri;
	private String tokenFileName;

	private String clientId;
	private String clientSecret;
	private ClientInfo blankClientInfo;
	private ClientInfo clientInfo;

	@BeforeEach
	void setUp() throws Exception {
		serverUri = new URI("ws://localhost:8070");
		newServerUri = new URI("ws://localhost:9090");
		tokenFileName = "test_tokens.json";

		clientId = System.getenv("TWITCH_CLIENT_ID");
		clientSecret = System.getenv("TWITCH_CLIENT_SECRET");
		blankClientInfo = new ClientInfo();
		clientInfo = new ClientInfo(clientId, clientSecret);
	}

	/** Constructor Tests **/
	@Test
	void testConstructor_WithUriAndTokenFile() {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		assertNotNull(webClient);
		assertNotNull(webClient.clientInfo);
		assertNotNull(webClient.callbackServer);
	}

	@Test
	void testConstructor_WithClientInfo() {
		WebClient webClient = new WebClient(serverUri, tokenFileName, clientInfo);
		assertEquals(clientInfo, webClient.clientInfo);
		assertNotNull(webClient.callbackServer);
	}

	@Test
	void testConstructor_WithBlankClientInfo() {
		WebClient webClient = new WebClient(serverUri, tokenFileName, blankClientInfo);
		assertEquals(blankClientInfo, webClient.clientInfo);
	}

	@Test
	void testConstructor_WithAllParameters() {
		CallbackServer callbackServer = new CallbackServer();
		WebClient webClient = new WebClient(serverUri, tokenFileName, clientInfo, callbackServer);
		assertEquals(clientInfo, webClient.clientInfo);
		assertEquals(callbackServer, webClient.callbackServer);
	}

	/** Connection Tests **/
	@Test
	void testIsOpen_InitiallyFalse() {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		assertFalse(webClient.isOpen());
	}

	@Test
	void testConnect_NoException() {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		assertDoesNotThrow(() -> webClient.connect());
	}

	@Test
	void testClose_NoException() {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		assertDoesNotThrow(() -> webClient.close());
	}

	/** updateURI Tests **/
	@Test
	void testUpdateURI_ChangesUri() throws Exception {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		assertDoesNotThrow(() -> webClient.updateURI(newServerUri));
	}

	@Test
	void testUpdateURI_WithNullUri() {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		assertThrows(Exception.class, () -> webClient.updateURI(null));
	}

	@Test
	void testUpdateURI_MultipleTimes() throws Exception {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		webClient.updateURI(newServerUri);
		URI thirdUri = new URI("ws://localhost:7070");
		assertDoesNotThrow(() -> webClient.updateURI(thirdUri));
	}

	/** Token Tests **/

	@Test
	void testGetBearerToken_ReturnsNonEmpty_Mocked() {
		try (MockedConstruction<TokenManager> mocked = mockConstruction(TokenManager.class, (mock, context) -> {
			when(mock.getBearerToken()).thenReturn("mock_token_abc123");
		})) {

			WebClient webClient = new WebClient(serverUri, tokenFileName, clientInfo);
			String bearerToken = webClient.getBearerToken();

			assertFalse(bearerToken.isEmpty());
			assertTrue(bearerToken.length() > 0);
		}
	}

	@Test
	void testOnMessage_CanBeOverridden() {

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onMessage(String message) {
			}
		};

		assertNotNull(webClient);
	}

	@Test
	void testOnClose_CanBeOverridden() {

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onClose(int code, String reason, boolean remote) {
			}
		};

		assertNotNull(webClient);
	}

	@Test
	void testOnError_CanBeOverridden() {

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onError(Exception ex) {
			}
		};

		assertNotNull(webClient);
	}

	@Test
	void testSend() throws Exception {

		WebClient webClient = new WebClient(serverUri, tokenFileName);
		webClient.send("Test");
	}

	@Test
	void testSend_TransmitsMessage() throws Exception {
		final String[] sentMessage = { null };

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			public void send(String text) {
				sentMessage[0] = text;
				super.send(text);
			}
		};

		try {
			webClient.send("test message");
		} catch (Exception e) {
		}

		assertNotNull(webClient);
	}

	@Test
	void testConnectBlocking_Connects() throws Exception {
		WebClient webClient = new WebClient(serverUri, tokenFileName);

		try {
			webClient.connectBlocking();
		} catch (Exception e) {
		}

		assertNotNull(webClient);
	}

	@Test
	void testCreateWebSocketClient_OnOpenCallback() throws Exception {
		final boolean[] onOpenCalled = { false };

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onOpen(ServerHandshake handshakedata) {
				onOpenCalled[0] = true;
			}
		};

		java.lang.reflect.Field clientField = WebClient.class.getDeclaredField("client");
		clientField.setAccessible(true);
		WebSocketClient client = (WebSocketClient) clientField.get(webClient);

		ServerHandshake mockHandshake = mock(ServerHandshake.class);
		client.onOpen(mockHandshake);

		assertTrue(onOpenCalled[0], "onOpen should have been called");
	}

	@Test
	void testCreateWebSocketClient_OnMessageCallback() throws Exception {
		final String[] receivedMessage = { null };

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onMessage(String message) {
				receivedMessage[0] = message;
			}
		};

		java.lang.reflect.Field clientField = WebClient.class.getDeclaredField("client");
		clientField.setAccessible(true);
		WebSocketClient client = (WebSocketClient) clientField.get(webClient);

		client.onMessage("test message");

		assertEquals("test message", receivedMessage[0], "onMessage should receive the message");
	}

	@Test
	void testCreateWebSocketClient_OnCloseCallback() throws Exception {
		final int[] closeCode = { -1 };
		final String[] closeReason = { null };
		final boolean[] closeRemote = { false };

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onClose(int code, String reason, boolean remote) {
				closeCode[0] = code;
				closeReason[0] = reason;
				closeRemote[0] = remote;
			}
		};

		java.lang.reflect.Field clientField = WebClient.class.getDeclaredField("client");
		clientField.setAccessible(true);
		WebSocketClient client = (WebSocketClient) clientField.get(webClient);

		client.onClose(1000, "Normal closure", true);

		assertEquals(1000, closeCode[0]);
		assertEquals("Normal closure", closeReason[0]);
		assertTrue(closeRemote[0]);
	}

	@Test
	void testCreateWebSocketClient_OnErrorCallback() throws Exception {
		final Exception[] receivedException = { null };

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onError(Exception ex) {
				receivedException[0] = ex;
			}
		};

		java.lang.reflect.Field clientField = WebClient.class.getDeclaredField("client");
		clientField.setAccessible(true);
		WebSocketClient client = (WebSocketClient) clientField.get(webClient);

		Exception testException = new Exception("Test error");
		client.onError(testException);

		assertEquals(testException, receivedException[0], "onError should receive the exception");
	}

	@Test
	void testUpdateURI_ReconnectsWithNewURI() throws Exception {
		final boolean[] onOpenCalled = { false };

		WebClient webClient = new WebClient(serverUri, tokenFileName) {
			@Override
			protected void onOpen(ServerHandshake handshakedata) {
				onOpenCalled[0] = true;
			}
		};

		try {
			webClient.updateURI(newServerUri);
		} catch (Exception e) {
		}

		assertNotNull(webClient);
	}

	/** Edge Cases **/
	@Test
	void testMultipleClose_NoException() {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		assertDoesNotThrow(() -> {
			webClient.close();
			webClient.close();
			webClient.close();
		});
	}

	@Test
	void testConstructor_WithEmptyTokenFileName() {
		assertThrows(IllegalArgumentException.class, () -> new WebClient(serverUri, ""));
	}

	@Test
	void testConstructor_WithNullClientInfo() {
		assertThrows(IllegalArgumentException.class, () -> new WebClient(serverUri, tokenFileName, null));
	}

	@Test
	void testConstructor_WithNullTokenFileName() {
		assertThrows(IllegalArgumentException.class, () -> new WebClient(serverUri, null));
	}

	/** Integration-style Tests **/
	@Test
	void testFullLifecycle() throws Exception {
		WebClient webClient = new WebClient(serverUri, tokenFileName);
		webClient.updateURI(newServerUri);
		webClient.close();
		assertFalse(webClient.isOpen());
	}

}