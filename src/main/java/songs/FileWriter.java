package songs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

abstract class FileWriter {
	Path filePath;

	public FileWriter(String fileName) {
		filePath = Path.of(fileName);
	}
	
	public boolean writeToFile(String data) {
		try {
			Files.writeString(filePath, data, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			return true;
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
			return false;
		}
	}
	
	public String readFromFile() {
		try {
			List<String> lines = Files.readAllLines(filePath);
			return lines.isEmpty() ? "" : lines.get(0); // Assuming token is on the first line
		} catch (IOException e) {
			System.err.println("Error reading from file: " + e.getMessage());
			return null;
		}
	}



}
