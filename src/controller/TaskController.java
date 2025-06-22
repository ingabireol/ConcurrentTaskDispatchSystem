package controller;

import model.entity.Task;
import model.entity.TaskStatus;
import model.entity.ProcessingResult;
import model.service.QueueService;
import model.service.ProcessingService;
import model.service.MetricsService;
import model.repository.TaskRepository;
import view.console.ConsoleView;
import util.logging.Logger;

/**
 * CONTROLLER: Orchestrates task operations between Model and View
 */
public class TaskController {
    private final QueueService queueService;
    private final ProcessingService processingService;
    private final MetricsService metricsService;
    private final TaskRepository taskRepository;
    private final ConsoleView consoleView;
    private final Logger logger = Logger.getLogger(TaskController.class);
    private final int maxRetries;
    
    public TaskController(QueueService queueService,
                         ProcessingService processingService,
                         MetricsService metricsService,
                         TaskRepository taskRepository,
                         ConsoleView consoleView,
                         int maxRetries) {
        this.queueService = queueService;
        this.processingService = processingService;
        this.metricsService = metricsService;
        this.taskRepository = taskRepository;
        this.consoleView = consoleView;
        this.maxRetries = maxRetries;
        logger.info("Task controller initialized with max retries: {}", maxRetries);
    }
    
    /**
     * Submit a new task to the system
     */
    public boolean submitTask(Task task) {
        logger.info("Submitting task: {}", task.getName());
        
        try {
            // Save to repository
            taskRepository.save(task, TaskStatus.SUBMITTED);
            
            // Add to queue
            boolean queued = queueService.offer(task);
            
            if (queued) {
                // Update metrics
                metricsService.recordTaskSubmission();
                
                // Update view
                consoleView.getTaskDisplayView().displayTaskSubmitted(task);
                
                logger.info("Task {} successfully submitted", task.getId());
                return true;
            } else {
                logger.error("Failed to queue task: {}", task.getId());
                taskRepository.updateStatus(task.getId(), TaskStatus.FAILED);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error submitting task: {}", task.getId(), e);
            consoleView.displayError("Failed to submit task: " + task.getName(), e);
            return false;
        }
    }
    
    /**
     * Process the next available task
     */
    public void processNextTask() throws InterruptedException {
        logger.trace("Processing next task from queue");
        
        Task task = queueService.take(); // Blocks until task available
        processTask(task);
    }
    
    /**
     * Process a specific task
     */
    private void processTask(Task task) {
        logger.info("Processing task: {}", task.getId());
        
        try {
            // Update status and metrics
            taskRepository.updateStatus(task.getId(), TaskStatus.PROCESSING);
            metricsService.recordTaskStarted();
            
            // Update view
            consoleView.getTaskDisplayView().displayTaskStarted(task);
            
            // Process the task
            ProcessingResult result = processingService.processTask(task);
            
            // Handle result
            handleProcessingResult(task, result);
            
        } catch (InterruptedException e) {
            logger.warn("Task processing interrupted: {}", task.getId());
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("Unexpected error processing task: {}", task.getId(), e);
            handleProcessingError(task, e);
        }
    }
    
    private void handleProcessingResult(Task task, ProcessingResult result) {
        metricsService.recordTaskCompleted(result);
        
        if (result.isSuccessful()) {
            // Task completed successfully
            taskRepository.updateStatus(task.getId(), TaskStatus.COMPLETED);
            consoleView.getTaskDisplayView().displayTaskCompleted(task, result);
            logger.info("Task {} completed successfully", task.getId());
            
        } else {
            // Task failed - check if we should retry
            if (task.getRetryCount() < maxRetries) {
                handleTaskRetry(task);
            } else {
                // Max retries exceeded
                taskRepository.updateStatus(task.getId(), TaskStatus.FAILED);
                consoleView.getTaskDisplayView().displayTaskFailed(task, result);
                logger.warn("Task {} failed permanently after {} retries", task.getId(), task.getRetryCount());
            }
        }
    }
    
    private void handleTaskRetry(Task task) {
        Task retryTask = task.withIncrementedRetry();
        
        logger.info("Retrying task: {} (attempt {})", task.getId(), retryTask.getRetryCount());
        
        // Update repository with retry task
        taskRepository.save(retryTask, TaskStatus.SUBMITTED);
        taskRepository.updateStatus(task.getId(), TaskStatus.RETRY_PENDING);
        
        // Re-queue for processing
        queueService.offer(retryTask);
        
        // Update view
        consoleView.getTaskDisplayView().displayTaskRetry(retryTask);
    }
    
    private void handleProcessingError(Task task, Exception error) {
        ProcessingResult errorResult = ProcessingResult.failure(
            "Processing error: " + error.getMessage(), error, 0);
        
        handleProcessingResult(task, errorResult);
    }
    
    public TaskStatus getTaskStatus(String taskId) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(taskId);
            return taskRepository.getStatus(uuid);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid task ID format: {}", taskId);
            return null;
        }
    }
}