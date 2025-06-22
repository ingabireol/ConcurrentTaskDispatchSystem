package view.formatter;

import model.entity.Task;
import model.entity.ProcessingResult;

import java.time.format.DateTimeFormatter;

/**
 * VIEW: Formats task information for display
 */
public class TaskFormatter {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public String formatSubmission(Task task) {
        return String.format("[%s] SUBMITTED - %s (Priority: %d, Source: %s)",
                           TIME_FORMAT.format(task.getCreatedTimestamp()),
                           task.getName(),
                           task.getPriority(),
                           task.getProducerSource());
    }
    
    public String formatProcessingStart(Task task) {
        return String.format("[%s] PROCESSING - %s by %s",
                           TIME_FORMAT.format(java.time.Instant.now()),
                           task.getName(),
                           Thread.currentThread().getName());
    }
    
    public String formatCompletion(Task task, ProcessingResult result) {
        return String.format("[%s] COMPLETED - %s in %dms by %s",
                           TIME_FORMAT.format(result.getCompletedAt()),
                           task.getName(),
                           result.getProcessingTimeMs(),
                           result.getWorkerThreadName());
    }
    
    public String formatFailure(Task task, ProcessingResult result) {
        return String.format("[%s] FAILED - %s (Retry: %d/%d) - %s",
                           TIME_FORMAT.format(result.getCompletedAt()),
                           task.getName(),
                           task.getRetryCount(),
                           3, // Max retries - should be configurable
                           result.getMessage());
    }
    
    public String formatRetry(Task task) {
        return String.format("[%s] RETRY - %s (Attempt %d)",
                           TIME_FORMAT.format(java.time.Instant.now()),
                           task.getName(),
                           task.getRetryCount() + 1);
    }
    
    public String formatTaskSummary(Task task) {
        return String.format("Task{id=%s, name='%s', priority=%d, retries=%d}",
                           task.getId().toString().substring(0, 8),
                           task.getName(),
                           task.getPriority(),
                           task.getRetryCount());
    }
}