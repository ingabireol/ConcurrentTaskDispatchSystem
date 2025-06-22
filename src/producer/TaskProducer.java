package producer;

import controller.TaskController;
import model.entity.Task;
import util.logging.Logger;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PRODUCER: Enhanced TaskProducer that integrates with MVC architecture
 * Supports multiple production strategies and comprehensive statistics tracking
 */
public class TaskProducer extends Thread {

    public enum ProducerStrategy {
        HIGH_PRIORITY_BURST("Generates mostly high-priority tasks in bursts"),
        MIXED_STEADY("Generates mixed priority tasks at steady rate"),
        LOW_PRIORITY_BULK("Generates many low-priority tasks"),
        ADAPTIVE("Adapts based on queue size and system load");

        private final String description;

        ProducerStrategy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Core components
    private final String producerName;
    private final ProducerStrategy strategy;
    private final TaskController taskController;
    private final Logger logger;
    private final Random random;

    // Configuration
    private final int tasksPerBatch;
    private final long batchIntervalMs;
    private final int maxTasks;

    // Statistics - Thread-safe counters
    private final AtomicInteger tasksProduced = new AtomicInteger(0);
    private final AtomicInteger tasksSuccessfullySubmitted = new AtomicInteger(0);
    private final AtomicInteger tasksRejected = new AtomicInteger(0);
    private final AtomicLong totalSubmissionTime = new AtomicLong(0);
    private final AtomicLong batchesProduced = new AtomicLong(0);

    // Control flags
    private volatile boolean running = true;
    private volatile boolean paused = false;

    // Adaptive strategy fields
    private final AtomicInteger consecutiveRejections = new AtomicInteger(0);
    private volatile long adaptiveDelay = 0;

    /**
     * Create a new enhanced TaskProducer
     */
    public TaskProducer(String producerName,
                        ProducerStrategy strategy,
                        TaskController taskController,
                        int tasksPerBatch,
                        long batchIntervalMs,
                        int maxTasks) {
        super("Producer-" + producerName);
        this.producerName = producerName;
        this.strategy = strategy;
        this.taskController = taskController;
        this.tasksPerBatch = tasksPerBatch;
        this.batchIntervalMs = batchIntervalMs;
        this.maxTasks = maxTasks;
        this.random = new Random();
        this.logger = Logger.getLogger(TaskProducer.class);

        setDaemon(false); // Don't let JVM exit while producers are running
        logger.info("Producer '{}' created with strategy: {} (max tasks: {}, batch size: {}, interval: {}ms)",
                producerName, strategy, maxTasks, tasksPerBatch, batchIntervalMs);
    }

    @Override
    public void run() {
        logger.info("Producer '{}' started with strategy: {}", producerName, strategy);
        System.out.printf("🚀 Producer %s started with strategy: %s%n", producerName, strategy);

        try {
            while (running && tasksProduced.get() < maxTasks) {
                if (paused) {
                    Thread.sleep(100);
                    continue;
                }

                // Generate a batch of tasks
                generateTaskBatch();

                // Adaptive delay based on system feedback
                long totalDelay = batchIntervalMs + adaptiveDelay;
                if (totalDelay > 0) {
                    Thread.sleep(totalDelay);
                }

                batchesProduced.incrementAndGet();
            }
        } catch (InterruptedException e) {
            logger.info("Producer '{}' interrupted", producerName);
            Thread.currentThread().interrupt();
        } finally {
            logFinalStats();
            System.out.printf("🛑 Producer %s stopped. Total tasks produced: %d (Success: %d, Rejected: %d)%n",
                    producerName, tasksProduced.get(), tasksSuccessfullySubmitted.get(), tasksRejected.get());
        }
    }

    /**
     * Generate a batch of tasks based on the producer's strategy
     */
    private void generateTaskBatch() throws InterruptedException {
        int batchSize = Math.min(tasksPerBatch, maxTasks - tasksProduced.get());

        logger.debug("Producer '{}' generating batch of {} tasks", producerName, batchSize);

        for (int i = 0; i < batchSize && running; i++) {
            Task task = generateTask();

            long startTime = System.nanoTime();

            // Submit task through MVC controller
            boolean submitted = taskController.submitTask(task);

            long submissionTime = System.nanoTime() - startTime;
            totalSubmissionTime.addAndGet(submissionTime);

            if (submitted) {
                tasksSuccessfullySubmitted.incrementAndGet();
                consecutiveRejections.set(0);
                adaptiveDelay = Math.max(0, adaptiveDelay - 50); // Reduce delay on success

                logger.debug("Task submitted successfully: {}", task.getName());
                System.out.printf("📤 [%s] Submitted %s%n", producerName, task);
            } else {
                tasksRejected.incrementAndGet();
                handleRejection();
                logger.warn("Task submission rejected: {}", task.getName());
                System.out.printf("❌ [%s] Rejected %s%n", producerName, task);
            }

            tasksProduced.incrementAndGet();
        }
    }

