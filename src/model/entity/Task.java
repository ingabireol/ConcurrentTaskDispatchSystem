package model.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * MODEL: Core Task entity representing work to be processed
 * Immutable design ensures thread safety
 */
public final class Task implements Comparable<Task> {
    private final UUID id;
    private final String name;
    private final int priority;
    private final Instant createdTimestamp;
    private final String payload;
    private final int retryCount;
    private final String producerSource;

    public Task(String name, int priority, String payload, String producerSource) {
        this.id = UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "Task name cannot be null");
        this.priority = priority;
        this.createdTimestamp = Instant.now();
        this.payload = payload;
        this.retryCount = 0;
        this.producerSource = producerSource;
    }

    // Copy constructor for retries
    private Task(Task original, int newRetryCount) {
        this.id = original.id;
        this.name = original.name;
        this.priority = original.priority;
        this.createdTimestamp = original.createdTimestamp;
        this.payload = original.payload;
        this.retryCount = newRetryCount;
        this.producerSource = original.producerSource;
    }


    public Task withIncrementedRetry() {
        return new Task(this, this.retryCount + 1);
    }

    @Override
    public int compareTo(Task other) {
        // Higher priority first, then by creation time
        int priorityComparison = Integer.compare(other.priority, this.priority);
        return priorityComparison != 0 ? priorityComparison :
                this.createdTimestamp.compareTo(other.createdTimestamp);
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getPriority() { return priority; }
    public Instant getCreatedTimestamp() { return createdTimestamp; }
    public String getPayload() { return payload; }
    public int getRetryCount() { return retryCount; }
    public String getProducerSource() { return producerSource; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Task{id=%s, name='%s', priority=%d, retries=%d, source='%s'}",
                id.toString().substring(0, 8), name, priority, retryCount, producerSource);
    }
}
