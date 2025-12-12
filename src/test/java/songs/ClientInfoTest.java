package songs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClientInfoTest {

	@Test
	void blankConstructor() {
		ClientInfo clientInfo = new ClientInfo();

		assertNull(clientInfo.getClientID());
		assertNull(clientInfo.getClientSecret());
	}

	@Test
	void filledConstructor() {
		String testID = "test-client-id";
		String testSecret = "test-client-secret";
		ClientInfo clientInfo = new ClientInfo(testID, testSecret);

		assertEquals(testID, clientInfo.getClientID());
		assertEquals(testSecret, clientInfo.getClientSecret());
	}

	@Test
	void setClientIdWithValidValue() {
		ClientInfo clientInfo = new ClientInfo();
		String validId = "valid-client-id";

		boolean result = clientInfo.setClientId(validId);

		assertTrue(result);
		assertEquals(validId, clientInfo.getClientID());
	}

	@Test
	void setClientIdWithBlankValue() {
		ClientInfo clientInfo = new ClientInfo("initial-id", "initial-secret");
		String blankId = "";

		boolean result = clientInfo.setClientId(blankId);

		assertFalse(result);
		assertEquals("initial-id", clientInfo.getClientID());
	}

	@Test
	void setClientIdWithWhitespaceOnly() {
		ClientInfo clientInfo = new ClientInfo("initial-id", "initial-secret");
		String whitespaceId = "   ";

		boolean result = clientInfo.setClientId(whitespaceId);

		assertFalse(result);
		assertEquals("initial-id", clientInfo.getClientID());
	}

	@Test
	void setClientSecretWithValidValue() {
		ClientInfo clientInfo = new ClientInfo();
		String validSecret = "valid-client-secret";

		boolean result = clientInfo.setClientSecret(validSecret);

		assertTrue(result);
		assertEquals(validSecret, clientInfo.getClientSecret());
	}

	@Test
	void setClientSecretWithBlankValue() {
		ClientInfo clientInfo = new ClientInfo("initial-id", "initial-secret");
		String blankSecret = "";

		boolean result = clientInfo.setClientSecret(blankSecret);

		assertFalse(result);
		assertEquals("initial-secret", clientInfo.getClientSecret());
	}

	@Test
	void setClientSecretWithWhitespaceOnly() {
		ClientInfo clientInfo = new ClientInfo("initial-id", "initial-secret");
		String whitespaceSecret = "   ";

		boolean result = clientInfo.setClientSecret(whitespaceSecret);

		assertFalse(result);
		assertEquals("initial-secret", clientInfo.getClientSecret());
	}

	@Test
	void multipleSetsOfClientId() {
		ClientInfo clientInfo = new ClientInfo();

		assertTrue(clientInfo.setClientId("first-id"));
		assertEquals("first-id", clientInfo.getClientID());

		assertTrue(clientInfo.setClientId("second-id"));
		assertEquals("second-id", clientInfo.getClientID());
	}

	@Test
	void multipleSetsOfClientSecret() {
		ClientInfo clientInfo = new ClientInfo();

		assertTrue(clientInfo.setClientSecret("first-secret"));
		assertEquals("first-secret", clientInfo.getClientSecret());

		assertTrue(clientInfo.setClientSecret("second-secret"));
		assertEquals("second-secret", clientInfo.getClientSecret());
	}

	@Test
	void constructorWithEmptyStrings() {
		ClientInfo clientInfo = new ClientInfo("", "");

		assertEquals("", clientInfo.getClientID());
		assertEquals("", clientInfo.getClientSecret());
	}

	@Test
	void constructorWithNullValues() {
		ClientInfo clientInfo = new ClientInfo(null, null);

		assertNull(clientInfo.getClientID());
		assertNull(clientInfo.getClientSecret());
	}
}