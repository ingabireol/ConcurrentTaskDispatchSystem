package model;

/**
 * TaskStatus represents the current state of a task in the ConcurQueue system.
 * Thread-safe enum for tracking task lifecycle.
 */
public enum TaskStatus {
    /**
     * Task has been submitted to the queue but not yet picked up by a worker
     */
    SUBMITTED("Task submitted to queue"),
    
    /**
     * Task is currently being processed by a worker thread
     */
    PROCESSING("Task being processed"),
    
    /**
     * Task completed successfully
     */
    COMPLETED("Task completed successfully"),
    
    /**
     * Task failed during processing
     */
    FAILED("Task failed during processing"),
    
    /**
     * Task failed and is being retried
     */
    RETRYING("Task failed, retrying"),
    
    /**
     * Task failed maximum number of times and will not be retried
     */
    PERMANENTLY_FAILED("Task permanently failed");
    
    private final String description;
    
    TaskStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this status indicates the task is still active (not in a final state)
     */
    public boolean isActive() {
        return this == SUBMITTED || this == PROCESSING || this == RETRYING;
    }
    
    /**
     * Check if this status indicates the task is completed (successfully or permanently failed)
     */
    public boolean isFinal() {
        return this == COMPLETED || this == PERMANENTLY_FAILED;
    }
    
    /**
     * Check if this status indicates the task failed but might be retried
     */
    public boolean isFailedButRetriable() {
        return this == FAILED;
    }
    
    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}