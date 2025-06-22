package model.service;

import model.entity.SystemMetrics;
import model.entity.ProcessingResult;
import util.logging.Logger;

/**
 * MODEL SERVICE: System metrics collection and calculation
 */
public class MetricsService {
    private final SystemMetrics metrics;
    private final Logger logger = Logger.getLogger(MetricsService.class);
    
    public MetricsService() {
        this.metrics = new SystemMetrics();
        logger.info("Metrics service initialized");
    }
    
    public void recordTaskSubmission() {
        metrics.incrementSubmitted();
        logger.trace("Task submission recorded");
    }
    
    public void recordTaskStarted() {
        metrics.incrementInProgress();
        logger.trace("Task start recorded");
    }
    
    public void recordTaskCompleted(ProcessingResult result) {
        metrics.decrementInProgress();
        
        if (result.isSuccessful()) {
            metrics.incrementCompleted();
            logger.debug("Task completion recorded - success");
        } else {
            metrics.incrementFailed();
            logger.debug("Task completion recorded - failure");
        }
        
        metrics.addProcessingTime(result.getProcessingTimeMs());
        logger.trace("Processing time recorded: {}ms", result.getProcessingTimeMs());
    }
    
    public SystemMetrics getMetrics() {
        logger.trace("Metrics requested");
        return metrics;
    }
    
    public void logCurrentMetrics() {
        SystemMetrics current = getMetrics();
        logger.info("Current metrics - Submitted: {}, Completed: {}, Failed: {}, In Progress: {}, Avg Time: {:.1f}ms",
                   current.getTasksSubmitted(),
                   current.getTasksCompleted(),
                   current.getTasksFailed(),
                   current.getTasksInProgress(),
                   current.getAverageProcessingTime());
    }
}