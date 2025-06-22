package model.service;

import model.entity.Task;
import model.entity.ProcessingResult;
import util.logging.Logger;

import java.util.concurrent.ThreadLocalRandom;

/**
 * MODEL SERVICE: Business logic for task processing
 */
public class ProcessingService {
    private final Logger logger = Logger.getLogger(ProcessingService.class);
    private final double failureRate;
    
    public ProcessingService() {
        this(0.1); // 10% failure rate by default
    }
    
    public ProcessingService(double failureRate) {
        this.failureRate = failureRate;
        logger.info("Processing service initialized with {}% failure rate", failureRate * 100);
    }
    
    public ProcessingResult processTask(Task task) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        logger.info("Starting processing of task: {} (Priority: {})", task.getName(), task.getPriority());
        
        try {
            // Calculate processing time based on priority (higher priority = faster processing)
            int baseTime = 1000; // 1 second base
            int priorityAdjustment = (10 - task.getPriority()) * 100; // Lower priority takes longer
            int processingTime = baseTime + priorityAdjustment;
            
            logger.debug("Estimated processing time for task {}: {}ms", task.getId(), processingTime);
            
            // Simulate work
            Thread.sleep(processingTime);
            
            // Simulate random failures
            boolean success = ThreadLocalRandom.current().nextDouble() > failureRate;
            long actualTime = System.currentTimeMillis() - startTime;
            
            if (success) {
                logger.info("Task {} completed successfully in {}ms", task.getId(), actualTime);
                return ProcessingResult.success("Processing completed successfully", actualTime);
            } else {
                logger.warn("Task {} failed during processing after {}ms", task.getId(), actualTime);
                return ProcessingResult.failure("Simulated processing failure", 
                                              new RuntimeException("Random failure"), actualTime);
            }
            
        } catch (InterruptedException e) {
            long actualTime = System.currentTimeMillis() - startTime;
            logger.warn("Task {} processing interrupted after {}ms", task.getId(), actualTime);
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            long actualTime = System.currentTimeMillis() - startTime;
            logger.error("Unexpected error processing task {}", task.getId(), e);
            return ProcessingResult.failure("Unexpected processing error: " + e.getMessage(), e, actualTime);
        }
    }
}
