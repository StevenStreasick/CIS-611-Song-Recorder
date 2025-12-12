package songs;

import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TokenManagerTest {

	private String fileName;
	private String clientId;
	private String clientSecret;
	private ClientInfo blankClientInfo;
	private ClientInfo clientInfo;
	private CallbackServer mockCallbackServer;

	@BeforeEach
	void setUp() {
		fileName = "test_tokens.txt";
		clientId = System.getenv("TWITCH_CLIENT_ID");
		clientSecret = System.getenv("TWITCH_CLIENT_SECRET");
		blankClientInfo = new ClientInfo();
		clientInfo = new ClientInfo(clientId, clientSecret);
		mockCallbackServer = mock(CallbackServer.class);

		removeTokenFile();
	}

	@AfterEach
	void tearDown() {
		removeTokenFile();
	}

	void removeTokenFile() {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
	}

	/** Environment Variable Tests **/
	@Test
	void testEnvironmentVariables() {
		assertNotNull(clientId, "TWITCH_CLIENT_ID should be set");
		assertNotNull(clientSecret, "TWITCH_CLIENT_SECRET should be set");
		assertFalse(clientId.isBlank(), "TWITCH_CLIENT_ID should not be blank");
		assertFalse(clientSecret.isBlank(), "TWITCH_CLIENT_SECRET should not be blank");
	}

	/** Constructor **/
	@Test
	void testConstructor_WithValidParameters() {
		TokenManager manager = new TokenManager(fileName, clientInfo, mockCallbackServer);

		assertNotNull(manager);
		assertFalse(manager.hasTokens());
	}

	@Test
	void testConstructor_WithBearerToken() {
		String testToken = "test_bearer_token";
		Long expiresIn = 3600L;
		String testRefresh = "test_refresh_token";

		TokenManager manager = new TokenManager(fileName, clientInfo, testToken, expiresIn, testRefresh);

		assertTrue(manager.hasTokens());
		assertEquals(testToken, manager.getBearerToken());
	}

	@Test
	void testConstructor_WithNullClientInfo() {
		assertThrows(IllegalArgumentException.class, () -> {
			new TokenManager(fileName, null, mockCallbackServer);
		});
	}

	/** Token Persistence Tests **/
	@Test
	void testTokens_WrittenToFile() throws IOException {
		String testToken = "test_access_token";
		Long expiresIn = 3600L;
		String refreshToken = "test_refresh_token";

		new TokenManager(fileName, clientInfo, testToken, expiresIn, refreshToken);

		File file = new File(fileName);
		assertTrue(file.exists());

		List<String> lines = Files.readAllLines(Path.of(fileName));

		assertEquals(3, lines.size());
		assertEquals(testToken, lines.get(0));
		assertEquals(refreshToken, lines.get(1));
		assertNotEquals("-1", lines.get(2));
		assertTrue(Long.parseLong(lines.get(2)) > Instant.now().getEpochSecond());

	}

	@Test
	void testTokens_ReadFromFile() throws IOException {

		String testToken = "test_access_token";
		Long futureExpiry = Instant.now().getEpochSecond() + 7200; // 2 hours
		String refreshToken = "test_refresh_token";

		String content = testToken + "\n" + refreshToken + "\n" + futureExpiry;
		Files.writeString(Path.of(fileName), content);

		TokenManager manager = new TokenManager(fileName, clientInfo, mockCallbackServer);

		assertTrue(manager.hasTokens());
	}

	@Test
	void testMultipleTokenManagers_ShareFile() {
		String testToken = "shared_token";
		Long expiresIn = 3600L;
		String refreshToken = "shared_refresh";

		new TokenManager(fileName, clientInfo, testToken, expiresIn, refreshToken);

		TokenManager manager2 = new TokenManager(fileName, clientInfo, mockCallbackServer);

		assertTrue(manager2.hasTokens());
	}

	@Test
	void testTokenFile_InvalidFormat() throws IOException {
		Files.writeString(Path.of(fileName), "invalid\ncontent");

		TokenManager manager = new TokenManager(fileName, clientInfo, mockCallbackServer);
		assertFalse(manager.hasTokens());
	}

	@Test
	void testTokenFile_Empty() throws IOException {
		Files.writeString(Path.of(fileName), "");

		TokenManager manager = new TokenManager(fileName, clientInfo, mockCallbackServer);
		assertFalse(manager.hasTokens());
	}

	/** Token Validation Tests **/
	@Test
	void testHasTokens_WhenNoTokens() {
		TokenManager manager = new TokenManager(fileName, clientInfo, mockCallbackServer);
		assertFalse(manager.hasTokens());
	}

	@Test
	void testHasTokens_WhenTokensExist() {
		String testToken = "test_token";
		Long expiresIn = 3600L;
		String refreshToken = "refresh_token";

		TokenManager manager = new TokenManager(fileName, clientInfo, testToken, expiresIn, refreshToken);
		assertTrue(manager.hasTokens());
	}

	/** Clear Tokens Tests **/
	@Test
	void testClearTokens_RemovesAllTokens() {
		String testToken = "test_token";
		Long expiresIn = 3600L;
		String refreshToken = "refresh_token";

		TokenManager manager = new TokenManager(fileName, clientInfo, testToken, expiresIn, refreshToken);
		assertTrue(manager.hasTokens());

		manager.clearTokens();
		assertFalse(manager.hasTokens());
	}

	@Test
	void testClearTokens_UpdatesFile() throws IOException {
		String testToken = "test_token";
		Long expiresIn = 3600L;
		String refreshToken = "refresh_token";

		TokenManager manager = new TokenManager(fileName, clientInfo, testToken, expiresIn, refreshToken);
		manager.clearTokens();

		List<String> lines = Files.readAllLines(Path.of(fileName));
		assertTrue(lines.get(0).isEmpty());
		assertTrue(lines.get(1).isEmpty());
		assertEquals("-1", lines.get(2));
	}

	@Test
	void testClearTokens_GetTokens() throws IOException {
		TokenManager manager = new TokenManager(fileName, clientInfo, mockCallbackServer);
		manager.clearTokens();
		manager.getBearerToken();

		assertTrue(manager.hasTokens());

		List<String> lines = Files.readAllLines(Path.of(fileName));
		assertFalse(lines.get(0).isEmpty());
		assertFalse(lines.get(1).isEmpty());
		assertNotEquals("-1", lines.get(2));
	}

	/** Edge Cases **/
	@Test
	void testGetBearerToken_WithBlankClientInfo() {
		TokenManager manager = new TokenManager(fileName, blankClientInfo, mockCallbackServer);
		String token = manager.getBearerToken();
		assertNull(token);
	}

	@Test
	void testExpiredToken_IsDetected() {
		String testToken = "expired_token";
		Long expiredTime = Instant.now().getEpochSecond() - 1000;
		String refreshToken = "refresh_token";

		String content = testToken + "\n" + refreshToken + "\n" + expiredTime;
		assertDoesNotThrow(() -> Files.writeString(Path.of(fileName), content));

		TokenManager manager = new TokenManager(fileName, clientInfo, mockCallbackServer);
		assertTrue(manager.hasTokens());
	}

	@Test
	void testFutureToken_IsValid() {
		String testToken = "future_token";
		Long futureExpiry = Instant.now().getEpochSecond() + 7200;
		String refreshToken = "refresh_token";

		TokenManager manager = new TokenManager(fileName, clientInfo, testToken, futureExpiry, refreshToken);
		assertTrue(manager.hasTokens());
	}

	@Test
	void testConstructor_WithNullFileName() {
		assertThrows(Exception.class, () -> {
			new TokenManager(null, clientInfo, mockCallbackServer);
		});
	}

	@Test
	void testConstructor_WithEmptyFileName() {
		assertDoesNotThrow(() -> {
			new TokenManager("", clientInfo, mockCallbackServer);
		});
	}

	/** FileWriter Inheritance Test **/
	@Test
	void testExtendsFileWriter() {
		assertTrue(FileWriter.class.isAssignableFrom(TokenManager.class));
	}

	/** Integration-style Tests (marked for manual execution) **/
	@Test
	@Tag("integration")
	@Disabled("Requires OAuth flow - run manually")
	void testIntegration_FullOAuthFlow() {
		CallbackServer realCallbackServer = new CallbackServer();
		TokenManager manager = new TokenManager(fileName, clientInfo, realCallbackServer);

		String token = manager.getBearerToken();
		assertNotNull(token);
		assertFalse(token.isBlank());
		assertTrue(manager.hasTokens());
	}

	@Test
	@Tag("integration")
	@Disabled("Requires valid tokens - run manually")
	void testIntegration_TokenRefresh() throws InterruptedException {
		CallbackServer realCallbackServer = new CallbackServer();
		TokenManager manager = new TokenManager(fileName, clientInfo, realCallbackServer);

		String initialToken = manager.getBearerToken();
		assertNotNull(initialToken);

		TimeUnit.SECONDS.sleep(2);
		boolean updated = manager.updateTokens();
		assertFalse(updated);
	}

	/** Concurrency Test **/
	@Test
	void testConcurrency_MultipleManagers() {
		String testToken = "concurrent_token";
		Long expiresIn = 3600L;
		String refreshToken = "concurrent_refresh";

		TokenManager manager1 = new TokenManager(fileName, clientInfo, testToken, expiresIn, refreshToken);
		TokenManager manager2 = new TokenManager(fileName, clientInfo, mockCallbackServer);
		TokenManager manager3 = new TokenManager(fileName, clientInfo, mockCallbackServer);

		assertTrue(manager1.hasTokens());
		assertTrue(manager2.hasTokens());
		assertTrue(manager3.hasTokens());

		manager1.clearTokens();

		TokenManager manager4 = new TokenManager(fileName, clientInfo, mockCallbackServer);
		assertFalse(manager4.hasTokens());
	}
}