package model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Task represents a unit of work in the ConcurQueue system.
 * Tasks are comparable by priority (higher priority = lower number, like process priorities).
 * Thread-safe and immutable once created.
 */
public class Task implements Comparable<Task> {
    private final UUID id;
    private final String name;
    private final int priority;
    private final Instant createdTimestamp;
    private final String payload;
    
    // Retry tracking (for later phases)
    private final int retryCount;
    private final int maxRetries;
    
    /**
     * Constructor for new tasks
     */
    public Task(String name, int priority, String payload) {
        this(UUID.randomUUID(), name, priority, Instant.now(), payload, 0, 3);
    }
    
    /**
     * Full constructor (used for retry scenarios)
     */
    public Task(UUID id, String name, int priority, Instant createdTimestamp, 
                String payload, int retryCount, int maxRetries) {
        this.id = Objects.requireNonNull(id, "Task ID cannot be null");
        this.name = Objects.requireNonNull(name, "Task name cannot be null");
        this.priority = priority;
        this.createdTimestamp = Objects.requireNonNull(createdTimestamp, "Created timestamp cannot be null");
        this.payload = payload; // Can be null
        this.retryCount = Math.max(0, retryCount);
        this.maxRetries = Math.max(0, maxRetries);
    }
    
    /**
     * Creates a new task for retry (increments retry count)
     */
    public Task createRetryTask() {
        if (retryCount >= maxRetries) {
            throw new IllegalStateException("Task has exceeded maximum retry attempts");
        }
        return new Task(this.id, this.name, this.priority, this.createdTimestamp, 
                       this.payload, this.retryCount + 1, this.maxRetries);
    }
    
    /**
     * Comparable implementation - Lower priority number = Higher priority
     * If priorities are equal, sort by creation time (FIFO for same priority)
     */
    @Override
    public int compareTo(Task other) {
        // Lower number = higher priority (like Unix nice values)
        int priorityComparison = Integer.compare(this.priority, other.priority);
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        // Same priority -> FIFO (earlier timestamp first)
        return this.createdTimestamp.compareTo(other.createdTimestamp);
    }
    
    /**
     * Check if task can be retried
     */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    /**
     * Get age of task in milliseconds
     */
    public long getAgeMillis() {
        return Instant.now().toEpochMilli() - createdTimestamp.toEpochMilli();
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getPriority() { return priority; }
    public Instant getCreatedTimestamp() { return createdTimestamp; }
    public String getPayload() { return payload; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public boolean isRetry() { return retryCount > 0; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return Objects.equals(id, task.id); // Tasks are equal if they have the same ID
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash based on ID only
    }
    
    @Override
    public String toString() {
        return String.format("Task{id=%s, name='%s', priority=%d, age=%dms, retries=%d/%d, payload='%s'}", 
                           id.toString().substring(0, 8) + "...", 
                           name, priority, getAgeMillis(), retryCount, maxRetries,
                           payload != null ? payload.substring(0, Math.min(20, payload.length())) + "..." : "null");
    }
    
    /**
     * Detailed string for logging/debugging
     */
    public String toDetailedString() {
        return String.format("Task{id=%s, name='%s', priority=%d, created=%s, age=%dms, retries=%d/%d, payload='%s'}", 
                           id, name, priority, createdTimestamp, getAgeMillis(), 
                           retryCount, maxRetries, payload);
    }
}