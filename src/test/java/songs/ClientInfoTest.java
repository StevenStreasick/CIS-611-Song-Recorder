package songs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClientInfoTest {

    @Test
    void blankConstructor() {
    	ClientInfo clientInfo = new ClientInfo();
    	
        assertEquals(clientInfo.getClientID(), null);
        assertEquals(clientInfo.getClientSecret(), null);
    }
    
    @Test
    void FilledConstructor() {
    	String testID = "";
    	String testSecret = "";

    	ClientInfo clientInfo = new ClientInfo(testID, testSecret);
    	
        assertEquals(clientInfo.getClientID(), testID);
        assertEquals(clientInfo.getClientSecret(), testSecret);
    }
}