    /**
     * Handle task rejection with adaptive backoff
     */
    private void handleRejection() {
        int rejections = consecutiveRejections.incrementAndGet();

        // Adaptive backoff: increase delay exponentially
        if (rejections > 3) {
            adaptiveDelay = Math.min(5000, adaptiveDelay + (rejections * 100));
            logger.warn("Producer '{}' experiencing {} consecutive rejections, adaptive delay: {}ms",
                    producerName, rejections, adaptiveDelay);
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
                // 80% high priority (8-10), 20% medium priority (5-7)
                priority = (random.nextDouble() < 0.8) ?
                        random.nextInt(3) + 8 : random.nextInt(3) + 5;
                taskName = "Critical-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
                payload = generateCriticalPayload();
                break;

            case MIXED_STEADY:
                // Even distribution across all priorities (1-10)
                priority = random.nextInt(10) + 1;
                taskName = "Standard-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
                payload = generateStandardPayload();
                break;

            case LOW_PRIORITY_BULK:
                // 80% low priority (1-4), 20% medium priority (5-7)
                priority = (random.nextDouble() < 0.8) ?
                        random.nextInt(4) + 1 : random.nextInt(3) + 5;
                taskName = "Bulk-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
                payload = generateBulkPayload();
                break;

            case ADAPTIVE:
                // Adapt priority based on queue performance
                priority = calculateAdaptivePriority();
                taskName = "Adaptive-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
                payload = generateAdaptivePayload(priority);
                break;

            default:
                priority = 5;
                taskName = "Default-" + System.currentTimeMillis();
                payload = "Default task payload";
        }

        logger.trace("Generated task: {} with priority {} and payload length {}",
                taskName, priority, payload.length());

        return new Task(taskName, priority, payload, producerName);
    }

    /**
     * Calculate adaptive priority based on system performance
     */
    private int calculateAdaptivePriority() {
        double successRate = tasksProduced.get() > 0 ?
                (double) tasksSuccessfullySubmitted.get() / tasksProduced.get() : 1.0;

        if (successRate > 0.9) {
            // High success rate - can afford to send lower priority tasks
            return random.nextInt(6) + 1; // Priority 1-6
        } else if (successRate > 0.7) {
            // Medium success rate - balanced approach
            return random.nextInt(10) + 1; // Priority 1-10
        } else {
            // Low success rate - focus on high priority tasks
            return random.nextInt(4) + 7; // Priority 7-10
        }
    }

    /**
     * Generate critical task payloads (shorter, more urgent)
     */
    private String generateCriticalPayload() {
        String[] criticalTasks = {
                "URGENT: Process security breach alert - immediate response required",
                "CRITICAL: Payment gateway failure detected - restore service",
                "EMERGENCY: Database connection lost - initiate failover procedure",
                "ALERT: Memory usage critical - trigger cleanup process",
                "URGENT: User authentication service down - investigate immediately",
                "CRITICAL: API rate limit exceeded - implement throttling",
                "EMERGENCY: Disk space critically low - free up storage"
        };
        return criticalTasks[random.nextInt(criticalTasks.length)] +
                " | Incident ID: " + random.nextInt(99999);
    }

    /**
     * Generate standard task payloads
     */
    private String generateStandardPayload() {
        String[] standardTasks = {
                "Process user registration email verification",
                "Generate daily analytics and performance report",
                "Update user preferences and notification settings",
                "Process file upload and virus scanning",
                "Send newsletter to active subscribers",
                "Validate and sanitize user input data",
                "Process password reset request with 2FA",
                "Update search index for product catalog",
                "Generate customer invoice and send notification",
                "Process subscription renewal and billing"
        };
        return standardTasks[random.nextInt(standardTasks.length)] +
                " | User: " + generateRandomData() + " | Session: " + random.nextInt(100000);
    }

    /**
     * Generate bulk task payloads (longer, less urgent)
     */
    private String generateBulkPayload() {
        String[] bulkTasks = {
                "Process batch data import from CSV file with 10K+ records",
                "Generate comprehensive monthly user activity analytics report",
                "Clean up old log files and compress archives older than 90 days",
                "Sync customer data with external CRM and backup services",
                "Process bulk email campaign to 50K+ subscribers",
                "Update and rebuild search index for entire product database",
                "Generate quarterly financial summary with detailed breakdowns",
                "Process bulk user preference updates from admin panel",
                "Archive old transaction records to cold storage",
                "Generate data warehouse ETL job for business intelligence"
        };
        return bulkTasks[random.nextInt(bulkTasks.length)] +
                " | Batch ID: " + random.nextInt(100000) +
                " | Record Count: " + (random.nextInt(50000) + 1000);
    }

