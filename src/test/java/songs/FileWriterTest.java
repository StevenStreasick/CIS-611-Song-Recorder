package songs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileWriterTest {
	String fileName = "\tokens.txt";
	
	@AfterEach
	void removeTokenFile() {
		File file = new File(fileName);

        if (file.exists()) {
        	file.delete();
        }
	}
	
	@Test 
	void StringConstructor() {
		assertDoesNotThrow(() -> {
			ConcreteFileWriter writer = new ConcreteFileWriter(fileName); 
		});
	}
	@Test 
	void PathConstructor() {
		assertDoesNotThrow(() -> {
			Path filePath = Path.of(fileName);
			ConcreteFileWriter writer = new ConcreteFileWriter(filePath); 
		});
	}
	
    @Test
    void writeToFile() {
    	
    	Path filePath = Path.of(fileName);
    	
    	assertDoesNotThrow(() -> {
    		ConcreteFileWriter writer = new ConcreteFileWriter(fileName); 
	        
	        assertTrue(writer.writeToFile("Hello World!"));
    	});
    	
    	try {
            //Read from file
    		List<String> lines = Files.readAllLines(filePath);
    		assertFalse(lines.isEmpty());
    		assertEquals(1, lines.size());
    		assertEquals("Hello World!", lines.get(0));
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    @Test
    void readFromFile() {
    	try {	    	   
	    	String writeString = "Do you wanna have a bad time?";
	    	
	    	FileWriter writer = new FileWriter(fileName);
	    	writer.write(writeString);
	    	writer.close();
	    	
	    	assertDoesNotThrow(() -> {
	    		ConcreteFileWriter myWriter = new ConcreteFileWriter(fileName); 
	    		List<String> result = myWriter.readFromFile();
	    		
	    		assertEquals(1, result.size());
	    		assertEquals(writeString, result.get(0));
	    	});
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    //TODO: Write a multi line test
    @Test
    void multilineReadFromFile() {
    	try {
	    	
	    	List<String> writeList = new ArrayList<String>();
	    	
	    	writeList.add("On days like these,");
	    	writeList.add("kids like you...");
	    	writeList.add("SHOULD BE BURNING IN HELL!");
	    	
	    	String writeString = "";
	    	for(int i = 0; i <writeList.size(); i++) {
	    		if(i != 0) {
	    			writeString += "\n";
	    		}
	    		
	    		writeString += writeList.get(i);
	    	}
	    	   
	    	
	    	FileWriter writer = new FileWriter(fileName);
	    	writer.write(writeString);
	    	writer.close();
	    	
	    	assertDoesNotThrow(() -> {
	    		ConcreteFileWriter myWriter = new ConcreteFileWriter(fileName); 
	    		List<String> result = myWriter.readFromFile();
	    		
	    		assertEquals(writeList.size(), result.size());
	    		for(int i = 0; i < result.size(); i++) {
	    			assertEquals(writeList.get(i), result.get(i));
	    		}
	    	});
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
}
