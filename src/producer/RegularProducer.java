package producer;

import controller.TaskController;

/**
 * PRODUCER: Generates regular business tasks
 * Uses MIXED_STEADY strategy for standard business operations
 */
public class RegularProducer extends TaskProducer {

    public RegularProducer(TaskController taskController) {
        super("Regular-Producer",
                ProducerStrategy.MIXED_STEADY,
                taskController,
                3,    // tasksPerBatch - moderate batch size
                4000, // batchIntervalMs - steady interval for regular tasks
                50);  // maxTasks - reasonable limit for regular operations
    }
}