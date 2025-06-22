package model.service;

import model.entity.Task;
import util.logging.Logger;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * MODEL SERVICE: Thread-safe queue operations
 */
public class QueueService {
    private final PriorityBlockingQueue<Task> queue;
    private final Logger logger = Logger.getLogger(QueueService.class);
    
    public QueueService() {
        this.queue = new PriorityBlockingQueue<>();
        logger.info("Queue service initialized with PriorityBlockingQueue");
    }
    
    public boolean offer(Task task) {
        boolean added = queue.offer(task);
        logger.debug("Task {} to queue: {}", added ? "added" : "rejected", task.getId());
        return added;
    }
    
    public Task take() throws InterruptedException {
        logger.trace("Taking task from queue (blocking)");
        Task task = queue.take();
        logger.debug("Retrieved task from queue: {}", task.getId());
        return task;
    }
    
    public Task poll(long timeout, TimeUnit unit) throws InterruptedException {
        logger.trace("Polling queue with timeout: {} {}", timeout, unit);
        Task task = queue.poll(timeout, unit);
        if (task != null) {
            logger.debug("Retrieved task from queue: {}", task.getId());
        } else {
            logger.trace("No task available within timeout");
        }
        return task;
    }
    
    public int size() {
        int size = queue.size();
        logger.trace("Queue size requested: {}", size);
        return size;
    }
    
    public boolean isEmpty() {
        boolean empty = queue.isEmpty();
        logger.trace("Queue empty check: {}", empty);
        return empty;
    }
    
    public void clear() {
        int sizeBefore = queue.size();
        queue.clear();
        logger.warn("Queue cleared - {} tasks removed", sizeBefore);
    }
}