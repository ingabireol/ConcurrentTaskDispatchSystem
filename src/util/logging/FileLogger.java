package util.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UTILITY: Thread-safe file logging
 */
public class FileLogger {
    private static final String LOG_DIR = "logs";
    private static final ConcurrentHashMap<String, FileWriter> writers = new ConcurrentHashMap<>();
    
    static {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }
    
    public static void writeToFile(String loggerName, String logEntry) {
        try {
            String filename = LOG_DIR + "/" + loggerName.toLowerCase() + ".log";
            FileWriter writer = writers.computeIfAbsent(filename, f -> {
                try {
                    return new FileWriter(f, true); // Append mode
                } catch (IOException e) {
                    System.err.println("Failed to create file writer for " + f + ": " + e.getMessage());
                    return null;
                }
            });
            
            if (writer != null) {
                synchronized (writer) {
                    writer.write(logEntry + System.lineSeparator());
                    writer.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    public static void closeAll() {
        writers.values().forEach(writer -> {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Failed to close log file: " + e.getMessage());
            }
        });
        writers.clear();
    }
}
