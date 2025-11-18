package songs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class TokenManagerTest {
//	TODO: Add a test for File Writer extends...
	String fileName = "\tokens.txt";
	
	String clientId = System.getenv("TWITCH_CLIENT_ID");
	String clientSecret = System.getenv("TWITCH_CLIENT_SECRET");

	ClientInfo blankClientInfo = new ClientInfo();
	ClientInfo clientInfo = new ClientInfo(clientId, clientSecret);

	static ClientInfo testingInfo = new ClientInfo(System.getenv("TWITCH_CLIENT_ID"), System.getenv("TWITCH_CLIENT_SECRET"));
	
	@BeforeEach
	void removeTokenFile() {
		File file = new File(fileName);

        if (file.exists()) {
        	file.delete();
        }
	}
	
	@Test 
	void environmentVariables() {
		assertNotNull(clientId);
		assertNotNull(clientSecret);
		assertFalse(clientId.isBlank());
		assertFalse(clientSecret.isBlank());
	}
	
    @Test
    void Constructor1() {
    	
        TokenManager manager = new TokenManager(fileName, clientInfo);
        
        String token = manager.getBearerToken();

        assertNotNull(token);
        assertFalse(token.isBlank());
        
    }
    
    @Test
    void writeToFile() {
        TokenManager manager = new TokenManager(fileName, clientInfo);
        
        String token = manager.getBearerToken();
        
        try (Scanner scanner = new Scanner(new FileReader(fileName))) {
    		String accessToken = scanner.nextLine();
    		
    		assertEquals(accessToken, token);
    		assertTrue(scanner.hasNextLine());
    		
    		String expiresIn = scanner.nextLine();
    		
    		assertNotNull(expiresIn);
    		assertFalse(expiresIn.isBlank());
        } catch(IOException e) {
        	
        }

        assertNotNull(manager.getBearerToken());
        assertFalse(token.isBlank());    
    }
    
    @Test
    void readToFile() {
    	String fileName = "\tokens.txt";
    	try {
	        TokenManager manager = new TokenManager(fileName, clientInfo);

    		String accessToken = manager.getBearerToken();
    		String expiresIn = "2000550985";
    		
    		java.io.FileWriter writer = new java.io.FileWriter(fileName);
    		
    		writer.write(accessToken + "\n" + expiresIn);
    		writer.close();
    		
	        manager = new TokenManager(fileName, clientInfo);
	        
	        assertEquals(accessToken, manager.getBearerToken());
        
        } catch(IOException e) {
        	assertTrue(false);
        }
    }
    
    @Test
    void multipleTokenManagers() {
        TokenManager manager1 = new TokenManager("\tokens.txt", clientInfo);
        String token1 = manager1.getBearerToken();

        TokenManager manager2 = new TokenManager("\tokens.txt", clientInfo);
        String token2 = manager2.getBearerToken();
        
        assertNotNull(manager1.getBearerToken());
        assertFalse(token1.isBlank());
        
        assertEquals(token1, token2);
    }
    
    @Test
    void Constructor2_validtoken() {
    	TokenManager manager1 = new TokenManager("\tokens.txt", clientInfo);
    	
    	String token = manager1.getBearerToken();
        TokenManager manager = new TokenManager("\tokens.txt", clientInfo, token, (long) 2000550985);
        
        assertEquals(token, manager.getBearerToken());
    }
    
    @Test
    void Constructor2_expiredtoken() {
    	String token = "Hello World!";
        TokenManager manager = new TokenManager("\tokens.txt", clientInfo, token, (long) 1000);
        
        String newToken = manager.getBearerToken();
        assertNotEquals(token, newToken);
        assertNotNull(newToken);
    }
}