    /**
     * Generate adaptive payload based on priority
     */
    private String generateAdaptivePayload(int priority) {
        if (priority >= 8) {
            return generateCriticalPayload();
        } else if (priority >= 4) {
            return generateStandardPayload();
        } else {
            return generateBulkPayload();
        }
    }

    /**
     * Generate random user data for task payloads
     */
    private String generateRandomData() {
        String[] userTypes = {"Customer", "Admin", "Guest", "Premium", "Business"};
        return userTypes[random.nextInt(userTypes.length)] + "-" + random.nextInt(10000);
    }

    /**
     * Gracefully stop the producer
     */
    public void stopProducer() {
        logger.info("Stopping producer '{}'", producerName);
        running = false;
        interrupt(); // Wake up if sleeping
    }

    /**
     * Pause the producer (can be resumed)
     */
    public void pauseProducer() {
        logger.info("Pausing producer '{}'", producerName);
        paused = true;
    }

    /**
     * Resume the producer from paused state
     */
    public void resumeProducer() {
        logger.info("Resuming producer '{}'", producerName);
        paused = false;
    }

    /**
     * Check if producer is currently running
     */
    public boolean isRunning() {
        return running && isAlive();
    }

    /**
     * Check if producer is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Get comprehensive producer statistics
     */
    public ProducerStats getStats() {
        double successRate = tasksProduced.get() > 0 ?
                (double) tasksSuccessfullySubmitted.get() / tasksProduced.get() * 100 : 0.0;

        double avgSubmissionTime = tasksProduced.get() > 0 ?
                (double) totalSubmissionTime.get() / tasksProduced.get() / 1_000_000 : 0.0; // Convert to ms

        return new ProducerStats(
                producerName,
                strategy,
                tasksProduced.get(),
                tasksSuccessfullySubmitted.get(),
                tasksRejected.get(),
                batchesProduced.get(),
                avgSubmissionTime,
                successRate,
                adaptiveDelay,
                running,
                paused
        );
    }

    /**
     * Log final statistics when producer stops
     */
    private void logFinalStats() {
        ProducerStats stats = getStats();
        logger.info("Final stats for producer '{}': {}", producerName, stats);

        if (stats.successRate < 90.0) {
            logger.warn("Producer '{}' had low success rate: {:.1f}%", producerName, stats.successRate);
        }

        if (stats.avgSubmissionTimeMs > 100.0) {
            logger.warn("Producer '{}' had high avg submission time: {:.1f}ms", producerName, stats.avgSubmissionTimeMs);
        }
    }

    /**
     * Enhanced statistics holder class
     */
    public static class ProducerStats {
        public final String name;
        public final ProducerStrategy strategy;
        public final int tasksProduced;
        public final int tasksSuccessfullySubmitted;
        public final int tasksRejected;
        public final long batchesProduced;
        public final double avgSubmissionTimeMs;
        public final double successRate;
        public final long adaptiveDelayMs;
        public final boolean running;
        public final boolean paused;

        public ProducerStats(String name, ProducerStrategy strategy,
                             int tasksProduced, int tasksSuccessfullySubmitted, int tasksRejected,
                             long batchesProduced, double avgSubmissionTimeMs, double successRate,
                             long adaptiveDelayMs, boolean running, boolean paused) {
            this.name = name;
            this.strategy = strategy;
            this.tasksProduced = tasksProduced;
            this.tasksSuccessfullySubmitted = tasksSuccessfullySubmitted;
            this.tasksRejected = tasksRejected;
            this.batchesProduced = batchesProduced;
            this.avgSubmissionTimeMs = avgSubmissionTimeMs;
            this.successRate = successRate;
            this.adaptiveDelayMs = adaptiveDelayMs;
            this.running = running;
            this.paused = paused;
        }

        @Override
        public String toString() {
            return String.format("ProducerStats{name='%s', strategy=%s, produced=%d, success=%d, rejected=%d, " +
                            "batches=%d, avgTime=%.2fms, successRate=%.1f%%, adaptiveDelay=%dms, running=%s, paused=%s}",
                    name, strategy, tasksProduced, tasksSuccessfullySubmitted, tasksRejected,
                    batchesProduced, avgSubmissionTimeMs, successRate, adaptiveDelayMs, running, paused);
        }

        /**
         * Get a formatted summary for display
         */
        public String getFormattedSummary() {
            return String.format("📊 %s: %d tasks (%.1f%% success), %d batches, %.1fms avg time",
                    name, tasksProduced, successRate, batchesProduced, avgSubmissionTimeMs);
        }
    }
}