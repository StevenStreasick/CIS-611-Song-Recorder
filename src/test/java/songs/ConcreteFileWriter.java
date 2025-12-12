package songs;

import java.nio.file.Path;

class ConcreteFileWriter extends FileWriter {
    public ConcreteFileWriter() {
        super();
    }
    
    public ConcreteFileWriter(String fileName) {
        super(fileName);
    }
    
    public ConcreteFileWriter(Path filePath) {
        super(filePath);
    }
    
    public Path getFilePath() {
        return this.filePath;
    }
    
}
