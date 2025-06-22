package util.logging;

/**
 * UTILITY: Log levels for structured logging
 */
public enum LogLevel {
    TRACE(0, "TRACE"),
    DEBUG(1, "DEBUG"),
    INFO(2, "INFO"),
    WARN(3, "WARN"),
    ERROR(4, "ERROR"),
    FATAL(5, "FATAL");
    
    private final int level;
    private final String name;
    
    LogLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }
    
    public int getLevel() { return level; }
    public String getName() { return name; }
    
    public boolean isLoggable(LogLevel threshold) {
        return this.level >= threshold.level;
    }
}
