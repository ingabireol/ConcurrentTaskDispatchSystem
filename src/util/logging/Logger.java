package util.logging;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UTILITY: Thread-safe logging framework
 */
public class Logger {
    private static final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>();
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault());
    
    private final String name;
    private volatile LogLevel threshold = LogLevel.INFO;
    private volatile boolean enableConsole = true;
    private volatile boolean enableFile = true;
    
    private Logger(String name) {
        this.name = name;
    }
    
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }
    
    public static Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, Logger::new);
    }
    
    // Configuration methods
    public Logger setThreshold(LogLevel threshold) {
        this.threshold = threshold;
        return this;
    }
    
    public Logger setConsoleEnabled(boolean enabled) {
        this.enableConsole = enabled;
        return this;
    }
    
    public Logger setFileEnabled(boolean enabled) {
        this.enableFile = enabled;
        return this;
    }
    
    // Logging methods
    public void trace(String message, Object... args) {
        log(LogLevel.TRACE, message, args);
    }
    
    public void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, args);
    }
    
    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }
    
    public void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }
    
    public void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }
    
    public void error(String message, Throwable throwable, Object... args) {
        log(LogLevel.ERROR, message + " - Exception: " + throwable.getMessage(), args);
        if (LogLevel.ERROR.isLoggable(threshold)) {
            throwable.printStackTrace();
        }
    }
    
    public void fatal(String message, Object... args) {
        log(LogLevel.FATAL, message, args);
    }
    
    private void log(LogLevel level, String message, Object... args) {
        if (!level.isLoggable(threshold)) {
            return;
        }
        
        String formattedMessage = formatMessage(message, args);
        String logEntry = createLogEntry(level, formattedMessage);
        
        if (enableConsole) {
            System.out.println(logEntry);
        }
        
        if (enableFile) {
            FileLogger.writeToFile(name, logEntry);
        }
    }
    
    private String formatMessage(String message, Object... args) {
        if (args.length == 0) {
            return message;
        }
        
        // Simple placeholder replacement: {} with arguments
        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", String.valueOf(arg));
        }
        return result;
    }
    
    private String createLogEntry(LogLevel level, String message) {
        return String.format("[%s] [%s] [%s] [%s] %s",
                           TIMESTAMP_FORMAT.format(Instant.now()),
                           Thread.currentThread().getName(),
                           level.getName(),
                           name,
                           message);
    }
}