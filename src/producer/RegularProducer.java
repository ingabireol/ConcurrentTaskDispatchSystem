package producer;
import controller.TaskController;
import model.entity.Task;

import java.util.concurrent.ThreadLocalRandom;

/**
 * PRODUCER: Generates regular business tasks
 */
public class RegularProducer extends TaskProducer {

    public RegularProducer(TaskController taskController) {
        super("Regular-Producer",
                ProducerStrategy.MIXED_STEADY,
                taskController,
                3,    // tasksPerBatch
                4000, // batchIntervalMs
                50);  // maxTasks
    }

    @Override
    protected void generateTasks() throws InterruptedException {
        // Generate 2-4 regular tasks
        int numTasks = ThreadLocalRandom.current().nextInt(2, 5);

        for (int i = 0; i < numTasks; i++) {
            int taskNum = tasksProduced.get() + 1;
            int priority = ThreadLocalRandom.current().nextInt(4, 8); // Priority 4-7

            String taskName = String.format("%s-STD-%d", producerName, taskNum);
            String payload = String.format("Standard business processing for request #%d", taskNum);

            Task task = new Task(taskName, priority, payload, producerName);

            boolean submitted = taskController.submitTask(task);
            if (submitted) {
                tasksSuccessfullySubmitted.incrementAndGet();
                System.out.printf("📤 [%s] Submitted %s%n", producerName, task);
            } else {
                tasksRejected.incrementAndGet();
                System.out.printf("❌ [%s] Rejected %s%n", producerName, task);
            }

            tasksProduced.incrementAndGet();
        }
    }
}
