package songs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

abstract class FileWriter {
	Path filePath;

	public FileWriter() {
	}
	
	public FileWriter(String fileName) {
		this.setPath(fileName);
	}
	
	public FileWriter(Path filePath) {
		this.setPath(filePath);
	}
	
	public boolean writeToFile(String data) {
		if(filePath == null) {
			return false;
		}
		
		try {
			Files.writeString(filePath, data, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			return true;
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getStackTrace());
			return false;
		}
	}
	// TODO: Improve this class by returning a list<String>	
	public List<String> readFromFile() {
		if(filePath == null) {
			return null;
		}
		try {
			List<String> lines = Files.readAllLines(filePath);
			return lines; 
		} catch (IOException e) {
			System.err.println("Error reading from file: " + e.getStackTrace());
			return null;
		}
	}
	
	public boolean setPath(Path filePath) {
		this.filePath = filePath;
		return true;
	}
	
	public boolean setPath(String fileName) {
		this.filePath = Path.of(fileName);
		return true;
	}



}
