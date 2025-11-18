package songs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class StreamerSonglistAPITest {
	
    @Test
    void validStreamerConstructor() {
		assertDoesNotThrow(() -> {
			StreamerSonglistAPI api = new StreamerSonglistAPI("a_couple_streams");
		});    	
    }
    
    @Test
    void nullStreamerConstructor() {
		assertThrows(IllegalArgumentException.class, () -> {
			StreamerSonglistAPI api = new StreamerSonglistAPI(null);
		});    	
    }
    
    @Test
    void emptyStreamerConstructor() {
		assertThrows(IllegalArgumentException.class, () -> {
			StreamerSonglistAPI api = new StreamerSonglistAPI("");
		});    	
    }
    @Test
    void blankStreamerConstructor() {
		assertThrows(IllegalArgumentException.class, () -> {
			StreamerSonglistAPI api = new StreamerSonglistAPI(" ");
		});    	
    }



}
