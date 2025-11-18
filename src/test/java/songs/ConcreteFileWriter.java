package songs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class ConcreteFileWriter extends FileWriter{
	public ConcreteFileWriter(Path filePath) {
		super(filePath);
	}
	
	public ConcreteFileWriter(String fileName) {
		super(fileName);
	}
	
	public boolean writeToFile(String data) {
		return super.writeToFile(data);
	}
	public List<String> readFromFile() {
		return super.readFromFile();
	}
    
}
