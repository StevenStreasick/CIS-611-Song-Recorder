package songs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class WebClientTest {
	
    private URI serverUri;
    private String tokenFileName;

    @BeforeEach
    void setUp() throws Exception {
        serverUri = new URI("ws://localhost:8080");
        tokenFileName = "test_tokens.json";
    }
    @Test
    void constructor() {
    	WebClient webClient = new WebClient(serverUri, tokenFileName);
    }
    
    @Test
    void bearerTokens() {
    	
    	WebClient webClient = new WebClient(serverUri, tokenFileName);
    	
    	String bearerToken = webClient.getBearerToken();
    	assertNotNull(bearerToken);
    	assertFalse(bearerToken.isEmpty());
	
    }
    
    @Test
    void extendsFileWriter() {    	
        assertTrue(FileWriter.class.isAssignableFrom(WebClient.class));	
    }
}
