package producer;

import controller.TaskController;

/**
 * PRODUCER: Generates low-priority background batch tasks
 * Uses LOW_PRIORITY_BULK strategy for maintenance and cleanup operations
 */
public class BackgroundProducer extends TaskProducer {

    public BackgroundProducer(TaskController taskController) {
        super("Background-Producer",
                ProducerStrategy.LOW_PRIORITY_BULK,
                taskController,
                5,    // tasksPerBatch - generate more tasks per batch for bulk processing
                7000, // batchIntervalMs - longer intervals for background processing
                100); // maxTasks - can generate many background tasks
    }
}