package songs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

public class FileWriterTest {
    
    @TempDir
    Path tempDir;
    
    String fileName;
    
    @BeforeEach
    void setup() {
        fileName = tempDir.resolve("tokens.txt").toString();
    }

    /** Constructor Tests **/
    @Test
    void defaultConstructor() {
        assertDoesNotThrow(() -> {
            ConcreteFileWriter writer = new ConcreteFileWriter();
            assertNull(writer.filePath);
        });
    }

    @Test
    void stringConstructor() {
        assertDoesNotThrow(() -> {
            ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
            assertNotNull(writer.filePath);
            assertEquals(fileName, writer.filePath.toString());
        });
    }
    
    @Test
    void pathConstructor() {
        Path filePath = Path.of(fileName);
        assertDoesNotThrow(() -> {
            ConcreteFileWriter writer = new ConcreteFileWriter(filePath);
            assertNotNull(writer.filePath);
            assertEquals(filePath, writer.filePath);
        });
    }

    /** Write Tests **/
    @Test
    void writeToFileSuccess() throws IOException {
        Path filePath = Path.of(fileName);
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        
        assertTrue(writer.writeToFile("Hello World!"));
        
        List<String> lines = Files.readAllLines(filePath);
        assertFalse(lines.isEmpty());
        assertEquals(1, lines.size());
        assertEquals("Hello World!", lines.get(0));
    }

    
    @Test
    void writeToFileWithNullPath() {
        ConcreteFileWriter writer = new ConcreteFileWriter();
        assertFalse(writer.writeToFile("This better fail"));
    }

    @Test
    void writeEmptyString() throws IOException {
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        assertTrue(writer.writeToFile(""));
        
        List<String> lines = Files.readAllLines(Path.of(fileName));
        assertEquals(0, lines.size());
    }

    @Test
    void writeOverwritesExistingContent() throws IOException {
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        
        assertTrue(writer.writeToFile("First content"));
        assertTrue(writer.writeToFile("Second content"));
        
        List<String> lines = Files.readAllLines(Path.of(fileName));
        assertEquals(1, lines.size());
        assertEquals("Second content", lines.get(0));
    }

    @Test
    void writeMultilineString() throws IOException {
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        String multiline = "Line 1\nLine 2\nLine 3";
        
        assertTrue(writer.writeToFile(multiline));
        
        List<String> lines = Files.readAllLines(Path.of(fileName));
        assertEquals(3, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 2", lines.get(1));
        assertEquals("Line 3", lines.get(2));
    }

    @Test
    void writeToInvalidPath() {
        String invalidPath = "/invalid/path/that/does/not/exist/file.txt";
        ConcreteFileWriter writer = new ConcreteFileWriter(invalidPath);
        
        assertFalse(writer.writeToFile("This should fail"));
    }

    /** Read Tests **/
    @Test
    void readFromFileSuccess() throws IOException {
        String content = "Do you wanna have a bad time?";
        Files.writeString(Path.of(fileName), content);
        
        ConcreteFileWriter reader = new ConcreteFileWriter(fileName);
        List<String> result = reader.readFromFile();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(content, result.get(0));
    }

    @Test
    void readFromFileWithNullPath() {
        ConcreteFileWriter reader = new ConcreteFileWriter();
        assertNull(reader.readFromFile());
    }

    
    @Test
    void readFromNonExistentFile() {
        String nonExistentFile = tempDir.resolve("nonexistent.txt").toString();
        ConcreteFileWriter reader = new ConcreteFileWriter(nonExistentFile);
        
        assertNull(reader.readFromFile());
    }

    @Test
    void readEmptyFile() throws IOException {
        Files.writeString(Path.of(fileName), "");
        
        ConcreteFileWriter reader = new ConcreteFileWriter(fileName);
        List<String> result = reader.readFromFile();
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void readMultilineFile() throws IOException {
        List<String> expectedLines = List.of(
            "On days like these,",
            "kids like you...",
            "SHOULD BE BURNING IN HELL!"
        );
        
        Files.write(Path.of(fileName), expectedLines);
        
        ConcreteFileWriter reader = new ConcreteFileWriter(fileName);
        List<String> result = reader.readFromFile();
        
        assertNotNull(result);
        assertEquals(expectedLines.size(), result.size());
        
        for (int i = 0; i < result.size(); i++) {
            assertEquals(expectedLines.get(i), result.get(i));
        }
    }

    @Test
    void readFileWithSpecialCharacters() throws IOException {
        String specialContent = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        Files.writeString(Path.of(fileName), specialContent);
        
        ConcreteFileWriter reader = new ConcreteFileWriter(fileName);
        List<String> result = reader.readFromFile();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(specialContent, result.get(0));
    }

    /** SetPath Tests **/
    @Test
    void setPathWithString() {
        ConcreteFileWriter writer = new ConcreteFileWriter();
        assertTrue(writer.setPath(fileName));
        assertEquals(fileName, writer.filePath.toString());
    }

    @Test
    void setPathWithPath() {
        ConcreteFileWriter writer = new ConcreteFileWriter();
        Path path = Path.of(fileName);
        
        assertTrue(writer.setPath(path));
        assertEquals(path, writer.filePath);
    }

    @Test
    void setPathOverwritesExisting() {
        ConcreteFileWriter writer = new ConcreteFileWriter("initial.txt");
        writer.setPath(fileName);
        assertEquals(fileName, writer.filePath.toString());
    }

    @Test
    void setPathToNull() {
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        writer.setPath((Path) null);
        assertNull(writer.filePath);
    }

    /** Integration Tests **/
    @Test
    void writeAndReadRoundTrip() {
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        String content = "Round trip test content";
        
        assertTrue(writer.writeToFile(content));
        
        List<String> result = writer.readFromFile();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(content, result.get(0));
    }

    @Test
    void multipleOperationsOnSameFile() throws IOException {
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        
        assertTrue(writer.writeToFile("First write"));
        List<String> read1 = writer.readFromFile();
        assertEquals("First write", read1.get(0));
        
        assertTrue(writer.writeToFile("Second write"));
        List<String> read2 = writer.readFromFile();
        assertEquals("Second write", read2.get(0));
    }

    @Test
    void changePathAndWrite() {
        ConcreteFileWriter writer = new ConcreteFileWriter(fileName);
        assertTrue(writer.writeToFile("First file"));
        
        String secondFile = tempDir.resolve("second.txt").toString();
        writer.setPath(secondFile);
        assertTrue(writer.writeToFile("Second file"));
        
        writer.setPath(fileName);
        List<String> result = writer.readFromFile();
        assertEquals("First file", result.get(0));
    }
}