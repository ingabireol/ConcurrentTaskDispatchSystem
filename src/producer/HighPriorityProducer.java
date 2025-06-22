package producer;

import controller.TaskController;

/**
 * PRODUCER: Generates high-priority urgent tasks
 * Uses HIGH_PRIORITY_BURST strategy for critical operations
 */
public class HighPriorityProducer extends TaskProducer {

    public HighPriorityProducer(TaskController taskController) {
        super("HighPriority-Producer",
                ProducerStrategy.HIGH_PRIORITY_BURST,
                taskController,
                2,    // tasksPerBatch - fewer tasks per batch but high priority
                3000, // batchIntervalMs - frequent submissions for urgent tasks
                30);  // maxTasks - limited number of high priority tasks
    }
}