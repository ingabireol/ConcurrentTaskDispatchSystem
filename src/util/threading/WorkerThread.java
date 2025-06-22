package util.threading;

import controller.TaskController;
import controller.SystemController;
import util.logging.Logger;

/**
 * UTILITY: Worker thread that processes tasks from the queue
 */
public class WorkerThread implements Runnable {
    private final TaskController taskController;
    private final SystemController systemController;
    private final Logger logger = Logger.getLogger(WorkerThread.class);
    private final String workerName;
    
    public WorkerThread(TaskController taskController, SystemController systemController, int workerId) {
        this.taskController = taskController;
        this.systemController = systemController;
        this.workerName = "Worker-" + workerId;
        logger.info("Worker thread '{}' initialized", workerName);
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(workerName);
        logger.info("Worker '{}' started", workerName);
        
        systemController.incrementActiveWorkers();
        
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Process next task (blocks until available)
                    taskController.processNextTask();
                    
                } catch (InterruptedException e) {
                    logger.info("Worker '{}' interrupted", workerName);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Unexpected error in worker '{}'", workerName, e);
                    // Continue processing despite errors
                }
            }
        } finally {
            systemController.decrementActiveWorkers();
            logger.info("Worker '{}' stopped", workerName);
        }
    }
}