package songs;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import io.socket.client.Socket;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StreamerSonglistAPITest {
	// Mocking crap
	@Mock
	private CloseableHttpClient mockHttpClient;

	@Mock
	private CloseableHttpResponse mockResponse;

	@Mock
	private StatusLine mockStatusLine;

	@Mock
	private HttpEntity mockEntity;

	@Mock
	private Socket mockSocket;

	private MockedStatic<HttpClientSingleton> mockedSingleton;

	private StreamerSonglistAPI api;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		mockedSingleton = mockStatic(HttpClientSingleton.class);
		mockedSingleton.when(HttpClientSingleton::getInstance).thenReturn(mockHttpClient);

	}

	/** Setup **/
	@AfterEach
	void tearDown() {
		if (api != null) {
			api.stopListening();
		}

		if (mockedSingleton != null) {
			mockedSingleton.close();
		}
	}

	private void setupSuccessfulHttpResponse(String jsonResponse) throws Exception {
		when(mockHttpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);

		when(mockEntity.getContent())
				.thenAnswer(invocation -> new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8)));
	}

	private void setup400HttpResponse() throws Exception {
		when(mockHttpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(400);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
	}

	private void setup500HttpResponse() throws Exception {
		when(mockHttpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(500);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
	}

	private void setupIOExceptionResponse() throws Exception {
		when(mockHttpClient.execute(any(HttpGet.class))).thenThrow(new IOException("Network error"));
	}

	@Test
	void testConstructorWithValidName() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");

		assertDoesNotThrow(() -> {
			api = new StreamerSonglistAPI("validStreamer");
		});
	}

	@Test
	void testConstructorWithNullName() {
		assertThrows(IllegalArgumentException.class, () -> {
			api = new StreamerSonglistAPI(null);
		}, "The provided broadcaster is not a valid twitch user");
	}

	@Test
	void testConstructorWithBlankName() {
		assertThrows(IllegalArgumentException.class, () -> {
			api = new StreamerSonglistAPI("   ");
		}, "The provided broadcaster is not a valid twitch user");
	}

	@Test
	void testConstructorWithEmptyName() {
		assertThrows(IllegalArgumentException.class, () -> {
			api = new StreamerSonglistAPI("");
		}, "The provided broadcaster is not a valid twitch user");
	}

	@Test
	void testSetBroadcasterIDSuccess() throws Exception {
		String jsonResponse = "{\"id\": 12345}";
		setupSuccessfulHttpResponse(jsonResponse);

		api = new StreamerSonglistAPI("testStreamer");

		Field broadcasterIDField = StreamerSonglistAPI.class.getDeclaredField("broadcasterID");
		broadcasterIDField.setAccessible(true);
		int broadcasterID = (int) broadcasterIDField.get(api);

		assertEquals(12345, broadcasterID);
	}

	@Test
	void testSetBroadcasterIDNotFound() throws Exception {
		setup400HttpResponse();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			api = new StreamerSonglistAPI("nonexistentStreamer");
		});

		assertEquals("The provided broadcaster is not found in streamersonglist", exception.getMessage());
	}

	@Test
	void testSetBroadcasterIDServerError() throws Exception {
		setup500HttpResponse();

		api = new StreamerSonglistAPI("testStreamer");

		Field broadcasterIDField = StreamerSonglistAPI.class.getDeclaredField("broadcasterID");
		broadcasterIDField.setAccessible(true);
		int broadcasterID = (int) broadcasterIDField.get(api);

		assertEquals(-1, broadcasterID);
	}

	@Test
	void testSetStartTimeValid() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		String validTime = "2024-01-15T10:30:00Z";
		boolean result = api.setStartTime(validTime);

		assertTrue(result);

		Field startTimeField = StreamerSonglistAPI.class.getDeclaredField("startTime");
		startTimeField.setAccessible(true);
		Instant startTime = (Instant) startTimeField.get(api);

		assertNotNull(startTime);
		assertEquals(ZonedDateTime.parse(validTime).toInstant(), startTime);
	}

	@Test
	void testSetStartTimeWithTimezone() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		String validTime = "2024-01-15T10:30:00-05:00";
		boolean result = api.setStartTime(validTime);

		assertTrue(result);

		Field startTimeField = StreamerSonglistAPI.class.getDeclaredField("startTime");
		startTimeField.setAccessible(true);
		Instant startTime = (Instant) startTimeField.get(api);

		assertNotNull(startTime);
	}

	@Test

	void testSetStartTimeInvalid() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		String invalidTime = "invalid-timestamp";
		boolean result = api.setStartTime(invalidTime);

		assertFalse(result);
	}

	@Test
	void testSetStartTimeNull() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		boolean result = api.setStartTime(null);

		assertFalse(result);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testClearSonglist() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		boolean result = api.clearSonglist();

		assertTrue(result);

		Field songlistField = StreamerSonglistAPI.class.getDeclaredField("songlist");
		songlistField.setAccessible(true);

		LinkedHashMap<String, Duration> songlist = (LinkedHashMap<String, Duration>) songlistField.get(api);

		assertTrue(songlist.isEmpty());
	}

	@Test
	void testWriteSonglistToFileWithoutStartTime() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		Exception exception = assertThrows(Exception.class, () -> {
			api.writeSonglistToFile();
		});

		assertTrue(exception.getMessage().contains("Please initialize startTime"));
	}

	@Test
	void testConvertStringToInstant() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		Method convertMethod = StreamerSonglistAPI.class.getDeclaredMethod("convertStringToInstant", String.class);
		convertMethod.setAccessible(true);

		String timeString = "2024-01-15T10:30:00Z";
		Instant result = (Instant) convertMethod.invoke(api, timeString);

		assertNotNull(result);
		assertEquals(ZonedDateTime.parse(timeString).toInstant(), result);
	}

	@Test
	void testStopListeningWithNullSocket() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("testStreamer");

		assertDoesNotThrow(() -> {
			api.stopListening();
		});
	}

	@Test
	void testConstructorConvertsToLowercase() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");
		api = new StreamerSonglistAPI("TestStreamer");

		Field broadcasterField = StreamerSonglistAPI.class.getDeclaredField("broadcaster");
		broadcasterField.setAccessible(true);
		String broadcaster = (String) broadcasterField.get(api);

		assertEquals("teststreamer", broadcaster);
	}

	@Test
	void testConstructorMixedCase() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 67890}");
		api = new StreamerSonglistAPI("TeSt_StReAmEr_123");

		Field broadcasterField = StreamerSonglistAPI.class.getDeclaredField("broadcaster");
		broadcasterField.setAccessible(true);
		String broadcaster = (String) broadcasterField.get(api);

		assertEquals("test_streamer_123", broadcaster);
	}

	@Test
	void testSetBroadcasterIDIOException() throws Exception {
		setupIOExceptionResponse();

		api = new StreamerSonglistAPI("testStreamer");

		Field broadcasterIDField = StreamerSonglistAPI.class.getDeclaredField("broadcasterID");
		broadcasterIDField.setAccessible(true);
		int broadcasterID = (int) broadcasterIDField.get(api);

		assertEquals(-1, broadcasterID);
	}

	@Test
	void testHttpClientCalledWithCorrectURL() throws Exception {
		setupSuccessfulHttpResponse("{\"id\": 12345}");

		api = new StreamerSonglistAPI("testStreamer");

		verify(mockHttpClient, atLeastOnce()).execute(any(HttpGet.class));
	}
}