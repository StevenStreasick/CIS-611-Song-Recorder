package songs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class TwitchAPITest {

	@Mock
	private CloseableHttpClient mockHttpClient;

	@Mock
	private CloseableHttpResponse mockResponse;

	@Mock
	private StatusLine mockStatusLine;

	@Mock
	private ClientInfo mockClientInfo;

	private MockedStatic<HttpClientSingleton> mockedSingleton;

	private static final String TEST_STREAMER = "test_streamer";
	private static final String TEST_CLIENT_ID = "test_client_id";
	private static final String TEST_BEARER_TOKEN = "test_bearer_token";
	private static final int TEST_STREAMER_ID = 12345;
	private static final String STARTED_AT = "2024-12-09T10:30:00Z";

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		when(mockClientInfo.getClientID()).thenReturn(TEST_CLIENT_ID);

		mockedSingleton = mockStatic(HttpClientSingleton.class);
		mockedSingleton.when(HttpClientSingleton::getInstance).thenReturn(mockHttpClient);
	}

	@AfterEach
	void tearDown() {
		if (mockedSingleton != null) {
			mockedSingleton.close();
		}
	}

	/** Constructor Tests **/

	@Test
	void testConstructor_NullStreamerName_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			new TwitchAPI(null);
		});
	}

	@Test
	void testConstructor_BlankStreamerName_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			new TwitchAPI("");
		});
	}

	@Test
	void testConstructor_WhitespaceStreamerName_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			new TwitchAPI("   ");
		});
	}

	/** getStreamerID Tests **/

	@Test
	void testGetStreamerID_Success_viaReflection() throws Exception {
		JSONObject streamerData = new JSONObject();
		streamerData.put("id", String.valueOf(TEST_STREAMER_ID));
		streamerData.put("login", TEST_STREAMER);

		JSONArray dataArray = new JSONArray();
		dataArray.put(streamerData);

		JSONObject responseJson = new JSONObject();
		responseJson.put("data", dataArray);

		HttpEntity entity = new StringEntity(responseJson.toString());
		when(mockResponse.getEntity()).thenReturn(entity);
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		Method m = TwitchAPI.class.getDeclaredMethod("getStreamerID");
		m.setAccessible(true);
		Integer returned = (Integer) m.invoke(spy);

		assertEquals(TEST_STREAMER_ID, returned.intValue());
		verify(mockHttpClient, atLeastOnce()).execute(any());
	}

	@Test
	void testGetStreamerID_EmptyDataArray_ReturnsNegativeOne() throws Exception {
		JSONObject responseJson = new JSONObject();
		responseJson.put("data", new JSONArray());

		HttpEntity entity = new StringEntity(responseJson.toString());
		when(mockResponse.getEntity()).thenReturn(entity);
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		Method m = TwitchAPI.class.getDeclaredMethod("getStreamerID");
		m.setAccessible(true);
		Integer returned = (Integer) m.invoke(spy);

		assertEquals(-1, returned.intValue());
	}

	@Test
	void testGetStreamerID_ErrorResponse_ReturnsNegativeOne() throws Exception {
		JSONObject responseJson = new JSONObject();
		responseJson.put("error", "Unauthorized");
		responseJson.put("message", "Invalid OAuth token");

		HttpEntity entity = new StringEntity(responseJson.toString());
		when(mockResponse.getEntity()).thenReturn(entity);
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		Method m = TwitchAPI.class.getDeclaredMethod("getStreamerID");
		m.setAccessible(true);
		Integer returned = (Integer) m.invoke(spy);

		assertEquals(-1, returned.intValue());
	}

	@Test
	void testGetStreamerID_MissingDataField_ReturnsNegativeOne() throws Exception {
		JSONObject responseJson = new JSONObject();
		responseJson.put("status", "200");

		HttpEntity entity = new StringEntity(responseJson.toString());
		when(mockResponse.getEntity()).thenReturn(entity);
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		Method m = TwitchAPI.class.getDeclaredMethod("getStreamerID");
		m.setAccessible(true);
		Integer returned = (Integer) m.invoke(spy);

		assertEquals(-1, returned.intValue());
	}

	@Test
	void testGetStreamerID_BlankIDField_ReturnsNegativeOne() throws Exception {
		JSONObject streamerData = new JSONObject();
		streamerData.put("id", "");
		streamerData.put("login", TEST_STREAMER);

		JSONArray dataArray = new JSONArray();
		dataArray.put(streamerData);

		JSONObject responseJson = new JSONObject();
		responseJson.put("data", dataArray);

		HttpEntity entity = new StringEntity(responseJson.toString());
		when(mockResponse.getEntity()).thenReturn(entity);
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		Method m = TwitchAPI.class.getDeclaredMethod("getStreamerID");
		m.setAccessible(true);
		Integer returned = (Integer) m.invoke(spy);

		assertEquals(-1, returned.intValue());
	}

	/** isStreamerLive Tests **/

	@Test
	void testIsStreamerLive_StreamerIsLive_ReturnsTrue() throws Exception {

		// Call 1: /users endpoint response
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		// Call2: /streams endpoint response
		JSONObject streamData = new JSONObject();
		streamData.put("user_id", String.valueOf(TEST_STREAMER_ID));
		streamData.put("user_login", TEST_STREAMER);
		streamData.put("started_at", STARTED_AT);
		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray().put(streamData));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		boolean live = spy.isStreamerLive();

		assertTrue(live);
		assertEquals(STARTED_AT, spy.getStartTime());
		verify(mockHttpClient, times(2)).execute(any());
	}

	@Test
	void testIsStreamerLive_StreamerIsOffline_ReturnsFalse() throws Exception {
		// Call 1: /users endpoint response
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		// Call 2: /streams endpoint response
		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		boolean live = spy.isStreamerLive();

		assertFalse(live);
		assertNull(spy.getStartTime());
	}

	@Test
	void testIsStreamerLive_MissingStartedAt_ReturnsFalse() throws Exception {
		// Call 1: /users
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		// Call 2: /streams with missing started_at
		JSONObject streamData = new JSONObject();
		streamData.put("user_id", String.valueOf(TEST_STREAMER_ID));
		streamData.put("user_login", TEST_STREAMER);

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray().put(streamData));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		boolean live = spy.isStreamerLive();

		assertFalse(live);
	}

	@Test
	void testIsStreamerLive_BlankStartedAt_ReturnsFalse() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);

		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		// started_at blank
		JSONObject streamData = new JSONObject();
		streamData.put("user_id", String.valueOf(TEST_STREAMER_ID));
		streamData.put("user_login", TEST_STREAMER);
		streamData.put("started_at", "");
		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray().put(streamData));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		boolean live = spy.isStreamerLive();

		assertFalse(live);
	}

	@Test
	void testIsStreamerLive_ErrorResponse_ReturnsFalse() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("error", "Service Unavailable");
		streamsResponse.put("message", "Service temporarily unavailable");

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		boolean live = spy.isStreamerLive();

		assertFalse(live);
	}

	@Test
	void testIsStreamerLive_MultipleStreamersInResponse_FindsCorrectOne() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject otherStreamer = new JSONObject();
		otherStreamer.put("user_id", "99999");
		otherStreamer.put("user_login", "other_streamer");
		otherStreamer.put("started_at", "2024-12-09T09:00:00Z");

		JSONObject ourStreamer = new JSONObject();
		ourStreamer.put("user_id", String.valueOf(TEST_STREAMER_ID));
		ourStreamer.put("user_login", TEST_STREAMER);
		ourStreamer.put("started_at", STARTED_AT);

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray().put(otherStreamer).put(ourStreamer));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		boolean live = spy.isStreamerLive();

		assertTrue(live);
		assertEquals(STARTED_AT, spy.getStartTime());
	}

	@Test
	void testIsStreamerLive_OtherStreamerLiveButNotOurs_ReturnsFalse() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject otherStreamer = new JSONObject();
		otherStreamer.put("user_id", "99999");
		otherStreamer.put("user_login", "other_streamer");
		otherStreamer.put("started_at", "2024-12-09T09:00:00Z");

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray().put(otherStreamer));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		boolean live = spy.isStreamerLive();

		assertFalse(live);
	}

	/** Observer Pattern Tests **/

	@Test
	void testAddObserver_StreamAlreadyLive_NotifiesImmediately() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamData = new JSONObject();
		streamData.put("user_id", String.valueOf(TEST_STREAMER_ID));
		streamData.put("user_login", TEST_STREAMER);
		streamData.put("started_at", STARTED_AT);
		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray().put(streamData));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		spy.isStreamerLive();

		StreamObserver mockObserver = mock(StreamObserver.class);
		spy.addObserver(mockObserver);

		verify(mockObserver).onStreamStart(STARTED_AT);
	}

	@Test
	void testAddObserver_StreamOffline_DoesNotNotify() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		spy.isStreamerLive();

		StreamObserver mockObserver = mock(StreamObserver.class);
		spy.addObserver(mockObserver);

		verify(mockObserver, never()).onStreamStart(anyString());
	}

	@Test
	void testRemoveObserver_NoLongerReceivesNotifications() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		StreamObserver mockObserver = mock(StreamObserver.class);
		spy.addObserver(mockObserver);
		spy.removeObserver(mockObserver);

		String onlineMessage = createStreamOnlineMessage(STARTED_AT);
		spy.onMessage(onlineMessage);

		verify(mockObserver, never()).onStreamStart(anyString());
	}

	@Test
	void testMultipleObservers_AllNotified() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		StreamObserver observer1 = mock(StreamObserver.class);
		StreamObserver observer2 = mock(StreamObserver.class);
		StreamObserver observer3 = mock(StreamObserver.class);

		spy.addObserver(observer1);
		spy.addObserver(observer2);
		spy.addObserver(observer3);

		String onlineMessage = createStreamOnlineMessage(STARTED_AT);
		spy.onMessage(onlineMessage);

		verify(observer1).onStreamStart(STARTED_AT);
		verify(observer2).onStreamStart(STARTED_AT);
		verify(observer3).onStreamStart(STARTED_AT);
	}

	/** onMessage Tests **/

	@Test
	void testOnMessage_SessionWelcome_ParsesSessionId() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject subscriptionData = new JSONObject();
		subscriptionData.put("type", "stream.online");
		subscriptionData.put("status", "enabled");
		JSONObject subscriptionResponse = new JSONObject();
		subscriptionResponse.put("data", new JSONArray().put(subscriptionData));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(subscriptionResponse.toString()))
				.thenReturn(new StringEntity(subscriptionResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		String welcomeMessage = createWelcomeMessage("test-session-123");
		spy.onMessage(welcomeMessage);

		verify(mockHttpClient, atLeast(3)).execute(any());
	}

	@Test
	void testOnMessage_StreamOnline_NotifiesObservers() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		StreamObserver mockObserver = mock(StreamObserver.class);
		spy.addObserver(mockObserver);

		String onlineMessage = createStreamOnlineMessage(STARTED_AT);
		spy.onMessage(onlineMessage);

		verify(mockObserver).onStreamStart(STARTED_AT);
		assertEquals(STARTED_AT, spy.getStartTime());
	}

	@Test
	void testOnMessage_StreamOffline_NotifiesObserversAndClearsStartTime() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		StreamObserver mockObserver = mock(StreamObserver.class);
		spy.addObserver(mockObserver);

		String onlineMessage = createStreamOnlineMessage(STARTED_AT);
		spy.onMessage(onlineMessage);
		assertEquals(STARTED_AT, spy.getStartTime());

		String offlineMessage = createStreamOfflineMessage();
		spy.onMessage(offlineMessage);

		verify(mockObserver).onStreamEnd();
		assertNull(spy.getStartTime());
	}

	@Test
	void testOnMessage_SessionKeepalive_DoesNothing() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		StreamObserver mockObserver = mock(StreamObserver.class);
		spy.addObserver(mockObserver);

		String keepaliveMessage = createKeepaliveMessage();

		assertDoesNotThrow(() -> spy.onMessage(keepaliveMessage));
		verify(mockObserver, never()).onStreamStart(anyString());
		verify(mockObserver, never()).onStreamEnd();
	}

	@Test
	void testOnMessage_SessionReconnect_HandlesReconnect() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);

		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		String reconnectMessage = createReconnectMessage("wss://eventsub.wss.twitch.tv/ws?session=new123");

		assertDoesNotThrow(() -> spy.onMessage(reconnectMessage));
	}

	@Test
	void testOnMessage_InvalidJson_HandlesGracefully() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		assertDoesNotThrow(() -> spy.onMessage("not valid json {{{"));
	}

	@Test
	void testOnMessage_EmptyMetadata_DoesNothing() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		JSONObject message = new JSONObject();
		message.put("metadata", new JSONObject());

		assertDoesNotThrow(() -> spy.onMessage(message.toString()));
	}

	/** getStartTime Tests **/

	@Test
	void testGetStartTime_InitiallyNull() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		assertNull(spy.getStartTime());
	}

	@Test
	void testGetStartTime_SetAfterStreamGoesLive() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamData = new JSONObject();
		streamData.put("user_id", String.valueOf(TEST_STREAMER_ID));
		streamData.put("user_login", TEST_STREAMER);
		streamData.put("started_at", STARTED_AT);
		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray().put(streamData));

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		spy.isStreamerLive();

		assertEquals(STARTED_AT, spy.getStartTime());
	}

	@Test
	void testGetStartTime_ClearedAfterStreamEnds() throws Exception {
		JSONObject user = new JSONObject();
		user.put("id", String.valueOf(TEST_STREAMER_ID));
		user.put("login", TEST_STREAMER);
		JSONObject usersResponse = new JSONObject();
		usersResponse.put("data", new JSONArray().put(user));

		JSONObject streamsResponse = new JSONObject();
		streamsResponse.put("data", new JSONArray());

		when(mockResponse.getEntity()).thenReturn(new StringEntity(usersResponse.toString()))
				.thenReturn(new StringEntity(streamsResponse.toString()));
		when(mockHttpClient.execute(any())).thenReturn(mockResponse);

		TwitchAPI twitch = new TwitchAPI("http://localhost", TEST_STREAMER, mockClientInfo);
		TwitchAPI spy = spy(twitch);
		doReturn(TEST_BEARER_TOKEN).when(spy).getBearerToken();

		String onlineMessage = createStreamOnlineMessage(STARTED_AT);
		spy.onMessage(onlineMessage);
		assertEquals(STARTED_AT, spy.getStartTime());

		String offlineMessage = createStreamOfflineMessage();
		spy.onMessage(offlineMessage);
		assertNull(spy.getStartTime());
	}

	/** Helper Methods **/

	private String createWelcomeMessage(String sessionId) {
		JSONObject session = new JSONObject();
		session.put("id", sessionId);
		session.put("status", "connected");
		session.put("connected_at", "2024-12-09T10:00:00Z");

		JSONObject payload = new JSONObject();
		payload.put("session", session);

		JSONObject metadata = new JSONObject();
		metadata.put("message_type", "session_welcome");
		metadata.put("message_id", "test-message-id");
		metadata.put("message_timestamp", "2024-12-09T10:00:00Z");

		JSONObject message = new JSONObject();
		message.put("metadata", metadata);
		message.put("payload", payload);

		return message.toString();
	}

	private String createStreamOnlineMessage(String startedAt) {
		JSONObject event = new JSONObject();
		event.put("broadcaster_user_id", String.valueOf(TEST_STREAMER_ID));
		event.put("broadcaster_user_login", TEST_STREAMER);
		event.put("type", "live");
		event.put("started_at", startedAt);

		JSONObject subscription = new JSONObject();
		subscription.put("type", "stream.online");
		subscription.put("created_at", startedAt);

		JSONObject payload = new JSONObject();
		payload.put("subscription", subscription);
		payload.put("event", event);

		JSONObject metadata = new JSONObject();
		metadata.put("message_type", "notification");

		JSONObject message = new JSONObject();
		message.put("metadata", metadata);
		message.put("payload", payload);

		return message.toString();
	}

	private String createStreamOfflineMessage() {
		JSONObject subscription = new JSONObject();
		subscription.put("type", "stream.offline");
		subscription.put("created_at", "2024-12-09T15:00:00Z");

		JSONObject payload = new JSONObject();
		payload.put("subscription", subscription);

		JSONObject metadata = new JSONObject();
		metadata.put("message_type", "notification");

		JSONObject message = new JSONObject();
		message.put("metadata", metadata);
		message.put("payload", payload);

		return message.toString();
	}

	private String createKeepaliveMessage() {
		JSONObject metadata = new JSONObject();
		metadata.put("message_type", "session_keepalive");

		JSONObject message = new JSONObject();
		message.put("metadata", metadata);

		return message.toString();
	}

	private String createReconnectMessage(String reconnectUrl) {
		JSONObject session = new JSONObject();
		session.put("reconnect_url", reconnectUrl);

		JSONObject payload = new JSONObject();
		payload.put("session", session);

		JSONObject metadata = new JSONObject();
		metadata.put("message_type", "session_reconnect");

		JSONObject message = new JSONObject();
		message.put("metadata", metadata);
		message.put("payload", payload);

		return message.toString();
	}
}
