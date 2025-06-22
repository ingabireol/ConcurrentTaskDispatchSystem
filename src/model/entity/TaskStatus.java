package model.entity;

/**
 * MODEL: Enumeration of possible task states
 */
public enum TaskStatus {
    SUBMITTED("Task has been submitted to the queue"),
    PROCESSING("Task is currently being processed"),
    COMPLETED("Task has been completed successfully"),
    FAILED("Task has failed after maximum retries"),
    RETRY_PENDING("Task failed but will be retried");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean isActive() {
        return this == PROCESSING;
    }
}
