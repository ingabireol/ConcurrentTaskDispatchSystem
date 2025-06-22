package model;

import model.entity.Task;
import model.entity.TaskStatus;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TaskProducer simulates API clients or services that submit tasks to the queue.
 * Each producer runs in its own thread and generates tasks based on a specific strategy.
 */
public class TaskProducer extends Thread {
    
    public enum ProducerStrategy {
        HIGH_PRIORITY_BURST,    // Generates mostly high-priority tasks in bursts
        MIXED_STEADY,          // Generates mixed priority tasks at steady rate
        LOW_PRIORITY_BULK      // Generates many low-priority tasks
    }
    
    private final String producerName;
    private final ProducerStrategy strategy;
    private final BlockingQueue<Task> taskQueue;
    private final ConcurrentHashMap<String, TaskStatus> taskTracker;
    private final Random random;
    
    // Configuration
    private final int tasksPerBatch;
    private final long batchIntervalMs;
    private final int maxTasks;
    
    // Statistics
    private final AtomicInteger tasksProduced = new AtomicInteger(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    
    // Control
    private volatile boolean running = true;
    private volatile boolean paused = false;
    
    /**
     * Create a new TaskProducer
     */
    public TaskProducer(String producerName, 
                       ProducerStrategy strategy,
                       BlockingQueue<Task> taskQueue,
                       ConcurrentHashMap<String, TaskStatus> taskTracker,
                       int tasksPerBatch,
                       long batchIntervalMs,
                       int maxTasks) {
        super("Producer-" + producerName);
        this.producerName = producerName;
        this.strategy = strategy;
        this.taskQueue = taskQueue;
        this.taskTracker = taskTracker;
        this.tasksPerBatch = tasksPerBatch;
        this.batchIntervalMs = batchIntervalMs;
        this.maxTasks = maxTasks;
        this.random = new Random();
        
        setDaemon(false); // Don't let JVM exit while producers are running
    }
    
    @Override
    public void run() {
        System.out.println("🚀 Producer " + producerName + " started with strategy: " + strategy);
        
        try {
            while (running && tasksProduced.get() < maxTasks) {
                if (paused) {
                    Thread.sleep(100);
                    continue;
                }
                
                // Generate a batch of tasks
                generateTaskBatch();
                
                // Wait before next batch
                if (batchIntervalMs > 0) {
                    Thread.sleep(batchIntervalMs);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("⚠️ Producer " + producerName + " interrupted");
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("🛑 Producer " + producerName + " stopped. Total tasks produced: " + tasksProduced.get());
        }
    }
    
    /**
     * Generate a batch of tasks based on the producer's strategy
     */
    private void generateTaskBatch() throws InterruptedException {
        int batchSize = Math.min(tasksPerBatch, maxTasks - tasksProduced.get());
        
        for (int i = 0; i < batchSize && running; i++) {
            Task task = generateTask();
            
            long startTime = System.nanoTime();
            
            // Submit task to queue (this may block if queue has capacity limits)
            taskQueue.put(task);
            
            // Track the task
            taskTracker.put(task.getId().toString(), TaskStatus.SUBMITTED);
            
            long waitTime = System.nanoTime() - startTime;
            totalWaitTime.addAndGet(waitTime);
            
            tasksProduced.incrementAndGet();
            
            System.out.printf("📤 [%s] Submitted %s (Queue size: %d)%n", 
                            producerName, task, taskQueue.size());
        }
    }
    
    /**
     * Generate a single task based on the producer's strategy
     */
    private Task generateTask() {
        String taskName;
        int priority;
        String payload;
        
        switch (strategy) {
            case HIGH_PRIORITY_BURST:
                // 80% high priority (1-3), 20% medium priority (4-6)
                priority = random.nextBoolean() && random.nextBoolean() ? 
                          random.nextInt(3) + 1 : random.nextInt(3) + 4;
                taskName = "Critical-" + System.currentTimeMillis();
                payload = generateCriticalPayload();
                break;
                
            case MIXED_STEADY:
                // Even distribution across all priorities (1-10)
                priority = random.nextInt(10) + 1;
                taskName = "Standard-" + System.currentTimeMillis();
                payload = generateStandardPayload();
                break;
                
            case LOW_PRIORITY_BULK:
                // 80% low priority (7-10), 20% medium priority (4-6)
                priority = random.nextBoolean() && random.nextBoolean() ? 
                          random.nextInt(3) + 4 : random.nextInt(4) + 7;
                taskName = "Bulk-" + System.currentTimeMillis();
                payload = generateBulkPayload();
                break;
                
            default:
                priority = 5;
                taskName = "Default-" + System.currentTimeMillis();
                payload = "default payload";
        }
        
        return new Task(taskName, priority, payload);
    }
    
    /**
     * Generate critical task payloads (shorter, more urgent)
     */
    private String generateCriticalPayload() {
        String[] criticalTasks = {
            "Process urgent user authentication",
            "Handle payment failure notification",
            "Execute emergency data backup",
            "Process security alert",
            "Handle system health check failure"
        };
        return criticalTasks[random.nextInt(criticalTasks.length)];
    }
    
    /**
     * Generate standard task payloads
     */
    private String generateStandardPayload() {
        String[] standardTasks = {
            "Process user registration email",
            "Generate daily analytics report",
            "Update user preferences",
            "Process file upload",
            "Send newsletter to subscribers",
            "Validate user input data",
            "Process password reset request"
        };
        return standardTasks[random.nextInt(standardTasks.length)] + 
               " - Data: " + generateRandomData();
    }
    
    /**
     * Generate bulk task payloads (longer, less urgent)
     */
    private String generateBulkPayload() {
        String[] bulkTasks = {
            "Process batch data import from CSV",
            "Generate monthly user activity report",
            "Clean up old log files and archives",
            "Sync data with external backup service",
            "Process bulk email campaign",
            "Update search index for all products",
            "Generate quarterly financial summary"
        };
        return bulkTasks[random.nextInt(bulkTasks.length)] + 
               " - Batch ID: " + random.nextInt(10000);
    }
    
    /**
     * Generate random data for task payloads
     */
    private String generateRandomData() {
        return "User-" + random.nextInt(1000) + ", Session-" + random.nextInt(10000);
    }
    
    /**
     * Gracefully stop the producer
     */
    public void stopProducer() {
        running = false;
        interrupt(); // Wake up if sleeping
    }
    
    /**
     * Pause/resume the producer
     */
    public void pauseProducer() {
        paused = true;
    }
    
    public void resumeProducer() {
        paused = false;
    }
    
    /**
     * Get producer statistics
     */
    public ProducerStats getStats() {
        return new ProducerStats(
            producerName,
            strategy,
            tasksProduced.get(),
            totalWaitTime.get() / 1_000_000, // Convert to milliseconds
            running,
            paused
        );
    }
    
    /**
     * Statistics holder class
     */
    public static class ProducerStats {
        public final String name;
        public final ProducerStrategy strategy;
        public final int tasksProduced;
        public final long totalWaitTimeMs;
        public final boolean running;
        public final boolean paused;
        
        public ProducerStats(String name, ProducerStrategy strategy, int tasksProduced, 
                           long totalWaitTimeMs, boolean running, boolean paused) {
            this.name = name;
            this.strategy = strategy;
            this.tasksProduced = tasksProduced;
            this.totalWaitTimeMs = totalWaitTimeMs;
            this.running = running;
            this.paused = paused;
        }
        
        @Override
        public String toString() {
            return String.format("Producer{name='%s', strategy=%s, produced=%d, waitTime=%dms, running=%s, paused=%s}",
                               name, strategy, tasksProduced, totalWaitTimeMs, running, paused);
        }
    }
}