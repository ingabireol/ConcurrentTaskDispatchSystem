package model.entity;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MODEL: Thread-safe metrics collection for system monitoring
 */
public class SystemMetrics {
    private final AtomicLong tasksSubmitted = new AtomicLong(0);
    private final AtomicLong tasksCompleted = new AtomicLong(0);
    private final AtomicLong tasksFailed = new AtomicLong(0);
    private final AtomicLong tasksInProgress = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final Instant startTime;
    
    public SystemMetrics() {
        this.startTime = Instant.now();
    }
    
    // Increment methods
    public void incrementSubmitted() { tasksSubmitted.incrementAndGet(); }
    public void incrementCompleted() { tasksCompleted.incrementAndGet(); }
    public void incrementFailed() { tasksFailed.incrementAndGet(); }
    public void incrementInProgress() { tasksInProgress.incrementAndGet(); }
    public void decrementInProgress() { tasksInProgress.decrementAndGet(); }
    public void addProcessingTime(long timeMs) { totalProcessingTime.addAndGet(timeMs); }
    
    // Getters
    public long getTasksSubmitted() { return tasksSubmitted.get(); }
    public long getTasksCompleted() { return tasksCompleted.get(); }
    public long getTasksFailed() { return tasksFailed.get(); }
    public long getTasksInProgress() { return tasksInProgress.get(); }
    public long getTotalProcessingTime() { return totalProcessingTime.get(); }
    public Instant getStartTime() { return startTime; }
    
    // Calculated metrics
    public double getAverageProcessingTime() {
        long completed = getTasksCompleted();
        return completed > 0 ? (double) getTotalProcessingTime() / completed : 0.0;
    }
    
    public double getCompletionRate() {
        long total = getTasksSubmitted();
        return total > 0 ? (double) getTasksCompleted() / total : 0.0;
    }
    
    public long getUptimeSeconds() {
        return Instant.now().getEpochSecond() - startTime.getEpochSecond();
    }
}
