package model.entity;

import java.time.Instant;

/**
 * MODEL: Immutable result of task processing
 */
public final class ProcessingResult {
    private final boolean successful;
    private final String message;
    private final Exception error;
    private final long processingTimeMs;
    private final Instant completedAt;
    private final String workerThreadName;
    
    private ProcessingResult(boolean successful, String message, Exception error, 
                           long processingTimeMs, String workerThreadName) {
        this.successful = successful;
        this.message = message;
        this.error = error;
        this.processingTimeMs = processingTimeMs;
        this.completedAt = Instant.now();
        this.workerThreadName = workerThreadName;
    }
    
    public static ProcessingResult success(String message, long processingTimeMs) {
        return new ProcessingResult(true, message, null, processingTimeMs, 
                                  Thread.currentThread().getName());
    }
    
    public static ProcessingResult failure(String message, Exception error, long processingTimeMs) {
        return new ProcessingResult(false, message, error, processingTimeMs, 
                                  Thread.currentThread().getName());
    }
    
    // Getters
    public boolean isSuccessful() { return successful; }
    public String getMessage() { return message; }
    public Exception getError() { return error; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public Instant getCompletedAt() { return completedAt; }
    public String getWorkerThreadName() { return workerThreadName; }
    
    @Override
    public String toString() {
        return String.format("ProcessingResult{success=%s, time=%dms, worker=%s, message='%s'}", 
                           successful, processingTimeMs, workerThreadName, message);
    }
}
